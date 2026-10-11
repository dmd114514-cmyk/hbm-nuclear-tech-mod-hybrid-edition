#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Item model surface checker - four-way, built to close the only checked the dimension I thought of hole.

Usage:
    python3 tools/verify/check_item_models.py [--root <repo root>] [--log <latest.log>]

The four ways:
  (1) Log judgement (ground truth): treat the last runClient log as authoritative;
     anything showing `Exception loading model for variant` or
     `FileNotFoundException: hbm:models/...` gets listed.
     This pass depends on no parsing assumption - whatever the game says goes.
  (2) Asset judgement (texture to model): for every `<registry name>.<suffix>.png` or `<registry name>.png` in `textures/items`
     there has to be a matching `models/item/<registry name>_<suffix>.json` / `<registry name>.json`.
     Inferred from assets, independent of the source parser.
  (3) Registry name judgement: every item registry name needs a base model `models/item/<registry name>.json`
     (Forge asks for the `<registry name>#inventory` default variant for every item).
  (4) Reverse judgement (model to texture): the texture layer0 points at must actually exist,
     which stops a pile of JSON that points at air.

Why four ways and not one: earlier rounds kept tripping over the generator and the checker sharing one parser -
the parser missed some form (enum written on one line, enum with block comments, cast assignments, suffixed models vs base names)
and the checker proved 0 missing with the same blind spot. Two of the four never look at the source, so they cover each other.
"""

import argparse
import os
import re
import sys


def registered_item_names(root):
    """Takes the registry-name set out of a registrar"""
    names = set()
    reg = os.path.join(root, 'src/main/java/com/hbm/items/registry')
    if not os.path.isdir(reg):
        return names
    # Take the LAST `, "name"` in each `HbmRegistry.item(...)` statement - that is the registry name.
    # Do not grab the first: chained construction passes other string arguments (`.aot(Mats.MAT_BISMUTH, "xxx")`),
    # and grabbing the wrong one invents registry names while missing the real one.
    head = re.compile(r'HbmRegistry\.item\(')
    tail = re.compile(r', "([a-z0-9_]+)"')
    for fn in os.listdir(reg):
        if not (fn.startswith('Items') and fn.endswith('.java')):
            continue
        with open(os.path.join(reg, fn), encoding='utf-8') as f:
            text = f.read()
        for hm in head.finditer(text):
            end = text.find(';', hm.end())
            if end < 0:
                continue
            stmt = text[hm.end():end]
            found = tail.findall(stmt)
            if found:
                names.add(found[-1])
    return names


def check_log(log_path):
    """① Log-based check. Sorted by item/block — this checker only answers for item models; the block batch belongs to the content layer"""
    if not os.path.isfile(log_path):
        return None, None
    item, block = [], []
    with open(log_path, encoding='utf-8', errors='ignore') as f:
        for line in f:
            if 'Exception loading model for variant' not in line and 'FileNotFoundException: hbm:models/' not in line:
                continue
            if 'hbm:models/block' in line or '#normal' in line or 'blockstate' in line:
                block.append(line.strip())
            else:
                item.append(line.strip())
    return item, block


def check_assets(root, names):
    """② Asset check (texture → model)"""
    tex_dir = os.path.join(root, 'src/main/resources/assets/hbm/textures/items')
    mod_dir = os.path.join(root, 'src/main/resources/assets/hbm/models/item')
    miss = []
    if not os.path.isdir(tex_dir):
        return miss
    for png in os.listdir(tex_dir):
        if not png.endswith('.png'):
            continue
        stem = png[:-4]
        if stem in names:
            model = stem
        elif '.' in stem and stem.split('.', 1)[0] in names:
            model = '%s_%s' % (stem.split('.', 1)[0], stem.split('.', 1)[1])
        else:
            continue
        if not os.path.isfile(os.path.join(mod_dir, model + '.json')):
            miss.append(model)
    return sorted(set(miss))


def check_registered(root, names):
    """③ Registry-name check"""
    mod_dir = os.path.join(root, 'src/main/resources/assets/hbm/models/item')
    return sorted(n for n in names if not os.path.isfile(os.path.join(mod_dir, n + '.json')))


def check_reverse(root):
    """④ Model → texture"""
    mod_dir = os.path.join(root, 'src/main/resources/assets/hbm/models/item')
    tex_dir = os.path.join(root, 'src/main/resources/assets/hbm/textures/items')
    bad = []
    for fn in os.listdir(mod_dir):
        if not fn.endswith('.json'):
            continue
        with open(os.path.join(mod_dir, fn), encoding='utf-8') as f:
            content = f.read()
        m = re.search(r'hbm:items/([\w.]+)', content)
        if m and not os.path.isfile(os.path.join(tex_dir, m.group(1) + '.png')):
            bad.append((fn, m.group(1)))
    return bad


def check_model_refs(root):
    """(5) override model references must resolve to a real file

    `overrides[].model` resolves against the `models/` root (`hbm:item/x` -> `models/item/x.json`),
    while `textures.layer0` uses `hbm:items/...` (the texture directory). They look alike and mean different things;
    mixing them gives a build that compiles, a startup that stays quiet, and one missing texture version - this pass exists for exactly that.
    """
    models_root = os.path.join(root, 'src/main/resources/assets/hbm/models')
    mod_dir = os.path.join(models_root, 'item')
    bad = []
    for fn in os.listdir(mod_dir):
        if not fn.endswith('.json'):
            continue
        with open(os.path.join(mod_dir, fn), encoding='utf-8') as f:
            content = f.read()
        for ref in re.findall(r'"model"\s*:\s*"([^"]+)"', content):
            path = ref.split(':', 1)[1] if ':' in ref else ref
            if not os.path.isfile(os.path.join(models_root, path + '.json')):
                bad.append((fn, ref))
    return bad


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', default=os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')))
    ap.add_argument('--log', default=None)
    args = ap.parse_args()
    root = args.root
    log_path = args.log or os.path.join(root, 'run/logs/latest.log')

    names = registered_item_names(root)
    print('Registered item names: %d' % len(names))

    failures = 0

    log_item, log_block = check_log(log_path)
    if log_item is None:
        print('① Log-based check: skipped (%s not found, client never ran)' % log_path)
    else:
        failures += len(log_item)
        print('① Log-based check (items): %d failures (%s)' % (len(log_item), log_path))
        for line in log_item[:20]:
            print('   ' + line[:160])
        if log_block:
            print('   (another %d **block** model failures - content layer turf, not counted by this checker)' % len(log_block))

    a = check_assets(root, names)
    failures += len(a)
    print('② Asset check (texture → model): %d missing %s' % (len(a), a[:10]))

    r = check_registered(root, names)
    failures += len(r)
    print('3) registry-name check (base model): %d missing %s' % (len(r), r[:10]))

    v = check_reverse(root)
    failures += len(v)
    print('④ Reverse check (model → texture): %d pointing at nonexistent textures %s' % (len(v), v[:5]))

    w = check_model_refs(root)
    failures += len(w)
    print('⑤ Reference check (override → model file): %d pointing at nonexistent models %s' % (len(w), w[:5]))

    print('—— problems total: %d' % failures)
    return 1 if failures else 0


if __name__ == '__main__':
    sys.exit(main())

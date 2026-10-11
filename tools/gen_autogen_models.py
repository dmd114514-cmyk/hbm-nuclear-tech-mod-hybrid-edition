#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generates per-material model JSON for the ItemAutogen family.

Why a script is needed: those items' meta is a material id, one texture per material,
and the texture is remapped from the base art using the material's colour at load time (not a ready-made file), so a model has to exist per meta.
Both the model and sprite names use the material id (`<item>_<id>` / `hbm:items/<item>-<id>`),
because in `Mats.java` the id is arithmetic over `_VS/_AS/_ES/_EX + NN` and can be computed offline;
while material names hide in another class's DictFrame constants, which the script has no reason to dig into.

Usage:
    python3 tools/gen_autogen_models.py [--root <repo root>] [--dry-run]

Data sources (all parsed, nothing downloaded):
  - item list: built into this script, one to one with the 16 `new ItemAutogen(MaterialShapes.X)` calls in ModItems;
  - material table: `setAutogen(...)` and the id arithmetic in `com/hbm/inventory/material/Mats.java`.
"""

import argparse
import json
import os
import re
import sys

# item registry name -> (shape, {material constant with an overridden texture: texture name})
ITEMS = {
    'bolt': ('BOLT', {}),
    'bedrock_ore_fragment': ('FRAGMENT', {'MAT_BISMUTH': 'bedrock_ore_fragment_bismuth'}),
    'shell': ('SHELL', {}),
    'pipe': ('PIPE', {}),
    'ingot_raw': ('INGOT', {}),
    'plate_cast': ('CASTPLATE', {'MAT_BISMUTH': 'plate_cast_bismuth'}),
    'plate_welded': ('WELDEDPLATE', {}),
    'wire_fine': ('WIRE', {
        'MAT_ALUMINIUM': 'wire_aluminium', 'MAT_COPPER': 'wire_copper',
        'MAT_MINGRADE': 'wire_red_copper', 'MAT_GOLD': 'wire_gold',
        'MAT_TUNGSTEN': 'wire_tungsten', 'MAT_CARBON': 'wire_carbon',
        'MAT_SCHRABIDIUM': 'wire_schrabidium', 'MAT_MAGTUNG': 'wire_magnetized_tungsten',
        'MAT_ZINC': 'wire_zinc',
    }),
    'wire_dense': ('DENSEWIRE', {}),
    'part_barrel_light': ('LIGHTBARREL', {}),
    'part_barrel_heavy': ('HEAVYBARREL', {}),
    'part_receiver_light': ('LIGHTRECEIVER', {}),
    'part_receiver_heavy': ('HEAVYRECEIVER', {}),
    'part_mechanism': ('MECHANISM', {}),
    'part_stock': ('STOCK', {}),
    'part_grip': ('GRIP', {}),
}

BASES = {'_VS': 0, '_AS': 30, '_ES': 20000, '_EX': 24000}
RE_MAT = re.compile(
    r'public static final NTMMaterial (\w+)\s*=\s*\w+\(\s*([^,]+?)\s*,(.*?)\)\s*((?:\.\w+\([^;]*?\))*)\s*;')


def parse_int(expr):
    """Evaluates expressions like `_ES + 05` / `24_000` / `0x1F`; returns None when it can't"""
    expr = expr.strip()
    m = re.match(r'^(?:_(VS|AS|ES|EX))\s*\+\s*(.+)$', expr)
    if m:
        base = BASES['_' + m.group(1)]
        rest = m.group(2).strip()
    elif re.match(r'^[\d_]+$', expr) or expr.startswith('0x'):
        base, rest = 0, expr
    else:
        return None
    rest = rest.replace('_', '')
    try:
        return base + (int(rest, 16) if rest.startswith('0x') else int(rest))
    except ValueError:
        return None


def parse_mats(root):
    """Returns [(constant name, id, set of shape names in setAutogen)]"""
    path = os.path.join(root, 'src/main/java/com/hbm/inventory/material/Mats.java')
    text = open(path, encoding='utf-8').read()
    out = []
    for m in RE_MAT.finditer(text):
        name, id_expr, _colors, chain = m.group(1), m.group(2), m.group(3), m.group(4)
        mid = parse_int(id_expr)
        if mid is None:
            continue
        shapes = set()
        for sm in re.finditer(r'setAutogen\(([^)]*)\)', chain):
            for s in sm.group(1).split(','):
                s = s.strip()
                if s:
                    shapes.add(s)
        out.append((name, mid, shapes))
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', default=os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
    ap.add_argument('--dry-run', action='store_true')
    args = ap.parse_args()
    root = args.root
    out_dir = os.path.join(root, 'src/main/resources/assets/hbm/models/item')

    mats = parse_mats(root)
    if not mats:
        print('Material table not parsed; check whether the Mats.java layout changed')
        return 1

    total = 0
    for item, (shape, overrides) in sorted(ITEMS.items()):
        count = 0
        for name, mid, shapes in mats:
            if shape not in shapes:
                continue
            count += 1
            tex = overrides.get(name)
            layer0 = 'hbm:items/%s' % tex if tex else 'hbm:items/%s-%d' % (item, mid)
            model = os.path.join(out_dir, '%s_%d.json' % (item, mid))
            if args.dry_run:
                continue
            with open(model, 'w', encoding='utf-8') as f:
                json.dump({'parent': 'item/generated', 'textures': {'layer0': layer0}},
                          f, ensure_ascii=False, indent=2)
                f.write('\n')
        total += count
        print('  %-24s %-14s %3d metas' % (item, shape, count))
    print('%d models total%s' % (total, '(dry-run, nothing written)' if args.dry_run else ' written'))
    return 0


if __name__ == '__main__':
    sys.exit(main())

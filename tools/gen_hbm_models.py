#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Turns 1.7.10 HBM's IIcon texture scheme into 1.12.2 blockstates/models JSON.

In 1.7.10 a block or item's texture was bound implicitly by registerIcon("hbm:X") and only known at runtime;
1.12.2 needs static JSON. This script digs the registry-name to texture-name pairs out of the sources,
generates everything it can, and reports the rest by category so a human only handles those batches.

Usage:
  gen_hbm_models.py --src <com/hbm source dir> --assets <source assets/hbm> --out <target resources> [--dry-run]
"""

import argparse
import json
import os
import re
import sys
from collections import defaultdict

# Chained calls are one statement per line, so splitting on ';' pairs setBlockName up with setBlockTextureName
RE_STMT_SPLIT = re.compile(r';')

RE_SET_BLOCK_NAME = re.compile(r'\.setBlockName\s*\(\s*"([^"]+)"\s*\)')
RE_SET_UNLOC_BLOCK = re.compile(r'\.setUnlocalizedName\s*\(\s*"([^"]+)"\s*\)')
# Note: the sources use all three forms - "hbm:name", "name", RefStrings.MODID + ":name",
# so grab the whole literal first and strip the domain in strip_domain(); do not expect one regex to do it all.
RE_SET_BLOCK_TEX = re.compile(r'\.setBlockTextureName\s*\(\s*(?:RefStrings\.MODID\s*\+\s*)?"([^"]+)"\s*\)')
RE_SET_TEX = re.compile(r'\.setTextureName\s*\(\s*(?:RefStrings\.MODID\s*\+\s*)?"([^"]+)"\s*\)')
# calls registerIcon(...) directly, mostly custom blocks' registerBlockIcons
RE_REGISTER_ICON = re.compile(r'\.registerIcon\s*\(\s*(?:RefStrings\.MODID\s*\+\s*)?"([^"]+)"\s*\)')
RE_REGISTER_ICON_MULTI = re.compile(r'\.registerIcon\s*\(\s*(?:RefStrings\.MODID\s*\+\s*)?"([^"]+)"\s*\+')


# fake multiblocks that come out as JSON models (the rail family): names get added per family as they are ported
RENDER_JSON = set(['rail_large_straight', 'rail_large_straight_short', 'rail_narrow_straight',
                  'rail_large_ramp', 'rail_large_buffer', 'rail_large_switch', 'rail_large_switch_flipped'])

# the rail family using vanilla shapes (wood / narrow / high-speed / fueled): the property is BlockRail's SHAPE and all ten values must be listed
RAIL_SHAPE = set(['rail_wood', 'rail_narrow', 'rail_highspeed', 'rail_booster'])
# (shape key, which vanilla model, y rotation) - lined up entry by entry with vanilla's blockstates/rail.json
RAIL_VARIANTS = [('north_south', 'flat', 0), ('east_west', 'flat', 90),
                 ('ascending_north', 'raised_ne', 0), ('ascending_south', 'raised_sw', 0),
                 ('ascending_east', 'raised_ne', 90), ('ascending_west', 'raised_sw', 90),
                 ('south_east', 'curved', 0), ('south_west', 'curved', 90),
                 ('north_west', 'curved', 180), ('north_east', 'curved', 270)]


def strip_domain(s):
    """'hbm:ore_iron' / ':ore_iron' / 'ore_iron' → 'ore_iron'。"""
    s = s.strip()
    if ':' in s:
        s = s.rsplit(':', 1)[1]
    return s.lower()

# Registry names lowercased throughout (1.12.2's ResourceLocation forces lowercase)
VALID_NAME = re.compile(r'^[a-z0-9_]+$')


def norm(name):
    return name.strip().lower()


def split_statements(text):
    """Splits source into statements on ';', folding newlines into spaces for whole-sentence regexes."""
    flat = text.replace('\r', ' ').replace('\n', ' ')
    return [s for s in RE_STMT_SPLIT.split(flat) if s.strip()]


def scan_sources(src_root, new_root=None):
    """Returns (blocks, items, multi, custom_icon).

    custom_icon is for block classes that have their own registerBlockIcons/getIcon logic but do not extend BlockEnumMulti.
    The texture-name to meta relationship in those classes cannot be pulled out with a regex (multi-meta, per-face, random per coordinate),
    so an auto-generated single-state blockstate is very likely not enough - a human has to check.
    """
    blocks = {}
    items = {}
    multi = set()
    custom_icon = set()
    cls_of = {}      # block name -> the class declaring it
    parents = {}     # class name -> parent class (used to tell fake multiblocks apart)

    for dirpath, _dirnames, filenames in os.walk(src_root):
        for fn in filenames:
            if not fn.endswith('.java'):
                continue
            path = os.path.join(dirpath, fn)
            try:
                with open(path, 'r', encoding='utf-8', errors='replace') as fh:
                    text = fh.read()
            except OSError:
                continue

            for stmt in split_statements(text):
                m_name = RE_SET_BLOCK_NAME.search(stmt)
                if m_name:
                    name = norm(m_name.group(1))
                    if not VALID_NAME.match(name):
                        continue
                    m_tex = RE_SET_BLOCK_TEX.search(stmt)
                    blocks[name] = strip_domain(m_tex.group(1)) if m_tex else name
                    m_new = re.search(r"new\s+(\w+)\s*\(", stmt)
                    if m_new:
                        cls_of[name] = m_new.group(1)
                    continue

                # Items: setUnlocalizedName + setTextureName; the two are not always the same name (butter_sword to budder_sword, say)
                m_unloc = RE_SET_UNLOC_BLOCK.search(stmt)
                if m_unloc:
                    name = norm(m_unloc.group(1))
                    if not VALID_NAME.match(name):
                        continue
                    m_tex = RE_SET_TEX.search(stmt)
                    if m_tex:
                        items[name] = strip_domain(m_tex.group(1))
                    # No explicit texture (mostly subclasses carrying their own): note the class's own name for now, decide later by whether the file exists
                    else:
                        items.setdefault(name, name)
                    continue

            m_ext = re.search(r"class\s+(\w+)\s+extends\s+(\w+)", text)
            if m_ext:
                parents[m_ext.group(1)] = m_ext.group(2)

            # Custom icon logic: BlockEnumMulti is the only kind that can be automated, everything else needs a human
            if ('registerBlockIcons' in text or re.search(r'public\s+IIcon\s+getIcon', text)) \
                    and 'extends BlockEnumMulti' not in text and 'IBlockMulti' not in text:
                m_cls = re.search(r'(?:public\s+)?(?:abstract\s+)?class\s+(\w+)', text)
                if m_cls:
                    custom_icon.add(m_cls.group(1))

            # Custom registerBlockIcons: anything with string concatenation counts as multi-variant and is flagged for a human
            if '.registerIcon' in text:
                for m in RE_REGISTER_ICON_MULTI.finditer(text):
                    multi.add(strip_domain(m.group(1)))
                for m in RE_REGISTER_ICON.finditer(text):
                    multi.add(strip_domain(m.group(1)))

    if new_root and os.path.isdir(new_root):
        for dirpath, _dirnames, filenames in os.walk(new_root):
            for fn in filenames:
                if not fn.endswith('.java'):
                    continue
                text = ''
                try:
                    with open(os.path.join(dirpath, fn), 'r', encoding='utf-8', errors='replace') as fh:
                        text = fh.read()
                except OSError:
                    continue
                m_ext = re.search(r"class\s+(\w+)\s+extends\s+(\w+)", text)
                if m_ext:
                    parents[m_ext.group(1)] = m_ext.group(2)


    # Fake multiblocks: anything whose inheritance chain reaches BlockDummyable counts. It carries a 0-15 meta property,
    # and the normal key in a blockstate only covers blocks with no properties; blocks with properties must list every value,
    # otherwise you get MissingVariantException in practice (the log shows the first few, the rest come back suppressed).
    def reaches(cls):
        seen = set()
        while cls in parents and cls not in seen:
            seen.add(cls)
            if cls == 'BlockDummyable' or cls == 'BlockMetaBase':
                return True
            cls = parents[cls]
        return False

    dummy = set(n for n, c in cls_of.items() if c and reaches(c))

    return blocks, items, multi, custom_icon, dummy, cls_of


def state_keys(name, model, dummy):
    """blockstate variant key: a plain block needs one normal entry;
    Fake multiblocks carry a meta=0-15 property and every value has to be listed, or not one property state matches (measured: MissingVariantException)."""
    if name in RENDER_JSON:
        # fake multiblocks drawn from JSON: only the four real-part states should draw a model, placeholder states point at an empty model,
        # otherwise one machine draws the same thing in all five cells (the old version blocked that with an if(meta < 12) return in renderWorld)
        return dict(('meta=%d' % i, {'model': 'hbm:%s' % model if i >= 12 else 'hbm:hidden'}) for i in range(16))
    if name in dummy:
        return dict(('meta=%d' % i, {'model': 'hbm:%s' % model}) for i in range(16))
    return {'normal': {'model': 'hbm:%s' % model}}


def texture_exists(assets, kind, tex):
    return os.path.isfile(os.path.join(assets, 'textures', kind, tex + '.png'))


def emit_rail_shape(assets, out_assets, name, tex):
    """Uses the vanilla rail_* templates and just plugs its own texture in.

    The old version drew a slope from a separate _turned icon at meta>=6; here the two raised shapes use it (falling back to the base texture when there is no _turned).
    """
    turned = '%s_turned' % tex if texture_exists(assets, 'blocks', '%s_turned' % tex) else tex

    for suffix, parent, used in [('flat', 'rail_flat', tex), ('curved', 'rail_curved', tex),
                                 ('raised_ne', 'rail_raised_ne', turned), ('raised_sw', 'rail_raised_sw', turned)]:
        write_json(os.path.join(out_assets, 'models', 'block', '%s_%s.json' % (tex, suffix)),
                   {'parent': 'minecraft:block/%s' % parent, 'textures': {'rail': 'hbm:blocks/%s' % used}})

    variants = {}
    for shape, kind, y in RAIL_VARIANTS:
        entry = {'model': 'hbm:%s_%s' % (tex, kind)}
        if y:
            entry['y'] = y
        variants['shape=' + shape] = entry

    write_json(os.path.join(out_assets, 'blockstates', '%s.json' % name), {'variants': variants})
    write_json(os.path.join(out_assets, 'models', 'item', '%s.json' % name), {'parent': 'hbm:block/%s_flat' % tex})


# -- block families that use vanilla shapes: fence / fence gate / glass pane / slab / stairs / trapdoor / ladder --
# A generic cube_all draws them as solid cubes (the ladder case was actually reported by a user). The variant tables below are copied entry by entry from
# vanilla's client.jar blockstates in the cache (ladder / fence / birch_fence_gate / glass_pane /
# stone_slab / oak_stairs / trapdoor), with only the model names swapped for ours.
# Property sets were checked against the vanilla classes too: BlockLadder(FACING), BlockFenceGate(OPEN/POWERED/IN_WALL),
# BlockPane(NORTH/EAST/SOUTH/WEST)、BlockSlab(HALF)、BlockStairs(FACING/HALF/SHAPE)、
# BlockTrapDoor(FACING/OPEN/HALF). Re-check the property set when you port these blocks - the table follows the vanilla convention.
SHAPE_BY_CLASS = [('FenceGate', 'fence_gate'), ('Fence', 'fence'), ('GlassPane', 'pane'),
                  ('Slab', 'slab'), ('Stairs', 'stairs'), ('Trapdoor', 'trapdoor'), ('Ladder', 'ladder')]
SHAPE_PARENTS = {
    'ladder':     {'ladder': 'block/ladder'},
    'fence':      {'post': 'block/fence_post', 'side': 'block/fence_side'},
    'fence_gate': {'gate_closed': 'block/fence_gate_closed', 'gate_open': 'block/fence_gate_open'},
    'pane':       {'post': 'block/pane_post', 'side': 'block/pane_side', 'side_alt': 'block/pane_side_alt',
                   'noside': 'block/pane_noside', 'noside_alt': 'block/pane_noside_alt'},
    'slab':       {'half': 'block/half_slab', 'upper': 'block/upper_slab'},
    'stairs':     {'straight': 'block/stairs', 'inner': 'block/inner_stairs', 'outer': 'block/outer_stairs'},
    'trapdoor':   {'bottom': 'block/trapdoor_bottom', 'top': 'block/trapdoor_top', 'open': 'block/trapdoor_open'},
}
SHAPE_TEXKEYS = {
    'ladder':     {'texture': '{Tex}'},
    'fence':      {'texture': '{Tex}'},
    'fence_gate': {'texture': '{Tex}'},
    'pane':       {'pane': '{Tex}', 'edge': '{Edge}'},
    'slab':       {'bottom': '{Tex}', 'top': '{Tex}', 'side': '{Tex}'},
    'stairs':     {'bottom': '{Tex}', 'top': '{Tex}', 'side': '{Tex}'},
    'trapdoor':   {'texture': '{Tex}'},
}
# which model the inventory shows: take the most front-facing one in the family
SHAPE_ITEM_ROLE = {'ladder': 'ladder', 'fence': 'post', 'fence_gate': 'gate_closed', 'pane': 'post',
                   'slab': 'half', 'stairs': 'straight', 'trapdoor': 'bottom'}
# (variant key, role, x, y, uvlock)
SHAPE_VARIANTS = {
    'ladder': [
    ('facing=north', 'ladder', 0, 0, False),
    ('facing=east', 'ladder', 0, 90, False),
    ('facing=south', 'ladder', 0, 180, False),
    ('facing=west', 'ladder', 0, 270, False),
    ],
    'fence_gate': [
    ('facing=south,in_wall=false,open=false', 'gate_closed', 0, 0, True),
    ('facing=west,in_wall=false,open=false', 'gate_closed', 0, 90, True),
    ('facing=north,in_wall=false,open=false', 'gate_closed', 0, 180, True),
    ('facing=east,in_wall=false,open=false', 'gate_closed', 0, 270, True),
    ('facing=south,in_wall=false,open=true', 'gate_open', 0, 0, True),
    ('facing=west,in_wall=false,open=true', 'gate_open', 0, 90, True),
    ('facing=north,in_wall=false,open=true', 'gate_open', 0, 180, True),
    ('facing=east,in_wall=false,open=true', 'gate_open', 0, 270, True),
    ('facing=south,in_wall=true,open=false', 'gate_closed', 0, 0, True),
    ('facing=west,in_wall=true,open=false', 'gate_closed', 0, 90, True),
    ('facing=north,in_wall=true,open=false', 'gate_closed', 0, 180, True),
    ('facing=east,in_wall=true,open=false', 'gate_closed', 0, 270, True),
    ('facing=south,in_wall=true,open=true', 'gate_open', 0, 0, True),
    ('facing=west,in_wall=true,open=true', 'gate_open', 0, 90, True),
    ('facing=north,in_wall=true,open=true', 'gate_open', 0, 180, True),
    ('facing=east,in_wall=true,open=true', 'gate_open', 0, 270, True),
    ],
    'slab': [
    ('half=bottom', 'half', 0, 0, False),
    ('half=top', 'upper', 0, 0, False),
    ],
    'stairs': [
    ('facing=east,half=bottom,shape=straight', 'straight', 0, 0, False),
    ('facing=west,half=bottom,shape=straight', 'straight', 0, 180, True),
    ('facing=south,half=bottom,shape=straight', 'straight', 0, 90, True),
    ('facing=north,half=bottom,shape=straight', 'straight', 0, 270, True),
    ('facing=east,half=bottom,shape=outer_right', 'outer', 0, 0, False),
    ('facing=west,half=bottom,shape=outer_right', 'outer', 0, 180, True),
    ('facing=south,half=bottom,shape=outer_right', 'outer', 0, 90, True),
    ('facing=north,half=bottom,shape=outer_right', 'outer', 0, 270, True),
    ('facing=east,half=bottom,shape=outer_left', 'outer', 0, 270, True),
    ('facing=west,half=bottom,shape=outer_left', 'outer', 0, 90, True),
    ('facing=south,half=bottom,shape=outer_left', 'outer', 0, 0, False),
    ('facing=north,half=bottom,shape=outer_left', 'outer', 0, 180, True),
    ('facing=east,half=bottom,shape=inner_right', 'inner', 0, 0, False),
    ('facing=west,half=bottom,shape=inner_right', 'inner', 0, 180, True),
    ('facing=south,half=bottom,shape=inner_right', 'inner', 0, 90, True),
    ('facing=north,half=bottom,shape=inner_right', 'inner', 0, 270, True),
    ('facing=east,half=bottom,shape=inner_left', 'inner', 0, 270, True),
    ('facing=west,half=bottom,shape=inner_left', 'inner', 0, 90, True),
    ('facing=south,half=bottom,shape=inner_left', 'inner', 0, 0, False),
    ('facing=north,half=bottom,shape=inner_left', 'inner', 0, 180, True),
    ('facing=east,half=top,shape=straight', 'straight', 180, 0, True),
    ('facing=west,half=top,shape=straight', 'straight', 180, 180, True),
    ('facing=south,half=top,shape=straight', 'straight', 180, 90, True),
    ('facing=north,half=top,shape=straight', 'straight', 180, 270, True),
    ('facing=east,half=top,shape=outer_right', 'outer', 180, 90, True),
    ('facing=west,half=top,shape=outer_right', 'outer', 180, 270, True),
    ('facing=south,half=top,shape=outer_right', 'outer', 180, 180, True),
    ('facing=north,half=top,shape=outer_right', 'outer', 180, 0, True),
    ('facing=east,half=top,shape=outer_left', 'outer', 180, 0, True),
    ('facing=west,half=top,shape=outer_left', 'outer', 180, 180, True),
    ('facing=south,half=top,shape=outer_left', 'outer', 180, 90, True),
    ('facing=north,half=top,shape=outer_left', 'outer', 180, 270, True),
    ('facing=east,half=top,shape=inner_right', 'inner', 180, 90, True),
    ('facing=west,half=top,shape=inner_right', 'inner', 180, 270, True),
    ('facing=south,half=top,shape=inner_right', 'inner', 180, 180, True),
    ('facing=north,half=top,shape=inner_right', 'inner', 180, 0, True),
    ('facing=east,half=top,shape=inner_left', 'inner', 180, 0, True),
    ('facing=west,half=top,shape=inner_left', 'inner', 180, 180, True),
    ('facing=south,half=top,shape=inner_left', 'inner', 180, 90, True),
    ('facing=north,half=top,shape=inner_left', 'inner', 180, 270, True),
    ],
    'trapdoor': [
    ('facing=north,half=bottom,open=false', 'bottom', 0, 0, False),
    ('facing=south,half=bottom,open=false', 'bottom', 0, 0, False),
    ('facing=east,half=bottom,open=false', 'bottom', 0, 0, False),
    ('facing=west,half=bottom,open=false', 'bottom', 0, 0, False),
    ('facing=north,half=top,open=false', 'top', 0, 0, False),
    ('facing=south,half=top,open=false', 'top', 0, 0, False),
    ('facing=east,half=top,open=false', 'top', 0, 0, False),
    ('facing=west,half=top,open=false', 'top', 0, 0, False),
    ('facing=north,half=bottom,open=true', 'open', 0, 0, False),
    ('facing=south,half=bottom,open=true', 'open', 0, 180, False),
    ('facing=east,half=bottom,open=true', 'open', 0, 90, False),
    ('facing=west,half=bottom,open=true', 'open', 0, 270, False),
    ('facing=north,half=top,open=true', 'open', 0, 0, False),
    ('facing=south,half=top,open=true', 'open', 0, 180, False),
    ('facing=east,half=top,open=true', 'open', 0, 90, False),
    ('facing=west,half=top,open=true', 'open', 0, 270, False),
    ],
}
# multipart rules: [(when or None, role, y, uvlock)]
SHAPE_MULTIPART = {
    'fence': [
    (None, 'post', 0, False),
    ({'north': 'true'}, 'side', 0, True),
    ({'east': 'true'}, 'side', 90, True),
    ({'south': 'true'}, 'side', 180, True),
    ({'west': 'true'}, 'side', 270, True),
    ],
    'pane': [
    (None, 'post', 0, False),
    ({'north': True}, 'side', 0, False),
    ({'east': True}, 'side', 90, False),
    ({'south': True}, 'side_alt', 0, False),
    ({'west': True}, 'side_alt', 90, False),
    ({'north': False}, 'noside', 0, False),
    ({'east': False}, 'noside_alt', 0, False),
    ({'south': False}, 'noside_alt', 90, False),
    ({'west': False}, 'noside', 270, False),
    ],
}


def shape_of(name, cls):
    """Judges the shape family by block/class name; returning None continues down the generic branch."""
    if 'ladder' in name:
        return 'ladder'
    for needle, kind in SHAPE_BY_CLASS:
        if needle in (cls or ''):
            return kind
    return None


def emit_shape(assets, out_assets, kind, name, tex):
    """Draws the look the vanilla way: first the model family (one per form), then the blockstate (ordinary variants or multipart)."""
    edge = tex + '_edge' if texture_exists(assets, 'blocks', tex + '_edge') else tex
    scope = {'Tex': tex, 'Edge': edge}

    for role, parent in SHAPE_PARENTS[kind].items():
        textures = dict((k, 'hbm:blocks/' + v.format(**scope)) for k, v in SHAPE_TEXKEYS[kind].items())
        write_json(os.path.join(out_assets, 'models', 'block', tex + '_' + role + '.json'),
                   {'parent': parent, 'textures': textures})

    if kind in SHAPE_MULTIPART:
        rules = []
        for when, role, y, uvlock in SHAPE_MULTIPART[kind]:
            apply = {'model': 'hbm:' + tex + '_' + role}
            if y: apply['y'] = y
            if uvlock: apply['uvlock'] = True
            rule = {'apply': apply}
            if when: rule['when'] = when
            rules.append(rule)
        write_json(os.path.join(out_assets, 'blockstates', name + '.json'), {'multipart': rules})
    else:
        variants = {}
        for key, role, x, y, uvlock in SHAPE_VARIANTS[kind]:
            entry = {'model': 'hbm:' + tex + '_' + role}
            if x: entry['x'] = x
            if y: entry['y'] = y
            if uvlock: entry['uvlock'] = True
            variants[key] = entry
        write_json(os.path.join(out_assets, 'blockstates', name + '.json'), {'variants': variants})

    write_json(os.path.join(out_assets, 'models', 'item', name + '.json'),
               {'parent': 'hbm:block/' + tex + '_' + SHAPE_ITEM_ROLE[kind]})


def variants_for(assets, base):
    """Finds <base>.<variant>.png variant textures and returns the variant names.

    Strip .png before comparing the prefix, or 'foo.png' looks like a variant of 'foo.' (that dot is the extension).
    """
    d = os.path.join(assets, 'textures', 'blocks')
    if not os.path.isdir(d):
        return []
    prefix = base + '.'
    out = []
    for fn in os.listdir(d):
        if not fn.endswith('.png'):
            continue
        stem = fn[:-4]
        if stem.startswith(prefix):
            out.append(stem[len(prefix):])
    return sorted(out)


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as fh:
        json.dump(obj, fh, indent=2, ensure_ascii=False)
        fh.write('\n')


def face_textures(assets, base):
    """Detects per-face textures like <base>_side / _top / _bottom.

    HBM uses this naming a lot for wood, ground and machine casings, and it is written into constructor arguments,
    so no regex gets it - it has to be inferred from the texture files. Returns the cube_bottom_top textures, or None.
    """
    side = base + '_side'
    top = base + '_top'
    bottom = base + '_bottom'
    if not (texture_exists(assets, 'blocks', side) and texture_exists(assets, 'blocks', top)):
        return None
    tex = {'side': 'hbm:blocks/' + side, 'top': 'hbm:blocks/' + top}
    tex['bottom'] = 'hbm:blocks/' + bottom if texture_exists(assets, 'blocks', bottom) else 'hbm:blocks/' + top
    return tex


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--src', required=True, help='com/hbm source root')
    ap.add_argument('--assets', required=True, help='source assets/hbm directory (textures are read from here)')
    ap.add_argument('--out', required=True, help='target src/main/resources directory')
    ap.add_argument('--dry-run', action='store_true', help='Report only, writes no files')
    ap.add_argument('--emit-variants', action='store_true', help='Force variant-multiplex blockstate output (default only reports, since the mapping needs enum info)')
    ap.add_argument('--new-src', help='This version (1.12.2) com/hbm source root, used to identify meta-carrying blocks by the **ported** inheritance tree')
    ap.add_argument('--only', help='Regenerate assets for these blocks only (comma-separated), so a whole-tree refresh does not clobber hand-fixed JSON')
    args = ap.parse_args()

    blocks, items, multi, custom_icon, dummy, cls_of = scan_sources(args.src, getattr(args, 'new_src', None))
    print('Found: %d blocks, %d items, %d self-drawn icon classes, %d fake multiblocks' % (len(blocks), len(items), len(multi), len(dummy)))

    only = None
    if args.only:
        only = set(x.strip() for x in args.only.split(',') if x.strip())
        blocks = dict((k, v) for k, v in blocks.items() if k in only)
        items = {}

    out_assets = os.path.join(args.out, 'assets', 'hbm')
    report = defaultdict(list)
    made = defaultdict(int)
    sanitized = []   # variant keys that had dots and were rewritten, for a human to check against the enum names

    # -- blocks --
    for name in sorted(blocks):
        tex = blocks[name]
        if name in RAIL_SHAPE:
            if not args.dry_run:
                emit_rail_shape(args.assets, out_assets, name, tex)
            made['block_rail'] += 1
            continue
        kind = shape_of(name, cls_of.get(name, ''))
        if kind is not None:
            if not args.dry_run:
                emit_shape(args.assets, out_assets, kind, name, tex)
            made['block_' + kind] += 1
            report['Draws the look the vanilla way (verify the property set item by item when porting the block)'].append(name)
            continue
        vs = variants_for(args.assets, tex)
        if vs:
            # Multi-variant blocks cannot be inferred from texture names: the blockstate property key has to match the Java enum and property
            # (platemetal and friends carry both a variant and a CT property) and texture name order or naming is not guaranteed
            # to match the enum. So this only reports by default; write it by hand against the enum when that block is ported.
            report['Variant-multiplex blocks (blockstate hand-written per enum and property; textures listed)'].append(
                '%s: %s%s' % (name, ', '.join(vs[:6]), ' …%d total' % len(vs) if len(vs) > 6 else ''))
            if not args.emit_variants:
                continue
            variants = {}
            for v in vs:
                # Texture file names can contain dots (plant_tall.cd2.lower.png) but a blockstate variant key
                # has to match the PropertyEnum constant name, and those never contain dots, so dots become underscores in the key and the model name alike
                key = v.replace('.', '_')
                if key != v:
                    sanitized.append('%s: %s -> %s' % (name, v, key))
                write_json(os.path.join(out_assets, 'models', 'block', '%s_%s.json' % (tex, key)),
                           {'parent': 'block/cube_all', 'textures': {'all': 'hbm:blocks/%s.%s' % (tex, v)}})
                variants[key] = {'model': 'hbm:%s_%s' % (tex, key)}
            if not args.dry_run:
                write_json(os.path.join(out_assets, 'blockstates', '%s.json' % name), {'variants': variants})
                write_json(os.path.join(out_assets, 'models', 'item', '%s.json' % name), {'parent': 'hbm:block/%s' % list(variants)[0].split(':', 1)[1]})
            made['block_variant'] += 1
        elif texture_exists(args.assets, 'blocks', tex):
            if not args.dry_run:
                write_json(os.path.join(out_assets, 'models', 'block', '%s.json' % name),
                           {'parent': 'block/cube_all', 'textures': {'all': 'hbm:blocks/%s' % tex}})
                write_json(os.path.join(out_assets, 'blockstates', '%s.json' % name),
                           {'variants': state_keys(name, name, dummy)})
                write_json(os.path.join(out_assets, 'models', 'item', '%s.json' % name),
                           {'parent': 'hbm:block/%s' % name})
            made['block_auto'] += 1
        else:
            # Per-face textures (_side/_top/_bottom): they live in constructor arguments and no regex gets them, so infer from the files
            faces = face_textures(args.assets, tex)
            if faces:
                if not args.dry_run:
                    write_json(os.path.join(out_assets, 'models', 'block', '%s.json' % name),
                               {'parent': 'block/cube_bottom_top', 'textures': faces})
                    write_json(os.path.join(out_assets, 'blockstates', '%s.json' % name),
                               {'variants': state_keys(name, name, dummy)})
                    write_json(os.path.join(out_assets, 'models', 'item', '%s.json' % name),
                               {'parent': 'hbm:block/%s' % name})
                made['block_faces'] += 1
            else:
                # Missing textures: usually OBJ models plus TESR (machines) or custom rendering, left to a human
                report['block — no texture (suspect OBJ/TESR/custom rendering)'].append('%s (expected hbm:blocks/%s)' % (name, tex))

    # -- items --
    for name in sorted(items):
        tex = items[name]
        if texture_exists(args.assets, 'items', tex):
            if not args.dry_run:
                write_json(os.path.join(out_assets, 'models', 'item', '%s.json' % name),
                           {'parent': 'item/generated', 'textures': {'layer0': 'hbm:items/%s' % tex}})
            made['item_auto'] += 1
        elif texture_exists(args.assets, 'blocks', tex):
            # item form reusing the block texture (ItemBlock and friends)
            if not args.dry_run:
                write_json(os.path.join(out_assets, 'models', 'item', '%s.json' % name),
                           {'parent': 'hbm:block/%s' % name})
            made['item_from_block'] += 1
        else:
            report['item — no same-named texture (suspect custom model/procedural texture)'].append('%s (expected hbm:items/%s)' % (name, tex))

    print('Generation stats:')
    for k in sorted(made):
        print('  %-16s %d' % (k, made[k]))

    if sanitized:
        report['Variant keys contain dots, converted to underscores (verify the PropertyEnum names)'].extend(sanitized)

    if custom_icon:
        report['Block class carries its own icon logic (the auto-generated single-state blockstate likely is not enough; verify meta/facing/random textures)'].extend(
            sorted(custom_icon))

    print('Awaiting manual handling:')
    for k in sorted(report):
        print('  %s: %d' % (k, len(report[k])))

    rep_path = os.path.join(args.out, 'model-gen-report.txt')
    if not args.dry_run:
        os.makedirs(args.out, exist_ok=True)
        with open(rep_path, 'w', encoding='utf-8') as fh:
            for k in sorted(report):
                fh.write('=== %s (%d) ===\n' % (k, len(report[k])))
                for line in report[k]:
                    fh.write('  ' + line + '\n')
        print('Detailed report: %s' % rep_path)


if __name__ == '__main__':
    main()

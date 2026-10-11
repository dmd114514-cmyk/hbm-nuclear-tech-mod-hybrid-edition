#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Bulk-rewrites vanilla field references in 1.7.10 sources to their 1.12.2 names.

Why it is needed: 1.7.10's vanilla fields are lowercase (Blocks.stone / Items.iron_ingot),
and 1.11+ turned them all into uppercase constants (Blocks.STONE / ITEMS.IRON_INGOT) - 1332 files hit across the tree.
Worse, a batch of them were really renamed rather than recapitalised (fence to OAK_FENCE, seeds to WHEAT_SEEDS...),
so a blind upper() is out of the question; the target field has to be confirmed present in 1.12.2 first.

Strategy:
  1. Pull every Blocks/Items field name out of 1.12.2 with javap;
  2. scan the sources for every `Blocks.X` / `Items.X`;
  3. the candidate for X is X.upper() - a hit is a safe mapping; only a miss falls through to the manual exception table;
  4. neither? Report only, do not touch. Better to miss one than to change one wrongly.

Usage:
  map_vanilla_fields.py --mc-jar <1.12.2 mapped jar> --src <source root> [--apply]
"""

import argparse
import os
import re
import subprocess
import sys
from collections import defaultdict

# Exception table for real renames (not just capitalisation), only confirmed entries
EXCEPTIONS = {
    'Blocks': {
        'fence': 'OAK_FENCE',
        'fence_gate': 'OAK_FENCE_GATE',
        'wooden_door': 'OAK_DOOR',
        'wooden_button': 'WOODEN_BUTTON',
        'wooden_pressure_plate': 'WOODEN_PRESSURE_PLATE',
        'wooden_slab': 'WOODEN_SLAB',
        'double_wooden_slab': 'DOUBLE_WOODEN_SLAB',
        'standing_sign': 'STANDING_SIGN',
        'wall_sign': 'WALL_SIGN',
        'grass': 'GRASS',
        'waterlily': 'WATERLILY',
        'noteblock': 'NOTEBLOCK',
        'piston_extension': 'PISTON_EXTENSION',
        'mob_spawner': 'MOB_SPAWNER',
        'silverfish': 'MONSTER_EGG',
        'monster_egg': 'MONSTER_EGG',
        'water': 'WATER',
        'flowing_water': 'FLOWING_WATER',
        'lava': 'LAVA',
        'flowing_lava': 'FLOWING_LAVA',
        'nether_brick_fence': 'NETHER_BRICK_FENCE',
        'yellow_flower': 'YELLOW_FLOWER',
        'red_flower': 'RED_FLOWER',
        'tallgrass': 'TALLGRASS',
        'deadbush': 'DEADBUSH',
        'pumpkin_stem': 'PUMPKIN_STEM',
        'melon_stem': 'MELON_STEM',
        'melon_block': 'MELON_BLOCK',
        'brown_mushroom_block': 'BROWN_MUSHROOM_BLOCK',
        'red_mushroom_block': 'RED_MUSHROOM_BLOCK',
        'double_stone_slab': 'DOUBLE_STONE_SLAB',
        'stone_slab': 'STONE_SLAB',
        'hardened_clay': 'HARDENED_CLAY',
        'stained_hardened_clay': 'STAINED_HARDENED_CLAY',
        'stained_glass': 'STAINED_GLASS',
        'stained_glass_pane': 'STAINED_GLASS_PANE',
        'glass_pane': 'GLASS_PANE',
        'carpet': 'CARPET',
        'double_plant': 'DOUBLE_PLANT',
        'end_portal': 'END_PORTAL',
        'end_portal_frame': 'END_PORTAL_FRAME',
        'command_block': 'COMMAND_BLOCK',
        'dragon_egg': 'DRAGON_EGG',
        'quartz_block': 'QUARTZ_BLOCK',
        'quartz_stairs': 'QUARTZ_STAIRS',
        'redstone_lamp': 'REDSTONE_LAMP',
        'redstone_wire': 'REDSTONE_WIRE',
        'unpowered_repeater': 'UNPOWERED_REPEATER',
        'powered_repeater': 'POWERED_REPEATER',
        'unpowered_comparator': 'UNPOWERED_COMPARATOR',
        'powered_comparator': 'POWERED_COMPARATOR',
        'flower_pot': 'FLOWER_POT',
        'heavy_weighted_pressure_plate': 'HEAVY_WEIGHTED_PRESSURE_PLATE',
        'light_weighted_pressure_plate': 'LIGHT_WEIGHTED_PRESSURE_PLATE',
        'hay_block': 'HAY_BLOCK',
        'coal_block': 'COAL_BLOCK',
        'iron_bars': 'IRON_BARS',
        'snow_layer': 'SNOW_LAYER',
        'trapdoor': 'TRAPDOOR',
        'furnace': 'FURNACE',
        'lit_furnace': 'LIT_FURNACE',
        'web': 'WEB',
        'vine': 'VINE',
        'portal': 'PORTAL',
        'fire': 'FIRE',
        'air': 'AIR',
    },
    'Items': {
        'seeds': 'WHEAT_SEEDS',
        'cooked_fished': 'COOKED_FISH',
        'fish': 'FISH',
        'reeds': 'REEDS',
        'wooden_door': 'OAK_DOOR',
        'skull': 'SKULL',
        'map': 'MAP',
        'filled_map': 'FILLED_MAP',
        'fireworks': 'FIREWORKS',
        'firework_charge': 'FIREWORK_CHARGE',
        'potionitem': 'POTIONITEM',
        'glass_bottle': 'GLASS_BOTTLE',
        'golden_apple': 'GOLDEN_APPLE',
        'gold_nugget': 'GOLD_NUGGET',
        'nether_star': 'NETHER_STAR',
        'nether_wart': 'NETHER_WART',
        'wheat_seeds': 'WHEAT_SEEDS',
        'melon_seeds': 'MELON_SEEDS',
        'pumpkin_seeds': 'PUMPKIN_SEEDS',
        'carrot_on_a_stick': 'CARROT_ON_A_STICK',
        'rabbit': 'RABBIT',
        'cooked_rabbit': 'COOKED_RABBIT',
        'rabbit_foot': 'RABBIT_FOOT',
        'rabbit_hide': 'RABBIT_HIDE',
        'experience_bottle': 'EXPERIENCE_BOTTLE',
        'blaze_powder': 'BLAZE_POWDER',
        'blaze_rod': 'BLAZE_ROD',
        'magma_cream': 'MAGMA_CREAM',
        'fermented_spider_eye': 'FERMENTED_SPIDER_EYE',
        'spider_eye': 'SPIDER_EYE',
        'rotten_flesh': 'ROTTEN_FLESH',
        'ghast_tear': 'GHAST_TEAR',
        'prismarine_shard': 'PRISMARINE_SHARD',
        'prismarine_crystals': 'PRISMARINE_CRYSTALS',
        'iron_horse_armor': 'IRON_HORSE_ARMOR',
        'golden_horse_armor': 'GOLDEN_HORSE_ARMOR',
        'diamond_horse_armor': 'DIAMOND_HORSE_ARMOR',
        'brewing_stand': 'BREWING_STAND',
        'cauldron': 'CAULDRON',
        'flower_pot': 'FLOWER_POT',
        'armor_stand': 'ARMOR_STAND',
        'comparator': 'COMPARATOR',
        'repeater': 'REPEATER',
        'ender_pearl': 'ENDER_PEARL',
        'ender_eye': 'ENDER_EYE',
        'tipped_arrow': 'TIPPED_ARROW',
        'spectral_arrow': 'SPECTRAL_ARROW',
        'boat': 'BOAT',
        'sign': 'SIGN',
        'bed': 'BED',
        'cake': 'CAKE',
        'apple': 'APPLE',
        'coal': 'COAL',
        'dye': 'DYE',
        'quartz': 'QUARTZ',
        'emerald': 'EMERALD',
        'diamond': 'DIAMOND',
        'arrow': 'ARROW',
        'bow': 'BOW',
        'book': 'BOOK',
        'bucket': 'BUCKET',
        'clock': 'CLOCK',
        'compass': 'COMPASS',
        'shears': 'SHEARS',
        'sugar': 'SUGAR',
        'stick': 'STICK',
        'string': 'STRING',
        'paper': 'PAPER',
        'egg': 'EGG',
        'bone': 'BONE',
        'leather': 'LEATHER',
        'slime_ball': 'SLIME_BALL',
        'snowball': 'SNOWBALL',
        'wheat': 'WHEAT',
        'bread': 'BREAD',
        'name_tag': 'NAME_TAG',
        'lead': 'LEAD',
        'painting': 'PAINTING',
        'item_frame': 'ITEM_FRAME',
        'banner': 'BANNER',
        'shield': 'SHIELD',
    },
}

RE_FIELD_USE = re.compile(r'\b(Blocks|Items|Material)\.([a-z][A-Za-z0-9_]*)\b')

# the 1.12.2 class behind each holder
HOLDER_CLASSES = {
    'Blocks': 'net.minecraft.init.Blocks',
    'Items': 'net.minecraft.init.Items',
    'Material': 'net.minecraft.block.material.Material',
}


def camel_to_snake(name):
    """redstoneLight → redstone_light; that's exactly how 1.12.2's Material was renamed."""
    return re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()


def fields_from_jar(javap, mc_jar, clazz):
    """Grabs a class's public static field names with javap."""
    try:
        out = subprocess.check_output([javap, '-cp', mc_jar, clazz],
                                      stderr=subprocess.STDOUT).decode('utf-8', 'replace')
    except subprocess.CalledProcessError as e:
        print('javap failed: %s' % e, file=sys.stderr)
        sys.exit(2)
    names = set()
    for line in out.splitlines():
        if 'static final' not in line:
            continue
        m = re.search(r'([A-Z][A-Z0-9_]*);\s*$', line.strip())
        if m:
            names.add(m.group(1))
    return names


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--mc-jar', required=True, help='1.12.2 mapped jar')
    ap.add_argument('--src', required=True, help='source root')
    ap.add_argument('--javap', default='javap', help='Path to the javap executable')
    ap.add_argument('--apply', action='store_true', help='Actually write files back (default only reports)')
    args = ap.parse_args()

    valid = {}
    for holder, clazz in HOLDER_CLASSES.items():
        valid[holder] = fields_from_jar(args.javap, args.mc_jar, clazz)
    print('1.12.2 field count:' + '，'.join('%s %d' % (h, len(valid[h])) for h in HOLDER_CLASSES))

    used = defaultdict(set)
    files = []
    for dirpath, _d, filenames in os.walk(args.src):
        for fn in filenames:
            if not fn.endswith('.java'):
                continue
            path = os.path.join(dirpath, fn)
            with open(path, 'r', encoding='utf-8', errors='replace') as fh:
                text = fh.read()
            found = RE_FIELD_USE.findall(text)
            if found:
                files.append(path)
                for holder, field in found:
                    used[holder].add(field)

    mapping = {}      # (holder, old) -> new
    unresolved = []   # (holder, old)
    for holder in HOLDER_CLASSES:
        for field in sorted(used[holder]):
            # Candidate names tried in order: straight uppercase, camelCase to UPPER_SNAKE; only a hit counts as safe
            for cand in (field.upper(), camel_to_snake(field).upper()):
                if cand in valid[holder]:
                    mapping[(holder, field)] = cand
                    break
            else:
                if field in EXCEPTIONS.get(holder, {}):
                    mapping[(holder, field)] = EXCEPTIONS[holder][field]
                else:
                    unresolved.append((holder, field))

    print('Source file hits: %d' % len(files))
    print('Safe to rewrite:' + '，'.join(
        '%s %d' % (h, sum(1 for k in mapping if k[0] == h)) for h in HOLDER_CLASSES))
    if unresolved:
        print('Needs manual confirmation (no matching field in 1.12.2, left untouched):')
        for holder, field in unresolved:
            print('  %s.%s' % (holder, field))
    else:
        print('Nothing unresolved.')

    if not args.apply:
        print('[dry-run] pass --apply to write files back.')
        return

    changed = 0
    for path in files:
        with open(path, 'r', encoding='utf-8') as fh:
            text = fh.read()
        new = RE_FIELD_USE.sub(
            lambda m: '%s.%s' % (m.group(1), mapping.get((m.group(1), m.group(2)), m.group(2))),
            text)
        if new != text:
            with open(path, 'w', encoding='utf-8') as fh:
                fh.write(new)
            changed += 1
    print('Rewrote %d files.' % changed)


if __name__ == '__main__':
    main()

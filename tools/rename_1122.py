#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
The 1.7.10 to 1.12.2 mechanical renamer (only proven rules; anything uncertain is reported, never changed).

Design rule: every one of these renames was verified against the official 1.12.2 jar with javap;
anything with a semantic change (ForgeDirection to EnumFacing, IIcon, IInventory, coordinate fields...)
is never touched automatically - it goes on a list at the end for a human to do file by file. A wrong rename costs far more than a missed one.

Watch the import trap: swapping fully qualified names is not enough. If a file has
`import net.minecraft.util.IChatComponent;` plus bare `IChatComponent x;` usages,
changing only the import leaves the bare name behind and it will not compile. So files that hit that import get the simple name swapped too.

Usage:
  rename_1122.py --src <dir> [--apply]
"""

import argparse
import os
import re
import sys
from collections import defaultdict

# (old FQN, new FQN) - all confirmed present in 1.12.2 via javap
CLASS_RENAMES = [
    ('net.minecraft.util.EnumChatFormatting', 'net.minecraft.util.text.TextFormatting'),
    ('net.minecraft.util.ChatComponentText', 'net.minecraft.util.text.TextComponentString'),
    ('net.minecraft.util.ChatComponentTranslation', 'net.minecraft.util.text.TextComponentTranslation'),
    ('net.minecraft.util.ChatComponentSelector', 'net.minecraft.util.text.TextComponentSelector'),
    ('net.minecraft.util.IChatComponent', 'net.minecraft.util.text.ITextComponent'),
    ('net.minecraft.util.ChatStyle', 'net.minecraft.util.text.Style'),
    ('net.minecraft.event.HoverEvent', 'net.minecraft.util.text.event.HoverEvent'),
    ('net.minecraft.event.ClickEvent', 'net.minecraft.util.text.event.ClickEvent'),
    ('net.minecraft.util.MovingObjectPosition', 'net.minecraft.util.math.RayTraceResult'),
]

# Whole package prefix moved: 1.12.2's jar has no cpw/ any more
PACKAGE_PREFIXES = [
    ('cpw.mods.fml', 'net.minecraftforge.fml'),
]

# CreativeTabs field renames (1.11+ dropped the tab prefix)
CREATIVE_TAB_FIELDS = {
    'tabBlock': 'BUILDING_BLOCKS',
    'tabDecorations': 'DECORATIONS',
    'tabRedstone': 'REDSTONE',
    'tabTransport': 'TRANSPORTATION',
    'tabMisc': 'MISC',
    'tabAllSearch': 'SEARCH',
    'tabFood': 'FOOD',
    'tabTools': 'TOOLS',
    'tabCombat': 'COMBAT',
    'tabBrewing': 'BREWING',
    'tabMaterials': 'MATERIALS',
    'tabInventory': 'INVENTORY',
}

# semantic changes that need a human call - counted, never applied
FLAGGED = [
    (r'\bForgeDirection\b', 'ForgeDirection → EnumFacing (semantics differ; judge direction offsets/order case by case)'),
    (r'\bIIcon\b|\bIIconRegister\b', 'IIcon/IIconRegister deleted; switch to JSON models or TextureAtlasSprite'),
    (r'\bStatCollector\b', 'StatCollector deleted; switch to I18nUtil.resolveKey'),
    (r'\bIInventory\b|\bISidedInventory\b', 'IInventory/ISidedInventory → IItemHandler Capability'),
    (r'\bworldObj\b', 'worldObj → world'),
    (r'\b[xX]Coord\b|\b[yY]Coord\b|\b[zZ]Coord\b', 'xCoord/yCoord/zCoord → pos.getX()/getY()/getZ()'),
    (r'\bstackSize\b', 'ItemStack.stackSize → getCount()/setCount()'),
    (r'\bloadItemStackFromNBT\b', 'loadItemStackFromNBT → new ItemStack(NBTTagCompound)'),
    (r'\bgetCurrentEquippedItem\b', 'getCurrentEquippedItem → getHeldItem(EnumHand)'),
    (r'\bGL11\b', 'GL11 → GlStateManager'),
    (r'\bITickable\b|\bupdateEntity\b', 'Tick interface: must implement ITickable.update()'),
    (r'\bregisterBlockIcons\b|\bgetIcon\b', 'Icon system: switch to JSON blockstates/models'),
]


def rewrite(path, text):
    out = text

    for old, new in PACKAGE_PREFIXES:
        out = out.replace(old, new)

    for old_fqn, new_fqn in CLASS_RENAMES:
        if old_fqn not in out:
            continue
        out = re.sub(re.escape(old_fqn) + r'(?![A-Za-z0-9_])', new_fqn, out)
        old_simple = old_fqn.rsplit('.', 1)[1]
        new_simple = new_fqn.rsplit('.', 1)[1]
        # Swapping FQNs alone leaves bare usages behind, so simple names go too (only in files that hit the import)
        if old_simple != new_simple:
            out = re.sub(r'\b' + re.escape(old_simple) + r'\b', new_simple, out)

    for old_field, new_field in CREATIVE_TAB_FIELDS.items():
        out = re.sub(r'\bCreativeTabs\.' + re.escape(old_field) + r'\b',
                     'CreativeTabs.' + new_field, out)

    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--src', required=True)
    ap.add_argument('--apply', action='store_true')
    args = ap.parse_args()

    files = []
    for dirpath, _d, filenames in os.walk(args.src):
        for fn in filenames:
            if fn.endswith('.java'):
                files.append(os.path.join(dirpath, fn))

    changed = []
    for path in files:
        with open(path, 'r', encoding='utf-8', errors='replace') as fh:
            text = fh.read()
        new = rewrite(path, text)
        if new != text:
            changed.append(os.path.relpath(path, args.src))
            if args.apply:
                with open(path, 'w', encoding='utf-8') as fh:
                    fh.write(new)

    print('Scanned %d files, %s %d'
          % (len(files), 'rewritten' if args.apply else 'rewritable', len(changed)))

    flagged = defaultdict(int)
    for path in files:
        with open(path, 'r', encoding='utf-8', errors='replace') as fh:
            text = fh.read()
        for pattern, _desc in FLAGGED:
            if re.search(pattern, text):
                flagged[pattern] += 1

    if flagged:
        print('The following need manual handling (the script leaves them alone):')
        for pattern, desc in FLAGGED:
            if flagged.get(pattern):
                print('  [%4d files] %s' % (flagged[pattern], desc))

    if not args.apply:
        print('[dry-run] pass --apply to write back.')


if __name__ == '__main__':
    main()

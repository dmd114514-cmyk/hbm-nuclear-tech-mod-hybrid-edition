#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Asset migration tool: takes the original 1.7.10 mod's assets and lays them out the way 1.12.2 wants them.

Two jobs:
  1) copy textures / sounds / models / manual / structures / shaders / disks / sounds.json across as-is;
  2) move the lang files across verbatim as lang/<lowercase code>.lang - 1.12.2 reads .lang (measured: the Locale constant is lang/%s.lang),
     and .json is a 1.16 thing; put that here and every string silently stops working.

Usage:
  python3 tools/migrate_assets.py                 # everything
  python3 tools/migrate_assets.py --only lang     # lang files only
  python3 tools/migrate_assets.py --only lang,textures

The default source/target paths assume the project layout; --src/--dst override them.
"""

import argparse
import json
import os
import shutil
import sys

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def find_source_assets():
    """Searching upward from the project root"mod source"directory (compatible with both the main checkout and side-by-side checkout layouts)."""
    rel = os.path.join("mod source", "HBM Space 1.7.10 latest source", "src", "main", "resources", "assets", "hbm")
    node = PROJECT_ROOT
    for _ in range(5):
        candidate = os.path.join(node, rel)
        if os.path.isdir(candidate):
            return candidate
        parent = os.path.dirname(node)
        if parent == node:
            break
        node = parent
    # Returns a readable expected path even when nothing is found, so errors make sense
    return os.path.join(PROJECT_ROOT, rel)


DEFAULT_SRC = find_source_assets()
DEFAULT_DST = os.path.join(PROJECT_ROOT, "src", "main", "resources", "assets", "hbm")

# directories copied across verbatim
COPY_DIRS = ["textures", "sounds", "models", "manual", "structures", "shaders", "disks"]
COPY_FILES = ["sounds.json"]

# the encodings the old lang files have turned up in, most likely first
ENCODINGS = ["utf-8", "utf-8-sig", "cp1251", "latin-1"]


def read_text(path):
    raw = open(path, "rb").read()
    for enc in ENCODINGS:
        try:
            return raw.decode(enc)
        except UnicodeDecodeError:
            continue
    return raw.decode("utf-8", errors="replace")


def parse_lang(text):
    """.lang is properties-style: one key per line, split on the first equals sign only"""
    out = {}
    for line in text.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        if key:
            out[key] = value.strip()
    return out


def copy_lang(src_dir, dst_dir):
    """1.12.2 reads assets/<domain>/lang/<code>.lang (measured: the Locale constant is lang/%s.lang),
    and JSON lang files came after 1.16; converting them here makes everything silently fail.
    So this only fixes the file name's case, copies the content verbatim, and preserves the keys this project added on the target side."""
    if not os.path.isdir(src_dir):
        print("  [skip] source language directory absent: %s" % src_dir)
        return 0

    os.makedirs(dst_dir, exist_ok=True)
    count = 0

    for name in sorted(os.listdir(src_dir)):
        if not name.endswith(".lang"):
            continue

        src_file = os.path.join(src_dir, name)
        out_name = os.path.splitext(name)[0].lower() + ".lang"
        out_file = os.path.join(dst_dir, out_name)

        entries = parse_lang(read_text(src_file))

        # The target may already hold keys this project added (skeleton samples, say); the source tree must not write over them;
        # on a key collision this project's value wins, the source tree only fills gaps
        if os.path.isfile(out_file):
            merged = dict(entries)
            merged.update(parse_lang(read_text(out_file)))
            entries = merged

        with open(out_file, "w", encoding="utf-8") as fh:
            for key in sorted(entries):
                fh.write("%s=%s\n" % (key, entries[key]))

        count += 1
        print("  [lang] %s -> %s (%d keys)" % (name, out_name, len(entries)))

    return count

def copy_tree(src, dst):
    if not os.path.exists(src):
        print("  [skip] source absent: %s" % src)
        return
    if os.path.isdir(src):
        # Existing target content gets overwritten too - assets come from the source
        shutil.copytree(src, dst, dirs_exist_ok=True)
        size = sum(os.path.getsize(os.path.join(r, f))
                   for r, _, fs in os.walk(src) for f in fs)
        print("  %s -> %s (%.1f MB)" % (os.path.basename(src), dst, size / 1048576.0))
    else:
        shutil.copy2(src, dst)
        print("  %s -> %s" % (os.path.basename(src), dst))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=DEFAULT_SRC, help="source assets/hbm directory")
    ap.add_argument("--dst", default=DEFAULT_DST, help="target assets/hbm directory")
    ap.add_argument("--only", default="all", help="Comma-separated: lang,textures,sounds,models,...")
    args = ap.parse_args()

    src = os.path.abspath(args.src)
    dst = os.path.abspath(args.dst)
    wanted = {x.strip() for x in args.only.split(",") if x.strip()}
    do_all = "all" in wanted

    if not os.path.isdir(src):
        print("Source directory absent: %s" % src)
        return 1

    os.makedirs(dst, exist_ok=True)
    print("Asset migration: %s -> %s" % (src, dst))

    if do_all or "lang" in wanted:
        print("[lang] copying .lang (1.12.2 only reads .lang; converting to JSON fails silently)")
        copy_lang(os.path.join(src, "lang"), os.path.join(dst, "lang"))

    for name in COPY_DIRS:
        if do_all or name in wanted:
            print("[ported] %s" % name)
            copy_tree(os.path.join(src, name), os.path.join(dst, name))

    for name in COPY_FILES:
        if do_all or name.split(".")[0] in wanted:
            print("[ported] %s" % name)
            copy_tree(os.path.join(src, name), os.path.join(dst, name))

    print("done")
    return 0


if __name__ == "__main__":
    sys.exit(main())

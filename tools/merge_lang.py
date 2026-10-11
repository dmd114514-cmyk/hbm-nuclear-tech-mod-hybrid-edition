#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Merges the lang fragments each module carries into one file.

1.12.2 reads `assets/<domain>/lang/<code>.lang` (measured: the `Locale` constant `lang/%s.lang`),
one domain only ever reads that single file, so a module carrying lang fragments can only be merged at build time, not concatenated at runtime.

Layout
  src/main/resources/assets/hbm/lang/<code>.lang            <- the mainline's own keys
  src/main/resources/assets/hbm/lang/fragments/<module>/<code>.lang  <- each module's fragment

On a key collision the mainline wins and a warning is printed: a module should never be able to rewrite existing mainline text - that kind of override only shows up as
somebody's item being renamed once a mod ships. Nothing reads the fragments directory at runtime, so leaving it in place costs nothing.

Usage: python3 tools/merge_lang.py [target lang dir]
With no arguments it works on the resource directory under src (for local checks); the build script passes the output directory under build.
"""

import os
import sys
import glob

FRAG_DIR = "fragments"


def parse_lang(path):
    """Reads in .lang format: one key per line, split on the first equals sign only, lines starting with # are comments"""
    out = []
    seen = set()
    with open(path, encoding="utf-8", errors="replace") as fh:
        for line in fh:
            raw = line.rstrip("\n")
            stripped = raw.strip()
            if not stripped or stripped.startswith("#") or "=" not in stripped:
                continue
            key = stripped.split("=", 1)[0].strip()
            if key in seen:
                continue
            seen.add(key)
            out.append(raw)
    return out


def merge(lang_dir):
    fragments_root = os.path.join(lang_dir, FRAG_DIR)
    if not os.path.isdir(fragments_root):
        print("[mergeLang] no module fragments directory; skipping: %s" % fragments_root)
        return 0

    moved = 0

    for base in sorted(glob.glob(os.path.join(lang_dir, "*.lang"))):
        code = os.path.splitext(os.path.basename(base))[0]
        lines = parse_lang(base)
        own = {l.split("=", 1)[0].strip() for l in lines}
        added = 0

        for frag in sorted(glob.glob(os.path.join(fragments_root, "*", "%s.lang" % code))):
            module = os.path.basename(os.path.dirname(frag))
            for line in parse_lang(frag):
                key = line.split("=", 1)[0].strip()
                if key in own:
                    print("[mergeLang] warning: module %s tried to change mainline key %s; ignored" % (module, key))
                    continue
                own.add(key)
                lines.append(line)
                added += 1

        if added:
            with open(base, "w", encoding="utf-8") as fh:
                fh.write("\n".join(lines) + "\n")
            print("[mergeLang] %s: mainline %d + modules %d -> %d keys"
                  % (code, len(own) - added, added, len(own)))
            moved += 1

    if not moved:
        print("[mergeLang] fragments directory exists, but no language file gained new keys")

    return moved


if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "src", "main", "resources", "assets", "hbm", "lang")

    if not os.path.isdir(target):
        sys.exit("Target directory absent: %s" % target)

    merge(target)

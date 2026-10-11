#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Hardcoded drop audit (contract section 15).

Section 15 says the content layer must not override `getItemDropped` / `quantityDropped` / `dropBlockAsItem`;
drops go through the standard JSON loot tables, or LootTweaker / LootTableTweaker cannot touch them at all.
Acceptance is a spot check and the hit count should be 0; this script is that ruler - run it yourself or before merging.

It also reports `setTable(` inside `LootTableLoadEvent`: section 15 item 4 forbids replacing a whole table
(it tramples other people's changes); only addPool or local edits to your own pool are allowed.

No third-party dependencies, python3 stdlib only.

Usage:
  python3 tools/verify/check_loot_hardcode.py                # scans src/main/java
  python3 tools/verify/check_loot_hardcode.py --path a/b     # only that subtree
  python3 tools/verify/check_loot_hardcode.py --quiet        # verdict only
"""
import argparse
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))

# the three override points section 15 bans; any hit is a violation
FORBIDDEN = ["getItemDropped", "quantityDropped", "dropBlockAsItem"]
# hint only, not a violation (local edits are fine; only whole-table replacement is banned)
ADVISORY = ["setTable("]


def java_files(root):
    out = []
    for cur, _dirs, files in os.walk(root):
        for name in files:
            if name.endswith(".java"):
                out.append(os.path.join(cur, name))
    return out


def scan(root, words):
    hits = []
    for path in java_files(root):
        try:
            with open(path, "r", encoding="utf-8") as fh:
                lines = fh.readlines()
        except (IOError, UnicodeDecodeError):
            continue
        for lineno, line in enumerate(lines, 1):
            stripped = line.strip()
            if stripped.startswith("//") or stripped.startswith("*"):
                continue  # a method name mentioned in a comment does not count
            for word in words:
                if re.search(r"\b%s\s*\(" % re.escape(word), line):
                    hits.append((os.path.relpath(path, REPO), lineno, stripped[:160]))
                    break
    return hits


def main():
    parser = argparse.ArgumentParser(description="Hardcoded drop spot-check (§15)")
    parser.add_argument("--path", default=os.path.join("src", "main", "java"), help="Relative directories to scan")
    parser.add_argument("--quiet", action="store_true", help="prints only the verdict")
    args = parser.parse_args()

    root = os.path.join(REPO, args.path)
    if not os.path.isdir(root):
        print("Directory absent: %s" % root)
        return 2

    forbidden = scan(root, FORBIDDEN)
    advisory = scan(root, ADVISORY)

    if not args.quiet:
        print("Scan scope: %s" % os.path.relpath(root, REPO))
        print("")
        print("Hardcoded drop hooks forbidden by §15 (should be 0):")
        if forbidden:
            for path, lineno, text in forbidden:
                print("  %s:%d  %s" % (path, lineno, text))
        else:
            print("  (no hits)")
        print("")
        print("Heads-up: setTable inside LootTableLoadEvent (only whole-table overrides violate; local addPool doesn't count):")
        if advisory:
            for path, lineno, text in advisory:
                print("  %s:%d  %s" % (path, lineno, text))
        else:
            print("  (no hits)")
        print("")

    if forbidden:
        print("Verdict: non-compliant — %d hardcoded drops; switch to JSON loot tables" % len(forbidden))
        return 1

    print("Verdict: compliant — 0 hardcoded drops")
    return 0


if __name__ == "__main__":
    sys.exit(main())

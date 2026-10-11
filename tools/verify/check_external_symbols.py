#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
External symbol audit: takes cross-package dependencies from the class level down to the member level.

Why it is needed: this project kept tripping in the same spot - seeing `import com.hbm.util.ArmorUtil;` and declaring
the dependencies complete, go ahead, when the `ArmorUtil.checkArmor(...)` actually used was never ported.
import exists does not mean the referenced method exists, and the compile error only shows up after you start.

How it works: given a 1.7.10 source file and a target tree, pull out every `<imported class>.<member>` pattern in the source
and look for a member of that name in the matching class file in the target tree; whatever is missing gets listed as a missing symbol.

Usage:
    python3 tools/verify/check_external_symbols.py <1.7.10 source file> [...] [--tree <target root>]
"""

import argparse
import os
import re
import sys

# 1.7.10 source tree: inferred from a sibling source directory by default; override via env var (never hardcode a local absolute path)
SRC_ROOT_DEFAULT = os.environ.get("HBM_1710_SRC", os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
    "mod source", "HBM Space 1.7.10 latest source", "src", "main", "java"))

# Known substitution table: class.member pairs that moved or changed implementation in this version; a hit here does not count as a gap.
# Each entry = (class regex, member regex, note, whole-class flag); the note records where it lives now and the verdict line carries the source.
# With the whole-class flag even a missing class is not reported (EnumUtil is simply not ported, for instance).
SUBSTITUTIONS = [
    (re.compile(r'^EnumUtil$'), re.compile(r'.*'),
     'EnumUtil.grabEnumSafely -> inlined in place this version (default value on out-of-range); the class is not ported', True),
    (re.compile(r'^MainRegistry$'), re.compile(r'^\w*[Tt]ab$'),
     'MainRegistry.*Tab → ModItems.*Tab (creative tab fields now held by the item layer)', False),
]


def strip_comments_and_strings(text):
    out = []
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        if c == '"':
            j = i + 1
            while j < n and text[j] != '"':
                j += 2 if text[j] == '\\' else 1
            i = j + 1
            continue
        if text.startswith('//', i):
            j = text.find('\n', i)
            i = n if j < 0 else j
            continue
        if text.startswith('/*', i):
            j = text.find('*/', i + 2)
            i = n if j < 0 else j + 2
            continue
        out.append(c)
        i += 1
    return ''.join(out)


def target_file(tree, fqcn):
    p = os.path.join(tree, fqcn.replace('.', '/') + '.java')
    if os.path.isfile(p):
        return p
    outer = os.path.join(tree, fqcn.rsplit('.', 1)[0].replace('.', '/') + '.java')
    return outer if os.path.isfile(outer) else None


def substitution_note(cls_name, member=None):
    for cls_re, mem_re, note, _whole in SUBSTITUTIONS:
        if cls_re.match(cls_name) and (member is None or mem_re.match(member)):
            return note
    return None


def class_wholly_substituted(cls_name):
    for cls_re, _mem_re, _note, whole in SUBSTITUTIONS:
        if whole and cls_re.match(cls_name):
            return True
    return False


def audit(src_file, tree):
    raw = open(src_file, encoding='utf-8').read()
    code = strip_comments_and_strings(raw)

    # already imported com.hbm/api classes: FQN plus simple name
    imports = re.findall(r'^import ((?:com\.hbm|api)\.[\w.]+);', raw, re.M)
    simple = {}
    for fq in imports:
        simple[fq.rsplit('.', 1)[-1]] = fq

    missing_class, missing_member, substituted = [], [], []
    for name, fq in sorted(simple.items()):
        if fq.endswith('.*'):
            continue
        tf = target_file(tree, fq)
        if tf is None:
            note = substitution_note(name) if class_wholly_substituted(name) else None
            if note:
                substituted.append('%s —— %s' % (name, note))
            else:
                missing_class.append('%s (%s)' % (name, fq))
            continue
        raw_body = open(tf, encoding='utf-8').read()
        # Strip comments and strings from the target class too, or a sentence like the docs say this method is not in yet turns a missing method into a present one
        body = strip_comments_and_strings(raw_body)
        # Grabs `<simple name>.<member>`, where the member may be a method or an uppercase constant
        for member in sorted(set(re.findall(r'(?<![\w.])' + re.escape(name) + r'\.([A-Za-z_]\w*)', code))):
            if not re.search(r'(?<![\w.])' + re.escape(member) + r'(?![\w])', body):
                note = substitution_note(name, member)
                if note:
                    substituted.append('%s.%s —— %s' % (name, member, note))
                else:
                    missing_member.append('%s.%s' % (name, member))
    return missing_class, sorted(set(missing_member)), sorted(set(substituted))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('files', nargs='+')
    ap.add_argument('--tree', default=os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', 'src/main/java')))
    args = ap.parse_args()

    bad = 0
    for f in args.files:
        path = f if os.path.isabs(f) else os.path.join(SRC_ROOT_DEFAULT, f)
        if not os.path.isfile(path):
            print('Skipped (source file absent):', f)
            continue
        mc, mm, sub = audit(path, args.tree)
        print('== %s' % os.path.basename(path))
        if mc:
            bad += len(mc)
            print('   missing class: %s' % ', '.join(mc))
        if mm:
            bad += len(mm)
            print('   missing member: %s' % ', '.join(mm))
        if sub:
            # Hits on the substitution table do not count as gaps, but they are traced with their origin so the next round does not treat them as stragglers
            print('   replacement-table hits (not counted as gaps): %s' % ', '.join(sub))
        if not mc and not mm:
            print('   external symbols complete (classes and members all present)')
    print('—— missing symbols total: %d' % bad)
    return 1 if bad else 0


if __name__ == '__main__':
    sys.exit(main())

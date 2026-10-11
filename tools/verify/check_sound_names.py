#!/usr/bin/env python3
"""Sound name check for NTMSounds: every constant value has to exist in assets/hbm/sounds.json.

A wrong sound name never fails the build and never errors at startup - it just never plays. Hence a list check.
Names ending in _PREFIX are deliberately prefixes (a suffix is appended to make the full name); only the variant is checked.
"""
import json, re, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "src/main/java/com/hbm/main/NTMSounds.java"
SOUNDS = ROOT / "src/main/resources/assets/hbm/sounds.json"

def main():
	constants = re.findall(r'public static final String (\w+) = "([^"]+)";', SRC.read_text(encoding="utf-8"))
	# The lookup folds names to lowercase, so both sides are compared lowercase; entries that only match after folding are reported separately,
	# since they mean camelCase keys are still in the table - that count only reaches zero once keys and file names are lowercase.
	keys = set(k.lower() for k in json.loads(SOUNDS.read_text(encoding="utf-8")))
	raw_keys = set(json.loads(SOUNDS.read_text(encoding="utf-8")))
	bad, prefixes, folded = [], [], []
	for name, value in constants:
		if not value.startswith("hbm:"):
			continue                                  # vanilla sounds are not this table's business
		path = value.split(":", 1)[1].lower()
		if name.endswith("_PREFIX"):
			variants = [k for k in keys if k.startswith(path)]
			if not variants:
				bad.append("%s = %s (a prefix, but no sound starts with it)" % (name, value))
			else:
				prefixes.append("%s → %d variants" % (name, len(variants)))
			continue
		if path not in keys:
			bad.append("%s = %s" % (name, value))
		elif path not in raw_keys:
			folded.append(name)
	print("%d sound constants, of which %d in the hbm domain, %d sounds.json keys"
		% (len(constants), sum(1 for _, v in constants if v.startswith("hbm:")), len(keys)))
	print("Matched only after lowercasing: %d entries %s" % (len(folded), folded[:6]))
	for line in prefixes:
		print("  available prefixes: " + line)
	for line in bad:
		print("  mismatch: " + line)
	if bad:
		sys.exit(1)

if __name__ == "__main__":
	main()

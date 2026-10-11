Each module's own translation fragments go here: `fragments/<module ID>/<language code>.lang`, format identical to the top-level `.lang`.

1.12.2 reads a single file per domain, `assets/<domain>/lang/<language code>.lang` (verified against the constants in `Locale`
`lang/%s.lang`), so these fragments are merged into the top-level file **at build time** by `tools/merge_lang.py`; the game never assembles them itself.
Same-named keys defer to the mainline with a warning — modules shouldn't be able to overwrite mainline copy.

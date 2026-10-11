## What this PR does

<!-- One to three sentences. Say why, not what the diff already says. -->

## Scope

- Packages added/changed: <!-- e.g. com/hbm/blocks/generic -->
- [ ] Added an `IRegistrar` (if so: it is registered in `registry/HbmModules.registerAll()`)
- [ ] Touched the hot files `blocks/ModBlocks` / `items/ModItems` / `entity/ModEntities`
      (if so: only moved things out, no new fields)
- [ ] Changed `build.gradle` / the toolchain
- [ ] Documentation only

## Verification

- [ ] `./gradlew compileJava` passes
- Level: `L1` / `L2` / `L3` (circle one)
- Evidence (paste the actual output, or say what you saw):
  - L2: block/item/entity registration counts from the log, and whether `MissingVariant` is 0
  - L3: `Done (` appears in the log, `crash-reports/` unchanged

## Notes

- Related issue:
- Known gaps / what was left out:
- What a reviewer should look at closely:

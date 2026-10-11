# HBM's Nuclear Tech Mod — Hybrid Edition

**An unofficial re-implementation of HBM's Nuclear Tech Mod for Minecraft 1.12.2.**

> `modid: hbm` · by **vanillaDev** · Minecraft **1.12.2** / Forge **14.23.5.2864** · **GNU GPLv3**

Rebuilds the content and gameplay of *HBM's Nuclear Tech Mod* — the 1.7.10 classic and its **NTM: Space** branch — on 1.12.2's own APIs: fission and nuclear explosions, radiation and fallout, machine automation, guns and ammunition, missiles and spaceflight. Assets are reused from the original mod (HBM's Nuclear Tech Mod and its Space branch); the codebase is rebuilt against 1.12.2's native API rather than ported line by line.

## What it is

| | |
|---|---|
| Mod ID | `hbm` |
| Platform | Minecraft **1.12.2** / Forge **14.23.5.2864** |
| Content sources | *HBM's Nuclear Tech Mod* and its **NTM: Space** branch — features, balance, data and assets carried over |
| Implementation | Rebuilt against 1.12.2's native API — same content and feel, not a line-by-line port of the 1.7.10 codebase |
| Dependencies | None required (Forge only). Optional: **JEI**, **CraftTweaker**, **LootTweaker / LootTableTweaker**, **GameStages** |
| | |

How this differs from the 1.7.10 original:

- Content is registered through `RegistryEvent` + `ResourceLocation` — no runtime numeric IDs
- Block states via `IBlockState` / `IProperty`; models as JSON on a stitched atlas
- Inventories, energy and fluids go through Forge **Capabilities** (`IItemHandler` / `IEnergyStorage` / `IFluidHandler`)
- Networking through `SimpleNetworkWrapper`, config through 1.12.2's annotated configs
- Textures, sounds, music, models, structures and the in-game manual are reused from the original mod

## Status

> ⚠️ **Early development.** The core framework (registration, networking, config, capabilities, the asset and model pipeline) is in place and the mod builds, loads and starts. Content is still being filled in batch by batch — it is not ready for normal play yet.

Last in-game measurement: **372 blocks / 1779 items / 31 entities / 15 potions** registered.

## What's planned

| Area | Content |
|---|---|
| Nuclear | RBMK reactors, nuclear explosions, radiation and fallout, waste handling |
| Industry | Machine automation, ore processing, energy and fluid networks, multiblocks |
| Ordnance | Guns and ammunition types, grenades, turrets, missiles |
| Spaceflight | Rockets, satellites, orbital dimensions (from the NTM: Space branch) |
| Misc | Armor and tools, food, decorative blocks, the in-game manual |

> That's the target; see **Status** for what's actually in so far.

## Building

Requires **JDK 8** and network access to `maven.minecraftforge.net`.

```bash
./gradlew build
```

Output: `build/libs/hbm-1.0.0.jar`

> Note: on a first-time build you may see `Could not find ..._mapped_...` together with a `ZipException`. That's a known ForgeGradle race while generating mapped artifacts — **run it once more**, nothing is wrong with your setup.

## For modpack authors

**JEI.** Machine recipes show up in Just Enough Items — 31 categories, one per machine family (assembly, chemical plant, coker, electrolyser, refinery, vacuum circuit, fuel pool, cyclotron, crucible …), with the same inputs and outputs the original NEI pages showed. JEI is a soft dependency: nothing breaks without it.

**Forge capabilities.** Machines expose the standard Forge capabilities — energy (`IEnergyStorage`), fluids (`IFluidHandler`) and inventories (`IItemHandler`) — so they work with any capability-based mod (pipes, tanks, other machines). Energy is measured in **HE** internally; the HE ↔ FE/RF ratio is **1:1 by default** and configurable (`HE per FE`). Generators and storage pick HE or FE per side, and one cable can carry both.

**CraftTweaker.** Weapons, ammunition, grenades, explosions, turrets, mobs, melee tools, food and nukes can be retuned from ZenScript. Values are read where they're used, not snapshotted at startup, so changes take effect immediately:

```zenscript
mods.hbm.tweaks.multiply("weapon", "g12", "damage", 0.25);   // multiply
mods.hbm.tweaks.set("nuke", "nuke_tsar", "radius", 800);     // absolute value
mods.hbm.tweaks.global("mobHealth", 4.0);                    // global fallback
mods.hbm.tweaks.dump();                                      // inspect current overrides
```

`domain` is a lowercase singular (`weapon` `gun` `grenade` `artillery` `nuke` `mob` `melee` `tool` `turret` `food` `global`); `target` is the **registry name**.

**Loot tables.** Everything drops through **standard JSON loot tables** — nothing is hard-coded in Java — so entries can be added, removed or replaced with LootTweaker / LootTableTweaker. Names follow the 1.12.2 convention:

| Purpose | Table name |
|---|---|
| Block drops | `hbm:blocks/<block registry name>` |
| Entity drops | `hbm:entities/<entity registry name>` |
| Machine / container contents | `hbm:machines/<registry name>` |

**Modules and GameStages.** New content comes in as modules (`api.hbm.module`) that can be switched on and off individually without touching the core. **GameStages needs no special work** — it's a CraftTweaker add-on, so the ZenScript hooks above are enough to gate content behind progression.

## Compatibility

- **Saves:** 1.7.10 worlds **cannot** be moved over (both the ID system and the chunk format changed). Worlds from other 1.12.2 HBM distributions can be migrated **conditionally** (shared registry names survive; the rest needs a mapping).
- **Coexistence:** the mod ID `hbm` is the same as theirs, so this **can't** be installed next to another HBM distribution.

## How this re-implementation was made

This project is an unofficial re-implementation: it rebuilds the content and feel of the upstream mod on 1.12.2's native API. It is not a line-by-line port, and I do not claim authorship of the original content.

- **AI-assisted (development aid):** mechanical, repetitive porting work — translating to 1.12.2 APIs (registry events, capabilities, `SimpleNetworkWrapper` wiring, annotated config), build/Gradle scaffolding, boilerplate bindings, and code comments. Review and merge decisions are mine; I test each change before it lands. Commit history is public at https://github.com/dmd114514-cmyk/hbm-nuclear-tech-mod-hybrid-edition/commits/main
- **Mine (maintainer):** scope, architecture, platform-specific decisions, what gets accepted into a release, and bug triage.
- **Not AI-generated:** gameplay, balance, textures, sounds, music, models, structures and the in-game manual. These are carried over unchanged from the upstream mod — see **Credits**.

If a specific 1.12.2 design choice looks odd, open an issue — I'll explain the reasoning. The answer to "why was it done this way" is deliberately something I can give.

**Upstream (gameplay / balance) belongs to** *HBM's Nuclear Tech Mod* by Bobcat / HBMMods and the **NTM: Space** branch by JamesH_2, Mellow. Gameplay or balance feedback goes to them; port-specific bugs go to this repo's tracker.

## Contributing

Pull requests are welcome. Two things before you start:

- **Build with JDK 8** and make sure it compiles (`./gradlew compileJava`). If blocks, items or block entities are touched, paste the relevant lines from `logs/latest.log` in the PR.
- **One content family per pull request.** Content is registered per family: add the objects, register them through the project's registration facade, and give the new family an entry in the assembly list. Please don't grow the shared legacy registration files — those are being shrunk, not extended.

For bug reports, the useful set is: versions, reproduction steps, `logs/latest.log` (or the crash report), and the mod list.

## Credits

- **Bobcat / HBMMods** — author of *HBM's Nuclear Tech Mod* and all its contributors
- **The NTM: Space branch authors** (JamesH_2, Mellow) — the content baseline for this project
- Other forks in the HBM/NTM ecosystem (Extended Edition, Community Edition, Reloaded and others) — referenced for how they solved things
- Minecraft Forge, MCP, LWJGL and the other foundational projects

**Assets:** textures, sounds, music, models and manual text come from the original mod and remain the property of their authors.

**Disclaimer:** this is an **unofficial** re-implementation, not affiliated with the original author or any official distribution.

## License

**Code and assets are under the GNU GPLv3**, inherited from upstream HBM.

| File | Scope |
|---|---|
| `LICENSE` / `LICENSE.LESSER` | This mod (same license as upstream HBM) |
| `LICENSE-Forge-MDK.txt` | The Minecraft Forge MDK license (LGPL 2.1); covers only the bundled scaffolding and Forge-related parts |
| `LICENSE-Paulscode *.txt` | Paulscode SoundSystem / IBXM components |

Original documentation: NTM Wiki — https://nucleartech.wiki/wiki/Main_Page

---

*Early stage: interfaces and implementation are still evolving.*

---

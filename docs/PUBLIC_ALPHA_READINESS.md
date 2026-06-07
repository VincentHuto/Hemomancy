# Hemomancy Public Alpha Readiness

> Last updated: 2026-06-07  
> Target release bar: Public Alpha for Minecraft 1.21.1 / NeoForge 21.1.x  
> Source of truth: current code plus `docs/HEMOMANCY_REFERENCE.md`

This report tracks what needs to be true before Hemomancy is presented to public testers. The alpha goal is not content-completeness; it is installability, first-hour clarity, crash safety, and honest labeling of partial systems.

## Release Bar

Public Alpha is acceptable when:

- The mod builds from the project root with the documented Java, NeoForge, and dependency expectations.
- A new player can enter a world, find or spawn the opening content, activate Blood Volume at a Mortal Display, and understand where Harbinger and Unstained paths begin.
- In-game and wiki guidance clearly distinguishes implemented systems from partial, dormant, and planned systems.
- Late-game or visual-polish gaps are documented as known alpha limitations rather than implied as finished content.
- Optional integrations are labeled accurately: JEI is alpha-ready through the local NeoForge jar; MnA and Curios are dormant on this branch.

## Current State Snapshot

The current branch is broad enough for alpha testing. The audited surface includes NeoForge 1.21.1 attachment-based player state, payload networking, Blood Volume, tendencies, vascular state, known manipulations, Harbinger degree state, Unstained progress, white humor, blood routing, Blood Moon sync, structures, NPC dialogue, JEI categories, and focused resource/source tests.

Important implemented content:

- Core Harbinger progression spine, Cardinal Rites, manipulations, memories, scars, morphlings, living staff forms, puppeteer spindle, drudges, Blood Moon, Qliphoth Communion, direct blood routing, and Flexible Founding Fane core.
- Core Unstained spine, White Humor Purification, Still Arts, Liber Immaculatus book data, Unstained NPCs, cleansing blocks/items, and mutual-exclusion rules.
- World/content systems including Harbinger outposts, Unstained churches, Broken Church/Annetta encounter route, Erythrocoral Reef, deep ocean vent fields, Chthonian termite mound, Blood Lantern Jelly ecology, voyager wrecks, and active voyager vessels.
- JEI support for custom categories documented in `docs/HEMOMANCY_REFERENCE.md`.

Known alpha limitations:

- The Harbinger guide/progression Java screen path is still partial; HutosLib JSON book data exists, but player-facing Liber access should be smoke-tested and described conservatively.
- Founding Fane is mechanically present but still needs balance, art polish, and broader tuning.
- Saints are partial: Hemorath has the first complete trial flow; Seraphae, Putriciel, and Velorum have shared encounter/boss spines but need bespoke rooms, placement tuning, art/animation, and balance.
- Fungal Dimension has access, return placement, mob population, and the Archon choice fork, but terrain/content depth remains WIP.
- Vesper and Mycophant are registered, rendered, sounded, combat-wired, and loot-wired, but their summoning ritual layer remains WIP.
- Annetta is playable through the Broken Church encounter, but final animation/combat polish and unique thrown-projectile rendering remain WIP.
- MnA and Curios source/config are dormant because compatible NeoForge 1.21.1 dependencies are not active.

## Priority Checklist

### P0 - Required Before Public Alpha

- [x] Run `./gradlew.bat build` from the project root and record the result.
- [ ] Smoke-test first-hour flow in `runClient`: new world/load world, Mortal Display activation, Blood Volume HUD, Field Notes/Liber access, early Harbinger hinting, and early Unstained hinting.
- [x] Add a public alpha status page to the wiki so testers know which systems are implemented, partial, dormant, or planned.
- [x] Update compatibility docs so MnA and Curios are not advertised as active integrations.
- [x] Document dev/debug-facing tools as creative/testing tools, not survival progression features.
- [x] Document the guidebook/Liber state conservatively until runClient smoke testing confirms the exact player experience.

### P1 - Highest Value Alpha Polish

- [ ] Run a focused playtest of direct blood routing, bloodwell absorption/projection, Somatic Loom, Mycelial Lantern, Hematic Armature, Morphling Jar, and Puppeteer's Spindle.
- [ ] Tune Founding Fane readability and balance after at least one Degree 5 tester pass.
- [ ] Add or refine first-hour advancement/dialogue wording if smoke testing shows unclear steps.
- [ ] Audit JEI categories in a client session and confirm custom machine recipes are populated.
- [ ] Prepare public release notes using this document and `wiki/Public-Alpha-Readiness.md`.

### P2 - Defer Past Alpha Unless Easy

- Seraphae, Putriciel, and Velorum bespoke trial rooms/world placement/art.
- Deeper Fungal Dimension terrain and feature population.
- Vesper/Mycophant summoning rituals.
- Annetta animation/combat polish.
- Forced manipulation rank-up rituals.
- Active Harbinger Voyager trade/rumor/quest expansion.
- Spectral Companion summon flow.
- MnA and Curios reactivation.

## Recommended Tester Path

Use this as the first public alpha validation route:

1. Install Hemomancy with HutosLib, GeckoLib, TerraBlender, and JEI.
2. Create a new world and locate a Blood Temple, Harbinger Outpost, or another Mortal Display source.
3. Activate the Mortal Display and confirm Blood Volume becomes active.
4. Open Field Notes / Liber surfaces and confirm blood status, path guidance, and memo/book behavior are understandable.
5. Start Harbinger progression: obtain early materials, craft or locate the Cardinal Altar path, perform Sanguine Initiation, learn at least one manipulation, and use the Somatic Loom.
6. Start a separate Unstained validation world or profile: obtain Hemolytic Solution, locate/use Unstained content, perform White Humor Purification, unlock at least one Still Art, and confirm Harbinger progress is reset as expected.
7. In creative or a controlled test world, validate alpha systems beyond the first hour: Mycelial Lantern, Hematic Armature, Morphling Jar, Puppeteer's Spindle, direct blood routing, Founding Fane, Blood Moon, and one saint/Annetta encounter.

## Verification Log

Record fresh verification here when preparing an alpha build:

| Date | Command / Check | Result | Notes |
|------|-----------------|--------|-------|
| 2026-06-07 | `./gradlew.bat build` | Passed | `BUILD SUCCESSFUL in 6s`; 11 actionable tasks, 2 executed, 9 up-to-date; `test` and `check` ran. |
| 2026-06-07 | `./gradlew.bat runClient` smoke test | Not run | Interactive client/world validation required; use the tester path above. |

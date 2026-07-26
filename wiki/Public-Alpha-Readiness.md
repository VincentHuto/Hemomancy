# Public Alpha Readiness

Hemomancy's public alpha target is **playable and honest**, not content-complete. The mod already has a large implemented surface, but some late-game, guidebook, visual, and optional-compatibility systems are still in development.

Use this page to understand what alpha testers should expect.

---

## Status Legend

- **Implemented** - Present in the current NeoForge 1.21.1 runtime path.
- **Partial** - Playable or compiled spine exists, with explicit remaining work.
- **Dormant** - Source or design exists, but it is excluded, unregistered, or blocked by unavailable dependencies.
- **Planned** - Design/lore intent exists, but active runtime behavior is not present yet.

---

## Alpha-Ready Systems

The current alpha build is strongest around these loops:

- **Opt-in start** - Mortal Display activation, Blood Volume, HUD, and first blood-magic state.
- **Harbinger core** - degree progression spine, Cardinal Rites, blood manipulations, memories, tendencies, vascular state, scars, morphlings, living staff forms, puppeteer spindle, Blood Moon, direct blood routing, Qliphoth Communion, Chamber of Will, and Founding Fane core.
- **Unstained core** - purification progress, White Humor Purification, Still Arts, Unstained NPCs, Pale/Lethean theming, and mutual exclusion with Harbinger progress.
- **World content** - Blood Temples, Harbinger Outposts, Unstained Churches, Broken Church/Annetta route, Chthonian termite mounds, Erythrocoral Reef, deep ocean vents, voyager wrecks, active voyager vessels, and Hemomancy mob ecology.
- **Discovery support** - JEI categories, item inquiry dialogue, HutosLib book data, advancement hooks, and wiki/reference docs.

---

## Known Alpha Limitations

These are expected rough edges for public alpha:

- **Guide/Liber surfaces are mixed.** HutosLib JSON book data exists for Harbinger and Unstained books, but the Harbinger Java progression renderer still has partial wiring. Treat Field Notes, Dictation Table, and Liber behavior as a smoke-test priority.
- **Founding Fane is mechanically present but still being tuned.** The bloodwell/stake footprint system, relation-aware boundary rendering, and blood routing hooks exist; final balance and art polish are still pending.
- **Chamber of Will V1 is caster-only.** The Degree 6 rite, personal chamber, radius growth, and dynamic sky themes exist; the later rite that pulls nearby players and mobs into the caster's chamber is not part of V1.
- **Drudges are post-alpha WIP.** Their implementation remains available for development, but the Electrode, Submission Device, and Semi-Sentient Construct have been moved to the WIP creative tab and the system is not part of the first-release progression promise.
- **The entire Saints suite is post-release WIP.** Hemorath now remains behind the same WIP boundary as Seraphae, Putriciel, and Velorum. Saint chambers do not generate naturally, and their encounter components, rewards, and Canon memories are development-facing content for the first major post-release update—not launch blockers or first-release progression.
- **Fungal Dimension is partial.** Access, safe return, dimension mob population, and the Archon choice fork exist; deeper terrain and broader dimension content remain WIP.
- **Endgame bosses are partial.** Vesper and the Mycophant have entities, rendering, sound, combat, boss music, and loot, but their summoning rituals still need another pass.
- **Annetta is playable but not final.** The Broken Church encounter and two-route structure are wired; animation polish, fuller biological combat identity, and unique thrown-projectile rendering remain WIP.
- **Some creative/debug tools exist for testing.** Structure Spawner and Debug Showcase are not intended as normal survival progression.

---

## Compatibility Status

- **JEI: alpha-ready.** Hemomancy currently uses a local NeoForge JEI jar and exposes custom recipe categories for major crafting systems.
- **Mana and Artifice: dormant.** Design and source are preserved, but MnA compatibility is excluded from the NeoForge 1.21.1 build until a compatible MnA dependency is available.
- **Curios: dormant.** Curios compatibility source is preserved, but it is excluded/unregistered until a compatible NeoForge 1.21.1 Curios dependency is available.

---

## Recommended Tester Path

1. Install Hemomancy with HutosLib, GeckoLib, TerraBlender, and JEI.
2. Create or load a world and find a Blood Temple, Harbinger Outpost, or another Mortal Display source.
3. Activate the Mortal Display and confirm Blood Volume appears.
4. Open Field Notes or book surfaces and confirm the first steps are understandable.
5. Follow the Harbinger path far enough to perform Sanguine Initiation and learn/use at least one manipulation.
6. In a separate world/profile, follow the Unstained path far enough to perform White Humor Purification and unlock/use at least one Still Art.
7. In a controlled test world, validate advanced release systems: Somatic Loom, Mycelial Lantern, Hematic Armature, Morphling Jar, puppeteering, direct blood routing, Founding Fane, Chamber of Will, Blood Moon, and the Annetta encounter. Saint encounters are excluded from first-release acceptance. For puppeteering, verify 8 charge per Thread, owner attunement, Spindle preparation without spawning, field call/recall, hostile focus on the matching Crossbar, per-body upkeep, unequipped grace, and deliberate dimension severance.

---

## What To Report

Alpha feedback is most useful when it includes:

- Whether installation and launch succeeded.
- Which dependencies and versions were used.
- Whether the first hour made sense without external help.
- Any crash logs or disconnects.
- Broken recipes, missing models/textures, confusing guidebook entries, or JEI gaps.
- Balance pain points: blood costs, ritual costs, fane strength, mob spawn density, or unclear progression gates.

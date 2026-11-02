# Brined Court: work paths

Date: 2026-10-29. Status: planning. Nothing in this folder is built yet.

The Brined Court is the sea offshoot of the Harbinger tradition: eight houses down the ocean's depth layers, each keeping a hereditary Morphling strain, between Marimo at the sunlit top and Hydra at the vents. It is the bodily counterpart to the Circus. The spec is the working design, copied into the repo so these tracks have a fixed source:

- **Spec:** [design/BRINED_COURT_HOUSES_WORKING_DESIGN.md](design/BRINED_COURT_HOUSES_WORKING_DESIGN.md) (copy of `C:\Users\Vince\Documents\Codex\2026-10-19\hek\outputs\BRINED_COURT_HOUSES_WORKING_DESIGN.md`, decisions to 2026-10-21). Edit the repo copy from now on.
- **Terrain the Court lives in:** [../PELAGIC_WORLDGEN.md](../../../PELAGIC_WORLDGEN.md) and [../PELAGIC_ECOLOGY.md](../../../PELAGIC_ECOLOGY.md).
- **Strain icons, already drawn:** [../output/brined-strain-icons-16x16/](../../concept/output/brined-strain-icons-16x16/README.md) (8 native 16×16 sprites with the new ids, pixel maps and an exporter).
- **Older material, superseded where it disagrees with the spec:** `docs/archive/concept/Underwater faction ideas/` (the original Marimo notes, the 2026-10-06 powwow, the Claude/OpenAI V2 proposals, the 2026-10-04 house concept plates with the old strain names).
- **Boundary with the Circus:** [../circus/06-underwater-boundary-and-deferred-work.md](../circus/06-underwater-boundary-and-deferred-work.md). Morphlings stay the only bodily-symbiosis system; no sea-only attachment framework.

## Status language

Same labels as the Circus suite.

| Label | Meaning |
| --- | --- |
| **SETTLED** | Decided in the spec or by the user. Build to it. |
| **PROPOSED** | My recommendation, written so a session can build it. Open to change until the user answers the linked question. |
| **OPEN** | Needs a user decision before the dependent work starts. Every OPEN item has a question id (Q-…) in the roll-up below. |

## Phases and tracks

The order follows the user's request: the Morphling swap first, then the world, then the people and the big encounters. Each file is one track. A session takes one track, reads this README and the spec first, and leaves the others alone.

| # | Phase | Track | File | Depends on |
| --- | --- | --- | --- | --- |
| 1 | A. Morphlings | Strain table; the eight new strains replace the old ones (no migration) | [01-strain-table-and-replacement.md](01-strain-table-and-replacement.md) | — |
| 2 | A. Morphlings | Strain powers: the eight ladders and the new shared mechanics | [02-strain-powers.md](02-strain-powers.md) | 1 |
| 3 | A. Morphlings | Strain visuals: multi-anchor layers, models, motion, HUD and icons | [03-strain-visuals.md](03-strain-visuals.md) | 1; reads 2's synced state |
| 4 | A. Morphlings | Strain lore, Bestiary, books and reference docs | [04-strain-lore-and-docs.md](04-strain-lore-and-docs.md) | 1, 2 |
| 5 | B. World | Court discovery: "Court known", the three routes, Relay Chart and letter | [05-court-discovery.md](05-court-discovery.md) | 7 for the chart's target (stub until then) |
| 6 | B. World | Ocean placement rules: structure sets, depth layers, exclusions, clutter budget | [06-ocean-placement-rules.md](06-ocean-placement-rules.md) | — |
| 7 | B. World | The two Seats: Lighthouse Garden and Vent Sanctum | [07-seats.md](07-seats.md) | 6 |
| 8 | B. World | Inner-house outposts and the small signs | [08-outposts-and-signs.md](08-outposts-and-signs.md) | 6 |
| 9 | C. People | Court NPCs: two or more roles per settlement, models, spawning, dialogue | [09-court-npcs.md](09-court-npcs.md) | 3 (strain attachments on NPCs), 7, 8 |
| 10 | C. People | Marimo and Hydra: presences, minibosses and the deferred conflict | [10-marimo-and-hydra.md](10-marimo-and-hydra.md) | 7, 9 |
| 11 | D. Court life | The D5 lesson, house membership and switch, standing | [11-court-life-and-progression.md](11-court-life-and-progression.md) | 5, 9 |

**Critical path.** Track 1 lands first: it replaces the fifteen-odd hand-kept strain lists with one table, so the new strains, their powers and their visuals all edit one place. Tracks 2 and 3 run in parallel after it. Track 6 needs nothing from Phase A and can start any time; Tracks 7 and 8 follow it. Track 9 waits for Track 3 because Court NPCs wear their house strain using the same attachment models. Track 10 is the last build track. Track 11's membership and standing should land before Track 9's dialogue is written.

**Phase gate.** Phase A ships as one unit: the old strains are deleted in Track 1, so the new ones need their powers and visuals before anything ships. The mod isn't live, so nothing is migrated (decided 2026-10-29). Phase B can ship without Phase C: outposts without people read as abandoned, which is wrong for the Court, so Track 8 ships with at least placeholder residents from Track 9 or not at all.

## Shared rules for every track

- Read `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md` first. Function-first packages: strain code stays in `common/item/harbinger/morphlings/`, Court player state in `common/capability/player/harbinger/court/`, structures in `common/worldgen/structure/court/`, NPCs in `common/entity/npc/court/`. No top-level `brined_court` package.
- Lang keys only, with the speaker's sheet from `docs/NPC_VOICES.md` (Track 9 adds a "Brined Court" section). Particles through `HemoParticleData` and the custom toolkit, never vanilla clouds (`docs/MANIPULATION_VISUALS.md` for focal moments). Render types built once. World drawing from `WorldEffectDispatcher`, HUD through `GuiLayerRegistrar`, animation time from `AnimationTime`. Nothing under `common/` names a client class.
- Player blood is spent only through `BloodAccounting.pay` / `payUpkeep` with a named `Source`. Targets through `ManipulationCombatHelper`. Healing others is not a `HealModifier` exception; healing the host counts toward the circulation cap (spec balance rule 5).
- Player state in attachments through `HemoCapabilityAccess`; sync through the `PlayerStateSync` table; migrations through `PlayerDataMigrations` with a `player_migration_validation` fixture. Session maps clear through `TransientServerStateEvents`.
- Payload changes bump `PacketHandler.PROTOCOL_VERSION` and regenerate `src/test/resources/network/payload-registry.txt`. Parallel sessions collide here: bump from what is on disk when you land.
- New blocks and items update `registry-ids.txt`, `creative-tab-order.txt`, `CreativeTabSection` / `CreativeTabGroup` filing and, for item-less blocks, `BlockInit.ITEMLESS_BLOCKS`.
- Lore: fungal symbiosis, not mermaids. No God/Satan framing for Marimo or Hydra. Strains are fungi convergent on animals, never the animal. No copper, brass or bronze on anything blood-side (bronze-age Sea Peoples imagery stays in cloth, bone, stone and lacquer). No modern diving tech.
- Model and texture art at 16 px per block. Set-piece models and new strain attachments come from a `tools/model_export/` generator with a `.bbmodel` and preview PNG; hand-written JSON models are not acceptable.
- Every build track ends with client proof in a review client row in `gradle/review-clients.gradle` (named in each track).
- Docs move with mechanics: `docs/HEMOMANCY_REFERENCE.md` (§16 Morphlings, the Reef Watch at the D3 assignments, a new Court section), `docs/LORE_REFERENCE.md`, the relevant `wiki/*.md` page, teacher dialogue and Liber pages.
- No commits; leave work in the tree.

## Decisions (answered 2026-10-29)

**Morphlings**
- No migration of any kind. The mod isn't live; the old eight strains are a first attempt and are deleted outright.
- M1: one strain table, landing with the new strains in Track 1.
- M2: ids `morphling_sunburst`, `_holdfast`, `_mermaids_purse`, `_dead_mans_rope`, `_ink_cap`, `_hushbell`, `_marrowplume`, `_kinbud`.
- M3: Mermaid's Purse keeps the red egg-case icon; its body is a hybrid that grows sailfish parts and red purse pouches.
- M4: secondary tendencies follow the gradient (each leans to its inward neighbour).
- M5: eight wild polyp families, one per strain, named for the partner organism, with catalysts from the house's water.
- M7: a strain's house line shows only once the Court is known.
- M8: build at first-pass numbers and tune in play, always inside the cost and cooldown bands.

**World**
- W1: no Court structures when Pelagic worldgen is off.
- W2: outposts 28/10 chunks, Seats 64/24, at most one faction landmark per 96 blocks. These are starting values.
- W3: the tube coliseum is the Lighthouse Garden's undercroft, with Marimo at the bottom.
- W4: Court sites can be visited before D5; residents feed and talk but don't teach.
- W5: "Relay Chart" and "Brine-Sealed Letter" are the names; Votaries are drowned would-be envoys.

**People**
- P1: roles approved, built on the jointed Hemomancy NPC rig (forearms, shins, inset joint fillers), not a vanilla humanoid.
- P2: Court NPCs are protected for now.
- P3: Shared Wake schools also spawn naturally, rarely, capped and never near a Seat.
- P4: NPCs give food and rest, cultivation help, gifts, inquiries and directions. Fuller trading comes later.
- P5: presences first. The Bloom Warden is fought only after stealing or transgressing at the Garden. The Kindled Champion is a title the player may choose to usurp as a rite of passage.
- P6: the player never fights Marimo or Hydra directly.

**Court life**
- L1: one free house entry plus one free switch.
- L2: the first lesson is faster growth when a strain is fed inside its own house's site.
- L3: Hydra judges worthiness by which follower its head crowns.
- L4: messengers and currents are set dressing only.

**Follow-ups (answered 2026-10-29)**
- T1: every listed act wakes the Bloom Warden: breaking Garden growth, taking from the undercroft, attacking or casting at residents or in the undercroft, taking a gift twice or others' food.
- T2: amends are made by bringing an offering. Holding an offering in hand stops the Warden attacking and opens reconciliation dialogue.
- T3: the Champion title gives nothing beyond recognition for now. Elders challenging the player for it comes later.
- T4: losing either fight means death, with a later retry.
- L5: no strain is needed to join; the house's own strain is needed for its lesson.
- L6: joining needs D5 plus guest standing.
- L7: the Court and Circus can be combined, but Puppet Interference (×1.25 Morphling upkeep while a puppet is active) still applies.
- L8: Unstained players may visit but are shunned by every house as a walking biohazard.

- T5: offerings to the Giving Light are foods.
- Puppet Interference cancels Sunburst's Sun-Fed upkeep waiver.

## Review fixes 2026-10-30

From the user's in-game review (unattended run; calls recorded in Tracks 1, 6, 8 and 9).

- **Creative tab.** The Morphling jar, the polyp and the eight strains are back in the Brined Court section (`CreativeTabSection.withMorphlings`; still listed in the Hematic Morphlings group, the section wins). The creative tool-order GameTest now expects the jar after the Hematic tools. `CreativeTabReview` takes a second "-more" shot of any section taller than one screen.
- **Polyp spawns.** Before: biome-modifier weight 3, groups of 1, and any open water block passed `canSpawnHere`, so polyps took the whole ocean monster cap (nearly nothing else passes a water check). After: same weight and group size (land unchanged), but a natural water spawn keeps one try in 40 (`OCEAN_SPAWN_ODDS`), only on a sturdy seabed block, and never within 48 blocks of another polyp (`OCEAN_SPACING`). Spawn eggs and other reasons are unaffected.
- **Wake nursery.** It never generated: the few Pelagic Ocean cells are deep basins. It now takes open water on a bank or the upper slope (Pelagic Ocean at Y 48 or Twilight water at Y 24, floor at or above Y 18, marker in Pelagic Ocean or Twilight, biome tag gains `twilight_ocean`), searches 64 blocks round its cell, floor variation 8, one valid cell in 24. `court_worldgen_validation` (`-PcourtRun=review-fix-final`): seed 42 one nursery, 1337 two, 8675309 three; every other site count unchanged from Track 8's runs, at most 2 Court sites per 512 region, sign caps unchanged.
- **Set-piece items.** Sunburst Bloom, Frond, Crust and Lamp, Petrified Follower, House Marker, Holdfast Root Mass, Fold Veil, Rope Pod, Current Tissue, Current Strand, Sclerotium Shell, Bell Sleeper, Marrow Plume, Kindled Tissue, Hydra Crown and Vent Offering Basin have block items (off `ITEMLESS_BLOCKS`), item models parenting their generated block models, and sit in the Brined Court section. The bloom places on water like a lily pad; the House Marker is one item listed once per house ("House Marker of the Giving Light" and so on), its house carried in the stack's `block_state` component and drawn by the `hemomancy:house` item property. Nest Cup, Relay Marker and Messenger Buoy stay item-less. No drops.
- **Portraits and crests.** `tools/model_export/court_npc_icons.py` writes a 48 x 48 portrait (`<role>_portrait.png`, a render of the role's model over its house's water) and a 32 x 32 profession crest (`dialogue/crests/court_<role>.png`, the house-coloured shield with a role glyph) for all ten built roles. The Court dialogue headers drew the missing-texture crest before this.
- **NPCs in the Still Bell, Whale Fall, Screened Cleft and Line Camp** are Track 9's remaining houses. Their resident marks are in place for its spawner: the validation's loaded outposts report Line Camp 2, Screened Cleft 2, Still Bell 3 and Whale Fall 4 resident markers.


**Follow-up, same day.**
- **Wake nursery and `/locate`:** `/locate` reports a structure's start chunk, and the nursery had been searching up to 64 blocks around it, so it was often 60 blocks from where `/locate` pointed and hidden under water fog. It now searches only within ±8 blocks of its start chunk's centre (`NURSERY_SEARCH_RADIUS = 8`), with rarity raised from 1 cell in 24 to 1 in 6 to make up for the smaller search. Validation on seeds 42, 1337 and 8675309 passes with 4, 4 and 5 nurseries. Already generated chunks keep the old placement.
- **More block items:** Nest Cup, Relay Marker and Messenger Buoy now have block items too, in the Brined Court section. Goldens are regenerated and all 47 registration GameTests pass.

## Still open

Only minor naming and art: final Seat names, the eight house marker designs, and whether the Wake nursery is wanted alongside natural schools. None block any track.

## Session prompts

One opening prompt per track, with start conditions: [SESSION_PROMPTS.md](SESSION_PROMPTS.md).

## Concurrency note

At the time of writing the working tree has uncommitted mausoleum placement edits in `StructurePlacementChecks.java`, `MausoleumStructure.java`, `MausoleumEntranceRules.java`, `PelagicWorldValidation.java` and `gradle/game-tests.gradle`. Tracks 6–8 touch the last three. Land on top of those edits; don't revert them.

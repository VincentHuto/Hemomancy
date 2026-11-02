# Cardinal Rites and Blood Structures

Status (2026-11-01): one system. Harbinger rites carry a purpose that picks their floor; machines are Working crafts on the Working floor; only the pinned floorless roster is still a Blood Structure; a completed rite leaves a rite space. Merge stages 1 to 4 are committed in code (the rite packages were last touched by `31e640b83`, `b6c7bd846` and `2b36430d9`); the seven design specs under `docs/superpowers/specs/` are still untracked.

## What it is

A Harbinger Cardinal Rite is a floor, a Cardinal Focus at its centre, lit Iron Braziers carrying the recipe's offering signature, a medium seated in the Focus and a planted Living Staff. `HarbingerCardinalRiteEvents` ticks every rite; `ActiveCardinalRite` persists it; activation validates the floor, degree, offerings and the completion effect's `ACTIVATION` refusal before anything is escrowed; the daemon rises from the staff, anchors fill, the seal, the offering procession and a culmination follow; every ending passes through `CardinalRiteTermination` (`docs/HEMOMANCY_REFERENCE.md` §5.3).

Every Harbinger rite declares a `purpose`, and the purpose decides its floor style and default culmination (`CardinalRitePurpose`):

| Purpose | Floor style | Default culmination | Meaning |
|---|---|---|---|
| `self` | Threshold | `ascension` | Degree up, or a power or body change in the caster |
| `world` | Dominion | `domain` | Changes the world: conjures, alters weather, makes something appear |
| `making` | Working | `forging` | Consumes components to make something new |
| `bond` | Communion | `binding` | Acts on ties between people: bloodlines, covenants, pools |

The serializer rejects a floor style that does not match the purpose unless the rite is `"special": true`. Floors come in minor 3x3, lesser 5x5, greater 7x7 and grand 9x9, nested so a floor grows outward; a larger floor of the same style satisfies a smaller rite. Three special grand floors exist: `apotheos_grand` (the Hush, `self`), `qliphoth_grand` (the inverted tree, `world`) and `ancestral_grand` (the lineage, `bond`). A special floor's JSON declares its `purpose`, and only a `special` rite of that purpose may name it. The Rite of Possession (`bond`, `special`) borrows the Qliphoth grand floor and is in the pinned special set with the three (`CardinalRitePurposeDataTest.SPECIAL`).

`world` rites send the daemon out instead of merging it into the caster: Sanguine Eclipse and Pallid Shadow strike the sky (`"world_finale": "sky"`); every other world rite slams the ground (§5.3, "World rite finales").

**Working crafts.** Machines, stations and constructs are `making` rites under `data/hemomancy/recipe/cardinal_rite/working/` (twenty files, `covenant_throne` to `warp_chair`). The old Blood Structure `pattern`/`key` became `subject.pattern`/`subject.key`, `bloodCost` became `subject.feed`, the offhand catalyst became the Focus `medium`, and offerings became the `brazier_signature`; the rite's own `bloodCost` is 0. The subject stands on the floor's upper layer beside the Focus inside the bed (minor 3x1, lesser 5x2, greater 7x3, grand 9x4; `WorkingCraftBed`). Activation escrows the offerings (`RiteOfferingEscrow`, shared with station upgrades) and records the subject; the rite starts in `SUBJECT_FEEDING`, where the caster holds Blood Projection on the subject until the feed is met; the daemon draws the offerings in and the forging culmination swallows the structure and drops the result on the Focus. Recall, collapse or a changed subject block returns the escrow and leaves the structure standing; completion settles once (`CardinalFocusBlockEntity.settledCraft`). Station upgrades are the same Working rite with an upgrade-in-place subject; their contract is `docs/STATION_UPGRADES.md`.

**The floorless tier.** Blood Structures (`data/hemomancy/recipe/blood_structure/`) are hand-fed with Blood Projection, no floor, Focus or brazier, and take no offerings (the serializer rejects an `offerings` field). The pinned Harbinger roster is Dried Gourd, Hematic Iron Block, Liber Sanguinium, Living Staff, Iron Brazier, Vial Centrifuge, Ghastly Alembic, Hematic Stake and Mycelial Synapse, plus the two Unstained structures (Pallid Retort, Unstained Pillar) (`WorkingCraftRosterDataTest`; §25.1).

**Rite spaces.** When a Harbinger rite completes on a formed floor, the cube above that floor (x and z within the floor's half-width, y from 1 to twice the half-width, the same cube braziers count in) becomes a rite space recording Focus, dimension, owner, purpose, floor and rite. One per Focus, latest wins; released when the Focus's once-a-second floor check finds a different floor or none, or the Focus is removed; stored in the Overworld (`hemomancy_rite_spaces`) and synced per dimension (`RiteSpaceSyncPacket`). The tags `hemomancy:rite_bound/self|world|making|bond|any` on blocks, items and entity types mark things that only work inside a matching space: blocks cannot be set down outside one and are inert there, items refuse use with an action-bar line, and tagged creatures unravel after five seconds. The only member is the Sanguine Omen block (`rite_bound/world`); every other tag file is empty.

**Braziers and the cube.** An Iron Brazier stands, mounts on a wall as a sconce, or hangs from a ceiling (`BrazierMount`); a rite counts every lit brazier with an offering anywhere in the N x N x N cube above its floor, excluding the Focus column and anchor-sore cells (`CardinalRiteBrazierSpace`). Possession's `optional_offerings` field is the one exception to the exact signature: each listed offering takes the first lit brazier holding it and the rest are ignored (§5.6e, `docs/archive/tracks/possession/01-rite-offerings.md`).

**Presentation.** The daemon is a ray-marched smoke volume with three Monolith eyes (`cardinal_daemon_haze` shader; `docs/superpowers/specs/2026-10-13-cardinal-daemon-volume-design.md` supersedes the two-card haze). Later anchor rings wake one phase at a time and cost 25 percent more per ring (ring gating spec). Each milestone (ring, seal, procession, culmination) raises a server-held escalation stage that drives fog, trench, creep, sky wound and blood rain, with the top panel off by default (world escalation spec). The floor membrane is `cardinal_floor_membrane.fsh` with a motif per style (indices 4 to 6 are the special floors), the Apotheos floor also uses `apotheos_floor_funnel`, and the Working swallow reuses `blood_structure_warp` with a `FillLine` uniform.

**Liber and JEI.** The Harbinger Liber has one Rites & Crafts map; families are grouped by purpose in `HarbingerRecipeMapDefinitions` in the order Floorless, Self, World, Making, Bond, Reference. JEI has Blood Structures (floorless), Threshold Rites, Dominion Rites, Working Rites & Crafts and Communion Rites, with Succession kept in its own category.

## Where it lives

| Part | Path |
|---|---|
| Lifecycle, kinds, activation, completion, termination | `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/` (`HarbingerCardinalRiteEvents`, `RiteKind` enum: `SUCCESSION`, `STATION_UPGRADE`, `WORKING_CRAFT`, `PUPPETEER_TRIAL`, `COUNTDOWN`, `CEREMONY`; `CardinalRiteActivation`, `CardinalRiteCompletion`, `CardinalRiteCompletionEffects`, `CardinalRiteTermination`, `RiteOfferingEscrow`, `WorkingCraftRites`, `WorkingCraftSubjectMatcher`, `WorkingCraftDaemonPath`) |
| Rite state, purposes, rules | `common/rite/` (`ActiveCardinalRite`, `CardinalRitePurpose`, `CardinalRiteStationMatcher`, `CardinalRiteBrazierSignatureRules`, `WorkingCraftBed`, `WorkingCraftRecipeRules`, `CardinalRiteEscalation`, `CardinalRiteDaemonFinale`) |
| Floors | `common/rite/floor/` (`CardinalRiteFloorDefinition`, `CardinalRiteFloorVisualRules`, `CardinalRiteBrazierSpace`, `CardinalRiteGashRules`); data `data/hemomancy/cardinal_rite_floor/` |
| Floorless crafting | `common/rite/structure/` (`BloodStructureCrafting`, `BloodStructureFormation`) |
| Rite spaces and rite-bound rules | `common/rite/zone/` (`RiteSpace`, `RiteSpaces`, `RiteSpaceRules`, `RiteSpaceSavedData`, `RiteBound`, `RiteBoundRules`, `RiteBoundEvents`) |
| Brazier mini-rites | `common/rite/brazier/` (`BrazierRite`, `BrazierRites`) |
| Recipes | `data/hemomancy/recipe/cardinal_rite/*.json` and `cardinal_rite/working/*.json`; floorless `recipe/blood_structure/` |
| Tags | `data/hemomancy/tags/{block,item,entity_type}/rite_bound/{self,world,making,bond,any}.json` |
| Liber map | `client/screen/skilltree/shared/` (`RitesAndCraftsTabController`, `RecipeMapPurposeGroup`, `HarbingerRecipeMapDefinitions`) |
| JEI | `compat/jei/BloodStructureRecipeCategory`, `JEIPlugin` |
| Shaders | `assets/hemomancy/shaders/core/world/cardinal_floor_membrane.*`, `apotheos_floor_funnel.*`, `cardinal_daemon_haze.*`, `blood_structure_warp.*` |
| Review client | `riteFloorReviewClient` in `gradle/review-clients.gradle` (`hemomancy.riteFloorReview`, `riteFloorReviewSpecial`) |
| GameTests | `src/gameTest/.../RiteKindCharacterisationGameTests.java`, namespace `rite_kind_validation`; `WorkingCraftGameTests` |
| Reference | `docs/HEMOMANCY_REFERENCE.md` §5.3 (flow, purposes, floors, special floors, rite spaces, braziers), §25.1 to §25.2; `wiki/Harbinger-Path.md` "Cardinal Rites" |

## Rulings that bind

From `AGENTS.md`, quoted:

- "Cardinal Rites share one tick in `HarbingerCardinalRiteEvents`; where kinds differ it asks the rite's `RiteKind` (succession, station upgrade, Working craft, Puppeteer trial, countdown, ceremony), and so do `CardinalRiteTermination` and `CardinalRiteCompletion.complete`. Add kind behaviour as a `RiteKind` hook, not an `isRite`/`isWorkingCraft` check there (`RiteKindGuardrailTest`). A kind's saved state is its own object behind `ActiveCardinalRite` and keeps its NBT keys (`ActiveCardinalRiteNbtLayoutTest`). Start rites with `CardinalRiteActivation.tryStart`; the `rite_kind_validation` GameTests characterise every kind."
- "Every Harbinger Cardinal Rite declares a `purpose` (`self` Threshold, `world` Dominion, `making` Working, `bond` Communion); its floor style must match unless it is `"special": true`. Machines, stations and constructs are Working crafts (`recipe/cardinal_rite/working/`, a `subject` fed in `SUBJECT_FEEDING`, offerings escrowed through `RiteOfferingEscrow`, settled once). Only the pinned floorless roster stays a Blood Structure, and Blood Structures take no offerings. Keep `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest` and the style-level selector test green when adding rites or crafts; see `docs/HEMOMANCY_REFERENCE.md` §5.3 and §25.1."
- "Harbinger rites and crafts share one Liber map (Rites & Crafts) whose families are grouped by purpose in `HarbingerRecipeMapDefinitions`; keep `RecipeMapPurposeGroupDataTest` green and file a new rite's family in its purpose's group. JEI lists rites per purpose. Special rites use special floors that declare their `purpose` (`SpecialFloorDataTest`). A completed Harbinger rite leaves a rite space over its floor until that floor or Focus goes; `hemomancy:rite_bound/*` tags limit blocks, items and entities to matching spaces. Never rite-bind something a player needs before their first completed rite."

User rulings:

- 2026-10-14: Blood Structures merge into Cardinal Rites in stages (stage 1 spec approved section by section). 2026-10-18: "just do it" for stages 2 to 4, so those stages' design calls are the session's.
- Unstained rites stay outside the purpose grammar: no floor, no purpose, no rite space. The Unstained get their own ritual system later; rite work targets Harbinger (`2026-10-14-rite-purposes-and-working-crafts-design.md` §1; ring-gating spec decisions).
- Harbinger floors never carry copper; copper is Unstained material and a White Humor witness (`docs/validation/CARDINAL_RITE_FLOORS_2026_10_12.md`). The Working accent is Bone Block; four migrated patterns swapped copper for Chiseled Hematic Iron.
- Approved purpose judgement calls: Crimson Beacon is `self`; Puppeteer trials, Succession, Mnemonic Restoration and Pallid Shadow are `world` (stage 1 spec §1).
- Brazier cube scope: anywhere in the cube; hanging braziers on chains, wall braziers on a bracket arm (mounted-braziers spec, approved).
- Daemon: a true 3D ray-marched volume, not stacked slices or a faked-depth card (daemon volume spec).
- Ring gating: later rings open at phase boundaries, cost 25 percent more per ring, and the phase runs but cannot end until its ring is full (ring-gating spec).
- World escalation: a server-held stage that only rises, visual and sound only, top panel off by default (world escalation spec).

## Calls made without the user

Stage 2 to 4 (`2026-10-18-rite-merge-stages-2-4-design.md`, all the builder's calls):

- The purpose-group order and colour sources on the Liber map, the family splits (Qliphoth/Forbidden into World/Qliphoth and Bond/Ancestral; Bloodline/Fane into World/Fane and Bond/Bloodline; Exsanguination into Vessel), and the family filter cycling groups instead of families.
- The five JEI categories, their icons, and Succession keeping its own category.
- The three special floor patterns (the Hush, the inverted tree, the lineage), grand tier only, with their palettes and membrane motifs; a D7 player who built a standard grand floor must rebuild the special one.
- Rite space geometry (the brazier cube), one per Focus with latest purpose winning, release on floor change or Focus removal but not chunk unload, Overworld saved data, and the mote rendering.
- The rite-bound tag set and the three enforcement rules (inert blocks, refused items, 5-second unravel for entities).
- The Sanguine Omen as the only rite-bound member, chosen because it is decorative and costs no progression.

Stage 1 (`2026-10-14-rite-purposes-and-working-crafts-design.md`, approved in conversation but authored by the session): the twenty-craft roster's tiers, media and feed values, the offerings marked "new" ("a first pass and can be tuned freely"), the Somatic Loom trim to 3x2 and the Semi-Sentient Construct redraw to 3x3x2 at greater rather than grand.

Possession (`docs/archive/tracks/possession/01-rite-offerings.md`): Possession borrowing the Qliphoth grand floor as a `special` `bond` rite, and the `optional_offerings` matcher hook. The brainstorm-approved per-rite design was the session's within the possession tracks.

## Open questions

- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-38 [D5-4], D-44 [D5-11], D-66 [D6-3], D-67 [D6-5], D-68 [D6-6], D-88 [CHAIN-1].
- Rite spaces have one member. The tags are the extension point for rite-born content, and the Sanguine Omen was a stand-in; what real content is rite-bound is undecided (stage 2 to 4 spec §4).
- Per-floor ceremony mechanics (the audit's Endure/Hold/Share/Precision archetypes, sigil result modifiers beyond the Reservoir refund, the participatory procession) remain out of scope in every spec (`docs/archive/audits/CARDINAL_RITE_AUDIT.md` "Not done"; stage 1 and stage 2 to 4 "Out of scope").
- F-11, pending: `PendingBloodCraftManager.tick()` runs once per loaded level per tick, so floorless crafts finish faster with more dimensions loaded; the proposed fix is once per server tick so crafts take their intended time (`docs/architecture/CONSOLIDATION_PROGRESS.md`, `ARCHITECTURE_AUDIT.md` F-11).
- A blood-cost balance pass across rites and crafts has never been done (stage 1 spec "Out of scope").
- The Unstained ritual system (stillness ceremony, vanilla particle sweep) is deferred by request (audit "Not done").
- `MapCodec.encode` still drops ceremony data (audit "Not done").
- Liber entries for rites without a chapter are a lore choice (audit "Not done").

## Proof owed and known gaps

Deferred minors from the stage 2 to 4 final review:

- The Qliphoth membrane motif ignores floor rotation.
- The Making/Bond and Floorless/World group tints on the Liber map look alike.
- Nested recipe booleans are still misread through NBT.
- The skill-tree editor still previews separate Rites and Crafting tabs.
- The hub label still reads "Rites".

Other gaps:

- The audit's cleanup pass noted the floor and staff-melt render types were rebuilt once per frame per Focus; `RenderTypeCachingGuardrailTest` now rejects per-frame builds, and the reference says render types are cached, but no live-client proof of the frame cost is recorded (`CARDINAL_RITE_AUDIT.md` "Known follow-up").
- `legacyVialsAreRejectedBeforeConsumption` in the succession GameTests read a brazier at a fixed offset that the 2026-10-12 socket redesign moved (audit status); check it still passes after the 2026-10-14 cube change.
- Shader changes (membrane motifs, daemon volume, funnel) need a client launch to prove compilation; `riteFloorReviewClient` is the launcher.
- The seven specs under `docs/superpowers/specs/` are untracked; the audit header still says stages 2 to 4 are "uncommitted" although their code is in the history.

Guardrails that hold the system: `RiteKindGuardrailTest`, `ActiveCardinalRiteNbtLayoutTest`, `CardinalRitePurposeDataTest`, `CardinalRiteLayeredRecipeDataTest`, `WorkingCraftRosterDataTest`, `SpecialFloorDataTest`, `RecipeMapPurposeGroupDataTest`, `WorldRiteFinaleDataTest`, `CardinalRiteBrazierSpaceTest`, `BrazierMountTest`, `CardinalRiteGashRulesTest`, `CardinalRiteFloorDesignDataTest`; GameTests `rite_kind_validation`, `WorkingCraftGameTests`, `BrazierMountGameTests` (`docs/architecture/ARCHITECTURE_STANDARDS.md`; §5.3).

## History

| Date | What | Source |
|---|---|---|
| 2026-10-12 | Read-only audit: 19 bugs, five lifecycles told apart by scattered checks, 39 of 52 ceremonies without an ordeal. Overnight cleanup fixed every §2 bug but #14 (reworked) and #16/#17 (client), unified termination, completion effects, zones and brazier rites. Floors lost copper, sockets rebuilt, consecration membrane added. | [audit](archive/audits/CARDINAL_RITE_AUDIT.md), [floor validation](validation/CARDINAL_RITE_FLOORS_2026_10_12.md) |
| 2026-10-13 | World escalation stage; ring gating one ring per phase; daemon as a hooded haze, then superseded by the ray-marched smoke volume. | [escalation](superpowers/specs/2026-10-13-cardinal-rite-world-escalation-design.md), [ring gating](superpowers/specs/2026-10-13-cardinal-rite-ring-gating-design.md), [haze](superpowers/specs/2026-10-13-cardinal-daemon-haze-design.md), [volume](superpowers/specs/2026-10-13-cardinal-daemon-volume-design.md) |
| 2026-10-14 | Stage 1: purposes, 17 rites rehomed, twenty machines moved to Working crafts, structure offerings removed, `RiteOfferingEscrow` shared. Mounted braziers and the rite cube. | [purposes and crafts](superpowers/specs/2026-10-14-rite-purposes-and-working-crafts-design.md), [braziers](superpowers/specs/2026-10-14-mounted-braziers-and-rite-cube-design.md) |
| 2026-10-18 | Stages 2 to 4: one Rites & Crafts map and per-purpose JEI; the three special floors; rite spaces and `rite_bound` tags with the Sanguine Omen. | [stages 2 to 4](superpowers/specs/2026-10-18-rite-merge-stages-2-4-design.md) |
| 2026-10-23 | Consolidation step 11: `RiteKind` strategy, per-kind state objects, `CardinalRiteActivation`; Blood Structure crafting moved out of the key-press packet (commit `b6c7bd846`). | [standards](architecture/ARCHITECTURE_STANDARDS.md) |
| 2026-11-02 | Playthrough suite fixes: rite recovery waits for a dead owner and counts only stacks that land (`CardinalRiteSavedData.deliverRecovery`); a Sanguine Formation medium seats on a sneak-click whatever the off hand holds (`SanguineFormationItem.useOn`). Open late questions: D-38 [D5-4], D-44 [D5-11], D-66 [D6-3], D-67 [D6-5], D-68 [D6-6], D-88 [CHAIN-1]. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) |
| 2026-10-29 | Possession phase 2: Possession becomes a `special` `bond` rite on the borrowed Qliphoth grand floor with `optional_offerings` through the braziers. | [possession track 1](archive/tracks/possession/01-rite-offerings.md) |

What the audit asked for and where it stands: bug pass, server unification (`terminate()`, effect registry, data-driven waves, `RiteZone`) and client unification landed in the 2026-10-12 pass; the `RiteVariant` interface landed as `RiteKind` in step 11; effects (reactive membrane, culmination styles, collapse choreography, wave telegraphs, daemon geometry, `rite.*` sounds) landed on 2026-10-12 and 2026-10-13; gameplay archetypes, sigil modifiers beyond the Reservoir refund, the participatory procession and the Unstained stillness ceremony have not.

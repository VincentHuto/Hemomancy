# Cardinal Rites and Blood Structures

Status (2026-11-01): one system. Harbinger rites carry a purpose that picks their floor; machines are Working crafts on the Working floor; only the pinned floorless roster is still a Blood Structure; a completed rite leaves a rite space. Merge stages 1 to 4 are committed in code (the rite packages were last touched by `31e640b83`, `b6c7bd846` and `2b36430d9`); the seven design specs under `docs/superpowers/specs/` are still untracked.

Status (2026-11-14): the owner answered every rite row on 2026-11-13 and the overnight build (package P09) built most of them, uncommitted. Package B25 (2026-11-15) built the rest but the cost pass, which waits for play. See "Proof owed and known gaps".

## What it is

A Harbinger Cardinal Rite is a floor, a Cardinal Focus at its centre, lit Iron Braziers carrying the recipe's offering signature, a medium seated in the Focus and a planted Living Staff. `HarbingerCardinalRiteEvents` ticks every rite; `ActiveCardinalRite` persists it; activation validates the floor, degree, offerings and the completion effect's `ACTIVATION` refusal before anything is escrowed; the daemon rises from the staff, anchors fill, the seal, the offering procession and a culmination follow; every ending passes through `CardinalRiteTermination` (`docs/HEMOMANCY_REFERENCE.md` §5.3).

Every Harbinger rite declares a `purpose`, and the purpose decides its floor style and default culmination (`CardinalRitePurpose`):

| Purpose | Floor style | Default culmination | Meaning |
|---|---|---|---|
| `self` | Threshold | `ascension` | Degree up, or a power or body change in the caster |
| `world` | Dominion | `domain` | Changes the world: conjures, alters weather, makes something appear |
| `making` | Working | `forging` | Consumes components to make something new |
| `bond` | Communion | `binding` | Acts on ties between people: bloodlines, covenants, pools |

The serializer rejects a floor style that does not match the purpose unless the rite is `"special": true`. Floors come in minor 3x3, lesser 5x5, greater 7x7 and grand 9x9, nested so a floor grows outward; a larger floor of the same style satisfies a smaller rite. Three special grand floors exist: `apotheos_grand` (the Hush, `self`), `qliphoth_grand` (the inverted tree, `world`) and `ancestral_grand` (the lineage, `bond`). A special floor's JSON declares its `purpose`. The serializer lets any `special` rite name any floor (`CardinalRitePurpose.violation`); which special rite stands on which special floor is pinned by `SpecialFloorDataTest`. The Rite of Possession (`bond`, `special`) borrows the Qliphoth grand floor and is in the pinned special set with the three (`CardinalRitePurposeDataTest.SPECIAL`).

`world` rites send the daemon out instead of merging it into the caster: Sanguine Eclipse and Pallid Shadow strike the sky (`"world_finale": "sky"`); every other world rite slams the ground (§5.3, "World rite finales").

**Working crafts.** Machines, stations and constructs are `making` rites under `data/hemomancy/recipe/cardinal_rite/working/` (eighteen files, `covenant_throne` to `vascular_effigy`). The old Blood Structure `pattern`/`key` became `subject.pattern`/`subject.key`, `bloodCost` became `subject.feed`, the offhand catalyst became the Focus `medium`, and offerings became the `brazier_signature`; the rite's own `bloodCost` is 0. The subject stands on the floor's upper layer beside the Focus inside the bed (minor 3x1, lesser 5x2, greater 7x3, grand 9x4; `WorkingCraftBed`). Activation escrows the offerings (`RiteOfferingEscrow`, shared with station upgrades) and records the subject; the rite starts in `SUBJECT_FEEDING`, where the caster holds Blood Projection on the subject until the feed is met; the daemon draws the offerings in and the forging culmination swallows the structure and drops the result on the Focus. Recall, collapse or a changed subject block returns the escrow and leaves the structure standing; completion settles once (`CardinalFocusBlockEntity.settledCraft`). Station upgrades are the same Working rite with an upgrade-in-place subject; their contract is `docs/STATION_UPGRADES.md`.

**The floorless tier.** Blood Structures (`data/hemomancy/recipe/blood_structure/`) are hand-fed with Blood Projection, no floor, Focus or brazier, and take no offerings (the serializer rejects an `offerings` field). The pinned Harbinger roster is Dried Gourd, Hematic Iron Block, Liber Sanguinium, Living Staff, Iron Brazier, Vial Centrifuge, Ghastly Alembic, Hematic Stake and Mycelial Synapse, plus the two Unstained structures (Pallid Retort, Unstained Pillar) (`WorkingCraftRosterDataTest`; §25.1).

**Rite spaces.** When a Harbinger rite completes on a formed floor, the cube above that floor (x and z within the floor's half-width, y from 1 to twice the half-width, the same cube braziers count in) becomes a rite space recording Focus, dimension, owner, purpose, floor and rite. One per Focus, latest wins; released when the Focus's once-a-second floor check finds a different floor or none, or the Focus is removed; stored in the Overworld (`hemomancy_rite_spaces`) and synced per dimension (`RiteSpaceSyncPacket`). The tags `hemomancy:rite_bound/self|world|making|bond|any` on blocks, items and entity types mark things that only work inside a matching space: blocks cannot be set down outside one and are inert there, items refuse use with an action-bar line, and tagged creatures unravel after five seconds. The only member is the Sanguine Omen block (`rite_bound/world`); every other tag file is empty.

**Braziers and the cube.** An Iron Brazier stands, mounts on a wall as a sconce, or hangs from a ceiling (`BrazierMount`); a rite counts every lit brazier with an offering anywhere in the N x N x N cube above its floor, excluding the Focus column and anchor-sore cells (`CardinalRiteBrazierSpace`). Possession's `optional_offerings` field is the one exception to the exact signature: each listed offering takes the first lit brazier holding it and the rest are ignored (§5.6e, `docs/archive/tracks/possession/01-rite-offerings.md`).

**Presentation.** The daemon is a ray-marched smoke volume with three Monolith eyes (`cardinal_daemon_haze` shader; `docs/superpowers/specs/2026-10-13-cardinal-daemon-volume-design.md` supersedes the two-card haze). Later anchor rings wake one phase at a time and cost 25 percent more per ring (ring gating spec). Each milestone (ring, seal, procession, culmination) raises a server-held escalation stage that drives fog, trench, creep, sky wound and blood rain, with the top panel off by default (world escalation spec). The floor membrane is `cardinal_floor_membrane.fsh` with a motif per style (indices 4 to 6 are the special floors) that turns with the floor's facing (`FloorFacing` uniform), the Apotheos floor also uses `apotheos_floor_funnel`, and the Working swallow reuses `blood_structure_warp` with a `FillLine` uniform.

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
- "Every Harbinger Cardinal Rite declares a `purpose` (`self` Threshold, `world` Dominion, `making` Working, `bond` Communion); its floor style must match unless it is `"special": true`. Machines, stations and constructs are Working crafts (`recipe/cardinal_rite/working/`, a `subject` fed in `SUBJECT_FEEDING`, offerings escrowed through `RiteOfferingEscrow`, settled once). Only the pinned floorless roster stays a Blood Structure, and Blood Structures take no offerings. Keep `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest` and `CardinalRitePurposeTest` (the style-level selector) green when adding rites or crafts; see `docs/HEMOMANCY_REFERENCE.md` §5.3 and §25.1."
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
- Rite-bound content is not a priority (D-10, 2026-11-06): the Sanguine Omen stays the only rite-bound member, and nothing more is planned.

Owner sheet, recorded 2026-11-13 (built 2026-11-14 where it changed code):

- F-11, PT-34 (2026-11-13): a pending Blood Structure craft counts down once per server tick, on its own level's tick (`PendingBloodCraftManager.tick(ServerLevel)`), however many dimensions are loaded.
- CN-2 (2026-11-13): a running rite's ground refuses every player's plain Projection, not only the caster's. On that ground plain Projection neither infuses a block nor stains it (`CardinalRiteFootprintResolver.onRiteGround`); anchors, the daemon, sigils, sinks, braziers and the Focus still take blood, and mining still breaks the floor. Built for running rites and plain Projection only; see "Calls made without the user".
- RV-43 (2026-11-13): keep the caster's own claim on their rite's ground; it is the caster's case of CN-2.
- PT-32 (2026-11-13): a player below the Degree of the rite their floor answers to is told the Degree it needs (the lowest, if several match), not that no rite matches. A player past a rank rite's Degree hears "You have already attained" at its floor (built 2026-11-15).
- PT-67 (2026-11-13): the Rite of the Chamber of Will refuses when no visit can start (a visit running, a projection or possession, standing in the Chamber, or no Chamber level). At activation nothing starts or is escrowed; at the finale the rite waits. It attunes only when the visit begins.
- PT-68 (2026-11-13): a plain click with a Sanguine Formation seats it in an empty Focus when a rite on the floor formed there takes the Formation as its medium. A sneak-click still seats it on any floor; elsewhere a plain click tries the floor.
- PT-88 (2026-11-13): Projection fills a dry anchor before it repairs it; the 50 mL repair is paid once the anchor is full. The solo stall was the playthrough harness, not the rite (see "Calls made without the user").
- FX-10 (2026-11-13): the rite recipe codec writes everything it reads (`ceremony`, `floor`, `required_structure`, `brazier_signature`, `optional_offerings`, `pattern`, `key`, `subject`), so JSON and NBT round trips give the same recipe.
- FX-13 (2026-11-13): the succession test reads the brazier the fixture filled first, not a fixed offset.
- FX-11 (2026-11-13): fix the five Rites & Crafts minors together. Built: the Qliphoth membrane motif turns with the floor's facing, nested recipe booleans read through NBT, the merged map's hub reads "Rites & Crafts", the Floorless and Making groups have tints of their own (bone `0xFFC8BC9C`, hematic iron `0xFF8C9DB8`), and the skill-tree editor previews one merged map.
- FX-12 (2026-11-13): rites that share a map icon get their own. The Rite of the Bloodline Covenant uses the Enthralling Filament, Hungering Earth the Sanguine Formation and Possession Adipocere. `noTwoRitesShareAMapIcon` (`rite_kind_validation`) fails if two rites share one.
- DS-4 (2026-11-13): every rite with no Liber entry gets one line. Built 2026-11-15 for the 19 Harbinger rites (NQ-13-5); `HarbingerRiteLiberRowDataTest` fails if a Harbinger rite opens no page.
- DS-9 (2026-11-13, built 2026-11-16, B34): every Working craft has a Liber page. The 14 crafts that had none got one each in The Hematic Order (the Sporitic Thurible as the D8 Apotheos relic), and so did the floorless Dried Gourd, Hematic Stake and Mycelial Synapse. A craft page opens at the craft's Degree, on picking up what it makes, or when the craft completes. The two Armature rites now open their own lessons in a Hematic Armature chapter instead of Bloodwork Stations. `LiberCraftAndBossPagesDataTest` fails if a Working craft has no page.
- D-21, CB-15 (2026-11-13): one cost and offering pass over rites and crafts runs after play, and it checks the Covenant Throne and the Vascular Effigy feeds. The Mycelial Lantern craft feeds 750 mL (built). The pass: Not built yet.
- F-12 (2026-11-13): the answer was "re-run the GameTest, then close". Built 2026-11-15 with NQ-13-2: hand Projection into a rite-locked station is refused and costs nothing (`IBloodReservoir.takeProjectedBlood` asks `canReceiveBlood()`; the Forge's own path keeps the lock). The test is now `riteLockedStationRefusesProjectedBlood`.
- RV-22 (2026-11-13, the dead `mind_spike` map entry removed 2026-11-15): keep the Rites & Crafts tab, the special floors, the Sanguine Omen as the only rite-bound member, the Working roster's tiers and feeds, and the Somatic Loom at 3x2.
- RV-48 (2026-11-13): keep the three listed raw block edits (the collapse backlash in `HarbingerCardinalRiteEvents`, `EngramStampItem`, `CrimsonFireballEntity`).
- DS-3 (2026-11-13): per-floor ceremony mechanics (the Endure, Hold, Share and Precision archetypes) stay parked.
- D-11 (2026-11-13): the Unstained ritual system stays parked.
- DS-11 (2026-11-13): the Unstained invasion rite stays parked.
- D-6 (2026-11-13): the Chimerization offering's name and recipe stay parked (recorded in [ENDGAME_PATHS.md](ENDGAME_PATHS.md)).
- PT-44 (2026-11-13, chunk 11, built 2026-11-15): the Hematic Unbinding pays each player member `pool / members`. A member who is away gets a banked share in `BloodlineSavedData` and is paid at their next login, or at their respawn if they logged in dead. The payment is recorded in the blood ledger as `bloodline_share`.
- NQ-11-9 (2026-11-14, built 2026-11-15): the ledger's free Disband still pays no pool blood. Only the Hematic Unbinding pays shares.
- NQ-11-10 (2026-11-14, built 2026-11-15): a share is capped by room in the member's vessel, online or at login. The rest is lost, and the line says how much was paid.
- PT-66 (2026-11-13, chunk 11, built 2026-11-15): the Eternal Covenant seal lives in the degree attachment (`eternal_covenant_sealed`), so it survives death and the rite stays sealed. The root key is gone.
- NQ-11-14 (2026-11-14, built 2026-11-15): the Unstained cure clears the Eternal Covenant seal with the rest of the Harbinger progress, so the rite can be done again after a return.
- NQ-13-1 (2026-11-14): the rite boundary drain stays 25 mL a tick, as built, with 1 damage a tick.
- NQ-13-2 (2026-11-15): hand Projection into a rite-locked station is refused and costs nothing (F-12).
- NQ-13-3 (2026-11-15): the rite-ground claim stays as built: running rites only, plain Projection only. It is reopened only if play shows a Sculpting breaking a running rite's floor.
- NQ-13-4 (2026-11-15): the other two icon pairs take existing items (Hungering Earth the Sanguine Formation, Possession Adipocere), with a no-shared-icon check.
- NQ-13-5 (2026-11-15): the Liber lines cover the 19 Harbinger rites with no entry; the 18 Working crafts and the 5 Unstained rites are left out.
- UN-48 (2026-11-15): the solo ordeal stall was diagnosed first. It was a harness bug, so no lever was needed.
- PT-38 (2026-11-13): the first Hematic Unbinding warns at activation, before the Nether Star is taken. The leader's first activation says the warning and starts nothing; a second inside ten minutes (`HematicUnbindingRules.CONFIRMATION_TICKS`, 12,000 ticks) starts the rite, and its completion dissolves the line with no second warning. One Nether Star in all. The window is session state (`HematicUnbindingRules`, cleared by `TransientServerStateEvents`), not root persistent data.

## Calls made without the user

Overnight build, package P09 (2026-11-14), all the builder's calls:

- CN-2 is built at its narrowest reading: running rites only, plain Projection only. Sculptings, ground marks (ash trails, vein nodes, gouges) and a formed floor with no rite running are not claimed. It widens only if play shows a Sculpting breaking a running rite's floor (NQ-13-3).
- PT-32: when several rites above the player match the floor, the line names the lowest Degree. The "You have already attained" line comes from a second pass over Harbinger rank rites the player has passed (B25).
- PT-68: "the floor wants one" is read from data. The Formation seats when the floor formed at the Focus is accepted by at least one rite whose medium is the Sanguine Formation, which today is every Working floor tier. On a Threshold floor a plain click still tries the floor and says no rite matches.
- PT-67: the refusal line was reworded to "The Chamber does not answer, so no visit can begin. The rite will not take hold." It is said at the Focus, before anything starts. A rite refused at its finale waits, as every refused finale does.
- FX-12: the Bloodline Covenant's new icon is an existing item (the Enthralling Filament, one of its offerings), not drawn art.
- FX-11: the hub label is a new key, "Rites & Crafts", wrapped to two lines inside the hub. Turning the Qliphoth motif also changed the south-facing floor: its husk swirls turn the other way and the halo tint shifted.
- FX-10: the codec also writes each anchor's resolved `x` and `z`, so a `layout` rite reads back where its anchors stood.
- PT-88 (B25): the solo stall was the harness's aim rule. It aimed at the first anchor needing anything, by index, so a full but damaged anchor came before a dry, damaged one. The dry anchor's deficit kept re-damaging the ring, so the repairs never ended and the dry anchor was never fed. A player sees the dry anchor, so this was not a game bug. The harness now aims at dry anchors first (undamaged, then damaged), then repairs. `apotheosRiteWithoutRescue` is required, and the logged ARRANGE rescue and its deadlock wrapper are gone (moved to `removed_assets/`).
- FX-11 (B25): the tints are my pick: bone `0xFFC8BC9C` for Floorless crafts and hematic iron `0xFF8C9DB8` for Making. `RecipeMapPurposeGroupDataTest` keeps every pair of groups at least 48 apart in RGB.
- DS-4 (B25): the line homes. The seven Puppeteer ordeals go on their `troupe_*` pages, the three vessel rites on a new page "Blood Gourds", Bloodline Recall, Covenant Vigil and Eternal Covenant on The Historical Record, Hematic Succession and Mnemonic Restoration on Mnemonic Ambergris, the two Armature rites on Bloodwork Stations (moved to their own lessons by DS-9, B34), Possession on The Source and Visitation on The True Purpose. Succession rites now open their page on completion; station upgrade rites open theirs on eligibility, as the other upgrade lessons do.
- PT-67 (B25): the visit path is proven by `chamberRiteReviewClient` (`ChamberRiteClientReview`) on a world with the Chamber loaded, not by a GameTest.

Stage 2 to 4 (`2026-10-18-rite-merge-stages-2-4-design.md`, all the builder's calls). The owner kept the Rites & Crafts tab, the special floors and the Sanguine Omen as the only rite-bound member (RV-22). Still unreviewed:

- The five JEI categories, their icons, and Succession keeping its own category.
- Rite space geometry (the brazier cube), one per Focus with latest purpose winning, release on floor change or Focus removal but not chunk unload, Overworld saved data, and the mote rendering.
- The rite-bound tag set and the three enforcement rules (inert blocks, refused items, 5-second unravel for entities).

Stage 1 (`2026-10-14-rite-purposes-and-working-crafts-design.md`, approved in conversation but authored by the session): the owner kept the roster's tiers and feeds and the Somatic Loom at 3x2 (RV-22). Still unreviewed: each craft's medium and the offerings marked "new" ("a first pass and can be tuned freely"), which the cost and offering pass will judge (D-21). The roster is eighteen crafts now: Mind Spike, the Visceral Mirror and the Semi-Sentient Construct are gone and the Morphling Cradle was added.

Possession (`docs/archive/tracks/possession/01-rite-offerings.md`): Possession borrowing the Qliphoth grand floor as a `special` `bond` rite, and the `optional_offerings` matcher hook. The brainstorm-approved per-rite design was the session's within the possession tracks.

## Open questions

Open questions live in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md). The one that belongs to this doc is NQ-10-3 (PT-31: the Sporitic Thurible is a D4 Working craft today, and the question decides whether it leaves that tier; the endgame area owns the row).

## Proof owed and known gaps

Ruled and not built (the task numbers are in [chunk 13](owner_answers/13-rites-and-crafts.md)):

- D-21, CB-15 (T14): no cost and offering pass has been run. It waits for play; the starting tables are in chunk 13.

Known failing or unproven:

- PT-88: `apotheosRiteWithoutRescue` passed in the B25 runs. It failed in most runs before, so a later run that fails it is news.
- The Chamber rite's visit path (PT-67) is proven only in a review client; the GameTest server has no Chamber.

For the owner to play, look at or read:

- Play the rites and crafts, then judge feeds, anchor costs and offerings (D-21). Start with the Covenant Throne at 8000 mL, the Sanguine Vigil craft at 6500 mL and the Mycelial Lantern at 750 mL.
- Cast a grand rite alone and judge the ordeal's weight (PT-88).
- Watch whether a Sculpting or a ground mark breaks a running rite's floor (NQ-13-3).
- Look at the turned Qliphoth motif: `build/rite-floor-client-review/screenshots/rite-floor-5.png` (south) and `rite-floor-7.png` (east).
- Look at the "Rites & Crafts" hub label and the new group tints (`liber-rites-and-crafts.png` in the same folder).
- Look at the Bloodline Covenant, Hungering Earth and Possession map icons and say if any should be drawn art (FX-12).
- Read the reworded Chamber refusal, `message.hemomancy.chamber_visit.rite_unavailable` (PT-67).
- Read the 19 Liber lines and the new page "Blood Gourds" (DS-4). The Possession and Visitation lines touch the 2026-10-31 rulings.

Other gaps:

- The audit's cleanup pass noted the floor and staff-melt render types were rebuilt once per frame per Focus; `RenderTypeCachingGuardrailTest` now rejects per-frame builds, and the reference says render types are cached, but no live-client proof of the frame cost is recorded (`CARDINAL_RITE_AUDIT.md` "Known follow-up").
- Shader changes (membrane motifs, daemon volume, funnel) need a client launch to prove compilation; `riteFloorReviewClient` is the launcher.
- The specs under `docs/superpowers/specs/` are tracked; an old audit header that calls stages 2 to 4 "uncommitted" is a dated snapshot (their code is in the history).

Guardrails that hold the system: `RiteKindGuardrailTest`, `ActiveCardinalRiteNbtLayoutTest`, `CardinalRitePurposeDataTest`, `CardinalRiteLayeredRecipeDataTest`, `WorkingCraftRosterDataTest`, `SpecialFloorDataTest`, `RecipeMapPurposeGroupDataTest`, `WorldRiteFinaleDataTest`, `CardinalRiteBrazierSpaceTest`, `BrazierMountTest`, `CardinalRiteGashRulesTest`, `CardinalRiteFloorDesignDataTest`; GameTests `rite_kind_validation`, `WorkingCraftGameTests`, `BrazierMountGameTests` (`docs/architecture/ARCHITECTURE_STANDARDS.md`; §5.3).

## History

| Date | What | Source |
|---|---|---|
| 2026-10-12 | Read-only audit: 19 bugs, five lifecycles told apart by scattered checks, 39 of 52 ceremonies without an ordeal. Overnight cleanup fixed every §2 bug but #14 (reworked) and #16/#17 (client), unified termination, completion effects, zones and brazier rites. Floors lost copper, sockets rebuilt, consecration membrane added. | [audit](archive/audits/CARDINAL_RITE_AUDIT.md), [floor validation](validation/CARDINAL_RITE_FLOORS_2026_10_12.md) |
| 2026-10-13 | World escalation stage; ring gating one ring per phase; daemon as a hooded haze, then superseded by the ray-marched smoke volume. | [escalation](superpowers/specs/2026-10-13-cardinal-rite-world-escalation-design.md), [ring gating](superpowers/specs/2026-10-13-cardinal-rite-ring-gating-design.md), [haze](superpowers/specs/2026-10-13-cardinal-daemon-haze-design.md), [volume](superpowers/specs/2026-10-13-cardinal-daemon-volume-design.md) |
| 2026-10-14 | Stage 1: purposes, 17 rites rehomed, twenty machines moved to Working crafts, structure offerings removed, `RiteOfferingEscrow` shared. Mounted braziers and the rite cube. | [purposes and crafts](superpowers/specs/2026-10-14-rite-purposes-and-working-crafts-design.md), [braziers](superpowers/specs/2026-10-14-mounted-braziers-and-rite-cube-design.md) |
| 2026-10-18 | Stages 2 to 4: one Rites & Crafts map and per-purpose JEI; the three special floors; rite spaces and `rite_bound` tags with the Sanguine Omen. | [stages 2 to 4](superpowers/specs/2026-10-18-rite-merge-stages-2-4-design.md) |
| 2026-10-23 | Consolidation step 11: `RiteKind` strategy, per-kind state objects, `CardinalRiteActivation`; Blood Structure crafting moved out of the key-press packet (commit `b6c7bd846`). | [standards](architecture/ARCHITECTURE_STANDARDS.md) |
| 2026-10-29 | Possession phase 2: Possession becomes a `special` `bond` rite on the borrowed Qliphoth grand floor with `optional_offerings` through the braziers. | [possession track 1](archive/tracks/possession/01-rite-offerings.md) |
| 2026-11-02 | Playthrough suite fixes: rite recovery waits for a dead owner and counts only stacks that land (`CardinalRiteSavedData.deliverRecovery`); a Sanguine Formation medium seats on a sneak-click whatever the off hand holds (`SanguineFormationItem.useOn`). Open late questions: D-38 [D5-4], D-44 [D5-11], D-66 [D6-3], D-67 [D6-5], D-68 [D6-6], D-88 [CHAIN-1]. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) |
| 2026-11-13 | The owner answered every rite row on the question sheet. The answers are recorded under "Rulings that bind"; the review's plan and tables are chunk 13. | [chunk 13](owner_answers/13-rites-and-crafts.md) |
| 2026-11-14 | Overnight build. Package P09: crafts count down once per server tick, rite ground refuses every player's plain Projection, the Degree line at a floor, the Chamber rite refuses with no visit, a Formation seats on a plain click, fill before repair, the codec round trip, the Qliphoth motif turns with the floor, the "Rites & Crafts" hub label, the Lantern craft at 750 mL, the Bloodline Covenant's own map icon. Package P12: the Hematic Unbinding warns at activation (PT-38). | [build log](owner_answers/BUILD_LOG_2026-11-14.md), [chunk 13](owner_answers/13-rites-and-crafts.md) |
| 2026-11-14 | Second docs pass: the planned defaults of chunks 13 and 11 that touch rites are listed under "Calls made without the user", and the pointer names NQ-10-3 beside NQ-13-1. | [chunk 11](owner_answers/11-bloodline-and-throne.md) |

What the audit asked for and where it stands: bug pass, server unification (`terminate()`, effect registry, data-driven waves, `RiteZone`) and client unification landed in the 2026-10-12 pass; the `RiteVariant` interface landed as `RiteKind` in step 11; effects (reactive membrane, culmination styles, collapse choreography, wave telegraphs, daemon geometry, `rite.*` sounds) landed on 2026-10-12 and 2026-10-13; gameplay archetypes, sigil modifiers beyond the Reservoir refund, the participatory procession and the Unstained stillness ceremony have not.
| 2026-11-15 | Package B25: the rite lock stops hand Projection (F-12), the "already attained" line, the 19 Liber lines, own map icons with a check, new group tints, the merged editor preview, the PT-88 harness fix and the Chamber visit review client. | [build log](owner_answers/BUILD_LOG_2.md) |

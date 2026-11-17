# 09. Manipulation visuals, particles, texels, shaders and animation time

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P16). Sprite sizes pinned by `ItemSpriteSizeTest` (B19). |
| T2 | Built (P16). |
| T3 | Built (P16). The unowned construct scene was added to `summonParticleReviewClient` (B19). |
| T4 | Built (P17). Swap only: the audit's rings and falls kept the old random spread (parked with VX-7). `ParticleSpawnSiteGuardrailTest` pins each rite's `UnstainedRiteParticles` call (B19). |
| T5 | Built (P16). The renderer's particle-free rule is pinned by `ParticleSpawnSiteGuardrailTest`; the station camera now sits inside its cleared box (B19). |
| T6 | Built (P18, B19). Scenes for batches B and E (`sweepParticleReviewClient`) and the D2 orb, vial and polyp and D1 jelly, snail and cuttle were added in B19; the four audit shapes and the lower Thrall and Drudge bursts too. Not staged: the Circus overlay, the Curved Horn emitter, the Thurible coal, the Elytra effect, the Silent Archon negation and the Living Sickle cut (see B19 log). |
| T7 | Built in part (B19): the lashes and eight held forms captured first and third person and from a second player, the form roster, and the dated re-grade addendum. The casting harness was not run (it needs a server and two Gradle clients at once). |
| T8 | Built (P16). Left: the stale V-3b sentence in `AGENTS.md`. |
| T9 | Built (B19). |
| T10 | Built (B19). Shader proof: client launches on NVIDIA, no shader error. The per-cast seed is tested by `CueSeedTest`. |
| T11 | Not built. After T7 and the owner's look at the A rows (VX-17). Three cut members wait on the VX-1 conflict rows, two on NQ-09-2. |
| T12 | Built (B20). `texel_density.py` and `texel_uv_rescale.py` in `tools/model_export/`; 84 models rescaled; 130 flagged models before, 20 after. The post-audit models were measured: only the 3-wide Specimen Vat reads low (9 to 12, faces near 3 blocks long). |
| T13 | Not built. Waits on NQ-09-4. |
| T14 | Built in part (B20): staff fittings (the real generator is the local `tools/staff_fittings/gen_staff_fittings.py`), Syringe and rack, cylinders, flywheel, Speculum, Pulse Pylon item (full scale), Sarcophagus item (three-quarter scale, the bound limit). Hydra Crown already at 17. Marionette Crossbar left for T15 (renderer scale bake). |
| T15 | Not built. Waits on NQ-09-5, after T12 to T14. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

The owner approved almost everything in this chunk as recommended: the 13-batch visual upgrade order, the vanilla particle sweep (Unstained batch now), a verdigris colour, the four texel batches, new shader shapes, per-cast seeds, and the three pending architecture calls (A-4, V-3b, V-3c).

The largest pieces of work are the particle sweep (133 call sites in 57 files to replace), the visual batches 2 to 12 (several sessions each for the Ductilis threads and the capstones), and texel batch T4 (ten models with no generator).

Surprises from the code:

- The particle allow-list has not moved since the audit. It still pins 73 files and 189 matches, and a recount of `src/main/java` today matches it row for row. No sweep work has started.
- 18 of the 115 visual grades are stale. The 2026-11-12 rework cut six held forms, rebuilt eight with their own mechanic, and rebuilt the three lashes. Three approved batch members would rebuild strand forms the owner cut (a Mortem strand brand, a Blood Eclipse spray, a Flammeus plant). They are on the conflict sheet and are not planned.
- Tendon Line already draws for every tracking player (built 2026-11-11 after the audit). That part of batch 7 is done.
- A-4 lists 32 classes today, not 29. Only 13 are real wall-clock animations in the world or on items. The rest are screens, HUD smoothing, cache stamps, fallbacks, or the dimension veil that has no level clock.
- A Ferric review launcher already exists (`tools/review/ferric-ductilis-review.init.gradle`), so one of the four "missing" review clients in FX-29 is partly there.
- No texel row names a HUD asset, and no model in batches T1 to T4 has changed since the audit. The Living Syringe rebuild (2026-11-08) was before the audit, so its row is current.
- VX-1 puts the in-game review of the A rows last (batch 14). VX-17 says review them before more A work. I read VX-17 as the later, more specific ruling, so the review moves ahead of batch 2.

Counts: 28 rows. BUILD 19, KEEP 5, DONE 0, MOOT 0, CONFLICT 0, UNCLEAR 0, OWNER 4, PARKED 0. One BUILD row (VX-1) has three members on the conflict sheet.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| VX-1 | The 13-batch upgrade order stands. Batches 1 to 3 first. Stale grades get a re-grade, not a rebuild. | BUILD | `docs/audits/2026-11-11-visual-balance/manipulation-visuals.md` batch table, `common/init/ManipulationInit.java` (115 registered, same as the audit), `common/manipulation/verb/Shapings.java`, `client/render/world/StrandRenderer.java`, file times under `common/manipulation` and `client/render/world`. 18 rows are stale (table in T7). Three members conflict with the 2026-11-12 rework. | T3, T7, T11 |
| VX-3 | Keep Vascular Dowsing as a UI-like locator. Low priority. | KEEP | `client/render/world/ManipulationVisualRenderer.java` `case ORE` still draws `VisceralMesh.locatorBox`. | none |
| VX-4 | Add new shader shape indices for Animus FOCUS and SLASH and the Umbra shapes. Leave 4 and 7 alone. | BUILD | `shaders/core/world/manipulation_animus.fsh` handles shapes 0, 1, 2, 6, 9 only. `manipulation_umbra.fsh` handles 2, 0 and 7. `LuxUmbraGeometry` ids run 0 to 9. No shader file changed since the audit. | T10 |
| VX-5 | Seed shared-tier casts per cast. | BUILD | Seeds still come from `packet.entityId()` or `from.hashCode()` in `AnimusMortemGeometry` line 33, `ThermalGeometry` line 45, `LuxUmbraGeometry` line 48, `FerricDuctilisGeometry` line 43 and `ManipulationVisualRenderer` line 692. | T10 |
| VX-6 | The 19 honest vanilla sites stay, each with its reason in the allow-list. | BUILD | `src/test/resources/architecture/vanilla-particle-allow-list.txt`: 73 rows, 189 matches. 6 rows (19 matches) carry an honest reason today. 67 rows (170 matches) still say "pending". 11 more rows need an honest reason. | T1, T3 to T6 |
| VX-7 | Sweep the Unstained particles now. Swap only, no redesign. | BUILD | Recounted the 17 Unstained files: 49 call sites, 2 stay (`CopperBrushingHandler`, `WhiteHumorPurificationEvents` line 243). Same as the audit. Only `VerdigrisMothEntity` moved (lines 148 and 151 are now 150 and 153). | T4 |
| VX-8 | New verdigris colour (86, 160, 132), Unstained effects only. | BUILD | No constant with that value exists. No shared palette class exists. 108 files declare private `ParticleColor` constants. The Verdigris Aura effect colour in `EffectInit` is `0x4A8B6F` (74, 139, 111), a different green. | T2 |
| VX-9 | Delete the Scriptorium's `ENCHANT` letters. No glyphs unless asked for later. | BUILD | `common/block/harbinger/crafting/EnzymaticScriptoriumBlock.java` line 109 still spawns `ParticleTypes.ENCHANT`. The Will-absorption glow on line 111 flies the same path. | T1 |
| VX-10 | Texel batch T1: UV rescale of the kitbashed JSON models. | BUILD | All named models exist under `assets/hemomancy/models` (25 Living Staff files, Baghnakh, Barbed Blade, 6 crossbow files, Blood Crystal, Puffball, 4 podiums, 26 Phlebotomist files). None changed since the audit. The measuring scripts were never committed. | T12 |
| VX-11 | Texel batch T2: repaint textures at their layout size. | BUILD | All twelve textures exist at the sizes the audit gives (elytras 128x64, Covenant Mantle and Chalybeate 256x128, Whale 64x64, Rampart and Polyp 256x256, Sanguine Omen 256x256). `CovenantThroneModel` still borrows `entity/model_sanguine_monolith.png`. None changed since the audit. | T13 |
| VX-12 | Texel batch T3: regenerate generator models at density 1. | BUILD | Generators exist: `tools/model_export/staff_fitting_sprites.py`, `living_syringe.py`, `hydra_crown.py`, `cylinders.py`, `vial_centrifuge.py`, `terrestrial_speculum.py`, `saint_sarcophagus.py`, `pulse_pylon.py`, and `tools/circus/build_marionette_crossbar.py`. The Living Syringe generator and models are dated 2026-11-08, before the audit. | T14 |
| VX-13 | Texel batch T4: bake renderer scale into geometry, in its own pass. | BUILD | Renderers and textures exist and are unchanged. Only `tools/model_export/unstained_weapons.py` covers a row. Ten rows have no generator. VX-14 removes the two headpieces from this batch. | T15 |
| VX-14 | Accept the Hemolymphopoda and Lantern Tick headpieces as creatures worn as hats. | KEEP | `client/render/layer/player/HemolymphopodaHeadpieceLayer.java` scale 0.4, `LanternTickHelmetLayer.java` `HEAD_SCALE` 0.56. Both unchanged. | none |
| VX-17 | Review every A-grade visual in game before more A work. | BUILD | No A row has been seen in play. 38 of the 43 A rows have geometry classes untouched since the audit. 5 have a changed held form. A build session can prepare the captures. The owner must watch them. | T7 |
| FX-6 | Resize three sprites to 16 px. | BUILD | `textures/item/barbed_splinter.png` 18x18, `incandescent_enzyme.png` 18x18, `hematic_condenser_kit.png` 15x16. Still off. | T1 |
| FX-7 | Delete the commented-out particle blocks in two armour items. | BUILD | `AbstractSpecialBloodLustArmorItem.java` lines 45 to 49 and `BloodLustArmorItem.java` lines 59 to 63 still hold the dead block. Each is one allow-list row. | T1 |
| FX-8 | Move the Morphling Incubator renderer's particle spawns into the client tick. | BUILD | `MorphlingIncubatorRenderer.java` spawns in `render` at lines 99 (purple ember), 104 (item crumbs) and 131 (`MYCELIUM`). `MorphlingIncubatorBlockEntity.clientTick` exists and is empty. | T5 |
| FX-29 | Add the missing review clients with their batches. | BUILD | `gradle/review-clients.gradle` has no Unstained, station-scene or `fungalStationReviewClient` row. `FungalStationClientReview.java` exists with no row. A Ferric launcher exists (`tools/review/ferric-ductilis-review.init.gradle`, `FerricDuctilisReview.java` stages Rampart, Spikes and Magnetism). No scene stages the Phantasmal Echo or the Blood Thrall. | T3, T4, T5, T6 |
| RV-23 | Keep cast impacts, basics and infusion as built. | KEEP | `CastImpactRules.THROTTLE = 5` (file dated 2026-11-10). `ManipulationVisualRenderer.bodyAnchored` still names six geometries. The rework did not touch them. Lash hits still go through `CastImpacts.deliveredAs` in `StrandHits`. | none |
| PR-7 | Watch the 43 A-grade visuals in the casting and outpouring review clients. | OWNER | The PLANT and BRAND half of the question is void: no registered memory carries a plant or brand since 2026-11-12. The casting harness is a launcher (`tools/review/casting-animation-review.init.gradle`), not a row. | T7 |
| PR-8 | One played session for impacts, basics and infusion veins. | OWNER | Harness captures are the only evidence (`docs/MANIPULATIONS_AND_CASTING.md` "Proof owed"). Nothing to build first. | none |
| PR-26 | Check particle replacements per batch in a review client. | OWNER | Every count, size and speed in the audit is a starting value. The clients come from T3 to T6. | T3 to T6 |
| PR-27 | Check texel batches per batch. | OWNER | No review client shows armour, elytras or hand weapons. T13 adds one for the Silent Archon leggings question. | T12, T13 |
| PR-28 | Accept NVIDIA-only shader proof for now. | KEEP | No shader file changed after 2026-11-11 11:00. New shader work in T10 still needs a launch on this GPU. | none |
| D-20 | Casting v2 as built. No animation library. | KEEP | Nothing under `client/player/tracks` changed. The lash flick (2026-11-12) is a mouse gesture read by `LashCamera`, not the dropped quick-cast flick animation. No conflict. | none |
| A-4 (arch) | Freeze every wall-clock animation under `/tick freeze` except screen chrome. | BUILD | `AnimationTimeGuardrailTest.WALL_CLOCK_USERS` lists 32 classes today, and 32 are found in source. The sheet says 29. Three veil classes were added on 2026-11-04. Split in T9. | T9 |
| V-3b | Ferric uses 127 seed tiles. | BUILD | `FerricDuctilisGeometry.java` line 28 `FERRIC_SEED_TILES=128`, used at line 347. `MaterialQuadGuardrailTest` asserts the 128 text. | T8 |
| V-3c | Write Outpouring surface marks through `MaterialQuads`. | BUILD | `client/render/outpouring/OutpouringRenderer.java` lines 307 to 312 still set UVs by hand and round alpha. It is the one entry in `MaterialQuadGuardrailTest.HAND_WRITTEN`. | T8 |

## Plan

Order: small deletions first, then the palette, then the sweep batches with their review clients, then the A-row review, then the architecture and toolkit work, then the visual batches, then texels.

### T1. Small fixes and honest reasons (size S)

- Rulings: VX-9, FX-6, FX-7, VX-6
- Change: Delete the `ENCHANT` call in `EnzymaticScriptoriumBlock` (line 109). Delete the two commented particle blocks. Resize `barbed_splinter` and `incandescent_enzyme` from 18x18 to 16x16 and `hematic_condenser_kit` from 15x16 to 16x16 (repaint the edge, do not stretch). Rewrite the reason on the eight allow-list rows whose only sites are honest: `MnemonicCandleBlock` (the candle's own smoke), `SanguineMonolithBlockEntity` (ground debris), `VigilArchiveBlockEntity` (vanilla shrieker), `CopperBrushingHandler` (vanilla copper scrape), `ChewWoodGoal` and `MyelinBorerWorkEffects` (block debris), `BloodBoltEntity` (potion cue of a tipped bolt), `EntityFlyingCharm` (bubbles under water).
- Files: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/EnzymaticScriptoriumBlock.java`, `common/item/harbinger/armor/AbstractSpecialBloodLustArmorItem.java`, `common/item/harbinger/armor/BloodLustArmorItem.java`, the three PNGs under `src/main/resources/assets/hemomancy/textures/item/`, `src/test/resources/architecture/vanilla-particle-allow-list.txt`
- Tests: `VanillaParticleRatchetTest` (three rows leave the list: Scriptorium and the two armour items). The list goes from 73 rows and 189 to 70 rows and 186. `tools/model_export/export_condenser_kit_sprites.py` reads `hematic_condenser_kit.png` as a reference, so run its check after the resize.
- Docs: none
- Depends on: nothing

### T2. Verdigris and an Unstained palette (size S)

- Rulings: VX-8
- Change: Add one shared palette class, new file `common/particle/HemoPalette.java`, beside `HemoParticleData`. It holds `VERDIGRIS = new ParticleColor(86, 160, 132)` and the still-water colours the Unstained sweep reuses: `STILL_WHITE` (244, 246, 248) and `STILL_SILVER` (201, 209, 217), today private in `StillArtInit`, and `PALE_GLOW` (214, 232, 236), today private in `PaleConsecrationEvents`. Both sides can read it. Add `HemoParticleData.letheanDrip()` for `ParticleInit.lethean_drip`, which the sweep uses about ten times. Leave the Verdigris Aura potion colour (`0x4A8B6F`) alone.
- Files: new `common/particle/HemoPalette.java`, `common/particle/HemoParticleData.java`, `common/init/StillArtInit.java`, `common/rite/unstained/PaleConsecrationEvents.java`
- Tests: a JVM test that pins the four RGB values. A new architecture guardrail that lists the files allowed to name `HemoPalette.VERDIGRIS`, so the colour cannot reach blood-side effects (the owner has ruled twice against copper tones on Harbinger work).
- Docs: `docs/architecture/ARCHITECTURE_STANDARDS.md` (guardrail index row), `docs/MANIPULATION_VISUALS.md` (one line under Colour: verdigris is Unstained only)
- Depends on: nothing

### T3. Ferric construct sweep, visual batch 1 (size S)

- Rulings: VX-1 (batch 1), VX-6, FX-29, PR-26
- Change: Replace `SQUID_INK`, `ASH` and the redstone dust in the three Ferric constructs with `HemoParticleData.darkGlow`, `bloodCell` and grey `emberSpark` (120, 118, 124), as the audit's batch D2 table gives. Put the five calls in one shared helper so they live once. This now also covers the Ferric Spikes held run, because `FerricRunShaping` spawns the same `EntityIronSpike`.
- Files: `common/entity/summon/EntityIronPillar.java`, `EntityIronSpike.java`, `EntityIronWall.java`, a new helper beside them, the allow-list (three rows of 6 leave)
- Tests: `VanillaParticleRatchetTest`. Review with the existing launcher `tools/review/ferric-ductilis-review.init.gradle` (`FerricDuctilisReview` stages all three casts). No new client is needed for this batch.
- Docs: `wiki/Ferric-Ductilis.md` if it describes the ink and ash
- Depends on: nothing

### T4. Unstained particle sweep, batch C (size L)

- Rulings: VX-7, VX-8, VX-6, FX-29, PR-26
- Change: Swap 47 call sites in 16 files to `glow`, `emberSpark`, `letheanDrip` and `HemoPalette` colours, as the audit's batch C table gives. Counts and speeds are the audit's starting values. Halve `sendParticles` speed figures when porting, as the audit notes. Keep `CopperBrushingHandler` line 103 and `WhiteHumorPurificationEvents` line 243 with the reason "vanilla copper scrape on a copper block". Add `unstainedParticleReviewClient`: a new gameTest review class that places the Altar, Podium, Bells, Retort, Condenser, Lattice and a Virid Salis trail and fires one rite completion per family (Baptism, Silver Dawn, Glass Lungs, Moon-Washed Copper).
- Files: `common/rite/unstained/UnstainedCardinalRiteEvents.java` (20 sites), `common/block/harbinger/functional/UnstainedPodiumBlock.java`, `common/block/unstained/functional/AltarOfCleansingBlock.java`, `PaleSilverBellsBlock.java`, `common/block/unstained/crafting/PallidRetortBlock.java`, `StillwaterCondenserBlock.java`, `VerdigrisLatticeBlock.java`, `common/block/unstained/ViridSalisTrailBlock.java`, `common/effect/SilverWardEffect.java`, `VerdigrisAuraEffect.java`, `common/entity/mob/animal/VerdigrisMothEntity.java`, `common/event/ConsecrationHandler.java`, `ViridSalisTrailHandler.java`, `WhiteHumorPurificationEvents.java`, `common/item/unstained/LetheanChaliceItem.java`, `PaleSilverBellItem.java`, `gradle/review-clients.gradle`, a new review class under `src/gameTest/java/com/vincenthuto/hemomancy/gametest/`, the allow-list
- Tests: `VanillaParticleRatchetTest` (15 rows leave, 2 keep one match each). `ParticleDataGuardrailTest` must stay green with the new factory.
- Docs: `docs/TESTING.md` (new review client), `wiki/Unstained-Path.md` if it names the old particles
- Depends on: T2. The Unstained ritual system stays deferred. This task does not redesign any rite visual.

### T5. Harbinger stations sweep, batch A, with the Incubator fix (size M)

- Rulings: VX-6, FX-8, FX-29, PR-26
- Change: Replace 14 sites in 7 files (Alembic, Suture Node, Engram, Armature, Incubator block entity and renderer, Crucible) as the audit's batch A table gives. Move all three per-frame spawns out of `MorphlingIncubatorRenderer.render` into `MorphlingIncubatorBlockEntity.clientTick`: the purple ember (line 99), the item crumbs (line 104, stays vanilla) and the drifting mote (line 131, becomes `emberSpark` about once per 4 ticks). Add a `fungalStationReviewClient` row for the existing `FungalStationClientReview` (property `hemomancy.fungalStationReview`). Add a small station scene for the Armature, Alembic, Suture Node and Engram.
- Files: `common/block/harbinger/crafting/GhastlyAlembicBlock.java`, `common/block/harbinger/functional/HematicSutureNodeBlock.java`, `common/block/harbinger/rite/EngramBlock.java`, `common/tile/harbinger/crafting/HematicArmatureBlockEntity.java`, `MorphlingIncubatorBlockEntity.java`, `MycelialCrucibleBlockEntity.java`, `client/render/tile/harbinger/crafting/MorphlingIncubatorRenderer.java`, `gradle/review-clients.gradle`, a new review class, the allow-list
- Tests: `VanillaParticleRatchetTest`. `CommonClientIsolationTest` must stay green (the client tick may use `level.addParticle` but no client class).
- Docs: `docs/TESTING.md`
- Depends on: T1 (the Scriptorium row is already gone)

### T6. The rest of the sweep: batches B, D1, D2 and E (size L)

- Rulings: VX-6, FX-29, PR-26
- Change: Replace the remaining 56 sites as the audit tables give: Chamber of Will theme change (B), mobs and the Circus overlay (D1, overlay stays subtle), Phantasmal Echo, Blood Thrall, Directed Blood Orb, Hemolytic Vial and Morphling Polyp (D2), armour abilities, effects and events (E). Rewrite the last three mixed rows to honest reasons (`MorphlingIncubatorRenderer` item crumbs, `ChoirKeeperEntity` chorus crumbs and ender eye, `WhiteHumorPurificationEvents` copper scrape). Add one summon scene for the Echo and the Thrall, and one ecology scene for the Choir Keeper, Cicada, Peacock Spider, Mortarbound, Will and Drudge. `LayerEvents` line numbers moved by two since the audit (the `SOUL` emitter is now line 443).
- Files: the 30 files with a replacement in the audit's B, D1, D2 and E tables (all present on the allow-list today), `gradle/review-clients.gradle`, two new review classes, the allow-list
- Tests: `VanillaParticleRatchetTest`. End state: 17 rows, each with an honest reason, none saying "pending". Check `BiomeInit` line 109 first: a biome's ambient particle goes through a codec, so confirm `sporiticSpore` serialises before swapping.
- Docs: `docs/TESTING.md`, `docs/architecture/ARCHITECTURE_STANDARDS.md` (the A-33 row can drop "pending"), `AGENTS.md` only if the allow-list wording changes
- Depends on: T2

### T7. A-row review pack and the re-grade (size M)

- Rulings: VX-17, VX-1, PR-7
- Change: Before any batch from 2 onward, capture every A row in a client and hand the sheets to the owner. Use the casting harness (`tools/review/casting-animation-review.init.gradle` with `CastingClientAcceptance`), `outpouringReviewClient`, `strandReviewClient` and the Ferric, Animus-Mortem and visceral launchers in `tools/review/`. Re-grade the 18 stale rows from source and from `strandReviewClient` captures, and write the result as a dated addendum, not an edit of the audit. Do not rebuild any 2026-11-12 work.

  Stale rows (grade or upgrade note written before the code changed):

  | Manipulation | Audit said | What changed after the audit | What to do |
  | --- | --- | --- | --- |
  | blood_lash | A | Lash rebuilt 2026-11-12 (tap, orb, thrown whip) | Re-grade |
  | searing_lash | A | R cast untouched. Strand lash rebuilt. | Re-grade the lash half |
  | gloam_laceration | B, batch 9 quick cut | R cast untouched. Lash is now three tails, faint to others. | Batch 9 entry stands for the R cast. Re-grade the lash. |
  | blood_needle_fan | A | Held spray cut. R cast untouched. | Grade stands for the R cast |
  | blood_needle_lance | A | Held jet replaced by a hovering volley (`VOLLEY` accent) | Re-grade the held form |
  | cruor_javelin | A | Held jet replaced by a drawn and thrown entity with recall (`JAVELIN` accent, `CruorJavelinEntity`) | Re-grade the held form |
  | glacial_grasp | B, batch 4 | Held plant replaced by an ice hand (`GRASP` accent). This is close to the upgrade the audit asked for. R cast still shares `case STILLNESS,ICE,CRYOGENIC_PULSE`. | Re-grade. Batch 4 keeps only the R cast split. |
  | hematic_flare | B, batch 8 | Held jet replaced by a searchlight (`BEAM` accent). R cast `FLARE` untouched. | Batch 8 entry stands for the R cast. Re-grade the beam. |
  | prismatic_reproof | B, batch 8 | Held spray cut. The note says "like the spray". | See NQ-09-2 |
  | blood_eclipse | B, batch 9 spray | Spray replaced by a carried dome (`DOME` accent, `UMBRAL_FIELD`) | Conflict sheet. Re-grade the dome. |
  | crimson_flame_conjuration | B, batch 10 plant | Plant replaced by painted fire (`ERUPTION` accent) | Conflict sheet for the plant. R cast entry stands. |
  | synaptic_jolt | B, batch 11 | Spray replaced by live conductors. The held form draws only HutosLib bolts. | See NQ-09-2. Re-grade the held form. |
  | conductive_mark | B, batch 11 | Held brand cut. R cast untouched. | Batch 11 entry stands |
  | hemorrhage, insatiable_hunger, grave_debt | B, batch 5 Mortem brand | Held brand cut from all three | Conflict sheet |
  | ferric_spikes | B with vanilla, batch 1 | Held plant replaced by a steered run that spawns `EntityIronSpike` | Batch 1 stands and now covers more (T3) |
  | tendon_line | C, batch 7 | `TendonLineRenderer` loops over every player. The anchor syncs to trackers. | Visibility fix is done. Re-grade. |

  Also touched after the audit, look not known to have changed: `ThermalGeometry` (2026-11-11 14:54, every Flammeus and Congeatio shared-tier row), `CellHandStreams` (palm shared with the lashes), and the Shaping classes of Blood Binding, Furnace Veins, Pyretic Forge, Carrion Communion and Eclipse Snuff. Recheck each at the start of its batch.
- Files: no source change. Output is a new dated addendum under `docs/audits/` and capture sheets under `build/`.
- Tests: none
- Docs: `docs/MANIPULATIONS_AND_CASTING.md` ("Proof owed": record what was captured), and fix its line 84, which gives the harness path as `tools/oneoff/`. The file is in `tools/review/`. Fix the stale scene comment above `strandReviewClient` in `gradle/review-clients.gradle`.
- Depends on: the chunk that owns the lash and strand rulings (CL-20 to CL-27), so the lashes are not re-tuned under the review

### T8. Ferric 127 and Outpouring marks through the encoder (size S)

- Rulings: V-3b, V-3c
- Change: Delete `FERRIC_SEED_TILES` and call `MaterialQuads.seedTile(seed)` in `FerricDuctilisGeometry`. Write the Outpouring surface marks through `MaterialQuads.vertex`, which truncates alpha where the marks round it, so some vertices lose one alpha level (the ledger already says so). The marks are camera-relative with no pose, so pass an identity matrix.
- Files: `client/render/world/FerricDuctilisGeometry.java`, `client/render/outpouring/OutpouringRenderer.java`
- Tests: `MaterialQuadGuardrailTest` (drop the 128 assertion and the `HAND_WRITTEN` entry). `MaterialQuadsEncodingTest` holds a vertex digest that includes seeds 127 and 128 on Ferric, so the digest changes and must be re-taken on purpose. `OutpouringMarksTest` if it pins alpha.
- Docs: `AGENTS.md` (drop "Ferric keeps its own 128-tile constant until decision V-3b"), `docs/architecture/ARCHITECTURE_STANDARDS.md` line 60, `docs/architecture/CONSOLIDATION_PROGRESS.md` decisions table (V-3b and V-3c to DONE), `docs/MANIPULATIONS_AND_CASTING.md` (move both from "Open questions" to "Rulings that bind")
- Depends on: nothing. Look at Ferric plates once in `FerricDuctilisReview` afterwards: a caster whose seed wraps differently gets a different rust pattern.

### T9. Wall-clock animation onto game time (size M)

- Rulings: A-4 (arch)
- Change: Move the 13 real world and item animations onto `AnimationTime`. Leave screens alone. Give the other listed classes a permanent reason in the guardrail.

  | Group | Classes | Action |
  | --- | --- | --- |
  | Item and armour animation | `SilentArchonArmorRenderHelper`, `MemoryOfVesperItemRenderer`, `MonolithFragmentItemRenderer`, `MonolithImbuedClothItemRenderer`, `MonolithicCornerstoneItemRenderer`, `MorphicNectarItemDecorator`, `QliphothPomeItemRenderer`, `QliphothSeedItemRenderer`, `ScarPatternItemRenderer`, `CovenantThroneItemRenderer`, `HematicStakeItemRenderer`, `SanguineMonolithItemRenderer`, `SpecimenJarItemRenderer` (13) | Freeze: read game time. Add one `AnimationTime` method for item time that falls back to wall clock when there is no level, so the fallback lives inside the helper. |
  | Screens | `BloodStructureHintScreen`, `MnemonicFolioScreen`, `RiteHintScreen`, `ScryingDiagnosticsScreen`, `VascularViewScreen`, `MorphlingJarScreen`, `CraftingTabView`, `MaterialsTabView`, `RitesTabView`, `MorphlingIncubatorScreen`, `MnemonicReliquaryScreen` (11) | Stay. Screen chrome. The 20 `animTime += 0.016f` frame counters are all in screens and stay too. |
  | Dimension veil | `ChamberVeil`, `ChamberTransitionScreen`, `LidlessPassageView` (3) | Stay. No level clock exists during a dimension change. `LidlessPassageView` already uses game time when a level exists. |
  | Cache stamps, not animation | `FirstPersonPalm`, `HeldWeaponPose` (2) | Stay. They time how stale a recorded pose is. |
  | Fallback only | `MorphlingCradleRenderer`, `UnstainedGaugeOverlay` (2) | Already on game time when a level exists. Move the fallback into the new helper method so they leave the list. |
  | HUD smoothing | `CardinalRiteOverlay` (1) | Stay. It eases a progress bar by frame time. It is not an animation clock. |

- Files: the 13 renderers under `client/render/armor`, `client/render/item` and `client/render/item/tile/harbinger/functional`, `client/render/AnimationTime.java`, `client/render/tile/harbinger/functional/MorphlingCradleRenderer.java`, `client/screen/overlay/UnstainedGaugeOverlay.java`
- Tests: `AnimationTimeGuardrailTest`: the list drops from 32 to 17, each remaining entry gets a reason comment, and the "pending decision A-4" wording goes. A JVM test for the new helper method is not possible without a client, so cover it by the guardrail only.
- Docs: `AGENTS.md` ("Wall-clock animation is listed until decision A-4" becomes the rule), `docs/architecture/ARCHITECTURE_STANDARDS.md` line 89, `docs/architecture/CONSOLIDATION_PROGRESS.md` (A-4 to DONE), `docs/MANIPULATIONS_AND_CASTING.md` (move A-4 to "Rulings that bind", correct 29 to 32)
- Depends on: NQ-09-3 (items in inventory slots). The living HUD already animates on game time (`BloodVolumeOverlay` line 221), so HUD is consistent with the ruling.

### T10. Toolkit enablers, visual batch 13 (size M)

- Rulings: VX-4, VX-5, VX-1 (batch 13)
- Change: Add shape ids 10 and up in `LuxUmbraGeometry` for an Animus focus and an Animus slash, and new Umbra shapes. Add their branches in `manipulation_animus.fsh` and `manipulation_umbra.fsh`. Do not touch the code paths for 4 and 7, which Riposte uses. Move non-Riposte callers to the new ids one geometry class at a time. For seeds, mix a per-cue salt into the seed the shared tiers compute (see NQ-09-1 for where the salt comes from). Bespoke classes that already vary per cast stay as they are.
- Files: `client/render/world/LuxUmbraGeometry.java`, `AnimusMortemGeometry.java`, `ThermalGeometry.java`, `FerricDuctilisGeometry.java`, `ManipulationVisualRenderer.java`, `src/main/resources/assets/hemomancy/shaders/core/world/manipulation_animus.fsh`, `manipulation_umbra.fsh`. Check `manipulation_animus_model.json`, which is a second program in the same folder.
- Tests: `MaterialQuadsEncodingTest` digest will change where seeds change, so re-take it. `ShaderNoiseIncludeGuardrailTest` must stay green. A shader change needs a client launch to prove it compiles (`AGENTS.md`). Launch `outpouringReviewClient` or the succession review client and read the log for shader errors. Offline validators are not proof.
- Docs: `docs/MANIPULATION_VISUALS.md` (new sprite ids under Structure, per-cast seed under Animation), `docs/MANIPULATIONS_AND_CASTING.md` (close "Catalogue enabler 1" in Open questions)
- Depends on: T8 (same encoder and digest). Best done before batches 2 to 12 so new classes can use the shapes.

### T11. Visual batches 2 to 12 (size L, one or more sessions per batch)

- Rulings: VX-1
- Change: Build in the approved order, each batch to the standard in `docs/MANIPULATION_VISUALS.md`: its own geometry class hooked before `AnimusMortemGeometry`, a pure rules class, a source-contract test for the hook, JVM tests for the rules.

  | Batch | Members that still stand | Dropped or changed |
  | --- | --- | --- |
  | 2 Ductilis threads | sanguine_marionette (port to `PuppetStringsGeometry`, delete the `MARIONETTE_*` branch at `ManipulationVisualRenderer` line 701), thread_ripper, axonal_transduction | none |
  | 3 Saint canon | crimson_tithe, unclosing_eye, bloom_of_rot, endless_hour | none. The Saint fight geometry classes exist. |
  | 4 Congeatio | cryogenic_pulse, osseous_bloom, glacial_rampart, rimebound_sentence, glacial_grasp R cast split | glacial_grasp held form is new: re-grade first |
  | 5 Mortem | exsanguinate, carrion_communion, the three Lignum forms | The Mortem strand brand is dropped (conflict sheet) |
  | 6 Capstones | all six | none |
  | 7 Animus mid-tier | blood_binding family, blood_cloud family, vital_effusion, sanguine_halo, living_land strike | tendon_line tracker draw is done |
  | 8 Lux | lumen_suture, hematic_beacon, hematic_flare R cast | prismatic_reproof waits on NQ-09-2 |
  | 9 Tenebris | black_veil_covenant, void_shroud, penumbral_drift, gloam_laceration R cut | The Blood Eclipse spray is dropped (conflict sheet) |
  | 10 Flammeus | sanguine_ignition, four updrafts, pyretic_forge, crimson_flame_conjuration R cast, cauterizing_rebuke, furnace_veins, vitric_combustion | The Flammeus plant is dropped (conflict sheet) |
  | 11 Ductilis bolts | activation_potential, conductive_mark, hemolymphal_pulse, living_circuit, sovereign_instinct | synaptic_jolt quick cast waits on NQ-09-2 |
  | 12 Ferric minor | iron_retort, iron_choir, hematic_hewing, lode_draw | vascular_dowsing stays as it is (VX-3) |

- Files: new classes under `client/render/world/`, hooks in `ManipulationVisualRenderer.java`, forms appended to `common/manipulation/ManipulationVisuals.java`. Appending a form does not re-lay-out the payload, but check `PacketHandler.PROTOCOL_VERSION` rules if a packet field is added.
- Tests: per class as above. `ManipulationVanillaParticleSourceTest`, `RenderTypeCachingGuardrailTest` and `MaterialQuadGuardrailTest` must stay green.
- Docs: `docs/MANIPULATIONS_AND_CASTING.md`, `docs/HEMOMANCY_REFERENCE.md` section 8, the school wiki pages (`wiki/Animus-Mortem-Visuals.md`, `wiki/Ferric-Ductilis.md`, `wiki/Flammeus-Congeatio-Visuals.md`)
- Depends on: T7 (owner has seen the A rows), T10 for classes that want the new shapes. Batch 3 should follow the Saint overhaul chunk if that chunk rebuilds the Saint fight geometry.

### T12. Texel batch T1: kitbashed UV rescale (size M)

- Rulings: VX-10, PR-27
- Change: First commit a measuring script under `tools/model_export/` so each batch can be checked by number as well as by eye. The audit's scripts were in a temp folder and are gone. Then one script over the listed models: remove explicit `uv` on faces that use a 16 px tiling block texture, and set the 256 px `blood_fill_tiled` faces to face size over 16. Rows 1, 6, 26, 27, 28, 30, 31, 32, 33, 35, 36, 38, 40, 41, 42 of the audit's table.
- Files: about 100 JSON models under `src/main/resources/assets/hemomancy/models/item` and `models/block` (Living Staff heads, Baghnakh, Barbed Blade, Living Crossbow, Chitinite Mace, Living Blaster, Living Rifle, Blood Crystal, Puffball, Hematic Microscope, Alembic Athanor and Condenser growths, four podiums, Phlebotomist's Cabinet and Field Case, Vacant Effigy, Escharian Scyphus, Resonant Forge cylinder, Mortal Display, Rite Fragment Inscription, Semi-Sentient Construct)
- Tests: `alphaCheck` asset-path checks. Any resource test that pins these models' JSON. The Cabinet and Field Case have local generators in `tools/oneoff/` (`phlebotomists_cabinet.py`, `phlebotomists_field_case.py`), which are not version-controlled, so edit the JSON and note it.
- Docs: `wiki/Phlebotomists-Cabinet-Validation.md` if it shows the old fill
- Depends on: nothing. The owner checks each look (PR-27): `staffMiningReviewClient` for the Staff, `harbingerSitesReviewClient` for the Cabinet.

### T13. Texel batch T2: repaint at layout size (size M to L)

- Rulings: VX-11, PR-27
- Change: Bring each PNG to its model's layout size: two elytras to 64x32, Covenant Mantle, Chalybeate Sclerite and Venous Strider layers to 128x64, Barbed Shield to 64x64, Silent Archon layer 2 to 128x128, Mnemonic Whale up to 128x128, Ferric Rampart and Morphling Polyp to 128x128, a new 128x128 Covenant Throne texture of its own, Sanguine Omen to 16x16. Add a small worn-armour and elytra review scene, because none exists and the Silent Archon leggings need a client to show which half is drawn.
- Files: the twelve PNGs under `src/main/resources/assets/hemomancy/textures/models/armor`, `textures/entity` and `textures/block`, `client/render/tile` code that binds the Throne texture, `gradle/review-clients.gradle`, a new review class
- Tests: packaged PNG-header checks in `alphaCheck`
- Docs: none
- Depends on: NQ-09-4 (who paints)

### T14. Texel batch T3: regenerate generator models (size M)

- Rulings: VX-12, PR-27
- Change: Rerun the tracked generators at density 1 with face-sized UVs: staff fitting sprites to 16x16, Living Syringe and vial rack, Hydra Crown, the three cylinders, the Centrifuge flywheel, the Terrestrial Speculum item, the Saint Sarcophagus and Pulse Pylon items (draw the block model at a smaller display scale), the Marionette Crossbar.
- Files: `tools/model_export/staff_fitting_sprites.py`, `living_syringe.py`, `hydra_crown.py`, `cylinders.py`, `vial_centrifuge.py`, `terrestrial_speculum.py`, `saint_sarcophagus.py`, `pulse_pylon.py`, `tools/circus/build_marionette_crossbar.py`, and their generated models and textures
- Tests: the model tests that read these generators (`AGENTS.md` says tests read `tools/model_export/`). Regenerate `docs/architecture/evidence/source-text-impact-map.csv` if a test's inputs change.
- Docs: none
- Depends on: T12 for the measuring script. `mausoleumReviewClient` shows sarcophagi and pylons. `vesselReviewClient` shows the Syringe.

### T15. Texel batch T4: renderer scale into geometry, its own pass (size L)

- Rulings: VX-13
- Change: Author each mesh at its drawn size so its renderer scale returns to about 1. Unstained weapons first, because `tools/model_export/unstained_weapons.py` exists. The other ten have no generator: Living Blade, Axe and Spear hands, Sporitic Thurible, Sanguis Lancea, Living Flail, Cardinal Focus heart, Choir Keeper, Mason's Effigy, Blood Gourd item. The two headpieces are out (VX-14).
- Files: `tools/model_export/unstained_weapons.py`, the item renderers (`client/render/item/harbinger/LivingBladeItemRenderer.java`, `LivingAxeItemRenderer.java`, `LivingSpearItemRenderer.java`, `client/render/item/BloodGourdItemRenderer.java`), `client/render/entity/summon/SanguisLanceaRenderer.java`, `client/render/tile/harbinger/functional/CardinalFocusRenderer.java`, `MasonsEffigyRenderer.java`, `client/render/entity/mob/animal/ChoirKeeperRenderer.java`, their models and textures
- Tests: per model as it is built
- Docs: none
- Depends on: NQ-09-5. Last in the chunk, as the owner asked.

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |
| VX-1 (batch 5 member) | Approved the order, which includes "a Mortem strand brand" for Hemorrhage, Insatiable Hunger and Grave Debt | On 2026-11-12 all three lost their held form. No registered memory has a brand. | Drop this member? I have not planned it. |
| VX-1 (batch 9 member) | Approved "blood_eclipse spray: a Tenebris spray of its own" | Blood Eclipse's held form is now a carried dome, not a spray. | Drop the spray, and judge the dome when it is re-graded? I have not planned the spray. |
| VX-1 (batch 10 member) | Approved "crimson_flame_conjuration (and its plant): a fire column that erupts from the pour" | The held form is now painted fire along the aim, not a plant. | Drop the plant, and keep only the R cast upgrade? I have not planned the plant. |

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-09-1 | VX-5 | Where does the per-cast seed come from? | A: the client salts each new cue with the game time it first saw it. No payload change. Two players may see different variants of one cast. B: the server packs a seed into `ManipulationVisualPacket`. Every viewer agrees. Needs a `PROTOCOL_VERSION` bump and a new `payload-registry.txt`. | A, because nobody compares screens and it avoids a payload change. Review captures stay repeatable because the harness timing is fixed. |
| NQ-09-2 | VX-1 | Prismatic Reproof and Synaptic Jolt quick casts were to be upgraded "like the spray". The sprays are cut. The art (`StrandSchoolGeometry.prismSpray`, `arcSpray`) is still in the file, used only by tests. Reuse it on the R cast? | A: reuse the prism rays and jagged arcs as art on the R cast only. B: design both R casts fresh. C: leave both at B grade. | A for Prismatic Reproof. B for Synaptic Jolt, and include its held form, which today draws only HutosLib bolts and has no geometry of its own. |
| NQ-09-3 | A-4 (arch) | Animated items (Monolith Fragment, Qliphoth Seed and Pome, Memory of Vesper, Scar Pattern and others) also draw in inventory slots. On game time they stop there too under `/tick freeze` and in the single-player pause menu. Is that wanted? | A: yes, one clock, items freeze everywhere. B: items freeze in hand and in the world but keep moving in a slot. | A. It is one rule and one helper. B needs every renderer to branch on display context. |
| NQ-09-4 | VX-11 | Who repaints the T2 textures? | A: a build session downsamples, cleans up by hand and shows the result in a review client. The owner approves or repaints. B: the owner paints all twelve. | A, with the Covenant Throne (new texture, not a downsample) and the Mnemonic Whale (upsample from 64 to 128) flagged for the owner's hand. |
| NQ-09-5 | VX-13 | Ten T4 models have no generator. How are they rebuilt? | A: write a generator per model in `tools/model_export/`, as for the Unstained weapons. B: the owner reworks them in Blockbench. C: accept the smaller offenders (Living Flail at 23 to 25, Thurible worn at 22) and do only the worst (Blade, Axe and Spear hands, Choir Keeper, Focus heart). | C then A, most-seen first. |
| NQ-09-6 | PR-7 | The PLANT and BRAND proof question is void. What replaces it? | A: check the eight rebuilt held forms and the three lashes at play distance and from a second player's view. The lash ledger lists what was never seen from another player (dome fog, beam, volley, faint Gloam lash, hand on a moving mob). B: no replacement. | A. Use `strandReviewClient` and `tools/review/review-onlooker.ps1`. |
| NQ-09-7 | VX-11 | The audit found three renderers that bind textures that do not exist: `LeechRenderer` (`textures/entity/denizen/model_denizen.png`) and `BloodBulletRenderer` and `WillAnchorRenderer` (`textures/entity/wretched_will/modelwretchedwill.png`). Both folders are still missing today, and all three renderers are registered in `EntityRendererRegistrar`. Fix or remove? | A: point each at a real texture. B: remove the entities if they are dead. | Closed in the merge, no answer needed. Sheet row VX-15 covers it and chunk 06 task T5 plans it. Checked in code: the Leech draws its real textures through `getLeechTypeName()` and only its unused constant names the missing file, the Will Anchor is live and draws lines only, and nothing creates a Blood Bullet, so that one is deleted. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| VX-17 | Watch the A-row capture sheets and say which rows hold at the Riposte bar. | Owner | T7 built |
| PR-7 | Same viewing, in the casting and outpouring clients. The plant and brand half is void. | Owner | T7 built, NQ-09-6 |
| PR-8 | Play one session with cast impacts, the body-anchored basics and infusion veins. | Owner | nothing |
| PR-26 | Judge counts, sizes and speeds of the particle replacements per batch. | Owner | T3, T4, T5, T6 built |
| PR-27 | Judge each T1 model's look after the UV rescale, and the Silent Archon leggings. | Owner | T12, T13 built |
| PR-28 | Shader proof on a second GPU vendor. Accepted as NVIDIA-only for now. | Later | a non-NVIDIA machine |
| VX-1 | Re-grade of 18 stale rows, then batches 2 to 12. | Build | T7, then T11 |
| VX-1 | The eight rebuilt held forms and three lashes have no grade. Any upgrade to them is a new decision after the re-grade. | Owner | T7 built |
| VX-13 | Texel batch T4. | Later | NQ-09-5, and T12 to T14 done |
| VX-3 | Vascular Dowsing upgrade, only if wanted. | Parked | owner |
| VX-9 | Rising glyphs on the Scriptorium, only if the owner wants letters back. | Parked | owner |
| VX-7 | Redesign of Unstained rite visuals. This pass only swaps particles. | Later | the Unstained ritual system |
| A-4 (arch) | Items in inventory slots under freeze. | Owner | NQ-09-3 |
| VX-10 | New models made after the audit were never measured (`sanguine_vigil`, `specimen_vat`, `titration_bandolier`, the ground-network models of 2026-11-11). Measure them once the script is committed. | Build | T12 |
| RV-23, D-20, VX-14 | Record the keep rulings in `docs/MANIPULATIONS_AND_CASTING.md` "Rulings that bind" and move them out of "Open questions". | Build | whoever edits the topic doc next |
| VX-1 | `docs/MANIPULATIONS_AND_CASTING.md` line 38 still describes five strand forms on live memories. The 2026-11-12 rework made that untrue. | Later | the chunk that owns the lash and strand rulings |

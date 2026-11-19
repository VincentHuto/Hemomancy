# Chamber Ordeals Pilot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The Gorebound Hulk ordeal runs on the Strongman's stage inside the Chamber of Will: the Hulk fights, the Strongman intervenes from above the ring, both drawn as their true selves, keyed by a vial of the Strongman's blood.

**Architecture:** A shared ordeal framework (`common/encounter/ordeal/`) owns the stage cell, the `ORDEAL` Chamber visit and the director that drives interventions; the Puppeteer trial rite hands off to it at manifestation and settles from its outcome through `RiteKind` hooks. The Strongman is an ordeal-only `CircusFacultyEntity` (true-self flag, invulnerable, on a flight path) whose interventions are `BossMoveTimeline` moves with `CircusEffects` footprints. True-self models come from the troupe's rig generators and are chosen by the renderers from the synced flag.

**Tech Stack:** NeoForge 1.21.1, Java 21, JUnit 5, GameTests, Python generators (`tools/circus/`, `tools/worldgen/circus/`).

**Spec:** `docs/superpowers/specs/2026-11-17-chamber-ordeals-pilot-design.md`

## Global Constraints

- Stakes: real inventory (`ChamberVisitRules.canMoveItems(ORDEAL)` true, not observational); a lethal hit is caught (a Last Rite in the death-save stage of `DamageStages`), the player is ejected with nothing dropped, the rite fails.
- The stage is the professor's Chamber: an `ORDEAL` visit triggers none of the player's own Chamber progression (room growth, sky theme, attunement, Chamber Liber pages, advancements); systems reading `ChamberVisitService.isActive` for the player's room ignore it.
- No open-world fallback: the rite refuses to start, with a line, when a stage cannot be used.
- The key: a `hemomancy:blood_vial` whose custom data holds `entity_type` `hemomancy:circus_strongman` and `troupe_key` true; given on the lesson; re-given after a failure once per in-game day (`getDayTime() / 24000`); a syringe cannot draw it from him; the brazier matches only it.
- Rite behaviour through `RiteKind` hooks only (`RiteKindGuardrailTest`); timing through `BossMoveTimeline` / `BossReplayClock`; text through `en_us.json` keys in the Strongman's sheet (`docs/NPC_VOICES.md`); no vanilla particles; no copper, brass or gold.
- True selves are their own generated models on the jointed rig at 16 texels a block; the physical models are untouched.
- Pilot only: the framework takes a professor as data; only the Strongman's row is filled.

## Review Focus

1. Logging out mid-ordeal and logging back in: the player is home with their inventory, the rite failed, no Strongman or Hulk left in the Chamber. → Task 4 GameTest `logoutInsideFailsAndReturns`.
2. The rite floor's Focus broken while the player is inside: the player is pulled back and the ordeal fails. → Task 4 GameTest `brokenFocusPullsBack`.
3. A second ordeal while one is running (same player, or the rite re-performed): refused with a line. → Task 4 GameTest `secondOrdealRefused`.
4. Server stop with an ordeal running: the player's next login is home, inventory intact, the stage cleared. → Task 3 GameTest `stopMidOrdealRestoresAtLogin`.
5. A player who never met the Strongman bringing a vial made some other way: refused (only the gifted key fits). → Task 2 unit test `onlyTheGiftFitsTheBrazier`.

---

### Task 1: Ordeal rules

**Files:** Create `common/encounter/ordeal/OrdealRules.java`, `common/encounter/ordeal/OrdealProfessor.java`; Test `src/test/java/.../common/encounter/ordeal/OrdealRulesTest.java`.

**Interfaces — Produces:**
- `enum Ramp { OPENING, PRESSED, DESPERATE }`; `static Ramp ramp(float hulkHealthFraction)` — above 2/3 OPENING, above 1/3 PRESSED, else DESPERATE.
- `record Intervention(String id, Ramp from, int windupTicks, int cooldownTicks)`; `record OrdealProfessor(String role, String summon, String stage, List<Intervention> interventions)`; `static OrdealProfessor STRONGMAN` with interventions `dropped_weight` (OPENING, 20, 100), `ground_heave` (OPENING, 20, 120), `bar_sweep` (PRESSED, 20, 140), `hold_the_line` (DESPERATE, 20, 200); `static Optional<OrdealProfessor> forSummon(String summon)`.
- `static Intervention next(OrdealProfessor p, Ramp ramp, long tick, long seed)` — rotates through those available at the ramp; `static int concurrent(Ramp ramp)` → 1, 2, 3; `static float cadence(Ramp ramp)` → 1.0, 0.85, 0.7.
- `static BlockPos stageOrigin(int chamberId)` — `new BlockPos(ORDEAL_X, FLOOR_Y, CHAMBER_SPACING * chamberId)` with `ORDEAL_X = 8192`.
- `enum Outcome { WON, CAUGHT, LEFT, TIMED_OUT, FLOOR_LOST }`; `static boolean completesRite(Outcome o)` (WON only); `static boolean keyRegivenToday(long lastGiftDay, long today)`.

- [ ] **Step 1:** Tests `rampFollowsHulkHealth` (0.9 → OPENING, 0.5 → PRESSED, 0.2 → DESPERATE), `interventionsUnlockByRamp` (OPENING never yields bar_sweep; DESPERATE can yield all four), `stagesNeverMeetRooms` (stageOrigin(id).getX() ≥ 8192 for ids 0..1000, rooms are at x 0), `onlyAWinCompletes`, `keyOncePerDay` (same day false, next day true). Run, see FAIL.
- [ ] **Step 2:** Implement. Run `./gradlew.bat test --tests "*OrdealRulesTest"` — PASS.

### Task 2: The blood key

**Files:** Modify `common/entity/npc/dialogue/CircusSchoolDialogue.java` (give on lesson), the Strongman's dialogue route class in `common/entity/npc/dialogue/route/` (re-ask after failure), `CircusProgress` (a saved `lastKeyDay` per role, through its attachment), the syringe draw path (refuse `circus_*` faculty), `data/hemomancy/recipe/cardinal_rite/puppeteer_trial_gorebound_hulk.json`; Create `common/encounter/ordeal/OrdealKey.java`; lang keys; Test `OrdealKeyTest`.

**Interfaces — Produces:** `static ItemStack OrdealKey.vial(String role)`; `static boolean OrdealKey.is(ItemStack stack, String role)`; `static boolean OrdealKey.mayRegive(ServerPlayer p, String role)`; `static void OrdealKey.markGiven(ServerPlayer p, String role)`.

- [ ] **Step 1:** Tests `theKeyRecordsItsProfessor` (vial("circus_strongman") is a blood vial whose `BloodSampleData.entityType` names circus_strongman and carries `troupe_key`), `onlyTheGiftFitsTheBrazier` (a blood vial with the same entity type but no `troupe_key` is not `is`; the recipe's ingredient JSON uses `neoforge:components` on `minecraft:custom_data` `{entity_type, troupe_key}` with `strict` true). FAIL.
- [ ] **Step 2:** Implement; add the ingredient to the recipe's `brazier_signature` (count 1, consumed). The lesson gives the vial once (record in `CircusProgress`); after a `CAUGHT`/`LEFT`/`TIMED_OUT`/`FLOOR_LOST` outcome the Strongman's dialogue offers "another vial" when `mayRegive`. A syringe on a faculty member says `hemomancy.circus.ordeal.key.no_draw` and draws nothing. Lines in his voice sheet.
- [ ] **Step 3:** Run `*OrdealKeyTest`, `*Circus*`, `*NpcTrade*`, `*DialogueRouting*` — PASS.

### Task 3: The ORDEAL visit and the stage

**Files:** Modify `common/encounter/ChamberVisitMode.java` (+ `ORDEAL`), `ChamberVisitRules.java` (ORDEAL: not timed, `canMoveItems` true, not protected, not observational), `ChamberVisitService.java` (`beginOrdealVisit(ServerPlayer, OrdealProfessor, Vec3 returnTo, float yaw)`, `endOrdealVisit(ServerPlayer, Outcome)`), the Chamber progression entry points (skip on ORDEAL: room build/growth, sky theme sync, attunement, Liber/advancement openers); Create `common/encounter/ordeal/OrdealStage.java` (place the professor's stage template at `stageOrigin`, clear it after), `common/encounter/ordeal/OrdealDeathSave.java` (Last Rite in the death-save stage for an ORDEAL visitor: cancel the lethal hit, schedule `endOrdealVisit(CAUGHT)`); Test GameTests in a new `ordeal_validation` suite (`gradle/game-tests.gradle` row).

**Interfaces — Produces:** `ChamberVisitService.isOrdeal(ServerPlayer)`; `OrdealStage.build(ServerLevel chamber, OrdealProfessor p, int chamberId) -> BoundingBox`; `OrdealStage.clear(ServerLevel chamber, BoundingBox box)`.

- [ ] **Step 1:** GameTests `ordealVisitKeepsRealInventory` (an arrow shot inside is gone after return), `lethalHitIsCaughtAndEjects` (health 1, take 20 damage: alive, home, nothing dropped, outcome CAUGHT), `ordealTouchesNoOwnChamber` (a D4 player's chamber data has no room/attunement after a visit), `stopMidOrdealRestoresAtLogin`. FAIL.
- [ ] **Step 2:** Implement. The movement clamp reads the stage's ring (radius 9) instead of the room shape for ORDEAL.
- [ ] **Step 3:** Run the suite and `*ChamberVisit*`, `*Chamber*` unit tests — PASS; existing Chamber GameTests green.

### Task 4: The rite hands off and settles

**Files:** Modify `common/rite/harbinger/RiteKind.java` (PUPPETEER_TRIAL: a hook that holds the rite while its caster is in an ORDEAL visit and keeps the floor chunk ticketed; a hook that refuses start when `OrdealProfessor.forSummon` exists but no stage can be used), `PuppeteerTrialRiteController.manifest` (for an ordeal summon: begin the visit and manifest the puppet on the stage instead of the floor), `HarbingerCardinalRiteEvents` (the caster-in-another-level snuff gives way to the hold, through the hook); Create `common/encounter/ordeal/OrdealDirector.java` (per-visit state: tick, ramp, end on the Hulk's death → WON; on Focus or floor broken → FLOOR_LOST; 6000-tick limit → TIMED_OUT); lang keys for refusals; GameTests.

**Interfaces — Consumes:** Tasks 1 and 3. **Produces:** `OrdealDirector.start(ServerPlayer, ActiveCardinalRite, OrdealProfessor)`, `OrdealDirector.outcome(UUID) -> Optional<Outcome>`.

- [ ] **Step 1:** GameTests `wonOrdealCompletesTheRite` (kill the stage Hulk: player home, rite completed, body unlocked), `logoutInsideFailsAndReturns`, `brokenFocusPullsBack`, `secondOrdealRefused`, `refusedWithoutAStage` (an active projection: rite refuses with the line). FAIL.
- [ ] **Step 2:** Implement through the hooks; `rite_kind_validation` fingerprints updated.
- [ ] **Step 3:** Run the ordeal suite, `rite_kind_validation`, `*RiteKind*` — PASS.

### Task 5: The Strongman intervenes

**Files:** Modify `common/entity/npc/circus/CircusFacultyEntity.java` (synced `TRUE_SELF` flag; when set: invulnerable but to `/kill`, no AI goals but the ordeal's, no gravity, follows `OrdealFlight`), `CircusPerformerRules` / a new `common/entity/npc/circus/OrdealMoves.java` (four `BossMoveTimeline` moves with footprints in `CircusEffects`: `dropped_weight` radius 2 circle near the player, `ground_heave` an inward ring wave from radius 9 (jumpable, 1 block high), `bar_sweep` a line across the ring at knee height, `hold_the_line` 60 ticks of a push toward the Hulk at 0.06 blocks a tick); Create `OrdealFlight.java` (a circle of radius 13 at 7 above the stage, 1 revolution a minute, bob 0.5); `GoreboundHulkEntity` (synced `TRUE_SELF`, a little more health and speed when set: ×1.25 health, ×1.1 speed); Strongman ordeal lines (opening, three ramps, win judgement, loss dismissal) in `en_us.json` per his sheet.

**Interfaces — Consumes:** Tasks 1, 4.

- [ ] **Step 1:** Unit tests `OrdealMovesTest` (each move's footprint hits the cell it marks and misses one a block outside; `bar_sweep` misses a jumping player (feet ≥ 1.1 up)). FAIL.
- [ ] **Step 2:** Implement; the director asks `OrdealRules.next` at its cadence and the Strongman plays the move. Damage: dropped weight 6, heave 4, sweep 5, hold none.
- [ ] **Step 3:** Run `*OrdealMoves*`, `*Circus*`, `*BossAnimationClock*` guardrail, `*VanillaParticle*` — PASS.

### Task 6: True selves

**Files:** Create `tools/circus/build_true_strongman.py`, `tools/circus/build_true_gorebound_hulk.py` (on `puppet_rig.py` / `build_faculty.py`), their models and textures; Modify the Strongman's and the Hulk's renderers to pick the true-self model and texture when `TRUE_SELF` is set (models baked once; render types static); `client/registry/ModelRegistrar` layers.

- [ ] **Step 1:** Generate; `verify_troupe_assets.py` and the texel-density checks pass.
- [ ] **Step 2:** Renderers switch; the true Strongman draws with a crimson-rim translucent pass (never fully solid), the true Hulk opaque.
- [ ] **Step 3:** `./gradlew.bat test --tests "*Guardrail*" --tests "*Circus*"` — PASS.

### Task 7: The Strongman's Iron Yard

**Files:** Create `tools/worldgen/circus/ordeal_stages.py` (writes `data/hemomancy/structure/ordeal/strongman.nbt`: a ring of radius 9 of chalked iron and bone, weight racks and plinths beyond the rim, sinew cables rising, open above); Modify `tools/worldgen/circus/build.py` to write it with checks.

- [ ] **Step 1:** Run `python tools/worldgen/circus_sites.py`; checks pass; the template exists.

### Task 8: Stills and docs

**Files:** Modify `src/gameTest/.../CircusClientReview.java` (scene `ordeal`: begin a Hulk ordeal, film the stage, the Strongman circling, each intervention's telegraph, the true Hulk, a win, then a second run ending in a caught hit); docs `docs/CIRCUS.md`, `docs/CARDINAL_RITES.md`, `docs/HEMOMANCY_REFERENCE.md` 17.1 and the Chamber section, `docs/OPEN_QUESTIONS.md` (the other six professors), `docs/NPC_VOICES.md` if the sheet gains lines.

- [ ] **Step 1:** Run the scene, read the stills, fix what reads wrong.
- [ ] **Step 2:** Docs; impact map regenerated; full `./gradlew.bat test` and the ordeal, troupe and rite-kind GameTest suites — PASS.

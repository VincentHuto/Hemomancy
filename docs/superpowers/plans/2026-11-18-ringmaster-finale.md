# The Ringmaster's Finale in his Chamber: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Crimson Troupe finale as a two-phase fight in the Ringmaster's Chamber of Will, on a hidden copy of the pavilion whose edges dissolve into his mind, ending at a kneel where the player kills him (Liberation) or absorbs him (Succession).

**Architecture:** A new finale director in `common/encounter/ordeal/finale/` reuses the ordeal pieces (per-player stage origin, the Chamber return point, the caught killing blow, logout/login recovery) but settles a pavilion site, not a rite. The stage is the pavilion template placed in the Chamber with the real site's rotation. The client crosses seamlessly under his opening scene (a held frame over the dimension change), sees a mask sky that mimics the overworld until the merge starts, and his own Chamber sky beyond a shrinking stencil dome generalized from the Somatic Loom's. Moves follow the ordeal standard (bold `OrdealKit` hazards, gather/drop/hit-stop/recovery, camera jolts); echoes reuse the passed professors' true-self entities and their `OrdealChoreography` moves.

**Tech Stack:** NeoForge 1.21.1, Java 21, the mod's boss kit (`BossMoveTimeline`, `BossReplayClock`, `BossGroundHazardEntity`, `BossScenes`), JUnit 5, GameTest, the `circusReviewClient` review client, Python model generators (`tools/circus/puppet_rig.py`).

**Spec:** `docs/superpowers/specs/2026-11-18-ringmaster-finale-design.md`

## Global Constraints

- Follow `AGENTS.md`: `Hemomancy.rloc`; every player-facing string an `en_us.json` key; `HemoParticleData`, never vanilla `ParticleTypes`; `PlayerBlockEdits` for player-caused block edits; session maps cleared through `TransientServerStateEvents` (named there by class); `HemoEasing` (never a private smoothstep/lerp/clamp01); the one `WorldEffectDispatcher`; render types built once; `getPersistentData()` only with an allow-list row.
- Payload changes bump `PacketHandler.PROTOCOL_VERSION` and rewrite `src/test/resources/network/payload-registry.txt`.
- He stays in `hemomancy:bosses`. No brass/bronze/copper on blood-side art. Lines follow his sheet in `docs/NPC_VOICES.md`.
- The mod is unreleased: drop old route state outright; no migration.
- Every move meets the owner's weight bar: a slow gather, a fast accelerating blow (a few ticks), a hit-stop, a heavy recovery, an impact sound (reuse `SoundInit` events), custom particles, a camera jolt for big hits, a bold telegraph from the first wind-up tick. Review films run at the real 20 ticks a second.
- Leave everything uncommitted (the owner's standing rule): no `git commit` steps.
- One Gradle process at a time. Unit tests: `./gradlew.bat test --console=plain` (or `--tests "<pattern>"`); films: `./gradlew.bat runCircusReviewClient -PcircusReviewOnly=finale --console=plain` driven through `build/circus-review/operator/inbox` (see `AGENTS.md`).
- Every number below is the builder's starting value, recorded as the builder's call in `docs/CIRCUS.md` for the owner's review.

## Review Focus

1. The crossing shows: light, fog, sky or a missing cast member differs between the last real frame and the first copy frame. Expected: no visible change. Pinned in Task 5 by a film step that diffs the two frames.
2. A finale broken off mid-fight (logout, crash, death the catch missed, the real site unloading). Expected: the player is home, the real cast is unfrozen, the site is released and the finale can be begun again. Pinned in Task 4 by `RingmasterFinaleDirectorRulesTest.interruptedFinaleReleases`.
3. A rotated pavilion (any `Rotation` but NONE). Expected: the copy is laid with the same rotation and the player arrives at the mapped spot and facing. Pinned in Task 3 by `FinaleStageMappingTest` over all four rotations.
4. An ineligible player at the kneel (Unstained, dormant blood, missing acclimation or acts). Expected: absorption is refused with his line and no rising blood; the kill still works. Pinned in Task 11 by `RingmasterAbsorptionRulesTest`.
5. A lethal hit on his copy from something other than the player's own blow (an echo, a mob, thorns, fall) or a hit during the kneel's settling beat. Expected: he kneels, never dies; only the owner's blow after the beat kills. Pinned in Task 2 by `FinaleRulesTest.kneelCatchesAnyLethalHit`.

---

### Task 1: The route choice moves to the kneel

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/circus/CircusRouteRules.java`
- Modify: `.../common/capability/player/harbinger/circus/CircusPlayerProgress.java`, `CircusProgress.java` (drop route choice and repair; keep completion)
- Modify: `.../common/entity/npc/circus/CircusRingmasterEntity.java` (dialogue: remove accept/reject/repair nodes and warnings; `handleChoice` keeps `EVENT_TRIAL`, `EVENT_FINALE`)
- Modify: `.../common/entity/npc/circus/CircusPerformanceController.java` (acts 2 to 4 gate on the attention act and a non-Unstained pact gate, not on `Route.SUCCESSION`)
- Modify: every other reader of `Route.SUCCESSION`/`Route.LIBERATION` (`CircusSchoolDialogue.aftermathKey`, `CircusCarouselEntity.severCaptive`, `CircusPavilionSavedData` site route) and `en_us.json` (remove the dead route keys; add the finale-gate keys)
- Test: `src/test/java/.../common/capability/player/harbinger/circus/CircusRouteRulesTest.java` (rewrite)

**Interfaces:**
- Produces: `enum Route { NEUTRAL, SUCCESSION_COMPLETE, LIBERATION_COMPLETE }`; `static final int SUCCESSION_CHALLENGES = 0b1_1101` (attention, acts 2 to 4; bit 1 retired); `static PactGate pactGate(boolean pactDegree, boolean anyReport, boolean understudyReport, boolean unstained)` (Unstained: OPEN with the Understudy report, else NEEDS_UNDERSTUDY; others: as today); `static boolean canBeginFinale(int challenges, PactGate gate)` (attention bit set and gate OPEN); `static boolean mayAbsorb(int acclimation, int challenges, boolean unstained)`; `CircusPlayerProgress.completeRoute(ServerPlayer, Route)` kept.

- [ ] **Step 1: Write the failing tests** in `CircusRouteRulesTest`:

```java
@Test void finaleOpensAfterTheAttentionActForAnOpenGate() {
    assertTrue(CircusRouteRules.canBeginFinale(0b1, CircusRouteRules.PactGate.OPEN));
    assertFalse(CircusRouteRules.canBeginFinale(0b0, CircusRouteRules.PactGate.OPEN));
    assertFalse(CircusRouteRules.canBeginFinale(0b1, CircusRouteRules.PactGate.NEEDS_DEGREE));
}
@Test void absorptionNeedsFullAcclimationTheActsAndNotUnstained() {
    assertTrue(CircusRouteRules.mayAbsorb(1000, 0b1_1101, false));
    assertFalse(CircusRouteRules.mayAbsorb(999, 0b1_1101, false));
    assertFalse(CircusRouteRules.mayAbsorb(1000, 0b0_1101, false));
    assertFalse(CircusRouteRules.mayAbsorb(1000, 0b1_1101, true));
}
@Test void theUnstainedMayFightWithTheUnderstudyReport() {
    assertEquals(CircusRouteRules.PactGate.OPEN, CircusRouteRules.pactGate(false, false, true, true));
    assertEquals(CircusRouteRules.PactGate.NEEDS_UNDERSTUDY, CircusRouteRules.pactGate(true, true, false, true));
}
```

- [ ] **Step 2:** Run `./gradlew.bat test --tests "*CircusRouteRulesTest" --console=plain`. Expected: compile failure (new signatures).
- [ ] **Step 3:** Implement the signatures above; delete `canChoose`, `choose`, `canRepair`, the route-argument `pactGate`, the route-argument `canBeginFinale`, route repair in the progress classes, and the dialogue's accept/reject/repair branches. The finale option shows when `canBeginFinale` holds; a refused finale names its gate (existing `gateKey` lines). Acts 2 to 4 open for a player with the attention act whose gate is OPEN and who is not Unstained.
- [ ] **Step 4:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL (fix every reader the compiler and the Circus tests name; `CircusRingmasterGameTests` route cases are rewritten in Task 13, so mark any that test the old pact as `@Disabled("finale rebuilt, Task 13")` only if they block the build).

### Task 2: The finale's rules (pure)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/encounter/ordeal/finale/FinaleRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/encounter/ordeal/finale/FinaleRulesTest.java`

**Interfaces:**
- Produces: `enum FinalePhase { CROSSING, RAFTERS, REVEAL, DESCENT, KNEEL, ENDED }`; `enum Ending { KILLED, ABSORBED }`; constants `INTRO_TICKS = 60`, `REVEAL_TICKS = 50`, `KNEEL_SETTLE_TICKS = 30`, `DESCENT_AT = 0.5F`, `MERGE_HIDDEN = 256.0F`, `MERGE_SEAM = 26.0F`, `MERGE_RAFTERS_END = 12.0F`, `MERGE_ISLAND = 9.0F`, `MERGE_SEAM_TICKS = 100`, `CHARGE_MAX = 100`, `CHARGE_PER_SIP = 20`, `EMPOWERED_COST = 50`.
- `static FinalePhase next(FinalePhase phase, int phaseTicks, float healthFraction)`: CROSSING to RAFTERS at `INTRO_TICKS`; RAFTERS to REVEAL when `healthFraction <= DESCENT_AT`; REVEAL to DESCENT at `REVEAL_TICKS`; else unchanged (KNEEL and ENDED are entered by `kneel`/`end`, never by `next`).
- `static float mergeRadius(FinalePhase phase, int phaseTicks, float healthFraction)`: CROSSING `MERGE_HIDDEN`; RAFTERS eases from `MERGE_HIDDEN` to `MERGE_SEAM` over `MERGE_SEAM_TICKS`, then follows health linearly from `MERGE_SEAM` at 1.0 to `MERGE_RAFTERS_END` at `DESCENT_AT`; REVEAL and DESCENT from `MERGE_RAFTERS_END` at `DESCENT_AT` to `MERGE_ISLAND` at 0; KNEEL `MERGE_ISLAND`; ENDED `MERGE_HIDDEN` (the snap back). Use `HemoEasing`.
- `static boolean kneelCatches(FinalePhase phase, float damage, float health)`: true when the phase is RAFTERS, REVEAL or DESCENT and `damage >= health`.
- `static boolean mayKill(FinalePhase phase, int phaseTicks, boolean ownerBlow)`: KNEEL, `phaseTicks >= KNEEL_SETTLE_TICKS`, and the owner's own blow.
- `static int charge(int charge, int sips)` capped at `CHARGE_MAX`; `static boolean mayEmpower(int charge)` (`>= EMPOWERED_COST`).

- [ ] **Step 1: Write the failing tests**: `phasesFollowHealthAndTime` (RAFTERS at 0.51 stays, at 0.5 goes REVEAL; REVEAL to DESCENT at 50), `mergeIsHiddenThroughTheCrossingAndShrinksToTheIsland` (CROSSING = 256; RAFTERS at tick 100 and health 1.0 = 26; RAFTERS at health 0.5 = 12; DESCENT at health 0 = 9; monotonic non-increasing over a sweep of health 1 to 0), `kneelCatchesAnyLethalHit` (true for 20 damage on 5 health in RAFTERS, REVEAL, DESCENT; false for 4 damage on 5 health; false in KNEEL), `onlyTheOwnersBlowAfterTheSettleKills` (false at KNEEL tick 29, false for a non-owner blow at tick 30, true for the owner's at 30), `chargeCapsAndEmpowers` (charge(90, 1) == 100; mayEmpower(49) false, (50) true).
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*FinaleRulesTest" --console=plain`. Expected: FAIL (missing class).
- [ ] **Step 3:** Implement `FinaleRules` with the signatures above.
- [ ] **Step 4:** Run the same command. Expected: PASS.

### Task 3: The pavilion copy as his stage

**Files:**
- Create: `.../common/encounter/ordeal/finale/FinaleStage.java`
- Modify: `.../common/entity/npc/circus/CircusSiteStateService.java` (make `CAROUSEL`, `PODIUM` and `rotation(ServerLevel, BlockPos)` public)
- Modify: `tools/worldgen/circus/pavilion.py` (a `RAFTER_PERCHES` list in template cells: six high perches on the king and quarter poles and the rigging towers, four low perches on the gallery rails; exported for the Java constants by a checked comment line `# RAFTER_PERCHES = [...]`)
- Test: `src/test/java/.../common/encounter/ordeal/finale/FinaleStageMappingTest.java`

**Interfaces:**
- Produces: `static BlockPos origin(int chamberId)` (= `OrdealRules.stageOrigin(chamberId)`); `static void build(ServerLevel chamber, BlockPos origin, Rotation rotation)` (places `crimson_troupe_pavilion` so its carousel cell lands on `origin`, with `rotation`); `static void clear(ServerLevel chamber, BlockPos origin)` (clears the template's full rotated bounds); `static Vec3 toCopy(Vec3 real, BlockPos realOrigin, BlockPos copyOrigin, Rotation rotation)` and `static float yawToCopy(float yaw)` (rotations match, so yaw is unchanged); `static final List<BlockPos> HIGH_PERCHES, LOW_PERCHES` (template cells relative to the carousel cell) and `static Vec3 perch(BlockPos copyOrigin, Rotation rotation, BlockPos cell)`.

- [ ] **Step 1: Write the failing test** `FinaleStageMappingTest`: for each `Rotation`, a point 3 east and 2 north of a real origin maps to the same rotated offset from the copy origin (`toCopy(realOrigin + offset) - copyOrigin == offset`, since the real site and the copy share a rotation); a perch cell maps through `StructureTemplate.calculateRelativePosition`'s rotation the way `CircusSiteStateService.at` does (assert one known cell per rotation); `HIGH_PERCHES` has 6 entries and `LOW_PERCHES` 4, all inside the template's 81 x 23 x 65.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*FinaleStageMappingTest" --console=plain`. Expected: FAIL.
- [ ] **Step 3:** Implement `FinaleStage`; add the perches to `pavilion.py` and the matching Java constants; rebuild with `python tools/worldgen/circus_sites.py --only crimson_troupe_pavilion` (the perches are template-cell coordinates, not new blocks, unless a perch needs a standing block: then a set-piece cell that passes `checks.py`).
- [ ] **Step 4:** Run the same command. Expected: PASS.

### Task 4: The finale director and the settlement

**Files:**
- Create: `.../common/encounter/ordeal/finale/RingmasterFinaleDirector.java` (the server side: begin, tick, catch, end, logout/login, session map)
- Create: `.../common/encounter/ordeal/finale/CircusFinaleSettlement.java` (moves `CircusRingmasterEntity.complete` and `grantLiberationMemory` here, keyed by `FinaleRules.Ending`)
- Create: `.../common/encounter/ordeal/finale/RingmasterFinaleDirectorRules.java` (pure: what an interruption does)
- Modify: `CircusRingmasterEntity` (`EVENT_FINALE` calls the director; a `frozenForFinale` synced flag: no AI, invulnerable, dialogue answers `hemomancy.circus.finale.elsewhere`; the old in-world finale tick paths are removed in Task 13), `CircusCarouselEntity` and `CircusPerformerEntity` (hold while their site's finale runs), `common/damage/DamageStages.deathSaves` (try the finale's catch after the ordeal's), `ChamberOfWillManager.tick` and `ChamberOfWillEvents` (the finale player is claimed like an ordeal player), `TransientServerStateEvents` (`RingmasterFinaleDirector.clearSessionState()`), `src/test/resources/architecture/persistent-data-allow-list.txt` (one row: the finale session marker)
- Test: `src/test/java/.../common/encounter/ordeal/finale/RingmasterFinaleDirectorRulesTest.java`

**Interfaces:**
- Produces: `static boolean begin(ServerPlayer player, CircusRingmasterEntity real, CircusCarouselEntity carousel)`; `static boolean isInFinale(ServerPlayer)`; `static Optional<Finale> finale(UUID)` with `Finale` exposing `BlockPos stage()`, `BlockPos siteOrigin()`, `ResourceKey<Level> siteLevel()`, `Rotation rotation()`, `FinalePhase phase()`, `int phaseTicks()`, `UUID ringmaster()`, `UUID carousel()`; `static void end(ServerPlayer, @Nullable FinaleRules.Ending)` (null = failed/left); `static boolean tryCatch(ServerPlayer, LivingDamageEvent.Pre)`; `CircusFinaleSettlement.apply(ServerLevel siteLevel, BlockPos siteOrigin, ServerPlayer player, FinaleRules.Ending ending)`; `static Route CircusFinaleSettlement.routeFor(FinaleRules.Ending ending)`.
- `RingmasterFinaleDirectorRules.onInterrupt(Reason reason)` with `enum Reason { LOGOUT, CAUGHT, DIED, SITE_LOST }` returning `Release` (`record Release(boolean returnHome, boolean unfreezeSite, boolean releaseSite, boolean restartable)`).
- Consumes: `FinaleStage` (Task 3), `FinaleRules` (Task 2), `CircusRouteRules.canBeginFinale` (Task 1).

- [ ] **Step 1: Write the failing test** `RingmasterFinaleDirectorRulesTest.interruptedFinaleReleases`: every `Reason` returns `returnHome`, `unfreezeSite`, `releaseSite` and `restartable` all true (the owner's ruling: a failed finale can be begun again); and `settlementPicksTheRoute`: `CircusFinaleSettlement.routeFor(Ending.KILLED) == Route.LIBERATION_COMPLETE`, `routeFor(Ending.ABSORBED) == Route.SUCCESSION_COMPLETE`.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*RingmasterFinaleDirectorRulesTest" --console=plain`. Expected: FAIL.
- [ ] **Step 3:** Implement. `begin` refuses as `OrdealDirector.refusal` does (a visit, an ordeal, a projection, no Chamber), marks the site owned (`CircusPavilionSavedData.begin` without a route), freezes the real cast, builds the copy with the real rotation, spawns the copy cast (a `CircusRingmasterEntity` flagged `stageCopy`, the carousel with its captives, no performers), remembers the return point, sets the persistent marker, and hands the crossing to Task 5 (until then: a plain `changeDimension` to `toCopy` of the player's position and facing). `end` clears the copy and its entities, returns the player, unfreezes the site, and either settles it (`CircusFinaleSettlement.apply`) or releases it. The catch mirrors `OrdealDirector.tryCatch`.
- [ ] **Step 4:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL, including `SessionStateRegistryTest` and `PersistentDataAllowListTest`.

### Task 5: The seamless crossing

**Files:**
- Create: `.../common/network/circus/FinaleCrossingPacket.java` (playToClient: `int ringmasterCopyId`, `float yaw`, `float pitch`; registered in `PacketHandler`, protocol bump, payload registry rewrite)
- Create: `.../client/network/ClientboundFinaleHandlers.java`, `.../client/data/FinaleCrossingClientState.java`
- Modify: `.../client/data/ChamberVeil.java` (a held-frame mode: `armFrame(RenderTarget frame)`, drawn by `ChamberTransitionScreen` and then for `FRAME_FADE_FRAMES = 8` frames after the new level's first frame, fading out)
- Modify: `RingmasterFinaleDirector.begin` (play the opening scene in the real tent; at `CROSS_AT = 24` ticks into it, send the packet, change dimension to the mapped position and facing, and resume the scene on the copy with `BossScenes.playTo(player, copy, BossPresentationIds.SCENE_CIRCUS_RINGMASTER, CROSS_AT)`)
- Modify: `gametest/CircusClientReview.java` (a `finale` scene: begins the finale at the real pavilion and records through the crossing; logs `CIRCUS_REVIEW crossing at <frame>`)

**Interfaces:**
- Consumes: `RingmasterFinaleDirector.begin` (Task 4), `FinaleStage.toCopy` (Task 3).
- Produces: `FinaleCrossingClientState.crossing()` (true from the packet until the veil has faded), used by Task 6 to pick the mask sky from the first copy frame.

- [ ] **Step 1:** Implement the packet, the held frame and the scene hand-off.
- [ ] **Step 2:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL (`NetworkPayloadRegistryTest` matches the rewritten registry).
- [ ] **Step 3: Film and diff the crossing.** Run `runCircusReviewClient -PcircusReviewOnly=finale`; take the frames just before and just after the logged crossing frame; compute the mean absolute pixel difference outside a 64-pixel border with PIL. Expected: the difference is no larger than between two consecutive frames earlier in the scene (the scene camera's own motion); look at both frames and confirm nothing pops (cast, light, sky, fog).

### Task 6: The mask sky, his Chamber sky and the merge

**Files:**
- Create: `.../client/data/RingmasterFinaleClientData.java` (active, phase, phaseTicks, health fraction, copy origin, rotation; fed by the copy Ringmaster's synced data)
- Create: `.../client/render/world/chamberofwill/RingmasterMaskChamberEffects.java` (the overworld sky captured at the crossing: day time, sky and fog colour, moon phase; a vanilla-like sky with stars)
- Create: `.../client/render/world/chamberofwill/RingmasterChamberEffects.java` (his mind: strings hanging out of the dark, stage-light shafts, a canvas sky)
- Create: `.../client/render/world/chamberofwill/ChamberDomePortal.java` (the stencil portal pulled out of `SomaticLoomDomeRenderer`: `static boolean render(RenderLevelStageEvent event, Vec3 centre, float radius, ChamberSkyTheme theme, float partial)`)
- Create: `.../client/render/world/circus/FinaleMergeRenderer.java` (the dome at the copy's carousel with `FinaleRules.mergeRadius`, the membrane, torn-canvas accents)
- Modify: `SomaticLoomDomeRenderer` (uses `ChamberDomePortal`), `ChamberOfWillManager` (`THEME_RINGMASTER_MASK`, `THEME_RINGMASTER`), `ChamberSkyThemeRegistry` (registers both; `activeTheme()` returns the mask while `RingmasterFinaleClientData.isActive()`), `WorldEffectDispatcher` (calls `FinaleMergeRenderer.render`)
- Test: no new unit test; the Loom's own film is the portal's regression (step 3).

**Interfaces:**
- Consumes: `FinaleRules.mergeRadius` (Task 2), `FinaleCrossingClientState` (Task 5).
- Produces: `RingmasterFinaleClientData.isActive()`, `phase()`, `mergeRadius(float partial)`, `copyCentre()`.

- [ ] **Step 1:** Implement; keep `SomaticLoomDomeRenderer`'s behaviour byte-for-byte through `ChamberDomePortal`.
- [ ] **Step 2:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL (`HudAndWorldDrawingGuardrailTest`, `RenderTypeCachingGuardrailTest`).
- [ ] **Step 3: Film.** Film `finale` and the Loom's existing review client (`loomReviewClient`). Expected: the Loom's dome looks as before; in the finale the tent looks real through the crossing, the first seams of his sky appear past the walls after the opening, and by the 50% mark the galleries and seating are gone into his sky.

### Task 7: The theatre in the dark and the performing echoes

**Files:**
- Create: `.../client/render/world/circus/FinaleTheatre.java` (drawn inside `RingmasterChamberEffects`'s sky pass, so it shows only beyond the dome: blazing stage lights, sweeping spotlights, a phantom audience on the dissolved seating, and the echoes performing)
- Create: `.../common/encounter/ordeal/finale/FinaleEchoes.java` (server: which professors this player has passed, `CircusApprenticeshipProgress.knows(player, professor.summon())`; synced to the client as a role mask on the copy Ringmaster)
- Modify: `PhantomFigures` (a `planFinale(...)` entry that reuses the phantom audience rig and look for the finale's seats)

**Interfaces:**
- Produces: `FinaleEchoes.passed(ServerPlayer) -> List<OrdealProfessor>`; `static int roleMask(List<OrdealProfessor>)` and `static List<String> roles(int mask)` (bit order = `OrdealProfessor.all()` order); the client draws each passed professor's true-self model (the existing `CircusTrue*Model` layers) at a station in the dark, in an idle loop of their act.
- Test: `src/test/java/.../common/encounter/ordeal/finale/FinaleEchoesTest.java`.

- [ ] **Step 1: Write the failing test** `FinaleEchoesTest.maskRoundTrips`: `roles(roleMask(list))` returns the same roles in roster order for the empty list, one professor, and all seven.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*FinaleEchoesTest" --console=plain`. Expected: FAIL.
- [ ] **Step 3:** Implement `FinaleEchoes` and `FinaleTheatre` (stations: seven fixed angles round the copy at radius 22, each echo drawn at its true-self scale facing the ring; only passed roles drawn).
- [ ] **Step 4:** Run the test. Expected: PASS. Film `finale` and confirm the audience, lights and passed echoes show beyond the dome edge and never inside it.

### Task 8: His true self

**Files:**
- Create: `tools/circus/build_true_ringmaster.py` (pattern: `build_true_strongman.py`; jointed rig with real hands, the topper, the coat and staff; visceral but recognisably him; glow atlas)
- Create: `.../client/model/entity/npc/CircusTrueRingmasterModel.java` (generated), `.../client/render/entity/npc/CircusTrueRingmasterRenderer.java`
- Modify: `CircusRingmasterEntity` (implements `TrueSelf`: a synced flag; the copy turns true at REVEAL), `EntityRendererRegistrar` (wrap his renderer in `TrueSelfRenderer`), `LayerEvents`

- [ ] **Step 1:** Write the generator and run `python tools/circus/build_true_ringmaster.py`. Expected: the model, atlas, glow atlas and BBModel written.
- [ ] **Step 2:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL (`EasingCopyGuardrailTest`, `EnglishLiteralAllowListTest`).
- [ ] **Step 3:** Film `finale` and confirm the reveal swaps him to his true self and it reads at range.

### Task 9: Phase 1, the rafters

**Files:**
- Create: `.../common/encounter/ordeal/finale/RingmasterFinaleMoves.java` (server: each move a `BossMoveTimeline`, picked by phase, synced as move id and tick on the copy)
- Create: `.../common/encounter/ordeal/finale/RingmasterFinaleGeometry.java` (pure hits)
- Create: `.../common/encounter/ordeal/finale/CarouselAlive.java` (the copy carousel's moves)
- Create: `.../client/event/RingmasterFinaleClientEvents.java` (the camera jolt per move, read from the synced move and tick)
- Modify: `CircusRingmasterEntity` (stage-copy tick delegates to `RingmasterFinaleMoves`; his doubles reused for the fakes), `CircusCarouselEntity` (a synced carousel move and tick, the horses' poses from `CarouselAlive`), `CircusRingmasterRenderer`/`CircusCarouselRenderer` (fakes and ghost volleys drawn translucent; the fakes' tell: no shadow and his thread does not reach them), `OrdealHazardKinds` (`RINGMASTER = 4070`, his block 4070 to 4089)
- Test: `src/test/java/.../common/encounter/ordeal/finale/RingmasterFinaleGeometryTest.java`

**Interfaces:**
- Moves (ids, wind-up ticks, damage; every one telegraphed from its first tick with `OrdealKit.hazard` in kinds 4070 to 4079):
  - `rafter_volley`: blinks to a high perch, two fakes on two others; 16-tick gather; each throws a knife down a lane at the player (lane 14 x 0.6); only his deals 5; a hit on a fake bursts it; after the volley he drops to a low perch for 40 ticks (melee window).
  - `string_lights`: a living string of lights snakes from the rigging along a curve toward the player (curve hazard drawn as lanes, 20-tick gather); a touch roots 30 ticks (`MorphlingLock.pin`) and deals 3; moving off the curve before the snap avoids it.
  - `burning_spotlight`: a disc of radius 2.5 tracks the player for 20 ticks, stops, burns 10 ticks later: 6 damage, fire 3 s, Blindness 40 ticks.
  - `audience_rush`: three lanes of phantom audience dash across the ring 6 ticks apart (lanes 18 x 1.2), 3 damage each and a shove; spawns two `EnthralledDollEntity` adds at the ring's edge (max four alive).
  - `horse_volley` (carousel): spins up 20 ticks, fires its three horses out on three lanes 120 degrees apart (lanes 9 x 1.4), 6 damage, the horses return in 20 ticks.
  - `horse_spiral` (carousel): a fast spin of 24 ticks, then the horses sprint outward on a spiral (radius from 2.4 to 9 over 40 ticks, a disc of 1.2 round each horse), 5 damage.
  - `pole_slam` (carousel): rises on its pole 24 ticks, slams down: two jumpable rings (from 2 to 9 over 18 ticks, 9 ticks apart, band 1.0), 5 damage each.
- Produces (pure): `RingmasterFinaleGeometry.laneHits(Vec3 from, Vec3 to, double halfWidth, Vec3 feet)`, `curveHits(List<Vec3> curve, double halfWidth, Vec3 feet)`, `horseLanes(Vec3 centre, float yaw) -> List<Vec3>` (three directions 120 degrees apart), `spiralRadius(int ticks)`, `ringHits(Vec3 centre, double radius, double band, Vec3 feet, double clearance)`.

- [ ] **Step 1: Write the failing tests**: `horseLanesAre120DegreesApart`, `spiralGrowsFrom2_4To9In40Ticks` (`spiralRadius(0) == 2.4`, `spiralRadius(40) == 9.0`, monotonic), `ringHitsTheBandAndIsJumpable` (feet 1.2 above the floor clear it at `clearance = OrdealMoves.CLEARANCE`), `laneAndCurveHits` (a point inside the half width is hit, one just outside is not).
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*RingmasterFinaleGeometryTest" --console=plain`. Expected: FAIL.
- [ ] **Step 3:** Implement the geometry, then the moves, the carousel's moves, the fakes and the camera jolts (`rafter_volley` 1.2 on his real hit, `pole_slam` 3.0, `horse_volley` 1.8). Phase 1 picks among the seven on a beat that quickens as his health falls (beat 70 ticks at full, 45 at 50%).
- [ ] **Step 4:** Run the tests. Expected: PASS. Film `finale` and check each move's telegraph shows from its first tick, its blow lands fast, and the fakes are readable by their tell.

### Task 10: Phase 2, the descent

**Files:**
- Modify: `RingmasterFinaleMoves`, `RingmasterFinaleGeometry`, `RingmasterFinaleClientEvents`
- Create: `.../common/encounter/ordeal/finale/FinaleEchoCue.java` (calling one passed professor's echo)
- Create: `.../common/encounter/ordeal/finale/FinaleSwarm.java` (the roosting bats)
- Modify: `VampireBatEntity` (a `roosting` finale variant: hangs upside down at a rafter until called; a swoop that bites, steals blood and flies back to him), `BloodAccounting` source constant `RINGMASTER_SWARM = Source.of("ringmaster_swarm", Category.WORLD, true)` with its lang key
- Test: extend `RingmasterFinaleGeometryTest`; create `src/test/java/.../common/encounter/ordeal/finale/FinaleSwarmTest.java`

**Interfaces:**
- REVEAL (50 ticks): he leaps down from his perch to the ring, the copy turns `TrueSelf`, a phase card plays (`BossScenes`).
- Moves: `staff_strike` (14-tick gather, a 3.2-block 100-degree wedge, 7), `carousel_swing` (grips the pole, swings round it at radius 3.5 for 20 ticks: an arc band of 1.2, 6, a shove out), `echo_cue` (`FinaleEchoCue.call`: a passed professor's echo steps in through the dome edge as their true-self entity and runs that professor's signature intervention, then fades; one echo at a time; 24 ticks before it lands), plus the phase 1 arena moves at a lower weight.
- Signature intervention per role (`FinaleEchoCue.SIGNATURE`): strongman `dropped_weight`, acrobat the trapeze swing, knife thrower the fan, stilt walker the planted stilt, beast tamer the whipcrack, fire eater the long breath, understudy the spotlight (each the id in that professor's `OrdealProfessor` row).
- `FinaleEchoCue.call(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity ringmaster, OrdealProfessor professor, Vec3 floor)`: spawns the professor's entity type, `setTrueSelf(true)`, places it with `professor.choreography().entrance(...)` translated to the copy, runs `choreography().mark(...)` and then `choreography().run(...)` each tick with the Ringmaster as the `puppet` argument until it reports over, then discards it.
- Swarm: four roosts of three bats in the copy's rafters, active from REVEAL; every 160 ticks a group of two or three swoops; a bite steals `FinaleSwarm.SIP_ML = 25` mL through `BloodAccounting.pay(player, SIP_ML, RINGMASTER_SWARM)` when the player's volume is active (else 3 damage); a bat that reaches him with blood adds `FinaleRules.CHARGE_PER_SIP`; a bat killed on the way back adds nothing.
- Empowered moves (only while `FinaleRules.mayEmpower(charge)`, each costs `EMPOWERED_COST`): `crimson_cascade` (six discs of radius 1.6 rain on the player's path, 6 ticks apart, 5 each), `marionette_pull` (threads from his hands: a lane to the player, 20-tick gather, a hit pulls the player 5 blocks toward him and the staff strike follows at once).
- Produces (pure): `FinaleSwarm.chargeFrom(int charge, boolean reachedHim)`; `RingmasterFinaleGeometry.arcHits(Vec3 pivot, double radius, double band, float fromDeg, float toDeg, Vec3 feet)`.

- [ ] **Step 1: Write the failing tests**: `FinaleSwarmTest.onlyBloodThatReachesHimCharges` (`chargeFrom(0, true) == 20`, `chargeFrom(0, false) == 0`, capped at 100); `arcHits` inside/outside the band and the swept angle.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*FinaleSwarmTest" --tests "*RingmasterFinaleGeometryTest" --console=plain`. Expected: FAIL.
- [ ] **Step 3:** Implement the reveal, the moves, the echo cue, the swarm and the empowered moves; register `FinaleSwarm`'s session state in `TransientServerStateEvents` if it keeps any.
- [ ] **Step 4:** Run the tests. Expected: PASS. Film `finale` with the review fighter's ordeals marked passed for three professors and confirm only those three echoes perform and are called.

### Task 11: The kneel and the two endings

**Files:**
- Create: `.../common/encounter/ordeal/finale/RingmasterAbsorption.java` (pattern: `VesperBloodAbsorptionInteractions`)
- Create: `.../common/encounter/ordeal/finale/RingmasterAbsorptionRules.java` (pure)
- Create: `.../client/registry` layer row + `.../client/screen/overlay/FinaleChoicePrompt.java` (the first-time prompt naming the two actions; a HUD layer in `GuiLayerRegistrar`)
- Modify: `BloodAbsorptionItem` and `LivingStaffItem` (call `RingmasterAbsorption.tryAbsorb` next to Vesper's), `CircusRingmasterEntity` (KNEEL state: health held at 1, kneel pose, his line, the threads on the copy carousel glow, his blood rises toward an eligible player; an absorption progress like Vesper's), `CircusRingmasterRenderer` (the kneel visuals), `en_us.json` (his kneel line naming both ways out, the refusal line, the prompt lines, the elsewhere line, the diminished Ringmaster's greeting and trade lines)
- Test: `src/test/java/.../common/encounter/ordeal/finale/RingmasterAbsorptionRulesTest.java`

**Interfaces:**
- `RingmasterAbsorptionRules.canAbsorb(boolean kneeling, int kneelTicks, boolean owner, boolean bloodActive, int acclimation, int challenges, boolean unstained)`: kneeling, `kneelTicks >= FinaleRules.KNEEL_SETTLE_TICKS`, owner, blood active, and `CircusRouteRules.mayAbsorb(...)`. `ABSORPTION_REQUIRED = 100` progress.
- `RingmasterAbsorption.tryAbsorb(Level level, LivingEntity user, double range, float progressPerTick) -> double` (same contract as Vesper's).
- At 100 progress: `RingmasterFinaleDirector.end(player, Ending.ABSORBED)`. The owner's blow after the settle (`FinaleRules.mayKill`): `end(player, Ending.KILLED)`. The real Ringmaster after `ABSORBED` is `retired` (a saved flag): no finale option, his greeting and lines from the `diminished` set, still the trade speaker and the conductor teacher.

- [ ] **Step 1: Write the failing test** `RingmasterAbsorptionRulesTest.ineligiblePlayersAreRefused`: true for a kneeling, settled owner with active blood, 1000 acclimation, `0b1_1101`, not Unstained; false when any one of those fails (seven cases: not kneeling, tick 29, not owner, dormant blood, 999 acclimation, a missing act, Unstained).
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*RingmasterAbsorptionRulesTest" --console=plain`. Expected: FAIL.
- [ ] **Step 3:** Implement the kneel, the absorption, the kill, the refusal (his line and no rising blood), the prompt (shown once per player: a flag in `CircusProgress`), the settlement on return and the retired Ringmaster.
- [ ] **Step 4:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 5: Film both endings.** Extend the `finale` review scene with `finale_kill` (the fighter strikes after the settle) and `finale_absorb` (an eligible fighter channels absorption). Expected: `Finale of CircusReview ended: KILLED` with the real site laid to ruin and Thread Ripper granted; `ended: ABSORBED` with the tenancy banner, the Pattern granted, and the real Ringmaster retired on his perch.

### Task 12: Lines

**Files:**
- Modify: `en_us.json` (all finale lines in his voice: the opening, the merge's first seam, the reveal, each empowered move's call, the kneel, the refusal, the kill, the absorption, the retired greeting; the professors' echoes stay silent)
- Modify: `docs/NPC_VOICES.md` only if his sheet needs a note for the retired register

- [ ] **Step 1:** Write the lines against his sheet in `docs/NPC_VOICES.md`.
- [ ] **Step 2:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL (the voice and English guardrails).

### Task 13: Retire the old finale, the tests and the docs

**Files:**
- Delete or rewrite: the in-world finale paths in `CircusRingmasterEntity` (RAFTERS, CAROUSEL, DESCENT, CURTAIN ticks, the anchor objective, `troupe()` finale gathering, `spawnSwarm` in the real tent), `CircusFinaleRules`, `CircusCarouselEncounterRules` (anchors), the anchor and severing paths in `CircusCarouselEntity` (the copy's captives are scenery), the old finale moves in `CircusRingmasterMoves` that no phase uses (move anything deleted into the untracked `removed_assets/2026-11-18-ringmaster-finale-v1/`, per the owner's rule)
- Rewrite: `gametest/CircusRingmasterGameTests`, `CrimsonTroupeGameTests`, `CrimsonTroupePavilionGameTests` and the CT playthrough band where they test the old finale or the pre-fight choice (the GameTest world has no Chamber: test the dialogue's finale gate, the frozen real site's refusal line, and the settlement through `CircusFinaleSettlement.apply` directly)
- Modify: `gametest/CircusClientReview` (drop the old `succession` and `liberation` scenes; `finale`, `finale_kill`, `finale_absorb` replace them)
- Modify: `docs/CIRCUS.md` (a new "The finale in his Chamber" section replacing "Ringmaster finale presentation", with every builder's call marked; "Rulings that bind" gains the 2026-11-18 rulings), `docs/OPEN_QUESTIONS.md` (a play-and-tune row), `docs/HEMOMANCY_REFERENCE.md` and `docs/LORE_REFERENCE.md` where they describe the routes or the finale, `docs/architecture/evidence/source-text-impact-map.csv` (`python gradle/source-text-impact-map.py`)

- [ ] **Step 1:** Remove the old paths and rewrite the tests.
- [ ] **Step 2:** Run `./gradlew.bat test --console=plain`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 3:** Run `./gradlew.bat runCrimsonTroupeGameTestServer --console=plain` and `./gradlew.bat runRiteKindGameTestServer --console=plain`. Expected: every required test passes.
- [ ] **Step 4:** Film `finale,finale_kill,finale_absorb` in one run. Expected: three clean runs, the crossing unseen, both endings settled on the real site.
- [ ] **Step 5:** Update the docs listed above.

# Boss Presentation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give every Hemomancy boss a framed boss bar and an opening scene, all run by one shared client director. The
Mycophant waking is the first scene moved onto it.

**Architecture:** The server sends two new payloads: `BossBarFramePacket` binds a vanilla bar UUID to a frame id, and
`BossScenePacket` starts a client scene. Frames are drawn by one `CustomizeGuiOverlayEvent.BossEventProgress`
subscriber that uses a shared painter and one `BossBarFrame` per boss. Scenes are client `BossScene` definitions made
of pure time tracks (letterbox, card, HUD, input, camera). The `BossSceneDirector` plays them through the existing
event seams plus one camera-position mixin. Server timing stays authoritative: bosses without an intro get a shared
`BossIntroWindow`.

**Tech Stack:** NeoForge 21.1.251 / MC 1.21.1, Java 21, JUnit 5 (JVM tests in `src/test`), GameTest review clients in
`src/gameTest`.

**Spec:** `docs/superpowers/specs/2026-10-24-boss-presentation-design.md`

## Global Constraints

- Lang keys only for shown text. The title keys are `boss.hemomancy.<id>.name` and `boss.hemomancy.<id>.epithet`, with
  the wording in the spec's title-card table (`HardCodedEnglishGuardrailTest`).
- `Hemomancy.rloc("snake_case")` for every id (`ResourceLocationStyleGuardrailTest`).
- Bump `PacketHandler.PROTOCOL_VERSION` from `"6"` to `"7"` and add both payloads to
  `src/test/resources/network/payload-registry.txt`.
- HUD art goes in a layer in `GuiLayerRegistrar`, never in `RenderGuiEvent`. The bar subscriber uses
  `CustomizeGuiOverlayEvent.BossEventProgress`, and hiding the HUD uses `RenderGuiLayerEvent.Pre`.
- Nothing in `common/` imports a client class. Client handlers go in `client/network/ClientboundEffectHandlers` through
  `ClientPayloadHandlers::dispatch`.
- `HemoEasing` for easing (never `Mth.smoothstep`), `AnimationTime` for client time, and `BossReplayClock` for synced
  boss ticks.
- New client caches are cleared in `ClientSessionEvents.onClientPlayerLogout`.
- New mixins are listed in `hemomancy.mixins.json` under `client` (`SourceTreeHygieneTest`).
- No vanilla particle clouds for scene visuals. Use `HemoParticleData` and the existing boss effect helpers.
- Server stays authoritative: client skip and config toggles never change server state.
- Config: `HemoClientConfig.BOSS_BAR_FRAMES` (`bossBarFrames`, true), `CINEMATIC_BOSS_SCENES` (`cinematicBossScenes`,
  true) and `BOSS_SCENE_SHAKE` (`bossSceneShake`, true), all under a new `boss_presentation` push.
- Every build runs under the shared lock (`build/.mycophant-gradle-lock` mkdir) if parallel sessions are building
  (memory: concurrent builds).

## Review Focus

- **Several bars at once** (the Vesper transformation briefly shows two; a Saint fight near another boss): stacked
  frames must not overlap, and a vanilla bar from another mod must still draw. *Test:* `BossBarFrameLayoutTest`
  checks that `increment()` is at least the frame height plus 2. *Review client scene:* `bars_stacked`.
- **The boss dies, unloads or teleports mid-scene** (the Naeglerophaeon transits, a rematch reload): the shot must fall
  back to the player's view and the camera type must be restored. *Test:* `BossSceneDirectorTest.missingBossFallsBackToPlayerView`
  and `restoresCameraTypeOnEnd`.
- **Logout, death or dimension change during a scene:** no stuck letterbox, input lock or third-person camera. *Test:*
  `BossSceneDirectorTest.clearRestoresEverything`, called from the logout clear and the `PlayerRespawn`/`Clone` client
  path.
- **A player joins an encounter whose bar is already showing:** the frame binding must arrive with the vanilla add
  packet, not only at spawn. `show(player)` sends it every time. *Test:* `BossBarBindingFrameTest` (source scan:
  `show` sends `BossBarFramePacket`).
- **Skipping the waking before CONTROL:** the input must stay locked because the server still binds the player.
  *Test:* `MycophantWakingSceneTest.skipKeepsServerBinding`.

---

## File map

**Common (server-safe)**
- `common/entity/boss/presentation/BossPresentationIds.java`: frame and scene id constants
  (`ResourceLocation FRAME_MYCOPHANT`, `SCENE_MYCOPHANT_WAKING`, ...).
- `common/entity/boss/presentation/BossIntroWindow.java`: the shared server intro window.
- `common/entity/boss/presentation/BossScenes.java`: `play(Entity boss, ResourceLocation scene)` sends
  `BossScenePacket` near the boss.
- `common/network/boss/BossBarFramePacket.java` and `common/network/boss/BossScenePacket.java`.
- Modify `common/entity/boss/BossBarBinding.java` (frame id; `show` sends the frame packet).

**Client**
- `client/screen/overlay/boss/BossBarFrameClientState.java`: the UUID-to-frame map, chip and reveal state, and pruning.
- `client/screen/overlay/boss/BossBarFramePainter.java`: the shared well, fill, chip, notches and reveal.
- `client/screen/overlay/boss/BossBarFrame.java`: the interface. `client/screen/overlay/boss/frame/*Frame.java` holds
  one class per boss.
- `client/screen/overlay/boss/BossBarFrames.java`: the id-to-frame registry.
- `client/event/BossBarFrameRenderer.java`: the `BossEventProgress` subscriber.
- `client/cinematic/BossScene.java`, `BossSceneTracks.java` (keys helper), `CinematicShot.java`, `TitleCard.java` and
  `BossSceneRegistry.java`.
- `client/cinematic/BossSceneDirector.java`: pure playback state, a singleton `INSTANCE`.
- `client/cinematic/BossSceneClientEvents.java`: the tick, angles, input, HUD hide and camera type.
- `client/screen/overlay/BossSceneOverlay.java`: the letterbox, fade, card and the scene's own overlay.
- `client/cinematic/scene/*Scene.java`: one class per boss scene.
- `mixin/core/MixinCamera.java` (client mixin).

---

### Task 1: Ids, payloads, and the bar's frame binding

**Files:**
- Create: `common/entity/boss/presentation/BossPresentationIds.java`, `common/network/boss/BossBarFramePacket.java`,
  `common/network/boss/BossScenePacket.java`, `common/entity/boss/presentation/BossScenes.java`
- Modify: `common/entity/boss/BossBarBinding.java`, every `new BossBarBinding(` site (12: the 11 bosses plus the
  `SaintTrialEntity` subclasses' supers), `common/network/PacketHandler.java` (version `"7"`, two `playToClient` lines),
  `common/network/ClientPayloadHandlers.java` and `client/network/ClientboundEffectHandlers.java` (handlers that call
  `BossBarFrameClientState.bind(uuid, frame)` and `BossSceneDirector.INSTANCE.start(scene, entityId, offset)`; both
  stub classes are created here with only those methods and `clear()`), `client/event/ClientSessionEvents.java`
  (clear both), `src/test/resources/network/payload-registry.txt`
- Test: `src/test/java/com/vincenthuto/hemomancy/architecture/BossPresentationBindingTest.java`

**Interfaces:**
- Produces:
  - `record BossBarFramePacket(UUID bar, ResourceLocation frame)` and
    `record BossScenePacket(ResourceLocation scene, int entityId, int offset)`, both with `TYPE` (`boss_bar_frame`,
    `boss_scene`) and `STREAM_CODEC` (`UUIDUtil.STREAM_CODEC`, `ResourceLocation.STREAM_CODEC`, `ByteBufCodecs.VAR_INT`).
  - `BossBarBinding(String titleKey, ResourceLocation frame, BossBarColor, BossBarOverlay)`, `ResourceLocation frame()`.
  - `BossScenes.play(Entity boss, ResourceLocation scene)` and `play(Entity boss, ResourceLocation scene, int offset)`,
    which use `PacketHandler.sendNear(level, x, y, z, 64, payload)`.
  - `BossScenes.playTo(ServerPlayer, Entity boss, ResourceLocation scene, int offset)`.
  - Ids in `BossPresentationIds`:
    - frames: `FRAME_MYCOPHANT`, `FRAME_ANNETTA_KNOWLES`, `FRAME_STAINED_PRIESTESS`, `FRAME_LATENT_INFECTION`,
      `FRAME_NAEGLEROPHAEON`, `FRAME_VESPER_CROWNED_REFUSAL`, `FRAME_VESPER_EVENING_STAR`, `FRAME_HEMORATH`,
      `FRAME_PUTRICIEL`, `FRAME_VELORUM`, `FRAME_SERAPHAE`;
    - scenes: `SCENE_MYCOPHANT_WAKING`, `SCENE_MYCOPHANT_REMATCH`, `SCENE_MYCOPHANT_CAPTURE`,
      `SCENE_ANNETTA_KNOWLES`, `SCENE_ANNETTA_BREAK_CARD`, `SCENE_STAINED_PRIESTESS`, `SCENE_LATENT_INFECTION`,
      `SCENE_NAEGLEROPHAEON`, `SCENE_NAEGLEROPHAEON_TRANSITION_CARD`, `SCENE_VESPER_CROWNED_REFUSAL`,
      `SCENE_VESPER_COCOON_CARD`, `SCENE_VESPER_EVENING_STAR`, `SCENE_MYCOPHANT_SPLIT_CARD`, `SCENE_SAINT_RISE`,
      `SCENE_HEMORATH_EMPTIES_CARD`;
    - `List<ResourceLocation> FRAMES` and `List<ResourceLocation> SCENES`.

- [ ] **Step 1: Write `BossPresentationBindingTest`.** It checks three things:
  - `everyBindingNamesAFrame` scans `src/main/java` for `new BossBarBinding(` and asserts each call's second argument
    is `BossPresentationIds.FRAME_*`;
  - `showSendsTheFrame` uses `SourceAssertions.methodBody` on `BossBarBinding.show` and asserts it contains
    `BossBarFramePacket`;
  - `everyPlayedSceneIsAnId` scans for `BossScenes.play` arguments and asserts each is `BossPresentationIds.SCENE_*`.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*BossPresentationBindingTest*"` and expect it to FAIL (no class).
- [ ] **Step 3: Implement the files above.** `show` calls `event.addPlayer(player)` and then
  `PacketHandler.sendToPlayer(player, new BossBarFramePacket(event.getId(), frame))`. Saints pass their frame through
  the `SaintTrialEntity` constructor's `BossBarBinding`.
- [ ] **Step 4:** Run `./gradlew.bat test --tests "*BossPresentationBindingTest*" --tests "*PayloadProtocolVersionTest*" --tests "*NetworkPackageGuardrailTest*"` and expect PASS.
- [ ] **Step 5:** Leave the work uncommitted (memory: no auto-commit).

### Task 2: Bar frame state, painter and renderer (generic frame)

**Files:**
- Create: `client/screen/overlay/boss/BossBarFrameClientState.java`, `BossBarFramePainter.java`, `BossBarFrame.java`,
  `BossBarFrames.java`, `client/event/BossBarFrameRenderer.java`
- Modify: `config/HemoClientConfig.java` (the `boss_presentation` push with all three toggles)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/BossBarFrameMathTest.java`, `BossBarFrameLayoutTest.java`

**Interfaces:**
- Consumes: `BossBarFrameClientState.bind/clear` from Task 1.
- Produces:
  - `BossBarFrame` methods:
    - `int increment()` (30);
    - `float[] notches()`;
    - `int fillTop(float phase)` and `int fillBottom(float phase)` (ARGB);
    - `int nameColor()`;
    - `void paintBack(BossBarFrameContext c)` and `void paintFront(BossBarFrameContext c)`.
  - `record BossBarFrameContext(GuiGraphics g, int x, int y, float progress, float chip, float reveal, float time, int phase, LerpingBossEvent event)`.
    `x` and `y` are the vanilla bar origin; the fill is 182×7 at `(x, y)`.
  - `BossBarFrameClientState.Entry` has `float chip(float progress, int nowTick)` and `float reveal(int nowTick, float partial)`.
  - `BossBarFrames.get(ResourceLocation)` returns a `BossBarFrame` or null.
  - `BossBarFramePainter.paint(BossBarFrame, BossBarFrameContext)` draws back, well, fill, chip, notches, front and
    the name.

- [ ] **Step 1: Write `BossBarFrameMathTest`:**
  - `chipHoldsThenDrains`: progress 1.0 → 0.6 at tick 0. `chip` reads 1.0 at ticks 0 and 15, below 1.0 at tick 17, and
    reaches 0.6 by tick 16 + 20 (drain 0.02 per tick).
  - `chipFollowsHealsImmediately`: when progress rises, chip equals progress.
  - `revealRunsTwentyTicks`: `reveal` reads 0 at bind and 1 at bind+20, and is monotonic.
  - `prunesUnseenBars`: an entry not touched for 40 ticks is removed by `prune(nowTick)`.
- [ ] **Step 2: Write `BossBarFrameLayoutTest`:** for every id in `BossPresentationIds.FRAMES`, `BossBarFrames.get(id)`
  is non-null, `increment() >= 24`, and notches are sorted and lie in (0, 1). This test fails until Tasks 3–5 register
  their frames; register the generic fallback for every id now and replace each one in its task.
- [ ] **Step 3:** Run both tests and expect FAIL.
- [ ] **Step 4: Implement.** The renderer is
  `@SubscribeEvent static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress e)`:
  - when the config is off, or the bar has no frame, it returns;
  - otherwise it calls `e.setCanceled(true)` and `e.setIncrement(frame.increment())`, then paints inside
    `graphics.drawManaged`.

  The painter draws the name itself, because vanilla skips the name when the event is cancelled.
- [ ] **Step 5:** Run the tests and expect PASS.

### Task 3: Frames for the Mycophant and the Annetta arc

**Files:**
- Create: `client/screen/overlay/boss/frame/MycophantFrame.java`, `AnnettaKnowlesFrame.java`,
  `StainedPriestessFrame.java`, `LatentInfectionFrame.java`
- Modify: `BossBarFrames.java`
- Test: extend `BossBarFrameLayoutTest` with `mycophantPhaseFromJoiningName`. It builds a fake name
  `Component.translatable("entity.hemomancy.mycophant.joining")` and asserts `MycophantFrame.phaseOf(name, progress) == 2`.

Motifs follow the spec table; palettes are copied from the bosses' constants (`MycophantMoves` AMBER/YOLK/CRIMSON/BONE,
`AnnettaKnowlesEntity` PALE/ICHOR/BLOOD, Priestess blood/clot/lancea, Latent blood/ichor/spore).

- [ ] **Step 1:** Write the test and run it: FAIL.
- [ ] **Step 2:** Implement the four frames with a `static int phaseOf(Component name, float progress)` where phase
  matters.
- [ ] **Step 3:** Run the tests and expect PASS.

### Task 4: Frames for Naeglerophaeon and both Vesper forms

**Files:**
- Create: `frame/NaeglerophaeonFrame.java`, `VesperCrownedRefusalFrame.java`, `VesperEveningStarFrame.java`
- Test: `naeglerophaeonPhaseFromColor` (PURPLE → 2, YELLOW → 3, WHITE → 4) and `eveningStarShamedFromName`.

- [ ] Steps as in Task 3. Palettes come from `NaeglerophaeonEffects` and `VesperVisualEffects`; notches are
  `{0.1, 0.5}`, `{0.12, 0.42, 0.72}` and `{0.33, 0.67}`.

### Task 5: Frames for the four Saints

**Files:**
- Create: `frame/HemorathFrame.java`, `PutricielFrame.java`, `VelorumFrame.java`, `SeraphaeFrame.java`
- Test: `hemorathEmptiedFromName` (the key `message.hemomancy.hemorath.saint_hemorath_the_vessel_empties` → phase 2).

- [ ] Steps as in Task 3. Notches: Hemorath `{0.5}`, Velorum `{0.25}`; Putriciel and Seraphae have none.

### Task 6: The director core

**Files:**
- Create: `client/cinematic/BossScene.java`, `BossSceneTracks.java`, `CinematicShot.java`, `TitleCard.java`,
  `BossSceneRegistry.java`, `BossSceneDirector.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/BossSceneDirectorTest.java`, `BossSceneTracksTest.java`

**Interfaces:**
- Produces the `BossScene` interface. Defaults are 0 or false unless noted:
  - `int length()`
  - `float letterbox(float t)`, `float fade(float t)`
  - `boolean hudHidden(float t)`, `boolean inputLocked(float t)`
  - `TitleCard card()` (null = none)
  - `CinematicShot shot()` (null = none)
  - `CameraOffsets offsets(float t)`
  - `void tick(int t, Minecraft mc)`
  - `void overlay(GuiGraphics g, int w, int h, float t, float time)`
  - `boolean skippable(int t)` (default true)
  - `boolean keepsInputAfterSkip(int t)`
- `record TitleCard(String nameKey, String epithetKey, int start, int length, int ruleColor)`, with
  `float alpha(float t)` (12-tick ease in and out) and `float rule(float t)` (0 → 1 over the first 16 ticks).
- `record CameraOffsets(float roll, float pitch, float yaw, float shake)`.
- `record CinematicShot(List<Key> keys)` and `record Key(float tick, float yawDeg, float distance, float height, float lookHeight)`.
  `Key at(float t)` eases with `HemoEasing.smoothstep`. `Vec3 position(Key k, Vec3 bossPos, float bossYaw)` and
  `float[] lookAngles(Vec3 camera, Vec3 target)` are pure functions.
- `BossSceneTracks.keys(float t, float... pairs)`: moved from `MycophantIntroClientState.keys`, which then delegates to it.
- `BossSceneDirector` methods:
  - `start(ResourceLocation id, int entityId, int offset)`;
  - `tick()` and `clear()`;
  - `active()`, `scene()`, `time(float partial)`;
  - `skipProgress()`, `holdSkip(boolean held)`;
  - `skipped()` (the presentation is gone, but the input still honours `keepsInputAfterSkip`);
  - `boolean usesShot()`, which is false when the shot is null, `cinematicBossScenes` is off, or the boss is not
    resolvable;
  - `CameraType restoreType`.

- [ ] **Step 1: Write the tests:**
  - `startsAtOffset`;
  - `endsAfterLength` (not active at `length()+1`);
  - `replaceRestartsClock`;
  - `holdingSneakTwentyTicksSkips`;
  - `skipKeepsInputWhenSceneSaysSo`;
  - `unknownSceneIsIgnored`;
  - `missingBossFallsBackToPlayerView` (`usesShot()` is false with a null resolver);
  - `clearRestoresEverything`;
  - `titleCardFadesTwelveTicks`;
  - `shotEasesBetweenKeys`;
  - `lookAnglesPointAtTarget` (a target straight ahead at +Z gives yaw 0 and pitch 0, following Minecraft's yaw
    convention).

  The director takes a `Function<Integer, Entity>`-style resolver through a constructor so tests run without a level.
- [ ] **Step 2:** Run the tests and expect FAIL.
- [ ] **Step 3:** Implement.
- [ ] **Step 4:** Run `BossSceneDirectorTest`, `BossSceneTracksTest` and the existing `MycophantIntroClientState` tests,
  and expect PASS.

### Task 7: Director client hooks, overlay and camera mixin

**Files:**
- Create: `client/cinematic/BossSceneClientEvents.java`, `client/screen/overlay/BossSceneOverlay.java`,
  `mixin/core/MixinCamera.java`
- Modify: `client/registry/GuiLayerRegistrar.java` (register `boss_scene` with `registerAboveAll`, just before
  `hud_markers`), `src/main/resources/hemomancy.mixins.json` (`MixinCamera` under `client`),
  `client/event/ClientSessionEvents.java`
- Test: `BossSceneDirectorTest.restoresCameraTypeOnEnd` (the director records the type and returns it on end and on
  clear), plus the existing `HudAndWorldDrawingGuardrailTest` and `SourceTreeHygieneTest`.

The hooks, all reading `BossSceneDirector.INSTANCE`:

| Hook | What it does |
|---|---|
| `ClientTickEvent.Post` | ticks the director; reads `mc.options.keyShift.isDown()` for the skip; switches to `THIRD_PERSON_BACK` while `usesShot()` and restores it after; calls `scene.tick` |
| `ViewportEvent.ComputeCameraAngles` | applies the shot's look angles, or adds the scene's offsets (the shake is scaled to zero when `bossSceneShake` is off); the two-wave shake formula moves here from `MycophantIntroClientEvents.tiltCamera` |
| `MovementInputUpdateEvent` | zeroes input while `inputLocked`, or after a skip while `keepsInputAfterSkip` |
| `RenderGuiLayerEvent.Pre` | cancels the vanilla layers in `HIDDEN = Set.of(HOTBAR, PLAYER_HEALTH, ARMOR_LEVEL, FOOD_LEVEL, AIR_LEVEL, VEHICLE_HEALTH, EXPERIENCE_BAR, EXPERIENCE_LEVEL, JUMP_METER, CROSSHAIR, EFFECTS, BOSS_OVERLAY, SELECTED_ITEM_NAME)` while `hudHidden` and not skipped |
| `MixinCamera` | `@Inject(method = "setup", at = @At("TAIL"))`: when `BossSceneDirector.INSTANCE.usesShot()`, calls `setPosition(...)` with the shot position |

`BossSceneOverlay.render(g, w, h, partial)` draws, in order:
1. the fade;
2. `scene.overlay`;
3. the letterbox bars (`height * 0.11 * letterbox`);
4. the title card;
5. the skip hint (`boss.hemomancy.scene.skip` "Hold %s to skip") once the scene is past tick 20.

- [ ] **Step 1:** Write the `restoresCameraTypeOnEnd` test and run it: FAIL.
- [ ] **Step 2:** Implement.
- [ ] **Step 3:** Run `./gradlew.bat test --tests "*BossScene*" --tests "*HudAndWorldDrawingGuardrailTest*" --tests "*SourceTreeHygieneTest*"`
  and expect PASS.
- [ ] **Step 4:** Run `./gradlew.bat compileJava` and expect it to succeed, with the mixin refmap built.

### Task 8: Move the Mycophant waking, rematch and capture onto the director

**Files:**
- Create: `client/cinematic/scene/MycophantWakingScene.java`, `MycophantRematchScene.java`,
  `MycophantCaptureScene.java`
- Modify: `client/network/ClientboundEffectHandlers.java` (`PacketMycophantIntro` WAKING, REMATCH and CAPTURE also
  start `SCENE_MYCOPHANT_*`; CLEAR clears the director when a Mycophant scene is playing),
  `client/event/MycophantIntroClientEvents.java` (remove `tiltCamera` and `holdInput`; the fiber-snap sound and the
  forced pose move into `MycophantWakingScene.tick`; the glimpse, watch and bloom stay),
  `client/data/MycophantIntroClientState.java` (keep its timing API; `keys` delegates to `BossSceneTracks.keys`)
- Lang: `boss.hemomancy.mycophant.name` / `.epithet`, `boss.hemomancy.scene.skip`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/MycophantWakingSceneTest.java`

**Interfaces:**
- Consumes: `BossScene` and `MycophantIntroRules.tickOf(Beat, boolean rematch)`.
- The waking scene's tracks:
  - `length() == 220`;
  - `inputLocked(t)` while `t < CONTROL`;
  - `keepsInputAfterSkip(t)` while `t < CONTROL`;
  - `hudHidden(t)` while `t < CONTROL`;
  - letterbox 1 → 0 between `CONTROL` and `CONTROL+12`;
  - card `(…mycophant.name, …epithet, ROAR, 40, AMBER)`;
  - `offsets` from the `MycophantIntroClientState` roll, pitch, sway and shake;
  - `overlay` calls `MycophantMyceliumOverlay.render(..., MycophantIntroClientState.INSTANCE)`.
- The rematch scene:
  - `length() == RUSH(rematch)+1`;
  - a shot with three keys (yaw 180° at distance 9 and height 4 at t=0, yaw 90° at t=12, yaw 0° at distance 8 and
    height 3 at t=ROAR);
  - card at ROAR;
  - `hudHidden` until ROAR.
- The capture scene: `inputLocked` for its whole length (180), with no card and no letterbox.

- [ ] **Step 1: Write the tests:**
  - `lockEndsAtControl`;
  - `skipKeepsServerBinding` (skip at t=40 still locks input at t=60 and frees it at CONTROL);
  - `cardAtRoar`;
  - `rematchCardAtRematchRoar`;
  - `captureLocksThroughout`.
- [ ] **Step 2:** Run the tests: FAIL.
- [ ] **Step 3:** Implement.
- [ ] **Step 4:** Run `./gradlew.bat test --tests "*Mycophant*"` and expect PASS (the existing intro tests unchanged).
- [ ] **Step 5:** Review client. Add a `waking_director` scene to `MycophantClientReview`. It runs `enterNursery` for
  the review player and captures at ticks 20, 150 and 170. Run `./gradlew.bat runMycophantReviewClient -PmycophantReviewOnly=waking_director`.
  The run passes if the screenshots show the lids or fibers under the letterbox, then the card over the roar, then
  the framed bar.

### Task 9: The shared server intro window

**Files:**
- Create: `common/entity/boss/presentation/BossIntroWindow.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/BossIntroWindowTest.java`

**Interfaces:**
- Produces: `final class BossIntroWindow`:
  - `BossIntroWindow(int length)`;
  - `void begin()` (elapsed = 0);
  - `void tick()`;
  - `boolean active()`;
  - `int elapsed()`, which is -1 when done or never begun;
  - `void save(CompoundTag, String key)` and `void load(CompoundTag, String key)`.

  A boss holds one window plus its own synced `EntityDataAccessor<Integer>` that mirrors `elapsed()` for client poses.
  While `active()`, the boss's hooks make it passive: `isInvulnerableTo` returns true, AI is skipped (its existing
  passive path), the bar is hidden, and `setVisible(true)` happens at the end.

- [ ] **Step 1: Write the tests:**
  - `inactiveUntilBegun`;
  - `runsExactlyLength`;
  - `survivesSaveLoad` (a reload mid-window resumes);
  - `endedWindowStaysEnded`.
- [ ] **Step 2:** Run: FAIL. **Step 3:** Implement. **Step 4:** Run: PASS.

### Task 10: The Annetta arc's openings

**Files:**
- Create: `client/cinematic/scene/AnnettaKnowlesScene.java`, `StainedPriestessScene.java`, `LatentInfectionScene.java`,
  `PhaseCardScene.java` (a generic 50-tick card with letterbox 0.6 and no camera or lock, built from the id,
  `TitleCard` and colour; registered once per card id)
- Modify:
  - `AnnettaEncounterRules`: `HARBINGER_INTRO_TICKS = 60`.
  - `AnnettaKnowlesEntity`: `startHarbingerRoute` begins a `BossIntroWindow(HARBINGER_INTRO_TICKS)`. The scream
    action plays at window tick 30. She is invulnerable and her goals are idle while the window runs.
    `BossScenes.play(this, SCENE_ANNETTA_KNOWLES)`. `startBreak` plays `SCENE_ANNETTA_BREAK_CARD`.
  - `StainedPriestessEntity.beginEmerging` plays `SCENE_STAINED_PRIESTESS`.
  - `LatentAnnettaInfectionEntity.beginEmerging` plays `SCENE_LATENT_INFECTION` with offset 0. The scene's first 90
    ticks hold the camera on the player, and the shot starts at 90.
- Lang: names and epithets for `annetta_knowles`, `stained_priestess`, `latent_annetta_infection`, and the break card
  `annetta_break` ("Annetta Knowles" / "Breaking").
- Test: `AnnettaSceneTimingTest`:
  - `introWindowMatchesScene` (`AnnettaKnowlesScene.length() == HARBINGER_INTRO_TICKS`);
  - `priestessShotEndsAtEmergeLength` (`== PriestessCombatRules.EMERGE_LENGTH`);
  - `priestessCardAtStandEnd`;
  - `latentShotSpansTearToEnd` (keys from `EMERGE_HIDDEN` to `EMERGE_END`).
- [ ] Steps 1–4 as before (test FAIL, implement, test PASS).
- [ ] **Step 5:** Add review scenes `annetta_intro`, `priestess_rise` and `latent_tear` to the Annetta review client
  (`AnnettaClientReview`), with screenshots at the card.

### Task 11: The Naeglerophaeon opening and transition card

**Files:**
- Create: `client/cinematic/scene/NaeglerophaeonScene.java`
- Modify:
  - `NaeglerophaeonCombatRules`: `INTRO_TICKS = 70`.
  - `NaeglerophaeonEntity`: `bindMind` begins the window and sets phase `NERVE_EMERGE`. `tick` holds `NERVE_EMERGE`
    (no `tickBlink` exit) while the window runs. `isInvulnerableTo` is true while active. `beginTransition` plays
    `SCENE_NAEGLEROPHAEON_TRANSITION_CARD`.
  - `VagrantMindEncounterManager`: after `bindMind`, `BossScenes.play(boss, SCENE_NAEGLEROPHAEON)`.
- Lang: the `naeglerophaeon` name and epithet, plus the `naeglerophaeon_transition` card ("Naeglerophaeon" /
  "Conducting").
- Test: `NaeglerophaeonIntroTest.introWindowMatchesScene`.
- [ ] Steps 1–4 as before.
- [ ] **Step 5:** Add an `intro` scene to the Naeglerophaeon review.

### Task 12: Vesper's openings

**Files:**
- Create: `client/cinematic/scene/VesperCrownedRefusalScene.java`, `VesperEveningStarScene.java`
- Modify:
  - `VesperCombatRules`: `ARRIVAL_TICKS = 80`.
  - `VesperTheCrownedRefusalEntity`: an arrival window. It is invulnerable and its controller idles while the window
    runs; the bar is hidden until the window ends. A synced `DATA_ARRIVAL_TICK` drives the mount's rear.
  - `VesperMountAnimState` / `VesperMountModel`: a procedural rear over ticks 20–56 (the body pitches up 28°, the
    forelegs lift, it settles by 70), using `BossReplayClock`.
  - `VesperOrdealManager.spawnCrownedRefusal`: begins the window and plays `SCENE_VESPER_CROWNED_REFUSAL`.
  - The cocoon burst plays `SCENE_VESPER_COCOON_CARD`. `spawnEveningStar` calls `star.beginAwakening()` and plays
    `SCENE_VESPER_EVENING_STAR`.
- Lang: names and epithets for `vesper_crowned_refusal` and `vesper_evening_star`, plus the `vesper_cocoon` card
  ("Vesper" / "Unbecoming").
- Test: `VesperSceneTimingTest`:
  - `arrivalWindowMatchesScene`;
  - `eveningStarSceneMatchesAwakening` (`== VesperPhaseTransitionRules` TOTAL 72);
  - `spawnEveningStarBeginsAwakening` (source scan of the method body for `beginAwakening()`).
- [ ] Steps 1–4 as before.
- [ ] **Step 5:** Add `arrival` and `awakening` scenes to the Vesper review (the review's `-PvesperReviewOnly`).

### Task 13: The Saints' rise, and the Mycophant and Hemorath cards

**Files:**
- Create: `client/cinematic/scene/SaintRiseScene.java`
- Modify:
  - `SaintTrialEntity` and `SeraphaeEntity`: a rise window (`SAINT_RISE_TICKS = 60`) with a synced
    `DATA_RISE_TICK`. It is invulnerable and passive while the window runs, and the bar shows at its end.
  - Each Saint's renderer translates by `-(1 - HemoEasing.smoothstep(rise)) * 2.2` blocks while rising.
  - `HarbingerSaintEncounterHooks.spawnSaintBoss`: after `addFreshEntity`, begins the rise and plays `SCENE_SAINT_RISE`.
    The card keys come from the boss's frame id: the scene reads the bound entity's type to choose
    `boss.hemomancy.<saint>.*`.
  - The rise emits a sarcophagus dust ring through the existing Saint or Hemomancy particle helpers
    (`HemoParticleData`).
  - `HemorathEntity.onPhaseTransition` plays `SCENE_HEMORATH_EMPTIES_CARD`.
  - `MycophantEntity.requestSplit` plays `SCENE_MYCOPHANT_SPLIT_CARD`.
- Lang: names and epithets for the four Saints, plus the `hemorath_empties` card ("Saint Hemorath" / "The Vessel
  Empties") and the `mycophant_split` card ("The Mycophant" / "Joining").
- Test: `SaintRiseTest`:
  - `riseWindowMatchesScene`;
  - `everySaintHasCardKeys` (lang JSON through `ResourceReader`).
- [ ] Steps 1–4 as before.

### Task 14: Language check, docs and the full verification

**Files:**
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (any key still missing),
  `docs/HEMOMANCY_REFERENCE.md` (a "Boss presentation" subsection: frames, scenes, skip, config),
  `docs/architecture/ARCHITECTURE_STANDARDS.md` (a rule: a boss's bar names a frame, and a boss's opening is a
  `BossScene` played through `BossScenes.play`; index `BossPresentationBindingTest`), and
  `docs/architecture/evidence/source-text-impact-map.csv` (regenerate with `python gradle/source-text-impact-map.py`)
- Test: `BossPresentationLangTest`. Every `boss.hemomancy.*` key referenced in `client/cinematic/**` exists in
  `en_us.json`.

- [ ] **Step 1:** Write the lang test and run it; fix any missing keys.
- [ ] **Step 2:** Run `./gradlew.bat build` and expect BUILD SUCCESSFUL.
- [ ] **Step 3:** Run `./gradlew.bat test` and expect every test to pass. Record any failures that existed before this
  work, with their output.
- [ ] **Step 4:** Run the review clients (Mycophant, Annetta, Vesper, Naeglerophaeon) for the new scenes. Read the
  screenshots and tune the frame art and shot keys.
- [ ] **Step 5:** Send one final reviewer over the whole change (memory: native execution, one final reviewer).

# Lidless Door Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A creative-only 1x2 door block that holds the player in its doorway while they walk a configurable void crossing (default 16 blocks) and steps them out the far side, with the void drawn on the door panel from outside and across the whole screen inside.

**Architecture:** Server side copies the Axonal Transduction hold: a UUID-keyed session map pins the player at the door and turns their input payload into progress. The hold's mixins are rerouted through one shared `HeldTraveller` predicate. Client side mirrors Axonal's client: a state payload drives a client state table, a full-screen post pass draws the void with two doorway windows, and a block-entity renderer draws the same void on the door panel from outside through a shared GLSL include.

**Tech Stack:** NeoForge 1.21.1 / Java 21, Mixin, GLSL 150 core shaders, GameTest framework, Gradle review clients.

**Spec:** `docs/superpowers/specs/2026-11-03-lidless-door-design.md`

**Commits:** none. The owner's standing rule is to leave work in the working tree and commit only when asked; every "commit" step is omitted.

## Global Constraints

- NeoForge imports only; ids through `Hemomancy.rloc("snake_case")`; subscribers `@EventBusSubscriber(modid = Hemomancy.MOD_ID[, value = Dist.CLIENT])`, never `bus=`.
- No `getPersistentData` call anywhere in the new code (`PersistentDataAllowListTest` stays unchanged).
- No `Component.literal("English")`; the one player-facing string is the block name key `block.hemomancy.lidless_door` in `en_us.json`.
- No vanilla particles; glow motes through `HemoParticleData.glow(ParticleColor)`.
- Nothing under `common/` names a client class; client calls go through `common/util/ClientBridge`.
- Payload changes bump `PacketHandler.PROTOCOL_VERSION` from `"34"` to `"35"` and rewrite `src/test/resources/network/payload-registry.txt`.
- Render types are built once through `SeededRenderTypes`; the GLSL include reads no uniform, imports nothing, starts with `// `.
- Frozen ids: `src/gameTest/resources/frozen/registry-ids.txt` and `creative-tab-order.txt` gain the new entries.
- Hemomancy particle, shader and sound assets only; no new audio; textures reuse `block/hematic_iron_door_top.png` and `block/hematic_iron_door_bottom.png`.
- Creative-only: no recipe, no loot table, no advancement.

## Review Focus

1. A player who logs out mid-crossing must be saved outside the door, on the entry side, with gravity and physics restored. Test in Task 3 (`logout_returns_player`).
2. Two players crossing the same door at once must each keep their own progress and both reach their own exits. Test in Task 3 (`two_players_cross_independently`).
3. A creative player flying into the door must land standing on the far side without keeping the hold's no-gravity flag (`noGravityBefore` is restored). Test in Task 3 (`flying_player_restores_gravity`).
4. The hold must not fight Axonal: a player pinned in a door beside a synaptic node must not be pulled into Axonal travel, and an Axonal traveller touching a door must not start a crossing. Test in Task 3 (`axonal_and_door_exclude_each_other`).
5. A door whose far cell is two blocks tall but whose upper cell is blocked (a 1-high gap) must hold at the end rather than wedge the player. Test in Task 3 (`blocked_far_cell_holds`, which blocks the upper cell only).

---

### Task 1: Shared hold predicate and standing-space helper

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/util/HeldTraveller.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/util/StandingSpaces.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/AxonalTransductionManager.java` (`safeLanding`, `safe`, `canEnter`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/Paralysis.java:33`
- Modify: `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinEntity.java`, `MixinAxonalLivingEntity.java`, `MixinParalysisPlayer.java:15`, `MixinLocalPlayer.java:26`, `MixinParalysisServerInput.java:19,28,43`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/endgame/NaeglerophaeonEntity.java:214`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/util/ClientBridge.java` (one new default method)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/ClientBridgeImpl.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/architecture/HeldTravellerGuardrailTest.java`

**Interfaces:**
- Produces: `HeldTraveller.isHeld(Player)`: true when `AxonalTransductionManager.isTraveling(player)` or `LidlessPassageService.isCrossing(player)` (Task 3; until then the second call is a stub returning `false` in a new `common/block/shared/LidlessPassageService.java` holding only that method). `HeldTraveller.hold(Player)`: sets `noPhysics = true`, `setNoGravity(true)`, `setDeltaMovement(Vec3.ZERO)`, `resetFallDistance()`.
- Produces: `StandingSpaces.safe(Entity entity, ServerLevel level, Vec3 feet)` and `StandingSpaces.near(Entity entity, ServerLevel level, BlockPos node)` (returns `Vec3` or `null`), bodies moved from `AxonalTransductionManager.safe` / `safeLanding` unchanged (the `0.3` half-width, `1.8` height, the `-2..2 / -1..2` search box, prefer `node.above()`).
- Produces: `ClientBridge.isCrossingLidlessDoor(Player)` default `false`; `ClientBridgeImpl` returns `LidlessPassageClientState.isCrossing(player)` (Task 4; until then returns `false`).

- [ ] **Step 1: Write the guardrail test**

```java
class HeldTravellerGuardrailTest {
    @Test void mixinsAskTheSharedPredicate() {
        for (String file : List.of("mixin/core/MixinEntity.java", "mixin/core/MixinAxonalLivingEntity.java",
                "mixin/core/MixinParalysisPlayer.java", "mixin/core/MixinLocalPlayer.java",
                "mixin/core/MixinParalysisServerInput.java", "common/manipulation/ductilis/Paralysis.java",
                "common/entity/boss/endgame/NaeglerophaeonEntity.java")) {
            String text = SourceAssertions.read(RepoPaths.mainJava().resolve("com/vincenthuto/hemomancy").resolve(file));
            assertFalse(text.contains("AxonalTransductionManager.isTraveling("), file + " asks HeldTraveller.isHeld");
            assertTrue(text.contains("HeldTraveller.isHeld("), file + " asks HeldTraveller.isHeld");
        }
    }
    @Test void axonalUsesSharedStandingSpaces() {
        String text = SourceAssertions.read(RepoPaths.mainJava().resolve("com/vincenthuto/hemomancy/common/manipulation/ductilis/AxonalTransductionManager.java"));
        assertFalse(text.contains("private static boolean safe("));
        assertTrue(text.contains("StandingSpaces.near("));
    }
}
```
Use the exact helper names `RepoPaths` and `SourceAssertions` expose (read `src/test/java/com/vincenthuto/hemomancy/support/RepoPaths.java` for the main-source accessor name).

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew.bat test --tests "*HeldTravellerGuardrailTest*"`
Expected: FAIL (predicate and helper do not exist).

- [ ] **Step 3: Create `HeldTraveller` and `StandingSpaces`, the `LidlessPassageService.isCrossing` stub, and the `ClientBridge` default method**

`LidlessPassageService.isCrossing(Player)`: `player instanceof ServerPlayer ? false : ClientBridge.get().isCrossingLidlessDoor(player)` for now (the server branch fills in at Task 3).

- [ ] **Step 4: Reroute the seven callers**

In `MixinEntity.move`: keep `SilentArchonArmorAbilityHandler.applySilentSlippingNoClip` and `AxonalTransductionManager.applyMovement`; replace the cancel condition with `if (HeldTraveller.isHeld(player)) { HeldTraveller.hold(player); ci.cancel(); }`. In the other six, swap `AxonalTransductionManager.isTraveling(x)` for `HeldTraveller.isHeld(x)`. Keep `Paralysis.blocksActions`' other terms. Replace `AxonalTransductionManager.safe` / `safeLanding` bodies with calls to `StandingSpaces` (keep the public `safeLanding(ServerPlayer, ServerLevel, BlockPos)` signature delegating to `StandingSpaces.near` so the GameTests still compile). Add `&& !LidlessPassageService.isCrossing(player)` to `AxonalTransductionManager.canEnter`.

- [ ] **Step 5: Run the guardrail and the existing Axonal suite**

Run: `./gradlew.bat test --tests "*HeldTravellerGuardrailTest*" --tests "*PersistentDataAllowListTest*" --tests "*CommonClientIsolationTest*"` then `./gradlew.bat naeglerophaeonGameTestServer`
Expected: all PASS, Axonal GameTests unchanged.

---

### Task 2: The block, its block entity, assets and registration

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/LidlessDoorBlock.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/shared/LidlessDoorBlockEntity.java`
- Modify: `common/init/BlockInit.java` (register beside `hematic_iron_door`), `common/init/BlockEntityInit.java`, `common/init/CreativeTabInit.java:59` (append `BlockInit.lidless_door` to `WIP`)
- Modify: `config/HemoServerConfig.java` (new `lidless_door` group)
- Create: `src/main/resources/assets/hemomancy/blockstates/lidless_door.json`, `models/block/lidless_door_lower.json`, `models/block/lidless_door_upper.json`, `models/item/lidless_door.json`, `textures/item/lidless_door.png`
- Create: `tools/model_export/lidless_door_icon.py` (writes the 16x16 item icon)
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (`"block.hemomancy.lidless_door": "Lidless Door"`)
- Modify: `src/gameTest/resources/frozen/registry-ids.txt` (block, item, block_entity_type `hemomancy:lidless_door`), `creative-tab-order.txt` (after `hemomancy:gourdvine_tap` in the `# hemomancywip` section)
- Test: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/LidlessPassageGameTests.java` (first two tests), `gradle/game-tests.gradle` (new holder row)

**Interfaces:**
- Produces: `LidlessDoorBlock extends BaseEntityBlock` with `FACING = HorizontalDirectionalBlock.FACING`, `HALF = BlockStateProperties.DOUBLE_BLOCK_HALF`; `static BlockPos lowerOf(BlockState, BlockPos)`; `getCollisionShape` empty when `LidlessPassageService.mayCross(entity)` (Task 3; stub `false` until then, so this task's test asserts the player case only after Task 3 lands: write both tests now, expect the player one to pass in Task 3), the panel otherwise; `entityInside` calls `LidlessPassageService.offer(ServerPlayer, BlockPos lower, BlockState lowerState)` (stub no-op until Task 3).
- Produces: `BlockInit.lidless_door`, `BlockEntityInit.lidless_door`, `HemoServerConfig.LIDLESS_PASSAGE_LENGTH` (`defineInRange("passage_length", 16, 4, 64)` under `push("lidless_door")`).
- Produces: the holder `lidless_passage_validation` with run `lidlessPassageGameTestServer`, directory `build/lidless-passage-gametest`, `alpha: true`, and a copy of `ductilis_arena.nbt` under `src/gameTest/resources/data/lidless_passage_validation/structure/`.

- [ ] **Step 1: Write the first two GameTests**

```java
@GameTestHolder("lidless_passage_validation")
@PrefixGameTestTemplate(false)
public final class LidlessPassageGameTests {
    static final String ARENA = "ductilis_arena";
    @GameTest(templateNamespace = "lidless_passage_validation", template = ARENA, batch = "lidless")
    public static void door_places_as_a_pair_and_breaks_as_a_pair(GameTestHelper h) {
        BlockPos lower = h.absolutePos(new BlockPos(2, 3, 2));
        h.getLevel().setBlock(lower, BlockInit.lidless_door.get().defaultBlockState().setValue(LidlessDoorBlock.FACING, Direction.NORTH), 3);
        // setPlacedBy is what the item calls; emulate with the block's own placeUpper
        LidlessDoorBlock.placeUpper(h.getLevel(), lower, h.getLevel().getBlockState(lower));
        h.assertBlockProperty(lower.above(), LidlessDoorBlock.HALF, DoubleBlockHalf.UPPER);
        h.getLevel().destroyBlock(lower, false);
        h.assertBlockPresent(Blocks.AIR, lower.above());
        h.succeed();
    }
    @GameTest(templateNamespace = "lidless_passage_validation", template = ARENA, batch = "lidless")
    public static void panel_is_solid_to_a_zombie(GameTestHelper h) {
        BlockPos lower = place(h);  // helper: sets both halves facing NORTH at (2,3,2), returns lower
        Zombie zombie = h.spawn(EntityType.ZOMBIE, new BlockPos(2, 3, 4));
        VoxelShape shape = h.getLevel().getBlockState(lower).getCollisionShape(h.getLevel(), lower, CollisionContext.of(zombie));
        h.assertTrue(!shape.isEmpty(), "panel blocks mobs");
        h.succeed();
    }
}
```

- [ ] **Step 2: Add the holder row to `gradle/game-tests.gradle`, copy the arena template, run to see the tests fail**

Run: `./gradlew.bat lidlessPassageGameTestServer`
Expected: compile failure (block missing).

- [ ] **Step 3: Write `LidlessDoorBlock`**

Properties: `BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).lightLevel(s -> 7).noOcclusion()`. `getStateForPlacement` returns `null` unless the cell above is replaceable, facing from `context.getHorizontalDirection()`; `setPlacedBy` calls `placeUpper(Level, BlockPos lower, BlockState lowerState)` which sets `lower.above()` to the lower state with `HALF = UPPER`. `updateShape` removes a half whose partner is gone (copy `DoorBlock.updateShape`'s half logic). `playerWillDestroy` removes the other half (copy `DoorBlock`'s). Panel shape: `Block.box(0, 0, 7, 16, 16, 9)` for NORTH/SOUTH, `box(7, 0, 0, 9, 16, 16)` for EAST/WEST. `getShape` returns the panel; `getCollisionShape(state, level, pos, context)`: `context instanceof EntityCollisionContext ec && ec.getEntity() != null && LidlessPassageService.mayCross(ec.getEntity()) ? Shapes.empty() : panel`. `getRenderShape` `MODEL`. `newBlockEntity` only for `HALF == LOWER`. `entityInside(state, level, pos, entity)`: `if (!level.isClientSide && entity instanceof ServerPlayer p) LidlessPassageService.offer(p, lowerOf(state, pos), level.getBlockState(lowerOf(state, pos)))`.

- [ ] **Step 4: Register block, block entity, config, WIP entry, lang, frozen ids; author the models**

`lidless_door_lower.json`: two jamb elements `[0,0,6]-[2,16,10]` and `[14,0,6]-[16,16,10]`, texture `hemomancy:block/hematic_iron_door_bottom`, UVs `[0,0,2,16]` and `[14,0,16,16]` on every face. `lidless_door_upper.json`: the same two jambs plus a lintel `[2,14,6]-[14,16,10]`, texture `hematic_iron_door_top`, lintel UV `[2,0,14,2]`. `render_type: cutout`. Blockstate: variants `facing=north,half=lower` → lower model, `y` rotation 0/90/180/270 for north/east/south/west, same for upper. Item model `minecraft:item/generated` with `layer0: hemomancy:item/lidless_door`. The icon script draws a 16x16 door silhouette: 2px dark iron jambs, black interior with three dark-red (`#5a0606`) specks and one open red eye (`#a81414`) at (8,6); run it once with `python tools/model_export/lidless_door_icon.py`.

- [ ] **Step 5: Run the holder and the frozen test**

Run: `./gradlew.bat lidlessPassageGameTestServer`
Expected: `door_places_as_a_pair_and_breaks_as_a_pair` and `panel_is_solid_to_a_zombie` PASS; `FrozenIdentifierGameTests` PASS (it runs in the alpha set; run `./gradlew.bat alphaCheck` at Task 7).

---

### Task 3: Payloads, server service and its GameTests

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/network/lidless/LidlessPassageStatePacket.java`, `LidlessPassageInputPacket.java`
- Modify: `common/network/PacketHandler.java` (register both after the Axonal pair; `PROTOCOL_VERSION = "35"`), `src/test/resources/network/payload-registry.txt` (`version 35`, `playToServer LidlessPassageInputPacket`, `playToClient LidlessPassageStatePacket`, alphabetical)
- Modify: `common/block/shared/LidlessPassageService.java` (full implementation)
- Modify: `common/event/TransientServerStateEvents.java` (`clearSessionState` beside Axonal's at line 129, `logout` beside line 174)
- Test: `LidlessPassageGameTests.java` (the rest)

**Interfaces:**
- Produces: `record LidlessPassageStatePacket(ResourceLocation dimension, int playerId, long token, boolean active, BlockPos door, Direction facing, boolean towardFront, float progress, float length)`; `record LidlessPassageInputPacket(long token, int direction, boolean sprinting, boolean sneaking)` with `handle` enqueuing `LidlessPassageService.input(ServerPlayer, token, direction, sprinting, sneaking)` after `ServerboundRateLimiter.tryAcquire(player, "lidless_input", 1)`.
- Produces in `LidlessPassageService` (`@EventBusSubscriber(modid = Hemomancy.MOD_ID)`):
  - `static boolean mayCross(Entity)`: `entity instanceof Player p && p.isAlive() && !p.isSpectator() && !p.isPassenger() && !p.isVehicle() && !AxonalTransductionManager.isTraveling(p)`.
  - `static boolean isCrossing(Player)`: server `SESSIONS.containsKey(uuid)`, client via bridge.
  - `static void offer(ServerPlayer, BlockPos lower, BlockState lowerState)`: no-op unless `mayCross` and not already crossing and `lowerState` is the door's lower half; starts a session: `towardFront = dot(feet - center, facingVec) < 0` where `center = Vec3.atBottomCenterOf(lower)`; records `noGravityBefore`; `stopFallFlying()`; drops mob targets within 64 as Axonal does; `ManipulationChannelManager.stop(p, false)`; `teleportTo(center)`; `HeldTraveller.hold`; sends the state to tracking and self; plays `SoundInit.ITEM_SYNAPTIC_STEP_USE` pitch `0.6F`; scatters 8 `HemoParticleData.glow(new ParticleColor(120, 8, 8))` at the panel centre.
  - `static void input(ServerPlayer, long token, int direction, boolean sprinting, boolean sneaking)`.
  - `static void tick(ServerPlayer)` from `PlayerTickEvent.Post` (and `PlayerTickEvent.Pre` calls `HeldTraveller.hold` on crossing players): ends in place if dead; ends toward entry if the door at `session.door` is no longer `BlockInit.lidless_door` lower with `session.facing`, or `position().distanceToSqr(session.expected) > 0.04`; advances `progress` by `speed(player) * (direction read from input, 0 if older than 4 ticks)` where `speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.16 * (sprinting ? 1.3 : sneaking ? 0.3 : 1.0)`; clamps to `[0, length]`; at `>= length` tries `exit(session, far cell)`, at `<= 0` (after at least one tick of backward input) tries `exit(session, entry cell)`; otherwise re-pins and sends the state to the player alone.
  - `static void end(ServerPlayer, boolean recover)`: removes the session, restores `noPhysics = isSpectator()`, `setNoGravity(noGravityBefore)`, zero velocity, `connection.resetPosition()`, chunk-source move; when `recover`, lands at the entry cell through `landing(...)`; sends the inactive state to tracking and self.
  - `static void logout(ServerPlayer)` (`end(p, true)`), `static void clearSessionState()`.
  - Landing rule `landing(ServerPlayer, ServerLevel, BlockPos cell)`: `StandingSpaces.safe(p, level, Vec3.atBottomCenterOf(cell))` ? that : `StandingSpaces.near(p, level, cell.below())`, else `Vec3.atBottomCenterOf(session.door)`. The far-cell exit uses only the first check (an unsafe far cell holds); the entry-side exit and every recovery use the full chain.
  - Events: `LivingIncomingDamageEvent` cancel unless `BYPASSES_INVULNERABILITY`; `LivingChangeTargetEvent` clear; `PlayerEvent.StartTracking` resend; `PlayerChangedDimensionEvent` and `LivingDeathEvent` end in place.
  - Length read once at session start: `HemoConfigValues.get(HemoServerConfig.LIDLESS_PASSAGE_LENGTH, 16)`.

- [ ] **Step 1: Write the GameTests**

Fixture in the Axonal style: `player(h, sink)` from `DuctilisGameTests.player`, door at `(2,3,2)` facing NORTH, player feet at `(2.5, 3, 3.9)` (south of the door, so `towardFront` is true and the far cell is `(2,3,1)`); `enter()` moves the player to `(2.5, 3, 2.6)` and calls `LidlessPassageService.offer(player, lower, state)`; `forward(n)` sends `input(player, token, 1, false, false)` then `tick(player)` n times; `back(n)` the same with `-1`.

```java
public static void entering_pins_and_syncs(h) { enter; assert isCrossing; assert position == atBottomCenterOf(lower); assert last captured state packet active && towardFront && length == 16; }
public static void forward_input_advances_by_walking_speed(h) { enter; forward(1); assert progress ≈ 0.216 (±0.01); }
public static void sprint_and_sneak_scale_progress(h) { enter; input(token,1,true,false); tick; p1; input(token,1,false,true); tick; assert p2 - p1 ≈ 0.0648 and p1 ≈ 0.2808; }
public static void completion_lands_in_far_cell(h) { enter; forward(80); assert !isCrossing; assert blockPosition == (2,3,1); assert !isNoGravity && !noPhysics; }
public static void walking_back_lands_on_entry_side(h) { enter; forward(3); back(20); assert !isCrossing; assert blockPosition == (2,3,3); }
public static void blocked_far_cell_holds(h) { setBlock((2,4,1), STONE); enter; forward(80); assert isCrossing; assert progress == 16; }
public static void broken_door_returns_to_entry_side(h) { enter; forward(10); destroyBlock(lower); tick; assert !isCrossing; assert blockPosition == (2,3,3); }
public static void death_ends_in_place(h) { enter; forward(10); player.kill(); tick; assert !isCrossing; }
public static void spectator_does_not_enter(h) { player.setGameMode(SPECTATOR); enter; assert !isCrossing; }
public static void player_collision_is_empty_while_eligible(h) { assert getCollisionShape(CollisionContext.of(player)).isEmpty(); }
public static void logout_returns_player(h) { enter; forward(10); LidlessPassageService.logout(player); assert !isCrossing; assert blockPosition == (2,3,3); assert !isNoGravity; }
public static void two_players_cross_independently(h) { two fixtures, second enters from the north side; forward both 10; assert progress differ in direction; forward first 80; assert first at (2,3,1) and second still crossing; }
public static void flying_player_restores_gravity(h) { player.getAbilities().flying = true; player.setNoGravity(true) before enter; forward(80); assert isNoGravity() == true (restored to what it was) and crossing over; }
public static void axonal_and_door_exclude_each_other(h) { place a synaptic node under the entry cell; enter the door; AxonalTransductionManager.tick(player); assert !AxonalTransductionManager.isTraveling(player) && isCrossing; }
public static void server_stop_clears_sessions(h) { enter; LidlessPassageService.clearSessionState(); assert !isCrossing; }
```

- [ ] **Step 2: Run to see them fail**

Run: `./gradlew.bat lidlessPassageGameTestServer`
Expected: the new tests fail (service stubs).

- [ ] **Step 3: Write the two payloads, register them, bump the protocol, rewrite the registry file**

- [ ] **Step 4: Implement `LidlessPassageService` and wire `TransientServerStateEvents`**

Session class fields: `ServerLevel level; BlockPos door; Direction facing; boolean towardFront; double progress; long token; boolean noGravityBefore; float length; Vec3 expected; int input; boolean sprinting, sneaking; long lastInputTick; boolean movedBack`. Far cell = `door.relative(towardFront ? facing : facing.getOpposite())`, entry cell the opposite.

- [ ] **Step 5: Run the holder and the payload test**

Run: `./gradlew.bat lidlessPassageGameTestServer` and `./gradlew.bat test --tests "*PayloadProtocolVersionTest*" --tests "*NetworkPackageGuardrailTest*"`
Expected: all PASS.

---

### Task 4: Client state, handlers and the hold on the client

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/data/LidlessPassageClientState.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/event/LidlessPassageClientEvents.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/sound/LidlessPassageSound.java`
- Modify: `client/network/ClientboundEffectHandlers.java` (register `LidlessPassageStatePacket.TYPE` beside Axonal's at line 102; handler enqueues `LidlessPassageClientState.update(packet)`), `client/ClientBridgeImpl.java` (`isCrossingLidlessDoor`), `client/event/ClientSessionEvents.java` (clear on logout and level unload)
- Test: `src/test/java/com/vincenthuto/hemomancy/architecture/LidlessClientWiringTest.java`

**Interfaces:**
- Produces: `LidlessPassageClientState` with `record View(BlockPos door, Direction facing, boolean towardFront, float length, long token, float previousProgress, float progress)`; `static void update(LidlessPassageStatePacket)` (adds or removes by `playerId` when the dimension matches; keeps the prior `progress` as `previousProgress`); `static boolean isCrossing(Player)`; `static View local()` (null when the local player is not crossing); `static float progress(float partialTick)` lerped; `static void clear()`.
- Produces: `LidlessPassageClientEvents` (`Dist.CLIENT`): `ClientTickEvent.Post` forces `CameraType.FIRST_PERSON` while `local() != null` and restores the saved type after, calls `HeldTraveller.hold(mc.player)` while crossing, and starts/stops `LidlessPassageSound`; `MovementInputUpdateEvent` sends `LidlessPassageInputPacket(token, up - down, keySprint down, keyShift down)` when `mc.screen == null` (direction 0 with a screen open) and zeros the input; `InputEvent.InteractionKeyMappingTriggered` cancel; `RenderHandEvent` cancel; `RenderBlockScreenEffectEvent` cancel; `RenderPlayerEvent.Pre` cancel when `isCrossing(event.getEntity())`; `RenderGuiLayerEvent.Pre` cancel every layer except `VanillaGuiLayers.CHAT` and `Hemomancy.rloc("screen_effects")` while crossing.
- Produces: `LidlessPassageSound extends AbstractTickableSoundInstance` on `SoundInit.RITE_AMBIENT_DRONE`, looping, volume `0.7F`, stops when `local() == null`.

- [ ] **Step 1: Write the wiring test**

```java
class LidlessClientWiringTest {
    @Test void statePacketIsDispatchedToTheClientHandler() {
        String handlers = SourceAssertions.read(RepoPaths...("client/network/ClientboundEffectHandlers.java"));
        assertTrue(handlers.contains("ClientPayloadHandlers.register(LidlessPassageStatePacket.TYPE"));
        String session = SourceAssertions.read(RepoPaths...("client/event/ClientSessionEvents.java"));
        assertTrue(session.contains("LidlessPassageClientState.clear()"));
        String events = SourceAssertions.read(RepoPaths...("client/event/LidlessPassageClientEvents.java"));
        for (String hook : List.of("RenderPlayerEvent.Pre", "RenderGuiLayerEvent.Pre", "MovementInputUpdateEvent", "RenderHandEvent"))
            assertTrue(events.contains(hook), hook);
    }
}
```

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew.bat test --tests "*LidlessClientWiringTest*"`
Expected: FAIL.

- [ ] **Step 3: Write the state, the events, the sound, the handler registration, the bridge and the session clears**

- [ ] **Step 4: Run the wiring test and the client-isolation guardrails**

Run: `./gradlew.bat test --tests "*LidlessClientWiringTest*" --tests "*CommonClientIsolationTest*" --tests "*RegistrationStyleGuardrailTest*" --tests "*HudAndWorldDrawingGuardrailTest*"`
Expected: PASS.

---

### Task 5: The void include, the panel shader and the door renderer

**Files:**
- Create: `src/main/resources/assets/hemomancy/shaders/include/noise/lidless_void.glsl`
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/lidless_door_panel.json`, `.vsh`, `.fsh`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/tile/shared/LidlessDoorRenderer.java`
- Modify: `client/render/shader/ShaderInit.java` (`LIDLESS_DOOR_PANEL` holder on `DefaultVertexFormat.BLOCK`, registered beside `RING_DOOR_OPENING`), `client/render/HemoRenderTypes.java` (`lidlessDoorPanel(...)`), `client/registry/EntityRendererRegistrar.java:320` (`registerBlockEntityRenderer(BlockEntityInit.lidless_door.get(), LidlessDoorRenderer::new)`)
- Test: `src/test/java/com/vincenthuto/hemomancy/architecture/LidlessShaderContractTest.java`

**Interfaces:**
- Produces GLSL (all parameters, no uniforms):
  - `float lidlessHash(vec2 p)`, `float lidlessNoise(vec2 p)`, `float lidlessFbm(vec2 p, float drift)` (5 octaves, the Monolith `mat2(1.62, 1.15, -1.15, 1.62)` rotation).
  - `vec3 lidlessVoid(vec3 ray, float time, float seed, float twist)`: `ray` unit length in passage space (`+z` toward the exit, `+y` up). Builds `vec2 p = ray.xy / (abs(ray.z) + 0.35)` then rotates `p` by `angle = twist * 2.4 + time * 0.05 + length(p) * 1.8` (the vortex), layers `lidlessFbm(p * 1.7, ...)`, `lidlessFbm(p * 4.1 + seed, ...)` and `lidlessFbm(p * 9.0 - seed, ...)` at rates `0.03`, `0.07`, `0.12`, and shades: `black = vec3(0.0015, 0.0001, 0.0001)`, `red = vec3(0.62, 0.015, 0.010)`, veins as `smoothstep(0.052, 0.010, abs(fbmA - 0.5))` with the glow `vec3(0.82, 0.018, 0.010)`; adds `lidlessEyes`.
  - `vec3 lidlessEyes(vec3 ray, float time, float seed)`: cells on the far sphere from `vec2 cell = floor(dir * 6.0)` with `dir = ray.xy / (abs(ray.z) + 0.5)`; an eye exists when `lidlessHash(cell + seed) < 0.25`; the eye centre is `cell + 0.5 + (hash - 0.5) * 0.4`; its almond `smoothstep` of `abs(d.y) * 2.2 + abs(d.x)` against radius `0.28`, scaled by `blink = smoothstep(0.0, 0.08, fract(time * 0.011 + hash * 7.0) ...)` open about 85% of a hashed 4 to 9 second cycle; sclera `vec3(0.36, 0.02, 0.02)`, iris `vec3(0.78, 0.08, 0.05)` offset toward the ray centre by `-d * 0.35`, slit pupil black along `y`. Eyes with `abs(ray.z) > 0.9` ahead (`ray.z > 0`) are scaled `1.6` and thinned to `hash < 0.08`.
- Produces: `HemoRenderTypes.lidlessDoorPanel(float gameTime, float doorSeed, Vec3 passageRight, Vec3 passageUp, Vec3 passageForward)`: `SeededRenderTypes<Float, LidlessDoorPanelUniforms, RenderType>` built like `createRingDoorOpening` (BLOCK format, QUADS, `NO_CULL`, `TRANSLUCENT_TRANSPARENCY`, `LEQUAL_DEPTH_TEST`, `LIGHTMAP`), uniforms `HemoTime`, `DoorSeed`, `PassageRight`, `PassageUp`, `PassageForward` (view-space vectors).
- Produces: `LidlessDoorRenderer implements BlockEntityRenderer<LidlessDoorBlockEntity>`: skips when `LidlessPassageClientState.local() != null && local().door().equals(blockEntity.getBlockPos())`; draws the front and back faces of the panel (`z = 7/16` and `9/16` in block space for NORTH, rotated by facing) as one quad each spanning `x 2/16..14/16`, `y 0..2 - 2/16` (both halves, under the lintel), with `uv` the quad's own 0..1, with the type keyed `doorSeed` for the front and `doorSeed + 0.5` for the back (forward flipped); `doorSeed = (pos.hashCode() & 0xFFFF) / 977.0F`; `getRenderBoundingBox` covers both halves. The passage basis: `forward = facing.getOpposite()` for the front face (looking into the door from its front, the exit is behind it), negated for the back; `up = (0,1,0)`; `right = up × forward`; all three rotated into view space with the camera's rotation (`camera.rotation()` as a quaternion applied to the world vectors).

- [ ] **Step 1: Write the contract test**

```java
class LidlessShaderContractTest {
    @Test void includeIsSelfContained() {
        String inc = ResourceReader.read("assets/hemomancy/shaders/include/noise/lidless_void.glsl");
        assertTrue(inc.startsWith("// "));
        assertFalse(inc.contains("uniform")); assertFalse(inc.contains("#moj_import"));
        for (String fn : List.of("vec3 lidlessVoid(vec3 ray, float time, float seed, float twist)", "vec3 lidlessEyes(vec3 ray, float time, float seed)"))
            assertTrue(inc.contains(fn), fn);
    }
    @Test void panelShaderImportsTheIncludeAndDeclaresItsUniforms() {
        String fsh = ResourceReader.read("assets/hemomancy/shaders/core/world/lidless_door_panel.fsh");
        assertTrue(fsh.contains("#moj_import <hemomancy:noise/lidless_void.glsl>"));
        String json = ResourceReader.read("assets/hemomancy/shaders/core/world/lidless_door_panel.json");
        for (String u : List.of("HemoTime", "DoorSeed", "PassageRight", "PassageUp", "PassageForward")) assertTrue(json.contains("\"" + u + "\""), u);
    }
}
```
Use `ResourceReader`'s real accessor name from `support/ResourceReader.java`.

- [ ] **Step 2: Run it to see it fail**

Run: `./gradlew.bat test --tests "*LidlessShaderContractTest*"`
Expected: FAIL.

- [ ] **Step 3: Write the include, the panel shader (vertex: `generic.vsh` pattern plus `out vec3 viewPos = (ModelViewMat * vec4(Position,1)).xyz`; fragment: `ray = normalize(viewPos)` projected onto the three passage vectors, `fragColor = vec4(lidlessVoid(ray, HemoTime, DoorSeed, 0.0), 1.0) * vertexColor`), the render type, the holder and the renderer; register the renderer**

- [ ] **Step 4: Run the contract test and the shader and render-type guardrails**

Run: `./gradlew.bat test --tests "*LidlessShaderContractTest*" --tests "*ShaderNoiseIncludeGuardrailTest*" --tests "*RenderTypeCachingGuardrailTest*" --tests "*MaterialQuadGuardrailTest*"`
Expected: PASS. (GPU compile is proven in Task 7.)

---

### Task 6: The crossing view

**Files:**
- Create: `src/main/resources/assets/hemomancy/shaders/core/screen/lidless_passage_view.json`, `.fsh`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/LidlessPassageView.java`
- Modify: `client/render/shader/ShaderInit.java` (`LIDLESS_PASSAGE_VIEW` on `POSITION_TEX_COLOR`, registered beside `AXONAL_TRAVEL_VIEW`), `client/event/ScreenOverlayEvents.java:57` (call `LidlessPassageView.renderGrade(...)` before Axonal's, returning when it draws)
- Test: extend `LidlessShaderContractTest` and `LidlessClientWiringTest`

**Interfaces:**
- Produces: `LidlessPassageView.renderGrade(GuiGraphics, int width, int height, float partialTick)`: returns `false` when `LidlessPassageClientState.local() == null`; otherwise blits the main target into a cached `TextureTarget scene` (the `AxonalTravelView.targets` pattern, same size rule), sets the uniforms and draws the full-screen quad with `ShaderInit.LIDLESS_PASSAGE_VIEW`, returns `true`. `clear()` destroys the target; called from `LidlessPassageClientEvents` when the crossing ends and from `ClientSessionEvents`.
- Shader `lidless_passage_view.fsh` (vertex `hemomancy:screen/screen_generic`), samplers `Sampler0` (scene), uniforms: `HemoTime`, `Progress`, `Length`, `Seed`, `CamRight`, `CamUp`, `CamForward` (vec3, the camera basis expressed in passage space), `FovTan` (tan of half the vertical fov), `Aspect`, `EyeHeight` (`1.62`). Per pixel: `ndc = uv * 2 - 1` (with `uv.y` flipped as Axonal's does), `rayCam = normalize(vec3(ndc.x * Aspect * FovTan, ndc.y * FovTan, 1.0))`, `ray = CamRight * rayCam.x + CamUp * rayCam.y + CamForward * rayCam.z`; window test: ahead when `ray.z > 0.001` at `t = max(Length - Progress, 0.05) / ray.z`, behind when `ray.z < -0.001` at `t = max(Progress, 0.05) / -ray.z`; hit `(x, y) = (ray.x, ray.y) * t`; inside `= smoothstep(0.5, 0.46, abs(x)) * smoothstep(-EyeHeight, -EyeHeight + 0.06, y) * smoothstep(2.0 - EyeHeight, 2.0 - EyeHeight - 0.06, y)`; `rim = inside edge band 0.04 wide` tinted `vec3(0.62, 0.015, 0.010)`; `world = texture(Sampler0, uv).rgb`; `color = mix(lidlessVoid(ray, HemoTime, Seed, Progress / Length), world, inside) + rim`; `fragColor = vec4(color, 1.0)`.
- Uniform sources in Java: `mc.gameRenderer.getMainCamera()`; `CamForward/Right/Up` = the camera's forward, left-negated and up vectors (from `camera.getLookVector()`, `camera.getLeftVector()`, `camera.getUpVector()`) expressed in passage space, where passage `forward = view.towardFront() ? facing.getOpposite()... ` — define passage forward as the world direction from entry cell to far cell: `facing` when `towardFront`, else `facing.getOpposite()`; passage `up = (0,1,0)`, passage `right = up × forward`; a world vector `v` becomes `(v·right, v·up, v·forward)`. `FovTan = tan(toRadians(mc.gameRenderer.getFov(camera, partialTick, true)) / 2)`, `Aspect = width / height`, `Seed` as the renderer's `doorSeed`, `Progress = LidlessPassageClientState.progress(partialTick)`.

- [ ] **Step 1: Extend the tests**

Add to `LidlessShaderContractTest`: `viewShaderImportsTheIncludeAndDeclaresItsUniforms` (json names `HemoTime, Progress, Length, Seed, CamRight, CamUp, CamForward, FovTan, Aspect, EyeHeight`, one sampler). Add to `LidlessClientWiringTest`: `ScreenOverlayEvents` contains `LidlessPassageView.renderGrade(`.

- [ ] **Step 2: Run to see them fail; write the shader and the view; wire the overlay**

Run: `./gradlew.bat test --tests "*Lidless*"`
Expected: FAIL, then PASS after writing.

- [ ] **Step 3: Run the full JVM guardrail set**

Run: `./gradlew.bat test`
Expected: PASS.

---

### Task 7: Review client, docs, alphaCheck

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/LidlessDoorClientReview.java`
- Modify: `gradle/review-clients.gradle` (row after `axonalReviewClient`: `lidlessReviewClient: [dir: 'build/lidless-client', user: 'LidlessReview', memory: ['2G', '5G'], props: ['hemomancy.lidlessReview': 'true']]`)
- Modify: `docs/HEMOMANCY_REFERENCE.md` (blocks section: one paragraph, creative-only curiosity, the config key), `docs/OPEN_QUESTIONS.md` (one open question: what the Lidless Door is for), `docs/README.md` only if it indexes reference sections

**Interfaces:**
- Produces: `LidlessDoorClientReview` (`Dist.CLIENT`, gated on `Boolean.getBoolean("hemomancy.lidlessReview")` and a game directory ending `lidless-client`), the Axonal review's structure: `setup(ServerPlayer)` builds a stone floor at y 100..101 from (-6..6, -6..6), clears air above, places the door at `(0,102,0)` facing NORTH, teleports the player to `(0.5, 102, 4.5)` yaw `180` (facing north toward the door) in survival; the tick script: tick 30 `capture("lidless-outside-front")`, tick 32 teleport to `(3.5, 102, 3.5)` yaw `225` and tick 40 `capture("lidless-outside-angle")`, tick 42 teleport back, ticks 50..70 `keyUp` down, tick 72 `capture("lidless-ahead-third")`, ticks 74..94 `keyUp`, tick 96 `capture("lidless-ahead-two-thirds")`, tick 98 `setYRot(0)` and tick 104 `capture("lidless-looking-back")`, tick 106 `setYRot(180)`, ticks 108..160 `keyUp`, tick 164 `capture("lidless-exited")` and a server log line with `isCrossing`, position and `noPhysics`, tick 180 `mc.stop()`. `capture` grabs the screenshot into the game dir as Axonal's does.

- [ ] **Step 1: Write the review client and its gradle row**

- [ ] **Step 2: Launch it and read the screenshots**

Run: `./gradlew.bat lidlessReviewClient`
Expected: the run reaches `mc.stop()` without a shader compile error in the log; six PNGs in `build/lidless-client/`. Read each with the Read tool: outside shots show a void panel inside iron jambs with at least one eye; ahead shots show a small then larger lit rectangle of world in a red-black void; the looking-back shot shows the entry rectangle; the exit log line shows `isCrossing=false` at `(0.5, 102, -0.5)`.

- [ ] **Step 3: Tune by eye**

Adjust the include's constants until the void reads as the Monolith family (deep black, red veins, visible blinking eyes) and re-run until it does; one or two rounds.

- [ ] **Step 4: Docs**

- [ ] **Step 5: Full gate**

Run: `./gradlew.bat alphaCheck`
Expected: PASS (JVM tests, alpha GameTests including `lidless_passage_validation`, resource checks).

# Monolith Walk Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** At Degree 7 the Sanguine Monolith cracks, splits and falls open instead of exploding; a diamond portal between the halves starts a Lidless crossing whose far end is a growing Qliphoth black hole with the Seed at its centre; reaching it takes the Seed and closes the portal.

**Architecture:** The Lidless Passage service gets a `LidlessPortal` contract and a `Mode` (`DOOR`, `SEED`); the door and the broken Monolith both implement the contract. The Monolith block gains a `BROKEN` state and its block entity the break clock, owner and seed flag. The client view draws the black hole, warp and a Seed mask target in `SEED` mode; the Monolith renderer draws cracks, halves, rubble, fog, lightning and the portal diamond.

**Tech Stack:** NeoForge 1.21.1 / Java 21, GLSL 150, GameTest, Gradle review clients.

**Spec:** `docs/superpowers/specs/2026-11-04-monolith-walk-design.md`

**Commits:** none; the owner's rule is work in the tree, commit only when asked.

## Global Constraints

- Same rules as the Lidless Door plan: NeoForge imports, `Hemomancy.rloc`, no `getPersistentData`, no `Component.literal`, no vanilla particles added to a file (`VanillaParticleRatchetTest` pins counts), nothing client under `common/`, payload changes bump `PROTOCOL_VERSION` (35 to 36) and rewrite `payload-registry.txt`, render types built once, shader includes read no uniform.
- The Lidless Door's behaviour is unchanged: the 20 tests in `lidless_passage_validation` stay green.
- The block id, block-entity id and item ids do not change (`FrozenIdentifierGameTests`).
- Lang: every new player-facing string is an `en_us.json` key; the two whisper lines are rewritten in the Fungal Whisper voice from `docs/NPC_VOICES.md`.

## Review Focus

1. A stranger standing in the gap of a broken Monolith must be stopped by it and never pulled into a crossing. Test in Task 2 (`stranger_is_solid_in_the_gap`).
2. The owner logging out mid-crossing must come back on the entry side with the Seed still inside and the portal still open. Test in Task 2 (`relog_mid_crossing_keeps_the_seed_inside`).
3. The husk broken while the owner is mid-crossing must drop the Seed on the ground and return the owner to the entry side. Test in Task 2 (`husk_broken_mid_crossing_drops_the_seed`).
4. A full inventory at completion must leave the Seed on the ground at the far side, never lost. Test in Task 2 (`full_inventory_drops_the_seed_at_the_exit`).
5. A broken Monolith surviving a save and reload must still know its owner and whether the Seed is inside. Test in Task 2 (`broken_monolith_survives_reload`).

---

### Task 1: The portal contract and the crossing mode

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/LidlessPortal.java`
- Modify: `common/block/shared/LidlessPassageService.java`, `common/block/shared/LidlessDoorBlock.java`, `common/network/lidless/LidlessPassageStatePacket.java`, `common/network/PacketHandler.java` (`PROTOCOL_VERSION = "36"`), `src/test/resources/network/payload-registry.txt` (`version 36`), `client/data/LidlessPassageClientState.java` (mode in `View`)
- Test: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/LidlessPassageGameTests.java`

**Interfaces:**
- Produces: `interface LidlessPortal { BlockPos anchor(); Direction facing(); boolean admits(ServerPlayer); boolean towardFront(ServerPlayer); LidlessPassageService.Mode mode(); void completed(ServerPlayer, boolean towardFront); }` with `anchor()` the lower cell, far cell `anchor().relative(towardFront ? facing : facing.getOpposite())` computed by the service as today. `completed` is called after the player lands, before the state sync.
- Produces: `enum LidlessPassageService.Mode { DOOR, SEED }`; `offer(ServerPlayer, LidlessPortal)` replaces `offer(ServerPlayer, BlockPos, BlockState)` (the door builds a `LidlessPortal` record from its lower state); `Session` keeps `portal`, `mode`; retreat speed in `SEED` mode is `speed * (1 - 0.75 * progress / length)` for `direction < 0`; completion in `SEED` mode always lands through the full recovery search (never holds); `progress(player)` and `isCrossing` unchanged. `LidlessPassageStatePacket` gains `Mode mode` after `towardFront` (written as a byte). `LidlessPassageClientState.View` gains `Mode mode()`.
- Produces: a test-only `StubPortal` in the GameTest class (anchor, facing, mode, a `completed` counter).

- [ ] **Step 1: Write the failing tests**

```java
public static void seed_mode_retreat_slows_toward_the_end(h) { enter a StubPortal(SEED) at DOOR; forward(60); double atEnd = progress(); back(1); double step = atEnd - progress(); assert step < WALK * 0.35 && step > WALK * 0.15; }
public static void seed_mode_completion_calls_the_portal_and_never_holds(h) { setBlock far upper cell STONE; enter StubPortal(SEED); forward(80); assert !crossing && stub.completed == 1 && player not inside the door column; }
public static void state_packet_carries_the_mode(h) { enter StubPortal(SEED); assert lastState.mode() == Mode.SEED; }
```
The stub needs a block for `entityInside`? No: tests call `LidlessPassageService.offer(player, stub)` directly, as the door tests do. The stub's `anchor()` is `DOOR` (the fixture's cleared cell, with no door placed; the service checks `portal.stillValid()` — add `boolean stillValid(ServerLevel)` to the contract, the door's checks `isLowerDoor` + facing, the stub returns true).

- [ ] **Step 2: Run to see them fail**

Run: `./gradlew.bat runLidlessPassageGameTestServer --offline`
Expected: compile failure (no `LidlessPortal`).

- [ ] **Step 3: Write `LidlessPortal`, refactor the service and the door, add the mode to the payload and client view; bump the protocol and rewrite the registry file**

The door's portal record: `admits` = `mayCross`, `towardFront` = the dot rule, `mode` = `DOOR`, `completed` = no-op, `stillValid` = `isLowerDoor` and facing match. `tick` replaces its door checks with `portal.stillValid(level)`.

- [ ] **Step 4: Run the holder and the payload tests**

Run: `./gradlew.bat runLidlessPassageGameTestServer --offline` then `./gradlew.bat test --tests "*PayloadProtocolVersionTest*" --tests "*Lidless*" --offline`
Expected: 23 GameTests pass; JVM tests pass.

---

### Task 2: The Monolith breaks, falls, and admits its owner

**Files:**
- Modify: `common/block/harbinger/functional/SanguineMonolithBlock.java`, `common/tile/harbinger/functional/SanguineMonolithBlockEntity.java`, `common/block/shared/FillerBlock.java` (a Monolith branch in `getCollisionShape` that asks the controller's `partCollision(state, level, pos, context)`), `src/main/resources/data/hemomancy/loot_table/blocks/sanguine_monolith.json` (a `block_state_property` condition `broken=false`), `src/main/resources/assets/hemomancy/lang/en_us.json` (the two whisper lines, one refusal line `hemomancy.monolith.already_breaking`), `src/main/resources/assets/hemomancy/blockstates/sanguine_monolith.json` (the `broken` property added to every variant key, same models)
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/playthrough/PlaythroughArchonGameTests.java` (`pressFurtherShatters`, `shatterBeforeCornerstone`)
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/MonolithWalkGameTests.java` (holder `monolith_walk_validation`), `src/gameTest/resources/data/monolith_walk_validation/structure/ductilis_arena.nbt` (copy), a row in `gradle/game-tests.gradle` (`alpha: true`, run `monolithWalkGameTestServer`, dir `build/monolith-walk-gametest`)

**Interfaces:**
- Produces on the block: `public static final BooleanProperty BROKEN`; `BREAK_TICKS = 240`, `CRACK_TICKS = 160`; `pressFurther` refuses when the entity is breaking or broken (message key `hemomancy.monolith.already_breaking`), else calls `entity.beginBreak(player)`; `getCollisionShape` and `partCollision`: whole or breaking Monolith = the slab; broken = empty for `entity.admits(player)`, the slab otherwise; `entityInside` (both cells, via the filler's controller) offers `entity.portal()` to the service; `playerWillDestroy` drops the Seed entity at the block when `broken && !seedTaken`.
- Produces on the block entity: `beginBreak(ServerPlayer)` (sets `breakStart`, `owner`), `isBreaking()`, `isBroken()`, `breakProgress(partialTick)` 0..1 over `BREAK_TICKS`, `crackProgress(partialTick)` 0..1 over `CRACK_TICKS`, `admits(Player)` = owner matches and `!seedTaken`, `seedInside()`, `takeSeed(ServerPlayer)` (inserts or drops the custom Seed entity at the player's feet, calls `LiberDiscoveryEvents.unlockForItem`, marks taken, syncs), `portal()` returning a `LidlessPortal` (`mode SEED`, `towardFront` by the dot rule on the slab's normal = `FACING`, `completed` = `takeSeed` then the whisper dialogue under the existing memo rule). The server tick: when breaking and `gameTime - breakStart >= BREAK_TICKS` and not yet broken: pop the Fragments at the base, set `BROKEN` on the block, send the shatter burst, sync. NBT keys `BreakStart`, `Owner`, `SeedTaken`.

- [ ] **Step 1: Write the GameTests**

Fixture: an Archon at D7 (`PlaythroughStates.archonD7` if reachable from the holder; else `HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(7)`) and a Monolith placed facing SOUTH at `(2,3,2)` with the filler above, via `BlockInit.sanguine_monolith` placement helper used by `PlaythroughSites.monolith`, player south of it.

```java
second_interaction_breaks_instead_of_dropping: pressFurther twice (through the dialogue context as the playthrough does, or call beginBreak directly after asserting pressFurther refuses a breaking stone); tick 240; assert BROKEN, fragments near, no seed item, block entity present.
stranger_is_solid_in_the_gap: broken stone; second player; collision at base non-empty; offer stranger -> not crossing.
owner_enters_in_seed_mode: owner collision empty; offer owner -> crossing, lastState.mode == SEED.
completion_takes_the_seed_and_closes: forward 80 -> !crossing, inventory has the seed, seedTaken, owner collision now the slab, player at far cell.
full_inventory_drops_the_seed_at_the_exit: fill inventory; forward 80; an EntityQliphothSeedItem within 2 blocks of the player.
blocked_far_cell_still_takes_the_seed: stone in far cells; forward 80; seed taken; player standing clear somewhere.
husk_broken_mid_crossing_drops_the_seed: forward 10; destroyBlock(stone); tick; !crossing; seed entity near stone; player at entry cell.
relog_mid_crossing_keeps_the_seed_inside: forward 10; logout; !crossing; at entry cell; seedInside; owner collision still empty.
broken_monolith_survives_reload: saveAdditional into a tag, new entity loadAdditional; owner and seed flag equal.
husk_after_seed_drops_nothing: complete; destroyBlock; no monolith item, no seed.
```

- [ ] **Step 2: Run to see them fail** (`./gradlew.bat runMonolithWalkGameTestServer --offline`, compile failure expected)

- [ ] **Step 3: Implement the block, block entity, filler branch, loot condition, lang, blockstate**

- [ ] **Step 4: Rewrite the two playthrough tests** to assert the break (no drop at the second press; after 240 ticks fragments but no seed; the Seed arrives through the crossing, which the playthrough reaches by calling the service's `offer` with the entity's portal and ticking 80 times).

- [ ] **Step 5: Run the holder, the playthrough tests and the JVM suite**

Run: `./gradlew.bat runMonolithWalkGameTestServer --offline`, `./gradlew.bat runPlaythroughGameTestServer --offline`, `./gradlew.bat test --offline`
Expected: all pass (`HardCodedEnglish`, `EnglishLiteralAllowList`, `FrozenIdentifier`, `VanillaParticleRatchet` included).

---

### Task 3: The crossing view in SEED mode and the Seed mask

**Files:**
- Modify: `client/screen/overlay/LidlessPassageView.java` (mode uniforms, `focus` target, `renderFocus(RenderLevelStageEvent)`), `client/render/world/WorldEffectDispatcher.java` (call `LidlessPassageView.renderFocus(event)` beside `captureProjection`), `shaders/core/screen/lidless_passage_view.fsh` + `.json` (uniforms `Mode`, `Hole` (vec2: apparent radius, warp), sampler `Sampler1` for the focus target), `client/render/shader/ShaderInit.java` (uniform names), `client/event/LidlessPassageClientEvents.java` (unchanged unless the drone pitch is lowered in SEED mode)
- Test: `src/test/java/com/vincenthuto/hemomancy/architecture/LidlessShaderContractTest.java` (new uniforms and the second sampler), `LidlessClientWiringTest.java` (`renderFocus` called from the dispatcher)

**Interfaces:**
- Shader: `Mode` 0 door, 1 seed. In seed mode: no ahead window; the black hole on the passage axis: angle from the axis `a = acos(ray.z)`, apparent radius `Hole.x` (Java: `0.05 + 0.75 * smoothstep(progress / length)` radians), event horizon `smoothstep(Hole.x, Hole.x * 0.92, a)` black, accretion ring `Hole.x .. Hole.x * 1.35` with the Monolith red and a swirl from the void's `hash21`/time, lensing: rays within `3 * Hole.x` of the axis are pulled toward it by `Hole.y * (Hole.x / a)^2` before the void is sampled (so the void streams into the hole); eyes thinned to a quarter within `2 * Hole.x`. Vision warp: screen `ndc` gets a radial pincushion `ndc *= 1 + Hole.y * 0.35 * length(ndc)^2` plus a slow rotational wobble `0.02 * Hole.y * sin(time * 0.1)`. `Hole.y` = `smoothstep(0.2, 1, progress / length)`. Finally `focus = texture(Sampler1, uv)`, `color = mix(color, focus.rgb, focus.a)`.
- `renderFocus`: in `SEED` mode at `AFTER_LEVEL`, bind a transparent `focus` target the size of the main target, draw the Seed item with `Minecraft.getItemRenderer().renderStatic(new ItemStack(ItemInit.qliphoth_seed), ItemDisplayContext.FIXED, 0xF000F0, NO_OVERLAY, pose, bufferSource, level, 0)` at `camera + passageForward * min(length - progress, 6) + up * 0.1`, scaled `1.6 + 0.6 * sin(time * 0.15)` and spun slowly, after `QliphothSeedTendrilEffects.spawnAt(key, origin, gameTime, partialTick)` has seeded tendrils there and `TendrilRenderer.render(partialTick, pose, bufferSource)` draws them into the same target; end the batch and rebind the main target.

- [ ] **Step 1: Extend the contract and wiring tests, run them to see them fail**
- [ ] **Step 2: Write the shader, the Java view changes and the dispatcher call**
- [ ] **Step 3: Run `./gradlew.bat test --tests "*Lidless*" --tests "*ShaderNoiseIncludeGuardrailTest*" --tests "*HudAndWorldDrawingGuardrailTest*" --offline`** → PASS (GPU compile in Task 5)

---

### Task 4: The Monolith renderer: cracks, halves, rubble, fog, lightning, portal

**Files:**
- Modify: `client/render/tile/harbinger/functional/SanguineMonolithRenderer.java`, `client/model/tile/functional/SanguineMonolithModel.java` (a second layer `createHalvesLayer()` with parts `left` and `right`, each `8 x 32 x 8`, pivots at the base outer corners), `client/registry/ModelRegistrar.java` or wherever the Monolith layer is registered (register the halves layer), `shaders/core/world/lidless_door_panel.fsh` + `.json` (uniform `Diamond` 0/1: when 1 the void is masked to a diamond in `texCoord0` with a 0.06 feather, alpha 0 outside), `client/render/HemoRenderTypes.java` (`lidlessDoorPanel` gains `float diamond`), `client/render/tile/shared/LidlessDoorRenderer.java` (passes 0)
- Test: `LidlessShaderContractTest` (the `Diamond` uniform)

**Interfaces:**
- Renderer by state: whole and `crackProgress == 0` → today's render. Breaking → today's slab plus cracks: a fixed set of 9 jagged polylines in slab space (from the base centre upward, branching), each drawn as a thin radiant quad strip (the renderer's existing radiant render type) whose visible length is `crackProgress` eased; glow brightens with progress. `breakProgress` in `[CRACK_TICKS/BREAK_TICKS, 1]` → the halves layer, each half rotated about its base outer edge by `45 * easeOutBack(fallProgress)` degrees outward (sideways along the slab's width), drawn with `monolithEntitySurface`; the rubble (12 shard facets from the shatter renderer's shard look, static, seeded by position) in a ring of radius 0.9 at the base; the fog disc (the rite fog material via a new public `CardinalRiteFogRenderer.drawStill(pose, buffer, center, radius, time, opacity, camera)` extracted from `drawRiteFog`) radius 3, opacity 0.7 while the Seed is inside, 0.35 after; lightning: every 60 to 120 ticks (seeded) `CardinalRiteFogLightning.strike(fulcrum, fulcrum + random up-and-out 2.5 blocks, seed, partialTick)` while the Seed is inside, every 200 to 300 after; the portal: while `seedInside`, a diamond quad between the halves (top at the slab top, bottom at the fulcrum, sides at the halves' inner faces at mid height) drawn front and back with `lidlessDoorPanel(..., diamond = 1)` and the passage basis from `FACING`, skipped for the local player mid-crossing (as the door does).

- [ ] **Step 1: Extend the contract test for `Diamond`, run it to see it fail**
- [ ] **Step 2: Write the model layer, the renderer, the fog draw extraction, the panel mask**
- [ ] **Step 3: Run `./gradlew.bat test --tests "*Lidless*" --tests "*RenderTypeCachingGuardrailTest*" --tests "*VanillaParticleRatchetTest*" --tests "*AnimationTimeGuardrailTest*" --offline`** → PASS

---

### Task 5: Review client, docs, gate

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/MonolithWalkClientReview.java`; row `monolithWalkReviewClient: [dir: 'build/monolith-walk-client', user: 'MonolithReview', memory: ['2G', '5G'], size: review, props: ['hemomancy.monolithWalkReview': 'true']]`
- Modify: `docs/HEMOMANCY_REFERENCE.md` (§5.9 Qliphoth Communion: the shatter paragraph and the Seed row), `docs/LORE_REFERENCE.md` §6.5a (one paragraph: the vessel cracks and falls open; the Seed is taken from the void), `docs/ENDGAME_PATHS.md` if it describes the shatter, `docs/OPEN_QUESTIONS.md` (D-24 answered: the door's first use is the Monolith walk)

**Interfaces:**
- The review script (the Lidless review's structure, `AccessibilityOnboardingScreen` skip, operator-driven save `MonolithWalkReview`): setup places a Monolith facing SOUTH at `(0,102,0)` with the player as a D7 Archon standing south; captures `monolith-whole`, calls `beginBreak` through the server, captures `monolith-cracks` at tick 100 and `monolith-fallen` at tick 260 from the front and at 300 from `(3.5,102,3.5)` yaw 135; walks in; captures `monolith-walk-third`, `monolith-walk-two-thirds`, `monolith-walk-near` (progress ~14); captures `monolith-exit` with the Seed in hand and the dormant husk behind; logs crossing, progress, seedTaken.

- [ ] **Step 1: Write the review client and row; launch `./gradlew.bat runMonolithWalkReviewClient --offline`** driven by one `{"op":"create","name":"MonolithWalkReview","seed":11}` after startup; read every capture; tune cracks, fall, fog, hole growth and warp by eye, one or two rounds.
- [ ] **Step 2: Docs**
- [ ] **Step 3: `./gradlew.bat alphaCheck --offline`** → PASS

# Claw Slash Ribbon Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a real client-rendered three-line claw slash visual and wire it into Gloam Laceration.

**Architecture:** Add a client-side world renderer that owns slash instances, plus a NeoForge 1.21 payload that spawns those instances from server logic. Server manipulation code calls a `PacketHandler` helper rather than importing renderer classes.

**Tech Stack:** Java 21, NeoForge 1.21.1 payload networking, `RenderLevelStageEvent`, `MultiBufferSource`, `RenderTypeInit`, source-level JUnit-style tests.

---

### Task 1: Source Test

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/client/render/world/ClawSlashRendererSourceTest.java`

- [ ] **Step 1: Write the failing source test**

Create a test that reads renderer, packet, client event, packet handler, and Gloam source files, then asserts the new symbols and wiring exist.

- [ ] **Step 2: Verify red**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.client.render.world.ClawSlashRendererSourceTest`

Expected: fails because `ClawSlashRenderer.java` and `SpawnClawSlashPacket.java` do not exist.

### Task 2: Renderer And Render Type

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/ClawSlashRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/RenderTypeInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`

- [ ] **Step 1: Add render types**

Add `CLAW_SLASH_GLOW` and `CLAW_SLASH_CORE` as translucent `POSITION_COLOR` quad render types.

- [ ] **Step 2: Add renderer**

Implement `ClawSlashRenderer.spawn`, `tick`, and `render`. Render three parallel tapered curved ribbons with a glow pass and a core pass.

- [ ] **Step 3: Hook client lifecycle**

Call `ClawSlashRenderer.tick()` from `ClientEvents.onClientTickPost` and `ClawSlashRenderer.render(...)` during `AFTER_TRANSLUCENT_BLOCKS`.

### Task 3: Payload And Server Helper

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/network/particle/SpawnClawSlashPacket.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java`

- [ ] **Step 1: Add payload**

Create a `CustomPacketPayload` with `TYPE`, `STREAM_CODEC`, encode/decode, and a handler that enqueues `ClawSlashRenderer.spawn(...)`.

- [ ] **Step 2: Register and send**

Register the payload with `playToClient` and add `PacketHandler.sendClawSlash(Vec3 pos, Vec3 forward, ParticleColor color, boolean ambush, float scale, double radius, ServerLevel level)`.

### Task 4: Manipulation Wiring

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/GloamLacerationManip.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityManipulationEffects.java`

- [ ] **Step 1: Wire player cast**

Replace Gloam's glow-dot slash trail with `PacketHandler.sendClawSlash`, using the player's look vector, Gloam palette, ambush flag, and a scale around `1.0F`.

- [ ] **Step 2: Wire entity cast**

Replace entity/Will-side Gloam glow burst with `PacketHandler.sendClawSlash`, using the caster aim vector and a slightly smaller scale.

### Task 5: Verification

**Files:**
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/ClawSlashRendererSourceTest.java`

- [ ] **Step 1: Verify green**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.client.render.world.ClawSlashRendererSourceTest`

Expected: pass.

- [ ] **Step 2: Verify compile and suite**

Run: `./gradlew.bat compileJava`, `./gradlew.bat test`, and `./gradlew.bat build`.

Expected: all pass.

# Silent Archon Monolith Player Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render a full Silent Archon wearer's base player body as the existing monolith entity surface, with transparent armor remaining on top.

**Architecture:** Add a small client render helper for full-set checks and monolith render type construction, then inject into `LivingEntityRenderer#getRenderType` through the existing client mixin system with an `AbstractClientPlayer` guard. Keep the Silent Archon armor overlay layer unchanged so render ordering stays vanilla-compatible.

**Tech Stack:** NeoForge 1.21.1, Minecraft client rendering, Sponge Mixin, existing `HemoRenderTypes.monolithEntitySurface`.

---

### Task 1: Source Contract Test

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/client/render/SilentArchonPlayerCoreRenderSourceTest.java`

- [x] Write a source test that asserts the mixin json includes `MixinLivingEntityRenderer`, the mixin targets `LivingEntityRenderer#getRenderType`, the helper checks all four Silent Archon armor slots, and the render type uses `HemoRenderTypes.monolithEntitySurface`.
- [x] Run `./gradlew.bat testClasses` and the direct test class; expect failure before implementation because the helper and mixin do not exist.

### Task 2: Render Helper

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/armor/SilentArchonPlayerRenderHelper.java`

- [x] Add `hasFullSilentArchonSet(LivingEntity entity)` using exact `ItemInit.silent_archon_*` slot checks.
- [x] Add `playerCoreRenderType(AbstractClientPlayer player)` returning `HemoRenderTypes.monolithEntitySurface(...)` with a stable UUID-derived seed and the existing Silent Archon armor time helper.

### Task 3: Living Entity Renderer Mixin

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinLivingEntityRenderer.java`
- Modify: `src/main/resources/hemomancy.mixins.json`

- [x] Inject into `LivingEntityRenderer#getRenderType(LivingEntity, boolean, boolean, boolean)`.
- [x] If the entity is an `AbstractClientPlayer`, the body is visible, not using the translucent vanilla path, and the full Silent Archon set is equipped, return the helper's monolith render type.
- [x] Register `MixinLivingEntityRenderer` in the client mixin list.

### Task 4: Docs And Verification

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [x] Document that Silent Archon full-set visuals replace the base player body render type with a monolith core beneath the translucent armor.
- [ ] Run the focused direct test and `./gradlew.bat build`.

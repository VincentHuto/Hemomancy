# Specimen Jar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Hemomancy-only Specimen Jar block/item that captures mod arthropods, displays them while placed or held, preserves captured entity data on pickup, and releases the exact entity when broken.

**Architecture:** Server-side capture and release behavior lives in focused common classes: `SpecimenJarItem`, `SpecimenJarBlock`, `SpecimenJarBlockEntity`, and `SpecimenJarData`. Rendering is isolated to client model/renderer/item renderer classes and reuses Minecraft's `EntityRenderDispatcher` for contained mobs. Capturability is data-driven through `hemomancy:specimen_jar_capturable`.

**Tech Stack:** NeoForge 1.21.1, Java 21, Minecraft NBT/entity save APIs, Hemomancy DeferredRegister patterns, BlockEntityRenderer, BlockEntityWithoutLevelRenderer, JSON resource data.

---

## File Structure

- Create `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/SpecimenJarBlockItem.java`: capture interaction and custom item renderer hook.
- Create `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenJarBlock.java`: placement, shift-right-click pickup, break-to-release.
- Create `src/main/java/com/vincenthuto/hemomancy/common/tile/functional/SpecimenJarBlockEntity.java`: persisted specimen NBT and client sync.
- Create `src/main/java/com/vincenthuto/hemomancy/common/util/SpecimenJarData.java`: shared NBT keys, stack/block entity helpers, release helper, display name helper.
- Create `src/main/java/com/vincenthuto/hemomancy/client/model/tile/functional/SpecimenJarModel.java`: compact jar geometry.
- Create `src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/SpecimenJarRenderer.java`: jar and contained entity rendering.
- Create `src/main/java/com/vincenthuto/hemomancy/client/render/item/tile/functional/SpecimenJarItemRenderer.java`: item rendering.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`: register block and custom block item.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java`: register block entity.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`: add `SPECIMEN_JAR_CAPTURABLE` tag key.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`: register block entity renderer.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`: register jar model layer.
- Add JSON resources for blockstate, model, recipe, loot table, item model, entity tag, and lang.
- Modify `HEMOMANCY_REFERENCE.md`: document feature and current capturable scope.

---

### Task 1: Server Data Helper

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/util/SpecimenJarData.java`

- [ ] **Step 1: Write the helper with pure NBT operations**

Create methods for `hasSpecimen(ItemStack)`, `getSpecimen(ItemStack)`, `setSpecimen(ItemStack, CompoundTag)`, `clearSpecimen(ItemStack)`, `copySpecimenToStack(ItemStack, CompoundTag)`, `captureEntity(LivingEntity)`, `releaseSpecimen(ServerLevel, BlockPos, CompoundTag)`, and `getSpecimenName(CompoundTag)`.

- [ ] **Step 2: Compile-check the helper**

Run: `./gradlew compileJava`

Expected before call-site wiring: compile may fail only because the helper has no callers; type/API errors in the helper must be fixed before continuing.

### Task 2: Block Entity Persistence

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/functional/SpecimenJarBlockEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java`

- [ ] **Step 1: Implement block entity storage**

Store one `CompoundTag specimen`, expose `hasSpecimen()`, `getSpecimenCopy()`, `setSpecimen(CompoundTag)`, `clearSpecimen()`, and sync via `getUpdateTag()` plus `getUpdatePacket()`.

- [ ] **Step 2: Register block entity**

Add `specimen_jar` to `BlockEntityInit.TILES` for `BlockInit.specimen_jar`.

- [ ] **Step 3: Compile-check**

Run: `./gradlew compileJava`

Expected: no errors from block entity registration.

### Task 3: Block And Item Behavior

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenJarBlock.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/SpecimenJarBlockItem.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`

- [ ] **Step 1: Add capturable entity tag key**

Add `public static final TagKey<EntityType<?>> SPECIMEN_JAR_CAPTURABLE = createTag("specimen_jar_capturable");`.

- [ ] **Step 2: Implement block**

Use `BaseEntityBlock`, `RenderShape.ENTITYBLOCK_ANIMATED`, transparent/no-occlusion shapes, `setPlacedBy` to move specimen NBT from stack to block entity, shift-right-click pickup that removes the block and gives the preserving stack, and `playerWillDestroy`/`onRemove` release behavior guarded against pickup duplication.

- [ ] **Step 3: Implement item capture**

Override `interactLivingEntity`; if stack is empty of specimen and target type is in the capturable tag, save target NBT, discard target, and replace/shrink/give a filled jar stack. Return `InteractionResult.sidedSuccess`.

- [ ] **Step 4: Register custom block item**

Register `specimen_jar` in `BlockInit.MODELEDBLOCKS` and route it through `SpecimenJarBlockItem` in `createItemBlock`.

- [ ] **Step 5: Compile-check**

Run: `./gradlew compileJava`

Expected: capture and placement classes compile.

### Task 4: Client Rendering

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/tile/functional/SpecimenJarModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/SpecimenJarRenderer.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/item/tile/functional/SpecimenJarItemRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`

- [ ] **Step 1: Implement model**

Create a simple Blockbench-style Java model with hematic iron base/rim and translucent jar body.

- [ ] **Step 2: Implement renderer**

Render jar model, then create/cache a client-side entity from stored specimen NBT and render it centered at `(0.5, 0.45, 0.5)`, scaled by max bounding-box dimension.

- [ ] **Step 3: Implement item renderer**

Render the same model and specimen from stack NBT in GUI, ground, fixed, and hand contexts using display-specific transforms.

- [ ] **Step 4: Register client pieces**

Register the model layer in `LayerEvents` and the block entity renderer in `ClientEvents`.

- [ ] **Step 5: Compile-check**

Run: `./gradlew compileJava`

Expected: client classes compile.

### Task 5: Resources And Docs

**Files:**
- Add: `src/main/resources/assets/hemomancy/blockstates/specimen_jar.json`
- Add: `src/main/resources/assets/hemomancy/models/block/specimen_jar.json`
- Add: `src/main/resources/assets/hemomancy/models/item/specimen_jar.json`
- Add: `src/main/resources/data/hemomancy/tags/entity_types/specimen_jar_capturable.json`
- Add: `src/main/resources/data/hemomancy/recipe/specimen_jar.json`
- Add: `src/main/resources/data/hemomancy/loot_table/blocks/specimen_jar.json`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify: `HEMOMANCY_REFERENCE.md`

- [ ] **Step 1: Add resources**

Use existing hematic iron and vivianite glass textures. Recipe uses Vivianite Glass and Hematic Iron around a hollow center.

- [ ] **Step 2: Add docs**

Add the Specimen Jar to the block/tile section and WIP/resolved notes if appropriate.

- [ ] **Step 3: Full verification**

Run: `./gradlew build`

Expected: build exits 0.

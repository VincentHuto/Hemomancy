# HutosLib 1.21.1 Porting Issues/Fixes Guide

## Current state (important)
- HutosLib now **build-targets 1.21.1** and most NeoForge migration work is in place.
- Per latest commit notes, **arm banner crafting + banner pattern item behavior is still not fully working** and should be treated as open follow-up.

## Major issues that were fixed during the port

### 1) Build system/toolchain breakage
- Migrated build setup to NeoForge ModDev plugin and modern MDK layout.
- Moved to Java 21 + updated Gradle/toolchain settings.
- Switched from `mods.toml` resource to generated `neoforge.mods.toml` template flow.
- Key references:
  - `build.gradle`
  - `gradle.properties`
  - `src/main/templates/META-INF/neoforge.mods.toml`

### 2) Package/API namespace migration (`net.minecraftforge` -> `net.neoforged`)
- Broad import and API surface migration to NeoForge packages.
- Event bus usage updated to NeoForge equivalents.
- Key reference: `src/main/java/com/vincenthuto/hutoslib/HutosLib.java`

### 3) Mod initialization/event bus setup changes
- Main mod class now takes `IEventBus` in constructor and registers content through that bus.
- Removed legacy patterns like `FMLJavaModLoadingContext.get()` and old DistExecutor style.
- Key reference: `src/main/java/com/vincenthuto/hutoslib/HutosLib.java`

### 4) Registry model changes
- Standardized DeferredRegister usage with `DeferredHolder` in registries.
- Updated registration flows for items/blocks/menu/serializers/attachments.
- Key references:
  - `src/main/java/com/vincenthuto/hutoslib/common/registry/HLItemInit.java`
  - `src/main/java/com/vincenthuto/hutoslib/common/container/HlContainerInit.java`
  - `src/main/java/com/vincenthuto/hutoslib/common/registry/HLAttachmentTypes.java`

### 5) Capabilities -> Attachment system migration
- Reworked persistent player data patterns to NeoForge attachments.
- Example attachment types now include karma and banner slot data.
- Key reference: `src/main/java/com/vincenthuto/hutoslib/common/registry/HLAttachmentTypes.java`

### 6) Networking rewrite (`SimpleChannel` -> payload handlers)
- Migrated packets to `CustomPacketPayload` registration via `RegisterPayloadHandlersEvent`.
- Switched sending patterns to modern `PacketDistributor` helpers.
- Key reference: `src/main/java/com/vincenthuto/hutoslib/common/network/HLPacketHandler.java`

### 7) Datagen API churn (recipes + loot)
- Recipe provider now uses `RecipeOutput`.
- Loot provider migrated to `BlockLootSubProvider`.
- Datagen wiring updated under GatherDataEvent.
- Key references:
  - `src/main/java/com/vincenthuto/hutoslib/common/data/HLRecipeProvider.java`
  - `src/main/java/com/vincenthuto/hutoslib/common/data/HLBlockLootTableProvider.java`
  - `src/main/java/com/vincenthuto/hutoslib/common/data/HLDataGeneration.java`

### 8) Data/resource format drift
- Worked through tag/type namespace drift (`forge` -> `neoforge`) and 1.21.x data expectations.
- Loot JSON path drift was identified in design notes (`loot_tables` vs modern `loot_table`) and handled by code-first datagen.
- Key references:
  - `src/main/resources/data/hutoslib/recipes/*`
  - `docs/superpowers/specs/2026-04-21-neoforge-1211-migration-design.md`

### 9) Banner + item data component migration
- Banner behavior migrated to data components (`DataComponents.BASE_COLOR`, `DataComponents.BANNER_PATTERNS`).
- Custom arm-banner crafting recipe logic adapted to the new component-based path.
- Key references:
  - `src/main/java/com/vincenthuto/hutoslib/common/recipe/ArmBannerCraftRecipe.java`
  - `src/main/java/com/vincenthuto/hutoslib/common/item/ItemArmBanner.java`

### 10) Event subscriber annotation cleanup
- Removed empty/incorrect event subscriber usage in common events helper to avoid invalid subscriber patterns.
- Key reference: `src/main/java/com/vincenthuto/hutoslib/common/event/HLCommonEvents.java`

## Open issues / follow-up hotspots (do this first when porting another mod)
- **Banner pattern item behavior**: verify pattern registration/tag binding end-to-end.
- **Arm banner crafting behavior**: confirm recipe registration + runtime matching in crafting table.
- **Datagen parity**: regenerate and verify recipes/tags/loot outputs against runtime behavior.
- **Resource path sanity**: check 1.21.x path/name expectations before debugging gameplay symptoms.

## Reusable checklist for porting other mods (ex: Hemomancy)
1. Update build stack first (plugin, Java, Gradle, mod metadata template).
2. Do namespace migration second (`net.minecraftforge` -> `net.neoforged`) before logic rewrites.
3. Port registries/events/bootstrap lifecycle.
4. Port capabilities/networking to attachments + payload handlers.
5. Port datagen providers and re-run data generation.
6. Validate custom recipes and data components early (especially any NBT-heavy systems).
7. Launch-game smoke test and verify in-game behavior for feature-critical content (crafting, patterns, containers, sync).
8. Only then do cleanup/refactor passes.

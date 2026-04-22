# NeoForge 1.21.1 Port — Remaining Work

**Branch:** `copilot/analyze-hemomancy-porting-state`
**Target:** NeoForge 21.1.172 / Minecraft 1.21.1
**Last updated:** 2026-04-22

> This document is the handoff guide for the next agent/session.  
> Each section lists the exact files and changes needed. Items are ordered from
> most-blocking (would prevent compilation) to least-blocking (runtime/polish).

---

## What Has Already Been Done

| Step | Summary |
|------|---------|
| 1a | Capability system: IBloodVolume, IBloodTendency, etc. migrated to NeoForge attachment types. `HemoCapabilityAccess` updated. |
| 1b | Network API: `SimpleChannel` → `RegisterPayloadsEvent`; `AttributeModifier` UUID → `ResourceLocation` keys; `ForgeHooksClient` removed. |
| 2 | `PotionUtils` → `PotionContents`; old `Tesselator`/buffer API → new fluent API; `ParticleRenderType` overrides removed. |
| 3 | 25 biome-modifier JSON files updated: `forge:` namespace → `neoforge:`. |
| 4 | Compat isolation: `compat/mna/**`, `compat/jei/**`, `compat/curios/**` excluded from compilation in `build.gradle`; `Hemomancy.java` cleaned; `EngramBlock`, `CrimsonFlameBlock`, `MixinHUDOverlayRenderer`, `CuriosPlugin` de-contaminated from MnA imports. |

---

## Remaining Issues

### 5 — `ItemStack` NBT API deprecations (medium priority — compile warnings / runtime errors)

In NeoForge 1.21.1, `ItemStack.getOrCreateTag()`, `ItemStack.setTag()`, and `ItemStack.getTag()` are
**deprecated** and replaced by the DataComponents API (`stack.set(DataComponents.CUSTOM_DATA, ...)` /
`stack.get(DataComponents.CUSTOM_DATA)`).  The following files still use the old API:

| File | Lines | Pattern |
|------|-------|---------|
| `common/tile/crafting/MorphlingIncubatorBlockEntity.java` | 349 | `result.getOrCreateTag()` |
| `common/network/keybind/ToggleGourdKeyPacket.java` | 44, 55 | `getOrCreateTag()`, `setTag()` |
| `common/entity/projectile/BloodBoltEntity.java` | 127 | `itemstack.getOrCreateTag().putInt(...)` |
| `common/item/morphlings/MorphlingItem.java` | 176 | `getOrCreateTag()` |
| `common/item/morphlings/SerpentMorphlingItem.java` | 71, 97, 130 | `getOrCreateTag()` |
| `common/item/bloodline/UnsignedLedgerItem.java` | 68, 83, 134 | `getOrCreateTag()`, `setTag()` |
| `common/item/tool/StructureScannerItem.java` | 83 | `getOrCreateTag()` |
| `common/item/tool/living/SanguisLanceaItem.java` | 129, 137 | `getOrCreateTag()`, `setTag()` |
| `common/item/tool/living/LivingCrossbowItem.java` | 56, 312 | `getOrCreateTag()` |
| `common/item/tool/living/LivingBladeItem.java` | 57, 95, 103 | `getOrCreateTag()`, `setTag()` |
| `common/item/tool/living/LivingSpearItem.java` | 171, 179 | `getOrCreateTag()`, `setTag()` |
| `common/item/tool/living/LivingStaffItem.java` | 91 | `getOrCreateTag()` |
| `common/item/tool/living/LivingSyringeItem.java` | 119, 121, 178, 182, 183 | `getOrCreateTag()` |
| `client/render/layer/player/BloodGourdLayer.java` | 67 | `stack.hasTag()`, `stack.getTag()` |
| `client/render/item/hematic/LivingBladeItemRenderer.java` | 56 | `stack.getTag()` |
| `client/render/item/BloodGourdItemRenderer.java` | 67 | `stack.hasTag()`, `stack.getTag()` |
| `common/item/ConsecratedSyringeItem.java` | 24 | `stack.getTag()` |
| `common/item/morphlings/MorphlingItem.java` | 64 | `stack.hasTag()` |

**Replacement pattern:**
```java
// Old
CompoundTag tag = stack.getOrCreateTag();
tag.putBoolean("state", true);
stack.setTag(tag);

// New (NeoForge 1.21.1)
CompoundTag tag = stack.getOrCreate(DataComponents.CUSTOM_DATA).copyTag();
tag.putBoolean("state", true);
stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
```
Simple boolean state (like living-tool toggle) can instead use a dedicated boolean
DataComponent — cleaner long-term but not strictly required for initial port.

---

### 6 — `ForgeRegistries` data-access calls (low-to-medium priority — compile warnings, some runtime errors)

`net.neoforged.neoforge.registries.ForgeRegistries` still exists in NeoForge 1.21 for most use
cases but `.tags()` can be `null` before world load, and `.getCodec()` was removed.
Direct replacements:

| File | Pattern | Replacement |
|------|---------|-------------|
| `client/event/ClientEvents.java:543` | `ForgeRegistries.ITEMS.getKey(...)` | `BuiltInRegistries.ITEM.getKey(...)` |
| `client/screen/tile/crafting/scar/ScreenScarBinderViewer.java:406` | `ForgeRegistries.ITEMS.getKey(...)` | `BuiltInRegistries.ITEM.getKey(...)` |
| `client/screen/tile/functional/MnemonicReliquaryScreen.java:83` | `ForgeRegistries.ITEMS.getValues()` | `BuiltInRegistries.ITEM` (iterable) |
| `client/screen/skilltree/harbinger/ManipulationsTabController.java:175` | `for (Item item : ForgeRegistries.ITEMS)` | `for (Item item : BuiltInRegistries.ITEM)` |
| `common/worldgen/village/VillageEvents.java:101` | `ForgeRegistries.ITEMS.tags()` | Use `level.registryAccess()` or `TagManager`; see note below |
| `common/capability/player/scar/VeinMinerHelper.java:162-163` | `ForgeRegistries.BLOCKS.tags().getTag(...)` | `level.registryAccess()` block tag lookup, or pass `HolderGetter<Block>` |
| `common/loot/modifier/AddItemModifier.java:23` | `ForgeRegistries.ITEMS.getCodec()` | `BuiltInRegistries.ITEM.byNameCodec()` |
| `common/item/tool/StructureScannerItem.java:239,255,300` | `ForgeRegistries.BLOCKS.getKey(...)` | `BuiltInRegistries.BLOCK.getKey(...)` |
| `common/item/tool/living/LivingSyringeItem.java:120` | `ForgeRegistries.ENTITY_TYPES.getKey(...)` | `BuiltInRegistries.ENTITY_TYPE.getKey(...)` |
| `common/item/armor/ChitiniteShieldItem.java:96` | `ForgeRegistries.ITEMS.tags().getTag(ItemTags.PLANKS)` | `repair.is(ItemTags.PLANKS)` (direct tag check) |
| `common/item/armor/BarbedShieldItem.java:114` | same | same |
| `common/item/BloodVialItem.java:34,75` | `ForgeRegistries.ENTITY_TYPES.getValue/getKey` | `BuiltInRegistries.ENTITY_TYPE` equivalents |
| `common/data/gen/HemoEntityTagProvider.java` | `ForgeRegistries.ENTITY_TYPES.getKey(...)` | `EntityInit.*.getId()` or `BuiltInRegistries.ENTITY_TYPE.getKey(...)` |
| `common/data/gen/HemoItemModelProvider.java` | `ForgeRegistries.ITEMS.getKey(...)` | `BuiltInRegistries.ITEM.getKey(...)` |
| `common/data/gen/HemoBlockStateProvider.java:143` | `ForgeRegistries.BLOCKS.getKey(...)` | `BuiltInRegistries.BLOCK.getKey(...)` |
| `common/recipe/serializer/*.java` | `ForgeRegistries.ITEMS/BLOCKS.getValue(...)` | `BuiltInRegistries.ITEM/BLOCK.get(rl)` |

> **Note on tag lookups (VillageEvents, VeinMinerHelper):** In NeoForge 1.21.1 you cannot call
> `ForgeRegistries.ITEMS.tags()` safely at early init time. Pass a `Level` or
> `RegistryAccess` into the method and use `level.registryAccess().registryOrThrow(Registries.ITEM)`.
> For `VeinMinerHelper.isOre()`, the simplest fix is:
> ```java
> return b.defaultBlockState().is(BlockTags.STONE_ORE_REPLACEABLES)
>     || b.defaultBlockState().is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
> ```

---

### 7 — `PacketHandler.sendToPlayersNear` — null `ServerLevel` argument (runtime crash)

**File:** `common/network/PacketHandler.java`, lines 231, 238, 244, 250, 255

The NeoForge 1.21.1 signature for `PacketDistributor.sendToPlayersNear` requires a non-null
`ServerLevel` as the first argument. All five helper methods pass `null`:

```java
// Current (broken):
PacketDistributor.sendToPlayersNear(null, null, pos.x, pos.y, pos.z, radius, packet);

// Fix: thread the level through or fetch from MinecraftServer
```

The callers of these helpers are in:
- `common/manipulation/` (various manip effects) — they all have access to `Level`; cast to `ServerLevel`
- `common/entity/` projectiles — same

Change the helper signatures to accept `ServerLevel level` and propagate to callers.  
Example fix for `sendAvatarHitParticles`:
```java
public static void sendAvatarHitParticles(Vec3 pos, ParticleColor color, double radius,
        ServerLevel level) {
    PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
            new SpawnAvatarParticlesPacket(pos, color));
}
```
The `ResourceKey<Level> dimension` parameter that was previously threaded through can be removed —
NeoForge 1.21 infers it from the `ServerLevel`.

---

### 8 — `EffectInit` — Brewing recipe stub (low priority — potions simply won't brew)

**File:** `common/init/EffectInit.java`, line 202

```java
@SubscribeEvent
public static void setupPotionRecipes(final FMLCommonSetupEvent event) {
    // TODO: update to 1.21 BrewingRecipeRegistry API
}
```

NeoForge 1.21.1 uses `net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry.addRecipe(...)`.
Add the missing brewing mixes for any hemomancy potions that should be craftable in a
brewing stand.  If none are needed right now, the empty method is harmless — just remove
the TODO comment.

---

### 9 — `BloodAvatarLayer.renderItem` — commented-out `renderStatic` call (visual gap)

**File:** `client/render/layer/player/BloodAvatarLayer.java`, lines ~140-145

```java
public void renderItem(...) {
    if (!pItemStack.isEmpty()) {
        // renderStatic(pLivingEntity, swirlConsumer, pItemStack, pItemDisplayContext, pLeftHand,
        //     pMatrixStack, pBuffer, pLivingEntity.level(), pCombinedLight, OverlayTexture.NO_OVERLAY,
        //     pLivingEntity.getId() + pItemDisplayContext.ordinal());
    }
}
```

The `renderStatic` call was commented out because `ItemRenderer.renderStatic` has a different
signature in 1.21.1. The 1.21.1 equivalent is:

```java
Minecraft.getInstance().getItemRenderer().renderStatic(
    pLivingEntity, pItemStack, pItemDisplayContext, pLeftHand,
    pMatrixStack, pBuffer, pLivingEntity.level(),
    pCombinedLight, OverlayTexture.NO_OVERLAY,
    pLivingEntity.getId() + pItemDisplayContext.ordinal());
```

Uncomment and update to fix items held by the Blood Avatar not rendering.

---

### 10 — `ManipulationsTabController` — `ForgeRegistries.ITEMS` as iterable (compile warning)

**File:** `client/screen/skilltree/harbinger/ManipulationsTabController.java`, line 175

```java
for (Item item : net.neoforged.neoforge.registries.ForgeRegistries.ITEMS) {
```

`ForgeRegistries.ITEMS` is still iterable in NeoForge 1.21.1, so this compiles, but
`BuiltInRegistries.ITEM` is preferred (no reflection through the forge wrapper):

```java
for (Item item : BuiltInRegistries.ITEM) {
```

---

### 11 — Re-enable optional compat once deps port (tracked separately)

When NeoForge 1.21.1 builds are published for each dep, undo the source exclusion in
`build.gradle` (three `exclude` lines in the `sourceSets.main.java {}` block) and
uncomment the corresponding registration block in `Hemomancy.java`:

| Dep | Exclusion key | Hemomancy.java block | Additional work needed |
|-----|--------------|----------------------|------------------------|
| **Mana and Artifice** | `compat/mna/**` | `if (modList.isLoaded("mna"))` | Review all `compat/mna/` classes for 1.21 API changes; restore `MixinHUDOverlayRenderer` full body; restore `CuriosPlugin.clientCurioSetup`; uncomment `HemoMnAConfig` import; restore `EngramBlock.getAnalogOutputSignal` ChalkRuneTile logic. |
| **JEI** | `compat/jei/**` | _(not currently registered — was annotation-driven)_ | Review JEI API changes for 1.21.1. |
| **Curios** | `compat/curios/**` | `if (modList.isLoaded("curios"))` | Re-check `SlotTypePreset` API; restore `CuriosPlugin.clientCurioSetup` WandRenderer registration once MnA is also available. |

---

### 12 — `Hemomancy.java` comment cleanup (trivial)

Line 74 in `Hemomancy.java` has a dangling comment artifact from an earlier port step:
```java
 * Remove any /* TODO: inject IEventBus via constructor – FMLJavaModLoadingContext removed */ calls.
```
This is inside a Javadoc comment and does not affect compilation, but should be cleaned up.

---

### 13 — `ModList` declared but referenced only in comments (low)

`Hemomancy.java` line 132 still declares `ModList modList = ModList.get();` even though
both `if (modList.isLoaded(...))` blocks are commented out.  Remove or suppress the
unused-variable warning until the compat blocks are re-enabled.

---

## Build & Test Instructions

```bash
cd /home/runner/work/Hemomancy/Hemomancy

# Full compile check (no running game):
./gradlew compileJava --info 2>&1 | grep -E "error:|warning:|BUILD"

# Full build (produces JAR in build/libs/):
./gradlew build 2>&1 | tail -50

# Data generation (regenerates src/generated/resources):
./gradlew runData
```

The mod currently does **not** have automated unit tests; validate correctness by
running `runClient` / `runServer` with the game client.

---

## Priority Order for Next Session

1. **§7** `PacketHandler.sendToPlayersNear` (null `ServerLevel`) — most likely to cause
   a hard crash the first time any blood manipulation sends a particle packet.
2. **§5** ItemStack NBT deprecations — causes soft crashes for any living-tool toggle,
   morphling jar interaction, or ledger interaction.
3. **§6** `ForgeRegistries` data calls — most are benign in 1.21.1 but `tags()` and
   `getCodec()` can NPE.
4. **§9** `BloodAvatarLayer.renderItem` — silent visual missing; non-crashing.
5. **§8** Brewing recipes — harmless stub; clean up when potions are needed.
6. **§10-13** Cleanup / trivial.

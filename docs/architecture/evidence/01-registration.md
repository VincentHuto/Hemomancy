# Hemomancy audit 01: registration and initialization

Scope: `Hemomancy.java`, `common/init/**` (28 `*Init` classes + 6 `skills/*Branch` = 34 files), client registration, event-bus wiring, reload listeners, and Forge-era leftovers. This was a read-only audit. Counts exclude the Gradle-excluded `compat/mna/**` and `compat/curios/**` (37 files) unless stated. Bus semantics were checked against the resolved loader: NeoForge 21.1.251 requires FML `loader 4.0.44`, whose `EventBusSubscriber.bus()` is `@Deprecated(forRemoval = true)` with the note "This value is ignored, and the bus is determined automatically" (routing goes by `IModBusEvent`).

Legend: **VERIFIED** means confirmed by grep or reading the code. **LIKELY** means strong static evidence, not run in game. **HYPOTHESIS** means plausible but unproven.

---

## 1. Init-class inventory

| File | Lines | DeferredRegisters owned | Style | Holder naming | Mixed responsibilities / notes |
|---|---|---|---|---|---|
| ManipulationInit | 1296 | `MANIPS` + custom registry `MANIPS_TYPE_REGISTRY` | raw `MANIPS.register` ×123 | 123 snake | Inline Drudge AI lambdas; name string duplicated in ctor (121/121 match the id); linear `getByName` |
| ItemInit | 1260 | `BASEITEMS`(266 direct), `HANDHELDITEMS`(8), `SPECIALITEMS`(52), `SPAWNEGGS`(60) | raw ×386 + helpers `registerBloodMemoryItem`(122), `registerScarItem`(24), `tincture`/`tinctureJug`(7+7) | 543 snake / 1 UPPER (`CHAMBER_OF_WILL_TELEPORTER`) | Anonymous `Item` subclass with gameplay (L75); `getAllItemEntriesAsStream` |
| EntityInit | 1139 | `ENTITY_TYPES` | raw ×124 | 142 snake | Entity tag, spawn placements (45), attribute creation (103 `event.put`), **empty** `commonSetup` subscriber (L1136) |
| BlockInit | 1041 | **10** (`BASE`,`SLAB`,`STAIR`,`COLUMN`,`CROSS`,`OBJ`,`MODELED`,`SPECIAL`,`POTTED`,`LIQUID`) | raw ×192 | 194 snake / 3 UPPER (liquid blocks) | BlockItem routing (`itemDecision`, `ItemRoute`, `CreativeRoute`, 5 hand lists L938–972), `RegisterEvent` item registration (L998), flower-pot setup (L1021), two different enumerators |
| BiomeInit | 475 | `BIOME_REGISTER` (**empty**) | ResourceKey helper | 9 UPPER keys | ~380 lines of dead Java biome builders; TerraBlender region/surface setup in `FMLCommonSetupEvent`; `bus = Bus.MOD` |
| SoundInit | 410 | `SOUNDS` | helper `registerSoundEvent` | 180 UPPER | none |
| EffectInit | 406 | `EFFECTS`, `POTION_TYPES` | raw + `schoolState` helper | 87 snake | Brewing mixes, mnemonic-potion drink events, **empty** `setupPotionRecipes` (L355) |
| RenderTypeInit | 361 | none | `extends RenderType` | n/a | Client-only class in `common/init` |
| PlacedFeatureInit | 313 | none (keys + `bootstrap`) | keys | 34 UPPER | `bootstrap` is dead (see §4) |
| ConfiguredFeatureInit | 288 | none (keys + `bootstrap`) | keys | 37 UPPER | `bootstrap` is dead |
| ShaderInit | 276 | none | `@EventBusSubscriber(Dist.CLIENT)` | mutable `public static ShaderHolder` | Client-only class in `common/init` |
| BlockEntityInit | 265 | `TILES` | raw | 63 snake | none |
| ScarInit | 244 | `SCARS` + custom registry | `reg()` + `cerebral`/`fungal` factories | 33 snake | Nested gameplay class `BloodHonedDefinition` (L200–243) |
| StillArtInit | 187 | `STILL_ARTS` + custom registry | raw | 9 snake | Inline gameplay lambdas; linear `getByName` |
| ManipulationTreeInit | 182 | none (static `ENTRIES`/`BY_NAME`) | `register(name,x,y,parents)` ×77 | n/a | UI layout data; only consumer is the client `ManipulationsTabController`; edited by `tools/skill_tree_editor` |
| FluidInit | 159 | `FLUID_TYPES`, `FLUIDS` | raw | 9 UPPER | Uses deprecated `FluidType.initializeClient` ×3 |
| SkillPointInit | 116 | none (mutable static lists + 73 public static fields) | `registerSkill` | snake | Hand-assigned int IDs (74, unique); format owned by the skill-tree editor |
| RecipeInit | 109 | `SERIALIZERS`, `RECIPE_TYPES` | raw | 30 snake | id/`RecipeType.simple` name drift (`memory_weaving_type` vs `memory_weaving`); field `chisel_recipe` holds `scar_recipe` |
| BaseFeatureInit | 94 | `FEATURE_REGISTER` | helper that captures an **eager** instance | UPPER (raw `Feature`, no holder) | Empty, uncalled `setup()` |
| ContainerInit | 90 | `CONTAINERS` | raw | 26 snake | none |
| AttributeInit | 87 | `ATTRIBUTES` | raw | 1 private UPPER | Fall-flying gameplay (`canFallFly`, `TriState`), unused instance `canFly`; `bus = Bus.MOD` |
| StructureInit | 87 | `STRUCTURES`, `STRUCTURE_PIECES`, `BIOME_MODIFIER_SERIALIZERS` (**non-final, empty, never registered**) | raw | 16 snake | Forge-era comment at L17 |
| DataComponentInit | 83 | `COMPONENTS` | raw, mix of one-liners and FQNs | 14 UPPER | `cacheEncoding()` on 4 of 14 |
| ParticleInit | 78 | `PARTICLE_TYPES` | raw | 12 snake | Client provider registration inside a `common/init` class |
| VillagerInit | 50 | `POINTS_OF_INTEREST`, `PROFESSIONS`, `STRUCTURE_PROCESSORS` | raw | 3 UPPER | Structure processors live in VillagerInit; unused private `HEMOPOTHECARY_PROCESSOR_LIST_KEY` |
| CarverInit | 23 | `CARVERS` | eager helper | 1 UPPER | none |
| LootModifierInit | 19 | `LOOT_MODIFIERS` | raw | 2 UPPER | none |
| TerraBiomeInit | 4 | none | n/a | n/a | **Empty class** with no references |
| skills/*Branch (6) | 420 | none | editor-generated blocks | n/a | `// <skill-editor ...>` markers |

Naming tally across holders: snake_case dominates gameplay registries (Block, Item, Entity, Effect, BlockEntity, Menu, Recipe, Manip, Scar, StillArt, Particle, Structure). UPPER_SNAKE is used in Sound, DataComponent, Fluid, LootModifier, Villager, and all worldgen keys. No camelCase holders exist. Mixed UPPER/snake appears inside BlockInit (3 liquids) and ItemInit (1). Hemomancy.java also owns `CREATIVETABS` with camel-less lowercase holders (`hemomancytab`, `hemomancywiptab`), and `HemoAttachmentTypes` owns `ATTACHMENT_TYPES`. In total there are 42 DeferredRegisters, and 41 of them are registered in the constructor (all but `BIOME_MODIFIER_SERIALIZERS`).

**Is the Block/Item split meaningful? Partly.** The block split drives datagen shape routing: `HemoBlockStateProvider` L35–157 and `HemoItemModelProvider` L27–84 iterate each register separately. The concatenation order of `getAllItemEntriesAsStream` (BASE→SPECIAL→HANDHELD→EGGS) and `getAllBlockEntriesAsStream` sets creative-tab order, and `RegistryCreativeOrderSourceTest` pins it. So the split is **not** arbitrary. The cost is the cross-register scans repeated in runtime code: `DrudgeEntity` L418–420, `DrudgeMemoryLayer` L64–68, `DebugShowcaseItem` L115–127/195–202, and `ClientEvents` L1353/L1413 (BASEITEMS only).

**Static-init order.** It works today because each cycle is eager in one direction only. `FluidInit.*_PROPS` (L43, L96, L151) eagerly read `BlockInit.*_BLOCK` holders, while `BlockInit`'s liquid blocks read `FluidInit` only inside suppliers. **HYPOTHESIS:** if a future change reads `FluidInit` eagerly from `BlockInit`, a null holder becomes possible. `BaseFeatureInit` and `CarverInit` construct `Feature`/`WorldCarver` instances at class-load and wrap them in `() -> value`. That is a Forge-era pattern; it is harmless today but bypasses lazy construction.

---

## 2. Competing patterns

### P1. Event-bus subscription styles (VERIFIED)
There are 211 `@EventBusSubscriber` classes holding 212 annotations (`ClientEvents` has a nested one) and 636 `@SubscribeEvent` methods:
- `modid = MOD_ID`: 159 (148 spaced + 11 unspaced)
- `modid + Dist.CLIENT`: 44, in 5 spelling variants
- `Dist.CLIENT` without modid: 3 (`RadialChooseManipScreen` L46, `RadialChooseVeinScreen` L30, `RadialMenuScreen` L22)
- with the ignored `bus = …MOD`: 6 (`AttributeInit` L22, `BiomeInit` L32, `ScriptoriumLegacyMigration` L12, `PhlegethonticClientEvents` L18, `FloorMesh.ReloadHook` L1110, `ClairaudiographAnalysis` L17)

Manual wiring uses three more styles:
- **Hemomancy constructor:** `modEventBus.addListener` ×6, including two lambdas that wrap static methods (`LiberDiscoveryEvents.commonSetup`, `UnstainedProgressEvents.commonSetup`), even though both classes already carry `@EventBusSubscriber` (L37 and L19). It also uses `forgeBus.register(this)` for one instance `@SubscribeEvent` (`onServerAboutToStart`) and `forgeBus.addListener` ×2.
- **Helper `register(bus)` methods:** `HemoCapabilityRegistrar.register` (L56) and `PacketHandler.registerChannels(IEventBus forgeBus)` (L72). The parameter is misnamed: it receives the mod bus.
- **Runtime registration:** `NeoForge.EVENT_BUS.register(RenderBloodLaserEvent.class)` (`ClientEvents` L1066) and `ClinicalRecipeVisibility` L28. The JEI one is lazy for a good reason: it only listens once JEI hands over a runtime.

The `ClientEvents.ClientModBusEvents` nested class (L853) exists only to separate mod-bus events. Bus auto-detection makes that split cosmetic.

- **Canonical:** `@EventBusSubscriber(modid = Hemomancy.MOD_ID[, value = Dist.CLIENT])`, never `bus=`, static handlers. Keep manual `addListener` only where the subscription is conditional (JEI).
- **Simplification:** remove 6 `bus=` arguments, 2 wrapper lambdas, the instance `register(this)`, and the nested-class indirection.
- **Difficulty:** Low. **Compatibility risk:** none (bus is ignored; no IDs touched).

### P2. Creative-tab membership is maintained in three places (VERIFIED)
- `Hemomancy.acceptWipCreativeTabContents` (L196) lists 42 items + 17 blocks.
- `Hemomancy.isWipItem` (L282) re-lists **the identical 42 items**; a `diff` of the extracted identifiers came back empty.
- `BlockInit.WIP_BLOCKS` (L966) re-lists **the identical 17 blocks**.

`BlockInit.CreativeRoute.WIP` is computed but never consumed; `Hemomancy` only uses `== MAIN`. Exclusions are spread over `shouldShowItemInCreativeTab` (8 hard-coded items), `isLegacyLivingWeaponMemory` (7), `ManipulationRetirementRules.isRetiredMemoryItem`, `BlockInit.HIDDEN_BLOCK_ITEMS`, and the special cases inside `buildContents` (fervent husk, cleansing hemolymph, syringe). `HemomancyCreativeTabsSourceTest` L97 currently *asserts* the duplication (`assertAppearsAtLeastTwice`).

- **Canonical:** one ordered `List<Supplier<? extends ItemLike>> WIP_TAB` that drives both acceptance and a derived `Set` for exclusion. Route WIP blocks through the existing `CreativeRoute.WIP` instead of a second list, and put the hide-list next to it (for example in a `HemoCreativeTabs` class).
- **Simplification:** about 100 lines removed from `Hemomancy.java`, plus one source of truth.
- **Difficulty:** Low. **Risk:** tab ordering is player-visible but cosmetic, and source tests need rewriting.

### P3. Three lookup contracts for three custom registries (VERIFIED)
- `ManipulationInit.getByName` (L1288) does a linear scan of the internal `getName()` string and returns `null`. It has 30 call sites. Seven other sites use `MANIPS_TYPE_REGISTRY.get/containsKey(rloc)` directly.
- `StillArtInit.getByName` (L171) does a linear scan and returns `StillArt.BLANK`. It has 3 call sites.
- `ScarInit.getByName` (L181) does an O(1) `registry.get(rloc)` and accepts namespaced ids. It has 8 call sites.

Every manipulation constructor repeats its own registry id as a string literal (121 of 121 checked, no mismatches). That duplicate source of truth is what forces the linear scan.

- **Canonical:** the ScarInit form, `registry.get(id)` with path-or-namespaced parsing, behind one small generic helper. Keep the `BLANK` null-object semantics where callers rely on them.
- **Difficulty:** Low for the lookup. Removing the duplicated ctor name is **Med** because `getName()` may be serialized (capability NBT, packets). Keep the field and just stop scanning it.
- **Risk:** Low if the lookup returns the same object for every current name.

### P4. Client renderer and registration placement (VERIFIED)
- **Entity renderers:** registered via `event.registerEntityRenderer` ×142 in `RegisterRenderers`.
- **Block entity renderers:** all 50 go through vanilla's static `BlockEntityRenderers.register` inside `FMLClientSetupEvent` (`ClientEvents` L1060–1145), **outside** `enqueueWork`. There are 0 uses of `event.registerBlockEntityRenderer`. **LIKELY:** setup runs in parallel across mods and `BlockEntityRenderers.PROVIDERS` is a plain map, so this is a latent thread-safety issue.
- **Particle providers:** registered with `Minecraft.getInstance().particleEngine.register(...)` inside `RegisterParticleProvidersEvent` (`ParticleInit` L64–76) rather than `event.registerSpriteSet/registerSpecial`.
- **Fluid client extensions:** use `FluidType.initializeClient` (`FluidInit` L27/56/114). This is `@Deprecated(forRemoval = true, since = "1.21")` in 21.1.251, which asks for `RegisterClientExtensionsEvent` instead. `ClientEvents` L1315 already uses that event for items.
- **`ItemBlockRenderTypes.setRenderLayer` ×5:** fluids still need it, but `frozen_cruor` could use model `render_type` like the 267 block models that already do.

Recommended canonical approach:
- `event.registerBlockEntityRenderer` in `RegisterRenderers`.
- `event.registerSpriteSet` for particles.
- `RegisterClientExtensionsEvent.registerFluidType` for fluids.

**Difficulty:** Low to Med (mechanical). **Risk:** Low, though the BER move needs a visual smoke test.

### P5. Client registration: central vs feature-local (VERIFIED)
- **Central:** `ClientEvents` (1495 lines) handles `RegisterRenderers`, menus (26), key mappings (12), colors, item decorations, client extensions, `ModelEvent.*` (3), GUI layers (16), dimension effects, render buffers, recipe-book categories, and 19 `ItemProperties`, alongside game-bus tick, tooltip and render handlers.
- **Feature-local:** `PhlegethonticClientEvents` registers its own layers, renderers and render layer.
- **Self-subscribed reload listeners:** `FloorMesh.ReloadHook` and `ClairaudiographAnalysis`. `ClientEvents` L877 registers a third reload listener.
- **Delegated:** `LowtideRuinObjModels.register/cache`, called from `ClientEvents`.
- **Split across files:** layer definitions live in `LayerEvents` (192) and `PhlegethonticClientEvents` (3).
- **Two part-model helper styles:** `Renderer.part(name)` vs inline `ModelResourceLocation.standalone(rloc("block/x_" + part))` (`ClientEvents` L1368–1398).
- **Wrong package:** `ParticleInit` and `ShaderInit` are client subscribers in `common/init`; `RenderTypeInit` is client-only in `common/init`; `RenderBloodLaserEvent` is a client handler in `common/capability/...`.

Recommended canonical approach:
- Split `ClientEvents` by concern into a `client/registry/` package: `EntityRendererRegistrar`, `ModelRegistrar`, `GuiLayerRegistrar`, `ClientExtensionsRegistrar`, `ItemPropertiesRegistrar`.
- Keep game-bus runtime handlers separate.
- Move `ShaderInit`, `RenderTypeInit` and the provider half of `ParticleInit` under `client/`. Keep the `PARTICLE_TYPES` DeferredRegister in common.

**Difficulty:** Med (large file, many imports). **Risk:** Low; no IDs change.

### P6. Reload listeners: one shared helper, mostly unused (VERIFIED)
Ten server listeners, all built on `SimplePreparableReloadListener`; none use Codecs.

| Listener | Base | Registered from | Parse style |
|---|---|---|---|
| `IchorianSigilLoader`, `CardinalRiteFloorLoader` | `JsonResourceReloadListener<T>` (shared helper, `common/data`) | `Hemomancy` L347–348 | Gson via helper |
| `DiscoveryInscriptionLoader`, `ItemInquiryLoader`, `ScriptoriumAffinities`, `BloodInjectionData`, `BloodProfileData`, `ClairaudiographCatalogue` | direct `SimplePreparableReloadListener`, hand-rolled `listResources` | `Hemomancy` L341–346 (4 written as inline FQNs) | manual Gson / `GsonHelper` |
| `SuccessionProfessions` | direct | its **own** `@SubscribeEvent AddReloadListenerEvent` (L29) | manual |

Plus 3 client `ResourceManagerReloadListener` lambdas (P5) and the HutosLib `BookPlaceboReloadListener` serializer.

- **Canonical:** `JsonResourceReloadListener` for one-file-per-definition loaders (the 6 direct ones fit), all registered in one `HemoReloadListeners.register(AddReloadListenerEvent)`. `ItemInquiryLoader`'s nested `<npc>/<namespace>/<item>` layout may justify staying custom; check before migrating.
- **Difficulty:** Med (each parser has a different error policy). **Risk:** Low for gameplay, but log text and error tolerance differ per loader. Keep the resource paths unchanged.

### P7. Registration helper vs raw `register` (VERIFIED)
- **Helpers:** SoundInit, ScarInit, `ItemInit` (4 helpers), `EffectInit.schoolState`, Carver/BaseFeature (eager).
- **Raw calls:** everything else, including 14 hand-built `DataComponentType.builder().persistent(X.CODEC).networkSynchronized(X.STREAM_CODEC)` chains with 24 inline FQNs, 124 `EntityType.Builder` chains, and 63 `BlockEntityType.Builder.of(...).build(null)`.
- **Canonical:** NeoForge's own `DeferredRegister.Items/Blocks/DataComponents` (`createItems`, `registerSimpleBlockItem`, `registerComponentType`) where they fit. `DeferredItem` extends `DeferredHolder`, so most call sites compile unchanged.
- **Difficulty:** Med. **Risk:** none for IDs; any change to a field's static type is source-only.

### P8. BlockItem dual path (VERIFIED, intentional)
Blocks get items through a `RegisterEvent` handler (`BlockInit` L998) driven by `ItemRoute` tables. Six blocks instead use MANUAL items in `ItemInit`. The routing table is well-tested (`RegistrationGameTests` L172–254) and is a reasonable design. The one recommendation is to fold `ITEMLESS_BLOCKS`, `HIDDEN_BLOCK_ITEMS` and `WIP_BLOCKS` into per-registration metadata instead of string sets keyed by path, which silently mismatch on typos. **Difficulty:** Med. **Risk:** creative contents only.

### Variants that differ for good reasons (no action)
- **SkillPointInit/skills and ManipulationTreeInit** are rewritten in place by `tools/skill_tree_editor` (`manipulationWorkspace.ts`, `// <skill-editor>` markers). Their verbose literal style is load-bearing, and the skill int IDs (74, unique) may be persisted. Do not "registry-fy" them.
- **122 `registerBloodMemoryItem` calls** mirror 123 manipulations one-to-one. Generating them in a loop would lose the public fields other code references.
- **`ClinicalRecipeVisibility`'s** lazy `addListener` is correct for an optional JEI runtime.

---

## 3. Hotspot classes

| Class | Lines | Why it is hot |
|---|---|---|
| `client/event/ClientEvents.java` | 1495 | ~25 kinds of registration events plus game-bus runtime handlers; 2 subscriber classes; 138 renderer registrations |
| `client/render/world/floor/FloorMesh.java` | 1431 | Mesh code plus a nested reload-hook subscriber |
| `common/init/ManipulationInit.java` | 1296 | 123 registrations with inline Drudge AI lambdas (~15 `getEntitiesOfClass` blocks) |
| `common/init/ItemInit.java` | 1260 | 544 holders across 4 registers; anonymous item class |
| `common/init/EntityInit.java` | 1139 | Types, spawn placements, 103 attribute puts, tag |
| `common/init/BlockInit.java` | 1041 | 10 registers; item routing; creative routing; pot setup |
| `common/network/PacketHandler.java` | 499 | 181 payload registrations in one method (in scope of another audit) |
| `common/init/BiomeInit.java` | 475 | ~380 dead builder lines; live TerraBlender setup |
| `client/event/LayerEvents.java` | 426 | 192 layer definitions |
| `common/init/SoundInit.java` | 410 | Flat but fine |
| `common/init/EffectInit.java` | 406 | Effects plus brewing plus drink events |
| `Hemomancy.java` | 366 | ~160 lines are creative-tab lists (P2) |

---

## 4. Dead and suspicious code

| # | Item | Confidence | How checked |
|---|---|---|---|
| D1 | `BiomeInit.bootstrapBiomes` and its private biome builders (~L57–431), `PelagicBiomes.bootstrap`, `ConfiguredFeatureInit.bootstrap`, `PlacedFeatureInit.bootstrap`, `data/gen/HemoFeatureUtils`, `HemoPlacementUtils` | VERIFIED | No callers in main, test, gameTest or Gradle. `DataGeneration` registers no datapack-registry provider. Two tests state "bootstrapBiomes is dead code" (`MyelinBorerSpawnSourceTest` L26, `CorticalDriftBiomeRegistrationSourceTest` L64). JSON under `data/hemomancy/worldgen/` (36 placed, 37 configured) is authoritative. **Caveat:** `EscharianOvergrowthResourceTest` L81 pins a source line inside the dead builder. The `ResourceKey` constants and `PelagicBiomes.key` are live and must stay. |
| D2 | `BiomeInit.BIOME_REGISTER` | VERIFIED | Has no entries; its only reference is `register(modEventBus)`. A DeferredRegister for a datapack registry is a no-op anyway. |
| D3 | `StructureInit.BIOME_MODIFIER_SERIALIZERS` | VERIFIED | Non-final, empty, never registered, 0 references. |
| D4 | `TerraBiomeInit` | VERIFIED | Empty body; only self-reference. |
| D5 | `BaseFeatureInit.setup()`, `EffectInit.setupPotionRecipes`, `EntityInit.commonSetup` | VERIFIED | Empty bodies (perl scan of all `@SubscribeEvent`); `setup()` has 0 callers. |
| D6 | `Hemomancy.instance`, `Hemomancy.forcesLoaded` | VERIFIED | Write-only across main, test and gameTest. |
| D7 | `AttributeInit.canFly` (instance), `ScarInit.getAllEntries`, `VillagerInit.HEMOPOTHECARY_PROCESSOR_LIST_KEY` | VERIFIED | 0 call sites. `MixinHooks.canFly` is a different method. |
| D8 | `BlockInit.getAllBlockEntries()` | VERIFIED | Only caller is `HemoBlockLootTableProvider`, which `DataGeneration` does not register. It also enumerates a different set than `getAllBlockEntriesAsStream` (it includes POTTED). |
| D9 | `RenderBloodLaserEvent` + `RenderBloodLaser` | VERIFIED | `renderLaser(...)` has an empty body (L84–85), and `drawLaser/drawLasers/adjustBeamToEyes` have 0 callers. Yet the handler is registered at client setup and iterates every player on **every** `RenderLevelStageEvent` stage. |
| D10 | `shouldShowItemInCreativeTab`'s `item != BlockInit.dictation_table.get().asItem()` (`Hemomancy` L259) | LIKELY | The item loop only walks `ItemInit` registers, and `dictation_table` (SPECIALBLOCKS, AUTO/MAIN route) gets its BlockItem from `RegisterEvent`, so the check is a no-op. The block loop then adds the "legacy" table (per `RegistrationGameTests` L69) to the main tab. Not observed in game. |
| D11 | `ManipulationTreeInit.init()` runs in common setup on dedicated servers | VERIFIED (harmless) | The only consumer is client `ManipulationsTabController`. |
| D12 | Particle `*Type`/`*Data` classes under `client/particle/{type,data}` (9+9) are imported by 22 common/server files | VERIFIED not a crash | None of the 18 files import client classes. It is a package-placement inconsistency: `HitGlow` alone already lives in `common/particle`. |
| D13 | `Hemomancy.clientSetup` references `client.morphling.MorphlingMutationRegistry` from the common `@Mod` class | VERIFIED safe | It only runs on `FMLClientSetupEvent`, which is lazily resolved. It should live in a client subscriber for consistency. |
| D14 | `AGENTS.md`/task says `BlockInit` has 9 DeferredRegisters | VERIFIED drift | There are 10 (`LIQUIDBLOCKS` L89). |

**Process note (VERIFIED):** 471 test files use `Files.readString` source assertions, and many pin registration text: creative order, WIP duplication, `itemDecision(` presence. Every recommendation above needs those tests updated together with the code.

---

## 5. Forge-era leftovers (exact hits, active source)

- `net.minecraftforge`, `@ObjectHolder`, `IForgeRegistry`, `DistExecutor`, `FMLJavaModLoadingContext`, `MinecraftForge.`, `NetworkRegistry`, `LazyOptional`, `ICapabilityProvider`, `ForgeConfigSpec`, `Mod.EventBusSubscriber`: **0 hits** (main, test and dormant compat).
- `RegistryObject`: 1 hit, a **comment only**: `common/worldgen/feature/SporeNexusTowerFeature.java:32`.
- `SimpleChannel`: 2 hits, both **comments** in `common/network/PacketHandler.java:50,60`. No code uses it.
- `ForgeRegistries`: 4 files, all **false positives** matching `NeoForgeRegistries`: `HemoAttachmentTypes:30/36`, `FluidInit:12/22`, `LootModifierInit:9/13`, `StructureInit:13/32`.
- `@OnlyIn`: **77 annotations in 63 files**, plus 2 in dormant compat (`import net.neoforged.api.distmarker.OnlyIn` ×65). 37 are in `client/` (redundant there), and the rest are on item, block and entity methods in `common/`. The mergetool javadoc says "modders should avoid its use". **Canonical:** delete them in `client/`; in common classes, move client calls behind `client/` helper classes rather than stripping methods. **Difficulty:** Low per file. **Risk:** Low, but verify no `@OnlyIn` method overrides a vanilla client-only signature before deleting.
- Forge-era style that is still valid NeoForge but superseded:
  - `bus = Bus.MOD` ×6: deprecated for removal and ignored.
  - `FluidType.initializeClient` ×3: deprecated for removal.
  - `BlockEntityRenderers.register` ×50 in client setup.
  - Eager-instance `() -> value` registration in `BaseFeatureInit`/`CarverInit`.
  - The "preferred way on Forge" comment at `StructureInit` L17.

---

## Suggested order (lowest risk first)
1. **Remove dead code:** D2–D7 and D9, and the `bus=` arguments. Update the source tests at the same time.
2. **Creative tabs:** single source of truth (P2), which also fixes D10 if confirmed in game.
3. **Lookups:** unify custom-registry lookups (P3).
4. **Client API alignment:** BER, particle and fluid extension APIs (P4).
5. **Remove the dead worldgen bootstrap chain (D1).** Do this only after rewriting `EscharianOvergrowthResourceTest` to assert the JSON instead.
6. **Large structural moves:** split `ClientEvents` and move client classes out of `common/init` (P5), adopt the reload-listener helper (P6), and adopt `DeferredRegister.Items/Blocks` (P7).

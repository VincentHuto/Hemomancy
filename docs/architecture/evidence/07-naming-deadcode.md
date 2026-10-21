# Hemomancy audit 07: naming, packages, resources, mixins, compat, config and dead code

This audit was read-only. No repository file was changed. Paths are relative to `src/main/java/com/vincenthuto/hemomancy/` unless shown otherwise. **VERIFIED** means I confirmed it with grep, find, git or by inspecting the jar. **HYPOTHESIS** means it is inferred and needs a runtime check or a check against the API. Nothing here suggests changing registry IDs, resource locations or serialized keys.

## 1. Package map (VERIFIED)

There are 3,492 main Java files: `client` 1,176, `common` 2,182, `compat` 58, `config` 6 and `mixin` 69 (core 67, util 2). Tests: `src/test` has 1,242 files, and 471 of them read source text with `Files.readString`. `src/gameTest` has 198.

The largest third-level packages are:

- `client/`: `render` 587, `screen` 221, `model` 196, `player` 52, `particle` 46, `event` 27, `data` 21.
- `common/`: `entity` 370, `item` 310, `network` 184, `capability` 180, `manipulation` 179, `worldgen` 144, `block` 143, `rite` 142, `tile` 92, `event` 56, `effect` 46, `mission` 44, `recipe` 39, `init` 34.
- Smaller packages: `circus` 21, `data` 20, `antecedent` 18, `succession` 18, `station` 10, `vein` 7, `command` 4, `testing` 1, `loot` 1.

**Concepts split across places:**

- **Block entities:** 62 of the 64 `*BlockEntity` classes are in `common/tile/**`. No `block/**/entity` package exists. The two outliers are `common/antecedent/VigilArchiveBlockEntity` and the dormant `compat/mna/tile/BrokenManaTrapazahedronBlockEntity`. The package is called `tile`, but only one class uses "Tile" in its name (`IWhiteHumorTile`).
- **Vein:** `common/vein` (7, server travel/session/rules) and `client/vein` (2, client state and visuals) are a clean side split.
- **Data:** the two `data` packages mean different things. `common/data` holds the `JsonResourceReloadListener` base, `book/` and the datagen `gen/` package. `client/data` holds 21 client caches of synced server state (`*ClientData`/`*ClientState`). The suffixes `ClientData` (15) and `ClientState` (6) are used interchangeably.
- **Station upgrades** are spread over four places:
  - `common/station` (catalog/progress/rules/migration)
  - `common/rite/harbinger/StationUpgradeRites`
  - `common/tile/harbinger/crafting/*BlockEntity`
  - the screens

  "Station" is also overloaded: `CardinalRiteStationMatcher` and `CardinalRiteNpcStationRules` use it for rite and NPC positions.
- **Testing:** `common/testing/DevTestHooks` is a reflective bridge into `src/gameTest`. It registers 14 classes by name, and all 14 exist. Another 115 gameTest classes use `@GameTestHolder`.
- **Client code in common:** 78 files under `common/` import `net.minecraft.client`. Examples are `common/init/RenderTypeInit` (extends `RenderType`), `ShaderInit`, `FluidInit` and `capability/.../bloodvolume/RenderBloodLaser`. HYPOTHESIS: some of these are class-loaded only on the client. A dedicated-server smoke run is the proof.

**Path splits.** `harbinger/unstained/shared` splits exist in `block`, `item`, `tile`, `capability/player`, `entity/npc`, `rite` (no shared), `mission` (no harbinger), `network/capa` (no shared), `client/render/{tile,item}` and `client/screen/skilltree`. They are absent from `manipulation`, `entity/mob`, `event`, `effect`, `recipe`, `worldgen` and `menu`, which use tendency or topic packages instead. The splits are uneven: item 238/25/34, block 116/10/11, tile 80/4/2.

**One-file packages** (about 45, mostly boss subfolders): `client/util`, `common/testing`, `common/particle` (and `/data`, `/type`), `common/loot/modifier`, `common/worldgen/carver`, `common/network/{discovery,will}`, `common/mission/{hermit,mnemonist,cicatrix_anchorite}`, `common/entity/boss/saint/{hemorath,putriciel,velorum}`, `client/render/{geometry,scar}`, `client/screen/{discovery,inventory,summon,unstained}`, `compat/curios`.

## 2. Naming

### Class-name suffix counts (VERIFIED, `src/main/java`)

| Suffix | Count | Suffix | Count |
|---|---|---|---|
| `*Rules` | **323** | `*Init` | 35 (28 core + 7 dormant MnA) |
| `*Events` | 112 | `*Registry` | 12 |
| `*Data` | 63 | `*Ledger` | 5 |
| `*State` | 59 | `*Access` | 5 |
| `*Helper` | 42 | `*Utils`/`*Util` | 4/1 |
| `*Handler` | 29 | `*Loader` | 4 |
| `*Manager` | 22 | `*Service` | 4 |
| `*Controller` | 16 | `*Policy`/`*Engine`/`*Registrar` | 2/1/1 |

`*Rules` (pure, testable logic) is now the dominant convention. It clusters in `entity/boss/endgame` (30) and `rite` (16+11). `*Events` is used for both `@EventBusSubscriber` holders and plain classes; three `*Events` classes are empty stubs (section 8). `*Handler` mixes event subscribers (`ConsecrationHandler`, `ScratchEngramHandler`) with `IItemHandler` implementations (`common/item/itemhandler/*ItemHandler`) and `PacketHandler`. `*Registry` (12) means static lookup tables, both client and common, not NeoForge registries. `HemoCapabilityRegistrar` is the only `*Registrar`.

### Manager / Service / Helper / Util with purpose (to show overlap)

- **Managers (22)** are static per-level or per-player runtime state plus a tick. Examples: `ChummedWatersAreaManager`, `LanternTickLightManager`, `BloodInfusionManager`, `BloodStructureFeedManager`, `PendingBloodCraftManager`, `CastingAnimationManager`, `TemporaryIceManager`, `ConductionManager`, `AxonalTransductionManager`, `ManipulationChannelManager`, `HematicCommandManager`, `BlackVeilCovenantManager`, `EarthenVeinTravelManager`, `FoldedHallwayManager`, and the encounter managers `ChamberOfWillManager`, `CorticalArcManager`, `MycophantEncounterManager`, `VagrantMindEncounterManager` and `VesperOrdealManager`.
- **Services (4)** do the same job as a Manager:
  - `ChamberVisitService`: visit safety/snapshots.
  - `MuscleMemoryActivationService`
  - `PaleIntercessionSummonService`
  - `CardinalRiteAllyService`

  `ChamberVisitService` and `ChamberOfWillManager` sit side by side in `common/worldgen` under different suffixes.
- **Helpers (42)** are static utilities. 17 are client render helpers, such as `ArmorItemModelPoseHelper` and `SilentArchonPlayerRenderHelper`. 14 are capability-state helpers, such as `ManipSlotHelper`, `ManipulationEquipHelper`, `SynapticLoadoutSlotHelper` and `KnownManipulationGrantHelper`. These four all manage manipulation slots and loadouts, and `ManipulationSlotLedger` overlaps them. Other examples are `BloodRoutingHelper`, `CirculationIncomeHelper`, `SkillPointHelper` and `MemoHelper`. `GuideBookTooltipHelper` is unreferenced (section 8).
- **Utils (5):** `EntityParticleUtils`, `ScreenDrawUtils`, `SetUtil`, plus the datagen `HemoFeatureUtils` and `HemoPlacementUtils`. Both datagen utils are unreferenced.

### Other competing conventions (VERIFIED counts)

| Convention | Count |
|---|---|
| `Packet*` prefix vs `*Packet` suffix | 71 vs 111 (both in `common/network`) |
| `Screen*` prefix vs `*Screen` | 4 vs 46 |
| `Render*` prefix vs `*Renderer` | 5 vs 296 |
| `Recipe*` prefix vs `*Recipe` | 22 vs 19 |
| `*Container*` vs `*Menu` | 6 vs 26 |
| `Item*` prefix vs `*Item` | 19 vs 187 |
| `I`-prefixed interfaces vs not | 35 vs 43 |
| `Enum*` prefix | 20 of 267 enums |
| `*Manip*` vs `*Manipulation*` | 103 vs 77 |

`common/network/capa` (117 files) uses a truncated name. The class-name typo `PhanstmalBloodlustArmorAbilityHandler` exists, and the method typo `DataGenParsers.genCrosslBlockModel` does too.

### Static holder styles in `common/init` (VERIFIED)

- **Holder types:** every holder is `DeferredHolder<R,T>`. There are 0 `DeferredItem` or `DeferredBlock`.
- **Holder field case:** most are lowercase snake matching the registry ID: Item 543/552, Block 194/200, Entity 142/142, Effect 87/88, Manipulation 123/123. `SoundInit` (181), `DataComponentInit`, `FluidInit` and `VillagerInit` use UPPER_SNAKE. `BlockInit` and `ItemInit` mix a few UPPER fields, for example `PHLEGETHONTIC_ICHOR_BLOCK` and `CHARM_TALISMAN`.
- **`DeferredRegister` field names:** plain plurals (`EFFECTS`, `SOUNDS`), `*_REGISTER` (`FEATURE_REGISTER`, `BIOME_REGISTER`), category buckets (`BASEBLOCKS`, `SLABBLOCKS`, `OBJBLOCKS`, `MODELEDBLOCKS`, `BASEITEMS`, `HANDHELDITEMS`, `SPECIALITEMS`, `SPAWNEGGS`), abbreviations (`MANIPS`, `TILES`) and `CREATIVETABS` in `Hemomancy`.
- **Non-registry "Init" classes:** `SkillPointInit` (mutable lists), `RenderTypeInit`, `ShaderInit` (non-final `ShaderHolder`s), `ConfiguredFeatureInit`, `PlacedFeatureInit`, `ManipulationTreeInit`, plus the empty `TerraBiomeInit` and the `init/skills/` subpackage.
- Registry ID typos such as `ITEM_QLIPHOPH_POME_*` and `ENTITY_ABHORENT_THOUGHT_*` must not change.

## 3. ResourceLocation and constants (VERIFIED)

| Form | Occurrences / files |
|---|---|
| `Hemomancy.rloc(` | 1,544 / 755 |
| `fromNamespaceAndPath(Hemomancy.MOD_ID` / `(MOD_ID` | 24 / 24 |
| `fromNamespaceAndPath("hemomancy"` | 56 / 32 |
| `fromNamespaceAndPath(` (all namespaces) | 99 / 66 |
| `ResourceLocation.parse("hemomancy:` | 5 / 4 |
| `ResourceLocation.parse(` (all) | 64 / 39 |
| `tryParse(` | 44 / 35 |
| `withDefaultNamespace(` | 13 / 8 |
| `new ResourceLocation(` | **0** |
| `"hemomancy"` literal | 75 / 44 |
| `"hemomancy:` literal prefix | 293 / 74 |

There are 60 files with non-canonical Hemomancy forms. They cluster in:

- client models: `VesperMountModel`, `VesperTheEveningStarModel`, 8 summon models, `ExcoriatedModel`, `PhlegethonticBombardierModel`
- packet classes: about 13 under `common/network/**`, for `TYPE`
- dialogue trees (`Harbinger*DialogueTrees`)
- `AttributeInit`, `BiomeInit`, `EffectInit`
- `station/StationUpgradeTier`, `UpgradeStation`
- `recipe/LegacyBloodStructureIds`
- `Hemomancy.java` itself

There is a second namespace constant, `DialogueHubFactory.NAMESPACE = "hemomancy"`. The Javadoc on `Hemomancy.rloc` is garbled; it mentions a "two-arg `ResourceLocation.parse(ns, path)`".

**Magic values (VERIFIED counts):**

- **Degree comparisons:** about 94 raw-int comparisons on degree getters across 58 files. `getPlayerDegreeNumber` accounts for 50 and `getDegreeNumber` for 12. Another 143 compare a local `degree` variable to an int. The most common thresholds are `>=8`, `>=7` and `>=5`. Seventeen classes declare their own degree constants (`REQUIRED_DEGREE` = 2, 3, 4×3 and 5; `MIN_DEGREE` = 5 and 7; `APOTHEOS_DEGREE = 8` twice; `HARBINGER_DEGREE_THRESHOLD = 5`, `DIVE_UNLOCK_DEGREE = 5`, `DEEP_MIN_DEGREE = 5`, `CARVING_DEGREE = 4`, `SILENT_ARCHON_DEGREE = 7`), even though `EnumInitiatoryDegree` and `RecipeDegreeGates` exist.
- **Ticks:** `1200` appears 52 times, `6000` 26, `24000` 25, `getGameTime() % 20` 11 and `20 * 60` 5.
- **Blood:** 116 `static final` constants named `*BLOOD*/*VOLUME*/*COST*/*DRAIN*`. The common values are `500.0`, `100` and `50`.
- **Duplicated constants:** `TAG_STRING = 8` is redefined in 4 files (`DialogueKnowledge`, `LiberKnowledge`, `MemoHelper`, `PreWrittenMemoItem`) even though vanilla has `Tag.TAG_STRING`. `TIME_CYCLE_MS = 240000` appears 7 times (mixed int/float), `VEIN_COUNT` = 28/18/10 across 14 files, and GUI geometry constants such as `W=176` (10 times) are repeated.
- Dedicated constants/keys classes are limited to `HemoCapabilityKeys`, `PhlegethonticTags` and `LegacyBloodStructureIds`.

## 4. Resource tree

**`assets/hemomancy` file counts (VERIFIED):**

| Directory | Files | Directory | Files |
|---|---|---|---|
| `textures` | 2,107 (1,959 PNG) | `shaders` | 206 |
| `models` | 1,652 | `sounds` | 184 + `sounds.json` |
| `blockstates` | 188 | `particles` | 14 |
| `font` | 8 | `book_themes` | 2 |
| `lang` | 1 | `geo` | 1 |
| `animations` | 1 | `entity_geometry` | 1 |
| `mna_guide` | 1 | | |

**`data/hemomancy` file counts (VERIFIED):**

| Directory | Files | Directory | Files |
|---|---|---|---|
| `recipe` | 600 | `dialogue_inquiry` | 353 |
| `loot_table` | 243 | `worldgen` | 201 |
| `advancement` | 192 | `clairaudiograph_sounds` | 157 |
| `tags` | 139 | `books` | 132 |
| `structure` | 108 | `blood_profiles` | 74 |
| `enchantment_affinity` | 42 | `neoforge/biome_modifier` | 41 |
| `discovery_inscription` | 30 | `cardinal_rite_floor` | 19 |
| `blood_injection` | 19 | `damage_type` | 14 |
| `ichorian_sigil` | 9 | `loot_modifiers` | 5 |
| `succession_profession` | 5 | `banner_pattern` | 2 |
| `dimension` | 2 | `dimension_type` | 2 |
| `antecedent` | 1 | | |

Other namespaces: `minecraft` 117, `c` 6, `curios` 2, `forge` 1, `hutoslib` 1, `mna` 1, `neoforge` 1.

**Legacy plural tag directories (VERIFIED present; that they are inert is known 1.21 behaviour, because 1.21 reads `tags/item`, `tags/fluid`).** The `hemomancy` namespace already uses singular directories.

| File | Problem |
|---|---|
| `data/minecraft/tags/items/{walls,small_flowers}.json` | There is no `tags/item/walls.json`, so venous walls are probably missing from `#minecraft:walls` (item). |
| `data/hutoslib/tags/items/knappers.json` | HutosLib declares the tag as `HutosLib.rloc("knappers")`, so `hematic_iron_knapper` is probably missing from the tag. |
| `data/curios/tags/items/{belt,spell}.json` | Lists `hemomancy:harbinger_grimore`, which is not registered. |
| `data/forge/tags/fluids/test_fluid.json` | Old `forge` namespace and a fluid that does not exist. |
| `data/mna/tags/items/dusts/purified_vinteum.json` | Dormant MnA. |

Tag IDs would stay the same if these moved, but **REQUIRES VERIFICATION** in-game.

**Archives and authoring files (VERIFIED):**

- 237 files are tracked under `/old/` directories: `textures/item/old` 186, `gui/old` 23, `block/old` 20, `gui/morphling_overlay/old` 8. They have 0 references from java or json.
- 42 `* - Copy*` files.
- 288 tracked `.bbmodel` files.
- 136 files in `models/entity/bbmodel/imports`.
- 10 `.mp3` files.
- `textures/item/sanguine formation/` is empty, but `build.gradle` still excludes it.

**What the built jar contains** (`build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`, 95.6 MB, inspected with `unzip -l`, VERIFIED):

- **`.cache/` datagen files:** shipped. The exclude `"src/generated/**/.cache"` never matches because exclude patterns are relative to each resource root. HYPOTHESIS: `.cache/**` would work.
- **`/old/` entries:** 203 shipped. These files are only hidden by the specific excludes; the folders are not excluded.
- **Reference image:** `models/entity/bbmodel/reference/pale_intercession_texture_reference.png` (2.6 MB) shipped.
- **Empty `bbmodel/` folders:** shipped.

The `build.gradle` excludes (` - Copy`, `bbmodel/imports`, `.mp3`, `OLD*.ogg`, `ShaderBasicMethods.txt`, `infected_cap.png`, `model_drudge_grey_old (*)`, `gui/old/BORDER.png`) hide those files from the jar but leave them in the repo. That is intentional ("Preserve authoring archives").

**`src/generated`:** 107 files (103 tracked). 46 are shadowed by `src/main`: 45 item models (`spawn_egg_*`) and `lang/en_us.json`. With `DuplicatesStrategy.EXCLUDE` the main copy wins. The generated lang has 1,252 keys, none of which are missing from main's 6,227, but 358 have different values, so generated lang output is effectively dead.

**Texture sample (VERIFIED method):** I sorted the 1,959 PNGs, excluded `/old/`, and took every 50th file, giving 35 files. For each, I ran `git grep -F` on the full stem and the basename across `src/**/*.java` and `*.json`, then checked the zero-hit ones by hand for dynamic path building. 31 of 35 are referenced, including dynamic cases: `_portrait` suffix in `DialogueScreen`, `base_degree_` in `BloodVolumeOverlay`, `diamond_clarity` in `UnstainedGaugeOverlay`, `texture("…")` in `HematicMicroscopeOverlay`, dialogue sprite icons, `morphling_overlay/`, and `mob_effect/` by ID. These four have no reference:

- `block/redstone_dust_overlay.png`
- `entity/blood_shot/model_blood_shot.png`
- `models/morphling/attachment_debug.png`
- `item/humoral_barometer.png` (the item model uses the block model)

That puts the unreferenced share at about 11%, or 4 of 35. HYPOTHESIS: about 200 non-archive PNGs plus the 237 archives repo-wide.

**Lang orphans (HYPOTHESIS; dynamic registration can hide matches):** comparing `item/block/entity.hemomancy.*` keys with `register*("id")` literals leaves 57 item, 11 entity and 7 block keys without a match. Examples: `abyssal_siphon`, `cruor_fiend`, `frozen_clot`, `void_drinker`, `synapse_hound`, `blood_tendency_gauge`, `bloodline_pool_monitor`, `field_notes`, `verdigris_censer`, and the MnA items.

## 5. Codec vs manual JSON (VERIFIED)

- All 10 custom data reload listeners extend `SimplePreparableReloadListener` or the local `JsonResourceReloadListener`, and all of them hand-parse with Gson/`GsonHelper`. None uses a Codec. They are:
  - `DiscoveryInscriptionLoader`
  - `ItemInquiryLoader`
  - `CardinalRiteFloorLoader`
  - `IchorianSigilLoader`
  - `ScriptoriumAffinities`
  - `BloodInjectionData`
  - `BloodProfileData`
  - `SuccessionProfessions`
  - `ClairaudiographCatalogue`
  - `JsonResourceReloadListener` (the base)
- File counts by API:
  - Gson: `JsonObject` 28, `GsonHelper` 9, `JsonParser`/Gson 17.
  - Codecs: `MapCodec` 93, `RecordCodecBuilder` 46, `StreamCodec` 231.
  - Other serialization: `CompoundTag` 330, `FriendlyByteBuf` 267.
- Recipes and loot modifiers use MapCodecs, as 1.21 requires.
- Packets: 182 `CustomPacketPayload` types. 126 use `StreamCodec.of(encode, decode)` with manual `FriendlyByteBuf` I/O, 5 use `StreamCodec.composite` and 1 uses `unit`. There is no `SimpleChannel` (only in comments).

## 6. Mixins

`hemomancy.mixins.json` lists 52 common and 13 client mixins (`defaultRequire: 1`). `mixin/core` holds 67 files. The two not listed are:

- `MixinScreenRenderState.java`: a **0-byte** tracked file.
- `MixinHUDOverlayRenderer`: an empty class with no `@Mixin`. It is referenced only by `hemomancy.mna.mixins.json`, which `neoforge.mods.toml` **never registers** (only `[[mixins]] config="hemomancy.mixins.json"`). It is `JAVA_17`.

| Group | Mixins | Target / purpose |
|---|---|---|
| Accessors/invokers (8) | `GoalSelectorAccessor`, `MobEffectInstanceAccessor`, `ServerPlayerSpawnGraceAccessor`, `SinglePoolElementAccessor`, `VigilTemplateAccessor`, `PelagicMonumentChildrenAccessor`, `PelagicPossibleBiomesAccessor`, `TroupeNoiseGeneratorAccessor`, `CastingHandRendererInvoker` | Field and method access |
| Pelagic worldgen (8) | `MixinPelagic{AquaticVegetation,Monument,ChunkMap,RandomState,BiomeSource,BiomeSearch,NoiseChunk,Carver}` | Ocean deepening and biome search; no event equivalent |
| Phlegethontic ore (3) | `OreFeature`, `ScatteredOreFeature`, `ChunkStatusTasks.light` | Vein completion |
| Antecedent/Vigil (7) | `JigsawStructure`, `Beardifier`, `VibrationSystem.Listener`, `SculkShriekerBlockEntity`, `SculkCatalyst`, `LevelChunk.setBlockState`, `SculkSensorBlock` (`MixinListeningScar`), client `AbstractSoundInstance.getVolume` | Ancient City annex and sculk behaviour |
| Paralysis (6) | `LivingEntity` (`isImmobile`, `jumpFromGround`, `startUsingItem`, `onEffectRemoved`, `travel`), `LivingEntity`+`Mob.doHurtTarget`, `Player.attack/touch`, `Slime.dealDamage`, `ServerGamePacketListenerImpl.handleMovePlayer`, `LocalPlayer.aiStep` | Freeze actions |
| School (3) | `LivingEntity` (`addEffect`, `hurt`, `onEffectRemoved`), `LivingEntity` awareness (`canAttack`, `getVisibilityPercent`, `hasLineOfSight`, `isCurrentlyGlowing`), `Entity.isInvisible` | Combat/stealth |
| Commanded (3) | `Brain`, `Creeper.spawnLingeringCloud`, `ThrownPotion` | Commanded mobs and potions |
| Potion response (3) | `HealOrHarm`/`Regeneration`/`Poison` effect `heal`/`hurt` redirects | Scale by blood response |
| Scriptorium (3) | `ItemStack.hasFoil`, `ItemRenderer` (glint), `ChunkSerializer.read` | Glint plus **legacy chunk migration** |
| Misc | `MixinEntity` (`move`: axonal travel/fold), `MixinAxonalLivingEntity.isPickable`, `MixinBloodResponseMovement` (`canFreeze`/`makeStuckInBlock`), `MixinFishingHook.catchingFish`, `MixinLivingEntity` (swing duration, `updateFallFlying`), `MixinPlayer.causeFoodExhaustion`, `MixinPowderSnowBlock`, `MixinAdvancedBrewingStand.isBrewable`, `MixinBoundPotion.finishUsingItem`, `MixinChamberInventoryDrops` (`AbstractContainerMenu.clicked`) | |
| Client | `CapeLayer`, `EntityRenderDispatcher` (crimson fire), `GameRenderer.pick`, `HumanoidArmorLayer`, `HumanoidModel.setupAnim`, `LivingEntityRenderer`, `LocalPlayer`, `PlayerItemInHandLayer`, `PlayerRenderer.setModelProperties` | Rendering |

**Overlap (VERIFIED):** `LivingEntity` is targeted by 6 mixins and `Entity` by 3. `Player` and `LocalPlayer` have 2 each, and `OreFeature` has 2. `onEffectRemoved` is injected by both `MixinParalysisLivingEntity` and `MixinSchoolDamage`.

**Content coupling (VERIFIED):** 50 of the 65 registered mixins import `common`/`client` content classes. `MixinLivingEntity` imports 13. Some call managers directly: `MixinEntity` calls `AxonalTransductionManager` and `FoldedHallwayManager` by fully qualified name, and `MixinVigilAmbience` calls `AntecedentClientEffects`. Others route through `MixinHooks` and `ClientMixinHooks`.

**Replacement candidates (HYPOTHESIS; the NeoForge 21.1 hooks are named, but each needs a behavioural check):**

| Mixin | Possible NeoForge replacement |
|---|---|
| `MixinBoundPotion` | `LivingEntityUseItemEvent.Finish` |
| `MixinParalysisLivingEntity.startUsingItem` | `LivingEntityUseItemEvent.Start` (cancelable) |
| `MixinParalysisPlayer.attack` | `AttackEntityEvent` |
| `MixinSchoolDamage.hurt` | `LivingIncomingDamageEvent` / `LivingDamageEvent.Pre` |
| `MixinSchoolDamage.addEffect` | `MobEffectEvent.Applicable` |
| `onEffectRemoved` (both mixins) | `MobEffectEvent.Remove` / `Expired` |
| `MixinSchoolAwareness.getVisibilityPercent` | `LivingEvent.LivingVisibilityEvent` |
| `MixinHealOrHarm` / `MixinRegeneration` heal scaling | `LivingHealEvent` (it fires for all heals, so it would need a source filter) |
| `MixinPoisonMobEffect` | `LivingIncomingDamageEvent` with a poison damage type |
| `MixinScriptoriumItemStack.hasFoil` | `DataComponents.ENCHANTMENT_GLINT_OVERRIDE` |
| `MixinVigilAmbience` | `PlaySoundEvent` |
| `MixinPlayerRenderer` / part of `MixinLivingEntityRenderer` | `RenderPlayerEvent.Pre` / `RenderLivingEvent.Pre` |
| `MixinAdvancedBrewingStand` | `RegisterBrewingRecipesEvent` / `IBrewingRecipe` (if it is only about validity) |

These should stay mixins:

- the worldgen, structure, sculk and accessor mixins
- `ChunkSerializer` (migration)
- `MixinChamberInventoryDrops`: AGENTS says menu drops must be rejected before removal
- `MixinParalysisServerInput`
- `GameRenderer.pick`
- `isPickable`
- swing duration

## 7. Compat and config

**Compat (VERIFIED):**

| Integration | Files | Compiled | Status |
|---|---|---|---|
| `jei` | 21 | yes (local jar, `optional` in mods.toml) | Live. `compat/jei/ScreenArea` is unreferenced. |
| `mna` | 36 | **excluded** | Dormant. Core reaches it only by reflection: `BloodManipulation.MNA_MANIP_COMBO_HELPER` → `Class.forName(...)`, with exceptions swallowed. `ManipulationCostLedger:66` calls `HemoMnAConfig.ARCANE_RESONANCE_BLOOD_REDUCTION.get()` on a spec that is never registered (null), wrapped in `try/catch`. MnA assets in core: `geo/`, `animations/`, `mna_guide`, `broken_mana_trapazahedron` OBJ, `data/mna` tag, `arcane_resonance` effect, and the inert `hemomancy.mna.mixins.json`. |
| `curios` | 1 | **excluded** | Dormant. Its comment has 1 TODO. |

**Build drift:**

- `build.gradle` says "MnA has no NeoForge 1.21.1 build yet / Curios pending", but it declares `implementation libs/mna-3.1.11-neoforge.1.21.1.0.jar` and Maven `curios-neoforge`.
- GeckoLib is an `implementation` dependency, but only the excluded `compat/mna` imports it, and its mods.toml dependency is commented out.
- These have **0 imports** in compiled code, so they are dev-runtime only but sit on the compile classpath: AstralSorcery, observerlib, theoneprobe, moonlight, dummmmmmy, buildinggadgets2, worldedit.
- `Hemomancy.forcesLoaded` is written (`isLoaded("forcesofreality")`) and never read. `Hemomancy.instance` is assigned and never read.

**Config (VERIFIED):** `HemoConfig.register()` registers SERVER (97 values), COMMON (7) and CLIENT (20). The style is static mutable `ModConfigSpec.*Value` fields assigned inside a builder method, not the `static final SPEC` pattern. Some field types are fully qualified (`net.neoforged…IntValue`), and `HemoConfigValues.get(value, fallback)` adds null-guards. `HemoMnAConfig` (15) has a `register()` that is never called.

I checked each field with `grep -rlw <FIELD>` across `src/main/java` (excluding its own file) and with in-file getter counts. These are never read by main code:

- `HemoServerConfig.MORPHLING_HUSBANDRY_STAGE_QUOTA`: commented "Legacy … Retained for compatibility", and pinned by a test.
- `HemoClientConfig.RENDER_MORPHLING_FEEDING_ANIMATION`: a test asserts the renderer no longer reads it.
- `HemoMnAConfig`: 5 values (`MANA_TO_BLOOD_RATIO`, `LIVING_THREAD_SET_BONUS_*` ×2, `TRAPEZOHEDRON_EFFECT_RADIUS`, `SANGUILITH_MAX_SUMMONS`), plus the remaining values read only from excluded compat.

Every other value is read directly or through a getter (`enablePelagicWorldgen`, `useTexturedDialogueStyle`, and so on).

## 8. Dead-code candidates

**Method (VERIFIED):** I tokenized all `.java`, `.json`, `.toml` and `.cfg` files under `src/` (comments stripped) plus `build.gradle`. Roots were `Hemomancy` and every class with `@Mod`, `@EventBusSubscriber`, `@JeiPlugin`, `@Mixin` or `@GameTestHolder`. A class counts as live if a live main class, a main resource, the mixin JSON or `build.gradle` names it. References from excluded `compat/mna|curios` code do not count. 80 compiled classes are unreachable. I then confirmed each one with `git grep -w <Class> -- src` and checked registry IDs with `git grep '"<id>"' common/init`. Reflection strings (`DevTestHooks`, `BloodManipulation`) were checked separately.

### SAFE (no registry, resource, reflection or test tie)

- Editor backups in source: `common/block/harbinger/functional/CardinalFocusBlock.java~`, `common/item/harbinger/tool/living/LivingSickleItem.java~`, `common/menu/tile/crafting/StillwaterCondenserMenu.java~`, `common/network/PlaceStructurePacket.java~`. All are git-tracked.
- `mixin/core/MixinScreenRenderState.java` (0 bytes).
- Empty classes: `common/init/TerraBiomeInit`, `common/capability/player/unstained/white_humor/WhiteHumorVolumeEvents`, `common/recipe/RecipeSacrificialBloodCrafting`, `common/event/TendencyWeaponCombatEvents` (comment only).
- Unreferenced and unregistered:
  - client: `client/particle/HitGlowParticle`, `client/render/layer/player/BloodArmorLayer`, `client/render/layer/mob/RenderChitiniteLayer`, `client/render/item/harbinger/CellHandBakedModel`, `client/screen/radial/IRadialMenuItem` and `RadialTextLines`
  - `compat/jei/ScreenArea`
  - common: `common/entity/boss/saint/EnumRejectionEvent`, `common/item/itemhandler/CharmItemHandler`, `common/item/shared/GuideBookTooltipHelper`, `common/capability/.../routing/{BloodlineSource,EquippedGourdSource,LinkedPlayerSource}`
  - old worldgen: `common/worldgen/feature/{AbstractFungusFeature,BigMushgloomFeature,FeatureLogic}`, `worldgen/config/FungusFeatureConfig`, `worldgen/terrablender/TestRegion1/2`
- `Hemomancy.forcesLoaded`, `Hemomancy.instance`.
- `data/forge/tags/fluids/test_fluid.json`: inert path and a fluid that does not exist.
- Packaging only: `.cache/**` and the `bbmodel/reference` PNG in the jar.

### REQUIRES VERIFICATION

- **Orphaned content, where the class, model and texture exist but there is no registry entry:**
  - Mobs: `AbyssalSiphon`, `CruorFiend`, `FrozenClot`, `SynapseHound` and `VoidDrinker`, each with Entity, Model and Renderer (15 classes). Their `SoundInit` `ENTITY_*` sounds are still registered, and their lang keys exist.
  - Items: `LivingPistolItem` (+ renderer), `LivingGraspItem`, `DSDItem`, `BloodTendencyGaugeItem`, `BloodlinePoolMonitorItem`, `WitnessOrganItem` (the `witness_organ` block is registered), `VerdigrisCenserItem`, `FieldNotesItem`, `FieldNotesBookItem`, `{Chitinite,Pests,Tick,Urchin}MorphlingItem`.
  - `ScreenScarPattern`: only `ScreenScarBinderViewer` references it.

  Confirm these are retired rather than planned work.
- **Test-only production logic.** The tests exercise these rule classes, but no main code calls them:
  - `CardinalRiteBrazierSignatureRules`
  - `CardinalRiteStructureConsumptionRules`
  - `MnemonicBlueprintRules`
  - `VialCentrifugeStartupRules`
  - `EndgameBossCombatRules`
  - `RecipeMapQuery`
  - `VesperEveningStarMotion`
  - `VesperWeaponAnimationRules` (loaded by `Class.forName` in a test)
  - `ManipulationTraceLayerCache` → `TendencyTraceStyle`

  Either the call site was lost or the class is dead. Check before trusting those tests as coverage.
- **Datagen providers that are not registered:** `HemoRecipeProvider`, `HemoBlockTagProvider`, `HemoBlockLootTableProvider`, `HemoEntityLootProvider`, `HemoEntityTagProvider`, `HemoFeatureUtils`, `HemoPlacementUtils`, `DataGenParsers` (`@Deprecated` `genCrosslBlockModel`).
- **Deprecated shims:** `InventoryVirtualFieldNotesEvents` and `VirtualFieldNotesButton` (HutosLib alias; a test reads HutosLib source). `@Deprecated` totals 18 across 9 files. Of these, `RitesTabView.minDegree` has 0 callers, `BloodManipulation.performDrudgeAction` has 0 external callers, and `measureInfoPanelHeight` is still used 11 times. The block-method `@Deprecated` tags (`CrimsonFlameBlock`, `ViridSalisTrailBlock`) are vanilla overrides.
- **`MobManipCaster`:** a test asserts that it "still exists".
- **`BlankChamberThemeEffects`:** source tests read it.
- **`SpentTempleInitiation`:** always returns false.
- **Legacy plural tag directories** (section 4) and the 4 unreferenced sample textures.
- **The 237 `/old/` archives:** exclude them from the jar, and leave whether to delete them in the repo to the owner.
- **Unused compile dependencies** (section 7).
- **The two unread config values.** Removing a key from a spec rewrites user config files.

### MUST PRESERVE

- `ItemInit.tome_of_the_unstained`: `@Deprecated(forRemoval=false)`, "Legacy registry alias retained so existing saves do not lose old stacks".
- Migrations that protect old saves:
  - `ScriptoriumLegacyMigration` (`BuiltInRegistries.BLOCK/ITEM.addAlias`) and `MixinScriptoriumChunkMigration`
  - `LegacyBloodStructureIds`
  - `StationUpgradeMigration`
  - `MorphlingMigrationRules`
  - `FungalScarMigrationRules` / `FungalScarMigrationEvents`
- `DevTestHooks`: a reflective gameTest bridge with no Java callers apart from `Hemomancy` and `CommonEvents`.
- `DynastyCastlePieces` / `DynastyCastlePieceProvider`: documented as a dormant regeneration tool, and tests use them.
- All of `compat/mna`, `compat/curios`, `HemoMnAConfig`, the MnA assets, `MixinHUDOverlayRenderer` and `hemomancy.mna.mixins.json`. They are dormant. Repair or remove them only together with a decision to re-enable MnA.
- Debug items `debug_showcase`, `structure_scanner`, `structure_spawner`: registered IDs, WIP tab only, with menu/component/client hooks.
- `MORPHLING_HUSBANDRY_STAGE_QUOTA`: config compatibility.
- Registered sound IDs for the orphaned mobs.

**Comment markers (VERIFIED):** `TODO` 3 (`CuriosPlugin`, `OpenCurvedHornModel`, `OpenBloodGourdModel`), `FIXME` 0, `HACK` 1, "legacy" 41 comment lines (mostly migration notes in `ActiveCardinalRite`, `MemoryWeavingRecipe` and station BEs), "unused" 6, plus 3 `ponytail:` notes. A heuristic scan for more than 10 consecutive code-like `//` lines or code-like `/* */` blocks found **0** blocks. Comment-heavy files such as `QliphothBloomRenderer` (141 `//` lines) are section banners and explanation, not disabled code.

**Repo-root clutter (`git ls-files` / `check-ignore`):**

| Item | Status |
|---|---|
| `bash.exe.stackdump` | **tracked**: SAFE |
| `ClaudeCodexComaprison.md` | **tracked** (28K) |
| `outputs/` | **2 tracked** (`brined-court-house-concepts`) |
| `docs/superpowers` | **32 tracked**, 9.2 MB |
| `Notes.txt`, `bin/` (15M), `run-alpha-journey/` (4.2M), `docs/done` (415M), `docs/output` (29M), `docs/OrchestratorReports`, `docs/antecedent-fresh-draft`, `docs/antecedent-text-export` | untracked and ignored (`/docs/*`) |

`libs/` is untracked (0 files), which matches the dependency-provisioning docs.

## 9. Docs drift (VERIFIED)

1. **Datagen.** AGENTS.md and HEMOMANCY_REFERENCE §300/§350 say `runData` generates only blockstates, models and lang, and that the "server recipe/tag/loot providers remain commented in `DataGeneration.java`". In the code, `DataGeneration` also registers the server-side `CircusPavilionStructureProvider`. The recipe, tag and loot providers are not commented out; nothing references them at all.
2. **Common config.** REFERENCE §3 says "`HemoCommonConfig` is currently an empty reserved common spec". In the code it defines 7 worldgen options (Pelagic, Phlegethontic Nether, Fungal Gardens, Cortical Drift, with their weights).
3. **GeckoLib.** REFERENCE §300 says it uses "local jars for TerraBlender, GeckoLib, and JEI". GeckoLib now comes from Maven (`software.bernie.geckolib:geckolib-neoforge`), and the local-jar line is commented out.
4. **Capability access.** AGENTS says to "read/write through `HemoCapabilityAccess` only". 23 non-registrar files call `getData(HemoAttachmentTypes…)` directly, for example `MuscleMemoryLayer`, `MuscleMemoryHandRenderer`, `VascularStatusScreen`, `MnemonicReliquaryScreen` and `MuscleMemory*Events`.
5. **ResourceLocation style.** `docs/agents/hemomancy-mod-agent.md` says to "Use … `Hemomancy.rloc("path")`". 85 call sites in 60 files use `fromNamespaceAndPath("hemomancy"/MOD_ID, …)` or `parse("hemomancy:…")`.

Also: `build.gradle`'s compat comment ("MnA has no NeoForge 1.21.1 build yet; Curios … pending") contradicts its own MnA jar and Curios Maven dependencies. AGENTS describes `mixin/` as backing `hemomancy.mixins.json`, but a second, unregistered `hemomancy.mna.mixins.json` also exists.

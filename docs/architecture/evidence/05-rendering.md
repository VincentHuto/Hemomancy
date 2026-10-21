# 05 — Rendering, VFX, models, particles, screens: architecture audit

Read-only audit, 2026-10-21, branch `neo-1.21.1` @ `3fae723d0` (clean tree). `H/` = `src/main/java/com/vincenthuto/hemomancy/`. Counts come from grep/wc over the working tree. **VERIFIED** means I checked it directly in source. **HYPOTHESIS** means it is inferred and still needs a runtime or visual check. Animation tracks and per-frame cost belong to other audits; they appear here only where they shape the architecture.

---

## 1. Rendering architecture map

### 1.1 Size of `client/` (VERIFIED)

| Package | Files | Lines | Notes |
|---|---:|---:|---|
| `client/render` | 587 | 73,980 | breakdown below |
| `client/screen` | 221 | 45,645 | `skilltree/` 107 files / 19.7k lines; `item/` 22 / 9.0k; `tile/` 26 / 9.3k; `overlay/` 32 / 4.0k |
| `client/model` | 196 | 42,187 | `entity/` 142, `armor/` 22, `item/` 17, `tile/` 13 |
| `client/player` | 52 | 4,303 | first/third-person poses |
| `client/event` | 27 | 3,754 | `ClientEvents` alone is 1,495 lines |
| `client/particle` | 46 | 2,328 | 11 particle classes, plus data/type/factory triplets |
| `client/data` | 21 | 1,948 | client mirrors of server state |
| others | 26 | ~2.7k | sound, morphling, boss, rite, vein (2 files, 119 lines), item, util |

`client/render` sub-packages:

| Sub-package | Files | Lines |
|---|---:|---:|
| `world/` (top level: 139 files, 21.9k) | 204 | 37,144 |
| ↳ `world/chamberofwill` | 28 | 8,437 |
| ↳ `world/rite` | 24 | 3,869 |
| ↳ `world/floor` | 8 | 2,478 |
| ↳ `world/zone` | 5 | 462 |
| `tile/` (BERs; `harbinger/` is 67 of them) | 76 | 11,471 |
| `entity/` (mob 52, boss 26, npc 19, projectile 18, misc 17, summon 16) | 154 | 9,537 |
| `item/` (BEWLRs) | 67 | 7,180 |
| `layer/` | 59 | 3,970 |
| `outpouring/` | 14 | 2,121 |
| `armor/`, `geometry/`, `scar/`, `shader/` + 4 top-level files | 13 | ~2.4k |

### 1.2 Renderer base classes (VERIFIED)

- Entities: 90 `extends MobRenderer`, 35 `extends EntityRenderer`, 2 `HumanoidMobRenderer`, 60 `extends RenderLayer`, 3 `EyesLayer`. **60 entity renderers are 30 lines or fewer.** They contain only a constructor, a `bakeLayer` call and a texture getter (for example `H/client/render/entity/mob/monster/CruorFiendRenderer.java`).
- BERs: 48 `implements BlockEntityRenderer`. There is no local base class.
- Item renderers: 47 `extends BlockEntityWithoutLevelRenderer`. 57 common item classes implement `H/client/item/HemoClientItemExtensionsProvider`, and `ClientEvents.registerClientItemExtensions` (`ClientEvents.java:1315`) gathers them.
- Local abstract bases: only `AbstractChamberThemeEffects` (9 subclasses), `CircusFacultyModel` (4) and `RadialMenuItem` (2). Everything else is a one-off.
- Focal manipulation VFX follow the documented pattern: 29 `*Geometry` classes in `client/render/world/` expose `static boolean handles(Form)`. `ManipulationVisualRenderer` (769 lines) dispatches to them through 38 `.handles(` calls.

### 1.3 World-render entry points (VERIFIED)

There are two ways a world effect gets drawn:

1. **A central dispatcher.** `ClientEvents.renderLevelLastEvent` (`H/client/event/ClientEvents.java:737-790`) calls about 33 static `render(...)` methods in a fixed order across the AFTER_SKY, AFTER_SOLID, AFTER_CUTOUT, AFTER_TRANSLUCENT and AFTER_WEATHER stages.
2. **Self-subscribing renderers.** These are not ordered relative to (1):
   - `ManipulationVisualRenderer.render` (:158)
   - `OutpouringRenderer` (:45)
   - `OutpouringScreenPasses` (:67)
   - `AvatarFirstPersonRenderer` (:49)
   - `CrossbarHolderTracker` (:44)
   - `AxonalTransductionClient` (:91)
   - `EarthenVeinTravelClientEvents` (:90)
   - `MycophantIntroClientEvents` (:99)
   - `AxonalTravelView` (:52)
   - `common/.../RenderBloodLaserEvent` (registered by hand at `ClientEvents.java:1066`)

Buffers: 53 files call `renderBuffers().bufferSource()` and end their own batches. 21 files use `Tesselator.getInstance()` immediate mode; 6 of those are screen overlays, the rest are world renderers (Chamber, Fane, Qliphoth, sky).

---

## 2. Competing patterns

### P1. RenderType holders: six places, three caching strategies (VERIFIED)

| Holder | `RenderType.create` | Notes |
|---|---:|---|
| `H/client/render/HemoRenderTypes.java` (1,625 lines) | 42 | 7 static finals, 7 LRU caches (`riteTypeCache`, limit 32, :751), about 28 functions that build a **new RenderType on every call** with uniforms captured in the setup lambda (e.g. `infectedPool` :594) |
| `H/common/init/RenderTypeInit.java` (361 lines, `extends RenderType`, **in `common/`**) | 30+ | `energySwirl`/`itemEnergySwirl` also build a new type per call |
| `LuxUmbraRenderTypes`, `ThermalRenderTypes`, `AnimusMortemRenderTypes`, `FerricDuctilisRenderTypes` (`client/render/world/`), `client/render/outpouring/OutpouringRenderTypes` | 2–3 each | **5 copies of one idiom**: `private static float time` + `begin(ticks)` + a TexturingStateShard that sets `HemoTime`/`ColorModulator` and then calls `setUniformDefaults()` |
| `H/client/render/tile/RadiantPortalRendertype.java` | 3 | raw `ShaderInstance` statics; builds a new type per call |
| `ManipulationMaterials` (enum, lazy), `StillArtRings`, `SomaticLoomSeamRenderer:164`, `MorphlingModelAttachment` (×5) | inline | |

Shader-uniform types are handled three different ways:
- (a) build a new type per call, with values in closures;
- (b) key an LRU cache by every parameter, **including `gameTime`**, so it misses every frame;
- (c) keep one mutable holder per seed and update uniforms in place (`CardinalRiteFogType`, :775/:1300).

Pattern (c) is the only one that is both allocation-free and correct. It needs the caller to end its batch per instance, and the code says so in its comments.

**Canonical approach:**
- One client-only `client/render/types/` package.
- One `ClockedShaderTypes.of(name, ShaderHolder, format, flags)` factory with a single shared clock. This replaces the 5 `begin()` holders and keeps every material and shader the same.
- Pattern (c) for every parameterised shader type, written as a "uniform slot" record that holds the values and is read in setup.
- Move `RenderTypeInit` from `common/init` to `client/render`.

**Simplification:** about 250 lines removed from the five holders, one caching idiom instead of three, and `common/` loses one class that extends a client class. **Difficulty:** Low for the holders, Medium for converting (a)/(b) to (c), because callers must end batches per instance. **Visual-identity risk:** none. Shaders, textures and state flags stay byte-identical.

### P2. Duplicate render-state definitions (VERIFIED)

Inside `RenderTypeInit` several entries share one CompositeState (lightning shader, TRANSLUCENT, LEQUAL, NO_CULL, NO_LIGHTMAP):
- **Core** (COLOR_DEPTH_WRITE): `RITE_BOUNDARY_CORE` (:58), `LOOM_EFFECT_CORE` (:151), `FLASK_FLUID` (:174), and `CLAW_SLASH_CORE` (:122, differs only in buffer size 512).
- **Glow** (COLOR_WRITE): `RITE_BOUNDARY_GLOW` (:69), `LOOM_EFFECT` (:162), `INCUBATOR_FLUID` (:185), and `CLAW_SLASH_GLOW` (:111, differs only in buffer size).
- The two `UNSTAINED_*` entries are already aliases (:96/:98).

Separate names do keep batches separate, and that can matter for draw order between systems. **Canonical approach:** two named base states (`FLAT_COLOR_CORE`, `FLAT_COLOR_GLOW`). Keep distinct names only where a batch boundary is intentional, and say so in a comment. **Difficulty:** Low. **Risk:** Low, but check sort order wherever two systems draw in the same frame.

### P3. Encoded-UV material quad: four implementations (VERIFIED)

The school material shaders decode `u = shape*2 + t` and `v = phase*2 + s`. The writer for that encoding exists four times:
- `LuxUmbraGeometry.quad/vertex` (`client/render/world/LuxUmbraGeometry.java:383-404`), with `ribbonPath` (:309-352), `joinWidth` (:354), `ribbon`, `card` and `ground`;
- `rite/RiteSurface.java:115-262`, a double-precision, allocation-free re-implementation of `ribbon`, `card`, `quad` and `ground`, with **its own `joinWidth` (:182)** using the same 0.45 constant;
- `FerricDuctilisGeometry.java:304-341` (`card`, `ribbon`, `quad`). **It uses `floorMod(seed,128)`, where all the others use 127** (:339 vs Lux :392 and RiteSurface :249). HYPOTHESIS: this is an accidental drift. It only changes which seed tile some ferric cards sample.
- `ThermalGeometry.java:426-430`.

`OutpouringBrushes` (`client/render/world/OutpouringBrushes.java`) is already the documented public bridge and delegates to `LuxUmbraGeometry`.

**Canonical approach:** make `LuxUmbraGeometry`'s primitives the single encoder, renamed to something like `MaterialSurface`. Give it two front-ends: the `Vec3`/`PoseStack` API and RiteSurface's allocation-free `Matrix4f` + double API. Both share one `encodeVertex(shape, seed, u, t)`. Ferric and Thermal call into it; their bespoke shapes (fire, frost, plates) stay local.

**Simplification:** about 150 lines, and one place where the UV contract lives. That place should get a JVM test, because 4 shaders depend on it. **Difficulty:** Medium. **Risk:** None if the 127/128 question is settled on purpose. Pick 128 for ferric only if a visual diff confirms it.

### P4. Tube / tendril / ribbon mesh builders: at least 9 (VERIFIED)

Parallel-transport or ring tube builders:
- HutosLib `client/particle/data/TendrilGeometry` (`createTubeQuads`) via `TendrilRenderer`; used by 18 Hemomancy files;
- `client/render/entity/boss/annetta/TendrilTube.java` (274 lines, 8-sided, parallel-transported);
- `client/render/world/chamberofwill/SeamTube.java` (parallel-transported);
- `client/render/entity/misc/ArborMeshEmitter.java:224-297` (parallel-transported);
- `common/rite/sigil/IchorianSigilOrganicGeometry.tubeFrames` (:111);
- `world/floor/FloorMesh.tube` (:1258);
- `NaeglerophaeonModel.tube` (:161);
- the flat 2-quad "tubes" in `VisceralMesh.tube/segment` (:34) and `ManipulationVisualRenderer.tube` (:747).

Ribbon builders: 14 definitions in 9 files. Besides P3, they include `ThreadKnotGeometry.ribbonSegment` (:141), `SanguineTendrilRibbonRenderer.ribbonSide` (:55), `VesperBurrowFissureRenderer.ribbon` (:75) and `AnnettaHeldItemLayer.ribbon` (:128).

**Canonical approach:**
- One pure, Minecraft-free `TubeFrames` helper in `client/render/geometry/`: parallel-transport frames from a centre line, ring sampling, and UV along the arc length. It is JVM-testable.
- Each art style keeps its own emitter (lit textured core, additive glow shell, webbing, bark), so nothing is homogenised.
- Upstream it to HutosLib only if HutosLib's `TendrilGeometry` can take frames from it without changing its look.

**Simplification:** roughly 300–400 lines of frame maths. **Difficulty:** Medium. **Risk:** Low–Medium, because twist and UV continuity must match exactly. Verify with the existing review clients.

### P5. Duplicated scalar and curve maths (VERIFIED)

Static helper definitions found across `client/` and `common/`:

| Helper | Definitions | Files | Examples |
|---|---:|---:|---|
| `clamp*` | 72 | 66 | `clamp01` alone 13×, e.g. `rite/CardinalRiteMath.java:15`, `world/rite/escalation/RiteWorldMath.java:34`, `outpouring/OutpouringMarks.java:178` |
| `smooth*` | 67 | 64 | |
| `smoothstep` | 16 | 15 | |
| `lerp` | 26 | 25 | |
| `hash*` | 13 | 11 | |
| `bezier` | 6 | 6 | `HematicRiposteGeometry:231`, `DeadlyGazeGeometry:236`, `LodestoneFieldGeometry:158`, `PhoenixDebtGeometry:217`, `CardinalRiteCulminationRenderer:627`, `NaeglerophaeonModel:196` |
| `catmullRom` | 4 | 3 | |
| `argbToNativeAbgr` | 4 | 4 | the four `*TraceLayerCache` screens |

`CardinalRiteMath` and `RiteWorldMath` already overlap with each other.

**Canonical approach:** one `common/util/HemoMath` with no Minecraft types (`clamp01`, `smoothstep`, `smooth`, `lerp`, cubic `bezier`, `catmullRom`, `hash`) and one `client/render/ColorUtil` (argb↔abgr, `shade`, lerp). Riposte's "pure rules class" convention still holds, because the rules classes would call `HemoMath`. Leave the easing curves to the animation audit.

**Simplification:** roughly 600–900 lines over time. **Difficulty:** Low per file, but very wide. **Risk:** None, as long as variants with different constants keep their own name (several `smooth` functions are not smoothstep).

### P6. GLSL utility duplication (VERIFIED)

There are 69 `.fsh` files:
- 45 define their own `hash`, 41 their own `noise`, 32 their own `fbm`;
- **none** `#moj_import` a Hemomancy include;
- the only include, `shaders/include/color_sorting.glsl`, is referenced nowhere.

Many hashes are seed-parameterised variants, for example `+PoolSeed*0.013` and `+CeilingSeed*0.193`. At least 7 files carry the identical `hash(vec2)` + value-noise pair (`127.1, 311.7, 43758.5453`).

**Canonical approach:** `shaders/include/hemo_noise.glsl` holding only byte-identical functions, imported with `#moj_import <hemomancy:hemo_noise.glsl>`. Leave seeded variants inline. **Difficulty:** Low–Medium. **Risk:** Low if only exact copies move. Per project memory, NVIDIA compile errors can slip past `glslangValidator`, so prove it with a GPU launch.

### P7. Two school/material taxonomies (VERIFIED)

- `ManipulationMaterials` enum (`client/render/world/ManipulationMaterials.java`): 8 textures on the vanilla `position_tex_color` shader. It decides form→school, and other code reads that decision (`AnimusMortemGeometry:27`, `ManipulationMotes:49`, `ManipulationBloodFormation:40`, `ManipulationVisualRenderer:488,534`, `PuppeteerThreadRenderer:77`).
- `LuxUmbraBatch.Material` (9 shader materials) and `OutpouringBrushes.Mat` (a mirror of it).

`ManipulationAmbientParticles:83` maps between them with `EnumBloodTendency.valueOf(material.name())`.

**Canonical approach:** keep both render paths, because the textured fallback and the shader materials look different. Move the form→school mapping out to `ManipulationVisuals.Form` (common) and make both enums derive from it. **Difficulty:** Low. **Risk:** none.

### P8. HUD: two mechanisms (VERIFIED)

- 15 `registerAboveAll` layers in `ClientEvents.registerGuiOverlays` (:1426).
- 9 other HUD draws through raw `RenderGuiEvent` / `RenderGuiLayerEvent` handlers:
  - `ClientEvents:710/716/721` (Morphic Nectar, White Humor and Sanguine Omen screen grades);
  - `EarthenVeinTravelClientEvents:128`;
  - `ManipulationVisualRenderer.hud` (:657);
  - `AntecedentClientEffects:74`;
  - `FungalProjectionClientEvents:41`;
  - three radial screens that hide vanilla layers.

**Canonical approach:** every persistent HUD element becomes a registered layer, which gives ordering and `/hideGui` behaviour for free. Use `RenderGuiLayerEvent.Pre` only for *suppressing* vanilla layers. **Difficulty:** Low. **Risk:** Low; check that full-screen grades still draw under the chat and over the hotbar as they do now.

### P9. World-render dispatch (VERIFIED)

See §1.3. **Canonical approach:** keep the explicit ordered list, which is a good property, but move it out of `ClientEvents` into a `WorldStageRenderer` with one ordered list per stage. The self-subscribing renderers register into that list instead of subscribing on their own. **Difficulty:** Low–Medium. **Risk:** Low, but draw order changes would show up visually, so keep the current order and add a source-contract test for it.

### P10. Particle data boilerplate (VERIFIED)

Each Hemomancy particle has a Data + Type + Factory triplet: 9 data classes (27–51 lines), 9 types (24 lines each, `client/particle/type/*`, identical apart from generics) and 12 factories. Several data classes are color-only and identical except for the registered type. `BloodCellData` and `SerpentParticleData` differ only at `ParticleInit.blood_cell` vs `ParticleInit.serpent` (diff verified). HutosLib's `ColorParticleData` already takes the `ParticleType` as a constructor argument. All the color data classes send color as a UTF-8 string (`ParticleColor.serialize`).

**Canonical approach:**
- One `ColorParticleOptions(ParticleType<…>, ParticleColor)` with a `MapCodec` factory, plus a generic `SimpleParticleType<T>(codec, streamCodec)` that replaces the 9 type classes.
- Keep the bespoke particle *behaviours* (`AbsorbedBloodCellParticle` 233 lines, `WillAbsorptionGlowParticle` 189).
- `HemoParticleData` (`common/particle`) stays the public facade.
- Move the data and type classes out of `client/particle/` into `common/particle/`, because server code builds them.

**Simplification:** about 500 lines. **Difficulty:** Low–Medium (registration only). **Risk:** none visually. The `HemoParticleData.ember` scale/alpha swap (documented in the class) must be kept.

### P11. Screens: copy-pasted chrome (VERIFIED)

- There is no local base screen: 22 `extends Screen`, 22 `extends AbstractContainerScreen`, 3 `EffectRenderingInventoryScreen`. No custom `AbstractWidget` subclasses; 4 `extends Button`.
- **`drawBorder`:** 20 private copies (e.g. `item/BloodlinePoolScreen.java:579`, `item/LedgerScreen.java:195`, `tile/crafting/VialCentrifugeScreen.java:317`, `tile/functional/SSCScreen.java:358`). They are line-for-line the same as `skilltree/util/ScreenDrawUtils.drawBorder` (:36) apart from the colour constants. Only `UnstainedProgressScreen:1481` delegates to it.
- **`drawSlotBackground`:** 10 copies.
- **`drawVeinTendril` + `renderVeinBackground`:** 16 private copies, about 60–90 lines each (e.g. `item/LedgerScreen.java:212/252`, `item/VascularViewScreen.java:138/188`, `tile/crafting/VialCentrifugeScreen.java:331/374`). Their bodies are the same algorithm, differing in whitespace, variable names and a few palettes (MorphlingJar's green). A shared, batched version already exists: `skilltree/harbinger/VeinBackgroundRenderer` (146 lines, `ColoredRectBatch`), used by 6 screens.
- `ScreenDrawUtils` (621 lines) already has tabs, wrap/truncate, progress bars and cubic traces, but only 24 screen files use it.

**Canonical approach:**
- Every screen uses `ScreenDrawUtils.drawBorder(…, outer, inner)`.
- `VeinBackgroundRenderer` gains a palette parameter (red, green, neural); all 16 private copies are removed.
- Add an `InventoryPanelChrome` helper for slot backgrounds.
- A base `HemoContainerScreen` is optional. The helpers give most of the gain.

**Simplification:** about 1,800–2,200 lines. **Difficulty:** Low. **Risk:** Low. Keep each screen's colours and vein parameter tables. The shared renderer batches its quads instead of calling `fill` each time, which should look the same; confirm with a screenshot.

### P12. Trivial entity renderers (VERIFIED)

60 renderers of 30 lines or fewer. **Canonical approach:** a `SimpleMobRenderer.factory(modelCtor, layer, texture, shadow)` used inside `renderEntities` (in `ClientEvents`). **Simplification:** about 1,200 lines and 60 files. **Difficulty:** Low. **Risk:** none. This one is optional: separate classes cost little and are easy to extend later.

---

## 3. Inventory of duplicated geometry and helpers

| Class | Lines | Role | Overlaps with |
|---|---:|---|---|
| `client/render/world/floor/FloorMesh.java` | 1,431 | cached Cardinal floor mesh (gash, roots, seats, membrane, tube) | `tube` (P4); own `smooth`/`mix` (P5) |
| `client/render/world/chamberofwill/ChamberOfWillRenderHelpers.java` | 1,217 | about 60 static procedural helpers (vessels, capillaries, neurons, nebula, box/plane); 31 users, 13 of them via `AbstractChamberThemeEffects` | `renderBox`/`renderSolidBox`/`renderPlane` overlap HutosLib `HLRenderShapes.renderSizedCube/Panel` (never used by Hemomancy) (HYPOTHESIS: they are close enough to swap) |
| `LuxUmbraGeometry.java` | 405 | canonical material primitives | P3 |
| `rite/RiteSurface.java` | ~270 | allocation-free copy of the above, plus dome and colourDisc | P3 |
| `VisceralMesh.java` | 212 | textured spell-body surfaces (quad, tube, plate, sword, shard) | `quad`, `tube` |
| `CastImpactGeometry.java` | 297 | shared impact shapes, including `sphere` | `sphere` ×5 elsewhere |
| `entity/boss/annetta/TendrilTube.java` | 274 | 8-sided tendril | P4 |
| `entity/misc/ArborMeshEmitter.java` | 391 | arbor tube/bark | P4 |
| `screen/skilltree/util/ScreenDrawUtils.java` | 621 | GUI chrome | P11 (ignored by 20 screens) |
| `screen/radial/GenericRadialMenu.java` | 643 | radial menu with its own vein strips | `RadialVeinBorderGeometry`, plus `lerp`/`clamp` copies |
| `common/block/harbinger/crafting/AlembicGeometry.java` / `ArmatureGeometry.java` | 672 / 726 | in `common/` | not checked for client use (out of scope) |

Vein drawing alone has about 25 independent generators:
- world: `CardinalRiteBoundaryRenderer.drawVeins/drawCapillary`, `QliphothBloomRenderer.drawVeins/drawRootVeins/drawBranches`, `SanguineMonolithRenderer.render*Veins`, `SomaticLoomRenderer.drawVeins`, `BloodCraftRingRenderer.drawVeins`, `RiteZoneBoundaryRenderer.drawVein`, `ArborBarkMeshes.emitVeins`, `BloodMoonVeinSkyRenderer`, the Chamber helpers;
- shader-side: `cast_veins` (`OutpouringRenderTypes`/`CastVeinsLayer`/`VeinEncoding`), `blood_infusion_veins`, `cardinal_floor_membrane`;
- the 16 screen copies.

Most of these are deliberately different art and should **not** be merged. The only real duplication is the screen copies (P11) and the frame and curve maths (P4/P5).

Lightning is already consistent (VERIFIED): every bolt goes through HutosLib `BoltRenderer`/`BoltParticleData`, in 11 files.

---

## 4. Client code inside `common/`

288 `common/` files import `net.minecraft.client`, `com.mojang.blaze3d`, `hemomancy.client` or `hutoslib.client`.

Probably harmless (HYPOTHESIS: safe because the JVM loads lazily; prove with `gameTestServer`/`runServer`):
- **72 files** whose only client import is HutosLib `ParticleColor`, a plain colour value class.
- **57 items** that implement `HemoClientItemExtensionsProvider` and build BEWLRs inside an anonymous `IClientItemExtensions`, such as `common/item/harbinger/QliphothSeedItem.java:51-57`. On a server these load only if the method runs.
- **40 `@OnlyIn`** uses in `common/`, mostly item and entity client methods.

Real misplacements (VERIFIED):
1. `common/init/RenderTypeInit.java`: `extends RenderType` (a client class) and lives in `common`. Move it to `client/render`.
2. `common/init/ShaderInit.java`: a `Dist.CLIENT` subscriber that imports `client.render.shader.*`. Move it to `client/`.
3. `common/init/ParticleInit.java`: holds both the common `DeferredRegister` and a client `RegisterParticleProvidersEvent` handler. That handler calls `Minecraft.getInstance().particleEngine.register` 12 times (:64-75) instead of `event.registerSpriteSet`. Split it: the registry stays in common; the providers move to `client/` and use the event API.
4. `common/capability/player/harbinger/bloodvolume/RenderBloodLaser.java` + `RenderBloodLaserEvent.java`: world renderers in a capability package, using `Minecraft.getInstance()`, `RenderLevelStageEvent` and `RenderTypeInit`. Move them to `client/render/world`.
5. `common/worldgen/feature/FungalSkyBoxRenderer.java`: an `@OnlyIn(CLIENT)` sky renderer with 13 `addVertex` calls. Its only caller is `client/event/FungalRealmsRenderInfo.java:65`. Move it.
6. `common/network/**`: 56 packet files import `hemomancy.client`. 10 call `Minecraft.getInstance()` directly in handlers, for example:
   - `PacketSyncScarsState:60`
   - `PacketEquipmentSync:47`
   - `SyncSchoolCombatS2CPacket:21`
   - `PacketToggleBinderMessage:33`
   - `ToggleMorphlingJarMessagePacket:27`
   - `SyncTrackingAvatarPacket:45`
   - `PacketGourdScarSync:55`
   - `SyncEquipmentLayerVisibilityPacket:51`

   Only 4 packets have a matching `*ClientPacket` handler class. **Canonical approach:** one `client/network/ClientPayloadHandlers` that the S2C `handle` methods delegate to.
7. 9 particle data and 9 particle type classes live in `client/particle/` but `common/` builds them (`SerpentParticleData` ×6, `BloodCellData` ×6, etc.). They do not import client classes, so this is a package-placement problem, not a crash risk.
8. Code calling `Minecraft.getInstance()` in `common/` overall: 52 files, 94 calls. Besides the above, these are item `use`/`inventoryTick` paths with 2 calls each (the `Living*Item` set, shields, Qliphoth items). HYPOTHESIS: guarded by `level.isClientSide`; I did not check each one.

---

## 5. Dead renderers, particles, screens, models and assets

How I checked: for each class, its simple name was grepped as a whole word across `src/main/java` (excluding the file itself) and `src/test`. Event-bus classes (`@EventBusSubscriber`/`@SubscribeEvent`) were then excluded, because they register themselves.

| Item | Confidence | Evidence |
|---|---|---|
| `CruorFiend`, `VoidDrinker`, `SynapseHound`, `FrozenClot`, `AbyssalSiphon`: Entity + Model + Renderer (15 files, about 1,340 lines) | **Verified** | Each chain references only itself. None is in `EntityInit` (case-insensitive grep returns 0). Model `LAYER_LOCATION`s are not registered. Assets exist (5–7 files each). |
| `client/particle/HitGlowParticle.java` (74) | **Verified** | `HitGlowParticleFactory:27` builds HutosLib `ParticleGlow` instead |
| `client/render/layer/mob/RenderChitiniteLayer.java`, `client/render/layer/player/BloodArmorLayer.java`, `client/render/item/harbinger/CellHandBakedModel.java` (77), `client/screen/radial/IRadialMenuItem.java`, `client/screen/radial/RadialTextLines.java` | **Verified** (no main or test references) | |
| `RenderTypeInit`: `LIGHTNING`, `LASER_MAIN_BEAM`, `LASER_MAIN_CORE`, `LOOM_BEAM_CORE`, `LOOM_BEAM_GLOW`, `LOOM_EFFECT_CORE`, `MYCELIAL_LANTERN_GLASS`, `ENTITY_BEAM_RENDER_TYPE`, `firstPersonEnergySwirl`, `itemEnergySwirl` | **Verified** | 0 uses; no static imports |
| `RadiantPortalRendertype.END_PORTAL` (private, unused), `textWithRayShader` (unused, and also mis-named `"textWithWaterShader"`), plus the `ray` core shader it alone registers (`ShaderInit:210`) | **Verified** | |
| `ShaderInit.DISTORTED_TEXTURE` (:21), `SCAR_GLOW` (:31) | **Verified** | registered and compiled on every reload, never used |
| 9 orphan core shader programs: `aoe_gun_projectile`, `lodestone_texture`, `particle/particle`, `radial_menu`, `runic_energy_flow`, `screen/screen_particle`, `shapes/scrolling_texture`, `shapes/scrolling_triangle_texture`, `shapes/triangle_texture` | **Verified** | not in `ShaderInit`; no string reference in Java (64 core JSONs vs 57 registered names) |
| `shaders/post/*` (7 chains) and `shaders/program/*` (21 files; 2 already excluded from the jar in `build.gradle:33-34`) | **Verified** | no `PostChain` or path reference in Java |
| `shaders/include/color_sorting.glsl` | **Verified** | never imported |
| GeckoLib dependency (`build.gradle:439`, `geckolib_version=4.9.2`) | **Verified** | `software.bernie` is used only under `compat/mna/**`, which `build.gradle:78` excludes from compilation. The 2 `geo/` and `animations/` assets belong to that MNA block. The dependency only bloats the dev runtime. |
| `client/model/entity/boss/endgame/VesperWeaponAnimationRules`, `VesperEveningStarMotion`, `client/screen/skilltree/harbinger/ManipulationTraceLayerCache`, `RecipeMapQuery`, `BlankChamberThemeEffects`, `VirtualFieldNotesButton` | **Likely live (test-only)** | referenced only from tests. Find out whether they are kept for test seams or were left behind. |
| `ChamberOfWillRenderHelpers.renderBox`/`renderSolidBox` vs HutosLib `HLRenderShapes`; `HLRenderHelper` unused by Hemomancy | **Hypothesis** | overlap judged from signatures only |

Model approaches (VERIFIED):
- 177 files use `LayerDefinition.create`: hand-written or Blockbench-exported Java. 15 carry a "Blockbench" marker.
- Supertypes: 83 `EntityModel`, 29 `HierarchicalModel`, 29 `HumanoidModel`, 25 `Model`.
- 18 files use vanilla `AnimationDefinition` keyframes.
- 12 item models use `neoforge:obj` (11 `.obj` files), and 12 use `neoforge:composite`.
- About 11 classes use custom cached meshes (`CachedMeshModelView`, `*Meshes`).
- No GeckoLib in compiled code.

---

## 6. Hotspot files

| File | Lines | Why |
|---|---:|---|
| `client/model/entity/boss/endgame/VesperTheEveningStarAnimations.java` | 3,003 | generated keyframe data (animation audit) |
| `client/model/entity/boss/endgame/MycophantModel.java` | 2,857 | boss model plus generator output |
| `client/screen/item/HarbingerAssignmentLedgerScreen.java` | 1,903 | 56 private methods; good candidate for per-section view classes, as `skilltree/shared/*TabView` already does |
| `client/render/world/QliphothBloomRenderer.java` | 1,847 | 4 vein generators, Tesselator immediate mode, own `hashIndex` |
| `client/screen/skilltree/shared/MaterialsData.java` | 1,787 | static catalogue; per AGENTS, its predicates must stay aligned with `MaterialAtlasSpec`, so treat it as data, not code to split |
| `client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java` | 1,716 | Chamber theme; immediate mode |
| `client/render/HemoRenderTypes.java` | 1,625 | P1 |
| `client/screen/skilltree/unstained/UnstainedProgressScreen.java` | 1,524 | shares tab controllers but keeps its PROGRESS tab inline |
| `client/event/ClientEvents.java` | 1,495 | god class: 31 handlers covering ticks, tooltips, login/logout, 3 HUD grades, the world dispatcher, entity/menu/key/model/colour/decoration/item-extension registration and the GUI layers. Split along those lines. |
| `client/render/world/floor/FloorMesh.java` | 1,431 | cohesive; leave as is |
| `client/render/world/CardinalRiteBoundaryRenderer.java` | 1,368 | already uses `CardinalRiteBoundaryGeometry` (30 calls); its boundary family (Unstained 243, Fane 493, Zone 249) is distinct art and should not be merged |
| `client/render/world/chamberofwill/ChamberOfWillRenderHelpers.java` | 1,217 | 60 helpers; split into vessel, neural and nebula files |

### Constraint on any refactor (VERIFIED)

`src/test` has 377 test files that read `src/main/java` source text, and 125 of them target `client/render`. Examples:
- `OutpouringWiringSourceTest`
- `QliphothSeedItemRendererSourceTest`
- `ManipulationVanillaParticleSourceTest`

Any move or rename in P1–P12 must update these source-contract tests in the same change. Treat them as a checklist, not an obstacle.

### Vanilla particle diagnostic (VERIFIED)

- `common/`: 211 `ParticleTypes.` uses in 83 files. Top files:
  - `rite/unstained/UnstainedCardinalRiteEvents` (20)
  - `entity/boss/saint/seraphae/SeraphaeEntity` (13)
  - `HemorathEntity` (9)
  - `VelorumEntity` (7)
  - `HematicArmatureBlockEntity` (5)
  - `PhantasmalEchoEntity` (5)
  - `PutricielEntity` (5)
  - `HematicCollapseGoal` (5)

  Most used: `END_ROD` 42, `SQUID_INK` 18, `CRIMSON_SPORE` 18, `SCRAPE` 13, `REVERSE_PORTAL` 11.
- `client/`: 9 uses in 6 files. The `BLOCK`/`ITEM` crumb particles are legitimate.
- Unstained rites are deferred by design (project memory), and the Saint bosses are the main candidates for the `HemoParticleData` sweep that `MANIPULATION_VISUALS.md` already requires for `common/manipulation/`.

---

### Suggested order

These are low-risk and keep the look exactly:

1. Delete the verified-dead items (§5) and the GeckoLib dependency.
2. P11: screen chrome and veins.
3. P1: holder unification, and move `RenderTypeInit`/`ShaderInit`/`ParticleInit`'s client half out of `common/`.
4. P8 and P9: one HUD path and one world-render path.
5. P10: particle boilerplate.
6. P3 and P5: one UV encoder and a shared math helper.
7. P4 and P6, which need visual verification in the review clients.

# HutosLib: NeoForge 1.20.1 → 1.21.1 Migration Design

**Date:** 2026-04-21  
**Approach:** Option A — phased, build-system first  
**Source state:** Raw 1.20.1 NeoForge (v47.1.65), `net.minecraftforge.*` throughout  
**Target:** NeoForge 1.21.1 (v21.1.172), `net.neoforged.moddev` plugin, Java 21

---

## Phase 1 — Build System

Goal: Get the project loadable in an IDE with the correct toolchain before touching any Java.

### Files to change

| File | Action | Detail |
|---|---|---|
| `gradle.properties` | Rewrite | `minecraft_version=1.21.1`, `neo_version=21.1.172`, `minecraft_version_range=[1.21.1,1.22)`, `loader_version_range=[1,)`, add `parchment_minecraft_version=1.21.1` and `parchment_mappings_version=2024.11.17`, remove `mapping_channel`, `mapping_version`, `jei_version`, update `pack_format_number=34` |
| `settings.gradle` | Rewrite | Match MDK: add `pluginManagement { repositories { gradlePluginPortal() } }` and `org.gradle.toolchains.foojay-resolver-convention` plugin |
| `build.gradle` | Full rewrite | Use `net.neoforged.moddev` v2.0.141; replace `minecraft {}` with `neoForge {}`; remove `fg.deobf`, `reobfJar`, `reobfuscation`; Java toolchain 17→21; update JEI deps to use `compileOnly`/`localRuntime` without `fg.deobf`; remove `srcJar` task (no longer needed) |
| `gradle/wrapper/gradle-wrapper.properties` | Update | Match MDK Gradle wrapper version |
| `src/main/resources/META-INF/mods.toml` | Delete | Replaced by new file below |
| `src/main/templates/META-INF/neoforge.mods.toml` | Create | Based on MDK template; dependency modId `forge`→`neoforge`; add `type="required"`; remove `mandatory`; update `versionRange` to `[${neo_version},)` |
| `src/main/resources/pack.mcmeta` | Update | `pack_format` → `34` |

### Key build.gradle structural changes
- Plugin: `net.neoforged.gradle` v6 → `net.neoforged.moddev` v2.0.141
- `minecraft { mappings ... }` block removed entirely (parchment handled inside `neoForge {}`)
- `neoForge { version = project.neo_version; parchment { ... }; runs { ... }; mods { ... } }` replaces old `minecraft {}` block
- Run configs: `property 'forge.logging.markers'` → `systemProperty`; `logLevel = org.slf4j.event.Level.DEBUG`
- JEI dependency: remove `fg.deobf()`; use `localRuntime` config instead of `runtimeOnly` for optional deps
- Remove `finalizedBy 'reobfJar'` from jar task (no reobfuscation step in new toolchain)

---

## Phase 2 — Import Migration

Goal: Mechanically replace all `net.minecraftforge.*` imports with their `net.neoforged.*` equivalents.

All changes are global find-and-replace across every `.java` file:

| Old import prefix | New import prefix |
|---|---|
| `net.minecraftforge.common.MinecraftForge` | `net.neoforged.neoforge.common.NeoForge` |
| `net.minecraftforge.fml.` | `net.neoforged.fml.` |
| `net.minecraftforge.eventbus.api.` | `net.neoforged.bus.api.` |
| `net.minecraftforge.registries.` | `net.neoforged.neoforge.registries.` |
| `net.minecraftforge.api.distmarker.` | `net.neoforged.api.distmarker.` |
| `net.minecraftforge.network.` | `net.neoforged.neoforge.network.` |
| `net.minecraftforge.common.capabilities.` | `net.neoforged.neoforge.capabilities.` (partial — Phase 3 rewrites the capability system fully) |
| `net.minecraftforge.event.` | `net.neoforged.neoforge.event.` |
| `MinecraftForge.EVENT_BUS` | `NeoForge.EVENT_BUS` |
| `ForgeRegistries` | `NeoForgeRegistries` (where applicable; many registries are now vanilla `BuiltInRegistries`) |

After this phase the project will have correct package names but will still fail to compile due to API-level changes — that is expected.

---

## Phase 3 — API Rewrites

Goal: Fix all breaking API changes file by file until the project compiles.

### Event Bus / Mod Setup
- `FMLJavaModLoadingContext.get().getModEventBus()` → `IEventBus` injected as a constructor parameter (NeoForge passes it automatically to the `@Mod`-annotated constructor)
- `DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> ...)` → remove; use `@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)` on client-only classes or guard with `if (dist == Dist.CLIENT)` inside `@Mod` constructor

### Capabilities (complete rewrite)

The old `CapabilityManager`/`CapabilityToken`/`AttachCapabilitiesEvent` system is gone. New pattern:

```java
// Declaration (replaces CapabilityManager.get(new CapabilityToken<>(){}))
EntityCapability<IKarma, Void> KARMA_CAP =
    EntityCapability.createSimple(ResourceLocation.fromNamespaceAndPath("hutoslib", "karma"), IKarma.class);

// Attachment (in RegisterCapabilitiesEvent)
event.registerEntity(KARMA_CAP, Player.class, (player, ctx) -> new KarmaImpl());

// Access (no Optional — returns null if absent)
IKarma karma = player.getCapability(KARMA_CAP);
```

Files requiring full rewrite: `BannerSlotCapability`, `KarmaProvider`, `KarmaEvents`, `BannerExtensionSlot`, `BannerFinderBannerSlot`.

### Networking (complete rewrite)

`SimpleChannel` / `NetworkRegistry` / `SimpleChannel.messageBuilder` are gone. Each packet class now implements `CustomPacketPayload`:

```java
public record PacketKarmaServer(boolean active, float karma) implements CustomPacketPayload {
    public static final Type<PacketKarmaServer> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("hutoslib", "karma_server"));
    public static final StreamCodec<FriendlyByteBuf, PacketKarmaServer> CODEC = StreamCodec.composite(...);
    @Override public Type<PacketKarmaServer> type() { return TYPE; }
}
```

Registration via `RegisterPayloadsEvent` (replaces `HLPacketHandler.registerChannels()`):
```java
event.registrar("hutoslib").playToClient(TYPE, CODEC, handler);
```

`PacketDistributor.PLAYER.with(...)` → `PacketDistributor.sendToPlayer(player, packet)`  
`PacketDistributor.NEAR.with(...)` → `PacketDistributor.sendToPlayersNear(...)`

Files requiring rewrite: `HLPacketHandler` and every `Packet*.java` file.

### Other API Changes

| Area | Old | New |
|---|---|---|
| `ResourceLocation` | `new ResourceLocation("hutoslib", "path")` | `ResourceLocation.fromNamespaceAndPath("hutoslib", "path")` |
| `ResourceLocation` (single-arg) | `new ResourceLocation("hutoslib:path")` | `ResourceLocation.parse("hutoslib:path")` |
| `ArmorMaterials` | `ArmorMaterials.LEATHER` (enum constant) | `ArmorMaterials.LEATHER` now a `Holder<ArmorMaterial>` — pass as-is, constructor signature of `ItemArmBanner` needs update |
| `BannerPattern` | `new BannerPattern("hutoslib_logo")` | `new BannerPattern()` — id comes from registry key, not constructor |
| `ItemBlockRenderTypes.setRenderLayer()` | Called in `clientSetup` | Removed; override `getRenderTypes(BlockState, RandomSource, ModelData, RenderType)` on the Block class via `IBlockExtension`, returning `ChunkRenderTypeSet.of(RenderType.cutoutMipped())` |
| `RegisterCapabilitiesEvent.register(Class)` | Old registration pattern | Replaced by `event.registerEntity(...)` / `event.registerItem(...)` |

---

## Phase 4 — Resources & Data

Goal: Fix all resource/data file paths and formats broken by 1.21.1 changes.

| Change | Detail |
|---|---|
| Loot table directory | `data/hutoslib/loot_tables/` → `data/hutoslib/loot_table/` (singular) |
| Recipe ingredient type | Any `"type": "forge:..."` → `"type": "neoforge:..."` |
| Tag `forge:` references | In tag JSON files, replace `"forge:..."` entries with `"neoforge:..."` |
| `assets/minecraft/atlases/blocks.json` | Verify format — no breaking changes expected for 1.21.1 |
| Banner pattern tag path | `data/hutoslib/tags/banner_pattern/` — path unchanged, content should be valid |

---

## Success Criteria per Phase

- **Phase 1:** Project syncs in IDE without Gradle errors; run configs appear
- **Phase 2:** No more `net.minecraftforge` symbol-not-found errors (new errors expected from API shape changes)
- **Phase 3:** Project compiles cleanly (`./gradlew build` succeeds)
- **Phase 4:** Game launches, mod loads, no missing resource warnings for hutoslib assets

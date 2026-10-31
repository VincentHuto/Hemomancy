# Hemomancy Architecture Standards

These are the conventions the codebase follows after the 2026 consolidation (steps 1 to 13 in
[CONSOLIDATION_PROGRESS](CONSOLIDATION_PROGRESS.md)). They were promoted from the approved and implemented parts of
[CANONICAL_STANDARDS_PROPOSAL](CANONICAL_STANDARDS_PROPOSAL.md); the proposal keeps the rationale and history, this
file keeps the rules. Every rule names the code to copy and the test that enforces it. When a rule and the code
disagree, the test decides: fix the code, or change the rule and its test together.

Three principles run through everything:

1. **Extend what exists.** Each concern below has one shared mechanism. Use it; do not add a second.
2. **Composition over hierarchy.** Small helpers and records come first. A base class is for a real family that
   shares state and lifecycle (the trial Saints, the jigsaw structures, the mob families).
3. **Persisted identifiers are frozen.** Registry ids, resource paths and JSON folder names, NBT and attachment keys,
   codec field names, payload byte layouts, structure codecs and command syntax do not change in cleanup. Changing one
   is a migration with its own approved decision and test.

Items still waiting for an owner decision are listed in the ledger's *Decisions* table; where a rule below stops short
because of one, it says so.

## 1. Registration

- One `DeferredRegister` per registry type in `common/init/<Thing>Init.java`; holder fields are named after the
  registry path. New registers use the typed registers (`DeferredRegister.Items`, `.Blocks`, `.DataComponents`).
- Hemomancy resource locations come from `Hemomancy.rloc("path")`, never `fromNamespaceAndPath("hemomancy", ...)` or
  `parse("hemomancy:...")`, including payload types, modifier ids and generated model layers
  (`ResourceLocationStyleGuardrailTest`).
- Look up a custom registry entry with `RegistryLookup.byName`; register data components with
  `registerComponentType`; creative-tab entries live in `CreativeTabInit`, whose one WIP list fills the WIP tab.
- An entity type is built under its own registry name (`EntityTypeBuildNameGuardrailTest`).
- Subscribers use `@EventBusSubscriber(modid = Hemomancy.MOD_ID)` or the same with `value = Dist.CLIENT`, never `bus=`.
  The `Hemomancy` constructor adds only mod-lifecycle listeners. Client registration uses the event APIs
  (`registerBlockEntityRenderer`, `registerSpriteSet`, `RegisterClientExtensionsEvent`).
- Every Hemomancy registry id and the creative-tab order are frozen by `FrozenIdentifierGameTests`.
- Tag files live in the singular 1.21 folders (`tags/item`, `tags/block`, `recipe`, `loot_table`, ...)
  (`DataFolderNamingGuardrailTest`).

## 2. Networking

- A payload is a `CustomPacketPayload` record with a static `TYPE` and `STREAM_CODEC`, registered once in
  `PacketHandler` in its real direction (`playToClient` / `playToServer`). Only `AntecedentEffectPacket` is
  bidirectional.
- Adding, removing or re-laying-out a payload bumps `PacketHandler.PROTOCOL_VERSION` and rewrites
  `src/test/resources/network/payload-registry.txt`.
- Send only through the `PacketHandler` facade (`sendToPlayer`, `sendNear`, `sendTracking`, `sendToAll`,
  `sendToDimension`, `sendToServer`). Serverbound claims are checked with `ServerboundGuards`.
- A payload decodes, validates and calls one service method; gameplay lives outside `common/network`. A client-bound
  handler that needs client classes is registered as `ClientPayloadHandlers::dispatch`, its body in
  `client/network/Clientbound*Handlers`.
- One-shot visuals use `ParticleBurstPacket` / `EntityVisualPacket`. Block entities sync through the vanilla update
  packet; mobs through `SynchedEntityData`; player attachments through `PlayerStateSync`.
- Server data that clients mirror is a channel in `DatapackSyncChannels`, sent as one `DatapackSyncPacket` per
  channel on every datapack sync; do not add a sync payload per data set.

## 3. Rendering

- Render types are built once: a static constant, a school material from `SchoolRenderTypes`, or a
  `SeededRenderTypes` holder keyed by seed. Never per call or per frame.
- School and rite material vertices go through `client/render/world/MaterialQuads` (`MaterialQuads.seedTile`, never
  `floorMod(seed, 127)`); Ferric keeps its 128-tile constant until decision V-3b.
- Screen chrome comes from `ScreenDrawUtils` and `VeinBackgroundRenderer` with a per-screen `Style`.
- HUD drawing is a layer in `client/registry/GuiLayerRegistrar`; world-space effects draw from the one ordered
  `client/render/world/WorldEffectDispatcher`.
- A boss's bar names its frame where it is built (`new BossBarBinding(key, BossPresentationIds.FRAME_*, ...)`); the
  frame's art is a `BossBarFrame` in `client/screen/overlay/boss/frame`, drawn by `BossBarFrameRenderer` through the
  shared `BossBarFramePainter`. A boss's opening or phase card is a `BossScene` registered in `BossSceneCatalog` and
  played from the server with `BossScenes.play`; its letterbox, card, camera, HUD hiding and input hold go through
  `BossSceneDirector`, and a boss with no opening state of its own holds a `BossIntroWindow`
  (`BossPresentationBindingTest`).
- Shared shader noise lives in `assets/hemomancy/shaders/include/noise/*.glsl` and is pulled in with `#moj_import`.
- Particle data is built through `HemoParticleData`; colour-only particles use `ColorParticleOptions`, every particle
  type is a `HemoParticleType`.
- Effect visuals reuse the Hemomancy/HutosLib toolkit (glow, cell, tendril, lightning, claw, slash, Outpouring);
  [MANIPULATION_VISUALS](../MANIPULATION_VISUALS.md) is the visual bar. A change that can alter pixels is checked in
  its review client with before and after screenshots.
- Nothing under `common/` names a client class, not even an item: item renderers and armour models are registered from
  `client/item/ItemClientExtensions`, keyed by item class (superclasses included) or, for a renderer-only block
  item, by item id. Other client calls from common code go through `common/util/ClientBridge`.

## 4. Animation

- Easing and interpolation come from `common/util/HemoEasing`; `smoothstep` is the cubic. Never `Mth.smoothstep`
  (quintic).
- A boss replays a synced tick through `common/entity/boss/BossReplayClock` and times moves with `BossMoveTimeline`.
  No boss model, renderer, layer or entity getter adds the partial tick to a synced counter
  (`BossAnimationClockGuardrailTest`).
- Third-person player poses apply through `client/player/PlayerAnimationRegistry.THIRD_PERSON` in its listed order;
  first-person hand renderers keep their pinned `RenderHandEvent` priorities.
- Client animation time comes from `client/render/AnimationTime`; wall-clock animation is listed until decision A-4.

## 5. Gameplay mechanics

| Mechanic | Use |
|---|---|
| Spend a player's blood | `BloodAccounting.pay` (one-off) or `payUpkeep` (recurring) with a named `Source` constant; it records to `BloodFlowLedger` and ends in `BloodVolumeEvents.syncVolume` |
| Projected blood into a block | `ProjectedBloodSink` (default on `IBloodReservoir`) |
| Choose targets | `ManipulationCombatHelper.canHarm(player, target)`, `canHarm(caster, target, Reach.X)` for an older filter's verdicts, `canHarmFor(source, target)` for constructs, summons, projectiles and effect holders |
| Drudge work | `DrudgeActions` |
| Heal reductions | `HealModifier` entries, applied in one stage |
| Flight | `FlightArbiter` grants and revokes |
| Held attribute bonus | `AttributeModifiers.sync` |
| Armour set check | `ArmorSets.isFullSet` |
| Degree and Unstained-stage gates | `ProgressionGates` (never a literal degree) |
| Multiblock placement | `PatternOrigins` |
| Block tickers | `HemoTickers.create` in `getTicker` |
| Resend a block entity | `BlockEntitySync.markAndSync` |
| Earned items that may not fit | `PendingDelivery` (living player, not observational, confirmed insertion) |
| Start a Cardinal Rite | `CardinalRiteActivation.tryStart`; kind behaviour is a `RiteKind` hook |
| Dialogue option events | `DialogueEventRouter` and one `<Npc>DialogueEvents` class per NPC |
| `/hemo` branches | one `<Name>Subcommand` class wired by `HemoCommand` |
| An entity casting a manipulation | `setEntityCast(EntityCasts::x)` in `ManipulationInit` |
| Station operations | in the station's block entity; its menu only offers and dispatches |
| Per-strain Morphling data (tendencies, passive, Cradle, Last Rite, polyp family, HUD art, order) | one row of `MorphlingStrains`; code reads the row, never a `morphling_<strain>` id |

Damage stages (G-1) and wider `Reach` presets (G-2b, F-16) wait on decisions; until then damage listeners keep their
priorities and each site keeps its named preset.

## 6. Persistence and state

| Kind of state | Home |
|---|---|
| Durable per-player progress | An attachment in `HemoAttachmentTypes` (`copyOnDeath`), read and written through `HemoCapabilityAccess` |
| Persisted cooldowns | An entry in `PlayerCooldowns` (resets on death and End return), unless part of a system's own attachment |
| Per world | A `SavedData` with `Factory` + `get` |
| Per block entity | Block-entity NBT; blood only in the `block_blood_volume` attachment (white humor in `block_white_humor_volume`) |
| Per item | A typed data component from `DataComponentInit` for new data (O-5 waits on a decision for reworked items) |
| Session-only server state | A static UUID-keyed map registered with `TransientServerStateEvents` |
| Client caches | Cleared in `ClientSessionEvents` on logout |

- Player accessors return the attachment itself (no `Optional`). Owner sync goes through the `PlayerStateSync`
  table, sent by `HemoCapabilityRegistrar` on login, respawn and dimension change; changes outside the senders raise
  the attachment's dirty flag for the end-of-tick flush.
- Old player data is rewritten once, at login, by an entry in `PlayerDataMigrations` gated by `PlayerDataVersion`
  (next version, a sunset date, an old-format fixture in `player_migration_validation`). Never rename keys inside an
  existing attachment. A new `getPersistentData()` call needs an entry in
  `src/test/resources/architecture/persistent-data-allow-list.txt`.
- An owned entity's synced owner UUID is a `SyncedOwner`. A block entity that syncs extends `HemoBlockEntity` or
  `HemoContainerBlockEntity`. A save from before the attachment-only rule still carries a block entity's own blood
  key, which is read only when present.

## 7. Events and lifecycle

- Where order matters (damage, death, heal) use explicit priorities and a documented stage table; never rely on
  class-scan order. The heal stage table: `HIGH` `PurityGainEvents.onLivingHeal` reads the requested heal, `NORMAL`
  `ManipulationStatusEvents.onLivingHeal` applies every `HealModifier`.
- Hot tick paths check cheap conditions first and use `getExistingData` / `hasData`.
- Player lifecycle goes through `PlayerStateSync` and `TransientServerStateEvents`, not per-system handlers.

## 8. Data-driven content

- JSON for catalogues designers or datapacks extend (rites, floors, sigils, blood profiles, inquiries, affinities);
  Java for behaviour.
- Server data loaders extend `common/data/JsonResourceReloadListener`, given a `Codec` (or a parser when a
  definition checks itself against its id), and are registered in `Hemomancy.onAddReloadListeners`. Loaders that
  validate a whole snapshot across files are listed with their reason.
- Recipe codecs are `RecordCodecBuilder` codecs or the one JSON bridge, `RecipeCodecJson.bridge`. A recipe that only
  stations read extends `StationRecipe` (`Recipe<RecipeInput>`); `CustomRecipe` is for real crafting-grid recipes.
- Template-pool structures extend `HemoJigsawStructure` (the one jigsaw codec, same six field names); a structure
  with its own start overrides `findGenerationPoint` and still places through `addPieces`. Authored buildings stay
  NBT; procedural geometry stays code. Every pooled template is committed, apart from the listed planned ones.
- Datagen runs only the blockstate, item-model and Circus pavilion providers; language, recipes, tags and loot are
  authored. A generated file with an authored twin is deleted.
- Player-facing English lives in `en_us.json`: no `Component.literal` shows English text, whether a plain string, a
  concatenated piece, a ternary branch or a `String.format` pattern (`HardCodedEnglishGuardrailTest`). Text with
  arguments is one key with `%s` placeholders, `Component.translatable(key, args)`, or `HemoText.translatable` when an
  argument is not a component, number, boolean or string (those cannot be sent to the client). Keep a message whole
  rather than appending fragments; an argument keeps its own style. Keys follow the class that shows them:
  `screen.hemomancy.<screen>.*`, `gui.hemomancy.<class>.*`, `tooltip.hemomancy.<item>.*`, `message.hemomancy.<class>.*`,
  `commands.hemomancy.<branch>.*`. A helper that shows text takes a `Component`, never a `String` it wraps in a
  literal, and an optional phrase is a key or `Component.empty()`, not an English string argument.
  `TranslatableContents` rejects any other argument type in its constructor, so a `BlockPos`, `UUID` or enum argument
  throws where the component is built. Tests read English through `support/EnglishText` and
  `SourceAssertions.assertShowsEnglish` (or `assertNotShowsEnglish` for English that must be gone).
- The same holds for English that never reaches a literal call: names and descriptions in data tables, enum display
  names and the reasons rule methods return are keys too (an id-keyed table derives them, such as
  `material.hemomancy.<id>` or `blood_source.hemomancy.<id>`, or reuses the item, block or entity name). A row label
  the client resolves is a key, never English. Text from datapack JSON (rite names, sigil names) is shown with
  `Component.translatableWithFallback(key, jsonText)`, so a language file can override it while datapacks keep working.
  English that no player reads (validation and decode errors, debug tooling, config comments, model labels, saved
  default names) is counted per file in `architecture/english-literal-allow-list.txt`
  (`EnglishLiteralAllowListTest`).
- JSON folder names never change.

## 9. Naming

For new code (no mass renames): `*Init` registers, `*Events` holds subscribers, `*Rules` is pure and testable,
`*Manager` owns session or world runtime state, `*Helper` is a family's stateless utility (general ones go in
`common/util`), `*Packet` is a payload, and vanilla vocabulary for block entities, menus, screens, renderers and
models. Log through `Hemomancy.LOGGER` or a class logger, never `System.out`.

## 10. Packages

- `common/` and `client/` by function, with `harbinger` / `unstained` / `shared` where both paths exist. A class
  lives with what it is (block, item, block entity, entity, payload, attachment, client state) or the function
  package it serves; a feature gets a named sub-package there (`entity/npc/circus`, `network/succession`,
  `world/vein`, `mission/circus`), never a top-level folder of its own. `common/rite` and `common/manipulation` are
  the two systems large enough to own a top-level package; their blocks, items and payloads still live in the
  function packages. The top-level packages are pinned by `PackageLayoutGuardrailTest`.
- `common/encounter/` holds boss and arena managers (the Chamber of Will, Chamber visits, Fungal Gardens travel,
  Mycophant, Vesper and Vagrant Mind encounters) and their rules; `common/worldgen/` holds only generation, with
  no loose top-level classes: `biome/` (TerraBlender regions and surface rules), `terrain/<region>/` (the Pelagic,
  Cortical Drift and Phlegethontic terrain systems), `feature/` (with `config/` and per-feature helper folders),
  `carver/`, `structure/` (with a folder for each multi-class structure) and `village/`.
- `common/network/` holds payloads, `PacketHandler`, `ServerboundGuards` and three listed helpers.
- `client/registry/` holds client registration, one class per concern; `client/network/` the client halves of
  payloads; `client/item/` the item client extensions.
- `client/**` holds nothing the server needs; `common/**` names no client class.

## 11. Testing and tooling

| What you are proving | Test kind |
|---|---|
| Pure logic, maths, codecs | JUnit 5, no Minecraft bootstrap |
| Data contracts | A `*DataTest` or resource test reading through `support/ResourceReader` |
| Behaviour in a world | A NeoForge GameTest |
| Visuals | A review client with before and after screenshots |
| Save compatibility | An old-format fixture loaded and asserted |

- New source-text assertions are for architecture guardrails only, through the `support/` helpers
  (`RepoPaths`, `SourceAssertions`, `ResourceReader`); no private `read` / `assert*` copies. Convert a legacy `main()`
  test to JUnit when you touch it.
- GameTests are found only by the annotation scan: every test or generator class carries a `@GameTestHolder`, and
  every namespace is declared in `gradle/game-tests.gradle`. A test that builds outside its own area or counts
  entities over a wide box runs in its own batch and clears what it built.
- Review, observer and profile clients are rows in `gradle/review-clients.gradle`; multi-client launchers live in
  `tools/review/`. `build.gradle` declares only the base runs.
- Dev-only mods that no class imports are `localRuntime` dependencies.
- `./gradlew.bat test` is green before a change and `alphaCheck` green after it.

## Guardrail index

Every architecture guardrail, by area, with the step that added it. `ArchitectureStandardsIndexTest` checks that each
test under `src/test/java/.../architecture/` is listed here.

| Area | Guardrail | Step | Protects |
|---|---|---|---|
| Registration | `RegistrationStyleGuardrailTest` | 8, 13 | Two subscriber forms, lifecycle-only constructor listeners, event-API client registration, registry lookups, typed components, one WIP list; GameTests registered only by annotation scan |
| Registration | `EntityTypeBuildNameGuardrailTest` | 13 | Entity types built under their registry names (F-26) |
| Registration | `ResourceLocationStyleGuardrailTest` | 13 | Every hemomancy resource location comes from `Hemomancy.rloc` |
| Networking | `ClientBoundPayloadRegistrationTest` | 1 | Client-handled payloads registered once, client-bound |
| Networking | `PayloadDirectionTest` | 4 | Sends match registered directions; one bidirectional payload |
| Networking | `PayloadProtocolVersionTest` | 4 | Payload set pinned to `PROTOCOL_VERSION` |
| Networking | `NetworkClientIsolationTest` | 4 | No client code in `common/network` |
| Networking | `SendFacadeTest` | 4 | Only `PacketHandler` and `DialogueSender` call `PacketDistributor` |
| Networking | `ServerboundGuardsCoverageTest` | 4 | Every serverbound payload uses `ServerboundGuards` or is listed |
| Networking | `NetworkPackageGuardrailTest` | 11 | `common/network` holds payloads and listed helpers only |
| Networking | `ReloadListenerGuardrailTest` | 13 | Loaders on the shared base, one registration place, one datapack sync channel table |
| Client isolation | `WillEntityClientIsolationTest` | 1 | `WillEntity` names no client class |
| Client isolation | `CommonClientIsolationTest` | 8, 13 | Nothing under `common/` names a client class; dispatched payloads have one client handler |
| Client isolation | `ClientSessionCacheTest` | 8 | Entity-id client caches cleared on logout |
| Rendering | `ScreenChromeGuardrailTest` | 10 | No private screen chrome copies |
| Rendering | `RenderTypeCachingGuardrailTest` | 10 | Render types built once |
| Rendering | `HudAndWorldDrawingGuardrailTest` | 10 | HUD layers and the one world-effect dispatcher, in their order |
| Rendering | `MaterialQuadGuardrailTest` | 10 | Material vertices through `MaterialQuads` |
| Rendering | `ShaderNoiseIncludeGuardrailTest` | 10 | Shared shader noise in includes |
| Rendering | `ParticleDataGuardrailTest` | 10 | Particle data classes and types |
| Animation | `EasingCopyGuardrailTest` | 6 | No private easing copies |
| Animation | `BossAnimationClockGuardrailTest` | 10 | Boss replay clocks |
| Animation | `PlayerAnimationGuardrailTest` | 10 | Pose registry and hand-renderer priorities |
| Rendering | `BossPresentationBindingTest` | boss presentation | Every boss bar names a frame and sends it on show; played scenes are named ids |
| Animation | `AnimationTimeGuardrailTest` | 10 | Animation time through `AnimationTime` |
| Mechanics | `SmallUtilityGuardrailTest` | 6 | No copies of the shared small helpers; no literal degree comparisons |
| Mechanics | `BloodAccountingGuardrailTest` | 7 | Player blood paid only through `BloodAccounting` |
| Mechanics | `CombatTargetingGuardrailTest` | 9 | Monster gathers ask `canHarm`; one harm predicate |
| Mechanics | `HealAndFlightGuardrailTest` | 9 | One heal stage; flight only through `FlightArbiter` |
| Mechanics | `RiteKindGuardrailTest` | 11 | Rite kinds as `RiteKind` hooks; rites built only by `CardinalRiteActivation` |
| Mechanics | `DialogueRoutingGuardrailTest` | 11 | One `DialogueEvent` subscriber and its route table |
| Mechanics | `HemoCommandStructureGuardrailTest` | 11 | `/hemo` one root, one class per branch |
| Mechanics | `EntityCastGuardrailTest` | 11 | Entity casts as registration hooks |
| Mechanics | `StationMenuGuardrailTest` | 11 | Menus never spend blood, record practice or enchant |
| Mechanics | `HeartNodeFaneSeparationGuardrailTest` | heart-node | A heart-node and a Founding Fane never read each other's saved data |
| Mechanics | `MorphlingStrainTableGuardrailTest` | Brined Court 1 | Strain ids named only by `MorphlingStrains` and `ItemInit`; the deleted first-attempt strains leave no trace in `src/` |
| Content | `BossFrameworkGuardrailTest` | 13 | Boss bars through `BossBarBinding`; all four Saints share `SaintTrialEntity` and its one ending (loot roll, tomb restore); a sarcophagus never picks a Saint at random |
| Content | `ContentBoilerplateGuardrailTest` | 13 | `SyncedOwner`, `HemoContainerMenu`, `ProtectedNpc`, `ModelShapes.rotate`, effect categories |
| Content | `ContentFamilyGuardrailTest` | 13 | Mob families, tracking orbs and combat-weapon storage share their base |
| Content | `HardCodedEnglishGuardrailTest` | 13 | No literal shows English; every key the code names is in `en_us` |
| Content | `EnglishLiteralAllowListTest` | 13 | Exact counts of the English literals no player reads, per file |
| State | `AttachmentAccessTest` | 5 | Attachments through `HemoCapabilityAccess` |
| State | `SessionStateRegistryTest` | 5 | Session maps registered with `TransientServerStateEvents` |
| State | `PlayerStateSyncTest` | 5 | Lifecycle resends only from `HemoCapabilityRegistrar` |
| State | `PendingDeliveryRegistryTest` | 5 | Only `PendingDelivery` counts insertions |
| State | `PlayerDataMigrationGuardrailTest` | 12 | Migrations run only from the table |
| State | `PersistentDataAllowListTest` | 12 | Exact `getPersistentData()` counts per file |
| State | `CooldownTierGuardrailTest` | 12 | Cooldowns keep their tier |
| State | `BlockEntityBaseGuardrailTest` | 13 | Update packet only in the block-entity bases |
| Data | `DataFolderNamingGuardrailTest` | 13 | Singular 1.21 data folders |
| Data | `GeneratedResourceShadowGuardrailTest` | 13 | No shadowed generated files; three datagen providers |
| Data | `RecipeSerializerGuardrailTest` | 13 | One recipe JSON bridge; station recipes off `CustomRecipe` |
| Data | `JigsawStructureGuardrailTest` | 13 | One jigsaw codec and placement call |
| Packages | `EncounterPackageGuardrailTest` | 13 | Encounter managers in `common/encounter` |
| Packages | `PackageLayoutGuardrailTest` | â€“ | Function-first top-level packages under `common/` and `client/`; no feature folders |
| Tooling | `BuildRunTableGuardrailTest` | 13 | Base runs only in `build.gradle`; both run tables applied |
| Tooling | `RepositoryTextEncodingTest` | 2 | Strict UTF-8 across sources, docs and tracked tools |
| Tooling | `PrivateTestHelperRatchetTest` | 2 | Private test helper copies only fall |
| Tooling | `SourceTreeHygieneTest` | 3, 5 | No empty sources or subscribers; mixin list matches |
| Rendering | `HemorathPresentationGuardrailTest` | 10 | Boss and attack goals emit no vanilla particles; Ferric links and blood effects use the shared world passes |
| Rendering | `PutricielPresentationGuardrailTest` | Putriciel | Bespoke rot-fire offering geometry, no vanilla fight particles, and pyres burning the Flammeus shader fire (`PyreFlames`) |
| Rendering | `VelorumPresentationGuardrailTest` | Velorum | Shared rime and shadow pipeline; no vanilla fight particles |
| Rendering | `SeraphaePresentationGuardrailTest` | 10 | No vanilla particles in the saint, fragments or anchors; dedicated radiance uses the shared Lux batch |
| Tooling | `ArchitectureStandardsIndexTest` | 13 | Every guardrail above is listed |

Behaviour and data tests that lock in consolidation work, beyond the guardrails: `LumpDonationValidationTest` (1),
`SourceAssertionsTest` and `LegacyMainTestSuiteContractTest` (2), `ServerboundGuardsTest`, `VisualPayloadCodecTest`
and `BloodlineChatRateLimitTest` (4), `PendingDeliveryTest` (5), `HemoEasingTest` and `ProgressionGatesTest` (6),
`BloodPaymentRulesTest` (7), `SeededRenderTypesTest`, `MaterialQuadsEncodingTest`, `BossReplayClockTest`,
`PlayerAnimationRegistryTest`, `ColorParticleOptionsTest` and `VeinBackgroundStyleTest` (10),
`ActiveCardinalRiteNbtLayoutTest` and `HemoCommandTreeTest` (11), `PlayerDataMigrationsTest` (12),
`StructureTemplateCoverageTest` (13). GameTests: `NetworkDefectGameTests` and `ServerboundGuardsGameTests` (4),
`SessionStateGameTests` and `OwnerSnapshotGameTests` (5), `AttributeSyncGameTests` and `PatternOriginGameTests` (6),
`BloodAccountingGameTests` (7), `FrozenIdentifierGameTests` (8), the `combat_targeting_validation` namespace with
`HealModifierGameTests` and `FlightSourceGameTests` (9), the `rite_kind_validation`, `dialogue_route_validation`,
`hemo_command_validation`, `entity_cast_validation` and `scriptorium_operation_validation` namespaces (11),
`player_migration_validation` (12), `RegistrationConsistencyGameTests` and `BlockBloodPersistenceGameTests` (13).
`verifyPackagedClientResources` and `verifyGameTestResourceLog` run in `alphaCheck`.

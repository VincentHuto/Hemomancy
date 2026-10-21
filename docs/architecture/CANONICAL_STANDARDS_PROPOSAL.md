# Hemomancy Canonical Standards Proposal

Status: **proposal**, 2026-10-21. Nothing here is enforced yet. Once approved, the
short version becomes `docs/architecture/ARCHITECTURE_STANDARDS.md`, and `AGENTS.md`
links to it.

Evidence for every claim is in [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md); finding
IDs such as `G-3` refer to it.

## How to read this

Each standard states five things:

- **Preferred pattern**: what new code should do.
- **Rationale**: why this fits Hemomancy specifically.
- **Existing example**: the best current code to copy.
- **Migration targets**: which current code should move to it.
- **Exceptions**: when an alternative is justified.

Three principles run through all of it:

1. **Extend what exists.** In nearly every area, Hemomancy already has a good shared
   mechanism that part of the code ignores. The standard is usually "use that one".
   It is rarely "build a new one".
2. **Composition over hierarchy.** Small helpers and records are preferred to
   universal base classes. A base class is justified only for a real family that
   shares state and lifecycle, such as the three Saints.
3. **Persisted identifiers are frozen.** Registry IDs, resource paths, NBT keys,
   codec field names, payload byte layouts and structure codecs do not change
   during cleanup. A change to any of them is a migration, which needs its own
   approval and its own test.

---

## 1. Registration

**Preferred pattern**
- One `DeferredRegister` per registry type, in `common/init/<Thing>Init.java`.
- Holder fields are `snake_case` and named exactly after the registry path.
- Use NeoForge's typed registers (`DeferredRegister.Items`, `.Blocks`,
  `.DataComponents`) for new registers.
- Every resource location comes from `Hemomancy.rloc("path")`.
- Look up a custom registry entry through the registry itself (`REGISTRY.get(rloc)`).
  Do not scan a list.

**Rationale**
- Registration is the first thing a contributor reads.
- 1,544 call sites already use `rloc`. snake_case holders are already the majority
  in gameplay registries.
- `ScarInit` already uses registry lookup. `ManipulationInit` and `StillArtInit` scan
  lists instead and fail in different ways: one returns null, the other returns `BLANK`.

**Existing example**
- `ScarInit.getByName`
- `DataComponentInit` for registry shape, but not its builder chains

**Migration targets**
- The 85 hand-built resource locations (D-2).
- `ManipulationInit.getByName` and `StillArtInit.getByName` (R-3).
- The 14 data-component builder chains (R-7).
- The creative-tab lists, out of `Hemomancy.java` (R-2).

**Exceptions**
- The UPPER_SNAKE holders in sound, data-component, fluid, villager and worldgen
  registries stay as they are. Renaming them gains little and churns many tests.
  New entries copy the style of the file they are added to.
- The split into four item registers and ten block registers stays. Datagen and
  creative ordering depend on it.

## 2. Networking

**Preferred pattern**

```java
public record ExamplePayload(int entityId, float amount) implements CustomPacketPayload {
    public static final Type<ExamplePayload> TYPE = new Type<>(Hemomancy.rloc("example"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExamplePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ExamplePayload::entityId,
                                  ByteBufCodecs.FLOAT, ExamplePayload::amount,
                                  ExamplePayload::new);
    @Override public Type<ExamplePayload> type() { return TYPE; }

    public static void handle(ExamplePayload msg, IPayloadContext ctx) {
        // server-bound: validate with ServerboundGuards, then call ONE service method
        // client-bound: forward to a static method in client/**
    }
}
```

- **Direction.** Register each payload once in `PacketHandler`, in its real
  direction: `playToClient` or `playToServer`.
- **Bidirectional is the exception.** `playBidirectional` is only for payloads that
  really travel both ways, and each one is documented.
- **Versioning.** `PacketHandler` declares a `PROTOCOL_VERSION`. Bump it whenever any
  payload's layout or the payload set changes.
- **Sending.** All sending goes through `PacketHandler` methods:
  - `sendToPlayer` and `sendToServer`
  - `sendNear(level, pos, radius, p)`
  - `sendTracking(entity, p)`
- **Packets stay thin.** A packet decodes, validates and delegates. Gameplay lives in
  `rite/`, `tile/`, `entity/` or `manipulation/`, never in `network/`.
- **One visual payload.** Server-to-client visuals use one data-driven payload with an
  enum kind, either `ManipulationVisualPacket` or the new `ParticleBurstPacket` and
  `EntityVisualPacket`.
- **Block entities.** Use the vanilla update packet. Do not add a custom payload.
- **Mobs.** Use `SynchedEntityData`.
- **Player attachments.** Sync through the attachment sync registry (§6).

**Rationale**
- Hemomancy is a multiplayer mod with blood economies, bloodline pools and rites.
  Server-bound payloads are its cheat surface.
- One shared validation layer gives every new payload checks such as `finite`,
  menu ownership and reach. Today each payload remembers them or doesn't (F-1, F-6, F-8).
- A real direction keeps client-only handlers off the server (F-2).
- A protocol version turns a confusing decode crash into a clear handshake refusal (F-10).

**Existing example**
- `CastingAnimationPacket` for payload shape.
- `ClairaudiographActionPacket` for menu actions: container ID, version, enum and index.
- `ManipulationVisualPacket` for VFX.
- `RiteZoneSync` and `RiteSpaces` for send-only-on-change.

**Migration targets**
- The 32 payloads mis-registered as bidirectional (N-1).
- The 8 `common/network` files with client imports (N-3).
- The 28 direct `PacketDistributor.sendToPlayer` calls (N-4).
- The gameplay inside `BloodCraftingKeyPressPacket`, `PacketLedgerAction` and
  `PlaceStructurePacket` (N-8).
- The 6 to 8 single-purpose VFX payloads (N-6).
- Payload style (N-2): **convert only payloads you are already touching.** Keep the
  byte layout. For example, `writeInt` becomes `ByteBufCodecs.INT`, not `VAR_INT`.

**Exceptions**
- `AntecedentEffectPacket` may stay bidirectional, with a comment.
- Bespoke-geometry VFX payloads keep their own types.
- Simple menu buttons with no data use `AbstractContainerMenu.clickMenuButton`.

## 3. Rendering

**Preferred pattern**
- **Render types** live in `HemoRenderTypes` or one per-school holder. They use a
  single caching pattern: a per-seed holder that is built once. Never build a
  `RenderType` per call or per frame.
- **Geometry** that encodes material UVs for school shaders goes through one shared
  quad encoder.
- **Screen chrome** goes through `ScreenDrawUtils` and `VeinBackgroundRenderer`, each
  taking a palette parameter.
- **HUD drawing** registers through `RegisterGuiLayersEvent`.
- **World-space effects** register with the ordered world-effect dispatcher, which
  moves out of `ClientEvents` (R-5).
- **Effect visuals** reuse the existing toolkit through public bridges:
  - Hemomancy particle data
  - glow, cell, tendril, lightning (HutosLib `BoltRenderer`), claw and slash effects
  - Outpouring (`OutpouringBrushes`)

  `docs/MANIPULATION_VISUALS.md` stays the visual standard.
- **Client code location.** All client code lives under `client/**`. Common code may
  reference a client class only inside a client-extension lambda.

**Rationale**
- Hemomancy's identity is its visuals. The goal is to keep the art distinct while
  sharing plumbing: caching, encoding, chrome and dispatch.
- Today the plumbing is copied: 20 border helpers, 4 quad encoders, 3 caching
  strategies. So a fix or optimisation lands in one copy only.

**Existing example**
- `OutpouringBrushes` (public bridge)
- `VeinBackgroundRenderer` (batched shared chrome)
- HutosLib `BoltRenderer`

**Migration targets**
- V-1 (screen chrome)
- V-2 (render-type caching)
- V-3 (quad encoder)
- V-5 (byte-identical shader noise into an include)
- V-6 (colour particle data)
- V-7 (HUD and world dispatch)
- V-8 (client code out of `common/`)

**Exceptions**
- These stay bespoke art: `FloorMesh`, the rite boundary renderers,
  `QliphothBloomRenderer`, each tube family's profile and motion (V-4), and seeded
  shader-noise variants.
- Any change that could alter pixels needs a check in the matching review client, with
  before and after screenshots.

## 4. Animation

**Preferred pattern**
- **Easing and interpolation** come from one `HemoEasing` class, seeded from
  `CardinalRiteMath`:
  - The cubic smoothstep `t*t*(3-2t)`.
  - `lerp(a, b, t)`, with that argument order.
  - `clamp01`, `pulse` and crossfade.
  - **Never substitute `Mth.smoothstep`.** It is quintic in 1.21.1.
- **Boss animation** replays a server-authored move or start tick on a shared client
  replay clock, promoted from `VesperAnimClock`. The controller owns the timeline,
  `BossMoveTimeline` (renamed from `VesperMoveTimeline`).
- **Simple mobs** sync a state through `SynchedEntityData` and start vanilla
  `AnimationState`s on the client, as Eruptus does.
- **Player animations** register with one client player-animation registry that keeps
  the current priority order. They are triggered by one game-time-stamped payload, as
  casting is.
- **Time source.** Animation time is game time plus partial tick, from one helper.
  Wall-clock time is used only where freezing would look wrong, and is documented.

**Rationale**
- Boss fights are Hemomancy's set pieces. Six sync conventions mean six ways to get
  stutter and drift, and each new boss copies whichever one its author found first.
  `VesperAnimClock` is the only one with a test and a written reason.
- About 60 private smoothstep copies show that every animator re-invents easing. The
  quintic trap means "just use `Mth`" would silently change every curve.

**Existing example**
- The casting system: one payload, server game-time stamps, pure playback maths and
  one `MixinHumanoidModel` hook.
- `VesperAnimClock`.
- Eruptus for simple mobs.

**Migration targets**
- A-1 (boss clocks)
- A-2 (easing copies, each replacement byte-equivalent)
- A-3 (bespoke player animations)
- A-4 (time sources)

**Exceptions**
- Procedural chain styles stay separate (A-5).
- Moving per-tick synced counters to start timestamps is a separate, higher-risk task.
  It is not part of any consolidation phase.

## 5. Gameplay mechanics and effects

This standard covers features that touch shared mechanics: manipulations, armour
sets, morphlings, scars, skills, effects, living weapons, rites, entity abilities,
Unstained mechanics, hazards, Still Arts and summons.

**Preferred pattern**

| Mechanic | Use |
|---|---|
| Deal damage | `SchoolDamage.cast(...)` context, so damage is attributed to the caster and school |
| Choose targets | `ManipulationCombatHelper.canHarm` / `allied` / `aimedTarget`. Never a bare `Monster.class` filter. |
| Apply a status effect from a school | `SchoolStates.canApply` |
| Spend blood (active cost, upkeep, machine cost) | `BloodAccounting.pay(player, amount, Source{id, category, countsAsSpend, reserveProtected})`. It wraps `BloodFlowLedger`, records diagnostics and calls `BloodVolumeEvents.syncVolume`. |
| Grant passive blood | `BloodFlowLedger` / `CirculationIncomeHelper` |
| Prevent death | `LastRiteHelper`, at the single death-save stage |
| Modify incoming damage | A registered stage: modifiers early, consumers of the final amount late (G-1) |
| Modify healing | The heal-modifier table |
| Temporary attribute bonus | `AttributeModifiers.sync(...)`, which does nothing when the value is unchanged, with a stable `ResourceLocation` id |
| Cast eligibility on the server | `BloodManipulation`'s gate or its shared predicate. Never a re-implementation in a packet. |
| Grant flight | The flight arbiter |
| Full armour set check | `ArmorSets.isFullSet` |

**Rationale**
- A manipulation, an armour ability and a morphling bite should feel different. But
  they should not disagree about whether a pet is a valid target, whether Sanguine
  Reserve protects blood, or who gets kill credit.
- Today those answers depend on which family the code lives in (F-13, F-15, F-16).
- `BloodFlowLedger` already exists to answer the blood questions. `ManipulationCombatHelper`
  and `SchoolDamage` already answer the combat ones.

**Existing example**
- Manipulations: the cast gate `BloodManipulation.tryPerformAction`, plus
  `ManipulationCombatHelper` and `SchoolDamage`.
- Morphlings follow most of this already.

**Migration targets**
- Armour abilities, scars, Still Arts, living weapons, primal morphlings, puppet
  threads, and machine and block blood payments (G-2, G-3, L-2).
- The 25 `Monster.class` filters.
- The 7 duplicated cast-eligibility checks (N-5).

**Exceptions**
- Domain rules stay local where they really are domain-specific: rite-space
  restrictions, puppet targeting AI, White Humor accounting and damage-absorption
  pools.
- **Every migration passes flags that reproduce today's behaviour.** Changing a flag
  to "fix" behaviour is a separate, approved change.

## 6. Persistence and state

**Preferred pattern**

| Kind of state | Home |
|---|---|
| Durable, earned, per-player (progress, knowledge, claims, unlocks, persisted cooldowns) | An attachment in `HemoAttachmentTypes` with `.copyOnDeath()`, accessed only through `HemoCapabilityAccess`, serialised with PascalCase keys |
| Per-world | A `SavedData` with `Factory` + `get`. Overworld-scoped data takes `MinecraftServer`, not an arbitrary level. |
| Per block entity | Block-entity NBT. Blood goes only through the `BLOCK_BLOOD_VOLUME` attachment. |
| Per item | A typed data component from `DataComponentInit` for new data |
| Session-only, server-side (active channels, session cooldowns, pending animations) | A static UUID-keyed map, registered with `TransientServerStateEvents`, which clears it on server start and stop and removes the entry on logout |
| Client-side caches | Cleared on `ClientPlayerNetworkEvent.LoggingOut` |

Supporting rules:
- **Accessors.** Player-attachment accessors return the concrete type, not `Optional`.
- **Sync.** Attachment sync goes through one registry of `(attachment, packet factory)`
  pairs. Mutations set a dirty flag that is flushed once per tick. Sync is not
  repeated per event, and whole attachments are not polled per tick.
- **Pending deliveries.** These go through `PendingDelivery`:
  - Deliver only to a living player.
  - Not during observational Chamber visits.
  - Count confirmed insertion, never Creative discard.
  - Callers own their tag access.
- **Migrations.** Each migration reads the old key once, writes the new home, clears
  the old key and bumps the per-player `dataVersion`. It records a removal date. It
  never runs per tick.
- **Gates.** Degree, blood-active and Unstained-stage checks go through
  `ProgressionGates`.

**Rationale**
- Player progression is the most valuable data in Hemomancy and the hardest to repair.
- Today a contributor has to guess whether to use an attachment, persisted NBT or root
  NBT, which key style to follow, and whether a clone handler is needed. Each guess
  creates another variant.
- One answer per kind of state removes the guessing. It also removes the five clone
  handlers and the 17 sync triplets.

**Existing example**
- `BloodVolume` as the attachment.
- `StationUpgradeProgress.LegacyMigrated` and `StationUpgradeMigration` for one-shot
  migration.
- `RiteZoneSync` for send-on-change.

**Migration targets**
- S-1 through S-9.
- The `common/data/JsonResourceReloadListener` loaders are covered in §8.

**Exceptions**
- **Do not rename NBT keys inside existing attachments.** Mixed casing is permanent
  history.
- The knowledge stores stay separate (they cover different domains).

## 7. Events and lifecycle

**Preferred pattern**
- **Subscribers.** Use `@EventBusSubscriber(modid = Hemomancy.MOD_ID)`, adding
  `value = Dist.CLIENT` for client classes. Never pass `bus=`, which NeoForge 21.1
  ignores.
- **Constructor listeners.** Only mod-lifecycle events (`FMLCommonSetupEvent`,
  `RegisterPayloadHandlersEvent`, `AddReloadListenerEvent`) are added in the
  `Hemomancy` constructor.
- **Ordering.** Where order matters (damage, death, heal), use explicit `priority` and
  a documented stage table. **Never rely on class-scan order.**
- **Tick handlers.** Check cheap conditions first. Use `getExistingData` / `hasData` on
  hot paths, so attachments are not created on every entity (P-2).
- **Player lifecycle.** Each system that keeps player state handles login, respawn,
  clone, dimension change and logout through the shared sync registry and
  `TransientServerStateEvents`, rather than hand-writing the subset it remembers.

**Rationale**
- 211 subscriber classes and seven registration styles mean nobody can find all
  handlers for an event.
- Damage order is the one place where this already causes a real gameplay difference
  (G-1).

**Existing example**
- `TransientServerStateEvents` (central reset).
- `HemoCapabilityRegistrar` (sync trio).

**Migration targets**
- R-1 (subscription style).
- G-1 (damage stages).
- S-2 and S-8 (lifecycle fan-out).

**Exceptions**
- JEI's lazy `addListener` for an optional dependency.

## 8. Data-driven content and configuration

**Preferred pattern**
- **Use JSON** when content is a catalogue that designers or datapacks should be able
  to extend, and its shape is stable. Examples are rites, floors, sigils, blood
  profiles, inquiry dialogue and Scriptorium affinities.
- **Use Java** when it is behaviour: state machines, AI, rendering and complex rules.
- **Loaders** extend `common/data/JsonResourceReloadListener`, parameterised by a
  `Codec`. They are registered in `Hemomancy.onAddReloadListeners` by simple class name.
- **Client sync.** When clients need the data, use one generic codec-based sync
  payload. Do not ship raw JSON and re-parse it on the client.
- **Config** (NeoForge `ModConfigSpec`) is for server owners' tuning knobs. Balance
  values that designers own stay in code or data.
- **Dead config.** Every config value has a reader. A config with no reader is deleted
  or wired up.

**Rationale**
- Ten loaders currently hand-parse Gson ten ways. Three classpath reads bypass
  datapacks entirely.
- One codec-based base removes the boilerplate. It makes errors consistent and gives
  datapacks a uniform contract.

**Existing example**
- `common/data/JsonResourceReloadListener`.
- `RiteZoneSyncPacket` (`StreamCodec.composite`).

**Migration targets**
- R-6 (the 6 hand-rolled listeners).
- The 3 raw-JSON sync payloads.
- D-5 (the datagen decision).
- F-25 (the unregistered MnA config).

**Exceptions**
- **JSON folder names never change.**
- Dialogue authoring (D-6) is out of scope until a content-pipeline decision is made.

## 9. Naming

The vocabulary below applies to **new code**. Existing classes are renamed only when
they are already being moved for another reason. **No mass renames.**

| Suffix | Means | Not |
|---|---|---|
| `*Init` | Holds `DeferredRegister`s and holders | Logic |
| `*Events` | A class of `@SubscribeEvent` handlers | `*Handler`, `*EventHandler` |
| `*Rules` | Pure, side-free decision logic, unit-testable without Minecraft running | Anything that mutates the world |
| `*Manager` | Owns session or world runtime state and its lifecycle | `*Service` (treat existing `*Service` classes as Managers) |
| `*Helper` | Stateless utility used by one feature family | General utilities: put those in `common/util` |
| `*Packet` (suffix) | A `CustomPacketPayload` | A `Packet*` prefix |
| `*BlockEntity`, `*Menu`, `*Screen`, `*Renderer`, `*Model` | Vanilla vocabulary | `*Tile`, `*Container` |

Other naming rules:
- **Registry paths** are snake_case. They are frozen once shipped.
- **Magic numbers.** Degrees are compared through `ProgressionGates` or
  `EnumInitiatoryDegree`, never a literal. Tick constants are named
  (`TICKS_PER_SECOND * n`) when they encode game time.
- **Logging.** Use `Hemomancy.LOGGER`, or a per-class `LogUtils.getLogger()`. Never
  `System.out`. A warning includes the player or position and the resource id involved.
- **Null handling.** Accessors for always-present state return non-null. `Optional`
  means "may be absent".

**Rationale**
- `*Rules` is already the dominant and most useful suffix (323 classes), because it
  signals "pure and testable". The rest of the vocabulary is drawn up around it.
- `Handler`, `Service` and `Manager` overlap today, so a reader cannot predict what a
  class does.

## 10. Packages

**Preferred pattern**
- Organise `common/` by **feature family**, with `harbinger`, `unstained` and `shared`
  sub-packages where both paths exist.
- Within a family:
  - content (blocks, items, entities)
  - `*Rules` (pure logic)
  - `*Events` (wiring)
  - `*Manager` (session state)
- New top-level packages:
  - `common/encounter/` for boss and arena managers, moved out of `worldgen` (W-3).
  - `client/registry/` for client registration, split out of `ClientEvents` (R-5).
  - `common/util/` for general utilities: `HemoEasing`, `PatternOrigins`,
    `AttributeModifiers`.
- `network/` holds payloads and `PacketHandler` only.
- `client/**` never holds anything the server needs. `common/**` holds no client code.

**Rationale**
- A contributor should be able to predict a class's purpose from its package.
- Today, encounter managers live in `worldgen`, gameplay lives in `network`, and
  client code lives in `common/init` and `capability`.

**Exceptions**
- Moves that would break many text-pinned tests wait until those tests are triaged
  (§11).

## 11. Testing and diagnostics

**Preferred pattern**

| What you are proving | Test kind |
|---|---|
| Pure decision logic (`*Rules`, maths, codecs) | JUnit 5, no Minecraft bootstrap |
| Data contracts (every rite declares a purpose, every manipulation has lang) | A `*DataTest` reading resources through a shared `ResourceReader` |
| Behaviour in a world (delivery, rites, combat, persistence across death) | A NeoForge GameTest using shared `TestPlayers` / journey fixtures |
| Visuals | A review client with before and after screenshots, recorded in the PR |
| Save compatibility | A fixture test that loads an old-format NBT snapshot and asserts the migrated state |

Further rules:
- **New source-text assertions are discouraged.** Allowed only for architecture
  guardrails, such as "no `net.minecraft.client` import under `common/network`", and
  only through a shared `SourceAssertions` helper.
- **Shared helpers.** Assertions and readers live in `src/test/.../support`, never as
  private copies.
- **GameTest registration** happens by annotation scan only.
- **Baseline.** `./gradlew.bat test` must be green before a consolidation phase starts.
  `alphaCheck` must be green at its end.

**Rationale**
- Hemomancy's tests are extensive: 3,908 JUnit tests and 1,268 GameTests. But hundreds
  of them pin source text rather than behaviour.
- Source-text tests hold refactoring back without protecting players.
- Moving them to behaviour and data tests is what makes every other standard
  affordable to adopt.

**Existing example**
- The `*DataTest` family, such as `CardinalRitePurposeDataTest`.
- `VesperAnimClock`'s unit test.
- The journey fixtures.

**Migration targets**
- T-1 through T-6.

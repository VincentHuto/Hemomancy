# Hemomancy Architecture Map

Status: analysis snapshot taken 2026-10-21 at commit `3fae723d0` on `neo-1.21.1`.

Companion documents:
- [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md): what is wrong, with evidence.
- [CANONICAL_STANDARDS_PROPOSAL](CANONICAL_STANDARDS_PROPOSAL.md): how things should be done.
- [CONSOLIDATION_ROADMAP](CONSOLIDATION_ROADMAP.md): the order of work.
- [QUICK_WINS](QUICK_WINS.md): small, low-risk changes.
- [evidence/](evidence/): the eleven subsystem audit reports, with line-level citations.

This guide explains where things live and how data moves. It describes the code as
it is today. Where today's state is not the recommended state, it links to the
standards proposal. Paths are relative to `src/main/java/com/vincenthuto/hemomancy/`
unless they start with `src/`, `docs/` or `tools/`.

---

## 1. Shape of the project

| Area | Files | Lines | Role |
|---|---|---|---|
| `Hemomancy.java` | 1 | 366 | Mod entry point. Registers every `DeferredRegister`, configs, attachments, payloads and reload listeners. It also holds the creative-tab routing lists, which belong elsewhere. |
| `common/` | 2,182 | ~250k | All gameplay: content, state, rules, networking and worldgen. Loaded on both sides. |
| `client/` | 1,176 | ~177k | Rendering, models, animation playback, particles, screens, HUD and client-only registration. Must never load on a dedicated server. |
| `mixin/` | 69 | ~1.9k | Vanilla hooks declared in `hemomancy.mixins.json`. |
| `compat/` | 58 | ~6.6k | JEI is compiled. MnA and Curios are dormant: `build.gradle` excludes them from compilation on purpose. |
| `config/` | 6 | ~1k | NeoForge config specs. |
| `src/test/java` | 1,242 | ~93k | JUnit 5, 3,908 tests. Several hundred of these files assert on Java source text. |
| `src/gameTest/` | 198 | ~57k | 1,268 NeoForge GameTests, journey fixtures, review-client drivers and the `/hemo test` command. |

External code that shapes the architecture:

- **HutosLib.** It is pulled as a composite build from `../HutosLib` when that folder exists. It provides the book and Liber pipeline (`BookPlaceboReloadListener`, `FieldNotes`, `BookKnowledge`), particle colour types, `BoltRenderer`, `TendrilGeometry`, `VanillaPacketDispatcher` and `MathUtils`. It does not provide dialogue, despite what older docs say.
- **NeoForge 21.1.251.** It supplies attachments, payload networking, `DeferredRegister` and the event buses.
- **GeckoLib.** It is on the classpath, but nothing compiled uses it. Its only users are in the excluded MnA compat.

---

## 2. Package ownership

`common/` is organised by feature family. The larger families split into
`harbinger`, `unstained` and `shared` sub-packages.

| Package | Owns | Notes |
|---|---|---|
| `common/init` | All registries (`*Init`) | It also hosts the client-only `ParticleInit`, `ShaderInit` and `RenderTypeInit`, which should move to `client/`. |
| `common/capability` | Player, entity and block attachments and access to them | `HemoAttachmentTypes`, `HemoCapabilityKeys`, `HemoCapabilityRegistrar` and `HemoCapabilityAccess`. Each attachment family has one `*Events` class. |
| `common/manipulation` | Blood manipulations, meaning active casting | `BloodManipulation` base class, per-tendency folders, combat helpers, and the channel, charge and reactive managers. |
| `common/rite` | Cardinal Rites, Working crafts, floors, sigils, rite spaces and zones, brazier rites, and the legacy Unstained rites | `HarbingerCardinalRiteEvents` drives the level tick for every rite kind. |
| `common/tile` | Block entities: machines and stations | There is no shared base class. |
| `common/block`, `common/item`, `common/entity` | Content classes | `entity` includes bosses, NPCs and dialogue (`entity/npc/dialogue`). |
| `common/station` | Shared station-upgrade catalog, progress and rites | The contract is `docs/STATION_UPGRADES.md`. All seven stations use it. |
| `common/network` | Payloads and `PacketHandler` | It also contains gameplay that belongs elsewhere: blood-structure crafting and rite activation. |
| `common/worldgen` | Structures, features and biomes, plus encounter managers | The Chamber of Will, Mycophant and Vesper managers live here although they are not worldgen. |
| `common/event` | Cross-cutting event handlers | Includes `TransientServerStateEvents`, the intended home for server start and stop cleanup. |
| `common/effect`, `damage`, `summon`, `brewing`, `enchanting`, `loot` | Mob effects, damage attribution, summons, and small station rule layers | `damage/SchoolDamage` is the damage-attribution context. |
| `common/mission`, `succession`, `circus`, `antecedent`, `world` | Side progression systems | Each is self-contained. Several keep their state in raw persistent NBT. |
| `common/data` | Data-driven loaders and datagen (`data/gen`) | `JsonResourceReloadListener` is the shared loader base. Only two loaders use it. |
| `common/command` | The `/hemo` command tree | `HemoCommand.java` is 1,982 lines. |
| `common/testing` | `DevTestHooks` for GameTest registration | Test infrastructure that ships in the main jar. |

`client/` mirrors this layout:
- `client/render` holds renderers, VFX and render types.
- `client/model` holds models.
- `client/particle` holds particles.
- `client/screen` holds screens.
- `client/player` holds casting and other player-animation playback, plus HUD.
- `client/event` holds client event handlers, including the 1,495-line `ClientEvents`.

---

## 3. Lifecycle from mod construction to a running world

1. **Construction.** The `Hemomancy` constructor registers 41 `DeferredRegister`s on
   the mod bus. It also registers `HemoAttachmentTypes.ATTACHMENT_TYPES`,
   `HemoCapabilityRegistrar`, the configs, and payloads through
   `PacketHandler.registerChannels(modBus)`.
2. **Annotation scanning.** FML discovers 211 `@EventBusSubscriber` classes. Between
   classes at the same priority, listener order is not guaranteed. This matters
   for damage handlers; see audit finding G-1.
3. **Common setup.** This runs the HutosLib book serializer, `FieldNotes`,
   `SkillPointInit.init()`, `ManipulationTreeInit.init()` and the entity predicates.
4. **Client setup.** `ClientEvents` and a few feature-local subscribers register:
   - entity renderers and layer definitions
   - 50 block-entity renderers
   - particle providers
   - menu screens, shaders, key mappings and GUI layers
5. **Server start.** Reload listeners run: 9 server JSON loaders plus the HutosLib
   books. `EngramTextureCache.loadAll` runs, and `TransientServerStateEvents` clears
   session state.
6. **Running.** Per-tick work is spread across about 60 player-tick handlers, the
   level-tick handlers for rites, the Chamber of Will and encounters, and the
   block-entity tickers.
7. **Server stop.** `TransientServerStateEvents` clears its list, and about 21 other
   classes clear their own static state.

---

## 4. Player state and how it reaches the client

```
server:
  gameplay code
    -> HemoCapabilityAccess.getX(player)
    -> player.getData(ATTACHMENT)      35 attachment types, INBTSerializable, copyOnDeath
  per-attachment *Events on login / respawn / dimension change
    -> PacketHandler.sendToPlayer(new PacketSyncX(...))
                    |
                    | payload
                    v
client:
  handler in client/** -> client static state or client copy of the attachment
```

- **Authority.** The server attachment is authoritative. The client only holds a copy
  for display.
- **Access.** Use `HemoCapabilityAccess`; 1,158 call sites already do. About 42 call
  sites bypass it with a direct `getData`. Most of those read `MUSCLE_MEMORY` or
  `DIALOGUE_KNOWLEDGE`, which do not have helpers yet.
- **Blood opt-in.** `IBloodVolume.isActive()` is the only opt-in flag for blood magic.
  Every player has an `IBloodVolume`.
- **Second storage substrate.** This is debt. About 100 files also keep player state in
  raw `player.getPersistentData()` keys:
  - Keys under `Player.PERSISTED_NBT_TAG` survive death.
  - Other keys are lost on death unless a hand-written `Clone` handler copies them.
  - Circus progress, Chamber access, pending whispers and several pending-delivery
    claims are stored this way.
  - The standards proposal moves durable state into attachments.
- **World state.** 20 `SavedData` subclasses hold world state: bloodlines, rite floors
  and spaces, Qliphoth blooms, fanes and the Chamber of Will. Most are scoped to the
  overworld.

### Progression paths

- **Harbinger progression** is the `InitiatoryDegree` attachment (degrees D0 to D7)
  plus `KnownManipulations`, scars, skills and many side ledgers.
- **Unstained progression** is the `UnstainedProgress` attachment: purity, clarity and
  the stage ladder.
- **The two paths are mutually exclusive.** `docs/HEMOMANCY_REFERENCE.md` documents
  the reset rules.
- **Degree checks are scattered.** Today they are written inline in about 36 files.
  The standards proposal routes them through one gate facade.

---

## 5. Casting a manipulation, end to end

```
key press (client/event/ClientEvents)
  -> UseManipKeyPacket (client to server)
     -> BloodManipulation.tryPerformAction            the single cast gate
          checks, in order: paralysis, known form, retired, cooldown,
          vascular section, purity, pome, cost ledger, charge,
          alignment, borrowed blood
          -> IBloodVolume.drain + addBloodSpend, then sync the volume
          -> getAction(...)                           subclass behaviour;
                                                      damage goes through SchoolDamage.cast
          -> ManipulationCastSounds, plus ManipulationVisualPacket (server to client VFX)
          -> startCooldown
     -> CastingAnimationManager                       server session, heartbeat every 20 ticks
          -> CastingAnimationPacket                   server to client, game-time stamped
             -> client/player CastingAnimationClientState -> CastingTracks.resolve
             -> third person: MixinHumanoidModel.setupAnim, TAIL inject
             -> first person: RenderHandEvent
```

Entities cast through a parallel path:
- Each manipulation's entity cast is attached at registration (`setEntityCast(EntityCasts::x)` in
  `ManipulationInit`, since step 11); `EntityManipulationEffects.cast` applies the shared rules and runs it.
- Drudge actions are attached the same way (`setDrudgeAction(DrudgeActions::x, ...)`).

---

## 6. Rites and crafts

- **Recipes.** There are 91 rite JSON files under
  `src/main/resources/data/hemomancy/recipe/cardinal_rite/**`. Each declares a
  `purpose`: `self`, `world`, `making` or `bond`. Working crafts live under `working/`.
- **Floors and sigils.** These come from the `CardinalRiteFloorLoader` and
  `IchorianSigilLoader` reload listeners. Special floors declare their purpose.
- **Activation.** `CardinalRiteActivation.tryStart`, reached from the Living Staff, the Sanguine Formation
  and (for Unstained rites) the blood crafting key, whose packet only delegates (step 11).
- **Running rites.** `ActiveCardinalRite` holds a rite's state, with each kind's own state in
  `PuppeteerTrialState`, `WorkingCraftState`, `StationUpgradeState` and `SuccessionState`.
  `HarbingerCardinalRiteEvents.onLevelTick` runs one shared course and asks the rite's `RiteKind`
  where kinds differ (step 11):
  - `SUCCESSION`
  - `STATION_UPGRADE`
  - `WORKING_CRAFT`
  - `PUPPETEER_TRIAL`
  - `COUNTDOWN`, the legacy and Unstained rites
  - `CEREMONY`, the interactive Harbinger ordeal
  Completion is `CardinalRiteCompletion`, endings `CardinalRiteTermination`, and the client snapshot
  `CardinalRiteSync`.
- **Offerings.** Working crafts and station upgrades escrow their offerings through
  `RiteOfferingEscrow`. Ordinary ordeals consume offerings directly. Blood Structures
  take none.
- **Rite spaces.** A completed Harbinger rite leaves a rite space behind.
  `hemomancy:rite_bound/*` tags restrict blocks, items and entities to matching spaces;
  see `RiteSpaces` and `RiteZoneSavedData`.
- **Brazier rites.** The rites in `rite/brazier` are already a clean shared family.
  They are the model for other rite variants.

---

## 7. Networking

- **Registration.** There is one registration point,
  `common/network/PacketHandler.onRegisterPayloads`. It registers 181 payloads in one
  flat list.
- **Payload shape.** Each payload is a `CustomPacketPayload` with a static `TYPE` and a
  `STREAM_CODEC`. Handlers run on the main thread, which is the NeoForge default.
- **Sending.** Code sends through `PacketHandler.sendToPlayer` and `sendToServer`. It
  also calls `PacketDistributor` directly for near, tracking and all-player broadcasts.
- **Client-side handling.** This belongs in `client/**`, called from `handle`. 66 of
  the 105 client-bound payloads already work this way.
- **Block-entity and entity state.** Block entities use the vanilla update packet:
  `getUpdateTag` plus `sendBlockUpdated`. Entity state uses `SynchedEntityData`.

---

## 8. Rendering, VFX and animation

- **Render types.** The central holder is `client/render/HemoRenderTypes`
  (1,625 lines). Five per-school geometry families hold their own render-type sets.
- **Shared VFX toolkit.** Prefer the existing pieces:
  - Hemomancy particle data types
  - dynamic glow, cell, tendril, lightning, claw and slash effects
  - the Cast Outpouring vein system, reached through its public bridge, `OutpouringBrushes`
  - lightning, which already goes through HutosLib `BoltRenderer`

  `docs/MANIPULATION_VISUALS.md` is the visual standard, and Hematic Riposte is the
  reference bar.
- **Server-to-client VFX.** `ManipulationVisualPacket` is the data-driven model: one
  payload, with an enum `Form` of about 100 values. A few older per-effect particle
  payloads remain alongside it.
- **Entity animation.**
  - About 105 models animate with hand-written `setupAnim` code.
  - Six mobs use vanilla keyframe animations.
  - Bosses run scripted controllers that sync a move or clock to the client.
    `BossReplayClock` (step 10, promoted from `VesperAnimClock`) is the shared replay clock.
- **Player animation.** This is the casting system from section 5, plus six smaller
  player animations that each have their own packet.
- **World effects.** These are drawn through an ordered dispatcher in `ClientEvents`
  (about 33 renderers), plus about 10 renderers that subscribe themselves outside
  that order.

---

## 9. Where to put new work

| You are adding... | Put it here | Follow |
|---|---|---|
| A block, item, entity, effect, sound or particle type | The matching `common/init/*Init`, with a snake_case holder and `Hemomancy.rloc` | Standards §1 |
| A manipulation | A class in `common/manipulation/<tendency>/` extending `BloodManipulation`, an entry in `ManipulationInit`, plus lang and model assets | AGENTS.md; standards §5 |
| A gameplay effect that hurts, heals, drains blood or targets mobs (armour, scar, morphling, Still Art, weapon) | Reuse `SchoolDamage`, `ManipulationCombatHelper.canHarm` and the blood-payment path | Standards §5 |
| Durable player progress | A field on an existing attachment, or a new attachment in `HemoAttachmentTypes` with an accessor in `HemoCapabilityAccess` | Standards §6 |
| Session-only server state, such as a cooldown map or an active channel | A static map cleared through `TransientServerStateEvents` and on logout | Standards §6 |
| A packet | A record in `common/network/<area>/`, registered once in `PacketHandler` with its real direction, with client handling in `client/**` | Standards §2 |
| A block-entity machine | `common/tile/**`. Blood goes through `IBloodReservoir` and the `BLOCK_BLOOD_VOLUME` attachment. Upgrades go through `common/station`. | `docs/STATION_UPGRADES.md` |
| A rite or Working craft | JSON under `data/hemomancy/recipe/cardinal_rite/**` with a `purpose`, and a Liber map family in `HarbingerRecipeMapDefinitions` | AGENTS.md rite rules |
| A data-driven JSON catalogue | A loader extending `common/data/JsonResourceReloadListener`, registered in `Hemomancy.onAddReloadListeners` | Standards §8 |
| A renderer or VFX | `client/render/**`, render types in `HemoRenderTypes`, existing particles and geometry helpers | `docs/MANIPULATION_VISUALS.md`; standards §3 |
| A boss | Entity, controller and a pure rules class, as the Mycophant does; animation through the shared replay clock | Standards §4 |
| A test | Pure JUnit for logic, a `*DataTest` for data contracts, a GameTest for behaviour. Avoid new source-text assertions. | Standards §11 |

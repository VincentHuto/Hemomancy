# Hemomancy audit 11: performance and lifecycle consistency

Scope: read-only static audit of `src/main/java/com/vincenthuto/hemomancy/` (branch `neo-1.21.1`, clean tree at `3fae723d0`). Nothing was run or profiled. Paths below are relative to that source root.

Labels:
- **VERIFIED** means the code path was read, and the cost or behaviour follows from the code plus documented vanilla/NeoForge 1.21.1 semantics.
- **HYPOTHESIS** means the code fact is verified but its real-world impact needs measurement or an in-game check.

Findings from the earlier audit (the 60 player-tick handlers, the KnownManipulation snapshot diff, StationUpgrade 20-tick resync, the FungalScarMigration scan, the 4x LiberKnowledge migration, the scattered ServerStopped handlers and the uncleared UUID maps) are cited here but not re-derived.

---

## 1. Tick-handler table (beyond player ticks)

**Counts:**
- `LevelTickEvent.Post`: 19 handlers. Each runs once per loaded level per tick, which means 5 levels in a normal game (3 vanilla dimensions plus `chamber_of_will` and `fungal_gardens`).
- `ServerTickEvent.Post`: 11.
- `EntityTickEvent`: 12 (Pre and Post combined).
- `ClientTickEvent`: 25.
- Block-entity tickers: 46 blocks override `getTicker`.
- Items with `inventoryTick`: 18.
- Effects with `applyEffectTick`: 29, of which 14 return `true` from `shouldApplyEffectTickThisTick`, so they run every tick.

| Handler (file:line) | Frequency / scope | What it scans | Allocations | Network |
|---|---|---|---|---|
| `damage/SchoolCombatEvents.tick` :266 | **every tick, every LivingEntity** (server) | `entity.getData(SCHOOL_COMBAT)`, which *creates* the attachment on first touch; 2 `hasEffect` checks | 1 attachment per living entity (EnumMap + 2 LinkedHashMaps) | sync only on `takeDirty()` (diff-gated, good) |
| `damage/SchoolAwareness.tick` :35 | every tick, every Mob | `getTarget` and brain memory; `canPerceive` (3 `hasEffect` + distance) only when a target exists | none in the common case | none |
| `circus/CircusDemonstrations.beforeTick` :23 | every tick, every entity | `getPersistentData().hasUUID` | none | none |
| `circus/CircusPracticalController.cleanup` :93 | every tick, every entity | `getPersistentData().hasUUID` | none | none |
| `summon/PuppetStrings.beforeTick` :80 | every tick, every Mob | persistent-data lookup | none | none |
| `event/MorphicNectarEvents` :57 and `WhiteHumorPurificationEvents` :50/:63 | every tick, every ItemEntity (3 handlers) | `getFluidState`; inside the fluid: `getAllRecipesFor` scan plus `scanAbsorbers` every tick | recipe stream | particles every 10 ticks |
| `event/ViridSalisTrailHandler` :65 | every LivingEntity, hashed down to 1/20 ticks | one `getBlockState` | none | particles only on hit |
| `rite/zone/RiteBoundEvents` :84 | all non-player entities, throttled by `ENTITY_CHECK_INTERVAL` | rite-space lookup | none | none |
| `worldevent/ChamberOfWillEvents` :109 | every ItemEntity | dimension check first | none | none |
| `HarbingerCardinalRiteEvents.onLevelTick` :179 | every tick, **every level** | `PendingBloodCraftManager.tick()` (not level-scoped, see 2.4); a 91-recipe linear scan per active rite per tick (:261) | lists on sync | rite sync every 10 ticks to all players in the level; Qliphoth sync every 200 ticks to all players |
| `FoundingFaneEvents` :38 | every 40 ticks, every level | iterates all fanes; during a Blood Moon, an AABB of radius `HEART+32+64` per fane | list per player | **always sends a packet to every live player, even when there are no fanes** |
| `QliphothBloomEvents` / `SanguineDominionEvents` / `PaleConsecrationEvents` / `LetheCovenantEvents` | interval-gated, per level | AABB queries per zone (radius `chunkRadius*16`, ±64 Y) | lists | progress sync only on change |
| `BloodRoutingEvents` :44 | every `ROUTE_INTERVAL`, per level | `getBlockEntity` per link | none | none |
| `ConductionManager.onTick` :232 | per pulse, per network | graph rebuild plus AABB(13), then `.stream().sorted()` | streams | visuals |
| `BlackVeilCovenantManager` :74 | every 5 ticks per veil | AABB(radius) | none | effect packets |
| `CorticalArcManager` :49 | End only, per player, every 10 ticks (staggered) | 145³ volume, palette-rejected per section | `ArrayList(MAX_NODES)` | arcs |
| `ChamberOfWillManager.tick` (`Hemomancy.java:141`, `ChamberOfWillManager.java:486`) | every 40 ticks, Chamber players | `ensureRoom`, `ensureArborOfWill`, `ensurePairedWarpChair` | none significant | **2 packets per player every 40 ticks, unconditionally** (`syncTo` :527) |
| `HematicMemoryToolEvents` :94 | 20 / 10 ticks per level | players and effigies | — | — |
| `ChummedWatersAreaManager` :52 | every tick, `getAllLevels()`, `prune(level)` each | AREAS list | none | particles every 12 ticks |
| `*StatusVisuals.tick` (4 classes) | every tick | iterate tracked maps (hold Entity refs) | none | send on expiry only |
| `VoyagerVesselGuidance` :54 | every tick, ≤2 probes | bounded structure probe (time-budgeted) | — | — |
| `SuccessionReconciliation` :13 | every 100 ticks | all levels' active rites | HashSet | none |

**Block-entity tickers with unconditional per-tick work:**
- `StillwaterCondenserBlockEntity.serverTick` (:69) scans **405 + 1,331 = 1,736 `getBlockState` calls every tick, per condenser, with no gate**: `hasGhostPipe` (9×5×9, :109) and `VerdigrisLatticeBlock.hasActiveLattice(level,pos,5)` (11³, `VerdigrisLatticeBlock.java:98`). Both scans run in full whenever the structure is incomplete, which is the common state while a player is building or idling. This is the worst block-volume scan in the mod.
- `GhastlyAlembicBlockEntity.serverTick` (:318) streams the 42 distillation recipes up to 3 times per tick, even when not heated. It also calls `BrewingResolver.resolve`, which allocates an output `ItemStack` every tick while an advanced match exists. `PallidRetortBlockEntity` (:166) is a copy of the same pattern.
- `MycelialLanternBlockEntity.serverTick` (:101) scans `findRecipe()` (8 recipes) every tick while there is a culture.
- `MycelialCrucibleBlockEntity` (:178) runs `tryStartPhase1` and `tryStartPhase2Batch` recipe scans every idle tick.
- `SerpentineIdolBlockEntity.tick` (:22) is registered for **both sides** (`BlockSerpentineIdol.java:70` returns a ticker unconditionally). When active it runs an `AABB(4)` query every tick on server and client, re-fetches its own BE, and calls `addEffect` on the client too.
- `GhastlyAlembicBlock` footprint: `pruneFillers` plus `hasSpace` (27 cells) runs every tick until space exists, so forever if it is blocked.
- Properly throttled, no action needed: BloodTrialAltar, BloodPylon, SanguineVigil, SemiSentientConstruct, MorphlingCradle and SomaticLoom (recipe refresh every 20 ticks).

**Items (`inventoryTick`):**
- `VialRackItem.inventoryTick` (:98) runs **on both sides every tick**. On the client it does 1 `copyTag()`. On the server it does 3 `copyTag()` calls plus **8 `ItemStack.parseOptional` codec decodes** (`getVials`, :123-130, via `migrateAhaematicSamples`), for every rack in every inventory slot.
- `RecycledEnzymeItem.inventoryTick` (:32) deep-copies `CUSTOM_DATA` every tick per stack.
- `OrganEchoItem` (:70) does a 41-slot scan per echo per tick.
- `MemoryThreadItem` builds a dimension `String` every tick while active.

**Effects:**
- `BloodBindingEffect` (:23) makes 4 `sendParticles` calls every tick per bound entity.
- `SanguineFertilityEffect` (:28) runs an AABB query every tick on **both sides** (radius 6+2·amp).
- `VerdigrisAura` and `SilverWard` are tickCount-gated, so they are fine.

**Unthrottled all-entity or block-volume scans flagged:** `SchoolCombatEvents.tick` (all living entities), `StillwaterCondenser` (block volume), `SerpentineIdol` (AABB every tick, both sides) and `SanguineFertility` (AABB every tick, both sides).

---

## 2. Demonstrable hotspots (ranked)

### 2.1 Equipment handler churns a syncable attribute every tick (VERIFIED)
- **Where:** `HarbingerEquipmentEntityEventHandler.playerTick` (:182-195).
- **What it does:** every tick, on both sides, for every player, it calls `removeModifier(elytra)` and then re-adds it whenever `chest.canElytraFly(player)` is true. A vanilla Elytra is enough to trigger this.
- **Why it is demonstrable:** `hemomancy.fallFlying` is registered with `setSyncable(true)` (`AttributeInit.java:33`). In vanilla, `AttributeInstance.removeModifier` and `addModifier` both call `setDirty()`. `ServerEntity.sendDirtyEntityData` then broadcasts a `ClientboundUpdateAttributesPacket` to the player and all trackers on every player update interval (2 ticks).
- **Effect:** roughly 10 attribute packets per second per Elytra wearer, multiplied by the number of trackers. The handler also ticks the equipment item handler on the client for every visible player.
- **Fix:** a "has it and should it" guard. Pure refactor.

### 2.2 `SchoolCombatEvents.tick` attaches state to every living entity (VERIFIED)
- **Where:** `SchoolCombatEvents.java:266-286`.
- **What it does:** `SchoolStates.data(target)` resolves to `entity.getData(SCHOOL_COMBAT)` (`HemoCapabilityAccess.java:52`). In NeoForge, `getData` creates the attachment if it is absent, so every living entity gets one on its first server tick: an EnumMap plus 2 LinkedHashMaps.
- **Persistence cost:** the type is `serializable(...)` with no "should serialize" filter, and `serializeNBT` always writes `observedAt`. Every saved mob therefore carries a `hemomancy:school_combat` compound. That costs chunk-save size and load-time allocation across the whole world.
- **Fix:** use `hasData` before `getData` in the tick; only `observe` entities that already hold state.

### 2.3 StillwaterCondenser block-volume scan (VERIFIED)
- 1,736 block reads per tick per condenser while the structure is incomplete (section 1).
- **Fix:** cache the results and re-check every 20–40 ticks, or on neighbour updates.

### 2.4 `PendingBloodCraftManager.tick()` runs once per level, not once per server tick (VERIFIED; correctness)
- **Where:** called from `HarbingerCardinalRiteEvents.onLevelTick` (:183), which fires for every level. `PendingCraft.tick()` decrements `remainingTicks` (`PendingBloodCraftManager.java:70`), and the comment at :116 says "every server tick".
- **Effect:** blood-structure craft delays run about 5x faster than authored.
- **Lifecycle:** `PendingBloodCraftManager.clear()` has **no callers** (grep), and `PendingCraft` holds `ServerLevel` and `ServerPlayer` references. Crafts pending at server stop survive into the next integrated-server session with dead level references.

### 2.5 BodyIdiom BLACK_HEART broadcast for every player every second (VERIFIED)
- **Where:** `BodyIdiomEvents.onPlayerTick` (:72-77).
- **What it does:** calls `ManipulationVisuals.attached(player, BLACK_HEART, …, ticks=0 when inactive)` every 20 ticks for **every** server player, regardless of blood activation. `send` uses `sendToPlayersNear(…, 64+)` (`ManipulationVisuals.java:67-71`).
- **Effect:** N players clustered together means about N² packets per second, all of them no-op "clear" cues.

### 2.6 VialRack and RecycledEnzyme `inventoryTick` (VERIFIED)
- Codec decoding and NBT deep copies every tick per carried stack (section 1).
- Players carry several vial racks, so this scales with inventory contents, not with gameplay.

### 2.7 Every-tick effect re-application (VERIFIED by vanilla semantics)
- `SheolicBastion…updateSheolicPassives` (:114-121) runs `addEffect(FIRE_RESISTANCE, 40)` every tick.
- `BlackVeilCovenantManager` player tick (:46) re-adds 3+ effects with duration 25 every tick inside a veil.
- A longer duration makes `MobEffectInstance.update` return true, and `onEffectUpdated` sends a `ClientboundUpdateMobEffectPacket` to the `ServerPlayer` each time. That is 20+ packets per second per affected player.

### 2.8 BloodBinding particle stream (VERIFIED)
- `BloodBindingEffect` makes 4 `sendParticles` calls per tick per bound entity, each to every player within 32 blocks.
- `SerpentineIdol` keeps re-applying `blood_binding` (visible, duration 5) to every non-player mob within 4 blocks of an active idol. An idol near a mob farm produces 80 particle packets per second per mob.

### 2.9 Fixed-interval broadcasts without diffing (VERIFIED)
- FoundingFane (40 ticks, every level, every player, including an empty list).
- Chamber `syncTo` (40 ticks, 2 packets).
- Rite sync (10 ticks, all players in the level, no distance filter, with a 91-recipe linear lookup per rite inside `toClientEntry` :1034).
- See section 6 for the full list.

### 2.10 Linear rite-recipe lookup (VERIFIED)
- `CardinalRiteRecipe.getRiteByLocation` (`CardinalRiteRecipe.java:27-50`) streams all 91 cardinal-rite recipes, and does it twice on a prefix miss, instead of using `RecipeManager.byKey`.
- It has 32 call sites, including the per-tick, per-active-rite path at `HarbingerCardinalRiteEvents.java:261`.
- It also mutates the shared recipe with `setId` on every call.

### Player-tick handlers: top 10 by work done before or without an early return
1. **KnownManipulationEvents** (:59): full `serializeNBT` plus `ListTag.equals` every tick (known from the earlier audit).
2. **BloodVolumeEvents.playerTick** (:71). For every active player, every tick:
   - `getUnstainedProgress` lookup;
   - `grantIfNotDone`, with an advancement map lookup, at volume ≥ 5000;
   - `recordEnzymeMastery`: a 36-slot scan with 12 item comparisons per slot (D2+);
   - `MaxBloodLedger.apply`: a new `ArrayList` of contributions, plus organ and scar iteration;
   - 4–5 config `.get()` calls and 3 skill-helper queries.
3. **HarbingerEquipmentEntityEventHandler.playerTick** (:182): both sides, plus attribute churn (2.1).
4. **MuscleMemoryWorldEvents.tick** (:77): `getData`, `migrateLegacyPriming`, reserve tick, 3 attribute lookups and a new `TickSnapshot` record plus `Vec3` per player per tick. Its modifier updates are idempotent.
5. **ScarsEntityEventHandler.playerTickScars** (:64): both sides, `IScars::tick`, then 3 helper ticks (Rooted, AfflictionDigest, Conserve) each tick; a remove-plus-add of a permanent modifier every 20 ticks while synergy holds (:147-151).
6. **BodyIdiomEvents** (:72): `reconcileIronHeartCapacity` every tick, plus the broadcast in 2.5.
7. **EquippedMorphlingEvents** (:61): morphling hunger and passive ticks every tick for equipped players.
8. **ClinicalBloodKnowledge** (:137): every 20 ticks, three pending-delivery routines plus a 41-slot inventory scan.
9. **AxonalTransductionManager.tick** (:133): with no session, `touchingNode` iterates the player AABB cells (`betweenClosed`, `hasChunkAt`, `getBlockState`, `new AABB` per cell) every tick for every player.
10. **SheolicBastion** (:66) / **SilentArchon** (:186) / **Edacious** (:68): unconditional per-tick set checks; SilentArchon also runs on clients for every player.
- Close behind: **QliphothPomeInventoryEvents** (:91), which does a SavedData lookup every tick, and **CircusDiscoveryProgress** (:38) and **VagrantMindEncounterManager** (:31), which run `structureManager().getStructureWithPieceAt` every 20 ticks per player.

---

## 3. Speculative concerns (HYPOTHESIS; measure first)

- **Per-call RenderType factories.** `HemoRenderTypes` has 37 factory methods that build a brand-new `RenderType` on every call, each with its own CompositeState, TextureStateShard and uniform lambdas (for example `mnemonicLowtideParchment` :112 and `bloodPool` :621). Render code calls them per object per frame (for example `BrazierFlames.java:48` per lit brazier, `ConsecratedBloodwellRenderer.java:117`, `AnnettaPoolRenderer.java:48`). Distinct instances also defeat `BufferSource` batching, so each call means a flush and a draw call. The allocations are certain; whether they matter depends on brazier counts, so profile a rite with 8+ braziers.
- **Off-screen rendering.** 16 BER types return `shouldRenderOffScreen = true`, and `HematicArmatureBlockEntity` (:512) and `NonEuclideanHallwayRenderer` (:43) use `AABB.INFINITE`. These may forfeit section and frustum culling.
- **End scans.** `CorticalArcManager`'s 145³ scan may degrade to full 4096-block section walks in synaptic-node-dense archipelago sections.
- **About 30 renderers per frame.** `ClientEvents.renderLevelLastEvent` (:736-790) calls about 30 renderers per frame at `AFTER_TRANSLUCENT_BLOCKS`. Most early-return on empty client data, but each is a static call plus a list fetch.
- **`FloorMesh.of`** (:224-228) computes `sockets()` and `standingCells()` lists **before** the cache check, so 2 list allocations per floor per frame even on a cache hit. This is verified but probably cheap.
- **Dead render handler.** `RenderBloodLaserEvent` (registered at `ClientEvents.java:1066`) has no stage filter, so it runs for every stage (about 11 per frame), iterating all players into an **empty** `renderLaser` body (`RenderBloodLaser.java:84`). The cost is trivial but it is pure dead code.
- **JEI visibility tick.** `compat/jei/ClinicalRecipeVisibility.update` runs every client tick and does `Hemomancy.rloc(path)` (ResourceLocation validation) plus an O(lessons²) `recipeVisible` loop.
- **`EngramTextureCache.loadAll`** (`Hemomancy.java:337`, :30 of the cache) does 26 tiny `ImageIO` reads once per JVM, which is fine. The plain `HashMap`s can be populated concurrently by the render thread (`DiscoveryInscriptionBlockRenderer.java:24/38`) and the integrated-server thread. This is a race risk, not a cost problem.
- **Reload listeners and registry init.** All 9 server reload listeners use `SimplePreparableReloadListener` or `JsonResourceReloadListener` (prepare runs off-thread), and `MorphlingMutationRegistry.init` lazily bakes models. **No heavy synchronous loaders found.**
- **Damage handlers.** There are 54 damage handlers in about 40 files (`LivingIncomingDamageEvent` 18, `LivingDamageEvent.Pre` 19, `Post` 17). Almost all early-return on `instanceof`, `isClientSide` or `hasEffect`. The ones that do work on every hit are `SchoolCombatEvents.prepare` (:63), `ScarsEntityEventHandler` (:103, no type gate), `ManipulationReactiveEvents` (:220, `EndlessHourManip.deferDamage`) and `EarlyInitiation` (:209, a `removeIf` over ACTIVE for every non-player hit).
- **Other high-frequency events:** `EntityJoinLevelEvent` has 6 handlers, `MovementInputUpdateEvent` 4, `RenderPlayerEvent` 4, `InputEvent` 3 and `RenderGuiLayerEvent` 4. 15 GUI layers are registered. `BloodVolumeOverlay` does `String.format` plus `Component` allocation per frame (:274-276, :733); its textures are static.

---

## 4. Client cache and invalidation

Lifecycle hooks:
- `ClientEvents.onClientPlayerLogout` (:628-665) clears about 35 states.
- `onClientLevelUnload` (:668) clears 15.
- `LoggingIn` (:619) clears 5.
- Reload listeners: `FloorMesh` (:1116), the Qliphoth tree cache (:877) and `ClairaudiographAnalysis`.

Of the client files with mutable static collections, the following are **not** reached by any logout, unload or reload hook:

| Cache | Key | Self-healing? | Risk |
|---|---|---|---|
| `CrimsonFireClientState`, `MonolithicDislocationClientState` | entity **id** → gameTime | expiry vs. the new world's gameTime | IDs are reused across worlds; a stale `until` can light an unrelated entity until expiry (HYPOTHESIS) |
| `BloodVialInjectionClientState`, `LivingStaffMorphClientState`, `HematicMicroscopeClientState`, `ArborGrowthAnimations` | entity id / skill id | tick-pruned or never pruned | same ID-reuse risk; `ArborGrowthAnimations.SNAPSHOTS` is never pruned |
| `SporiticThuribleRenderHelper.STATES`, `LivingFlailRenderHelper.STATES` | holder UUID × view × arm | **never removed** | unbounded growth, small per entry; no entity references |
| `BloodProjectileEffects.TRAILS` (Trail holds an `Entity`), `SuccessionBrazierLabels.nearby` (BE references) | — | reset when `world != mc.level` | **retains the old ClientLevel** while the player sits in menus after disconnect (memory, HYPOTHESIS) |
| `CardinalFloorConsecrationRenderer.DIG_DUST_TICK/TENDRILS_SPAWNED/SWALLOWS`, `QliphothSeedTendrilEffects.*`, `EngramGlyphs`, `EngramSigilMesh.CACHE` | packed BlockPos | partial | position-keyed state survives a world switch |
| `MorphlingPlayerPartVisibility` (IdentityHashMap<PlayerModel>), `VesperEveningStar/MountAnimState` (WeakHashMap) | model / entity | weak or identity | fine |
| Track registries (`*Tracks`, `AimLayers`, `EchoCasts`, `ChamberSkyThemeRegistry`, `MorphlingMutationRegistry`) | enum / item | static data | fine |

Server-side caches reachable from the client (integrated server): `EngramTracing.CLIENT_TRACES` and `ActiveEngramLooms.CLIENT` are cleared from `EngramClientState`, which is good.

**Ichorian knowledge (VERIFIED code fact, HYPOTHESIS consequence).** `IchorianKnowledgeEvents` syncs on login and respawn but **not** on `PlayerChangedDimensionEvent`. Its packet writes to the client player's attachment (`PacketSyncIchorianKnowledge.java:175`). Vanilla recreates the LocalPlayer on a dimension change, so the client copy is probably lost after portal travel until the next relog or respawn.

---

## 5. Lifecycle coverage matrix (player-scoped server state)

The matrix shows which lifecycle events a system's own file handles. Key: **in** = login, **resp** = respawn, **clone** = clone, **dim** = dimension change, **out** = logout, **stop** = server stop, **T** = cleared via `TransientServerStateEvents`.

| System | in | resp | clone | dim | out | stop | T | Gap |
|---|---|---|---|---|---|---|---|---|
| **ScaleGripEvents.PENDING_RETURNS** (UUID→ItemStacks) | – | ✔ | – | – | – | – | – | **item loss**, see below |
| PendingBloodCraftManager | – | – | – | – | – | – | – | `clear()` never called; holds Level/Player references |
| AvatarManifestationManager.STATES | – | – | – | – | ✔ | – | – | no stop clear |
| LignumMortisManip.SESSIONS | – | – | – | – | – | – | – | no logout or stop clear |
| BloodFlowLedger.RECENT (UUID→source map) | – | – | – | – | – | – | – | grows per player for the server lifetime |
| SporiticThuribleEvents.LAST_YAW, LivingSpearCombatEvents | – | – | – | – | – | – | – | removed only on the in-game path |
| FoundingFaneEvents.FANE_BOUNDARY_PREVIEWS | – | – | – | – | – | – | – | debug-command state, never cleared |
| IchorianKnowledgeEvents (client sync) | ✔ | ✔ | – | **–** | n/a | n/a | n/a | dimension change missing |
| HarbingerCardinalRiteEvents (UUID maps) | ✔ | – | – | ✔ | – | ✔ | – | no respawn or logout |
| CircusDiscoveryProgress | – | ✔ | – | ✔ | – | – | – | no login |
| MycophantEncounterManager / VesperOrdealManager | ✔ | – | – | ✔ | – | – | – | rely on persistent data (acceptable) |
| ManipulationChannelManager, CastingAnimationManager, HematicRiposteEvents, ConductionManager | – | ✔ | – | ✔ | ✔ | mixed | – | no stop for some (ChannelManager, Riposte) |
| AxonalTransductionManager, EarlyInitiation | ✔ | – | – | ✔ | ✔ | ✔ | – | no respawn; dead-player paths rely on per-tick checks |
| MuscleMemoryEvents | ✔ | ✔ | – | ✔ | ✔ | – | ✔ | complete |
| StationUpgradeProgress.deliver | via level tick every 20 ticks | | | | | | | **no `isAlive` check** (see below) |

**ScaleGrip item loss (VERIFIED code path).**
- `ScaleGripEvents.onLivingDeath` (:31-56) removes the protected stacks from the inventory and parks them in a static map. It runs at NORMAL priority and never checks `isCanceled`.
- `ChamberVisitEvents.onDeath` (:84) and `MycophantEncounterManager.onDeath` (:413) also run at NORMAL priority and **cancel** the death. Their order relative to ScaleGrip is undefined.
- If ScaleGrip runs first, the player survives with the items stripped, and no `PlayerRespawnEvent` fires to return them.
- Nothing persists the map. A server stop, crash, or logout plus restart loses the items.
- A later real death with protected items **overwrites** the entry with `put` (:51), losing the earlier stash.
- The return path uses `Inventory.add`, which can mean a Creative-mode discard; AGENTS.md treats that as unconfirmed delivery.
- This breaks the AGENTS.md delivery rules: living-player delivery, confirmed insertion, and death persistence.

**Station kit delivery while dead (VERIFIED code; consequence HYPOTHESIS).**
- `HarbingerCardinalRiteEvents.java:196-200` calls `stationUpgrades(player).deliver(player)` every 20 ticks for every player in `sLevel.players()`, dead ones included.
- `StationUpgradeProgress.deliver` (:103-110) has no `isAlive` check and clears the pending bit as soon as `Inventory.add` empties the stack.
- A kit delivered to a dead player is likely lost on respawn without keepInventory, and Creative discard counts as delivered. Both contradict the "never deliver while dead" contract in AGENTS.md.

**OrganEchoItem wrong-slot deletion (VERIFIED code; consequence HYPOTHESIS).**
- `OrganEchoItem.inventoryTick` (:89-90) calls `setItem(slotId, EMPTY)` using the compartment-relative `slotId`.
- An echo in the offhand ticks with `slotId = 0`, so when a duplicate exists the handler clears **hotbar slot 0**, whatever item sits there.

---

## 6. Periodic network sync list

| Sender | Period | Scope | Diffed? |
|---|---|---|---|
| Equipment attribute churn (`HarbingerEquipmentEntityEventHandler:189`) | every update (2 ticks) | player + trackers | **no** |
| SheolicBastion / BlackVeil effect refresh | every tick | self | **no** |
| `BloodBindingEffect` particles | 4 per tick per bound entity | players within 32 blocks | no |
| `KnownManipulationEvents` | every tick (diff of a full serialization) | self | yes, but expensive (known) |
| `StationUpgradeEvents` → `sync(false)` | 20 ticks | self | yes (`lastSynced`); also re-unlocks Liber entries each call (known) |
| `BodyIdiomEvents` BLACK_HEART | 20 ticks, every player | 64+ blocks | **no** |
| `FoundingFaneEvents.syncFaneBoundaries` | 40 ticks, every level | every live player | **no** (sends an empty list too) |
| `ChamberOfWillManager.syncTo` | 40 ticks | Chamber players, 2 packets | **no** |
| `HarbingerCardinalRiteEvents.syncActiveRites` | 10 ticks while any rite is active | **all players in the level** | no (also sends on phase change) |
| `syncQliphothBlooms` | 200 ticks | all players | no |
| `BloodMoonEvents.syncToAllPlayers` | 200 ticks while active | all players | no (idempotent boolean) |
| `MycophantEncounterManager` → `syncDegree` | 20 ticks in Gardens | self | no |
| `RiteZoneSync`, `RiteSpaces.broadcast` | 20-tick gate | all players | **yes** (`consumeChanged`), the model to copy |
| `SchoolCombatEvents` | on change | trackers | yes |

---

## 7. Recommendations

**Low-risk, pure refactors (no behaviour change intended):**
1. Equipment elytra: only remove or add when the desired state differs (`hasModifier` vs. `canElytraFly`), and gate with `!isClientSide` (2.1).
2. `SchoolCombatEvents.tick`: `if (!target.hasData(SCHOOL_COMBAT)) return;` before `getData` (2.2). Optionally give the attachment a "should serialize when empty" filter.
3. StillwaterCondenser: cache `hasGhostPipe` and `latticeBoosted`, and re-check every 40 ticks or on `neighborChanged`.
4. `PendingBloodCraftManager.tick()`: call it from `ServerTickEvent.Post` (or tick only crafts whose `level == sLevel`), and add `clear()` to `TransientServerStateEvents`.
5. `getRiteByLocation`: use `getRecipeManager().byKey(loc)` and then the prefixed key; stop mutating the recipe with `setId` on every lookup.
6. BodyIdiom BLACK_HEART: send only on a saturation or active-state change (keep a last-sent value per player).
7. FoundingFane and Chamber `syncTo`: diff against the last payload per player, and skip empty-to-empty.
8. Effect refresh (Sheolic, BlackVeil): re-add only when `getEffect(...)` is missing or has fewer than about 10 ticks remaining.
9. VialRack and RecycledEnzyme: run the migration and identity logic server-side only, and skip it when a "migrated" marker exists; avoid `copyTag()` for read-only checks (use `CustomData.contains` / `read`).
10. `SerpentineIdolBlockEntity`: return a server-only ticker for the effect logic and a separate client-only ticker for particles; throttle to every 5 ticks.
11. Delete the dead `RenderBloodLaserEvent` registration (`ClientEvents.java:1066`).
12. `FloorMesh.of`: compute `sockets` and `standingCells` only on a cache miss.
13. Alembic and Retort: compute `findMatchingRecipe` once per tick, and skip it when not heated and the input is empty.

**Need measurement or a behaviour decision first:**
- **ScaleGrip:** move the stash into a `PlayerPersisted` attachment and check `isCanceled` at LOWEST priority. This is a behaviour change; follow the AGENTS.md delivery contract.
- **Station `deliver`:** add an `isAlive` check and confirmed-insertion counting (AGENTS.md contract).
- **IchorianKnowledge:** add a dimension-change resync, then verify in game.
- **OrganEcho:** fix the slot-index handling.
- **HemoRenderTypes:** convert the per-call factories to cached `RenderType`s with uniforms set in a per-draw callback. This touches shader lifecycles, so profile first.
- **Rite sync scope:** restrict it to players within tracking range of each rite.
- **Off-screen BERs and infinite AABBs:** measure frame time with and without.
- **Client entity-id caches:** clear them on `LoggingOut`. Cheap, but confirm no feature relies on persistence.

**How to measure:**
- **Server:** install **Spark** (`/spark profiler start --thread "Server thread" --timeout 120`) on a test world with 5+ players (or bots), a Blood Moon, an active rite with 8 braziers, a mob farm near a Serpentine Idol and several Stillwater Condensers. Compare `LevelTickEvent`/`EntityTickEvent` subscriber self-time.
- **Network:** use `/spark tickmonitor` plus the vanilla network debug (F3+L, or a packet logger such as the NeoForge `-Dneoforge.network.debug` option or the "Packet Logger" mod) to count `ClientboundUpdateAttributesPacket`, `ClientboundUpdateMobEffectPacket` and `hemomancy:manipulation_visual` per second.
- **Client:** record with **JFR** (`-XX:StartFlightRecording=duration=60s,settings=profile`) during a rite-plus-brazier scene and check allocation pressure in `HemoRenderTypes.*` and `RenderType.create`. NeoForge's F3 frame graph or `/neoforge tps` gives a coarse confirmation.
- **Save size:** compare a region file's NBT size before and after the `SCHOOL_COMBAT` fix (NBTExplorer), or use `/data get entity @e[limit=1,type=zombie]` to see the `neoforge:attachments` compound.

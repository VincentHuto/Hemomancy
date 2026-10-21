# Hemomancy audit 02: networking and synchronization

Scope: `common/network/` (184 files, 14,135 lines), the four payloads outside it (`common/rite/zone/RiteSpaceSyncPacket`, `RiteZoneSyncPacket`, `common/succession/ResidentsRequestPacket`, `ResidentsSnapshotPacket`), block-entity, entity and menu sync. This audit does not repeat the earlier findings on sync triplets, the per-tick `KnownManipulationEvents` diff, or the `StationUpgradeEvents` resync.

**Method.** I scanned every `CustomPacketPayload` implementor with a script, parsed all `net.play*` registrations in `PacketHandler.java`, and grepped `src/main`, `src/test` and `src/gameTest` for constructors, static factories and fully qualified references. I read the handlers of every serverbound payload, and checked NeoForge behaviour against `neoforge-21.1.251-sources.jar`.

**Evidence labels.** **[V]** means I verified it in code. **[H]** means it is a hypothesis that needs a runtime check.

---

## 1. Inventory

**Totals [V]:** 182 payloads, and 181 are registered. The one unregistered payload is `TeleportToVeinPacket`. 78 payloads are records and 104 are classes.

| Registration | Count |
|---|---|
| `playToClient` | 105 |
| `playToServer` | 43 |
| `playBidirectional` | 33 |

Of the 33 bidirectional registrations, only `AntecedentEffectPacket` actually travels both ways. 27 only go client to server, and 5 only go server to client. Details are in §2-P2.

### Payloads by purpose

| Purpose | # | Direction | Examples / state carried | Rough size |
|---|---|---|---|---|
| Player/attachment state sync | 43 | 40 S2C, 3 bidirectional | `BloodVolumeServerPacket`, `KnownManipulationServerPacket` (whole known/equipped lists), `PacketSyncIchorianKnowledge`, `PacketSyncUnstainedProgress`, `PacketSyncLiberKnowledge`, `PacketSyncScarsState`, `PacketEquipmentSync` (slot + ItemStack). 9 of these ship whole `CompoundTag`s (`PacketSyncSkills`, `BloodTendencyServerPacket`, `VascularSystemServerPacket`, `PacketSyncStationUpgrades`, `PacketSyncMuscleMemory`…) | 10 B to several KB |
| World/data sync | 12 | S2C | `PacketSyncActiveRites` (per-rite entries + position arrays), `RiteZoneSyncPacket`, `RiteSpaceSyncPacket`, `CardinalRiteFloorSyncPacket`, `BloodProfileSyncPacket`, `BloodInjectionSyncPacket`, `PacketSyncFaneBoundaries`, `PacketSyncBloodMoon`, `PacketSyncSutureLinks`, `ResidentsSnapshotPacket` | 0.1 to 10+ KB |
| Screen open / BE-menu state | 12 | S2C | `OpenSSCScreenPacket`, `OpenHarbingerAssignmentLedgerPacket` (306 lines), `OpenVisceralMirrorPacket`, `VisceralMirrorUpdatePacket`, `ClairaudiographChoicesPacket` | 0.1 to 2 KB |
| VFX / sound / animation | 36 | 34 S2C, 2 bidirectional | All 20 in `particle/`, plus `Sync{MonolithicDislocation,CrimsonFire}Visual`, `SyncBloodBindingTendril`, `CastingAnimationPacket`, `PacketBloodCraftRing`, `EarthenVeinTravelVisualPacket`… | 8 to 100 B |
| Boss/encounter scenes | 4 | S2C | `PacketSyncVesperFightScene`, `PacketSyncMycophantFightScene`, `PacketMycophantIntro`, `PacketSyncChamberOfWill` | small |
| Casting/keybind input | 24 | 13 C2S, 11 bidirectional | `UseManipKeyPacket`, `CastingChargePacket`, `ManipulationChargeVisualPacket`, `GroundBloodDrawPacket`, `BloodCraftingKeyPressPacket`, `EquipManipulationPacket`, `SynapticLoadoutActionPacket` | 1 B to an ItemStack |
| GUI/menu actions | 24 | 18 C2S, 5 bidirectional, 1 unregistered | Scar station ×4, `StartCentrifugeButtonPacket`, `ClairaudiographActionPacket`, `PacketLedgerAction`, `PacketKickBloodlinePlayer`, `PacketLumpDonate`, `PacketBloodlineMessage`, skills ×2, `SelectEarthenVeinDestinationPacket` | small |
| Screen-open requests | 9 | all registered bidirectional, used C2S | `PacketOpenScarsInv`, `OpenMorphlingJarPacket`, `PacketOpenTendencyView`… | 0 to 8 B |
| Pull/resync requests | 8 | 7 C2S, 1 bidirectional | `BloodVolumeClientPacket`, `KnownManipulationClientPacket`, `KnownSummonsRequestPacket`, `PacketRequestPoolData`, `ResidentsRequestPacket` | 0 B |
| Dialogue | 7 | 4 C2S, 3 S2C | `DialogueOptionPacket`, `DialogueRewardChoicePacket`, `DialogueTopicOpenedPacket`, `OpenDialoguePacket` (full tree) | up to KB |
| Debug | 1 | C2S | `PlaceStructurePacket` (441 lines) | small |
| Client chat toggles | 2 | bidirectional | `PacketToggleBinderMessage`, `ToggleMorphlingJarMessagePacket` (both dead) | 1 B |

Sizes come from reading the codecs. Byte counts are estimates **[H]**.

### `PacketHandler.java` (499 lines) [V]

- **Registration.** All 181 payloads are registered in one method, `onRegisterPayloads` (L76–375), as one long list. Banner comments group some of it ("Blood Volume capability", "Scar system", "Particles"…), but the top block (L78–113) has no grouping. Indentation mixes tabs and spaces, and 9 registrations use fully qualified class names.
- **Version string.** L77 calls `event.registrar(Hemomancy.MOD_ID)`. That argument is the network version (`RegisterPayloadHandlersEvent.registrar(String version)`), so the version is the constant `"hemomancy"` and never bumps. There is no `executesOn` or `optional()`, so every handler uses the default `HandlerThread.MAIN` (`PayloadRegistrar` L29).
- **Send helpers.** L383–465 hold 15 typed send helpers, all of them used. `sendToPlayer` (L471–493) also contains dialogue business logic: whisper queueing, Monolith context clearing and `SuccessionDialogue.decorate`.

---

## 2. Competing patterns

### P1. How payloads are declared
- **Current split [V]:** 78 records and 104 classes. 180 use `StreamCodec.of`, 2 use `StreamCodec.composite` (`RiteSpaceSyncPacket`, `RiteZoneSyncPacket`) and 1 uses `StreamCodec.unit` (`ManipulationInterruptedPacket`). There are no `ofMember` or `ByteBufCodecs` codecs. 172 codecs are typed `StreamCodec<FriendlyByteBuf,…>`, but 9 of those cast to `(RegistryFriendlyByteBuf)` to read ItemStacks (for example `PacketGourdScarSync.java:38`, `BloodCraftingKeyPressPacket.java:61`, `PacketSyncScarsState`).
- **Forge-style leftovers:**
  - `toBytes` methods plus `(FriendlyByteBuf)` constructors in 3 files (`PacketGourdScarSync`, `PacketEquipmentSync`, `SyncTrackingAvatarPacket`).
  - Nested `Handler` classes in 7 files (`UpdateCurrentManipPacket`, `UpdateCurrentVeinPacket`, `PacketLoadScarPattern`, `PacketScarCraftingEvent`, `PacketUpdateScarPattern`, `ChangeMorphKeyPacket`, `PacketUpdateLivingStaffMorph`).
  - A `ctxSupplier` parameter name in 7 files.
- **Type IDs:** 125 use `Hemomancy.rloc`, 6 use `ResourceLocation.fromNamespaceAndPath("hemomancy",…)`, and the rest are multi-line `rloc` calls. There are no duplicate IDs.
- **Recommendation:** use a record with `TYPE`, then `STREAM_CODEC`, then a static `handle`. Use `StreamCodec.composite` with `ByteBufCodecs` up to 6 fields, and private static `encode`/`decode` otherwise. `CastingAnimationPacket` is a good template. Codecs that touch ItemStacks or components should be typed `RegistryFriendlyByteBuf` instead of casting.
- **Simplification:** about 100 class bodies lose roughly 20 lines each of boilerplate (around 2k lines).
- **Difficulty:** Low per file, Medium overall. **Compatibility risk:** Low if the byte layout is kept, for example `ByteBufCodecs.INT` rather than `VAR_INT` where the old code called `writeInt`. Change only files you are already touching.

### P2. `playBidirectional` used as the Forge-era "either direction" default [V]
- **Only truly bidirectional:** `AntecedentEffectPacket`. It branches on `context.player() instanceof ServerPlayer` (L16–20).
- **Server-to-client only, with client code in the handler:**
  - `PacketEquipmentSync` (`PacketHandler.java:162`)
  - `PacketGourdScarSync` (:165)
  - `SyncTrackingAvatarPacket` (:203)
  - `PacketCurvedHornAnimation` (:175, calls `client.event.LayerEvents::playHornAnimation`)
  - plus the dead `PacketToggleBinderMessage` and `ToggleMorphlingJarMessagePacket`
- **Client-to-server only (27):** the scar, manipulation and morphling inputs, the open requests, and `CPacketFlight`. Several of them still carry `if (!player.level().isClientSide)` guards, which only exist because of the bidirectional registration (`UseManipKeyPacket.java:62`, `UpdateCurrentManipPacket.java:36`, `GroundBloodDrawPacket.java:50`).
- **Recommendation:** register the real single direction. Either split `AntecedentEffectPacket` into two payloads or keep it as the one documented exception.
- **Difficulty:** Low. **Compatibility risk:** Low, because the channel IDs don't change. Negotiated flows do change, but mixed builds are not supported anyway (see D2).

### P3. Where client-side handling lives [V]
Among the 105 `playToClient` handlers:

| Handler style | Count |
|---|---|
| Delegates to a class in `client/` | 66 (about 65 distinct client classes; no single client dispatcher) |
| Side-agnostic `ctx.player()` work | 27 |
| Lambda inside `PacketHandler` | 3 (`ManipulationInterruptedPacket`, `ConductionPathPacket`, `NerveLinkPacket`, L103–108) |
| Other | 9 |

`Minecraft.getInstance()` or `net.minecraft.client.*` is imported directly in 8 `common/network` files:
- `SyncTrackingAvatarPacket:5,45`
- `PacketGourdScarSync:7,55`
- `PacketToggleBinderMessage:4-5,33`
- `PacketEquipmentSync:5,47`
- `PacketSyncScarsState:7,60`
- `SyncEquipmentLayerVisibilityPacket:7,51`
- `SyncSchoolCombatS2CPacket:5,21`
- `ToggleMorphlingJarMessagePacket:4-5,27`

Two files guard with `FMLEnvironment.dist` (`PacketSyncLiberKnowledge:152`, `PacketSyncUnstainedProgress:181`).

The dedicated server already loads these classes for `TYPE`/`STREAM_CODEC` and evidently starts. The client references sit inside lambda bodies, which resolve lazily, so the classloading risk is latent rather than immediate **[H: confirm with `runServer` plus a crafted packet]**. The live risk is P2: three of these handlers can be invoked on the server.

- **Recommendation:** keep the majority pattern, where `handle` forwards to a `client/…` static. Move the 8 inline handlers out of `common/network`.
- **Difficulty:** Low. **Compatibility risk:** None.

### P4. `enqueueWork`
- 173 of 182 payloads call `ctx.enqueueWork`; 9 don't (the 3 `PacketHandler` lambdas wrap it, and `particle/Spawn{Avatar,Flask,BloodClaw,LivingTool}` call `addParticle` directly).
- Under NeoForge 21.1 this is **redundant but harmless [V]**:
  - The default `HandlerThread.MAIN` wraps every handler in `MainThreadPayloadHandler`.
  - `ServerPayloadContext.enqueueWork` runs inline when already on the main thread (L31–34).
- Do not "fix" the 9 non-enqueueing handlers as if they were threading bugs.
- **Recommendation:** omit `enqueueWork` in new code. Remove it only opportunistically.
- **Difficulty:** Trivial. **Compatibility risk:** None.

### P5. How payloads are sent [V]

| Send call | Count |
|---|---|
| `PacketHandler.sendToPlayer` | 229 |
| `PacketHandler.sendToServer` | 83 |
| Direct `PacketDistributor.sendToPlayer` | 28 (in 23 files) |
| `PacketDistributor.sendToPlayersNear` | 30 (18 files) |
| `PacketDistributor.sendToPlayersTrackingEntityAndSelf` | 18 (14 files) |
| `PacketDistributor.sendToAllPlayers` | 8 |
| `PacketDistributor.sendToPlayersInDimension` | 1 |
| `PacketDistributor.sendToServer` | 2 |

Direct `PacketDistributor.sendToPlayer` calls skip the `player.connection == null` guard used for GameTests and the `OpenDialoguePacket` routing. No direct call sends `OpenDialoguePacket` today, so nothing is broken yet. Examples of direct calls: `EquippedMorphlingEvents.java:460`, `ScarStateSyncEvents.java:61`, `RiteSpaces.java:97`, `StationUpgradeProgress.java:88`.

- **Recommendation:**
  - Make `PacketHandler` the only facade, with generic `sendNear(level, pos, radius, p)` and `sendTracking(entity, p)` instead of 15 per-effect helpers.
  - Move the dialogue routing in `sendToPlayer` (L478–491) into a `DialogueSender`.
- **Difficulty:** Medium (about 85 call sites). **Compatibility risk:** None.

### P6. Validation code is duplicated (details in §3)
- **Recommendation:** add one `ServerboundGuards` utility with these checks:
  - `menu(player, Class<M>)`: an instance check plus `stillValid`
  - `nearBlock(player, pos, maxSq)`: loaded chunk plus block type plus distance
  - `nearEntity(player, id, max)`
  - `finite(double…)`
  - one shared cast-eligibility predicate
- **Difficulty:** Medium. **Compatibility risk:** None for the wire format. Re-run the gameplay GameTests.

### P7. Block-entity sync [V]
- **Counts:** 63 block entities, with 49 extending `BlockEntity` directly and 12 `BaseContainerBlockEntity`. There is **no Hemomancy sync base class**.
  - 38 override `getUpdateTag` and 39 `getUpdatePacket`.
  - 20 override `onDataPacket` and 18 `handleUpdateTag`. These overrides are mostly redundant with NeoForge's defaults **[H: check whether each override calls `loadAdditional` or `loadWithComponents` before deleting it]**.
  - 33 call `sendBlockUpdated`.
- **Four names for the same helper:** `sendUpdates` (18), `markDirtyAndSync` (5), `sync` (3), `setChangedAndSync` (2). Between them they have 7 different bodies, which vary in whether they call `setBlocksDirty`, use `UPDATE_ALL` vs `3`, and how they handle a null level.
- **A second mechanism:** HutosLib's `VanillaPacketDispatcher.dispatchTEToNearbyPlayers` has 12 call sites. In several places it is layered on top of `sendBlockUpdated`:
  - `SomaticLoomBlockEntity.java:1196-1199` (`sendUpdates()` followed by a dispatch)
  - `ScarStationBlockEntity.java:281-282`
  - `ScarStationBlock.java:65-69`: `sendUpdates()` plus **two** dispatches, which is three block-entity syncs per right-click
- **Custom block-entity payloads** are used legitimately for per-viewer progress, for example `VisceralMirrorUpdatePacket` from `VisceralMirrorBlockEntity:317,327`.
- **Recommendation:** add a small `HemoSyncedBlockEntity` base, or a static `BlockEntitySync.markAndSync(be)`. Standardize on `setChanged()` plus `sendBlockUpdated(…, UPDATE_CLIENTS)`, and drop the stacked dispatches.
- **Difficulty:** Medium. **Compatibility risk:** None, since it is the same vanilla packet. Saved NBT is untouched.

### P8. Entity sync [V]
- 256 `SynchedEntityData.defineId` calls across 85 files. No custom serializers, and no `IEntityWithComplexSpawn`.
- Entity-targeted custom payloads (about 26 carry an entity ID) are almost all player-attachment state or VFX. Bespoke packets are reasonable here: NeoForge 21.1 has no attachment sync **[H: confirm in 21.1.251 javadoc]**.
- **Recommendation:** no change. Keep `SynchedEntityData` for mob state.

### P9. Menu and GUI actions [V]
- 25 menus. Only 2 use `clickMenuButton` (`EnzymaticScriptoriumMenu`, `ResonantForgeMenu`). 12 use `ContainerData` or `addDataSlots`.
- Generic action payloads come in several shapes:
  - `PacketLedgerAction`: raw `int` constants, L63–66
  - `SynapticLoadoutActionPacket`: enum
  - `PacketPuppeteersSpindleAction`: enum plus a String
  - `PacketCrossbarRadialAction`: enum
  - `ClairaudiographActionPacket`: carries `containerId`, a version, an action and an index; this is the best model
  - `UseManipKeyPacket.Action`
- `StartCentrifugeButtonPacket` is a pure button with no data.
- **Recommendation:**
  - For menu buttons with no data, use `clickMenuButton`. Vanilla validates the container ID and `stillValid`.
  - For actions carrying data, follow the Clairaudiograph shape (container ID plus enum plus arguments).
  - Use enums instead of raw ints.
- **Difficulty:** Low to Medium. **Compatibility risk:** Low.

### P10. VFX payloads that only trigger a visual
- **Already data-driven:** `ManipulationVisualPacket` dispatches on `ManipulationVisuals.Form`, an enum of about 100 values, and is constructed at 23 call sites. That is the right model.
- **Same wire shape, different particle factory [V]:**
  - 4 packets carry pos plus color: `SpawnAvatarParticlesPacket`, `SpawnFlaskParticlesPacket`, `SpawnBloodClawParticlesPacket` and `SpawnLivingToolParticlesPacket`. A diff of Avatar against BloodClaw shows only the type ID and the particle factory changing.
  - 2 packets carry an entity ID plus a duration: `SyncMonolithicDislocationVisualS2CPacket` and `SyncCrimsonFireVisualS2CPacket`.
  - The pos-only pair `SpawnPomePulsePacket` / `SpawnMonolithShatterBurstPacket` is a close cousin.
- **Recommendation:** fold these 6 to 8 into a `ParticleBurstPacket(kind, pos, color)` and an `EntityVisualPacket(kind, entityId, ticks)`. Leave the bespoke-geometry payloads alone (`BloodFlowPacket`, `ManipulationFlowPacket`, `SpawnClawSlashPacket`, `SpawnSanguineOmenEffectPacket`, `SpawnBlackVeilPacket`, `CardinalRiteImpactPacket`, `SpawnBrazierItemAbsorptionParticlesPacket`); merging them would only move the switch somewhere else.
- **Simplification:** about 6 classes and 6 registrations removed.
- **Difficulty:** Low. **Compatibility risk:** Low (new type IDs; both sides ship together).

**Refactor hazard [V]:** 22 source-text tests read `PacketHandler.java` as a string and assert things like `contains("RiteZoneSyncPacket.TYPE")`. They include `RiteZoneBoundaryRulesTest` and `HarbingerBestiaryTabSourceTest`. Splitting or reorganizing the registration will break them, so update them together with any change.

---

## 3. Server-side validation

### Good patterns already in place [V]
- `UseManipKeyPacket.java:145-146` clamps the client-reported charge through `ManipulationCastingRules.clampReportedCharge`, which rejects non-finite values.
- `PacketUpdateScarPattern.decode` enforces an 8×8 grid.
- `PacketOpenScarsInv.java:48-53` checks the loaded chunk, the block type and `MAX_USE_DISTANCE_SQR`.
- `ArborFruitInteractPacket` checks the dimension, the owner and the reach.
- `DialogueTopicOpenedPacket` checks the namespace, the path length and an 8-block entity distance.
- `PacketCrossbarRadialAction` caps the string length.
- `PacketBloodlineMessage` caps messages at 256 characters.

### Duplicated validation [V]
- **Cast eligibility** (equipped, available, `isActive()`, `blocksKnownBloodPowerUse`, tendency alignment, cooldown, alive) is re-implemented in:
  - `ManipulationChargeVisualPacket.java:25-38`
  - `CastingAnimationManager.canPresent` (L78–89)
  - `UseManipKeyPacket.java:63-65,218`
  - `UseQuickManipKeyPacket:47` and `UseContManipKeyPacket:41` (both dead)
  - `ManipulationChannelManager:117`, `AvatarManifestationManager:92`, `BloodManipulation:404`
- **Menu guard** (`containerMenu instanceof X && stillValid`) appears in `PacketUpdateScarPattern:54`, `PacketScarCraftingEvent`, `StartCentrifugeButtonPacket`, `PacketPuppeteersSpindleAction` and `PacketLoadScarPattern`. `ClairaudiographActionPacket:14` checks `containerId` instead. Vanilla's per-tick `stillValid` makes this guard partly redundant either way.
- **NPC speaker and distance checks** are spread across `DialogueOptionPacket.dispatch:73` (`distanceTo > 8.0`), `DialogueTopicOpenedPacket` (8.0F), `ClinicalBloodKnowledge.teach:65` (`distSqr > 64`), `StationUpgradeDialogue.allowedSpeaker`, and per-event `instanceof` checks in both `DialogueOptionPacket` (L74–103) and the 1,561-line `DialogueEventHandler`.
- **Bloodline authority** is spelled two ways: `PacketKickBloodlinePlayer:58` compares the leader UUID directly, while `PacketLedgerAction:259,299` calls `canManage`. They are semantically identical.

### Missing or weak validation (cheat surface)
1. **`PacketLumpDonate` accepts NaN [V by reading].** `Math.min(NaN, vol)` is NaN, and `NaN <= 0` is false. `BloodVolume.drain(NaN)` then passes `wouldOverstrain` (`vol - NaN < 0` is false) and sets the player's volume to NaN. Next, `Bloodline.contributeBlood((float)NaN)` reaches the `bloodVolume < maxBloodVolume` branch and **fills the bloodline pool to max**. This needs a modified client. Fix: reject the value unless `Double.isFinite(amount) && amount > 0`.
2. **`PacketUpdatePoolSettings:55-57` [V].** `Math.max/min` pass NaN straight through to the trickle rate and the auto-draw threshold. Impact unknown **[H]**.
3. **`GroundBloodDrawPacket:51-56` [V].** A client float, `parTick`, goes unclamped into `player.pick(6, pTic, false)`. Extrapolating the eye position with a large partial tick lets a moving player extend reach **[H on magnitude]**. Clamp it to [0,1], or use 1.0F on the server. The packet is also sent every tick while the key is held (`ClientEvents.java:341-342`).
4. **`PacketLedgerAction:82-95` [V].** Its 4 actions (summon NPCs, recall to lodge, set recall point, disband) check leadership but not whether the player holds or owns the `UnsignedLedgerItem`. The handlers were "moved from UnsignedLedgerItem" (L98). A client can trigger them without the item.
5. **`PacketBloodlineMessage:42-77` [V].** A broadcast with no rate limit that bypasses vanilla chat: no signing, no spam kick, no mute handling, no chat logging. A spam vector.
6. **`PlaceStructurePacket:87` [V].** The only check is `isCreative()`. There is no op permission and no check that the player holds the Structure Spawner, so any creative-mode player on a server can paste rites, floors and seeded offerings. Low severity.
7. **`DialogueOptionPacket` [V/H].** `entityId == 0` means "no speaker", and arbitrary `eventId`s are dispatched as `DialogueEvent`. There is no server-side open-session or tree-membership token, so safety depends on every branch of `DialogueEventHandler` re-checking its speaker. The branches I spot-checked do (station claims, clinical lessons, Annetta). I did not check them exhaustively **[H]**.
8. **Server-to-client-only packets that a client can send (P2) [V].** Sending `PacketEquipmentSync`, `PacketGourdScarSync` or `SyncTrackingAvatarPacket` to a dedicated server runs `Minecraft.getInstance()` on the server thread. That is presumably a `NoClassDefFoundError`, which `NetworkRegistry.guard` logs rather than disconnecting, giving a log-spam vector. On an integrated server, a LAN guest could make the host's server thread modify the host's `ClientLevel` **[H on exact behaviour]**.
9. **Pull requests are unthrottled [V].** 7 live pull requests (for example `BloodVolumeClientPacket`, `KnownManipulationClientPacket`, `PacketRequestPoolData`) answer every request with a full resync. Low severity.

---

## 4. Dead and unregistered payloads

**Unregistered but sent: `TeleportToVeinPacket` [V].**
- Its registration was removed in commit `fa30de5ae` and replaced by `OpenEarthenVeinDisplayPacket` and `SelectEarthenVeinDestinationPacket`.
- `RadialChooseVeinScreen.java:121,155` still sends it, but nothing opens that screen; a grep found no references. It is latent: if anything reopened the screen, `NetworkRegistry.checkPacket` would throw "may not be sent to the server".
- `TeleportToVeinPacketSourceTest` still asserts on its source.

**Registered but never sent (18).** For each one I grepped the class name across `src/main`, `src/test` and `src/gameTest`, including fully qualified uses and `PacketHandler` helpers, and found nothing besides imports, its registration and source-text tests.

| Payload | Confidence | Note |
|---|---|---|
| `UseQuickManipKeyPacket`, `UseContManipKeyPacket` | Verified | Superseded; see `UseManipKeyPacket.java:39-40`. Source tests still read `UseQuickManipKeyPacket`. |
| `PacketOpenNormalInv`, `PacketOpenSporeInv`, `PacketOpenScarBinder`, `PacketLoadScarPattern` | Verified | |
| `BloodTendencyClientPacket`, `VascularSystemClientPacket` | Verified | Their server-to-client replies are live via other paths. |
| `ClearLoomStatePacket`, `DisplayKnownManipsPacket`, `UpdateCurrentVeinPacket` | Verified | |
| `ChangeMorphKeyPacket`, `JarTogglePickupPacket` | Verified | |
| `PacketToggleBinderMessage`, `ToggleMorphlingJarMessagePacket` | Verified | Bidirectional with `Minecraft.getInstance`. |
| `EntityHitParticlePacket` | Verified | |
| `AirBloodDrawPacket` | Verified | Its handler is empty (L24). |
| `PacketOpenVascularView` | Likely dead | Only sent by `GameplayCampaignDriver.java:190` (gameTest). |

---

## 5. Hotspot files

| File (`common/network/…`) | Lines | Issue |
|---|---|---|
| `capa/harbinger/BloodCraftingKeyPressPacket.java` | 802 | Blood-crafting gameplay lives inside the packet. It sends the client's main-hand ItemStack (L61/66/792), which the server never reads. |
| `PacketHandler.java` | 499 | 181 flat registrations, typed VFX helpers, dialogue routing |
| `capa/harbinger/PacketLedgerAction.java` | 449 | NPC summon, recall and disband logic in a packet class |
| `PlaceStructurePacket.java` | 441 | Structure, rite and floor placement in a packet class |
| `capa/harbinger/manips/SynapticLoadoutActionPacket.java` | 319 | Loadout logic |
| `mission/OpenHarbingerAssignmentLedgerPacket.java` | 306 | Large screen DTO |
| `capa/harbinger/manips/UseManipKeyPacket.java` | 296 | Cast dispatch, duplicated eligibility checks |
| `capa/harbinger/BloodStructureCraftingHelper.java` (+3 `*Rules`) | 242 (+178) | Non-payload gameplay code in `network/`, imported by `event/BloodStructureFeedManager`, `rite/harbinger/WorkingCraftRites` and `tile/…/IronBrazierBlockEntity`, so the dependency points the wrong way |
| `particle/GroundBloodDrawPacket.java` | 240 | Gameplay in the packet; unclamped `parTick` |
| `capa/harbinger/PacketSyncActiveRites.java` | 239 | Hand-rolled nested codec |
| `capa/harbinger/manips/KnownManipulationServerPacket.java` | 201 | Whole-attachment sync |

An ignored backup file, `common/network/PlaceStructurePacket.java~`, sits in the source tree. It is untracked (`.gitignore:76`).

---

## 6. Observed defects (reported only, not fixed)

- **D1 [V]:** `TeleportToVeinPacket` is unregistered but still sent from the unreachable `RadialChooseVeinScreen` (see §4).
- **D2 [V]:** The network version is the constant mod ID (`PacketHandler.java:77`). Mismatched client and server builds are not refused at the handshake, so they fail later at decode time.
- **D3 [V]:** A NaN donation fills the bloodline pool (`PacketLumpDonate.java:56`; §3 item 1).
- **D4 [V]:** Six server-to-client-only payloads are registered bidirectional, and the live ones run client-only code when a client sends them (§3 item 8).
- **D5 [V]:** `BloodCraftingKeyPressPacket` serializes the full main-hand ItemStack, including components, on every keypress and never reads it on the server. That is wasted bandwidth and extra client-controlled decode surface.
- **D6 [V]:** Stacked block-entity syncs: three per Scar Station right-click (`ScarStationBlock.java:65-69`), and two per Somatic Loom mutation (`SomaticLoomBlockEntity.java:1196-1199`).
- **D7 [V]:** `AirBloodDrawPacket` is registered with an empty handler and never sent.
- **D8 [H]:** Client-supplied partial ticks in `GroundBloodDrawPacket` allow reach extrapolation.
- **D9 [V]:** The ledger's four actions run without any ledger-item check on the server.

### No Forge-era API remains [V]
There are no `SimpleChannel`, `NetworkEvent.Context`, `NetworkDirection` or `PacketDistributor.PLAYER.with(...)` uses. The remaining vestiges are stylistic and listed under P1 and P2.

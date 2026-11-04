# Runtime robustness, performance and multiplayer correctness

Audit date: 2026-11-02, branch `neo-1.21.1` at `8d01a316c` plus the working tree. Read-only static audit: nothing was
built, run or profiled.

Paths are relative to `src/main/java/com/vincenthuto/hemomancy/common/`, except:

- paths starting with `client/`, `compat/` or `config/`, which are relative to `src/main/java/com/vincenthuto/hemomancy/`;
- paths starting with `src/` or `docs/`, which are relative to the repository root.

This builds on `docs/architecture/ARCHITECTURE_AUDIT.md` (sections C.11 and F) and
`docs/architecture/evidence/11-performance-lifecycle.md`. Pending decisions keep their ids from
`docs/architecture/CONSOLIDATION_PROGRESS.md`; they are cited, not re-reported.

## Summary

I read all 116 server tick subscribers (68 `PlayerTickEvent`, 24 `LevelTickEvent`, 11 `ServerTickEvent`, 13
`EntityTickEvent`), the block-entity tickers of the 48 blocks that override `getTicker`, the 17 `inventoryTick` items,
the 15 effects that tick every tick, the 32 client tick handlers and the world-effect render passes, and all 62
serverbound payloads (61 `playToServer` plus the server branch of `AntecedentEffectPacket`). The networking
consolidation holds up: payload handlers run on the main thread, menus are re-checked, numbers are clamped, and the
F-1/F-6/F-7/F-8/F-9 fixes are in place. The problems are elsewhere. There are **26 findings: 1 Critical, 4 High,
7 Medium, 14 Low**.

- **Critical (RT-17):** three item-backed menus (Morphling Jar, Living Syringe, Scar Binder) can be duplicated with a
  vanilla client. The fix already exists in `MnemonicFolioMenu`.
- **High:**
  - RT-1: every blood payment or income sends 5 packets and rebuilds 4 diagnostics, and several sources do this every
    tick.
  - RT-2: blood routing force-loads every linked chunk every 10 ticks.
  - RT-18: a forged `memo_capture:` dialogue event unlocks any Liber entry, including the D8 rite-discovery gates, and
    grows saved player data without bound.
  - RT-19: block-editing manipulations and payloads skip spawn-protection, adventure-mode and claim checks.
- **Prior audit:** of the 11 performance items (P-1 to P-11), 4 are fixed (P-1, P-8, P-9, P-11), P-10 is mostly fixed,
  and P-2 to P-7 are still present (RT-3 to RT-8, RT-11).

No new problems turned up in dimension transfer, persistence round trips or config load order. The heuristic sweeps
behind the last two are described under "Not a problem".

---

## Tick subscribers

Verdicts:

- **OK:** gated cheaply or bounded.
- **Watch:** real but modest cost, or cost that scales with content.
- **Hot:** measurable cost in normal multiplayer play.

### Server: `EntityTickEvent` (runs for every entity in every loaded level)

| Handler | Side / gate | Per-tick work | Verdict |
|---|---|---|---|
| `damage/SchoolCombatEvents.tick` :266 | server, every `LivingEntity` | `getData(SCHOOL_COMBAT)` creates the attachment on every living entity; sends when dirty | Hot (RT-3) |
| `damage/SchoolAwareness.tick` :35 | server, every `Mob` | target and brain memory; perception only with a target | OK |
| `mission/circus/CircusDemonstrations.beforeTick` :23 | server, every entity | persistent-data read; teacher `AABB(16)` every 40 t | OK |
| `mission/circus/CircusPracticalController.cleanup` :102 | server, every entity | `getPersistentData().hasUUID` creates an empty tag on every entity | Watch (RT-11) |
| `entity/summon/puppeteer/PuppetStrings.beforeTick` :80 | server, every `Mob` | persistent-data read | OK |
| `entity/boss/endgame/VesperEncounterPuppetEvents.leash` :62 | server, `LEASH_INTERVAL` | puppets only | OK |
| `event/MorphicNectarEvents.onEntityTick` :58 | server, every `ItemEntity` | fluid lookup; out of fluid it removes 2 persistent keys (creates the tag); in fluid a recipe scan each tick | Watch (RT-11) |
| `event/RenderingPoolEvents.onEntityTick` :36 | server, every `ItemEntity` | the same pattern for bile | Watch (RT-11) |
| `event/WhiteHumorPurificationEvents` Pre :50 / Post :63 | server, every `ItemEntity` | two recipe matches per tick while in white humor | Watch |
| `event/ViridSalisTrailHandler.onEntityTick` :65 | server, hashed to 1/20 | one `getBlockState` | OK |
| `event/worldevent/ChamberOfWillEvents.onEntityTick` :110 | server, `ItemEntity` in the Chamber | owner lookup | OK |
| `rite/zone/RiteBoundEvents.onEntityTick` :84 | server, `ENTITY_CHECK_INTERVAL` | rite-space lookup | OK |

### Server: `LevelTickEvent` (runs once per loaded level per tick)

| Handler | Gate | Per-tick work | Verdict |
|---|---|---|---|
| `capability/player/harbinger/bloodvolume/routing/BloodRoutingEvents.onLevelTick` :44 | 10 t | `getBlockEntity` on every saved link, loaded or not | Hot (RT-2) |
| `capability/player/harbinger/morphling/EquippedMorphlingEvents.onLevelTick` :304 | skipped when empty | temporary webs | OK |
| `encounter/ChamberOfWillManager.onLevelTick` :490 | 40 t, Chamber only | room upkeep; 2 packets per player regardless of change | Watch (RT-7) |
| `encounter/VagrantMindEncounterManager.onLevelTick` :59 | End only | attempt update; restores are `hasChunkAt`-guarded | OK |
| `entity/npc/court/CourtSchools.onLevelTick` :122 | `CHECK_INTERVAL` | biome probe, per player | OK |
| `entity/npc/court/CourtVacancies.onLevelTick` :86 | `CHECK_INTERVAL` | `isLoaded`-guarded | OK |
| `event/HeartNodeEnvelopeSync.onLevelTick` :55 | `CHECK_TICKS`, Gardens only | per-player sync, diffed by `LAST_SENT` | OK |
| `event/worldevent/BloodMoonEvents.onLevelTick` :136 | overworld only | state checks | OK |
| `event/worldevent/FoundingFaneEvents.onLevelTick` :38 | 40 t, every level | a packet to every living player even with no fanes; Blood Moon `AABB` per fane | Watch (RT-7) |
| `item/harbinger/memory/HematicMemoryToolEvents.onLevelTick` :74 | 20 / 10 t | vein-spider inventory scan; effigies: a `Mob` query of radius 96 per player | Watch (RT-9) |
| `manipulation/congeatio/TemporaryIceManager.onLevelTick` :93 | every tick | ice entries; `getBlockState` with no loaded check | Watch (RT-25) |
| `manipulation/ductilis/ConductionManager.onTick` :222 | per pulse | graph rebuild plus `AABB(13)` per network | OK |
| `manipulation/ferric/FerricContacts.tick` :25 | every tick | prune | OK |
| `manipulation/HematicCommandManager.onLevelTick` :259 | skipped when empty | commanded mobs | OK |
| `manipulation/ManipulationReactiveEvents.onLevelTick` :368 | every tick | gaze slams and eclipse wells | OK |
| `manipulation/tenebris/BlackVeilCovenantManager.onLevelTick` :74 | 5 t | `AABB` per veil | OK |
| `rite/harbinger/HarbingerCardinalRiteEvents.onLevelTick` :98 | every tick, every level | `PendingBloodCraftManager.tick()` is not level-scoped (:102, F-11); linear recipe lookups per rite; recovery and kit delivery every 20 t (:118, F-19); rite sync every 10 t to every player in the level | Watch (RT-7, RT-8) |
| `rite/harbinger/QliphothBloomEvents.onLevelTick` :87 | interval | `AABB` per bloom | OK |
| `rite/harbinger/SanguineDominionEvents.onLevelTick` :31 | interval | `AABB` per zone | OK |
| `rite/unstained/LetheCovenantEvents.onLevelTick` :39 | interval | per player | OK |
| `rite/unstained/PaleConsecrationEvents.onLevelTick` :39 | interval | `AABB` per zone | OK |
| `rite/zone/RiteZoneEvents.onLevelTick` :31 | overworld, 20 t | diffed broadcasts (the model to copy) | OK |
| `world/AdipocereCrustDecoration.onLevelTick` :74 | queue-gated | `CHUNKS_PER_TICK` through `getChunkNow` | OK |
| `worldgen/terrain/cortical/CorticalArcManager.onTick` :49 | End, staggered per player | palette-filtered scan | OK |

### Server: `ServerTickEvent`

| Handler | Gate | Per-tick work | Verdict |
|---|---|---|---|
| `capability/PlayerStateSync.flushDirty` :197 | every tick | dirty predicates per player (allocates `Entry.values()`) | OK |
| `effect/ChummedWatersAreaManager.onServerTick` :52 | every tick, all levels | prune; particles every 12 t | OK |
| `effect/LanternTickLightManager.onServerTick` :42 | refresh interval | per-player light | OK |
| `entity/npc/harbinger/succession/SuccessionReconciliation.tick` :14 | 100 t | reservations | OK |
| `item/harbinger/tool/living/LivingAxeLodestoneFields.tick` :106 | every tick | fields, `hasChunkAt`-guarded | OK |
| `manipulation/FerricDuctilisStatusVisuals.tick` :88 | every tick | tracked entries | OK |
| `manipulation/ManipulationStatusVisuals.tick` :44 | every tick | pending list | OK |
| `manipulation/MortemStatusVisuals.tick` :29 | every tick | infections | OK |
| `manipulation/ThermalStatusVisuals.tick` :35 | every tick | active entries | OK |
| `mission/vicar/VoyagerVesselGuidance.tick` :53 | at most 2 probes per tick, time-budgeted | structure probe | OK |
| `network/capa/harbinger/PacketLedgerAction.onServerTick` :396 | skipped when empty | pending summons | OK |

### Server: `PlayerTickEvent.Post` (68 handlers, per player per tick)

| Handler | Side / gate | Per-tick work | Verdict |
|---|---|---|---|
| `armor/ability/EdaciousBloodburstArmorAbilityHandler.onPlayerTick` :60 | server, every tick | `FlightArbiter.grant` runs `onUpdateAbilities` (sends an abilities packet) every tick while the set is worn | Watch (RT-6) |
| `armor/ability/SheolicBastionBloodlustArmorAbilityHandler.onPlayerTick` :69 | server, every tick | re-adds Fire Resistance every tick (:123), and Slowness while Bastion is active (:146) | Watch (RT-6) |
| `armor/ability/SilentArchonArmorAbilityHandler.onPlayerTick` :183 | both, every tick | set check and movement | OK |
| `brewing/AdvancedBrewEffects.onTick` :64 | server, every tick | persistent-data reads | OK |
| `capability/PendingDelivery.onPlayerTick` :124 | server, 20 t | `deliverAll` | OK |
| `capability/player/harbinger/bloodvolume/BloodVolumeEvents.playerTick` :70 | server, every tick | `MaxBloodLedger.apply`, enzyme scan (D2+), per-tick income each followed by `syncVolume` | Hot (RT-1) |
| `capability/player/harbinger/circus/CircusDiscoveryProgress.onPlayerTick` :38 | server, 20 t | `getStructureWithPieceAt` plus an unconditional packet | Watch (RT-7) |
| `capability/player/harbinger/court/CourtDiscoveryProgress.onPlayerTick` :137, `CourtLife.onPlayerTick` :170 | server, interval | structure and house lookups until the Court is known | OK |
| `capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.onGlideTick` :40, `.playerTick` :141 | both, every tick | equipment tick; `AttributeModifiers.sync` (:146, P-1 fixed) | OK |
| `capability/player/harbinger/manip/KnownManipulationEvents.refreshPlayerDimensions` :41 | both, every tick | `refreshDimensions()`, which posts `EntityEvent.Size` | Watch (RT-10) |
| `capability/player/harbinger/morphling/EquippedMorphlingEvents.playerTick` :65, `MorphlingPowerEvents.onPlayerTick` :134 | server / both | morphling upkeep and passives | OK |
| `capability/player/harbinger/musclememory/MuscleMemoryWorldEvents.tick` :78 | server, every tick | a snapshot record per player | OK |
| `capability/player/harbinger/organs/VisceralOrgansEvents.onPlayerTick` :49 | server, 40 t | organ effects | OK |
| `capability/player/harbinger/scar/ScarsEntityEventHandler.playerTickScars` :66 | both, every tick | scar tick and 3 helpers | OK |
| `capability/player/harbinger/vascular/VascularSystemEvents.playerTick` :174, `capability/player/unstained/PurityGainEvents.onPlayerTick` :328 | server, interval | | OK |
| `effect/MnemonicCandleAuraEvents.onPlayerTick` :24 | server, interval | candle search | OK |
| `encounter/MycophantEncounterManager.onPlayerTick` :80, `VagrantMindEncounterManager.onPlayerTick` :45 | server, dimension-gated | | OK |
| `entity/npc/circus/CircusPerformanceController.onPlayerTick` :95, `entity/npc/court/CourtPresences.onPlayerTick` :91 | server, active act / 20 t | | OK |
| `entity/npc/dialogue/FungalWhisperEvents` :45, `OurLadyWhisperEvents` :49 | server, 200 t staggered | | OK |
| `event/AntecedentEvents.recordClosed` :57, `.epilogue` :64 | server | `WeakHashMap` lookup / 200 t | OK |
| `event/ApotheosBodyEvents` :39, `ArmorSetBonusHandler` :112, `BodyRefinementSkillEvents` :54 | server, Apotheos only / interval / 5 t | | OK |
| `event/ChamberVisitEvents.onTick` :36, `FungalProjectionEvents` :25, `GardensNetworkEvents` :16 | server, active visit / projection / Gardens only | | OK |
| `event/HeartNodeEvents.onPlayerTick` :170, `.onEdgeTick` :182 | server, interval | | OK |
| `event/MausoleumEvents.onThresholdTick` :178, `.onPlayerTick` :222 | server, every tick / 20 t | anchors come from the `WardAnchors` registry, no search | OK |
| `event/NpcProgressionMarkerEvents.onPlayerTick` :30 | server, 10 t | `getEntities(RANGE)`; diffed send | OK |
| `event/OverworldFungalSurveyEvents` :15, `VagrantMindInquiryEvents` :16, `UnstainedMilestoneHandler` :41, `WillAmbushDirector` :49 | server, 40 to 200 t | | OK |
| `event/PossessionEvents.onPlayerTick` :144, `ToggleableSkillEvents.playerTick` :34 | server | possession only / `AABB(16)` every 20 t while enabled | OK |
| `event/worldevent/ChamberOfWillEvents.onPlayerTick` :63 | server, Chamber only | progression refresh, bounds check | OK |
| `item/harbinger/BloodVialInjectionAnimation.tick` :55, `MicroscopeExamination.playerTick` :137, `tool/TerrestrialSpeculumPlantingManager.tick` :48 | server | session-map lookup | OK |
| `item/harbinger/QliphothPomeInventoryEvents.onPlayerTick` :89 | server, every tick | SavedData pending-return lookup; container scan while a menu is open | Watch |
| `item/harbinger/tool/living/LivingStaffWeaponFormEvents.onPlayerTick` :87 | server, 10 t | inventory sanitize | OK |
| `item/harbinger/tool/SporiticThuribleEvents.onPlayerTick` :50, `item/unstained/tool/SilthmereGlaiveEvents` :42 | server | off-hand / interval | OK |
| `manipulation/animation/CastingAnimationManager.tick` :109, `animus/AvatarManifestationManager.onPlayerTick` :73, `ManipulationChannelManager.onPlayerTick` :69 | server | session maps | OK |
| `manipulation/ductilis/AxonalTransductionManager.beforeTick` :132, `.afterTick` :133 | both / server | `touchingNode` reads about 12 cells per player per tick (:90-98) | Watch |
| `manipulation/ferric/IronheartedEvents.onPlayerTick` :31, `ManipulationReactiveEvents.onPlayerTick` :325 | server | | OK |
| `manipulation/mortem/BlackheartedEvents.onPlayerTick` :53 | server, 20 t | a visual to everyone within 64 blocks, for every player, active or not | Watch (RT-7) |
| `manipulation/tenebris/BlackVeilCovenantManager.onPlayerTick` :46 | server, every tick | `VEILS` stream; re-adds 3 effects every tick inside one's own veil | Watch (RT-6) |
| `mission/alchemist/ClinicalBloodKnowledge.tick` :137, `PallidVesselAssignment.onPlayerTick` :145 | server, 20 t | 41-slot scan while eligible | OK |
| `mission/circus/CircusPracticalController.tick` :75, `mission/vicar/EarlyInitiation.tick` :164 | server, session only | | OK |
| `world/fold/FoldedHallwayEvents.onPlayerTick` :18 | both, every tick | map lookup keyed by side | OK |
| `world/vein/EarthenVeinTravelManager.tick` :168 | server | session map | OK |

### Block entities, items, effects and client

| Ticker | Side / gate | Per-tick work | Verdict |
|---|---|---|---|
| `tile/unstained/crafting/StillwaterCondenserBlockEntity.serverTick` :69 | server, ungated | 405 + 1,331 `getBlockState` calls (:73-75, :108-111; `VerdigrisLatticeBlock.java:96-101`) | Hot (RT-4) |
| `tile/harbinger/decoration/SerpentineIdolBlockEntity.tick` :22 | **both** (`BlockSerpentineIdol.java:71`) | `AABB(4)` query; `addEffect(blood_binding)` on every mob, on both sides | Hot near mobs (RT-5) |
| `effect/BloodBindingEffect.applyEffectTick` :22 | every tick | 4 `sendParticles` per bound entity (:37-48) | Hot (RT-5) |
| `effect/BloodLossEffect` :42, `effect/HemolysisEffect` :47 | every tick | `payUpkeep` runs `syncVolume` (5 packets) every tick | Hot (RT-1) |
| `effect/SanguineFertilityEffect.applyEffectTick` :27 | every tick, both sides | an `AABB(6 + 2·amp)` `Animal` query | Watch |
| `tile/harbinger/crafting/GhastlyAlembicBlockEntity.serverTick` :318; `tile/unstained/crafting/PallidRetortBlockEntity` :137-150 | server, ungated | recipe match and `BrewingResolver` every tick, even when cold | Watch |
| `tile/harbinger/crafting/MycelialLanternBlockEntity` :141, `MycelialCrucibleBlockEntity` :257 | server | recipe scans while active or idle | Watch |
| `tile/harbinger/functional/VisceralMirrorBlockEntity.serverTick` :86 | server, ritual only | nearest player each tick | OK cost; correctness RT-22 |
| `tile/harbinger/plant/PelagicColonyBlockEntity.tick` :26, `PhlebotomistsCabinet` :74, `PhlebotomistsFieldCase` :102, `Clairaudiograph` :102, `EarthenVein` :44, `VigilArchive` :85 | both, split inside | | OK |
| `tile/harbinger/crafting/PuppeteersSpindleBlockEntity.serverTick` :61 | server | syncs only on change | OK |
| 34 other `getTicker` overrides | server- or client-only by `getTicker`, or none | not individually notable (prior audit: BloodTrialAltar, BloodPylon, SanguineVigil, SemiSentientConstruct, MorphlingCradle, SomaticLoom are throttled) | OK |
| `item/harbinger/tool/living/VialRackItem.inventoryTick` :98 | both | 2 `copyTag` plus 8 `ItemStack.parseOptional` per rack per tick on the server (:116-131) | Watch (RT-11) |
| `item/harbinger/RecycledEnzymeItem.inventoryTick` :32 | server | a `copyTag` per stack per tick (:52-58) | Watch (RT-11) |
| `item/harbinger/OrganEchoItem.inventoryTick` :70 | server | 41-slot scan; wrong-slot clear (:89-90, F-5) | Watch (F-5) |
| `client/render/world/ManipulationVisualRenderer` :182-189 | per frame, twice | 6 boss-type `getEntitiesOfClass(radius 96)` queries (8 counting Seraphae's anchors and fragments) whether or not a boss is near | Watch (RT-9) |
| Client: `BloodProjectileEffects` :33, `FerricDuctilisEffects` :59, `MorphlingStrainWorldRenderer` :68, `PuppeteerThreadRenderer` :63, `SchoolStateVisuals` :23 | per frame | each walks `entitiesForRendering()` | Watch |
| Client: `compat/jei/ClinicalRecipeVisibility.tick` :35 | every client tick | loop over lesson recipes with `Hemomancy.rloc` | Watch |
| Client: the other 29 `ClientTickEvent` handlers | per tick | state cues and session maps | OK |

---

## Serverbound payloads

All 62 are registered on `event.registrar(PROTOCOL_VERSION)` (`network/PacketHandler.java:99`) with no
`executesOn(HandlerThread.NETWORK)`, so they run on the main thread. Every handler also wraps its work in
`enqueueWork`. File paths are under `common/network/`.

| Payload (handle) | Client supplies | Server checks | Verdict |
|---|---|---|---|
| `ActivateArmorSetAbilityC2SPacket` (capa/harbinger :28) | ability id | possession refusal; registry, alive, set worn, `canActivate` (`armor/ability/ArmorSetAbilityRegistry.java:105-122`) | OK |
| `AntecedentEffectPacket` server branch (:17) | kind, pos | kind 4 needs complete research and D7 | Weak, Low: records EPILOGUE without the server's own conditions (`event/AntecedentEvents.java:64-70`) |
| `ArborFruitInteractPacket` (capa/harbinger :26) | entity id, skill id | Chamber dimension, Arbor type, owner, visibility, eye distance | OK |
| `AxonalInputPacket` (axonal :16) | token, direction | session token; direction clamped (`manipulation/ductilis/AxonalTransductionManager.java:125-131`) | OK |
| `BloodCraftingKeyPressPacket` (capa/harbinger :35) | a full `ItemStack` (unused) | server `pick(3)`; the craft and rite services check | Weak, Low (RT-23) |
| `BloodFormationKeyPressPacket` (capa/harbinger :40) | nothing | observational visit, Unstained block, sword, volume > 100 | Weak (RT-23) |
| `BloodVolumeClientPacket` (capa/harbinger :28) | nothing | none; replies with `syncVolume` and pome progress | Weak (RT-24) |
| `BurnSporeLoadKeyPacket` (capa/harbinger :17) | nothing | alive, Apotheos, cooldown (`capability/player/harbinger/spore/SporeLoadService.java:17-32`) | OK |
| `CPacketFlight` (capa/harbinger/scars :28) | nothing | grounded, water, levitation, `canFallFly` | OK |
| `CastingChargePacket` (:20) | name (128), held | only starts a clock; casts clamp to the observed hold (`manipulation/ManipulationChargeClock.java:25-50`) | OK |
| `ChangeSelectedManipPacket` (capa/harbinger/manips :37) | float (unused) | cycles the equipped, known manipulations | OK |
| `ClairaudiographActionPacket` (:14) | menu id, version, action, index | container id; `ClairaudiographMenu.action` re-checks `stillValid`, version and bounds (`menu/tile/functional/ClairaudiographMenu.java:50-64`) | OK |
| `ClosePendingWhisperPacket` (dialogue :16) | UUID | per-player open map | OK |
| `DialogueOptionPacket` (dialogue :46) | event id (up to 32767 chars), entity id | speaker table for listed events; 8-block reach only when the entity id is not 0; per-event type checks. `memo_capture:` and Liber unlocks run before any of these (:54-60) | **Exploitable** (RT-18, RT-20) |
| `DialogueRewardChoicePacket` (dialogue :18) | event id, entity id, request id | only acknowledgement events, then `dispatch` | Inherits RT-20 |
| `DialogueTopicOpenedPacket` (dialogue :34) | topic id, entity id | namespace `hemomancy`, path ≤ 128; reach only when the id is not 0 | **Exploitable** (RT-21) |
| `EquipManipulationPacket` (capa/harbinger/manips :44) | name, flag | exact known, retired, slot capacity | OK |
| `GroundBloodDrawPacket` (particle :47) | partial tick | clamped (F-8 holds); living staff held; server `pick(6)` | Weak (RT-19, RT-23) |
| `HeartNodeSettingsPacket` (heartnode :37) | pos, settings | `nearBlockEntity` within 8; owner `mayOpen` | OK |
| `ImprintMnemonicBlueprintPacket` (capa/harbinger :32) | enum, recipe id | discovered and degree gate; blank blueprint held | OK |
| `KnownManipulationClientPacket` (capa/harbinger/manips :29) | nothing | none; 2 replies | Weak (RT-24) |
| `KnownSummonsRequestPacket` (capa/harbinger/summon :28) | nothing | none; 1 reply | Weak (RT-24) |
| `ManipulationChargeVisualPacket` (capa/harbinger/manips :22) | held ticks | paralysis, charged selection, range, ready, cooldown, alive, 4-tick throttle | OK |
| `NpcTradeRequestPacket` (trade :40) | entity id, offer id, slots (≤ `MAX_SLOTS`) | `activePlayer`, `distinctSlots`, `nearLivingEntity` 8, `tradesWith`, limiter; takes before gives (`entity/npc/trade/NpcTradeService.java:209-226`) | OK |
| `NpcTradeSeenPacket` (trade :30) | entity id | `activePlayer`, reach, `tradesWith` | OK |
| `OpenMorphlingJarPacket` (morphling :34) | nothing | a jar in the inventory or equipment slot | OK (the menu it opens: RT-17) |
| `PacketBloodlineMessage` (capa/harbinger :43) | text ≤ 256 | rate limit (F-7 holds), bloodline | OK |
| `PacketCrossbarRadialAction` (summon :41) | UUID, enum, value ≤ 64 | equipped crossbar; `hotSwap` checks control, skill, cooldown and known summon | OK |
| `PacketKickBloodlinePlayer` (capa/harbinger :43) | UUID | leader of the line (local copy refreshed at login, `capability/player/harbinger/bloodvolume/BloodVolumeEvents.java:447-466`), membership | OK |
| `PacketLedgerAction` (capa/harbinger :83) | action int | ledger held (F-6 holds), bloodline, owned fane | OK |
| `PacketLumpDonate` (capa/harbinger :45) | double | active, bloodline, `donationAmount` (F-1 holds), `BloodAccounting` | OK |
| `PacketOpenScarsInv` (capa/harbinger/scars :37) | pos | `nearBlock` scarlet vanity | OK |
| `PacketOpenScryingDiagnostics` (capa/harbinger/manips :38) | pos | `nearBlock` scrying podium | OK |
| `PacketOpenTendencyView` (capa/harbinger/manips :27) | nothing | none (a view-only menu) | OK |
| `PacketPrepareScarPattern` (capa/harbinger/scars :67) | pos, id list | menu and pos match, BE type, cap, known cerebral scars | OK (decode hardening: RT-16) |
| `PacketPuppeteersSpindleAction` (summon :36) | enum, name | menu `stillValid`, crossbar | OK |
| `PacketRequestPoolData` (capa/harbinger :34) | nothing | bloodline | Weak (RT-24) |
| `PacketRequestSpecimenBestiary` (capa/harbinger/bestiary :29) | nothing | none | Weak (RT-24) |
| `PacketScarCraftingEvent` (capa/harbinger/scars :41) | nothing | menu, D4, tier skill, personal access, station failure | OK |
| `PacketToggleSkill` (capa/harbinger :22) | skill id | toggleable and unlocked (`SkillProgress.toggleEnabled`) | OK |
| `PacketToggleUnstainedBonus` (capa/unstained :37) | id 0 or 1 | none; the effect is gated where it applies (`event/UnstainedMilestoneHandler.java:64, 74`) | OK |
| `PacketUnlockSkill` (capa/harbinger :48) | skill id | degree, parents, points, active blood, cost | OK |
| `PacketUpdateLivingStaffMorph` (morphling :26) | slot index | jar present, bounds, `IMorphling` | OK |
| `PacketUpdatePoolSettings` (capa/harbinger :49) | 2 doubles | `finite` (F-8 holds), clamps | OK |
| `PacketUpdateScarPattern` (capa/harbinger/scars :45) | 8×8 bytes | decode enforces 8×8; menu | OK |
| `PlaceStructurePacket` (:85) | structure data | creative, permission level 2, spawner held (F-9 holds) | OK |
| `PossessionActionPacket` (capa/harbinger :26), `PossessionSkipPacket` (:21) | ordinal / nothing | `byOrdinal` null check; possession services | OK |
| `ResidentsRequestPacket` (succession :27) | UUID, flag | bloodline, `canManage` to dismiss, at the fane to reissue (generation-keyed) | OK (one snapshot reply per request: RT-24) |
| `SelectEarthenVeinDestinationPacket` (vein :22) | nonce, UUID | session nonce, phase, near source, known vein, validated destination | OK |
| `StartCentrifugeButtonPacket` (capa/harbinger/manips :29) | nothing | menu; `attemptStartup` | OK |
| `SynapticLoadoutActionPacket` (capa/harbinger/manips :77) | enum, pos, slot, name | active, D5, reach, BE type, slot unlocked, name sanitized | OK |
| `ToggleEquipmentLayerVisibilityPacket` (capa/harbinger :35) | slot | whitelist | OK |
| `ToggleGourdKeyPacket` (capa/harbinger :34), `ToggleSilentSlippingC2SPacket` (:27) | nothing | gourd in slot 6 / full set (`armor/ability/SilentArchonArmorAbilityHandler.java:75-85`) | OK |
| `UpdateCurrentManipPacket` (capa/harbinger/manips :26) | index | bounds, retired, equipped | OK |
| `UpdateCurrentMemoryPacket` (capa/harbinger/manips :21) | storage key | muscle-memory kind, equipped, known | OK |
| `UpdateSelectedStillArtPacket` (capa/unstained :35) | name | known, unlocked | OK |
| `UseManipKeyPacket` (capa/harbinger/manips :58) | action ordinal, float | possession, Unstained block, paralysis, active, equipped; server cooldown (`manipulation/BloodManipulation.java:509`); charge clamp | OK |
| `UseStillArtKeyPacket` (capa/unstained :27) | nothing | paralysis, known, unlocked, server cooldown (`manipulation/stillarts/StillArt.java:115-144`); no alive or spectator check | Weak, Low (RT-23) |
| `VisceralMirrorCancelPacket` (capa/harbinger/visceral :37) | pos | reach only, no ownership | Weak (RT-22) |
| `VisceralMirrorExtractPacket` (capa/harbinger/visceral :41) | pos, organ ordinal | reach, bounds, `startRitual` checks the sender | Weak: the ritual then acts on whoever is nearest (RT-22) |

---

## Findings

### 1. Tick cost

**RT-1. Every blood payment or income sends five packets and recomputes four diagnostics; several sources do it every tick.** High. Verified.

- **What happens:**
  - `BloodAccounting.settle` calls `syncVolume` whenever blood is paid (`capability/player/harbinger/bloodvolume/BloodAccounting.java:80-82`).
  - `BloodFlowLedger.applyDirectIncome` and `applyCirculationIncome` do the same whenever blood is added (`BloodFlowLedger.java:57-58, 101-102`).
  - `syncVolume` (`BloodVolumeEvents.java:428-433`) sends `BloodVolumeServerPacket` plus three diagnostics packets, and `ManipulationDiagnosticsSync.sync` sends a fifth. Building those packets means a sorted stream over the ledger (`BloodFlowLedger.collect` :156-175), `MaxBloodLedger.collect`, `ManipulationSlotLedger.collect` and `ManipulationCostLedger.collect`.
- **Sources at interval 1 (every tick):**
  - Sanguine Surge (`BloodVolumeEvents.java:118-121`), the candle aura (:125-130) and Last Wind (:132-138), whenever the player is below maximum.
  - Blood Loss (`effect/BloodLossEffect.java:42`) and Hemolysis (`effect/HemolysisEffect.java:47`).
  - `entity/projectile/RecallBarbEntity.java:119`.
- **Effect:** a Harbinger with Sanguine Surge receives about 100 packets a second until full, and the server rebuilds four diagnostics each tick for each such player.
- **Fix:** give the `BLOOD_VOLUME` row of `PlayerStateSync` a dirty predicate. `syncVolume` then marks it dirty instead of sending, and `flushDirty` sends once per tick at most. Send the diagnostics only while the scrying diagnostics menu is open, or every 20 ticks.
- **Effort:** M.

**RT-2. Blood routing force-loads every linked chunk every 10 ticks.** High. Verified (code path; the chunk loading is vanilla behaviour).

- **What happens:**
  - `BloodRoutingEvents.onLevelTick` walks every saved link in the level every `ROUTE_INTERVAL_TICKS` = 10 (`capability/player/harbinger/bloodvolume/routing/BloodRoutingEvents.java:44-58`; `BloodRoutingHelper.java:31`).
  - It calls `level.getBlockEntity(entry.getKey())` (:54). `routeLinkedTarget` calls it again (`BloodRoutingHelper.java:65`), before it checks whether the owner is online (:134).
  - On the server thread, `Level.getBlockEntity` goes through `getChunkAt`, which loads a missing chunk synchronously under a short-lived ticket.
- **Effect:** every routed base in the world is reloaded from disk 2 times a second, whether or not anyone is near or online.
- **Fix:** skip a link unless `level.isLoaded(pos)` (or `shouldTickBlocksAt`) for both its target and its source, and resolve the owner before touching any block.
- **Effort:** S.

**RT-3. The school-combat attachment is created on every living entity, saved on every mob, and resent on every load and track.** Medium. Verified. This is P-2, still present.

- **Where:**
  - `damage/SchoolCombatEvents.tick` :266-286 calls `SchoolStates.data(target)`, which is `getData` (`capability/HemoCapabilityAccess.java:112`), for every `LivingEntity` every tick.
  - The type is a plain `serializable` (`capability/HemoAttachmentTypes.java:51-53`), so every saved mob writes the compound.
  - `deserializeNBT` and `restore` set `dirty = true` (`damage/SchoolCombatState.java:82, 133`), so every mob loaded from disk sends a `SyncSchoolCombatS2CPacket` on its first tick.
  - `tracking` (:289-293) sends one more per living entity to each player who starts tracking it, empty or not.
- **Fix:** use `hasData` in the tick and in tracking; have the serializer skip empty state; mark dirty on load only when entries exist.
- **Effort:** S.

**RT-4. The Stillwater Condenser reads 1,736 blocks per tick per condenser.** Medium. Verified. This is P-3, still present.

- **Where:** `tile/unstained/crafting/StillwaterCondenserBlockEntity.java:73-75` runs `hasGhostPipe` (a 9×5×9 volume, :108-111) and `hasActiveLattice` (11³, `block/unstained/crafting/VerdigrisLatticeBlock.java:96-101`) every tick. It does so with no bottles and no output room.
- **Fix:** cache both results, re-check every 40 ticks or on `neighborChanged`, and skip the check when there are no bottles.
- **Effort:** S.

**RT-5. The Serpentine Idol and Blood Binding make a per-tick particle stream and do server work on the client.** Medium. Verified. This is prior §2.8, still present.

- **Where:**
  - `block/harbinger/decoration/BlockSerpentineIdol.java:71` registers the ticker on both sides.
  - `tile/harbinger/decoration/SerpentineIdolBlockEntity.java:22-34` runs an `AABB(4)` query each tick and re-applies `blood_binding` to every non-player within it, on the client too.
  - `effect/BloodBindingEffect.java:37-48` sends 4 particle packets per tick for every bound entity, to every player within 32 blocks.
- **Effect:** an idol beside a mob pen streams about 80 packets a second per mob to each nearby player.
- **Fix:** register a server-only ticker that runs every 5 to 10 ticks, and a client-only ticker for the local particles; send one particle packet per bound entity every few ticks.
- **Effort:** S.

**RT-6. Effect and ability refreshes send a packet to the player every tick.** Low. Verified (the packet follows from vanilla `addEffect` and `onUpdateAbilities` semantics). Prior §2.7, still present.

- **Where:**
  - Sheolic Fire Resistance (`armor/ability/SheolicBastionBloodlustArmorAbilityHandler.java:123`) and the Bastion slowness (:146).
  - Black Veil's three effects (`manipulation/tenebris/BlackVeilCovenantManager.java:53-55`).
  - The Edacious set's `FlightArbiter.grant` every tick (`armor/ability/EdaciousBloodburstArmorAbilityHandler.java:51`), which runs `onUpdateAbilities` (`util/FlightArbiter.java:46-51, 73`).
- **Effect:** about 20 packets a second per wearer or occupant.
- **Fix:** re-add an effect only when it is missing or has fewer than about 10 ticks left; grant flight only on a change.
- **Effort:** S.

**RT-7. Fixed-interval broadcasts are not diffed.** Low. Verified. Prior P-4 and P-5, still present.

- **Where:**
  - The Founding Fane sends an empty list to every living player in every level every 40 ticks (`event/worldevent/FoundingFaneEvents.java:43-48`).
  - `BlackheartedEvents.onPlayerTick` sends a visual for every player, active or not, to everyone within 64 blocks every second (`manipulation/mortem/BlackheartedEvents.java:53-57`; `manipulation/ManipulationVisuals.java:53-73`). With players clustered this grows as N².
  - `CircusDiscoveryProgress` sends every player one packet a second (`capability/player/harbinger/circus/CircusDiscoveryProgress.java:38-55`).
  - The Chamber sends two packets per player every 40 ticks (`encounter/ChamberOfWillManager.java:535-547`).
  - Rite sync goes to every player in the level every 10 ticks (`rite/harbinger/HarbingerCardinalRiteEvents.java:74, 327`; `rite/harbinger/CardinalRiteSync.java:43-58`).
- **Fix:** keep the last payload per player and send only on change, as `RiteZoneSync`, `RiteSpaces` and `NpcProgressionMarkerEvents` already do. Limit rite sync to players within tracking range.
- **Effort:** S to M.

**RT-8. Rite recipes are still looked up by linear scan.** Low. Verified. This is P-7, still present.

- **Where:**
  - `recipe/CardinalRiteRecipe.getRiteByLocation` (:27-60) streams every cardinal-rite recipe up to 3 times and calls `setId` on the shared recipe each time.
  - Server callers each tick: once per active rite (`HarbingerCardinalRiteEvents.java:135`), several more times through `CardinalRiteAllyService` (:45, :95, :214, :244, :250, :286), and inside `CardinalRiteSync.toClientEntry` (`rite/harbinger/CardinalRiteSync.java:68`).
  - Client callers each frame: `client/render/world/CardinalRiteBoundaryRenderer.java:376`, `client/render/world/floor/AnchorSores.java:52`, `client/render/world/rite/CardinalRiteAnchorJetRenderer.java:67`.
- **Fix:** `RecipeManager.byKey` with the prefix fallback, and no mutation.
- **Effort:** S.

**RT-9. Large-radius area scans run with nothing to gate them.** Low. Verified.

- **Where:**
  - The Husk effigy tick queries `Mob`s in a radius of 96 around every player every 10 ticks, with a `getPersistentData()` predicate that materialises a tag on each mob (`item/harbinger/memory/HematicMemoryToolEvents.java:190-194`).
  - On the client, `ManipulationVisualRenderer` runs six boss-type `getEntitiesOfClass(radius 96)` queries twice a frame whether or not a boss exists (`client/render/world/ManipulationVisualRenderer.java:182-189`; e.g. `client/render/world/MycophantGazeGeometry.java:48`, `client/render/world/SeraphaeRadianceGeometry.java:32-63`).
  - Five more client passes walk `entitiesForRendering()` each frame (see the table).
- **Fix:** keep loaded effigies and bosses in a registry (as `WardAnchors` does) and skip the pass when it is empty.
- **Effort:** S.

**RT-10. `refreshDimensions()` runs for every player every tick on both sides.** Low. Verified.

- **Where:** `capability/player/harbinger/manip/KnownManipulationEvents.java:41-43`. Each call posts `EntityEvent.Size` (which `PossessionEvents.onSize` handles) and re-applies the bounding box.
- **Fix:** call it only when the avatar form or the possession form changes.
- **Effort:** S.

**RT-11. Item and entity data is churned every tick.** Low. Verified. Prior P-6, still present.

- **Where:**
  - `VialRackItem.inventoryTick` (`item/harbinger/tool/living/VialRackItem.java:98-101`) runs `ensureInitialized` (a `copyTag`) and `migrateAhaematicSamples`, which decodes all 8 vials (:123-131) for every rack in every inventory every tick.
  - `RecycledEnzymeItem.identity` copies the tag every tick (:52-58).
  - Item-entity handlers materialise and edit persistent data on every `ItemEntity` outside their fluid (`event/MorphicNectarEvents.java:70-75`, `event/RenderingPoolEvents.java:38-42`); `CircusPracticalController.cleanup` does it for every entity (:104).
- **Fix:** run the migrations once, behind a marker, and read with `CustomData.contains`/`read` rather than `copyTag`. Clear fluid-progress keys only when the entity was in the fluid on its last tick.
- **Effort:** S.

Known and still present (decisions pending, not re-reported):

- F-11: `PendingBloodCraftManager.tick()` still runs once per level (`HarbingerCardinalRiteEvents.java:102`). Its lifecycle half is fixed: `clear()` is now called from `event/TransientServerStateEvents.java:118`.
- F-19: kit delivery to every player, alive or not, every 20 ticks (`HarbingerCardinalRiteEvents.java:118`).

### 2. Leaks

**RT-12. `VigilSites` keeps unloaded levels alive.** Medium. Suspected (it depends on unload callbacks).

- **Where:** `tile/harbinger/functional/VigilSites.java:11-15` is a single `WeakHashMap<Level, Set<VigilArchiveBlockEntity>>` shared by both sides. Its values (the block entities) hold strong references to their `Level` key, so a key is never collected while its set is non-empty. Entries leave only through `onChunkUnloaded` and `setRemoved` (`VigilArchiveBlockEntity.java:56-61`).
- **Why it leaks:** `WardAnchors` has the same structure and documents that a client never unloads the level it left on a dimension change (`tile/harbinger/puzzle/WardAnchors.java:22-33`). It fixes that with side-split maps, pruning other client levels on add, `clearClient` on logout (`client/event/ClientSessionEvents.java:105`) and `clearServer` in the hub (`TransientServerStateEvents.java:162`). `VigilSites` has none of these, so after visiting a Vigil site the old `ClientLevel` (and, on an integrated server, the old `ServerLevel`) can stay reachable.
- **Fix:** copy the `WardAnchors` shape.
- **Effort:** S.

**RT-13. Two Folded Hallway traveller maps are never cleared, and the session guardrail cannot see them.** Low. Verified.

- **Where:** `world/fold/FoldedHallwayManager.java:21-22` declares two static concurrent collections keyed by a record that wraps a UUID (:187-195). Neither is cleared at server stop or on logout, so an active fold state survives a world switch in single player.
- **Why the guardrail misses it:** `SessionStateRegistryTest` (`src/test/java/com/vincenthuto/hemomancy/architecture/SessionStateRegistryTest.java`) only matches `Map<UUID, …>` declarations, and only checks that the owning class is named somewhere in the hub. It misses record-keyed maps, static lists of records that hold UUIDs or entities (for example `ManipulationReactiveEvents` :58-60), and fields the named class forgets to clear.
- **Fix:** register the two clears. Widen the guardrail to static collections whose key or element type holds a UUID or an entity, and to a per-field check.
- **Effort:** S.

**RT-14. A few client caches are still unbounded or keep the old level.** Low. Verified. Prior §4, partly fixed.

- **Still present:**
  - `client/render/item/SporiticThuribleRenderHelper.java:32` and `client/render/item/harbinger/LivingFlailRenderHelper.java:35`: `STATES` are never pruned.
  - `client/render/entity/misc/ArborGrowthAnimations.java:11`: `SNAPSHOTS` is never cleared.
  - `client/render/entity/npc/SuccessionBrazierLabels.java:25-32` holds the previous `ClientLevel` and its braziers until the next world renders.
- **Fixed since the prior audit:** `ClientSessionEvents` now clears the other entity-id caches (CrimsonFire, MonolithicDislocation, BloodVialInjection, LivingStaffMorph, Microscope, BloodProjectileEffects, QliphothSeedTendrilEffects).
- **Fix:** clear these four in `ClientSessionEvents`.
- **Effort:** S.

### 3. Thread and side safety

**RT-15. Two threads can fill `EngramTextureCache` at the same time.** Low. Verified (the race window is narrow).

- **Where:** `block/harbinger/rite/EngramTextureCache.java:25-26, 41-66` fills two plain `HashMap`s through a check-then-clear-then-fill sequence. It is reached from the server thread (`onServerAboutToStart` :37; `network/particle/GroundBloodDrawPacket.java:156`) and from the render thread (`client/render/tile/inscription/DiscoveryInscriptionBlockRenderer.java:24, 38`). In single player both can run together.
- **Fix:** build immutable maps once behind a holder idiom, or a `synchronized` load.
- **Effort:** S.

Also on the client side: the Serpentine Idol adds effects in client-side ticks (RT-5). Every other `ClientBridge` call in `common/` is guarded by `isClientSide`, sits in a tooltip or client-only path, or prefers the server (`item/harbinger/scar/ItemScarPattern.java:113-127`).

### 4. Null and state hazards

**RT-16. Small decode and load hardening.** Low. Verified.

- **Where:**
  - `PacketPrepareScarPattern.decode` loops on a client-supplied `int` count (`network/capa/harbinger/scars/PacketPrepareScarPattern.java:59-63`). Only packet size bounds it, and a bad count disconnects only the sender.
  - `ChamberOfWillManager.load` parses saved dimension and theme strings with `ResourceLocation.parse` and no guard (`encounter/ChamberOfWillManager.java:723, 730`), so one corrupt entry fails the whole SavedData load. The next loop down already guards (:736-744).
- **Fix:** use a bounded list codec; wrap both parses with `tryParse` and skip bad entries.
- **Effort:** S.

Sweeps with nothing reachable:

- 6 unchecked `(…BlockEntity) getBlockEntity` casts: each is preceded by a type check or sits in a client menu constructor.
- 73 `(ServerPlayer)` casts in `common/`: those sampled are server-gated.
- 34 `orElseThrow`/`.get()` unwraps: they act on registry or catalogue entries validated at load (for example blood-injection effects are checked by `knownEffect` in `item/harbinger/BloodInjectionRules.java:43, 65`).
- 4 `ServerLifecycleHooks.getCurrentServer().` dereferences: these are server-side helpers (`capability/player/harbinger/scar/fungal/VeinMinerHelper.java:139`).

### 5. Multiplayer and exploits

**RT-17. Three item-backed menus can be duplicated with a vanilla client.** Critical. Verified code path; not reproduced in game.

- **Where:**
  - `MorphlingJarMenu`, `LivingSyringeMenu` and `ScarBinderInventoryMenu` bind a `StackBackedItemHandler` to the `ItemStack` object they found when opening, and load its contents once (`menu/MorphlingJarMenu.java:71`; `item/itemhandler/StackBackedItemHandler.java:78-84` writes back only to that object).
  - Their `stillValid` checks only that the remembered slot still holds *some* item of the same class (`MorphlingJarMenu.java:168-177`, `LivingSyringeMenu.java:169-172`, `ScarBinderInventoryMenu.java:64-67`).
  - None of them stops the open item from being moved. The player's 36 slots sit in the menu (`menu/HemoContainerMenu.java:23`).
- **Sequence:**
  1. Carry two jars, A and B. Open A.
  2. Press the number key for B's hotbar slot while hovering A's slot. The SWAP moves the references, A's slot now holds B, and `stillValid` passes.
  3. Press Q over A in the hotbar. The THROW splits a copy of A, contents included, into the world, and the bound original drops to count 0.
  4. Take the morphlings out of the jar slots. The dropped copy still holds them.
- **Fix:** copy `MnemonicFolioMenu`'s guard (`menu/MnemonicFolioMenu.java:51-94`): `stillValid` checks identity, and `wouldMoveOpenFolio` refuses clicks, swaps and throws that touch the open item. `MorphlingJarItemHandler.hasSameBackingStack` (`item/itemhandler/MorphlingJarItemHandler.java`) already exists for the identity test.
- **Effort:** S.

**RT-18. A forged `memo_capture:` dialogue event unlocks any Liber entry and grows saved data without bound.** High. Verified for the Hemomancy code path; HutosLib's `unlockEntry` base is outside this repo.

- **What happens:**
  1. `DialogueOptionPacket.handle` runs `MemoHelper.handleMemoEvent` for any `memo_capture:<id>` event, before the activity, speaker and reach checks (`network/dialogue/DialogueOptionPacket.java:54-57`).
  2. `captureMemo` stores the id in `LiberKnowledge.pendingMemos` with no check against `MemoDefinitions`, and resends the whole Liber state each time (`capability/player/shared/knowledge/discovery/MemoHelper.java:46-79`; the sync carries every pending memo, `network/capa/PacketSyncLiberKnowledge.java:73`).
  3. On dictation, an unknown memo's path is `SHARED` (:328-331, so every Liber accepts it), and its Liber entry is the memo id itself (`.orElse(memoId)`, :312-315). `unlockDictatedMemos` then unlocks that entry (:182-188).
- **Effect:**
  - A client that sends `memo_capture:hemomancy:libersanguinium/the_infection/pages/the_truth` and dictates gains the `TRUTH` entry. `recipe/RiteDiscoveryRules.java:14-19, 29-32` uses that entry as the discovery gate for the Apotheos Rite and the Eternal Covenant, and other entries gate the Qliphoth Bloom, Ancestral Communion and Sanguine Eclipse.
  - Each forged id also adds to saved player data and to every later `PacketSyncLiberKnowledge`, so a flood grows without bound. Once the sync payload passes the clientbound size limit it would fail to encode on every sync (Suspected).
- **Also:** `LiberKnowledgeHelper.handleDialogueEvent`, called at `DialogueOptionPacket.java:58` before any check, unlocks the entry for `hermit_first_rite` (`LiberEntryDefinitions.java:178`) the same way, from anywhere.
- **Fix:**
  - Accept only ids in `MemoDefinitions`, and only from a source that offers the memo (a whisper or NPC tree in reach).
  - Remove the `.orElse(memoId)` fallbacks.
  - Cap `pendingMemos`.
  - Run the Liber unlock after `dispatch`'s checks.
- **Effort:** M.

**RT-19. Block-editing manipulations and payloads skip protection checks.** High on servers with claims or spawn protection; otherwise Medium. Verified.

- **Where:**
  - `SanguineExcavationManip` breaks up to 27 connected blocks with `dropResources` plus `destroyBlock`, with no `BlockEvent.BreakEvent`, spawn-protection, adventure-mode or tool-tier check (`manipulation/ferric/SanguineExcavationManip.java:88-123`).
  - `BloodLampManip` places light blocks unchecked (`manipulation/lux/BloodLampManip.java:66-75`).
  - `GroundBloodDrawPacket` places and converts ash-trail blocks with `setBlockAndUpdate` and no `mayBuild` or `mayInteract` (`network/particle/GroundBloodDrawPacket.java:79, 93, 121, 134`).
- **Why it matters:** claim and protection mods listen for `BreakEvent` and `EntityPlaceEvent`, so these grief claimed land and spawn. The codebase already has two helpers that do the job: `ManipulationCombatHelper.mayAlter` (`mayBuild` and `mayInteract`, `manipulation/ManipulationCombatHelper.java:117-119`; used in 5 manipulations) and `VeinMinerHelper.hasBreakPermission` (spawn protection, `mayUseItemAt`, `fireBlockBreak`, `capability/player/harbinger/scar/fungal/VeinMinerHelper.java:128-143`).
- **Fix:** route every player-caused block edit through `hasBreakPermission` (breaks) or `mayAlter` plus an `EntityPlaceEvent` (placements), and add a guardrail that a `setBlock`/`destroyBlock` in `common/manipulation` or `common/network` names one of them.
- **Effort:** M.

**RT-20. Dialogue events missing from the speaker table can be fired from anywhere with entity id 0.** Medium. Verified.

- **Where:** `DialogueEventSpeakers` documents that "the server never checks that the event was in the tree it sent" and allows any unlisted event (`entity/npc/dialogue/DialogueEventSpeakers.java:16, 56-61`). `dispatch` checks reach only when the entity id is not 0 (`DialogueOptionPacket.java:76`).
- **Reachable this way:**
  - `scout_give_notes` gives a fresh `scout_field_notes`, with no once-only check, every time it is sent (`entity/npc/dialogue/route/ScoutDialogueEvents.java:26-49`).
  - The Guardian's dagger exchange works remotely (`GuardianDialogueEvents.java:28`).
  - The Liber unlock in RT-18.
- **Already safe:** the other routes re-check their speaker (Vicar, Alchemist, Mnemonist, Court, Circus, Annetta and others).
- **Fix:** invert the default. Require a speaker within reach for every event except the listed whisper events, or record the tree the server last sent to each player and accept only its option ids.
- **Effort:** M.

**RT-21. `DialogueTopicOpenedPacket` saves any `hemomancy:` id it is sent, with no cap.** Medium. Verified.

- **Where:** a modified client can send unlimited distinct `hemomancy:<random>` ids with entity id 0 (`network/dialogue/DialogueTopicOpenedPacket.java:29-38`). Each one is added to `DialogueKnowledge.readTopics` (`capability/player/shared/knowledge/DialogueKnowledge.java:19-21`), which is saved and copied on death, with no size cap.
- **Effect:** player-data bloat and slower saves.
- **Fix:** accept only topic ids from the loaded dialogue trees or inquiry data, require a speaker in reach, cap the set, and rate-limit the packet.
- **Effort:** S.

**RT-22. The Visceral Mirror acts on whoever is nearest, and anyone in reach can cancel it.** Medium. Verified.

- **Where:**
  - `startRitual` checks the sender's degree, scalpel and blood (`tile/harbinger/functional/VisceralMirrorBlockEntity.java:121-178`) but does not record who started.
  - Each tick, `serverTick` charges blood to `findNearestInitiatedPlayer()` (any non-spectator within 5 blocks, :89, :299-303). That player is also hurt every second during extraction (:224) and checked for the echo (:238-246).
  - `VisceralMirrorCancelPacket` lets any player within 8 blocks call `cancelRitual` (`network/capa/harbinger/visceral/VisceralMirrorCancelPacket.java:46`; `VisceralMirrorBlockEntity.java:182-194`), wasting the blood already drained.
- **Effect:** a bystander, even an Unstained or D0 player, can be drained and hurt; a griefer can cancel the ritual.
- **Fix:** store the starter's UUID, act only on that player, cancel if they leave, and accept cancels only from them.
- **Effort:** S.

**RT-23. Key-press payloads have no rate limit or liveness check.** Low. Verified.

- **Where:**
  - `BloodFormationKeyPressPacket` has no cooldown and no alive, spectator or `isActive` check (the volume is 0 when inactive), and each press broadcasts 30 particle packets (`network/capa/harbinger/BloodFormationKeyPressPacket.java:40-66`).
  - `GroundBloodDrawPacket` is sent on every client tick while the key is held (`client/event/ClientInputEvents.java:103-105`). Each one sends up to 4 particle packets and has no alive or spectator gate.
  - `UseStillArtKeyPacket` (`network/capa/unstained/UseStillArtKeyPacket.java:27-33`) and `BloodCraftingKeyPressPacket` (:35-45) have no `activePlayer` check. The latter still decodes an unused client `ItemStack` on every press (:25-28).
- **Fix:** `ServerboundGuards.activePlayer` on all four, a short server-side cooldown on formation, and drop the `ItemStack` (protocol bump).
- **Effort:** S.

**RT-24. Request-and-reply payloads amplify without a limit.** Low. Verified.

- **Where:** each of these answers every request, with no limiter:
  - `BloodVolumeClientPacket`: `syncVolume` (5 packets) plus pome progress (:28-36).
  - `KnownManipulationClientPacket`: 2 packets (:29-37).
  - `KnownSummonsRequestPacket`, `PacketRequestPoolData`, `PacketRequestSpecimenBestiary`.
  - `ResidentsRequestPacket`: a snapshot of every resident of the line (`network/succession/ResidentsRequestPacket.java:53-59`).
- **Fix:** a shared per-player limiter like `NpcTradeRequestLimiter`.
- **Effort:** S.

Known and still present: F-4 (ScaleGrip parking map), F-5 (OrganEcho clears hotbar slot 0 for an off-hand duplicate, `item/harbinger/OrganEchoItem.java:89-90`), F-19 and S-5b.

### 6. Persistence

**RT-25. Temporary frozen cruor becomes permanent across a restart, and its upkeep reads unloaded chunks.** Low. Verified.

- **Where:**
  - `TemporaryIceManager` keeps expiries in static maps cleared at server stop (`manipulation/congeatio/TemporaryIceManager.java:21-22, 28-32`).
  - `frozen_cruor` is a copy of packed ice with no random ticks (`init/BlockInit.java:249-251`), so ice placed shortly before a stop or crash never goes away. Encounters have a reload fallback (:78-90); casts outside encounters do not.
  - The upkeep calls `getBlockState` on its entries every 40 ticks and at expiry with no loaded check (:107, :111-112).
- **Fix:** keep the entries in a per-level SavedData, or put a scheduled tick or expiry on the block; guard reads with `hasChunkAt`.
- **Effort:** M.

The SchoolCombat save bloat on every mob is part of RT-3, and the unbounded player-data growth is part of RT-18 and RT-21. The round-trip sweeps found nothing else (see "Not a problem").

### 7. Error handling

**RT-26. Routine events write log or chat spam.** Low. Verified.

- **Where:**
  - `CardinalRiteStructure.verify` logs a WARN line for every pattern position, matching or not, whenever a rite's structure no longer matches (`rite/harbinger/CardinalRiteStructure.java:48-85`). That is tens to hundreds of lines each time a player breaks a rite block.
  - `BloodVolumeEvents.onDimensionChange` still sends a "Welcome, current blood volume" chat line on every dimension change (`capability/player/harbinger/bloodvolume/BloodVolumeEvents.java:435-443`, listed in the prior audit).
- **Fix:** log one summary line at WARN with the detail at DEBUG; remove the welcome line.
- **Effort:** S.

Counts:

- 46 broad `catch (Exception|Throwable|RuntimeException)`; 17 empty or ignore-only catch blocks. All of them are NBT, number or enum parsing of saved or typed data, apart from `capability/player/harbinger/manip/ManipulationCostLedger.java:70` (F-25).
- 0 `printStackTrace`; 0 `System.out`/`System.err`.
- 7 `LOGGER.info` calls, all at load or registration time.

### 8. Dimension and teleport flows

| Flow | Entry | Disconnect | Crash or stop | Death | Missing dimension | Verdict |
|---|---|---|---|---|---|---|
| Chamber visits (dream, guided, rite, admin, chair) | `encounter/ChamberVisitService.startVisit` :215 | `event/ChamberVisitEvents.onLogout` :60-65 returns the player and restores the snapshot | the snapshot sits in root persistent data (:229-238); login outside the Chamber recovers (`ChamberVisitEvents` :41-50); inside, the timer continues (:256-292) | protected visits cancel death and return (`ChamberVisitEvents` :75-81); unprotected visits die normally, carrying real inventory | `ChamberOfWillManager.java:130` logs, then `startVisit` recovers (:239-243) | OK |
| Mycophant arena | `MycophantEncounterManager` :167-172 | the active flag persists; login outside the Chamber clears it (:428-433) | same | death cancelled, attempt failed (:421-425); leaving the dimension fails it (:436-439) | Chamber-gated | OK |
| Vesper ordeal | `VesperOrdealManager` :88-92 | login recovery rules (:403-420) | same | `abandonAttempt` on real death (:424-429) | `getLevel` null-checked (:144) | OK |
| Gardens Spine travel and return | `FungalGardenTravelHelper` :195-235 | the return point is in root persistent data (:180-186) | same | the return point is lost on respawn (respawn at bed, by design) | a null Gardens returns early (:197); return falls back to the overworld (:216-218) | OK |
| First-visit projection | `event/FungalProjectionEvents` :25-40 | logout forces the return (:46-50) | the countdown persists; the tick forces the return (:30-33) | damage cancelled (:61-63) | n/a | OK |
| Visitation | `encounter/VisitationService.visit` :37-56 | physical travel | | normal | refused when the Gardens are missing (:32-33, :39) | OK |
| Possession | `capability/player/harbinger/possession/PossessionService.begin` :131-200 | `returnNow` (`PossessionEvents` :174-176) | the attachment is copied on death; login runs `returnNow` (:179-183) | death cancelled, then eject (:136-141); leaving the Gardens ejects (:149-152) | `inGardens` check | OK |
| Earthen Vein travel | `world/vein/EarthenVeinTravelManager` :110-140, teleport :261 | cancelled through the hub (`TransientServerStateEvents.java:176`) | session lost; the player stays at the source | cancelled (:176-178) | destination validated | OK |
| Axonal transduction | `manipulation/ductilis/AxonalTransductionManager.begin` :99-110 | hub logout (:175) | a recovery tag in persistent data (:105-108) | n/a | home level resolved | OK |
| Ledger recall, rite summons, NPC moves | `PacketLedgerAction.java:238, 358`; `CardinalRiteCompletionEffects.java:629`; `SuccessionResidents.java:117`; `CardinalRiteNpcTravel.java:179` | instant | instant | n/a | results checked (`moved != null`) | OK |

No new findings. Two cross-cutting items are already pending decisions:

- Several `LivingDeathEvent` handlers cancel death at NORMAL priority (`event/ChamberVisitEvents.java:75`, `event/PossessionEvents.java:136`, `encounter/MycophantEncounterManager.java:421`), and their order against ScaleGrip's parking (F-4) and other death handlers is undefined. That is G-1/F-18 (one death-save stage).
- Visit and return state lives in root persistent data, not `PlayerPersisted`, so it does not survive death. That is the intended reset.

### 9. Config load order and registry use

No findings.

- Server config reads guard against an unloaded spec where it matters (`config/HemoConfigValues.java`; `encounter/MycophantEncounterManager.java:417-418`).
- Static registry reads (`client/render/entity/projectile/LivingSickleHookRenderer.java:20`; `worldgen/biome/FungalGardensSurfaceRuleData.java:14-17`) run in classes loaded after registration.
- Configs register in the constructor (`src/main/java/com/vincenthuto/hemomancy/Hemomancy.java:52` → `config/HemoConfig.java`).
- I did not prove that none of the 88 `HemoServerConfig.X.get()` calls is reachable from a client tooltip in the main menu. The ones I sampled (morphling upkeep, borrowed blood, Drudge) are server paths.

---

## Status of the prior performance audit

| Item | Status | Where now |
|---|---|---|
| P-1 Elytra attribute churn | Fixed | `HarbingerEquipmentEntityEventHandler.java:146` (`AttributeModifiers.sync`) |
| P-2 School combat on every entity | Still present | RT-3 |
| P-3 Stillwater scan | Still present | RT-4 |
| P-4 Undiffed broadcasts | Still present | RT-7 |
| P-5 Black Heart broadcasts, effect re-applies, Blood Binding | Still present | RT-5, RT-6, RT-7 |
| P-6 VialRack and RecycledEnzyme copies | Still present | RT-11 |
| P-7 Linear rite lookup | Still present | RT-8 |
| P-8 KnownManipulation snapshot diff | Fixed | dirty flag (`PlayerStateSync.java:89-91`) |
| P-9 RenderType per call | Fixed and guarded | `RenderTypeCachingGuardrailTest` |
| P-10 Client entity-id caches | Mostly fixed | the rest in RT-14 |
| P-11 Dead `RenderBloodLaserEvent` | Fixed (removed) | |
| SerpentineIdol on both sides; SanguineFertility `AABB` on both sides | Still present | RT-5; `effect/SanguineFertilityEffect.java:27-33, 81-83` |
| Alembic and Retort per-tick recipe matching | Still present | table (Watch) |
| EngramTextureCache race | Still present | RT-15 |
| Edacious abilities packet every tick | Still present | RT-6 |
| "Welcome" chat on dimension change | Still present | RT-26 |
| `PendingBloodCraftManager` | Lifecycle fixed, tick rate pending (F-11) | `TransientServerStateEvents.java:118`; `HarbingerCardinalRiteEvents.java:102` |

## Not a problem

- **Payload threading:** the default PayloadRegistrar thread is the main thread and no payload opts into the network thread, so no handler touches a `Level` off-thread.
- **Charged casts:** the server credits only the hold it watched (`manipulation/ManipulationChargeClock.java:20-48`).
- **Cooldowns:** manipulation (`manipulation/BloodManipulation.java:509`), Still Art (`manipulation/stillarts/StillArt.java:132`) and spore burn cooldowns are enforced on the server; `ClientManipulationCooldowns` is a display mirror.
- **NPC trades:** slots validated, reach and tradesWith checked, rate-limited, price taken before the goods are given (`entity/npc/trade/NpcTradeService.java:209-226`).
- **Earthen Vein:** selection is nonce-checked against known veins (`EarthenVeinTravelManager.java:110-140`).
- **Heart-node settings:** owner check, and Arbor fruit is owner, visibility and distance checked.
- **Axonal input:** checked against the session token.
- **Bloodline leadership:** re-derived from saved data at every login (`BloodVolumeEvents.java:447-466`), so a stale local copy cannot kick or chat as a former leader across a relog.
- **Escrowed brazier offerings:** only shown in the bowl (`tile/harbinger/rite/IronBrazierBlockEntity.java:116-127`); breaking the brazier drops only the real offering (`block/harbinger/rite/BrazierBlock.java:351-365`), so there is no escrow dupe.
- **Warp chairs:** paired chairs check their owner (`block/harbinger/functional/WarpChairBlock.java:112-113`).
- **Ward anchors:** `WardAnchors` is side-split and cleared; mausoleum thresholds read it instead of searching.
- **Lodestone fields, Court vacancies, Vagrant Mind restores:** each checks that its chunk is loaded before reading blocks.
- **Reload listeners:** they share `data/JsonResourceReloadListener.java`, which logs and skips a bad file per file and publishes an immutable map only after preparation.
- **Block-entity save/load:** a key-symmetry sweep of every `saveAdditional`/`loadAdditional` pair found only legacy keys that are read when present (`TAG_BLOOD_LEVEL`, `BloodVolume`) and keys handled in helper methods.
- **Attachments:** the same sweep over every attachment's `serializeNBT`/`deserializeNBT` found only false positives. `spore_load`, `cooldowns` and `surrogate_tether` skip copy-on-death on purpose.
- **SavedData:** every mutator in the 20 SavedData classes calls `setDirty`, directly or through `changed()`. Every overworld-only SavedData caller passes the overworld or is overworld-gated (`BloodMoonEvents.java:138`, `RiteZoneEvents.java:34`).
- **Morphling equip:** swapping morphlings re-arms the Last Rite source but not its cooldown (`event/LastRiteHelper.java:37-57`), so it cannot farm death saves.
- **Chamber orphan items:** `ChamberOfWillEvents` only handles items that carry the mod's own owner tag.
- **Prior network fixes:** F-1 (NaN donation), F-6 (ledger needs the item), F-7 (chat rate limit), F-8 (NaN pool settings, clamped partial tick) and F-9 (structure paste needs permission 2 and the spawner) all hold.
- **Logging:** no `printStackTrace` or `System.out` in production code.

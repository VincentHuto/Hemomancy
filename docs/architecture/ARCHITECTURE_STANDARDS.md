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
  `floorMod(seed, 127)`); Ferric and the Outpouring surface marks use it too (V-3b, V-3c).
- Verdigris (`HemoPalette.VERDIGRIS`, 86, 160, 132) is Unstained only; shared particle colours live in
  `common/particle/HemoPalette` (`VerdigrisPaletteGuardrailTest`).
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
- Client animation time comes from `client/render/AnimationTime`. Every world, item and armour animation freezes
  under `/tick freeze`, inventory slots included (A-4, NQ-09-3, built 2026-11-15); an item with no level falls back to
  the wall clock inside `AnimationTime.itemMillis()`. Only screen chrome, the dimension veil, pose cache stamps and the
  rite bar's ease read the wall clock.

## 5. Gameplay mechanics

| Mechanic | Use |
|---|---|
| Spend a player's blood | `BloodAccounting.pay` (one-off) or `payUpkeep` (recurring) with a named `Source{id, category, reserveProtected}` constant; it records to `BloodFlowLedger` and ends in `BloodVolumeEvents.syncVolume`. There is no spend count: `addBloodSpend` and the `countsAsSpend` flag are retired (NQ-08-20) |
| Projected blood into a block | `ProjectedBloodSink` (default on `IBloodReservoir`) |
| Choose targets | `ManipulationCombatHelper.canHarm(player, target)`, `canHarm(player, target, Reach.X)` for a named wider preset (the two Monster filters), `canHarmFor(source, target)` for Drudges, constructs, summons, projectiles and effect holders (the Verdigris Aura among them) |
| Drudge work | `DrudgeActions` |
| Heal reductions | `HealModifier` entries, applied in one stage |
| Change or read a hit's final amount | A call in its stage of `common/damage/DamageStages` (attackers, defenders, death-saves, consumers); never another `LivingDamageEvent.Pre` subscriber |
| Save a player from a lethal hit | A Last Rite in `DamageStages.deathSaves`, gated by `LastRiteHelper.canFire` and spent with `consume`; Ink Mantle and Cryptobiosis answer a landed hit from `LivingDamageEvent.Post` |
| Flight | `FlightArbiter` grants and revokes; a revoke keeps flight while another source still grants it (G-7b). A grant ends with its source: the Elytra effect's ends when the effect runs out or is removed (`ElytraEffectEvents`) |
| Is it a boss | `HemoEntityPredicates.isBoss`, which reads only the entity type tag `hemomancy:bosses`; a new boss body joins the tag. No class-name or package check. Rite sacrifices and `puppet_attention_immune` (`#hemomancy:bosses`) read the tag; kill blood gain x5 (Dragon and Wither only) is the one named exception (G-2c, NQ-07-3) |
| A block a player causes to be placed, broken or converted (a cast, a payload, an item, a block's use, an event handler, a rite, a projectile, a run or other world system acting for a player) | `PlayerBlockEdits.place` / `mayBreak` / `breakBlock` (drops for a tool) / `replace` (in-place conversion: break then place checks) / `lift` and `settle` (take a block out without drops and put it back) (game mode, spawn protection, the NeoForge break or place event). The break event `mayBreak`, `replace` and `lift` fire is a check: a break listener that answers a real break (vein mining, Laboring Arms, routing-link cleanup) returns early while `PlayerBlockEdits.isCheckingBreak()` |
| Held attribute bonus | `AttributeModifiers.sync` |
| Armour set check | `ArmorSets.isFullSet` |
| Degree and Unstained-stage gates | `ProgressionGates` (never a literal degree) |
| Multiblock placement | `PatternOrigins` |
| Block tickers | `HemoTickers.create` in `getTicker` |
| Resend a block entity | `BlockEntitySync.markAndSync` |
| Earned items that may not fit | `PendingDelivery` (living player, not observational, confirmed insertion) |
| A slow world search for a player (an item that locates a structure) | `common/util/OffThreadSearch` on the background pool, one search per player, answer back on the server thread; random-spread structure sites predicted without chunk loads by `StructureSitePrediction` (the Scarlet Waybill's `CircusWaybillSearch`; the Relay Chart's `CourtSeatSearch` uses the helper with its own seat search) |
| Start a Cardinal Rite | `CardinalRiteActivation.tryStart`; kind behaviour is a `RiteKind` hook |
| Dialogue option events | `DialogueEventRouter` and one `<Npc>DialogueEvents` class per NPC |
| NPC trades | data under `data/hemomancy/npc_trade/<speaker>/`, run by `NpcTradeService` from `NpcTradeRequestPacket`; never a `DialogueEvent`, a memory, a kit or a progression proof |
| `/hemo` branches | one `<Name>Subcommand` class wired by `HemoCommand` |
| An entity casting a manipulation | `setEntityCast(EntityCasts::x)` in `ManipulationInit` |
| What a selected manipulation makes of Projection or Absorption (a Sculpting) | `setProjectionSculpting(Sculptings::x)` / `setAbsorptionSculpting(Sculptings::x)` in `ManipulationInit`, run only by `SculptingDispatcher`, which the verb chains ask after founder initiation, Saints, Wills and Cardinal Rites (and, on Absorption, the block endpoints) and before the plain verb's endpoints. A pulse pays through `BloodManipulation.tryPerformVerbPulse` (the cast gates, then `BloodAccounting.payUpkeep` as `sculpting`); targets come from `VerbTargeting.pick`, reach from `VerbReach.of`. A narrow Sculpting that takes a few targets ahead of the main one is `addProjectionSculpting(Sculptings::x)` / `addAbsorptionSculpting(Sculptings::x)`, tried in the order attached before the main one; a held channel already open keeps the channel ahead of them all, and a session continues only for the same Sculpting (`SculptingSessions`; round two P4) |
| A block's blood (a station tank, a buffer, a Tap) | The `hemomancy:block_blood_volume` attachment is a `BlockBloodVolume` (fresh, clot, feeding stamp, owner), read only through `ReservoirBlood.settled(blockEntity)`, which settles clotting against the level clock (`CoagulationRules`); `IBloodReservoir.getBloodCapability` and `HemoCapabilityAccess.getBloodVolume(BlockEntity)` both go there. A living holder says so with `isLivingVessel()`; a block with no tank with `holdsBlood()` false. Clot leaves a block as Formations only through `HeldFormations`; who may draw is `BloodDrawRights` (core-loop Track 3). A multiblock filler keeps no tank: `ReservoirBlood.tank(blockEntity)` resolves it to its main block's, and an orphan filler reads as no tank (round two P3) |
| A crawl through connected blocks a Sculpting or tool breaks | `common/manipulation/verb/CrawlSession` (selector, budget, `dismantle` / `dismantleExcept` through `PlayerBlockEdits.breakBlock`); Lignum Mortis uses it, and every ore vein (Lode Draw and the Talaromyces scar) goes through `common/manipulation/ferric/OreVeins`, whose `mine` sets the player's vein start so its own breaks never open a second vein (core-loop Track 8) |
| How much the Living Staff has grown, and what its digging forms may mine | Rings from `LivingStaffProgress.bloodHandled` through the pure `LivingStaffGrowthRules`; blood moved through the Staff is added only by `LivingStaffGrowth.record`, which announces a closing ring. The Staff and its forms carry the count as the `hemomancy:staff_rings` component (`LivingStaffGrowth.stamp`), which the digging forms' tier and the client's `LivingStaffRingModel` read; the focus profile carries it as `growthRing` for reach and rates. A digging form (`LivingToolItem.digs()`) pays per block only through `LivingDigging` (core-loop Track 8) |
| A combat strand on held Projection (a lash; the jet, spray, plant and brand kits stay for tests only while CQ-25 waits) | Data on the registration: a `Sculptings` factory calls `Strands.lash(...)` with a `LashProfile` and its `LashStyle`, and `ManipulationInit` attaches it; the client sends the whip aim and the flick (`LashAimPacket`), the tap (`LashTapPacket`) and the accent (`StrandSnapPacket`), and the view turns a capped share while a lash swings (`LashCamera`); `StrandService` runs every open strand as a held channel (`VerbSculpting.holdsChannel`), its rules are pure classes (`LashRules` and the rest) shared by server and client, and every hit goes through `StrandHits.strike` (`canHarm`, the cast scope, a fresh root attack). Targets come from the two shared gathers, `ManipulationCombatHelper.swept` and `cone`. The strand package names no manipulation (`SculptingGuardrailTest`); clients draw from the keyed `STRAND` cue and their own run of the rules, never per-tick packets (core-loop Track 12). What a strand skill level is worth lives in the pure `StrandSkills`; Long Strand, Whipcrack and Free Hand also reach the held manipulations through `HeldSkills` (CQ-20 B), and Second Strand is the lash's twin only (CQ-22 A). A skill the clients need to run the caster's motion and the off-hand twin ride the cue's own bits (20 to 22; 23 and 24 a lash's phase), not the client's skill cache. A price or reach that depends on the player is `VerbSculpting.bloodPerPulse(Player)` / `reachScale(Player)`, read by the verb's payment, the HUD and the tests alike (round two P5) |
| A real block a manipulation takes out of its place for a while (Living Land's strikes and raises) | Lifted and set through `PlayerBlockEdits.lift` / `settle`, and recorded in the level's `common/world/livingland/LivingLandLedger` until it is back. Whatever carries the block (a strike entity, a raise held by a session) touches its record every tick; the ledger's sweep settles anything untouched for 40 ticks, and a removed strike settles at once. A block goes back to its own cell or the nearest free one and is never dropped as an item (`living_land_validation` counts every block before and after) (core-loop Track 9) |
| A Sculpting refusing a target for reasons of place or right (a ward, a rite space, protected ground) | `VerbSculpting.admits(player, target)`, asked by `BloodManipulation.tryPerformVerbPulse` on every pulse, after the cast gates and before any price, work already running included (CN-13): a refusal keeps the claim (the plain verb does not run) and costs nothing; the Sculpting says why itself (core-loop Track 9) |
| A player clinging to a wall or ceiling | Server-owned transient state `hemomancy:cling` (`ClingState`) with its `PlayerStateSync` row `CLING` and a tracking send; the numbers are the pure `ClingRules`, the faces `ClingSurface`, shared with the Holdfast morphling. The owner's client lays walking input on the face; the server holds the body between move packets and judges where it ended up. Not flight (`FlightArbiter` is untouched) and not a hold (`HeldTraveller` is untouched) (core-loop Track 9) |
| What Absorption gains from a creature's school state | One table, `common/damage/AbsorptionRiders`, keyed by `SchoolState`; `AbsorptionRiderEffects` applies it after each draw. Never a per-manipulation absorption hook (core-loop Track 12, CL-26) |
| Temper, the spendable pool per school beside Temperament | The `hemomancy:blood_temper` player attachment (`BloodTemper`) with its `TEMPER_POOL` sync row; numbers in `TemperRules`, catalysts in `data/hemomancy/temper_catalyst/<school>.json` (`TemperCatalysts`); a cast or Sculpting pulse charges it only through `TemperCharge` in `BloodManipulation` (planned before pricing, so the cost ledger shows the dry line; settled after payment); a new source fills through `TemperCharge` in the temperament package (`TemperGuardrailTest`) |
| Teach a core-loop system with an advancement (a first Sculpting, scraped clot, a dissolved catalyst, a leash, a lit gouge run, a Staff ring, a lash crack, a cling, a paying meal) | An `impossible`-trigger advancement in `data/hemomancy/advancement/hemomancy/`, its constant in the "Core loop firsts" block of `HarbingerAdvancementGranter`, and one `grantIfNotDone` call where the act happens (after payment, never on a refused attempt). A memory that sculpts a verb carries its `hemomancy.manipulation.<id>.sculpting.<verb>` line and is named on a Liber Sanguinium page (a family form on its family's page) (`CoreLoopTeachingGuardrailTest`, core-loop Track 11) |
| A Liber page a player should be able to read | A constant in `LiberEntryDefinitions` (a literal `entry("...")` or `immaculatusEntry("...")` path) registered with its discovery sources, and at least one way to open it: a Degree floor in `LiberKnowledgeHelper.unlockForDegree` (a purity or clarity stage in `unlockForUnstained` for Liber Immaculatus) plus earlier openers in `LiberDiscoveryEvents` (an item, which opens only on a ground pickup or an explicit `unlockForItem`, so never an item alone; an advancement). A memory learned opens its school's pages through `LiberKnowledgeHelper.unlockForMemory` (`LiberPageRegistrationGuardrailTest`, round two P9) |
| A dev fixture for a core-loop state (`/hemo temperpool`, `tank`, `vat`, `gouge`) | A dev-only `<Name>Subcommand` (`.requires(DevCommands::available)`) over `common/command/CoreLoopFixtures`; session state keyed by UUID and cleared through `TransientServerStateEvents`, no `getPersistentData`, feedback without positions (`HemoCommandTreeTest`, `hemo_command_validation`, `core_loop_validation`; round two P11) |
| What another player's client needs to draw a caster's verb (reach, active verb, selected manipulation) | `common/manipulation/verb/VerbPresence` sends `PacketSyncVerbPresence` to trackers only while a verb is channelled (forced as a channel opens, then on change every 10 ticks, and on start-tracking); the client keeps it in `client/data/OnlookerVerbState` and common code reads it through `ClientBridge.onlookerReach`, `onlookerSelection`, `onlookerVerb` (`VerbReach.of`, `SculptingDispatcher.selectedManipulation`, `VerbChannel.activeVerb`); nothing is computed for an idle player (round two P11) |
| A Sculpting that draws its own link from hand to target (the Tendon Line's cord) | `VerbSculpting.drawsOwnStream()` true: the cell hand's stream is `replaced` while it claims (round two P11) |
| A sutured machine's stall reason on its own screen | `BloodRoutingHelper.linkStatus` (one rule with `canUseLink`), kept on the tank as `BlockBloodVolume.link` (saved under `Link` only when not `NONE`) by the routing pass, resent with `BlockEntitySync.markAndSync` only on change; `ReservoirReading.link`; `BloodVolumeBarWidget` draws the pip and hover (round two P11) |
| A crop's growth answering blood | One pure rule, `common/world/farm/BloodFedGrowthRules` (levels 0 to 3, the verdict on a growth check); `BloodFedGround` works out a crop's level when asked and stamps fed soil on the `hemomancy:fed_ground` chunk attachment (`FedGroundStamps`); the one crop subscriber is `common/event/BloodFedGrowthEvents` on `CropGrowEvent.Pre` and `BonemealEvent`, never a block-class check, so other mods' crops behave. A crop that needs blood is tagged `hemomancy:needs_blood_to_grow` (core-loop Track 7) |
| Keeping a creature (leash, jar, vat) | Who may be kept is one pure rule, `common/entity/menagerie/MenagerieRules.canKeep`, asked through `MenagerieLeash.verdict`, which tells the mod's people, Wills and other unkeepable kinds by the entity type tags `hemomancy:menagerie_people`, `menagerie_wills` and `menagerie_unkeepable`, never by class name (CR-26, `MenagerieKeepingGuardrailTest`); the leash lives on the creature as the `hemomancy:menagerie_leash` attachment and is run only by `MenagerieLeash`; a kept creature's state is `KeptSpecimen` inside a `SpecimenVessel` block entity (living buffer, never clots), with its pure numbers in `VatVigorRules`; the jar and vat blocks share `SpecimenVessels` (core-loop Track 5). A vessel's screen is `SpecimenVesselMenu`, no slots, whose data the server works out per viewer in `VesselView`; its one button dispatches to the vessel's own `SpecimenVessel.pressBrood` (which runs `SpecimenBrood.press`), and breeding lives in `SpecimenBrood` with its pure numbers in `BroodRules`; the menu holds no work of its own and adds no payload: its values travel as menu data slots scaled to fit a short, its button through vanilla `clickMenuButton` (round two P6) |
| What a player knows of a creature's blood | `common/mission/alchemist/BloodResearch` over `ClinicalBloodProgress.examinedSources` (synced to its owner); tooltips, readouts, the vessel's screen and the Living Bestiary ask it, never the stack's identified flag (core-loop Track 5). Kinds met at close quarters are `BloodResearch.met` / `recordMet` over `ClinicalBloodProgress` `Met`, recorded only for a profiled kind and a player the clinical lessons are open to, and written only when not empty (round two P6) |
| How fast a body makes blood, and what food gives | One pure rule, `BloodRegenRules` (the food bar's multiplier, Well Rested, a meal's blood), read by the regeneration tick in `BloodVolumeEvents`; meals are data in `data/<ns>/blood_food/` (`BloodFoods`) paid by the one hook `DailyLoopEvents.eat` on `LivingEntityUseItemEvent.Finish` as `BloodFlowLedger` income; Temper from food goes through `TemperCharge.fillFromFood`. Never a per-item `finishUsingItem` payment (core-loop Track 10) |
| Blood left in the world, and what answers it | The level saved data `common/world/answer/BloodSpilledLedger` (a decaying count per 64-block region, settled lazily; pure numbers in `WorldAnswerRules`), fed only by waste through `WorldAnswer` (`stainLaid`, `stainCleaned`, `chumPoured`, `killed`), never from a payment's category. Readers ask `WorldAnswer` or the ledger: the Blood Moon roll, the Wills' chance, the scavenger and hunter draws, stained sod, and the barometer and clock (blockstate on their own scheduled tick). Ambient draws go through `WorldAnswer.scavengersAwake` (config `world_answer.scavengers`, never on Peaceful); the GameTest server turns them off (`GameTestWorldAnswer`) and tests call the draws directly (core-loop Track 10) |
| Blood in water (chum) | Pools live in level saved data `ChumPools` (pure numbers in `ChumRules`), poured only by the projection chain's water link `ChumPools.tryProject`; the lure is `MixinFishingHook`, the catch the `hemomancy:chum_catch` global loot modifier, the look the toolkit form `CHUM_BLOOM` (core-loop Track 7). The hunter a strong pool draws is chosen by the water in `WorldAnswer.drawHunter` (pure `WorldAnswerRules.hunterWaters`), capped across kinds by `#hemomancy:chum_hunters` (round two P7) |
| Fungal strains | One row per strain in `common/block/harbinger/plant/FungalStrains` (spore, block, school, substrate group); blocks registered from the table in `BlockInit` and planted by their spore (`ItemNameBlockItem`). Strains grow through the NeoForge crop hooks and are tagged `needs_blood_to_grow`, so `BloodFedGrowthEvents` stays the one crop subscriber; a strain is never a `CropBlock`, so the cloche keeps culturing spores. Substrates are tags (`#hemomancy:strain_substrates/*`), never block classes (round two P7) |
| A mark blood leaves on the world (stain, gouge, living vein, Lux trace) | A multiface block in `common/block/harbinger/ground/` extending `GroundMarkBlock` (no shape of its own; a ray finds it from the face it lies on, `GroundNetwork.markAt`). Its rules live in `common/world/ground/GroundNetwork` with pure numbers in `GroundRunRules`; a lit cell's clock in `GroundRunSavedData` and its own scheduled tick; vein length per owner in `VeinLedger`; catalyst functions in `data/hemomancy/gouge_catalyst/<function>.json` (`GougeCatalysts`, read through the Temper catalyst table). The projection chain's last link is `GroundNetwork.stain`; the ground's claims sit after the block endpoints in both verb chains (`SculptingGuardrailTest`). Lit runs are drawn only by `GroundRunRenderer` (core-loop Track 6). A lit run carries the UUID of the player whose pulse lit it (`GroundRunSavedData.Lit`, the six-argument `GroundNetwork.pulse`); its block edits (lighting, freezing, stoking) go through `PlayerBlockEdits` for that owner, only while they are online, alive and in the run's level, and an ownerless run edits nothing (round two P3). A furnace burned from outside (a hot run, Furnace Veins) goes through `FurnaceHeat.stoke`, which lights it as a `PlayerBlockEdits` edit before raising its burn time; Blood Eclipse's field of dark is the session service `DarkFields` (numbers in `DarkFieldRules`), read by `SchoolAwareness.canPerceive` and drawn as the `UMBRAL_FIELD` cue with `UmbralFieldFog` (round two P8) |
| Which verb a player channels | `VerbChannel` (a cell hand is its verb; the Living Staff channels the active verb in `IKnownManipulations`); switched only by the verb key's `VerbSelection` |
| Station operations | in the station's block entity; its menu only offers and dispatches |
| A core-loop lesson an NPC teaches | One row of `common/entity/npc/dialogue/CoreLoopLessons` (speaker, Degree range, branches, Liber pages); `CoreLoopLessonDialogue.append` adds the unread lore cards at the end of the speaker's `progressionDialogue`; the event is taken on the first line of that speaker's `<Npc>DialogueEvents.handle` by the package-private `route/CoreLoopLessonRoutes.teach` (speaker type, alive, 8 blocks, blood awake, not Unstained, Degree range), never a new subscriber or route. A lesson opens its Liber pages (source DIALOGUE) and nothing else; ids use `craft` and never a Quest Work word (`CoreLoopLessonsTest`, `CoreLoopLessonGameTests`; round two P10) |
| Per-strain Morphling data (tempers, passive, Cradle, Last Rite, polyp family, HUD art, order) | one row of `MorphlingStrains`; code reads the row, never a `morphling_<strain>` id |

The two Monster filters spare the caster's allies (G-2b, 2026-11-13): a Drudge or other non-player caster asks
`canHarmFor`. Area damage from armour, Still Arts, Sheolic and Saprovitta asks the strict `canHarm` and names the player
in its damage source (F-16, 2026-11-13); Sheolic retribution places its flames through `PlayerBlockEdits`. The unused
morphling look target and `Reach.UNGATED` are deleted, and the Verdigris Aura asks `canHarmFor(holder, target)` (F-16,
2026-11-14). Every handler of a hit's final amount runs from the one stage table in `DamageStages` (G-1 / F-18,
2026-11-14): attacker bonuses, then defender reductions (flat ones first, no cap on stacking, NQ-17-2), then the
death-save stage, then the readers. Wound blood, vein strain and the Mnemonist puppet's memory read the amount that
lands, so a saved hit costs blood only for the damage that got through (NQ-17-1). The Curved Horn is a Last Rite: it
arms only while nothing else is armed, any other source arms over it, and it shares the 12,000-tick cooldown (F-17,
NQ-17-3). The `combat_order_validation` goldens pin the amounts and the save order.

## 6. Persistence and state

| Kind of state | Home |
|---|---|
| Durable per-player progress | An attachment in `HemoAttachmentTypes` (`copyOnDeath`), read and written through `HemoCapabilityAccess` |
| Persisted cooldowns | An entry in `PlayerCooldowns` (resets on death and End return), unless part of a system's own attachment |
| Per world | A `SavedData` with `Factory` + `get` |
| Credit earned while its player was offline (a structure craft closing, a Loom climax) | `OfflineCredits`: world `SavedData` keyed by UUID, applied once at the next login through its table of appliers (PT-17, PT-20). Not a migration and not `PlayerDataMigrations` |
| Per block entity | Block-entity NBT; blood only in the `block_blood_volume` attachment (white humor in `block_white_humor_volume`) |
| Per item | A typed data component from `DataComponentInit` for new data; an item being reworked moves its new data to one (O-5) |
| Session-only server state | A static UUID-keyed map registered with `TransientServerStateEvents`; a periodic broadcast keeps what it last sent each player in a `LastSent` and sends only a change (FX-14); a send made outside the periodic pass forgets that mark (`ChamberOfWillManager.sendScene`). The Cardinal Rite list goes to each player only for the rites within 128 blocks and the rites they cast or help, and an empty list goes once (`CardinalRiteSync`) |
| Client caches | Cleared in `ClientSessionEvents` on logout |

- Player accessors return the attachment itself (no `Optional`). Owner sync goes through the `PlayerStateSync`
  table, sent by `HemoCapabilityRegistrar` on login, respawn and dimension change; changes outside the senders raise
  the attachment's dirty flag for the end-of-tick flush.
- The mod is unreleased, so player data changes need no migration. After release, old player data is rewritten once, at
  login, by an entry in `PlayerDataMigrations` gated by `PlayerDataVersion` (the table is empty; the next version is 12).
  Never rename keys inside an existing attachment. A new `getPersistentData()` call needs an entry in
  `src/test/resources/architecture/persistent-data-allow-list.txt`.
- An owned entity's synced owner UUID is a `SyncedOwner`. A block entity that syncs extends `HemoBlockEntity` or
  `HemoContainerBlockEntity`. A save from before the attachment-only rule still carries a block entity's own blood
  key, which is read only when present.

## 7. Events and lifecycle

- Where order matters (damage, death, heal) use explicit priorities and a documented stage table; never rely on
  class-scan order. The heal stage table: `HIGH` `PurityGainEvents.onLivingHeal` reads the requested heal, `NORMAL`
  `ManipulationStatusEvents.onLivingHeal` applies every `HealModifier`.
- The damage stage table, `common/damage/DamageStages` for `LivingDamageEvent.Pre`, runs each stage's handlers in its
  listed order:

  | Stage | Priority | Handlers |
  |---|---|---|
  | Attackers | `HIGH` | Sanguine Fists, the Absolved bonus, the Penumbral ambush |
  | Defenders | `NORMAL` | Second Pulse and Silver Ward (flat), Silent Archon negation, the Sheolic stance and retribution, the memory tool's effigy, Iron Will, the avatar, the armour sets (sabatons, Chitinite, thorns, Prismatic flash), Still Waters, then the reactions (Phantasmal, scars) |
  | Death-saves | `LOW` | Endless Hour's deferral, then the one armed Last Rite: Silent Refusal, Phoenix Debt, the Curved Horn |
  | Consumers | `LOWEST` | Wound blood and the Formation drop (`BloodVolumeEvents.onPlayerDamaged`), vein strain, the Mnemonist puppet's memory |

  A saving rite cuts the hit to what leaves 1 health (`DamageStages.leaveBarelyAlive`), and a reader takes
  `DamageStages.landing`, never more than the body held. `LivingIncomingDamageEvent` and `LivingDamageEvent.Post`
  handlers keep their own priorities. The death cancels for possession, the Mycophant encounter and a Chamber visit are
  not saves and stay on `LivingDeathEvent`.
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
- Datagen runs only the blockstate and item-model providers; language, recipes, tags and loot are authored, and
  structure templates are authored NBT. A generated file with an authored twin is deleted.
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
  (`RepoPaths`, `SourceAssertions`, `ResourceReader`); no private `read` / `assert*` copies. Every test is a JUnit
  `@Test`; no test class declares a `main()` (UN-71, 2026-11-16: the 293 legacy `main()` tests were converted and the
  reflective adapter removed).
- GameTests are found only by the annotation scan: every test or generator class carries a `@GameTestHolder`, and
  every namespace is declared in `gradle/game-tests.gradle`. A test that builds outside its own area or counts
  entities over a wide box runs in its own batch and clears what it built.
- A GameTest that fails once and passes on a re-run is a defect (a flake): find the cause and fix it. Every test-grid
  GameTest server runs under the isolation harness (`src/gameTest/resources/gametest-isolation.mixins.json`, the four
  `gametest/mixin/GameTest*Mixin`, settings in `gametest/GameTestIsolation`): structures 64 blocks apart, two margin
  chunks forced and entity-ticking before a test starts, and a random origin snapped to a chunk corner
  (`-PgameTestOrigin=x,z`, `-PgameTestMarginChunks=N`). A deterministic test stays inside its template (or the all-air
  `hemomancy:open_room`), never reads light the tick it placed blocks, seeds its rolls, counts only its own entities,
  waits on conditions rather than fixed delays, and never relies on two `onEachTick` tasks running in a given order
  (they run in hash order); see `docs/TESTING.md` "Deterministic GameTests" (core-loop round two P1, P8).
- Review, observer and profile clients are rows in `gradle/review-clients.gradle`; multi-client launchers live in
  `tools/review/`. `build.gradle` declares only the base runs. A row marked `bare: true` also gets a
  `write<Run>Classpath` task, so a launcher such as `tools/review/review-onlooker.ps1` can start the client as plain
  java after one Gradle call (round two P11).
- Tracked tools (`.gitignore`): `tools/model_export/`, `tools/review/`, the `*.py` in `tools/circus/` (the puppet
  and faculty builders and `verify_troupe_assets.py`; the old site builders moved to `removed_assets/` on 2026-11-15),
  and the worldgen generators (`tools/worldgen/mausoleum_complexes.py` + `mausoleum/`, `brined_court_seats.py`,
  `brined_court_outposts.py` + `brined_court/`, `masons_respite.py`, `harbinger_sites.py` + `harbinger_sites/`,
  `wayside_sites.py` + `wayside/`, `circus_sites.py` + `circus/`). The
  rest of `tools/` stays local.
- Dev-only mods that no class imports are `localRuntime` dependencies.
- `./gradlew.bat test` is green before a change and `alphaCheck` green after it.

## Guardrail index

Every architecture guardrail, by area, with the step that added it. `ArchitectureStandardsIndexTest` checks that each
test under `src/test/java/.../architecture/` is listed here.

| Area | Guardrail | Step | Protects |
|---|---|---|---|
| Registration | `RegistrationStyleGuardrailTest` | 8, 13 | Two subscriber forms, lifecycle-only constructor listeners, event-API client registration, registry lookups, typed components, one WIP list; GameTests registered only by annotation scan |
| Registration | `EntityTypeBuildNameGuardrailTest` | 13 | Entity types built under their registry names (F-26) |
| Registration | `RetiredNamesGuardrailTest` | 13, core-loop Track 11 | Ids and code names renamed to what players see stay gone, with no aliases; the core loop's retired items, payloads and rules (Constrictor Cord, the Tendon Line item, the ground-draw payload and key, the Living Pistol, the Staff's selection rule, the Memory Thread route, the Centrifuge and Alembic blood gain) |
| Registration | `ResourceLocationStyleGuardrailTest` | 13 | Every hemomancy resource location comes from `Hemomancy.rloc` |
| Networking | `ClientBoundPayloadRegistrationTest` | 1 | Client-handled payloads registered once, client-bound |
| Networking | `PayloadDirectionTest` | 4 | Sends match registered directions; one bidirectional payload |
| Networking | `PayloadProtocolVersionTest` | 4 | Payload set pinned to `PROTOCOL_VERSION` (54 since the 2026-11-15 Leave the Gardens button added `HeartNodeLeavePacket`; 51 came from the 2026-11-15 cleats fitting, which added `ToggleCleatsPacket`; 50 came from the 2026-11-12 lash rework, which added the lash payloads; 49 came from core-loop round two P11, which added `PacketSyncVerbPresence` and `VerbRefusalPacket` and re-laid `PacketSyncTendonAnchor` and `PacketSyncActiveRites`) against `src/test/resources/network/payload-registry.txt` |
| Networking | `NetworkClientIsolationTest` | 4 | No client code in `common/network` |
| Networking | `SendFacadeTest` | 4 | Only `PacketHandler` and `DialogueSender` call `PacketDistributor` |
| Networking | `ServerboundGuardsCoverageTest` | 4 | Every serverbound payload uses `ServerboundGuards` or is listed |
| Networking | `NetworkPackageGuardrailTest` | 11 | `common/network` holds payloads and listed helpers only |
| Networking | `ReloadListenerGuardrailTest` | 13 | Loaders on the shared base, one registration place, one datapack sync channel table |
| Client isolation | `WillEntityClientIsolationTest` | 1 | `WillEntity` names no client class |
| Client isolation | `CommonClientIsolationTest` | 8, 13 | Nothing under `common/` names a client class; dispatched payloads have one client handler |
| Client isolation | `ClientSessionCacheTest` | 8 | Entity-id client caches cleared on logout |
| Client isolation | `LidlessClientWiringTest` | Lidless Door | The crossing reaches the client through the existing handler, session clears, overlay grade and world-effect dispatcher |
| Rendering | `ScreenChromeGuardrailTest` | 10 | No private screen chrome copies |
| Rendering | `RenderTypeCachingGuardrailTest` | 10 | Render types built once |
| Rendering | `HudAndWorldDrawingGuardrailTest` | 10 | HUD layers and the one world-effect dispatcher, in their order |
| Rendering | `MaterialQuadGuardrailTest` | 10 | Material vertices through `MaterialQuads` |
| Rendering | `ShaderNoiseIncludeGuardrailTest` | 10 | Shared shader noise in includes |
| Rendering | `LidlessShaderContractTest` | Lidless Door | The Lidless void is one self-contained include that both its shaders import and declare their uniforms for |
| Rendering | `ChamberTransitionScreenTest` | Chamber gateway | The Chamber of Will's dimension change is veiled by our own transition screen both ways, never the dirt screen, and the crossing's colours survive the level swap for it |
| Rendering | `MonolithShaderContractTest` | Chamber gateway | The Monolith's fissure shader lights its cracks by a Fill uniform the husk's glow drives (dark when shut, filling from the base as blood goes in, lit while the eye is open), and its vein overlay has its own vertex stage carrying the surface shader's morph so the tendrils ride the stone instead of hovering |
| Rendering | `ParticleDataGuardrailTest` | 10 | Particle data classes and types |
| Rendering | `VanillaParticleRatchetTest` | cleanup A-33 | Vanilla particle references per file only go down; the sweep is done (2026-11-14) and the 17 files left each list an honest reason (block debris, item crumbs, water, a vanilla fixture's own vocabulary) |
| Rendering | `VerdigrisPaletteGuardrailTest` | VX-8 (2026-11-14) | Only the listed Unstained files name `HemoPalette.VERDIGRIS`; blood-side work never takes the copper green |
| Rendering | `ParticleSpawnSiteGuardrailTest` | FX-8, VX-7 (2026-11-15) | The Morphling Incubator renderer spawns no particle (its block entity's client tick does); each Unstained rite completion calls its own `UnstainedRiteParticles` method, and every method there has its rite |
| Animation | `EasingCopyGuardrailTest` | 6 | No private easing copies |
| Animation | `BossAnimationClockGuardrailTest` | 10 | Boss replay clocks |
| Animation | `PlayerAnimationGuardrailTest` | 10 | Pose registry and hand-renderer priorities |
| Rendering | `BossPresentationBindingTest` | boss presentation | Every boss bar names a frame and sends it on show; played scenes are named ids |
| Animation | `AnimationTimeGuardrailTest` | 10, A-4 (2026-11-15) | Animation time through `AnimationTime`; only the 17 listed wall-clock users stay (11 screens, 3 dimension-veil classes, 2 pose cache stamps, the rite bar's ease), each with its reason |
| Animation | `CircusShowFreezeGuardrailTest` | A-4 (2026-11-16) | The Circus phantom show and its fog read `AnimationTime.partialTick()`, not the world pass's or the fog event's partial tick, so the show holds still under `/tick freeze` |
| Animation | `EntityModelAnimationGuardrailTest` | cleanup A-11 | Every entity model animates; models still by design are listed |
| Mechanics | `SmallUtilityGuardrailTest` | 6 | No copies of the shared small helpers; no literal degree comparisons |
| Mechanics | `BloodAccountingGuardrailTest` | 7 | Player blood paid only through `BloodAccounting`; the retired spend count stays gone |
| Mechanics | `CombatTargetingGuardrailTest` | 9 | Monster gathers ask `canHarm`; one harm predicate |
| Mechanics | `PlayerBlockEditGuardrailTest` | cleanup A-6, core-loop Track 0, round two P3 | Player-caused block edits under `common/manipulation`, `network`, `event`, `item`, `block`, `tile`, `rite`, `entity` and `world` go only through `PlayerBlockEdits`; every raw edit site (`setBlock`, `setBlockAndUpdate`, `destroyBlock`, `removeBlock`) is counted per file, and each file with one is listed in `architecture/player-block-edit-allow-list.txt` as count, file and reason. A guarded call elsewhere in a file does not excuse its raw edits; wrong counts and stale rows fail |
| Bosses | `SaintCombatKitGuardrailTest` | Saint kit 2026-11-05 | Saints use the combat kit: no goals or legacy powers (the legacy pieces and their allow-list were deleted at the 2026-11-06 compile gate), no `actuallyHurt`/`die` overrides or raw damage sources, block edits only through `SaintArena`, players gathered only by the arena or `SaintMoves.struck`, held channels only through `SaintChannelRouter`, the active move cancelled only by the ending and a `Cut.IMMEDIATE` phase change, move enums in their Saint's package, `SaintChamberLayout` pinned to `pieces.py`; one Saint payload (`SaintMeterPacket`), one HUD layer (`SaintMeterLayer`), one footprint pass (`SaintTelegraphRenderer`, straight after the manipulation visuals), footprints drawn only by the telegraph kit, the Saints' own files and the Crimson Troupe ordeal's bold pass (`OrdealTelegraphPass`, claimed by hazard kind through the kit's renderer), and a meter and telegraph style for every Saint in `SaintClientStyles` |
| Mechanics | `HealAndFlightGuardrailTest` | 9 | One heal stage; flight only through `FlightArbiter` |
| Mechanics | `DamageStageGuardrailTest` | G-1, F-18 (2026-11-14) | Every `LivingDamageEvent.Pre` handler runs from `DamageStages` at its stage's priority; wound blood and vein strain run last and read `DamageStages.landing`; every `LastRiteHelper.consume` sits in a save of the death-save stage or one of the two `Post` Morphling rites; the Curved Horn no longer cancels a death |
| Mechanics | `BossDefinitionGuardrailTest` | G-2c (2026-11-14) | `HemoEntityPredicates.isBoss` is the one boss predicate and reads only `hemomancy:bosses`; no class-name or package boss check; the Dragon-and-Wither pair only in the kill blood gain exception; rite sacrifices and `puppet_attention_immune` read the tag; the Ringmaster is in it; every living type registered from `common/entity/boss/` is in the tag or on its named not-a-boss list (empty) |
| Mechanics | `HeldTravellerGuardrailTest` | Lidless Door | Every hold site asks `HeldTraveller.isHeld`; landings through `StandingSpaces`; Axonal refuses a door-crossing player |
| Mechanics | `RiteKindGuardrailTest` | 11 | Rite kinds as `RiteKind` hooks (the ordeal engine, rite state and escalation too); rites built only by `CardinalRiteActivation`, through its ordered `GATES`; waves by `CardinalRiteWave`; effects by `completion_effect`; the rite tick runs only rites |
| Mechanics | `DialogueRoutingGuardrailTest` | 11 | One `DialogueEvent` subscriber and its route table; every NPC route class is in `DialogueEventRouter.ROUTES`; no route class grows past 400 lines. The core-loop lessons (round two P10) are taken on the first line of six speakers' own classes by the package-private `CoreLoopLessonRoutes.teach`, so they added no subscriber and no route |
| Mechanics | `NpcTradeGuardrailTest` | NPC trade 7 | Trades are data run by `NpcTradeService` from its payload, never a `DialogueEvent`; no card sells a memory, kit or `trade/never_sold` item or gives what another of its speaker's cards wants; price kinds stay on their side; the Unstained refusal is checked at the board and in the transaction; only the trade classes write `NpcTradeLedger` |
| Mechanics | `HemoCommandStructureGuardrailTest` | 11 | `/hemo` one root, one class per branch (the four dev-only core-loop fixture branches of round two P11 included) |
| Mechanics | `EntityCastGuardrailTest` | 11 | Entity casts as registration hooks; they sound through the `manipulation.<id>.cast` events (UN-12) |
| Mechanics | `SculptingGuardrailTest` | core-loop Track 1 | Sculptings as registration hooks read only by `SculptingDispatcher`, which names no manipulation; each verb chain (cell hands and the Living Staff) asks the dispatcher at its pinned place after rites, Saints and Wills; the Staff reads the active verb, never the selection; no entity cast, Will or Drudge path reaches the dispatcher or a Sculpting (core-loop Track 2); each `Sculptings` factory is attached exactly once, by `set` or `add` (`.getSculptings(` is the dispatcher's too; round two P4) |
| Mechanics | `SculptingGuardrailTest.theStrandPackageNamesNoManipulation`, `CombatTargetingGuardrailTest.strandGathersAskCanHarm` | core-loop Track 12, reworded 2026-11-15 (UN-5) | Strand forms are optional kits: the lash kit is built from data and its package names no manipulation. A held manipulation is its own `VerbSculpting` class with its own mechanic, never a recolour of another. Strands gather targets through `ManipulationCombatHelper.swept` and `cone`, each filtered by `canHarm` in its own arguments, and every strand hit asks `canHarm` again |
| Mechanics | `CoreLoopTeachingGuardrailTest` | core-loop Track 11, round two P4 to P9 | Every Sculpting has its memory-tooltip line and is named on a Liber Sanguinium page; every core-loop first is an `impossible`-trigger advancement with its words, granted from the code where the act happens (27 firsts pinned, matched by whole constant name); every core-loop page is registered with a way to open it (29 pages pinned) |
| Content | `LiberPageRegistrationGuardrailTest` | round two P9 (CQ-1) | Every `*/pages/*.json` of both Libers is registered in `LiberEntryDefinitions` (literal paths, upgrade lessons, and the three families registered in a loop over their tables), since `MemoBookFilter` hides an unregistered page from everyone; every older page is in an older-page list and has a Degree floor, stage, item or advancement opener in `LiberKnowledgeHelper` or `LiberDiscoveryEvents`; the eight Manipulations school pages name only real memories, with their cast type, and name every registered memory |
| Mechanics | `TemperGuardrailTest` | core-loop Track 4, round two P11 | Only the temperament package, the sync row, the HUD and Scrying readers and the dev fixture `CoreLoopFixtures` (`/hemo temperpool`) touch the Temper pool; casts and Sculpting pulses plan their charge before the ledger prices them and settle it right after payment; absorbing from a creature fills its schools |
| Mechanics | `StationMenuGuardrailTest` | 11 | Menus never spend blood, record practice or enchant |
| Mechanics | `MenagerieKeepingGuardrailTest` | CR-26 (2026-11-14) | `MenagerieLeash` asks no class name; `hemomancy:menagerie_people` holds exactly the types under `common/entity/npc` (Court members included), `menagerie_wills` exactly those under `common/entity/mob/monster/will`, and `menagerie_unkeepable` includes both and `#hemomancy:bosses` |
| Mechanics | `HeartNodeFaneSeparationGuardrailTest` | heart-node | A heart-node and a Founding Fane never read each other's saved data |
| Mechanics | `MorphlingStrainTableGuardrailTest` | Brined Court 1 | Strain ids named only by `MorphlingStrains` and `ItemInit`; the deleted first-attempt strains leave no trace in `src/` |
| Content | `BossFrameworkGuardrailTest` | 13 | Boss bars through `BossBarBinding`; all four Saints share `SaintTrialEntity` and its one ending (loot roll, tomb restore); a sarcophagus never picks a Saint at random |
| Content | `ContentBoilerplateGuardrailTest` | 13 | `SyncedOwner`, `HemoContainerMenu`, `ProtectedNpc`, `ModelShapes.rotate`, effect categories |
| Content | `ContentFamilyGuardrailTest` | 13 | Mob families and combat-weapon storage share their base |
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
| Data | `GeneratedResourceShadowGuardrailTest` | 13 | No shadowed generated files; two datagen providers |
| Data | `RecipeSerializerGuardrailTest` | 13 | One recipe JSON bridge; station recipes off `CustomRecipe` |
| Data | `JigsawStructureGuardrailTest` | 13 | One jigsaw codec and placement call |
| Packages | `EncounterPackageGuardrailTest` | 13 | Encounter managers in `common/encounter` |
| Packages | `PackageLayoutGuardrailTest` | â€“ | Function-first top-level packages under `common/` and `client/`; no feature folders, and no folder anywhere in `src/` named for the core loop (core-loop Track 11) |
| Tooling | `BuildRunTableGuardrailTest` | 13 | Base runs only in `build.gradle`; both run tables applied |
| Tooling | `RepositoryTextEncodingTest` | 2 | Strict UTF-8 across sources, docs and tracked tools |
| Tooling | `PrivateTestHelperRatchetTest` | 2, UN-71 (2026-11-16) | Private test helper copies only fall (ceiling 101, down from 593) |
| Tooling | `LegacyMainTestGuardrailTest` | UN-71 (2026-11-16) | No `*Test.java` declares `main()`; the radial vein GPU review is the one listed entry point |
| Tooling | `SourceTreeHygieneTest` | 3, 5 | No empty sources or subscribers; mixin list matches |
| Rendering | `HemorathPresentationGuardrailTest` | 10 | Boss and attack goals emit no vanilla particles; Ferric links and blood effects use the shared world passes |
| Rendering | `PutricielPresentationGuardrailTest` | Putriciel | Bespoke rot-fire offering geometry, no vanilla fight particles, and pyres burning the Flammeus shader fire (`PyreFlames`) |
| Rendering | `VelorumPresentationGuardrailTest` | Velorum | Shared rime and shadow pipeline; no vanilla fight particles |
| Rendering | `SeraphaePresentationGuardrailTest` | 10 | No vanilla particles in the saint, fragments or anchors; dedicated radiance uses the shared Lux batch |
| Tooling | `ArchitectureStandardsIndexTest` | 13 | Every guardrail above is listed |

Behaviour and data tests that lock in consolidation work, beyond the guardrails: `LumpDonationValidationTest` (1),
`SourceAssertionsTest` (2), `ServerboundGuardsTest`, `VisualPayloadCodecTest`
and `BloodlineChatRateLimitTest` (4), `PendingDeliveryTest` (5), `HemoEasingTest` and `ProgressionGatesTest` (6),
`BloodPaymentRulesTest` (7), `SeededRenderTypesTest`, `MaterialQuadsEncodingTest`, `BossReplayClockTest`,
`PlayerAnimationRegistryTest`, `ColorParticleOptionsTest` and `VeinBackgroundStyleTest` (10),
`ActiveCardinalRiteNbtLayoutTest` and `HemoCommandTreeTest` (11), `PlayerDataMigrationsTest` (12),
`StructureTemplateCoverageTest` (13). GameTests: `NetworkDefectGameTests` and `ServerboundGuardsGameTests` (4),
`SessionStateGameTests` and `OwnerSnapshotGameTests` (5), `AttributeSyncGameTests` and `PatternOriginGameTests` (6),
`BloodAccountingGameTests` (7), `FrozenIdentifierGameTests` (8), the `combat_targeting_validation` namespace with
`HealModifierGameTests` and `FlightSourceGameTests` (9), `CombatStageCharacterisationGameTests` with its two goldens
in `combat_order_validation` and `PeriodicBroadcastGameTests` (2026-11-14), the `rite_kind_validation`, `dialogue_route_validation`,
`hemo_command_validation`, `entity_cast_validation` and `scriptorium_operation_validation` namespaces (11),
`RegistrationConsistencyGameTests` and `BlockBloodPersistenceGameTests` (13).
`verifyPackagedClientResources` and `verifyGameTestResourceLog` run in `alphaCheck`.

Behaviour and data tests that lock in the core loop (both rounds; `docs/archive/tracks/core_loop/BUILD_LOG.md` and
`BUILD_LOG_ROUND2.md`): JUnit `SculptingDispatcherTest` (claim order, the sweep join, dry walls), `LashRulesTest`,
`JetRulesTest`, `SprayPlantBrandRulesTest`, `StrandCueTest` and `StrandSkillsTest` (strands and their skills),
`CoagulationRulesTest`, `GroundRunRulesTest` (shared gutter fill, run joins), `GroundRunSavedDataTest` (a lit run's
owner), `DarkFieldRulesTest`, `CarrionRulesTest`, `FurnaceVeinsBurnTest`, `BroodRulesTest`, `KeptQuirksTest`,
`VatVigorRulesTest`, `FungalStrainsTest`, `WorldAnswerRulesTest`, `EarthenVeinTravelRulesTest`, `CoreLoopLessonsTest`
(keys, briefs, ids and voice pins of the 21 lessons), `OnlookerPayloadsTest` (the onlooker payloads and stall reasons),
`OfferedDialoguesTest` and `KnownManipulationsOldSaveTest` (an unreadable save loads without a kick). GameTests: the
`sculpting_validation`, `living_blood_validation`, `temper_validation`, `ground_network_validation`,
`earthen_vein_travel_validation`, `farming_validation`, `chum_validation`, `menagerie_validation`, `strand_validation`,
`staff_mining_validation`, `living_land_validation`, `cling_validation`, `daily_loop_validation` and
`core_loop_validation` namespaces (all in `alphaCheck`), and `OwnerSnapshotGameTests.anUnreadableManipulationSaveLoadsWithoutKickingThePlayer`.
The isolation harness every test-grid GameTest server runs under (section 11) keeps them deterministic.

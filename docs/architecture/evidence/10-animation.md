# Hemomancy Animation Audit (read-only)

Scope: player casting and other player animations, entity, block and item animation, and the conventions and helpers shared across them. Paths are relative to `src/main/java/com/vincenthuto/hemomancy/` unless prefixed. **V** = Verified by reading code or grep. **L** = Likely: strong evidence, not run. **H** = Hypothesis.

---

## 1. Casting animation lifecycle (casting animations v2)

**Size (V).** Core server code is `common/manipulation/animation/` (256 lines in total). `CastingAnimationManager` is 156 lines, and there are the enums `CastPhase`, `CastStyle` (31 bespoke styles plus `SCHOOL`), `CastPurpose` and `CastLane`. On the client, `client/player/` holds `CastingAnimationClientState` (107), `CastingDisplay` (55), `CastingPlayback` (69), `CastingTrack` (70), `CastingTrackRules` (108), `PhaseCrossfade` (38), `CastingSequenceGuard` (25), `CastingPlayerPose` (90), `CastingPose` (63) and `HandPose` (33). The tracks live in `client/player/tracks/` (about 1,500 lines). There are 8 school files × 5 ranks = 40 school tracks, resolved through `EnumMap<EnumManipulationRank,…>`. The 31 bespoke tracks are split across `StyleTracks`, `PerfectusTracks`, `GameplayTracks` and `ChannelTracks`. `StillArtTracks` is keyed by `EnumClarityStage`, and `StillArtEndings` and `AimLayers` hold the endings and aim layers.

**Server trigger (V).** These call sites start or end a cast:
- `BloodManipulation.java:552`, which calls `CastingAnimationManager.release`.
- `UseManipKeyPacket.java:96/106/150/197/224`, which calls `passive`, `release` and `finishChargeAttempt`.
- `ManipulationChannelManager.java:37/50`, which calls `channelStart` and `channelStop`.
- `CastingChargePacket.java:24`, a client→server charge heartbeat that calls `charge`.
- `StillArt.java:142`, which calls `releaseStillArt`.

The manager keeps `Map<UUID,CastingAnimationPacket> ACTIVE` and a global `sequence` (`CastingAnimationManager.java:29-30`). It does presentation only: the class comment says it never spends blood. `create()` reuses the sequence while a sustained cast of the same manipulation continues (`:100`). `publish()` uses `sendToPlayersTrackingEntityAndSelf` (`:109`).

The per-tick upkeep (`:115-137`) does the following:
- Drops terminal sessions after 20 ticks.
- Cancels a charged cast that has gone 12 ticks without an update.
- Sends a HOLD heartbeat every 20 ticks.
- Cancels when `canPresent` fails.

Late observers receive only sustained casts (`StartTracking`, `:138-143`). Logout, respawn and dimension change each publish CANCEL (`:145-154`).

**How the client learns (V).** A single S2C payload, `common/network/CastingAnimationPacket.java` (70 lines), is registered at `PacketHandler.java:88` and carries all presentation inputs. It has server game-time stamps (`started` and `updated`) and nullable enums for the Harbinger and Still Art lanes. `handle` enqueues `CastingAnimationClientState.accept` (`:62-63`). No synched entity data or attachment is used.

**Client state (V).** `CastingAnimationClientState` keeps `Map<UUID,Animation>` and a `CastingSequenceGuard`, which keeps terminal ordering for up to 1,200 ticks and 1,024 players. `levelCheck()` clears everything when the level changes (`:24-27`). `accept()` forwards the same packet to `OutpouringClientState`, which is a second consumer used for the VFX (`:33`). A retarget re-resolves the track only when `CastingTracks.presentationChanged` reports a change.

**Track selection (V).** `CastingTracks.resolve` (`tracks/CastingTracks.java:36-40`) picks the track in this order:
1. Unstained lane → `StillArtTracks.get(stage)`.
2. Bespoke style → `StyleTracks`, then `PerfectusTracks`, then `GameplayTracks`, then `ChannelTracks`.
3. Otherwise → the school/rank base track.

The aim layer is the purpose ending for school bases only. Bespoke tracks carry none, and the Still Art lane uses its ending (`CastingAnimationClientState.java:82-93`).

**Ticking and interpolation (V).** Time is always `level.getGameTime()` plus the partial tick, measured against the server stamps.
- `CastingPlayback.phase(now)` splits OPENING into HOLD using the track's opening length, and RELEASE into RECOVERY (`:104-109`).
- `phaseTime` restarts the clock for each shown phase (`:111-117`).
- Expiry happens 40 ticks after the last heartbeat for a sustained cast, or at release + recovery or cancel for a terminal one (`:118-120`).
- `CastingTrack.Segment.sample` interpolates keyframes using `Easing {LINEAR, SMOOTH, EASE_OUT, HOLD}` (`CastingTrack.java:137-156`).
- `CastingTrackRules.sample` (`:79-107`) blends strain by charge and adds a sin loop on HOLD. On RELEASE it converges from the strained pose. Passives use on/off segments.
- The weight against vanilla ramps in over 3 ticks and fades over the last 4 (`:61-77`).
- `PhaseCrossfade` (3-tick smooth crossfade with stale detection) wraps the third-person pose, the first-person pose and the weight (`CastingDisplay.java:15-17`).

**End and cancel (V).** CANCEL plays the authored `cancel` segment and fades. The client `tick()` removes an entry when it expires, when its entity has been seen and then disappears, or when the entity is dead or its UUID does not match (`CastingAnimationClientState.java:40-52`). `EntityLeaveLevelEvent` untracks it (`client/event/CastingAnimationClientEvents.java:278-280`).

**Layering onto the vanilla model (V).** There is no `RenderPlayerEvent` pose override. The single entry point is `mixin/core/MixinHumanoidModel.java:23-34`, a `@Inject(TAIL)` on `HumanoidModel.setupAnim`. It calls `CardinalRiteStaffPlantingClientState.applyThirdPersonPose`, then `PlayerAnimationClientState.applyThirdPersonPose` (which first runs `CastingPlayerPose.apply`, `PlayerAnimationClientState.java:82`), then `WarpChairPlayerPose.applyModelPose`.

`CastingPlayerPose.apply` (`:148-194`) works as follows:
- It mirrors the pose for left-handed players and blends the arms per `armAvailable`.
- It pivots the torso about the waist through `WaistPivotRules`. That writes `body.z` and `head.z`, which is why they are reset at `PlayerAnimationClientState.java:78-81`.
- It gates the legs by a stance weight.
- It copies the sleeves, jacket and pants. This copy is redundant because vanilla `PlayerModel.setupAnim` copies them after `super`, but it does no harm (L).

Exclusivity is enforced by `CastingPlayerPose.exclusive` (`:127-132`) together with the early-return priority chain in `PlayerAnimationClientState.java:83-142`. `PlayerAnimationPriorityTest` exists for that chain.

`CastingAnimationLayer` (53 lines) records hand and outpouring anchors. For the Unstained lane it also draws the Still Art rings (`:230-249`).

**First person (V).** `CastingAnimationClientEvents.hand` runs at `RenderHandEvent` LOWEST (`:282-336`) and does the following:
- Cancels the vanilla hand render.
- Applies the clamped `HandPose` (`FirstPersonHandRules`).
- With an empty hand, re-renders it using copied vanilla `renderPlayerArm` constants (`.64000005`, `-.71999997` …).
- With an item, renders through the invoker mixin `CastingHandRendererInvoker` → `ItemInHandRenderer.renderArmWithItem`.

`CastingPlayerPose.renderFirstPerson` sets a flag so the `setupAnim` mixin skips third-person posing while `renderHand` runs.

**Other player animations (V).** Each has its own packet and its own client-state class with private level-change and expiry logic:

| Animation | Server | Packet | Client state |
|---|---|---|---|
| Blood vial injection | `common/item/harbinger/BloodVialInjectionAnimation.java` (79) | `PacketBloodVialInjection` | `BloodVialInjectionClientState` (132), `…Playback` (26), `…Pose` (135) |
| Hematic microscope | – | `PacketHematicMicroscopeViewing` | `HematicMicroscopeClientState` (130), `…Playback`, `…Pose` |
| Living torch breath | – | `PacketSyncPlayerAnimation` + `PlayerAnimationKind` (1 value) | `PlayerAnimationClientState` |
| Cardinal staff planting | – | `PacketCardinalRiteStaffPlanting` | `client/rite/CardinalRiteStaffPlantingClientState` (96) |
| Living staff morph | – | `PacketLivingStaffMorph` | `LivingStaffMorphClientState` (109) |
| Flail impact | – | `LivingFlailImpactPacket` | `LivingFlailImpactClientState` |
| Weapon swings | – | none (vanilla `attackAnim`) | `LivingWeaponPlayerPose` (304), `UnstainedWeaponPlayerPose` (213), `SwingCurve`, `SwingComboTracker` |
| Warp chair | – | none (vehicle) | `WarpChairPlayerPose` (100) |

Side finding: `BloodVialInjectionClientState.impact` bakes a brand-new `PlayerModel` from a `LayerDefinition` on every confirmed injection (`:84-86`). That is allocation-heavy but rare (V, Low).

---

## 2. Entity animation inventory

There are 115 entity model classes with `setupAnim` under `client/model/entity` and 142 `registerEntityRenderer` calls. **GeckoLib** is declared at `build.gradle:439`, but its only users are 3 files in `compat/mna/**`, which is excluded from compilation (`build.gradle:78`). The core mod therefore makes no GeckoLib use (V). **HutosLib** offers no animation or easing helper (V; its `math/` has none). Its only contributions here are `HLClientUtils.getPartialTicks()` and `TendrilGeometry` (world tendrils).

**Counts by approach (V)**

| Approach | Model classes | Entities |
|---|---|---|
| (a) Hand-rolled `setupAnim` math, sometimes with a shared helper (`SuccessionModelPose` ×5, `CircusDownedPose` ×5, `*Rules` imports ×15) | ~105 | majority of mobs, NPCs, summons, saints |
| (b) Vanilla `AnimationState` + `AnimationDefinition` via `this.animate`/`KeyframeAnimations` | 10 | BarbedUrchin (3 size models), PhlegethonticBombardier, AbhorentThought, ErythromyceliumEruptus, Fargone, PaleIntercession; plus Vesper Evening Star (4 clips) and the Crowned rider (1 clip) as hybrids |
| (b') Local `client/model/AnimationHelper` (105 lines; non-`HierarchicalModel` port of `KeyframeAnimations`) + `IAnimatedModel` | 5 | BER/item only: SanguisLancea, AnnettasSanguisLancea, EarthenVein, FloatingEye, FungalImplantationPylon |
| (c) GeckoLib | 0 live | (MnA compat only, excluded) |
| (d) HutosLib helpers | 0 | – |
| (e) Scripted controller (synced action id + clock, hand-keyed poses) | 9 | Annetta, Latent Annetta, Stained Priestess, Mycophant, Vesper ES, Vesper CR (mount + rider), Naeglerophaeon, 4 saints (state + countdown) |
| (f) Procedural chains | ~9 | see below |

**Scripted and boss table (V)**

| Entity | Model (lines) | Approach | State sync | Trigger and client time source |
|---|---|---|---|---|
| Vesper Evening Star | `VesperTheEveningStarModel` (558) + `VesperEveningStarMoves` (779, Catmull-Rom float[] poses) + `VesperEveningStarAnimState` (303) + `VesperTheEveningStarAnimations` (3,003) | Procedural moves; keyframes only for IDLE, DEFEAT, HIT and REMOVE_HOOD via direct `KeyframeAnimations.animate` (`Model:516-518`) | `DATA_WEAPON_ACTION`, `DATA_ACTION_TICK`, `DATA_ACTION_VARIANT`, `DATA_STANCE_TICK` … (`VesperTheEveningStarEntity.java:45-69`) | `VesperAnimClock` replay (tolerance 2) and per-entity `WeakHashMap` motion memory with pose-inertia blend |
| Vesper Crowned Refusal | `VesperMountModel` (1,284) + `VesperMountAnimState` (359); rider `VesperTheCrownedRefusalModel` (417) via `VesperRiderLayer` | Procedural; `AnimationState` only for TRANSFORMATION (`RefusalModel:322-325`) | `DATA_ATTACK`, `DATA_ATTACK_TICK`, flight/burrow/stagger/transition ticks | 5 `VesperAnimClock`s; the rider reads the mount's state. This depends on render order, as the comment at `RefusalModel:328` states |
| Annetta Knowles | `AnnettaKnowlesModel` (939) | Hand-keyed state machine (`switch(EncounterState)`) | `DATA_STATE`, `DATA_ACTION` (int constants), `DATA_ACTION_TICK`, `DATA_SCENE_TICK` | Entity-side replay `tickClientClocks` (`AnnettaKnowlesEntity.java:186-209`, tolerance 2) |
| Stained Priestess | `StainedPriestessModel` (673) + tendril wings (`PriestessTendrilRules` 132, `TendrilTube` 274) | Hand-keyed | `DATA_ACTION`, `DATA_ACTION_TICK`, `DATA_MODE_TICK` | **Raw** `getActionTick()+partial` (`Model:217-221`) |
| Latent Annetta | `LatentAnnettaInfectionModel` (999) | Hand-keyed | `DATA_ACTION`, `DATA_ACTION_TICK`, `DATA_EMERGE_TICK` | **Raw** `getActionTick()+partial` (`Model:818-823`) |
| Mycophant | `MycophantModel` (2,857; 14 pose tables, string review poses) | Hand-keyed + procedural tentacles | `DATA_STATE`/`DATA_ATTACK` (enum ordinals), 5 tick counters | **Raw** counter+partial (`Model:2326-2376`); `tickStateClock` sets data every tick (`MycophantEntity.java:159`) |
| Naeglerophaeon | `NaeglerophaeonModel` (200) + renderer `State` `WeakHashMap` | Procedural curve | `PHASE` + `PHASE_START` (gameTime, set once) | `tick+partial-animationStart()` (`NaeglerophaeonRenderer.java:89`) |
| Hemorath, Putriciel, Velorum, Seraphae | 83–288 each | Hand-rolled | `DATA_VISUAL_STATE` + countdown `DATA_VISUAL_TICKS` (decremented each tick) | Raw |
| Eruptus, AbhorentThought, Fargone, Bombardier, PaleIntercession, BarbedUrchin | 168–1,613 | Vanilla keyframes | Synced booleans/state → client `startIfStopped` in `tick()` | `AnimationState` (Bombardier: own 6-tick crossfade, `BombardierAnimationBlend`) |
| Drudge | `DrudgeModel` (121) | Hand-rolled biped | none for animation (`DrudgeAction` is AI) | limbSwing |
| Harbinger NPCs ×8, Unstained ×4 | 185–322 | Hand-rolled. Near-identical copy-pasted biped walk + cloth flutter (V: Voyager vs Artificer differ only in type and one line) | none | `entity.tickCount + HLClientUtils.getPartialTicks()` |

**(f) Procedural chains: three styles, none shared (V)**
1. **Open-loop phase-offset sine per segment on ModelPart rotations:**
   - Mycophant face and pulp tentacles (`MycophantModel.java:2642-2690`).
   - `ScarletSerpentSlitherRules` (34) and `VenomRibCentipedeSlitherRules` (67): the same `sin(wave − i·PHASE_STEP)·amp·taper` with different constants.
   - PrismCuttle (`PrismCuttleRules`) and Hemojelly.
   - The Latent Annetta whip arm (segments chained by `translateToWhipTip`).
2. **Curvature-wave integration into a tube mesh:** Priestess wings (`PriestessTendrilRules`, 18 segments, 6-tick pose blend).
3. **Endpoint-aligned segments or curves:** `ExcoriatedModel.stringSegment` (`:407`) and `NaeglerophaeonModel.captureCurve` (`:120`).

World tendrils use HutosLib `TendrilGeometry`. No IK or verlet solver exists.

---

## 3. Competing conventions and recommendations

**Boss trigger and sync conventions: 6 distinct (V)**
1. **Synced action id + per-tick synced counter, read raw (+partial):** Mycophant, Priestess, Latent, saints (countdown) and Bombardier (`incrementStateTick`, `:263`). This marks entity data dirty every tick, which sends one `SetEntityData` per tick to every tracker (L). The Vesper clock's own doc comment says reading this raw "stalls and skips" (`VesperAnimClock.java:3-6`).
2. **The same data, replayed on a client clock with tolerance-2 rebase:**
   - `VesperAnimClock` (35 lines, unit-tested by `VesperAnimClockTest`), used by Vesper ES and the mount.
   - An inline copy of the same algorithm in `AnnettaKnowlesEntity.tickClientClocks` (`:197-209`).

   The Annetta encounter therefore mixes conventions 1 and 2: Annetta replays, while the Priestess and Latent Annetta read raw.
3. **Start-stamp synced once:** `NaeglerophaeonEntity` `PHASE_START` (long gameTime, `:52,206`). The player casting packet's `started`/`updated` works the same way.
4. **Vanilla `AnimationState.startIfStopped(tickCount)` on the client, driven by synced flags:** Eruptus (`ErythromyceliumEruptusEntity.java:214-238`), BarbedUrchin, ChalybeateSnail, Crowned TRANSFORMATION and PaleIntercession.
5. **`broadcastEntityEvent`/`handleEntityEvent`:** used for animation only by `LuminalCicadaEntity.java:124/135` (an 8-tick flash timer). The other 18 call sites are particles or vanilla hearts.
6. **Custom payloads:** players only (section 1); no entity uses them.

Action ids are also encoded three ways: int constants (Annetta ×12, Priestess ×5, LanternTick ×4), enum ordinal (Mycophant, Vesper) and enum with explicit id (`EncounterState.byId`).

**Recommended canonical form for scripted bosses.** Use a synced enum action id with a synced action tick, sampled through **one shared public `ReplayedClock`**, promoted from `VesperAnimClock`. The best existing example is `VesperAnimClock` + `VesperEveningStarAnimState`, which has a tested clock, per-entity motion memory, and signature-change pose inertia.
- Migrate the Priestess, Latent Annetta and Mycophant models, and replace Annetta's inline copy.
- Difficulty: **Medium**.
- Timing and visual risk: medium-low. Server contact ticks stay authoritative, but a rebase can shift the displayed clip by at most 2 ticks against the old raw reading. Raw readers will also look smoother (no stalls), which is a visible change to review in the boss review clients.
- Do **not** switch counters to start-stamps (convention 3) as part of this. Client gameTime jumps on `ClientboundSetTimePacket`, and entity `tickCount` differs per side, so that is **High** risk. Treat it as a separate network optimisation.

**Recommended canonical form for simple mobs.** Use convention 4, with `ErythromyceliumEruptusEntity` plus `ErythromyceliumEruptusModel:273-277` as the example. For crossfades, `BombardierAnimationBlend` is the model to follow. Difficulty is **Low**, and nothing needs migrating.

**Player animation.** The casting stack is the reference design: pure, sequence-guarded, tested, and phase-crossfaded. The other six player animations each repeat level-change clearing (`BloodVialInjectionClientState:31`, `HematicMicroscopeClientState:28`, `LivingStaffMorphClientState:25/38`, `CardinalRiteStaffPlantingClientState:27/44`, `PlayerAnimationClientState:34/59`), entity validity and expiry, and they key by entity id while casting keys by UUID. `PlayerAnimationKind`/`PacketSyncPlayerAnimation` was meant to be the "reusable channel" but carries one kind.

Recommendation: a shared client presentation registry (level reset, expiry, entity check), keeping each pose class and the **exact priority order** in `PlayerAnimationClientState.applyThirdPersonPose`. Difficulty **Medium**. Risk is low if `PlayerAnimationPriorityTest` and the playback tests stay green.

**Vesper pose blend divergence (V code, H effect).** `VesperEveningStarAnimState.blend` wraps angles (`angleLerp`), while the near-identical `VesperMountAnimState.blend` uses plain `Mth.lerp`. A mount part whose rotation crosses ±π could blend the long way round. Unifying the two needs visual review.

**Fargone (V).** The model plays WALK on `idleAnimationState` (`FargoneModel.java:169`), so the entity's `walkAnimationState` is never read. Switching it would change the walk phase, so leave it unless a visual change is wanted.

---

## 4. Duplicated easing and helper functions (V)

There is no shared easing utility in Hemomancy or HutosLib. The closest things are `common/rite/CardinalRiteMath` (`clamp01` :15/19, `smoothstep` :24/30, `lerp` :35, `easeOutCubic` :39, `pulse` :70; 24 call sites) and the enum `CastingTrackRules.ease` (`:11`).

**Definition counts by name:** `smooth`/`smoothstep`/`smoothStep`/`smootherstep` **60**, `lerp` 19, `clamp01` 13, `pulse` 5, `easeInOut` 4, `easeOutCubic` 4, `ease` 4, `easeIn` 3, `easeOut` 2. About 57 of the smoothstep bodies are the identical cubic `t*t*(3-2t)` in float/double variants.

**Smoothstep definitions (file:line):**
- Client:
  - `client/model/entity/boss/endgame/`: NaeglerophaeonBlockModel:170, NaeglerophaeonModel:199, VesperEveningStarPose:62, VesperMountMotionRules:117, VesperWeaponAnimationRules:191/196.
  - `client/model/entity/summon/CinderBellowsModel:152`.
  - `client/player/`: BloodVialInjectionPose:129, HematicMicroscopePose:87, LivingTorchPlayerPose:23.
  - `client/render/`: entity/misc/ArborFruitMeshes:495, item/MonolithFragmentItemRenderer:187, item/MonolithImbuedClothItemRenderer:120, layer/player/AvatarManifestationTransition:97, outpouring/OutpouringRules:117, tile/.../SomaticLoomEcho:240, SomaticLoomEffectRules:206, puzzle/SaintSarcophagusRenderer:84.
  - `client/render/world/`: BloodShotGeometry:221, CardinalRitePlantedStaffGeometry:61, chamberofwill/ApotheosChamberEffects:1068, floor/FloorMesh:584, HematicRiposteGeometry:240, ManipulationLifecycle:52, ManipulationUndulation:79, rite/CardinalRiteVisualRules:71, rite/escalation/RiteWorldMath:38, rite/SwallowedFragmentRules:65, VeinSpiderCourierRenderer:131, WorkingCraftSwallowRules:121, WorldRiteFinaleRules:101.
  - `client/screen/`: overlay/SanguineOmenOverlayState:64, skilltree/shared/SkillTreeLayerRules:55.
- Common:
  - `entity/`: boss/annetta/AnnettaEncounterRules:111, boss/endgame/VesperCombatRules:106, VesperEveningStarPresentationRules:63, VesperPhaseTransitionRules:108, npc/harbinger/HarbingerHermitEntity:336, utility/DaemonFlight:136.
  - `manipulation/`: animus/AvatarManifestRules:94, BloodRushRules:48, congeatio/CruorJavelinRules:37, ferric/CellHandRules:88, LodestoneCollapseRules:53, LodestoneFieldRules:83, SanguineMendingRules:39, WeaponConjureRules:161, flammeus/SearingLashRules:50, impact/CastImpactRules:66, mortem/FruitingContagionRules:53, tenebris/UmbralRendRules:58.
  - `rite/`: floor/CardinalRiteFloorVisualRules:369, harbinger/CardinalRiteCancellationGeometry:50, CardinalRiteOrdealEngine:350, CardinalRitePlantingSequence:49.
  - Other: summon/PuppetStringsRules:106, vein/EarthenVeinTravelManager:469, worldgen/pelagic/PelagicTerrainSampler:206.
- Inline, not methods: `BombardierAnimationBlend:11-12`, `AnnettaKnowlesModel` `ramp`/`window`.

**Other helpers:**
- `lerp`: PriestessTendrilRules:129, MonolithFragmentItemRenderer:191, GourdVineRenderer:276, CardinalRiteAnchorJetRenderer:110, CardinalRiteVisualRules:279, RadialVeinBorderGeometry:99, plus 13 in common. Most of these re-implement `Mth.lerp`.
- `easeOutCubic`: ClawSlashRenderer:177, CardinalRiteVisualRules:81, SanguineMonolithShatterRenderer:213.
- `easeInOut`: HematicRiposteGeometry:245, CardinalRiteVisualRules:76, UmbralRendRules:63, WorkingCraftDaemonPath:89.
- `clamp01`: 13 sites, including OutpouringMarks:178, SomaticLoomEffectRules:202, RiteWorldMath:34, OfferingDissolveRules:41 and two skill-tree caches.

**Crossfade and transition helpers duplicated:** `PhaseCrossfade` (3 ticks), `BombardierAnimationBlend` (6), Vesper ES (6, tau 0.4/1.7), Vesper mount (9, tau 0.45/3.2), and Priestess `BLEND_TICKS` (6).

**Recommendation.** Add a `common/util/Easing` class, since about 25 copies live in common rule classes used on both sides. Seed it from `CardinalRiteMath`, then migrate file by file. Difficulty **Low**, but read the caution below.

**CAUTION (V).** In 1.21.1, `Mth.smoothstep(double)` is the **quintic** `6t^5-15t^4+10t^3` (`net/minecraft/util/Mth.java:682-684`), not cubic. Replacing the private cubic copies with `Mth.smoothstep` would change every curve. Preserve the cubic as the default, and keep the separate `smootherstep` (VesperWeaponAnimationRules) distinct.

---

## 5. Block, BER and item animation (V)

- Of 76 tile renderers, 46 use `getGameTime()` (+partial). Only `MorphlingCradleRenderer:35` uses `Util.getMillis`.
- Item renderers mostly use **wall-clock** `System.currentTimeMillis`, which keeps animating while the game is paused: MemoryOfVesper:32, MonolithFragment:48, MonolithicCornerstone:33, MonolithImbuedCloth:35, QliphothPome:63, QliphothSeed:41, ScarPattern:82/112, CovenantThrone:87, HematicStake:31, SanguineMonolith:84, and `MorphicNectarItemDecorator:19`. These are probably deliberate for GUI rendering without a level, but they are inconsistent with the matching BERs.
- `System.nanoTime` in `FirstPersonPalm` and `HeldWeaponPose` is staleness bookkeeping, not animation.
- **Partial tick has 4 sources:**
  - `getGameTimeDeltaPartialTick(false)`, 24 sites.
  - `getGameTimeDeltaPartialTick(true)`, 21 sites (respects tick freeze).
  - `HLClientUtils.getPartialTicks()`, 19 sites.
  - `ageInTicks - entity.tickCount`, 11 sites.

  Mixing the `true` and `false` variants means some parts freeze under `/tick freeze` and others do not (L).
- `FloorMesh` takes its time from the caller (blend at `:406-408`). `CardinalRiteBoundaryRenderer:169` and `QliphothBloomRenderer:117` use `gameTime + partialTick`. `QliphothBloomGeometry` has its own geometric `animate(path, …)` (`:43/72/96`), which is unrelated to model animation and shares the name.
- BER/item `AnimationHelper` users hold **one `AnimationState` per renderer**, started at 0 (`EarthenVeinRenderer:31,35`, `SanguisLanceaItemRenderer:30,35`) and fed `gameTime+partial`. Every instance therefore animates in lockstep on absolute time. The state object adds nothing over `gameTime*50ms` (V, Low).
- `SanguisLanceaItemRenderer` and `AnnettasSanguisLanceaItemRenderer` are 150-line near-clones (22 differing lines). Their models are 182 and 146 lines.

---

## 6. Animation sync mechanisms (V)

1. **Player S2C payloads:** `CastingAnimationPacket`, `PacketBloodVialInjection`, `PacketHematicMicroscopeViewing`, `PacketSyncPlayerAnimation`, `PacketCardinalRiteStaffPlanting`, `PacketLivingStaffMorph`, `LivingFlailImpactPacket`, `PacketCurvedHornAnimation`. **Redundant:** the generic `PacketSyncPlayerAnimation` coexists with six bespoke equivalents.
2. **Per-tick synced counters** (bosses, Bombardier, `HarbingerHermitEntity:154`, `PelagicAnimal`): redundant bandwidth wherever the client could replay.
3. **Synced start stamp:** Naeglerophaeon only.
4. **Synced flags driving client `AnimationState`.**
5. **Entity events:** LuminalCicada flash only.
6. **Client-only derivation** (limbSwing, `isMovingOnLand`).
7. **Client per-entity `WeakHashMap` motion memory:** Vesper ES, Vesper mount, Naeglerophaeon renderer.

**Likely defect (L).** `PacketCurvedHornAnimation` is registered `playBidirectional` (`PacketHandler.java:175`), but its handler calls the client-only `LayerEvents::playHornAnimation`. `LayerEvents` is `@EventBusSubscriber(value=Dist.CLIENT)` and uses `Minecraft`/`LocalPlayer`. A client→server send would execute client code on a dedicated server. It should be `playToClient`. Only `sendToPlayer` sends it today (`HarbingerEquipmentEntityEventHandler:84`).

---

## 7. Dead animation code

| Item | Evidence | Confidence |
|---|---|---|
| 54 of 58 `AnimationDefinition`s in `VesperTheEveningStarAnimations.java`: WALK, all IDLE_/STANCE_ ×16, RAGE_IDLE and every attack clip (~2,750 of 3,003 lines; the used IDLE/DEFEAT/HIT/REMOVE_HOOD functions total about 243 lines) | 0 references repo-wide. Only `HIT` is pinned by `VesperCombatImplementationSourceTest:137-146`. Superseded by `VesperEveningStarMoves` | **Verified** |
| `VesperEveningStarMotion` (36) and `VesperWeaponAnimationRules` (200) | Referenced only by their unit tests | **Verified** (runtime-dead) |
| 39 `AnimationState` fields started (or declared) but never read by any client model, across 31 classes: Hemorath ×2, Putriciel ×2, Seraphae ×3, SeraphaeFragment, Velorum ×2, CrimsonDoe ×2, VenousStrider ×2, VerdigrisMoth ×2, BloodLanternJelly, BrinedVotary ×2, ChalybeateSnail ×2, Hemojelly, Fargone `walk`, Hemolymphopoda, Bombardier `presentation`, LanternTick `idle`, 8 Harbinger NPC idles, 4 Unstained NPC idles, and `EarthenVeinBlockEntity.idleAnimationState` (never started) | grep of each field name in `client/` matches only Urchin/Abhorent/Eruptus/Fargone/SpecimenJar. No tests reference them | **Verified** |
| `AbyssalSiphon`, `CruorFiend`, `FrozenClot`, `SynapseHound`, `VoidDrinker`: entity + model + renderer triads | No `EntityInit` entry, renderer never registered, model layer not registered | **Verified** (unreachable) |
| `BloodBulletModel` | Layer registered (`LayerEvents:96`); `BloodBulletRenderer` does not use it | **Verified** |
| `AnimationCycleManager` (177) | Single user, `AbhorentThoughtEntity` | Live; "generic" API is over-built (**Likely**) |
| `PlayerAnimationKind` channel generality | One enum value | Live; abstraction unused (**Verified**) |

Removing these does not change any visual, because none of them is read at render time. The one exception is the triads: confirm that design docs do not plan to reactivate them.

---

## 8. Hotspot files

| File | Lines | Why |
|---|---|---|
| `client/model/entity/boss/endgame/VesperTheEveningStarAnimations.java` | 3,003 | about 92% dead |
| `client/model/entity/boss/endgame/MycophantModel.java` | 2,857 | geometry + 14 pose tables + string review poses + procedural tentacles + raw clock |
| `client/model/entity/mob/monster/ErythromyceliumEruptusModel.java` | 1,613 | BlockBench export |
| `client/model/entity/mob/monster/AbhorentThoughtModel.java` | 1,259 | BlockBench export |
| `client/model/entity/boss/endgame/VesperMountModel.java` | 1,284 | with `VesperMountAnimState` 359 |
| `client/model/entity/boss/endgame/VesperEveningStarMoves.java` | 779 | procedural moves |
| Annetta trio: `AnnettaKnowlesModel` / `LatentAnnettaInfectionModel` / `StainedPriestessModel` | 939 / 999 / 673 | mixed clock conventions within one encounter |
| `common/entity/boss/annetta/AnnettaKnowlesEntity.java`, `VesperTheCrownedRefusalEntity.java`, `NaeglerophaeonEntity.java` | 1,244 / 1,281 / 1,126 | large boss entities |
| `client/player/PlayerAnimationClientState.java` | 168 | priority chain for every third-person player pose |
| `mixin/core/MixinHumanoidModel.java` | 35 | sole injection point, runs for every humanoid `setupAnim` |
| `client/player/LivingWeaponPlayerPose.java` | 304 | weapon swing poses |
| `tools/model_export/*_anim.java` | – | annetta_knowles, latent_annetta_infection, stained_priestess, mycophant, vesper_mount; source-of-truth animation snippets spliced into models, which must stay in sync with hand edits |

**Verified vs hypothesis summary.**
- Verified: the casting lifecycle and file:line citations, the approach counts, the six boss conventions, the duplicate-helper counts, the `Mth.smoothstep` semantics, and every dead-code row marked Verified.
- Likely: per-tick entity-data bandwidth, the bidirectional horn-packet hazard, and partial-tick freeze inconsistency.
- Hypothesis: the Vesper mount long-way angle blend.

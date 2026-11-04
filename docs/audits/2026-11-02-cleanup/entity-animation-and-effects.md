# Entity animation and effects audit

Date: 2026-11-02. Scope: every registered entity, its renderer, model and animation states; clocks; particles and
world effects; sounds; manipulation visuals; boss presentation; render-type and per-frame hazards. Method: source
reads and scripted sweeps over `src/main/java`, `src/main/resources` and `tools/model_export/`. Nothing was built, run or
launched, so every "Verified" below means "verified in source", not "seen in game". Items already in
`docs/OPEN_QUESTIONS.md` are cited by id, not re-reported.

## Summary

`EntityInit` registers **182 entity types** (164 named holders plus 18 Brined Court roles from `CourtRole`). All 182
have a renderer (178 in `client/registry/EntityRendererRegistrar`, 4 in `client/event/PhlegethonticClientEvents`). One
mod creature still renders as a vanilla zombie with the vanilla zombie texture and is spawned by a D8 Saint trial; the
Circus finale boss is locked in a fixed perch pose for its whole fight; eight live mobs have an empty `setupAnim` and do
not move a limb. The big bosses (Annetta's three forms, Vesper's two, the Mycophant, the four Saints, the Court
minibosses) are in good shape: every Mycophant move and Priestess action has a pose, 28 of 30 generator `_anim.java`
fragments match the committed models line for line, and the Saint telegraph timing is shared between the server hit
and the client pose. The two drifted fragments would reintroduce a banned clock if regenerated.

Counts: **41 findings** (2 High, 14 Medium, 25 Low). **198 vanilla particle references in 86 files** (about 35 in 22
files are reasonable fallbacks such as block debris and item crumbs; the rest sit in focal code). **188 sound events**,
all with `sounds.json` entries and subtitles; 3 point at OGG files that do not exist and **57 OGG files (35 MB) are
referenced by nothing**. **123 manipulations**: 15 are retired (`ManipulationRetirementRules`); of the other 108, 37
have their own layered geometry at the Riposte bar (most still "pending in-game review" in the archived catalogue),
68 are adequate on a school tier, lightning or tendrils, and 3 are below it (two legacy `VisceralMesh` paths, one with
live vanilla dust, and one cast with no formation visual at all). Boss presentation covers 14 bar frames and 26 scenes;
the Ringmaster finale has none.

## Entity table

Model source key: **gen** = `tools/model_export/<file>.py` (tracked); **gen-local** = `tools/circus/<file>.py`
(gitignored, see AN-9); **BB** = Blockbench export; **hand** = hand-written Java; **vanilla** = vanilla model class;
**geom** = renderer draws its own geometry, no `EntityModel`. "Shallow" = renderer, model and drivers read, no timing
check.

| Entity id | Renderer | Model source | Animation states (driver) | Verdict |
|---|---|---|---|---|
| annetta_knowles | AnnettaKnowlesRenderer | gen `annetta_knowles.py` + `_anim.java` | cower, phase 1 actions, break, purged heal (synced action + `actionClock` BossReplayClock) | OK |
| stained_priestess | StainedPriestessRenderer (+ tendril layer) | gen `stained_priestess.py` + `_anim.java` | EMERGING/TAKEOFF/HOVER/SWOOP/LANDED/SHIELD/WIND_BLAST/PLUMMET, VOLLEY/LANCE/COMBO/PLANTED, death slack (synced mode/action ticks via `modeTime`/`actionTime`) | OK; generator drift AN-8 |
| latent_annetta_infection | LatentAnnettaInfectionRenderer | gen `latent_annetta_infection.py` + `_anim.java` | emerge, claw rake, spear, whip, bloom, eruption, death (synced attack/emerge) | OK; generator drift AN-8 |
| annetta_pool / infected_bolt / stained_lance / priestess_eruption | own renderers | geom (+ `AnnettasSanguisLanceaModel`) | lance flight, planted quiver and heat (`plantedAge`) | OK |
| vesper_crowned_refusal | VesperTheCrownedRefusalRenderer | gen `vesper_mount.py` + `_anim.java` (mount); BB rider | ground moves, flight, burrow stages, wounds/carapace aneurysm, transformation (`VesperMountAnimState`, `BossMoveTimeline`, keyframed transformation started in `onSyncedDataUpdated`) | OK |
| vesper_evening_star | VesperTheEveningStarRenderer | BB + procedural `VesperEveningStarMoves` | stances per tendency, weapon actions, hood removal, rage, awaiting absorption | OK |
| vesper_scute_projectile | VesperScuteProjectileRenderer | geom | flight | OK (shallow) |
| mycophant | MycophantRenderer | gen `mycophant.py` + `_anim.java` | all 22 `MycophantAttack` moves, split, holds, rematch intro (`MycophantAnimState`, `BossMoveTimeline`) | OK |
| mycophant_hazard / _glob / _lance | own renderers | geom | flight trails | OK |
| naeglerophaeon | NaeglerophaeonRenderer | geom (`NaeglerophaeonModel` pose solver) | hunt, capture, grab, discharge, recovery, enraged tint (synced phase start game time) | OK; clock note AN-25 |
| hemorath | HemorathRenderer | gen `hemorath.py` + `_anim.java` | visual states, collapse charge, phase 2, swing, siphon, secondary patterns | Gaps: vanilla death flop AN-6 |
| seraphae | SeraphaeRenderer | gen `seraphae.py` + `_anim.java` | Seraphae states (fracturing/condensing via synced state), secondary patterns | Gaps: ending pops AN-6 |
| seraphae_fragment / containment_anchor | own renderers | geom / hand `ContainmentAnchorModel` | idle, active | OK (shallow) |
| frayed_thread / thread_bolt / thread_hazard | weave renderers | geom | thread, bolt, ground hazard (custom embers) | OK (shallow) |
| putriciel | PutricielRenderer | gen `putriciel.py` + `_anim.java` | visual states, absolution windows, nova, release, swing, secondaries | Gaps: ending pops AN-6 |
| velorum | VelorumRenderer | gen `velorum.py` + `_anim.java` | phases, martyrdom (synced ticks), swing, bespoke death (`getFlipDegrees` 0) | OK |
| bloom_warden | CourtMemberRenderer | gen `court_bloom_warden.py` + `_anim.java` | walk, moves (`BossMoveTimeline`, BossReplayClock) | OK; silent AN-13 |
| kindled_champion | CourtMemberRenderer | gen `court_kindled_champion.py` + `_anim.java` | neck sweep, sting, grab, crown buds | OK; silent AN-13 |
| court_* (18 roles) | CourtMemberRenderer per `CourtMemberModels` row | gen `court_<role>.py` + `_anim.java` on `court_humanoid.py` rig | walk, swim stroke, anchored kneel/withdraw, sleeper, speak, wary close-up, role idles; strike pose for defenders (Tidewarden, Edge Swimmer) | OK |
| hydra_rising_head | HydraRisingHeadRenderer | geom | rise (BossReplayClock), crown motes | OK |
| phlegethosoma | PhlegethosomaRenderer | gen `phlegethosoma.py` | ichor swim, breach attack, swing, hurt, death roll | OK (shallow) |
| coagulum_projectile | ThrownItemRenderer | item sprite (`sanguine_formation`) | none | Placeholder-ish AN-36 |
| temporary_coagulum | CoagulumRenderer | geom | static clot | OK (shallow) |
| circus_ringmaster | CircusRingmasterRenderer | hand `CircusRingmasterModel` (HumanoidModel) | one fixed perch pose, staff pulse | **Broken** AN-1 |
| circus_fire_eater / stilt_walker / acrobat / knife_thrower | CircusPerformerRenderer | hand (HumanoidModel) | walk, act states (`getActState`), spin, juggle, downed | Gaps: vanilla rig, vanilla effects AN-16 |
| circus_strongman / beast_tamer / threadkeeper / understudy | CircusPerformerRenderer | gen-local `tools/circus/build_faculty.py` (HumanoidModel) | walk, setup/perform, downed | Gaps: untracked generator AN-9 |
| circus_carousel | CircusCarouselRenderer | hand | turntable spin, severed riders, broken anchors (`prepare`) | OK |
| circus_knife | CircusKnifeProjectileRenderer | geom | spin | OK (shallow) |
| ringmaster_pattern / veinwing_vulture / marrow_spitter / gorebound_hulk / mnemonist_puppet / scarlet_mummer / sanguine_hound / cinder_bellows | own renderers | gen-local `tools/circus/build_*.py` | walk/fly; Hulk windup; Mummer performing; Bellows breath; Spitter juggling loop | Gaps: AN-9, AN-23, AN-24, AN-31 |
| vampire_bat | VampireBatRenderer | gen `vampire_bat.py` (Mesh) + hand animator | cruise flight, hunting flight, hang (`isResting`, synced HUNTING) | Gaps: no bite beat AN-32 |
| crimson_doe | CrimsonDoeRenderer | gen `crimson_doe.py` (Mesh) + hand animator | walk, run, idle ear/tail flicks (procedural) | OK; dead AnimationStates AN-21 |
| venous_strider | VenousStriderRenderer | gen `venous_strider.py` (Mesh) + hand animator | walk, idle (procedural) | OK; dead AnimationStates AN-21 |
| fungling | FunglingRenderer | gen `fungling.py` (Mesh) + hand animator | walk, bite (attackTime), hurt, grown scale (SCALE attribute) | OK |
| erythromycelium_eruptus | ErythromyceliumEruptusRenderer | gen `eruptus.py` (Mesh) + hand animator | corpse stir, swell, eruption, lunge crouch/air/landing (synced game-time stamps) | OK; vanilla creeper hiss AN-33 |
| osteophage | OsteophageRenderer | gen `osteophage.py` (Mesh) | walk | Gaps: melee with no attack pose AN-4 (shallow) |
| ice_fish | IceFishRenderer | gen `ice_fish.py` (Mesh) | swim | OK |
| vampire_squid | VampireSquidRenderer | gen `vampire_squid.py` (Mesh) + `VampireSquidPose` | swim, cloak | OK (shallow) |
| chiton / pyrosome / pelagic_herring / siphonophore / bloody_belly_comb_jelly / hagfish | PelagicCreatureRenderer (Style) | gen `pelagic_ecology.py` (`PelagicMeshes`) | swim cycle per style | OK; silent AN-14 |
| brined_votary | BrinedVotaryRenderer | gen `brined_votary.py` + `_anim.java` | swim, aggressive, swing | OK; dead swim AnimationState AN-21 |
| barbed_urchin | BarbedUrchinRenderer | BB (3 size models) | keyframed idle (`idleAnimationState`) | OK |
| chalybeate_snail | ChalybeateSnailRenderer | hand | crawl, retract (`isRetracted`) | OK; dead AnimationStates AN-21 |
| blood_lantern_jelly / hemojelly | own renderers | hand | bob/pulse | OK (shallow) |
| mnemonic_whale | MnemonicWhaleRenderer | hand | swim (delta movement) | OK (shallow) |
| prism_cuttle | PrismCuttleRenderer | hand | swim, flash (`isFlashing`) | OK (shallow) |
| morphling_polyp | MorphlingPolypRenderer (+ appendage layer) | BB | none (empty `setupAnim`) | **Static** AN-3 |
| leech | LeechRenderer | BB | none (empty `setupAnim`) | **Static** AN-3 |
| bloodlicker | BloodlickerRenderer | hand | walk, belly fill (`getBloodFullness`) | OK (shallow) |
| bog_revenant | BogRevenantRenderer | hand `ModelBogRevenant` | walk, arms raised constantly | Gaps: no attack beat AN-4 |
| fargone | FargoneRenderer | hand, keyframes | idle, walk (walk played on the idle state) | Bug AN-22 |
| desiccant | DesiccantRenderer | hand | walk, sting (`getStingProgress`) | OK (shallow) |
| thirster | ThirsterRenderer | BB | none (empty `setupAnim`) | **Static** AN-3 |
| abhorrent_thought | AbhorrentThoughtRenderer | hand, keyframes | idle, walk (AnimationStates) | OK (shallow) |
| blood_drunk_puppeteer | BloodDrunkPuppeteerRenderer | hand | walk, carousel horse mount | OK (shallow) |
| excoriated / phlegethontic_bombardier | Phlegethontic renderers | hand / hand keyframes | Excoriated walk, attack, death; Bombardier state machine with keyframes | OK (shallow); registration site AN-27 |
| recall_barb / gore_wound_harpoon | Phlegethontic renderers | geom | flight, return | OK (shallow) |
| will | WillRenderer | hand `WillModel` | walk | OK (shallow) |
| will_anchor / arbor_of_will | own renderers | geom | anchor; arbor growth (`ArborGrowthAnimations`) | OK; per-frame cost AN-30 |
| blood_thrall | BloodThrallRenderer | hand | carry ratio, entity-event squish | OK (shallow) |
| drudge | DrudgeRenderer | BB | none (empty `setupAnim`) | **Static** AN-3 |
| enthralled_doll | EnthralledDollRenderer (+ glow layer) | hand | melt squash only (renderer `scale`), no limbs | **Static** AN-3 |
| hematic_construct | HematicConstructRenderer | **vanilla** `HumanoidModel` on `ModelLayers.ZOMBIE`, vanilla zombie texture | vanilla zombie arms | **Placeholder** AN-2 |
| unstained_zealot / guardian / acolyte / scout | own renderers | hand (EntityModel) | walk, head | OK (shallow); silent AN-14 |
| harbinger_hermit / alchemist / artificer / cicatrix_anchorite / mnemonist / vicar / voyager / votary_wayfarer | own renderers | hand (EntityModel); `npc_portraits.py` only renders portraits | walk, head; Vicar initiating; Hermit farewell dissolve | OK (shallow); dead idle states AN-21 |
| spectral_companion (PaleIntercession) | PaleIntercessionRenderer | hand, keyframes | presentation states | OK |
| phantasmal_echo | PhantasmalEchoRenderer | vanilla `HumanoidModel` on PLAYER layer, own translucent texture | vanilla humanoid | OK by design (player echo) |
| possession_echo | PossessionEchoRenderer | vanilla `PlayerModel` | player poses | OK by design |
| humanity_sprite / awakened_ichorian_sigil / unsettled_ichor / covenant_throne_seat / hematic_armature_restraint | own renderers | geom | sprite and sigil animation; seats invisible | OK (shallow) |
| peacock_spider | PeacockSpiderRenderer | hand | walk | Gaps: no attack/display beat AN-4 (shallow) |
| chitinite / fervent_chitinite | own renderers | hand | none (empty `setupAnim`); synced roll-up never drawn | **Static** AN-3, AN-5 |
| tooth_pecks | ToothPecksRenderer | hand | walk, latched, feed count | OK; missing OGGs AN-10 |
| hemolymphopoda | HemolymphopodaRenderer | hand | walk | OK (shallow) |
| lantern_tick | LanternTickRenderer | hand | walk, bite ticks, tick state | OK (shallow) |
| myelin_borer | MyelinBorerRenderer | hand | crawl | OK (shallow) |
| mortarbound | MortarboundRenderer | hand | walk, forms | OK (shallow) |
| venom_rib_centipede | VenomRibCentipedeRenderer | hand | segment walk, strike (`getStrikeProgress`) | OK (shallow) |
| chthonian / chthonian_queen | own renderers | hand / BB | walk, climb head-follow, mandible chew | OK; Queen has no bar AN-37 |
| verdigris_moth | VerdigrisMothRenderer | hand | flight | OK; dead fly state AN-21 |
| choir_keeper | ChoirKeeperRenderer | hand | flight pose, flare | OK (shallow) |
| luminal_cicada | LuminalCicadaRenderer | hand | cling face, flash (entity event) | OK (shallow) |
| hematic_burrower / scarlet_serpent | own renderers | hand | walk; serpent strike and hood | OK (shallow) |
| lump_of_thought | LumpOfThoughtRenderer | BB | none (empty `setupAnim`) | **Static** AN-3 |
| antibody | AntibodyRenderer | hand | spin and bob only | Gaps: melee with no attack beat AN-4 |
| hunter_fungus | HunterFungusRenderer | hand | walk, aggressive | OK (shallow) |
| facsimile_bat / enderman / blaze / golem / horse / donkey / wolf / spider | FacsimileRenderers.* | vanilla rigs + `FacsimileDeformation` | vanilla animation, deformed | OK by design |
| crimson_fireball / morphling_field | NoopRenderer | none | drawn by particles / `MorphlingStrainWorldRenderer` | OK |
| iron_pillar / iron_wall / iron_spike | own renderers | hand (static by design) | none | OK; legacy vanilla particles AN-17 |
| directed_blood_orb / tracking_blood_orb / tracking_snake / tracking_pests / blood_cloud / blood_cloud_carrier | own renderers (no geometry) | none | particle trails (client-guarded) | OK; dead texture constants (Not a problem) |
| blood_bolt / blood_needle / blood_bullet / blood_shot / dark_arrow / sanguis_lancea / living_sickle_hook / living_flail_head / veinwing_feather / wretched_will | own renderers | geom / hand | flight | OK (shallow) |
| flying_charm / hemolytic_vial_projectile / constrictor_cord_projectile / blood_chum_projectile | ThrownItemRenderer | item sprite | none | OK (thrown items) |
| morphling_polyp_item / qliphoth_seed_item | own item-entity renderers | geom | bob, spin | OK |

## Findings

### 1. Entity inventory and placeholders

**AN-1 · High · The Circus Ringmaster is locked in one fixed perch pose for the whole finale.**
Evidence: `client/model/entity/npc/CircusRingmasterModel.java:141-158` calls `super.setupAnim` with zero limb swing and
zero head yaw, then hard-sets body, arm, leg and calf rotations every frame; `common/entity/npc/circus/CircusRingmasterEntity.java:87`
gives it a `MeleeAttackGoal`, and lines 115-147 run the RAFTERS, CAROUSEL and DESCENT phases (knife throws, swarm calls,
the vulnerable descent). The hard-set right arm also overwrites the vanilla swing. Result: the finale boss slides
around crouched, never turns its head, never walks and never visibly strikes or throws. Confidence: Verified.
Action: give the Ringmaster per-phase poses driven by the synced site phase (perched in RAFTERS, standing on the
carousel, walking and striking in DESCENT) and a throw beat timed to `throwKnife`. Effort: M.

**AN-2 · High · `hematic_construct` renders as a vanilla zombie, model and texture, and is spawned by Hemorath's Downfall chapel and the Blood Trial Altar.**
Evidence: `client/render/entity/mob/monster/HematicConstructRenderer.java:12` (texture
`minecraft:textures/entity/zombie/zombie.png`), `:15` (`new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE))`);
spawned by `common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java:397-401` (the `default` branch, which is Hemorath's
Downfall; Hemorath's Chapel slots are Beholding/Litany/Downfall per `docs/HEMOMANCY_REFERENCE.md` §Chapels) and
`common/tile/harbinger/puzzle/BloodTrialAltarBlockEntity.java:96`. No custom model, no ambient/hurt/death sounds
(`common/entity/mob/monster/HematicConstructEntity.java`). A D8 set piece shows plain zombies. Confidence: Verified.
Action: give the construct a generated blood-iron body (`tools/model_export/`, jointed rig) and its own sounds, or swap
the Downfall spawn to an existing Hemomancy mob. Effort: M.

**AN-3 · Medium · Eight live mobs have an empty `setupAnim`: no walk, idle or attack motion at all.**
Evidence: `client/model/entity/mob/arthropod/ThirsterModel.java:73` (Thirster has `MeleeAttackGoal`,
`common/entity/mob/monster/ThirsterEntity.java:68`); `client/model/entity/mob/animal/LumpOfThoughtModel.java:185` (melee,
`common/entity/mob/monster/LumpOfThoughtEntity.java:78`); `client/model/entity/mob/monster/EnthralledDollModel.java:89`
(melee, `common/entity/mob/monster/EnthralledDollEntity.java:63`; a Circus Succession challenge mob; the renderer only
squashes it while melting); `client/model/entity/npc/DrudgeModel.java:118` (summon with melee,
`common/entity/npc/DrudgeEntity.java:125`); `client/model/entity/mob/animal/LeechModel.java:68`;
`client/model/entity/summon/MorphlingPolypModel.java:98`; `client/model/entity/mob/arthropod/ChitiniteModel.java:167`;
`client/model/entity/mob/arthropod/FerventChitiniteModel.java:190`. Confidence: Verified (renderers checked for
compensating poses; only the Doll's melt squash exists). Action: add at least a walk cycle and an attack beat, or
regenerate the bodies on the jointed rig; prioritise the Doll (Circus) and the Drudge (player summon). Effort: M.

**AN-4 · Low · Several melee mobs have a walk cycle but no attack beat.**
Evidence: `client/model/entity/mob/monster/AntibodyModel.java:55-63` (spin and bob only; melee goal at
`common/entity/mob/monster/AntibodyEntity.java:88`); `client/model/entity/mob/monster/ModelBogRevenant.java:83-97` (arms
fixed forward, no swing); Osteophage and Peacock Spider models read only limb swing while their entities override
`doHurtTarget`. Confidence: Verified for Antibody and Bog Revenant, Suspected for the other two (timing not traced).
Action: read `attackTime` (vanilla swing) in these models. Effort: S each.

**AN-5 · Medium · The Chitinite roll-up (invulnerable state) has no visual.**
Evidence: `common/entity/mob/arthropod/AbstractChitiniteEntity.java:34` syncs `ROLLED_UP`; `:58-63` cancels all damage while
rolled; `isRolledUp()` is never read by client code (scripted sweep of client references); both models have empty
`setupAnim` (AN-3). A player hits a cave mob that silently ignores every hit. Confidence: Verified. Action: draw the
roll (curl the body, or swap to a ball pose) from `isRolledUp()`. Effort: S.

**AN-6 · Medium · Saint endings are uneven: Velorum has a bespoke fall, Hemorath's fall is the vanilla sideways flop, and the other three endings vanish in one frame.**
Evidence: Hemorath's HP "fall" runs a 20-tick `tickDeath` (`common/entity/boss/saint/hemorath/HemorathEntity.java:362-364`)
and `client/render/entity/boss/hemorath/HemorathRenderer.java:24` does not override `getFlipDegrees`, so
`LivingEntityRenderer` tips him 90 degrees with the red hurt tint; `client/render/entity/boss/velorum/VelorumRenderer.java:38`
returns 0 and `VelorumModel.java:1241` drives its own death pose. Hemorath's overload (`HemorathEntity.java:297-305`),
Putriciel's fifth absolution (`PutricielEntity.java:373-380`) and Seraphae's containment (`SeraphaeEntity.java:538-553`)
end with a particle burst and `discard()`, so the body disappears on the next frame; Seraphae's ending sound is the
vanilla `UI_TOAST_CHALLENGE_COMPLETE` (`:541-542`). Putriciel and the Hemorath renderer share the missing flip override,
reachable only by `/kill`-style damage. Saints "cannot be killed" in lore (`SaintTrialEntity.java:51`). Confidence:
Verified. Action: return 0 from `getFlipDegrees` for all four and give each ending a short sink/return pose before the
discard (fits D-2). Effort: M.

**AN-7 · Medium · An enraged (forced-door) Saint is visually identical to a normally woken one.**
Evidence: `common/entity/boss/saint/SaintTrialEntity.java:273` keeps `awakening` as a plain saved field, not synced data;
`:295-325` applies damage, speed and health modifiers and Resistance/Regeneration with `visible=false`; no client class
reads an enrage flag (only Naeglerophaeon has `isEnraged`). No frame variant, tint, title card or sound marks it.
Confidence: Verified. Action: sync an enraged flag and show it (frame reaction, tint, or a short card). Effort: S.

**AN-8 · Medium · Two generator animation fragments still read a synced counter plus the partial tick; regenerating either model reintroduces the A-1b violation.**
Evidence: `tools/model_export/stained_priestess_anim.java:65,67` (`entity.getModeTick() + partial`,
`entity.getActionTick() + partial`) and `tools/model_export/latent_annetta_infection_anim.java:243-244`
(`getActionTick() + partial`, `getEmergeTick() + partial`), while the committed models use the replayed clock
(`client/model/entity/boss/annetta/StainedPriestessModel.java:219,221` call `entity.modeTime(partial)` /
`entity.actionTime(partial)`). `BossAnimationClockGuardrailTest` scans `tools/model_export` only for the old Vesper
names. The other 28 `_anim.java` fragments match their models exactly. Confidence: Verified. Action: copy the committed
lines back into both fragments and extend the guardrail's tools scan to `RAW_SYNCED`. Effort: S.

**AN-9 · Medium · Thirteen committed models were generated by scripts that are not in the repository.**
Evidence: the four Circus faculty models and their base (`client/model/entity/npc/CircusFacultyModel.java` and the
Strongman, Beast Tamer, Threadkeeper, Understudy subclasses) name `tools/circus/build_faculty.py`; the eight puppet and
pattern bodies (`client/model/entity/summon/CinderBellowsModel.java`, `GoreboundHulkModel.java`, `MarrowSpitterModel.java`,
`MnemonistPuppetModel.java`, `RingmasterPatternModel.java`, `SanguineHoundModel.java`, `ScarletMummerModel.java`,
`VeinwingVultureModel.java`) name `tools/circus/build_*.py`. `.gitignore:70` ignores `/tools/*` and does not re-include
`tools/circus/`; `git ls-files tools/circus` is empty. The BOSSES ruling "never hand-edit a generated model" cannot be
followed for Vesper's seven puppets without the local scripts. Confidence: Verified. Action: move the builders into
`tools/model_export/` (tracked), or add `!/tools/circus/` with a note in AGENTS.md. Effort: S.

### 2. Animation states

**AN-21 · Low · Twenty-odd AnimationStates are started every client tick and never read by any model.**
Evidence: started but unread: `HemorathEntity.java:95,337` (`collapseChargeAnimationState`), `PutricielEntity.java:104,191`
(`absolvedAnimationState`), `SeraphaeEntity.java:122-123,593-600` (`fracturing`, `condensing`), `VelorumEntity.java:105,219`
(`martyrdomAnimationState`), `VerdigrisMothEntity.java:36,106-110` (`fly`), `BrinedVotaryEntity.java:35,154` (`swim`),
`ChalybeateSnailEntity.java:50-51,207-211` (`crawl`, `retract`), `CrimsonDoeEntity.java:24-25,82-87` and
`VenousStriderEntity.java:25-26,84-88` (`idle`, `walk`), and the `idleAnimationState` of the eight Harbinger NPCs, four
Unstained NPCs, the Saints, Blood Lantern Jelly, Hemojelly, Hemolymphopoda and Lantern Tick. Only Barbed Urchin,
Abhorrent Thought, Fargone, Pale Intercession, Vesper and the Bombardier models call `animate(...)`. The models read
the same state through getters, so nothing is visibly broken. Confidence: Verified. Action: delete the unread states
and their `tick` branches (falls under M-3 dead code if you prefer to batch it). Effort: S.

**AN-22 · Low · Fargone plays its walk keyframes on the idle AnimationState.**
Evidence: `client/model/entity/mob/monster/FargoneModel.java:167-170` animates `FargoneModel.WALK` with
`pEntity.idleAnimationState`; the entity starts a separate `walkAnimationState` (`common/entity/mob/arthropod/FargoneEntity.java:162`)
and never stops idle while walking, so walk and idle stack and the walk's phase is tied to when idle first started.
Confidence: Verified. Action: pass `walkAnimationState` to the WALK call. Effort: S.

**AN-23 · Low · The Gorebound Hulk reads a synced counter plus the partial tick, outside the boss guardrail's scope.**
Evidence: `client/model/entity/summon/GoreboundHulkModel.java:87-90` (`windup + partialTick` on `getStrikeWindup()`,
synced each server tick at `common/entity/summon/GoreboundHulkEntity.java:28-39`). The damage lands at windup 10
(`:33-36`) and the pose lifts over `windup / 8`, so timing matches, but the strike stutters under network bursts in
Vesper's fight. `BossAnimationClockGuardrailTest.BOSS_CODE` covers only `client/model/entity/boss/` and three other boss
paths. Confidence: Verified. Action: replay it with `BossReplayClock` and widen the guardrail to Vesper's puppets.
Effort: S.

**AN-24 · Low · Marrow Spitter and Veinwing Vulture fire with no throw or shoot beat.**
Evidence: `client/model/entity/summon/MarrowSpitterModel.java:103-139` loops the juggle on `age` only; the throw comes from
the unsynced `shotCooldown` (`common/entity/summon/MarrowSpitterEntity.java:117-118`). `VeinwingVultureModel` reads no
entity state while `VeinwingVultureEntity.java:113-116` shoots feathers. Confidence: Verified. Action: sync a throw
tick and add a release pose. Effort: S each.

**AN-31 · Low · Vesper's and the Circus's puppets keep vanilla navigation bodies' sounds and look (zombie, wolf, vex).**
Evidence: `common/entity/summon/GroundPuppetEntity.java:17` (`extends Zombie`, no sound overrides, inherited by Bellows,
Hulk, Mnemonist Puppet, Pattern, Mummer); `SanguineHoundEntity.java:31` (`extends Wolf`); `VeinwingVultureEntity.java:26`
(`extends Vex`). The reference says they reuse Zombie navigation "only" (`docs/HEMOMANCY_REFERENCE.md` "Artificial-body
lifecycle"), but they groan, bark and vex-chirp. Confidence: Verified. Action: override ambient/hurt/death/step sounds
(silence or custom). Effort: S.

**AN-32 · Low · The Vampire Bat's bite has no pose, effect or sound.**
Evidence: `common/entity/mob/animal/VampireBatEntity.java:130-131` hurts the prey every 60 ticks (25 for an aggregate); the
model only loops a jaw snap while `isHunting` (`client/model/entity/mob/animal/VampireBatModel.java:120-128`); no
particle or sound on the bite. Confidence: Verified. Action: broadcast an entity event on the bite and snap the jaw /
emit a `bloodCell` accent. Effort: S.

### 3. Clocks

**AN-25 · Low · `NaeglerophaeonRenderer` builds its own game-time clock instead of `AnimationTime`, and the guardrail regex cannot see it.**
Evidence: `client/render/entity/boss/endgame/NaeglerophaeonRenderer.java:49,67,87,90,94` (`long tick=entity.level().getGameTime()`,
then `tick+partial`). It is game time, so it freezes and survives rejoin correctly, but `AnimationTimeGuardrailTest.RAW`
only matches `getGameTime()` or `tickCount` literally followed by `+ partial`. Confidence: Verified. Action: use
`AnimationTime.gameTime(level, partial)`; consider matching local `tick + partial` in the guardrail. Effort: S.

**AN-26 · Low · Synced block-entity counters plus the partial tick in the Loom, Engram sigil and Dome renderers.**
Evidence: `client/render/tile/harbinger/crafting/engram/EngramSigilRenderer.java:67`, `SomaticLoomEcho.java:84`,
`SomaticLoomRenderer.java:243`, `client/render/world/chamberofwill/SomaticLoomDomeRenderer.java:94`,
`SomaticLoomSeamRenderer.java:51` (`getClimaxTicks() + partial`). Same stall/skip risk the A-1b ruling removed from
bosses, outside every guardrail. Confidence: Suspected (depends on how often the block entity resends). Action: replay
with `BossReplayClock` or a client-advanced counter. Effort: S.

The wall-clock users all sit on the `AnimationTimeGuardrailTest` A-4 list; no other wall-clock source (`Blaze3D.getTime`,
`glfwGetTime`, `Util.getNanos`, `Instant.now`) appears. Client-tick counters such as
`client/data/CardinalRiteEventClientState.java:85` keep running under `/tick freeze`; that is decision A-4.

### 4. Particles and effects

Vanilla particle references by file (count, lines, particle; **(i)** reasonable fallback, **(ii)** should be a
Hemomancy effect). References include static option constants.

- **(ii)** `common/rite/unstained/UnstainedCardinalRiteEvents.java` 20: L292-843 END_ROD, SCRAPE, SNOWFLAKE, WAX_OFF
- **(ii)** `common/manipulation/ferric/VascularDowsingManip.java` 9: L36-44 DustParticleOptions (sent at L76)
- **(ii)** `common/tile/harbinger/crafting/HematicArmatureBlockEntity.java` 6: L67,226-251 Dust, FLAME, SMOKE, CRIMSON_SPORE
- **(ii)** `common/entity/summon/PhantasmalEchoEntity.java` 5: L120-278 REVERSE_PORTAL, SMOKE
- **(ii)** `common/entity/summon/BloodConstructEntity.java`, `EntityIronPillar.java`, `EntityIronSpike.java`, `EntityIronWall.java` 16: ASH, SQUID_INK, REDSTONE dust (legacy non-player construct path)
- **(ii)** `common/entity/mob/monster/MortarboundEntity.java` 4: L145-151 SCULK_SOUL, SQUID_INK
- **(ii)** `common/block/unstained/functional/AltarOfCleansingBlock.java` 4, `UnstainedPodiumBlock.java` 2, `PaleSilverBellsBlock.java` 2, `crafting/PallidRetortBlock.java` 2, `crafting/VerdigrisLatticeBlock.java` 2, `crafting/StillwaterCondenserBlock.java` 1: END_ROD, HAPPY_VILLAGER, WAX_OFF, DRIPPING_LAVA
- **(ii)** `common/event/MorphicNectarEvents.java` 4: TOTEM_OF_UNDYING, WITCH, MYCELIUM; `WhiteHumorPurificationEvents.java` 4: END_ROD, POOF, SCRAPE; `ConsecrationHandler.java` 2; `PossessionEvents.java` 2 (L115,128 TOTEM_OF_UNDYING); `HematicSalvageEvents.java` 1; `worldevent/ChamberOfWillEvents.java` 1
- **(ii)** Circus: `CircusFireEaterEntity.java` 3 FLAME, `CircusAcrobatEntity.java` 3 CLOUD/POOF/PORTAL, `CircusCarouselEntity.java` 3 EXPLOSION/CRIMSON_SPORE, `CircusKnifeThrowerEntity.java` 1 CRIT, `CircusRingmasterEntity.java` 1 CRIMSON_SPORE, `mission/circus/CircusDemonstrations.java` 1 FLAME, `client/screen/overlay/CircusPerceptionOverlay.java` 3
- **(ii)** Mobs: `ChoirKeeperEntity.java` 4 (END_ROD, PORTAL), `VerdigrisMothEntity.java` 3, `LuminalCicadaEntity.java` 2 (FLASH, END_ROD), `ChalybeateSnailEntity.java` 2, `PrismCuttleEntity.java` 2 dust, `PeacockSpiderEntity.java` 2 dust, `BloodLanternJellyEntity.java` 1, `monster/will/WillEntity.java` 2 (POOF, CRIMSON_SPORE), `summon/BloodThrallEntity.java` 2 dust, `item/EntityMorphlingPolypItem.java` 3
- **(ii)** Stations and blocks: `MorphlingIncubatorBlockEntity.java` 2, `MycelialCrucibleBlockEntity.java` 2, `SanguineMonolithBlockEntity.java` 2 (LARGE_SMOKE, SQUID_INK), `VigilArchiveBlockEntity.java` 2, `HematicSutureNodeBlock.java` 1 dust, `ViridSalisTrailBlock.java` 1 dust
- **(ii)** Armour, effects, items: `PhanstmalBloodlustArmorAbilityHandler.java` 3, `EdaciousBloodburst...` 1, `SheolicBastion...` 1, `SilentArchon...` 1; `ElytraEffect.java` 1, `NeuralOverloadEffect.java` 1, `SilverWardEffect.java` 1, `VerdigrisAuraEffect.java` 1; `LivingSicklePruning.java` 2, `MemoryThreadItem.java` 1, `PaleSilverBellItem.java` 1; `client/event/LayerEvents.java` 1 SOUL; `projectile/DirectedBloodOrbEntity.java` 1, `TrackingBloodOrbEntity.java` 1 ASH; `ConstrictorCordEntity.java` 1 CRIT
- **(i)** Block debris `BlockParticleOption`: `client/render/world/CardinalFloorConsecrationRenderer.java`, `VesperPhaseTwoCombat.java:803`, `HematicBurrowerEntity.java:99`, `ChewWoodGoal.java:261`, `MyelinBorerWorkEffects.java:24`, `CircusStiltWalkerEntity.java:84`
- **(i)** Item crumbs `ItemParticleOption`: `OsteophageEntity.java:209`, `ChoirKeeperEntity.java:263`, `MorphlingIncubatorRenderer.java:104`, `rite/CardinalRiteOfferingDissolveRenderer.java:166`
- **(i)** Water and vanilla-shaped events: `BloodChumEntity.java` BUBBLE/FISHING, `ChummedWatersAreaManager.java` BUBBLE, `EntityFlyingCharm.java` BUBBLE, `HemolyticVialEntity.java` SPLASH/SNOWFLAKE, `LetheanChaliceItem.java` SPLASH, `BloodBoltEntity.java` ENTITY_EFFECT (tipped-bolt swirl), `BiomeInit.java` ambient spores, candle/engram SMOKE, Alembic DRIPPING_LAVA, Thurible FLAME, Scriptorium ENCHANT, copper brushing and Verdigris SCRAPE/WAX_OFF (`CopperBrushingHandler.java`, `VerdigrisCenserItem.java`, `ViridSalisTrailHandler.java`)

**AN-11 · Medium · The manipulation vanilla-particle test misses vanilla dust; Vascular Dowsing still sends it.**
Evidence: `src/test/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationVanillaParticleSourceTest.java`
matches only `ParticleTypes.`; `common/manipulation/ferric/VascularDowsingManip.java:36-44,75-76` builds nine
`DustParticleOptions` and sends one per ore through `sendParticles`. Three more manipulations declare an unused vanilla
dust constant: `flammeus/VitricCombustionManip.java:35`, `mortem/CarrionCommunionManip.java:25`,
`mortem/FuneralBellManip.java:23`. `docs/MANIPULATION_VISUALS.md` says no vanilla particles in `common/manipulation/`.
Confidence: Verified. Action: add `DustParticleOptions|DustColorTransitionOptions|BlockParticleOption|ItemParticleOption`
to the test pattern, delete the three dead constants, move Dowsing's ore marks onto the `ORE` form or `HemoParticleData.glow`.
Effort: S.

**AN-12 · Medium · No guardrail stops new vanilla particles outside `common/manipulation/`, and focal systems still use them heavily.**
Evidence: the list above (about 163 references in focal code); `ParticleDataGuardrailTest` checks data classes only. The
worst: the Unstained Cardinal Rites (20, `UnstainedCardinalRiteEvents.java:292-843`), the Hematic Armature upgrade on a
restrained player (48 vanilla redstone dust plus spores and flame per upgrade, `HematicArmatureBlockEntity.java:224-251`),
possession totems (`PossessionEvents.java:115,128`), the Circus acts and finale, and the iron constructs' legacy path
(`EntityIronSpike.java:58-71`, `EntityIronWall.java:59-71`, `EntityIronPillar.java:92-104`, `BloodConstructEntity.java:122-134`).
Confidence: Verified. Action: a ratchet test (allow-list of the current (i) files with reasons, everything else fails),
then sweep the (ii) files system by system; the Unstained rites can wait for D-11. Effort: S for the ratchet, L for the
sweep.

**AN-16 · Medium · The Circus acts and finale are presented with vanilla particles and vanilla sounds.**
Evidence: fire breath `CircusFireEaterEntity.java:41,52,65` FLAME with FIRECHARGE_USE; acrobat `CircusAcrobatEntity.java:52,78,100`
CLOUD/POOF/PORTAL with ENDERMAN_TELEPORT; knives `CircusKnifeThrowerEntity.java:75` CRIT; carousel break
`CircusCarouselEntity.java:170,215,292` EXPLOSION; finale phase change `CircusRingmasterEntity.java:148` ENDER_DRAGON_GROWL;
finale completion `:289-291` CRIMSON_SPORE and TOTEM_USE. The performer models are vanilla `HumanoidModel` subclasses
(see table), not the jointed rig. Confidence: Verified. Action: rebuild the act effects on the toolkit (embers for fire
breath, claw/slash for knives, puppet-string geometry for the finale) when the Circus gets its pass (D-18). Effort: M.

**AN-17 · Low · The iron constructs' legacy (non-player) path is a vanilla cloud.**
Evidence: `common/entity/summon/EntityIronSpike.java:57-71`, `EntityIronWall.java:58-71`, `EntityIronPillar.java:92-104`,
`BloodConstructEntity.java:121-134` (SQUID_INK, REDSTONE dust, ASH every tick for the construct's life). Player
constructs return early (`isPlayerConstruct()`, `FerricConstructEntity.java:57`), so only Will/Drudge/boss casts show
it. Confidence: Verified. Action: route the legacy path through `FerricDuctilisEffects`. Effort: S.

**AN-28 · Low · Two particle JSONs have no registered particle type.**
Evidence: `src/main/resources/assets/hemomancy/particles/blue_sparkle_velocity.json`, `trail_orbit.json`; the 12 types in
`common/init/ParticleInit.java` all have a provider (`client/registry/ParticleProviderRegistrar.java:21-32`) and a JSON.
Confidence: Verified. Action: delete the two files. Effort: S.

Server-only spawns: 20 `addParticle` call sites in `common/` without a visible guard were traced to their callers; all
run on the client (`isClientSide` branches, `clientTick`, `handleEntityEvent`). No server-only `addParticle` was found.

### 5. Sounds

**AN-10 · Medium · Tooth Pecks plays three sound events whose OGG files do not exist.**
Evidence: `assets/hemomancy/sounds.json` entries `entity.tooth_pecks.ambient|hurt|death` name
`hemomancy:entity/tooth_pecks/{ambient,hurt,death}`; there is no `sounds/entity/tooth_pecks/` folder; the entity uses all
three (`common/entity/mob/animal/ToothPecksEntity.java:161,207-218`). It is silent and the sound engine logs a missing
file on every resource load. Confidence: Verified. Action: add the OGGs or point the entries at existing sounds.
Effort: S.

**AN-15 · Medium · 57 OGG files (35 MB) are referenced by no sound event.**
Evidence: `assets/hemomancy/sounds/entity/{beast_from_beyond,colin,dark_young,denizen,dread_bot,hastur,samples,seraphim,tentacle}/`
and `sounds/item/{bow_blade,star_slug}/` (including several `music*.ogg` tracks and `OLD*` files); none appears in
`sounds.json`. They ship in the jar and look like leftovers from another project. Confidence: Verified. Action: delete
them (and confirm none was meant for an unbuilt feature). Effort: S.

**AN-13 · Medium · The Court minibosses are silent and every boss voice is a vanilla remap.**
Evidence: `BloomWardenEntity` and `KindledChampionEntity` (`common/entity/boss/court/`) override no ambient/hurt/death
sound (generic hostile hurt only). Saints: `HemorathEntity.java:374-384` WITHER_AMBIENT/HURT/DEATH, `PutricielEntity.java:408-418`
WITHER, `VelorumEntity.java:381-391` GUARDIAN/ELDER_GUARDIAN, `SeraphaeEntity.java:653-663` amethyst and beacon; Annetta's
three forms and Phlegethosoma (slime) use vanilla sounds only. No `entity.<saint>.*` event exists in
`common/init/SoundInit.java`. M-4 covers the mausoleum ambience picks, not boss voices. Confidence: Verified.
Action: give the Court minibosses at least hurt/death sounds now; schedule boss voices with D-2. Effort: S (Court),
M (voices).

**AN-14 · Low · Many mod creatures fall back to silence or the generic hurt sound.**
Evidence (no ambient/hurt/death override in the class chain): the four Unstained NPCs and eight Harbinger NPCs,
the Circus performers and Ringmaster, `hematic_construct`, `antibody`, `hunter_fungus`, `venom_rib_centipede`,
`scarlet_serpent`, `hematic_burrower`, `lantern_tick`, `phlegethontic_bombardier`, `prism_cuttle`, the six
`PelagicCreatureRenderer` species and `vampire_squid`. Confidence: Verified (scripted sweep of overrides). Action:
add hurt/death sounds for the hostile ones first (Antibody, Hunter Fungus, Venom-rib, Serpent). Effort: M.

**AN-33 · Low · The Eruptus swell uses the vanilla creeper hiss.**
Evidence: `common/entity/mob/monster/ErythromyceliumEruptusEntity.java:192` (`SoundEvents.CREEPER_PRIMED`) although it has
five mod sound events. Confidence: Verified. Action: a mod swell sound. Effort: S.

Not reported: the 51 `sounds.json` entries that alias vanilla sounds (rite, Riposte, microscope, mausoleum) are
deliberate remaps; the mausoleum ones are M-3/M-4.

### 6. Manipulation visuals

Grades from code, against `docs/MANIPULATION_VISUALS.md`: **bar** = its own layered geometry class (most marked "A?,
pending in-game review" in `docs/archive/audits/MANIPULATION_VISUAL_CATALOGUE.md`); **adequate** = a school tier
(`AnimusMortemGeometry`, `ThermalGeometry`, `LuxUmbraGeometry`, `FerricDuctilisGeometry`), HutosLib lightning or tendrils;
**vanilla** = a vanilla-particle or legacy `VisceralMesh` path; **none** = nothing beyond the shared outpouring and
cast impact; **retired** = in `ManipulationRetirementRules` (not graded).

| Tendency | bar | adequate | vanilla / legacy | none | retired |
|---|---|---|---|---|---|
| Animus (25) | hematic_riposte, crimson_coronation, blood_aneurysm, blood_rush, blood_shot, guided_blood_shot, hematic_mortar, sanguine_halo, summon_avatar (+arms, armor, legs, complete), conjure_blade | blood_binding, lingering_/chain_blood_binding, blood_lattice, blood_cloud, expansive_/pursuing_blood_cloud, sanguine_tempest, vital_effusion, crimson_tithe | | | summon_thrall |
| Ductilis (17, plus sanguine_marionette and axonal_transduction, registered without a tendency argument) | deadly_gaze, sanguine_ward, conjure_crossbow | hematic_rebuke, hematic_impressment, activation_potential, synaptic_jolt, conductive_mark, hemolymphal_pulse, ferric_rampart, ferric_spikes, sovereign_instinct, synaptic_storm, living_circuit, thread_ripper, axonal_transduction | sanguine_marionette (legacy) | | venous_travel, crimson_harvest |
| Ferric (18) | conjure_axe, conjure_staff, blood_absorption, blood_projection, sanguine_mending, lodestone_collapse | blood_needle, blood_needle_fan, blood_needle_lance, iron_retort, ironhearted, sanguine_magnetism, iron_choir | vascular_dowsing (legacy + vanilla dust) | | ferric_transmutation, sanguine_excavation, ferric_resonance, hematic_ballast |
| Lux (12) | conjure_spear, radiant_wake | hematic_flare, prismatic_reproof, hematic_beacon, lumen_suture, white_verdict, unclosing_eye | | | hemosynthesis, blood_lamp, crimson_sight, vigil_of_glass |
| Tenebris (11) | conjure_claws, umbral_step, umbral_reversal, eclipse_well, umbral_rend | void_shroud, gloam_laceration, blood_eclipse, black_veil_covenant, penumbral_drift | | | blood_eclipse_mantle |
| Flammeus (13) | conjure_torch, searing_lash | crimson_flame_conjuration, pyretic_forge, sanguine_ignition, vitric_combustion, cauterizing_rebuke, scalding_/soaring_/suspended_/expulsive_updraft, furnace_veins, phoenix_debt | | | |
| Congeatio (11) | conjure_flail, cruor_javelin | glacial_grasp, cryogenic_pulse, osseous_bloom, absolute_stillness, rimebound_sentence, endless_hour | | glacial_rampart | glacial_circulation, glacial_bastion |
| Mortem (14) | conjure_sickle, funeral_bell, fruiting_contagion | hemorrhage, lignum_mortis, canopy_mortis, worked_lignum, exsanguinate, insatiable_hunger, grave_debt, blackhearted, carrion_communion, bloom_of_rot | | | vital_reservoir |

Furthest below the bar: vascular_dowsing, sanguine_marionette, glacial_rampart, then the interchangeable-spiral and
three-wisp forms the catalogue's Tier 3 names (`COMMUNION`, `GROWTH`, Mortem statuses), then the single-accent
`unclosing_eye`.

**AN-18 · Medium · Sanguine Marionette and Vascular Dowsing still draw on the legacy `VisceralMesh` switch.**
Evidence: `client/render/world/ManipulationVisualRenderer.java:654-710` ("Only forms no school tier claims reach this legacy
path": `MARIONETTE_TETHER`, `MARIONETTE_ORDER`, `ORE`), fed by `common/manipulation/HematicCommandManager.java:421,430` and
`ferric/VascularDowsingManip.java:73`. The marionette tether is a 32-segment tube with one bead; Dowsing is a locator
box plus vanilla dust (AN-11). Vesper's borrowed attacks reuse the same marionette forms
(`common/entity/boss/endgame/VesperBorrowedAttacks.java:74,105`). Confidence: Verified. Action: move both onto
`PuppetStringsGeometry` / a Ferric locator built from the `LuxUmbraGeometry` primitives. Effort: M.

**AN-19 · Low · Glacial Rampart has no formation visual.**
Evidence: `common/manipulation/congeatio/GlacialRampartManip.java:84-93,110-116` places `frozen_cruor` blocks through
`TemporaryIceManager` with vanilla GLASS_PLACE and POWDER_SNOW_BREAK only; no form, particle or tendril. The wall pops
in. Confidence: Verified. Action: a `FROST_ADVANCE`-style creep along the placement before the blocks land. Effort: S.

**AN-20 · Low · Four manipulation forms are drawn by the client but never sent.**
Evidence: `SWORD_IMPACT`, `SUTURE`, `RUPTURE`, `LUX_MIST` in `common/manipulation/ManipulationVisuals.java` have no
`Form.X` sender anywhere in `common/` but have drawing code in `ManipulationSignatureEffects`, `LuxUmbraGeometry`,
`AnimusMortemGeometry`, `LuxUmbraEffects`. (`FROST_CONJURE`, `MORTEM_CONJURE` and `GRAVE_REFUND` are used by the
client-only echo casts.) Confidence: Verified. Action: delete the four, or wire them where they were meant to play.
Effort: S.

### 7. Boss presentation

| Boss | Bar frame | Opening scene | Intro window | Phase card | Scene audience / late join |
|---|---|---|---|---|---|
| Annetta / Priestess / Latent | yes x3 | yes x3 | Annetta (Harbinger route) | Annetta break | everyone within 64 blocks; no late join |
| Vesper Crowned Refusal / Evening Star | yes x2 | yes x2 | Crowned Refusal | cocoon | within 64 blocks; no late join |
| Mycophant | yes | waking, rematch, capture | own intro rules | split | the hunt owner (`playTo`); no late join |
| Naeglerophaeon | yes | yes | yes | transition | the breaker only |
| Phlegethosoma | yes | yes | yes | no | within 64 blocks; replayed on `startSeenByPlayer` |
| Hemorath / Seraphae / Putriciel / Velorum | yes x4 | shared `saint_rise` | `SaintRise` | Hemorath only | within 64 blocks; no late join |
| Bloom Warden / Kindled Champion | yes x2 | yes x2 | yes x2 | no | the transgressor / within 64 blocks |
| Hydra / Marimo presences | n/a | `hydra_rise`, `marimo_presence` | n/a | n/a | n/a |
| Circus Ringmaster (finale) | **no** | **no** | **no** | **no** | n/a |
| Chthonian Queen ("boss variant") | no | no | no | no | n/a |

**AN-29 · Medium · The Ringmaster finale has no boss presentation at all.**
Evidence: `BossBarBinding` is used by 14 boss classes (through `SaintTrialEntity` for the four Saints) and none is in
`common/entity/npc/circus/`; `CircusRingmasterEntity.java:115-147`
runs three finale phases with only a vanilla ENDER_DRAGON_GROWL on each change (`:148`) and vanilla spores and TOTEM_USE
on completion (`:289-291`). Confidence: Verified. Action: a `BossBarBinding` with a Circus frame, a short opening and a
card per phase on the shared director. Effort: M.

**AN-34 · Low · Only Hemorath of the four Saints has a phase card.**
Evidence: `client/render/cinematic/scene/BossSceneCatalog.java` registers `hemorath_empties` and no card for Seraphae's
fracture/condense, Putriciel's absolutions or Velorum's martyrdom, though each has a synced phase state. The
presentation spec chose this set (OPEN_QUESTIONS §2 "Boss presentation"), so this is a consistency note for D-2.
Confidence: Verified. Action: decide per Saint during D-2. Effort: S each.

**AN-35 · Low · Hemorath's boss bar title key is still `entity.hemomancy.hollow_vessel`.**
Evidence: `common/entity/boss/saint/hemorath/HemorathEntity.java:98`; the lang value already reads "Saint Hemorath"
(`en_us.json:1019`). The retired name survives in the key only (D-17). Confidence: Verified. Action: switch to
`entity.hemomancy.hemorath` when D-17 is settled. Effort: S.

**AN-37 · Low · The Chthonian Queen is documented as a boss variant but has no bar.**
Evidence: `docs/HEMOMANCY_REFERENCE.md:4272`; no `BossBarBinding` in `ChthonianQueenEntity`. Confidence: Verified.
Action: decide whether it is an elite (no bar) and say so in the reference, or bind a plain bar. Effort: S.

**AN-38 · Low · Seraphae's Downfall raises vanilla Vexes.**
Evidence: `ChapelFocusBlockEntity.java:398` (`case "SERAPHAE" -> EntityType.VEX`). The doc names vexes
(`docs/SAINT_MAUSOLEUMS.md:23`), so it is intended, but it is the only Saint wave of pure vanilla mobs next to AN-2.
Confidence: Verified. Action: keep or replace during D-2. Effort: S.

### 8. Render-type and performance hazards

**AN-30 · Low · Per-frame allocations in hot model and renderer paths.**
Evidence: `client/model/entity/boss/hemorath/HemorathModel.java:1462` (new nine-element `ModelPart[]` every frame);
`client/model/entity/boss/endgame/VesperMountModel.java:776-784` (`legParts` allocates a four-part array per leg per call,
called from several per-frame helpers at `:731,759,768,790`); `client/model/entity/summon/MarrowSpitterModel.java:142-149`
(a new `PoseStack` per hand, six per frame); `client/render/entity/misc/ArborOfWillRenderer.java:80-83` (rebuilds the fruit
placements and streams them every frame); `client/model/entity/boss/seraphae/SeraphaeModel.java:2125-2132` (new `PoseStack`,
array and stream per feather-tip query). Confidence: Verified. Action: hoist the arrays to fields, reuse one
`PoseStack`, cache the Arbor placements by degree. Effort: S.

**AN-27 · Low · Entity renderers are registered outside `client/registry`.**
Evidence: `client/event/PhlegethonticClientEvents.java:28-31` registers `excoriated`, `phlegethontic_bombardier`,
`recall_barb` and `gore_wound_harpoon`, against AGENTS.md "Client registration lives in `client/registry/`".
Confidence: Verified. Action: move the four lines into `EntityRendererRegistrar`. Effort: S.

**AN-36 · Low · The Phlegethosoma's coagulum projectile renders as a flat item sprite.**
Evidence: `client/registry/EntityRendererRegistrar.java:226` (`ThrownItemRenderer`) with
`common/entity/boss/phlegethosoma/CoagulumProjectileEntity.java:15` (`sanguine_formation` item). Confidence: Verified.
Action: a small clot renderer matching `CoagulumRenderer`. Effort: S.

**AN-39 · Low · Synced data written and never read anywhere.**
Evidence: `common/entity/mob/monster/will/WillEntity.java` `getAbsorptionStage()` (`DATA_ABSORPTION_STAGE`) has no reader in
common or client code; `MyelinBorerEntity.getAgitation()` and `CourtMemberEntity.isWary()` are read only server-side
(the Court model keeps its own client approach). Confidence: Verified for Will, Suspected for the others being
intentionally server-only. Action: delete or draw. Effort: S.

**AN-40 · Low · Vanilla effects on several mod creature abilities.**
Evidence: `MortarboundEntity.java:145-151` (SCULK_SOUL, SQUID_INK), `ChoirKeeperEntity.java:232,265` (END_ROD, PORTAL),
`LuminalCicadaEntity.java:127,129` (FLASH, END_ROD), `WillEntity.java:552,554` (POOF, CRIMSON_SPORE),
`PhantasmalEchoEntity.java:120-278`. Confidence: Verified. Action: part of the AN-12 sweep. Effort: M.

**AN-41 · Low · `ManipulationVisualRenderer` legacy draw looks up entities by id every frame.**
Evidence: `client/render/world/ManipulationVisualRenderer.java:668-669,694-695` (`world.getEntity` twice per cue per
frame). Cheap individually, but it runs per tether per frame during Vesper's marionette attacks. Confidence: Verified.
Action: resolve once per cue per tick. Effort: S.

## Not a problem

- Every one of the 182 entity types has a renderer; the only `NoopRenderer` entities (`crimson_fireball`,
  `morphling_field`) are drawn by particles or `MorphlingStrainWorldRenderer`.
- Model child lookups resolve: every `getChild("x")` in `client/model/` exists in its layer definition or mesh (15
  apparent misses were all `"name_" + i` loops).
- Generator fragments: 28 of the 30 `tools/model_export/*_anim.java` match their committed models line for line (all
  20 Court fragments, Hemorath, Seraphae, Putriciel, Velorum, Annetta, Mycophant, Vesper mount, Brined Votary). The
  Mesh-only generators (bat, doe, strider, fungling, eruptus, osteophage, ice fish, squid, pelagic) write only the
  `*Mesh.java`, so animation edits in the hand-written animators cannot drift from them.
- Mycophant: all 22 `MycophantAttack` values have a branch in `MycophantModel`/`MycophantAnimState`. Stained Priestess:
  every action (VOLLEY, LANCE, COMBO, PLANTED) and the special modes are posed. Vesper's keyframed transformation is
  started from `onSyncedDataUpdated`, so a rejoining client replays it.
- Saint secondary moves share one table (`SaintAttackPattern`) for the server damage window and the client gather/strike
  motion, so tells and hits cannot drift; Gorebound Hulk's pose and damage both key off windup 10.
- Eruptus, Naeglerophaeon and the Saints time on synced game-time stamps or `BossReplayClock`: correct under
  `/tick freeze` and on rejoin.
- `WorldEffectDispatcher` is the only `RenderLevelStageEvent` subscriber; none of the other nine files that name the
  event carries a `@SubscribeEvent` for it.
- No wall-clock time outside the A-4 allow list; no other wall-clock API in use.
- Particle types: 12 registered, 12 providers, 12 JSONs; colour data goes through `HemoParticleData`.
- Sounds: 188 registered, 188 `sounds.json` entries, every subtitle has a lang key; the three Antecedent recordings are
  played by id (`common/item/component/AncientRecordings.java:12`).
- Client texture paths: 389 checked; the only missing files are dead constants on particle-only renderers
  (`tracker.png`), Leech's unused `TEXTURE` constant and three GUI screens outside this audit.
- Court: all 18 roles plus the two minibosses have generated models and animations; swimmers swim, anchored members
  kneel, defenders (Tidewarden, Edge Swimmer) strike with `getAttackAnim`; the wary posture reads a client-side ease.
- Boss frames and scenes: all 14 frame ids and 26 scene ids in `BossPresentationIds` are registered on the client
  (possession scenes through `PossessionSceneClientState`).
- Facsimiles, the possession echo and the phantasmal echo use vanilla rigs by design (copies of vanilla mobs and of the
  player).
- Fungling growth is shown through the synced `SCALE` attribute, not an unread flag.
- Render types: `RenderTypeCachingGuardrailTest` covers per-call construction; no new per-frame `RenderType.create` was
  found.

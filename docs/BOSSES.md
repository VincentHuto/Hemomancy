# Bosses and Encounters

Status (2026-11-14): Annetta, Vesper, the Mycophant, the Naeglerophaeon opening and the shared presentation are built and committed (see History). The Saint combat kit, shared plumbing for the four Saint fights, was built on 2026-11-05 and is uncommitted. The owner approved the presentation and the fight calls on 2026-11-13 without a screenshot pass (PR-9 stills are still owed). The 2026-11-14 overnight build (uncommitted) made the one boss definition, changed three fight numbers, routed Vesper and Saint experience through the victory and keeps a Mycophant win through a logout or death in the yield. On 2026-11-15 (B15) Annetta's health went to 51 (CR-16), the lost rematch re-arms (PT-84), and the Ringmaster, rite sacrifices and the Scarlet Mummer's attention joined the boss tag. Left: the PR-9 stills. On 2026-11-15 (B31) the Circus Ringmaster moved onto the generic half of the kit, with five mental moves and a yield beat (D-33, D-36). The Phlegethosoma summon (CR-7) was built on 2026-11-15 (B14). Open questions are in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md).

## What it is

Hemomancy's authored fights and the plumbing they share. This page covers the non-Saint encounters and the presentation layer; the four Saints are in `docs/SAINT_MAUSOLEUMS.md` and the Brined Court minibosses in `docs/BRINED_COURT.md`.

| Encounter | Entry | Shape | Source |
|---|---|---|---|
| Annetta Knowles (Broken Church) | dialogue with a Tooth Pecks jar (Harbinger) or a Draught of Still Mercy (Unstained, needs Clarity) | two routes: Phase 1 and the break into the Stained Priestess, or the purge into the Latent Infection with Annetta as an ally | `HEMOMANCY_REFERENCE.md` §entity table (Annetta rows), `LORE_REFERENCE.md` §11 |
| Vesper, the Crowned Refusal then the Evening Star | the Living Sickle cut of the owned Bloom, owner-bound ordeal | phase 1 on the mount (ground moves, flight from 50%, burrow, three wounds, puppet roster); phase 2 in three acts | `HEMOMANCY_REFERENCE.md` §5.10 |
| The Mycophant (Spawn Mother) | the 15-minute Gardens hunt or a Fruiting Lure, into the Nursery | the waking, the claim (100 to 60%), the split, the joining, the Last Brood below 25%, the yielding | `HEMOMANCY_REFERENCE.md` §Mycophant Hunt and Morphic Nursery |
| Naeglerophaeon | breaking a Vagrant Mind's Dormant Ganglion | existing miniboss; this round only gave it a 70-tick opening | `HEMOMANCY_REFERENCE.md` §Naeglerophaeon |
| Bloom Warden, Kindled Champion | Brined Court transgression / Head-tender challenge | Court minibosses on the shared plumbing | `docs/BRINED_COURT.md` |
| Saints (Hemorath, Putriciel, Velorum, Seraphae) | mausoleum trials | the Saint combat kit (below); the `saint_rise` opening, one frame and one meter style each | `docs/SAINT_MAUSOLEUMS.md`, `HEMOMANCY_REFERENCE.md` §5.8 |

**Annetta (three passes, 2026-10-15 to 10-18).** One generated rig carries four looks chosen by `AnnettaLooks`: cowering and Phase 1 (long red hair, gold eyes, the only tells on an otherwise human Unstained cleric; Phase 1 adds her dagger, the cracked Unstained Shield and the neck tick that swells as her health falls) and the purged looks (short white pixie-bob, blue eyes, dagger only, a healing pose). The break needs both 2 minutes survived and 50 damage; she stabs the tick, loses her head, falls, and a reanimation pool raises her as the Stained Priestess, who flies (EMERGING, TAKEOFF, HOVER, SWOOP, PLUMMET, LANDED in `PriestessCombatRules`) with procedural tendril wings and a three-eyed tendril head (`PriestessTendrilRules`, `StainedPriestessTendrilLayer`). On the Unstained route the Draught tears the Latent Infection out of her: gaunt, bone cowl, toothed chest maw around a red core, coiled vessel whip arm, two clawed arms on a lance, serpent tail, black hooked claws (`LatentCombatRules`: claw rake, spear thrust, whip lash, infection bloom, root eruption; it sometimes turns on Annetta). Early death is guarded: she sits at 1 HP until both break conditions are met.

**Vesper (overhaul 2026-10-16 to 10-18).** Vesper is male; he/him everywhere. `VesperMountController` owns the mount's locomotion, facing and move timeline; the AI is never switched off and `NoAI` is cleared from old saves. Burrow (`VesperBurrowRules`, `VesperBurrow`): he grips the mount's nape, it plunges, a fissure trail hunts 10 to 15 seconds with alternating claw and tail strikes, a rumble, an eruption at a point locked partway through the rumble, then about 3.5 seconds of recovery; every second Diving Rend becomes a 5 to 7 second burrow. Wounds at 70/40/10% open on their own (`VesperWoundRules`), are drained by holding Blood Absorption (`VesperBloodAbsorptionInteractions`), scab back only to half, and the mount fights back while wounded. Roster (`VesperPuppetRoster`): seven shapes, Ringmaster excluded, up to three out, killed shapes struck for good, survivors recallable; every unstruck shape lends the Evening Star one borrowed attack (`VesperBorrowedAttacks`). Phase 2 (`VesperEveningStarController`, `VesperEveningStarAct`): Locked (100 to 67%), Open (67 to 33%, cross-school synergy chains), Sickles (33 to 0, strings in `VesperSickleRules`, Harvest once below 10%). `VesperManipulationCaster` casts any fight manipulation; the coverage table lives in the reference.

**The Mycophant (rebuilt 2026-10-18, moveset expanded 2026-10-24).** A genderless brood-mother dragged on four crab claws, with a grasping hand, a feeding siphon, a slim beaked skull in a frilled collar and a gestation sac. The waking (`MycophantIntroRules`, 190 ticks): the player wakes bound, the fibers snap, they rise facing away cradling a fungling, drop it, roar at 150 (bar, music and vulnerability start) and rush at 190 with the embrace as the first attack. Position-aware picking (`MycophantSelectionRules`, `MycophantPositionRules`: bands HUG/NEAR/MID/FAR, sectors FRONT/FLANK/REAR, anti-repeat, kite and hug pressure, follow-ups two deep). At 60% the split: the skull cracks, the Ebrietas pulp face blooms, hyphae wings sprout (never for flight). Phase 2 adds the Fusion Embrace, Wing Guard with a riposte, Wing Sweep, Pulp Bloom, Hyphae Lances, Lattice Net and the Gaze. The Last Brood runs once below 25%. The yielding offers the Tendril; the Apotheos wins by surviving assimilation, not by killing a villain.

**Saint combat kit (2026-11-05).** Shared plumbing for the four Saint fights, shaped so the other bosses could move onto it later (the Circus Ringmaster moved onto its generic half on 2026-11-15, see below; G-2c, one boss definition, was settled on 2026-11-13 as a tag, see Rulings). The generic half lives in `common/entity/boss/`: `BossFootprint` (sealed ground shapes with height bands that the server tests and the client draws with the same geometry, `BossFootprints` and `FootprintFrame` for their saved frame), `BossBands` (range bands HUG under 3, NEAR under 6.5, MID under 12, and FRONT/FLANK/REAR sectors), `BossHold` with `BossHoldCarrier` (a player held at a point on a boss's body, released by named reasons, refused for a `HeldTraveller`), and `BossGroundHazardEntity` (a lingering footprint its owner ticks). The Saint half in `common/entity/boss/saint/` is the move table (`SaintMove`, `SaintMoves`), the picker (`SaintPickRules`: weights by band and sector, anti-repeat, 200-tick pressure, follow-ups two deep), `SaintCombatController` (the Mycophant controller's model: roam, pick, wind-up that tracks, commit that locks the aim, active, recovery; interrupts queue to the recovery boundary), `SaintMeter` with `SaintMeterSync` and the one new payload `SaintMeterPacket`, `SaintPresence`, `SaintOpenings`, `SaintPhaseTable` (phase cards through `BossScenes`), `SaintArena` (the only Saint block editor, restoring on every exit), `SaintDeeds`, `SaintChannelRouter` (held Blood Absorption and Projection into a fight), `SaintEnding` and the damage pipeline in `SaintTrialEntity`. Client: `SaintMeterLayer` (one HUD layer) and `SaintTelegraphRenderer` (one world pass), with each Saint's styles listed in `client/registry/SaintClientStyles`. Timing still runs on `BossReplayClock` and `BossMoveTimeline`. Spec: `superpowers/specs/2026-11-05-saint-overhauls-design.md` (Kit API).

**The Circus Ringmaster's finale in his Chamber (2026-11-18).** The real-world finale below was replaced: the whole fight now runs on a copy of the pavilion in his Chamber of Will (`common/encounter/ordeal/finale/`, `RingmasterFinaleDirector`, `RingmasterFinaleMoves`, `FinaleRules`), still on `BossMoveTimeline`s with his `circus_ringmaster` bar frame shown on his stage copy, two phases, a kneel and an ending chosen by the player's act. The old moves (Sleight, the Cue, the Knife Board, Strings Cut) and the yield and collapse beats are deleted. Details in [CIRCUS.md](CIRCUS.md) "The finale in his Chamber".

**The Circus Ringmaster on the kit's generic half (2026-11-15, B31; superseded 2026-11-18).** The first non-Saint boss on the kit. He keeps his class in `common/entity/npc/circus` and has no Saint controller; his own controller follows the Court's Kindled Champion: a saved `BossIntroWindow` (60 ticks), moves as `BossMoveTimeline`s synced as a move id and tick and replayed through `BossReplayClock`, each striking a `BossFootprint` that the hit test and his server-drawn telegraph (`CircusEffects.footprint`) share, `BossBands.STANDARD` for picking in DESCENT, one `CircusArena` record for the ring, and one writer of the phase. His bar names the CAROUSEL objective. His moves are `CircusRingmasterMoves`: knife, staff blow, the Spotlight, Sleight, the Cue, the Knife Board and Strings Cut (NQ-15-4 A). The last blow opens a saved closing beat (CURTAIN): a 60-tick yield on Succession (the Pattern is granted when it ends) or a 40-tick collapse on Liberation; a relog or a death during it waits. Details in [CIRCUS.md](CIRCUS.md).

**A boss's own telegraph pass over the kit (the pattern since 2026-11-06).** The kit's `SaintTelegraphPainter` over `TelegraphCanvas` is a fallback with a subtlety budget (fills at alpha 0.22 or less, edges 0.012 wide or less, nothing off the floor); after the user played Hemorath it was found too plain for attacks. A Saint's look is its own pass, the shape all four now follow and the one to copy for a later boss on the kit:
- `<Saint>TelegraphStyle` claims every move and hazard and paints nothing, so the kit's flat tints never show; the kit, the canvas and the painter stay unchanged for any boss that has no pass.
- `<Saint>TelegraphPass` (package-private in `client/render/world/`) runs from the manipulation visuals' solid and translucent passes and reads only what the kit's renderer reads: the synced move block, the move's `BossFootprint`, `FootprintFrame`, timeline and reveal (`TelegraphDraw`). Shapes come from the footprint the server tests, so a drawn edge is the hit edge.
- Its materials are its own shaders in `shaders/core/world/`, with render types built once in `<Saint>RenderTypes` and vertices in the school encoding (`MaterialQuads`, state in the colour); the school flows are accents. Timing and layout live in a pure rules class with JVM tests (`HemorathFissureRules`, `SeraphaeOvergrowthRules`, `PutricielBloomRules`, `VelorumFrostRules`).
- Every move has the same life (emerge, build to the commit, a flash, the strike, a linger, a dissolve), effects that follow a limb read the posed model's own points (`<Saint>AnimState`, fed by the renderer each frame), and struck bodies get `<Saint>ImpactGeometry` (Velorum's pass draws its own impacts).
- A lane that cuts the floor is a strip on the floor plane whose shader traces the view ray into a channel below it (Hemorath's rake trenches, carried to the other three), so it reads sunk with no geometry under the floor.
- Proof is the boss's fight review client at 3-tick stills from the boss camera and from a player's eye, set beside Hematic Riposte (`MANIPULATION_VISUALS.md`). Details per Saint: `docs/SAINT_MAUSOLEUMS.md`, "Visuals".

**Shared presentation (2026-10-24).** `BossBarBinding` names a `BossBarFrame` id and sends `BossBarFramePacket` after the bar; `BossBarFrameRenderer` replaces the vanilla draw with a painted frame (lag chip, notches, reveal, phase reaction) per boss in `client/screen/overlay/boss/frame/`. `BossScenePacket` starts a client scene from `BossSceneCatalog`; `BossSceneDirector` runs letterbox, fade, HUD hide, input lock, `TitleCard`, `CinematicShot` camera and skip. `BossIntroWindow` is the server half: a fixed run of passive, untouchable ticks with the bar hidden, saved so a reload carries on. `BossReplayClock` and `BossMoveTimeline` time every move; `BossTelegraphs` draws tells from the authored suite.

## Where it lives

| Piece | Path |
|---|---|
| Shared plumbing | `common/entity/boss/BossReplayClock.java`, `BossMoveTimeline.java`, `BossTelegraphs.java`, `BossBarBinding.java`; `common/entity/boss/presentation/BossIntroWindow.java`, `BossScenes.java`, `BossPresentationIds.java` |
| Boss definition | `data/hemomancy/tags/entity_type/bosses.json` (key `EntityInit.BOSSES`), read only by `common/entity/HemoEntityPredicates.java` `isBoss` |
| Encounter managers | `common/encounter/VesperOrdealManager.java` (the ordeal's victory and its experience), `MycophantEncounterManager.java` (hunt, claim, win, rematch), `MycophantWinSavedData.java` (wins recorded by owner UUID until settled) |
| Saint combat kit | generic `common/entity/boss/BossFootprint.java`, `BossFootprints.java`, `FootprintFrame.java`, `BossBands.java`, `BossHold.java`, `BossHoldCarrier.java`, `BossGroundHazardEntity.java`; Saint `common/entity/boss/saint/Saint*.java`; client `client/screen/overlay/boss/SaintMeterLayer.java`, `client/render/world/SaintTelegraphRenderer.java`, `client/registry/SaintClientStyles.java`, `client/event/BossHoldClientEvents.java` |
| Payloads | `common/network/boss/BossBarFramePacket.java`, `BossScenePacket.java`, `SaintMeterPacket.java` |
| Director (client) | `client/render/cinematic/BossSceneDirector.java`, `BossScene.java`, `BossSceneTracks.java`, `CinematicShot.java`, `CameraOffsets.java`, `TitleCard.java`, `BossSceneClientEvents.java`, `scene/BossSceneCatalog.java` |
| Bar frames (client) | `client/screen/overlay/boss/frame/*Frame.java` (Annetta, Stained Priestess, Latent Infection, Mycophant, Naeglerophaeon, both Vespers, four Saints, Bloom Warden, Kindled Champion, Phlegethosoma, the Circus Ringmaster) |
| Annetta | `common/entity/boss/annetta/` (`AnnettaKnowlesEntity`, `AnnettaEncounterRules`, `StainedPriestessEntity`, `PriestessCombatRules`, `LatentAnnettaInfectionEntity`, `LatentCombatRules`, pools, bolts, lance, eruption) |
| Vesper | `common/entity/boss/endgame/Vesper*.java` (`VesperMountController`, `VesperBurrow`, `VesperWound`, `VesperPuppetRoster`, `VesperEveningStarController`, `VesperSickleRules`, `VesperManipulationCaster`, ...); client `VesperEveningStarMoves` in `client/model/entity/boss/endgame/` |
| Mycophant | `common/entity/boss/endgame/Mycophant*.java` (`MycophantEntity`, `MycophantController`, `MycophantMoves`, `MycophantSelectionRules`, `MycophantLastBrood`, hold kinds, hazards, tumors); client `MycophantAnimState` |
| Naeglerophaeon | `common/entity/boss/endgame/Naeglerophaeon*.java` |
| Court minibosses | `common/entity/boss/court/` |
| Generators | `tools/model_export/annetta_kit.py`, `annetta_knowles.py` + `_anim.java`, `stained_priestess.py` + `_anim.java`, `latent_annetta_infection.py` + `_anim.java`, `annetta_pools.py`, `vesper_mount.py` + `_anim.java`, `mycophant.py` + `_anim.java` (reference blockout `tools/model_export/reference/spawn-mother-v8.html`) |
| Review clients | `gradle/review-clients.gradle` rows `annettaReviewClient`, `vesperReviewClient`, `mycophantReviewClient`, `bossPresentationReviewClient` (the last doubles as the Saint fight review with `-P<saint>FightReview`) |
| Guardrails | `src/test/java/.../architecture/BossAnimationClockGuardrailTest`, `BossFrameworkGuardrailTest`, `BossPresentationBindingTest`, `SaintCombatKitGuardrailTest`, `BossDefinitionGuardrailTest` |
| Config | `HemoClientConfig` section `boss_presentation`: `BOSS_BAR_FRAMES`, `CINEMATIC_BOSS_SCENES`, `BOSS_SCENE_SHAKE` |
| Lang | `boss.hemomancy.<id>.name` / `.epithet` in `en_us.json` |
| Specs | `docs/superpowers/specs/2026-11-05-saint-overhauls-design.md` (the kit) and the four Saint designs; `docs/superpowers/specs/2026-10-15-annetta-encounter-models-design.md`, `2026-10-16-stained-priestess-tendril-form-design.md`, `2026-10-16-annetta-encounter-combat-design.md`, `2026-10-16-vesper-fight-overhaul-design.md`, `2026-10-18-mycophant-spawn-mother-design.md`, `2026-10-24-mycophant-moveset-expansion-design.md`, `2026-10-24-boss-presentation-design.md` |

## Rulings that bind

Animation and models (user, across the three encounter projects):
- All new boss animation is written in Java, procedural or keyframe, Annetta-style Catmull-Rom keys on each move's own clock. Not Blockbench. Vesper's Blockbench clips stopped being the source of truth for anything new; only his transformation clip remains keyframed (spec 2026-10-16 Vesper §4.8; reference §5.10).
- Models come from `tools/model_export/` generators with their `_anim.java` beside them. Never hand-edit a generated model (Mycophant spec §4.1).
- The Annetta rebuild is the smoothness and quality bar for every later boss (Vesper spec §1, Mycophant spec §1).
- Every generated texture sits at 16 px per block after render scale (texel-density ruling of 2026-10-17; `annetta_kit` scripts set `TEXEL_SCALE` for their renderer's scale).
- `AGENTS.md`: "A boss replays a synced tick counter through `common/entity/boss/BossReplayClock` (`sample` per frame, or `sync` per client tick and `at` per frame) and times moves with `BossMoveTimeline`; never keep a private `tickCount - start` clock or add the partial tick to a synced counter (`BossAnimationClockGuardrailTest`)."
- Focal effects use the Hemomancy/HutosLib suite; vanilla particle clouds are the failure case (`AGENTS.md`, every boss spec's anti-goals).
- Boss attacks, telegraphs, impacts and focal moves are bold, layered, physical geometry at the Hematic Riposte bar; the "subtle, no spaghetti" ruling covers overlays on a body and on the screen, not attacks (user, 2026-11-06, after playing Hemorath; `docs/SAINT_MAUSOLEUMS.md` "Rulings that bind").
- No copper, brass, bronze or silver on blood-side bodies (mount, Priestess, Latent Infection). Annetta keeps consecrated copper only while she is still Unstained (models spec §1).

Annetta (user):
- Cowering form is human with long red hair and gold eyes; nothing else infected shows. The purged form has a white pixie-bob and blue eyes and heals the player, dagger only, no shield.
- The break needs 2 minutes survived AND 50 damage dealt (combat spec §Harbinger route).
- The Priestess flies and swoops with tendril wings and a tendril head; the tendril form replaced the box wings and thorn head (tendril spec).
- The Latent Infection is gaunt, bone cowl, toothed chest maw, vessel whip arm, black claws (models spec §5, revised mid-build from the user's reference images).

Vesper (user):
- He/him. Burrow attack; the wound is drained with Blood Absorption; 7-shape roster with the Ringmaster excluded, up to 3 out, killed shapes struck for good, each unkilled shape lends the Evening Star a borrowed attack; phase 2 acts Locked / Open / dual Sickles.
- The mount is rebuilt as a generated model with an organic scute look (an overgrown morphling over an insect; the noisy red and black paint was rejected). Vesper's own rider model and the Evening Star geometry are unchanged.

Mycophant (user):
- A genderless brood-mother: motherly, never sexual, never feminine; assimilation is biological reproduction only.
- Dragged by 4 crab claws, no walking legs ("spider-like" rejected). Big forms only; plating, seams, veins and fungus are texture work.
- Opening: the player wakes dazed and bound, the bindings snap, she rises facing away cradling a fungling, the rushing embrace is the first attack.
- Phase 2 at 60% (CR-15, 2026-11-13; it was 50%): the skull cracks and an Ebrietas pulp face shows, wings sprout and are used offensively and defensively, never for flight.
- The Apotheos wins by surviving assimilation; the ending is a yielding with the Tendril offered.
- Damage lands at full rate in both phases; broken fusions add a bonus chunk. Casts break a hold only when they hit her or are cast while held.
- The user found the first fight "2 things on repeat" and approved every proposed move of the 2026-10-24 expansion, with Ebrietas and FromSoftware bosses as the reference.

Presentation (user): the Mycophant's proto waking is the model for the shared system and the first scene moved onto it. Everything else in that spec was built on "go ahead based on your guess" (2026-10-24) and kept by the owner on 2026-11-13 (RV-18, below).

Ruled 2026-11-13 (owner's answer sheet, chunk 07 of `owner_answers/`):
- G-2c, CQ-12 (2026-11-13): one boss definition, and every reader uses it. The entity type tag `hemomancy:bosses` decides and `HemoEntityPredicates.isBoss` is its only reader (`BossDefinitionGuardrailTest`). It holds the Ender Dragon, Wither, Warden, Elder Guardian, `#c:bosses`, the 16 living bodies under `common/entity/boss/` and the Circus Ringmaster (D-33, added 2026-11-15). Rite sacrifice exclusion and `puppet_attention_immune` read it; kill blood gain x5 is the one named exception (NQ-07-3).
- DS-9 (2026-11-13, built 2026-11-16, B34): Vesper, the Mycophant and Naeglerophaeon have Liber pages in Cosmic Forces, and the Bloom Warden and Kindled Champion in the new Brined Court chapter (NQ-16-3 A). Each boss page, the Phlegethosoma's included, opens for the player when that fight begins (`LiberKnowledgeHelper.unlockForEncounter` at the Vesper arrival, the Nursery entry, the Naeglerophaeon opening, the Phlegethosoma's summons, the Warden's waking and the Champion's call; Annetta's page keeps its route-start opener).
- CR-15 (2026-11-13): the Mycophant splits into phase 2 at 60% health (`MycophantCombatRules.PHASE_TWO_FRACTION`; the bar notch reads it). On 720 health the bands are 288 / 252 / 144 / 36 (the 144 is the Last Brood band under 25%).
- CR-19 (2026-11-13): the Crowned Refusal's wounds open at 70%, 40% and 10% (`VesperCombatRules.anchorThresholds`; the bar notches read them). On 520 health: 156 / 156 / 156, then 52.
- CR-28 (2026-11-13): the grounded burrow stays in the rotation as built. Its health gate moved with the first wound, to 68%, so it still opens 2% under it.
- CR-20 (2026-11-13): the Kindled Champion regrows 2 / 3 / 3 health a second by crown stage (it was 4.5 at the last stage; `BRINED_COURT.md`).
- CR-22 (2026-11-13): Vesper's 180 experience comes through the ordeal's victory on every completed ordeal; the Evening Star's loot table is gone (moved to `removed_assets/`) and its death gives none. The Saints' experience comes through `endTrial` (see `SAINT_MAUSOLEUMS.md`).
- PT-81 (2026-11-13): a Mycophant win is recorded by owner UUID at the killing blow (`MycophantWinSavedData`) and settled at the next login; a death in the yield settles it at once.
- PT-84 (2026-11-13): after a lost rematch the hunt comes back by itself once the retry cooldown has run, as the expelled line promises. Built 2026-11-15 (B15): `failAttempt` sets a server-only "rematch owed" flag on the degree attachment (`mycophant_rematch_owed`, not synced), `MycophantEncounterRules.shouldRearmRematch` starts the claim as a rematch once the cooldown has run in the Gardens, and the win clears the flag.
- NQ-07-4 (2026-11-14): every lost rematch re-arms after the cooldown, until the player wins. Built (B15).
- NQ-07-1 (2026-11-14): Annetta's max health is 51, the 50 damage the break needs plus the 1 HP guard. Raise it if the wait at 1 HP reads as a stuck boss. Built (B15, `AnnettaKnowlesEntity.setAttributes`).
- NQ-07-2 (2026-11-14): the Seraphae Fragment and the Velorum Devotee stay in the boss tag. Nothing changes for them.
- NQ-07-3 (2026-11-14): rite sacrifice exclusion (`HarbingerCardinalRiteEvents.processSacrifices`) and Scarlet Mummer attention immunity (`puppet_attention_immune` is `#hemomancy:bosses`) read the boss tag. Kill blood gain x5 stays Dragon and Wither only, the one named exception in `BossDefinitionGuardrailTest`. Built (B15).
- D-33, boss tag row (2026-11-14): the Circus Ringmaster is in `hemomancy:bosses`, so every boss cap and immunity covers him; Thread Ripper no longer names him apart. Built (B15).
- V-6b (2026-11-13): the Mycophant claim's spores are green (122, 184, 87) and amber (235, 168, 61) on the 0 to 255 scale.
- CR-27 (2026-11-13): title cards sit on the lower third, the epithet above the name (`TitleCard.java`).
- RV-18 (2026-11-13): boss presentation stays as built. That covers the shared director tracks (letterbox, fade, HUD hide, input lock, title card, camera shot, overlay, tick), with the input lock held while the server still binds the player; skip by holding sneak 20 ticks (`BossSceneDirector.SKIP_TICKS`); bars hidden under the opening; bar frames tied to the bar UUID, a 182 px fill in a roughly 206x22 frame, the chip lit 16 ticks then draining, a 20-tick reveal; every per-boss frame motif, notch set and phase reaction (the table in the 2026-10-24 spec); every opening's shot and length (Annetta 60 ticks, Priestess 90, Latent 34, Naeglerophaeon 70, Crowned Refusal 80, Evening Star awakening 72, Saints 60, Mycophant rematch 61); phase cards of 50 ticks with no camera (Annetta break, Mycophant split, Naeglerophaeon transition, Vesper cocoon, Hemorath empties); the server intro windows for Annetta, Naeglerophaeon and the Crowned Refusal and the Evening Star's `beginAwakening`; and the title card wording: "Mother of Every Strain", "Last of the Broken Church", "What Wears Annetta", "Torn Out of Annetta", "Predator of the Vagrant Mind", "Unbecoming", "Breaking", "Conducting".
- RV-21 (2026-11-13): boss fight numbers stay as built, except where CR-15 and CR-19 changed one. Vesper: the wound sites (carapace behind the throne, throat seam, under the tail base), wounds that open by themselves at their thresholds, hits that drain at 10% as a fallback, scabbing that floors at half, and the Sickle act at 33% (moved from 30%). Mycophant: the band numbers, the 8% fusion bonus and the raised Nursery ceiling. Annetta: `AnnettaPoolEntity` with its three pool kinds and the 1 HP early-death guard.
- RV-82 (2026-11-13): the Broken Church spawns in swamp and dark forest, and thrall or puppet kills credit the owner.
- RV-84 (2026-11-13): Vesper keeps his own manipulation caster, the mount's head and neck, and summoned Vespers that burrow with particles only.
- RV-85 (2026-11-13): the Mycophant is she/her in player text, rendered at 2x with a 4.5 x 5 hitbox, brood funglings capped at 4, with the custom Pulp Bloom screen effect.
- CR-29 (2026-11-13): the Mycophant's brood clutch (funglings armouring her sac, expansion spec §14) and the amber failure beat before expulsion (rebuild spec §6.6) stay deferred. Neither is built.
- CR-21 (2026-11-13): the Bloom Warden out-healing a D0 to D2 player is intended; the answer is amends (`BRINED_COURT.md`).
- C-7 (2026-11-13): the Annetta encounter is promoted from Partial to implemented. CR-16 lowered her health to 51 (NQ-07-1, built 2026-11-15). The lancea lore line is FX-21's.

Ruled 2026-11-13 in other chunks (the plans are chunks 04, 06 and 15 of `owner_answers/`):
- LP-16 (2026-11-13): a boss takes a fixed number of successful Blood Bindings, set by its max health (1,000 HP gives 10). Each attempt can also fail, by a chance that grows with max health and with how healthy the boss is. A failed attempt does not count. Built 2026-11-14 (B04): `BossBindingRules`, `BossBindings` and `BloodBindingManip.applyBinding`, for the R cast and the held form alike.
- NQ-04-2 (2026-11-14): a boss takes max HP / 100 bindings (at least 1: the Wither 3, the Warden 5), one count per boss shared by every player, saved on the boss entity. It never comes back for that body's life; a Saint that returns to its tomb wakes as a new body with a fresh count. Built (B04).
- NQ-04-3 (2026-11-14): an attempt slips by max HP / 2,000, held between 10% and 60%, times (0.5 + 0.5 x health fraction). A 1,000 HP boss at full health slips half the time. A slip costs the blood and starts the R cooldown; the held form waits 60 ticks before trying that boss again. Built (B04).
- NQ-04-1, NQ-04-4 (accepted 2026-11-14): one binding is one hold of the boss length (60 ticks; Lingering 120, Chain 40, Lattice 80); it drops when it ends, whatever the player holds. The constricted slow and the Animus brand land only with a binding that held. Built (B04).
- CR-7 (2026-11-13): the Phlegethosoma stops spawning naturally. A player summons it by projecting 500 mL of blood into the basin's ichor. Built 2026-11-15 (B14): the natural spawn is gone; `PhlegethosomaSummoning` is the Projection chain's ichor link, ahead of chum, and pays through `BloodAccounting` (`phlegethosoma_summon`). It needs the Nether's basin biome, a 7 by 7 pool and a world that is not Peaceful, and refuses while another lives within 64 blocks.
- NQ-06-7 (2026-11-14): the summon counts blood per ichor pool up to 500 mL, at 2 mL a tick, and the count fades like chum (half in 45 seconds). One pour takes about 14 seconds. Built (B14, `PhlegethosomaSummonRules`, `PhlegethosomaSummons`).
- NQ-06-8 (2026-11-14): with no target for 60 seconds a summons dives and is gone 2 seconds later, with no loot or experience, so it never blocks the pool. A player has one summons at a time; an older one still out dives when its caller calls another. Built (B14).
- NQ-06-10 (2026-11-14): any player with awake blood may summon it. The Alchemist's D5 basin commission line `hemomancy.alchemist.phlegethontic_commission.phlegethosoma` and the Liber page `cosmic_forces/pages/phlegethosoma` (opens at D5) teach it. Built (B14).
- D-33 (2026-11-13): the Circus Ringmaster moves onto the generic boss kit. Built 2026-11-15 (B31, chunk 15 T13).
- NQ-15-4 A (2026-11-14): on the kit he fights with five mental moves (the Spotlight, Sleight, the Cue, the Knife Board, Strings Cut) beside his knives and staff. Built 2026-11-15 (B31, chunk 15 T15).
- NQ-15-3 A (2026-11-14): the Circus mini-boss ladder, the snake creature and the staff form stay struck; acts 2 to 4 stay timed acts.
- D-36 (2026-11-13): Succession ends with a yield beat and the Ringmaster gets a music event (no track yet). Built 2026-11-15 (B31, chunk 15 T14).

Answered 2026-11-06:
- D-16: the Mycophant's pronouns are she/they. Player text may use either; canon keeps the brood-mother genderless.
- D-7: there is no Core. The Mycophant is fought and found in the Nursery, and `mycophant_heir` is the record written when she/they are beaten (`MycophantEncounterManager` writes it on every win; nothing has to read it).
- D-17: Hemorath's epithet is "The Hollow Vessel"; "The First Saint" is retired.
- D-35 (2026-11-13): Thread Ripper does not work on Vesper's encounter bodies. `ThreadRipperManip.protectedBody` reads `VesperEncounterPuppetEvents.isEncounterPuppet`, and the refusal has its own line, `message.hemomancy.thread_ripper.vesper_strings` (built 2026-11-14).

## Calls made without the user

The owner kept the presentation calls and the named fight numbers on 2026-11-13 (RV-18, RV-21, RV-84, RV-85, CR-27); those moved to "Rulings that bind". What is left here was never put to him.

Annetta (Claude's, in the specs): the Priestess spawns face-down and emerges invulnerable; the Latent keeps two claw arms and a spear where the reference showed one claw; hitbox numbers (Priestess 0.9x2.2, Latent 1.6x4.5); all dialogue lines; Phase 1 equips `annettas_absolution_dagger`.

Vesper (Claude's, spec "Calls I made"): draining a wound feeds the player blood; a puppet kill tears a quarter; muster prefers uncalled shapes; sigil flares at awakening; the whole string roster, shield rip, tether break and once-only Harvest.

Mycophant (Claude's, spec §14 and expansion §14): the translucent sac layer; a central nectar pool; brood funglings are flagged `FunglingEntity`; the 80% Wing Guard reduction; filled meters never lethal; band and sector thresholds, weights and kite-pressure numbers; shorter holds (5 s and 4 s); Claw Rake became the Rake String; grown brood is a scaled fungling; brood tumors are blocks placed only in owned fights; the Last Brood numbers (once below 25%, half damage, 5% floor, anchors 6 to 9 blocks out).

Overnight build, 2026-11-14 (P02; see `owner_answers/BUILD_LOG_2026-11-14.md`, "For the owner"):
- The boss tag was built with exactly the old membership. The Seraphae Fragment and the Velorum Devotee are in it. (B15 then added the Ringmaster and folded in rite sacrifices and `puppet_attention_immune`.)
- The old checks also caught other mods' classes in an `.entity.boss.` package and subclasses of the vanilla bosses. Those now need the `c:bosses` tag.
- The burrow gate is the first wound minus 2% (68%), and the idle threshold reads the second wound (40%).
- Vesper's 180 experience is given on every completed ordeal, not only the first. A Saint's experience goes once to the loot's recipient; an abandoned trial gives none and a forced-door win gives all of it.
- A Mycophant win is recorded only when the owner is online and in the fight at the killing blow. It is kept in server saved data (`hemomancy_mycophant_wins`), not on the player. A dimension change in the yield settles it too.
- A death in the yield leaves the winner at 35% health with effects cleared, the same recovery an expulsion gives.
- An owned Mycophant taken by `/kill` still counts as a win, as before.

B15, 2026-11-15:
- The rematch owed flag is written to the save only while it is set, so saves and fingerprints of players who never lost a rematch do not change.
- A re-armed rematch skips the hunt's whispers and visions: the claim starts on the first Gardens tick after the cooldown, as a rematch (the short opening).
- With 51 health Annetta's bar empties after about 50 damage, and she then stands at 1 HP until two minutes have passed.

Planned defaults, 2026-11-14 triage. Each will be built as written unless the owner objects:
- NQ-04-1 (LP-16): one binding on a boss is one hold of the boss length (60 ticks; Lingering 120, Chain 40, Lattice 80). It then drops, and the next hold is a new attempt.
- NQ-04-4 (LP-16): the constricted slow and the Animus brand land on a boss only with a successful binding.
- 04-T6-store (LP-16): the count of successful bindings is saved on the boss entity, with two action-bar lines as `en_us.json` keys.
- UN-16 (built in B35, 2026-11-16): the split plays `entity.mycophant.split` (was `entity.mycophant.teleport`), and the death file is cut to 3.6 s so that at its pitch of 0.6 it ends with the 6 s yield scene (the untrimmed file is in `removed_assets/`). See [SOUND.md](SOUND.md).

## Open questions

Open questions live in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md). UN-14 (Putriciel's body sound) is answered A: no voice, non-vocal sounds of his own; its files wait on Sound S5. The ruling is in [SOUND.md](SOUND.md).

## Proof owed and known gaps

Owed by the owner (play, watch, tune):
- PR-9: the stills pass over every bar frame, opening and phase card, the Mycophant waking and capture, and the Saint rise. Captured 2026-11-15 (B15) in `build/build2/B15-shots/` with an `index.txt`: 53 presentation stills and 303 Mycophant stills. The moved bar notches (60%, and 70/40/10) show in them. The Mycophant review's hold probe logged one FAIL, "last brood cut free"; B15 did not touch the Last Brood.
- Play the Mycophant end to end. The "2 things on repeat" complaint is the last recorded play, it predates the 2026-10-24 expansion, and with the split at 60% (CR-15) phase 2 is now the longer half.
- Call a Phlegethosoma in a Basin (CR-7): pour 500 mL into a broad ichor pool, watch it rise and fight it; then walk away and see it dive after a minute.
- Play the Crowned Refusal once: the grounded burrow should first show just after the first wound opens (CR-19, CR-28).
- Tune in play: Kindled Champion regrowth 2 / 3 / 3, Vesper experience 180, Saint experience 80 and 120, and Annetta's health of 51 in a timed fight (CR-16).
- Watch three things never verified by reading: bars hidden under every opening (RV-18), summoned Vespers burrowing with particles only (RV-84), the Pulp Bloom screen effect (RV-85).
- In the Seraphae and Velorum fights, judge whether adds that shrug off Binding and Paralysis feel right (NQ-07-2).

Not built yet:
- The Ringmaster's finale in his Chamber (2026-11-18) is built and filmed (`runCircusReviewClient -PcircusReviewOnly=finale,finale_kill,finale_absorb`) but not yet played by the owner; the play-and-tune row is in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md).

Known gaps:
- The burrow gate (68%) and the idle threshold (second wound) are `VesperCombatRules.mayGroundBurrow` and `groundedIdleTicks`, pinned in `VesperCombatRulesTest` (B15).
- The Vesper experience (CR-22) is proved by a source assertion only (`EndgameBossRewardResourceTest`); no GameTest runs a whole ordeal victory. The GameTest servers load no Chamber of Will, and `completeVictory` refuses an owner outside it, so a live test needs a Chamber in the GameTest server or a split of the guarded method (07 T4 test, skipped in B15).
- Sound: the Stained Priestess's plunge now sounds only the first and last thorn of its line (UN-17, B35); the plummet's nine thorns still all sound at once.
- Sound: the Bloom Warden and the Kindled Champion play no sounds at all (C-9). Tracked with the Brined Court (`BRINED_COURT.md`); it waits on the sound pass.
- `AGENTS.md` has no line for the boss definition; the rule is in `architecture/ARCHITECTURE_STANDARDS.md` only.
- Shader and tendril work (Priestess wings, Vesper wound skin `entity/vesper_wound_draw`, Mycophant glow and sac layers) needs a client launch to prove compilation; offline validators miss driver errors (`AGENTS.md`).
- Vesper review: `runVesperReviewClient` then open the `VesperReview` save. Loaded GameTests cover ordeal recovery; mid-burrow and mid-wound reload fixtures were asked for in the spec §7.
- The Saint combat kit is proven only for the four Saints: `saint_kit_validation` and the four fight suites pass, and the fight review clients ran every move; nothing has been played. Two kit fixes found at verification change every Saint: the damage-taken factor now reaches health, and `BossHold` finds players added to the level alone. See the Saint doc.

## History

| Date | Event | Source |
|---|---|---|
| 2026-10-15 | Annetta models design: five looks, generator pipeline, no GeckoLib | `superpowers/specs/2026-10-15-annetta-encounter-models-design.md` |
| 2026-10-16 | Priestess tendril form replaces box wings; Annetta combat and choreography for both routes | `superpowers/specs/2026-10-16-stained-priestess-tendril-form-design.md`, `2026-10-16-annetta-encounter-combat-design.md` |
| 2026-10-16 | Vesper fight overhaul design: controller, burrow, wound, roster, acts, mount rebuild, Java animation ruling | `superpowers/specs/2026-10-16-vesper-fight-overhaul-design.md` |
| 2026-10-17 | Texel density ruling: 16 px per block; Vesper mount and Annetta models brought to it | memory `feedback_texel_density` |
| 2026-10-18 | Mycophant Spawn Mother rebuild approved and built; Annetta and Vesper passes finished | `superpowers/specs/2026-10-18-mycophant-spawn-mother-design.md` |
| 2026-10-21 | Generators version-controlled under `tools/model_export/` | commit c435034d4 |
| 2026-10-23 | `BossReplayClock` and `BossMoveTimeline` consolidated as the shared clock | commit 9136d673d, `architecture/ARCHITECTURE_STANDARDS.md` |
| 2026-10-24 | Mycophant moveset expansion approved ("They all look good, let's start on them"); boss presentation design built on a guess | `superpowers/specs/2026-10-24-mycophant-moveset-expansion-design.md`, `2026-10-24-boss-presentation-design.md` |
| 2026-10-26 | Boss presentation, Mycophant combat and Saints committed; Phlegethosoma Nether miniboss on the same plumbing | commits 71f06de03, 3b67b0834 |
| 2026-10-29 to 10-30 | Scene catalog and director updates with the endgame, heart-node and Brined Court commits; Court minibosses added | commits 2ff58cc50, 185aa3502, 62f5b38da |
| 2026-11-05 to 11-06 | The Saint combat kit and the four Saint overhauls designed, built and verified (committed in `93070b745`) | `superpowers/specs/2026-11-05-saint-overhauls-design.md` |
| 2026-11-06 | Visual pass 2: each Saint's own telegraph pass and shaders over the kit, the animations rebuilt, the trench lanes, five polish rounds per Saint (committed in `93070b745`) | the four Saint designs' "Visual pass 2" sections |
| 2026-11-01 | This page. `git log -- common/entity/boss` shows the latest touch is 62f5b38da; the boss, cinematic and frame paths carry no uncommitted changes | `git status` |
| 2026-11-13 | The owner answered the question sheet. Chunk 07 review: 19 rows, eight tasks, four new questions. Rulings recorded above | `owner_answers/07-bosses.md` |
| 2026-11-14 | Overnight build, package P02 (uncommitted): `hemomancy:bosses` and `HemoEntityPredicates.isBoss`; Mycophant split 60%, Crowned Refusal wounds 70/40/10, Champion regrowth 2/3/3; spore colours; Vesper and Saint experience through the victory; a Mycophant win survives a logout or death in the yield | [owner_answers/BUILD_LOG_2026-11-14.md](owner_answers/BUILD_LOG_2026-11-14.md), `owner_answers/07-bosses.md` |
| 2026-11-14 | Open questions consolidated into `OPEN_QUESTIONS.md`: one question left here (NQ-07-1), the kept calls moved to "Rulings that bind", planned defaults listed for NQ-07-2 to NQ-07-4 | [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| 2026-11-15 | Package B31 (uncommitted): the Circus Ringmaster on the kit's generic half, his five mental moves, the yield and collapse beats, the Circus sound and music events | [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md) |
| 2026-11-15 | Package B15 (uncommitted): Annetta's health 51; the Ringmaster, rite sacrifices and Mummer attention on the boss tag; the lost rematch re-arms; burrow gate and idle threshold moved into `VesperCombatRules` | [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md) |
| 2026-11-14 | Second docs pass: rulings other chunks hold about bosses recorded here (LP-16 boss bindings, CR-7 the Phlegethosoma summon, D-33 the Ringmaster on the kit), with their questions in the pointer and two sound gaps noted | `owner_answers/04-cast-economy-and-temper.md`, `owner_answers/06-creatures-and-spawns.md`, `owner_answers/15-circus.md` |

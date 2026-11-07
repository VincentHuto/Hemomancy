# Bosses and Encounters

Status (2026-11-06): Annetta, Vesper, the Mycophant, the Naeglerophaeon opening and the shared presentation are built and committed (see History). The Saint combat kit, shared plumbing for the four Saint fights, was built on 2026-11-05 and is uncommitted. Every presentation specific is still unreviewed by the user.

## What it is

Hemomancy's authored fights and the plumbing they share. This page covers the non-Saint encounters and the presentation layer; the four Saints are in `docs/SAINT_MAUSOLEUMS.md` and the Brined Court minibosses in `docs/BRINED_COURT.md` (both being written separately).

| Encounter | Entry | Shape | Source |
|---|---|---|---|
| Annetta Knowles (Broken Church) | dialogue with a Tooth Pecks jar (Harbinger) or a Draught of Still Mercy (Unstained, needs Clarity) | two routes: Phase 1 and the break into the Stained Priestess, or the purge into the Latent Infection with Annetta as an ally | `HEMOMANCY_REFERENCE.md` §entity table (Annetta rows), `LORE_REFERENCE.md` §11 |
| Vesper, the Crowned Refusal then the Evening Star | the Living Sickle cut of the owned Bloom, owner-bound ordeal | phase 1 on the mount (ground moves, flight from 50%, burrow, three wounds, puppet roster); phase 2 in three acts | `HEMOMANCY_REFERENCE.md` §5.10 |
| The Mycophant (Spawn Mother) | the 15-minute Gardens hunt or a Fruiting Lure, into the Nursery | the waking, the claim (100 to 50%), the split, the joining, the Last Brood below 25%, the yielding | `HEMOMANCY_REFERENCE.md` §Mycophant Hunt and Morphic Nursery |
| Naeglerophaeon | breaking a Vagrant Mind's Dormant Ganglion | existing miniboss; this round only gave it a 70-tick opening | `HEMOMANCY_REFERENCE.md` §Naeglerophaeon |
| Bloom Warden, Kindled Champion | Brined Court transgression / Head-tender challenge | Court minibosses on the shared plumbing | `docs/BRINED_COURT.md` |
| Saints (Hemorath, Putriciel, Velorum, Seraphae) | mausoleum trials | the Saint combat kit (below); the `saint_rise` opening, one frame and one meter style each | `docs/SAINT_MAUSOLEUMS.md`, `HEMOMANCY_REFERENCE.md` §5.8 |

**Annetta (three passes, 2026-10-15 to 10-18).** One generated rig carries four looks chosen by `AnnettaLooks`: cowering and Phase 1 (long red hair, gold eyes, the only tells on an otherwise human Unstained cleric; Phase 1 adds her dagger, the cracked Unstained Shield and the neck tick that swells as her health falls) and the purged looks (short white pixie-bob, blue eyes, dagger only, a healing pose). The break needs both 2 minutes survived and 50 damage; she stabs the tick, loses her head, falls, and a reanimation pool raises her as the Stained Priestess, who flies (EMERGING, TAKEOFF, HOVER, SWOOP, PLUMMET, LANDED in `PriestessCombatRules`) with procedural tendril wings and a three-eyed tendril head (`PriestessTendrilRules`, `StainedPriestessTendrilLayer`). On the Unstained route the Draught tears the Latent Infection out of her: gaunt, bone cowl, toothed chest maw around a red core, coiled vessel whip arm, two clawed arms on a lance, serpent tail, black hooked claws (`LatentCombatRules`: claw rake, spear thrust, whip lash, infection bloom, root eruption; it sometimes turns on Annetta). Early death is guarded: she sits at 1 HP until both break conditions are met.

**Vesper (overhaul 2026-10-16 to 10-18).** Vesper is male; he/him everywhere. `VesperMountController` owns the mount's locomotion, facing and move timeline; the AI is never switched off and `NoAI` is cleared from old saves. Burrow (`VesperBurrowRules`, `VesperBurrow`): he grips the mount's nape, it plunges, a fissure trail hunts 10 to 15 seconds with alternating claw and tail strikes, a rumble, an eruption at a point locked partway through the rumble, then about 3.5 seconds of recovery; every second Diving Rend becomes a 5 to 7 second burrow. Wounds at 72/42/12% open on their own (`VesperWoundRules`), are drained by holding Blood Absorption (`VesperBloodAbsorptionInteractions`), scab back only to half, and the mount fights back while wounded. Roster (`VesperPuppetRoster`): seven shapes, Ringmaster excluded, up to three out, killed shapes struck for good, survivors recallable; every unstruck shape lends the Evening Star one borrowed attack (`VesperBorrowedAttacks`). Phase 2 (`VesperEveningStarController`, `VesperEveningStarAct`): Locked (100 to 67%), Open (67 to 33%, cross-school synergy chains), Sickles (33 to 0, strings in `VesperSickleRules`, Harvest once below 10%). `VesperManipulationCaster` casts any fight manipulation; the coverage table lives in the reference.

**The Mycophant (rebuilt 2026-10-18, moveset expanded 2026-10-24).** A genderless brood-mother dragged on four crab claws, with a grasping hand, a feeding siphon, a slim beaked skull in a frilled collar and a gestation sac. The waking (`MycophantIntroRules`, 190 ticks): the player wakes bound, the fibers snap, they rise facing away cradling a fungling, drop it, roar at 150 (bar, music and vulnerability start) and rush at 190 with the embrace as the first attack. Position-aware picking (`MycophantSelectionRules`, `MycophantPositionRules`: bands HUG/NEAR/MID/FAR, sectors FRONT/FLANK/REAR, anti-repeat, kite and hug pressure, follow-ups two deep). At 50% the split: the skull cracks, the Ebrietas pulp face blooms, hyphae wings sprout (never for flight). Phase 2 adds the Fusion Embrace, Wing Guard with a riposte, Wing Sweep, Pulp Bloom, Hyphae Lances, Lattice Net and the Gaze. The Last Brood runs once below 25%. The yielding offers the Tendril; the Apotheos wins by surviving assimilation, not by killing a villain.

**Saint combat kit (2026-11-05).** Shared plumbing for the four Saint fights, shaped so the other bosses could move onto it later (G-2c, one boss definition, stays pending; none has moved). The generic half lives in `common/entity/boss/`: `BossFootprint` (sealed ground shapes with height bands that the server tests and the client draws with the same geometry, `BossFootprints` and `FootprintFrame` for their saved frame), `BossBands` (range bands HUG under 3, NEAR under 6.5, MID under 12, and FRONT/FLANK/REAR sectors), `BossHold` with `BossHoldCarrier` (a player held at a point on a boss's body, released by named reasons, refused for a `HeldTraveller`), and `BossGroundHazardEntity` (a lingering footprint its owner ticks). The Saint half in `common/entity/boss/saint/` is the move table (`SaintMove`, `SaintMoves`), the picker (`SaintPickRules`: weights by band and sector, anti-repeat, 200-tick pressure, follow-ups two deep), `SaintCombatController` (the Mycophant controller's model: roam, pick, wind-up that tracks, commit that locks the aim, active, recovery; interrupts queue to the recovery boundary), `SaintMeter` with `SaintMeterSync` and the one new payload `SaintMeterPacket`, `SaintPresence`, `SaintOpenings`, `SaintPhaseTable` (phase cards through `BossScenes`), `SaintArena` (the only Saint block editor, restoring on every exit), `SaintDeeds`, `SaintChannelRouter` (held Blood Absorption and Projection into a fight), `SaintEnding` and the damage pipeline in `SaintTrialEntity`. Client: `SaintMeterLayer` (one HUD layer) and `SaintTelegraphRenderer` (one world pass), with each Saint's styles listed in `client/registry/SaintClientStyles`. Timing still runs on `BossReplayClock` and `BossMoveTimeline`. Spec: `superpowers/specs/2026-11-05-saint-overhauls-design.md` (Kit API).

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
| Guardrails | `src/test/java/.../architecture/BossAnimationClockGuardrailTest`, `BossFrameworkGuardrailTest`, `BossPresentationBindingTest`, `SaintCombatKitGuardrailTest` |
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
- Phase 2 at 50%: the skull cracks and an Ebrietas pulp face shows, wings sprout and are used offensively and defensively, never for flight.
- The Apotheos wins by surviving assimilation; the ending is a yielding with the Tendril offered.
- Damage lands at full rate in both phases; broken fusions add a bonus chunk. Casts break a hold only when they hit her or are cast while held.
- The user found the first fight "2 things on repeat" and approved every proposed move of the 2026-10-24 expansion, with Ebrietas and FromSoftware bosses as the reference.

Presentation (user): the Mycophant's proto waking is the model for the shared system and the first scene moved onto it. Everything else in that spec was built on "go ahead based on your guess" (2026-10-24).

Answered 2026-11-06:
- D-16: the Mycophant's pronouns are she/they. Player text may use either; canon keeps the brood-mother genderless.
- D-7: there is no Core. The Mycophant is fought and found in the Nursery, and `mycophant_heir` is the record written when she/they are beaten (`MycophantEncounterManager` writes it on every win; nothing has to read it).
- D-17: Hemorath's epithet is "The Hollow Vessel"; "The First Saint" is retired.

## Calls made without the user

Presentation (all of spec 2026-10-24, unreviewed):
- The shared director tracks (letterbox, fade, HUD hide, input lock, title card, camera shot, overlay, tick); the title card on the lower third with epithet above the name (`TitleCard.java`); skip by holding sneak 20 ticks (`BossSceneDirector.SKIP_TICKS`); the input lock stays while the server still binds the player.
- Bars hidden under the opening; bar frames tied to the bar UUID; the 182 px fill in a roughly 206x22 frame; chip lit 16 ticks then draining; 20-tick reveal.
- Every per-boss frame motif, notch set and phase reaction (the table in the spec), and every opening's shot and length: Annetta 60 ticks (`HARBINGER_INTRO_TICKS`), Priestess 90, Latent 34, Naeglerophaeon 70 (`INTRO_TICKS`), Crowned Refusal 80, Evening Star awakening 72, Saints 60, Mycophant rematch 61. Phase cards (50 ticks, no camera, not skippable as a camera scene): Annetta break, Mycophant split, Naeglerophaeon transition, Vesper cocoon, Hemorath empties.
- Title card wording: "Mother of Every Strain", "Last of the Broken Church", "What Wears Annetta", "Torn Out of Annetta", "Predator of the Vagrant Mind", "Unbecoming", "Breaking", "Conducting". Hemorath's epithet is "The Hollow Vessel" (D-17, 2026-11-06); "The First Saint" is retired.
- New server intro windows for Annetta, Naeglerophaeon and the Crowned Refusal, and wiring the Evening Star's `beginAwakening` (previously never called).

Annetta (Claude's, in the specs): `AnnettaPoolEntity` with three pool kinds; the 1 HP early-death guard; Priestess spawns face-down and emerges invulnerable; the Latent keeps two claw arms and a spear where the reference showed one claw; hitbox numbers (Priestess 0.9x2.2, Latent 1.6x4.5); all dialogue lines; Phase 1 equips `annettas_absolution_dagger`.

Vesper (Claude's, spec "Calls I made"): wound sites (carapace behind the throne, throat seam, under the tail base); wounds open by themselves at threshold; hits drain at 10% as a fallback; draining feeds the player blood; scabbing floors at half; a puppet kill tears a quarter; muster prefers uncalled shapes; sigil flares at awakening; the Sickle act moved from 30% to 33%; the whole string roster, shield rip, tether break and once-only Harvest; Vesper gets his own manipulation caster; the mount gained a head and neck; summoned Vespers burrow with particles only.

Mycophant (Claude's, spec §14 and expansion §14): she/her in player-facing text (she/they ruled 2026-11-06, D-16); 2x render scale and the 4.5x5 hitbox; the translucent sac layer; a central nectar pool; rematches get a short opening; brood funglings as flagged `FunglingEntity` capped at 4; 8% fusion bonus and 80% Wing Guard reduction; custom Pulp Bloom screen effect; Nursery ceiling raised; filled meters never lethal; band and sector thresholds, weights and kite-pressure numbers; shorter holds (5 s and 4 s); Claw Rake became the Rake String; grown brood is a scaled fungling; brood tumors are blocks placed only in owned fights; the Last Brood numbers (once below 25%, half damage, 5% floor, anchors 6 to 9 blocks out); the brood clutch left for later.

## Open questions

- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-81 [D8-2], D-84 [D8-5].
- Title card placement: the code and spec put the card on the lower third. Confirm this is what the user wants before any frame art is revisited.
- Vesper spec §4.4 left the Ground Burrow gate (once wings exist or from about 70% health) to the builder; the reference records only the Diving Rend entry ("every second Diving Rend"). Is a grounded burrow in the rotation?
- Mycophant brood clutch (funglings armouring her sac) is deferred (expansion spec §14).
- Mycophant failure beat (amber screen before expulsion) was optional and is not recorded as built (rebuild spec §6.6).
- Lore says the Priestess's thrown projectile still shares the standard blood-lance path (`LORE_REFERENCE.md` §11 "Remaining WIP"); the reference describes a `StainedLanceEntity`. One of the two docs is stale.

## Proof owed and known gaps

- Every presentation specific is unreviewed in game by the user: frames, shots, cards, skip. Proof is `./gradlew.bat runBossPresentationReviewClient` screenshots at the card and bar-reveal beats.
- Shader and tendril work (Priestess wings, Vesper wound skin `entity/vesper_wound_draw`, Mycophant glow and sac layers) needs a client launch to prove compilation; offline validators miss driver errors (`AGENTS.md`).
- `HEMOMANCY_REFERENCE.md` still lists the Annetta encounter as Partial, with "Final animation polish" and in-church balance passes outstanding (§status, `LORE_REFERENCE.md` §11).
- The Mycophant's full fight has not been played end to end by the user since the 2026-10-24 expansion; the "2 things on repeat" complaint is the last recorded play.
- Vesper review: `runVesperReviewClient` then open the `VesperReview` save. Loaded GameTests cover ordeal recovery; mid-burrow and mid-wound reload fixtures were asked for in the spec §7.
- The Saint combat kit is proven only for the four Saints: `saint_kit_validation` (8) and the four fight suites pass, and the fight review clients ran every move; nothing has been played. Two kit fixes found at verification change every Saint: the damage-taken factor now reaches health, and `BossHold` finds players added to the level alone. See the Saint doc.

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
| 2026-11-05 to 11-06 | The Saint combat kit and the four Saint overhauls designed, built and verified (uncommitted) | `superpowers/specs/2026-11-05-saint-overhauls-design.md` |
| 2026-11-06 | Visual pass 2: each Saint's own telegraph pass and shaders over the kit, the animations rebuilt, the trench lanes, five polish rounds per Saint (uncommitted) | the four Saint designs' "Visual pass 2" sections |
| 2026-11-01 | This page. `git log -- common/entity/boss` shows the latest touch is 62f5b38da; the boss, cinematic and frame paths carry no uncommitted changes | `git status` |

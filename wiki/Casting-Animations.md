# Casting animations

A hemomancer forces the blood to remember an instruction, and the body performs that memory. An Unstained settles the body so still that the world's infection has nothing to hold. Casting presentation shows both: every Harbinger manipulation plays an authored first- and third-person track chosen by school, rank, cast type and purpose, and every Still Art plays an Unstained track chosen by clarity stage and ending.

Presentation never touches gameplay. Costs, cooldowns, charge clocks, targeting, hit windows and movement stay where they are; quick casts execute before their animation starts, and charged and continuous timing is the server's. Presentation metadata is resolved from registry identity and is never written into saved manipulation data.

The design contract is [the v2 spec](../docs/superpowers/specs/2026-10-19-casting-animations-v2-design.md). This page describes what was built, including the rulings made during the build where they differ from the spec. The [concept package](../docs/done/HEMOMANCY_CASTING_ANIMATION_CONCEPTS/README.md) remains the material and silhouette reference for each tendency.

## The grammar

Six layers, applied in order. Each owns specific parts so they never fight.

| # | Layer | Owns | Source |
|---|---|---|---|
| 1 | Base track | Torso, support arm, legs, and the default lead arm and head | Harbinger: school and rank. Unstained: clarity stage |
| 2 | Cast type | Which segments play and how HOLD behaves | Quick, charged, continuous or passive |
| 3 | Rank or stage scope | How much of the body takes part | Humilis and Awakened move the lead arm only; Summa and above add the waist and stance |
| 4 | Aim layer | Lead arm and head from RELEASE through RECOVERY | Purpose (Harbinger) or ending (Unstained) |
| 5 | Bespoke override | The whole track, both views | A named manipulation (31 styles) |
| 6 | Secondary accent | Material colour at the hands, no movement | Secondary tendency |

The lead arm is the main arm; the support arm is the other one. Tracks are authored right-handed and mirrored for left-handed players, first-person hands included.

## Track model

A `CastingTrack` is a Java-authored record of named segments, each a list of keyframes `(tick, pose, easing)` sampled by the pure `CastingTrackRules`:

- **Segments.** `opening`, `hold`, `release`, `recovery` and `cancel` are required in both views. `strain`, `loop`, `on` and `off` are optional. First-person segments (`firstOpening`, `firstHold` and so on) share the clock.
- **Poses.** Third person uses `CastingPose`: six part rotations plus a waist pitch. First person uses `HandPose(lead, support)`: a camera-space translation in blocks and a rotation for each hand.
- **Easing.** Per keyframe: `LINEAR`, `SMOOTH`, `EASE_OUT` (decelerate into the pose) and `HOLD`. A `HOLD` keyframe keeps the previous pose until its tick and then jumps, which is how Ductilis steps and other staccato motion are written.
- **Durations.** Each segment's length is its last keyframe tick. `CastingPlayback` takes the opening, release, recovery and cancel lengths from the track instead of fixed numbers. The server switches a sustained cast from OPENING to HOLD after 6 ticks, but the client keeps showing OPENING until the track's own opening (8 to 12 ticks) has played in full. HOLD and RECOVERY time start where the opening and release end.
- **Strain.** During HOLD the hold pose blends toward the strain pose by charge (`held / required`). `StrainDisplay` follows rising charge at once. When damage lowers the charge, the pose eases back over 4 ticks, whatever the size of the drop, and the clock does not restart. The server's charged RELEASE carries no charge, so the client latches the strain it was showing when a sustained cast ends in the same sequence. RELEASE leaves from that latched strain and converges on the authored release frames. A new cast starts unstrained.
- **Loops.** A loop adds a sine-shaped offset to the hold: an amplitude pose and a period in ticks. Because hold time starts where the opening ends, the loop starts from rest instead of mid-swing. Channels therefore read as effort rather than a freeze. Congeatio has no loop; its hold is absolute.
- **Phase crossfade.** Whenever the shown phase or the cast sequence changes, `PhaseCrossfade` blends from the last shown pose into the new phase over 3 ticks (`SMOOTH`). The third-person pose, the first-person hands and the blend weight each get their own fade. A cancel during the opening, a release from strain and a new cast replacing a recovery therefore never pop. A value that was off screen when the phase changed is not faded from.
- **Blend weight.** The weight against vanilla ramps in over the first ticks of OPENING. It stays at full through hold, release and the authored recovery, cancel and passive off beats, then fades to vanilla over the last 4 ticks of RECOVERY or CANCEL (`CastingTrackRules.weight`). Authored gestures therefore play at their full amplitude.
- **Passive on/off.** For a passive manipulation, RELEASE plays the `on` gesture and CANCEL plays `off`. Recovery is empty and the aim layer is off.

`TrackAuthoring` holds the shared shorthand (`p`, `out`, `step`, `lin` and their first-person forms) and a builder that derives the hold, recovery and cancel from the opening and release. `CastingTracks.resolve` picks a bespoke track when the style has one, otherwise the school and rank base.

## Harbinger schools

Each school has one verb. Rank changes how much of the body performs it, never the verb. Humilis keeps the torso and legs still so spamming a quick cast never wobbles the body; Perfectus holds have zero body motion for 240 ticks (Ductilis keeps only its authored snaps), because the blood pouring out of the caster does the grand motion.

| School | Verb | Humilis | Mediocritas | Summa | Magister | Perfectus | Cancel |
|---|---|---|---|---|---|---|---|
| Animus | Contract, then open (heartbeat) | Lead arm to the sternum, head dips, thrusts forward | Both arms cup before the chest, then open to a guard | Planted stance, 4° waist fold; recovery is a weaker second beat | Slow two-beat squeeze; strain tightens the elbows and drops the head; loop 48 | Wide open guard held dead still; one 2° waist drop | Subsides |
| Flammeus | Coil, then expel | Lead arm cocks out and snaps through; head flinches back | Torso winds back, support arm braces low across the belly | Both arms cock wide; the drive pitches 5° forward under a head recoil | Two-stage coil; strain shakes the arms wider; loop rocks at the waist, 48 | Planted, arms low and wide, head up; one hard 4° pitch | Vents outward and falls |
| Ductilis | Snap, pause, snap | Lead arm in three held steps, head counter-jerks; release steps forward | Arms alternate up and crossed low; an uneven forward snap | Twist flips 8° and the stance jumps on every step | Wider steps with a waist jerk; strain splits further; loop is a two-step tic at period 12 | Steps lock into a rigid symmetric frame; one snap wider and back | Collapses in two uneven steps |
| Lux | Rise, part, proclaim | Lead arm rises palm forward, head lifts; release lifts higher | Both arms rise together; release parts them to the sides | Orant near shoulder height, slight lean back, face up | Strain lifts the parted arms toward level and tips the face up; loop 48 | Full cruciform, face up, perfectly still; a 2° lift | Arms lower first, head last |
| Mortem | Reach, hook, haul | Lead arm reaches, hooks and hauls to the hip; the hunch is in the bowed head | Support arm hooks across; both haul to the hips | Waist bows, feet spread; arms hang a beat in recovery | Strain deepens the bow and curls the arms in; loop is a slow haul, 60 | Bowed, arms hanging forward; release bows once deeper | Straightens, arms relax |
| Congeatio | Close, decelerate, lock | Lead arm closes across the chest; 1-tick micro-snap | Both arms cross, legs plant | Waist and feet lock with the arms; recovery thaws linearly | Strain tightens the cross and lowers the head; the snap grows with charge; no loop | Arms half-raised and crossed, head bowed, utterly still; a 1-tick 1° settle | Fractures: one 3-tick drop |
| Ferric | Plant, brace, drive | Lead arm rises high, drives down to chest height and stops dead | Support arm sets a level brace; the lead hammers across it | Stance planted, waist lowered; the stop recoils upward 2 ticks | Strain climbs the hammer arm and widens the plant; loop settles the weight, 60 | Monumental plant, arms low and level; one 2° drop held 2 ticks | Unloads downward |
| Tenebris | Turn away, cross, unfold late | Lead arm crosses the face, sweeps open; the head follows late | Torso turns 25° away, support arm trails low and leads the unfold | Deeper turn on an offset stance; the unfold overshoots square | Strain deepens the turn and the head's counter-turn; loop sways, 56 | Turned 28° away, one arm crossing; a 1-tick 2° head turn | Body untwists before the arms lower |

Every opening, hold, strain and release in the table has a matching first-person hand track. Strain is authored at Magister and Perfectus; loops at Magister only.

## Cast types

The server already distinguishes the four types; the client plays them differently.

- **Quick.** Gameplay has executed before the client sees RELEASE, so the track plays release then recovery. A new cast replaces recovery cleanly. The spec's local quick-cast flick was dropped: Hematic Riposte's arm geometry and parry window both arrive with the RELEASE packet, so a client-side opening could not improve sync and could show a cast the server refuses.
- **Charged.** OPENING plays on the first heartbeat; HOLD blends hold toward strain by charge; RELEASE leaves from the current strain. Charged casts that repeat while held re-enter HOLD without replaying OPENING.
- **Continuous.** OPENING, then HOLD with the track's loop. Upkeep pulses never retrigger OPENING.
- **Passive.** Manual activation plays `on` (16 ticks, drawing the power inward) and dismissal plays `off` (12 ticks, throwing it outward). These are complete gestures rather than the spec's split 6/4/6 and 4/3/5 phases; the totals match. Avatar, Phoenix Debt, Blackhearted and Sovereign Instinct author on/off gestures. Passives without a bespoke style (Vigil of Glass, Hematic Ballast) play their school's release for activation and its cancel for dismissal. Automatic triggers keep their own effect accents, and passives never change idle posture.

## Aim layer

The aim layer replaces the old 8 to 18% purpose blend. It owns the lead arm and head at full weight: it ramps in over the first two ticks of RELEASE (HOLD is open-ended, so the release moment cannot be anticipated) and fades out across RECOVERY while holding the ending's final frame. The base track keeps the torso, support arm and legs.

| Purpose | Lead arm (third person) | Head | Lead hand (first person) |
|---|---|---|---|
| Projectile | Straight at the aim | Toward the aim | To the crosshair line, slightly near, with a 1-tick overshoot |
| Self | Returns to the chest | Level | Draws in and down to the chest line |
| Area | Sweeps outward | Slightly up | Sweeps to the frame edge |
| Targeted | Points at the target and holds | Toward the target | To the crosshair line and holds |
| Construction | Sweeps across to establish width | Down 4° | Slides across the lower frame |
| Conjuration | Takes the weapon's carrying line | Level | Moves to the vanilla held-item position |
| Travel | Narrows across the body | Toward the destination | Crosses toward centre |

Bespoke styles skip the aim layer entirely. Their tracks author the lead arm themselves (Deadly Gaze is head-led, Riposte has its parry sweep, Marionette holds the crossbar), and the aim layer would overwrite them. Passives skip it too.

## Bespoke styles

A bespoke style replaces the whole base track in both views. There are 31.

| Style | Manipulation ids | Shape |
|---|---|---|
| Ironheart | `ironhearted` | Plant wide, support forearm level across the chest, hammer arm high and climbing with charge; one straight drive onto the brace that stops dead |
| Umbral Step | `umbral_step` | Turn away with the lead arm cocked and the support trailing; both cross in front, then the body untwists |
| Umbral Reversal | `umbral_reversal` | The exact mirror of Umbral Step, taken from the other shoulder |
| Avatar | `summon_avatar`, `summon_avatar_arms`, `summon_avatar_armor`, `summon_avatar_legs`, `summon_avatar_complete` | On draws both arms into the chest; off throws them open past the shoulders |
| Staff | `conjure_staff`, `conjure_blade`, `conjure_axe`, `conjure_spear`, `conjure_claws`, `conjure_crossbow`, `conjure_torch`, `conjure_flail`, `conjure_sickle` | The support hand closes on the lead wrist and draws the haft out; the lead arm swings onto the weapon's carrying line |
| Crimson Tithe | `crimson_tithe` | Bow and reach, hook both arms out wide, haul to the hips as the body rocks back; the arms hang a beat |
| Unclosing Eye | `unclosing_eye` | Orant rise, a further lift, then the arms part level into the cruciform, head up |
| Bloom of Rot | `bloom_of_rot` | Stoop and cup both hands low, then open them outward and down like a flower going over |
| Endless Hour | `endless_hour` | One decelerating close where every part arrives on the same tick, a one-tick snap, a slow linear thaw |
| Crimson Coronation | `crimson_coronation` | Contract, then raise a crown before the brow (capped at x −128); release opens to the cruciform guard on a 2° heartbeat drop |
| Synaptic Storm | `synaptic_storm` | Three staccato steps into a rigid symmetric hold that flutters at period 12; release is one snap wider and back |
| White Verdict | `white_verdict` | Full level cruciform, head lifted and climbing with charge; release lifts the upper body 2° |
| Funeral Bell | `funeral_bell` | Bowed, arms hanging; release swings the body upright once like a bell, then back into the bow |
| Absolute Stillness | `absolute_stillness` | One ease-out contraction into crossed arms; nothing moves again (empty loop); cancel fractures |
| Lodestone Collapse | `lodestone_collapse` | Monumental plant; strain lifts the arms 10° against the weight; release drops everything and holds 2 ticks |
| Eclipse Well | `eclipse_well` | Turned 20° away, one arm crossing, head counter-rotated; strain moves only the head; release is a 1-tick head turn |
| Phoenix Debt | `phoenix_debt` | On folds both arms across the chest; off throws them open |
| Riposte | `hematic_riposte` | Guard with the support forearm across the chest; release follows the Riposte arm: a tighter cock, then the outward parry sweep |
| Blood Rush | `blood_rush` | Arms pump back once with a 6° lean into the dash; recovery is 8 ticks so the dash owns the body |
| Deadly Gaze | `deadly_gaze` | Arms never move; the head locks on, bows further with charge and snaps up in one tick; first-person hands stay at rest |
| Marionette | `sanguine_marionette` | Both hands raised at chest height, fingers-down carriage; the loop lifts one hand as the other drops so the crossbar walks |
| Ground Touch | `lignum_mortis`, `canopy_mortis`, `worked_lignum` | The body lowers from the waist and the lead hand presses toward the ground; a slow 2° press loop |
| Dowsing | `vascular_dowsing` | The lead arm drives level like a rod and the head scans with it |
| Furnace Veins | `furnace_veins` | Arms low and cocked, rocking from the hips like a bellows |
| Living Circuit | `living_circuit` | The hands close a circuit before the sternum; a current tics through it at period 12 |
| Iron Choir | `iron_choir` | Planted, arms level over the work; the weight settles 2° and lifts again |
| Carrion Communion | `carrion_communion` | Bowed, both arms hooked inward; a slow 3° haul toward the body |
| Penumbral Drift | `penumbral_drift` | Turned away, lead arm crossing low, support trailing; a slow 3° sway |
| Radiant Wake | `radiant_wake` | Arms low, open and symmetric, palms forward, head lifted; the figure breathes upward 2° |
| Blackhearted | `blackhearted` | On reaches out and hauls the blackened heart into the chest; off throws the arms wide |
| Sovereign Instinct | `sovereign_instinct` | On clenches inward in three staccato steps; off snaps open in two |

The source is split by family: `StyleTracks` (the nine older styles), `PerfectusTracks` (the eight Perfectus iconics), `GameplayTracks` (Riposte, Blood Rush, Deadly Gaze, Marionette) and `ChannelTracks` (ground work, the six Magister channels and the two Magister passives).

## Still Arts: the Unstained lane

A Still Art is not forced; it is allowed. Where a Harbinger contracts and drives, an Unstained settles: slow, symmetrical and level, ending stiller than it began. Hands stay open, the head is level or slightly bowed and never thrown back, and the silhouette is a figure standing in still water. None of this lane borrows blood vocabulary or tint.

Still Arts are quick casts. After a successful `action.cast(...)`, `StillArt.performAction` calls `CastingAnimationManager.releaseStillArt`, which publishes one RELEASE packet with `lane = UNSTAINED`, the art's required clarity stage and its ending, and no school, rank or purpose. Refusals (unknown art, cooldown, clarity too low, or an action block: Paralysis, Disrupted or axonal travel) publish nothing, so observers never see them. There is no local opening flick, for the same reason as quick casts.

Timing is inverted from the Harbinger lane: release is slow (8 to 12 ticks) and recovery is a long settle (14 to 24 ticks) that ends at rest with zero velocity, so the stillness is the memorable frame.

### Stage tracks

Stage follows `EnumClarityStage`. Each stage track lives in `StillArtTracks` with both views. The server publishes RELEASE only, so the release rises out of a low readiness pose close to rest; the 2-tick opening and 10-tick cancel exist for previews and for any future sustained art.

| Stage | Third person | First person | Release | Settle |
|---|---|---|---|---|
| Awakened | Lead arm lifts to a level open palm at chest height; head level | Lead hand rises toward lower centre, palm toward the world; the off hand stays vanilla | 8 | 14 |
| Discerning | Both arms lift to level open palms, shoulders square | Both hands rise level and symmetric in the bottom third | 9 | 16 |
| Vigilant | Both arms lift, the body straightens, feet come together, head bows | Hands a little higher, level | 10 | 18 |
| Resolute | Arms rise to shoulder height, forward and out, palms forward; release ends in one 2° lowering like a bell damping | Hands rise toward the mid-frame edges and lower together | 11 | 20 |
| Enlightened | Arms open to a level stance and the body is perfectly still from tick 8; the world moves around it | Hands open toward the frame edges and hold | 12 | 24 |

The settle reaches 97% of the way home two ticks early, so it lands at rest without velocity.

### Endings

Each art declares a `StillArtEnding`, the Unstained counterpart of a purpose. Endings use the same aim-layer mechanism (`StillArtEndings`).

| Ending | Arts | Third person | First person |
|---|---|---|---|
| Rebuke | Silver Rebuke, Autoimmune Edge | The palm pushes forward slowly; head level | Pushes toward the crosshair line, slow, no overshoot |
| Hush | Lethean Mute, Quietus Bell | Both palms turn down and lower 10° | Both hands lower slowly, palms down |
| Stillness | Still Pulse | Arms stop level and hold into the settle | Hands hold level |
| Sight | Pale Diagnosis | Lead hand rises to the brow; the head scans left, then right | Lead hand rises to the top of the frame and sweeps once across |
| Shear | Memory Shear | One slow horizontal cut at shoulder height | A slow horizontal sweep across the frame |
| Step | Absolving Step | Arms trail back as the body leans forward; short recovery | Hands trail to the bottom corners |
| Intercession | Pale Intercession | Both arms open outward and slightly up, palms up | Hands open to the upper edges, palms up |

`StillArt.BLANK` uses Stillness. The ending is presentation only and never affects gameplay or saved data.

| Art | Stage | Ending |
|---|---|---|
| Silver Rebuke | Awakened | Rebuke |
| Lethean Mute | Awakened | Hush |
| Still Pulse | Discerning | Stillness |
| Pale Diagnosis | Discerning | Sight |
| Memory Shear | Vigilant | Shear |
| Absolving Step | Vigilant | Step |
| Quietus Bell | Resolute | Hush |
| Pale Intercession | Resolute | Intercession |
| Autoimmune Edge | Enlightened | Rebuke |

### Accents

- **Hand ring.** A thin pale ring (white `0xF4F6F8` with a silver edge `0xC9D1D9`), drawn by `StillArtRings` as flat, untextured quads through the unlit Lux wing render type, so it reads pale in daylight and darkness. (The textured `LUX` material sampled a dark texel and turned the first build's rings charcoal.) It pulses once on release and dissolves through the settle.
- **Still-water ring.** From Discerning up, a flat translucent ring on the ground at the feet, expanding once and fading. Its radius grows with stage, from 0.6 to 1.6 blocks.
- **Motes.** The Still Art actions in `StillArtInit` use pale `HemoParticleData.glow` motes; the old vanilla end-rod, snowflake and wax particles are gone, and a source test keeps `ParticleTypes.` out of that file.
- **Reduced effects.** Minimal particles removes the ground ring; the hand ring stays.

No blood tint appears anywhere in the lane.

## First person

`CastingAnimationClientEvents.hand` applies the full camera-space hand transform (translate, then rotate X, Y, Z) before drawing the hand or the held item, so a held item moves with the posed hand.

- **Hand roles.** The lead hand is the main arm and the support hand is the off arm. Left-handed players get the mirrored pose. Humilis and Awakened animate the main hand only; the off hand renders vanilla.
- **Clamp.** Translation is clamped to ±0.35 blocks horizontally, ±0.25 vertically and ±0.2 in depth from the vanilla hand rest, so hands never leave the frame.
- **Crosshair band.** No hand may sit in the central 12% band of the frame, raised toward the crosshair, during HOLD; release and the passive on/off gestures may cross it for at most two ticks. `FirstPersonHandRules.insideCrosshairZone` carries the vanilla hand rest point (±0.64, −0.6, −0.72) through the same translate-then-rotate transform the renderer uses, then tests it against the band and the raised line, both scaled with depth. It also carries the fist (the far end of the arm box) and rejects a fist within 0.16 view tangents of the crosshair, which catches folds where the rest point stays low (Phoenix Debt's first "on" gesture). Hand rotation counts: a lead hand pulled 0.33 blocks toward centre and yawed 28° lands in the band. The track tests check every hold, release and passive gesture.
- **On screen.** Every posed hold hand must stay readable: `FirstPersonHandRules.onScreen` requires the projected fist inside a conservative frame (|x| ≤ 0.95, −0.60 ≤ y ≤ 0.45 in view tangents; the hand projection uses a fixed 70° vertical FOV, so the frame edge is y = ±0.70) and the arm's cut (shoulder) end out of view (y ≤ −0.80, beyond |x| 1.25, or behind the eye). The limits are calibrated on the 2026-10-19 captures. Negative hand rx pitches the hand down: lifting it with positive ty while pitching it down sank Lux, Ductilis and Ferric fists below the frame, and a raised hand pitched up (Ironheart's first hammer) brought the cut end on screen. Holds are checked at no charge and full strain, every tick including loops.
- **Camera.** The camera never moves and aim is never changed.
- **Occupied hands.** A hand that is using an item, swinging, morphing a Living Staff or hidden by a Morphling form is left to its owner.

## Third person

### Waist pivot

Body pitch pivots about the waist instead of the neck. The torso rotates by θ and is translated so its bottom edge stays on the legs: y by 12·(1 − cos θ) and z by −12·sin θ in model units. The arms ride the same offset. The head follows the neck forward (z) but keeps its height (`WaistPivotRules.headOffsets`): lowering it with the neck while the pitched torso lifts its back top edge sank it about a pixel into the shoulders. It is counter-rotated so it keeps the player's aim rather than nodding. The offsets are assigned each frame from the un-pivoted base (`WaistPivotRules.partOffsets`), never added. Vanilla resets part y every frame but never body or head z, so `PlayerAnimationClientState.applyThirdPersonPose` clears body, head, hat and jacket z before any pose runs. A pitched pose therefore cannot drift the torso, and nothing stays displaced after the cast. Pitch is capped at ±14°, and `WaistPivotRules` tests check the waist point for every authored pitch. Torso yaw, head motion and pitch apply only when both arms are free and the player is not riding, swimming, crawling, gliding, using an item or swinging.

### Arm authoring convention

Tracks are authored with right-arm z negative (and left-arm z positive) meaning outward. Vanilla model parts use the opposite sign and apply X before Z, so `CastingPose.toModel()` flips arm z once at the two render sites (`CastingPlayerPose.apply` and the Somatic Loom mannequin, `SomaticLoomEcho`). Two consequences matter when authoring:

- At x near −90 (arm pointing forward), z is pure roll. Sideways spread needs a small x (roughly −15 to −40) with a large z; a cruciform written as x −90, z ±90 reads as arms forward.
- Arms that cross the chest need arm yaw toward the centre; at a moderate x, z alone only rolls the arm.

### Locomotion and priority

Standing legs join the stance; walking fades them into locomotion by speed; riding, swimming, crawling and Elytra keep vanilla body posture and take arms only. `CastingPlayerPose.apply` runs first in `PlayerAnimationClientState.applyThirdPersonPose`, so microscope, vial injection, Living Torch breath, Unstained weapon strokes, rite staff planting and item use still override it. Armour, sleeves, pants and jacket copy the posed parts.

## Timing budgets

All numbers are visual ticks authored in the track.

| Rank | Opening | Release | Recovery | Cancel |
|---|---|---|---|---|
| Humilis | 8 | 6 | 12 | 10 |
| Mediocritas | 9 | 7 | 14 | 11 |
| Summa | 10 | 8 | 16 | 12 |
| Magister | 11 | 9 | 18 | 13 |
| Perfectus | 12 | 10 | 20 | 14 |

Congeatio thaws more slowly (recovery 14, 16, 18, 19, 20). Bespoke styles author their own lengths in the same range; Perfectus iconics use the Perfectus row, and Blood Rush recovers in 8 so the dash owns the body. Loops keep their amplitude at or under 4° with periods of 44 to 60, except the Ductilis tics at 12. Passive on and off are 16 and 12 ticks. Still Arts release over 8 to 12 ticks and settle over 14 to 24.

Client expiry follows the track: a non-sustained phase lasts its release plus recovery (cancel lasts the cancel length), which lets a Still Art's long settle finish. Sustained phases expire 40 ticks after the last heartbeat. Sequence ordering rejects old phase updates, and late observers join a sustained session at its age without replaying a release. A packet update re-resolves the track and aim layer whenever any presentation input changes (manipulation, lane, school, rank, style, purpose, stage or ending; `CastingTracks.presentationChanged`); phase, clock and charge updates only advance the playback.

## The cast outpouring

Harbinger casts put nothing beside or behind the caster. The spectacle is the caster's own blood (`client/render/outpouring/`; design: `docs/superpowers/specs/2026-10-19-harbinger-cast-outpouring-design.md`).

- **Where it leaves.** Blood exits at the manipulation's vascular section. HEAD exits at the eyes, plus the nose and ears from Summa. HEART exits at the left chest, BODY at the flanks, abdomen and back, ARMS at the casting hand (both hands from Mediocritas), and LEGS at the thighs and calves. Third person captures the exits from the posed model each frame (`OutpouringAnchors`); first person estimates them from body yaw and look direction.
- **Veins.** `CastVeinsLayer` re-renders the posed player with the `cast_veins` shader. A body atlas (`SkinBodyAtlas`) maps every skin texel to a point on a standing body and records which body axes its u and v run along. The shader rebuilds the exact point inside each texel, so veins resolve at screen resolution and stay continuous across UV seams. The vessels are the Cardinal floor membrane's arteries and capillaries (warped Voronoi walls that swell and pinch), lifted into body space. Armoured parts get the same veins on a shell baked with the skin's UVs just outside the armour. The first-person arm gets them too. A manifested blood avatar hides them.
- **The blood.** `OutpouringSim` moves the blood as motes with a per-tendency behaviour, colliding with blocks one axis at a time. `OutpouringRenderer` draws it through the manipulation toolkit (`client/render/world/OutpouringBrushes`), so it is the mod's own art:
  - **Animus:** living-blood drops land in small, lobed brazier-blood pools that merge, grow and stay where they land. Their spray rises in a whirlwind of the Riposte's wisp mist.
  - **Flammeus:** flammeus flame tongues lick off the exits, flame cards burn to soot, and Thermal licks and flames accent them.
  - **Ductilis:** nerve strands, plus HutosLib Ductilis lightning that branches from the exit to whatever the strands struck (one tree every 4 ticks: a trunk to the farthest hit, a fork to each other hit, twigs off every branch and, from Magister, twigs off the twigs). Showers of ember sparks burst where each branch ends.
  - **Lux:** many small light orbs.
  - **Mortem:** sludge strands and blobs with rot spray.
  - **Congeatio:** frost-shelled clots that grow into clusters of glass ice shards over cruor frost cracks, with frost powder.
  - **Ferric:** blood sinews that harden into forged blood-iron spikes and riveted plates on the ferric material.
  - **Tenebris:** heavy umbral smoke that pours down and rolls slowly along the ground, each puff a few billowing lobes with a red blood-mist rim and a thin blood smear behind it; the darkness pass glows crimson where the fog thins.
- **Marks.** Where blood lands it leaves a fading mark on the block face (`OutpouringMarks`), drawn with the same toolkit materials:
  - Animus: a brazier pool
  - Flammeus: a molten crust
  - Ductilis: a crackle
  - Mortem: hyphae sludge
  - Congeatio: frost cracks
  - Ferric: an iron plate
  - Tenebris: an ink pool

  Marks are client-only, capped at 256 and never saved. The caster's own body takes a matching frost, scorch or corrosion creep through the vein shader.
- **Light and dark.** `OutpouringScreenPasses` reads scene depth: `cast_lumen` lights surfaces around Lux orbs without placing block light, and `cast_umbra` drains the light around a Tenebris caster and rolls fog through it. The caster's own first-person view gets 35% strength with a clear centre.
- **Timing.** Charges build with their charge and peak on release. Quick casts peak on arrival. Channels ramp to a living near-peak and burst on release. Cancels draw the blood back into the veins. Passive toggles play a short reduced flow. Riposte, Crimson Coronation, avatar stages and staff/conjure forms keep only veins and a trickle, so their own blood reads as coming out of the wound.
- **Rank.** Rank scales volume, reach (0.6 to 5 blocks), wildness, mark count and life, and how far the veins spread (`OutpouringRules.profile`).
- **Detail.** Minimal particles shows veins only. Decreased halves the flow and keeps marks at the feet. The outpouring reduces past 24 blocks, shows veins only past 32, and shows nothing past 48.
- **Real world changes.** The server hook `OutpouringWorldEffects` is gated by `cast_outpouring.realEffectMinRank` (default `-1`, never), and every effect is a no-op.

## Local preview

In a development client, `/hemocastpreview` previews a presentation on your own player for eight seconds. It sends nothing, spends nothing, unlocks nothing and applies no gameplay. Tab completion lists every school, rank, purpose, phase, style, cast type, clarity stage and ending. `section <head|arms|heart|body|legs>` places a Harbinger preview's outpouring.

```text
/hemocastpreview <school> <rank> <purpose> <phase> [style <style>] [type quick|charged|continuous|passive] [charge <0..1>]
/hemocastpreview still <stage> <ending> <phase>
/hemocastpreview stop
```

The options after the phase may come in any order. Examples:

```text
/hemocastpreview lux perfectus area hold
/hemocastpreview ferric magister construction hold charge 0.9
/hemocastpreview animus perfectus area hold style crimson_coronation charge 0.5
/hemocastpreview mortem magister self release type passive style blackhearted
/hemocastpreview still resolute hush release
/hemocastpreview stop
```

- `style` swaps in a bespoke track; without it the school base plays.
- `charge` sets the strain: held is `round(charge × 40)` of a required 40. Without it the preview starts at half charge. In HOLD the preview keeps charging from there, as a real charged cast does. `type quick` and `type continuous` send no charge (required 0), as the server does, so they show no strain and ignore `charge`.
- `type passive` makes RELEASE play the on gesture and CANCEL the off gesture. The client decides passive from the manipulation id, so the preview borrows `blackhearted` (a registered passive) for the packet; other types use `preview`.
- `still` builds an Unstained packet with the given stage and ending. Still Arts are quick casts, so `release` is the phase to inspect.

Use first person or F5 to inspect either view on the same clock.

## Source

| Area | Classes |
|---|---|
| Server | `CastPresentation`, `CastStyle`, `CastLane`, `CastPhase`, `CastingAnimationManager`, `CastingAnimationPacket`, `StillArt`, `StillArtEnding`, `StillArtInit` |
| Track model | `CastingTrack`, `CastingTrackRules`, `CastingPose`, `HandPose`, `AimLayer`, `AimLayerRules`, `CastingPlayback`, `CastingDisplay`, `StrainDisplay`, `PhaseCrossfade` |
| Authored tracks | `client/player/tracks/`: `TrackAuthoring`, `CastingTracks`, `AimLayers`, the eight `<School>Tracks`, `StyleTracks`, `PerfectusTracks`, `GameplayTracks`, `ChannelTracks`, `StillArtTracks`, `StillArtEndings` |
| Rendering | `CastingPlayerPose`, `WaistPivotRules`, `FirstPersonHandRules`, `CastingAnimationClientEvents`, `CastingAnimationClientState`, `CastingAnatomyRenderer` (Still Art rings), `CastingPreviewCommands` |
| Outpouring | `client/render/outpouring/`: `OutpouringRules`, `OutpouringTracker`, `OutpouringClientState`, `OutpouringAnchors`, `OutpouringSim`, `OutpouringRenderer`, `OutpouringAccents`, `OutpouringGeometry`, `OutpouringMarks`, `OutpouringScreenPasses`, `CastVeinsLayer`, `VeinEncoding`, `SkinBodyAtlas`, `OutpouringRenderTypes`; toolkit bridge `client/render/world/OutpouringBrushes`; server `common/manipulation/outpouring/OutpouringWorldEffects` |

## Verification

```powershell
.\gradlew.bat test --tests '*Casting*Test' --tests '*TracksTest' --tests '*StillArt*Test'
.\gradlew.bat runCastingAnimationGameTestServer
.\gradlew.bat alphaCheck
```

The JVM suite covers every school and rank in both views with every required segment, continuity between segments, return to rest, rotation and pitch bounds, first-person clamps and the crosshair band, Humilis torso stillness, Perfectus stillness over 240 ticks, Ductilis steps and Congeatio's simultaneous stop, monotonic strain, the strain latch and eased drops, phase crossfades and the blend-weight fade, loop amplitude and loop phase, distinct passive gestures, mirroring, aim-layer weight, the waist pivot, playback durations, the sequence guard, packet round trips for both lanes, the Still Art release wiring and particle sweep, and this page naming every style and ending. The `casting_animation_validation` GameTests cover registry coverage, quick-cast payment and release, charge cancellation and reduction, continuous upkeep, late tracking, and one Still Art release per successful cast with none on refusal.

### Two-client review fixture

`tools/oneoff/casting-animation-review.init.gradle` and the flag-gated `CastingClientAcceptance` GameTest source provide a disposable two-client capture run. Never use a personal world: the fixture prepares a quartz stage and test inventories in its own review directory.

1. Run `./gradlew.bat classes gameTestClasses` once. Concurrent compilation into the same output directory is unsafe.
2. Launch `runServer`, `runCastingCaster` and `runCastingObserver`, each with `-I tools/oneoff/casting-animation-review.init.gradle -PcastingRun=<name>`. The server is localhost-only on port 25579 under `build/<name>-server`; both clients have first-run accessibility onboarding disabled.
3. The sweep covers the 40 school and rank profiles, every bespoke style (sent as the manipulation that owns it, passives as on/off gestures) and the five Still Art stages before the integration scenes. `-PcastingIntegrationOnly=true` on the server skips straight to the integration scenes; `-PcastingCasterName=CastCaster1` selects the WIDE skin model.

Results and remaining limits are recorded in [Casting-Animation-Validation.md](Casting-Animation-Validation.md).

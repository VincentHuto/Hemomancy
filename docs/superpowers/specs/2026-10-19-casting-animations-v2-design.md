# Casting animations v2: somatic forms for every school, rank, cast type and Still Art

> Caster-body effects (hand veins, anatomy constructs, Lux wings) were replaced by the Cast Outpouring: `docs/superpowers/specs/2026-10-19-harbinger-cast-outpouring-design.md`.

Date: 2026-10-19. Status: design for review. The user asked for a plan covering new first- and third-person casting animations for every cast type and school, the Unstained Still Arts, and especially the Harbinger ranks and tendencies. The session was non-interactive, so every design call below is the builder's call unless marked **(user)**. Open decisions that change what gets built are collected in §16.

Supersedes the restrained-motion revision recorded in `wiki/Casting-Animations.md` and the concept package under `docs/done/HEMOMANCY_CASTING_ANIMATION_CONCEPTS/`. The concept package's tendency briefs remain the material and silhouette reference; this document is the movement and runtime contract.

## 1. Intent

A hemomancer forces the blood to remember an instruction and the body performs that memory. An Unstained settles the body so still that the world's infection has nothing to hold. Today the player sees almost none of that. Every one of the 123 registered manipulations resolves to one of 40 three-pose school/rank compositions plus a purpose blend applied at 8–18% strength, every quick cast is a six-tick twitch followed by a fade, charged and continuous casts hold the same frozen pose, first person is a few degrees of rotation on the vanilla hand, and Still Arts have no presentation at all.

The goal is a casting vocabulary that a player can read at twenty blocks: which school, roughly which rank, whether the caster is winding up, holding, channelling or releasing, and where the power is going. In first person, the same cast should feel authored rather than incidental.

## 2. Goals, non-goals, anti-goals

Goals

- Re-author all 40 Harbinger school/rank presentations as multi-keyframe tracks in both views, with anticipation, hold, strain, release, recovery and cancel movement that differs by school and grows by rank.
- Give the four cast types distinct movement grammars: quick, charged (tension follows charge fraction), continuous (held loop), passive (on and off gestures).
- Make the seven cast purposes legible by letting purpose own the lead arm and head at release, instead of a faint blend.
- Add an Unstained Still Art presentation lane: five clarity-stage bases and seven ending kinds covering all nine arts, in both views.
- Add bespoke overrides for the eight Perfectus manipulations and keep the nine existing bespoke styles, re-authored to the new grammar.
- Keep every gameplay rule exactly where it is: costs, cooldowns, charge clocks, targeting, movement and hit windows are untouched.

Non-goals

- No new animation library, no extra bones, no finger or wrist articulation. The rig stays head, body, two arms, two legs.
- No change to the server session model (`CastingAnimationManager`, `CastingAnimationPacket`, `CastPhase`) beyond carrying a lane and a stage for Still Arts.
- No per-manipulation bespoke clip for all 123 entries. Bespoke work is tiered in §10.
- No mob or Drudge casting animation.
- Vascular-condition degradation (vein strain changing movement) stays deferred.

Anti-goals (it fails even if it works)

- Gestures that pull the torso off the waist or slide the arms off the shoulders (the artifact that triggered the restrained revision).
- Perfectus casts that get busier and faster instead of stiller and larger.
- A Still Art that reads as a pale-coloured blood cast. The Unstained grammar must be its own thing.
- First-person hands that cover the crosshair during a hold, or that jump when the server's RELEASE arrives.
- Any track that visibly fights the walk cycle, swimming, Elytra, riding, item use, shield block, the Living Staff morph, the microscope, vial injection or rite staff planting.

## 3. Constraints

- Six rigid player parts in both skin models; armour, sleeves, pants and jacket copy the posed parts.
- Quick casts execute gameplay first; the client sees RELEASE after success. Anticipation for quick casts is therefore local and optional (§8.1).
- Charged and continuous timing is the server's. Visual phase durations are decorative and must never gate or extend gameplay.
- Phase payloads already exist: OPENING, HOLD, RELEASE, RECOVERY, CANCEL, with `started`, `updated`, `held`, `required` and a sequence guard. The client already derives a continuous clock from them. Keep it.
- Animation priority order in `PlayerAnimationClientState.applyThirdPersonPose` and the arm-availability rules in `CastingPlayerPose` stay authoritative. Casting yields to microscope, injection, torch breath, Unstained weapon strokes, staff planting and item use.
- Effects stay on the existing textured meshes (`VisceralMesh`, `LuxCastingWings`, `ManipulationMaterials`). No new particle emitters for the Harbinger lane. Still Art accents use `HemoParticleData` and the Lux/pale materials, never vanilla `ParticleTypes` (the Still Art actions in `StillArtInit` currently use END_ROD, SNOWFLAKE and WAX_OFF; see §11.6).
- No copper, brass or bronze reads anywhere in the Harbinger lane. Copper and silver belong to the Unstained lane.
- Vocabulary: Harbinger copy uses Hematic Order / Sanguine Brotherhood / Crimson Lodge terms; Unstained copy uses Lethean and still-water language.
- Match the formatting of the file being edited. Pure maths in `*Rules` classes with no Minecraft types; JVM tests on the rules, source-contract tests on wiring, GameTests on server behaviour.

## 4. Current state (what the builder inherits)

| Piece | Today | Keep / change |
|---|---|---|
| `CastPresentation` | id → purpose + style, 9 styles | Keep; add Perfectus and tier-B styles |
| `CastingAnimationManager` | server sessions, phases, heartbeats, tracking | Keep; add Still Art entry points |
| `CastingAnimationPacket` | school, secondary, rank, purpose, style, phase, clock, hand | Add `lane`, nullable school/rank, `stage`, `ending` |
| `CastingClips` | 40 × (opening, hold, release, firstHold, firstRelease) | Replace with keyframe tracks (§12) |
| `CastingPoseModifiers` | purpose blend 8–18%, style overrides | Replace with aim layer (§9) and style tracks |
| `CastingPlayback` | hard-coded 6/6/14/10 tick phases | Durations come from the track |
| `CastingPlayerPose` | applies rotations; body pitch disabled | Add waist pivot (§13.1) |
| `CastingAnimationClientEvents.hand` | one rotation per hand, tiny x shift | Full camera-space hand transform (§12.2) |
| `CastingAnatomyRenderer` | hand veins + rank anatomy | Keep; add pale Still Art accents |
| `/hemocastpreview` | school rank purpose phase | Add style, type, charge, lane |
| Still Arts | no presentation | New lane (§11) |

## 5. The grammar

Six layers, applied in this order. Each layer owns specific parts so they never fight.

1. **Base track** — Harbinger: school × rank. Unstained: clarity stage. Owns torso, support arm, legs, and the default lead arm and head.
2. **Cast type** — selects which track segments play and how HOLD behaves (§8).
3. **Rank / stage scope** — how much of the body and how much surrounding anatomy takes part (§7, §11.2).
4. **Aim layer** — purpose (Harbinger) or ending kind (Unstained). Owns the lead arm and head from the last two ticks of OPENING through RELEASE, and fades out through RECOVERY (§9).
5. **Bespoke override** — a whole-track replacement for a named manipulation or art (§10).
6. **Secondary accent** — a material colour at the hands only; no movement (unchanged).

"Lead arm" is the main arm. "Support arm" is the other one. Humilis and Awakened use the lead arm only; every other rank and stage uses both.

## 6. Shared timing

All numbers are visual ticks and live in the track, not in `CastingPlayback`.

| Segment | Quick | Charged | Continuous | Passive on / off | Still Art |
|---|---|---|---|---|---|
| Opening | local flick 0–3 (§8.1) | 8 (H) → 12 (P) | 8 → 12 | 6 / 4 | local 0–2 |
| Hold | none | strain blend by charge | loop, period 48, amplitude ≤ 4° | none | none |
| Release | 6 (H) → 10 (P) | 6 → 10 | 6 → 8 | 4 / 3 | 8 → 12 |
| Recovery | 12 (H) → 18 (P) | 14 → 20 | 12 → 16 | 6 / 5 | 14 → 24 (settle) |
| Cancel | 10 | 10 → 14 | 10 | n/a | n/a |

The server's expiry windows (`CastingPlayback.expired`) widen to the longest authored release + recovery (30 ticks) and the sustained heartbeat window stays at 40.

## 7. Harbinger school languages

Each school has one motion verb and one silhouette. Rank changes how much of the body performs it, never the verb. Angles are guidance for the builder; arm x is pitch (negative raises forward), arm z is spread (negative = right arm outward), body pitch is about the waist (§13.1).

Every rank row below lists third person first, then first person (FP). FP hands are camera-space: "in" = toward screen centre, "out" = toward the edge, "near" = toward the camera.

### 7.1 Animus — contract, then open (heartbeat)

Verb: squeeze inward, release outward. Silhouette: a closed fist of a body opening into a guard.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm pulls to the sternum (x −70, z −20) over opening; head dips 3°. Release: arm thrusts forward (x −95) with a 2° body pitch forward then back. | Lead hand draws in and down toward centre-chest, then pushes to centre, slightly up. Support hand idle. |
| Mediocritas | Both arms draw to the chest, elbows wide (z ±35). Release: both open to a guard (x −90, z ±40). | Both hands pull in to a loose cup under the crosshair, then open outward and up. |
| Summa | As above with stance (legs z ±8), body pitch 4° forward on contract, 2° back on release. Recovery is a second, weaker beat (one 3-tick dip). | Hands cup, then open wider; a small near-push on the beat. |
| Magister | Opening is a slow two-beat squeeze. Strain keyframe (charged): elbows tighten, head drops 6°, stance widens. Loop (continuous): a 4° chest-height pulse at period 48. | Hands tremble inward on strain (≤ 1.5° jitter at period 6); open fully on release. |
| Perfectus | Iconic: arms open to a wide cruciform guard (x −90, z ±70), head level, body still. The heart anatomy performs the beat. Release is a single 2° body drop. | Hands spread to the frame edges and hold; the vascular hand veins carry the pulse. |

Cancel: everything subsides toward rest with one shallow dip.

### 7.2 Flammeus — coil, then expel

Verb: recoil behind, drive forward. Silhouette: a thrower.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm cocks back and up (x −60, z −30, body twist −6°). Release: arm snaps forward (x −100), twist +4°, head recoils 4° back. | Lead hand pulls back and out past the frame edge, then thrusts to centre and slightly down. |
| Mediocritas | Lead arm cocks; support arm braces low across the belly (x −30, z +25). Release: lead drives, support opens. | Lead cocks out; support hand rises to a brace at lower centre, then both push. |
| Summa | Both arms cock, twist −10°, stance ±9. Release drives both forward with 5° body pitch forward and a 3-tick head recoil. | Both hands pull back wide, then drive in to centre with a near-overshoot. |
| Magister | Strain: cocked arms shake outward as charge rises (z grows ±10 with charge); head lowers. Loop: slow 3° rocking. | Hands held wide, shaking slightly; release is the same two-hand drive. |
| Perfectus | Iconic: planted stance, arms low and wide (x −45, z ±55), head up 6°. The caster stands inside the inferno. Release: a single hard 4° forward pitch. | Hands low at the frame corners, palms up; release pushes them off the bottom edge. |

Cancel: arms vent outward and fall.

### 7.3 Ductilis — snap, pause, snap (staccato)

Verb: step through two or three sharply separated poses. Silhouette: a body seized by its own nerves. No easing between steps; each step holds 2–3 ticks.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm: step 1 half-raised and angled in (x −50, z +15), step 2 raised and angled out (x −95, z −25). Head counter-jerks on each step. Release: a third step straight forward. | Lead hand snaps between two off-centre positions, then to centre. |
| Mediocritas | Arms alternate: when one is up, the other is low and crossed. Three steps. Release: both snap forward asymmetrically. | Hands alternate up/down, then both snap in. |
| Summa | Adds a body twist that flips sign on each step (±8°) and legs that swap stance (z ±9 ⇄ ∓3). | Hands alternate with a small camera-space roll (±6° z). |
| Magister | Strain: the step period shortens with charge (from 3 ticks to 1); amplitude grows. Loop: a 2-step tic at period 12. | Hand jitter period shortens with charge. |
| Perfectus | Iconic: between steps the body is rigid and symmetric (arms x −90, z ±60); the nerve anatomy carries the impulses. Release: one full-body snap to a wider pose and back. | Hands rigid at mid-frame; a single 1-tick snap outward and back on release. |

Cancel: the circuit collapses; arms drop in two uneven steps.

### 7.4 Lux — rise, part, proclaim

Verb: symmetrical elevation. Silhouette: an orant.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm rises palm-forward (x −95, z −12), head lifts 3°. Release: a small upward lift (x −105) then settle. | Lead hand rises to upper-centre, palm toward the world, then lifts a little more. |
| Mediocritas | Both arms rise and part (x −95, z ±30). Release parts them wider (z ±50). | Both hands rise symmetric and part. |
| Summa | Adds a 3° backward body lean, stance ±6, head up 6°. Wings open behind the shoulders (existing). | Hands rise to the upper corners and hold; release opens them to the edges. |
| Magister | Strain: arms climb higher with charge (x −95 → −120), head tilts further up. Loop: 2° breathing rise and fall. | Hands climb with charge. |
| Perfectus | Iconic: full cruciform, arms level (x −90, z ±90), head up 8°, perfectly still. Release: a single 2° lift of the whole upper body. | Hands at the extreme frame edges, held; release lifts them 2° and settles. |

Cancel: arms lower together, head lowers last.

### 7.5 Mortem — reach, hook, haul

Verb: reach out, then drag inward. Silhouette: a hunched taker.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm reaches forward low (x −80, z 0), body pitches 4° forward, head down 5°. Release: the arm hauls back to the hip (x −30) with a 3° body pitch back. | Lead hand reaches to centre, then drags down and in toward the camera's chest. |
| Mediocritas | Both arms reach; the support arm hooks across (z +30). Release hauls both to the hips. | Both hands reach, then drag back and down. |
| Summa | Adds 7° forward pitch, stance ±7, head down 8°. Recovery: arms fall to the sides and hang 2 ticks before returning. | Hands reach far, drag back past the bottom of the frame, then rise to rest. |
| Magister | Strain: the pitch deepens with charge (4° → 12°) and the arms curl inward. Loop: a slow 3° rocking haul at period 60. | Hands slowly curl closed and inward with charge. |
| Perfectus | Iconic: bowed (pitch 10°), arms hanging slightly forward, head down 12°. The cadaver anatomy performs the extraction. Release: a single 3° deeper bow and recover. | Hands low and open at the bottom of the frame, trembling very slightly; release curls them. |

Cancel: the body straightens too quickly, arms relax.

### 7.6 Congeatio — close, decelerate, lock

Verb: slow contraction ending in a hard simultaneous stop. Silhouette: a figure freezing mid-gesture. Easing is ease-out only; the final keyframe arrives with zero velocity and every part stops on the same tick.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm closes across the chest (x −75, z +20) decelerating; head lowers 3°. Release: every moving part stops; then a 1-tick micro-snap outward (x −85). | Lead hand slides slowly to centre and stops dead; release is a 1-tick outward snap. |
| Mediocritas | Both arms close and cross (z ±35 inward). Legs plant (z ±6). | Both hands close toward centre and stop. |
| Summa | Adds a 3° forward pitch that stops with the arms; stance ±9. Recovery is slow (18 ticks) and linear, like thawing. | Hands close, stop, then thaw open slowly. |
| Magister | Strain: the closed pose tightens with charge (arms cross further, head lowers) and the stop gets harder (release snap amplitude grows). Loop: no loop; the hold is absolute. | Hands tighten with charge; no idle motion at all. |
| Perfectus | Iconic: arms half-raised and crossed, head bowed, no motion whatsoever; the crystal anatomy grows. Release: nothing moves on the body except a 1-tick 1° settle. | Hands crossed at mid-frame, utterly still. |

Cancel: the pose fractures; arms drop in one abrupt 3-tick move.

### 7.7 Ferric — plant, brace, drive

Verb: lower the weight, then hammer. Silhouette: a smith at an anvil.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Lead arm rises high (x −110, z −10), body lowers 3° (pitch forward). Release: arm drives down to chest height (x −60) with a hard stop and a 2-tick settle. | Lead hand rises to the top of the frame, then drives down to lower-centre and stops. |
| Mediocritas | Support arm braces horizontally across the body (x −80, z +40); lead arm hammers down across it. | Support hand holds a level brace at lower-centre; lead hand hammers down to meet it. |
| Summa | Stance ±10, body lowers 5°. Release has a 2-tick recoil upward after the stop. | Hands as above with a small near-recoil. |
| Magister | Strain: the raised arm climbs higher with charge and the stance widens; head lowers. Loop: a slow 2° settling of weight at period 60. | Lead hand climbs with charge. |
| Perfectus | Iconic: monumental plant, arms low and level (x −60, z ±45), head level, body lowered 6°. The foundry anatomy aligns. Release: a single 2° drop of the whole body, held 2 ticks, then recover. | Hands low and level at the frame edges; release drops them 2° and holds. |

Cancel: the arms unload downward and the stance narrows.

### 7.8 Tenebris — turn away, cross, unfold late

Verb: conceal behind one arm, counter-turn the head, release from the mirrored side. Silhouette: a figure facing the wrong way.

| Rank | Third person | First person |
|---|---|---|
| Humilis | Body twists away (yaw −12°), lead arm crosses the face (x −95, z +35), head counter-rotates toward the target (+10°). Release: arm sweeps outward to the opposite side (z −30) while the body untwists late (2 ticks after the arm). | Lead hand crosses over the crosshair from the outside, then sweeps away to the opposite edge. |
| Mediocritas | Support arm trails behind low (x +20, z +15). Release: the trailing arm leads the unfold. | Support hand drops out of the bottom corner; lead crosses; release swaps their positions. |
| Summa | Twist deepens (−18°), stance offset (one leg forward z +8, the other z −2). Release: twist overshoots +4° before settling. | Hands cross, then both slide to the opposite side of the frame. |
| Magister | Strain: the twist and head counter-rotation grow with charge. Loop: a slow 3° sway at period 56. | Hands drift slowly in opposite directions while held. |
| Perfectus | Iconic: the player is still and turned 20° away, one arm crossing, head toward the target; the blood-shadow anatomy casts. Release: nothing on the body except a 1-tick 2° head turn. | Hands held crossed and off-centre; the shadow does the work. |

Cancel: folds into alignment, the body untwisting before the arms lower.

## 8. Cast-type grammars

The server already distinguishes these; the client only needs to play them differently.

### 8.1 Quick

Gameplay has executed before the client sees RELEASE. The track plays release → recovery. Two additions:

- **Local flick (builder's call):** on the client that pressed the key, play OPENING locally for up to 3 ticks on key-down. If no RELEASE with a newer sequence arrives within 5 ticks, play CANCEL. Other players never see the flick. This is purely local state in `CastingAnimationClientState` and sends nothing.
- Humilis quick casts never move the support arm, legs or torso, so spamming Blood Shot in combat does not wobble the body.

### 8.2 Charged

OPENING plays on the first heartbeat. HOLD samples the base track's hold keyframe blended toward its **strain** keyframe by `CastingPlayback.charge()` (held / required, which the packet already carries). At full charge, strain is 100%. Damage reducing charge eases the pose back without restarting the clock. RELEASE plays from the current strain. Repeating charged casts that fire while held re-enter HOLD from RELEASE without replaying OPENING.

### 8.3 Continuous

OPENING, then HOLD plays a **loop** segment: a small periodic offset from the hold keyframe (amplitude ≤ 4°, period from the track) so a channel reads as effort rather than a freeze. Upkeep pulses never retrigger OPENING. Congeatio has no loop; its hold is absolute.

### 8.4 Passive

Manual activation plays a short **on** gesture (6-tick opening, 4-tick release, 6-tick recovery) and manual dismissal a distinct **off** gesture. On gestures draw inward (the power settles into the body); off gestures push outward (it is released). Automatic triggers keep their existing effect accents without taking the limbs. There is no persistent mantle pose; passives must not alter idle posture.

## 9. Aim layer (purpose endings and Still Art endings)

The aim layer replaces the 8–18% purpose blend. From two ticks before RELEASE through the end of RELEASE it owns the lead arm and head at full weight, then fades out over RECOVERY. The base track keeps the torso, support arm and legs. First-person aim targets own the lead hand's final position.

| Purpose | Lead arm (third) | Head | Lead hand (first) |
|---|---|---|---|
| Projectile | Straight at the aim (x −90 + pitch, z 0), locked for the whole release | Toward the aim | To the crosshair, slightly near; a 1-tick overshoot |
| Self | Returns to the chest (x −60, z +10) | Level | Draws in and down to the chest line |
| Area | Sweeps outward (z −60) with the support arm mirroring | Level, slightly up | Sweeps to the frame edge |
| Targeted | Points at the target (x −90, z 0) and holds 4 ticks into recovery | Toward the target | To the crosshair and holds |
| Construction | Sweeps across (z −10 → −55) to establish width | Down 4° | Slides horizontally across the lower frame |
| Conjuration | Takes the weapon's carrying line (x −70, z −15) | Level | Moves to the vanilla held-item position so the weapon appears in-hand |
| Travel | Narrows across the body (z +25), body yaw −6° | Toward the destination | Crosses toward centre; the camera does not move |

Still Art endings use the same mechanism with their own table in §11.3.

## 10. Bespoke overrides

A bespoke override replaces the whole base track (both views) but still takes the aim layer unless the override says otherwise. Existing bespoke styles are re-authored, not dropped.

Tier A (in scope)

| Id | School / type | Why bespoke | Sketch |
|---|---|---|---|
| `crimson_coronation` | Animus Perfectus charged | the Perfectus iconic | Both arms raise a crown shape above the head during charge; release lowers them to the cruciform guard as the heart anatomy beats. |
| `synaptic_storm` | Ductilis Perfectus charged | iconic | Rigid symmetric hold; staccato snaps at a charge-driven period; release is one whole-body snap. |
| `white_verdict` | Lux Perfectus charged | iconic | Cruciform; the head rises steadily with charge; release is the 2° lift with the wings fully open. |
| `funeral_bell` | Mortem Perfectus charged | iconic | Bowed; arms hang; on release the body straightens once like a bell swinging, then re-bows. |
| `absolute_stillness` | Congeatio Perfectus continuous | iconic | Crossed, still; the loop is explicitly empty; cancel fractures. |
| `lodestone_collapse` | Ferric Perfectus charged | iconic | Monumental plant; arms rise 10° with charge as if lifting weight; release drops them with a 2-tick hold. |
| `eclipse_well` | Tenebris Perfectus charged | iconic | Turned away; the head counter-rotation grows with charge; release is the 1-tick head turn. |
| `phoenix_debt` | Flammeus Perfectus passive | iconic | On: arms fold across the chest; off: they throw open. Uses the existing Phoenix Debt geometry for the body. |
| `hematic_riposte` | Animus Mediocritas quick | parry | A guard: support arm across the chest, lead arm raised; release is a short outward parry sweep timed to the Riposte arm geometry. |
| `blood_rush` | Animus Humilis quick (self) | lunge | Arms pump back once, body pitch 6° forward; recovery 8 ticks only so it never fights the dash. |
| `deadly_gaze` | Ductilis Humilis charged | head-led | Arms stay down; the head leads: it locks on the target and tilts further with charge; release is a 1-tick head snap. First person: hands stay at rest. |
| `sanguine_marionette` | Ductilis continuous | puppeteer hands | Both hands raised at chest height, fingers-down carriage; the loop is a slow alternating 3° lift. Must stay consistent with the crossbar item renderer. |
| `ironhearted`, `umbral_step`, `umbral_reversal`, `summon_avatar*`, `conjure_*`, four Saint canons | existing styles | re-author | Same intent as today, authored as full tracks with first-person hands. |

Tier B (list now, build if time allows)

`lignum_mortis` / `canopy_mortis` / `worked_lignum` (continuous ground-touch: body lowers, lead arm down toward the ground), `vascular_dowsing` (continuous: lead arm level like a dowsing rod, head scanning), `furnace_veins` / `living_circuit` / `iron_choir` / `carrion_communion` / `penumbral_drift` / `radiant_wake` (Magister continuous channels with school-specific loops that differ from the Magister base), `blackhearted` / `sovereign_instinct` (Magister passives with stronger on/off gestures).

## 11. Unstained lane: Still Arts

### 11.1 Grammar

A Still Art is not forced; it is allowed. Where a Harbinger contracts and drives, an Unstained **settles**: slow, symmetrical, level, ending stiller than it began. Nothing clenches. Hands are open. The head is level or slightly bowed, never thrown back. The silhouette is a figure standing in still water.

Timing is inverted from the Harbinger lane: release is slow (8–12 ticks) and recovery is a long **settle** (14–24 ticks) that ends at rest with zero velocity, so the stillness is the memorable frame.

### 11.2 Stage bases

Still Arts are all quick casts: the server performs the action on key press. The base track therefore has a local opening flick (0–2 ticks), release and settle. Stage follows `EnumClarityStage`.

| Stage | Third person | First person |
|---|---|---|
| Awakened | Lead arm lifts to a level open palm at chest height (x −75, z −10); head level. Settle: the arm lowers over 14 ticks. | Lead hand rises to lower-centre, palm toward the world, level. Support hand idle. |
| Discerning | Both arms lift to level open palms (z ±20), shoulders square, head level. Settle 16. | Both hands rise level and symmetric at the bottom third of the frame. |
| Vigilant | Both arms lift; the body straightens (pitch −2°), stance narrows to zero (feet together, legs z 0 forced), head bows 4°. Settle 18. | Hands rise a little higher and hold; a 1° slow sink during settle. |
| Resolute | Arms lift to shoulder height (x −90, z ±35), palms forward, head bows 4°. Release is a single slow 2° lowering of both arms, like a bell damping. Settle 20. | Hands rise to mid-frame edges and lower together slowly. |
| Enlightened | Arms rise to a level open stance (x −90, z ±60) and the body is perfectly still; the world moves around it (pale accents, §11.5). Settle 24. | Hands at the frame edges, still; settle is a slow sink. |

### 11.3 Ending kinds

Each art declares an ending kind; it is the Unstained aim layer.

| Ending | Arts | Lead arm / head (third) | Lead hand (first) |
|---|---|---|---|
| Rebuke | Silver Rebuke, Autoimmune Edge | Palm pushes forward slowly (x −90, z 0); head level | Pushes to the crosshair, slow, no overshoot |
| Hush | Lethean Mute, Quietus Bell | Both palms turn downward and lower 10° | Both hands lower slowly, palms down |
| Stillness | Still Pulse | Arms stop level and hold 6 ticks into settle | Hands hold level |
| Sight | Pale Diagnosis | Lead hand rises to the brow (x −120, z +15); head turns 6° scanning left then right over release | Lead hand rises to the top-centre, then sweeps once across |
| Shear | Memory Shear | One slow horizontal cut at shoulder height (z −40 → +10) | A slow horizontal sweep across the crosshair |
| Step | Absolving Step | Arms trail back 15° as the body leans forward 4°; recovery 8 ticks only | Hands trail to the bottom corners |
| Intercession | Pale Intercession | Both arms open outward and slightly up (z ±50, x −95), palms up; on recall, the same reversed | Hands open to the upper edges, palms up |

### 11.4 Server plumbing

`StillArt.performAction` publishes a RELEASE presentation after a successful `action.cast(...)`, through a new `CastingAnimationManager.releaseStillArt(player, art)`. The packet carries `lane = UNSTAINED`, `stage = art.getRequiredStage()`, `ending = art.getEnding()` and a null school/rank/purpose. `canPresent` for the Unstained lane checks alive, not paralysed, clarity unlocked and the art known. There are no sustained Still Arts, so no heartbeats are needed; if one is ever added it uses the same OPENING/HOLD path.

`StillArt` gains an `ending` field (enum `StillArtEnding`) set in `StillArtInit`; `StillArt.BLANK` uses `STILLNESS`.

### 11.5 Accents

Hands: a thin pale ring (white 0xF4F6F8 with a silver edge 0xC9D1D9) drawn with the existing mesh primitives on the `LUX` material, pulsing once on release and dissolving through settle. Body (Discerning and above): a still-water ring on the ground at the feet, radius growing with stage (0.6 → 1.6 blocks), drawn as a flat translucent ribbon that expands once and fades. No blood tint anywhere in this lane. Reduced effects: Minimal removes the ground ring; the hand ring stays.

### 11.6 Particle sweep

The Still Art actions in `StillArtInit` spawn vanilla END_ROD, SNOWFLAKE and WAX_OFF particles. Those are replaced with `HemoParticleData.glow` motes (pale, small, slow) as part of this work, and a source-contract test asserts no `ParticleTypes.` remains in `StillArtInit`. The gameplay of each action is otherwise unchanged.

## 12. Track model and authoring

### 12.1 Track

A track is a Java-authored record, built with a small fluent builder, consisting of named segments: `opening`, `hold`, `strain` (optional), `loop` (optional: amplitude pose + period), `release`, `recovery`, `cancel`, `on`, `off`. Each segment is a list of keyframes `(tick, pose, easing)`; sampling is linear between keyframes with per-keyframe easing (`LINEAR`, `SMOOTH`, `EASE_OUT`, `HOLD` for staccato steps). Each segment knows its duration. A pose is the existing `CastingPose` (six part rotations) plus a body pivot pitch (§13.1).

First-person tracks use `HandPose(lead, support)` where each hand is a translation (x, y, z in camera units) and rotation (x, y, z degrees). They share the segment structure and clock.

Sampling and blending live in a pure `CastingTrackRules` class with no Minecraft types. `CastingClips` becomes the registry of authored tracks (`CastingTracks`), one static method per school with the five ranks, one per bespoke style, one per Still Art stage, and tables for aim layers. Formatting follows the existing dense style in `CastingClips`.

Java rather than data files is the builder's call: it matches every other authored animation in the mod, needs no loader or schema, and JVM tests can enumerate every track directly. If live tuning proves painful, a later JSON reload layer can wrap the same record.

### 12.2 First-person rendering

`CastingAnimationClientEvents.hand` applies the full hand transform (translate then rotate) in camera space. Humilis and Awakened only affect the main hand; the off hand renders vanilla. The held-item path (`hemomancy$renderCastingHand`) is kept so a held item moves with the posed hand. The aim layer's Conjuration ending drives the lead hand to the vanilla item position before the weapon appears.

Rules:

- No hand may enter the central 12% of the frame during HOLD. Release may cross it for at most 2 ticks (projectile and targeted overshoot).
- Hand translation is clamped to ±0.35 blocks horizontally and ±0.25 vertically from the vanilla rest so hands never leave the frame entirely except on Travel and Step endings.
- The camera never moves; aim is never changed.

### 12.3 Preview command

`/hemocastpreview` gains optional arguments: style, cast type (`quick|charged|continuous|passive`), charge fraction, and a `still <stage> <ending> <phase>` form. Existing argument forms keep working. The preview still sends nothing and spends nothing.

## 13. Third-person rendering details

### 13.1 Waist pivot

Body pitch is re-enabled by pivoting about the waist rather than the neck: when the track pitches the body by θ, the body part is rotated by θ and translated so its bottom edge stays at the leg attachment (y offset by 12·(1−cos θ), z by −12·sin θ in model units), the head and arms move with it, and the sleeves and jacket copy the result. A JVM test checks the waist point stays within 0.05 model units of the rest position for every authored pitch. Pitch is capped at ±14°.

### 13.2 Locomotion and posture

Unchanged rules: standing legs join the stance, walking fades them to locomotion by horizontal speed, riding, swimming, crawling and Elytra keep vanilla body posture and apply arms only. Vigilant and above force legs to z 0 only while stance weight is 1.

### 13.3 Priority

`CastingPlayerPose.apply` runs first in `applyThirdPersonPose` and the later poses still override it. Arm availability (`armAvailable`) is unchanged. Left-handed players mirror the whole track including the first-person hands.

## 14. Verification

JVM (rules and tracks)

- Every Harbinger school × rank and every Still Art stage has both views and every required segment; segment durations are within the §6 budgets.
- Continuity: the last pose of opening equals the first of hold; release starts from hold (or from strain at a given charge); recovery ends at `ZERO` with zero velocity; cancel ends at `ZERO`.
- Bounds: arm rotations within ±130° x, ±95° z; body pitch within ±14°; first-person translation within the §12.2 clamp; hold never enters the crosshair zone.
- Staccato: Ductilis steps change pose discontinuously at step boundaries and hold between them; Congeatio final keyframes arrive with zero velocity and all parts stop on the same tick; Perfectus holds have zero body motion for 240 ticks (Ductilis allows its authored snaps only).
- Charged strain is monotonic in charge; continuous loops stay within amplitude; passive on/off are distinct.
- Mirroring, aim-layer fade-out, waist pivot invariant, playback phase transitions with track-defined durations, sequence guard.
- Every nonretired manipulation and every Still Art resolves explicit presentation metadata (extends the existing registry-coverage test).

Source-contract

- `StillArt.performAction` calls the Still Art release; `StillArtInit` has no `ParticleTypes.`; the packet encodes lane/stage/ending; the preview command registers the new forms; `wiki/Casting-Animations.md` names every bespoke style.

GameTests (server)

- Still Art release publishes to the caster and trackers once per successful cast and not on refusal (unknown art, cooldown, stage too low).
- Existing casting GameTests pass unchanged (quick payment/release, charge cancel/reduce, continuous upkeep, late tracking).

Live

- `./gradlew.bat test --tests '*Casting*Test' --tests '*StillArt*Test'`, `runCastingAnimationGameTestServer`, `alphaCheck`.
- The two-client review fixture (`tools/casting-animation-review.init.gradle`) captures every school/rank hold and release in both views, every bespoke style, every Still Art stage, both skin models, armour, occupied hands, walking, crouching and cancel. Screenshots are inspected before the work is called done.

## 15. Documentation

- Rewrite `wiki/Casting-Animations.md` for the new grammar; add a Still Arts section; append a dated entry to `wiki/Casting-Animation-Validation.md`.
- `docs/HEMOMANCY_REFERENCE.md` §15.1 gains one paragraph on Still Art presentation (lane, stage, ending) and the status table row references it.
- `docs/done/HEMOMANCY_CASTING_ANIMATION_CONCEPTS/README.md` gets a two-line note pointing here as the current contract.
- `docs/MANIPULATION_VISUAL_CATALOGUE.md` lists the Still Art accents.

## 16. Decisions

Builder's calls (made here; the user can overturn any)

1. Java-authored tracks with a builder, not JSON data files (§12.1).
2. Re-enable body pitch via a waist pivot capped at ±14° (§13.1).
3. Quick casts get a local 3-tick flick on the casting client only (§8.1).
4. Purpose becomes a full-weight aim layer owning the lead arm and head (§9).
5. Still Arts are a lane on the existing packet and manager rather than a parallel system (§11.4).
6. The vanilla particles in `StillArtInit` are swept as part of this work (§11.6).
7. Tier A bespoke set is the eight Perfectus manipulations, four gameplay-driven cases and the nine existing styles (§10).

Decisions for the user **(user)**

1. **How far from restrained?** The restrained revision capped gestures at chest height and removed hold motion after a torso artifact. This design brings back torso pitch (waist-pivoted), small hold loops and larger rank-scaled gestures. Say if you want it kept closer to the current restraint, or pushed harder (bigger Summa/Magister wind-ups).
2. **Quick-cast local flick:** keep (default) or drop, since it is the one visual that can show before the server confirms the cast.
3. **Tier B scope:** build the Magister continuous channel loops and ground-touch continuous casts now, or leave them on the base tracks for a later pass.
4. **Still Art ground ring:** include the still-water ring at the feet (default) or keep Still Arts to hands and body only.
5. **Player animation library:** this design stays library-free. If you want finger, wrist or spine articulation, that is a separate decision with a dependency and is not covered here.

## 17. Size

A project. Roughly: track model and rules (small), 40 Harbinger tracks in two views (large, mostly authoring), aim layers (small), 21 bespoke tracks (medium), Still Art lane end to end (medium), first-person renderer and waist pivot (small), preview, tests, docs and live review (medium). The implementation plan sequences it so the engine and one school land first and the rest is authoring against a working preview.

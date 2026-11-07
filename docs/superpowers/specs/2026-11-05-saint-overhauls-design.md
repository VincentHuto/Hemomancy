# Saint fight overhauls: one combat kit, four fights

Date: 2026-11-05. Answers OPEN_QUESTIONS D-2 ("per-Saint combat and visual overhauls"). Status: umbrella design. The
four per-Saint designs are written from it by the design run (see "The workflow") and come back to the user before
anything is built.

The user asked for this on 2026-11-05: a unified Saint/boss attack system "if it makes sense", and a workflow of Opus 5.5
high agents, one on each of the four fights' combat and visual overhaul. The user skipped the question round, so every
call below not quoted from a ruling is mine and is listed in "Calls I made".

## Why

The four Saints are the D8 capstone fights of both endgame paths, and they are the thinnest boss code in the mod.
About 3,900 lines cover all four, against about 12,600 for the Mycophant and Vesper. Today each Saint is a vanilla
`MeleeAttackGoal`, one or two fixed-cadence powers ("every 120 ticks a rot nova") and the same two-move
`SaintSecondaryAttacks` cycle, with ground warnings drawn from a fixed `SaintAttackPattern` table. They read as timer
fights. The user already rejected "2 things on repeat" for the first Mycophant and approved the expansion with
Ebrietas and FromSoftware bosses as the bar. Annetta's rebuild is the smoothness bar for every later boss.

The user's notes for each overhaul (rulings of 2026-10-26, `SAINT_MAUSOLEUMS.md` "Rulings that bind"):

- **Hemorath:** a proximity blood drain, a leaking absorbed meter, iron-spike patches on the bone.
- **Seraphae:** a mechanics cleanup, wings as nervous projections, white vessel light with separate gold nerve flashes.
- **Putriciel:** fire only in a cleansing interval, and a palette with no red.
- **Velorum:** a stillness meter that stops players resting, with openings that halt it, and a frozen congregation the
  player can wake to fight beside them.

## What exists and stays

- `SaintTrialEntity`: the rise (`BossIntroWindow`, `SaintRise`), the bar (`BossBarBinding` with a frame each), the tomb
  binding, the one ending (`endTrial`: one loot roll, `SaintSarcophagusBlock.restoreDormant`), the forced-awakening
  enrage record (`SaintEnrageRules`, skipped Chapels), save/load of all of it. Every Saint keeps extending it.
- Presentation: the four bar frames, `SCENE_SAINT_RISE`, the Hemorath "Vessel Empties" card, `BossScenes`,
  `BossSceneCatalog`, `BossSceneDirector`.
- Timing: `BossReplayClock`, `BossMoveTimeline`. Telegraph helpers: `BossTelegraphs`. World materials:
  `ManipulationVisuals.Form` `HEMORATH_RELIQUARY`, `SERAPHAE_RELIQUARY`, `PUTRICIEL_OFFERING`, `VELORUM_RIME` and their
  `client/render/world/*Geometry` classes.
- Models: `tools/model_export/<saint>.py` with `<saint>_anim.java`, `client/model/entity/boss/<saint>/`.
- The chambers: 37 by 27 by 17, a clear 15 by 15 boss floor, a three-step dais and apse, `CHAMBER_SOCKETS` filled by
  each theme's `chamber_sockets` hook in `tools/worldgen/mausoleum/themes/<saint>.py`. Hemorath's four basins,
  Seraphae's containment anchors (`SeraphaeAnchorRules`), Putriciel's pyres and spore shelves
  (`PutricielChamberLightingRules`), Velorum's `frozen_devotee` blocks.
- The endings by ruling: Hemorath overloads or is worn down and is exsanguinated; Seraphae is contained at full
  integrity; Putriciel rests after the fifth absolution; Velorum's body fails and he reverts to dormancy.

What gets replaced: `MeleeAttackGoal` and the stroll goals on Saints, `SaintSecondaryAttacks`, the fixed
`SaintAttackPattern` table (its footprints move into the kit), and the ad-hoc goals and cadence timers
(`HematicCollapseGoal`, `EmptyPulseGoal`, the Putriciel/Velorum tick counters). Each Saint designer decides which old
moves survive as kit moves.

## Goals

1. Four fights that each feel like their Saint's thematic sentence: *Hemorath consumed*, *Seraphae restored*,
   *Putriciel purified*, *Velorum preserved*. A player should be able to say what each fight is about after one try.
2. Every fight is a puzzle fight won through its Saint's mechanic (overload, containment, absolution, stillness), with
   wearing the body down as Hemorath's second road only.
3. Readable, committed moves on the Mycophant's model: a wind-up that tracks, a commit tick that locks the aim, an active
   span, a recovery that is the punish window. Strings two deep. Position-aware choice. No move on a fixed timer.
4. Each fight uses its chamber: the set pieces the mausoleums already place take part, and what the fight changes is
   put back when the trial ends.
5. A forced awakening fights with the deeds of the skipped Chapels as moves and rules, not only stat bonuses.
6. Visual overhauls to the Riposte standard (`docs/MANIPULATION_VISUALS.md`) with the Hemomancy/HutosLib toolkit: model
   and texture passes through the generators, Java animation per move, world geometry for every telegraph and impact.
7. One shared kit, so the four builds share the plumbing and differ only where the Saints differ.

## Non-goals

- The Saint gifts and the Canon Memory rework (D-1). Rewards stay the loot tables as they are.
- Moving the Mycophant, Vesper, Annetta or the Court minibosses onto the kit. The generic parts are shaped so they could
  move later; G-2c (one boss definition) stays pending.
- Chapels, the ring door, hallowed ground, the ward, placement and the outer halls. Chamber edits are limited to what
  the fight itself needs, made through that Saint's theme generator.
- New audio files. Use the mod's registered sound events and vanilla events at chosen pitches, as today.
- A balance pass over the rest of the mod.

## Anti-goals (a failure even if it works)

- A Saint that still reads as a timer, or as "2 things on repeat".
- Focal moments carried by vanilla particle clouds; thick strands, noise-contour lines or any overlay the user would call
  "spaghetti" (feedback of 2026-11-04: overlays stay subtle and partly clear).
- Red anywhere on Putriciel outside the cleansing fire; fire on Putriciel outside the cleansing interval.
- Velorum with literal time manipulation, or as a martyr or caretaker. Seraphae as a willing villain. Hemorath shown only
  as bloated.
- A Saint that can be killed, or an ending that skips `endTrial`.
- A chamber left changed after the trial ends, unloads or the server stops.

## Decision: one Saint combat kit

It makes sense, and only for the right parts. All four fights need the same machinery: a move state machine with
shared server/client footprints, a picker, a signature meter, a proximity presence, punish windows, phases, the chamber,
and the enrage deeds. Today each Saint carries its own half-version of these. What does not unify is everything that
makes a Saint itself: its moves, its meter's rules, its presence effect, its visuals. The kit therefore owns the plumbing
and calls into a per-Saint table for everything else.

Generic pieces that any boss could use live in `common/entity/boss/`; Saint-only pieces live in
`common/entity/boss/saint/`. Per-Saint code stays in `common/entity/boss/saint/<saint>/`.

### The kit's parts

1. **`BossFootprint`** (`common/entity/boss/`). The ground shapes, from `SaintAttackPattern.hits` grown into a small
   sealed set: lane (with side lanes), line, cone sweep, expanding ring wave, disc at a point, cross, scattered discs, and
   a path (a list of segments for moves that travel). Each answers `hits(relative position, aim, age)` on the server and
   exposes its draw data so the client telegraph draws the same shape it tests. Height bands are part of the footprint.

2. **`SaintMove`** (interface; each Saint has an enum of its moves implementing it). A move declares: its id, a
   `BossMoveTimeline`, its footprint, its aim rule (track until commit, lock at start, or lead the target), the range
   bands and sectors it is picked from with weights, its cooldown, the phases it belongs to, follow-ups with their link
   gap and chance, whether it opens a punish window in its recovery and how long, and two hooks: `onTick(context, age)`
   for its Saint-specific effect and `onHit(context, target)` for its damage and status. Damage goes through a per-Saint
   damage type (`hemomancy:saint_<name>`, with death message keys) and `ManipulationCombatHelper.canHarmFor`.

3. **`SaintCombatController`**. The one owner of the Saint's locomotion, facing (turn-rate limited) and move timeline,
   on the `MycophantController` model: roam, approach or hold; pick; wind up; commit; active; recover; chain. Range bands
   (HUG, NEAR, MID, FAR) and sectors (FRONT, FLANK, REAR) with anti-repeat history and pressure (kiting, hugging,
   turtling), follow-ups at most two deep. A stagger or a phase change never cuts an active move; it queues until the
   recovery boundary. Locomotion is a per-Saint strategy (a stalk, a chained glide on a tether, a shamble, near-stillness).
   The controller syncs one block of entity data that every client part reads: move id, move start tick (read through
   `BossReplayClock`), locked aim, target id, phase, opening state.

4. **`SaintMeter`**. Each Saint's signature resource with one shape: a value, a maximum, thresholds, a leak or decay
   rule, and whether it belongs to the Saint or to each player. Hemorath's absorbed blood and Seraphae's containment
   integrity are the Saint's; Putriciel's absolutions count to five; Velorum's stillness belongs to each player. The
   Saint's meter syncs as entity data; per-player meters sync through one new clientbound `SaintMeterPacket` (the only
   new payload; the kit build bumps `PacketHandler.PROTOCOL_VERSION` once and rewrites
   `src/test/resources/network/payload-registry.txt`). One HUD layer, `SaintMeterLayer`, registered in
   `GuiLayerRegistrar` and wrapped by `HudVisibilityRules`, draws whichever Saint the player is fighting, under that
   Saint's bar frame, with per-Saint art (a `SaintMeterStyle` per Saint, painted like the bar frames).

5. **`SaintPresence`**. The proximity field each Saint carries: radius, cadence, falloff, line-of-sight rule, a
   per-Saint effect hook, and a subtle ground read drawn through `WorldEffectDispatcher`. Hemorath drains, Seraphae
   over-heals, Putriciel colonises, Velorum chills.

6. **`SaintOpening`**. A punish window with an entry tell, a duration, what counts during it (a hit, a held Blood
   Projection, a cast of some school), a damage-taken multiplier, and what it feeds (the meter, a stagger, a node). Every
   win condition runs through openings: the Saint decides when it is open; the player decides whether to take it.

7. **`SaintArena`**. Resolves the chamber from the tomb position and facing at the rise: chamber and boss-floor bounds,
   the dais, the sockets, and the set pieces found by one scan of the chamber box (basins, anchors, pyres, shelves,
   devotees). Every block the fight changes goes through the arena, which records the original state and restores it in
   `endTrial`, in `remove`, and on load of a Saint whose trial already ended. The arena also leashes the Saint to its
   chamber and resets the trial, unrewarded, when no challenger has been inside the chamber for 600 ticks.

8. **`SaintPhaseTable`**. Phases by health fraction or by meter threshold, each with its move set, locomotion tweaks, bar
   colour and name, and an optional phase card (`BossScenes`, 50 ticks, as Hemorath's "The Vessel Empties").

9. **`SaintDeeds`**. What each skipped Chapel lends a forced awakening. The current stat bonuses stay as the floor
   (+30% attack, +15% speed, Downfall +50% health, Repose +20% speed, Beholding Resistance, Litany Regeneration). On
   top, each Saint designer gives each of its three Chapel kinds a deed in that Saint's own language: a move added, a
   rule hardened, an opening shortened. "Skip the Downfall and it fights with its fall; skip the Repose and it never
   pauses" (`LORE_REFERENCE.md` §10).

10. **Client side.** `SaintTelegraphRenderer` (a pass under `WorldEffectDispatcher`) draws the active move's footprint
    from the synced move block in the Saint's material family; per-Saint impact and move geometry extend the existing
    `*Geometry` classes. Each Saint's `AnimState` poses from the synced move id and move tick, with one Java keyframe
    track per move written beside the generator (`<saint>_anim.java`, Annetta-style Catmull-Rom keys).

The kit replaces `SaintSecondaryAttacks` and the Saints' vanilla goals. `SaintTrialEntity` gains the controller, the
arena and the meter as fields, so the rise, the enrage and the ending keep one home. The kit is built and tested before
any Saint moves onto it, and is frozen while the four Saints are built (see "Lanes").

## Rules every Saint follows

From rulings and `AGENTS.md`; quoted here so the four designs need not repeat them.

- Tendencies: Hemorath ANIMUS + FERRIC, Seraphae LUX + DUCTILIS, Putriciel MORTEM + FLAMMEUS, Velorum CONGEATIO +
  TENEBRIS. Titles and pronouns: Hemorath The First Saint, he; Seraphae The Chain Saint, she; Putriciel The Befouled
  Saint, they now (he in life); Velorum Saint of the Endless Hour, he. "Hollow Vessel" never appears in player text.
- No Saint can be killed; every ending goes through `endTrial` once.
- Putriciel's fire is the Flammeus look (crimson body, pink-white heat, black soot tips), never green or yellow in any
  fire texel (`PutricielFireResourceTest`). Outside the fire, Putriciel's palette is ochre, olive, mould green, muted
  yellow, pale cream and blackened charcoal, with no red. No bone-brick trims on Putriciel.
- Seraphae: white Lux light in the vessels, gold Ductilis flashes along the nerves, kept apart.
- Velorum: his ice is Frozen Cruor (dark oxblood and black-red blood-ice under pale frost; blue only as frost light), his
  dark is Tenebris liquid darkness with torn, smoky edges (`LORE_REFERENCE.md` tendency canon); dignified rigidity, no
  frenzy; near-time wording only. Corrected by the user on 2026-11-06: the first builds read as "a generic ice wizard".
- Hemorath: frail, sunken, desiccated at rest; iron through the bone; no copper, brass, bronze or silver.
- Animation in Java on the move's own clock, never Blockbench; `BossReplayClock` only (`BossAnimationClockGuardrailTest`).
- Models and textures from `tools/model_export/` generators; never hand-edit a generated model; 16 px per block after
  render scale. New mobs (Velorum's woken devotees) use the jointed elbow/knee rig from the generators, never the
  vanilla humanoid.
- Focal effects from the Hemomancy/HutosLib toolkit through `HemoParticleData` and the world passes; a vanilla particle
  may leave a file but never arrive (`VanillaParticleRatchetTest`). Touching a Saint file sweeps its vanilla particles
  out.
- Player text is lang keys (`HardCodedEnglishGuardrailTest`); Saint lines follow the voice rules in `docs/NPC_VOICES.md`
  where a Saint speaks.
- Blood leaves a player only through `BloodAccounting.pay` / `payUpkeep` with a named `Source`.
- Targets through `ManipulationCombatHelper.canHarmFor`; flight through `FlightArbiter`; heal reductions as
  `HealModifier` entries.
- Both D8 paths must be able to win: a Silentium usually wakes the Saint properly; an Apotheos usually forces the door
  and fights enraged while hallowed ground burns them. Neither path gets a fight it cannot finish.

## Fight shape targets (my calls; designers may argue with numbers, not with the shape)

- A proper awakening lasts about four to six minutes for a prepared solo D8 player; a forced one is harder, not longer.
- Each Saint has at least eight distinct moves across its phases, plus its presence and its signature mechanic, and at
  least two strings. Every phase adds something the player has not seen.
- Wind-ups of at least 12 ticks for melee and 20 for area moves; every area move shows its footprint before it commits.
- The signature mechanic is taught in the first minute by the fight itself (a tell, a line, a visible meter), not by
  the Liber.
- Each fight asks for at least one Hemomancy verb beyond hitting: holding Blood Absorption or Blood Projection,
  spending or withholding blood, casting a school, moving or keeping still.
- One to four players: Saint meters scale with the number of challengers, per-player meters do not.

## The four briefs

Each designer expands its brief into a full design. The brief is direction, not a moveset.

### Hemorath: *Hemorath consumed*

The fight is appetite against a leak. His presence drains every challenger nearby (blood from the blood-active, health
from the rest) into his absorbed meter, and the meter leaks all the time, faster as it fills: the overload win means
out-feeding the leak, which means letting him take, or giving him more than he can hold. The meter should show on his
body: the swelling the lore describes returns as he fills (ribs prising apart, the hollow torso filling) and sinks as he
leaks, so the frail silhouette is his rest state and the bloat is something the player causes. Iron-spike patches erupt
on his bone as Ferric moves (iron rakes, floor spikes, a spiked halo). His four chamber basins are the arena's part:
he can draw on them to fill, and the player can drain or deny them. The second road, wearing his body down, stays.
Phase 2 keeps "The Vessel Empties".

### Seraphae: *Seraphae restored*

A containment fight, cleaned up. Her presence over-heals: near her the player regrows too fast, and the excess becomes
growth that slows, roots or bursts. Her wings become branching nervous projections that lash, grasp and convulse; gold
nerve flashes are the tells and the strikes, white vessel light is the healing that hurts. She is chained: her reach is
the chain, and the containment anchors draw it in. Containment integrity rises from what the player does (binding her
fragments, waking anchors, striking in her condensing windows) and decays when left alone. Full integrity ends the
fight with the anchors drawing her wings and hands inward. She reaches for the player out of the old instinct to heal;
the fight should make that reach the most dangerous move she has.

### Putriciel: *Putriciel purified*

Rot, then a cleansing fire, then rot again. The rot phases are olive, ochre and cream with no red and no fire: spore
sacs swelling out of rhythm, shelf colonies, censers trailing rot, the open offering hand, colonisation that builds on
the player. The cleansing interval is the fight's centre: Putriciel's will surfaces, their blood ignites crimson and
pink-white, the growths blacken and burn, the outline of the man shows, the chamber's pyres light. The fire is the most
dangerous thing in the fight and the only time absolutions count: a hit landed in the interval lights one of the five
nodules in the palm. The fire also cleanses: the player's own colonisation burns off if they dare stand near it. Then the
fire dies and the rot comes back stronger.

### Velorum: *Velorum preserved*

He fights from stillness and asks the player to keep moving while everything he does slows them. Each player carries a
stillness meter that rises when they stand, sneak, rest or are slowed, and falls only while they move; at full they are
preserved for a moment, frozen and taking heavy damage, a figure in his congregation. His openings halt it: a lantern
that dims, an invocation held too long, a bow under a wound. The frozen congregation is the arena: `frozen_devotee`
figures around the chamber can be woken. Woken devotees fight beside a Silentium who woke him properly and against an
Apotheos who forced the door (`LORE_REFERENCE.md` §10.8). Darkness is sensory: narrowed sight, muffled sound, never a
blackout. When his body fails, the hood bows and the frost falls out of the light.

## Lanes and shared files

The build runs four Saint builders at once in one working tree, so each owns a lane and the shared files have rules.

**Each builder owns:** `common/entity/boss/saint/<saint>/`; `client/model/entity/boss/<saint>/`;
`client/render/entity/boss/<saint>/`; its own layers in `client/render/layer/mob/` and geometry in
`client/render/world/`; its bar frame and `SaintMeterStyle`; `tools/model_export/<saint>.py` and `_anim.java` and
anything they generate; its textures; `tools/worldgen/mausoleum/themes/<saint>.py` (re-run for that Saint only); new
GameTest and JVM test files named for its Saint; its own design spec.

**Shared, add-only:** `en_us.json` (keys under the Saint's own prefix), `EntityInit` and the renderer and attribute
registrars (a Saint that needs a new mob), `BossPresentationIds` and `BossSceneCatalog`, damage type JSON and lang.
Re-read before every edit; add your lines; never reorder or reformat.

**Not touched by a Saint builder:** the kit's files, `PacketHandler` and the payload registry, the allow-lists,
`build.gradle`, `gradle/review-clients.gradle`, topic docs and references. A builder that needs a kit change writes it
under "Kit follow-ups" in its spec and works around it in its own package; the integration stage folds those in.

**No Gradle while Saints are being written.** Builds in one checkout collide (memory: concurrent builds share build dirs;
`libs/` and `../HutosLib` are not in git, so worktrees cannot build). Compiling, tests and review clients happen in the
serialized stages after the writers finish.

## The workflow

Two runs, with the user's review between them.

**Run 1, design (5 agents, Opus 5.5 at high effort).** Four Saint designers in parallel. Each reads this spec, its
Saint's lore (§10.x), `SAINT_MAUSOLEUMS.md`, `BOSSES.md`, the current Saint code, model generator, animation, client
geometry and chamber theme, `docs/MANIPULATION_VISUALS.md`, and the Mycophant expansion spec as the quality bar, then
writes `docs/superpowers/specs/2026-11-05-saint-<name>-overhaul-design.md`: the moveset table, phases, meter, presence,
openings, arena use, deeds, visual pass, sound, numbers, tests, an ordered build list, and kit requests. A fifth agent,
after all four, reconciles the kit requests into the "Kit API" section of this spec. I then bring the user one screen per
Saint and the questions only they can answer.

**Run 2, build (12 agents, Opus 5.5 at high effort), approved by the user on 2026-11-05.**

1. Kit (2 agents one after the other, the only ones using Gradle): the server half (footprints to the ending, the
   shared edits, the guardrail and JVM tests), then the network and client half (the payload, the HUD layer, the
   telegraph pass, the placeholders, the kit GameTests), leaving the four Saints compiling and unchanged in behaviour.
2. Saints (4 agents in parallel, no Gradle): each moves its Saint onto the kit and builds its design in its lane:
   moves, meter, presence, openings, arena, deeds, model and texture pass, animation tracks, geometry, frame art, lang,
   tests.
3. Compile gate (1 agent): gets the whole tree compiling and the JVM tests green, folds kit follow-ups in, bumps nothing
   the kit stage already bumped.
4. Verify (4 agents, one after another): each runs its Saint's GameTests and the Saint's fight review client
   (`-P<saint>FightReview`), captures stills of every move and phase, and fixes its own Saint.
5. Final review (1 agent): reviews the whole diff against this spec, the four designs and the guardrails; runs
   `alphaCheck`; regenerates `docs/architecture/evidence/source-text-impact-map.csv`; updates `SAINT_MAUSOLEUMS.md`,
   `LORE_REFERENCE.md` §10, `HEMOMANCY_REFERENCE.md`, `BOSSES.md`, the Saints wiki page and `OPEN_QUESTIONS.md` (D-2 moves
   to the Saint doc's "Rulings that bind").

## Calls I made

- A shared kit, with the generic half in `common/entity/boss/` and the Saint half in `common/entity/boss/saint/`; the
  other bosses do not move onto it now.
- The kit's ten parts and their names; one new payload, for per-player meters only.
- Hemorath's swelling tied to his meter (bloat as something the player causes, frailty as rest).
- Putriciel's cleansing fire burning the player's own colonisation off.
- Velorum's preservation as a heavy hit and a brief freeze, not an instant death.
- The leash: an empty chamber for 600 ticks resets the trial unrewarded.
- Deeds as moves on top of the existing stat bonuses, which stay.
- Fight length, move count, wind-up floors and the one-to-four-player scaling rule.
- No new audio files.
- Two runs with a review between, and the lanes and Gradle rules that let four builders share one checkout.

## Kit API

Reconciled on 2026-11-05 from the kit requests of the four designs (`2026-11-05-saint-hemorath-overhaul-design.md`
§18, `...-seraphae-...` §18, `...-putriciel-...` §18, `...-velorum-...` §18). This is what the kit builder (run 2,
stage 1) implements first and what stays frozen while the four Saints are built. Paths are under
`src/main/java/com/vincenthuto/hemomancy/` unless they start with `src/test`, `src/gameTest`, `tools/` or `docs/`.
The ledger at the end of this section says what happened to every request.

### What changes in the ten parts

The ten parts keep their names and jobs. Three things change, because the requests showed the parts were short:

1. **Part 3's rule "a stagger or a phase change never cuts an active move" gains one exception.** An ending
   (`SaintEnding`) and a phase change marked `Cut.IMMEDIATE` cancel the active move at once. Hemorath's first full
   meter is the moment his body fails (Hemorath §3); every ending stops the fight on its first tick (Velorum §11,
   Putriciel §11). Staggers, openings and ordinary phase changes still queue to the recovery boundary.
2. **Part 4 carries more than one number per player.** A per-player meter and a per-player secondary value beside a
   Saint-owned meter both travel in the one payload, with a Saint-defined flag word and a state countdown (Hemorath's
   Tithe debt, Seraphae's growth, Putriciel's colonisation, Velorum's halted, preserved, sheltered and watched
   states). A coarse copy of every challenger's value also rides the Saint's entity data, so a client can draw the
   state of other players (Seraphae's sores, Putriciel's shelves, Velorum's ice shell).
3. **Four supporting pieces join the ten,** because at least two Saints need each and none fits inside a Saint's
   lane: a channel router for held Blood Absorption and Projection (11), a generic boss hold (12), a ground hazard
   entity (13), and a saved ending window (14). The kit also owns the damage pipeline in `SaintTrialEntity` (hit
   classification, damage-taken factor, health floor) so no Saint overrides `actuallyHurt` or `die`.

### Build order and what the kit leaves alone

> Built 2026-11-05 (server half, stage 1a): everything below through section 15, sections 19 to 22's server parts and
> the guardrail are in the tree; the four Saints compile and behave as before (`createKit()` returns null for each).
> Not built here, left to the client stage: `SaintMeterPacket` and `SaintMeterSync` (section 16; the send rules they
> need are in `SaintMeter`), the HUD layer and styles (17), the telegraph pass and styles (18), `SaintClientStyles`,
> the eight placeholders, `client/event/BossHoldClientEvents`, guardrail rules 7 and 8's style half,
> `SaintMeterPacketTest` and the `saint_kit_validation` GameTests. Each section below says where the build differs
> from what is written, and why.
>
> Built 2026-11-05 (client half, stage 1b): sections 16 to 18, the client parts of 12, 13 and 20, guardrail rules 7
> and 8's style half and the kit GameTests are in the tree. The four Saints still compile and behave as before; the
> new layer and pass draw nothing for a legacy Saint (no kit, so no meter packet and no synced move).
>
> Compile gate 2026-11-06 (stage 3): all four Saints are on the kit and the tree compiles with the JVM suite green.
> Kit changes made here:
>
> - `SaintTrialEntity.createKit()` is abstract and `kit()` is never null; the null checks that served legacy Saints
>   are gone, and so is the legacy branch of `actuallyHurt`. `die` refuses every source but `/kill` (a Saint's
>   endings all run through `beginEnding` and `tickEnding`, which calls `endTrial`). `tickRise` is private to the kit's
>   tick. `registerTrialGoals`, `sendFloorRing`, the visual state (`DATA_VISUAL_STATE`, `DATA_VISUAL_TICKS`,
>   `VISUAL_NONE`, `getVisualState`, `tickVisualState`, `holdsVisualState`), the secondary cycle (`DATA_SECONDARY_*`,
>   `tickSecondaryAttacks`, `secondaryTime`) and the unused `reapplyEnrageEffects` are deleted.
> - `SaintSecondaryAttacks`, `SaintAttackPattern`, `client/render/world/SaintPatternGeometry` and their tests are
>   deleted; `ManipulationVisualRenderer` no longer asks the pattern geometry for anchors. Its Seraphae hook lines now
>   pass the school batch (Lux and Ductilis), as her design asked.
> - The legacy allow-list is deleted and `SaintCombatKitGuardrailTest.saintsUseTheKitNotTheLegacyPieces` requires no
>   violations; `SaintRiseTest` checks the kit's tick holds the fight through the rise and that no Saint calls
>   `tickRise`; `BossFrameworkGuardrailTest` checks that `die` keeps the floor and the ending calls `endTrial`.
> - `SaintKitGameTests`' legacy cases use the test kit Saint (the router passes when the Saint declines) and Velorum
>   (the drain refusal).
>
> Kit follow-ups not folded in: no request came from two or more Saints in the same form. The nearest pair is
> forced starts (Seraphae wants `start(move, cut, variant)`, Velorum wants forced starts to respect a veto and set a
> cooldown, Hemorath wants a state with its own move set); they ask for three different hooks, and each Saint works
> round its own today. They stay in each design's "Kit follow-ups" for the integration pass, together with Putriciel's
> phase-change time in `BossBarFrameContext` and `endRecovery()`, Seraphae's `recoveryStartedNow()`, jump-only root and
> unloaded-owner query, Hemorath's dwelling `Path`, hazard end-of-life callback and per-part hit report, and Velorum's
> channel opening factor, `hurtByChannel` grace reset, blows-between-challengers hook and synced set-piece compound.
>
> Seraphae verify stage 2026-11-06: two kit fixes, both found by Seraphae's GameTests.
>
> - **The damage-taken factor never applied.** NeoForge 21.1's `LivingEntity.actuallyHurt` ignores its amount argument
>   and spends the damage container's new damage, so `super.actuallyHurt(source, amount * factor)` changed nothing:
>   every Saint took full damage outside its openings (Seraphae's factor 0, Velorum's 30%, Hemorath's own). The kit's
>   `actuallyHurt` now scales the container's new damage by the factor before calling super.
>   `SaintKitGameTests.theDamageFactorReachesHealth` pins it (a half takes 4 of 8, 0 takes nothing). Saints whose
>   numbers were tuned while the factor was dead may now feel different; Velorum's open failures are worth rerunning.
> - **`BossHold` looked the held player up in the server's player list**, so a GameTest or review patient (added to the
>   level alone) was released as LOST on the tick it was caught. It now asks its own level (`getPlayerByUUID`); the
>   same-level check was already there, so a real player is held exactly as before.
>
> Velorum verify stage 2026-11-06: two more kit fixes in `SaintTrialEntity.actuallyHurt`, both found by Velorum's tests.
>
> - **The health floor was read after the blow landed.** `healthFloor(hit)` was called after `super.actuallyHurt`, so a
>   Saint whose floor depends on its health (Velorum's Still Guard: "the next floor, or the health itself when he already
>   stands on or under it") read the reduced health and let every blow through: the 60% and 25% floors never held. The
>   floor is now read before the blow. Putriciel's constant floor is unaffected; Hemorath and Seraphae use the default.
> - **A `/kill` was floored like any blow.** `die()` lets `GENERIC_KILL` through, but the pipeline kept the Saint at its
>   floor (at least 1 health) first, so `/kill` never killed and never sent the Saint home
>   (`SaintEncounterGameTests.killedSaintReturnsToItsTombUnrewarded`). A `GENERIC_KILL` now skips the factor and the floor.


The kit stage ends with the four Saints compiling and behaving as today. So it **adds** and does not remove:
`SaintSecondaryAttacks`, `SaintAttackPattern`, `client/render/world/SaintPatternGeometry`, `registerTrialGoals`,
`sendFloorRing`, `DATA_VISUAL_STATE`, `DATA_VISUAL_TICKS`, `DATA_SECONDARY_MOVE` and `DATA_SECONDARY_TICKS` all stay,
listed in the legacy allow-list of the guardrail below. A Saint is on the kit when its `createKit()` returns a kit;
until then the kit fields are inert. Each Saint builder moves its Saint off the legacy pieces. The compile gate then
deletes them, makes `createKit()` abstract, empties the legacy allow-list, and removes the old Saint cases in the
shared `SaintPowerGameTests`, `SaintPowerRulesTest`, `SaintEncounterGameTests` and `SaintPatternGeometryTest`.

So that no Saint builder edits a kit registry, the kit creates one **placeholder per Saint for every per-Saint hook it
dispatches to**, in that Saint's lane, drawing the generic look: `HemorathMeterStyle`, `SeraphaeMeterStyle`,
`PutricielMeterStyle`, `VelorumMeterStyle` (`client/screen/overlay/boss/frame/`) and `HemorathTelegraphStyle`,
`SeraphaeTelegraphStyle`, `PutricielTelegraphStyle`, `VelorumTelegraphStyle` (`client/render/world/`). The kit's
`client/registry/SaintClientStyles` maps `EnumSaintType` to them. Each Saint builder rewrites its two in place.

### 1. `BossFootprint` (`common/entity/boss/`)

> Built 2026-11-05: `hits` takes world points, not points relative to the origin; angles are degrees from the aim,
> positive toward the right hand (clockwise from above). Added `Height.admits`, `BossFootprint.cell(x, z)` and
> `cellAt` (the column keys `Region` excludes) and a public nested `Shapes` helper (bearing, local frame, `at`, path
> length and `pointAlong`) so the client draws with the hit test's geometry. Semantics fixed by the build: a `Lane`
> strikes everything between the feet and its front, side lane `i` starts `|i| x sideDelay` after the centre, and a
> fanned lane uses `fanDegrees` per index; `Ring.band` is the wave's whole thickness; `Annulus` arcs are evenly
> spaced; a landed `Discs` disc stays live; `Path` with `walkers == 0` is live along its whole length; a `Tether` with
> `halfWidth == 0` strikes within `Tether.END_REACH` (0.75) of its `to` end; `Union.height()` is STANDING because each
> part keeps its own band. `FootprintFrame` gained `simple(...)` and `with*` copies.


A sealed set of ground shapes. The server tests hits with it and the client draws it, from the same accessors.

```java
public sealed interface BossFootprint {
    /** Height band above the frame's floor; groundOnly also needs the target on the ground on the tested tick. */
    record Height(double min, double max, boolean groundOnly) {
        public static final Height STANDING = new Height(0.0D, 2.5D, false);
        /** The one jump clearance every jumpable wave uses (see Cross-Saint notes B-9). */
        public static final Height JUMPABLE = new Height(0.0D, 0.9D, false);
    }
    enum Anchor { ORIGIN, AIM, TARGET }
    enum Area { BOSS_FLOOR, CHAMBER, AROUND_ORIGIN }
    enum End { BODY, TARGET, ANCHOR }

    Height height();
    /** Planar test; age is ticks since the commit tick (negative through the wind-up). */
    boolean hits(FootprintFrame frame, Vec3 point, double age);
    /** Planar test plus the height band and ground flag. Occlusion needs the level: see {@link Occluded}. */
    default boolean hits(FootprintFrame frame, Vec3 feet, boolean onGround, double age) { ... }

    /** Parallel lanes, or fanned when fanDegrees > 0; a front that runs out at frontSpeed (0 = all at once). */
    record Lane(double length, double halfWidth, int sideLanes, double spacing, double fanDegrees,
                double frontSpeed, int sideDelay, Height height) implements BossFootprint { }
    /** n radial spokes from the origin; the wedges between are safe. */
    record Spokes(int count, double halfWidth, double inner, double length, double offsetDegrees,
                  double frontSpeed, Height height) implements BossFootprint { }
    record Cone(double inner, double radius, double degrees, double offsetDegrees, Height height)
            implements BossFootprint { }
    /** A cone whose live band sweeps across it over sweepTicks of the active span. */
    record Sweep(double inner, double radius, double degrees, double bandDegrees, boolean clockwise,
                 int sweepTicks, Height height) implements BossFootprint { }
    /** waves expanding rings, waveGap ticks apart, each from `from` to `to` over travelTicks. */
    record Ring(double from, double to, double band, int waves, int waveGap, int travelTicks, Height height)
            implements BossFootprint { }
    record Disc(Anchor at, double radius, Height height) implements BossFootprint { }
    /** A full annulus when arcs == 0, else that many arcs of arcDegrees turning `revolutions` times. */
    record Annulus(double inner, double outer, int arcs, double arcDegrees, double revolutions, int activeTicks,
                   Height height) implements BossFootprint { }
    record Cross(Anchor at, double arm, double halfWidth, Height height) implements BossFootprint { }
    /** Discs at frame.points(), landing landingGap ticks apart in list order. */
    record Discs(double radius, int landingGap, Height height) implements BossFootprint { }
    /** Segments through frame.points(); walkers fronts of frontLength travelling at frontSpeed, spacing apart. */
    record Path(double halfWidth, double frontSpeed, double frontLength, int walkers, double spacing,
                Height height) implements BossFootprint { }
    /** The arena area named, minus the cells in frame.cells(). */
    record Region(Area area, double radius, Height height) implements BossFootprint { }
    /** A line between two ends re-read every tick (a chain to an anchor, a thread to the target). halfWidth 0
     *  strikes only the target end; range breaks it. */
    record Tether(End from, End to, int anchor, double halfWidth, double range, Height height)
            implements BossFootprint { }
    /** Parts with their own delay after the commit; each part strikes a target separately. */
    record Union(List<Part> parts) implements BossFootprint { public record Part(BossFootprint shape, int delay) { } }
    /** Blocked when a block ray from the frame origin (at eye height) to the target's body centre is not clear. */
    record Occluded(BossFootprint shape) implements BossFootprint { }
    /** No ground shape: wells, communions, the move's own logic decides who it touches. */
    record None() implements BossFootprint { public static final None INSTANCE = new None(); }
}

public record FootprintFrame(Vec3 origin, Vec3 aim, Vec3 target, double floorY, List<Vec3> points,
        LongSet cells, List<Vec3> anchors, AABB bossFloor, AABB chamber) {
    public CompoundTag save();
    public static FootprintFrame load(CompoundTag tag);
}

public final class BossFootprints {        // NBT for hazards and the synced frame
    public static CompoundTag save(BossFootprint shape);
    public static BossFootprint load(CompoundTag tag);
}
```

Every time-varying record exposes its motion as pure methods (`Lane.front(age)`, `Sweep.angle(age)`,
`Ring.radius(age, wave)`, `Annulus.arcAngle(age, arc)`, `Path.walker(age, index)`, `Union.live(age)`), and `hits` is
written only in terms of them, so a drawn edge is a hit edge. `Occluded` is tested by `SaintMoves.struck` with level
access; the client draws it as its inner shape.

### 2. Bands and sectors: `BossBands` (`common/entity/boss/`)

> Built 2026-11-05: `BossBands.STANDARD` is (3, 6.5, 12), resolution B-1, not the Mycophant's 3.5/7/13;
> `SaintLocomotion.bands()` is a default method returning it.


```java
public record BossBands(double hug, double near, double mid) {
    public static final BossBands STANDARD = new BossBands(3.5D, 7.0D, 13.0D);   // the Mycophant's
    public enum Band { HUG, NEAR, MID, FAR }
    public enum Sector { FRONT, FLANK, REAR }
    public Band band(double horizontalDistance);
    public static Sector sector(double degreesFromFacing);     // FRONT <= 60, FLANK <= 120
    public static double degreesFromFacing(double fromX, double fromZ, float yawDegrees, double toX, double toZ);
}
```

Each Saint's locomotion returns its bands (Putriciel and Velorum asked for their own). The Mycophant keeps
`MycophantPositionRules`.

### 3. `SaintMove` and the picker (`common/entity/boss/saint/`)

> Built 2026-11-05: `SaintMove` gained `default int rollVariant(SaintPickContext, IntUnaryOperator)` (the synced
> variant needs a source). `SaintPickRules.followUpLink(...)` returns the whole `SaintFollowUp` (the controller needs
> its link gap); `followUp` delegates to it, one chance roll per candidate in list order. `FollowCondition` gained
> `sectors(...)` and `and(...)`; `SaintPickContext` gained `of(phase, band, sector)` and `inputs(variant)`;
> `SaintMoveContext` gained `age()`. `SaintMoves` gained `scaled(...)` (the pure enrage rule), `hurt(saint, target,
> base, type)` for damage outside a move, `clearSight`, `kindFor`/`attachedReach` (the AREA rule of resolution B-2 as
> code) and the constants `MELEE_FLOOR`, `AREA_FLOOR`, `AREA_REACH`; `cut(kind, windup, fraction)` removes `fraction`
> of the wind-up. The controller calls `SaintMoves.struck` and the move's `onHit` itself on every ACTIVE tick, so a
> move only writes `onTick` for its own extra effects.


Each Saint has an enum implementing `SaintMove`. Methods the client calls read only synced inputs.

```java
public interface SaintMove {
    enum Kind { MELEE, AREA }               // wind-up floors 12 and 20, and reveal floors (section 18)
    enum Aim { TRACK, LOCK_AT_START, LEAD }

    int id();                               // 1..255, unique within the Saint; 0 means no move
    String key();                           // lower_snake: lang, review stops, tests
    Kind kind();
    Aim aim();
    default int leadTicks() { return 0; }
    /** False for moves only the Saint starts (Ignition, the Last Thirst, the Radiance Flood). */
    default boolean picked() { return true; }
    boolean inPhase(int phase);

    // read by both sides, from synced inputs only
    BossMoveTimeline timeline(SaintMoveInputs in);
    BossFootprint footprint(SaintMoveInputs in);
    default TelegraphSpec telegraph(SaintMoveInputs in) { return TelegraphSpec.DEFAULT; }

    // server only
    int weight(SaintPickContext ctx);                       // 0 = not here
    default boolean eligible(SaintPickContext ctx) { return true; }
    int cooldown(SaintPickContext ctx);
    default List<SaintFollowUp> followUps(int phase) { return List.of(); }
    /** Opens at the recovery's first tick; the recovery lasts max(recovery, opening ticks). */
    default @Nullable SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) { return null; }
    /** 0: each part strikes a target once per move; n: at most once per n ticks. */
    default int hitEvery() { return 0; }
    default float baseDamage() { return 0.0F; }
    /** Null uses the Saint's damage type (Putriciel's fire moves name the cleansing type). */
    default @Nullable ResourceKey<DamageType> damageType() { return null; }
    default void onTick(SaintMoveContext ctx) { }
    default void onHit(SaintMoveContext ctx, LivingEntity target) { SaintMoves.hurt(ctx, target, baseDamage()); }
}

public record SaintMoveInputs(int phase, int variant, int deedMask, int challengers) { }

public enum TelegraphFamily { ANIMUS, CRUOR, IRON, LUX, DUCTILIS, PUTRICIEL_ROT, FLAME, RIME, UMBRA }

/** fill and edge families, the tick the footprint first shows, a Saint style id for its painter. */
public record TelegraphSpec(TelegraphFamily fill, TelegraphFamily edge, int revealTick, int style,
        boolean showWhileActive) {
    public static final TelegraphSpec DEFAULT = new TelegraphSpec(null, null, 0, 0, true); // null: the Saint's family
}

public record SaintFollowUp(SaintMove next, float chance, int linkGap, FollowCondition when) { }

@FunctionalInterface
public interface FollowCondition {
    boolean test(SaintFollowContext c);
    FollowCondition ALWAYS = c -> true;
    static FollowCondition onHit() { ... }
    static FollowCondition bands(BossBands.Band... bands) { ... }
}
public record SaintFollowContext(SaintTrialEntity saint, int phase, boolean hitLanded, BossBands.Band band,
        BossBands.Sector sector) { }

public record SaintPickContext(SaintTrialEntity saint, int phase, @Nullable LivingEntity target, BossBands.Band band,
        BossBands.Sector sector, double distance, int kiteTicks, int hugTicks, int unseenTicks,
        int ticksSinceOpening, int ticksInPhase, float meterFraction, float targetMeter, float targetSecondary,
        int deedMask, String awakening, int challengers) { }

public record SaintMoveContext(SaintTrialEntity saint, ServerLevel level, SaintMove move, SaintMoveInputs inputs,
        int tick, BossMoveTimeline.Stage stage, boolean committedNow, FootprintFrame frame,
        @Nullable LivingEntity target, SaintArena arena) { }

public final class SaintPickRules {                      // pure, JVM-tested
    public static int cell(int weight, boolean full, boolean half);
    public static @Nullable SaintMove choose(List<? extends SaintMove> moves, SaintPickContext ctx,
            ToIntFunction<SaintMove> cooldownLeft, History history, IntUnaryOperator roll);
    public static @Nullable SaintMove followUp(SaintMove done, SaintFollowContext ctx, int depth,
            boolean interruptPending, DoubleSupplier roll);
    public static final int MAX_CHAIN = 2;              // follow-ups, so a string is at most three moves
    public static final class History { record(SaintMove); repeatFactor(SaintMove); last(); }  // 0, .4, .6, .8
    public static final class Pressure { sample(Band, boolean seen); kiteTicks(); hugTicks(); unseenTicks();
                                         resetKite(); }                              // 200-tick window
}

public final class SaintMoves {
    /** base × attack attribute / its base value, so the enrage's +30% reaches every move. */
    public static float damage(SaintTrialEntity saint, float base);
    /** Damage of the move's type, through ManipulationCombatHelper.canHarmFor; false if refused. */
    public static boolean hurt(SaintMoveContext ctx, LivingEntity target, float base);
    /** Challengers and CHALLENGERS-allegiance companions inside the footprint now, occlusion tested, once per
     *  part per hitEvery window. */
    public static List<LivingEntity> struck(SaintMoveContext ctx);
    public static BossMoveTimeline floored(SaintMove.Kind kind, BossMoveTimeline timeline);   // 12 / 20
    public static int cut(SaintMove.Kind kind, int windup, float fraction);                  // phase cuts, floored
    public static int revealFloor(SaintMove.Kind kind);                                      // 12 / 20
}
```

The damage types the kit ships (`data/hemomancy/damage_type/`): `saint_hemorath`, `saint_seraphae`,
`saint_putriciel`, `saint_putriciel_cleansing`, `saint_velorum`, each `message_id` `hemomancy.<id>`, `scaling`
`when_caused_by_living_non_player` (the designs' numbers are Normal), none tagged `is_fire`, all in a new tag
`hemomancy:saint_damage`. Each Saint builder adds its own `death.attack.hemomancy.<id>` and `.player` lines
(add-only lang).

### 4. `SaintCombatController` and `SaintLocomotion` (`common/entity/boss/saint/`)

> Built 2026-11-05: the recovery boundary is the recovery's first tick or no move at all; a queued stagger or forced
> move ends the rest of the recovery when it lands (an open recovery opening stays open). `start(move, Cut.IMMEDIATE)`
> ends the running move without `cancel()`, which stays the ending's and the immediate phase change's (guardrail rule
> 6). Added `timeline()` (the running move's, recovery stretched), `inputs()`, `interruptible()`,
> `cooldownLeft(move)`, `staggered()`, `motor()` and the static `defaultTarget(...)`; `SaintLocomotion.STILL`;
> `SaintMotor.lunging()`. The target is re-chosen only between moves and during an uncommitted wind-up. A move under
> way is not saved: a reloaded Saint keeps its cooldowns and replay serial and picks again.


```java
public final class SaintCombatController {
    public enum Cut { QUEUE, IMMEDIATE }
    public void tick();                                    // roam, pick, wind up, commit, active, recover, chain
    public @Nullable SaintMove move();
    public int moveTick();
    public BossMoveTimeline.Stage stage();
    public @Nullable LivingEntity target();
    public @Nullable FootprintFrame frame();
    /** A forced move (Ignition, the Last Thirst, the Radiance Flood): at the next recovery boundary, or now. */
    public void start(SaintMove move, Cut cut);
    public void requestStagger(int ticks, @Nullable Vec3 face);   // queued to the recovery boundary
    public void skipToRecovery();
    /** Only SaintEnding and Cut.IMMEDIATE phase changes call this (guardrail). */
    public void cancel();
    public void setPoints(List<Vec3> points);              // Discs, Path: synced at start and commit
    public void setCells(LongSet cells);                   // Region exclusions
    public void setAnchor(int index, Vec3 point);          // Tether ANCHOR ends, re-synced when they move
    public void markHit();
    public boolean hitLanded();
    public int chainDepth();
    public int ticksSinceOpening();
    public void delay(int ticks);
    public SaintPickRules.History history();
    public SaintPickRules.Pressure pressure();
}

public interface SaintLocomotion {
    BossBands bands();
    void roam(SaintTrialEntity saint, SaintMotor motor, @Nullable LivingEntity target);
    default void inMove(SaintTrialEntity saint, SaintMotor motor, SaintMoveContext ctx) { }
    /** False while the Saint may not pick (Seraphae reeled in, Velorum's settle). */
    default boolean mayPick(SaintTrialEntity saint) { return true; }
    /** Default: the challenger who hurt it in the last 100 ticks, else the nearest one in sight. */
    default @Nullable LivingEntity chooseTarget(SaintTrialEntity saint, List<Player> challengers,
            @Nullable LivingEntity current) { ... }
    float turnRate(SaintTrialEntity saint, boolean trackingWindup);    // degrees per tick
    int decisionDelay(SaintTrialEntity saint);
}

public final class SaintMotor {                            // every motion clamped to the arena leash
    public void walk(Vec3 direction, double speed);
    public void pathTo(Vec3 point, double speed);          // vanilla navigator, no goals
    public void glide(Vec3 point, double speed, double hover);
    public void lunge(Vec3 direction, double distance, int ticks);
    public void face(Vec3 point, float maxStep);
    public void stop();
}
```

The controller replaces the vanilla goals: a kit Saint registers no goals. Aim: TRACK follows the target until the
commit tick, then locks; LOCK_AT_START fixes at the start; LEAD aims `leadTicks` ahead of the target's motion. Phase
wind-up cuts and deed changes go through `SaintMoves.floored` and `cut`, so no table can dip under 12 or 20.

### 5. `SaintMeter` (`common/entity/boss/saint/`)

> Built 2026-11-05: every per-player method has a `UUID` overload (JVM tests cannot make a `Player`), plus
> `existing(UUID)`, `players()`, `retain(Set)`, `challengers(int)`, `playerMax()`, `rule()` and the factory
> `fixed(max)`; the roster tick is `tick(Map<UUID, Player>)` behind `tick(saint)`. A per-player maximum is read with
> one challenger (per-player meters never scale); thresholds cross per player only on an `Owner.PLAYER` meter.
> `stateTicks` counts down by one each tick. The send rules live here for the client stage: `dataDue`/`markDataSent`
> (entity data, 0.1% or 10 ticks), `packetDue` and `PlayerState.packetDue`, `markSent`, `forgetSent` (the packet's
> 0.25%, 2-tick gap, 20-tick keep-alive, flag and countdown changes), and `coarse(state)` for `DATA_CHALLENGERS`.
> `SaintKit.scale(n)` is the one 1 + 0.5 (n - 1) rule.


```java
public final class SaintMeter {
    public enum Owner { SAINT, PLAYER }
    public interface Rule {
        float max(int challengers);
        default float start(SaintTrialEntity saint) { return 0.0F; }
        /** Saint-owned: change per tick (a leak is negative), from the Saint's live state. */
        default float drift(SaintTrialEntity saint, float value, float max) { return 0.0F; }
        /** Per-player: change per tick for one challenger. */
        default float drift(SaintTrialEntity saint, Player player, PlayerState state) { return 0.0F; }
        default float floor(SaintTrialEntity saint) { return 0.0F; }
        default float[] thresholds() { return new float[0]; }      // fractions, rising
        default void crossed(SaintTrialEntity saint, @Nullable Player player, int threshold, boolean rising) { }
    }
    public static final class PlayerState {
        public float value(); public float secondary(); public float secondaryMax(); public int flags();
        public int stateTicks();
    }
    public SaintMeter(Owner owner, Rule rule);
    public Owner owner();
    public float value(); public float max(); public float fraction();
    public float add(float amount, @Nullable Player contributor);   // clamped floor..max; returns what moved
    public void set(float value);
    public @Nullable UUID lastContributor();
    public PlayerState player(Player player);                        // made on first read
    public float add(Player player, float amount);
    public void secondary(Player player, float value, float max);
    public void flags(Player player, int flags);
    public void stateTicks(Player player, int ticks);
    public void tick(SaintTrialEntity saint);                         // drift, floor, thresholds, sync
    public void save(CompoundTag tag); public void load(CompoundTag tag);
}
```

Values are floats (Velorum's 0.55-a-tick drain). Per-player states live on the Saint entity, are dropped when the
arena's roster drops the player, and are never player data. The bar is a separate hook (section 15): health by
default, the meter for Seraphae, the absolutions for Putriciel.

### 6. `SaintPresence` and `SaintHealing` (`common/entity/boss/saint/`)

> Built 2026-11-05: `SaintHealing` has `UUID` and game-time overloads for tests; `HealModifier.SAINT_PRESENCE` applies
> when the multiplier is not 1. The presence is skipped while a phase card plays (`SaintPhaseTable.cardTicks()`),
> during the rise and the ending, and when `presencePaused()`.


```java
public interface SaintPresence {
    SaintPresence NONE = ...;
    double radius(SaintTrialEntity saint);                 // read every tick: the Last Thirst's 18
    default double verticalReach(SaintTrialEntity saint) { return 4.0D; }
    int cadence(SaintTrialEntity saint);                   // read every pulse: the Repose deed's 14
    default boolean needsSight(SaintTrialEntity saint) { return true; }
    default float falloff(SaintTrialEntity saint, double distance) { return 1.0F; }
    default float playerFactor(SaintTrialEntity saint, Player player) { return 1.0F; }   // the Apotheos ×1.25
    default void everyTick(SaintTrialEntity saint, Player player, float strength) { }    // Velorum's multipliers
    void pulse(SaintTrialEntity saint, Player player, float strength);
}

public final class SaintHealing {                          // backs HealModifier.SAINT_PRESENCE
    public static void set(Player player, float multiplier, int ticks);    // lowest live entry wins
    public static float multiplier(LivingEntity entity);                    // 1 when none
    public static void clear();                                             // TransientServerStateEvents
    public static void end(Player player);                                  // logout
}
```

The kit runs the presence over `arena.challengers()` only, through `canHarmFor`, and not during the rise, a phase
card, the ending, or while the Saint's `presencePaused()` says so. `DATA_PRESENCE_RADIUS` carries the radius for each
Saint's ground read.

### 7. `SaintOpening`, `SaintOpenings` and `SaintHit` (`common/entity/boss/saint/`)

> Built 2026-11-05: `admit` counts a COUNTED hit against the cap (so a caller never counts twice); added `length()`,
> `admit(Delivery, n)`, `Spec.cap(n)` and `Spec.save/load` (an open opening is saved whole). `open(spec, ticks <= 0)`
> uses the spec's own length. `SaintHitEvents` stages at NORMAL priority from the `SchoolDamageSource`'s context or
> `SchoolDamage.current()`. The classification and floor rules are the pure `SaintDamageRules`; a Saint's channel
> handler deals a CHANNEL hit with `SaintTrialEntity.hurtByChannel`.


```java
public final class SaintOpening {
    public enum Delivery { MELEE, PROJECTILE, SCHOOL_CAST, CHANNEL, OTHER }
    public enum Admit { COUNTED, CAPPED, NOT_COUNTED, CLOSED }
    public record Spec(int id, String key, int ticks, float damageTaken, int hitCap, int capPerExtraChallenger,
            Set<Delivery> counts) { }
}

public final class SaintOpenings {
    public boolean open(SaintOpening.Spec spec, int ticks);        // false while another is open
    public void chainTo(SaintOpening.Spec spec, int ticks);        // closes the current one into this (Velorum's Bow)
    public int extend(int ticks, int capPerWindow);                // returns ticks added
    public void close();
    public boolean isOpen(); public boolean isOpen(SaintOpening.Spec spec);
    public @Nullable SaintOpening.Spec current();
    public int age(); public int remaining(); public int hits(); public int ticksSinceClosed();
    public float damageTaken();                                    // 1 when closed
    public SaintOpening.Admit admit(SaintHit hit, int challengers);
    public void tick(); public void save(CompoundTag tag); public void load(CompoundTag tag);
}

public record SaintHit(@Nullable Player attacker, DamageSource source, float amount, SaintOpening.Delivery delivery,
        @Nullable EnumBloodTendency school, boolean fire, @Nullable SchoolHitContext context) { }
```

`SaintHitEvents` (`common/entity/boss/saint/`, the kit's one `LivingIncomingDamageEvent` subscriber, after
`SchoolCombatEvents.prepare`) stages the hit's `SchoolHitContext` on the Saint so `actuallyHurt` can classify it.
Per-school gains and multipliers (Seraphae's stilling schools, Putriciel's Flammeus ×2.5, Velorum's casts ×1.25) stay
in each Saint's rules, read off `SaintHit`.

### 8. `SaintArena` (`common/entity/boss/saint/`)

> Built 2026-11-05: an original keeps its block entity data with its state (`SaintArena.Original`), so a restored
> basin or pyre gets its data back. The record is the pure, generic `SaintArenaRecord<S>`. Added `clamp(point)` (the
> leash), `floorY()`, `home(entity)`, `discardOwned()`, `restoreNow()` (records and owned entities), `changed(pos)`,
> `emptyTicks()` and `hasRecords(tag)`; `restore(order, ticks)` leaves owned entities (Velorum's devotees walk home
> during the ending); `tick(fighting)` returns true when the empty chamber resets. The server-stopping restore is
> `TransientServerStateEvents.onServerStopping` calling `SaintTrialEntity.restoreArenaNow()`, not a
> `SaintArenaLifecycleEvents` class: `SessionStateRegistryTest` lets only the hub listen for server stop events. A
> Saint with no tomb gets a fallback box the chamber's size with the boss floor centred on it (`chamberFound()` false,
> no leash reset).


```java
public final class SaintArena {
    public enum RestoreOrder { IMMEDIATE, OUTERMOST_FIRST, INNERMOST_FIRST }
    public static final int MIN_CELL_INTERVAL = 10;          // one change per cell per 10 ticks
    public static final int EMPTY_RESET_TICKS = 600;

    public static SaintArena resolve(SaintTrialEntity saint);   // tomb position and facing, else a fallback square
    public boolean chamberFound();
    public AABB chamber(); public AABB bossFloor(); public BlockPos tomb(); public Direction facing();
    public BlockPos toWorld(int x, int y, int z); public Vec3 toWorld(double x, double y, double z);
    public Vec3 toLocal(Vec3 world);
    /** One scan of the chamber box at resolve per key, cached: basins, gutters, anchors, pyres, figures, lanterns. */
    public List<BlockPos> find(String key, Predicate<BlockState> test);
    public boolean contains(Vec3 point);
    public List<Player> challengers();                 // alive, not spectating, inside the chamber box
    public int challengerCount();                      // counted at the rise's end; rises, never falls

    public boolean set(BlockPos pos, BlockState state);
    public boolean set(BlockPos pos, BlockState state, int flags);
    public void schedule(BlockPos pos, BlockState state, int delayTicks);   // travelling fronts
    public void swapFor(BlockPos pos, BlockState state, int ticks);         // timed, then the original returns
    public boolean revert(BlockPos pos);
    public boolean clearColumn(BlockPos base, int storeys);                 // two-tall set pieces, upper first
    public EditBudget budget(String key, int perWindow, int windowTicks, int maxRecorded);
    /** Past maxRecorded the oldest edit of that budget reverts first. */
    public boolean edit(EditBudget budget, BlockPos pos, BlockState state);
    public <E extends Entity> E own(E entity, @Nullable BlockPos home);
    public void disown(Entity entity);
    public List<Entity> owned();
    public void restore(RestoreOrder order, int overTicks);
    public boolean restoring();
    public boolean clean();                            // no records, no owned entities
    public void tick(); public void save(CompoundTag tag); public void load(CompoundTag tag);
}

public final class SaintChamberLayout { ... }          // the chamber-local constants of tools/worldgen/mausoleum/pieces.py
```

`SaintChamberLayout` holds the numbers the designs quote (37 by 27 by 17; boss floor x 9 to 23, z 6 to 20;
`CHAMBER_ZC` 13; pillar x 9, 13, 17, 21, 25 on z 5 and 21; aisles z 3 and 23; tomb (30, 4, 13)), pinned to
`pieces.py` by the guardrail.

**The restore contract.**

- Every block a fight changes goes through the arena. It records a position's original state the first time it
  changes; the record is saved with the Saint.
- **`endTrial`:** the ending's spec chooses the order and length (Seraphae's walls-first wave over 20 ticks); anything
  left restores immediately before the Saint is discarded.
- **`remove` with a destroying reason, and the leash reset:** immediate; owned entities are discarded.
- **Load of a Saint with records:** on its first server tick, every record restores immediately and owned entities
  are discarded. A Saint whose trial ended is then discarded. A Saint mid-fight carries on with a clean chamber, and
  its `onArenaReloaded()` hook puts back what its phase needs (Putriciel's pyre table).
- **Server stopping:** `SaintArenaLifecycleEvents` (`ServerStoppingEvent`) restores every loaded Saint's arena
  immediately, while the chunks are still loaded.
- **Chunk unload with the Saint inside:** nothing is written while the chunk unloads; the saved record restores on
  load. The chamber can stand changed only while it is unloaded with its Saint, never after it loads.
- A restore writes each original without drops and without neighbour updates that would break a two-tall piece. A
  position a player changed since is still restored.
- An owned entity (a devotee, an anchor, a fragment, a hazard) carries its Saint's UUID through `SaintCompanion` or
  the hazard's owner field; one that loads with no live Saint discards itself.
- The leash: the motor clamps the Saint to the chamber box; after `EMPTY_RESET_TICKS` with no challenger inside, the
  kit calls `resetTrial()`: arena restored, tomb closed with its door record, no loot, Saint discarded.

### 9. `SaintPhaseTable` (`common/entity/boss/saint/`)

> Built 2026-11-05: added `single(nameKey, color)` (a legacy Saint's), the pure `target(health, meter, saintPhase)`
> and `update(wanted, atBoundary)`, `ticksInPhase()`, `cardTicks()`, `playCardOnce(n)` and `CARD_TICKS` (50).
> `advance(n, cut)` also sets a floor the source cannot take back (Hemorath's health may sit above phase 2's
> threshold); the immediate cancel happens in `tick`.


```java
public final class SaintPhaseTable {
    public enum Source { HEALTH, METER, SAINT }
    public record Phase(int number, String nameKey, BossEvent.BossBarColor color, float at,
            @Nullable ResourceLocation card) { }
    public static SaintPhaseTable byHealth(Phase... phases);
    public static SaintPhaseTable byMeter(Phase... phases);
    public static SaintPhaseTable bySaint(ToIntFunction<SaintTrialEntity> phase, Phase... phases);  // Putriciel
    public SaintPhaseTable ratchet();                     // a phase once reached is kept (Seraphae)
    public int phase(); public Phase current(); public boolean pending();
    public void tick(SaintTrialEntity saint, SaintCombatController controller);   // queues to the boundary
    public void advance(int phase, SaintCombatController.Cut cut);                  // Hemorath's first full meter
    public void save(CompoundTag tag); public void load(CompoundTag tag);
}
```

A phase change sets the bar's colour and name and plays its card once (saved). Health floors are not phase data:
they are the `healthFloor` hook (section 15).

### 10. `SaintDeeds` (`common/entity/boss/saint/`)

> Built 2026-11-05: `mask()` carries `SaintDeeds.FORCED` (bit 30) on every forced awakening, so a forced fight with no
> Chapel skipped still reads forced on the client; added `ofMask(mask)` and `NONE`.


```java
public final class SaintDeeds {
    public static SaintDeeds of(String awakening, List<String> skippedChapels);
    public boolean forced();
    public boolean skipped(ChapelRules.Kind kind);
    public int mask();
    public static boolean skipped(int mask, ChapelRules.Kind kind);
}
```

The stat floor stays where it is (`SaintTrialEntity.setAwakening`, `SaintEnrageRules`). The mask is synced
(`DATA_DEEDS`) so a client can draw what a deed hides.

### 11. `SaintChannelRouter` (`common/entity/boss/saint/`)

> Built 2026-11-05: `tryAbsorb` and `tryProject` return `Result`, not a double: a refused result is handled with no
> blood, and only the flag can stop the item's own paths. `BloodAbsorptionItem.isValidAbsorptionTarget` asks
> `SaintChannelRouter.refusesOrdinaryDrain(entity)` so the three items name no Saint code but the router (guardrail
> rule 5). Added the pure `first(candidates, ask)` and `aimsAt(eye, look, point, reach, cone)`, the 48-block search, a
> 40-tick cue limit (session state in the hub) and the package door `SaintTrialEntity.channel(request)`. Rates passed:
> bare absorption `bareBlockAbsorptionPerTick()` at `bareAbsorptionRange()`, the staff `blockAbsorptionPerTick(focus)`
> at `absorptionRange(focus)`, projection the item's tile transfer rate at
> `SanguineProjectionTargeting.PROJECTION_REACH`.


One door for held Blood Absorption and Projection aimed at a fight: at the Saint's body, a companion entity, or an
arena set piece.

```java
public final class SaintChannelRouter {
    public enum Kind { ABSORB, PROJECT }
    public record Request(ServerPlayer player, Kind kind, double ratePerTick, double reach, boolean livingStaff,
            Vec3 eye, Vec3 look, @Nullable EntityHitResult entity, @Nullable BlockHitResult block, int useTicks) { }
    public record Result(boolean handled, double blood, @Nullable Component cue) {
        public static final Result PASS = new Result(false, 0.0D, null);
        public static Result taken(double blood);
        public static Result refused(Component cue);       // handled: no blood, a rate-limited action-bar cue
    }
    /** Called after the Vesper hook in BloodAbsorptionItem.onUseTick and LivingStaffItem's absorption. */
    public static double tryAbsorb(Level level, LivingEntity user, double reach, double ratePerTick, boolean livingStaff);
    /** Called in BloodProjectionItem.performProjection after EarlyInitiation, before the Will, rite and block paths. */
    public static double tryProject(Level level, LivingEntity user, double reach, double ratePerTick, boolean livingStaff);

    public static boolean aimsAt(Request request, Vec3 point, double reach, double coneDegrees, boolean needsSight);
    public static BloodAccounting.Payment pay(Request request, double amount, BloodAccounting.Source source);
    public static double credit(ServerPlayer player, double amount);   // fills the volume up to room and syncs it
}
```

The router asks Saints whose arena lists the player as a challenger, nearest first, through
`SaintTrialEntity.onChannel(Request)`; the first handled result wins and the item's own paths never run that tick.
Each Saint resolves its own targets from the request (Hemorath's ribs and wells, Seraphae's anchors, fragments and
hold, Putriciel's palm and sacs, Velorum's seam, frozen figures and preserved players) and pays only through
`pay` with its named `Source`. `BloodAbsorptionItem.isValidAbsorptionTarget` refuses every `SaintTrialEntity` and
`SaintCompanion`, so a Saint's body is never drained by the ordinary living-target path.

### 12. `BossHold` (`common/entity/boss/`)

> Built 2026-11-05 (client): `client/event/BossHoldClientEvents` finds the `SaintTrialEntity` whose `syncedHoldPlayer()`
> is the local player, pins it within `|syncedHoldSlack()|` of `BossHold.point(lerpTarget position, lerpTargetYRot,
> syncedHoldOffset())`, drops its fall, and while the slack is not negative zeroes its velocity and movement input. Only
> a `SaintTrialEntity` carries the synced hold fields, so another `BossHoldCarrier` gets no client pin yet.

> Built 2026-11-05: the constructor is `BossHold(Entity body, Sync sync, Consumer<Reason> released)`; the pure rules
> are `refuses`, `ends` and `pinned`; the held set is session state (`isHeld(UUID)`, cleared by the hub).
> `BossHoldClientEvents` is not built yet (client stage).


```java
public interface BossHoldCarrier { BossHold bossHold(); Entity holdBody(); }

public final class BossHold {
    /** offset is forward, up, right in the body's frame; slack in blocks before the server pins. */
    public record Spec(int maxTicks, Vec3 offset, double slack, boolean lockInput) { }
    public enum Reason { DONE, BROKEN, LOST, CANCELLED }
    @FunctionalInterface public interface Breaks { boolean breaks(ServerPlayer held, int heldTicks); }
    /** Refuses a spectator, a creative player, a player already held, and HeldTraveller.isHeld. */
    public boolean tryHold(ServerPlayer player, Spec spec, Breaks breaks);
    public void tick();
    public void release(Reason reason);
    public boolean holding(); public @Nullable UUID held(); public int heldTicks();
    public Vec3 point();
    public static Vec3 point(Vec3 feet, float yawDegrees, Vec3 offset);        // pure, shared with the client
}
```

New code, shaped on `MycophantHoldKind` and `MycophantHoldClientEvents`; the Mycophant is not moved onto it (umbrella
non-goal). `client/event/BossHoldClientEvents` pins the local player to the synced point within the slack and zeroes
input while `lockInput`. A kit Saint is a `BossHoldCarrier`; the hold releases on the ending, the leash reset and
removal.

### 13. `BossGroundHazardEntity` (`common/entity/boss/`)

> Built 2026-11-05 (client): `SaintTelegraphRenderer` paints hazards (the `NoopRenderer` stays). One synced field was
> added, `DATA_FADE_AT` (`fadeStart()`, the tick count `expire` ran at, saved as `FadeAt`), so the client can run the
> fade; `SaintTelegraphTiming.hazardFade` is the rule. A hazard whose owner is not a Saint draws in ANIMUS.

> Built 2026-11-05: registered with vanilla `NoopRenderer` (the telegraph pass will paint it); also syncs a fade
> length (`expire` and `fading()`); `inside(owner)` gives the bodies its footprint holds; it waits 40 ticks for an
> unloaded owner, then discards itself, and is saved only with an owner. Lang `entity.hemomancy.boss_ground_hazard`.


```java
public final class BossGroundHazardEntity extends Entity {
    public interface Owner {
        void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside);   // the effect, on its pulse
        default boolean hazardTouches(LivingEntity entity) { return true; }
    }
    public static BossGroundHazardEntity spawn(ServerLevel level, Entity owner, int kind, BossFootprint shape,
            FootprintFrame frame, int lifeTicks, int pulseEvery);
    public int kind(); public int life(); public BossFootprint shape(); public FootprintFrame frame();
    public @Nullable Entity owner();
    public void expire(int fadeTicks);
}
```

Registered as `hemomancy:boss_ground_hazard` (`EntityInit`, no model; its renderer draws nothing). Synced: owner id,
kind, life, and the shape and frame as one compound set at spawn; age is its own `tickCount` read through
`AnimationTime.age`. It covers Hemorath's standing iron and tide, Seraphae's healed pads, Putriciel's colonies, fire
patches and breath cloud, and Velorum's splinter field. A Saint spawns its hazards through `arena.own(...)`, so they
go with the trial.

### 14. `SaintEnding` (`common/entity/boss/saint/`)

> Built 2026-11-05: added `Spec.IMMEDIATE`, the `Step` enum and the pure `SaintEnding.Clock`. With `finishEarly` and
> `done`, the Saint is also discarded at once. A reload loses the `done` supplier, so a resumed ending reads the hook
> `endingDoneAfterLoad()` instead.


```java
public final class SaintEnding {
    public record Spec(int endTrialTick, int length, boolean finishEarly, boolean resumeOnLoad,
            SaintArena.RestoreOrder restore, int restoreTicks) { }
}
// on SaintTrialEntity:
protected final void beginEnding(SaintEnding.Spec spec, @Nullable Player victor, BooleanSupplier done);
public boolean ending();
public int endingTick();
protected void onEndingTick(int tick) { }
```

`beginEnding` cancels the active move, releases the hold, expires hazards, stops the presence and makes the Saint
untouchable. `endTrial` runs once at `endTrialTick`, or earlier when `finishEarly` and `done` holds (Velorum's
faithful home); the arena restores after it in the spec's order; the Saint is discarded at `length`. The tick is saved:
with `resumeOnLoad` a reload carries on, without it `endTrial` runs on the first tick.

### 15. `SaintTrialEntity`: hooks and synced data

> Built 2026-11-05: one extra synced field, `DATA_CHALLENGER_COUNT` (INT; the client's `SaintMoveInputs.challengers`).
> `DATA_CHALLENGERS` is a compound keyed by entity id as a string, each value `coarse << 16 | flags` (flags bits
> 0..15). `DATA_MOVE_FRAME` carries a whole `FootprintFrame.save()` (floor, boxes, points, cells, anchors); origin,
> aim and target stay live in their own fields. Read them through the `synced*()` getters or `view()`. Also added:
> `damageType()`, `hurtByChannel`, `restoreArenaNow()`, the hooks `onHoldReleased` and `endingDoneAfterLoad`, and
> `SaintTrialEntity implements BossHoldCarrier, BossGroundHazardEntity.Owner` (override `hazardTick`). The kit tick
> runs in `aiStep` after `super.aiStep()`: a kit Saint must not call `tickRise()` itself. `die` on a kit Saint passes
> only `GENERIC_KILL`. The challenger health modifier is ADD_MULTIPLIED_TOTAL. `onArenaReloaded()` runs on every
> mid-fight load (the chamber is always clean then); `onFloorReached` fires once per floor value. `SeraphaeEntity`'s
> private `arena()` was renamed `containmentBox()` because it clashed with the kit's accessor; behaviour is unchanged.


A Saint provides its kit once, from its own class:

```java
protected @Nullable SaintKit createKit() { return null; }      // null: legacy (until the compile gate)

public record SaintKit(List<? extends SaintMove> moves, SaintLocomotion locomotion, SaintMeter meter,
        SaintPresence presence, SaintPhaseTable phases, ResourceKey<DamageType> damageType,
        double healthPerExtraChallenger, SaintEnding.Spec ending) { }

public SaintCombatController controller(); public SaintMeter meter(); public SaintOpenings openings();
public @Nullable SaintArena arena(); public SaintPhaseTable phases(); public SaintDeeds deeds();
public BossHold bossHold(); public @Nullable SaintMove move(int id); public SaintMoveView view();
public void resetTrial();

// hooks
protected float damageTakenFactor(SaintHit hit) { return 1.0F; }      // × openings.damageTaken()
protected float healthFloor(SaintHit hit) { return 1.0F; }            // never below 1: a Saint is never killed
protected void onHitTaken(SaintHit hit, float applied, SaintOpening.Admit admit) { }
protected void onFloorReached(SaintHit hit) { }                         // usually beginEnding
protected float barProgress() { return getHealth() / getMaxHealth(); }
protected SaintChannelRouter.Result onChannel(SaintChannelRouter.Request request) { return Result.PASS; }
protected boolean presencePaused() { return false; }
protected void onArenaResolved(SaintArena arena) { }
protected void onArenaReloaded() { }
protected void onPhaseChanged(int from, int to) { }
protected void onOpeningChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) { }
protected void tickSaint() { }                                          // after the kit's tick
```

The kit owns `actuallyHurt` (classify, `admit`, factor, floor, `onHitTaken`, `onFloorReached`), `die` (only a
`GENERIC_KILL`, which goes to `remove`), `isInvulnerableTo` (rise and ending) and the server tick order: rise, arena,
phases, meter, presence, openings, controller, hold, ending, sync, then `tickSaint`. Max health is raised by a
permanent `saint_challengers` modifier to `1 + healthPerExtraChallenger × (n − 1)` whenever `challengerCount()` rises,
keeping the health fraction.

| Synced field | Serializer | Written | Read by |
| --- | --- | --- | --- |
| `DATA_MOVE` | INT | move id at start, 0 at end | animation, telegraphs |
| `DATA_MOVE_SERIAL` | INT | +1 at every start | `BossReplayClock` key |
| `DATA_MOVE_TICK` | INT | every tick a move runs | `BossReplayClock.sample` |
| `DATA_MOVE_VARIANT` | INT | at start | `SaintMoveInputs` |
| `DATA_MOVE_ORIGIN` | VECTOR3 | start, commit | `FootprintFrame` |
| `DATA_MOVE_AIM` | VECTOR3 | while tracking, when it moves 0.05 or more; locked at commit | `FootprintFrame` |
| `DATA_MOVE_TARGET` | INT | at start and on a target change | frame, animation |
| `DATA_MOVE_FRAME` | COMPOUND_TAG | points, cells and anchors, when the Saint sets them | `FootprintFrame` |
| `DATA_MOVE_FLAGS` | INT | bit 0 committed, bit 1 aim locked | telegraphs |
| `DATA_PHASE` | INT | on a phase change | everything |
| `DATA_OPENING` | INT | opening id, 0 closed | animation, HUD |
| `DATA_OPENING_TICK` | INT | every tick an opening is open | `BossReplayClock` |
| `DATA_OPENING_LENGTH` | INT | at open and extend | HUD |
| `DATA_METER`, `DATA_METER_MAX` | FLOAT | on a change of 0.1% of max, or every 10 ticks | HUD, body art |
| `DATA_PRESENCE_RADIUS` | FLOAT | on a change | ground reads |
| `DATA_DEEDS` | INT | at `setAwakening` | telegraph reveal, art |
| `DATA_CHALLENGERS` | COMPOUND_TAG | entity id to (value 0..255 of max, flags), every 5 ticks when changed | drawing other players' state |
| `DATA_HOLD_PLAYER` | INT | held entity id, -1 none | `BossHoldClientEvents` |
| `DATA_HOLD_OFFSET` | VECTOR3 | at hold | the pin point |
| `DATA_HOLD_SLACK` | FLOAT | at hold; negative means input unlocked | the pin |
| `DATA_ENDING_TICK` | INT | -1, else every tick of the ending | animation |
| `DATA_RISE_TICK` | INT | kept | `SaintRise` |
| `DATA_VISUAL_*`, `DATA_SECONDARY_*` | INT | legacy, deleted by the compile gate | legacy Saints |

`SaintMoveView` (common, no client classes) owns the move, opening and ending `BossReplayClock`s and the decoded
frame: `moveTime(partial)`, `openingTime(partial)`, `endingTime(partial)`, `frame()`, `inputs()`. Each Saint's
`AnimState` reads it; a Saint's own extra synced fields (Hemorath's swell, Seraphae's restraints, Putriciel's
Kindling phase, Velorum's lantern) stay in its own class.

### 16. `SaintMeterPacket` (`common/network/boss/`)

> Built 2026-11-05: as written; `CLEAR` is `Integer.MIN_VALUE`, plus `SAINT_FLAGS` (0xFFFF), `clear(saintId)`,
> `clears()` and `saintFlags()`; a negative countdown clamps to 0. `value`/`max` are the challenger's own `PlayerState`
> value and `meter.playerMax()` (for a Saint-owned meter that is the Saint's maximum); the Saint's own value stays in
> entity data. `SaintMeterSync` (`common/entity/boss/saint/`) is one per Saint (`SaintTrialEntity.meterSync()`), ticked
> in `kitTick` whenever the Saint is not rising and its trial has not ended (so the ending keeps sending keep-alives),
> and cleared in `releaseChallengers()` (end, reset, destroying removal). It finds a player in the Saint's level
> first, then on the server. The set it has sent to is not saved; a client whose Saint vanished times its state out
> after 60 ticks (`SaintMeterClientState.STALE_TICKS`). PROTOCOL_VERSION is 38.

```java
public record SaintMeterPacket(int saintId, float value, float max, float secondary, float secondaryMax, int flags,
        int stateTicks) implements CustomPacketPayload {
    public static final int CLEAR = 1 << 31;              // bits 0..15 are the Saint's; the rest the kit's
    public static final Type<SaintMeterPacket> TYPE = new Type<>(Hemomancy.rloc("saint_meter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaintMeterPacket> STREAM_CODEC = ...;  // StreamCodec.of, seven fields
}
```

The only new payload. `playToClient`, handled by `ClientPayloadHandlers::dispatch` into
`client/network/ClientboundSaintHandlers.meter`, which stores it in `client/screen/overlay/boss/SaintMeterClientState`
(cleared on `CLEAR`, dimension change and logout). `SaintMeterSync` sends it to each challenger when they join the
roster, when their state changes (0.25% of max, any flag, a state countdown starting or stopping), at most every 2
ticks, every 20 ticks as a keep-alive, and with `CLEAR` when they leave the roster or the trial ends or resets. A
Saint-owned meter with no per-player part still sends it on join and leave: that is how the client knows which Saint
it is fighting. The kit bumps `PacketHandler.PROTOCOL_VERSION` from 37 to 38 and rewrites
`src/test/resources/network/payload-registry.txt`.

### 17. HUD: `SaintMeterLayer` and `SaintMeterStyle` (`client/screen/overlay/boss/`)

> Built 2026-11-05: `SaintMeterContext.canvas` is nullable (null for the `muffle` query) and the context gained
> `right()`, `centerX()`, `saintFraction()`, `ownFraction()`, `secondaryFraction()`, `openingFraction()` and `flag(bit)`.
> `stateTicks` is the packet's countdown run down by the ticks since it arrived. The Saint's type, bar frame and meter
> are remembered from the body while it is in range (`SaintMeterClientState`), so with the body out of range the style
> gets `saint == null` and the last seen values; a Saint never seen on this client draws nothing. The bar origin is
> recorded by `BossBarFrameRenderer` (not the painter, which never sees the increment) into
> `BossBarFrameClientState.Entry.painted(x, y, now)`, read back with `BossBarFrameClientState.painted(frame, now)`;
> the same renderer adds `SaintMeterLayer.room(frame)` to the bar's increment. With the bar hidden (or bar frames off
> in the client config) the meter draws at the first bar's place, y 12 + 14. `reveal` runs over 20 ticks from the first
> packet naming the Saint. The generic look is `DefaultSaintMeterStyle` (8 rows: a 4-row strip of the Saint's meter, a
> hairline of the challenger's own value, a draining tick while an opening is open); the four placeholders extend it
> with their colours. `client/event/SaintSoundEvents` (`PlaySoundEvent`) applies `muffle` to sounds farther than 3
> blocks from the Saint, leaving music, master, relative and ticking sounds alone; a factor of 0 cancels the sound.

```java
public final class SaintMeterLayer { public static void render(GuiGraphics graphics, DeltaTracker delta); }

public interface SaintMeterStyle {
    int height();                                          // rows the style needs under the bar
    void paint(SaintMeterContext c);
    default void paintScreen(SaintMeterContext c) { }      // full-screen pass: Velorum's frost edge
    default float muffle(SaintMeterContext c, SoundInstance sound) { return 1.0F; }   // Velorum's darkness
}

public record SaintMeterContext(GuiCanvas canvas, int x, int y, int screenWidth, int screenHeight, float time,
        float partialTick, @Nullable SaintTrialEntity saint, LocalPlayer viewer, float saintValue, float saintMax,
        float ownValue, float ownMax, float ownSecondary, float ownSecondaryMax, int flags, int stateTicks,
        int phase, int openingId, float openingTime, int openingLength, float reveal) { }
```

Registered once in `GuiLayerRegistrar`: `event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY,
Hemomancy.rloc("saint_meter"), hud(SaintMeterLayer::render))`, so `HudVisibilityRules` wraps it. It draws only while
`SaintMeterClientState` names a Saint: first `paintScreen`, then `paint` under that Saint's bar.
`BossBarFramePainter` records each bar it paints (origin and frame id) in `BossBarFrameClientState`, and adds the
style's `height()` to the increment of the bar that carries a Saint meter, so bars below make room; the layer draws at
that bar's `y + 14`. With the bar hidden or the Saint out of render range the style gets `saint == null` and draws
from the packet alone. `client/event/SaintSoundEvents` (`PlaySoundEvent`) applies `muffle` to sounds the Saint did
not make.

### 18. Telegraphs: `SaintTelegraphRenderer` (`client/render/world/`)

> Built 2026-11-05: the pass runs in both stages `ManipulationVisualRenderer` uses: `AFTER_ENTITIES` (the solid pass,
> `TelegraphCanvas.solids()`, where only `canvas.iron()`, the Ferric plate, draws) and `AFTER_LEVEL` (translucent).
> So a style's `paint` is called twice a frame and must check `canvas.solids()`. Ground fills are a flat tint in one
> new render type, `HemoRenderTypes.SAINT_TELEGRAPH_TINT` (position-colour, depth-tested, no depth write), because
> every school shader masks each quad's edges and a tessellated fill in them shows seams; edges are flat material
> ribbons through `LuxUmbraBatch` (ribbon shape; the frosted surface for RIME and CRUOR, ember dots for FLAME). IRON's
> translucent fill and edge use the ANIMUS material with an iron-grey tint. The canvas caps fills at 0.22
> (`MAX_FILL_ALPHA`) and edges at 0.012 (`MAX_EDGE_WIDTH`). The default painter is `SaintTelegraphPainter`
> (`paint`, `paintHazard`, and `footprint(canvas, shape, frame, age, Look)` for styles that want the default shape
> drawing in their own strengths); through the wind-up the whole footprint shows as a faint wash that firms toward the
> commit, then the live part is lit. A `Region` larger than 48 cells a side is not filled. `HazardDraw` gained `fade`.
> The pure timing is `SaintTelegraphTiming` (JVM-tested). The timeline drawn is `SaintControllerRules.effective`, as
> the server runs it. `client/registry/SaintClientStyles` maps each Saint to an `Entry(meter, telegraph, fill, edge)`:
> Hemorath ANIMUS/ANIMUS, Seraphae LUX/DUCTILIS, Putriciel PUTRICIEL_ROT/PUTRICIEL_ROT, Velorum RIME/UMBRA. The four
> telegraph placeholders return false, so the default painter draws them in those families.

```java
public final class SaintTelegraphRenderer { public static void render(RenderLevelStageEvent event); }

public interface SaintTelegraphStyle {
    /** True when the style drew this move; false falls back to the default painter. */
    boolean paint(TelegraphDraw draw, TelegraphCanvas canvas);
    default boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas) { return false; }
}

public record TelegraphDraw(SaintTrialEntity saint, SaintMove move, BossFootprint footprint, FootprintFrame frame,
        BossMoveTimeline timeline, BossMoveTimeline.Stage stage, float moveTime, float age, TelegraphSpec spec,
        float reveal, float commitFlash, float partialTick) { }
public record HazardDraw(BossGroundHazardEntity hazard, BossFootprint shape, FootprintFrame frame, float age,
        float life, float partialTick) { }
/** The family-to-material map over LuxUmbraGeometry, MaterialQuads, FerricDuctilisRenderTypes and the flame types. */
public final class TelegraphCanvas { ... }
```

One entry in `WorldEffectDispatcher`, straight after `ManipulationVisualRenderer`. Its input is only synced data:
`DATA_MOVE*`, `DATA_PHASE`, `DATA_DEEDS`, decoded by `SaintMoveView` into `SaintMoveInputs` and a `FootprintFrame`,
then `move.footprint(in)`, `move.timeline(in)` and `move.telegraph(in)`. It draws from the reveal tick to the
commit, and through the active span when `showWhileActive`, with a 4-tick commit flash on the edge. The reveal tick is
clamped so a footprint always shows for at least `revealFloor(kind)` ticks before its commit (20 area, 12 melee),
whatever a deed hides. Families map to materials: ANIMUS, CRUOR, LUX, DUCTILIS, PUTRICIEL_ROT, RIME and UMBRA to their
`LuxUmbraBatch.Material`; IRON to `FerricDuctilisRenderTypes.IRON`; FLAME to the Flammeus flame types. A null family
uses the Saint's. The default painter keeps to the subtlety budget: ground fills at alpha 0.22 or less, edges 0.012
wide or less, no strands. It never draws during the rise or the ending. Hazards draw in the same pass through
`paintHazard`, else as their shape's default fill.

### 19. Small shared helpers the kit adds

> Built 2026-11-05: `SaintLines.teach` returns whether it showed and takes an optional colour; the pure rules are
> `firstTeaching` and `voiceDue`; `reset(saint, challengers)` runs at the trial's end. `SaintRoot` adds
> `release(player, key)`, `releaseAll(player)` and `rooted(player)`; expiry runs from its own `PlayerTickEvent.Post`
> subscriber and death releases. Both, and `SaintHealing`, are cleared by the hub.


Not requested by name, but all four designs need them and each would otherwise write its own:

- **`SaintLines`** (`common/entity/boss/saint/`): `teach(Player player, String key)` once per fight per player on the
  action bar (grey italic by default, a Saint colour allowed); `voice(SaintTrialEntity saint, String key)` in chat to
  challengers, at most once per 200 ticks per line; both reset with the trial.
- **`SaintRoot`** (`common/entity/boss/saint/`): `root(Player player, int ticks, ResourceLocation key, boolean jump)`
  through held `AttributeModifiers.sync` modifiers, removed on release, death, logout and the trial's end. Seraphae's
  Knotting and burst, Putriciel's Overgrown, Velorum's preservation and Still Floor all root.

### 20. Edits the kit makes outside its own packages

> Built 2026-11-05: the kit ships the ten `death.attack.hemomancy.saint_*` lines (`LangCoverageTest`, which now reads
> each damage type's `message_id`, needs them; Saint builders may reword them in place). Damage types use exhaustion
> 0.1. `HemoEasing.ramp` is `smoothstep(from, to, t)`; `annetta_knowles.py` imports HemoEasing so the generator writes
> the same model. `EasingCopyGuardrailTest` flags any `(float t, float... keys)` curve; `BossSceneTracks.keys` and
> `MycophantIntroClientState.keys` are listed (smoothstep segments, not Catmull-Rom). `PlayerBlockEditGuardrailTest`
> lists `SaintArena` in a new non-player editors check (it walks only `manipulation` and `network`).
> `GuiLayerRegistrar`, `WorldEffectDispatcher`, the bar-frame classes, `PacketHandler` and the payload registry are
> the client stage's.


- `BloodAbsorptionItem` (router after the Vesper hook; `isValidAbsorptionTarget` refuses Saints and companions),
  `BloodProjectionItem.performProjection` (router after `EarlyInitiation`), `LivingStaffItem` (router after its Vesper
  hook).
- `HealModifier`: `multiplier()` becomes `multiplier(LivingEntity)` (existing entries keep their constants) and one new
  entry `SAINT_PRESENCE(SaintHealing::multiplier)`. `TransientServerStateEvents` clears `SaintHealing`.
- `ManipulationCombatHelper`: a `SaintCompanion` interface (`common/entity/boss/saint/`: `UUID saintId()`,
  `Allegiance allegiance()`, `Allegiance { CHALLENGERS, SAINT }`). `canHarm(player, companion)` is false for a
  CHALLENGERS companion; `canHarmFor(companion, target)` lets a CHALLENGERS companion harm only its Saint; a Saint's
  `canHarmFor` skips its own SAINT companions.
- `HemoEasing`: `catmullRom(float t, float... keys)`, `ramp(float t, float a, float b)`, `window(float t, float a,
  float b, float c, float d)`, lifted from `AnnettaKnowlesModel`, which then calls them; `EasingCopyGuardrailTest`
  learns the pattern.
- `EntityInit` and the renderer registrar: `boss_ground_hazard`. The five damage type JSONs and the tag.
- `GuiLayerRegistrar` (the layer), `WorldEffectDispatcher` (the pass), `BossBarFramePainter` and
  `BossBarFrameClientState` (bar origins and room), `PacketHandler` and the payload registry,
  `client/registry/SaintClientStyles`, the eight placeholders.
- Allow-lists and indexes: `PlayerBlockEditGuardrailTest` lists `SaintArena` as a non-player editor;
  `HudAndWorldDrawingGuardrailTest` gets the new layer and pass in order; `ARCHITECTURE_STANDARDS.md` gets a guardrail
  index row.

### 21. The guardrail: `SaintCombatKitGuardrailTest`

> Built 2026-11-05 (client): rule 7 is `oneSaintPayloadOneHudLayerAndOneFootprintPass` (the only `Saint*` file in
> `common/network/boss`, the only Saint names in `GuiLayerRegistrar` and `WorldEffectDispatcher`, the pass straight
> after the manipulation visuals, and no client file outside the telegraph kit or a Saint-named file naming
> `BossFootprint`, `TelegraphDraw` or `HazardDraw`); rule 8's style half is `everySaintHasBothClientStylesAndTheyExist`.

> Built 2026-11-05: rules 1 to 6, 8's first half (move enums in their Saint's package) and 9 are in. The legacy
> allow-list holds nine files (the kit's own legacy `SaintTrialEntity`, `SaintSecondaryAttacks` and
> `SaintAttackPattern`, the four Saints, and Seraphae's anchor and fragment); the test hard-codes them as the ceiling,
> so an entry can leave and never arrive. Rule 2's raw-damage check exempts `SaintTrialEntity`, whose reward roll
> keeps a generic source as its fallback.
>
> Compile gate 2026-11-06: every Saint is on the kit, so the legacy pieces are gone and the allow-list with them.
> `saint-kit-legacy-allow-list.txt` is deleted (archived under `removed_assets/`); `saintsUseTheKitNotTheLegacyPieces`
> now requires no file to break rules 1 to 5, and the `LEGACY` pattern still bans the deleted names coming back.


`src/test/java/com/vincenthuto/hemomancy/architecture/SaintCombatKitGuardrailTest.java`, with
`src/test/resources/architecture/saint-kit-legacy-allow-list.txt` (the four Saint files that still use legacy pieces;
an entry may leave, never arrive; the compile gate empties it). Through the `support/` helpers it holds:

1. No file under `common/entity/boss/saint/` adds goals, calls `registerTrialGoals`, `tickSecondaryAttacks`,
   `setVisualState` or `sendFloorRing`, or names `SaintAttackPattern`, unless allow-listed.
2. No Saint class overrides `actuallyHurt` or `die`; no file there calls `damageSources().indirectMagic(`,
   `.generic()` or `.mobAttack(` (Saint damage goes through `SaintMoves.hurt`).
3. No `setBlock`, `setBlockAndUpdate`, `removeBlock` or `destroyBlock` under `common/entity/boss/saint/` outside
   `SaintArena.java`.
4. Players are gathered only through `arena.challengers()` or `SaintMoves.struck` (no `getEntitiesOfClass(Player.class`
   under `saint/` outside the kit's files).
5. `SaintChannelRouter` is the only Saint code named by `BloodAbsorptionItem`, `BloodProjectionItem` and
   `LivingStaffItem`; no Saint file calls `isChannelingBloodAbsorption` or polls `getUseItem` to fake a channel.
6. `SaintCombatController.cancel()` is called only from `SaintTrialEntity`'s ending and from `SaintPhaseTable` with
   `Cut.IMMEDIATE`.
7. `SaintMeterPacket` is the only payload under `common/network/boss/` named `Saint*`; `SaintMeterLayer` is the only
   HUD layer a Saint draws through; `SaintTelegraphRenderer` the only Saint footprint pass.
8. Every enum implementing `SaintMove` lives in its Saint's package; every `SaintClientStyles` entry resolves.
9. `SaintChamberLayout`'s constants match `tools/worldgen/mausoleum/pieces.py`.

### 22. Tests the kit ships

> Built 2026-11-05 (client): `SaintMeterPacketTest` and `SaintTelegraphTimingTest` (JVM). `SaintKitGameTests`
> (namespace and suite `saint_kit_validation`, `runSaintKitGameTestServer`, in the combined gate) runs on the empty
> template with a bare `SaintTrialEntity` on a one-move test kit (no tomb, so the fallback arena) rather than the
> mausoleum chamber, and a legacy Hemorath for the inert cases: endTrial, removal, reload and leash restores (the leash
> case binds a tomb so the chamber resolves, in its own batch `saint_kit_leash`), owned hazards and entities, the
> router's PASS, the refused drain, and the meter's join, CLEAR on leaving, rejoin and CLEAR on removal. The native
> look is reviewed with `runBossPresentationReviewClient -PsaintKitReview=true` (`SaintKitClientReview`: two legacy
> Saints' bars, a hand-sent meter packet, a hazard of every shape).

> Built 2026-11-05: all the JVM tests above except `SaintMeterPacketTest` (client stage); `HemoEasingTest` gained the
> Catmull-Rom golden values. Tests share the `TestMoves` table. The GameTests are the client stage's.


JVM, under `src/test/java/com/vincenthuto/hemomancy/common/entity/boss/` and `.../boss/saint/`:

- `BossFootprintTest`: hits and misses for every record, height bands and `groundOnly`, lane gaps and fans, spoke
  wedges, sweep angle by age, ring waves and the jump band, annulus arcs turning, path walkers, region exclusions,
  tethers re-read as their ends move, union delays; NBT round trips.
- `BossFootprintAgreementTest`: points sampled inside each shape's drawn outline (from the motion methods) hit, and
  points just outside miss.
- `BossBandsTest`: bands per threshold set, sectors, degrees from facing.
- `SaintPickRulesTest`: cells, eligibility, cooldowns, the 0/0.4/0.6/0.8 history, the 200-tick pressure windows,
  follow-ups ignoring damping and cooldowns, the two-deep cap, an interrupt cancelling a chain.
- `SaintControllerRulesTest` (the controller's pure rules): commit locks the aim, LEAD, interrupts queued to the
  recovery boundary, `Cut.IMMEDIATE`, wind-up and reveal floors, recovery openings stretching the recovery.
- `SaintMeterTest`: drift, floor, ceiling, thresholds both ways, per-player states, last contributor, max by
  challengers, the send thresholds.
- `SaintOpeningsTest`: one at a time, `chainTo`, `extend` caps, hit caps with challengers, admit results,
  `ticksSinceClosed`.
- `SaintPhaseTableTest`: the three sources, ratchet, queue against immediate, cards once, save and load.
- `SaintArenaRecordTest` (the arena's pure record): original recorded once, per-cell interval, schedule, `swapFor`
  expiry, budgets with oldest-first revert, the three restore orders over N ticks, save and load.
- `SaintEndingTest`: `endTrial` once at its tick, `finishEarly`, discard at length, resume or finish on load.
- `BossHoldRulesTest`: the point from offset and yaw, slack, release reasons, the `HeldTraveller` refusal.
- `SaintChannelRouterRulesTest`: `aimsAt` reach, cone and sight; nearest Saint first; a refused result stops the
  item's own paths.
- `SaintDamageRulesTest`: enrage scaling, factor stacking with the opening, the floor of 1, the floor reached once.
- `SaintHealingTest`, `HemoEasingTest` (Catmull-Rom matches Annetta's golden values; ramp and window),
  `SaintMeterPacketTest` (codec round trip; `CLEAR`).

GameTests, `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SaintKitGameTests.java`, batch
`saint_kit_validation`, on the mausoleum chamber template with a real mock player and a legacy Saint (so it also
proves the kit is inert for one): the arena restores an edited cell on `endTrial`, on `remove`, after a reload and on
the leash reset; owned entities and hazards go with it; the router returns `PASS` for a legacy Saint and the item
paths run as before; a Saint body refuses the ordinary absorption drain; `SaintMeterPacket` reaches a challenger and
`CLEAR` follows their leaving.

### 23. Request ledger

| Saint | Request | Verdict | Where |
| --- | --- | --- | --- |
| Hemorath | 1 channel router | accepted, merged with Seraphae K-1, Putriciel K-3, Velorum K-6 | §11 |
| Hemorath | 2 RADIAL, REGION, union | accepted | §1 `Spokes`, `Region`, `Union` |
| Hemorath | 3 weight and eligibility hooks | accepted, merged with Velorum K-9 | §3 |
| Hemorath | 4 per-player secondary meter | accepted, merged with Seraphae K-6, Putriciel K-13, Velorum K-1 | §5, §16 |
| Hemorath | 5 dynamic presence | accepted, merged with Seraphae K-9's cadence | §6 |
| Hemorath | 6 dynamic leak and floor | accepted | §5 `Rule.drift`, `floor` |
| Hemorath | 7 scheduled edits and scan | accepted, merged with Seraphae K-8, Putriciel K-8, Velorum K-5 | §8 |
| Hemorath | 8 telegraph style hooks | accepted, merged with Putriciel K-1 | §3 `TelegraphSpec`, §18 |
| Hemorath | 9 old blood debt retirement | rejected for the kit | integration stage |
| Hemorath | 10 phase advance by event, immediate cancel | accepted; part 3's rule amended | §9, §4 |
| Hemorath | 11 generic boss hold | accepted, merged with Seraphae K-2 | §12 |
| Hemorath | 12 max-health scale per challenger | accepted, shared with Velorum | §15 |
| Seraphae | K-1 channel target | accepted (merged) | §11 |
| Seraphae | K-2 BossHold | accepted (merged) | §12 |
| Seraphae | K-3 union, moving line, occluded, NONE | accepted | §1 `Union`, `Tether`, `Occluded`, `None` |
| Seraphae | K-4 phase ratchet | accepted | §9 |
| Seraphae | K-5 bar shows meter | accepted as a bar hook, shared with Putriciel | §15 `barProgress` |
| Seraphae | K-6 style gets the entity and a secondary | accepted (merged) | §16, §17 |
| Seraphae | K-7 gains by delivery and school, caps | accepted in part: classification and caps in the kit, per-school tables in her rules | §7 |
| Seraphae | K-8 predicate scan, batch swap, ordered restore | accepted (merged) | §8 |
| Seraphae | K-9 per-player maps, cadence override | accepted in part: state on the entity (§5), cadence (§6); no static map, so no `TransientServerStateEvents` entry | §5, §6 |
| Seraphae | K-10 damage type | accepted: the kit ships all five | §3 |
| Seraphae | K-11 target hook, pick veto | accepted, merged with Putriciel K-12 | §4 |
| Seraphae | K-12 Catmull-Rom in HemoEasing | accepted | §20 |
| Putriciel | K-1 per-move telegraph family | accepted (merged) | §3 |
| Putriciel | K-2 per-move damage type | accepted | §3 |
| Putriciel | K-3 channel routing | accepted (merged) | §11 |
| Putriciel | K-4 Saint presence heal modifier | accepted, merged with Velorum K-10 | §6, §20 |
| Putriciel | K-5 state-driven phases | accepted | §9 `bySaint` |
| Putriciel | K-6 per-Saint bands | accepted, shared with Velorum | §2 |
| Putriciel | K-7 enrage bonus on move damage | accepted for every Saint | §3 `SaintMoves.damage` |
| Putriciel | K-8 edit caps, revert, tracked entities | accepted (merged) | §8 |
| Putriciel | K-9 timed ending | accepted, merged with Velorum K-4 | §14 |
| Putriciel | K-10 generic ground hazard | accepted (four Saints have lingering hazards) | §13 |
| Putriciel | K-11 extra footprints | accepted | §1 `Union`, `Region`, `Annulus` |
| Putriciel | K-12 quarry hook | accepted (merged) | §4 |
| Putriciel | K-13 entity read in the style | accepted (merged) | §17 |
| Putriciel | K-14 shared test cleanup | rejected for the kit | compile gate |
| Velorum | K-1 player state | accepted (merged) | §5, §16 |
| Velorum | K-2 opening chains and extension | accepted as `chainTo` and `extend`; the condition stays in his rules | §7 |
| Velorum | K-3 phase floors and guard | accepted as hooks; his floors and guard are his | §15 `healthFloor`, `damageTakenFactor` |
| Velorum | K-4 ending window | accepted (merged) | §14 |
| Velorum | K-5 removals, timed swaps, companions | accepted (merged) | §8 |
| Velorum | K-6 Saint blood contact | accepted (merged into the router) | §11 |
| Velorum | K-7 annulus, lanes, moving path, sight line | accepted | §1 `Annulus`, `Lane`, `Path`, `Tether` |
| Velorum | K-8 muffle and screen paint | accepted (only the kit's layer can give them) | §17 |
| Velorum | K-9 picker context | accepted (merged) | §3 |
| Velorum | K-10 two HealModifier entries | rejected as asked; one kit entry serves Putriciel and Velorum | §6 |
| Velorum | K-11 FrozenDevoteeBlock endpoint | rejected: the router runs before the block paths and hands him the picked block, so the shared block is not edited | §11 |
| Velorum | K-12 companion allegiance | accepted (only the shared combat helper can give it) | §20 |
| Velorum | K-13 hitbox in EntityInit | rejected: override `getDefaultDimensions` in his own class, as Hemorath does | his lane |
| Velorum | K-14 preserved player pose | rejected for the kit: optional, a registry entry the integration stage may add after the warp chair | integration |

## Cross-Saint notes

Read side by side by the reconciler. The designs are not rewritten here; each note names the file and section. Files
are `2026-11-05-saint-<name>-overhaul-design.md`, shortened to Hemorath, Seraphae, Putriciel and Velorum.

### A. Headline moves that feel the same

1. **Three parallel lanes from the feet.** Hemorath §7 `IRON_RAKE` (three lanes 14 long, 2 apart, near to far, stand
   in the gaps), Velorum §7 `BLACK_ICE` (three lanes at ±2, 12 long, a front at 1 block a tick, stand in the gaps) and
   Putriciel §7.2 `CAUTERISING_LANE` (centre and side lanes at ±3, 12 long, a front at 1 block a tick). Same shape,
   speed and answer. Black-Ice is the old `FROZEN_ADVANCE`, which already shared `SaintAttackPattern.lane` with
   `IRON_RAKE`. Velorum's phase-3 fan, used from phase 1, or a single wide lane for Putriciel, would separate them.
2. **A disc under each challenger plus two.** Hemorath §7 `IRON_THICKET`, Seraphae §7 `healed_ground` and Velorum §7
   `SPLINTER_CROWN` use the same placement rule, and Putriciel §7.1 `SHELF_VOLLEY` is close (quarry plus led discs).
   All four then leave a standing hazard. A placement that says something about the Saint (Seraphae's on the wounded,
   Velorum's where players stood still longest) would tell them apart.
3. **Jump the ring.** Every Saint has one, most with a second wave: Hemorath `MARROW_RUPTURE` (band top 0.9),
   Seraphae `convulsion` (0.9) and its wider repeat, Putriciel `SAC_SWELL` (1.0), `IGNITION` (1.2) and `LAST_PURGE`
   (1.0, three rings), Velorum `HOLLOW_TOLL` (0.8) and its repeat, plus `STILL_FLOOR`. See B-9 for the heights.
4. **A far tether broken behind a rank pillar.** Seraphae §7 `transfusion_thread` (a white thread from her heart to the
   target's chest, gold beads, anti-kite, broken by 4 ticks without sight) and Velorum §7 `BID_TO_KNEEL` (five
   hairline threads from the target's chest to his palm, anti-kite, broken by sight) look and play alike. Hemorath §7
   `TITHE_RECKONING` (chain to the target, broken by sight at commit) is the third; it differs because it can be paid.
   The same pillar ranks at z 5 and 21 are the answer in all three, and to Hemorath's siphon, Seraphae's Eye and
   Velorum's presence and Long Night.
5. **The staff slam that sticks.** Hemorath §7 `STAFF_FALL` (raised two-handed, slammed, Planted 24 ticks) and
   Putriciel §7.1 `STAFF_PLANT` (raised two-handed, held, driven down, Staff Caught 30 ticks) are one move; Velorum's
   `STILL_FLOOR` also ends in a 30-tick planted-staff punish.
6. **Swing and return.** All four open with a wide body sweep and its return string: Hemorath `RAKING_CLAW` and
   `BACKHAND`, Seraphae `nerve_lash` and `counter_lash`, Putriciel `CENSER_SWING` and `CENSER_BACKSWING` (and again
   `PURGING_SWEEP` and `PURGING_RETURN`), Velorum `LANTERN_SWEEP`. Only Seraphae's low-then-high split has a different
   answer. Expected for melee, but Putriciel carries two.
7. **The grab.** Hemorath §7 `GORGE` (lunge, held against the body, drained, broken by hits or a cast) is Seraphae's
   headline reach with a drain. It is deed-only, but a forced Apotheos with the Downfall skipped fights it all fight.

### B. Numbers that should agree

1. **Range bands.** Hemorath §7 and Seraphae §7 use 3.5, 7, 13; Putriciel §7 2.5, 6, 11; Velorum §7 2.5, 5.5, 11. Same
   15-block floor, bodies 0.9 to 1.4 wide. The kit allows per-Saint bands, but standing 3 blocks off counts as HUG on
   Hemorath and NEAR on Velorum. Agree on one set or give a reason per Saint.
2. **Melee or area.** No design defines it, and several moves sit under the 20-tick area floor with a footprint away
   from the body or past NEAR: Seraphae `wing_grasp` (disc at the target, 16), Velorum `SET_IN_PLACE` (disc at the
   target, 18), Putriciel `OFFERING_GRASP` (6-block lane, 18), Hemorath `CROWN_FALL` (disc of 3.5, 12) and `GORGE`
   (lunge, 16). Proposed rule for `SaintMove.kind()`: AREA is any footprint not attached to the body or reaching past
   7 blocks. Wide sweeps of 6 to 7 blocks at 12 to 16 ticks (Seraphae `counter_lash` 12, Putriciel
   `CENSER_BACKSWING` and `PURGING_RETURN` 12, Velorum `LANTERN_SWEEP` 14) are then melee, on the edge.
3. **Beholding hides tells by different amounts.** Hemorath §10: area footprints show for their last 20 ticks (never
   under 20 or 12). Seraphae §10: the reach's lines show only in the last 8 ticks. The kit clamps reveals to 20 and 12,
   so Seraphae's becomes 20 (or 12 if the reach is MELEE).
4. **Held Projection rates.** Hemorath §4 and §6 take 20 blood a tick (Gorged, Strained, Last Thirst, Tithe), 1000 in
   one Gorged window. Putriciel §6 takes 5 a tick, Seraphae §9 4, Velorum §6 4 into him and §9 3 to wake and 2 to pin
   or warm. Hemorath's is four to ten times the others. Reaches also differ (Hemorath 6, Putriciel 5.5 and 5); the
   router passes the item's own reach, which every Saint should use.
5. **Scaling per extra challenger.** Health: Hemorath §15 ×(1 + 0.35(n − 1)), Velorum §15 ×(1 + 0.6(n − 1)). Meters:
   Hemorath's maximum and Seraphae's decay ×(1 + 0.5(n − 1)), Putriciel's fill ÷(1 + 0.6(n − 1)). Two coefficients
   each for the same job. Counting also differs: Hemorath counts at the rise's end and ratchets, Velorum on entry,
   Seraphae by the chamber box, Putriciel does not say. The kit counts once (`challengerCount()`, ratcheted from the
   rise's end) and all four should read it.
6. **Fight length.** Seraphae §4 (about five minutes) and Putriciel §3 (about four) give worked estimates. Hemorath
   gives only the forced overload road (three to four minutes, §15) and Velorum only a call (§20). Both need a worked
   proper-awakening number against the four-to-six-minute target.
7. **Decision delay.** Putriciel §7 and §8 give none; Hemorath 14, 18, 24 by meter state, Seraphae 14, 10, 8, Velorum
   24, 18, 12.
8. **Pose blending.** Hemorath §13 and Putriciel §13 blend 4 ticks from the last pose, Velorum §13 6; Seraphae §13
   makes every track absolute with no blend, so she will snap between moves.
9. **Jump clearance.** Ring tops of 0.8 (Velorum), 0.9 (Hemorath, Seraphae), 1.0 (Putriciel) and 1.2 (Putriciel's
   `IGNITION`, too high for a sure vanilla jump). The kit's `Height.JUMPABLE` is 0 to 0.9; all jumpable waves should
   use it.
10. **Ending on reload.** Putriciel §11 runs `endTrial` on the first tick of a reloaded ending; Hemorath §11,
    Seraphae §11 and Velorum §11 resume it. The kit offers both (`resumeOnLoad`); resuming keeps the ending whole.
11. **Texel density.** Hemorath §12 lands at about 15.5 px per block (2 texels a unit at render scale 2.06); the others
    land at 16.
12. **Hitboxes.** Hemorath §8 overrides `getDefaultDimensions` in his own class; Seraphae §18 and Velorum §18 K-13 ask
    for `EntityInit` edits. The override keeps it in the lane for all three.

### C. Names that clash

1. **"Gutter".** Seraphae §6 Gutter (the Eye's recovery), Putriciel §3 and §6 Gutter (the interval's end), Velorum §6
   the Guttering Lantern (track `gutter`, §13), and Hemorath's arena is built of `blood_gutter` blocks. At least two
   openings need new names.
2. **"Tithe".** Hemorath §4, §6, §7 `TITHE_RECKONING`, the Tithe opening and `HemorathTitheLedger`; Velorum §7 and §10
   `TITHE`, the Tithe of Silence. Both are blood paid to the Saint.
3. **"Last ...".** Hemorath's Last Thirst, Seraphae's Last Reach, Putriciel's Last Purge: three desperation moves on
   one naming pattern.
4. **Death message keys.** Seraphae §14 writes `death.attack.hemomancy.saint_seraphae`; Putriciel §17 step 12 writes
   `death.attack.saint_putriciel*` (no namespace); Hemorath and Velorum do not say. The kit's `message_id` makes it
   `death.attack.hemomancy.<id>` for all.
5. **Move ids.** Seraphae §7 writes ids in lower case (`laying_on_of_hands`); the others use enum constants. Constants
   upper snake case, `key()` lower.
6. **Kit request numbers** restart at K-1 in each design; the ledger above names them by Saint.

### D. Rules and anti-goals

1. **Hemorath shown only as bloated.** Hemorath §10's Downfall deed floors the meter at 35%, so a forced Hemorath never
   returns to the Sunken rest silhouette, and §15 says a forced Apotheos usually fights all three deeds.
2. **Red on Velorum.** Velorum §12 adds frozen `cruor` (dark oxblood) on the halo links. The rule is ice blue and pale
   frost against black. The lore line supports it; it needs the user's yes.
3. **Time manipulation.** Velorum §7 and §12 `ENDLESS_HOUR` damps every motion to zero, freezes the motes where they
   hang and thins the sound. That reads as time stopping, close to the anti-goal; slow motes instead of stopped ones
   would keep it on the stillness side.
4. **Harder, not longer.** Putriciel §10's Repose deed removes the Gutter (its ×2 Kindling head start) and its Litany
   deed shortens Offerings to 40 ticks; both lengthen a forced fight, which §15 says they do not. Velorum §10's Repose
   cuts the Bow from 70 to 40 ticks and his Litany removes opening extensions, so fewer damage ticks reach a health
   bar; §15 says not longer. Both need a worked number.
5. **Blood without a named source.** Hemorath §7 drains blood with `RAKING_CLAW` (80), `MARROW_SIPHON` (6 a tick),
   `GORGE` (10 a tick), `EXSANGUINATING_TIDE` (15 every 10 ticks) and `LAST_THIRST`'s gutters (3 a tick), but names
   only `hemorath_presence` and `hemorath_well_offering`. Velorum §4 and §9 pay for warming, waking and pinning
   through "a named source" without naming it. Seraphae §6 and §9 do not say whether the Condensing feed and the
   reserve wake use `seraphae_anchor_binding`.
6. **The first minute.** Putriciel §3's Rot I lasts 55 to 65 seconds before the first interval, so the first Offering
   comes at about 75 seconds. The Kindling is taught at the bar reveal, so the meter is taught in time; the absolution
   is not.
7. **Near-timers.** Putriciel §7.1 `BLOOM_OF_ROT` becomes eligible after 900 ticks in a phase with weight 30 in every
   cell, so it fires a few picks after tick 900 of every rot phase. Velorum §7 `ENDLESS_HOUR` is forced at phase 3 and
   then at most every 1,200. Both are gated rather than timed, but should be watched in review.
8. **Seraphae's lane workaround** (§17, polling `BloodAbsorptionItem.isChannelingBloodAbsorption` from her own
   entities) is moot once the router lands, and the kit guardrail forbids it.

### E. Edits outside a lane

1. Hemorath §17 step 5 deletes `common/entity/boss/goal/HematicCollapseGoal.java` and `EmptyPulseGoal.java` (outside
   `saint/hemorath/`: the compile gate's), and step 6 removes lang keys (`en_us.json` is add-only for Saint builders).
2. Velorum §17 step 11 removes dead lang keys, and step 12 removes old tests including the Velorum cases in the shared
   `SaintPowerRulesTest`, `SaintPowerGameTests` and `SaintPatternGeometryTest` (the compile gate's).
3. Velorum §18 K-11 edits `common/block/harbinger/decoration/FrozenDevoteeBlock.java` and K-13 an existing
   `EntityInit` line; both are now unnecessary (router, own override).
4. Putriciel §12.4 and §17 step 8 add `arc` and `line` to the shared `client/render/world/PyreFlames.java` (also used
   by `PulsePylonRenderer`) and make `client/render/tile/harbinger/decoration/RotPyreRenderer.java` read a new query.
   Neither file is in the lane: both are compile-gate follow-ups, or Putriciel keeps its tongues in its own geometry
   class.
5. `ManipulationVisualRenderer` is shared. Hemorath §16, Putriciel §12.4, Velorum §12 and Seraphae §18 all keep or add
   a hook line there; a new hook line (Seraphae's `SeraphaeMoveGeometry`) is a compile-gate follow-up, as Seraphae
   already lists it.

### F. Resolved in the kit

- Other players' meters on the client: Putriciel §5 syncs everyone's colonisation in its own compound, and Seraphae §5
  (sores on other players) and Velorum §12 (the ice shell on other players) need the same without saying so. The
  kit's `DATA_CHALLENGERS` serves all three; Putriciel can drop its own compound.
- Teaching lines: Hemorath (narration, no channel named), Seraphae (grey italic action bar), Putriciel (pale cream
  italic, no channel named) and Velorum (action bar) present them four ways; `SaintLines.teach` gives one.
- Rooting: Seraphae §5 (jump strength 0), Putriciel §5 (−100% speed and zeroed upward motion), Velorum §4 and §7 (held
  modifiers) root three ways; `SaintRoot` gives one.
- Enrage scaling: Seraphae divides by a base of 4, Velorum by 9, Putriciel quotes "before ×1.3", Hemorath is silent;
  `SaintMoves.damage` applies one rule to every table.

## Resolutions that override the designs (2026-11-05)

The user read the four one-screen summaries and said to go ahead with the defaults. This section settles the cross-Saint
notes above. Where a per-Saint design disagrees with this section, this section wins. Each Saint builder's first step
is to update its own design to match (mark each change "Resolved 2026-11-05"), then build.

### The user's answers (all defaults)

1. Hemorath's first full meter in phase 1 empties him into phase 2 (the blood tide, the health tear, "The Vessel
   Empties" card); only a later fill bursts him.
2. Openings are the fight. Seraphae: hits outside an opening add nothing to integrity. Velorum: 30% damage outside his
   openings, health floors at 60% and 25% that only an opening passes.
3. The fights may change real chamber blocks (Hemorath's gutters fill, Seraphae's healed stone glows, Putriciel scorches
   the boss floor to packed ash), always through `SaintArena` and always restored.
4. Seraphae speaks short nurse's lines; Velorum speaks four lines (the rise, the first preservation, the proper phase 2,
   the Endless Hour); Putriciel is silent, with narration only. Voice sheets for Seraphae and Velorum go into
   `docs/NPC_VOICES.md` (final review stage).
5. Seraphae is rebuilt at about 3.2 blocks tall at render scale 2.0; her gilt halo and trims become bone and her locks
   hematic iron, so gold on her is only ever nerve light.
6. When Velorum's body fails, the woken devotees walk back to their places and kneel, and are preserved again.
7. Putriciel gets jointed legs under the robe for a shamble; Velorum glides.

Every other question in the four designs takes its design's default: Hemorath's Tithe as a per-fight ledger, the Last
Thirst stopping the leak, drinking healing him; Putriciel's Offering accepting held Projection, bog bodies rising on a
skipped Downfall; Velorum's Apotheos pinning figures with hyphae; and the rest as each design wrote them.

### A. Each headline move belongs to one Saint

1. **Lanes.** Hemorath keeps `IRON_RAKE` (iron cracking out of the floor is Ferric). Velorum's `BLACK_ICE` becomes his
   fan: five narrow leaning splinter rows fanned about 15 degrees apart that grow from his feet and then stand as black
   ice for 60 ticks, narrowing the floor, so its answer is to move through the gaps before they close, not to wait in a
   gap. Putriciel's `CAUTERISING_LANE` becomes one wide fire run (3 blocks) from a lit pyre toward Putriciel, cleansing
   in Putriciel's own geometry.
2. **Placement under the players.** Hemorath's `IRON_THICKET` erupts where the challengers' blood fell in the last 5
   seconds (where they were hit or paid). Seraphae's `healed_ground` grows under the most wounded challenger and where
   she last healed. Velorum's `SPLINTER_CROWN` rises where challengers stood still longest in the last 5 seconds and
   shatters at once (no standing hazard; his standing hazard is the black ice). Putriciel's `SHELF_VOLLEY` stays as
   thrown shelves that land and root as colonies.
3. **One jump ring per Saint at most.** Hemorath keeps `MARROW_RUPTURE` as the jump ring. Putriciel's jump ring is the
   desperation purge (renamed, see C-3) at `Height.JUMPABLE`; `SAC_SWELL` becomes a slow spore cloud you walk out of
   (full height band, not jumpable) and `IGNITION` a body burst you must be out of range of by its commit. Seraphae's
   `convulsion` becomes gold nerve spokes with rotating gaps you step into, not a ring. Velorum's `HOLLOW_TOLL` strikes
   anyone whose speed is below walking pace as the wave passes (keep moving and it passes through you), and
   `STILL_FLOOR` roots anyone grounded and not moving at its commit; neither is jumped.
4. **Tethers.** Seraphae keeps `transfusion_thread` (the nurse's line). Velorum's `BID_TO_KNEEL` becomes a gaze: while
   his raised lantern's cold cone holds you in sight, your stillness climbs fast and you slow; break sight behind a
   devotee or a pillar. No thread is drawn. Hemorath's chain move keeps its chain because it can be paid.
5. **The staff slam.** Hemorath keeps `STAFF_FALL`. Putriciel's `STAFF_PLANT` becomes a censer drop: the censer is
   swung up on its chain and dropped, bursts, and fouls in the floor; the opening is the fouled chain while Putriciel
   drags it free. Velorum's planted staff stays the hold of his invocation, which is already a different opening.
6. **Swing and return.** Melee pairs are allowed once per Saint. Putriciel keeps `CENSER_SWING` and `CENSER_BACKSWING`
   in rot; `PURGING_SWEEP` in the cleansing interval becomes one long reach that leaves a short fire wall, with no
   return swing. Velorum's `LANTERN_SWEEP` is one slow sweep with no return.
7. **The grab.** Seraphae's Laying On of Hands is the only hold. Hemorath's deed-only `GORGE` becomes a pull: his open
   ribs draw the target across the floor toward him unless they sprint away; contact is a drained burst, with no hold.

### B. Numbers that agree

1. One set of range bands for all four: HUG under 3, NEAR under 6.5, MID under 12, FAR beyond. Move weights keep their
   band names; a move whose reach sits on a boundary is re-checked by its builder.
2. `SaintMove.kind()`: AREA is any footprint not attached to the body or reaching past 7 blocks. Floors are 12 ticks
   for melee, 20 for area. The moves listed in cross-Saint note B-2 (`wing_grasp`, `SET_IN_PLACE`, `OFFERING_GRASP`,
   `CROWN_FALL`, and the reworked `GORGE` pull) are area and go to at least 20.
3. Reveals clamp to 20 (area) and 12 (melee), including under the Beholding deed.
4. The router passes the item's own reach and rate. No Saint takes more blood a tick than the held item moves; a Saint
   that needs a held channel to matter more converts each unit at its own rate (Hemorath's overload counts projected
   blood at a multiplier) instead of draining faster.
5. One scaling rule: Saint health and a Saint-owned meter's maximum or decay scale by 1 + 0.5 × (n − 1), with n from the
   kit's `challengerCount()` (ratcheted from the rise's end). Per-player meters do not scale.
6. Hemorath and Velorum add a worked proper-awakening fight length to their designs against the four-to-six-minute
   target.
7. Putriciel's decision delay is 20, 16 and 12 ticks in rot I, II and III, and 10 in the cleansing interval.
8. Every animation track blends from the last pose over 5 ticks; Seraphae's tracks are not absolute.
9. Every jumpable wave uses `Height.JUMPABLE` (0 to 0.9).
10. A reloaded ending resumes (`resumeOnLoad`) for all four.
11. Hemorath lands at exactly 16 px per block.
12. Hitboxes are `getDefaultDimensions` overrides in each Saint's own class; nobody edits an existing `EntityInit`
    line.

### C. Names

1. "Gutter" stays with Hemorath's `blood_gutter` blocks and Velorum's Guttering Lantern. Seraphae's Eye recovery
   becomes the Lapse; Putriciel's end of the interval becomes the Embering.
2. "Tithe" stays with Hemorath (Crimson Tithe is his Canon Memory). Velorum's Tithe of Silence becomes the Draw of
   Silence (`SILENCE_DRAW`), after the lore line "Silence draws".
3. One "Last": Hemorath's Last Thirst. Seraphae's Last Reach becomes the Straining Reach; Putriciel's Last Purge becomes
   the Full Purge.
4. Death message keys are `death.attack.hemomancy.<damage id>` for all.
5. Move constants are upper snake case; `key()` is lower case.

### D. Rules

1. Hemorath's Downfall deed no longer floors his meter. He rises having drunk his wells (the wells empty, the meter at
   25%) and leaks at half rate for his first 1,200 ticks, so a forced Hemorath starts bloated and can still sink back
   to his Sunken rest.
2. Velorum's frozen cruor on the halo links stays: the lore's appearance text gives it ("its hanging links crusted with
   frozen cruor"). Keep it dark, desaturated and small.
3. Velorum's Endless Hour slows motes to a crawl (about a tenth) and never stops them; the damping is on the players
   (slowing, the stillness climb), not on the world. Sound thins; nothing freezes in mid-air.
4. Harder, not longer. Putriciel's Repose deed removes the lull after the fire instead of the Kindling head start; its
   Litany deed is the glyph naming the act the palm accepts and keeps 60-tick Offerings. Velorum's Repose deed keeps a
   shorter Bow (40) with a higher damage-taken factor in it so the damage a Bow admits is unchanged; his Litany deed
   removes opening extensions but brings openings about a fifth more often. Each builder shows the worked number.
5. Every drain from a player is a named `BloodAccounting.Source` constant in that Saint's package, one per distinct
   drain (Hemorath's claw, siphon, tide and thirst among them; Velorum's warming, waking and pinning; Seraphae's
   Condensing feed and reserve wake).
6. Putriciel's first interval starts by about 45 seconds (Rot I's Kindling creeps faster), and the first time the palm
   opens a teaching line names the absolution, so it is taught in the first minute.
7. Gated moves, never timed: Putriciel's `BLOOM_OF_ROT` needs 900 ticks in the phase and Kindling under half, at weight
   12; Velorum's Endless Hour is triggered by state (phase 3 and two challengers or one challenger above half
   stillness, or a health floor reached), not by a cooldown.
8. Seraphae's polling workaround is not built; the router serves her.

### E. Edits outside a lane

Every edit listed in the cross-Saint notes' section E is the compile gate's: deleting `HematicCollapseGoal` and
`EmptyPulseGoal`, removing lang keys, removing the old cases in the shared Saint tests, hook lines in
`ManipulationVisualRenderer`, and any change to `PyreFlames` or `RotPyreRenderer`. Putriciel keeps its fire tongues in
its own geometry class. Velorum does not edit `FrozenDevoteeBlock`; the router hands him the block. A builder records
each such edit it needs under "Compile-gate follow-ups" in its design.

Anything deleted (a texture, a model, a class with art in it) is first copied to the untracked `removed_assets/` folder,
keeping its path, and the whole file is read before it is deleted.

## Final review 2026-11-06

Stage 5 reviewed the whole diff against this spec, the four designs and `AGENTS.md`, then ran `test` (green) and
`alphaCheck` (1424 of 1424 GameTests on the confirming run; the run before failed only the known flaky Puppeteer case
`crossingDimensionsDeliberatelyUnravelsSummon`, which passed on the rerun). Fixed here:

- `SaintTrialEntity.loadKit` and `saveKit` resolve the kit first. Before, a reloaded Saint loaded its meter and phase
  table into the placeholders that `kit()` then replaced, so every reload reset the meter and the phase (and a save
  before the first tick wrote the placeholders). A load also re-applies the phase's bar colour and name.
- `TransientServerStateEvents.onServerStopping` collects the Saints before restoring their arenas, because a restore
  discards owned entities from the list being walked.
- `PutricielEntity.hazardTouches` asks `canHarmFor`, so a Creative or allied player in a hazard is not colonised or set
  alight.
- Velorum's four lines carry a "Velorum: " prefix like Seraphae's, so the speaker is clear in chat.
- `src/gameTest/resources/frozen/registry-ids.txt` gained the seven new ids (five `saint_*` damage types,
  `boss_ground_hazard`, `velorum_devotee`), written by `HEMOMANCY_FROZEN_SNAPSHOT_OUT` and diffed: additions only,
  as earlier feature commits did. Flagged for the user's review.
- Docs: `SAINT_MAUSOLEUMS.md`, `LORE_REFERENCE.md` §10, `HEMOMANCY_REFERENCE.md` §5.8, `BOSSES.md`,
  `wiki/Saints-and-Encounters.md`, `NPC_VOICES.md` (Seraphae and Velorum), `OPEN_QUESTIONS.md` (D-2 out of 1.1),
  `TESTING.md`; the source-text impact map regenerated.

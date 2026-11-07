# Saint Putriciel overhaul: *Putriciel purified*

Date: 2026-11-05. Status: design, written in run 1 of the Saint overhaul workflow from the umbrella spec
`docs/superpowers/specs/2026-11-05-saint-overhauls-design.md` (its kit, rules, fight-shape targets, lanes and anti-goals
bind this document). Status: approved with the defaults on 2026-11-05, updated to the umbrella's "Resolutions that
override the designs" (section 0 lists every change, each marked Resolved 2026-11-05) and built in run 2 (see "Build
notes 2026-11-05" at the end). Where a table further down still shows an older number, section 0 wins.

The user's rulings this design carries (2026-10-26, `SAINT_MAUSOLEUMS.md` "Rulings that bind", restated in the task):
fire only in a dedicated cleansing interval; the fire is the Flammeus look (crimson body, pink-white heat, black soot tips),
never green or yellow in any fire texel; everything else on Putriciel is ochre, olive, mould green, muted yellow, pale
cream and blackened charcoal with no red; they/them now (he in life); five absolutions end the trial and they rest until
the rot reclaims them; no bone-brick trims. Standing feedback that also binds: subtle, partly clear overlays (no
"spaghetti"), 16 px per block after render scale, no vanilla `ParticleTypes` in touched files (glow motes for traces,
never `hitGlow`), no copper, brass, bronze or silver accents, the jointed elbow/knee rig for humanoids, VFX from the
existing toolkit through public bridges, the Hematic Riposte bar for focal moments, delete first-attempt features outright
(no migration), boss animation in Java on the move's own clock.

## Revision request 2026-11-06 (the user, after visual pass 2; queued behind the Velorum revision)

The user's words: "go back over the putriciel the purifier stage atleast in regards to how fiery he physically is during
that stage? the red lights in his chest doesnt feel like nearly enough I want his staff itself to be burning and mabye his
back too. also during his fight areas of rotting sludge should sometimes persist after his attacks that act as area denial
and then they should be burned away during the purifying stage. also in general his model feels like it could use some
color variation". (Putriciel stays "they" in player text and docs, per the 2026-10-26 ruling.)

What this asks for, and what it overrides below:

1. **Physically on fire in the cleansing interval.** The ember seams in the chest are not enough. The staff itself burns
   along its length, with the censers flaring. The back colony catches and burns: the shelves and sacs blacken and char
   under real flame. Arms and shoulders smoulder where the growths were. The body sheds fire and ash as it moves. Big,
   layered, physical fire in the Flammeus look (crimson body, pink-white heat, black soot tips; never green or yellow in
   a fire texel), present only in the interval. The ignition builds the fire up over the body and the Embering dies it
   back down. The rot phases stay fireless.
2. **Rotting sludge that persists.** Several rot attacks (not only Shelf Volley; the designer picks which, as often as
   "sometimes" suggests) leave pools of rotting sludge on the floor after they land. They are area denial: walking in them
   slows the player and adds colonisation, and they block or bend paths. They last until the purifying stage, which burns
   them away visibly: the sludge catches, boils, blackens and leaves ash or scorch, and this happens as the fire spreads,
   not as an instant disappearance. They need a cap so the floor never fills completely, and the arena restores the
   floor afterwards. They must read as thick, wet, glossy rot with body, not a flat decal.
3. **Colour variation on the model.** Putriciel's palette (ochre, olive, mould green, muted yellow, pale cream,
   blackened charcoal; no red outside the fire) currently reads as one olive mass. Give it range within that palette:
   pale cream and ivory vestment remnants, dark eschar, ochre and yellow shelf undersides, mould-green bloom, charcoal
   burns, wet highlights, varied values from part to part, through the generator at 16 px per block.

## Revision 2026-11-06: burning purifier, rot sludge, colour

Status: design, not built. Where this section differs from sections 5, 7, 9, 12 and 13 or from the visual pass 2
notes, this section wins; section 0's numbers stand wherever this section does not name a new one. No move's timeline,
damage, footprint, weight or cooldown changes. The only new gameplay is the sludge (R2). The fire on the body and the
colour are presentation.

What still binds, restated so no lane has to look it up: Putriciel is "they" in player text and docs. Fire exists only
in the cleansing interval (IGNITING, BURNING, the Embering) and the ending, always in the Flammeus look: crimson body,
pink-white heat, black soot tips, never a green or yellow texel. Outside the fire nothing is red: the palette is
ochre, olive, mould green, muted yellow, pale cream, ivory and blackened charcoal. No bone-brick trims; no copper,
brass, bronze or silver. Attacks, fire and sludge are bold, layered geometry at the Riposte, cast outpouring and
Hemorath pass-2 bar; "subtle" is for overlays only. No vanilla particle clouds, no noodle strands, no noise-contour
webs. 16 px per block. Java animation on `BossReplayClock`. Models come from the generator and are never hand-edited.

### R1. The purifier on fire

The ember seams and the ember layer stay, but they become the smallest part of the fire. In the interval the body
carries real flame geometry on about seventy anchors, a burning layer on the model itself, and a steady shedding of
fire and ash. All of it is client presentation, driven by what the client already has: the synced stage and stage
age, the Kindling, the synced move block (through `SaintMoveView`'s `BossReplayClock`s), the deed mask and the posed
model. No packet is added.

#### R1.1 What burns

Anchors are named points on generator parts, each with an outward normal, a size and a kindle order (R4, MODEL). Heights
are world blocks at the steady burn (intensity 1.0).

| Region | Parts (generator names) | Anchors | Fire at the steady burn |
|---|---|---|---|
| STAFF | the four upper shaft rings (rewrapped as `staff_wrap`, R3), the three `staff_head` prongs, the two head-bar ends, `staff_growth_0` to `_2` | 12 | The staff burns along its length. Each wrap holds two tongues 0.35 to 0.55 that wrap round the shaft; between the wraps a file of eight thin tongues 0.2 to 0.35 hugs the shaft from the grip to the head; each prong carries a tongue 0.4 to 0.7, so the head is a three-pronged torch; the head-bar ends drip short tongues 0.3; the staff's shelves burn at their rims. Below the grip the shaft burns at half height down to an ember-tipped foot |
| CENSER | `censer_left`, `censer_right` | 2 | A bed of coals glowing in each bowl; three tongues 0.5 to 0.8 roaring up through the pierced lid; a stream of flame behind the censer whenever it moves (R1.4) |
| BACK | `back_shelf_0` to `_10`, `back_mushroom`, `growth_mushroom_0` to `_3`, `back_spore_0` and `_1`, the colony's root | 19 | The tallest fire on the body. Every back shelf burns along its rim (three tongues 0.5 to 0.9); the sacs split and pour fire up out of the tear (0.6); over the colony's centre stands the back column, three broad tongues 1.2 to 1.8 tall leaning back off the shoulders, with a dark soot plume above it |
| SHOULDER | `left_shoulder_shelf_0` to `_2`, `right_shoulder_shelf_0` to `_4`, both mantles, `shoulder_mushroom`, `shoulder_spore`, both `*_elbow_growth`, `knuckle_growth` | 15 | Smouldering: low licks 0.15 to 0.35, embers glowing in the shelf cracks, heavy grey-black smoke. An arm moving faster than 0.2 blocks a tick flares its anchors to 0.6 |
| CROWN | `crown_spire_0` to `_5` tips, `head_shelf_left` and `_right`, `crown_mushroom` | 9 | A candelabrum: one tongue 0.2 to 0.35 on each spire tip; the head shelves burn at 0.3 to 0.45; the crown mushroom chars and smokes. The hood's rim and the purifier's face are lit from below by the glow |
| CHEST | `spore_0` to `_3` tears, the two upper seam ends | 6 | Fire breathing out of the split chest sacs (0.3 to 0.5), the seams lit by the ember layer as now |
| HEM | `hem_shelf_0` to `_5`, `hem_spore` | 7 | Low tongues 0.15 to 0.3 on the hem shelves and a crimson ember line along the robe's lower edge (burning layer) |

About 70 anchors, at most 220 tongues on the body at once, 60 smoke cards and 40 shed pieces (R1.9).

#### R1.2 How it builds at the Ignition

On the Ignition move's own clock (30 wind-up, 10 active, 20 recovery; the commit is tick 30). Each anchor shows smoke 4
ticks before its kindle tick, then its flame grows from 0 to 0.6 over 5 ticks.

| Ticks | Beat |
|---|---|
| 0 to 10 | Heat without flame. Thin soot seeps from the seams, the sac splits and the back colony's root (alpha 0.15 rising to 0.35). Nothing is lit |
| 10 to 16 | The first embers. A glow bed opens in each chest sac tear and at the grip wrap; tongues no taller than 0.1 |
| 16 to 26 | The climb. The chest tears catch at 16. The staff's fire runs up the shaft from the grip wrap to the head at a tenth of its length a tick, and down to the foot at half that speed. The shoulders catch at 18 and the fire spreads down the arms to the elbows by 24 and the knuckle by 26. The back catches at the colony's root at 20 and spreads shelf by shelf, in kindle order, to the outermost shelves by 28. The censers light inside at 22 and push up through the lids by 25. The crown's spires light one a tick from 24 to 29. The hem catches at 26 |
| 28 to 29 | The locked beat of the animation: every flame drawn in to 0.7 of its height, the cores white |
| 30 | The commit. Every anchor flares to 1.45 with the body burst (the ring of 6), the back column leaps to 2.2, 24 sparks fly, the heat flashes at the core, and the pyres start to relight |
| 30 to 42 | The flare settles to 1.0, dipping to 0.92 at 36 like a breath |
| 42 on | The steady burn |

#### R1.3 The steady burn and its peaks

Intensity per anchor = the region's base x (0.6 + 0.4 x Kindling fraction) x a seeded flicker (two slow sines and a
hash per anchor, within 12%) x the current peak. The fire visibly burns down with the meter: at 66% the body burns at
0.86, at 33% at 0.73. Height scales with intensity; width with its square root.

| Moment | Peak |
|---|---|
| Purging Sweep | STAFF and CENSER x1.4 from 3 ticks before the commit to 8 after; every other region x1.1 |
| Censer Cast | The thrown censer x1.5 and streaming; STAFF x1.2 |
| Cauterising Lane | The palm arm's SHOULDER anchors x1.5, licking down toward the lane |
| Pyre Heart | Wind-up ticks 0 to 20 every region x0.8, 20 to 28 x0.6 (the fire sucked into the core), 28 x1.6 on everything with the back column at 2.6 joining the core's column, back to 1.0 by 44 |
| Pyre Lines | CROWN and the palm arm x1.3 while the palm is raised |
| The Full Purge | Each ring a x1.35 pulse that decays over 6 ticks |
| Offering | Every region x0.55, the palm at full (the fire drawn down to the palm, as section 6 says). An accepted Offering runs a x1.5 pulse from the palm up the arm and across the back over 6 ticks, then the body returns to 0.55; it returns to the steady burn over 8 ticks once the Offering closes |
| A hit taken | x1.2 for 4 ticks and a tear-off burst (R1.5) at the anchor nearest the wound |

#### R1.4 Following motion

- Each anchor's world velocity comes from its posed position on consecutive frames, divided by the render-age step
  (`PutricielAnimState`, keyed by age like the limb trails, so it holds at any frame rate).
- A tongue's tip direction is up, plus a quarter of the anchor's normal, minus 1.6 x the velocity, through a spring
  (`follow`, frequency 0.35, damping 0.5), clamped to 70 degrees from up. Its length grows by up to 80% with speed
  (full at 0.35 blocks a tick). A swung staff drags its flames back along the swing and they swish past when it stops.
- Above 0.25 blocks a tick an anchor sheds tear-offs: detached tongues that keep its velocity, rise and shrink over 5
  ticks.
- The staff head and both censers leave a stream along their last 6 ticks of path (`staffHeadAt`, `censerAt`): a train
  of overlapping tongue cards whose size tapers from 0.35 to 0.05 with age. It is fire with body, built of tongues,
  never a ribbon line.
- The shamble and stride footfalls leave a scorched print at each foot for 40 ticks (client-only `SCORCH` on the
  ground, 0.5 by 0.35), with two low tongues for its first 8 ticks. No block changes.

#### R1.5 Shedding fire and ash

- Ash flakes: charcoal and ash-grey cards 0.04 to 0.08 across, drifting down and out from the BACK and SHOULDER anchors,
  6 a second, a burst of 8 on a motion spike or a hit. Drawn on the spore material's new `ASH` shape (grey only).
- Burning fragments: chunks of shelf (`PutricielGrowthGeometry` clods with the char channel at 0.8 and glowing cracks)
  break off the back on the commits of the Purging Sweep, the Censer Cast and the Pyre Heart, three to five each, fall,
  bounce once and smoulder on the floor for 20 ticks.
- Embers: `HemoParticleData.emberSpark` as accents, 4 to 8 a second off the back and staff head under All particles,
  fewer under Decreased, none under Minimal. The geometry carries the fire with particles off.
- Soot: `ThermalGeometry.smoke` plumes over the back column and the staff head, and a thin pall over the shoulders.

#### R1.6 Dying back at the Embering

On the Embering clock (60 ticks):

| Ticks | Beat |
|---|---|
| 0 to 10 | The fire gutters. Tongue height falls to nothing (ease-in), the tongues' heat drops so the soot tips take them over, and the back column collapses into two smoke columns |
| 10 to 30 | Embers only: crimson glow beds at the anchors, winking out in reverse kindle order (the hem, the crown and the arms first, then the back from its outer shelves in, the staff head last at 30), each with a puff of smoke as it goes |
| 30 to 45 | Smoke only, thinning; twelve ash flakes fall |
| 45 on | Nothing. The growths regrow over the black (R1.8) |

The ember layer's fade in the Embering stretches from 15 to 24 ticks so the seams go out with the embers
(`PutricielVisualRules.look`). Under the Repose deed there is no Embering, so the fire gutters inside the interval
instead: while the Kindling is under 6% (the last 18 ticks of the burn-down) the intensity is multiplied by the
Kindling fraction over 0.06, and the body is out when the rot returns. The rot phases carry no fire from their first
tick, with or without the deed. Without Repose the last 6% shows the fire faltering to 0.5, not out.

#### R1.7 Layers and materials

Back to front at each anchor:

1. A smoke body over it (`ThermalGeometry.smoke`, soot black to grey).
2. The outer flame: broad bent tongues on `ThermalGeometry` spines, `PutricielFlameGeometry.tongue` (crimson body).
3. The core: a hotter, narrower tongue licking up inside on its own sway and lean (pink-white).
4. The bed: a glow card at the anchor on `STEADY_FLAME`, the coals the flames rise from.
5. Accents: sparks with their streaks, tear-offs, ash flakes, fragments, the ember sparks.

No new fire shader. The tongues stay on the Flammeus `FLAME` material and the beds on `STEADY_FLAME`
(`ThermalRenderTypes`), so the shader owns the palette and no fire texel can be green or yellow. The body fire draws
in `PutricielTelegraphPass`'s fire layer, after the entity, in the same batch as the moves' fire.

On the model itself, a new **burning layer** (`PutricielBurnLayer`, a static `RenderType.eyes` constant on the new
atlas `putriciel_burning.png`, RGB scaled by alpha as the spore glow and ember layers now are) lights what is under
the flames: the shelf and cap rims crimson with pink-white flecks, the char atlas's alligator cracks glowing pink-white
on the caps, the staff wraps in crimson streaks with hot cores, the iron heated crimson for a unit either side of each
wrap, the censer lid bars' inner faces, the top 1.5 units of each crown spire, the hem shelf rims, the robe's lower
edge and the sac tears. Fire palette only (crimson (214, 30, 46), pink-white (255, 168, 190), soot (22, 10, 12)). The
layer draws the model once per region (7 regions; the parts outside a region are `skipDraw`n), each at its region's
intensity, so the model's own burn climbs the staff and spreads across the back with the flames. It returns before
drawing in a rot phase and draws nothing after the Embering's tick 30.

`PutricielRenderer`'s block-light floor rises from 8 to 13 while the fire burns (and with the Embering's embers to
tick 30), so the fire lights the body it is on.

#### R1.8 The back colony: charring and regrowth

- At the Ignition the back's shelves blacken in kindle order, each as the fire reaches it, not all at once: the
  per-part growth scale (1.0 to 0.35) and the char layer's alpha are staggered by kindle order, 1 tick a step, inside
  the existing 10 to 30 window.
- Through the burn the two outermost back shelves (`back_shelf_9`, `_10`) and the back mushroom are eaten further,
  from 0.35 to 0.2 by the end of the burn-down, shedding fragments as they go.
- In the Embering the colony regrows shelf by shelf from the root outward, 3 ticks a shelf, each with the existing 1.1
  overshoot (`PutricielVisualRules.regrow`), and the char clears per shelf as it regrows. The arms and the crown
  regrow from their tips inward on the same 3-tick step. By the Embering's tick 60 everything stands at 1.0.

#### R1.9 Budgets and guards

- At most 220 tongues, 60 smoke cards, 40 shed pieces and 7 burning-layer passes a frame for the body; the stream
  trains count inside the tongue budget.
- Intensity is 0 in the ROT stage for every stage time, Kindling and deed mask (`PutricielBodyFireRules`, tested).
- The body fire and the burning layer draw only when the stage is IGNITING or BURNING, or EMBERING under tick 45 (the
  burning layer under tick 30), or in the ending's fire beats (section 11: ticks 20 to 70). The guardrail pins it.

### R2. Rot sludge

The rot pools where it lands, and it stays until the fire comes for it. Thick, wet, glossy pools of rot that slow
and colonise whoever wades through them, cut the floor into lanes, and burn away visibly when the purifier wakes.
What the fire burns leaves ash, and that ash is the next Bloom of Rot's safe ground: the rot a player let pool becomes
the ground they stand on later.

#### R2.1 Which moves leave it

| Move | What it leaves | How often | Shape |
|---|---|---|---|
| `SHELF_VOLLEY` | The landings at index 0, 3 and 6 (the first is always the quarry's locked disc) leave a pool instead of a rot colony patch; the others keep their colony patch | 2 of 5 landings, 3 of 7 or 9 | Disc radius 1.5 at the landing |
| `CENSER_DROP` (Rot II+) | The fouled crater fills | Always (it only runs as the Thurible string's end) | Disc radius 2.0 at the locked aim |
| `OFFERING_GRASP` | The trench along the lane overflows into a runnel along its outer 4 blocks | When the move's synced serial is even: every other Grasp | Lane 4 long, half-width 0.55, from 2 to 6 blocks out along the locked lane |
| `ROOT_SNARE` | The rot meant for the quarry pools at the cross | Only when the Snare strikes no one (a read the player can learn) | Disc radius 1.75 at the cross's centre |
| `BLOOM_OF_ROT` (Rot II+) | The field drains into two pools at its last pulse | Once per rot phase (the Bloom's own rule) | Two discs radius 1.75 on field cells at least 3 from any safe cell and 2 from any challenger, picked by the move's seed |

Censer Swing, Backswing, Rot Breath, Sac Swell, Censer Orbit and Mould Communion leave none: they come too often or are
clouds. No fire move and no Embering leaves sludge. In Rot I a player who kites draws about four pools before the first
interval; one who fights close draws one or two.

#### R2.2 The pool: size, life, cap, placement

- **Kind.** A new `PutricielHazard.ROT_SLUDGE` (id 5) on the kit's `BossGroundHazardEntity`, spawned through
  `spawnHazard` (so `arena.own` tracks it), life 2400 ticks (a safety only; the interval always comes first), pulse 5.
- **Placement.**
  - Inside the boss floor only (x 9 to 23, z 6 to 20 of the chamber), clipped to it.
  - Never within 1.5 + its radius of a pyre, so the pyre circles stay clean.
  - Never on this fight's packed ash. A disc whose centre cell is ash is not laid. Otherwise its radius is clipped to
    the distance from its centre to the nearest ash cell's centre, less 0.5. Under 1.0 it is not laid. A runnel stops
    short of its first ash cell.
  - A new pool may overlap an older one by up to 40% of its own radius. Closer than that it is not laid: the landing
    splashes onto the older pool's surface instead (a ripple burst, drawn from the older pool's age).
- **Cap.** At most 6 live pools and 42 square blocks of sludge (about 19% of the boss floor) with one challenger, +1
  pool and +6 square blocks per extra challenger (at most 9 pools, 60 square blocks). Laying past the cap makes the
  oldest pool **settle**: it sinks into the moss over 30 ticks (`expire(30)` in a rot stage) and touches nothing while
  it does. Settling is not fire.
- **Life.** A pool lasts until the next interval's fire burns it (R2.5), the cap settles it, the ending burns it, or
  the arena restores. On reaching its safety life it settles.

#### R2.3 Standing in it

- Movement speed x0.65: a transient modifier `hemomancy:putriciel_sludge` (`AttributeModifiers.sync`,
  `ADD_MULTIPLIED_TOTAL` -0.35), refreshed on each 5-tick pulse and removed once a player has been out of every pool for
  6 ticks. It stacks with Rooted. It is removed on leaving the chamber, death, logout, the trial's end, reset or unload,
  with colonisation's other modifiers.
- Colonisation +0.5 a pulse (+1 per 10 ticks), so wading doubles the rot presence.
- No damage. The denial is the slow and the growth.
- Putriciel is not slowed and prefers to stand in or beside a pool, as it does beside a colony patch
  (`PutricielLocomotion`).
- Fruiting's spread and Overgrown apply as usual, so a player Overgrown in a pool is rooted where they stand.
- A player's own Flammeus fire does nothing to a pool in the rot: only the interval burns sludge.

#### R2.4 How it reads

The pool is a real mesh with body, standing on the floor. It is not a decal.

- **Mesh** (`PutricielSludgeGeometry`, drawn in the solid pass on `putriciel_growth`'s new `SLUDGE` shape, depth-writing
  and facet-lit). A polar heightfield, 12 rings by 28 segments. 0.18 blocks thick at the centre (3 px), falling to a
  raised meniscus lip 0.06 high just inside the rim, then a rounded edge to the floor. The rim is lobed, not a circle:
  five to seven seeded lobes vary it between 0.85 and 1.1 of the radius. A runnel is the same section swept along its
  lane, pinched at both ends.
- **Surface** (fragment shader; patterns on the floor's sixteen texels a block):
  - near-black olive body (26, 28, 14) at its deepest to (48, 50, 26) at the shallows;
  - a broad, sharp gloss that moves with the view: a Blinn-Phong highlight from the shader's key light at a high
    exponent, whitish olive (196, 200, 168), plus a cold sheen at grazing angles;
  - slow drifting cream scum islands (220, 208, 168) where the vertex blue (bloom) is high;
  - an ochre meniscus band (140, 116, 56) one or two texels wide at the lip;
  - a faint slow undulation of the surface (0.008 blocks).
- **On the floor.** A dark wet stain on `putriciel_ground`'s MAT reaches 0.35 beyond the rim, with a mould-green
  fringe (96, 128, 64) where it meets the moss.
- **Bubbles.** In the rot, one or two at a time: a glossy dome rises (0.06 to 0.14), swells and bursts into a ring
  ripple and a small pale spore puff (`putriciel_spore` PUFF) every 25 to 40 ticks a pool.
- **Forming.** It spreads from the splash out to its rim over 8 ticks (ease-out), its thickness overshooting to 1.15
  and settling. Ooze gouts and clods from the move's own landing geometry splash into it.
- **Settling** (cap or safety life). Over 30 ticks the gloss dies (vertex green to 0), it dulls to a matte olive
  crust, thins to 0.2 of its thickness and sinks; the stain fades with it.
- **Telegraphs stay readable.** A telegraph drawn over a pool rides its surface: `PutricielGroundGeometry` lifts each
  vertex to the sludge's surface height from the frame's live pools (`PutricielSludgeField.height`), so no ground read
  hides under sludge.
- **Every colour is in the rot palette.** No red anywhere until it burns.

#### R2.5 The fire takes it

Nothing vanishes at once. At the Ignition's commit the server computes when the fire reaches each live pool and colony
patch, and from where (`PutricielSludgeRules.catchSchedule`). It saves the schedule with the entity and recomputes it
on load from the saved commit tick if it is missing.

| Source | When the pool catches |
|---|---|
| The Ignition ring | Pools whose nearest edge lies within 6 of the body catch as the ring passes: the commit + 10 x distance / 6 ticks, at their nearest edge |
| A relit pyre | Each pyre relights 4 ticks apart from the commit (dais first). A **fire runner** leaves its circle's edge 4 ticks after it relights and crawls across the floor to the nearest pool within 8 blocks at 0.5 blocks a tick |
| A burning pool | A runner leaves it 6 ticks after it caught, for any pool within 3 blocks of its rim |
| Fire moves and fire hazards | During any fire move's active span, and on each pulse of a purging fire or fire wall, a pool whose rim or centre points the footprint hits catches that tick at the first point hit. The Cauterising Lane running through a pool lights it under the front |
| Putriciel's own body | In IGNITING after the commit, or in BURNING, a pool within its radius + 0.6 of the body catches at its nearest edge (the burning purifier walks into it) |
| A burning player | A challenger carrying crimson fire who stands in a pool during the interval lights it under their feet |
| Fallback | Anything still unlit 140 ticks after the commit catches at its edge nearest the body |

The earliest source wins. Fire moves and the body can only bring a catch forward. The runners are a new harmless
hazard kind `FIRE_RUNNER` (id 7): a Lane from the source to the catch point, life = its travel + 30, no pulse effect.
The client draws its front: a scorched strip 0.6 wide with low tongues (0.3 to 0.5) at its head. It is a burning
fuse with body, not a line.

When a pool catches, the server calls `expire(BURN)` on it (BURN = 50 ticks) and spawns a `SLUDGE_FIRE` hazard
(id 6, fire) on the same footprint. Its frame's `aim` is the catch point and its `target` the runner's source, so the
client draws the burn spreading from the right edge. Its life is 110 ticks: 50 of burning, then 60 of cooling that
touch nothing. While burning it pulses every 10 ticks for 1 cleansing and -2 colonisation (a small cleanse that costs).
The slow ends the tick the pool catches.

| Burn tick | Beat (drawn from the catch point outward) |
|---|---|
| 0 to 6 | **Catch.** A burning front runs across the surface from the catch point (radius / 6 a tick), low tongues 0.3 to 0.5 rising along it, soot lifting behind |
| 6 to 30 | **Boil.** Tongues 0.6 to 1.1 tall over the whole pool, about one per 0.35 square blocks, leaning with the draught toward the body. The surface heaves and boils: glossy bubbles swell and burst, each with a heat flash, three sparks and a soot puff, up to two bursts a tick. The sludge darkens from the catch point outward (char channel 0 to 0.7), loses its gloss and thins from 0.18 to 0.08; its lobes curl up at the rim |
| 30 to 44 | **Char.** A black crust (char 0.7 to 1) cracks into curled plates whose seams glow crimson to pink-white and then cool. The tongues die back to embers (1.1 to 0.2), and two soot columns rise |
| 44 | **Ash lands.** The server turns the cells under the pool (disc radius - 0.25, or the runnel's lane) to `hemomancy:packed_ash` through `SaintArena.edit` under a new budget `putriciel_sludge_ash` (8 cells a pool, 24 per 600-tick window, 64 recorded), triggered when the `SLUDGE_FIRE` hazard's saved age reaches 44, so a reload keeps it |
| 44 to 50 | **Crumble.** The crust breaks to ash-grey flakes (spore ASH cards, a few lifting) and sinks onto the new ash; the tongues go out |
| 50 to 110 | **Cooling.** The `SCORCH` ground shape on the ash: charcoal plates whose seams glow faintly and go dark, fading by 110 |

The fire palette is used from the catch onward and never before. The sludge's own body, while it burns, uses the growth
shader's char semantics (black, then glowing cracks, then cold ash), whose fire constants are already pinned to
crimson and pink-white.

Rot colony patches (`ROT_COLONY`) join the same schedule instead of expiring at the Ignition. Their burn is 30 ticks:
the growths char black with embers in their cracks and crumble, as visual pass 2 already draws. They leave no ash.
Breath clouds (`ROT_CLOUD`) still flash off at the commit over 10 ticks. `burnRotHazards()` becomes "schedule the rot
hazards" plus that flash.

At the ending's tick 0 every pool and patch catches at once from its edge nearest the body, with the burn scaled to
10 ticks (section 11: every hazard burns out over 10 ticks). No ash is laid at the ending, because the arena is about to
restore.

#### R2.6 With the Bloom, the ash and the colonies

- **Ash.** A pool is never laid on this fight's ash, and the ash a pool leaves is ordinary fight ash. It is safe
  ground for the next Bloom of Rot and reclaimed by that Bloom when empty (section 9).
- **The Bloom's field.** It passes over live pools: they are part of the rot, and its pulses do not stack on them. Each
  pulse heaves every pool's surface (a swell and a ring of bubbles). Its last pulse drains two new pools (R2.1), under
  the cap.
- **Colony patches.** A pool laid over a colony patch (centres closer than the patch's radius plus half the pool's)
  absorbs it: the patch expires over 12 ticks, and its rosettes and sacs sink into the sludge instead of withering. The
  client draws the sink when a fading patch overlaps a pool younger than 12 ticks. Shelf Volley's sludge landings
  replace their colony patch outright.
- **Openings.** An Offering may open with a pool between the player and the palm. The player wades (slow, +colonisation)
  or waits for the fire. Pools near the kneeling body are the first to burn, because the Ignition ring and the body
  light them.

#### R2.7 Arena, save and restore

Pools, burning pools and runners are owned entities (`arena.own`). `endTrial`, `remove`, unload and a load after the
trial ended discard them all, and the arena restores every ash cell, the sludge ash's budget included. The kit's hazard
entity already saves its kind, shape, frame, life, fade and age. The catch schedule saves with Putriciel. The pool count
and area for the cap are read from the owned pools, so nothing else needs saving. A pool's seed is the hash of its
frame's origin, so both sides draw the same lobes and bubbles.

#### R2.8 Sounds and lines

| Beat | Event | Volume / pitch |
|---|---|---|
| A pool forms | vanilla `HONEY_BLOCK_PLACE` + `MUD_PLACE` | 1.0 / 0.6 ; 0.8 / 0.7 |
| A player wading (every 8 ticks, to that player) | `HONEY_BLOCK_STEP` | 0.6 / 0.6 |
| A pool settles | `MUD_BREAK` | 0.6 / 0.6 |
| A runner crawls | `FIRE_AMBIENT` at its head, once | 0.8 / 1.1 |
| A pool catches | `FIRECHARGE_USE` | 0.6 / 1.2 |
| Boiling (per burst, at most one a pool per 4 ticks) | `LAVA_POP` | 0.7 / 0.7 |
| Ash lands | `FIRE_EXTINGUISH` | 0.5 / 0.8 |
| The body fire (every 20 ticks in the fire) | `FIRE_AMBIENT` | 1.0 / 0.7 |

Two teaching lines, through `SaintLines.teach`, each once a fight:
- `message.hemomancy.putriciel.sludge_taught`, at the first pool: "The rot pools where it falls. Only the fire will take
  it."
- `message.hemomancy.putriciel.sludge_burns`, at the first catch: "The fire runs to the pooled rot."

Putriciel stays silent.

### R3. Colour

The body reads as one olive mass because almost every material sits in the same mid olive value. The pass gives it
three value bands and five hue families within the palette, with the brass read of round 2 held off by rule.

**Bounds.** No red texel (the `PutricielPaletteTest` rule: hue within -20 to +15 degrees, saturation at least 0.4,
red at least 70) in `putriciel.png`, `putriciel_glow.png` or `putriciel_charred.png`. Every warm colour in the base
sits at hue 30 degrees or above. Ochre and muted yellow are kept matte and noise-broken, never smooth gradients, never on
iron beyond single-texel flecks, and they cover at most 15% of the used base texels (the brass guard). Iron stays dark
and its edges go olive mould, never pale grey, so nothing reads as silver.

#### R3.1 Part by part

| Part (material) | Base | Accents | Value |
|---|---|---|---|
| Robe panels (`blood_cloth`) | Rot-dyed olive brown (70, 64, 40); every other non-ivory panel deep mould green (52, 66, 40) | Ochre tide marks (128, 108, 56) at noise-wobbled heights where the rot wicked up; the hem darkened to charcoal olive (34, 34, 22) over its lowest 3 texels; wet dark patches with single-texel highlights (150, 152, 120) | Mid, dark toward the floor |
| Vestment remnants (`ivory_cloth`: collars, mantles, hood rim, robe panels 0, 4, 8, 12; new `stole`) | Ivory (236, 228, 204) to pale cream (222, 210, 176) | Ochre stain at the edges (166, 140, 80); mould-green bloom (110, 140, 76) creeping in from the seams; on the stole, charcoal-stitched bands (46, 44, 32) every 4 texels | Light: the brightest cloth on the body |
| Flesh (hips, legs, chest, hood sides) | Pallid grey cream (150, 144, 118) mottled with olive (88, 90, 56) | Mould-green veins (96, 124, 66); dark eschar patches (42, 36, 30) with lighter scab rims (70, 60, 44); darker inside the hood (cavity) | Mid to light |
| Eschar seams (`seam`) | Dark eschar (40, 34, 28) | Scab texels (70, 60, 44) | Dark |
| Spore sacs (`spore`) | Graded: pale cream crown (226, 218, 184), muted yellow middle (196, 176, 98), ochre base (150, 124, 58) | Olive veins (84, 92, 48); the thin tops glow cream in the glow atlas | Light at the top, mid at the base |
| Bracket shelf tops (`shelf_cap`) | Concentric zones: charcoal olive heart (58, 58, 34), olive (96, 96, 56), ochre (150, 126, 62), muted yellow (192, 172, 96) | A pale cream rim (228, 216, 176) and a lighter top edge texel | Dark heart, mid zones, light rim |
| Shelf undersides (`gills`) | Ochre and muted yellow pores (176, 150, 78) | Mould-green bloom at the root (110, 150, 76), lit in the glow atlas (118, 206, 92) | Mid |
| Mushrooms (`pale_cap`) | Ivory caps (232, 222, 192) | Ochre freckles (160, 134, 70); cream stems and gill skirt | Light |
| Ichor and drips (`ichor`) | Glossy near-black olive (28, 30, 16) | One wet highlight texel (180, 186, 150) at the top of each drip column | Dark |
| Roots and feet (`root`) | Dark bark (52, 46, 30) | Ochre fibre (120, 100, 52) on the upper face | Dark |
| Iron (staff, crown, censers) | Blackened iron (34, 33, 30) to (96, 90, 72); the staff head sooted to (24, 22, 20) | Olive mould bloom on the edges (98, 110, 52); single ochre flecks | Dark |
| Staff wraps (new `staff_wrap`) | Rot-soaked rag, dark ochre (110, 92, 48) to charcoal olive (46, 44, 28) in loose diagonal turns | Ichor-dark soaked texels; these are what burns in R1 | Mid to dark |
| Moss tufts (new `moss`) | Mould green (126, 164, 84) | Darker underside (76, 100, 52) | Mid: the one saturated green accent |
| Bone (fingers) | Ivory bone (210, 198, 164) | Dark joint texels (126, 108, 68) | Light |
| Purifier's face (`ash_face`) | Unchanged pale ash cream (206, 196, 172) | Charcoal hollows | Light |
| Void | Unchanged (20, 24, 13) | | Darkest |

Each shelf takes one of three schemes from its index's hash (ochre-led, olive-led, pale-led), so no two neighbours
match. The sacs take their seed's shift of the grading.

#### R3.2 Value plan

- Three bands by luminance: dark under 0.22 (eschar, ichor, iron, void, the hem, roots), mid 0.22 to 0.6 (robe, olive
  caps, gills, flesh mottling, wraps, moss), light over 0.6 (ivory vestments, sac crowns, cap rims, mushrooms, bone,
  the face).
- Targets over the used base texels: dark 25 to 35%, mid 40 to 50%, light 20 to 30%; mould green 5 to 10%.
- The body grades from light at the top (crown mushroom, hood rim, mantles, collars, sac crowns) to dark at the floor
  (tide-marked robe, charcoal hem, roots), as rot wicks up from the ground. The silhouette from behind is broken by the
  light cap rims against mid shelves.

#### R3.3 In the fire

The char atlas no longer scorches every cloth texel to one value. The hem and lower robe scorch fully to (52, 48, 40).
The collars, mantles and stole scorch only at their edges and stay pale ash cream (196, 188, 168) inside, so in the
fire the vestment remnants still show pale under the flames: the man under the rot. Caps char to charcoal with ash
rims as now; the burning layer (R1.7) lights their cracks.

#### R3.4 Generator changes (`tools/model_export/putriciel.py`)

- New materials `staff_wrap` (the four upper shaft rings, which stop being iron; the foot ring stays iron), `moss`
  (tufts of 0.5-unit cubes at six back shelf roots and two on each shoulder) and `stole`.
- New parts `stole_left` and `stole_right`: ivory strips 1.5 units wide down the chest front from the collars to the
  belt, carried on by the stole pattern on the front robe panels 6 to 8.
- Per-cube `variant` for shelves and sacs (the three schemes; the sac grading shift).
- A world-height tide function for cloth (darker and tide-marked toward `FEET_Y`).
- Rim and top-edge highlight texels on caps; the single-texel wet highlights on ichor and robe wet patches.
- The new `putriciel_burning.png` atlas (`burning(cube, t)`, fire palette only) and the char atlas's cloth gradient.
- The fire anchor table (R1.1), the region of every part and the kindle order of every anchor and growth part, written
  into the model's generated fields (R4, MODEL).
- Assertions in the generator: 16 px per block (kept); no red texel in the base, glow and char atlases; only the three
  fire colours in the embers and burning atlases; the value-band targets; ochre and yellow at most 15%; mould green 5 to
  10%; every anchor on a part that exists.
- `--preview` adds `build/putriciel_palette.png`: per-material swatches, the luminance histogram with the band
  targets, and the burning atlas over a charred pose.
- Re-run regenerates `PutricielModel.java`, the five atlases and the `.bbmodel`. The cube count rises by about 24.

### R4. Build list

Three lanes that can run side by side once the contracts below exist. Shared add-only files are marked; re-read each
right before editing, add lines, never reorder or reformat. Anything deleted or replaced goes to `removed_assets/`
first, keeping its path, with a `MANIFEST.txt` line (this pass copies the four current Putriciel atlases there before
the regenerate, for before and after). Only a stage told it may run Gradle runs it.

**Contracts (written first, by the lane named):**

1. MODEL: `client/model/entity/boss/putriciel/PutricielFireAnchors` holds the `Region` enum (STAFF, CENSER, BACK,
   SHOULDER, CROWN, CHEST, HEM) and `Anchor(Vec3 position, Vec3 normal, Vec3 velocity, Region region, float size,
   int order)`. `PutricielAnimState.fireAnchors()` returns this frame's anchors, and `kindleOrder(part)` returns each
   growth part's order. Until it lands, EFFECTS draws from the existing points (core, palm, sacs, censers, staff head
   and foot).
2. SERVER: `PutricielHazard.ROT_SLUDGE` (5), `SLUDGE_FIRE` (6), `FIRE_RUNNER` (7). Frame conventions: a sludge's
   origin is its centre; `SLUDGE_FIRE`'s `aim` is the catch point and its `target` the source; `FIRE_RUNNER` is a Lane
   from source to catch point. `PutricielSludgeRules` publishes the beats (`CATCH` 6, `BOIL_END` 30, `ASH_AT` 44,
   `BURN` 50, `COOL` 60, `SETTLE` 30, `FORM` 8, `RUNNER_SPEED` 0.5, `COLONY_BURN` 30, `ENDING_BURN` 10).
3. EFFECTS: `common/entity/boss/saint/putriciel/PutricielBodyFireRules` (pure, no client class) gives intensity by
   region, anchor and time: kindle, flare, the Kindling's scaling, move peaks, the Offering draw-down, the Embering
   die-back and the Repose gutter. MODEL reads it for the burning layer's region alpha and the growth stagger.

**SERVER** (`common/entity/boss/saint/putriciel/`, hazards, rules, lang, tests)

1. `PutricielSludgeRules` (new, pure): which landings and moves lay, the shapes and radii, the Grasp serial rule, the
   Snare-miss rule, the Bloom's drain spots; placement (boss floor clip, pyre exclusion, ash clip, overlap rule); the
   cap and the oldest-first settle; `catchSchedule` (ring, pyres with their relight ticks, runners, chains, fallback);
   `touches` for fire footprints and the body; ash cells per pool; the beat constants.
2. `PutricielHazard`: the three new kinds (`ROT_SLUDGE` life 2400, pulse 5, 0 damage, +0.5 colonisation;
   `SLUDGE_FIRE` life 110, pulse 10, 1 cleansing, -2 colonisation, harmless after age 50; `FIRE_RUNNER` harmless).
3. `PutricielMoveEffects`: lay pools on the Volley's landings 0, 3 and 6 (instead of their colony), the Censer Drop's
   impact, the even Grasp's runnel, the missed Snare, and the Bloom's last pulse; the colony absorption.
4. `PutricielEntity`: `tickSludge()` in `tickSaint` (cap and settle, the slow modifier per player with its 6-tick
   release, the catch schedule and live touches in the fire stages, `SLUDGE_FIRE` and runner spawns, ash at age 44 under
   the `putriciel_sludge_ash` budget); `burnRotHazards()` becomes the schedule plus the cloud flash; the ending's
   fast burn; the schedule's save and load; `hazardTick` for the new kinds; clearing the sludge modifier wherever
   colonisation's modifiers clear.
5. `PutricielLocomotion`: prefer pools as it prefers colony patches.
6. `PutricielArenaRules`: the second budget's numbers; pools excluded from Bloom safe cells (ash from sludge counts as
   ash).
7. Sounds per R2.8 (registered events only).
8. Lang (shared, add-only `en_us.json`): `message.hemomancy.putriciel.sludge_taught`, `message.hemomancy.putriciel.sludge_burns`.
9. Tests:
   - JVM `PutricielSludgeRulesTest` (new): lay rules per move, placement and clipping, overlap, cap and settle order,
     every catch source and the earliest-wins rule, runner timing, the fallback by 140, ash cells and caps, the ending
     burn.
   - `PutricielMovesTest` (extended): only the five rot moves lay, never in a fire stage.
   - `PutricielArenaRulesTest` (extended): the second budget, restores.
   - GameTests in `PutricielFightGameTests` (add-only): `sludge_slows_and_colonises_and_clears_on_leave`,
     `sludge_waits_for_the_fire_then_burns_to_ash`, `sludge_cap_settles_the_oldest`,
     `sludge_never_lies_on_ash_or_pyre_circles`, `sludge_ash_restores_on_end_remove_and_load_after_end`;
     `rot_phases_never_burn` extended (no `SLUDGE_FIRE` or runner and no catch in a rot stage).
   - Review client (`PutricielFightClientReview`): new stops `sludge_rot` (pools after the Volley, Drop, Grasp and
     Snare, SIDE and LOW), `sludge_burn` (the Ignition spreading over pools, HIGH, every third tick to burn tick 110),
     `body_fire_ignition` (ticks 0 to 56 every 4, a close three-quarters camera and BACK), `body_fire_motion` (the
     Purging Sweep and the stride, SIDE), `embering_dieback` (0 to 60), `palette_portrait` (the rot, front, three-quarters
     and back). Any new Gradle property is an add-only line in `gradle/review-clients.gradle`.

**EFFECTS** (`client/render/world/Putriciel*`, shaders, fire and sludge geometry, the effects and visual-rules classes)

1. `PutricielBodyFireRules` (contract 3) and its JVM test `PutricielBodyFireRulesTest`: 0 in ROT for every input; the
   kindle times and the climb; the flare and settle; the Kindling's scaling; each peak; the Offering draw-down and the
   acceptance pulse; the Embering die-back to 0 by 45; the Repose gutter reaching 0 at Kindling 0; lean and stretch
   bounded.
2. `PutricielBodyFireGeometry` (new): the per-anchor clusters in R1.7's layer order, the staff sleeve between the wraps,
   the prong torch, the censer beds, lids and streams, the back column and plume, the crown candelabrum, the smoulder,
   tear-offs, ash flakes, burning fragments, ember sparks and footfall prints.
3. `PutricielSludgeGeometry` (new): the heightfield mesh, lobes, runnels, bubbles, forming, the rot idle, settling,
   the burn beats from the catch point, the colony sink and the runner front. `PutricielSludgeField` (new): the frame's
   pool heights for conforming.
4. `PutricielTelegraphPass`: the body fire in the fire layer; the sludge in the solid layer and its stain in the ground
   layer; the three new hazard kinds; the colony sink. `PutricielGroundGeometry`: conform to `PutricielSludgeField`.
   `PutricielFlameGeometry`: an anchored tongue taking a lean, a stretch and a heat, and the tear-off (additive).
   `PutricielBloomRules`: sludge looks (form, idle, settle, burn) and the runner front. `PutricielTelegraphStyle`:
   claims the new kinds and paints nothing.
5. Shaders: `putriciel_growth.fsh` gains `SLUDGE` (body grading, Blinn-Phong gloss and grazing sheen, scum, meniscus,
   undulation, and the char crust with cracks on the existing fire constants); `putriciel_spore.fsh` gains `ASH` (grey
   only, no hue); `putriciel_ground.fsh` MAT gains the wet stain ring. No new shader, so no `ShaderInit` change. A
   client launch must prove they compile.
6. `PutricielVisualRules`: the Embering ember fade from 15 to 24 ticks. `PutricielEffects`: no new burst; the pool's
   forming is drawn from the hazard's age.
7. Tests: `PutricielEffectGeometryTest` (extended: the body fire and sludge budgets, the mesh stands between the floor
   and 0.25 and is never under 0.8 of its radius, nothing drawn for a ROT body, burn beats by age);
   `PutricielBloomRulesTest` (extended); `PutricielPresentationGuardrailTest` (extended: the body fire and burning
   layer draw only in the stages of R1.9, no `ParticleTypes` in any new file, no rot constant in the changed shaders
   leans red and no fire constant leans green or yellow, render types still built once). Regenerate
   `docs/architecture/evidence/source-text-impact-map.csv` if the guardrail reads new files.

**MODEL** (`tools/model_export/putriciel.py` and `putriciel_anim.java`, `client/model/entity/boss/putriciel/*`, renderer
layers and pose code, the texture and colour pass)

1. `putriciel.py`: everything in R3.4, plus the fire anchor table (part, local point, normal, size, region, kindle
   order) and each part's region, emitted into the generated fields. Copy the current atlases to `removed_assets/`, then
   run `python tools/model_export/putriciel.py --preview`. Never hand-edit the outputs.
2. `putriciel_anim.java`: `firePoints` walks each anchor's part chain like `core()` and `palm()`; the per-part growth
   scale and char stagger by kindle order at the Ignition; the outer back shelves eaten to 0.2 through the burn; the
   Embering regrowth shelf by shelf from the root, and the arms and crown from their tips, 3 ticks a step.
3. `PutricielFireAnchors` (contract 1); `PutricielAnimState`: stores the anchors per frame with their age-keyed
   velocities (like the limb trails), exposes `fireAnchors()` and `kindleOrder`.
4. `PutricielRenderer`: records the anchors after the body draws (the rise's and the ending's sink included); the
   block-light floor of 13 in the fire; adds the burning layer.
5. `client/render/layer/mob/PutricielBurnLayer` (new): the static eyes type on `putriciel_burning.png`, RGB scaled by
   alpha, one pass per region with the other regions `skipDraw`n, alpha from `PutricielBodyFireRules`; it returns in a
   rot phase. `PutricielCharLayer`: per-region alpha for the staggered char and regrowth.
6. Tests: `PutricielPaletteTest` (extended: the value bands, the hue families, ochre and yellow at most 15%, mould
   green 5 to 10%, no red in the three rot atlases); `PutricielFireResourceTest` (extended to `putriciel_burning.png`);
   `PutricielModelTest` (extended: about 70 anchors, each on its region's part with a unit normal; the staff anchors run
   along the shaft from grip to head; the stole and wraps exist; 16 px per block; the stagger and regrowth reach 1.0
   by the Embering's tick 60).

**After the lanes.** One verify stage compiles, runs the JVM suite and `runPutricielFightGameTestServer`, and launches
`runBossPresentationReviewClient -PputricielFightReview=true` for the shader compile and the new stops. Then the docs
stage updates `SAINT_MAUSOLEUMS.md`, `BOSSES.md`, `HEMOMANCY_REFERENCE.md` (the sludge is a mechanic) and the Saints
wiki page. This revision touches no doc but this spec.

**Calls made here** (no ruling covers them): the five sludge moves and their rates; the cap; the 0.65 slow and +1 per
10 ticks with no damage; ash from burned sludge being Bloom-safe; fire runners from pyres and pools; burning players
lighting pools; the burning pool's small cleanse; the 50-tick burn and 60-tick cooling; the body fire's regions,
heights, kindle times and peaks; intensity following the Kindling; the stole, wraps and moss tufts; the value targets
and the 15% brass guard.

## 0. Resolutions applied (Resolved 2026-11-05)

The umbrella's resolutions win over this design. Every change they make to Putriciel, with the numbers it now uses:

| Ref | Resolved 2026-11-05 | Was |
|---|---|---|
| A-1 | `CAUTERISING_LANE` is one wide fire run, 3 blocks wide (half-width 1.5), from a lit chamber pyre toward Putriciel: a `Path` through the pyre and the body with one front 4 blocks long travelling 1 block a tick. It is picked only while a lit pyre stands within 20 blocks. Its tongues are Putriciel's own geometry (`PutricielFireGeometry`), not `PyreFlames.line` | centre and side lanes at +-3, 12 long, from the body |
| A-3 | One jump ring: the **Full Purge** (was the Last Purge), three rings at `Height.JUMPABLE` (0 to 0.9). `SAC_SWELL` is a slow spore cloud you walk out of: a ring 0 to 8 (II 9, III 10) over 36 ticks, band 1.8, full height (`Height.STANDING`), so it cannot be jumped. `IGNITION` is a body burst: a disc of radius 6 round the body at its commit; be out of it by then | `SAC_SWELL` and `IGNITION` were jump rings (1.0 and 1.2) |
| A-5 | `STAFF_PLANT` becomes **`CENSER_DROP`**: the censer is swung up on its chain and dropped at the quarry, bursts and fouls in the floor (disc radius 2.75 at the locked aim). Its opening is the **Fouled Chain** (30 ticks, x1.25, Kindling x1.5) while Putriciel drags the chain free | staff slam, Staff Caught |
| A-6 | One melee pair: `CENSER_SWING` and `CENSER_BACKSWING` stay in the rot. `PURGING_SWEEP` is one long reach (a lane 8 long, half-width 1.0, front at 2 blocks a tick) that leaves a short **fire wall** across its end (a lane 4 long, half-width 0.4, 60 ticks). `PURGING_RETURN` is deleted. The Purge string is Purging Sweep then Censer Cast (quarry at MID, 60%, gap 6) | a sweep and its return |
| B-1 | One set of range bands: HUG under 3, NEAR under 6.5, MID under 12, FAR beyond (`BossBands.STANDARD`). `ROT_BREATH` (a 7-block cone) now also answers MID FRONT at half weight; nothing else moved a cell | HUG 2.5, NEAR 6, MID 11 |
| B-2 | AREA moves (wind-up floor 20): `OFFERING_GRASP` (20, was 18), `PURGING_SWEEP` (20, was 16), `CENSER_DROP`, `ROT_BREATH`, `SAC_SWELL`, `SHELF_VOLLEY`, `ROOT_SNARE`, `BLOOM_OF_ROT`, `IGNITION`, `CENSER_CAST`, `CAUTERISING_LANE`, `PYRE_LINES`, `FULL_PURGE`. MELEE (floor 12): the censer pair, `CENSER_ORBIT`, `PYRE_HEART`, `MOULD_COMMUNION` and the two kneeling beats (`OFFERING`, `EMBERING`) | - |
| B-3 | Reveals clamp to 20 (area) and 12 (melee) | - |
| B-4 | The router passes the item's own reach and rate. The Offering's projection takes at most the item's rate a tick (5 at the staff's usual rate) until 100 blood is in the palm; the Spent Sacs absorption moves no blood (the absorber takes rot, not blood) and stokes 1.5 Kindling a tick | fixed 5.5 / 5 reaches |
| B-5 | One scaling rule: Saint health and the Kindling's maximum scale by 1 + 0.5 (n - 1) from `challengerCount()`. The creep, the burn-down and the Repose head start are fractions of the maximum, so their timing does not change with the group; damage-driven fill is absolute, so a group fills a larger meter. Shelf Volley still adds two discs per extra challenger (cap 9) | fill divided by 1 + 0.6 (n - 1) |
| B-7 | Decision delay 20, 16 and 12 ticks in Rot I, II and III, 10 in the cleansing interval | none given |
| B-8 | Every animation track blends in from the last pose over 5 ticks | 4 |
| B-9 | The Full Purge is the only jumpable wave and uses `Height.JUMPABLE` | 1.0 |
| B-10 | A reloaded ending resumes (`resumeOnLoad`) | `endTrial` on the first tick |
| C-1 | The interval's end is the **Embering** (opening key `embering`), not the Gutter | Gutter |
| C-3 | The Last Purge is the **Full Purge** (`FULL_PURGE`) | `LAST_PURGE` |
| C-4 | Death messages `death.attack.hemomancy.saint_putriciel` and `death.attack.hemomancy.saint_putriciel_cleansing` (with `.player`), as the kit shipped them | `death.attack.saint_putriciel*` |
| C-5 | Move constants upper snake case, `key()` lower case | - |
| D-4 | Harder, not longer. The **Repose** deed (*It Never Pauses*) removes the Embering's lull; instead of the head start the Embering's x2 hits would have built, the next rot phase starts with Kindling at 25% of its maximum. Rot recoveries are 25% shorter and only a Flammeus hit breaks a Communion. The **Litany** deed (*The Unread Rite*) is the glyph naming the act the palm accepts (a censer: strike; a cupped hand: pour), and keeps Offerings at 60 ticks so the glyph costs reading time, not absolutions | Repose removed the Gutter and its head start; Litany shortened Offerings to 40 |
| D-5 | Every drain from a player names a `BloodAccounting.Source` in Putriciel's package. Putriciel has one: `PutricielEntity.OFFERING_SOURCE` (`putriciel_offering`, category RITE, not a spend, not reserve-protected) | - |
| D-6 | The first interval starts by about 45 seconds: Rot I's creep is +1 per 12 ticks (Rot II +1 per 20, Rot III +1 per 15), so a player who keeps striking fills it in 35 to 45 s and one who never strikes in 60 s. The first time the palm opens, the teaching line names the absolution | +1 per 25 ticks |
| D-7 | `BLOOM_OF_ROT` is gated, never timed: 900 ticks in the phase and Kindling under half, weight 12 in every cell, once per rot phase. The Rot III "or Kindling 70" clause is gone | weight 30, Rot III also at 70 |
| E | Compile-gate edits, not Putriciel's: removing the old Putriciel cases from `SaintPowerGameTests`, `SaintPowerRulesTest` and `SaintEncounterGameTests`; any change to `PyreFlames` or `RotPyreRenderer` (Putriciel keeps its fire tongues in its own geometry class, and the pyre-flare query is not wired); removing the two old lang keys | Putriciel edited them |
| Answers | Offerings accept held Projection (and cleanse the projector by 10); the fire scorches the boss floor to real packed ash through `SaintArena`, always restored; jointed legs under the robe for the shamble; bog bodies rise as faithful on a skipped Downfall; Putriciel is silent, narration only | defaults taken |

**Kit shape the build follows.** An absolving hit is "at least 4" before armour: the kit's pipeline classifies the hit
before armour and the 30% floor, so `hit.amount()` (what the attacker swung, after the opening's factor) is the number
read. Kindling takes +1 per 5 of that raw damage (armour 12 takes about a fifth off, so this is the old "+1 per 4 after
armour"). Colonisation rides the kit meter's per-player state: its exact value is the state's secondary (maximum 20),
and bits 0 to 5 of the flag word carry it in halves so `DATA_CHALLENGERS` shows everyone's growth to every client; bit
6 is Fruiting, bit 7 Overgrown, bit 8 burning (the purging heat on them). Pyre Lines' fronts no longer travel: each line
is a kit `Tether` from its pyre (an anchor) to the body, live through the active span. The Offering and the Embering
are kneeling moves (`OFFERING`, `EMBERING`) whose recovery is the opening, so nothing else is picked while the palm is
out; an accepted Offering queues a 14-tick stagger, which ends the move.

**Worked fight length (proper awakening, prepared solo D8).** Rise 3 s; Rot I 35 to 45 s; interval one about 23 s
(Ignition 2.8 s, the burn-down 15 s, two Offerings and their recoils about 4 s) and its Embering 3.6 s; Rot II about
60 s; interval two 23 s and Embering 3.6 s; Rot III about 50 s; interval three about 15 s (Ignition, the burn to 66%,
the Full Purge's 5.4 s, the fifth Offering); the ending 5 s. With two absolutions in each of the first two intervals:
about 3 minutes 50 seconds. Each missed Offering adds a rot phase's share, about 25 s; a ragged fight runs five to six
minutes.

**Worked forced number (all three deeds, the usual Apotheos).** The Repose deed removes two Emberings (-7 s) and starts
Rot II and III at 25% (about -15 s each), and every rot recovery is a quarter shorter, so the rot presses harder; the
Litany glyph keeps 60-tick Offerings, so a player who reads it absolves as often as in a proper fight; the Downfall
faithful add pressure, not time. About 3 minutes 15 seconds against a Saint that hits 30% harder: harder, not longer.

**Deed number check (D-4).** Without Repose, the Embering's 60 ticks at x1.5 damage taken and x2 Kindling give a solo
player about 5 hits of 7 to 10 raw, so 7 to 10 Kindling, doubled to 14 to 20, plus the creep the next phase starts
with: about the flat 25% the Repose deed now hands over.

## 1. The fight in one paragraph

Putriciel cannot be beaten down; they can only be absolved, and only while the purifier is awake in them. The rot
phases are olive, ochre and cream and never burn: censers trail rot, spore sacs swell out of rhythm, shelf colonies are
flung across the floor and the presence colonises everyone who stands near, a bracket of fungus growing on the player's
HUD and on their body. Every blow the player lands, and above all every Flammeus cast (fire fed to the rot, as the lore
says, is drunk and wakes the buried fire), stokes the **Kindling**, a censer hanging under the bar. When it fills, the
will surfaces: the growths blacken, the outline of the man shows, the chamber's pyres light, and for about twenty seconds
the purifier burns crimson and pink-white. The fire is the most dangerous thing in the room, and the only time the trial
moves: twice in each interval Putriciel kneels and holds out the open palm, and one hit, or twenty ticks of held Blood
Projection poured into the palm, is accepted as an absolution and lights one of the five nodules. Standing in the heat
burns the player's own colonisation off, and the floor the fire scorches to ash is the only ground the next Bloom of Rot
cannot take. Then the fire gutters and the rot comes back stronger. The player learns to feed the fire they fear, to walk
into it when the palm opens, and to fight the rot from the ash the last purge left. Winning feels like finishing the work
he could not: on the fifth absolution the rot stills, the last flame goes through the body and out of the palm, the man
shows for a breath, and Putriciel sinks back into the tomb, purified for a time.

## 2. What stays and what goes

Paths are under `src/main/java/com/vincenthuto/hemomancy/` unless they start with `tools/`, `src/test` or `src/gameTest`.

**Stays (extended):**

| File | Why it stays | What changes |
|---|---|---|
| `common/entity/boss/saint/SaintTrialEntity.java` | The rise, the bar, tomb binding, `endTrial`, enrage record, save/load | Nothing in Putriciel's lane; the kit adds its fields |
| `common/entity/boss/saint/putriciel/PutricielEntity.java` | The entity, its attributes (400 health, armour 12, knockback resistance 0.9), its bar binding and frame id | Rewritten on the kit (§17 step 3). Everything listed under "goes" leaves it |
| `common/entity/boss/saint/putriciel/PutricielChamberLightingRules.java` | The chamber box from the tomb (`AHEAD 30`, `BEHIND 6`, `ACROSS 13`, `BELOW 4`, `ABOVE 12`), the fallback circle, the pyre/shelf switch | `Phase` changes meaning. Today `BURNING` (pyres lit, shelves dim) is the default fight state and `ROT` (pyres out, shelves bloom) is the absolution window, which is backwards for the ruling. New phases: `ROT` (pyres out, shelves bloom) for the rot phases, `CLEANSING` (pyres lit, shelves dim) for the interval, `REST` (as built: pyres lit, shelves dim) for the restored chamber. The scan moves into the kit's `SaintArena`; the rules keep the box maths and the switch table |
| `common/entity/boss/saint/putriciel/PutricielEffects.java` | The `PUTRICIEL_OFFERING` burst channel and `lightShift` (pyre flares in crimson and pink, gutters to soot; shelves breathe green) | Burst kinds re-keyed to the new moves and impacts; `hitGlow(SPORE)` becomes `glow(SPORE)` (traces and accents are glow motes, never `hitGlow`) |
| `common/entity/boss/saint/putriciel/PutricielVisualRules.java` | The presentation envelopes, `SCALE`, `pose`, `fade` | Rebuilt around the kit's move block; the nova and window constants go; `SCALE` becomes 2.0 (§12.1) |
| `client/model/entity/boss/putriciel/PutricielModel.java` | Generated rig | Regenerated from the new generator; never hand-edited |
| `client/model/entity/boss/putriciel/PutricielAnimState.java` | Per-entity replay state with `BossReplayClock`s and the posed attachment points (core, palm, sacs, censers) | Its three clocks (visual, window, nova) become one move clock read from the kit's synced move block, plus a phase clock for the char/ember blend; gains `faceAnchor`, `staffFoot`, `legs` points |
| `client/render/entity/boss/putriciel/PutricielRenderer.java` | Renderer, `SaintRiseRender.sink`, attachment capture each frame | Scale 2.0; adds the char and ember layers |
| `client/render/layer/mob/PutricielSporeGlowLayer.java` | Emissive mask on spore sacs and gills | Fades out as the char rises; cream and green only |
| `client/render/world/PutricielOfferingGeometry.java` | The body-anchored rot and fire geometry, the five palm nodules, the `PUTRICIEL_OFFERING` burst drawing | Split into three package-private classes (§12.4); the square nova outline (today an AABB, `perimeter`) becomes the true ring the new footprint tests |
| `client/screen/overlay/boss/frame/PutricielFrame.java` | Putriciel's painted bar frame (mossed rails, blooms) | Five nodules, phase fills, the ember heart only in the fire (§12.6) |
| `common/block/harbinger/decoration/RotPyreBlock.java`, `SporeShelfBlock.java`, `client/render/tile/harbinger/decoration/RotPyreRenderer.java`, `client/render/world/PyreFlames.java` | The set pieces the fight drives, and Putriciel's Flammeus shader fire | `RotPyreRenderer` reads a new client flare query; `PyreFlames` gains two additive public methods (`arc`, `line`); existing signatures stay for `PulsePylonRenderer` |
| `tools/model_export/putriciel.py`, `putriciel_anim.java` | The rig, atlases, `.bbmodel` and the Java pose code | Rig rescale, legs, purifier face, two more sacs, palette pass, two new atlases; the anim file is rewritten as Catmull-Rom tracks |
| `src/test/.../common/block/harbinger/decoration/PutricielFireResourceTest.java` | Fire texels never green or yellow | Extended to the new ember atlas |

**Goes (deleted outright; nothing is released, so no save migration):**

- In `PutricielEntity`: the cadence timers (`ROT_NOVA_INTERVAL` 120 ticks, `ABSOLUTION_CYCLE` 300, `ABSOLUTION_WINDOW`
  80, `absolveCycleTimer`, `absolveWindowTimer`), `fireRotNova`, `openAbsolutionWindow`, `endAbsolutionWindow`, the synced
  `DATA_ABSOLVED`, `DATA_ABSOLUTION_TIMER`, `DATA_NOVA_TIMER`, `DATA_NOVA_RELEASE`, `VISUAL_ROT_NOVA`,
  `VISUAL_ABSOLUTION_HIT`, `registerTrialGoals(0.6D)` (the vanilla `MeleeAttackGoal` and stroll), the `doHurtTarget`
  melee, the `hurt` override's "any hit while absolved counts" rule, `tickSecondaryAttacks`, the `WITHER_AMBIENT` /
  `WITHER_DEATH` sounds and the old chat lines' use.
- `common/entity/boss/saint/putriciel/PutricielCleansingRules.java` (the three-block, two-damage cleansing pulse at
  remaining 60/40/20): its idea becomes the Pyre Heart move.
- `SaintAttackPattern.SPORE_GARDEN` and `CENSER_SWEEP` and the Putriciel branch of `SaintSecondaryAttacks` and
  `client/render/world/SaintPatternGeometry.java` go with the kit, which owns those files; Putriciel's footprints move into
  `PutricielMove`.
- `src/test/.../architecture/PutricielPresentationGuardrailTest.java`'s two Putriciel-entity assertions
  (`DATA_NOVA_RELEASE`, `80, witherAmp, false, false, true`): rewritten (§16). The pyre and dispatcher assertions stay.
- `src/gameTest/.../gametest/PutricielFightClientReview.java`: rewritten as the new fight review (§16).
- Putriciel's cases in `SaintPowerGameTests` (`putricielBothSecondaryAttacksRespectWindupAndRecovery`,
  `putricielRotNeverBurnsAndCleansingWaitsForItsWindup`) and `SaintEncounterGameTests`
  (`putricielsPhasesAlternateTheChambersPyresAndSporeShelves`, `DrivenPutriciel`): those files are shared, so their
  removal is a kit follow-up (§18), replaced by `PutricielFightGameTests`.
- Lang keys `message.hemomancy.putriciel.the_rot_stills_absolve_him_now` (it also says "him") and
  `message.hemomancy.putriciel.the_window_closes` stop being used; the integration stage deletes them (`en_us.json` is
  add-only for a Saint builder).

Removed textures and the old `.bbmodel` go to the untracked `removed_assets/` when the build runs, not now.

## 3. Phases

Phases are driven by the fight's own cycle, not by health: Putriciel's health floors at 30% and is never the win
(§7.4). "Interval" means a cleansing interval; the count is how many intervals have ended.

| Phase | Trigger | What it adds | Bar (vanilla colour; frame fill; name) | Card |
|---|---|---|---|---|
| Rise | Woken (vial, paste, relic, door) | `SaintRise` as today, 60 ticks. At the bar reveal the rot takes the room: every chamber pyre gutters out (one every 3 ticks, dais first), every spore shelf blooms | Hidden, then shown | `SCENE_SAINT_RISE` (existing) |
| Rot I, *The Befouled* | Rise ends | Censer Swing and Backswing, Offering Grasp, Rot Breath, Spore Sac Swell (ring 8), Shelf Volley (5 discs). Presence +1 colonisation per 20 ticks, radius 7. Kindling creeps +1 per 25 ticks. The Kindling meter appears with its first line | GREEN; olive fill (`0xFF8A9A3A` to `0xFF3E4A1A`); "Saint Putriciel" | none |
| Cleansing interval (every one) | Kindling reaches 100, started at the next recovery boundary | Rot moves and presence stop. Ignition, then fire moves (Purging Sweep and Return, Censer Cast, Cauterising Lane, Pyre Heart), two Offerings, the Gutter. Pyres lit, shelves dim. Purging heat round the body. Targets the most colonised challenger | PINK; crimson fill with a pink-white top (`0xFFFFA8BE` to `0xFFD61E2E`); "Putriciel, the Purifier" | First interval only: `putriciel_purifier_surfaces`, 50 ticks, "The Purifier Surfaces" |
| Rot II, *The Rot Returns* | First interval's Gutter ends | Staff Plant (Thurible string end), Root Snare (and its string), Mould Communion, Bloom of Rot (once per rot phase). Sac Swell ring 9. Presence +1.5 per 20 ticks, radius 8. Creep +1 per 20 ticks | GREEN; mould-green fill (`0xFF9AC44A` to `0xFF3E5A1A`, today's) | First return only: `putriciel_rot_returns`, 50 ticks, "The Rot Begins Again" |
| Interval 2 and later | As above | Adds Pyre Lines | As above | none |
| Rot III, *The Everlasting Blight* | Second interval's Gutter ends (and every later one) | Censer Orbit. All rot wind-ups 3 ticks shorter, never below the umbrella floors. Sac Swell ring 10, Shelf Volley 7 discs. Presence +2 per 20 ticks, radius 9. Creep +1 per 15 ticks. Bloom of Rot may also start at Kindling 70 | YELLOW; ochre fill (`0xFFC8B45A` to `0xFF6A5A22`) | none |
| The Last Purge | Inside any interval, when absolutions stand at 4 and an Offering is due | The Last Purge replaces the fire move before that Offering; that Offering lasts 60 ticks | PINK | none |
| Ending | The fifth absolution | §11 | Bar full, then hidden | none (an authored sequence, not a camera scene) |

The rot phases never leave Rot III: a player who misses Offerings fights the strongest rot until they absolve. Phase
changes queue to the recovery boundary like every interrupt (kit rule).

**Fight length** (Resolved 2026-11-05: superseded by the worked number in section 0). Rise 3 s; Rot I about 55 to 65 s; each interval about 23 s; Rot II about 60 s; Rot III about 50 s.
With two absolutions in each of the first two intervals and one in the third, a prepared solo D8 player finishes in
about four minutes; each missed Offering adds about 25 s, so a ragged fight runs five to six.

## 4. The signature meter: the Kindling

**Owner.** The Saint (one value shared by every challenger), a kit `SaintMeter` with value 0 to 100 (stored as tenths),
thresholds 66, 33 (interval) and 70 (Rot III Bloom), synced as entity data. The win count, the five absolutions, is the
boss bar's progress (`absolutions / 5`, as today), not this meter.

**Filling (rot phases only).**

| Source | Kindling |
|---|---|
| Natural creep (the buried will always stirs) | +1 per 12 ticks (Rot I; Resolved 2026-11-05, D-6), 20 (Rot II), 15 (Rot III) |
| Damage a challenger deals (after armour, before the 30% floor clamps it) | +1 per 4 damage |
| Flammeus damage (a FLAMMEUS manipulation's hit, or any fire-tagged damage) | ×2.5 on the line above |
| Fire on Putriciel's body (they cannot burn: fire-tagged damage deals 0 and is drunk) | the drunk amount ×2.5 / 4, as above |
| Hits during Spent Sacs, Staff Caught | ×1.5 |
| Hits during Broken Communion, Spent Bloom, the Gutter | ×2 |
| Held Blood Absorption on spent sacs (§6) | +1.5 per tick, at most +40 per opening |
| Breaking a Mould Communion | +10 at the break |

Resolved 2026-11-05 (B-5): the maximum scales by `1 + 0.5 × (challengers − 1)` instead; the creep and burn are
fractions of it, damage-driven fill is absolute.

**Draining.** Mould Communion drains 1 per 2 ticks over its 60-tick active span (30 in all) unless broken. Nothing else
drains it in the rot.

**The interval.** At 100 the interval is queued; it starts at the current move's recovery boundary (never mid-move,
never during an opening). Kindling holds at 100 through Ignition, then burns down 1 per 3 ticks. It pauses while an
Offering is open and during the Last Purge. Crossing 66 arms the first Offering and 33 the second; each opens when the
current fire move's recovery ends. At 0 the fire gutters. A full interval is 60 (Ignition) + 300 (burn) + up to 2 × 50
(Offerings) + recoils, about 470 ticks.

**Tells for the meter, all without fire.** At 75 and above the body trembles ("the fungal mass begins to tremble"),
heat shimmer cards rise off the back colony in clear, colourless distortion, and the eschar cracks glow pale cream. A hit
in the rot makes the struck patch of cracks glow cream for 6 ticks. No crimson appears before the interval.

**The HUD meter (`PutricielMeterStyle`, new, painted with `GuiCanvas` like the bar frames).** A pierced censer hangs on
two chain strokes under the bar's centre, 22 by 18 px, blackened iron (`0xFF2A2822` body, `0xFF5C5646` rim light) with
olive mould at its seams (`0xFF6A7A3A`). In the rot, its bowl fills from the bottom with slow olive-grey smoke
(`0xFF5A5E3A` to `0xFF8A8A5A`) and cream glints (`0xFFE8D8A0`) rising through it; at 75 and over it trembles a pixel. In
the interval its contents are fire: a crimson body (`0xFFD61E2E`), a pink-white heart (`0xFFFFA8BE`), black soot curling
at the top (`0xFF16100C`), draining downward; two bone pins (`0xFFD8CDB0`) mark 66 and 33, and the censer swings toward
the bar's nodule row while an Offering is open. Left of the censer, the player's own colonisation (§5) is a bracket fungus
of four tiers, ochre and cream caps (`0xFFA98D4F`, `0xFFE3CC8B`) over mould-green gills (`0xFF76A85C`) with a charcoal
outline; a tier grows at 5, 10, 15 and 20; at 15 it sheds single spore dots; at 20 it pulses; colonisation burned off
flakes away as ash grey (`0xFF4A4744`). Nothing red outside the interval. The meter draws under Putriciel's frame through
the kit's `SaintMeterLayer`.

**First lines (taught in the first minute).** On the bar reveal: "The rot keeps something buried. Fire wakes it." (one
line, `message.hemomancy.putriciel.kindling_taught`). At Kindling 75 the first time: "Heat builds under the growths."
At the first Ignition: "The fire remembers. The man shows." At the first Offering: "The palm opens. Absolve them." Italic,
pale cream (`ChatFormatting.YELLOW` is too strong; the builder uses a translatable with `withColor(0xE8D8A0)`). Narration
only: Putriciel never speaks (§20).

## 5. Presence: colonisation

**Rot phases.** Every 20 ticks each challenger inside the chamber box (the arena's bounds, no line of sight needed:
spores drift round pillars) and within the radius gains colonisation.

| Phase | Radius (horizontal) | Gain per 20 ticks |
|---|---|---|
| Rot I | 7 | 1 |
| Rot II | 8 | 1.5 |
| Rot III | 9 | 2 |

Colonisation is per player, 0 to 20 (stored in halves), kept in Putriciel's own state (a UUID map saved with the entity),
and synced as one entity-data `CompoundTag` of entity id to value for every challenger, so everyone sees everyone's
growth.

| Threshold | Name | Effect |
|---|---|---|
| 5 | Rooted | −10% movement speed (a transient `AttributeModifiers.sync` modifier, `hemomancy:putriciel_rooted`) |
| 10 | Shelved | Healing received ×0.6 (a heal-modifier entry; kit request K-4) |
| 15 | Fruiting | Every 40 ticks the player sheds spores: 1 rot damage to themselves and +1 colonisation to every other challenger within 3 blocks. A group must spread out |
| 20 | Overgrown | Rooted fast for 30 ticks (−100% speed modifier, upward velocity zeroed each tick), 6 rot damage, then colonisation falls to 12 |

Moves add colonisation on hit (listed per move). It falls:

- by 1 per 40 ticks outside the presence radius;
- by 3 each time the player lands a Flammeus manipulation of their own (the purifier's way: self-cautery);
- by 2 per 10 ticks inside the purging heat (below), and by the amounts the fire moves list;
- to 0 at the ending.

Everything colonisation applies is removed when the player leaves the chamber, dies, logs out (transient modifiers are
not saved), or the trial ends, resets or unloads.

**Interval.** The rot presence stops. The **purging heat** takes its place: within 4 blocks of Putriciel, every 10 ticks,
colonisation −2 and 1 cleansing damage per 20 ticks. The cure and the harm come from the same place.

**Ground read.** Rot: a faint mould ring at the presence radius, a `LuxUmbraGeometry.ground` RIPPLE on the
`PUTRICIEL_ROT` material at alpha 0.10, breathing to 0.18 on each 20-tick gain; a soft mould stain under each colonised
player's feet (radius 0.4 + 0.04 × colonisation, alpha 0.25). Interval: a thin heat ring at 4 blocks on the `FLAME`
material, alpha 0.2, rippling outward. No strands, no contour lines.

## 6. Openings

Every win runs through openings. The Saint decides when it is open; the player decides whether to take it.

| Opening | Entry tell | Length | What counts | Damage taken | Feeds |
|---|---|---|---|---|---|
| **Offering** (interval) | After a fire move's recovery once 66 (then 33) is crossed: the fire drops to the body, Putriciel goes down on one knee and holds the open palm toward the most colonised challenger; the five nodules show over the palm, lit ones pink-white; the palm burns pink-white | 50 ticks (60 for the one after the Last Purge; Litany deed 40) | Either one hit on Putriciel by a challenger that deals at least 4 after armour, or 20 ticks of held Blood Projection on the palm (5 blood a tick, 100 in all, through `BloodAccounting.pay` with `PutricielEntity.OFFERING_SOURCE = Source.of("putriciel_offering", ...)`) by one challenger within projection reach (5.5) | ×1 | One absolution: a nodule lights, the body recoils "beneath an accepted offering" (14 ticks), the Offering closes. The projector also loses 10 colonisation (their offered blood is cleansed). Unanswered, it closes with nothing |
| **Spent Sacs** (rot) | After Spore Sac Swell: the sacs hang empty and split, the body sags forward | 34 ticks (recovery) | Held Blood Absorption aimed at Putriciel within absorption reach (5): the absorber draws the rot into their own blood, as he did | ×1 | Kindling +1.5 a tick (cap +40) and the absorber +1 colonisation per 10 ticks; any hit ×1.5 Kindling |
| **Staff Caught** (Resolved 2026-11-05: now the **Fouled Chain**, after `CENSER_DROP`; rot II+) | After Staff Plant: the staff is stuck in the floor, Putriciel hauls on it | 30 ticks | Any hit | ×1.25 | Kindling ×1.5 |
| **Broken Communion** (rot II+) | Mould Communion is interrupted: the body jerks upright, spores scatter from the palm | 40-tick stagger | Any hit | ×1.25 | Kindling +10 at the break, hits ×2 |
| **Spent Bloom** (rot II+) | After Bloom of Rot: Putriciel lies slumped in the colony | 40 ticks | Any hit | ×1.5 | Kindling ×2 |
| **Gutter** (Resolved 2026-11-05: renamed the **Embering**; every interval's end; not with the Repose deed, which grants 25% Kindling instead) | Kindling reaches 0: the tongues shrink to soot, the body slumps to both knees and the growths rush back over it | 60 ticks | Any hit | ×1.5 | Kindling ×2: a head start on the next fire |

No absolution counts outside an Offering. An Offering cannot be taken twice: the first qualifying act closes it. When
two challengers qualify on the same tick, the hit wins (it is checked in `hurt`, before the channel tick).

## 7. Moveset

Ticks are wind-up / active / recovery. Damage is for Normal, before the enrage's ×1.3 (kit request K-7). Rot damage is
`hemomancy:saint_putriciel`, fire damage `hemomancy:saint_putriciel_cleansing` (neither tagged `is_fire`, so Fire
Resistance does not cancel the hit; the crimson afterburn from `CrimsonFireHelper.igniteCrimson` is ordinary fire and
does respect it). Every damage check goes through `ManipulationCombatHelper.canHarmFor`. Range bands (Resolved
2026-11-05, B-1): HUG under 3, NEAR 3 to 6.5, MID 6.5 to 12, FAR over 12; sectors FRONT up to 60°, FLANK 60° to 120°, REAR
beyond. "Picked from" gives the base weight per cell; a half cell is half weight. No move runs on a timer: each has a
cooldown, and the picker, pressure and follow-ups decide.

### 7.1 Rot moves (rot phases only; no fire texel, no ignition)

| Id | Name | Phase | Picked from | Tell (animation beat; world geometry) | Footprint | Timeline | Damage and effect | Counterplay | Follow-ups |
|---|---|---|---|---|---|---|---|---|---|
| `CENSER_SWING` | Censer Swing | I+ | HUG/NEAR FRONT 22, FLANK 11; cd 60 | The staff is drawn back over the left shoulder and both censers swing up on their chains (tick 6); a rot cone appears at wind-up start | Cone sweep 130°, radius 1 to 6, sweeping right to left through the active span; height 0 to 2.2 | 16 / 6 / 14 | 7 rot, +2 colonisation | Step out past 6 or round to the rear; it cannot be jumped | `CENSER_BACKSWING`, 70%, if still HUG/NEAR, gap 5 |
| `CENSER_BACKSWING` | Censer Backswing | I+ (string only) | follow-up | The censers check at the end of the swing and come back | Cone sweep 150°, radius 1 to 6.5, left to right | 12 / 6 / 18 | 7 rot, +2 colonisation | Read it off the first swing; it reaches a little further | `STAFF_PLANT`, 50%, Rot II+, if HUG, gap 6 |
| `STAFF_PLANT` (Resolved 2026-11-05: now `CENSER_DROP`, section 0 A-5) | Staff Plant | II+ (string only) | follow-up | The staff is raised two-handed and held at the top for 6 ticks (the delay that catches an early roll), then driven down | Disc radius 3 at the staff foot, 1.8 blocks ahead of the body, plus four root cracks (lines, 3 long, half-width 0.4) radiating from it | 20 / 4 / 30 (opens Staff Caught) | 10 rot, an upward toss (0.5), +3 colonisation | Wait out the hold, then leave the disc | none |
| `OFFERING_GRASP` | Offering Grasp | I+ | NEAR FRONT 16, MID FRONT 8; cd 80; ×2 while the quarry holds a shield up (turtling) | The right hand opens palm up and its rot drips run long (tick 8), then it turns palm out and reaches; a lane telegraph runs from the palm; aim tracks until the commit tick | Lane 6 long, half-width 0.8, locked at commit | 18 / 5 / 16 | 5 rot, +5 colonisation (through a raised shield: the damage is blocked, the colonisation is not), a pull of 2 blocks toward Putriciel (one velocity impulse), Slowness II for 20 ticks | Sidestep the lane after the palm turns | `ROT_BREATH`, 75%, on hit, gap 4 |
| `ROT_BREATH` | Rot Breath | I+ | NEAR FRONT 12; cd 100; also the Grasp's follow-up | The hood tilts back, the face vines part, spore motes gather in the hollow (tick 12); cone telegraph | Cone 60°, radius 7, then a lingering cone cloud for 60 ticks | 20 / 30 / 18 | 2 rot per 10 ticks inside during the exhale, +1 colonisation per 10 ticks; the lingering cloud 1 rot and +1 colonisation per 20 ticks | Leave the cone sideways; flank | none |
| `SAC_SWELL` (Resolved 2026-11-05: a slow spore cloud at full height, section 0 A-3) | Spore Sac Swell | I+ | HUG/NEAR any sector 14, plus hug pressure (+3 per 20 HUG ticks in the last 200, cap +21); cd 160 | The eight sacs swell one after another, out of rhythm, 3 ticks apart, the body hunching deeper with each; the final ring shows at wind-up start, the travelling front through the active span | Expanding ring wave 0 to 8 (II: 9, III: 10) over 12 ticks, band width 0.9, height 0 to 1.0 | 30 / 12 / 34 (opens Spent Sacs) | 6 rot, Wither I 60 ticks (the old nova's), +3 colonisation | Jump the front, or be outside it | none |
| `SHELF_VOLLEY` | Shelf Volley | I+ | MID/FAR FRONT 16, FLANK 8, plus kite pressure (+2 per 20 MID/FAR ticks, cap +16); cd 140 | The back colony's shelves flex up together and the staff points; landing rings appear on the floor at wind-up start | Scattered discs: 5 (III: 7; +2 per extra challenger, cap 9), radius 1.5; one on the quarry's locked position, the rest within 4 blocks along their motion (lead 10 ticks); landings 4 ticks apart | 24 / 20 / 16 | 5 rot per landing disc, +2 colonisation. Each disc leaves a **rot colony** patch (radius 1.5, 160 ticks): 1 rot and +1 colonisation per 20 ticks while stood in. The interval burns every patch away | Read the rings; never fight in a patch | none |
| `ROOT_SNARE` | Root Snare | II+ | MID/FAR any sector 12, plus kite pressure; cd 120 | The ground roots under the robe pull taut (tick 6); crack marks race across the floor to the quarry; a cross forms under them | Cross at the locked target, arms 4.5, half-width 0.6 | 26 / 8 / 18 | 7 rot, Slowness IV 30 ticks (rooted), +2 colonisation | Step off diagonally before the commit | `SHELF_VOLLEY`, 50%, on hit, gap 6 (the snared player is a sitting target) |
| `CENSER_ORBIT` | Censer Orbit | III | HUG any sector 18, plus hug pressure; cd 120 | The staff rises overhead, both chains pay out and the censers start to circle, one slow turn through the wind-up | Annulus 1.5 to 4.5 with two opposite 40° arcs turning through two revolutions; height 0 to 2 | 22 / 40 / 26 | 5 rot per pass (at most once per 10 ticks per player), +1 colonisation | Step in under 1.5 (the eye at their feet) or out past 4.5 | none |
| `MOULD_COMMUNION` | Mould Communion | II+ | Any band with no challenger in HUG/NEAR and Kindling at least 30: 10; cd 600 | Putriciel kneels and lays the palm on the floor; the chamber's shelves bloom brighter; fine spore motes drift from the shelves to the body (cards, never strands) | none (no damage) | 16 / 60 / 20 | Kindling −1 per 2 ticks through the active span (−30), Putriciel heals 1% per 10 ticks | Interrupt it: 14 damage per challenger in the fight, or any Flammeus hit, breaks it into Broken Communion (Repose deed: only a Flammeus hit) | none |
| `BLOOM_OF_ROT` | Bloom of Rot | II+ | Once per rot phase, eligible after 900 ticks in the phase and Kindling under half (Resolved 2026-11-05: weight 12 in every cell, no Kindling-70 clause) | Putriciel sinks to the knees in the colony; every sac and shelf swells; a translucent mould sheet spreads across the floor; ash cells and pyre circles keep clear with a pale cream rim | Field: every floor cell within 12 blocks of the body inside the chamber, except safe cells (packed ash laid by this fight's fire, cells within 1.5 of a pyre, cells within 1.2 of the body); height 0 to 2 | 50 / 60 (pulses at active 0, 20, 40) / 40 (opens Spent Bloom) | 5 rot per pulse, +3 colonisation per pulse. After the last pulse the rot reclaims bare ash: every scorched cell with no one standing on it reverts to its original block (§9) | Stand on the ash the last purge left, or by a pyre | none |

### 7.2 Fire moves (cleansing interval only; the Flammeus look)

| Id | Name | Phase | Picked from | Tell (animation beat; world geometry) | Footprint | Timeline | Damage and effect | Counterplay | Follow-ups |
|---|---|---|---|---|---|---|---|---|---|
| `IGNITION` (Resolved 2026-11-05: a body burst, disc 6, section 0 A-3) | Ignition | every interval, first (not picked) | — | The rot stills; the body shakes; the growths blacken from the core outward and the ember seams open; a flame ring shows at radius 6. The pyres relight one every 4 ticks, dais first | Expanding ring 0 to 6 over 10 ticks, band 1.0, height 0 to 1.2 | 30 / 10 / 20 | 6 cleansing, crimson fire 3 s, −6 colonisation to anyone it strikes. Every rot colony patch burns out; Downfall faithful in the ring burn to ash | Jump it, or take it on purpose to cleanse | none |
| `PURGING_SWEEP` (Resolved 2026-11-05: one long reach and a fire wall, section 0 A-6) | Purging Sweep | interval | HUG/NEAR FRONT 20, FLANK 10; cd 50 | The censers flare and are drawn back low; a flame cone shows | Cone sweep 150°, radius 1 to 7; height 0 to 2.2 | 16 / 6 / 10 | 8 cleansing, crimson fire 3 s, −2 colonisation. Scorches up to 8 floor cells along the arc's outer edge | Leave the arc | `PURGING_RETURN`, 80%, if HUG/NEAR, gap 4 |
| `PURGING_RETURN` (Resolved 2026-11-05: deleted) | Purging Return | interval (string only) | follow-up | The censers come back the other way | Cone sweep 150°, radius 1 to 7, reversed | 12 / 6 / 22 | 8 cleansing, crimson fire 3 s, −2 colonisation; scorch as above | Read it off the first | `CENSER_CAST`, 60%, if the quarry is MID, gap 6 |
| `CENSER_CAST` | Censer Cast | interval | MID/FAR FRONT 18, FLANK 9, plus kite pressure; cd 80 | One censer is whirled overhead twice on its paying-out chain, trailing embers; a disc telegraph sits at the quarry, leading them 8 ticks | Disc radius 2.5 at the locked aim | 22 / 4 / 18 | 10 cleansing, crimson fire 4 s. Leaves a **purging fire** patch (radius 2, 100 ticks): 2 cleansing per 10 ticks inside and −2 colonisation per 10 ticks (a cleanse that costs). Scorches the disc (at most 12 cells) | Move off the disc; step into the patch only to cleanse | none |
| `CAUTERISING_LANE` (Resolved 2026-11-05: one 3-wide run from a lit pyre, section 0 A-1) | Cauterising Lane | interval | Any band FRONT 14, FLANK 7; cd 120 | The palm turns down and sweeps low across the body; three flame lanes show (centre and both sides) | Lane with side lanes: 12 long, half-width 0.75, side lanes at ±3; the centre front advances 1 block a tick, the side fronts start 6 ticks later | 26 / 18 / 20 | 9 cleansing, crimson fire 4 s. Scorches the centre lane (at most 12 cells) | Stand between lanes, or outrun the fronts sideways | none |
| `PYRE_HEART` | Pyre Heart | interval | HUG any sector 18, plus hug pressure; cd 140 | The body straightens and the arms open; the core brightens from within and the seams go pink-white; heat shimmer; a disc telegraph | Disc radius 3.5 round the body | 28 / 8 / 30 | 12 cleansing, crimson fire 5 s, −10 colonisation to those it strikes. Its recovery is the likeliest door to an Offering | Leave the disc, or eat it to burn off a heavy colonisation | none |
| `PYRE_LINES` | Pyre Lines | interval 2+ | MID/FAR any sector 16, plus kite pressure; cd 200 | Putriciel raises the palm; every lit chamber pyre within 20 blocks flares tall in the hot palette; a line telegraph runs from each pyre to the body | One line per flared pyre (at most 10), half-width 0.6, from pyre to body, the front travelling inward at 0.8 blocks a tick | 30 / 20 / 24 | 8 cleansing, crimson fire 3 s. Scorches the lines (at most 16 cells) | Stand in the wedges between the spokes | none |
| `LAST_PURGE` (Resolved 2026-11-05: now `FULL_PURGE`, the only jump ring, `Height.JUMPABLE`) | The Last Purge | when absolutions stand at 4 and an Offering is due (not picked) | — | The fire goes white at the core; Putriciel rises on tiptoe with both arms out; three rings show, one after another | Three expanding rings 0 to 8, 16 ticks apart, band 0.9, height 0 to 1.0 | 40 / 48 / 20, then the 60-tick Offering | 7 cleansing per ring, crimson fire 3 s, −3 colonisation per ring | Jump each ring as it comes | the final Offering |

### 7.3 Strings

| String | Chain | Depth |
|---|---|---|
| The Thurible | Censer Swing → Censer Backswing → Staff Plant (Rot II+) | 2 |
| Seed and Breathe | Offering Grasp → Rot Breath (on hit) | 1 |
| Snare and Volley | Root Snare → Shelf Volley (on hit) | 1 |
| The Purge | Purging Sweep → Purging Return → Censer Cast (quarry at MID) | 2 |

Follow-ups ignore anti-repeat damping and cooldowns and never run more than two deep (kit rule). A queued interval or
phase change cancels the rest of a string at the next recovery boundary.

### 7.4 Rules round the moves

- **Health.** 400 as today. Putriciel's health cannot fall below 30%: damage past the floor is not applied but still
  feeds the Kindling. At the end of each Gutter they regrow to full ("the surviving fungal structures absorbed the heat
  and rapidly regenerated"). Health is never shown; the bar is the absolutions.
- **Target.** Rot phases: the kit's default quarry (who hurt them last, else the nearest challenger). Interval: the most
  colonised challenger, ties to the nearest. The purifier hunts the infection, which both endangers and offers cleansing
  to whoever carries the most.
- **Anti-repeat and pressure** as the Mycophant: never the same move twice running, recent picks ×0.4, 0.6, 0.8;
  kite and hug pressure from the last 200 ticks as listed.
- **Distribution target.** In a simulated fight no move takes more than a third of a phase's picks, and every band has
  at least two answers in every phase (§16).

## 8. Locomotion: the shamble and the stride

Putriciel's strategy plugs into the kit's `SaintCombatController` (new class `PutricielLocomotion`, Putriciel's lane).

- **Rot: the shamble.** A lurching step, 0.9 blocks every 16 ticks (Rot III: every 12), the weight thrown forward on
  the lead foot and the roots dragging behind (the ground-root trails scrape on each step). Average speed about 0.056
  blocks a tick (Rot III 0.075). Putriciel holds the middle of the boss floor (x 9 to 23, z 6 to 20 of the chamber) and
  shambles toward the quarry only when they have stood in MID or FAR for more than 60 ticks; it prefers to stand on or
  beside a rot colony patch; it never walks onto a pyre or into the aisles. Turn rate 5° a tick (Rot II 6, Rot III 8).
- **Interval: the stride.** Upright, a steady walk with a 14-tick step cycle, 0.11 blocks a tick, closing to NEAR on the
  most colonised challenger. Turn rate 10° a tick. Knockback resistance 1.0 while the fire burns.
- **Openings and the ending:** still. The Offering faces its challenger by turning in place at 6° a tick.
- **Leash:** the kit's (the chamber box, 600 ticks empty resets unrewarded).

## 9. Arena

**Set pieces that take part**, all placed today by `tools/worldgen/mausoleum/themes/putriciel.py` in the 37 by 17 by 27
chamber (`pieces.py`: boss floor x 9 to 23, z `CHAMBER_ZC` ±7; tomb at (30, 4, 13) facing west):

| Piece | Where (chamber coordinates) | Role |
|---|---|---|
| Rot Pyres, dais | two `dais_flank` sockets (28, 5, 7 and 19), two `BACK_PYRES` (33, 4, 8 and 18) | Out in the rot, lit in the interval; safe circles for Bloom of Rot; Pyre Lines sources |
| Rot Pyres, aisles | aisle foot of pillars x 9, 17 and 25, at z 4 and 22 (`dress_chamber`) | The same |
| Spore Shelves, rank pillars | every `CHAMBER_PILLARS` x at y 6 on the boss-floor faces (z 6 and 20), ten | Bloom in the rot, dim in the interval; Mould Communion draws from them |
| Spore Shelves, apse | three `apse_niche` sockets | The same; the epilogue shelf (§11) |
| Bog bodies | eight `aisle_alcove` and two `aisle_end` sockets | The Downfall deed's faithful (§10) |
| Boss floor | floor course y 0, moss, rooted dirt and mud from `dress_chamber` | Scorched to packed ash by the fire moves; reclaimed by the Bloom |

**What the fight changes, all through the kit's `SaintArena`** (which records each position's first original state and
restores it):

| Change | When | Cap |
|---|---|---|
| Pyre `lit=false` / `true` | The rise's end puts every pyre out; each Ignition relights them; each Gutter puts them out | every pyre in the chamber box |
| Shelf `bloom=true` / `false` | Bloom in the rot, dim in the interval | every shelf |
| Floor cell (y 0, open above, inside the boss floor or the processional) to `hemomancy:packed_ash` | Fire moves' scorch, per move as listed | 48 per interval, 160 recorded in all; past the cap the oldest scorch reverts first |
| Ash cell back to its original | Bloom of Rot's end, for every scorched cell with no one on it | — |
| Bog body to air while its faithful lives | Downfall deed only | 8 per fight |

Scorched cells are real blocks so the Bloom's safe ground is the ground the player can see; packed ash already exists
and walks like ordinary ground. The interval also burns every rot colony patch (entities, not blocks).

**Restoration.** `endTrial`, `remove` (a `/kill`, a discard, an unload) and a load of a Putriciel whose trial already
ended all restore the arena: pyres lit as built, shelves dim, every floor cell and bog body back, every hazard and
faithful removed. That is the `REST` lighting phase.

**No chamber generator edit is needed.** The chamber already has everything the fight uses. If a review shows the
pyre circles too few for the first Bloom, the fix is two more `dais_flank`-style pyres in `chamber_sockets`, re-run for
Putriciel only.

## 10. Deeds

A forced awakening keeps the umbrella's stat floor (+30% attack, +15% speed; Downfall +50% health; Repose +20% speed;
Litany Regeneration I). For Putriciel the health and Regeneration bonuses mean little (health is never the win), so each
skipped Chapel also lends a deed in Putriciel's language. A Putriciel complex has Litany, Downfall and Repose; Beholding
lends nothing here.

| Chapel skipped | Deed | What it does |
|---|---|---|
| **Litany** (the Burned Library; Resolved 2026-11-05: Offerings stay 60 ticks, section 0 D-4) | *The Unread Rite* | The words he read over each relic are ash, so the rite must be read off the palm. Each Offering names the act it will accept with a glyph burning over the palm: a censer (strike them) or a cupped hand (pour blood into the palm). The other act does nothing. The glyph is chosen at random, never the same three times running. Offerings last 40 ticks instead of 50 (the Last Purge's 60 stays) |
| **Downfall** (his fall, fought as a gauntlet) | *The Faithful Rise* | The faithful the field took rise with them. Each Root Snare, and each Bloom of Rot's first pulse, raises the bog body nearest the quarry as a colonised Bog Revenant (`EntityInit.bog_revenant`, tagged `putriciel_faithful`, no drops, no experience): at most 2 alive, 8 per fight. A faithful's hit adds +2 colonisation. The fire takes them back: Ignition's ring, Pyre Heart and the Last Purge burn every faithful they touch to ash |
| **Repose** (the Ash Garden's stillness; Resolved 2026-11-05, section 0 D-4) | *It Never Pauses* | No Gutter: the interval ends straight into the rot, with Kindling at 0 and no punish window. Rot recoveries are 25% shorter (rounded down, Offering-feeding openings excepted). Mould Communion can be broken only by a Flammeus hit |

## 11. The ending

| Tick | Beat |
|---|---|
| 0 | The fifth absolution lands. The Offering is accepted; the five nodules blaze pink-white; Putriciel stops and becomes untouchable (the kit's ending hook, K-9). Presence and heat stop; every hazard patch and faithful burns out over 10 ticks; every challenger's colonisation burns away to 0 with them (flakes of ash on the HUD bracket). The bar fills |
| 0 to 20 | *The rot stills.* They sink to both knees, palm still up; the growths blacken from their edges inward (char layer to 0.9) |
| 20 to 50 | *The last cleansing.* Crimson fire climbs the body from the core (ember layer at its peak), the shelves, sacs and mushrooms burn down to stubs (scale to 0.2), the face vines burn away and the purifier's face shows inside the hood, held still for 30 ticks: the man, for a breath. Every chamber pyre flares tall in the hot palette |
| 50 to 70 | *The fire dies.* The pyres gutter to embers, the ember layer fades to soot, and where the body kneels spores and fading threads lift (rot material motes and fine short threads, alpha under 0.4, no thick strands) |
| 70 to 100 | *Rest.* The body sinks into the sarcophagus (the rise's sink played backwards). Line: "Putriciel is purified, and will rest for a time." (existing key) |
| 100 | `endTrial` once: one loot roll, `SaintSarcophagusBlock.restoreDormant`, the door record kept; the arena restores the chamber (pyres lit as built, shelves dim, floor and bog bodies back); the entity is discarded |
| 100 to 160 (client only) | *Until the rot reclaims them.* The apse's middle spore shelf breathes one faint bloom of glow motes, then stills. No block changes |

The ending tick is saved; a Putriciel loaded mid-ending resumes it (Resolved 2026-11-05, B-10).

## 12. Visual pass

### 12.1 Model and generator (`tools/model_export/putriciel.py`)

- **Texel density.** Today `kit.SCALE = 2` and a 1.4 render scale give about 23 px per block. The rig is rescaled by
  0.7 (every pivot and box multiplied, snapped to quarter units, no cube under 0.5 units) and drawn at 2.0
  (`PutricielVisualRules.SCALE`), so the world size is unchanged (about 3.85 blocks to the crown's tallest spire) and the
  atlas lands at 16 px per block (2 px per unit × 16 ÷ 2.0). The generator asserts it.
- **Legs.** New jointed legs under the robe (`left_thigh` / `left_shin` / `left_foot`, same right; knee and ankle
  pivots from the `annetta_kit` joint helpers), knees slightly bent and toes turned in at rest. The fourteen robe panels
  follow the thighs (front panels take 70% of the thigh's pitch, sides 40%) so the shamble reads through the cloth. The
  `ground_roots` trails stay and drag.
- **Posture.** The chest's rest pitch goes from 8° to 18° (the permanent hunch the fungal structures force); in the
  fire it straightens to 2°.
- **The man under the rot.** New `purifier_face` (a gaunt face plate in pale ash cream with charcoal eye hollows, 3.5 by
  4 units) set 1 unit behind the hollow `void` face and pushed forward in the fire; new `eschar_seams` (thin plates along
  the chest, upper arms and back colony's roots in a seam material that is dark in the base atlas and lit only in the
  ember atlas).
- **Sacs.** Two more sacs on the back colony (`back_spore_0`, `back_spore_1`) so the out-of-rhythm swell reads from
  behind: eight in all.
- **Palm.** Five nodule bumps (`palm_nodule_0` to `_4`, 0.75 units) on the offering palm; their light stays geometry
  (§12.4).
- **Censers.** Pierced lids on both censers (a ring of 0.5-unit gaps), so the fire shows through them in the interval.
- **No bone-brick anywhere**; bone stays on the fingers only, as now.

### 12.2 Textures and palette

The base atlas is mostly olive already. What is red today, and its replacement:

| Material | Today | New |
|---|---|---|
| `offering` (censer contents) | (208, 61, 30) and lit | smouldering rot: ochre (150, 128, 60), glow mask cream (240, 224, 160) at half strength |
| `blood_cloth` hem shading | mixes to (47, 25, 22) | charcoal olive (40, 38, 24) |
| `iron` | patina from (61, 62, 29) to (176, 133, 51), a brass read, with (72, 46, 26) rust flecks | blackened iron (34, 33, 30) to (96, 90, 72), olive mould bloom (98, 110, 52) on edges, ochre flecks (110, 92, 44): no brass, no rust red |
| `red_cap` | renamed `pale_cap`; tip (220, 184, 146) | tip (226, 212, 170) |
| `blood` | olive (66, 62, 29) | unchanged colour, renamed `ichor` |

New atlases from the same rig and UVs:

- `textures/entity/boss/putriciel/putriciel_charred.png`: the growths burned: charcoal caps (30, 28, 26) with alligator
  cracks, pale ash on the rims (150, 146, 138), vestments scorched to (52, 48, 40). Drawn over the body by the char layer.
- `textures/entity/boss/putriciel/putriciel_embers.png`: the eschar seams, sacs' splits, the censers' insides and the
  palm, in the fire palette only: crimson body (214, 30, 46), pink-white heat (255, 168, 190), black soot tips (22, 10, 12).
  Never green or yellow (`PutricielFireResourceTest` extended).
- `putriciel_glow.png` keeps cream (255, 233, 145) on sacs and gills; the gills move to mould green (118, 206, 92), the
  one green the ruling allows.

### 12.3 Layers and shaders

- `PutricielSporeGlowLayer` (kept): alpha × (1 − char).
- New `client/render/layer/mob/PutricielCharLayer`: the model again with `putriciel_charred.png` through a static
  `RenderType.entityTranslucent` constant; alpha = char (0 in the rot; rises to 0.85 across Ignition; holds; falls to 0
  across the Gutter's regrowth).
- New `client/render/layer/mob/PutricielEmberLayer`: `RenderType.eyes(putriciel_embers.png)` as a static constant;
  alpha = ember (0 outside the interval and the ending, checked in code: the layer returns before drawing when the
  synced phase is a rot phase).
- Growth scale: in the interval the shelves, mushrooms and sacs shrink to 0.35 (Ignition) and regrow with a 10%
  overshoot through the Gutter.
- **No new shader.** Rot uses `manipulation_putriciel_rot` through `LuxUmbraBatch.Material.PUTRICIEL_ROT`; fire uses the
  Flammeus `FLAME` / `STEADY_FLAME` (`ThermalRenderTypes`) through `PyreFlames`, `ThermalGeometry.smoke` and
  `ThermalParticles.lick`, the same fire the pyres burn.

### 12.4 World geometry, per move and per impact

`PutricielOfferingGeometry` splits into three package-private, static-only classes in `client/render/world/`, hooked
where it is today (`ManipulationVisualRenderer` before `AnimusMortemGeometry`, `ManipulationMotes` skip):
`PutricielRotGeometry` (body-anchored rot, rot moves and impacts), `PutricielFireGeometry` (fire moves, Offering,
ending) and `PutricielColonisationGeometry` (player-anchored growth and ground reads). Telegraph shapes are drawn by the
kit's `SaintTelegraphRenderer` from the synced move block, in the rot or flame family per move (K-1). Base tint white;
accents hot core `0xFFD2C4` and, for rot, a dark clot of charcoal olive `0x2A2A14` (never the Riposte's red clot).

| Move or beat | Geometry (toolkit pieces) |
|---|---|
| Body, rot | Existing sac drips and motes (`ribbonPath` RIBBON, `card` SLASH on `PUTRICIEL_ROT`), censer rot wisps; the fire branch on the body is removed outside the interval |
| Censer Swing / Backswing | Censer trails: a `ribbonPath` WISP sampled from the last five censer poses, alpha 0.35; on hit, a rot impact: two short SLASH cards and four `HemoParticleData.sporiticSpore` motes |
| Staff Plant | Floor cracks as four dark ribbons 0.05 wide racing out; a rot RIPPLE `ground` at the foot |
| Offering Grasp | The drips stretch along the lane (three ribbons, alpha 0.5); on hit, a mould bloom card on the player's chest |
| Rot Breath | Drifting WISP cards through the cone, alpha at most 0.35, plus motes; the lingering cloud the same, fading |
| Spore Sac Swell | The ring wave as a `ground` RIPPLE on rot plus low WISP cards along the travelling front (replacing today's square) |
| Shelf Volley | Shelf fragments as SLASH cards on parabolic arcs from the back colony; colony patches as a `ground` POOL (shape 8, its fine capillaries kept at the shader's low hypha factor) at alpha 0.55 |
| Root Snare | Crack ribbons to the target; root cards (SLASH) rising at the cross |
| Censer Orbit | A ribbon along each censer's arc, rot WISP, alpha 0.4 |
| Mould Communion | Spore motes (cards 0.03) drifting from the bloomed shelves to the palm; a palm stain `ground` on rot |
| Bloom of Rot | A mould sheet as `ground` POOL tiles per 2 by 2 cells at alpha 0.22, breathing; safe cells rimmed in pale cream (ribbon 0.04, alpha 0.3); each pulse a RIPPLE |
| Ignition | `PyreFlames.purge` ring from radius 0 to 6 with its tips charring by age; the ember layer opens |
| Purging Sweep / Return | Tongues along the swept arc (new additive `PyreFlames.arc`), censer flame cards (`STEADY_FLAME`), soot by `ThermalGeometry.smoke` |
| Censer Cast | The chain as a thin dark ribbon, the censer trailing `HemoParticleData.emberSpark` crimson and pink-white; impact `PyreFlames.purge` radius 2.5; the purging fire patch as low `PyreFlames.render` tongues (height 0.4) |
| Cauterising Lane | Tongues along each lane (new additive `PyreFlames.line`), leaving the ash blocks behind |
| Pyre Heart | A core heat card (FOCUS, `0xFFD2C4`), the seams at full, a purge ring at 3.5 |
| Pyre Lines | The pyres flare through `RotPyreRenderer` reading a new client query `PutricielPyreFlares.hot(pos)` (hot palette, 1.6× height); tongues along each line |
| Last Purge | Three purge rings in turn, the core card white-hot |
| **Offering** (the focal moment, to the Riposte standard) | Back to front: *shell*, a broad translucent heat wisp rising off the palm (alpha 0.28) with WISP cards trailing upward; *sinew*, three fine flame threads spiralling up from the palm, tapering 0.07 to 0.035, pulsing with `pow(max(0, sin(t × 0.6)), 4)`, alpha 0.5; *knots*, the five nodule cards (lit ones FOCUS pink-white); *business end*, the palm's flame card; *motion*, on acceptance a recoil trail ribbon from the palm down the forearm and a burst: the new nodule's FOCUS card swelling with a `sin` overshoot, four ember sparks. Seeded flicker per element |
| Gutter | The tongues shrink to soot (purge age to 1), `darkGlow` soot motes, the growths regrow |
| Ending | §11: the man's face, the climbing fire, the lifting spores and threads |
| Cleansing impact (any fire hit) | Four ember sparks, a FOCUS flash, a small purge ring radius 0.6 at the target's feet |
| Colonisation on a player | Shelf cards on the shoulders and back from 10, small cap cards from 15, the foot stain; growth burned off flakes as soot motes |

Particles stay accents and read with Minimal particles; geometry carries every effect. No `ParticleTypes` in any
Putriciel file.

### 12.5 HUD meter art

§4: the censer and the colonisation bracket, `PutricielMeterStyle`.

### 12.6 Bar frame (`PutricielFrame`)

- The centre bloom becomes a row of five fungal nodules on the lower rail at 20, 40, 60, 80 and 100%: unlit, olive base
  and pale cream cap; absolved, a cream-white glow (`0xFFF4ECCB`).
- During the interval: the rails char (moss to charcoal `0xFF2A2622`), lit nodules and the end blooms' hearts burn
  crimson with pink-white centres. `EMBER` (`0xFF6A1A`, an orange-red shown at every fill today) is used only in the
  interval; in the rot the hearts are cream.
- Fills per phase as §3 (`fillTop(phase)` / `fillBottom(phase)`); the name colour stays `0xD8D8A8`.
- Phase reaction: a 20-tick charring sweep across the rails at Ignition, a moss creep back at the Gutter.

## 13. Animation

Java keyframe tracks in `tools/model_export/putriciel_anim.java`, Annetta-style Catmull-Rom keys on each move's own clock
read through the kit's synced move block and `BossReplayClock`; the phase clock (interval start, gutter start) drives the
char and growth blends. Every move blends in from the last pose over 4 ticks; strings blend hit to hit. Keys below are
ticks from the move's start.

| Track | Beats |
|---|---|
| `idle_rot` | 120-tick breath; chest hunch 18°; sacs pulsing out of phase (0.11 rad/tick, 0.9 offsets); chains and robe sway; head drifting toward the quarry within ±25° |
| `idle_fire` | Upright (chest 2°), a fine tremor (±0.6°, 0.9 rad/tick), censers swinging lit, palm half open |
| `shamble` | 16-tick cycle (III 12): 0 plant the lead foot, 4 weight thrown over it with the chest dropping 4°, 8 the trailing leg drags (robe panels lag 3 ticks), 12 recover; the staff ground-taps on 0 |
| `stride` | 14-tick even walk, knees lifting, censers swinging in counter-phase |
| `turn` | Shuffle steps in place, both phases |
| `rise` | Existing `SaintRise` sink; adds the shelves unfurling from 0.6 to 1 between ticks 30 and 60 |
| `censer_swing` | 0 rest; 6 staff back over the left shoulder, chest yaw +30°; 16 commit, chest yaw +40°, censers high; 19 mid-sweep; 22 follow-through yaw −35°, censers trailing; 36 rest |
| `censer_backswing` | 0 check; 12 commit; 18 through to yaw +30°; 36 rest |
| `staff_plant` | 0 two-hand grip; 8 staff overhead; 14 to 20 held at the top (the delay); 22 driven down; 24 impact, chest down 25°; 24 to 54 hauling on the stuck staff |
| `offering_grasp` | 0 palm up, drips lengthen; 8 the palm turns out; 18 reach (right arm forward, chest lunges 10°); 23 close; 39 rest |
| `rot_breath` | 0 hood back; 12 vines part; 20 exhale (jaw of the hood down, chest heaves); 50 close; 68 rest |
| `sac_swell` | Sacs swell one by one at 0, 3, 6 ... 21; hunch deepens 4° per sac; 30 release (all sacs collapse, chest snaps up 12°); 42 to 76 spent sag |
| `shelf_volley` | 0 back shelves flex up; 12 staff points; 24 the shelves snap flat on each launch (4 apart); 60 rest |
| `root_snare` | 0 roots pull taut, body leans back; 26 snap forward; 52 rest |
| `censer_orbit` | 0 staff up; 22 censers at full circle; staff turning above the head two revolutions to 62; 88 rest |
| `mould_communion` | 0 kneel; 16 palm to the floor; 16 to 76 slow breathing with the shelves; broken: a 6-tick jerk upright, then the stagger |
| `bloom_of_rot` | 0 sink to the knees; 50 the colony swells; pulses heave the back at 50, 70, 90; 110 to 150 slumped |
| `ignition` | 0 tremor rising; 10 to 30 blackening (char 0 to 0.85, growth to 0.35), chest straightening 18° to 2°, face vines burning back, purifier face forward; 30 release, arms out; 60 into `idle_fire` |
| `purging_sweep` / `purging_return` | As the censer swings, faster and lower, censers on fire |
| `censer_cast` | 0 censer whirled overhead (two turns by 22); 22 thrown; 26 chain taut; 44 reeled back |
| `cauterising_lane` | 0 palm turns down; 26 low sweep across the body; 64 rest |
| `pyre_heart` | 0 straighten, arms opening; 28 core flare, head back; 36 release; 66 rest |
| `pyre_lines` | 0 palm raised; 30 the palm closes; 74 rest |
| `last_purge` | 0 rise on tiptoe, arms out; 40, 56, 72 a pulse per ring; 108 into the Offering |
| `offering` | 0 drop to one knee; 8 palm out and up; held; accepted: a 14-tick recoil (chest back 20°, palm jerking up, the nodule lighting at 2) |
| `spent_sacs`, `staff_caught`, `spent_bloom` | The recoveries above, held |
| `gutter` | 0 slump to both knees; 0 to 60 growths regrow with overshoot (scale 0.35 to 1.1 to 1), char 0.85 to 0, chest back to 18° |
| `ending` | §11 by tick: kneel 0 to 20, burn 20 to 50, the man's face held 20 to 50, soot 50 to 70, sink 70 to 100 |

## 14. Sound

Registered events only; no new audio files. Upper-case names with an `ENTITY_`, `RITE_`, `MAUSOLEUM_` or `ITEM_`
prefix are `SoundInit` fields; the rest are vanilla `SoundEvents` fields.

| Beat | Event | Volume / pitch |
|---|---|---|
| Rise end, pyres go out | `MAUSOLEUM_PUTRICIEL_FLARE` | 1.0 / 0.6, one per pyre, 3 ticks apart |
| Rot idle (ambient) | `ENTITY_ERYTHROMYCELIUM_ERUPTUS_AMBIENT` | 0.8 / 0.7 |
| Hurt | `ENTITY_FUNGLING_HURT` | 1.0 / 0.55 |
| Shamble step | vanilla `SoundEvents.MUD_STEP` + `ROOTED_DIRT_STEP` | 0.7 / 0.8 |
| Censer swing wind-up / sweep | `CHAIN_STEP` / `PLAYER_ATTACK_SWEEP` | 0.9 / 0.7 ; 1.0 / 0.6 |
| Staff Plant impact | `RITE_STAFF_IMPACT` | 1.2 / 0.7 |
| Offering Grasp reach | `HONEY_BLOCK_SLIDE` | 1.0 / 0.6 |
| Rot Breath | `ENTITY_MYCOPHANT_AMBIENT` | 1.0 / 1.3 |
| Sac swells (each) / release | `FUNGUS_PLACE` / `ENTITY_MYCOPHANT_SUMMON` | 0.8 / 0.6 + 0.05 per sac ; 1.3 / 0.8 |
| Shelf Volley launches / landings | `SHROOMLIGHT_BREAK` / `MOSS_BREAK` | 0.9 / 0.7 ; 1.0 / 0.8 |
| Root Snare cracks / eruption | `ROOTED_DIRT_BREAK` / `MANGROVE_ROOTS_BREAK` | 1.0 / 0.6 ; 1.2 / 0.7 |
| Censer Orbit | `CHAIN_HIT` looped per revolution | 1.0 / 0.8 |
| Mould Communion | `RITE_OFFERING_DRAWN` | 0.8 / 0.6 |
| Communion broken | `RITE_CANCEL` | 1.0 / 0.8 |
| Bloom of Rot tell / pulse | `RITE_AMBIENT_DRONE` / `ENTITY_MYCOPHANT_HURT_OTHER` | 1.0 / 0.5 ; 1.2 / 0.6 |
| Kindling at 75 (tremor) | `MAUSOLEUM_PUTRICIEL_CREAK` | 1.0 / 0.7 |
| Ignition | `MAUSOLEUM_PUTRICIEL_CRACKLE` then vanilla `FIRECHARGE_USE` | 1.4 / 0.8 ; 1.2 / 0.6 |
| Pyres relight | `MAUSOLEUM_PUTRICIEL_FLARE` | 1.0 / 1.1 |
| Purging Sweep / Return | `ITEM_LIVING_TORCH_BREATH_LOOP` (one shot) + `PLAYER_ATTACK_SWEEP` | 1.0 / 0.9 ; 1.0 / 0.8 |
| Censer Cast throw / impact | `CHAIN_FALL` / `BLAZE_SHOOT` | 1.0 / 0.7 ; 1.1 / 0.7 |
| Cauterising Lane | `FIRE_AMBIENT` + `ENTITY_STAINED_PRIESTESS_FLARE` | 1.4 / 0.7 ; 0.8 / 0.6 |
| Pyre Heart | `RITE_CULMINATION` | 1.2 / 0.7 |
| Pyre Lines | `MAUSOLEUM_PUTRICIEL_FLARE` per pyre + `RITE_WAVE_START` | 1.0 / 1.2 ; 1.0 / 0.7 |
| Last Purge rings | `RITE_ESCALATE` per ring | 1.2 / 0.7, 0.8, 0.9 |
| Offering opens / accepted | `RITE_CONSECRATE` / `RITE_SEAL` | 1.0 / 0.8 ; 1.3 / 1.0 |
| Offering lapses | `RITE_COLLAPSE` | 0.8 / 0.7 |
| Gutter | `FIRE_EXTINGUISH` + `MAUSOLEUM_PUTRICIEL_CREAK` | 1.2 / 0.6 ; 1.0 / 0.6 |
| Colonisation tier gained (to that player) | `FUNGUS_PLACE` | 0.6 / 0.9 + 0.1 per tier |
| Overgrown | `ROOTED_DIRT_PLACE` | 1.0 / 0.5 |
| Ending: kneel / burn / sink | `RITE_SUBJECT_SINK` / `MAUSOLEUM_PUTRICIEL_CRACKLE` / `RITE_COMPLETE` | 1.0 / 0.7 ; 1.4 / 0.9 ; 1.2 / 0.8 |

## 15. Multiplayer and both paths

**One to four players.** Kindling's damage-driven fill divides by `1 + 0.6 × (n − 1)`; creep does not scale. Shelf
Volley adds two discs per extra challenger (cap 9). Mould Communion's break threshold is 14 per challenger. Offerings do
not multiply: still two per interval, one absolution each, so a group cannot finish in one fire; any challenger's act
counts. Colonisation is per player and does not scale, but Fruiting spreads it, so a bunched group pays. The interval
hunts the most colonised challenger, which a group can use (one player carries the rot and is cleansed in the fire while
the others take the Offering). Faithful cap stays 2.

**Silentium (usually proper).** No deeds. The verbs they own fit the fight: the Living Staff's held Blood Projection
pours into the palm and its held Blood Absorption drains the spent sacs (the staff's ranges apply), Flammeus casts stoke
the Kindling and burn off colonisation, and hallowed ground tolerates them. The intended fight.

**Apotheos (usually forced, burned by hallowed ground).** All three deeds (the door was shattered past every Chapel):
read the glyph, fight beside nothing as the faithful rise, and no Gutter. Hallowed ground drains 4 spore load a second,
then burns 1 a second, so the fight's length is their real enemy; Putriciel's design keeps a forced fight no longer than
a proper one (the Repose deed removes the Gutter's head start, the Litany deed shortens Offerings, but Kindling's pace is
unchanged). Colonisation does not touch spore load: the hallowed ground rejects the hive's will, not fungal matter, and
Putriciel's rot is matter. An Apotheos's sustain (potions, which are only refused in the Gardens; Sanguine Mending; the
spore-load burn on key U while load lasts) carries the fight; the Shelved threshold's heal cut is the threat they must
avoid by cleansing in the fire. Both paths can finish: absolution needs no particular school, only a hit or a projection.

## 16. Tests

**JVM rules tests** (new files under `src/test/java/com/vincenthuto/hemomancy/common/entity/boss/saint/putriciel/`):

| Test | Covers |
|---|---|
| `PutricielKindlingRulesTest` | Fill per damage, the Flammeus ×2.5, drunk fire, opening multipliers, creep per phase, the multiplayer divisor, Communion drain, the 100 trigger queued to the recovery boundary, burn-down 1 per 3 ticks, the pauses, the 66/33 arming |
| `PutricielAbsolutionRulesTest` | An Offering takes one act (hit ≥4 after armour, or 20 projected ticks), the first act closes it, nothing absolves outside one, the hit beats a same-tick projection, the Litany glyph's acceptance and no-three-in-a-row rule, Offering lengths (50, 40, 60), the Last Purge's insertion at four, the fifth ends |
| `PutricielColonisationRulesTest` | Radius and gain per phase, the four thresholds and their effects, decay, Flammeus self-cautery, the purging heat, Overgrown's fall to 12, the ending's clear |
| `PutricielMovesTest` | Every timeline (melee wind-ups ≥12, area ≥20, Rot III's −3 clamped to the floors), every footprint's hit and miss geometry (both cone sweeps by age, ring band and jump clearance, cross, lanes and side lanes, annulus arcs, disc leads, Pyre Lines' converging spokes, the Bloom field's safe cells), phase membership (no fire move in a rot phase, no rot move in an interval), follow-ups and their two-deep cap, every move with a cooldown and none on a cadence |
| `PutricielSelectionDistributionTest` | A few thousand simulated decisions with a scripted challenger visiting every band and sector: no move over a third of a phase's picks; every band has two or more answers per phase; kite and hug pressure move the weights |
| `PutricielArenaRulesTest` | Scorch caps (48 per interval, 160 recorded, oldest first), the Bloom's reclaim of empty ash, the restore list, the lighting table for ROT, CLEANSING and REST |
| `PutricielPaletteTest` | No red texel (hue within −20° to +15°, saturation ≥0.4, red channel ≥70) in `putriciel.png`, `putriciel_glow.png` or `putriciel_charred.png`; `putriciel_embers.png` only crimson, pink-white and soot |
| `PutricielChamberLightingRulesTest` (updated) | The new `Phase` meanings |
| `PutricielModelTest`, `PutricielOfferingGeometryTest` (updated) | The new parts, the 16 px density assertion, the split geometry's bounded meshes |
| `PutricielFireResourceTest` (extended) | The ember atlas |
| `PutricielPresentationGuardrailTest` (rewritten) | No `ParticleTypes` in any Putriciel file; the ember layer returns before drawing in a rot phase; the fire geometry draws only with the interval or ending flag; the dispatcher hook order (kept) |

**GameTests:** `src/gameTest/java/com/vincenthuto/hemomancy/gametest/PutricielFightGameTests.java` (new), namespace
`putriciel_fight_validation`:

1. `rise_puts_out_the_pyres_and_blooms_the_shelves`
2. `rot_phases_never_burn` (every rot move against a real player: no fire damage, no ignition)
3. `kindling_fills_from_hits_and_flammeus_casts`
4. `interval_waits_for_the_recovery_boundary`
5. `offering_accepts_one_hit`
6. `offering_accepts_held_projection` (100 blood paid through `BloodAccounting`, −10 colonisation)
7. `no_absolution_outside_an_offering`
8. `fifth_absolution_runs_the_ending_then_endTrial_once`
9. `ending_survives_a_reload`
10. `colonisation_thresholds_apply_and_clear_on_leave_and_end`
11. `purging_heat_and_fire_moves_cleanse_colonisation`
12. `bloom_of_rot_spares_ash_and_pyre_circles_and_reclaims_empty_ash`
13. `arena_restores_on_end_on_remove_and_on_load_after_end`
14. `litany_deed_glyph_refuses_the_wrong_act`
15. `downfall_deed_raises_faithful_and_the_fire_burns_them`
16. `repose_deed_skips_the_gutter`
17. `two_challengers_scale_the_kindling_and_share_offerings`
18. `silentium_proper_and_apotheos_forced_both_finish` (scripted challengers)

**Review client:** `./gradlew.bat runBossPresentationReviewClient -PputricielFightReview=true`, driven by the rewritten
`src/gameTest/.../gametest/PutricielFightClientReview.java`. Stops (still each): the chamber at rest; the rise's
pyre-out; Rot I idle; each rot move at its tell and its active peak (Censer Swing, Backswing, Staff Plant, Grasp, Rot
Breath, Sac Swell's sacs and ring, Shelf Volley's rings and patches, Root Snare, Censer Orbit, Mould Communion, Bloom of
Rot's field with ash safe cells); the Kindling tremor at 75; Ignition; each fire move; the Offering with 0 to 4 nodules
lit; an accepted Offering's burst; the Gutter; Rot III; a player at colonisation 0, 5, 10, 15 and 20 (HUD bracket and
body); the HUD censer at 0, 50, 100 and mid-burn; the bar frame in each phase; the Litany glyph; a faithful rising; every
ending beat and the restored tomb with its epilogue shelf. Plus one shader-compile launch (any change touching the
`PUTRICIEL_ROT` or flame passes needs a client launch, `AGENTS.md`).

## 17. Build list

All inside Putriciel's lane (umbrella "Lanes and shared files"); shared add-only files are marked; no Gradle in the
Saint stage.

1. Read the kit as frozen by the kit stage (`BossFootprint`, `SaintMove`, `SaintCombatController`, `SaintMeter`,
   `SaintPresence`, `SaintOpening`, `SaintArena`, `SaintPhaseTable`, `SaintDeeds`, `SaintTelegraphRenderer`,
   `SaintMeterLayer`/`SaintMeterStyle`). Where a kit request (§18) is not in, work round it inside
   `common/entity/boss/saint/putriciel/` and record it under "Kit follow-ups" here.
2. Pure rules, each with its JVM test (§16), in `common/entity/boss/saint/putriciel/`: `PutricielPhase` (new enum:
   RISE, ROT_I, ROT_II, ROT_III, INTERVAL, ENDING), `PutricielKindlingRules` (new), `PutricielAbsolutionRules` (new),
   `PutricielColonisationRules` (new), `PutricielMove` (new enum implementing `SaintMove`: timelines, footprints,
   weights, cooldowns, follow-ups, phases), `PutricielArenaRules` (new: scorch caps, Bloom field and reclaim),
   `PutricielEndingRules` (new: the 100-tick beats), `PutricielDeeds` (new); update `PutricielChamberLightingRules`.
3. Rewrite `PutricielEntity` on the kit: controller with `PutricielLocomotion` (new), presence `PutricielPresence` (new),
   the openings, the arena hooks, the deeds, the ending, the 30% health floor and fire drinking, synced data (Kindling,
   absolutions, phase, interval start, colonisation compound, Litany glyph), save and load; delete
   `PutricielCleansingRules`; declare `OFFERING_SOURCE`.
4. The two hazard kinds (rot colony, purging fire, rot breath cloud): use the kit's ground hazard if K-10 lands; else a
   new `PutricielHazardEntity` in the lane with `EntityInit` and renderer-registrar lines (shared, add-only).
5. Damage types `data/hemomancy/damage_type/saint_putriciel.json` and `saint_putriciel_cleansing.json` with death
   message keys (shared add-only lang).
6. Generator: `tools/model_export/putriciel.py` (rescale, legs, purifier face, seams, sacs, nodules, censer lids,
   palette, the two new atlases, the density assertion); rewrite `tools/model_export/putriciel_anim.java` (§13); run
   `python tools/model_export/putriciel.py --preview` to regenerate `PutricielModel.java`, the four atlases and the
   `.bbmodel`. Never hand-edit the outputs.
7. Client: `PutricielRenderer` (scale 2.0, layers), `PutricielAnimState` (move clock, phase clock, new points),
   `PutricielSporeGlowLayer`, new `PutricielCharLayer` and `PutricielEmberLayer` in `client/render/layer/mob/`.
8. Geometry: split `PutricielOfferingGeometry` into `PutricielRotGeometry`, `PutricielFireGeometry`,
   `PutricielColonisationGeometry`; keep the dispatcher hooks; add `PyreFlames.arc` and `PyreFlames.line` (additive);
   new `client/render/world/PutricielPyreFlares` read by `RotPyreRenderer`; sweep `hitGlow` out of `PutricielEffects`.
9. HUD: new `PutricielMeterStyle`; rework `PutricielFrame` (§12.6).
10. Presentation: `BossPresentationIds` and `BossSceneCatalog` lines for `putriciel_purifier_surfaces` and
    `putriciel_rot_returns` cards (shared, add-only).
11. Sounds wired per §14 (no new files).
12. Lang (shared, add-only, keys under `message.hemomancy.putriciel.*`, `boss.hemomancy.putriciel.*`,
    `blood_source.hemomancy.putriciel_offering`, `death.attack.saint_putriciel*`): the narration lines, bar phase names,
    card titles, glyph names, hazard names.
13. Tests: the JVM files of §16, `PutricielFightGameTests`, the rewritten `PutricielFightClientReview` and guardrail.
14. No change to `tools/worldgen/mausoleum/themes/putriciel.py` (§9); if review asks for more pyre circles, add them in
    `chamber_sockets` and re-run the generator for Putriciel only.
15. Hand the docs list to the final review stage: `LORE_REFERENCE.md` §10.4 "Trial / Encounter" and "Ending",
    `SAINT_MAUSOLEUMS.md`, `HEMOMANCY_REFERENCE.md`, `BOSSES.md`, the Saints wiki page (the Saint builder touches none).

## 18. Kit requests

| Id | Request | Why |
|---|---|---|
| K-1 | Telegraph material chosen per move, not per Saint (`SaintMove.telegraphFamily()`) | Putriciel's rot telegraphs must never use fire, and fire telegraphs must be fire |
| K-2 | Damage type chosen per move (`SaintMove.damageType()`), not one per Saint | Rot and cleansing damage read differently in death messages and tooling |
| K-3 | Held Blood Projection and held Blood Absorption aimed at a Saint routed to the Saint (`SaintOpening.onChannel(player, kind, ticks)`, with an optional named body point such as the palm), the blood paid through `BloodAccounting` with the Saint's `Source`. Hooked once in `BloodProjectionItem` / `BloodAbsorptionItem` beside the Vesper hook | The Offering's projection and the Spent Sacs' absorption; Hemorath will want absorption too. Today projection only reaches blocks |
| K-4 | One heal-modifier entry the kit owns (`HealModifier.SAINT_PRESENCE`) that asks the active Saint presence for a multiplier | Colonisation's Shelved threshold; Seraphae's over-heal may use the same |
| K-5 | Phases driven by the Saint's own state as well as health or meter thresholds (`SaintPhaseTable` accepting a phase supplier) | Putriciel's phases are the fire cycle and the absolution count, not health |
| K-6 | Range-band thresholds per Saint | Putriciel's 1-wide body and 15-block floor want HUG 2.5, NEAR 6, MID 11 |
| K-7 | The enrage damage bonus applied to kit move damage | Fixed move damage today ignores the +30% attack floor |
| K-8 | `SaintArena` block edits with a per-move cap, a recorded-count cap with oldest-first revert, a "revert this position" call, and tracked spawned entities removed on end | Scorch, the Bloom's reclaim, the faithful |
| K-9 | A timed ending hook: the Saint runs a saved, untouchable sequence of N ticks, then the kit calls `endTrial` once | The 100-tick absolution ending |
| K-10 | A generic lingering ground hazard (`BossGroundHazard` entity with kinds: radius, life, pulse, effect hook, client draw hook) | Rot colonies, purging fire, the breath cloud; other Saints likely need one. Fallback: Putriciel's own entity |
| K-11 | Footprints: a composite (union of footprints, for Pyre Lines and Staff Plant's disc plus cracks), a field with excluded cells (Bloom of Rot), an annulus with rotating arcs (Censer Orbit) | Shapes the listed set does not cover |
| K-12 | A quarry strategy hook on the controller | The interval hunts the most colonised challenger |
| K-13 | A Saint-entity read for the HUD style (`SaintMeterStyle` given the Saint entity and the viewer) so a second, per-player gauge can be drawn from the Saint's own synced data | Colonisation rides Putriciel's entity data, so no new payload is needed |
| K-14 | Kit follow-up: remove Putriciel's cases from the shared `SaintPowerGameTests` and `SaintEncounterGameTests` | Those files are outside the lane; their Putriciel cases test deleted behaviour |

## 19. Questions for the user

| Question | Default I would take |
|---|---|
| Should an Offering accept held Blood Projection into the palm as well as a hit? | Yes, both; projection also cleanses 10 colonisation |
| Is it all right for the fire to scorch the boss floor to real packed-ash blocks during the fight (restored at the end), so the safe ground in Bloom of Rot is the ground you can see? | Yes, real blocks through the arena, capped and restored |
| Should Putriciel get jointed legs under the robe for the shamble, or keep gliding on the robe and root trails? | Legs under the robe |
| Skipping the Downfall: should the chamber's bog bodies rise as colonised Bog Revenants during a forced fight? | Yes, two at a time, eight in all, burned by the fire |
| Should the purifier say anything when the man shows, or stay silent with narration only? | Silent; narration lines only |

## 20. Calls made

Every decision here that no ruling covers:

- The signature meter is the **Kindling** (the will catching), with the absolutions kept as the bar; the interval is
  triggered by a meter the player fills, with a natural creep so it always comes.
- Fire fed to Putriciel is drunk (0 damage) and stokes the Kindling, and Flammeus hits count ×2.5 (from §10.4: "Flames
  were absorbed into the network of infected tissue, feeding the dormant Flammeus power").
- Two Offerings per interval at Kindling 66 and 33, one absolution each, so five needs at least three intervals.
- What counts in an Offering: one hit of at least 4 after armour, or 20 ticks of held Blood Projection (100 blood) into
  the palm; ranged hits count.
- Health floors at 30% and regrows at each Gutter; it is never the win and never shown.
- Colonisation as the presence, its 0 to 20 scale, the four thresholds and their effects, its decay and cleanses, and
  Flammeus self-cautery; synced through Putriciel's entity data rather than a payload.
- The purging heat (4 blocks) replacing the presence in the interval.
- The interval hunts the most colonised challenger.
- Scorched floor as real packed-ash blocks, the Bloom of Rot's safe ground, and the Bloom reclaiming empty ash; the
  scorch caps.
- The rise puts the pyres out and blooms the shelves; the ending restores the chamber as built (pyres lit).
- Every move, its name, numbers, footprint, cooldown and weight; the four strings; the bands' thresholds.
- Phase names, bar colours and names ("Putriciel, the Purifier" in the fire), two phase cards and their titles.
- The deeds: the Unread Rite (glyph and 40-tick Offerings), the Faithful Rise (bog bodies), It Never Pauses (no
  Gutter, shorter recoveries, Communion only breakable by fire).
- The 100-tick ending and its client-only epilogue shelf.
- Rig rescale by 0.7 at render scale 2.0 for 16 px per block; legs; the purifier's face; eight sacs; palm nodule bumps;
  pierced censer lids; the palette replacements (no brass iron, no red hem, no red censer contents); the charred and
  ember atlases; the char and ember layers.
- The geometry split into three classes; `PyreFlames.arc` / `line` as additive public methods; the pyre flare query.
- The HUD censer and the colonisation bracket.
- The bar frame's five nodules, charring rails and fire-only ember heart.
- Narration lines in pale cream; Putriciel never speaks.
- Every sound pick.
- No chamber generator change.
- Two damage types, neither tagged `is_fire`, so Fire Resistance blunts only the afterburn.
- The rot breath cloud, colony patch and purging fire patch as hazards (kit or own entity).

## Build notes 2026-11-05

Built in run 2's Saint stage, without Gradle (the stage forbids builds; nothing here has been compiled or run). The
generator was run (`python tools/model_export/putriciel.py --preview`): 538 cubes, 176 joints, a 512 by 256 atlas, 16
px per block asserted; the base, glow and charred atlases carry no red texel and the embers atlas only crimson,
pink-white and soot (checked in Python and pinned by `PutricielPaletteTest`). Old files were copied to the untracked
`removed_assets/` first.

### What is built

- **On the kit.** `PutricielEntity` returns its kit from `createKit()` and keeps none of the legacy pieces: no goals,
  no `tickRise`, no `SaintSecondaryAttacks`, `SaintAttackPattern`, `setVisualState` or `sendFloorRing`, no
  `doHurtTarget`, no timers. `PutricielCleansingRules` is deleted. The kit's damage pipeline owns the hits; Putriciel
  overrides only `healthFloor` (30%), `barProgress` (absolutions / 5), `onHitTaken`, `onChannel`, `hazardTick`, the
  arena and phase hooks, `onEndingTick` and `hurt` (the rise check first, then fire drunk into the Kindling).
  `fireImmune()` is true.
- **Pure rules, each JVM-tested** (`common/entity/boss/saint/putriciel/`): `PutricielPhase` (the four kit phases with
  their pace: decision delay 20/16/12/10, creep 12/20/15 ticks, presence radius and gain, turn rate, step cycle),
  `PutricielStage` (ROT, IGNITING, BURNING, EMBERING), `PutricielKindlingRules`, `PutricielAbsolutionRules` (the act,
  the pour, the glyph), `PutricielColonisationRules` (the thresholds and the flag word), `PutricielArenaRules` (scorch
  caps, Bloom safe cells and reclaim, line and disc cells, faithful caps), `PutricielEndingRules`, `PutricielDeeds`,
  `PutricielOpenings`, `PutricielHazard`, `PutricielVisualRules` (the body's look by stage and ending tick), and the
  updated `PutricielChamberLightingRules` (ROT, CLEANSING, REST).
- **Moves.** `PutricielMove`: 11 rot moves, 7 fire moves and the two kneeling beats (`OFFERING`, `EMBERING`), with
  timelines, footprints, telegraph families, weights, cooldowns, follow-ups (the Thurible, Seed and Breathe, Snare and
  Volley, the Purge), recovery openings and damage types. Effects, sounds, hazards and scorch live in
  `PutricielMoveEffects`.
- **The cycle.** The Kindling (`PutricielKindling`, a Saint-owned kit meter) creeps and fills in the rot; full queues
  the interval at the recovery boundary (never in an opening); the forced Ignition relights the pyres, burns the rot
  hazards and starts the heat; the burn-down arms two Offerings at 66% and 33% (the Full Purge first when four
  absolutions stand); at 0 the Embering (or, under Repose, straight back to the rot with 25%); the next rot phase
  regrows health to full.
- **Absolution.** A COUNTED hit of 4 or more raw in an open Offering, or 100 blood poured through
  `SaintChannelRouter.pay` with `OFFERING_SOURCE` at no more than the item's rate (5 at most) a tick; the projector
  loses 10 colonisation; the Offering closes and a 14-tick recoil stagger ends the kneel. The Litany glyph names the
  act. The fifth runs `beginEnding(PutricielEndingRules.SPEC)` (100 ticks, `endTrial` at 100, resumes on load).
- **Presence and colonisation.** `PutricielPresence` (rot gain by phase, no sight; the purging heat at 4 in the fire).
  Colonisation is the kit meter state's secondary, its flag word bits 0 to 8; Rooted (a transient speed modifier),
  Shelved (`SaintHealing` 0.6), Fruiting (self-damage and spread), Overgrown (`SaintRoot`, 6 damage, falls to 12); all
  of it leaves a player who leaves the chamber, and every challenger at the ending, reset or unload.
- **Arena.** Every change goes through `SaintArena`: the rise puts the pyres out (one every 3 ticks, dais first,
  scheduled) and blooms the shelves; the Ignition relights them (every 4 ticks); the Embering puts them out; fire moves
  scorch the boss floor to packed ash under the 48 / 600-tick / 160 budget; the Bloom spares ash, pyre circles and the
  body and then reclaims empty ash; Downfall faithful replace bog bodies (owned, reverted when they die).
  `onArenaReloaded` puts back the stage's lights and re-queues the beat a save dropped.
- **Hazards.** The kit's `BossGroundHazardEntity`, spawned through `arena.own`: rot colony (Shelf Volley landings),
  breath cloud (Rot Breath's end), purging fire (Censer Cast), fire wall (Purging Sweep's end).
- **Locomotion.** `PutricielLocomotion`: the shamble (0.9-block lunges every 16 or 12 ticks, holding the boss floor's
  middle, following a kiter after 60 kite ticks), the stride in the fire, the interval hunting the most colonised
  challenger, still in moves.
- **Model and textures.** `tools/model_export/putriciel.py`: rescaled by 0.7 about the feet (quarter units, no cube
  under half a unit, the staff stretched back to the floor), jointed legs, the purifier's face behind the hollow, the
  eschar seams, two back sacs (eight in all), five palm nodules, pierced censer lids, the chest hunched to 18 degrees,
  the palette pass (blackened iron with olive mould, smouldering ochre censers, no red hem), and the charred and
  embers atlases. `PutricielModel.java`, the four atlases and the `.bbmodel` are regenerated.
- **Animation.** `tools/model_export/putriciel_anim.java`: the stance by look (hunch, tremor, sac pulse), the shamble
  and stride on the legs with the robe following the thighs, and a Catmull-Rom track for every move, each blended in and
  out over 5 ticks; the ending's kneel, burn and sink; growth scale, the purifier's face and the grips.
- **Client.** `PutricielAnimState` (the kit clocks through `SaintMoveView`, the stage clock, the look; a `detached()`
  state for tests and stills), `PutricielRenderer` (scale 2.0, the ending's sink, attachments), the char and ember
  layers (static render types; the ember layer returns in the rot), the spore glow fading with the char.
  `PutricielOfferingGeometry` is now the dispatcher's door over `PutricielRotGeometry`, `PutricielFireGeometry` (the
  Offering in the Riposte layering) and `PutricielColonisationGeometry` (growth on players from `DATA_CHALLENGERS`,
  the presence ring). `PutricielTelegraphStyle`, `PutricielMeterStyle` (the censer and the bracket) and the reworked
  `PutricielFrame` (five nodules, phase fills, charred rails and crimson hearts only in the fire).
- **Lang and presentation** (add-only): 27 keys (narration, glyphs, refusals, phase names, two cards, the blood
  source); `BossPresentationIds.SCENE_PUTRICIEL_PURIFIER_CARD` and `SCENE_PUTRICIEL_ROT_RETURNS_CARD` with their
  `BossSceneCatalog` phase cards.
- **Tests.** JVM: `PutricielKindlingRulesTest`, `PutricielAbsolutionRulesTest`, `PutricielColonisationRulesTest`,
  `PutricielMovesTest`, `PutricielSelectionDistributionTest` (its weights checked against a Python simulation: no move
  over 27% of a phase's picks), `PutricielArenaRulesTest`, `PutricielPaletteTest`, and rewritten
  `PutricielVisualRulesTest`, `PutricielChamberLightingRulesTest`, `PutricielModelTest`,
  `PutricielOfferingGeometryTest` and `PutricielPresentationGuardrailTest`. GameTests: `PutricielFightGameTests`
  (namespace `putriciel_fight_validation`, template `empty`, one batch per test), 13 cases. Review client: the
  rewritten `PutricielFightClientReview` for `-PputricielFightReview=true` (the gradle row already exists).

### Deviations from the design

- Root Snare starts in Rot I, so FAR has two answers in every phase (Shelf Volley and Root Snare).
- An absolving hit and the Kindling read the raw damage (before armour): `MIN_HIT` 4 raw, Kindling +1 per 5 raw.
- Pyre Lines' fronts do not travel (kit `Tether`s are live whole); the move telegraphs each line from its pyre.
- Colonisation rides the kit meter's state and flag word, not a compound of Putriciel's own.
- The Offering and Embering are kneeling moves whose recovery is the opening; an accepted Offering ends with a queued
  stagger. A queued forced Offering cuts the current fire move's recovery short (the kit's boundary rule).
- Teaching lines use `SaintLines.teach` (grey italic action bar); narration is pale cream italic chat to the bar's
  viewers. Faithful drop nothing, through `PutricielFaithfulEvents` (`LivingDropsEvent`, `LivingExperienceDropEvent`).
- The pyre-flare query (`PutricielPyreFlares` read by `RotPyreRenderer`) and the epilogue shelf (ticks 100 to 160) are
  not built; the ending's pyres flare by lighting them through the arena at tick 20.
- The bar frame's 20-tick charring sweep and moss creep are not built (the frame sees no phase-change time).

### Not verified

Nothing compiled, no JVM test or GameTest ran, no client launched. The materials are existing ones (`PUTRICIEL_ROT`,
`FLAME`), so no shader changed.

### Compile-gate follow-ups

1. Remove `putriciel/PutricielEntity.java` from `src/test/resources/architecture/saint-kit-legacy-allow-list.txt`
   (it uses no legacy piece now, so the stale check fails until the line goes).
2. Remove the Putriciel cases from the shared `SaintEncounterGameTests` (`DrivenPutriciel` overrides the deleted
   `openAbsolutionWindow` and `endAbsolutionWindow`; it will not compile), `SaintPowerGameTests` (`boss.isAbsolved()`;
   will not compile) and `SaintPowerRulesTest` (imports the deleted `PutricielCleansingRules`).
3. `SaintRiseTest.everySaintHoldsThroughItsRise` asserts every Saint's `aiStep` calls `tickRise()`; a kit Saint must
   not, and Putriciel no longer overrides `aiStep`. Narrow it to the legacy Saints or to `SaintTrialEntity`.
4. Add a `putriciel_fight_validation` suite row to `gradle/game-tests.gradle` (or the combined gate).
5. Add `SCENE_PUTRICIEL_PURIFIER_CARD` and `SCENE_PUTRICIEL_ROT_RETURNS_CARD` to `BossPresentationIds.SCENES` if that
   list is meant to be complete (no test reads it today).
6. Delete the unused lang keys `message.hemomancy.putriciel.the_rot_stills_absolve_him_now` and
   `message.hemomancy.putriciel.the_window_closes`.
7. Optional, outside the lane: `PutricielPyreFlares` in `RotPyreRenderer` (hot palette, 1.6x height while Pyre Lines or
   the ending flares) and the epilogue shelf.
8. Regenerate `docs/architecture/evidence/source-text-impact-map.csv` (new assertions in
   `PutricielPresentationGuardrailTest`).

### Kit follow-ups

1. `BossBarFrameContext` carries no time since the phase changed, so a frame cannot run a phase-change sweep.
2. A move whose recovery opening closes early (an accepted Offering) keeps its stretched recovery; a
   `controller().endRecovery()` would let a Saint end it without a stagger.
3. `SaintLines.teach` takes a `ChatFormatting`; a style or RGB colour would let Putriciel teach in pale cream.
4. `BossFootprint.Tether` has no travelling front; Pyre Lines would use one.

### Compile gate 2026-11-06

The whole tree compiles (`compileJava compileTestJava compileGameTestJava`) and the JVM suite is green. Shared edits
made at the gate: the legacy Saint pieces are deleted (`SaintSecondaryAttacks`, `SaintAttackPattern`,
`SaintPatternGeometry` and their two tests, `registerTrialGoals`, `sendFloorRing`, the visual-state and secondary synced
fields, `reapplyEnrageEffects`), `createKit()` is abstract, the legacy allow-list is gone, `SaintPowerRulesTest` and
`SaintPowerGameTests` (every case drove a legacy piece; the `saint_power_validation` suite row and its template went
with them) are deleted, and the four fight suites have rows in `gradle/game-tests.gradle` (alpha). All deleted files
are under `removed_assets/` with MANIFEST lines. In this Saint:
- `SaintEncounterGameTests.putricielsPhasesAlternateTheChambersPyresAndSporeShelves` (and its `DrivenPutriciel`) is
  retired; `PutricielFightGameTests.riseTakesTheRoomAndRestoresItOnRemoval` covers the chamber.
- `PutricielOfferingGeometry` used `LuxUmbraBatch.DISCARD`, which does not exist; it now passes
  `FerricDuctilisGeometry.DISCARD`.
- `PutricielColonisationRules.clamp(float)` is renamed `bounded(float)`: `EasingCopyGuardrailTest` treats any
  one-argument `clamp` as an easing copy.
- Both new scene ids are in `BossPresentationIds.SCENES`; `the_rot_stills_absolve_him_now` and `the_window_closes` are
  removed from `en_us.json`.
- Not done: `PyreFlames` and `RotPyreRenderer` stay untouched (resolution E keeps the tongues in Putriciel's geometry);
  the pyre-flare query and the epilogue shelf are still to build.

## Verification 2026-11-05

Run on 2026-11-06 in the verify stage, the only stage running Gradle or a client at the time.

### What ran

- **JVM.** `./gradlew.bat test` filtered to Putriciel, Saint, the architecture package, the boss bar and presentation
  tests, mausoleum, palette and crimson fire: 486 tests, 0 failures at the start. 508 tests, 0 failures after every fix
  below (`build/putriciel-verify-final.log`).
- **GameTests.** `runPutricielFightGameTestServer`: 12 of 13 passed at first, then 13 of 13 after the fix below.
  `runSaintKitGameTestServer`: 8 of 8. The combined gate `runGameTestServer`, which carries the shared
  `SaintEncounterGameTests` in the `hemomancy` namespace, ran 1424 tests with 7 failures, none of them Putriciel's:
  `thelasttenthonlyfallsinsideanopeningandthetrialendsonce`, `preservationfreezeshurtsandthawswithitscap`,
  `killedsaintreturnstoitstombunrewarded`, `velorumendingclosesthetombonceandrewardsonce` and
  `stillguardholdsthefloorsandthebowopensatsixty` (all five are Velorum's), `highhealthrestrictionsrespectapprovedsamplingexceptions`
  (BloodProfile `velorum_devotee`) and `everyhemomancyregistryidmatchesthefrozensnapshot` (the snapshot lacks the kit's
  `saint_*` damage types; only an approved migration may replace it). Log: `build/putriciel-verify-combined.log`.
- **Review client.** `runBossPresentationReviewClient -PputricielFightReview=true`, six rounds. The last one passed every
  contract in `build/boss-presentation-review/operator/putriciel-fight-review.json`: all 18 picked and forced moves
  reached their active span (`moves_never_active` is empty), and every opening was seen (fouled chain, spent sacs,
  offering, embering, broken communion, spent bloom). Also passed: the room taken at the rise, Rot II and Rot III
  reached, a faithful raised, the Litany glyph (POUR in the last round, answered through the router), the ending began,
  the reward was paid once, 0 chamber cells changed after the ending, and the tomb closed.
- **Shaders.** Every client launch compiled clean on this GPU. The log shows only the two existing warnings (the
  `vesper_wound_draw` FogColor uniform and the `rendertype_entity_translucent_emissive` Sampler2), and neither is
  Putriciel's.

### Stills

The final round's 149 stills are `build/boss-presentation-review/screenshots/putriciel-fight-NN-<stop>-TTT.png`, with
earlier rounds kept under `screenshots/putriciel-run1` to `putriciel-run5`. The stops, in fight order:

- The chamber at rest and the rise.
- The room taken: pyres out, shelves bloomed.
- Rot I idle, and a close portrait in the rot.
- The eight Rot I moves at their tell, peak and recovery.
- The Kindling tremor and the Ignition with the purifier card.
- A burning idle, and a close portrait in the fire.
- The five fire moves and the HUD mid-burn.
- The first Offering struck, the second poured, and the Embering into Rot II with its card.
- The Mould Communion, broken.
- The second interval, and the Embering into Rot III.
- A faithful rising, the Censer Orbit and the Bloom of Rot over packed ash.
- Colonisation at 0, 5, 10, 15 and 20.
- The HUD censer at 0, 50 and 100. The 100 still starts the third interval.
- The Full Purge, the fifth Offering under the Litany glyph, the ending at ten beats, and the restored chamber.

The most useful stills to check: `04-portrait_rot-020`, `16-portrait_fire-020`, `14-ignition-034`,
`21-fire_pyre_lines-035`, `23-offering_one_struck-034` and `-041`, `25-embering_into_rot_ii-104`,
`35-rot_bloom_of_rot-079`, `46-ending-026` to `-146` and `47-restored-020`.

### Fixed

- `PutricielFightGameTests.colonisationThresholdsApplyAndClearOnLeaving`: `SaintArena.challengers()` is read once a game
  tick, and the test called `aiStep` in a loop inside one tick, so the player who had left was still listed. The leaving
  is now checked three server ticks later with `runAfterDelay`.
- `PutricielFightClientReview` rewritten on the Hemorath and Seraphae harness:
  - The real `putriciel/chamber` template is placed, and Putriciel rises through `spawnSaintBoss`.
  - A survival quarry has blood and a player-info entry, so the client draws it.
  - The fight runs in order through the three rot phases and three intervals, with Rot III under the Litany and
    Downfall deeds.
  - Cameras sit inside the nave. The first round's cameras were inside walls.
  - The rest of the harness matches the other Saints': captures per scene, Bright gamma, a hidden hand, onboarding
    dismissal, and a block-for-block restore check.
  - The reward check counts loot on the floor and the fragment that a forced awakening pays.
- **Palette: the body read as brass gold in game.** In `tools/model_export/putriciel.py`, the spore growths, shelf caps,
  pale caps, gills, flesh and robe edges move from ochre and gold to pale mould over olive. The base atlas and the
  `.bbmodel` were regenerated; the Java model is byte-identical.
- **`PutricielSporeGlowLayer` and `PutricielEmberLayer`.** `RenderType.eyes` is additive (ONE, ONE) and ignores vertex
  alpha, so the spore glow meant to run at 90 to 130 of 255 drew at full strength and gilded the body. Both layers now
  scale RGB, as the Doe, Fungling, Strider, Mycophant and Cinder glow layers do.
- **`PutricielRenderer`.** Block light now has a floor of 8 (Hemorath's pattern). With the pyres out the body read as a
  black outline.
- **`CrimsonFireClientState.tick`.** It dropped a fresh crimson mark when the entity's synced on-fire flag had not
  arrived yet, so Putriciel's cleansing fire showed vanilla orange on players. Marks now leave only on expiry or when the
  entity is gone, and `isActive` still requires the flag. This is shared code, but it is the only path for crimson fire
  on entities. Verified: the quarry burns crimson in the last rounds.
- **`PutricielEntity.PALM_FORWARD` and `PALM_UP`** move from 0.9 and 1.2 to 0.7 and 2.45. The posed model holds the
  Offering palm up at the brow, measured from the client's attachment. The absolution burst and the pour aim now meet
  the palm the player sees, where before they landed at the waist.
- **The Offering's layers** (`PutricielFireGeometry.offering`) are larger, so they read at fight distance:
  - shell 0.3 by 0.56
  - threads rise to 0.75
  - nodule ring radius 0.3, with cards of 0.07 lit and 0.055 unlit
  - palm flame 0.11 by 0.15

  Strand widths are unchanged.
- **`PutricielTelegraphStyle`.** The rot fill and edge go from 0.16 and 0.5 to 0.2 and 0.62, and the preview from 0.06
  and 0.3 to 0.08 and 0.38. On the moss floor the rot wind-ups barely read. Fills stay at or under the 0.22 cap.

### Still open

- The Offering's layers draw at the raised palm, but they are subtle. The boss's nameplate (shown while the crosshair is
  on it) and the head cover part of the nodule ring from the front. This needs an in-game look.
- Rot world geometry is sparser than the section 12.4 table:
  - Rot Breath has no wisp cards.
  - Sac Swell has no spore-cloud front.
  - Shelf Volley's colony pools and the Root Snare crack ribbons do not show in the stills.
  - Censer trails are short wisp cards.

  The telegraphs carry those moves.
- The Bloom of Rot field (fill 0.1) barely shows. The scorched packed-ash cells read clearly as the safe ground, but the
  sheet does not.
- Colonisation growth on a player (`PutricielColonisationGeometry.players`) did not show on the quarry at 10, 15 or 20.
  The HUD bracket does show beside the censer.
- At distance from above, the body still reads warm khaki. From the front it reads olive and pale mould.
- The ending sinks Putriciel where they kneel, not into the tomb. The tomb closes on the dais afterwards.
- Not built (from the build notes): the pyre-flare query, the epilogue shelf, the bar frame's charring sweep and moss
  creep (a kit hook), and travelling Pyre Lines fronts.
- GameTests still missing from section 16: the Bloom's ash reclaim in-world, both paths finishing with scripted
  challengers, two challengers sharing Offerings, and the arena restore on end and on load after end. The review covers
  the restore after the ending (0 cells changed).
- The faithful (bog revenants) keep the mob's own red-and-green texture.

### For the user to look at

1. The palette pass: `04-portrait_rot-020` against round 2's gold (`putriciel-run2/putriciel-fight-03-rot_i_idle-030`).
   This is my call on "no brass" and is easy to tune in `putriciel.py`.
2. The Offering at the brow (`23-offering_one_struck-034`): whether the palm should stay raised (server palm now
   follows it) or the anim should lower it in front of the kneel, as section 12 first described.
3. Whether the rot moves need the missing world geometry, or whether the telegraphs plus the subtle body motes are
   enough under the subtle-effects ruling.
4. The eschar seams in the fire are vivid crimson and pink stripes at full ember. They stay in the fire palette, but they
   are bold.

## Visual pass 2 (2026-11-06), effects

The user played Hemorath's first build and found the Saint effects plain next to the rest of the mod ("very plain... like
debug lines", ground telegraphs "SUPER basic"). Hemorath's pass 2 was accepted as the direction, and this pass carries it
over to Putriciel in Putriciel's own language, not as a recolour of Hemorath's cracks and iron. Putriciel's stills had the
same gap: every rot move was a flat olive disc or wedge on the moss, and every fire move a flat red one (see "Still open"
above).

The "subtle, partly clear" ruling still holds for overlays on the body and the HUD: the sac drips, the censer wisps, the
Offering's layers and the colonisation growth on players. Attacks, telegraphs, hazards and impacts are now bold, layered,
physical geometry.

The user's trench idea for Hemorath's Iron Rake lanes (a filled, sunken trench instead of parallel cracks) is used here
from the start: every lane-shaped footprint is a trench.

Nothing here is compiled or launched, because this stage could not run Gradle; see "Needs a launch". Another stage redid
the animation at the same time, and it is not described here.

### How it is drawn

- **Their own pass.** `PutricielTelegraphPass` (new, `client/render/world/`) draws every Putriciel move, all four hazards,
  every struck body, and the ending's floor and fire.
  - `ManipulationVisualRenderer` calls it in both passes, through one line added after `PutricielOfferingGeometry.collect`.
  - It reads the same inputs as the kit's `SaintTelegraphRenderer`: the synced move, the `FootprintFrame`, the timeline
    (stretched by a recovery opening) and the reveal tick. Every drawn edge is the hit test's edge.
  - `PutricielTelegraphStyle` now claims every move and every Putriciel hazard and paints nothing, so the kit's flat tints
    and hairline edges never show for them. The pass-1 style and its cream safe-cell tints are in `removed_assets/`.
  - The kit (`TelegraphCanvas`, `SaintTelegraphPainter`) is unchanged.
- **Three layers a frame, each one batch.** The floor (`PutricielRenderTypes.GROUND`) is drawn first. The air
  (`PutricielRenderTypes.SPORE`) comes next, with the fire on the `FLAME` flows. The growths
  (`PutricielRenderTypes.GROWTH`) are drawn in the solid pass. All three render types are built once, on one clock.
- **The floor's own material.** This is `shaders/core/world/putriciel_ground` (`ShaderInit.PUTRICIEL_GROUND`; translucent,
  no depth write). Its patterns snap to the floor's sixteen texels a block, and the vertex colour carries state, not tint.
  It has four shapes:
  - *MAT*, a colonised patch.
    - Blotches of colony take the floor where the per-vertex spread (red) beats a low noise, so the front runs out ragged
      from where the move starts.
    - Each colony has a mould-green heart, ochre, muted yellow and olive growth zones, a pale cream rim, and dark crevices
      between colonies.
    - Rot pools into the low spots (green) as glossy near-black ooze, with a slow shine, cream scum and an ochre meniscus.
    - Cream sporulation specks thicken with blue.
    - A hyphal fringe lies along the front, and the floor stains dark a little ahead of it.
  - *TRENCH*, a furrow torn along a strip (the trench idea).
    - A ragged gap that wanders in width; red opens it.
    - Its walls are banded strata, the far wall lit and the near one in shadow, darkening toward the bottom.
    - Ooze rises in it (green) to 78% of its depth and never brims, with scum and a meniscus.
    - The torn lips are raised and scalloped with pale shelf frills, which brighten as it fruits (blue).
    - A mould stain spreads round it.
  - *SCORCH*, the fire eating the rot.
    - The char spreads the way the colony does (red).
    - The floor goes to soot and charcoal with ash flecks (blue).
    - Alligator cracks glow from ember through crimson to pink-white with the heat (green), white-hot above 0.9.
    - A burning line runs along the char's front.
  - *FURROW*, a burning trench: charred strata with ember seams, and a bed of coals whose gaps glow and throb. Its lips are
    blistered, their crest gone to ash, with a soot stain round them.

  The rot shapes use charcoal olive, olive, mould green, ochre, muted yellow and pale cream. The fire shapes use soot,
  charcoal, ash grey, crimson and pink-white, never green or yellow. The guardrail under Tests pins both palettes.
- **The rot in the air.** This is `putriciel_spore` (`ShaderInit.PUTRICIEL_SPORE`, translucent). Its vertex colour is
  state: red is how thin, green how dark (from pale spore cloud to dark rot smoke), blue the glow. It has no red and no
  fire. It has four shapes:
  - *PUFF*: a billow with a rolling edge, lit from above, with a pale crest and a darker underside. As it thins it frays
    from the inside.
  - *OOZE*: a rounded, glossy, near-black body along a ribbon, with a shine line, cream scum and an ochre edge.
  - *MOTE*: a spore glint.
  - *CURTAIN*: a standing sheet, dense at the floor, with a billowing crest.
- **Growths with mass.** These use `putriciel_growth` (`ShaderInit.PUTRICIEL_GROWTH`). It is solid and depth-writing,
  drawn in the solid pass, and lit by its own facets with a key light, a fill and a rim. `PutricielGrowthGeometry` builds
  real meshes:
  - *Bracket caps*: a thick fan of 30 to 34 quads. Its domed upper face is zoned in ochre, muted yellow, olive and cream
    bands with a pale rim (CAP). Its underside is gilled in mould green and cream, with a scalloped rim (GILL).
  - *Rosettes*: three to six caps in up to three tiers round a seven-sided knot of root. Each tier unfurls a beat after
    the one under it (`PutricielBloomRules.rosette`).
  - *Spore sacs*: lumpy veined bulbs that swell, breathe and tear open from the top (SAC).
  - *Roots*: tapered five-sided tubes of furrowed bark that breach the floor and arch (ROOT).
  - *Clods*: tumbling lumps of loam and fungus flesh (CLOD).

  The vertex colour is state. Red is the char: a burnt growth blackens, then glows crimson to pink-white in its cracks
  (only once the growth round them is black), then goes to cold ash. Green is the wet and blue the bloom. The alpha is the
  formation, so a cap unfurls out from its root and crumbles back from its rim.
- **Fire with body** (`PutricielFlameGeometry`). The fire stays on the Flammeus `FLAME` material the pyres burn: crimson
  body, pink-white heat, black soot tips, with the shader owning the palette. The geometry is built from:
  - bent tongues on `ThermalGeometry`'s spines that lean and lick;
  - rows of tongues along a front, with a height profile;
  - rings of tongues leaning out or in, and a column over the body;
  - soot rising, and sparks on falling arcs, each with a streak behind it;
  - the glow of a hot bed, and a heat flash.
- **The censers', the palm's and the staff head's real paths.** The animation stage's `PutricielAnimState` keeps a
  world-space trail of each, recorded by `PutricielRenderer` every frame off the posed model and keyed by the body's render
  age (`censerAt`, `palmAt`, `staffHeadAt`, on `PutricielTrail`, Hemorath's claw trail made general). The pass reads them
  back at any recent age (`PutricielTelegraphPass.trail`), so no effect can disagree with the animation.
  - The censer wakes, the Censer Drop's arc, the Censer Cast's whirl and the Censer Orbit's smoke follow the animation's
    own censers.
  - The Purging Sweep's fire follows the staff head and both censers through the swing.
  - The Cauterising Lane's low palm sweep trails fire along the palm's path through the last ten ticks of its wind-up.
  - The Offering Grasp's reach leaves the animated palm.
  - An effects-stage recorder (`PutricielTrails`) was written first and retired to `removed_assets/` once the animation
    stage's hook landed.
- **Impacts on struck bodies** (`PutricielImpactGeometry`). These are read on the client from the hurt flash every client
  already has: the body's `hurtTime` and the damage event's source, which must be Putriciel. No packet was added.
  - A rot hit: three bracket caps burst out of the wound along the blow and wither from tick 6. A burst of spores breaks
    off the wound, four gouts of ooze are flung along the blow, clods drop at the feet, and a glint flashes.
  - A cleansing hit (`saint_putriciel_cleansing`): five tongues climb the body from the wound, with a heat flash. Six
    sparks fly along the blow, soot lifts, and the floor scorches at the feet.
  - Timeline: the burst runs from tick 0 to 3, holds to 6, then withers and fades by 10.
- **Bursts.** The bursts the server sends stay, but they are cut to accents wherever the pass now draws the move itself:
  - `CENSER_BURST` is a glint of spores.
  - `IGNITE` (sent by the Ignition and by each Full Purge wave) is a heat glow leaving the body with four sparks, so the
    purge's ring keeps the waves' own timing.

  The pass-1 flat ripples, pools and censer cards in `PutricielRotGeometry` are gone. It keeps the body overlays: the sac
  drips, the motes, the censer wisps and the palm's drips.
- **Pure rules** (`PutricielBloomRules`, JVM-tested). These cover:
  - the life of a rot telegraph (`look`) and of a fire telegraph (`burn`), and the spreading front;
  - rosettes, unfurl, quiver, wither and formation; sac swell and tear; root breach;
  - clods and puffs, the reach, the wake and throw arcs;
  - the tongues' flare and flicker, the shock ring, and a stable hash.

### The life of a telegraph

- **Rot** (`PutricielBloomRules.look`):
  - *Emerge and build* (from the reveal to the commit).
    - The colony spreads from where the move starts (the body, the aim or a pyre) and covers the whole footprint by 65%
      of the reveal.
    - Rot pools in from 25% of the reveal, up to 0.8.
    - The spores ripen from 0.15 to 0.6 (squared).
    - A trench opens from 0.3 of its width.
    - Strength rises over the first 15%.
  - *Flash* (4 ticks): the colony fruits in full (spore 1) and heaves, a trench yawns 25% wider, and the pools fill.
  - *Strike* (the active span): full and wet, with the spores settling to 0.45.
  - *Linger and dissolve* (16 ticks): the pools drain over the first 80%, the spores fade, and a trench closes by 40%
    from 30% in. The whole fades over the last 45%.
- **Fire** (`PutricielBloomRules.burn`):
  - The char covers the footprint by 70% of the reveal, while the heat climbs from 0.15 to 0.75 (squared).
  - It runs white-hot over the 4-tick flash.
  - Through the active span it burns at 0.88, easing to 0.68.
  - Then the embers cool and the ash rises over 18 ticks.
- **Growths**:
  - A cap unfurls to 1.12 over 4 ticks, settles over 4, and quivers 0.09 rad, dying away over 8. It withers over 12: it
    droops, shrinks by half, and crumbles from the rim from 30% of the way in.
  - A sac swells over 10 ticks, tears open over 3 and shrivels over 10.
  - A root breaches over 3 ticks.

### Each move and hazard

| Move or moment | Build (wind-up) | Commit and active | Linger |
| --- | --- | --- | --- |
| Censer Swing, Backswing | The cone colonises out from the body (wet ×0.6). Its outer arc is a shallow trench 0.24 wide. Four to seven bracket rosettes (0.45 to 0.65 across, four caps) unfurl along the arc in the swing's order, through the first 60% of the wind-up. | From 3 ticks before the commit, a wake of spores follows each censer's real path: pale puffs round a dark rot-smoke core, a short glossy smear of ooze just behind the censer, and glints falling off it. Behind the censer's band the swept floor fruits in full. Each rosette puffs as the band reaches it. | The wake frays over 9 ticks, spreading, lifting and thinning. The rosettes wither from 8 ticks after the active span. |
| Censer Drop | The disc at the aim colonises and rot pools into it (wet ×1.0). Five radial trenches tear out to 0.95 of the radius. Five spore sacs swell round it at 0.62, one every 1.5 ticks. The censer's real arc leaves a wake from 10 ticks before the commit. | The sacs tear open one every 0.6 ticks, each with a burst of spores. Ooze is thrown up in seven gouts, arcing 1.4 high. A spore curtain 0.9 tall rolls out to 3.55 over 8 ticks, and seven clods fly. From tick 1, three rosettes (five caps) push up round the crater, which pools full. | The rosettes wither 2 ticks before the Fouled Chain ends; the trenches drain. |
| Offering Grasp | The trench idea: a ragged trench, 0.68 wide each side, tears open along the lane from the body and fills with ooze, with a colonised strip round it. Two-cap frills grow on its lips every 0.9 blocks, facing outward, as the tear reaches them. | The reach: a rounded body of ooze lashes out from the animated palm along the trench over 3 ticks, dipping low over it. It has a pale spore shell, a dark core, a cream scum line along its top, a dark knot and glint at the head, and drops falling off it. It holds through the active span and snaps back over 4 ticks. Spores erupt out of the trench behind the head. | The frills wither 10 ticks after the active span; the trench drains and closes. |
| Rot Breath | The cone colonises (wet ×0.4). Six puffballs swell in it, and spore glints spiral into the hood. | The exhale: twenty billows roll out from the face along the cone, 9 ticks each. They grow from 0.32 to 1.42 across and sink to waist height as they go; a third of them are dark. The floor fruits, and the puffballs burst one after another from the body outward. | The exhale fades over 8 ticks; the lingering cloud is the hazard. |
| Spore Sac Swell | The floor round the body colonises to 2.6. A faint trench ring (0.22) marks the wave's full reach. Spore puffs rise and swirl round the swelling sacs. | The wave: a spore curtain 2.6 tall rolls out from the body to 8, 9 or 10 over 36 ticks. That is the standing band, so it cannot be jumped. Puffs roll at its foot and crest on top, and it thins as it goes. The floor behind it fruits. | The curtain thins and falls over 8 ticks at its reach. |
| Shelf Volley | Each landing colonises faintly (1.5) inside a trench ring 0.16 wide. Shelves torn off the back colony (2.6 up, 0.5 behind the body) tumble through the air to each landing over 10 ticks, on arcs 2.4 high, shedding spores. | Each disc lands on its tick, with a splash of ooze, a rolling burst of spores and six clods. | The standing colony is the hazard. |
| Root Snare | A crack trench races from the body to the quarry over the first 45% of the wind-up. Then the cross tears open from its centre: four trenches 0.48 wide each side, with the floor colonised round them. | Roots breach along all four arms, two arches each (0.55 to 0.85 high, over 3 ticks, staggered by 0.8). Five roots rise round the centre and close over it, 1.55 to 1.75 high. Eight clods fly, and spores burst at the centre and the arm ends. | The roots hold 4 ticks past the active span, then wither and crumble over 12. |
| Censer Orbit | The annulus colonises out from its inner edge. A cream-frilled trench ring at 1.5 marks the eye, the safe ground. | Each censer's arc is a wedge of billowing spore cloud (twelve puffs) that rolls round with three thinning wakes behind it. The floor it has passed fruits, and the censers' real paths smoke. | It fades over 8 ticks. |
| Mould Communion | | Under the palm the floor colonises out to 2.4, and rot pools into it, breathing. Four rosettes push up round the stain. Trains of puffs drift in from twelve bearings at 7.5 blocks and shrink as the palm draws them in. | It withers and fades over 12 ticks. |
| Bloom of Rot | The field colonises cell by cell out from the body. Every corner touching a safe cell (ash, a pyre circle, the body) takes almost nothing, so the mould stops short of the safe ground with its fringe toward it. Twelve rosettes (five caps) and six sacs push up across it from the body outward. | Each pulse (ticks 0, 20 and 40) heaves a ring of fruiting across the field and sends a low spore curtain, 1.1 tall, out to 12 over 10 ticks. Every rosette puffs, and a third of the sacs burst on each pulse. | Everything withers from halfway through the recovery. |
| Ignition | The floor scorches black out to 6 from the body. A burning furrow rings it at 6, with low tongues (0.22 to 0.57) licking along it, and the bed glows at the body. | The purge: a ring of tongues 1.35 tall sweeps out from the body to 6 over the active span, with a lower ring burning behind it at 78% of its radius. Sixteen sparks fly, and the heat flashes at the core. | The tongues die over 12 ticks, and the scorch cools to ash. |
| Purging Sweep | A burning furrow runs along the lane in a scorched strip, with low tongues along it out to the char's front. From 3 ticks before the commit, the staff head's and both censers' real paths trail flame and sparks. | The fire runs out behind the front at 2 blocks a tick, in tongues up to 1.9 leaning along the sweep, each leaping as the front reaches it. Sparks fly off the head. | The tongues die back over 18 ticks; the wall at the end is the hazard. |
| Censer Cast | The target disc scorches and a furrow rings it. The censer's real whirl overhead trails flame and sparks. | A column of fire leaps at the aim (1.9, flaring), and a ring of tongues runs out to the disc's edge over 4 ticks, with twelve sparks and a heat flash. | The column dies over 10 ticks; the purging fire is the hazard. |
| Cauterising Lane | From the lit pyre a furrow burns toward the body through a scorched strip, coals glowing and low tongues along it out to the char's front. Through the last ten ticks the palm's real low sweep trails fire. | The front runs down the path at 1 block a tick: a wall of tongues 4 blocks long, 2.1 tall at its head, leaning along its run. Sparks fly off the head, and behind it the furrow burns low and dies. | The whole path smoulders down over 18 ticks. |
| Pyre Heart | The disc round the body scorches. A furrow rings it at 3.5, and a ring of tongues (0.2 to 0.85) licks up round it, leaning in, while the core's glow swells. | A column of fire erupts from the body to 3.4, flaring. A ring of tongues races out to 3.5 over 3 ticks, with eighteen sparks and a heat flash. | The column dies over 14 ticks. |
| Pyre Lines | From each flared pyre a furrow burns in toward the body, with a tongue running at its head. | Each line stands as a row of tongues 1.6 tall, leaping in from the pyre end. The wedges between stay clean scorched floor, which is the safe read. | They die back over 18 ticks. |
| The Full Purge | The floor round the body scorches. Three ring furrows burn in at 2.7, 5.3 and 8, one after another. | Three waves of fire roll out to 8, 16 ticks apart. Each is a ring of tongues (base 0.7) that settles at about the jump's clearance once its flare has passed. Sparks fly, and the floor chars black behind each wave. | The scorch cools. |
| Offering | A scorched ring (1.9) round the kneeling body, its cracks breathing with the palm's fire. The focal Offering on the palm is unchanged (`PutricielFireGeometry`). | | |
| Embering | | The scorch round the slumped body cools to ash while the rot creeps back in over it from the edge (an inward front). The tongues round the body gutter to nothing over 16 ticks, and soot lifts. | |
| Rot colony (hazard) | | A breathing colonised patch (1.7) with a big rosette (six caps), two small ones and two sacs. It puffs spores on every 20-tick pulse. | The growths wither over its last 20 ticks. When the fire burns it, the growths char black with embers in their cracks and crumble, the patch scorches, and tongues lick over it with soot. |
| Breath cloud (hazard) | | Eighteen billows low over the cone (0.35 to 1.55 up), turning slowly. The floor under it fruits. | It thins from 40% of its life. |
| Purging fire (hazard) | | A scorched disc of coals (2.2) that heats and cools, with two rings of low tongues, a centre tongue and a glowing bed. | It fades with the hazard. |
| Fire wall (hazard) | | A burning furrow with a row of tongues 2 tall standing along it. | It fades with the hazard. |
| Struck body | | See the impacts above. | |
| The ending | Ticks 0 to 20: the rot round the kneeling body darkens. | Ticks 20 to 50: the floor scorches out to 3.6 from the body, while a column of fire (2.7) climbs it with a ring of tongues round the knees and sparks rising. Ticks 50 to 70: the fire dies to ash. | Ticks 70 to 100: spores and glints lift slowly off the body as it rests, and the scorch fades. |

### Tests

- `PutricielBloomRulesTest` (new) covers:
  - a rot telegraph's whole life, and a fire telegraph's;
  - the spreading front;
  - rosettes being tiered, varied and stable over 40 seeds;
  - unfurl, quiver, wither and formation, and sacs and roots;
  - debris that arcs and settles;
  - the reach, the wake and the throw arc;
  - the flare, the shock ring and the hash.
- `PutricielEffectGeometryTest` (new) covers:
  - patches and trenches keeping the ground shader's shape tiles, even round an 18-block ring;
  - the colony being taken behind its front and untouched ahead of it;
  - a safe cell left bare, with the colony thinning toward it;
  - growths drawing nothing before they break out, standing about their scale rooted under the floor, and gone once
    withered or shrivelled;
  - spores, ooze and fire staying in budget, and spent ones drawing nothing;
  - a tongue standing about its height;
  - the style claiming every move.
- `PutricielPresentationGuardrailTest` (extended) checks that:
  - the dispatcher runs the pass in both passes;
  - the style paints nothing and claims every move and hazard;
  - the pass draws all twenty moves and all four hazards;
  - the three shaders are registered and present, their render types are built once, and the growths are solid;
  - no rot constant in the three shaders leans red, and no fire constant leans green or yellow.
- None of these tests has been run.

### Needs a launch (not done here)

- Compile the tree, then run the tests above and the existing Putriciel suite.
- Launch `runBossPresentationReviewClient -PputricielFightReview=true` and look at every move, hazard and the ending.
  This also proves that `putriciel_ground`, `putriciel_spore` and `putriciel_growth` compile on the GPU, which offline
  validators cannot (they miss driver errors).
- Check the growths' lighting in the dark chamber.
- Check that the spore clouds read pale against the moss. They are drawn in their own batch before the sorted flows, so a
  cloud and a fire tongue never sort against each other.
- Watch the frame cost of the Bloom of Rot (about 450 floor cells, twelve rosettes and six sacs) and of the Censer Orbit's
  puffs.
- Check that the censer wakes, the sweep's fire and the Grasp's reach follow the limbs in motion. The animation stage's
  trails keep 64 frames: about 21 ticks at 60 frames a second, but only about 9 at 144. At high frame rates the Censer
  Drop's wake (which reaches up to 21 ticks back) and the longer frays come up short, so the trail would want keeping by
  age rather than by frame count.
- Check the impacts on a real survival player; the review's quarry may not receive damage events.
- Two calls to check by eye:
  - the Full Purge's tongue height against the jump (base 0.7, flickering to about 0.97 once settled);
  - the Sac Swell curtain at 2.6 tall, which should read as a wall to walk out of, not one to jump.
- For the polish stage: regenerate `docs/architecture/evidence/source-text-impact-map.csv`, because the guardrail now
  reads three shaders and two more Java files.

## Visual pass 2 (2026-11-06), animation

The bar is Hemorath's accepted pass 2 (his spec's "Visual pass 2, animation") and Annetta's rebuild: a coil, a fast arc
through the commit, follow-through past the line, a weighted settle, springs on everything that hangs, inertial blends
between tracks and feet that stay on the floor. Pass 1 played one Catmull-Rom curve per part on a window weight, the
staff was held upright in world space whatever the arm did, the censers hung rigidly off it, and the ending's translate
lifted the body instead of sinking it. Nothing here is compiled or launched yet (this stage could not run Gradle); the
stills need the review client.

### What changed in the machinery

- **Layers of channels, not part curves.** A frame is the stance, then the gait, then the playing track as deltas over
  both (81 channels in `putriciel_anim.java`), then the blend, then the springs, then `pose()`. Every move's keys start
  and end at zero, so a move rises out of the stance and settles back into it.
- **The stance follows the look.** Rot and fire are one continuous weight read off the look's hunch
  (`PutricielAnimState.fire()`), so Ignition and the Embering never step. The 120-tick breath, the sacs' out-of-rhythm
  pulse and the weight sway run on phases carried in the state.
- **Tracks and clocks** (`PutricielTracks`, new, JVM-tested). One pure selector picks the track from the replayed
  clocks only (the kit's move, opening and ending through `SaintMoveView`'s `BossReplayClock`s, the rise, the last
  accepted absolution): ending, then rise, then the recoil, then the move (or its recovery opening), then a lone opening,
  then idle. Moves are authored on their base timeline (Rot I or the interval, no deed): a Rot III wind-up cut by three
  ticks plays the same keys faster and still strikes on its commit, and a Repose recovery plays the whole settle in less
  time. The Offering and the Embering keep their own track through their opening and hold to its length.
- **Inertial blend.** Hemorath's `HemorathPoseBlend` (general, called, not moved): a changed track (a new move, the same
  move again by its synced serial, an opening, the recoil, the ending) keeps the gap from the last shown pose and the
  body's velocity, and it dies away over 5 ticks (resolution B-8).
- **Springs.** Hemorath's `HemorathSpring`, 48 slots in `PutricielAnimState`. Loose and wet in the rot, tighter in the
  fire. The hood trails the chest's pitch, twist and lean; the chest dips on through a sudden crouch or kneel; the palm
  forearm and fingers trail the arm and whip through; the staff forearm gives under the staff; the vestment chains
  swing off the chest, the walk and the turn, each link bending a little more than the one above; the robe trails the
  walk and settles forward; the mantles bounce; the back shelves, hem shelves and shoulder shelves wobble; the sacs
  jiggle; the crown's spires and mushroom sway; the drips swing; the ground roots drag round after a turn; a hit kicks a
  recoil that rings out. Each trail is clamped to 0.7 rad.
- **The censers hang on real pendulums.** Each chain's anchor at the staff head is carried into the world's frame (the
  renderer's mirror, the body's yaw and position) and the censer lags it on a spring at the pendulum's own rate, so a
  swung, spun or walked staff swings its censers and they swing past when it stops. Over that the moves tilt them: flung
  out along a swung staff (`CENSER_FLING`), out round a turning staff (`CENSER_OUT`), the left one whirled and thrown
  (`CAST`, `CAST_THROW`), the right one fouled in the floor ahead (`STUCK`). The links stay straight under the weight.
- **The staff is one pole he leans on and swings.** Planted, its iron foot stays where the stance, gait or move puts it
  and the grip slides up to 6 units on the shaft. Lifted, its head points where the move says (`STAFF_TILT` from
  upright toward `STAFF_AZ`, positive to his right, the same sign as the footprints' bearings), and `STAFF_SPIN` turns it
  about its length for the Orbit and the Cast.
- **The hands reach.** A damped least-squares solve on the shoulder and elbow, started from the authored angles, moves
  the staff hand by its grip offsets (across the body, overhead, forward) and the palm to its targets. The Offering holds
  the palm exactly where the server aims the absolution (`PALM_FORWARD` 0.7, `PALM_UP` 2.45). The offering hand keeps
  its world orientation: palm up, turned out at the reach, turned down for the floor and the lane.
- **Grounded.** The knees fold for a crouch and the kneels and the hips sink until a sole or a kneecap meets the floor
  (`PutricielGait.contact`); a lifted foot does not hold him up; the soles stay flat; a kneeling leg lays its instep on
  the floor. The robe panels follow the thighs and flare as the hips sink so the cloth heaps instead of passing through
  the floor. The arms are too short for the hand itself to reach the floor from a kneel, so at the Communion the palm is
  pressed down and its drips run down into the moss.
- **The walk** (`PutricielGait`, new, JVM-tested), keyed to distance so nothing skates. The rot's shamble is a limp,
  one cycle per 0.9-block lunge: the left foot always leads, the weight is thrown over it, the right foot drags up low
  behind, and the feet stand together at each lunge's end; the staff walks with the lead foot and taps down as it lands.
  The fire's stride is an even 1.54-block cycle, knees lifting, shoulders countering the hips.
- **Limb trails for the effects.** The renderer records the palm, both censers and the staff head every frame
  (`PutricielTrail`, new, JVM-tested) at the posed points, the rise's and the ending's sink included:
  `PutricielAnimState.palmAt(age)`, `censerAt(index, age)` and `staffHeadAt(age)`, read like Hemorath's `clawAt`. The
  effects stage's own `PutricielTrails` can read these instead of recording.
- **The renderer.** The ending's sink now goes down (the pose is flipped in `scale`, so down is +Y; pass 1 translated by
  -1.6 and lifted the body a block and a half by tick 100). The attachments include both sinks. `update` is safe to call
  from the model and the world geometry in either order: only the first call in a frame advances the clocks and springs.
- **Rig.** Unchanged: no part or pivot moved, so `putriciel.py` only gained imports; the atlases and the `.bbmodel` are
  byte-identical after the re-run (538 cubes, 176 joints, 16 px a block).

### Track beats

Times are on the authored clock; the commit is the end of the authored wind-up.

| Track | Beats |
| --- | --- |
| Stance, rot | sunk onto the planted staff on soft knees, listing toward it; the hood lolls with a slow sway and ticks between held looks; the palm held low, fingers slowly curling; the eight sacs pulse out of rhythm; the 120-tick breath; a wet shudder through the growths every 70 ticks; from 75% Kindling a tremor |
| Stance, fire | straight, shoulders back, hood level and tracking wider (35 degrees); the palm half open and lifted; a fine steady tremor; the censers swing lit; every 50 ticks the purifier lifts and tests the palm |
| Shamble | left foot lurches out ahead (first 45% of the lunge), the weight thrown over it, the chest dropping and the hips swinging onto it, a jolt as it lands; the right foot drags up low from 60%; the staff swings ahead with the lead foot and taps down |
| Stride | even walk, knees lifting, shoulders countering the hips, the palm arm swinging, the staff walking with the left foot |
| Turn | shoulders and hood lead, hips lag, the staff swings out, the feet shuffle round when he turns in place, the roots drag round after |
| Rise (60) | comes up bowed deep, hood down, arms hanging, the shelves still folded; the shelves unfurl from 0.6 to 1 over 30 to 60; at 44 a heave upright with a shudder, the hood lifting to the room |
| Censer Swing (36) | 0-14 the staff comes off the floor and is drawn back across the body to his right, shoulders winding, censers lifting; 16-22 it whips right to left with the cone, the left foot stamping in, censers flung out along the staff; 22-27 on past his left, off balance; 27-36 down and planted |
| Censer Backswing (36) | 0-11 the staff hauled back across to his left, shoulders winding the other way; 12-18 back left to right, the right foot stepping across; 18-24 flung past his right; 26-36 planted |
| Censer Drop (56) | 0-12 hoisted high and tipped back, the right censer swung up and over; 16-22 held at the top, trembling; 22-26 over his head and down past level, the head driven at the floor ahead, the body lunging after it; 26-50 the censer fouled, hauled on in jerks (the Fouled Chain usually takes this) |
| Offering Grasp (41) | 0-8 the palm comes up, drips running long, fingers spread; 8-18 turns out and draws back to the shoulder; 20-22 thrown out in a lunge, the right foot stepping in; 23 the fingers close; 25-29 hauled back with the catch; 41 rest |
| Rot Breath (68) | 0-19 hood back, vines parting, chest rising, arms drawing out; 20 the head thrown forward and the chest heaving the cloud out; 22-50 the head sweeps the cone, heaving; 54-60 a cough; 68 rest |
| Spore Sac Swell (100) | sacs swell one by one 3 ticks apart with overshoot, the hunch deepening and the trembling building; 30 they burst, the chest snapping up and the arms flung out; 33-90 sagging over the split sacs, heaving (Spent Sacs takes it) |
| Shelf Volley (24 + 4n + 20) | 0-10 the back shelves flex up together; 8-12 the staff raised to point at the quarry; from 24 the shelves snap flat with each launch, four apart, the body jerking with each; then settles |
| Root Snare (52) | 0-6 the roots pull taut and the body leans back against them, the palm closing; 6-25 straining, trembling; 26 snaps forward, the roots released, the palm thrust down and out; 52 rest |
| Censer Orbit (88) | 0-10 the staff up overhead; 4-22 one accelerating turn, the censers swinging out; 22-62 two revolutions with the footprint's arcs (front and back at the commit, clockwise), the body swaying under it; 62-80 slowing, censers swinging in; 74-88 down and planted |
| Mould Communion (96) | 0-10 down on one knee with a jolt, bowed deep, the palm turned over and pressed at the floor, drips running into the moss; 16-76 slow breathing with the shelves, the sacs swelling in turn; 80-96 rises |
| Bloom of Rot (150) | 0-24 down onto both knees, arms spread low, palm to the floor; 30-50 everything swells, shuddering; 50, 70, 90 the back heaves with each pulse, colony snapping up, sacs swelling; 110-144 slumped (Spent Bloom takes it) |
| Ignition (56) | 0-28 shaking harder and harder, drawing in (palm to the chest, head bowed, knees bent) as the growths blacken and the chest straightens; 28-29 a locked beat; 30 everything flings open, arms wide, head thrown back, up onto the toes, past open by 35; 40-56 settles into the fire stance |
| Purging Sweep (38) | 0-18 the staff drawn back low behind him, the weight on the back foot, censers flaring; 20-23 brought over and down along the lane in one long reach, the left foot lunging, censers flung ahead; 23-30 held at full reach; 30-38 hauled back and planted |
| Censer Cast (44) | 0-6 the staff up overhead; 3-22 the left censer whirled round it twice, the body swaying with it; 22-24 the staff chops forward and the censer is thrown at the quarry, a step following; 26 the chain snaps taut and jerks him; 28-44 reeled in |
| Cauterising Lane (70) | 0-10 the palm turns down and is drawn across the body to the left, low, the body coiling; 26-31 sweeps low across to the right as the fire runs, the right foot stepping out; 31-50 held over the lane, trembling in the heat; 60-70 rises |
| Pyre Heart (66) | 0-20 straightens, arms opening slowly, onto the toes; 20-28 draws in tight, arms to the chest, knees bending, shaking; 28-30 flares, arms flung wide, chest out, head back; 30-40 held in the heat, shaking; 66 rest |
| Pyre Lines (74) | 0-18 the palm raised high and open, the head following it up, onto the toes, trembling; 30 the fist closes and drags down to the chest, the body hunching over it; 30-50 held; 74 rest |
| Full Purge (108) | 0-16 up onto the toes, both arms out, head back; 16-40 shaking harder; 40, 56, 72 a jolt with each ring, the heels dropping, arms flung wider; 88-108 down off the toes into the Offering |
| Offering (12 / 1 / its opening) | 0-8 down onto the right knee with a jolt, the staff planted out to the side; 4-12 the palm raised open to the brow, where the server aims the absolution; held, breathing, the palm trembling; rises over the last 8 ticks |
| Recoil (18) | 0-1.5 the chest thrown back and the palm jerked up as the nodule lights, the hood snapping back; a shudder; 6-15 up off the knee |
| Embering (12 / 1 / 60) | 0-7 slumps onto both knees, head dropping, arms slack, the palm rolled over, the staff leaned away; spasms at 15, 30 and 45 as the growths rush back; rises over the last 10 ticks |
| Spent Sacs | sagging forward over the split sacs, heaving, hood hanging and rolling; eases up over the last 10 ticks |
| Fouled Chain | the staff head down at the floor ahead with the censer fouled in it; hauled on in jerks, braced; torn free with a stagger at the end |
| Broken Communion | a jerk upright, the palm torn off the floor, arms flung out; a stagger back two steps, left then right, swaying, trembling |
| Spent Bloom | slumped on both knees in the colony, bowed, sagging with each breath, the hood rolling; hauls itself up at the end |
| Ending (101) | 0-16 the rot stills and the body goes down onto both knees, the palm still raised; 20-28 the chest lifts and the hood tips back as the fire climbs; 26-46 held utterly still while the man's face shows; 50-70 slackens, the hood bowing, the palm lowering; 70-100 bows deep as the body sinks, the staff leaning away |
| Flinch | a spring kicked by a hit: the chest and hood recoil and ring out |

### Tests

- `PutricielGaitTest` (new): the planted shamble feet and stride feet sweep back at the body's speed; each lunge leaves
  the feet together with the lead foot ahead and each foot covering one lunge of ground; the lead foot lifts high and the
  trailing one only scrapes; the thigh reaches its ankle; a kneeling leg touches with its knee and a standing one with
  its sole.
- `PutricielTracksTest` (new): the wind-up stretches, the active span keeps and the recovery stretches; a Rot III wind-up
  strikes on the authored commit; the ending, the rise and the recoil outrank the move; a recovery opening takes the track
  but the kneeling beats keep their own; the volley carries its disc count.
- `PutricielTrailTest` (new): interpolation, nothing outside the kept frames, one record a frame, a rewind starts again,
  the buffer's span and the speed.
- `PutricielModelTest` (pose checks rewritten; the rig and atlas checks kept): an accepted Offering recoils the chest;
  the fire straightens the hunch; the Offering holds the palm at the server's aim; every track keeps the staff one rigid
  pole (planted: pointing at its foot, which stays put while the grip slides; lifted: pointing where the move says), the
  palm up wherever `PALM` is 0, every part finite, and returns to the same idle; at rest the staff stands plumb on the
  floor and the censers hang plumb, bowed or not, and a flung censer swings out along the staff; a crouch and both kneels
  sink the hips with a sole or a knee on the floor, and a swinging foot does not hold him up; every move starts and ends
  at zero; every strike is at least 2.5 times faster than its wind-up and carries on past the commit (thirteen strikes);
  the stance moves from the rot to the fire without a step; the shamble lurches on the lead foot and the stride walks
  evenly.
- Checked numerically before the Java tests were written: the strike speeds and follow-through, and every move's start
  and end at zero (a Python port of the tracks), and the kneel contact and the arm solve's reach (the generator's forward
  kinematics). The Java tests themselves have not been run.

### For the review

- Compile, run the four test classes, then `runBossPresentationReviewClient -PputricielFightReview=true` and look in
  motion: the censers' pendulums (rate 0.28, damping 0.16 in `censerPose`), the shamble's limp, the swing's whip and the
  Orbit's spin against the footprint's arcs.
- Tune points: the spring gains in `secondary()`, the trail clamp (0.7), the grip slide (6), the censer limit (1.45),
  the arm solve's damping (0.8), the kneel angles in `legs()`, `OFFER_HAND`, `FLOOR_HAND`, `STUCK_AT`.
- Not done: the censers do not leave the chain for the Drop's or the Cast's aim (the world geometry carries the burst at
  the aim; the model censer is flung along the chain's reach, about a block); a hit's direction (the flinch always
  recoils backward); the staff hand is solved but its fingers stay as authored.
- Calls I made: the shamble leads with the left foot every lunge (a limp), the Offering kneels on the right knee, the
  Communion kneels on one knee and presses the palm down rather than flat (the arms cannot reach), and the ending's sink
  now goes down where they kneel.

## Visual pass 2 (2026-11-06), polish

The effects and animation stages above were written without Gradle or a launch. This stage compiled them, ran the
tests and the fight suite, launched the review client five times, judged every still against Hemorath's accepted pass 2,
Hematic Riposte and the cast outpouring, and fixed what fell short. The user's words were the test: no debug lines, no
flat patches, telegraphs that read across the floor, attacks with body.

### Tests and launches

- The JVM tests and `runPutricielFightGameTestServer` (13 of 13) passed before any change, and again at the end (see
  "What ran").
- `putriciel_ground`, `putriciel_spore` and `putriciel_growth` compile on this GPU: no shader or render errors in any of
  the five client logs.
- Round 1 and round 4 ran the whole review (62 scenes, 820 stills, `passed` true, every move active, the chamber put
  back block for block, no burn in the rot). Rounds 2, 3 and 5 ran filtered subsets.

### The harness (`PutricielFightClientReview`)

It now matches Hemorath's and Seraphae's:

- Moves are shot every third tick from a 6-tick lead, so the camera has moved and the chunks drawn before the move is
  forced (no black first frames).
- `-PputricielReviewOnly=a,b` keeps the chamber, the rise and the named scenes. It also keeps the Ignition before a
  kept fire scene and the Embering before a kept Rot II or III scene, so a filtered run shows each move in its own stage.
  A filtered Full Purge is forced, because a filtered run has not stood four absolutions.
- `-PputricielReviewParticles=minimal|decreased|all`, default all (it was hard-coded to minimal).
- New cameras:
  - SIDE: three-quarters from the front left, looking between the body and the quarry. Used for the Censer Swing,
    Backswing, Drop, Offering Grasp, Root Snare, Censer Orbit, Purging Sweep, Censer Cast and Cauterising Lane.
  - LOW: a player's eye on the floor. Used for the trenches: the Offering Grasp, Root Snare, Purging Sweep, Cauterising
    Lane and Pyre Lines.
  - FAR: high behind a far quarry. Used for the Shelf Volley and the Root Snare, whose quarry now stands at 10.5 so it,
    not the camera, is the nearer challenger the move aims at.
  - HIGH: under the crown. Used for the Spore Sac Swell, the Bloom of Rot, Pyre Lines and the Full Purge.
- The Gradle properties are in `gradle/review-clients.gradle` (add-only lines).

### What was fixed

- **The colony was a rug.** MAT drew one Voronoi bullseye per cell on a regular grid, the same green heart and cream
  ring everywhere, lit as if it glowed, so every rot move was a bright patterned mat with a ruled edge. It is now:
  - round mould colonies of many sizes, big ones (up to a block and a half) over smaller satellites, with gaps of damp
    floor between them;
  - wobbling, fuzzy rims, and more colonies the further the rot has taken the floor;
  - each colony with its own proportions of green heart, ochre and muted-yellow growth and cream hyphal rim;
  - a dome lit from one side, with a contact shadow round its foot;
  - sparse sporulation specks;
  - ooze pooling in the low spots between colonies;
  - about half the brightness it had.

  Rectangles of colony now thin out ragged to their sides and far end, so the colony never stops on a ruled line.
  (`putriciel_ground.fsh` `moulds()` and the MAT branch; `PutricielGroundGeometry.rect`.)
- **The trench was painted, not sunk.** The trench idea is now carried over properly. TRENCH and FURROW trace the view
  ray into the channel through its mouth, as Hemorath's Iron Rake trenches do (`traceTrench`, from the screen
  derivatives in uniform control flow, walls leaning in 0.3 a block, 0.8 deep a block of mouth):
  - rot walls in strata of loam and rot on the floor's sixteen texels, the lit wall brighter, a lit cut edge, pale
    hyphae hanging out of the cut;
  - the ooze standing in it at most 78% full, glossy, with a cold sheen at a glancing look and an ochre meniscus;
  - fire walls of soot and char with ember seams along the courses, and a bed of coals glowing in their gaps at the
    bottom.

  The flat painted trench stays only as the fallback where there is no frame. A trench now pinches shut at both ends
  instead of squeezing its strata into chevrons (`PutricielGroundGeometry.strip` carries the opening per vertex; the
  shader closes the channel as it falls).
- **The scorch was a web.** One crack network of thin pink lines covered every scorched disc, like a debug overlay. Now
  the char lies in big curled plates whose seams glow only where a low-frequency hot patch lies under them. A cold seam
  is a sooty line, and the pink-white heat comes only near the flash. The ash flecks are fewer and dimmer. A scorch
  rectangle keeps its edge (its char front draws a burning line, which along a side would be a hairline).
- **The fire was spikes.** Every tongue is now a full body (1.75 times the old width) with a hotter core licking up
  inside it on its own sway and lean (`PutricielFlameGeometry.tongue`; still inside the geometry test's budget).
  - The Purging Sweep's fire fills the lane: two lower rows of tongues lick off both lips beside the tall file down the
    middle.
  - Each Full Purge wave carries a band of white-hot floor under its crest and a second, lower ring of tongues just
    behind it. Seen from above it reads as a burning front, not a hairline circle, and the tongues still settle under
    the jump.

### Contact sheets

- One per scene, its frames in order: `build/putriciel-pass2/sheets/<scene>.png` (62 sheets: the full round-4 run,
  with round 5's re-shot scenes in place of round 4's). Each round's own sheets are in
  `build/putriciel-pass2/round<n>/sheets/`.
- Putriciel next to the bar: `build/putriciel-pass2/compare-pass2-riposte-hemorath.png`. It has rows for Riposte,
  Hemorath's pass 2b, Putriciel at round 1 and Putriciel after polish.
- The sheet builders are `build/putriciel-pass2/sheets.py` and `compare.py`.

### Where it falls short

- The rot reads as colonisation now, but from the boss camera a colonised footprint is still mostly a floor pattern.
  The growths that rise out of it (bracket rosettes, sacs, roots) are small against a 10 to 12 block field and get lost
  in the Bloom of Rot. Hemorath's iron has more mass in frame than Putriciel's fungus does.
- The trenches are now sunk, but they are narrow (the Offering Grasp's mouth is about 0.7 a side). From a player's eye
  they read as a dark cut with a lit far wall and frilled lips, less boldly than Hemorath's flooded lanes. The ooze in
  them is dark by design and so carries little light.
- The fire has body at eye height, but seen from the high cameras a ring of tongues still reads partly as a toothed
  circle. The Pyre Heart's wind-up ring is the plainest.
- The Censer Drop's and the Censer Cast's own arcs are small in every frame. The Shelf Volley and the Root Snare are
  legible from FAR but small.
- The ending's floor scorch is hidden under the body and its fire column from the ending camera.
- Motion was judged only from 3-tick strips:
  - the censers' pendulums and the shamble look plausible in the side strips but were not watched in real time;
  - the body is small in most move frames, so the animation stage's beats (coil, whip, follow-through) are not shown
    well;
  - a closer three-quarters camera per melee move would be the next harness step.
- Not checked:
  - frame cost: the traced trenches add a per-pixel trace, and the Bloom of Rot draws about 450 floor cells of the
    heavier colony shader;
  - first person when struck;
  - a real survival player's impacts (the quarry is a mock player);
  - multiplayer.

### What ran

- `gradlew test --continue`: 5454 tests, 0 failures, 0 errors, 0 skipped (`build/putriciel-pass2/final-test.log`). No
  allow-list was widened. The source-text impact map was regenerated (3639 rows; no change from this stage).
- `runPutricielFightGameTestServer`: all 13 required tests passed (`build/putriciel-pass2/final-gametest.log`).
- Five client rounds: `build/putriciel-pass2/round1.log` to `round5.log`.

## Revision build notes (server)

Built 2026-11-06 in the SERVER lane of the revision (R2, the server half of R4), beside the MODEL and EFFECTS lanes.
No Gradle ran and nothing was compiled: every claim below is from reading the code, not from a run.

### What is built

- **`PutricielSludgeRules`** (new, pure, `common/entity/boss/saint/putriciel/`). The beats of contract 2 (`CATCH` 6,
  `BOIL_END` 30, `ASH_AT` 44, `BURN` 50, `COOL` 60, `SETTLE` 30, `FORM` 8, `RUNNER_SPEED` 0.5, `COLONY_BURN` 30,
  `ENDING_BURN` 10, plus `ABSORB` 12). Which moves lay (`LAYERS`, `lays(move, stage)`, `volleyPool`, `graspPool`,
  `snarePool`). The `Patch` record (a disc, a runnel or a colony patch) with its rim, gap and probe geometry.
  Placement (`place`: the boss floor with clipping, the pyre circles, this fight's ash, the 40% overlap splash). The
  cap (`poolCap`, `areaCap`, `settleCount`, oldest first). The Bloom's drain spots (`bloomDrains`). The ash cells
  (`ashCells`). The live-source tests (`touched`, `bodyTouches`). The catch schedule (`catchSchedule` with its
  `Schedule`, `Catch` and `Source`): the Ignition ring, a runner from each relit pyre, runners on from every burning
  pool (Dijkstra over the catch ticks), the fallback at 140, and `Schedule.light` for a live source, which only brings
  a catch forward and sends that pool's runners on early.
- **`PutricielHazard`**: `ROT_SLUDGE` (5: life 2400, pulse 5, no damage, +0.5 colonisation), `SLUDGE_FIRE` (6: life
  110, pulse 10, 1 cleansing, -2 colonisation, harmless from age 50) and `FIRE_RUNNER` (7: harmless, its life set per
  spawn). `harmless(age)` and `catches()` are new.
- **`PutricielMoveEffects`**: pools on the Volley's landings 0, 3 and 6 (instead of their colony; a landing that
  cannot pool keeps its colony), the Censer Drop's crater at its commit, every other Grasp's runnel (even synced move
  serial) and a Snare that landed no hit (both at the last active tick), and the Bloom's two drains at its last pulse
  (active age 40). Every fire move but the Ignition tests its live footprint against the pools each active tick.
- **`PutricielEntity`**: `laySludge` (placement, the cap's settle, colony absorption, the forming sounds, the
  teaching line), `tickSludge` in `tickSaint` (the safety settle, the schedule, the slow, the burning pools),
  `burnRotHazards(relit)` (the breath clouds flash off; the schedule starts with the pyres in relight order),
  `lightPatch` (a pool expires over `BURN` and a `SLUDGE_FIRE` with aim = the catch point and target = the source takes
  its footprint; a colony expires over `COLONY_BURN`; the slow ends that tick), the runners, the ash at `SLUDGE_FIRE`
  age 44 under `putriciel_sludge_ash`, the boil's lava pops, the ending's fast burn, the body fire's crackle every 20
  ticks, the sludge modifier `hemomancy:putriciel_sludge` released with colonisation's other modifiers (leaving,
  death, logout, end, removal), `catch_clock` saved, and test accessors (`sludgePools`, `catchClock`; `scorch` is
  public now). The Bloom no longer counts cells under a live pool as safe.
- **`PutricielArenaRules`**: the second budget (`SLUDGE_ASH_BUDGET` `putriciel_sludge_ash`, 8 a pool, 24 a 600-tick
  window, 64 recorded) and a `safeCells` overload that drops pooled cells.
- **`PutricielLocomotion`**: in the rot it holds in the nearest live pool or colony patch within `HOLD_RADIUS` +
  `ROT_PULL` (4) of the floor's middle, standing within `ROT_STAND` (0.75) of its centre.
- **Lang** (add-only): `message.hemomancy.putriciel.sludge_taught`, `message.hemomancy.putriciel.sludge_burns`.
- **Tests.** JVM: `PutricielSludgeRulesTest` (new: the lay rules, placement and clipping, the splash, the cap and its
  settle order, every catch source and the earliest-wins rule, runner timing, the 8-block pyre reach, colonies taking
  no runners, the fallback by 140, the ash cells and their budget, the ending's catch point, the frame contract, the
  hazard kinds). `PutricielMovesTest` (only the five rot moves lay, never in an interval stage). `PutricielArenaRulesTest`
  (the second budget; pooled cells are never safe, their ash is). GameTests in `PutricielFightGameTests` (add-only):
  `sludgeSlowsAndColonisesAndClearsOnLeave`, `sludgeWaitsForTheFireThenBurnsToAsh`, `sludgeCapSettlesTheOldest`,
  `sludgeNeverLiesOnAshOrPyreCircles`, `sludgeAshRestoresOnEndRemoveAndLoadAfterEnd`; `rotPhasesNeverBurn` now also
  asserts no `SLUDGE_FIRE` or runner and no catch clock in the rot. The new tests drive the hazards with a `run` helper
  that advances the Saint's and each owned hazard's `tickCount` as the level would.
- **Review client** (`PutricielFightClientReview`, add-only): `palette_portrait_front`, `_three_quarters` and `_back`
  (Rot I); `sludge_rot` (SIDE) and `sludge_rot_low` (LOW) after the rot moves, laying one pool of each laying move's
  shape on top of what the forced moves left; `sludge_burn` (HIGH, every third tick, 120 ticks) straight after the first
  Ignition; `fire_purging_sweep_body_fire_motion` and `body_fire_motion_stride` (SIDE); `body_fire_ignition` (a close
  three-quarters camera, every 4 ticks) as the second interval's Ignition, then `body_fire_back` (BACK) in its steady
  burn; `embering_dieback` (every 4 ticks) as the second interval's Embering. The report gains `sludge_laid`,
  `sludge_pools`, `sludge_burning`, `sludge_runners`, `sludge_left_after_burn` and `embering_dieback`. No new Gradle
  property.

### The contract the client lanes read

- A pool's frame origin is its centre. A disc pool is a `BossFootprint.Disc` at `ORIGIN`. A runnel is a whole-length
  `BossFootprint.Path` (walkers 0) through `frame.points()` = its two ends, with the origin at their midpoint and the aim
  at its far end. `PutricielSludgeRules.patchOf(shape, frame, colony)` turns either back into a `Patch`.
- A `SLUDGE_FIRE` carries the pool's own shape and frame with `aim` = the catch point and `target` = the source. One
  with `life()` = `ENDING_BURN` (10) is the ending's fast burn; the normal one lives `FIRE_LIFE` (110).
- A `FIRE_RUNNER` is a `BossFootprint.Lane(length, 0.3, 0, 0, 0, 0.5, 0, STANDING)` from its origin (the source) to its
  aim (the catch point); its front is `Lane.front(age, 0)`, and it lives its travel plus 30.
- Fades tell the endings apart: a pool that catches fades over `BURN` (50), one that settles over `SETTLE` (30), one the
  ending expires over 10 (the kit's); a colony patch that burns fades over `COLONY_BURN` (30), one a pool absorbs over
  `ABSORB` (12).

### Deviations and calls

- **The runnel is a `Path`, not a `Lane`.** A `Lane` measures from its frame's origin along its aim, so it could not
  keep "a sludge's origin is its centre" nor let a burning runnel's aim move to the catch point. A whole-length `Path`
  through the two ends does both, and the Cauterising Lane already draws one.
- **The pyre circles clip as the ash does.** A pool is cut back to 1.5 from each pyre's centre and dropped under radius
  1.0, instead of being refused whenever its full radius would reach the circle. The circle stays clean either way.
- **The cap is literal.** Six Volley pools (radius 1.5) come to 42.4 square blocks, just over 42, so the sixth Volley
  pool settles the oldest by area; six pools stand together only with smaller or runnel pools in the mix.
- **The schedule saves only its clock.** A load of a fighting Saint restores the chamber and discards every owned
  hazard (the kit's contract), so no pool survives to be scheduled. `catch_clock` is saved and the schedule is rebuilt
  from whatever patches stand when it is missing, with late catches landing at once.
- **Runners go to pools only.** A colony patch catches from the ring, the body, a fire move or the fallback, and sends
  no runner on.
- **The splash** onto an older pool is a `ROT_IMPACT` burst and a honey place sound at its rim, since no new burst kind
  was wanted.
- **The Grasp's runnel and the missed Snare's pool** are laid on the last active tick, after the move's strikes; a hit
  on that same last tick is not seen by the Snare's rule.
- **A burning player** is any challenger `isOnFire()` in the interval, not only crimson fire (the server cannot tell
  the two apart).
- **The Bloom's pulses do not stack on pools**: while the Bloom is active, a pool's pulse adds no colonisation.
- **Body fire crackle** plays on `stageAge % 20` in IGNITING after the commit and in BURNING (not the Embering).
- **Review-client order.** One Ignition and one Embering run per interval, so the body-fire Ignition stop takes over
  the second interval's Ignition (`ignition_two` becomes a still of the burn), the die-back stop takes over the second
  Embering (`embering_into_rot_iii` becomes a still of Rot III), and `sludge_burn` follows the first Ignition instead of
  containing it (the ring's catches fall inside `ignition`'s MID shots). The BACK camera is a still of the steady
  burn. `-PputricielReviewOnly=embering_dieback` alone does not reach the fire: run it in the full review.

### Not verified

Nothing compiled; no JVM test, GameTest or client ran. Needs the verify stage: `compileJava compileTestJava
compileGameTestJava`, the JVM suite, `runPutricielFightGameTestServer` (5 new tests, `rotPhasesNeverBurn` extended), and
the review client for the new stops. The source-text impact map needs no regenerate (no guardrail reads a new file).

## Revision build notes (model)

Built in the revision's MODEL lane, without Gradle or a Java build. The generator ran
(`python tools/model_export/putriciel.py --preview`): 560 cubes (538 before), 182 joints, a 512 by 512 atlas (it was
512 by 256; the new cubes pushed the layout down a row band), 16 px per block asserted, 70 fire anchors, 48 growth
parts. The four old atlases, the old generator, pose code and generated model are under `removed_assets/` with
`.pre-revision-2026-11-06` in their names and MANIFEST lines.

### What is built

- **Colour (R3), in `tools/model_export/putriciel.py`.** Every material from the R3.1 table: rot-dyed olive-brown robe
  panels with every other rot-dyed panel deep mould green, ochre tide marks at noise-wobbled heights, a charcoal hem
  over the lowest three texels and the robe darkening toward the floor by world height; ivory to pale cream vestments
  (collars, mantles, hood rim, robe panels 0, 4, 8, 12) with ochre stains and mould-green bloom at their edges; pallid
  grey-cream flesh mottled with olive, mould-green veins and dark eschar with scab rims, darker inside the hood; sacs
  graded cream crown, muted yellow middle, ochre base, each shifted by its seed, with olive veins; shelf tops in
  concentric zones by three schemes (ochre-led, olive-led, pale-led; neighbours never match) with a pale cream rim on
  the brim and a shaded underside; ochre pores and mould-green blooming roots under the shelves; ivory mushroom caps
  with ochre freckles on cream stems (new `stem` material); glossy near-black ichor with one wet highlight per drip
  column; dark bark roots with ochre fibre on top; blackened iron with olive mould on the edges of wide faces only and
  rare single ochre flecks (the first pass put a fleck at every small face's corner, which read as brass beads on the
  chains and crown). New materials: `staff_wrap` (rot-soaked rag in diagonal turns), `moss` and `stole`.
- **New geometry.** `staff_wrap_0` to `_3` are their own parts on the staff (the four upper rings, with a loose rag
  tail each; the ring at 9 and the foot ring stay iron); `stole_left` / `stole_right` run down the chest from the
  collars to the belt and the stole band carries on down the front robe panels 6 to 8; moss tufts sit at the roots of
  back shelves 0, 2, 4, 6, 8 and 10 and two on each mantle.
- **Measured palette** (the generator asserts each, and `PutricielPaletteTest` reads the same rules from the atlas):
  dark 30.6% (target 25 to 35), mid 43.2% (40 to 50), light 26.2% (20 to 30), ochre and muted yellow 10.6% (15% at
  most), mould green 6.3% (5 to 10), no red texel in the base, glow or charred atlas, no warm texel between hue 15 and
  30. Luminance is Rec. 709 on the stored values; ochre is hue 30 to 58 with saturation and value at least 0.45; mould
  green is hue 78 to 150, saturation at least 0.25, value at least 0.2 (the olive mould on iron, hue 72, is olive).
- **Char atlas (R3.3).** Blood cloth on the robe scorches by height, fully at the hem; the vestments scorch only at
  their edges and stay pale ash cream (196, 188, 168) inside, so the man's vestments show pale through the fire.
  Moss, wraps and stems char with the growths.
- **Burning atlas** (`putriciel_burning.png`, crimson, pink-white and soot only, 6036 texels): shelf and cap rims with
  pink-white flecks, the char's alligator cracks glowing pink-white on the caps (the same noise as the char atlas, so
  they line up), the wraps in crimson streaks with hot cores, the shaft iron heated for a unit round each wrap, the
  censer lid bars' inner faces and the coals, the top 1.5 units of each crown spire (its last half unit white), the
  robe panels' lower edges and the sac tears. Gill edges get a few crimson texels.
- **The fire tables** (generated fields in `PutricielModel`): `ANCHOR_*` (part, point and normal in the part's units,
  steady height in blocks, region, palm arm, global kindle order and kindle tick), `GROWTH_*` (part, kindle tick,
  kindle step, Embering regrowth rank, eaten), `REGION_PARTS` / `REGION_OF`, and generated methods for each anchor's
  part chain, each growth's ancestors and tree, and each region's parts. Kindle ticks follow R1.2: the chest tears
  and the grip wrap at 16, the staff up from the grip wrap at a tenth of its upper length a tick (down at half that,
  capped at 28), shoulders 18 to 19.4, the colony's root at 20 and the back by distance from it to 28, censers 22,
  elbows 23.5 to 24, crown spires 24 to 29 one a tick, head shelves and crown mushroom 26.5 to 28, hem 26 to 27.25,
  knuckle 26.
- **`PutricielFireAnchors`** (contract 1): the `Region` enum (same order as `PutricielBodyFireRules.Region`, with
  `rules()`), the `Anchor` record as the contract names it plus `at(origin)`, `place()` (kindle rank within its region,
  0 to 1, as `PutricielBodyFireRules` reads it), `palmArm()`, `bodyRegion()`, `height()`, `speed()`, `kindleTick()` and
  `flame(moment)`; static `kindleOrder(part)`, `kindleTick(part)`, `regrowRank(part)`, `regionOf(part)` and
  `selfLight(...)`.
- **`PutricielAnimState`**: `fireAnchors()` (an unmodifiable `List`, empty until the body has drawn once),
  `recordFireAnchors(...)` with velocities from the render-age step eased over half a tick (a rewind or a gap over 5
  ticks restarts at rest, a jump over 2 blocks a tick is never a velocity), `fireMoment()` (the
  `PutricielBodyFireRules.Moment` for this frame: Repose from the synced deed mask, the move's commit and active span
  from its timeline, the last hit by render age), `kindleOrder(part)` and `selfLight()`.
- **Pose code (`putriciel_anim.java`).** `burn(m)` scales every growth part by `PutricielBodyFireRules.growth` over
  what `growths` set (the rise and the Bloom still multiply), so each shelf, sac and mushroom shrinks as the fire
  reaches it, the two outermost back shelves and the back mushroom are eaten to 0.2 with the Kindling, and the Embering
  regrows the colony from its root outward and the arms and crown from their tips inward, three ticks a rank; the rot
  drips shrink with their mushrooms. The shoulder shelves, elbow and knuckle growths and staff growths now burn too
  (they never scaled before). `fireAnchors(points, normals)` walks each anchor's chain like `core()`.
  `renderCharred` draws each growth with its children at its own `charAt`, and the rest at the look's char.
  `renderBurning` draws the model once per region with every other part `skipDraw`n. The Ignition track gained the
  catches: a jerk at 16 as the chest tears catch, the shoulders hunching at 18, the back arching off the fire at 20 with
  the colony's shelves flaring up, the head ticking up with each spire from 24, and the flare's breath at 33 to 42.
- **Renderer and layers.** `PutricielRenderer` records the anchors after the body draws (sink included, normals
  flipped and turned with the body), raises the block-light floor from 8 to 13 with `selfLight` (the Ignition's 10 to
  30, the burn, the Embering to 30, the ending's fire beats) and adds `PutricielBurnLayer`: a static `RenderType.eyes`
  on `putriciel_burning.png`, returning first in a rot phase, then on `PutricielBodyFireRules.drawsLayer`, each
  region's strength `PutricielBodyFireRules.region(...)` times its seeded `flicker`, in the colour. `PutricielCharLayer`
  returns in the rot and draws through `renderCharred`.
- **Tests (not run).** `PutricielPaletteTest`: the burning atlas is fire only, the three value bands, the brass guard
  and no warm texel under hue 30, mould green 5 to 10%, the burning atlas shares the layout. `PutricielFireResourceTest`:
  no green or yellow texel in the embers and burning atlases. `PutricielModelTest`: seventy anchors, 12 / 2 / 19 / 15 /
  9 / 6 / 7 by region, each on its region's part with a unit normal and its own kindle order, kindle ticks 16 to 29;
  the staff's anchors stand on the shaft and kindle up it from the grip wrap; the stole, wraps and moss exist; every
  growth is 1 in the rot, 1 before tick 10, 0.35 by the commit, 1 and clear by the Embering's tick 60, the root shelf
  before the outer shelf, the outer shelf eaten to 0.2, the root regrowing first, the knuckle before the shoulder; the
  posed model burns the root shelf before the outermost and stands every back shelf at 1 at the Embering's tick 60; the
  anchors' velocity from the render-age step and the rewind.

### Deviations and calls

- The growth stagger reads `PutricielBodyFireRules.growth` / `charAt` (EFFECTS) rather than a copy: a growth's
  `kindleOrder` is its kindle step (its kindle tick less 16, 0 to 12), so `blacken` takes it from tick 10 + step to its
  kindle tick, the six ticks before the fire reaches it. The Ignition's growth therefore runs on the Ignition move's
  clock, while the cloth's char still follows the look on the stage clock.
- `Anchor.size` is the anchor's place in its region's tongue range (0 to 1, as `PutricielBodyFireRules.Region.height`
  reads it), fitted at class load from the generated heights in blocks; `height()` gives blocks. `Anchor.order` is the
  whole body's kindle order; `place()` is the in-region fraction.
- The burning layer draws per region at the middle anchor's fire (`PutricielBodyFireRules.region`), so within a region
  the climb is carried by the flames and the char, not by the layer.
- The ochre and green families are my reading of R3's words, pinned the same in the generator and the test.
- Hood rim and face lit from below (R1.1, CROWN) is only the raised light floor; no burning texel is on the hood.

### Not verified

Nothing compiled and no test ran. The verify stage should compile, run `PutricielModelTest`, `PutricielPaletteTest`,
`PutricielFireResourceTest` and the rest of the Putriciel suite, and launch the review client to look at the colour
in game (the preview renderer has no lighting model), the burning layer's strength at full fire, the char stagger at
the Ignition and the regrowth at the Embering. Points to look at: the flesh mottle may read busy at fight distance;
the robe's stole band; whether the burning layer needs a lower ceiling than 1 on the BACK region.

## Revision build notes (effects)

Built 2026-11-06 in the revision's EFFECTS lane (R1's presentation, R2.4 to R2.6 on the client, the shaders), beside the
SERVER and MODEL lanes and against their contracts as they landed. No Gradle ran and nothing was compiled. The three
changed shaders pass `glslangValidator` offline with their includes expanded; that does not prove they compile on a
driver.

### What is built

- **`PutricielBodyFireRules`** (contract 3, new, pure, `common/entity/boss/saint/putriciel/`). The `Region` enum (STAFF,
  CENSER, BACK, SHOULDER, CROWN, CHEST, HEM) with each region's steady-burn tongue range (R1.1), its kindle window on the
  Ignition's clock (R1.2: the chest at 16, the staff 16 to 26, the shoulders 18 to 26, the back 20 to 28, the censers 22
  to 25, the crown 24 to 29, the hem 26 to 28) and its Embering out window (R1.6: the hem, crown and arms first, the back
  from its outer shelves in, the staff head last at 30). `Moment` (stage, stage time, Kindling, Repose, the move with its
  commit and active span, the opening, ticks since the last absolution and the last hit, the ending's tick) and `Flame`
  (tongue, bed, smoke, heat). `anchor(region, place, palmArm, moment)`: smoke four ticks before the catch, the 0.6 kindle
  over five ticks, the locked beat at 28 (drawn in to 0.7, cores white), the flare to 1.45 at 30 settling to 1 by 42
  with the 0.92 breath at 36; the steady burn at 0.6 + 0.4 x the Kindling; the last 6% guttering under Repose and
  faltering to half without it; every peak of R1.3 (the Purging Sweep, the thrown censer, the palm arm in the Lane and
  Pyre Lines, the Pyre Heart's draw-in and 1.6 flare, the Full Purge's rings, a hit); the Offering's draw-down to 0.55
  with the palm at full and the accepted pulse up the arm and across the back; the Embering's gutter by 10 and embers
  out by 30; the ending's climb from the core over ticks 20 to 32 and out by 70. Also `column` (the back column, 2.2 at
  the commit, 2.6 at the Pyre Heart), `region` (the burning layer's alpha), `heat`, `drawsBody` (IGNITING, BURNING, the
  Embering under 45, the ending's 20 to 70) and `drawsLayer` (the same, the Embering under 30), `tip` and `clampLean`
  (up + a quarter of the normal - 1.6 x the velocity, within 70 degrees), `stretch` (up to 1.8 at 0.35 a tick),
  `sheds` (over 0.25), `armFlare` (a fast arm to 0.6), `flicker` (within 12%), and for the model `growth`, `charAt`,
  `blacken` and `regrowth` (R1.8). Every value is zero in the ROT stage, whatever the time, Kindling or deeds.
- **`PutricielBodyFireGeometry`** (new). Called by the pass for each body in all three layers. Its sites are the
  model's posed `fireAnchors()`; before they are recorded it builds the same seventy (12 / 2 / 19 / 15 / 9 / 6 / 7) off
  the recorded points. Each site's cluster back to front: soot over it, the crimson outer flame with its core licking
  inside (`PutricielFlameGeometry.anchored`), the bed of coals. A wrap on the shaft holds two tongues wrapping round it,
  a big back shelf three along its rim, a censer three through its lid. Then the structure: a sleeve of eight thin
  tongues hugging the shaft from the grip to the head and four half-height ones down to an ember-tipped foot, the
  censers' coal beds and soot, the streams (a train of tongues along the staff head's and both censers' last six ticks
  of path, 0.35 tapering to 0.05, only while they move), the back column (three broad tongues leaning back off the
  shoulders, a soot plume over them, collapsing into two smoke columns at the Embering), a pall over the shoulders and
  the glow under the hood. It follows motion: every tip rides a spring (`HemorathSpring`, 0.35 / 0.5) toward
  `PutricielBodyFireRules.tip`, stretches with speed, and a fast anchor sheds tear-offs. It sheds: ash flakes (the spore
  shader's new `ASH`) six a second off the back and shoulders and twelve through the Embering's smoke, a burst of eight
  and four tear-offs at the anchor nearest a hit, three to five burning shelf fragments at the commits of the Purging
  Sweep, the Censer Cast and the Pyre Heart (solid clods, char 0.8 with glowing cracks, falling, bouncing once and
  smouldering 20 ticks, kept past the move), `HemoParticleData.emberSpark` accents off the back and staff head (six a
  second under All, three under Decreased, none under Minimal), and a scorched print (`SCORCH`, 0.5 by 0.35, 40 ticks,
  two low tongues for 8) every half stride while it walks in the fire. Budget per body a frame: 220 tongues (streams
  and tear-offs inside it), 60 smoke cards, 40 shed pieces. An unanswered Offering's close eases the body back up on a
  spring instead of stepping.
- **`PutricielSludgeGeometry`** (new) and **`PutricielSludgeField`** (new). A pool is a polar heightfield of 12 rings
  (packed toward the rim so the lip reads) by 28 segments on the growth shader's new `SLUDGE` shape: 0.18 at the
  centre, thinning to 0.035 just inside the rim, a meniscus lip 0.06 high at 0.88, a rounded edge to the floor, a rim of
  five to seven seeded lobes between 0.85 and 1.1 of the radius (the seed is the hash of the frame's origin, so the
  burning pool keeps the pool's lobes). A runnel is the same section swept along its `Path`, pinched at both ends. It
  forms out of the splash over 8 ticks with its body overshooting to 1.15; in the rot one or two glossy domes rise,
  swell and burst into a ring ripple and a pale spore puff every 25 to 40 ticks, and the Bloom's pulses heave every
  pool and raise extra bubbles; settling (any fade but the burn's) dulls it to a matte crust, thins it to a fifth and
  sinks it, the stain fading. The `SLUDGE_FIRE` draws the burn from its aim (the catch point) outward: the front at a
  sixth of the radius a tick with low tongues on it, the boil (tongues 0.6 to 1.1 over the whole pool, one per 0.35
  square blocks, leaning with the draught toward the source; domes swelling and bursting two a tick with a heat flash,
  three sparks and soot; the char spreading behind the front to 0.7; the gloss gone; thinning to 0.08; the lobes
  curling up), the crust closing to cold by 44 with two soot columns, the crumble into ash flakes by 50, and the scorch
  on the new ash cooling to dark by 110. The ending's ten-tick `SLUDGE_FIRE` runs the whole burn in its life. The fire
  runner draws a scorched strip 0.6 wide behind a knot of low tongues on a glowing bed with sparks, dying 10 ticks after
  it arrives. The field holds the frame's pools for `PutricielGroundGeometry`, which lifts every floor patch and trench
  vertex onto the sludge's surface plus 0.02 (and ramps back to the floor over a quarter radius past the rim), so no
  telegraph hides under a pool.
- **`PutricielTelegraphPass`**: the body fire for every risen body (ending included); `ROT_SLUDGE` and `SLUDGE_FIRE`
  (stain in the ground layer, mesh and bubbles in the solid layer, fire, puffs and ash in the air) and `FIRE_RUNNER`;
  the frame's sludge field before the ground layer; a colony patch a pool absorbs (fade `ABSORB`) sinks its rosettes and
  sacs into the sludge instead of withering.
- **`PutricielGroundGeometry`**: the field (`begin(field)`, every vertex lifted) and the new `STAIN` shape (`stain`):
  the dark wet ring under and 0.35 past a pool's lobed rim with its mould-green fringe.
- **`PutricielFlameGeometry`** (additive): `anchored` (a tongue along a leaned direction at its length, its core
  shrinking as the heat drops, a pink-white glow at its root above 0.9), `tearOff` (5 ticks, keeping most of the
  anchor's velocity, rising and shrinking) and `glow`. **`PutricielSporeGeometry`**: `ASH` and `ash`.
  **`PutricielBloomRules`**: the sludge's looks (`sludge`, `settling`, `sludgeBurn`, `sludgeChar`, `crust`, `boil`,
  `sludgeFire`, `catchFront`, `coolingHeat`, `coolingAlpha`), its section and lobes (`sludgeProfile`, `lobe`), the
  bubbles (`bubble`, `boilBubble`), the runner (`runnerHead`, `runnerFire`), the fragments (`fragment`) and the ash
  stream (`flake`), on the server's beats (`PutricielSludgeRules`). **`PutricielTelegraphStyle`**: unchanged code (it
  claims every hazard by its kind already); its doc names the new kinds.
- **Shaders.** `putriciel_growth.fsh` `SLUDGE` (5): near-black olive deep to the shallows, slow cream scum where the
  bloom is high, an ochre meniscus band at the lip, a Blinn-Phong gloss off the key light at exponent 64 broken by the
  texels, a cold sheen at a glance, a matte olive crust as the gloss goes; the burning crust uses the existing char
  (black, cracks glowing crimson to pink-white, cold ash) with bigger plates; the sludge's alpha is its radial place, so
  the formation discard and the growths' wet and bloom terms skip it. `putriciel_spore.fsh` `ASH` (4): greys only.
  `putriciel_ground.fsh` `STAIN` (4). Every new constant is in the rot palette (`SLUDGE_DEEP`, `SLUDGE_SHALLOW`,
  `GLOSS`, `SCUM`, `MENISCUS`, `COLD_SHEEN`, `MATTE_CRUST`, `ASH_PALE`, `ASH_DARK`, `STAIN_MOULD`); no fire constant
  was added. No new shader, so no `ShaderInit` change.
- **`PutricielVisualRules`**: `EMBER_FADE` 24, the ember layer's fade through the Embering (was 15).
- **Tests (not run).** `PutricielBodyFireRulesTest` (new): zero in the rot for every stage time, Kindling, deed, move,
  opening and hit; the climb, the locked beat, the flare and the settle; the Kindling's scaling; every peak; the
  Offering's draw-down and pulse; the Embering by 10, 30 and 45; the Repose gutter and the falter; the ending's beats;
  the lean, stretch and flicker bounds; the growth stagger and regrowth by 60. `PutricielBloomRulesTest` (extended):
  the section and its lip, the lobes, forming and its overshoot, bubbles, settling, the burn's char, thinning, curl,
  fire, boil bursts (at most two a tick) and cooling, the runner, the fragments' single bounce, the ash rate.
  `PutricielEffectGeometryTest` (extended): the pool and the runnel stand between the floor and 0.25 and reach out to
  their lobed rim (never under 0.8 of the radius); the field lifts over a pool and not beside it; the burn chars from
  the catch point, thins, crusts and is gone by 50, with no fire on the cooling ash; the body fire draws nothing in the
  rot, only soot at the Ignition's tick 5, stays in its budget at its hottest (a swung staff at the Sweep's commit with
  every anchor tearing), sheds grey ash and is out by the Embering's 45; the anchored tongue's length and lean.
  `PutricielVisualRulesTest`: the ember layer still glows at 15 and is out at 24. `PutricielPresentationGuardrailTest`
  (extended): the body fire and the burning layer draw only in R1.9's stages; the pass draws the three new kinds; the
  floor reads ride the field; the shader shapes exist and their constants are read by the colour ruling; the new
  files build no render type and use no vanilla particle; the embers are Hemomancy particles and none show under
  Minimal. `docs/architecture/evidence/source-text-impact-map.csv` regenerated (3652 rows).

### Deviations and calls

- **The stain is its own ground shape (`STAIN`, 4)**, not a branch of MAT: MAT's three channels are all taken by the
  colony, and the stain needs the place out from the lobed rim, the wetness and the fringe.
- **The beds** are camera-facing glow cards on the Flammeus `FLAME` material's glow shape, drawn in the pass's own
  FLAME flow, rather than on `STEADY_FLAME`: the pass has no `STEADY_FLAME` batch and R1.7 asks for the same batch as
  the moves' fire.
- **The ember layer's fade** stretches to 24 ticks as R1.6 says; the body's embers wink out by 30.
- **The grip** sits at 0.42 of the staff from its foot in the structure's sleeve; the anchors carry their own.
- **The anchor's place** is the model's in-region kindle rank; the rules give each region a kindle window and read the
  rank into it, so the model's generated kindle ticks and the rules agree to within a tick or two.
- **The Offering's return after an unanswered close** eases on a spring (no synced close time exists); an accepted
  one returns over 8 ticks after the recoil, in the rules.
- **The ending's pools**: once the ending runs, a pool draws nothing itself and its ten-tick `SLUDGE_FIRE` draws the
  burn. Any fade of a pool but the burn's is drawn as a settle (the cap's 30, the kit's 10 at its safety life).
- **Lava pops and the other sounds** are the server's (R2.8); the client adds none.

### Not verified, and what needs a launch

Nothing compiled and no test ran. The verify stage should compile, run `PutricielBodyFireRulesTest`,
`PutricielBloomRulesTest`, `PutricielEffectGeometryTest`, `PutricielVisualRulesTest`,
`PutricielPresentationGuardrailTest` and the Putriciel suite, then launch `runBossPresentationReviewClient
-PputricielFightReview=true` for the shader compile on the GPU (`putriciel_growth` gained a branch, the spore and
ground shaders a shape each) and the server lane's new stops. Look at:

- the fire's mass from fight distance at the steady burn (220 tongues on the body) and its frame cost with the moves'
  fire on top;
- the streams and tear-offs on a swung staff, and the springs' drag (0.35 / 0.5);
- the back column's lean and plume against the chamber's ceiling;
- the sludge's gloss and facets in the dark chamber (the highlight is per facet, broken by the texels), the meniscus
  band's width, the scum's drift and the stain's fringe on the moss;
- a telegraph riding a pool (no z-fighting at the surface, no dip at the rim);
- the burn from the catch point, the boil's bursts, the crust's plates and the crumble onto the server's ash;
- the runner's fuse crawling from a pyre and from a burning pool;
- the ember sparks at Decreased and Minimal particles.

## Revision polish (2026-11-06)

The three revision lanes were written without Gradle. This stage compiled them, ran the tests and the fight suite,
launched the review client four times and judged the stills against the user's three asks. Each launch ran the whole
review: 81 scenes, about 1060 stills, `passed` true, every move active, the chamber put back block for block, no burn
in the rot.

### What ran

- Compile: two errors, both in `PutricielSludgeGeometry`. A call to the static `seed(Vec3)` inside the `Pool` record
  resolved to the record's own `seed()` accessor, and a `double` was passed for the `float` heave. Nothing else failed.
- `gradlew test --continue`: 5571 tests, 0 failures, 0 errors (`build/putriciel-revision/final-test.log`). No
  allow-list was widened. `PutricielFireResourceTest` is green, with `putriciel_burning.png` included.
- `runPutricielFightGameTestServer`: all 18 required tests passed, the 5 new sludge cases included
  (`build/putriciel-revision/final-gametest.log`).
- `putriciel_growth`, `putriciel_spore` and `putriciel_ground` compile on this GPU. None of the four client logs
  (`build/putriciel-revision/round<n>/latest.log`) has a shader or render error for them.
- The generator re-ran once, for the anim change below. The atlases came out byte-identical, and the palette
  measurements did not move: dark 30.6%, mid 43.2%, light 26.2%, ochre 10.6%, mould green 6.3%, no red.

### Fixed

- **The body fire read as red flecks.** At R1.1's heights (0.15 to 0.9 a tongue, on a body about five blocks tall),
  the burning purifier showed as a dark figure speckled red from every camera, the boss camera included.
  - The geometry now draws every body tongue 1.75 times taller and 1.6 times wider (`PutricielBodyFireGeometry.BOLD_HEIGHT`
    and `BOLD_WIDTH`). That covers the clusters, the staff sleeve and foot, the streams and the back column.
  - The rules keep R1.1's numbers, so the kindle, the flare, the peaks and the die-back are unchanged.
  - The staff now burns as a torch along its length, the back column stands over the shoulders, and the body reads as
    on fire from the side cameras at fight distance.
- **The Embering drew pink flesh.** As the growths regrew out of their char, the burning layer still added crimson and
  pink-white over their cream. On the arms, crown and censers that read as salmon-pink cubes. Two changes:
  - `renderBurning` skips every growth whose char is under 0.5 (`putriciel_anim.java`, regenerated);
  - `PutricielBurnLayer` runs the Embering's embers at 0.55 and fades them out between its ticks 6 and 24.
- **The sludge read as water in a dish.** It was a pale, glassy pool with a mirror sheet of gloss, a cream ring and a
  bright green outline. Now:
  - lumps of thicker rot stand in the surface (`PutricielSludgeGeometry.LUMP`, 0.022, inside the 0.25 ceiling);
  - the body carries near-black clots and mould-green flecks;
  - the gloss is broken per texel into wet glints at exponent 140, and held back at a glancing look;
  - the cold sheen is a third of what it was;
  - the scum islands are smaller and fewer, and the meniscus is narrower, broken and darker;
  - the stain's mould fringe frays into the moss in sparse dark tufts;
  - a runnel ends round instead of in a blade point.

  The spec's colour constants are unchanged; only how strongly they apply changed.
- **The burning pools' tongues** are a third wider, so a boiling pool reads as a bed of flame from above rather than
  as red dots.
- **The harness** (`PutricielFightClientReview`):
  - The laying rot moves each aim at their own patch of floor, so their pools stand apart instead of splashing onto
    one another. Four or five pools now stand after Rot I's moves where only one did, and the cap settles the oldest.
    A `sludge_after_<move>` still follows each laying move.
  - `laySludge` takes three sets, each clear of the ash the sets before it leave.
  - `body_fire_front` and `body_fire_side` join `body_fire_back`. `body_fire_ignition` and `embering_dieback` shoot
    every third tick.
  - `sludge_burn_low` watches the second Ignition's pools from a player's eye, with the quarry out of the line.
  - The third Ignition (`hud_censer_100_ignition_three`) runs 210 ticks, shot every third tick from high over the
    floor. Four pools are laid round the body before it, so the ring, the pyre runners, the pool-to-pool runners and
    the fallback each light one, through to the ash.

### Contact sheets

- Before and after against visual pass 2: `build/putriciel-revision/compare-before-after.png`. It covers the colour
  in the rot, the fire close and at idle, the fight distance, the Ignition and the floor after the Volley.
- Per beat (round 4), all in `build/putriciel-revision/`:
  - the Ignition build: `beat-ignition-build.png`;
  - the steady burn: `beat-steady-burn-front-side-back.png`, `beat-steady-burn-side.png` and
    `beat-steady-burn-back.png`;
  - the Embering: `beat-embering-dieback.png`;
  - motion: `beat-body-fire-motion.png` and `beat-stride.png`;
  - colour: `beat-palette-rot.png` and `beat-palette-back.png`;
  - sludge in the rot: `beat-sludge-rot.png`, `beat-sludge-rot-low.png` and one `beat-sludge-after-<move>.png` per
    laying move;
  - the burn: `beat-sludge-burn-spread.png` and `beat-sludge-burn-low.png`.
- One sheet per scene for round 4 is in `build/putriciel-revision/sheets/`. Each round's own sheets are in
  `round<n>/sheets/`.
- The builders are `build/putriciel-revision/sheets.py` and `compare.py`.

### Still open

- The fire is crimson with pink-white flecks. The black soot tips and the soot plume hardly show against the dark
  chamber, so on the body the Flammeus three-tone look is mostly two-tone.
- The burning layer's red texels over the charred black read busy close up, like red speckle. From the fight cameras
  they read as embers.
- The colour pass gives range: ivory vestments, dark eschar and iron, ochre and cream shelves, mould green. But the
  mottled flesh reads as camouflage blocks at portrait distance. The next step is to lower the mottle's contrast in
  the generator.
- The sludge reads as dark wet rot from the boss cameras. From a player's eye on the floor it is still a little smooth
  and translucent-looking, and the runnel is a long even spindle.
- Pools are scheduled only at the Ignition's commit, so a pool laid after the schedule does not burn before its
  safety life. Today only the review's direct `laySludge` can do that, since no fire stage lays sludge. It becomes a
  gap if a later change lets a pool form in the interval.
- The Root Snare's pool needs a miss. The review's quarry is hit every time, so the stills show only the review's own
  laid Snare-shaped pool. The Bloom's drains stood once (1 pool) in rounds 3 and 4.
- Not checked:
  - frame cost with the body fire (up to 220 tongues), the moves' fire and four burning pools at once;
  - Decreased and Minimal particles;
  - first person;
  - multiplayer.

### Honest verdict

- **Ask 1, physically on fire: met.** Putriciel is unmistakably on fire through the interval:
  - the staff burns from the grip to a three-pronged torch head;
  - the back carries the tallest fire, and the arms and shoulders smoulder;
  - ash and streams of fire shed with motion;
  - the Ignition builds it up the body and the Embering dies it down;
  - the rot phases carry none.
- **Ask 2, sludge: met for behaviour, close for look.**
  - Behaviour: the rot moves leave pools that persist and slow. The interval's fire takes them visibly (catch front,
    boil, crust, ash), from the ring, the runners and the fallback.
  - Look: the pools now read as dark glistening rot, not a decal. At a player's eye they are not yet as thick and heavy
    as the brief's "with body".
- **Ask 3, colour: met.** The flesh mottle is the one weak spot.
- **Boldness:** the fire meets the bar; the sludge nearly does.

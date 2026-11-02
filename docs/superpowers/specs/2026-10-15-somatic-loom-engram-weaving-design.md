# Somatic Loom: Engram Weaving — design

Date: 2026-10-15. Status: approved in conversation (four sections); supersedes the combat half of
`2026-10-11-somatic-loom-weave-design.md` (the witnessed weave). Setup, payment, tiers, the echo's casts, the
Cortical dome and the seam stay.

## Why

The witnessed weave's fight was arbitrary: every tendency's thread played the same "dodge, strike until slack,
hook" loop, strain was a hidden health bar, and nothing the weaver did revealed anything about the memory. With
103 one-and-done recipes the ritual must reward knowledge, not reflexes, and feel like uncovering an ancient
recollection from the bloodline's collective blood-memory.

## The act: Engram Weaving

Paying the blood unfurls **the engram** on the ground around the loom: an eight-pointed star sigil ({8/3}) whose
centre is the loom itself. Engram fragments, one per enzyme unit of the recipe, rise at its tips. The weaver threads
each true fragment home to the loom along the star's lines without two colours ever sharing a node. Each landed
lane lets the echo above the loom remember one more beat of the cast; the last lane plays the existing climax.

Vocabulary: the Somatic Loom keeps its name; the act is Engram Weaving; the star is the engram; tip nodes are engram
fragments recalled from past Harbingers' blood-memory; lanes are threads spun from stored enzyme.

## The engram (geometry)

- **Shape.** {8/3} star: eight lines, each joining tip k to tip k+3. Each line is crossed four times; the crossings
  form two rings of eight: the **notches** (radius 0.541 of the tip radius, offset 22.5°) and the **inner octagon**
  (radius 0.414, on the tip angles).
- **Nodes (25).** 0–7 tips, 8–15 notches, 16–23 inner octagon, 24 the **Vessel** (the loom).
- **Edges (56).** Five segments along each star line (40), the outer ring arcs between neighbouring tips (8), and
  eight **spokes** from each inner-octagon node into the Vessel (8). Spoke i enters Vessel **face** i.
- **Orientation.** Tip 0 points out of the loom's front (model −z). Nodes are numbered clockwise seen from above.
- **Size.** Tip radius by loom stage: base 5.0 blocks, Ganglionic 5.75, Cortical 6.5 (inside the 13-block dome).
  The inner octagon must clear the loom frame (half-diagonal ≈1.6), so the base sigil is ~10×10, not 9×9.
- **Ground.** The sigil drapes over the real ground around the loom (option A): every column inside the tip radius
  carries the membrane on its top surface within two blocks of the loom's floor; steps get a hanging drape; gaps
  stay open. Nodes are virtual targets in sigil space, so blocks never block the puzzle.

## Rules

| Piece | Rule |
|---|---|
| Fragment | Sits on a tip (on notches too when tips run out or at Perfectus). Coloured by tendency. Its lane must reach the Vessel. |
| Lane | Starts at its fragment and moves node to node along edges. A node holds one lane, except plying (below). |
| Plying | A lane may end on a node of a lane of **its own colour**; it is then carried by that lane. Different colours never share a node. |
| Spoke / face | Entering the Vessel uses a spoke. From Summa up, faces are **keyed** to the tendency wheel and a lane may only enter its own colour's face. Below Summa any face takes any colour. A face takes one strand (plying shares it). |
| Scar | Dead node: nothing passes. |
| Membrane (Summa+) | Colour-locked node: only its tendency's lane may enter. |
| Prism (Magister+) | A grey fragment's true colour is hidden until listened to. A grey lane takes a colour by passing a prism node; past a prism of the wrong colour it will not extend. |
| Intrusion (Mediocritas+) | A false fragment from a failed Harbinger's memory, coloured like a real one. Connecting it to the Vessel (directly or by plying) triggers the rank's stake and snaps that lane. |

**Tendency wheel.** `EnumBloodTendency` declaration order: Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio,
Ferric, Tenebris (the Harbinger progress screen's star). Opposites sit four apart: Animus–Mortem,
Flammeus–Congeatio, Ductilis–Ferric, Lux–Tenebris. Each board rotates the wheel; face `rotation` is marked (the
Animus face) so the order reads from one landmark.

**Completion.** Every true fragment's lane reaches the Vessel (directly or plied) and the connected fragments of
each colour (grey fragments counted under their true colour) equal the recipe's count. Intrusions never count.

**Listening.** Holding the staff on a fragment without moving plays its beat on the echo: a true fragment replays
*this* memory's real cast (small and dim); an intrusion replays a *different* memory of the same tendency, and it
pulses out of step with the loom's heartbeat. Listening is client-side only.

### By rank

| Rank | Board | A connected intrusion |
|---|---|---|
| Humilis | Any face; 1–3 scars | (none) |
| Mediocritas | Any face; 2–4 scars; 1 intrusion | **Cost**: the lane snaps and its spans burn from stock above the reserve; any shortfall drains 5 mL of the weaver's blood per span |
| Summa | Keyed faces; membranes on the solution; 1 intrusion | **Fungal pressure** +1; at 3 a free node off the solution scars over (or, if none, a new intrusion appears on a free tip) and the meter empties |
| Magister | Summa + 1–2 grey prism fragments, prisms on the solution plus one decoy prism; 1–2 intrusions | Fungal pressure |
| Perfectus | Magister + fragments may sit on notches; 2–3 intrusions | **Bite**: a Broken Will fragment manifests at the intrusion's tip, makes one telegraphed tendency attack, then fades |

**No weave can kill.** The 4-health blackout floor (`WeaveDamageEvents`) applies at every rank, as do the
existing harm windows, so lava and other attackers still kill. Blacking out pauses the weave.

### Cost and the revolving stock

- Inserted enzyme becomes **3 spans** per item in that tendency's vial. Vial cap **192 spans** (64 items, as before).
- Every edge a lane covers costs one span of its colour (grey lanes pay in their true colour). Plied lanes pay only
  their own segment. A board's spans are consumed **only at completion**; retracting or cancelling refunds them.
- A lane will not extend past the colour's stock minus the spans already committed on the board.
- The **reference cost** is the generator's own solution. The weave starts only if each colour's stock covers its
  reference cost; that amount is **reserved**, so a stake can only burn surplus and the reference solution always
  stays affordable. Enzymes may be topped up mid-weave.
- A shortest lane (tip → notch → inner → Vessel) is 3 spans = one enzyme item, so an efficient unplied solve costs
  the recipe's old enzyme count; plying can beat it, wandering costs more. JEI shows fragment counts and "≈N enzyme".

### Generation (pure, seeded)

Seed = hash(world seed, recipe id, weaver UUID). The generator lays a solution first, then dresses the board:

1. Rotate the wheel; place each colour's fragments on tips (notches when needed), grey/prism fragments, intrusions.
2. Route colours in a shuffled order: each colour's fragments grow a tree (BFS on free nodes, plying allowed into
   its own tree) to an allowed face. Retry with new placement on failure (bounded attempts; then relax: drop keyed
   faces, then allow notches).
3. Scars on free nodes off the solution; membranes on solution nodes of their lane's colour; prisms on grey lanes'
   paths plus a decoy; record the reference cost per tendency.

Every one of the 103 recipes × every rank × several seeds must generate a solvable board (JVM test). Fungal
pressure only ever scars or seeds nodes off the reference solution.

### Fragment beats and intrusion decoys

Each true fragment carries the recipe's memory. Each intrusion carries a decoy memory: another registered memory
item's manipulation of the same tendency (any other memory when none exists), chosen by the seed on the server and
stored on the board, so the client can replay its real echo cast.

## Interaction (Living Staff)

Server-authoritative, no new packets: the server reads the weaver's look every tick while the staff is in use.

| Action | Effect |
|---|---|
| Use (press) on a fragment or a lane's head | That lane becomes **active** |
| Hold, stay on a fragment ~0.5 s | **Listen** (client) |
| Hold and sweep | The active lane follows the aim node to node; sweeping back onto its previous node retracts one step (Flow-style). A fast sweep along one straight line fills the skipped empty nodes. |
| Use on a node next to the active head | Extend one step (click-by-click) |
| Sneak-use on a lane node | Cut the lane back to that node |
| Sneak-use on a fragment | Clear its lane |

- Aim = the eye ray against the ground the sigil lies on (block face hit inside the sigil), falling back to the
  sigil's plane; it snaps to the nearest node within 0.7 blocks (scaled with stage). Reach: 5.5 blocks base, 8
  Ganglionic/Cortical; listening reaches 16 on Ganglionic+.
- While the staff traces, it opens no blood channel (projection/absorption) and does not hit the loom.
- Only the weaver traces. Others see everything.
- Crouch-using the loom block still cancels (blood lost, no enzyme lost).

## Lifecycle and persistence

`SomaticLoomBlockEntity` keeps idle → awaiting blood → weaving → climax. The live board lives in an
`EngramWeaveSession` owned and persisted by the loom (board, lanes, active lane, pressure, reserve, decoys).

- **Pausing, never failing**: the weaver dying, leaving 12 blocks, logging out or blacking out freezes the board
  (lanes kept); returning resumes. An orphaned weave is adopted by the nearest player.
- **Completion** consumes lane spans, the catalysts and the blank, then plays the climax and records practice as
  today.
- **Old saves.** Stored enzyme units ×3 → spans (behind `weaveSaveVersion` 2). A weave in progress from the
  witnessed weave (`weaveThreads`) or older `ritualOrbs` becomes a fresh engram with its blood already paid; its
  spun enzyme returns to the vials as spans. Loose `FrayedThreadEntity`s of a migrated weave unravel.

## The bloodline remembers

`EngramRecollections` (SavedData) records, per player, the memory recipes they have woven. When another member of
the weaver's bloodline has woven the same recipe, the board's intrusions are **pre-marked** as grey husks.

## Tiers (unchanged gates, reframed perks)

| | Base | Ganglionic | Cortical |
|---|---|---|---|
| Ranks | Humilis–Summa | + Magister | + Perfectus |
| Sigil tip radius | 5.0 | 5.75 | 6.5 |
| Trace reach | 5.5 | 8 | 8 |
| Listening | within reach | anywhere on the sigil (16); feelers droop toward the listened fragment | same |
| Echo | small, grey, stutters | coloured, steady | full stature |
| Space | — | — | the Chamber dome closes over the sigil |

Practice proofs (`loom_weave`, `loom_weave_ganglionic`), kits, rites and offerings are unchanged.

## Visual direction

Aesthetic anchors: the consecrated Cardinal floor membrane (vessels sunk into living flesh, wet highlights, lip
shading, heartbeat), the blood pool, the daemon haze, Hematic Riposte's layered anatomy. Alien, magical and
visceral, never cluttered: few elements, each layered and alive.

1. **The engram membrane** (`engram_sigil` core shader on a terrain-draped mesh): a thin translucent flesh skin
   grows out from the loom like the Cardinal floor's spread, its edge a ragged living hem. The star, ring and
   spokes are **empty vessels sunk into it** (groove + lip + wet core, like the floor's tributaries), faintly
   bioluminescent and breathing with the loom's heartbeat. Scars are necrotic clots; membranes are coloured valves;
   prisms are faceted irises; keyed faces are coloured where spokes meet the loom; pressure is grey-green rot
   creeping inward from the ring. Where the skin meets a step it drapes over the edge.
2. **Lanes** (same shader, lane layer): a vessel filling with the tendency's ichor, flowing toward the loom with
   peristaltic bulges and a bright head that pushes forward as the lane extends; plied joins swell into a knot.
   Landed lanes pulse into the loom; a pirn spins and a warp thread lights.
3. **Fragments**: an engram glyph (the 26 Engram characters) standing over each tip, lit in its colour, rooted to
   the membrane by tendrils; a listened fragment flares and sends a nerve-thread to the echo. Intrusions flicker
   off-beat; pre-marked ones are grey husks; a bitten intrusion tears open.
4. **Aim**: HitGlow sparks at the exact traced point; the aimed node blooms; an invalid step flinches red.
5. **Unfurl / climax**: tendrils race from the loom to the tips as the skin spreads; at the climax every lane
   lifts off the ground as a thread drawn up into the warp while the skin withdraws into the loom.
6. Only Hemomancy/HutosLib particles; no vanilla `ParticleTypes`.

## Architecture

Pure, JVM-tested (`common/tile/harbinger/crafting/engram/`):
- `EngramSigilGeometry` — nodes, edges, lines, rings, spokes/faces, unit positions, collinear runs.
- `EngramBoard` — node contents, fragments (colour, true colour, intrusion, decoy index, pre-marked), lanes,
  pressure, wheel rotation, reference cost; NBT round-trip.
- `EngramBoardGenerator` — seeded solution-first generation by rank.
- `EngramWeaveRules` — step legality, plying, keyed faces, membranes, prisms, connectivity, completion, span cost,
  stakes by rank, sigil radius and reach by stage, the stock/reserve check.
- `EngramAim` — ray/plane and snap maths in sigil space.

Game glue:
- `EngramWeaveSession` (server state + NBT) inside `SomaticLoomBlockEntity`.
- `EngramTracing` — Living Staff hook: begins/continues/ends traces from look rays, suppresses the staff channel.
- `EngramRecollections` SavedData.
- `FrayedThreadEntity` becomes the Perfectus **intrusion**: spawned at the tip, one telegraphed attack (reusing
  `ThreadAttacks`, bolts, hazards and the damage floor), then unravels. Strain/slack/hooking retire.
- Client: `EngramSigilRenderer` (+ `EngramSigilMesh` terrain drape, `EngramFragmentRenderer`), `EngramAimClient`
  (cursor, listening, preview), echo changes in `SomaticLoomEcho`.

## Testing

- **JVM**: geometry (counts, degrees, planarity of the drawn edges, ring radii); rules (each table row, plying,
  completion, cost, reserve); generator (all recipe enzyme profiles × ranks × seeds solvable, reference within
  192 spans, deterministic per seed); pressure keeps the reference solution valid; aim snapping; NBT round-trip.
- **GameTests** (`somatic_loom_validation`): pay → engram; trace the reference solution via the session API and via
  staff use with a set look; completion consumes exactly the lanes' spans and delivers the memory; cut refunds;
  leaving/death pauses; Mediocritas burn; Summa pressure; Perfectus bite floored with blackout; bloodline
  pre-marking; stock-below-reference refusal; rank/stage refusal; upgrade rites and practice; save migration from
  witnessed-weave and orb saves; reload mid-weave.
- **Source contracts**: no `ParticleTypes.` in loom/engram code; shader registered; docs mention Engram Weaving.
- **Native review**: each tier idle and tracing, readability from eye height and from atop the loom, Cortical dome,
  climax; shader compile on the real GPU.

## Out of scope

Recipe catalysts and blood costs; manipulation balance; weaving inside the Chamber of Will dimension.

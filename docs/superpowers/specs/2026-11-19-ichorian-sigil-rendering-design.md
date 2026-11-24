# Ichorian Sigil rendering redesign

Date: 2026-11-19. Status: design, awaiting owner approval. Nothing built.

## Intent

An Ichorian Sigil on the ground reads today as a diagram: thin flat ribbons between
blobs. It should read as something cut into the ground and then come alive. The links
become gouges like the consecrated floor's brazier trench. When the pattern closes, the
gouges fill and light along their length, veins creep out of them, and the living form
rises and stays floating over its own pattern.

## What exists today

- Links: `CardinalRiteBoundaryRenderer.drawSigilSegments` draws each
  `ActiveRiteData.SigilSegment` as a wobbling flat ribbon
  (`IchorianSigilOrganicGeometry`) on `RITE_BOUNDARY_CORE` / `RITE_BOUNDARY_GLOW`.
  No shader of its own.
- Nodes: sphere-built landmarks (`IchorianSigilLandmarkRenderer`), fed one at a time.
- The trench the owner likes: `gash()` in `cardinal_floor_membrane.fsh`, drawn on the
  floor's own strip (`FloorMesh`, `GashLoop`). It only exists on the consecrated floor.
- Sigil nodes sit on whatever surface is under them
  (`CardinalRiteSigilRules.surfaceAirPosition`), each at its own height.
- Living form: `AwakenedIchorianSigilEntity` peels up over 30 ticks, then the server
  moves it on a per-sigil orbit of the rite (`AwakenedIchorianSigilMotion`). Facing,
  bank and body animation key off travel speed. No gameplay reads its position.
- Clients learn of awakening only by the one-shot `SIGIL_AWAKENED` beat and the
  entity; `SigilSegment` carries no sigil identity or stage.

## Goals

1. Links render as gouges with a dedicated shader, on any ground, at any angle.
2. Gouges cut in link by link as nodes are fed: dry and dark while the pattern is open.
3. On completion the gouges fill with blood and a glow runs the pattern node to node,
   then settles to a slow pulse on the rite's heartbeat.
4. On completion thin veins creep outward from the gouge lips along the ground, and a
   few thin tendrils lift from the nodes toward the living form.
5. The living form peels up as now and then hovers over its pattern's centre. It does
   not travel.

## Non-goals

- No change to sigil gameplay: node order, blood cost, awakening rules, sockets.
- No change to the node landmarks' anatomy or to the living form's anatomy.
- No change to the consecrated floor shader or mesh.
- Unstained rites are out of scope.

## Anti-goals

- No vertical beams or shafts of light (owner: reads as generic magic).
- No thick strands and no noise-contour lines; veins stay thin and partly clear
  (the "spaghetti" ruling). The completion moment itself is a focal effect and is bold.
- No vanilla particles. No copper, brass or bell timbres.

## Approaches

Gouges:
- A (picked). Own strip mesh and own shader, borrowing the trench's look. Works on any
  ground; the floor shader is untouched.
- B. Add links to the consecrated floor's mesh. Rejected: only works on the floor and
  couples sigils to the largest shader in the mod.
- C. Reuse `GroundRunRenderer` gouge runs. Rejected: block-aligned, and brings ground
  mark gameplay rules with it.

Veins:
- A. Shader veins on a ground skirt only. Cheap, subtle, never leaves the ground.
- B. Tendril geometry only. More presence, spaghetti risk.
- C (picked). Shader veins on the ground as the main effect, plus a few thin tendrils
  at the nodes reaching toward the hovering form.

## Design

### 1. Sync

`SigilSegment` gains what the client needs to stage the effect without guessing:
a per-sigil key (so segments group into one pattern), the link's index in the
pattern, and whether its sigil is awakened. The client stamps its own game time when
it first sees a link and when it first sees its sigil awakened, and keeps those in
client state keyed by rite and sigil; a late joiner sees the settled look with no
replay. This re-lays-out `PacketSyncActiveRites`: bump `PacketHandler.PROTOCOL_VERSION`
and rewrite `payload-registry.txt`.

### 2. Gouge strip and shader

- New geometry class under `client/render/world/` (name: `IchorianSigilGougeGeometry`):
  per link, a strip of quads from node to node, wide enough for the cut plus the vein
  skirt, sampled along its length and lifted a hair off the surface. Height is
  interpolated between the two node heights (builder: decide whether to sample the
  terrain per step where the ground between nodes is uneven; nodes on one plane is the
  common case). The link ends tuck under the node landmarks.
- New shader `ichorian_sigil_gouge` in `shaders/core/world/`, with a render type built
  once per the caching rule (`SeededRenderTypes` holder keyed by seed if it needs
  per-pattern uniforms, otherwise a constant with per-vertex data). Vertices go through
  `MaterialQuads` if they use the shape/seed tile layout.
- The shader draws, across the strip: the cut (faked depth by view-angle parallax, as
  `gash()` does), dark walls, a torn irregular lip, a slight raised bruise outside the
  lip, then the skirt.
- Per-link inputs: cut-in progress (the gouge opens along its length from the earlier
  node), fill level, glow front position along the whole pattern, heartbeat, colour
  from the sigil's authored colour through `IchorianSigilRenderPalette`.
- Shared noise comes from the existing `noise/*.glsl` includes. The trench look is
  rewritten for this shader, not imported from the membrane shader.
- Drawn from `CardinalRiteBoundaryRenderer`'s existing sigil step (it stays under the
  one `WorldEffectDispatcher`); `drawSigilSegments` and its ribbon path are removed,
  with `IchorianSigilOrganicGeometry` kept only for what the living form still uses.
- Rite endings: the gouges follow the rite's existing fade (`frame.fade`); on a
  violent ending they drain and close with it.

### 3. Stages

| Stage | Gouges | Veins and tendrils | Living form |
|---|---|---|---|
| Open pattern | Each new link cuts in over about a second; dry, dark, faint wet sheen | none | none |
| Completion (about 2 s) | Blood wells up from the last node and fills every link; a bright front runs the pattern in node order | ground veins begin to creep from the lips | peels up (existing 30 ticks) |
| Awake | Filled; slow glow pulse on the heartbeat, a dimmer front re-running the pattern now and then | veins at full reach, writhing slowly; node tendrils up | hovering |
| Rite ends | fade or drain with the rite | retract | discards as now |

### 4. Veins and tendrils

- Ground veins are in the gouge shader's skirt: branching from the lip outward, reach
  driven by a creep uniform, writhing by slow animated noise, thinning and fading to
  nothing at the skirt's edge. Reach is about one block from the lip (builder tunes).
- Node tendrils reuse the existing tendril toolkit (`HLRenderTypeInit.TENDRIL_*`, the
  ribbon helpers the staff tendrils use): two or three per node at most, thin, rooted
  in the node, leaning toward the living form and swaying; they stop short of it. A
  node count cap per pattern keeps large sigils from bristling.

### 5. Living form hover

- `AwakenedIchorianSigilMotion` loses the per-sigil orbit paths. Position is the peel
  from the pattern origin to a hover point straight above the pattern's centre, then a
  small bob. Hover height is a rule in common code (start: 2.2 blocks above the
  surface; builder tunes so the largest form clears the tendrils).
- Facing no longer follows travel. The form turns slowly in place (per-sigil rate and
  phase so two sigils do not turn in step).
- `AwakenedIchorianSigilBodyAnimation` and the pose calculator get an idle input in
  place of movement speed: breathing and limb drift continue at rest. The per-sigil
  character the orbits carried (the shunt's quickness, the seal's stillness) moves
  into the idle animation's rate and amplitude.
- Entity data: `ORBIT_RADIUS` and `STARTING_ANGLE` go (the angle's hash can seed the
  turn phase); the 20-tick orbit radius refresh goes. The mod is unreleased, so old
  saved sigil entities need no migration.
- `CardinalRiteFootprintRules.awakenedSigilOrbitRadius` and its tests are removed if
  nothing else uses it.

### 6. Tests, review and docs

- JVM tests: gouge geometry (strip spans node to node, follows node heights, winding),
  stage timing rules (pure class), hover position (peel then fixed point, bounded
  bob), and updates to the existing motion, facing, body animation and footprint tests.
- A review client row (`sigilGougeClientReview`) that stages one sigil at open,
  completing and awake, on flat and on stepped ground, and takes stills. It also
  proves the new shader compiles on the GPU.
- Docs: `docs/CARDINAL_RITES.md`, the rite sections of `docs/HEMOMANCY_REFERENCE.md`,
  `ARCHITECTURE_STANDARDS.md` if a guardrail list changes, and
  `docs/OPEN_QUESTIONS.md` for the play-and-tune numbers below.

## Calls made without the owner (check these)

- Gouges cut in per link while the pattern is open, dry until completion.
- The form turns slowly in place rather than facing the caster or the Focus.
- Hover point is the pattern's own centre, about 2.2 blocks up.
- Node tendrils stop short of the form; two or three per node.
- Late joiners see the settled look, no replay.
- The trench look is rewritten in the new shader, not shared with the floor shader.

## Left to the builder

Strip width, cut depth, vein reach, timings, glow strength, hover height and turn
rates; whether the strip samples terrain between nodes; render type holder shape.

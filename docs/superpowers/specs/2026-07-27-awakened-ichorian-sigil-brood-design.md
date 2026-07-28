# Awakened Ichorian Sigil Brood Design

## Summary

Awakened Ichorian sigils will become specialized castes of one alien, anatomical
organism rather than variations of the same floating magical glyph. Each sigil
retains its exact authored ground inscription while dormant. When awakened, that
inscription continuously unfolds into a distinct living body: its original nodes
migrate into anatomical landmarks, its connecting lines stretch into vessels and
tendons, membranes grow between selected landmarks, a central organ begins to
pulse, and one shared dark-crimson eye opens at the creature's front.

The implementation will be data-driven. Each sigil definition may provide an
optional awakened anatomical rig alongside its grounded form. The existing
generic organic rendering remains as a compatibility fallback for definitions
without a valid awakened rig.

## Goals

- Make the nine current awakened sigils immediately distinguishable by
  silhouette, posture, and movement.
- Preserve the visual lineage between a grounded inscription and its awakened
  body through a continuous transformation.
- Establish a coherent brood identity without making the castes look like
  copies of familiar animals.
- Use the original sigil nodes as meaningful anatomical landmarks such as
  joints, organs, valves, eyes, and limb tips.
- Scale the caste bodies modestly with sigil tier, from roughly 0.8 blocks at
  tier one to roughly 1.6 blocks at tier five.
- Keep movement bounds, ritual behavior, effects, costs, progression, and save
  behavior unchanged.
- Keep rendering performant through bounded procedural geometry rather than
  particle-heavy bodies or a separate model asset for every caste.

## Non-goals

- This design does not alter what any sigil does mechanically.
- It does not change node counts, drawing order, blood costs, learned progress,
  orbit bounds, rite scaling, or awakened entity persistence.
- It does not introduce combat hitboxes, new AI, pathfinding, or physical
  collision for the anatomical forms.
- It does not replace the current grounded rigid sigil presentation.
- It does not require Blockbench models or textures for each caste.

## Existing Context

Grounded sigils are loaded from
`data/hemomancy/ichorian_sigil/*.json` into `IchorianSigilDefinition`.
`AwakenedIchorianSigilEntity` stores the sigil identity, ritual center, orbit
state, age, and other movement state. `AwakenedIchorianSigilMotion` already
defines distinct orbit styles for individual sigils.
`AwakenedIchorianSigilRenderer` currently lifts the same planar path for every
sigil and renders it with procedural vessel bands and node spheres.

The new system retains this division of responsibilities while adding an
anatomical pose layer between the definition and the renderer.

## Brood Anatomy

Every caste shares the following anatomy:

- Exactly one dark-crimson eye at the creature's authored forward end.
- Exactly one primary pulsing organ.
- Vascular tendons derived from the sigil's original sequential connections.
- Optional secondary vessels that complete the living anatomy without changing
  the player's drawing order.
- Selective membranes stretched across authored node triangles.
- Subtle asymmetry, peristalsis, valve motion, and delayed soft-tissue response.
- A stable forward orientation derived from smoothed movement velocity.

The common eye is the brood's strongest family resemblance. It should be a dark,
saturated red that remains obvious against black tissue without reading as a
bright generic magical light. On sharp turns the body turns first, while the eye
briefly lags and then refocuses. At near-zero velocity, the creature retains its
last stable orientation rather than snapping or spinning.

The organ, vessels, and membranes use the mod's established fleshy visual
language. They should look grown, tensioned, and perfused rather than assembled
from geometric beams. Not every caste needs an enclosing membrane; exposed
vascular and skeletal silhouettes are important to the brood's diversity.

## Ground-to-Living Transformation

Awakening is a continuous anatomical unfolding, not a swap between two models.
The grounded inscription remains the source skeleton for the entire animation.

The target duration is approximately 40 ticks, with overlapping stages:

1. **Detachment, ticks 0–10:** the rigid inscription peels away from its
   supporting surface while retaining its planar shape.
2. **Migration, ticks 6–32:** original nodes move along eased, organic paths to
   their authored three-dimensional anatomical positions. Original sequential
   lines remain visible and stretch into primary vascular tendons.
3. **Quickening, ticks 18–40:** secondary vessels grow, membranes inflate, the
   central organ starts beating, and the crimson eye opens.
4. **Living state, after tick 40:** the completed caste body uses its authored
   idle and locomotion deformation.

Stages overlap so the inscription appears to peel, pull, inflate, and awaken as
one event. No original node disappears, and no primary connection is replaced by
an unrelated line. Secondary anatomy may grow around the source skeleton but
must not conceal the transformation's continuity.

Pose interpolation should use smooth curves with restrained overshoot. Organic
motion comes from staggered migration, tension changes, and soft-tissue lag,
not from noisy random offsets.

## Caste Roster

| Sigil | Tier | Awakened caste | Silhouette and anatomy | Living motion |
| --- | ---: | --- | --- | --- |
| Reservoir | 1 | Pendulous Ampulla | A heavy vertical bladder suspended by four tendons, with the eye on a short forward prow. The grounded diamond is retained. | The sac hangs below its route, swelling and settling while the suspension tendons contract in sequence. |
| Suture | 1 | Needle-thread | A long tendon filament with alternating transverse hooks and a hardened leading tip. The grounded crossing stroke is retained. | It advances eye-first in quick sewing strokes; opposing hook pairs alternate as though pulling tissue closed. |
| Bastion | 2 | Contractile Shield | An upright crescent slab with internal muscle ribs and three anchoring tendons. Its existing hexagonal ground path folds vertically. | It patrols broadside with little banking, keeping the shield face outward while anchor tendons provide measured thrust. |
| Shunt | 2 | Arterial Fork | An exposed Y-shaped body with a pump at the bifurcation and a valve mouth on each branch. It has no enclosing membrane. | Acceleration travels outward as a pulse; the two branch valves alternate, visibly redirecting flow. |
| Mnemonic | 3 | Knotted Recall Ribbon | A living neural band that loops through itself, with ganglia partly hidden where the loops cross. | The ribbon turns through its own knot while ganglion pulses travel around a figure-eight route. |
| Seal | 3 | Five-lipped Shutter | Five muscular lobes surround a central throat-organ, with the shared eye riding one forward lip. | It moves deliberately while the lips breathe open and clamp shut in a staggered sequence. |
| Cage | 4 | Walking Rib Tower | A tall, open lantern of ribs suspended between upper and lower muscular rings. It is explicitly not a rounded cell body. | It remains vertical; the rings counter-rotate while individual ribs lengthen and shorten like a walking mechanism. |
| Hematic Lattice | 4 | Vascular Arbor | A branching three-dimensional circulation tree with inner valve nodes and capillary-like tips. It has no enclosing body. | Sequential contractions flow through the branch network and drive its established four-lobed route. |
| Lens | 5 | Optic Stalk and Veil | The central ground node becomes an oversized forward eye on a flexible stalk. The eight surrounding nodes form a trailing sensory veil. | The eye leads, the stalk bends, and the veil trails, cups, and twists in delayed response to movement. |

### Grounded Shape Revisions

The grounded forms remain rigid surface inscriptions. Five paths will be revised
to make their eventual caste anatomy legible before awakening:

- **Shunt:** a clearer branching Y.
- **Mnemonic:** interlocked loops.
- **Seal:** an iris-like stroke rather than a square.
- **Cage:** paired rails with cross-rungs.
- **Hematic Lattice:** an explicit branching network.

Reservoir, Suture, Bastion, and Lens retain their current grounded paths.

All revisions preserve the current node count and sequential drawing order.
Therefore blood cost, learned-node progress, correctness checks, and save data do
not change.

## Data Model

The existing sigil JSON gains two optional concepts:

1. `connections` describes visible grounded edges independently of drawing
   order.
2. `awakened_form` describes the anatomical rig.

Node array order remains authoritative for drawing order, blood expenditure, and
progress. When `connections` is omitted, the existing sequential edge behavior
is used. Explicit connections only alter the visible grounded edge graph.

An illustrative definition shape is:

```json
{
  "nodes": [
    { "x": 0.0, "z": -1.0 },
    { "x": 0.8, "z": 0.5 },
    { "x": -0.8, "z": 0.5 }
  ],
  "connections": [
    [0, 1],
    [0, 2]
  ],
  "awakened_form": {
    "forward": [0.0, 0.0, -1.0],
    "animation": {
      "style": "arterial_fork",
      "pulse": 1.0,
      "flex": 0.7,
      "lag": 0.25
    },
    "nodes": [
      { "source": 0, "position": [0.0, 0.0, -0.7], "role": "eye" },
      { "source": 1, "position": [0.55, 0.15, 0.35], "role": "valve" },
      { "source": 2, "position": [-0.55, -0.1, 0.35], "role": "organ" }
    ],
    "vessels": [
      [1, 2]
    ],
    "membranes": []
  }
}
```

The final field names may follow the codebase's serialization conventions, but
the schema must preserve these semantics:

- Every grounded node maps exactly once to an awakened node.
- Every awakened node identifies a landmark role and a local-space target
  position.
- Exactly one node has the `eye` role and exactly one has the `organ` role.
- `vessels` contains optional secondary connections; original sequential
  connections always remain primary tendons.
- `membranes` contains optional triangles referencing node indices.
- `forward` is the canonical local-space facing direction.
- Animation style selects caste-specific deformation; pulse, flex, lag, and
  similar values tune it without embedding behavior in the renderer.
- Authored target positions may be asymmetric.

Parsed and validated rigs are cached with the existing sigil definition
registry. The full definition, including new optional fields, continues to sync
through the existing client knowledge packet.

## Runtime Architecture

### Entity and movement

`AwakenedIchorianSigilEntity` remains responsible for sigil identity, ritual
center, orbit state, age, and authoritative movement. Existing orbit functions
and rite-size scaling remain unchanged.

The renderer derives a smoothed facing frame from recent client movement. It
retains the last reliable frame when velocity is too small to determine a new
one.

### Anatomical pose

A dedicated pose calculator receives:

- the validated anatomical rig,
- partial-tick entity age,
- transformation progress,
- the stable movement-facing frame, and
- the selected caste animation parameters.

It returns local or world-space positions for every original node plus
visibility, thickness, tension, membrane inflation, organ pulse, and eye state.
This keeps interpolation and anatomy testable without invoking rendering code.

### Caste animation

Caste animation strategies deform the target rig after its base movement-facing
transform. They control characteristic motion such as valve alternation, rib
walking, lobe clamping, ribbon circulation, or veil lag. They do not control
world movement or gameplay state.

### Geometry rendering

The renderer consumes the calculated pose and draws:

- bounded-segment organic tubes for primary tendons and secondary vessels,
- simple indexed membrane triangles,
- landmark geometry for joints, valves, organ, tips, and eye, and
- the transformation growth state.

Procedural geometry is preferred over body-forming particle clouds. Ambient
particles may accent the creatures but must not be required to define their
silhouette.

## Validation, Fallback, and Compatibility

An awakened rig is invalid if it has missing or duplicate source-node mappings,
out-of-range vessel or membrane references, no unique eye, no unique organ,
non-finite coordinates, or an unknown required animation style.

Invalid definitions should log a useful resource-location-specific diagnostic
and use the existing generic floating organic sigil renderer. Definitions with
no `awakened_form` also use that fallback. A malformed optional rig must not make
the sigil unusable or prevent resource reload.

Existing worlds and active entities remain compatible because the entity saves
the sigil resource identity rather than serialized anatomical geometry. Existing
JSON remains valid because both new fields are optional. Network decoding must
remain compatible within the mod's normal matched-client/server version model.

## Performance Constraints

- Use one existing awakened entity per sigil; do not create child entities for
  limbs or organs.
- Cache parsed rigs and static topology.
- Bound tube subdivision counts and reuse existing procedural vessel techniques.
- Render membranes as simple indexed surfaces.
- Avoid particle density proportional to body volume or rite radius.
- Perform caste pose calculation once per rendered entity per frame, then share
  the result across vessels, landmarks, membranes, eye, and organ passes.
- Cull with a bounding volume large enough for the tier-five rig and its
  authored soft-tissue motion.

The intended body-size progression is approximately:

- Tier 1: 0.8 blocks
- Tier 2: 1.0 blocks
- Tier 3: 1.2 blocks
- Tier 4: 1.4 blocks
- Tier 5: 1.6 blocks

These are overall readable extents, not uniform scale mandates. A narrow caste
may be longer in one dimension and smaller in the others as long as it remains
within the appropriate visual tier and movement bounds.

## Testing

### Automated tests

- Parse and client-sync sigils with and without `connections` and
  `awakened_form`.
- Reject or fall back for duplicate mappings, missing mappings, invalid
  references, multiple eyes, multiple organs, missing eyes, missing organs,
  non-finite values, and unknown animation styles.
- Verify every authored original node maps exactly once into the anatomical rig.
- Verify pose endpoints reproduce the grounded form at transformation start and
  reach the authored living form at completion.
- Sample intermediate ticks to ensure finite, continuous node motion without
  discontinuous model swaps.
- Verify tier scaling remains within the intended size bands and entity render
  bounds contain every posed landmark.
- Verify explicit grounded connections affect rendering only; node order,
  correctness, cost, and progress remain unchanged.
- Verify old JSON definitions, saved awakened entities, and the generic fallback
  continue to function.
- Run focused Ichorian sigil tests followed by the complete JVM test suite.

### In-game visual verification

Inspect all nine castes in grounded, transforming, idle, accelerating, turning,
and near-stationary states. Confirm:

- each caste is recognizable from silhouette before inspecting fine detail;
- all castes visibly belong to the same brood through their eye, organ, vessels,
  and material treatment;
- none reads primarily as a generic cell or a direct copy of an animal;
- every original node can be tracked into a living landmark;
- the ground path does not blink out during awakening;
- the dark-crimson eye remains readable without becoming an oversized glowing
  orb;
- membranes and secondary tissue do not hide the primary node skeleton;
- movement remains legible at normal ritual camera distances;
- the forms stay inside their existing rite-scaled movement space; and
- performance remains stable when the largest normal set of awakened sigils is
  active.

## Acceptance Criteria

The feature is complete when all nine current sigils have validated data-driven
anatomical rigs, continuously unfold from their grounded paths, present the
approved distinct caste silhouettes and motions, share the brood's single
dark-crimson-eye anatomy, retain existing gameplay and persistence behavior, and
pass both automated and in-game verification without relying on dense particle
effects to convey their bodies.

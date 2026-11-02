# Shared Particle and Rank Standards

Implemented effects use existing textured blood meshes and player render layers. They add no particle emitters, so the ranges below remain prototype ceilings rather than new per-cast allocations. Existing spell emitters retain their shared budget. Minimal particles removes large casting anatomy; Decreased reduces it to chest vessels. Model poses remain visible in both settings. Large anatomy is culled beyond 32 blocks and simplified beyond 24 blocks.

## 1. Blood-first material hierarchy

Every manipulation effect should be assembled in this order:

1. **Blood material:** liquid streams, beads, ribbons, clots, vessels, membranes, sprays, pools, or hardened blood.
2. **Tendency behavior:** ignition, conduction, illumination, necrosis, freezing, metallization, shadowing, or living growth.
3. **Rank amplification:** increased bodily involvement, spatial reach, persistence, deformation, and environmental response.

Suggested visual balance:

- **70–80%:** crimson, burgundy, black-red, or recognizable blood material.
- **20–30%:** tendency color and secondary phenomenon.

This is a direction rather than a shader-level formula. The test is whether a still frame reads first as hemomancy and only then as fire, nerves, light, death, cold, iron, shadow, or life.

## 2. Shared particle vocabulary

| Particle family | Purpose | Typical behavior |
|---|---|---|
| **Blood bead** | Smallest readable blood unit | Suspends, orbits briefly, then rejoins a stream or falls |
| **Viscous ribbon** | Shows direction and cast motion | Stretches between caster and aim direction; narrows before release |
| **Vascular branch** | Shows shaping and high-rank anatomy | Grows in bifurcating paths; pulses from origin to tip |
| **Clot fragment** | Shows compression, damage, death, or iron content | Dense, slow, irregular motion; may break or harden |
| **Membrane sheet** | Shows barriers, Lux revelation, or advanced Animus growth | Thin translucent red surface with visible capillary web |
| **Impact droplet** | Confirms contact | Short radial spray followed by gravity or target adhesion |
| **Blood pool** | Persistent area and Tenebris origin | Spreads shallowly, feeds upward tendrils, then retracts or coagulates |
| **Heart pulse** | Rank and rhythm cue | One expanding pressure wave synchronized with a torso contraction |

## 3. Rank escalation

| Rank | Body use | Particle scope | Deformation and manifestation |
|---|---|---|---|
| **Humilis** | One arm plus minor head/torso support | One stream or small cluster close to the caster | None beyond a brief vein glow |
| **Mediocritas** | Both arms and torso | Two or more shaped streams; clear tendency accent | Minor vascular overlay |
| **Summa** | Full-body stance and recovery | Local field, construct, or dense release | First temporary anatomical alteration |
| **Magister** | Authored containment and resisted release | A stable loop when the existing cast type sustains | Progressive deformation and looping containment structure |
| **Perfectus** | Iconic still pose | Large but organized circulatory apparition | World-scale anatomy appears to perform the cast |

Perfectus should not merely multiply particle count. It should replace chaotic density with one legible, monumental structure.

## 4. Provisional particle budgets

These figures are prototype targets, not fixed balance requirements.

| Rank | Suggested client-visible budget |
|---|---|
| Humilis | 6–14 short-lived particles per quick cast |
| Mediocritas | 12–28 particles plus at most two short ribbons |
| Summa | 25–50 particles plus one bounded construct or anatomy layer |
| Magister | 8–18 particles per second while charging, with a firm live-particle cap |
| Perfectus | 40–80 initial particles supporting one major mesh, ribbon system, or apparition |

Prefer reusable meshes, ribbons, or animated render layers over hundreds of independent particles. Provide reduced-particle settings without removing cast telegraphing.

## 5. Timing expectations

Each non-passive cast may expose up to four phases:

1. **Anticipation:** the body assumes the tendency pose; blood first appears.
2. **Shaping or hold:** blood takes on the tendency behavior.
3. **Release:** the gameplay event and strongest directional motion coincide.
4. **Recovery:** blood retracts, falls, coagulates, evaporates, or is reabsorbed.

Quick casts start at release after successful gameplay; they never wait for anticipation. Charged casts use the existing duration and damage reduction. Continuous casts retain one playback clock across paid pulses. A fifth cancellation track returns each school to rest without executing a release. Rank never determines cast type.

## 6. Shared rig constraint

The base Minecraft-compatible player rig contains:

- Head
- Torso
- Left arm
- Right arm
- Left leg
- Right leg

Do not require fingers, wrists, elbows, knees, or articulated spine segments. Additional anatomy belongs to temporary render layers, attachments, meshes, or particles.

## 7. Mixed tendencies

- Primary tendency controls the skeleton animation and most particles.
- Secondary tendency contributes one accent behavior, accent color, or recovery event.
- Secondary visuals should not exceed roughly one quarter of the total effect.
- Never overlay two complete particle systems without a bespoke reason.

## 8. Implementation questions

- Which effects use ordinary particles, ribbons, temporary entities, or custom render layers?
- Can animation layers preserve locomotion during Humilis and Mediocritas casts?
- Which ranks may temporarily lock movement?
- How are charge duration and particle intensity synchronized in multiplayer?
- What first-person arm animation is necessary for each base tendency?
- How should effects degrade under reduced-particle settings?
- Can deformation attachments reuse a shared vascular mesh library?

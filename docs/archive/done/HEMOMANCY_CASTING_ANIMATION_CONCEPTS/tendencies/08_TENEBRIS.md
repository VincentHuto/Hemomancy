# Tenebris Casting Animation and Particle Brief

Implemented coverage includes all five ranks, with separate first-person tracks and school-specific cancellation. These storyboards remain material and silhouette references; quick casts execute gameplay immediately and begin their visual release afterward. Rank does not imply charging or channeling. The runtime uses six rigid player parts and bounded anatomy meshes. See [implemented behavior and preview](../../../../wiki/Casting-Animations.md).

![Tenebris rank progression](../images/08_tenebris_rank_progression.png)

## Identity

Tenebris darkens blood until it absorbs light, loses reliable position, and behaves as a displaced shadow. Its magic remains wet, vascular, and bodily beneath the darkness.

- **Dominant material:** Glossy black-red blood, burgundy ribbons, pools, and shadow-veins.
- **Secondary accent:** Deep-purple oily iridescence along edges.
- **Motion character:** Conceal, twist, displace, unfold.
- **Silhouette rule:** Torso, head, and arms deliberately imply conflicting directions.

## Base animation language

- Torso twists away from the intended destination.
- Head counter-rotates toward it.
- One arm crosses the head or torso while the other trails behind.
- Arrival or reversal begins from a mirrored departure pose.
- Perfectus separates the physical caster's stillness from an independently moving blood-shadow.

## Rank progression

| Rank | Animation | Blood manifestation |
|---|---|---|
| Humilis | Small concealing twist behind one arm | One black-red blood ribbon obscures part of the body |
| Mediocritas | Strong asymmetrical fold using both arms and torso | Two dark streams bend around the silhouette |
| Summa | Full-body displaced stance | Detached blood-shadow and delayed burgundy outline |
| Magister | Charged pose partially dissolves toward the ground or nearby darkness | Blood pool, curtain, and delayed silhouettes accumulate around the caster |
| Perfectus | Physical player becomes perfectly still | Enormous independent shadow made from flowing black-red blood performs the cast |

## Particle expectations

- Blood surfaces are nearly black but retain red specular highlights.
- Purple appears only as a thin oily sheen or deep internal glimmer.
- Pools feed vertical ribbons and detached silhouettes.
- Teleports retract blood into the departure shadow and spill it outward at arrival.
- Silhouette particles should lag behind or move opposite the player to create displacement.

## Cast-purpose variants

- **Concealment:** A black-red membrane folds over the body before becoming transparent.
- **Teleport:** Blood collapses into a pool or ribbon at departure and unfolds in reverse at arrival.
- **Reversal:** A delayed blood silhouette remains at the original position and completes the opposite movement.
- **Synthetic darkness:** Vascular cords build the boundary before a light-absorbing blood membrane fills it.

## Avoid

- Purple smoke as the primary material
- Generic black holes
- Featureless darkness
- Symmetrical poses
- Teleportation with no remaining blood trace

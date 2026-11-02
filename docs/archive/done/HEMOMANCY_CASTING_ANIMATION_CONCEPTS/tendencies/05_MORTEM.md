# Mortem Casting Animation and Particle Brief

Implemented coverage includes all five ranks, with separate first-person tracks and school-specific cancellation. These storyboards remain material and silhouette references; quick casts execute gameplay immediately and begin their visual release afterward. Rank does not imply charging or channeling. The runtime uses six rigid player parts and bounded anatomy meshes. See [implemented behavior and preview](../../../../wiki/Casting-Animations.md).

![Mortem rank progression](../images/05_mortem_rank_progression.png)

## Identity

Mortem removes vitality, incurs debt, and turns living blood into old matter. Blood begins red and becomes darker, clotted, and faintly ruinous green as it is extracted or exhausted.

- **Dominant material:** Black-red blood, heavy clots, and emptied vessels.
- **Secondary accent:** Sickly dark green staining.
- **Motion character:** Hook, extract, contract, collapse.
- **Silhouette rule:** The body appears to haul something inward at a cost.

## Base animation language

- Torso hunches and head lowers.
- One arm sweeps across the target direction as a rigid hook-like silhouette.
- The arm retracts sharply toward the torso.
- The second arm braces or closes around the extracted blood at Mediocritas.
- Recovery lets both arms and torso sag.

## Rank progression

| Rank | Animation | Blood manifestation |
|---|---|---|
| Humilis | One-arm sweep and inward pull | Thin red stream darkens as it reaches the caster |
| Mediocritas | Second arm braces the extraction | Two coagulating ribbons and falling clots |
| Summa | Full-body contracted stance | Draining veins spread across caster and affected space |
| Magister | Held inward pull; posture progressively collapses | Dense suspended mass of old blood and necrotic vascular cords |
| Perfectus | Bowed caster remains still | Vast cadaverous circulatory apparition performs the extraction overhead |

## Particle expectations

- Particles move toward the caster or a debt vessel rather than radiating outward.
- Fresh red changes gradually into burgundy, black-red, then green-stained clot.
- Clots fall faster and move less responsively than ordinary blood beads.
- Empty translucent vessel outlines may remain briefly after blood is removed.
- Debt effects should preserve a tether until repayment or consequence resolves.

## Cast-purpose variants

- **Damage-over-time:** Thin draining vessels remain attached for the readable duration.
- **Execute:** A strong inward pull collapses several streams into one clot at the caster.
- **Blood gain:** Extracted fluid visibly enters the torso rather than vanishing on contact.
- **Debt:** Borrowed blood forms an external suspended mass or tether whose condition remains visible.

## Avoid

- Green fog as the primary effect
- Generic souls
- Scythes or necromancer props
- Blood becoming invisible during extraction
- Effortless upright casting

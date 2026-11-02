# Animus Casting Animation and Particle Brief

Implemented coverage includes all five ranks, with separate first-person tracks and school-specific cancellation. These storyboards remain material and silhouette references; quick casts execute gameplay immediately and begin their visual release afterward. Rank does not imply charging or channeling. The runtime uses six rigid player parts and bounded anatomy meshes. See [implemented behavior and preview](../../../../wiki/Casting-Animations.md).

![Animus rank progression](../images/01_animus_rank_progression.png)

## Identity

Animus expresses blood as living circulation: heartbeat, pressure, growth, pumping, and expansion. It must not read as generic red energy. Blood remains glossy, liquid, warm, and visibly alive.

- **Dominant material:** Bright arterial red blood and branching vessels.
- **Secondary accent:** Slight inner crimson glow; no separate elemental color required.
- **Motion character:** Contract, pulse, expand.
- **Silhouette rule:** The body closes toward its center before opening outward.

## Base animation language

- An arm draws toward the torso as the body contracts.
- The torso pitches slightly forward at peak compression.
- One or both arms thrust or sweep outward on the heartbeat release.
- Legs become involved at Summa by establishing a wide, living brace rather than a rigid defensive stance.
- Recovery resembles a second, weaker heartbeat.

## Rank progression

| Rank | Animation | Blood manifestation |
|---|---|---|
| Humilis | One arm pulls inward and releases; small torso pulse | One orbiting bead or narrow stream |
| Mediocritas | Both arms pump outward in a coordinated release | Paired arterial ribbons branching from the torso |
| Summa | Full body contracts, legs brace, arms open | Swollen vascular overlay and localized capillary growth |
| Magister | Repeating charged heartbeat loop; posture strains against each pulse | Arterial thicket thickens around the body with each beat |
| Perfectus | Caster becomes still in an open pose | A colossal anatomical heart and circulatory tree contract around the caster |

## Particle expectations

- Streams should pulse in discrete waves rather than flow at a constant speed.
- Droplets briefly expand on each heartbeat, then accelerate outward.
- Branching vessels grow from thick trunk to thin capillary tips.
- Blood may climb upward against gravity to communicate vitality.
- Recovery particles retract or rejoin the caster instead of evaporating.

## Cast-purpose variants

- **Projectile:** Heartbeat drives a compressed blood bead along the aiming arm.
- **Self-buff:** Two streams complete a circuit around torso and limbs.
- **Area pulse:** Blood pressure ring expands from the torso and leaves droplets suspended at its edge.
- **Growth or utility:** Capillaries spread into the target surface before the effect occurs.

## Avoid

- Generic red aura
- Fire-like motion
- Undirected gore spray
- Plant vines without visible vascular structure
- Constant particle emission without a pulse rhythm

# Cast Impacts — design (2026-10-19)

The user asked to build this straight away and skipped the design Q&A. Every call below is mine (Claude's) unless marked otherwise.

## Intent
A cast has a body pose, blood pouring from the caster's veins, and then the projectile or effect. What happens at the target is mostly generic. This design gives every school hit a short, authored **impact** at the contact point. It is built like Hematic Riposte's separate success flash. There is one look per tendency, varied by how the effect lands.

## Goals
- One system that covers every school hit. It hooks the existing choke point, `SchoolCombatEvents.commit`, which handles every DIRECT school hit that dealt health or absorption damage.
- Eight looks, one per `EnumBloodTendency`, drawn on the mod's own materials (Lux/Umbra/Thermal batch, iron, cruor glass) following the Riposte recipe. Vanilla particles are not used.
- Four delivery shapes: MELEE, PROJECTILE, AREA, TETHER. They are chosen server-side at hit time.
- A short life of 10 ticks: flash (0–2), burst (1–6), fray (5–10). The impact never pops off, and visual timing never gates gameplay.

## Non-goals
- No gameplay or damage changes.
- Effect-only manipulations (bindings, wound, paralysis with no damage) and block hits get no impact. Block splashes already exist.
- The bespoke impacts (Riposte strike, Blood Shot splash, aneurysm rupture and so on) are not replaced. This system layers under them.
- The reaction payoff bursts in `commit` (CRUOR_BREAK, MORTEM_BURST, ...) stay as they are and still mark reactions.

## Anti-goals
- Spam from repeated ticks. PERIODIC and REACTION hits are skipped, and each target gets at most one impact per 5 ticks. A heavy hit bypasses that limit.
- Constructs floating near the caster. The impact lives only at the target.
- Particle clouds as the main visual.

## Server
- `ManipulationVisuals.Form.CAST_IMPACT` is appended to the end of the enum, because forms are encoded by ordinal.
- `CastImpactRules` is a pure class. It holds:
  - delivery classification;
  - count encoding (primary, secondary + 1, delivery, weight tier);
  - the per-target throttle;
  - the weight tier taken from damage;
  - the timeline constants.
- Delivery is decided in this order:
  1. The direct entity differs from the attacker: PROJECTILE.
  2. The ability is not a registered manipulation (so a tendency weapon or Will melee): MELEE.
  3. Otherwise, by `CastPresentation.forId(id).purpose()`:
     - PROJECTILE → PROJECTILE
     - AREA, SELF, CONSTRUCTION → AREA
     - TARGETED → TETHER
     - CONJURATION, TRAVEL → MELEE
- The contact point is the target's bounding box clamped toward the source: the projectile's position, or the attacker's eye. If the source is inside the box, it falls back to the target's centre of mass. The direction is from source to contact.
- The packet is sent with `ManipulationVisuals.burst(level, CAST_IMPACT, contact, contact + dir, radius, 10, count)`. Radius scales with the target's width.

## Client: `CastImpactGeometry` (follows the Riposte recipe)
Shared layers:
- a contact flash (a FOCUS card shrinking, with a hot pale core);
- the delivery shape in the primary school's material;
- the school flourish;
- two thin rays in the secondary school's material.

| Delivery | Shape |
|---|---|
| MELEE | Three parallel cross streaks (`slashOffsets`) plus rays sprayed along the blow |
| PROJECTILE | An expanding entry ring perpendicular to the travel direction, a forward spray cone and one back-splash streak |
| AREA | A horizontal ripple ring expanding round the target plus short upward rays |
| TETHER | A taut strand snapping in from the source side, a knot cinching at the contact point, and barbs |

| School | Flourish |
|---|---|
| Animus | Clot droplets flung on arcs (dark clot plus pale core) |
| Flammeus | Flame tongues licking up, turning to smoke at fray |
| Congeatio | Cruor ice shards bursting out and turning |
| Ductilis | Flickering jagged nerve bolts |
| Lux | A white-gold starburst and a larger flash |
| Mortem | Slow sinking blobs; everything moves at about 0.6× speed |
| Ferric | Solid iron splinters and molten sparks |
| Tenebris | Rays collapse inward (implosion), then a small dark puff |

The weight tier scales size by 0.75, 1 or 1.35, and scales the ray counts too.

## Verification
- JVM rule tests.
- A source contract confirming the hook and the absence of vanilla particles.
- Review-fixture scenes for 8 schools × 4 deliveries, sent through the casting review harness behind `-PcastingImpactsOnly=true` and captured from the observer.

## Calls made during the build (after the first captures)
- **Position and direction.** The contact point sits about 0.08 blocks outside the struck surface. Sprays and flourishes leave the wound outward, back toward the source; spraying along the travel direction buried them in the body. The contact height is clamped to between 25% and 80% of the target's height.
- **Size.** About 0.9 × radius × weight scale.
- **Melee.** The melee rake runs diagonally across the struck face, so it reads both side-on and from the attacker's eye.
- **Congeatio.** The flash uses the LUX material, because cruor draws a square card. The ice shards use the GLASS material and read as blood-ice.
- **Review fixes:**
  - Only real `Projectile` entities count as PROJECTILE. Clouds, pillars and constructs land as their cast.
  - The hook runs last in `commit`.
  - Weapon detection uses a registry `containsKey`.
  - Expiring impacts emit no default motes.
  - Each outpouring screen pass clears only its own school's casters.

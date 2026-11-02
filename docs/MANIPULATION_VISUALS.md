# Manipulation visuals: the Hematic Riposte standard

Current standard: 2026-10-09. Hematic Riposte's blood-arm (`client/render/world/HematicRiposteGeometry.java`) is the reference for focal manipulation, rite, boss and impact visuals. Read that file before building or upgrading one. Bring older effects up to this bar when they are touched; do not copy them as templates. Current source remains authoritative; update this page when the standard intentionally changes. For every existing effect's current grade and the ordered touch-up list, see [the manipulation visual catalogue](archive/audits/MANIPULATION_VISUAL_CATALOGUE.md).

## Structure

- Give each focal effect its own `final`, package-private, static-only geometry class in `client/render/world/`. It needs `handles(Form)` and a `draw(packet, poses, vertexConsumer, camera, right, up, time, age, alpha, frame)` entry point.
- Hook it into `ManipulationVisualRenderer` **before** the shared `AnimusMortemGeometry` branch. Draw on the school's material (for example `FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS)`) so the school shader and texture supply the colour.
- Build from the `LuxUmbraGeometry` primitives: `ribbonPath` for spines, strands and trails; `card` for camera-facing glows and wisps; `stream` for streaks; `slashOffsets()` for parallel claw marks. Use the `RIBBON`, `WISP`, `FOCUS` and `SLASH` sprites.
- Keep timing, encoding and direction maths in a pure rules class with no Minecraft types (`HematicRiposteRules`), so JVM tests can cover it.

## Animation

- Author a full timeline with named phase constants instead of relying on the shared entrance fade. Riposte uses emerge 0–2, sweep 2–6, strike 6–8 and dissolve 8–16 ticks. The shared formation fade swallows the first ticks of a fast effect, so draw at full opacity until the cue retires.
- Ease each phase differently: `smooth` (smoothstep) for growth, cubic `easeInOut` for travel, a `sin` overshoot for the impact.
- End by fraying, not popping: during dissolve, widths grow, alpha falls and strands drift upward.
- Vary each cast: pack a seeded side/tilt into the packet `count` (keep it ≥ 1 so the cue is not treated as empty). Retire the keyed cue before sending a new one (`attached(entity, form, 0, 0, 0)` then the real cue) so rapid recasts replay the whole animation instead of refreshing a dying one.

## Anatomy and layering

Draw from back to front:

1. **Shell:** a broad translucent ribbon (alpha about 0.28) along a cubic-Bezier spine, plus flickering `WISP` cards trailing *against* the motion vector, so the sheath reads as flame-like.
2. **Sinew:** three helical strands twisting round the spine. They taper in three segments (0.07 → 0.05 → 0.035 × scale) and pulse with a heartbeat, `pow(max(0, sin(time * 0.6)), 4)`.
3. **Knots:** `FOCUS` cards at the root, elbow and wrist. The root bursts larger as it erupts.
4. **Business end:** a palm card and four fanned claws. Each claw is drawn in three passes: a thick `SLASH` base, a dark clot-coloured tip, and a thin pale core line. The claws curl inward through the strike.
5. **Motion:** a trail ribbon sampled from the last few poses, and parallel claw streaks on the hit frame.

Give each element its own seeded flicker (`sin(time * 0.9 + i * 1.7 + seed)`) so repeated parts never pulse in lockstep.

## Colour

- Base tint is white (`0xFFFFFF`); the school material provides the hue.
- Add exactly two accents: a hot pale core for highlights (`0xFFD2C4`) and a dark clot for depth (`0x7A1020`).
- The shading comes from contrast between translucent layers (shell, wisps) and opaque ones (sinew, claws). Do not substitute a wider palette.

## Anchoring

- In the renderer, capture `bodyYaw`, `headYaw`, `scale` and `crouching` on the cue each frame. Root the effect on the body and aim it with the head, so it reads as part of the wearer and crosses the first-person view.
- Express points as world-axis offsets from the wearer's feet so camera-facing cards stay true, and multiply every size by the entity scale.
- In first person, skip the near part of the effect (Riposte drops the first 40% of the spine and the root knot) and lower it slightly so it never walls off the view. Hide the effect when the source entity is invisible.
- Hand- and body-anchored casts take a `CasterFrame` (add the form to `ManipulationVisualRenderer.bodyAnchored`). Its `hand` is the posed third-person main hand, which `CastingAnimationLayer` records every frame, cast or not; in first person it is the drawn cell hand's palm (`FirstPersonPalm`, shared with the cell particles) or vanilla's first-person item position, and unrendered casters fall back to an estimate in front of the eyes. A Living Staff channelling Blood Absorption or Projection sends the same `CELL_HAND` use cue as the bare hands and moves the hand to the staff's drawn head (`CellHandStreams.staffHead`, from `HeldWeaponPose.top`), so streams, absorbed cells and particles all meet there; other players' staff mode is read from that cue because their selected utility is not synced. The 1.21 first-person hand pose already carries the inverse camera rotation, so posed points are world-axis offsets from the camera: never rotate them by the camera again. A cast that forms on a held item should draw on that item's real faces in its real held pose (`HeldWeaponPose`), not on an authored approximation of it. In first person, drop soft `WISP` glow cards near the hand: magnified that close, they turn blocky.

## Impact

- Use a separate short burst form at the contact point (`RIPOSTE_STRIKE`). Encode variants in `count`: radiating rays with a cross-slash for melee, a deflection streak for projectiles.
- Layer HutosLib/Hemomancy accents on top: `PacketHandler.sendClawSlash`, `HemoParticleData.hitGlow` and `bloodCell`. Particles are accents and must stay readable with Minimal particles; the geometry carries the effect.

- Every landed school hit already gets a shared cast impact (`CAST_IMPACT`, `CastImpactGeometry`): a flash, the delivery shape and the school flourish. A bespoke impact layers over it. Build the bespoke one when a manipulation needs its own signature, not to make a hit visible at all.

## Casting outpouring

Casting effects on the caster come from the caster's own blood: it leaves the manipulation's vascular section, veins light under the skin, and the blood turns into the school's material. Never draw detached constructs (wings, figures, rings of shapes) beside or behind a caster. Spells that put their own blood on the body (Riposte, Coronation, avatar, staff forms) are in `OutpouringRules.woundOnly`, so the outpouring steps down to veins and the exit wound.

## Server and visual split

Visual timing never gates gameplay. The server owns windows, damage and cooldowns, and the presentation timeline is decorative. Pair new geometry with a source-contract test that its renderer hook and draw order are in place, plus JVM tests for the pure rules.

No vanilla `ParticleTypes` in `common/manipulation/`: use a `ManipulationVisuals.Form` or `HemoParticleData`. `ManipulationVanillaParticleSourceTest` fails the build if one comes back.

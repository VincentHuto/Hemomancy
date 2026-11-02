# Ring door: opened by held Blood Projection

## Why

Right-clicking the ring door opened it at once: an Apotheos's click shattered it with no warning, which reads as
the door vanishing, and an irreversible choice (an enraged Saint, a fragment drop) was one stray click. The door
should be opened the way Hemomancy opens things, by projecting blood into it, and the opening should look like an
infusion: something spreading across the door from where the player aims.

## What the player does (user's decisions)

- Right-clicking the door no longer opens it, on either path. It only tells the player the door answers blood.
- The player **holds Blood Projection on the door**. Opening takes **10 seconds of held projection** (200 ticks).
- Letting go does not reset the door: its progress **slowly bleeds off**, so stepping back in resumes from where it is.
- **Both paths cost 1000 blood**, drawn steadily while held (5 a tick), not in lumps.
- **Silentium:** every Chapel node must be lit. A blood stream runs from the hand to the hit point; infusion-style
  veins spread across the door from that point in the usual blood look, and behind the spreading front the blood
  turns into the black-and-red **monolith** look. When the whole door has turned it sinks open properly.
- **Apotheos:** may open it at any time. **Hyphae grow from the player's hand to the hit point**; from that point
  white-yellow hyphae spread across the door and crack it, and at full progress it explodes outward (forced
  opening: enraged Saint, skipped Chapels recorded, fragment drop, as before).

## Rules (my calls, approved in chat)

- One meter, `charge` 0..200. A tick fed by an eligible player adds 1 and pays 5 through the door's `ring_door`
  source. The door takes at most one feed per game tick, whoever gives it.
- With no feed for a few ticks (grace 4) the meter bleeds 1 every 2 ticks: a full door drains in 20 s. At 0 the
  door is idle again. Blood already paid is not refunded.
- While a door is charging for one path, a player of the other path is refused (the door is already answering
  someone).
- Refusals reuse the existing messages: not D8 Silentium/Apotheos, nodes unlit (Silentium), not enough blood (now
  names 1000; a projector needs at least one tick's worth).
- A Living Staff's projection counts too (it goes through the same `ProjectedBloodSink`).
- The spread follows the aim: the door keeps the latest hit point and the client glides its shown point toward it.

## Look (my calls)

- One new world shader, `ring_door_opening`, drawn over the slab face and, as a separate layer, over the ring
  well's front, the way the old crack lines were laid. Uniforms: progress, mode, seed, the camera-relative hit point,
  the door's pixel origin and right axis, and the well (centre, radius) plus the spread's full reach.
- Silentium: the infusion vein web (cellular arteries and capillaries, dark wall, beating core) over a thin blood
  wash, growing from the hit point to cover the door at full charge; 14-34 px behind the front it turns into the
  monolith look (black stone with red fractal faults and flowing red veins), with a hot seam where it turns. The ring
  channels fill as the charge passes them (outer, middle, inner), the seam pulses near the end, then the slab sinks
  over 2 s and the door opens; for Hemorath the blood still runs on into his chamber.
- Apotheos: a hyphae tube (the Mycophant hyphae core, pale white-yellow glow) from the hand to the hit point while
  feeding. On the door, a felted mat of branching strands creeps out from the hit point, fraying at its front;
  behind it cracks open (dark fissures with a white-yellow glow inside) that brighten toward the end, the slab
  shudders harder, and at full charge it bursts into the existing fragments with a server burst of spores and glow.
- Shared shader noise goes through includes: `cast_veins`' `fbm` and `cellular` move to `noise/vein_fbm.glsl` and
  `noise/vein_cellular.glsl` and both shaders import them.

## State

`RingDoorBlockEntity` phases become `IDLE`, `OPENING` (charge > 0), `SINKING` (Silentium, after full charge),
`OPEN`. Saved/synced: charge, the path being opened (`DoorMode` PROPER or FORCED), the projector, whether it is
being fed, and the hit point. The client advances the charge between syncs (feeding: +1 a tick; bleeding: -0.5).
`PROJECTING` and `SHATTERING`, the per-ring payment and the old hub crack lines are removed (the mod is not live).

## Not in scope

- Ring rotation and node lighting from Chapels are unchanged. Hallowed Ground, the Saint's enrage rules and the tomb
  record are unchanged.

## Verification

- `RingDoorRulesTest` for the new rules (cost, timing, bleed, ring fills from charge).
- GameTests: a Silentium FakePlayer fed for 200 ticks pays 1000 and the door sinks open (PROPER); an Apotheos fed
  for 200 ticks bursts it (FORCED, skipped Chapels recorded); a door left alone bleeds back to idle; right-click
  does nothing.
- `ShaderNoiseIncludeGuardrailTest` and the build; the shader needs a client launch to prove it compiles.
- Mausoleum review stops for the opening phases.

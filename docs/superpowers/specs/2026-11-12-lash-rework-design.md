# Lash rework: tap, charge, flick

Status: **design for review, 2026-11-12. Nothing built.**

Replaces the lash half of [core-loop Track 12](../../archive/tracks/core_loop/12-strand-combat.md) and retires the unbuilt spike
[12a](../../archive/tracks/core_loop/12a-lash-spike.md). Jet, Spray, Plant and Brand are not touched.

Lines marked **(owner)** are the owner's decisions from the design conversation. Lines marked **(my call)** are the
writer's and are open to change on review. Every number is a starting point for hand tuning.

## Why

The lash built in Track 12 does not feel like a whip, and using it hurts aiming.

- Held Projection puts the strand out at once as a straight line along the look. It reads as a rod.
- The strand is ten nodes that each slide part of the way toward that line each tick (`LashRules.step`). There is no
  momentum, so it can only trail the crosshair and straighten again. No overshoot, no curl, no snap.
- Damage needs a tip speed of 0.6 to 2.4 blocks a tick. On a 7.5 block lash, full damage is about 360 degrees a second
  of camera turn, and a crack needs about 200 degrees a second and then a sharp reversal.
- The lash's only input is the camera's yaw and pitch. Whipping and aiming are the same motion, so a player who
  whips hard enough to hurt cannot see what they are hitting.

## What the player does

### Left click: the tap **(owner)**

With a lash manipulation selected and the Projection tool (hand or Staff) held, and right click not held:

- One quick lash straight down the crosshair, out and back. No mouse swinging.
- Cheaper and quicker than a charged crack, with a lower ceiling.

### Right click held: the charged whip **(owner)**

1. **Charge.** Blood gathers into an orb of orbiting blood in the hand. No strand is out. The view turns normally.
   The longer the hold, the stronger the first crack.
2. **Flick.** A fast mouse movement throws the lash out in the direction of the flick and cracks it. The lash moves
   with the full speed of the hand.
3. **Live.** The strand stays out and swings with the hand, back and forth, for as long as the hand keeps moving.
4. **Fade.** When the hand stops, the strand slowly dissolves back toward the hand. This is a grace period: a new
   flick during the fade picks the strand up again from what is left.
5. **Back to charge.** If the fade finishes, the player is holding the orb again and charges toward a new first crack.

Releasing right click ends it from any state.

### The camera while right click is held **(owner)**

- The mouse drives two things. The lash takes the whole movement. The view takes a capped share.
- A hard flick turns the view only a little. The view then eases to a stop with some drag, as if it had weight. It
  does not chase the lash and does not catch up afterwards. Where it settles is the new aim.
- The lash keeps travelling on its own momentum through the arc.
- Slow mouse movement stays under the cap and turns the view normally **(my call)**. That is how the player re-aims
  between strokes.

### Specialisation per manipulation **(owner)**

Each lash manipulation can lean toward the tap or toward the charged whip. A lash's registration carries its own
numbers for both halves, so one can be a fast cheap tapper with a weak charge and another a slow heavy charger.

## Goals

- A wrist flick, not an arm swing, is enough for a full-strength crack.
- The strand visibly behaves like a whip: the tip lags, overshoots and snaps.
- The player can always see and choose what they hit.
- A player who never swings the mouse has a complete weapon in the tap.
- Feel numbers can be tuned by hand in a running client without a rebuild.

## Non-goals

- The other four strand forms.
- The R-key cast of these manipulations (`SearingLashManip`'s canned burst and the like), entity casts and Vesper's
  borrowed versions.
- New lash manipulations. The three that exist are re-profiled; nothing is added.
- Rebalancing lash damage against swords beyond keeping it in today's range.

## Anti-goals

- Motion sickness. The view must never swing as far or as fast as the whip.
- A whip that only hits where the crosshair is. The point of the split is that the lash can be somewhere the view
  is not.
- Client-decided hits.
- A straight rod at any point in the held mode.

## How it works

### One state machine, shared rules

`LashRules` stays the one pure class the server and every client run, and gains the phases:

| Phase | Entered by | Strand | Costs |
| --- | --- | --- | --- |
| `CHARGING` | Right click held, lash selected | None; orb in hand | Charge blood per pulse |
| `LIVE` | A flick while charging or fading | Out, full physics | Upkeep per pulse, as today |
| `FADING` | Whip aim at rest for a short time | Shortening toward the hand | Nothing **(my call)** |

- A **flick** is the whip aim turning faster than a threshold (start: 6 degrees a tick, about a third of today's crack
  rate).
- **Charge** runs 0 to 1 over 30 ticks **(my call)** and holds at 1. The first crack out of `CHARGING` multiplies
  crack damage by 1.0 to 1.75 by charge, and lays the school state for longer by the same factor. Only that first
  crack takes the bonus; a crack that revives a fading strand does not.
- **Fade** takes 30 ticks **(my call)**. The strand's length shrinks to nothing over it. A flick during the fade
  returns to `LIVE` at the current length and regrows to full over a few ticks.
- The phase is decided on the server from the aim it is sent, and rides on the existing strand cue to clients.

### The whip aim

Today the lash reads `player.getLookAngle()`. It will read a **whip aim** instead: a yaw and pitch of its own.

- On the casting client, raw mouse movement is added to the whip aim in full. The camera receives what the cap lets
  through. While the view and the whip aim differ, the difference is kept; nothing pulls the whip back to the
  crosshair except the fade.
- While `CHARGING`, the whip aim is the look.
- A new serverbound payload carries the whip aim each tick while the phase is `LIVE` or `FADING`, and the flick that
  leaves `CHARGING`. The server refuses an aim further than a limit from its own copy of the player's look (start:
  150 degrees) and clamps the turn per tick, so a modified client cannot aim the lash behind walls or spin it at
  impossible speed.
- The server relays the whip aim to tracking clients so other players draw the same whip. One small payload a tick
  per lashing player, only while the strand is live.
- Usual rules apply: `ServerboundGuards`, `ServerboundRateLimiter`, sends through the `PacketHandler` facade,
  `PROTOCOL_VERSION` bumped and `payload-registry.txt` rewritten.

### The camera cap

- NeoForge's `CalculatePlayerTurnEvent` only scales sensitivity. A cap with drag needs the mouse deltas themselves,
  so this is a client mixin where `MouseHandler.turnPlayer` hands the deltas to the player, with the logic in a
  plain client class the mixin calls. The builder should confirm the exact hook.
- Per frame: the raw delta goes to the whip aim. The camera's turn rate is limited to a cap (start: 4 degrees a
  tick), and excess is not banked. When input stops, the camera's turn rate decays over a few ticks instead of
  stopping dead (the drag).
- Active only in `LIVE` and `FADING`. `CHARGING` and the tap leave the camera alone.
- A client config slider sets how strong the cap is, down to off **(my call)**, for players who want the old
  behaviour or find any camera damping uncomfortable. With it off, the whip aim is the look and everything else
  still works.

### Whip motion

Replace the slide-toward-a-line chain with a chain that carries momentum: the spike doc's model B, a Verlet rope.

- Same node count, root pinned at the hand, driven by the whip aim's direction at the root.
- Each node keeps its velocity, with damping, a small pull toward the aim line so it does not hang limp, the
  existing stretch clamp, and the existing block clipper.
- It must stay deterministic and bounded (clamp node speed), since server and clients each run it.
- The throw out of `CHARGING` is itself the first crack: it always fires and takes the charge bonus. The bonus is
  never banked or lost to a fade.
- **Where the first crack lands (owner).** The lash travels an arc in the direction of the flick. It is thrown from
  the side opposite the flick so that the arc passes through the crosshair. It cracks at the first creature or block
  the strand meets along that arc; if it meets nothing, it cracks at the end of the arc. Cracking on first contact is
  deliberate: it makes the charged hit easier to land. The arc's width, and how far past the crosshair it ends, are
  tuning numbers (start: 60 degrees, centred on the crosshair) **(my call)**.
- **Left click while `CHARGING` (owner).** Releases the charge as a crack straight down the crosshair, with the full
  charge bonus, at no cost beyond the charge already paid. The whip is then `LIVE` along the view. A player who
  never flicks still gets the charged hit.
- After that, a crack is the tip passing a speed threshold as a stroke reverses or ends, read from the chain, not
  from camera turn rate. The free-crack cooldown stays (20 ticks) **(my call)**, so the live whip is a sustained
  weapon with a crack about once a second, not a crack per flick.
- Sweep damage still comes from tip speed between a slack floor and a cap, with both thresholds lowered so a flick
  under the camera cap reaches full damage. The stroke-tiring rule (spinning in place falls to floor damage) stays.

### The tap

- Client: a left click press with a lash selected, the Projection tool held and no item in use sends one payload and
  cancels the vanilla swing for that click. The existing `StrandSnapPacket` path is the model; whether it is widened
  or replaced is the builder's choice.
- The tap takes the click only when the crosshair is on air or a creature **(my call)**. A click on a block inside
  vanilla reach mines as it does today, and a held button keeps mining. A click during the tap's cooldown at air or a
  creature does nothing. The server applies the same claim rules a strand uses, so stations, rite anchors, Saints
  and Wills are not tapped.
- Server: a straight strike from the hand along the look to the lash's length, clipped by blocks. It hits the first
  creature on the line only, one target, no area **(my call)**. Pays through `BloodAccounting.pay` with its own
  `Source`.
- A tap lays no school state by default **(my call)**, so a 20 mL tap never undercuts the R cast. The profile has a
  tap-state duration that is zero unless a manipulation's lean says otherwise (Gloam Laceration's short Obscured).
  Whipcrack raises tap damage, and tap state only where there is one.
- While the whip is `LIVE` or `FADING`, a left click forces a straight crack along the view, as the snap does today
  and at the snap's price, and pulls the whip aim back to the crosshair **(my call)**. This is the recovery move
  when the whip has drifted off target.

### Per-manipulation profile

`StrandSpec` gains a lash profile (or `Strands.lash` takes one): tap damage, tap blood, tap cooldown, charge blood
per pulse, charge time, charged-crack multiplier, tap-state ticks. Starting leans **(my call)**:

| Manipulation | Lean | Tap | Charged whip |
| --- | --- | --- | --- |
| Blood Lash (Animus, teaching) | Even | Cheap, weak | Short charge, modest bonus |
| Searing Lash (Flammeus) | Charge | Ordinary | Long charge, largest bonus, long Searing on the charged crack |
| Gloam Laceration (Tenebris) | Tap | Fast, cheap, lays a short Obscured | Small bonus |

Baseline numbers for an even profile: tap 4.5 damage, 20 mL, 12 tick cooldown; charged crack 9 damage (today's
crack) times 1.0 to 1.75.

### Skills and the twin

- Taut Line, Whipcrack and Long Strand keep their meaning: tip stiffness, accent damage and state, length. Whipcrack
  applies to the tap and to cracks.
- The Second Strand twin follows the same whip aim from the off hand, a little slacker, as today. It does not tap.

### Visuals

- The orb: orbiting blood in the hand that grows and tightens with charge, built from the mod's own toolkit (the
  outpouring and cell-hand material), no vanilla particles. Read `docs/MANIPULATION_VISUALS.md` first.
- The throw: the orb unspools into the strand along the flick.
- The fade: the strand thins and beads back toward the hand; it must read as "going", so the player knows the grace
  period is running.
- The tap: a fast, thin lash with its own short crack flare.
- First-person arm and the third-person `StrandPlayerPose` follow the whip aim, not the look.

### Tuning harness

- Every feel number (flick threshold, camera cap and drag, rope damping and pull, speed thresholds, charge and fade
  times) lives in one tuning record with defaults in `LashRules`.
- The existing `strandReviewClient` gains a mode that re-reads those numbers from a JSON file in the game directory
  when it changes, with a corner readout of tip speed, phase, charge and camera turn rate. The reader lives in the
  gameTest source set. Final values are written back as the defaults.

### Added in the build's review pass (2026-11-12) **(my call)**

- The orb cannot be released by left click until it is half gathered (`releaseMinCharge`, a tuning number), and the
  snap's cooldown follows the player across sessions. Otherwise hold-and-click at once is a free crack that beats the
  tap.
- A lash's pulse is priced by what it did since the last pulse, so a rest timed just before the pulse is not free.
- A throw tests its whole length for contact; a crack does not reach through a block; a tap is not swallowed by the
  target's hurt cooldown.
- The server treats a tick with no whip aim as late, not as a hand at rest, for up to three ticks.
- The view only coasts after a capped flick, and only once the mouse has been silent for half a tick; slow aiming
  never drifts.

## Constraints

- Hits are the server's (`StrandHits`, `ManipulationCombatHelper`). Blood goes through `BloodAccounting`.
- Session state stays in `StrandSession` and clears through `TransientServerStateEvents`.
- Player-facing text is lang keys. The teaching text for the lash (`CoreLoopLessons`, Liber entries, any advancement
  wording) changes with the mechanic.
- Docs to update with the build: `docs/archive/tracks/core_loop/12-strand-combat.md`, `docs/MANIPULATIONS_AND_CASTING.md`,
  `docs/HEMOMANCY_REFERENCE.md`, and CL-22 in `docs/OPEN_QUESTIONS.md`. Mark 12a superseded.
- Tests: `LashRulesTest` is rewritten for the new rules (phases, charge, fade and revive, rope bounded and
  deterministic, thresholds, tiring). `StrandGameTests` lash cases are rewritten against scripted whip aims. Add
  server-side cases for the aim limits and the tap. Existing Searing Lash R-cast tests stay green untouched.

## Left to the builder

- The exact mixin target for the mouse deltas.
- Whether the whip aim travels as a new payload pair or extends existing ones, and its wire format.
- Rope integration details, so long as it is bounded and identical on both sides.
- Class layout inside `common/manipulation/verb/strand/` and the client packages.
- All visual construction, within the visual standard.

## How it is judged

By hand, by the owner, in the tuning client:

- A wrist flick cracks at full strength and the view stays readable.
- The strand looks like a whip in first and third person.
- Tap-only play works as a weapon.
- The fade reads clearly and reviving it feels natural.
- Each of the three lashes feels different to use.

# Track 12a: The lash spike

Status: **plan for review, 2026-11-09. Not built. Building it needs a yes.**

A spike is a throwaway build made to answer a question. This one answers whether a swung lash is fun, before
[Track 12](12-strand-combat.md) is designed any further. It is judged by hand, in game, by the owner. Screenshots
cannot answer it.

## The questions

| # | Question | How the spike answers it |
| --- | --- | --- |
| 1 | Does a lagging tip feel like a whip, or like a laser that is late? | Three tip models to switch between |
| 2 | Does damage from motion feel earned, or like wrist strain? | Speed-scaled damage against flat damage per sweep |
| 3 | Is the left button free while an item is in use, and is a click-to-crack enough for a player who will not swing? | Read the key during the channel; play a round using only clicks |
| 4 | Does "a stroke tires" stop spinning without the player needing it explained? | Toggle it; spin in a ring of targets |
| 5 | How fast must the player move while lashing for it to be a weapon? | Three movement settings |
| 6 | Can the first-person view read the lash, or does the near end cover the screen? | Look at it |
| 7 | Where does the damage land against a sword, and what does it cost? | A debug readout of damage and blood per second |
| 8 | What does it cost the server? | Time the rule per tick; log the average |

## What it is

- **Searing Lash only,** from the Blood Projection hand tool only. No Staff, no other school, no other form.
- **Hold Projection with Searing Lash selected** and a lash comes out of the hand and follows the aim. Sweep it
  through a creature to hit. Reverse sharply, or left click, to crack it.
- **A review client,** one row in `gradle/review-clients.gradle`:

  ```groovy
  lashSpikeReviewClient: [dir: 'build/lash-spike-client', user: 'LashSpike', size: review,
                          props: ['hemomancy.lashSpike': 'true']]
  ```

  On world load it gives the player active blood, Searing Lash known and selected, and Blood Projection in hand, on
  a flat arena.
- **Five target sets,** switched from the operator inbox or a number key: one still dummy at 6 blocks; a ring of six
  around the player; a pack of three with AI; a file of three, one behind another; the pack again among pillars.
- **Live tuning.** Every number below is read from `lash-spike.json` in the game directory and re-read when the file
  changes. Feel gets tuned in minutes, not rebuilds.
- **A readout** in the corner: tip speed, stroke arc, last hit, damage per second and mL per second over the last ten
  seconds, average rule time. Plain debug text.

## What it is not

Each of these is left out on purpose and belongs to Track 12:

- Track 1's framework. The spike hooks in with its own tick subscriber and one hard-coded check.
- Temper, the other four forms, Absorption changes, the skill line.
- Other players. The crack is sent with `server.execute` from the integrated server's own client, so there is no
  payload and no second client. Question "do observers see the same lash" stays open and is carried to the track.
- The third-person arm pose. The arm stays in the bow pose.
- Tests, lang keys, Liber text, Vesper coverage, docs.
- Balance. The numbers only need to be close enough to judge feel.

## The rule to prototype

One pure class, `LashRules`, no Minecraft types beyond vectors. It is the one piece written to be kept.

**Tip models** (switch with `model` in the JSON):

- **A, lag chain.** Eight nodes. Each tick, node `i` moves a fraction `k_i` of the way to its place on the straight
  line from the hand along the look. `k` falls from about 0.9 at the hand to about 0.35 at the tip. Then clamp each
  segment to its rest length. Cannot overshoot, cannot blow up.
- **B, rope.** The same eight nodes as a Verlet rope with a little gravity and damping, root pinned to the hand, pulled
  toward the aim line. Overshoots and curls like a real whip; needs more care to stay stable.
- **C, tip only.** A straight line from the hand whose end lags behind the aim. The cheapest; the control for whether
  A and B are worth their cost.

**Every model then does the same:**

1. Clip each node against blocks from its last position; a blocked node stops at the face.
2. For the outer half of the chain, test the swept segment (last position to new position) of each node, and the
   segment between neighbours, against the boxes of nearby creatures grown by the strand radius.
3. Refuse a creature hit less than `lockoutTicks` ago.
4. Damage is `lerp(floorDamage, capDamage, (tipSpeed - floorSpeed) / (capSpeed - floorSpeed))`, clamped; nothing below
   `floorSpeed`. With `flatDamage` on, any sweep over the floor deals `capDamage`.
5. Stroke tiring: add up the angle swept while the turn direction holds. Past `strokeArcDegrees`, damage is
   `floorDamage` until the direction reverses or the tip rests.
6. Crack: a reversal while the turn speed is over `crackTurnSpeed`, or a left click. One hit in `crackRadius` at the
   tip for `capDamage × crackMultiplier` and the long Searing. Once per `crackCooldownTicks`.

**Starting numbers:**

| Key | Start | Note |
| --- | --- | --- |
| `length` | 7.5 | Projection reach 5.5 plus 2 |
| `strandRadius` | 0.35 | |
| `floorSpeed`, `capSpeed` | 0.35, 1.6 | Blocks per tick at the tip |
| `floorDamage`, `capDamage` | 3, 6 | × Crimson Mastery |
| `lockoutTicks` | 10 | Vanilla's own repeat-hit window |
| `strokeArcDegrees` | 90 | |
| `crackTurnSpeed` | 12 | Degrees per tick before the reversal |
| `crackMultiplier`, `crackRadius` | 1.5, 1.25 | |
| `crackCooldownTicks` | 20 | |
| `searingTicks` | 120 | The R cast's value |
| `upkeepPerSecond` | 60 | mL |
| `snapCost` | 40 | mL per click crack |
| `movement` | 0.8 | Tried at 0.2, 0.6 and 1.0 as well |

## What it reuses

| Need | Use |
| --- | --- |
| The hit | `ManipulationCombatHelper.hurt(manip, player, target, level, amount, duration, 1)` inside `SchoolDamage.cast(manip, player, 1)`. Targeting rule, school, Searing and the `CAST_IMPACT` visual come with it. |
| The cost | `BloodAccounting.payUpkeep` and `pay` with a spike `Source`. |
| The ribbon | `SearingLashGeometry`'s flame ribbon and two ribbon passes (`ThermalGeometry.flameRibbon`, `LuxUmbraGeometry.ribbonPath`), fed the live node list. |
| The crack flare | The flare, tongues and embers `SearingLashGeometry` already draws at the tip. |
| The hand point | `CasterFrame.hand()`, or the manipulation's own `hand(player)` (eye + look × 0.4, 0.45 down). |
| The movement fix | The `MovementInputUpdateEvent` handler pattern in `ClientInputEvents` that Absorption uses. |
| The left button | The `keyAttack.isDown()` poll the baghnakh handler uses. |
| The harness | `OutpouringClientReview` as the model for a property-gated review class; the operator inbox. |

## Where the code would live

- **Almost all of it in the gameTest source set** (`src/gameTest/java/com/vincenthuto/hemomancy/gametest/`), which
  never reaches a release jar: a server tick subscriber gated on `hemomancy.lashSpike`, the client side (input poll,
  local copy of the rule, readout), the arena setup, and the JSON reader.
- **Why no edit to `BloodProjectionItem` is needed:** aimed at air or at a mob, Projection does nothing and spends
  nothing today. The spike's own tick subscriber can watch for "using Blood Projection with Searing Lash selected" and
  run beside it.
- **One known wrinkle:** plain Projection still runs underneath, so an aim that crosses the ground may feed an
  infusion and muddy the blood readout. If it does, the spike adds one early return in
  `BloodProjectionItem.performProjection` behind the system property, and reverts it at the end.
- **Two touches in main, both small:**
  - `SearingLashGeometry.draw` split into "build the points" and `drawPath(points)`, with a way for the spike to
    hand it a point list. Track 12 needs this split anyway.
  - `LashRules` under `common/manipulation/`, if it is to be shared by the server and client halves cleanly.
- **Drawing.** World effects draw only from `WorldEffectDispatcher`. The spike should hang its ribbon off the existing
  `ManipulationVisualRenderer` path rather than add its own render subscriber, so it does not trip
  `HudAndWorldDrawingGuardrailTest` if someone runs the tests while it is in the tree.

## Things to confirm on the way

- **The left button during use.** From memory, vanilla throws away attack clicks and skips block breaking while an
  item is in use. If that is wrong, the snap needs a different key and `CL-22` changes.
- **Server look rate.** The server's copy of the player's look updates as movement packets arrive. Check that a fast
  flick is not flattened into one big step that skips a target between two ticks. The swept test is there for this;
  see that it holds at the fastest stroke a hand can make.
- **Hurt timing.** Confirm a 10-tick lockout lands every hit, and that a crack inside the window of a sweep hit is
  not thrown away (a larger hit should pass; check it).
- **`withApplication(0, 1)`.** Whether a plain `hurt` refreshes Searing. The design wants only the crack to.

## How to judge it

Play each target set for a few minutes per setting. For each question write one of **keep**, **change** (and to
what), or **stop**.

Reference points for question 7:

- An iron sword is about 9.6 damage per second on one target, a diamond sword about 11.2.
- The lash should sit under a same-tier sword on one target and over it on three in an arc.
- The R cast pays 225 mL for 4 damage and the long Searing. A lash should be cheaper per point of damage and slower
  to lay the state.

Signs it is working: the owner starts using strokes on purpose, cracks on the last target of a sweep, and stops
looking at the readout.

Signs it is not: spinning wins; the hand tires before the blood does; the click does everything and the swing adds
nothing; the lash reads as lag.

If the answer is **stop**, Track 12 keeps Jet, Spray, Plant and Brand, which do not depend on swung motion, and the
lash stays an R cast.

## What comes back, what goes

| Comes back to Track 12 | Deleted |
| --- | --- |
| The chosen tip model and its constants | Every spike class in the gameTest source set |
| A verdict on each of the eight questions | The review client row and its build directory |
| Whether the left button is free during use | `lash-spike.json` |
| Movement and cost numbers that felt right | The readout |
| Measured server cost | The early return in `performProjection`, if it was added |

`LashRules` and the `SearingLashGeometry` split are candidates to keep. Keeping either is a new decision made after
the spike, not part of it.

## Size

One session. No Gradle build runs beside another session's. Nothing is committed.

# Crossbar Radial Command Design

## Purpose

The Marionette Crossbar gains a hybrid radial interface that preserves fast call/recall while making persistent puppet commands legible and adding a late-stage field hot-swap reward. The Spindle remains the required preparation station before Degree 5. The Crossbar radial does not replace attunement, learning trials, fueling, capacity, upkeep, or tether rules.

## Player Interaction

Using an owner-attuned Crossbar starts a ten-tick hold check.

- Releasing use before ten ticks performs the existing call/recall action for the prepared shape.
- Holding for ten ticks opens a non-pausing radial and suppresses the tap action.
- Releasing over a valid segment sends one selection to the server.
- Releasing in the center, outside the rings, or cancelling with Escape performs no action.
- Sneak-right-click shape cycling is removed.

Before the player learns Skein Transposition, shape selection remains a Spindle action. After learning it, the radial's outer ring permits field hot-swapping.

## Radial Layout

The radial uses the established text-only, two-ring interaction language of the manipulation radial.

### Inner Ring: Crossbar Command

The inner ring is available whenever the held Crossbar is owner-attuned. It contains four persistent modes:

- **Follow:** Bodies remain near their owner. They do not freely hunt, but retaliate when the owner or another body bound to the same Crossbar is attacked.
- **Guard:** The server records the owner's current block position and dimension as an anchor. Bodies hold that area and attack hostiles within the guard radius. If the owner leaves the anchor's tether range or dimension, the Crossbar falls back to Follow.
- **Hunt:** Bodies proactively acquire hostile entities implementing Minecraft's `Enemy` contract within the owner's tether range.
- **Passive:** Bodies follow the owner but do not automatically acquire targets.

Changing mode is free, persistent, and applies to every active shaped summon or Commandeered Will bound to that exact Crossbar. It does not affect bodies belonging to another Crossbar, even when they share an owner.

The existing attack-focus command remains available. A valid focused target temporarily overrides the persistent mode. When that target dies, becomes invalid, or leaves tether range, each body resumes the Crossbar's stored mode.

### Outer Ring: Prepared Shape

The outer ring appears only after the player learns Skein Transposition. It always displays the four canonical artificial puppet shapes.

- The currently prepared shape is highlighted.
- Learned, affordable, capacity-valid shapes are selectable.
- Unlearned shapes are disabled and identify the missing trial.
- Shapes blocked by charge, capacity, or another server rule are disabled and state the reason.
- Commandeered Wills never appear as selectable shapes.

Selecting the already prepared shape is a no-op. Selecting a different valid shape performs a field hot-swap.

## Skein Transposition

Skein Transposition is a one-rank Degree 5 Harbinger skill with Far Tether as its prerequisite. It unlocks only the outer shape ring and field hot-swapping.

Thread Economy remains a parallel Degree 5 node. Bound Command remains a Degree 6 node. The inner command ring requires no new skill beyond owning and holding an attuned Crossbar.

## Hot-Swap Transaction

A hot-swap means recalling the old prepared cohort and immediately calling one body of the selected replacement shape at the ordinary, skill-adjusted call cost.

The affected cohort contains only active artificial bodies that:

1. belong to the requesting player,
2. are bound to the exact Crossbar UUID supplied by the validated held item, and
3. match the shape that was prepared when the request began.

Bodies of other shapes on the same Crossbar remain active. Bodies on other Crossbars and all Commandeered Wills remain active.

The projected shaped-body count is calculated as the current shaped count minus the affected cohort plus one replacement. Existing player-wide, shaped-body, and claimed-Will capacity rules remain authoritative.

The server validates the complete transaction before changing persistent or world state:

- requesting player and equipped hand,
- permanent Crossbar ownership and attunement,
- exact Crossbar UUID,
- Skein Transposition unlock,
- selected canonical shape and learned trial,
- selection differing from the prepared shape,
- adjusted call charge,
- projected capacity,
- cooldown eligibility, and
- successful replacement entity construction and insertion.

The replacement must be constructible and insertable before the old cohort is destructively recalled. Charge is then spent through the existing authoritative charge operation. If any validation, insertion, or charge step fails, the candidate is discarded and the old bodies, prepared shape, and charge remain unchanged. On success, the affected cohort is recalled, the new shape becomes prepared, and the replacement remains active. No extra hot-swap surcharge is added.

## Stored State

The Crossbar adds persistent data for:

- command mode,
- guard block position, and
- guard dimension.

Existing owner, stable Crossbar UUID, prepared shape, charge, and capacity data remain unchanged. New data must survive inventory movement, dropping and pickup, save/load, death behavior permitted by existing item rules, and reconnecting. Missing command data on older Crossbars defaults to Follow; guard data is ignored unless the stored mode is Guard and the dimension can be resolved.

## Client and Network Responsibilities

The client controls hold timing, radial presentation, hover feedback, and sending one requested action. The radial center shows current charge, prepared shape, current command mode, and the hovered segment's concise description or disabled reason.

The client sends the Crossbar UUID plus either a requested command mode or requested replacement shape. It does not author ownership, cost, capacity, unlock, or guard coordinates.

For every request, the server locates an equipped, owner-attuned Crossbar with the matching UUID and revalidates current state. Guard anchors use the server's current player position and dimension. Forged, stale, foreign, unequipped, or otherwise invalid requests have no effect.

## Failure and Boundary Behavior

- Opening the radial prevents the quick call/recall from firing on release.
- Cancelling the radial has no cost or cooldown.
- Command changes have no charge cost and do not invoke the summon cooldown.
- A hot-swap uses the ordinary summon cooldown and adjusted call cost.
- A same-shape request is a no-op rather than a recall or duplicate call.
- Guard automatically changes the stored Crossbar mode to Follow when its owner exceeds tether range or changes dimension; bodies then follow normal Follow behavior.
- Multiplayer screens do not pause the game.
- Server rejection never partially recalls a cohort or mutates the prepared shape.

## Verification

Automated coverage should verify:

- release before and after the ten-tick threshold, including radial cancellation;
- suppression of the tap action after radial opening;
- removal of sneak-use cycling;
- inner-ring availability and Degree 5 outer-ring gating;
- learned-shape, charge, cooldown, and projected-capacity validation;
- exact-Crossbar isolation when an owner has multiple Crossbars;
- recall of only the previously prepared matching cohort;
- survival of other shapes and Commandeered Wills during a hot-swap;
- preservation of bodies, charge, and prepared shape after every failed transaction stage;
- ordinary adjusted charge expenditure and cooldown after success;
- Follow, Guard, Hunt, and Passive acquisition behavior;
- focused-target override and return to the persistent mode;
- server-authored Guard anchors and tether/dimension fallback;
- Commandeered Wills obeying modes while remaining absent from shape selection;
- rejection of forged, stale, foreign, and unequipped requests;
- serialization and legacy-default behavior for new Crossbar fields; and
- regression coverage for ordinary call/recall, Spindle preparation, upkeep, tether severance, and capacity rules.

## Documentation Impact

After implementation, canonical documentation and the derivative wiki must replace descriptions of field sneak-cycling with the hybrid tap/hold controls, document all four modes, and describe Skein Transposition and its Degree 5 placement. The docs remain the lore and mechanics authority; wiki text must be derived from them.

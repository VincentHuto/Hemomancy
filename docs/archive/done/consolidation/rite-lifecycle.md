# Rite lifecycle and event handling

## Current flow

1. [CardinalRiteRecipe](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/CardinalRiteRecipe.java) and its JSON define the rite. [CardinalRiteStationMatcher](../../src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteStationMatcher.java), floor/sigil definitions, and preflight rules establish what can start.
2. [CardinalRiteInteractionHandler](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteInteractionHandler.java) routes projection, station feeding, sigil work, and right-click interactions. [CardinalRiteCancellationHandler](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteCancellationHandler.java) handles cancellation intent.
3. [ActiveCardinalRite](../../src/main/java/com/vincenthuto/hemomancy/common/rite/ActiveCardinalRite.java) is the active record, stored by [CardinalRiteSavedData](../../src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteSavedData.java).
4. [HarbingerCardinalRiteEvents](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java) performs the per-level tick, progression through phases, timeout/collapse, special ordeal dispatch, and cleanup. [UnstainedCardinalRiteEvents](../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedCardinalRiteEvents.java) performs Unstained activation/completion paths.
5. Completion may create a lasting zone or event; [StillWatersEvents](../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/StillWatersEvents.java), [PaleConsecrationEvents](../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/PaleConsecrationEvents.java), [LetheCovenantEvents](../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/LetheCovenantEvents.java), and their saved-data classes continue the effect.

## Repeated responsibilities

| Responsibility | Repeated or scattered behavior | Candidate shared seam |
| --- | --- | --- |
| Tick dispatch | Main Harbinger level tick also ticks pending blood craft, structure feed, infusion, projection, delivery, and active rites | Separate a feature-tick coordinator from the rite state machine. Preserve tick order and dimension gating. |
| Phase transition | Individual branches set phase, consume offerings, spawn threats, sync, and mark dirty | One transition API that records old/new phase and runs exactly one cleanup/notification path. Keep ceremony-specific actions delegated. |
| Cancellation/recovery | Disconnect grace, invalid recipe, missing anchor, caster death, manual cancellation, and succession cleanup have related but distinct paths | Enumerate exit reasons and their escrow/offerings/threat cleanup policy before extracting common cleanup. |
| Persistent zones | Still Waters, Pale Consecration, and Lethe Covenant separately prune expired records every 100 ticks and query dimension/position | The [Stage 4 zone audit](stage-4-unstained-zones/README.md) identifies a common cleanup dispatch. Their active query geometry and dimension routes differ; keep effect hooks and event phases separate. |
| Visual sync | Active rite packets/client state, bloom sync, helix/impact particles | Name which cues are transient and which state must be rebuilt for late clients. |

## Important differences to preserve

Still Waters modifies `LivingDamageEvent.Pre`; Lethe Covenant cancels `LivingIncomingDamageEvent` for a Silver Ward player and also handles spawn suppression and periodic purity; Pale Consecration ticks hostile mobs and applies damage/slowness. These are not interchangeable damage hooks. Pale's effect and Lethe's purity tick run only in the Overworld even though their saved entries include a dimension; Stage 4 records this for gameplay verification. Main rite records can survive while a caster is disconnected; recovery and staff escrow must remain idempotent. A saved rite whose recipe disappeared is retired with its own recovery path.

## First migration slice

1. Draw the state transition table for one ordinary Harbinger rite, one Unstained rite, one upgrade rite, and one succession rite: trigger, phase, persisted fields, resources consumed, emitted cue, and every exit reason.
2. Extract only the shared **exit transaction** after proving it returns/stores each resource once and clears threats and client projection correctly.
3. Move unrelated feature ticks out of `HarbingerCardinalRiteEvents` only after recording their per-level ordering and dimensions.
4. Then unify the 100-tick zone cleanup dispatch using the [Stage 4 contract](stage-4-unstained-zones/README.md). Leave each zone's effect event and saved key in place.

See [World persistence and reloads](world-persistence-and-reloads.md) for NBT keys and [Combat and powers](combat-and-powers.md) for damage-phase rules.

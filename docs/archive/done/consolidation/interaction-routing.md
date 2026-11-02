# Interaction routing and event ownership

## The overlapping paths

- Block classes such as [EnzymaticScriptoriumBlock](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/EnzymaticScriptoriumBlock.java) implement `useWithoutItem` and `useItemOn` directly.
- Global `PlayerInteractEvent` handlers also change the same interactions: [BloodProjectionInteractionEvents](../../src/main/java/com/vincenthuto/hemomancy/common/event/BloodProjectionInteractionEvents.java) selects item use over block use, [MachineAccessEvents](../../src/main/java/com/vincenthuto/hemomancy/common/event/MachineAccessEvents.java) denies use and break by progression, [SomaticLoomInteractionEvents](../../src/main/java/com/vincenthuto/hemomancy/common/event/SomaticLoomInteractionEvents.java) favors loom use for a crouched enzyme, and [CardinalRiteInteractionHandler](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteInteractionHandler.java) handles rite projection and targeted right-clicks.
- Multi-block fillers are resolved in more than one handler through [FillerBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/shared/FillerBlockEntity.java). The shared operation is finding the actual controller block/entity, but each caller then makes a different decision.

## Why this is difficult to follow

A right click can pass through a global event, `TriState` use-block/use-item selection, a block override, an item override, and a menu-opening path. Cancellation and `InteractionResult` determine whether the later path runs. A source scan confined to the block class misses the access or projection rule; a scan confined to events misses the machine's own action.

Make a routing sheet per interaction family with **event priority, client/server side, hand, sneaking, target resolution, useBlock/useItem choice, cancellation result, block/item callback, and final authority**. Put shared filler-to-controller resolution in one utility only after checking invalid/unloaded filler behavior. Keep the feature-specific decision in its handler.

## First migration slice

1. Trace blood projection against a direct machine, a filler block, and a recruited NPC. Record the order of global and local callbacks.
2. Extract controller resolution for identical filler cases; migrate Blood Projection and Machine Access if both accept the same null/unloaded semantics.
3. State which handler owns access denial versus interaction selection. Preserve explicit `TriState` and cancellation behavior.
4. Verify empty hand, held tool, offhand, sneaking, client prediction, and server result. This is where a seemingly small refactor can cause duplicate activation or a swallowed menu.

# Phase 4 Tendon Line Design

## Goal
Add `tendon_line` as a reusable biological traversal utility: a climbable line that anchors into a solid block face and pulls the player toward that anchor while held.

## Behavior
- Right-clicking a sturdy block face with `tendon_line` stores that block position as the line anchor.
- Sneak-right-clicking clears the stored anchor.
- Right-clicking without a block target starts using the line if an anchor is stored.
- While the item is being used, the player is gently pulled toward the anchor. The motion is controlled and climbing-oriented, not a hookshot launch.
- The item clears fall distance while actively pulling.
- The line detaches if the player changes dimension, the anchor block is no longer valid, the player moves beyond the detach range, or the player gets close enough to the anchor.

## Balance
- Anchor range: 18 blocks from the player when setting the line.
- Detach range: 24 blocks from the anchor while using it.
- Pull speed: 0.34 blocks/tick, blended with existing motion so it feels like a living rope tightening.
- Close distance: 1.35 blocks.
- Cooldown: 10 ticks after anchoring or starting use.

## Implementation
- Store anchor data on the item stack using `DataComponents.CUSTOM_DATA`, following the existing `StructureScannerItem` pattern.
- Add `TendonLineRules` as a pure helper for constants and pull-vector math.
- Add `TendonLineItem` for anchor storage, use handling, ticking motion, and feedback.
- Register the item in `ItemInit`.
- Add recipe, model, texture, lang, and docs.

## Testing
- Add a pure sentinel test for `TendonLineRules`.
- Add a resource sentinel test for model, texture, and recipe existence.
- Run `./gradlew.bat testClasses --no-daemon`, the direct sentinel tests, and `./gradlew.bat build --no-daemon`.

# Two-Layer Manipulation Radial Design

## Goal

Rework the manipulation radial wheel so fixed mechanical manipulations are always available on an inner two-part wheel, while the player's memorized offensive and utility manipulations remain on the outer wheel.

## User Experience

- The radial keeps a small unselectable center for existing self/gourd text.
- Moving the cursor into the inner band selects a fixed mechanical manipulation.
- The top inner half selects `blood_absorption`.
- The bottom inner half selects `blood_projection`.
- Moving the cursor into the outer band selects normal memorized manipulations.
- Blood absorption and blood projection do not appear in the outer band.
- Blood absorption and blood projection cannot be unequipped from the player's mechanical manipulation set.

## Architecture

Extend `GenericRadialMenu` from a single-ring menu into a band-aware radial menu. The default behavior remains a single outer band so other callers can keep using `add`, `addAll`, `clear`, hover, click, drawing, and keyboard cycling without needing their own geometry logic.

Add a second item collection for inner-band items. Each band has its own radius range:

- Inner mechanical band: from the center text dead zone to the old inner radius.
- Outer memorized band: from the old inner radius to the old outer radius.

The radial menu owns all hover, click, draw, and mouse-clamp math. `RadialChooseManipScreen` only decides which items belong to each band.

## Data Flow

`RadialChooseManipScreen` builds three groups from `IKnownManipulations`:

- `blood_absorption` mechanical item for the inner top half.
- `blood_projection` mechanical item for the inner bottom half.
- normal equipped manipulation items for the outer wheel.

Every selectable item still sends `UpdateCurrentManipPacket` with the manipulation's index in the full known manipulation list. This keeps server behavior consistent with the existing packet.

## Capability Rules

Add shared constants for the two fixed mechanical manipulation names. Equip list normalization should preserve those names and reject unequipping them. Normal memorized manipulations continue to respect the existing slot limit.

## Rendering

The inner band uses two half-circle slices with the same icon rendering style as current memory wheel items. The center text stays in the middle and is not clickable. The outer ring keeps the current icon and tooltip behavior.

## Testing

- Add a focused regression test for mechanical manipulation equip rules: absorption/projection are retained and cannot be unequipped.
- Add source-level regression coverage that the manipulation radial screen builds dedicated inner mechanical entries and excludes those fixed entries from the outer memorized ring.
- Run the focused tests and `./gradlew.bat build` after implementation.

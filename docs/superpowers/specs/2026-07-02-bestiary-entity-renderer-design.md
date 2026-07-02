# Bestiary Entity Renderer Design

## Goal

Add a discovered-only entity preview renderer to the Harbinger Progress Screen's Bestiary tab. The preview supports panning so players can inspect recorded specimens without spoiling locked entries.

## Scope

- Update only the Harbinger bestiary tab surface under `client/screen/skilltree/harbinger`.
- Preserve the existing left-side specimen/morphling list and discovery gating.
- Use a split dossier detail layout: entity preview on the left side of the detail panel, text/status/source on the right.
- Render previews for discovered specimen entries that have a direct living entity type in `EntityInit`.
- Keep locked entries and entries without a concrete preview entity text-only or withheld.

Morphling strain entries are not required to gain preview models in this pass because they are item/layer records rather than direct living specimen entities.

## User Experience

When a discovered specimen is selected, the detail panel shows a framed 3D viewport beside the existing title, status, description, and source text. The viewport uses the existing green/black bestiary palette and is clipped to its frame.

Controls inside the preview viewport:

- Left-drag pans the entity within the preview frame.
- Mouse wheel zooms the entity preview.
- Shift-left-drag rotates the entity for inspection.

Mouse wheel outside the preview keeps the existing list/detail scrolling behavior. Locked entries do not show creature silhouettes or full models.

## Implementation Shape

Add a small bestiary preview helper rather than putting entity creation/rendering directly in `BestiaryTabView`.

Suggested responsibilities:

- Map specimen entry ids such as `hemomancy:chitinite` and `hemomancy:crimson_doe` to their matching `EntityInit` holders.
- Create and cache one preview `LivingEntity` for the selected entry and current client level.
- Disable mob AI for preview entities.
- Render through the same `InventoryScreen.renderEntityInInventory` path already used by `SummonsTabView`.
- Capture and restore entity rotations during rendering so preview drawing does not mutate cached entity state in surprising ways.

Extend `BestiaryTabState` with preview state:

- selected preview key/world cache identity
- preview entity reference
- pan X/Y
- zoom
- rotation angle
- dragging mode and previous mouse position

Reset pan/zoom/rotation when selecting a different bestiary entry.

## Layout

Keep the current bestiary outer layout:

- Header across the top.
- List panel on the left.
- Detail panel on the right.

Inside the detail panel, use the split dossier layout selected during brainstorming:

- Preview viewport takes 45% of the detail panel width when the detail panel is at least 360 pixels wide.
- Text dossier takes the remaining width.
- On narrow widths, clamp the preview to a usable minimum and keep text wrapping inside its column.
- Existing detail scrolling applies to the text column, not the preview viewport.

## Testing And Verification

Prefer focused tests for pure logic if the existing Gradle test setup supports them cleanly:

- preview id mapping resolves every current specimen id with a registered entity where expected
- preview state resets when selection changes
- pan/zoom clamps prevent the entity from disappearing entirely

Because the visible renderer depends on the Minecraft client, final verification includes:

- `./gradlew.bat build`
- `./gradlew.bat runClient` manual check of the Bestiary tab with a discovered specimen

Manual client checks confirm discovered entries render, locked entries stay withheld, panning works inside the preview, and scrolling still behaves correctly outside the preview.

## Documentation Impact

This is a client presentation change for an already documented bestiary feature. `docs/HEMOMANCY_REFERENCE.md` does not need mechanics changes unless implementation changes unlock rules, specimen recording, or player-facing progression behavior.

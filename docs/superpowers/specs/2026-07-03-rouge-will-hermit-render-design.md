# Rouge Will Hermit Render Design

## Goal

Redo the Will renderer/model so the Rouge Will reads almost identical to the Harbinger Hermit, but without the gourd on its back and with a translucent ghostlike material. This replaces the current blocky placeholder silhouette shown in-game.

## Approved Direction

Use a Hermit-derived Will model:

- Keep `WillEntity`, `WillModel`, and `WillRenderer` as the public client wiring for Wills.
- Replace the current simple Will geometry with the Harbinger Hermit silhouette: hood, robed torso, side/back cloth panels, arms, and legs.
- Omit the Hermit's `gourd`, `rope`, and attached back-accessory parts from the Will model entirely.
- Use the Hermit texture path for now so the Will visually matches the Hermit as closely as possible.
- Render the Will with `RenderType.entityTranslucent(...)` and pass a fixed alpha tint through `renderToBuffer`.

## Architecture

`WillModel` remains a model for `WillEntity`, not a subclass of `HarbingerHermitModel`, because the Hermit model is typed to `HarbingerHermitEntity` and currently constructs the gourd branch directly. Copying the Hermit layer into `WillModel` keeps the entity type correct and avoids making Hermit-specific renderer behavior leak into Wills.

`WillRenderer` should continue to register through existing entity renderer wiring, but it should use:

- the updated `WillModel`;
- the Harbinger Hermit texture;
- a translucent render type override;
- a smaller/no-shadow presentation if needed for a transparent entity.

## Animation

Reuse the Hermit's basic humanoid animation style for head tracking, walking arms/legs, and cloth sway. Keep it simple: no Hermit farewell dissolve, no beams, no gourd animation, and no new gameplay behavior.

## Testing

Build with `./gradlew.bat compileJava` or `./gradlew.bat build` after editing. If feasible, launch `runClient` and spawn a Will to visually confirm the model no longer looks like the broken placeholder, has no gourd, and is visibly translucent.

## Documentation Impact

This is a client-only visual/model change. No mechanics, progression, recipes, or player-facing lore behavior changes are expected, so `docs/HEMOMANCY_REFERENCE.md`, `docs/LORE_REFERENCE.md`, and wiki pages do not need updates.

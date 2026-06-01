# Silent Archon Shader Armor Design

## Goal

Silent Archon armor keeps its existing custom robe/armor model and authored texture detail, and gains the same black and red Monolith Fragment / Memory of Vesper shader language as a semi-translucent overlay when worn. The four armor item stacks stop presenting as flat generated sprites and render as 3D armor-piece models in inventory, hands, frames, and dropped contexts.

## Current State

- Equipped Silent Archon pieces are `SilentArchonArmorItem`s that return `SilentArchonArmorModel` instances through `IClientItemExtensions#getHumanoidArmorModel`.
- `SilentArchonArmorModel` uses the normal translucent entity texture render type, so the final equipped armor is driven by `silent_archon_layer_1.png` and `silent_archon_layer_2.png`.
- Monolith Fragment and Memory of Vesper items draw procedural geometry through `HemoRenderTypes.monolithFragment(...)`, which uses `ShaderInit.MONOLITH_FRAGMENT`.
- The current monolith fragment render type uses triangle mode because those item renderers emit triangles. The armor model emits quads, so armor needs a render type that reuses the same shader/uniforms with quad mode.
- Silent Archon item JSONs currently use `minecraft:item/generated`, which is why the stack icons are flat sprites.

## Design

Add an armor-specific shader render type in `HemoRenderTypes` named `silentArchonArmorOverlay(...)`. It reuses `ShaderInit.MONOLITH_FRAGMENT` and the same uniforms as `monolithFragment(...)`, but uses a quad-compatible vertex format/mode suitable for humanoid model cubes. Its defaults use a subdued overlay: translucent enough for the robe texture to remain readable, with `Attuned` high enough to read as Vesper/Monolith refusal and `Burden` low-to-moderate unless later gameplay state calls for a burdened variant.

Add a client render path for worn Silent Archon pieces that performs a second pass after normal armor rendering. The first pass remains vanilla/custom armor rendering with the existing armor textures. The overlay pass renders only the equipped Silent Archon slot model parts, using the same part visibility decisions as the base model so morphling-hidden armor parts stay coherent.

Add a `SilentArchonArmorItemRenderer` for item stacks. It reuses the existing `SilentArchonArmorModel` baked layers for helmet, chestplate, leggings, and boots, selects the correct model by `ItemStack`, and renders a textured base pass plus the same shader overlay pass. The four item JSON files switch from `minecraft:item/generated` to `builtin/entity` so NeoForge routes them through the custom renderer. Display transforms are tuned per context so GUI, ground, fixed, first-person, and third-person views read as compact 3D armor pieces rather than full player-scale robes.

## Scope

Included:

- Worn Silent Archon armor gets a semi-translucent animated black/red shader overlay.
- Existing Silent Archon textures and custom model remain in use.
- Silent Archon armor item stacks render as 3D custom armor models.
- Documentation in `docs/HEMOMANCY_REFERENCE.md` is updated to mention the overlay and 3D item-stack rendering.

Not included:

- Gameplay changes to Silent Archon set bonuses, degree gates, or death refusal.
- New lore text or item names.
- Shader changes for Monolith Fragment or Memory of Vesper item behavior.
- Reworking other armor sets or all armor item sprites.

## Testing

- Compile with `./gradlew.bat compileJava`.
- Build with `./gradlew.bat build` after compilation.
- Run `./gradlew.bat runClient` for visual validation of equipped armor and item-stack display contexts.

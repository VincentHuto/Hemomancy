# Phase 5 Curor Lens Design

## Goal
Add `curor_lens` as a Thaumometer-like diagnostic instrument: the player holds a 3D lens up to their view, receives a translucent lens overlay, and can read living target diagnostics through it.

## Behavior
- Holding right-click raises the item using a spyglass-style use pose.
- While held, a custom Curor Lens overlay renders on the client.
- The overlay raycasts living entities up to 16 blocks from the camera.
- If a living target is centered, the overlay displays client-visible health, blood volume, bloodline mark, dominant tendency, and scar information where available.
- If no target is centered, the overlay shows a faint no-resonance line.
- The item does not damage, debuff, highlight through walls, or open a GUI.

## Implementation
- `CurorLensRules` owns scan range and formatting helpers.
- `CurorLensItem` owns hold-to-use behavior.
- `CurorLensOverlay` renders the texture overlay and target readout through the existing client GUI layer registration.
- The item uses a resource-pack JSON 3D model with first-person transforms and generated bitmap textures.

## Testing
- Add pure tests for scan range and diagnostic formatting.
- Add resource tests for model, item texture, overlay texture, and recipe.
- Verify with `testClasses`, direct sentinel tests, and full build.

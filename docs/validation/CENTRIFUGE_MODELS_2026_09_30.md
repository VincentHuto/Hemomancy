# Centrifuge tier models - 2026-09-30

## Visual design

The original eight-vial animated rotor remains the focal point. The Calibrated tier adds a low octagonal footing, four bearing braces, indexed rails, and a large front governor dial. The Fractionating tier retains that assembly and adds two glass receivers with separate return pipes, rear risers, and a split manifold above the moving rotor. Native existing iron, bloodwood, calcified trim, glass, and sanguine textures keep the apparatus consistent with the other authored Harbinger stations.

The horizontal bounds stay inside 16 by 16 model units. The Calibrated static body reaches 12 units; the existing rotor supplies the upper silhouette. Fractionating's rear manifold reaches 21.5 units. Receivers stay below the lowest spinning sample, rear risers/ferrules remain outside its sweep, and the crosshead stays above the rotor. No new machine inventory, processing rule, rite, or eligibility rule was added.

## Sources and integration

- Editable bodies: `src/main/resources/assets/hemomancy/models/block/bbmodel/vial_centrifuge_calibrated.bbmodel` and `vial_centrifuge_fractionating.bbmodel`.
- Runtime bodies: matching files under `models/block/`.
- The existing `CentrifugeArmsModel` remains the moving-part source; sample visibility and speed remain controlled by the existing renderer.
- All twelve stage/facing combinations select their correct static body. The world renderer draws the old stand only for stage zero, preventing a second stand inside the upgraded models.
- The custom item renderer reads the saved block-state stage and renders the corresponding body with the original moving rotor. The base item appearance remains unchanged.
- The block is non-occluding, matching the open machinery used by the other Harbinger stations.
- The export script owns the authored static geometry, generates the runtime models and embedded-texture Blockbench bodies, and retains the original base assets.

```powershell
python tools/model_export/export_centrifuge_upgrades.py
python tools/model_export/check_centrifuge_upgrades.py
python tools/model_export/preview_centrifuge_upgrades.py
```

The Blockbench files contain complete static bodies; the animated rotor is deliberately kept in its shared Java model. The preview composes actual rotor geometry and textures with each body, showing filled specimens. It is an asset render, not a Minecraft screenshot.

Preview: `build/model-previews/centrifuge_upgrades.png`; front/rear images also exist separately in that directory.

## Verification

- Export parity and twelve stage/facing variants: passed.
- 0.25-unit bounds, dimensions, and pivots; valid outliner membership and embedded textures: passed.
- Native texel density and UV bounds on every new cube face: passed.
- Rotor sweep clearance: passed.
- Cube budgets: Calibrated 38, Fractionating 73.
- Textured front/rear views and 64-pixel silhouette previews inspected.
- 21 focused JVM tests: zero failures/errors.
- 32 station runtime GameTests: all required tests passed.
- Java compilation and mod assembly passed.

```powershell
./gradlew.bat test --tests 'com.vincenthuto.hemomancy.common.station.*' --tests 'com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifuge*' runStationUpgradeGameTestServer assemble --offline --console=plain
```

Build/test log: `build/centrifuge-model-validation.log`. The full JVM suite was not repeated for this model pass; its previous unrelated failures remain documented in [the gameplay validation report](CENTRIFUGE_UPGRADES_2026_09_30.md).

## Live acceptance limitation

The isolated `runScriptoriumClient` loaded resources and atlases; the log contains no diagnostics naming either new body. Supported Computer Use returned `failed to activate captured window`; refreshed selection could no longer identify a unique review window. The launched client had exited with native exit code `-1073740791`, so screen capture, in-world motion, directional lighting, and item views could not be inspected. Log: `build/centrifuge-model-client.log`. No live visual acceptance or cause of the native exit is claimed.

The kit sprites were not part of this request: the Fractionating Rotor kit still references the previous Condenser artwork.

## Item art follow-up

Both kit sprites were subsequently replaced with distinct native 16x16 art. See [kit sprite validation](CENTRIFUGE_KIT_SPRITES_2026_09_30.md) for the pixel grids, previews, generation prompts, and packaging checks. The earlier kit-art note describes the model handoff before that follow-up.

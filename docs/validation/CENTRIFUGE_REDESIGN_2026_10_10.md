# Vial Centrifuge redesign — 2026-10-10

The Centrifuge now matches the other redesigned Harbinger stations. A script authors every stage body and part model, and the renderer animates them. The machine is a swing-bucket bench centrifuge across all three stages (Base, Calibrated, Fractionating). Mechanics, slots, upgrade rites and the one-block footprint are unchanged. Only the selection outline changed, to follow the new body.

## Sources

- `tools/model_export/vial_centrifuge.py` writes the stage bodies, 16 part models, Blockbench projects and blockstate. It also checks bucket swing clearance. Add `--preview` to render `build/vial_centrifuge_preview.png` through `vial_centrifuge_preview.py`.
- `VialCentrifugeRotorRules` holds speed, inertia, swing, governor lift and needle sweep. `VialCentrifugeBlockEntity.clientTick` integrates the rotor and drive angles.
- `VialCentrifugeRenderer` draws the moving parts. `VialCentrifugeItemRenderer` reuses its `renderMachine` with the saved stage's body.
- Retired: `CentrifugeArmsModel` and `CentrifugeStandModel` with their textures and Blockbench sources, plus `export/check/preview_centrifuge_upgrades.py`.

## Results

- The export is deterministic: two runs gave identical hashes. Coplanar resolution reports 0 fights on the stage bodies. Fully swung buckets reach radius 7.85 px (block edge is 8) and stay 0.37 px above the bowl rim.
- JVM: `VialCentrifugeRotorRulesTest` 5/5, `VialCentrifugeModelSourceTest` 4/4, `VialCentrifugeYieldRulesTest` 3/3, `VialCentrifugeStartupRulesTest` 1/1, `WaterloggedModelSelectorTest` 2/2 and `StationUpgradeCatalogTest` 6/6. `compileJava`, `compileTestJava` and `compileGameTestJava` passed.
- Native client: run `gradlew.bat -I build/centrifuge-world.init.gradle runCentrifugeWorldReviewClient`, which sets `hemomancy.centrifugeWorldReview`. It places all three stages in the disposable `CentrifugeReview` world and captures `build/centrifuge-client-review/screenshots/centrifuge-world-0..4.png`. The captures cover the stages at rest, a real balanced spin on each stage (startup `SUCCESS`), two close-ups and the hotbar/held item per saved stage. During the spin, buckets swing out, vials show their contents, the governors lift, and the receivers, spleen and flywheel render. The log has no missing-model errors for `vial_centrifuge_*`.
- Not run: the full GameTest suite and `alphaCheck`. Server-side centrifuge logic is untouched apart from the outline shape.

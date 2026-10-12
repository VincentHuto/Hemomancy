# Morphling Incubator and Mycelial Crucible redesign — 2026-10-10

Both stations now match the other redesigned Harbinger stations: a script authors each baked model and the renderers draw only what moves. Mechanics, slots, recipes and footprints are unchanged.

## Designs

- **Morphling Incubator.** A strapped glass brood tank stands on a hematic iron plinth with bone claw feet. The plinth has a glowing brood hearth behind an iron grate. A bone ribcage arches over the lid. Under the lid, a grafted placenta lowers an umbilical cord toward the incubated item, and a flesh nest lines the tank floor. Four enzyme ampoules sit on the plinth's diagonals. A blood ampoule on the east bracket feeds the collar.
- **Mycelial Crucible.** The culture basin sits on a bone-ribbed pedestal under a grafted erythrocytic fruiting body. The cap is carried by four posts. Its stipe is clamped and tapped into a bone dropper over the pool. The west vat holds a settled hyphal culture with fruiting strands. The east cistern holds blood. Each vat's arm turns down into a nozzle over the basin. Hyphal threads creep across the venous-stone dais. Enzyme ampoules stand on a rail under the bowl.
- **Materials.** Hematic iron, bone, blood-wood, venous stone, bog flesh, hemorrhagic crust, calcified hyphae and erythrocytic mycelium. There is no copper, brass or silver.

## Sources

- `tools/model_export/station_kit.py` is the shared element model, Blockbench export and preview rasteriser. It is the `vial_centrifuge.py` kit with the texture table passed in.
- `tools/model_export/morphling_incubator.py` writes `morphling_incubator.json`, its item model and Blockbench project. Run it with `--preview` to render `build/morphling_incubator_preview.png`.
- `tools/model_export/mycelial_crucible.py` writes `mycelial_crucible.json`, the `mycelial_crucible_blood` fill part, its item model and Blockbench project. Run it with `--preview` to render `build/mycelial_crucible_preview.png`.
- `MorphlingIncubatorRenderer` draws the octagonal blood column (to the stored level), the centre item floating in the tank, and the orbiting catalysts.
  - The old entity-model "creature" was drawn about a block below the floor, so players never saw it. It has been removed, along with `MorphlingIncubatorModel`, its layer, its item renderer and both of its textures.
- `MycelialCrucibleRenderer` draws three things:
  - the shader pool as an octagon matching the bowl;
  - the cistern blood scaled by fill;
  - the centre item turning half-sunk under the dropper.
- The crucible renderer's east/west facing rotation was mirrored. That was invisible on the old square basin. It now matches the blockstate.
- `MycelialCrucibleBlock`'s particle anchors moved to the new nozzle tips.
- `MycelialCrucibleBlockItem` keeps its custom class, because the registration GameTest pins it. Its dead placeholder renderer was dropped.
- The incubator's outline now follows the plinth and tank.
- Retired: `MycelialCrucibleModel.bbmodel`, `model_mycelial_crucible_model.png`, `model_morphling_incubator.png` and `textures/entity/morphling_incubator_model.png`.

## Results

- **Export.** Both exports are deterministic: two runs gave identical hashes. `coplanar.py` reports 0 fights on both models.
- **JVM.**
  - `compileJava`, `compileTestJava` and `compileGameTestJava` passed.
  - Three contracts pass when run directly: `ModelFirstTileRendererResourceTest` (updated for the generator-authored incubator), `WaterloggableDecorativeBlocksTest` and `RegistryCreativeOrderSourceTest`.
  - The full `test` task had 10 failures out of 2711, all in unrelated in-progress work: item textures, lang, docs, mojibake, the ossuary clock, and the legacy-test count drop from the deleted armature test.
- **Native client.** Run `gradlew.bat -I build/fungal-station-world.init.gradle runFungalStationReviewClient`. `FungalStationClientReview` stocks a north-facing incubator and two crucibles (north- and east-facing) in a disposable world. It captures `build/fungal-station-client-review/screenshots/fungal-station-0..6.png`.
  - Blood shows at 65% in the tank and 70% in the cistern.
  - The polyp floats in the tank, and the immature culture turns in the bowl.
  - Blood-cell and green ember streams leave the correct nozzles on both facings.
  - The east-facing cistern holds the blood.
  - Both items render in the hotbar.
  - The crucible's GUI and hand transforms were adjusted after this capture and have not been re-captured.
- **Layer fix.** The first export baked each whole station translucent because it contained glass. The translucent chunk layer draws after block entities, and neither the crucible pool nor the incubator blood writes depth. So the bowl and tank floors were painted over them, and the swirl vanished.
  - Each station now bakes a cutout body plus a separate translucent `*_glass` model. `station_kit.split_glass` does the split, and the facing multipart blockstate applies both models.
  - The item model still carries the body and the glass together.
  - In a re-run capture (`fungal-station-6.png`), the red and green swirl fills the octagonal bowl. In `fungal-station-7.png`, the incubator's blood column and polyp render correctly through the glass.
- **Mycelial Lantern (follow-up).** `tools/model_export/mycelial_lantern.py` replaces the converted post lantern with a blood-fed cloche.
  - An iron-hooped blood-wood crock stands on cast feet, with a front sight glass. On its mycelium bed, six fruiting bodies glow from the gills, and a flesh wick lifts the culture. A stepped glass bell stands in a hematic iron lantern cage, with a vent and a carry bail.
  - The model follows the same body/glass split as the other two stations.
  - `MycelialLanternRenderer` keeps its capped chamber lighting. It now turns the item above the wick and fills `mycelial_lantern_blood` in the sight glass.
  - The gauge moves only when the block syncs, since a test forbids per-tick full syncs.
  - Exports are deterministic with 0 coplanar fights. The model, waterlogging, lighting, workshop and screen-performance contracts pass.
  - Native captures `fungal-station-8..10.png` show the north- and east-facing lanterns with 60% blood in the gauge, which faces the right way on both, and the vivacious spore culture turning in the bell.
- **Not run.** The GameTest suite and `alphaCheck`.

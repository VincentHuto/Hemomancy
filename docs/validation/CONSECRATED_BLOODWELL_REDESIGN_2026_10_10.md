# Consecrated Bloodwell redesign — 2026-10-10

The Consecrated Bloodwell now matches the other redesigned Harbinger stations. A script authors its baked model, replacing the six-cube Blockbench goblet. The fountain renderer was re-fitted to the new basin. Fane binding, pool transfer, the pool monitor and the stake rules are unchanged.

## Design

- **Body.** A heart font for the Founding Fane's heart.
  - Four arterial roots on the diagonals, and eight rootlets, creep out from under a stepped venous-stone plinth in an iron band.
  - A flesh heart (bog flesh) sits on a bold iron dais, with two ventricles, an apex, two atria and coronary veins on each open face. Its aorta, vena cava and pulmonary vessel rise into the basin.
  - Four bone ribs on the diagonals are socketed in iron. They bow out round the heart, pinch, and splay again to carry the basin, with costal spurs and tubercles.
  - The wide hematic iron basin has diagonal pillar straps with bone bosses, a venous frieze, and crust weeping from the brim. Inside are a dried-blood floor, a venous inlay, a bone-ringed ostium where the jet rises, and a crust tide line.
  - The bold rim is notched on the four cardinal flats. Each notch carries a fanged iron spout whose crust channel runs from the pool to the lip. A blood-filled iron catch cup sits on the ground below each spout.
- **Symmetry.** The block has no facing, so the model is symmetric under quarter turns and the blockstate keeps one variant.
- **Materials.** Hematic iron, bone, venous stone, bog flesh, hemorrhagic crust and blood-scorched scab. There is no copper, brass or silver.
- **Renderer** (`ConsecratedBloodwellRenderer`). Its geometry constants name the model dimensions they follow.
  - The pool surface rises with fullness, from just above the dried floor to just below the spout channels. It stays inside the basin wall.
  - The jet rises from the pool surface. Low-pressure arcs fall back into the pool, mid-pressure arcs land on the rim, and high-pressure arcs clear it.
  - The old irregular 5 to 12 rim streams are gone. Above 60% pressure, the pool runs along the four spout channels and pours from each lip into its catch cup.
- **Particles.** These were already Hemomancy/HutosLib particles: absorbed blood cells and HutosLib glow. Basin cells now rise from the actual pool surface, and spilling cups throw blood-cell splashes. There are no vanilla `ParticleTypes` in the bloodwell block, block entity or renderer.
- **Outline.** `ConsecratedBloodwellBlock` traces the plinth, dais, heart column and basin instead of the old goblet.

## Sources

- `tools/model_export/consecrated_bloodwell.py` writes:
  - the block model and the item model (the item carries the baked body, not a parent reference);
  - the blockstate;
  - `models/block/bbmodel/consecrated_bloodwell.bbmodel`, with one `bloodwell` group. This replaces the old hand-made goblet project.
- `--preview` writes `build/consecrated_bloodwell_preview.png`.

## Results

- **Export.** 284 elements. Deterministic: two runs gave identical hashes. `coplanar.py` reports 0 fights.
- **JVM.**
  - `compileJava` passed.
  - `ModelFirstTileRendererResourceTest` passes, run directly through `java`. Its new bloodwell assertions cover:
    - the generator credit, bounds and cutout layer;
    - the heart, spout channels and catch cups, and the baked item;
    - the Blockbench group;
    - the rising pool and spout spill in the renderer;
    - no `ParticleTypes.` in the block, block entity or renderer.
  - `FaneHeartAndStakeSourceTest` passes unchanged. All its renderer contract strings are kept.
- **Native client.** Run `gradlew.bat -I build/bloodwell-world.init.gradle runBloodwellReviewClient`. Exit 0.
  - `BloodwellClientReview` places empty, low (18%), half (55%) and full wells in a disposable world. It sets each client block entity's pool values directly, because an unregistered well's server tick holds zero and never resends it. It captures `build/bloodwell-client-review/screenshots/bloodwell-0..10.png` by day and night, plus the hotbar item.
  - No bloodwell model or texture errors.
  - The empty well shows the dry basin and ostium. The low well's jet stays in its pool. The half well's arcs land on the rim. The full well throws arcs clear of the rim and pours from all four spouts into the cups.
- **Not run.** The full `test` task, the GameTest suite and `alphaCheck`. The live Founding Fane rite and real bloodline pool sync were not exercised.

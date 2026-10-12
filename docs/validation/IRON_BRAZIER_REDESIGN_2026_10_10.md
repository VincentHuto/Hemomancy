# Iron Brazier redesign — 2026-10-10

The Iron Brazier now matches the other redesigned Harbinger stations. A script authors its baked models, and the renderer still draws only the floating offering. Offering, lighting, absorption and rite mechanics are unchanged.

## Design

- **Body.** A rite chalice.
  - Four bone claw feet grip an octagonal hematic iron dais. A venous-stone inlay steps up to the stem.
  - The stem is an iron core threaded through three bone vertebrae, with transverse and spinous processes. A blood vein runs up its front.
  - A stepped iron bowl with a bold rim holds a coal bed with lumps and a charred bone. Blood drips down the bowl's four diagonal facets.
  - Four bone-tipped iron talons curl in from the rim, with inward hooks and outward barbs. They cradle the offering, which floats at 1.05 blocks.
  - An iron pour-gutter with a bone lip faces the player (z = 0 on the north-facing model). It takes the blood of the lighting Projection.
- **Phases.** The blockstate is a facing × `ritual_phase` multipart:
  - `iron_brazier`: always.
  - `iron_brazier_lit`: phases 1 and 2. A full-bright ember bed and embers, crossed flame sheets, and four inward-leaning tongues.
  - `iron_brazier_sanguine`: phase 2 only. Full-bright copies of every vein, slightly proud: the stem vein, the bowl drips, the gutter channel and a dais band.
- **Textures.** `iron_brazier_flame.png` (32 frames, frametime 2, seamless loop) and `iron_brazier_embers.png` are authored by the generator. The shared `crimson_flames` fire read as a muted pink mass on a focal block, so the brazier no longer uses it.
- **Materials.** Hematic iron, bone, venous stone, hemorrhagic crust and blood-scorched scab. There is no copper, brass or silver.
- **Particles.** These are all Hemomancy/HutosLib particles; vanilla `SMOKE` and `SOUL_FIRE_FLAME` were removed.
  - `BrazierBlock.animateTick`: crimson HutosLib embers from the coal bed, hot-core sparks, and dark-glow soot at the flame tips. Phase 2 adds wheeling blood cells.
  - Extinguish: a soot-and-ember collapse.
  - `BrazierItemAbsorptionRite` channel pulses: crimson ember flares.
  - The offering's own `ITEM` crumbs elsewhere stay, since no custom particle can show an item's texture.
- **Outline.** `BrazierBlock` now uses a dais + stem + bowl/talon union instead of the old 8×17 column.

## Sources

- `tools/model_export/iron_brazier.py` writes:
  - the three block models and the item model;
  - the blockstate and both textures (with `.mcmeta`);
  - `models/block/bbmodel/iron_brazier.bbmodel`, with brazier/lit/sanguine groups.
- `--preview` writes `build/iron_brazier_preview.png`.
- The stale duplicate `models/block/bbmodel/brazier.bbmodel` is retired.

## Results

- **Export.** Deterministic: two runs gave identical hashes for the models and textures. `coplanar.py --together` reports 0 fights across body, lit and sanguine.
- **JVM.**
  - `compileJava` and `compileGameTestJava` passed.
  - `ModelFirstTileRendererResourceTest` passes. Its new brazier assertions cover the generator credit, bounds, talons, phase multipart, authored flame and mcmeta, the retired bbmodel, and no `ParticleTypes.` in `BrazierBlock`/`BrazierItemAbsorptionRite`.
  - `WaterloggableDecorativeBlocksTest` passes.
- **Native client.** Run `gradlew.bat -I build/brazier-world.init.gradle runBrazierReviewClient`.
  - `BrazierClientReview` places unlit, lit, sanguine and east-facing sanguine braziers with offerings in a disposable world. It captures `build/brazier-client-review/screenshots/brazier-0..10.png` by day and night.
  - No brazier model or texture errors.
  - The flames animate as crimson fire and glow at night, and the sanguine veins glow.
  - Offerings float inside the talon crown.
- **Not run.** The full `test` task, the GameTest suite and `alphaCheck`.

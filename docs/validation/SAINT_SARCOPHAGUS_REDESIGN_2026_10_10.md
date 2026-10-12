# Saint Sarcophagus redesign — 2026-10-10

The sarcophagus now matches the other redesigned Harbinger blocks. A script authors the baked model, and the renderer draws only what moves. Interactions, offerings, encounters and the 1×3×2 footprint are unchanged.

## Design

- **Tomb.** A polished venous-stone chest tomb stands on a two-step venous brick dais. Hematic iron straps its corners. Each flank has three bone-framed lancet panels carved with vein-trees. The head end carries a weeping witnessing eye. The foot end carries an inscribed bone plaque over the slot the offering gutter drains into. A bone-dentil cornice holds the lid.
- **Lid.** The slab is bound by two iron straps with clasps on both flanks. On top lies a gisant cast in hematic iron:
  - a robed, veiled Saint in a bone mask, whose closed eyes weep blood;
  - a crimson stole down the chest;
  - folded arms holding a lacquered heart;
  - a bone skull at the feet, facing whoever kneels at the bowl.
- **Corpus.** The Saint lies on a crimson pall. It is wound in yellowed, blood-soaked linen, with arms bound at its sides and forearms folded across its chest. Its desiccated hands clasp a sealed vial of its blood, and a crimson stole falls to its knees. Erythrocytic fruiting bodies crown its head and break through the shroud at its side.
- **Foot of the dais.** An iron offering bowl with a bone rim stands on a bone stem, and an iron gutter feeds the tomb. Tallow candle stubs stand in pooled wax on either side. A cresset on a tall iron stand rises at each corner.
- **Materials.** Venous stone, hematic iron, bone, Crimson Lacquer, linen, desiccated flesh, tallow and erythrocytic mycelium. There is no copper, brass, silver or gold.

## Sources

- `tools/model_export/saint_sarcophagus.py` writes the following, using the `fixture_atlas` packer and painter:
  - `saint_sarcophagus.json` and four parts: `_lid`, `_blood`, `_coals` and `_veins`;
  - the closed-tomb item model with display transforms;
  - the facing blockstate, the painted 128×256 atlas and the Blockbench project;
  - `SarcophagusGeometry.java`.
- `--preview` renders four sheets to `build/model-previews/`: `saint_sarcophagus` (sealed), `saint_sarcophagus_open`, `saint_sarcophagus_corpus` and `saint_sarcophagus_gisant`.
- `SaintSarcophagusRenderer` draws four things:
  - The lid slides 8 px east, then tips 18° over the rim edge.
  - The bowl blood rises a third per offering and stays full once the tomb is consecrated.
  - The cresset coals are full-bright and flicker while consecrated.
  - The corpus veins are full-bright while the lid is open. They are faint while Dormant, pulse slowly while Responsive and pulse fast while Awakened.
- `SarcophagusGeometry` holds coarse collision boxes cut into per-cell shapes. The main block and its fillers use them through `SaintSarcophagusBlock.partShape` and `FillerBlock`. Upper cells now outline only the gisant and cressets instead of full cubes. The foot cell still presents a large north face, which the consecration offering requires.
- The brazier and bowl particle anchors moved to the new cresset coals and bowl surface.
- The block no longer uses vanilla particles. Each change follows the visual-toolkit rule:
  - Cresset and bowl `FLAME` became crimson HutosLib embers, the same family as the Ghastly Alembic firebox.
  - The consecration flare at the bowl became blood cells.
  - The dormant `SOUL_FIRE_FLAME` pulse became dark crimson glow.
  - The `SOUL` burst after a peaceful draw became the corpus's blood cells with glow.
- The block entity now syncs `OfferingBloodVolume` and sends updates on each offering and corpus-state change. The bowl level and vein pulse need these.
- Retired: `HarbingerSaintSarcophagusModel` and its layer, `SaintSarcophagusItemRenderer`, `SaintSarcophagusBlockItem` (the generic `BlockItem` now applies), and the placeholder `textures/entity/model_saint_sarcophagus.png`.

## Results

- **Export.** Two runs give identical hashes. `coplanar.py` reports 0 fights on the body, the lid, the item and the body with its blood and coal parts.
- **JVM.** `compileJava`, `compileTestJava` and `compileGameTestJava` pass. `ModelFirstTileRendererResourceTest` has new sarcophagus assertions, and they pass. The legacy main suite has 356 tests and 3 failures. All three are in unrelated in-progress work: a Foxfire reference in `MANIPULATION_BALANCE_PLAN.md`, the cuttlefish chromatophores lang key, and the ossuary clock top cap.
- **Native client.** Run `gradlew.bat -I build/sarcophagus-client.init.gradle runSarcophagusReviewClient`. `SarcophagusClientReview` places four tombs: north sealed with two offerings, east consecrated and open, south awakened and open, and west sealed and unlit. It captures `build/sarcophagus-client-review/screenshots/sarcophagus-0..8.png`.
  - All four facings render with their bowls on the foot end.
  - The north bowl holds two thirds of its blood.
  - Each open lid has slid and tipped toward that facing's east-in-model side.
  - The kindled corpus veins show through both open tombs.
  - The server's flame and glow particles sit on the new cresset coal beds and in the bowl.
  - The item renders in the hotbar and in hand. The first capture showed the first-person transform too large; it was reduced and recaptured.
  - No Hemomancy model or resource errors were logged.
  - The review world is a copy of the reliquary review save. The username must be 16 characters or fewer: `SarcophagusReview` broke the login packet, so the run uses `SarcReview`.
- **Not run.** The GameTest suite and `alphaCheck`.

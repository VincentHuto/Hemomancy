# Book models: plan

Spec: [2026-11-01-book-models-design.md](../specs/2026-11-01-book-models-design.md). Executed inline.

1. **Generator** `tools/model_export/hemomancy_books.py`: one chassis builder (covers, half-spines, page blocks,
   flip leaves) plus a decoration pass per book. It reuses the box-UV `Rig`, `layout`, `paint_atlas` and
   `render_quads` from `unstained_weapons.py`. It writes `client/model/item/book/HemoBookLayers.java` (one
   `LAYER_*` location and `create*Layer()` per book), `textures/entity/book/<book>.png`, and with `--preview` a
   sheet with closed, open and flipping views in `build/hemomancy_books_preview.png`.
2. **Model** `client/model/item/book/HemoBookModel.java` (hand-written): finds the shared parts and `closed_*` /
   `open_*` toggles, and runs `setupAnim(time, rightFlip, leftFlip, open)` with vanilla's opening curve.
3. **Bindings** `client/model/item/book/HemoBookBinding.java`: an enum mapping each book item to its layer, texture
   and cached render types, plus the glow type for the Dictation Table.
4. **Renderers**:
   - `client/render/item/HemoBookItemRenderer` reads `BookAnimState` with `AnimationTime.partialTick()`.
   - `ClientExtensionsRegistrar` routes Hemomancy guide books to it.
   - `EnzymaticScriptoriumRenderer` draws the Liber Sanguinium binding.
   - `DictationTableRenderer` becomes a full Hemomancy renderer with the glow, and the Harbinger Escritoire uses it too.
   - `LayerEvents` registers the four layers.
5. **Items and textures**: point the `ItemInit` texture arguments at `textures/entity/book/*`, delete the old
   vanilla-layout book textures, and tune the item JSON display transforms.
6. **Tests and docs**: update `HarbingerHermitRoadMissionSourceTest`'s renderer and texture pins, add a source test that
   pins the generator outputs and part contract, regenerate the source-text impact map, and update the reference doc's
   book rows.
7. **Verify**: run `test` and `build`, then a client run to screenshot each book in hand, in the GUI, and on the
   Scriptorium and Dictation Table, adjusting transforms until they read well.

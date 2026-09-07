# Puppet summon assets

The seven models follow the recovered August puppet concepts: a skeletal Veinwing, a tripod Marrow Spitter, a hunched Gorebound Hulk, a spool-bearing Mnemonist, a cobra-gorget Mummer, and a bridled Sanguine Hound. The Ringmaster Pattern uses the established top hat, lapels, coat tails, and conductor identity. Its entity type remains the Mnemonist; rendering selects its own model by summon name.

`author_puppet_summons.mjs` contains the authored geometry and material assignments. It replaces only `createBodyLayer()` in the seven Java models, runs the existing Java-to-Blockbench exporter, paints the used UV faces, and embeds the PNG in each `.bbmodel`. Animation and renderer methods remain hand-maintained Java. Run from the repository root with Node and `pngjs` available:

```powershell
node tools/model_export/author_puppet_summons.mjs
./gradlew.bat test --tests '*PuppeteerSummonTextureUvTest' jar
java src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsPreviewSourceTest.java
```

The original atlas dimensions are retained: Vulture, Spitter, and Hulk 128x128; Mnemonist and Mummer 64x64; Hound 64x32. The new Ringmaster uses 128x128. Repeated boxes share UVs only when their material and dimensions match. All opaque pixels use the same twelve RGB colors: `151219 30232c 523039 79333e a44549 cf6159 694d44 997957 bca47b e0cda3 48414a 716777`.

Blood Curs use a brighter blood-only atlas variant, preserving ivory and iron. The Ringmaster glow atlas contains only its command threads. Base textures are not multiplied by a red tint. `SanguineHoundModel.setColor` remains available to existing callers.

## Artwork source

`puppet_materials.png` was produced with the built-in ImageGen tool, then translated to native 16-pixel material grids, restricted to the shared palette, and assigned to the UV faces. Faceplates, seams, iron edges, thread rows, and nozzle cavities receive pixel-grid detailing. This source sheet is authoring input and is not packaged as a runtime texture.

Generation prompt:

> Use case: stylized-concept. Asset type: a production pixel-art MATERIAL TEXTURE SHEET to be sampled onto seven Minecraft blood-puppet entity UV atlases. Generate one square image, an exact 4-column by 3-row grid of twelve square material tiles, each touching its neighbors with NO gutters, NO labels, NO text, NO border. Each tile is a flat orthographic seamless surface sample, NOT a creature or a 3D cube. Crisp hand-painted low resolution pixel clusters; each tile should look like a 16x16 pixel sprite enlarged by nearest neighbor. Consistent dark-fantasy Hemomancy palette across ALL tiles using ONLY these twelve RGB hex colors: #151219 #30232c #523039 #79333e #a44549 #cf6159 #694d44 #997957 #bca47b #e0cda3 #48414a #716777. Tiles row-major: row1: dark burgundy bound coagulate muscle with directional broad sinew striations; porous marrow ivory bone with a few dark fine fissures; black iron with sparse worn grey edge flecks; warm brown stitched leather binding. row2: thin scarlet cobra membrane with a clearly branching burgundy vein pattern; tightly wound crimson tendon thread horizontal rows; muted ivory blank faceplate with a restrained vertical crack and NO eyes or mouth; dark wine lacquered ossified wood with sparse vertical grain. row3: deep black-cherry coat cloth subtle stitched weave; old-gold bone regalia filigree on dark burgundy ground; deep shadowed red marrow nozzle cavity; scarlet cord muscle with pale diagonal tendon seams. No gradients, no blur, no antialiasing, no photographic noise, no characters, no monsters, no skull illustrations. Distinct large pixel clusters, strong material separation, texture suitable for Minecraft pixel density.

## Review boundary

The offline review sheet in `build/puppet_review/` renders the exported meshes from front, side, and back plus representative joint poses. It is not a capture of Minecraft's animation or rendering pipeline. Desktop capture failed with `SetIsBorderRequired failed: No such interface supported (0x80004002)` during this revision. Live-client checks of movement, Mummer performance, Blood Cur rupture, Summons previews, and dismissal remain necessary.

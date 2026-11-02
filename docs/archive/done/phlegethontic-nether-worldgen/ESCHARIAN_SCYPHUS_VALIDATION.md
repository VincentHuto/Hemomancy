# Escharian Scyphus validation

Implemented and checked through 2026-09-15. The authored Blockbench geometry, UVs, textures, and item transforms are preserved across five exported count models. The plant has six-face sturdy attachment, count-aware rotated selection shapes, no movement collision, waterlogging, a normal item/creative entry, and count-aware self-drop loot. Large multiblock cups no longer generate. Existing chunks are unchanged.

## One-to-five groups — current revision

One block space now holds one to five Scyphus. Repeated placement increments the count without changing the original attachment direction or waterlogged state; sneaking retains normal block-placement behavior, and a sixth Scyphus is rejected without consuming it. Breaking or losing support drops the stored count. Counts one through four use `OneSmall`, `TwoSmall`, `ThreeSmall`, and `FourSmall`; count five uses `OneLarge`, the original large authored cup.

Worldgen stores a seeded count on every generated plant position. On fresh seed `20260912`, run `scyphus-counts-20260915c`, the generated area contained 38 count-one, 64 count-two, 47 count-three, 34 count-four, and 42 count-five states. It retained 19 wall/ceiling patches and 2 ground piles, with zero invalid colonies, unrooted colonies, unsupported Scyphus, or incorrect backing blocks.

All 21 focused JVM tests, both disposable-server tests, and the build passed. Runtime coverage includes counts one through five on all six faces, dry and waterlogged stacking, count-aware selection shapes and five-item drops, sixth-placement rejection, rotation/mirroring, replanting, five-item support-loss drops, and white-rim rejection. The disposable client repeated the acceptance test and visually confirmed the five model states. Evidence: `build/escharian-counts-final.log`, `build/escharian-counts-runtime-final.log`, `build/phlegethontic-worlds/20260912-scyphus-counts-20260915c/phlegethontic-validation.json`, and `build/escharian-counts-client.log`.

![Scyphus count states one through five](scyphus-review/count-states.png)

## Terrain integration — previous revision

All generated ground, wall, and ceiling colonies now have infested venous stone foundations. A roughly two-block infested band follows the exposed terrain, interlaced with a further two-block transition of regular venous stone and the original material. Replacements preserve the terrain contour and exclude ores, structures, fluids, and decorations. Ground piles retain dense cups on dark supports and bare white base rims. Manual planting does not paint terrain; existing chunks remain unchanged.

The 21 focused JVM tests, both disposable-server tests, and build passed. The fresh seed `20260912`, run `scyphus-substrate-20260914b`, contains 19 wall/ceiling patches and 2 ground piles, with zero invalid or unrooted colonies. Tests cover deterministic stone bands, all six surface directions, buried foundations, steps and corners, protected material, and the existing attachment/harvest checks. Neighboring dark growth bodies remain separated to preserve colony size limits; their surrounding stone patches may blend together.

Evidence: `build/escharian-substrate-final.log`, `build/phlegethontic-worlds/20260912-scyphus-substrate-20260914b/phlegethontic-validation.json`, and `build/escharian-substrate-client.log`. The build used the focused Escharian test filter, not the entire repository test suite. Natural colonies in the disposable client were inspected with Night Vision and captured below.

![Ground pile with infested foundation and venous stone transition](scyphus-review/ground-substrate.png)

![Wall colony integrated into the terrain](scyphus-review/wall-substrate.png)

![Ceiling colony following the stone contour](scyphus-review/ceiling-substrate.png)

## Denser ground piles — previous revision

Ground piles now fill every available exposed position around dark centre blocks, allowing multiple cups on different faces of a single block. White rim blocks are excluded from generated placement, placement preflight, and manual attachment. Other sturdy manual supports remain valid. Wall/ceiling patch coverage and feature rarity are unchanged.

The same fresh seed now produces 11 cups on the larger ground pile (previously 6) and 5 on the smallest pile (previously 4), all attached to dark blocks. The 19 wall/ceiling patches are unchanged. The 17 focused JVM tests, both runtime tests, and build pass. Runtime tests cover white-rim rejection on every face, dry and waterlogged, item preservation after rejection, and dropping a cup when its dark support is replaced by white rim.

Previous revision evidence: `build/escharian-dense-validation.log`, `build/phlegethontic-worlds/20260912-scyphus-dense-20260914a/phlegethontic-validation.json`, and `build/escharian-dense-client.log`. Both naturally generated piles were inspected in the disposable client with Night Vision and captured below. Earlier captures further down document the initial implementation.

![Denser ground pile](scyphus-review/ground-pile-dense.png)

![Small ground pile](scyphus-review/ground-pile-small-dense.png)

## Initial verification

- 17 focused JVM tests passed; `compileGameTestJava` and `build` passed. The build used the focused Escharian test filter, not the entire repository test suite.
- Both disposable-server tests passed: fresh Nether generation and real BlockItem attachment/harvest checks. Dry and waterlogged placement, rotated shapes, mirrors, empty-hand harvesting, replanting, and support-loss drops were exercised on all six faces.
- Fresh seed `20260912`, run `scyphus-20260914c`: 576 chunks, 19 wall/ceiling patches and 2 ground piles, with zero invalid colony sizes, coverage, or ground-rim/taper checks. Patches contain 10–30 backing blocks; the two solid piles measure 3×3×4 and 3×2×3 (width × height × depth).
- 381 solid growth blocks and 217 plants in the generated area. Plant facings: 35 down, 86 up, 22 north, 17 south, 28 west, 29 east. Zero unsupported plants, invalid plant backings, elevated rim blocks, sampled Basin escapes, ichor-proximity failures, out-of-chunk writes, or fluid escapes.
- Disposable client `Scyphus Review Final 20260914`: all six manual attachment orientations inspected from above and below; attachment/harvest checks also passed with the real player. Natural wall, ceiling and ground colonies were inspected and captured. The client was stopped normally.

The first server pass caught waterlogged support loss silently replacing the plant without loot. Scyphus now requests normal destruction, which drops the item and restores its water. The client fixture needs its chunks loaded before counting item entities; the final client run preloaded the review area. Its temporary water sources are explicitly cleared after testing.

Surface patches replace only exposed tagged natural stone. Inward cavern corners join exposed cells along an edge, so surface traversal and colony validation include those edge connections while leaving buried corner stone intact. No rim blocks are used on wall or ceiling patches. Ground attempts retain the one-in-eight preference and habitat fallback. Legacy configurations preserve ichor radius and anchor-search budget, while obsolete cup settings give way to defaulted patch/pile tuning.

## Captures

Natural wall colony, with Night Vision for texture and attachment visibility:

![Wall colony](scyphus-review/wall.png)

Natural ceiling colony, with Night Vision:

![Ceiling colony](scyphus-review/ceiling.png)

Natural 3×3×4 ground pile, with Night Vision:

![Ground pile](scyphus-review/ground-pile.png)

Ambient lighting: [wall](scyphus-review/wall-ambient.png), [ground pile](scyphus-review/ground-pile-ambient.png).
Six-direction fixtures: [above/northeast](scyphus-review/orientations-above.png), [below/southwest](scyphus-review/orientations-below.png).

## Local evidence

- `build/escharian-scyphus-complete.log`
- `build/phlegethontic-worlds/20260912-scyphus-20260914c/phlegethontic-validation.json`
- `build/escharian-scyphus-client-final.log`
- `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`

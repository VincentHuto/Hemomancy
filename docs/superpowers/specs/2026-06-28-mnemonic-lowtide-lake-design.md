# Mnemonic Lowtide Lake Design

## Goal

Build the first visible pass for `hemomancy:mnemonic_lowtide`: a glossy black, beige, and red lake that appears below the Chamber of Will refuge as an endless lower-horizon skybox layer. The lake feels close to the blood-wood floor, but unreachable and not physically under the platform. It must be a living tessellated surface with shader-driven wave motion and shader-driven liquid texture noise, not a flat static plane.

## Current State

- `ChamberOfWillManager` already exposes `THEME_MNEMONIC_LOWTIDE` and includes it in the ordered Chamber sky theme list.
- `ChamberSkyThemeRegistry` currently registers Lowtide with a dark blank skybox and `BlankChamberThemeEffects`.
- `AbstractChamberThemeEffects` already gives each theme an isolated effects strategy, with Qliphoth and Silent Archon as examples of bespoke theme-owned passes.
- `AbstractChamberThemeEffects.renderSky(...)` is the correct path for a lake that should behave like Silent Archon's clouds and monolith depth: camera-relative, skybox-scale, and impossible for the player to fly under.
- `ShaderInit` and `HemoRenderTypes` already support custom world shaders through `ShaderHolder`, `ExtendedShaderInstance`, and render-type uniform setup. The Mycelial Crucible basin is the closest liquid-shader precedent, but it only uses fragment noise and CPU-side height ripples.
- `docs/HEMOMANCY_REFERENCE.md` currently documents Lowtide as a reserved blank transitional theme.

## Design

Add a dedicated `MnemonicLowtideChamberEffects` class in the Chamber of Will renderer package and register it for `THEME_MNEMONIC_LOWTIDE` instead of `BlankChamberThemeEffects`. This class owns the theme identity and renders the lake inside the Chamber skybox pass. The rest of the skybox remains restrained in this first pass, keeping Lowtide focused on the nearby lower-horizon tide rather than introducing the full later sky language yet.

Render the lake from `MnemonicLowtideChamberEffects.renderBeforeSharedLayers(...)` using `ChamberThemeRenderContext.skyDistance()`. Because this happens in skybox space, the lake moves with the Chamber sky rather than existing at a world Y coordinate. The blood-wood refuge renders afterward as normal world geometry, so it visually occludes the lake without making the surface reachable.

The mesh is a large tessellated skybox quad grid bowed slightly downward at the edges. It sits in the lower camera-relative hemisphere, close enough that the floor edge frames it, but scaled far beyond the room so it reads as an endless flooded horizon. The renderer must not depend on Chamber floor Y, synced Chamber radius, or Chamber cell spacing.

The mesh is renderer-only. It adds no blocks, no collision, no fluids, no player movement rules, and no gameplay reachability. Existing Chamber safety, rescue, item recovery, and bounds behavior remain untouched.

## Shader And Render Type

Add `ShaderInit.MNEMONIC_LOWTIDE_LAKE`, backed by:

- `assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json`
- `assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh`
- `assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh`

Add `HemoRenderTypes.mnemonicLowtideLake(...)` using `DefaultVertexFormat.POSITION_TEX_COLOR`, quad mode, translucent blending, `LEQUAL_DEPTH_TEST`, `NO_CULL`, `NO_LIGHTMAP`, and color-only writes. Depth writes stay disabled so the animated tide does not interfere with later sky overlays or world geometry.

The vertex shader performs the real surface motion. It should displace Y with layered waves using world/grid position, time, and a stable chamber seed. Include at least two wave scales: a broad slow swell and a smaller cross-current. Damp displacement near the outer mesh edge so the surface does not visibly tear against the horizon or room boundary.

The fragment shader performs the liquid material. It should use fbm/noise and slow UV warping to mix:

- near-black glossy base
- deep oxidized red streams and pools
- beige/parchment highlights like reflected pages or memory silt

The surface feels viscous and reflective, not clean water. Highlights are sparse and moving, with stronger red/beige detail in streaks and softer dark areas between them. The shader exposes uniforms for time, seed, wave strength, noise scale, gloss/highlight strength, and edge fade so tuning can happen without rewriting shader logic.

## Scope

Included:

- Replace Lowtide's blank effects strategy with a dedicated Lowtide strategy.
- Render a tessellated skybox-space lower-horizon lake in the Lowtide theme.
- Add a custom shader with both vertex wave displacement and fragment liquid noise.
- Keep the skybox lake free of world-distance fog so height changes do not wash black/red/parchment tones toward the Chamber fog color.
- Update source tests that currently expect Lowtide to use `BlankChamberThemeEffects`.
- Update `docs/HEMOMANCY_REFERENCE.md` so Lowtide is no longer documented as blank.

Not included:

- New gameplay hazards, collision, swimming, fluids, blocks, or rescue rules.
- Full Lowtide skybox/weather overhaul beyond what is needed to frame the lake.
- Floating parchment props, ruined towers, reflections of buildings, or paper debris. Those can be follow-up passes once the lake base is working.
- Changes to progression gates or the command theme override flow.

## Testing

- Add or update source tests to assert that `ChamberSkyThemeRegistry` registers Lowtide with `MnemonicLowtideChamberEffects`.
- Add source tests for shader registration and render-type ownership: `ShaderInit.MNEMONIC_LOWTIDE_LAKE`, shader resource filenames, and `HemoRenderTypes.mnemonicLowtideLake(...)`.
- Adjust stale Chamber renderer path assumptions in affected tests so the moved `client/render/world/chamberofwill` package is the source of truth.
- Run `./gradlew.bat test`.
- Run `./gradlew.bat compileJava`.
- Run `./gradlew.bat runClient` for visual validation in the Chamber using `/hemo chamber theme set mnemonic_lowtide`, checking that the floor frames the lake, the lake extends like an endless lower horizon, flying below the platform does not let the player get underneath it, and the surface shows real wave motion plus black/beige/red texture noise.

## Documentation

Update `docs/HEMOMANCY_REFERENCE.md` after implementation to describe Lowtide as a transitional skybox-space horizon tide theme. The wording should preserve the Chamber's inward-refuge framing: this is the first destabilizing blood-memory tide around the refuge, not a new dimension, external ocean, or apocalyptic end state.

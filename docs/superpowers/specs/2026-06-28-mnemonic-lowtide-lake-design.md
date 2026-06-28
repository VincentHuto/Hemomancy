# Mnemonic Lowtide Lake Design

## Goal

Build the first visible pass for `hemomancy:mnemonic_lowtide`: a glossy black, beige, and red lake that sits just below the Chamber of Will refuge. The lake feels close to the blood-wood floor, but unreachable. It must be a living tessellated surface with shader-driven wave motion and shader-driven liquid texture noise, not a flat static plane.

## Current State

- `ChamberOfWillManager` already exposes `THEME_MNEMONIC_LOWTIDE` and includes it in the ordered Chamber sky theme list.
- `ChamberSkyThemeRegistry` currently registers Lowtide with a dark blank skybox and `BlankChamberThemeEffects`.
- `AbstractChamberThemeEffects` already gives each theme an isolated effects strategy, with Qliphoth and Silent Archon as examples of bespoke theme-owned passes.
- Chamber world-stage rendering already runs from `ClientEvents.renderLevelLastEvent` at `RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS` for other world overlays. This is the correct depth-aware stage for a lake that should be hidden by the refuge floor and platform geometry.
- `ShaderInit` and `HemoRenderTypes` already support custom world shaders through `ShaderHolder`, `ExtendedShaderInstance`, and render-type uniform setup. The Mycelial Crucible basin is the closest liquid-shader precedent, but it only uses fragment noise and CPU-side height ripples.
- `docs/HEMOMANCY_REFERENCE.md` currently documents Lowtide as a reserved blank transitional theme.

## Design

Add a dedicated `MnemonicLowtideChamberEffects` class in the Chamber of Will renderer package and register it for `THEME_MNEMONIC_LOWTIDE` instead of `BlankChamberThemeEffects`. This class owns the theme identity and exposes a static world-render entry point for the lake. The skybox remains restrained in this first pass, keeping Lowtide focused on the nearby subfloor tide rather than introducing the full later sky language yet.

Render the lake from the late world render stage, after translucent blocks, only when the player is inside `ChamberOfWillManager.CHAMBER_OF_WILL` and the active client theme is `hemomancy:mnemonic_lowtide`. That stage has terrain depth available, so the blood-wood refuge can naturally occlude the lake. The lake renderer builds its own camera-relative `PoseStack` using the event pose stack and camera position pattern used by existing world overlays, then snaps the lake center to the current Chamber cell so it follows the player's private room instead of drifting with camera motion.

The mesh is a wide tessellated quad grid, centered under the Chamber cell and positioned slightly below the floor. It sits low enough that the floor edge and platform block the player from feeling able to step onto it, but close enough that the surface reads as a nearby underfloor tide. The initial target is 1.25 blocks below the Chamber floor, with lake span based on the synced Chamber radius plus margin so tier growth does not expose hard edges.

The mesh is renderer-only. It adds no blocks, no collision, no fluids, no player movement rules, and no gameplay reachability. Existing Chamber safety, rescue, item recovery, and bounds behavior remain untouched.

## Shader And Render Type

Add `ShaderInit.MNEMONIC_LOWTIDE_LAKE`, backed by:

- `assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json`
- `assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh`
- `assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh`

Add `HemoRenderTypes.mnemonicLowtideLake(...)` using `DefaultVertexFormat.POSITION_TEX_COLOR`, quad mode, translucent blending, `LEQUAL_DEPTH_TEST`, `NO_CULL`, `NO_LIGHTMAP`, and color-only writes. Depth testing stays enabled so room geometry occludes the lake; depth writes stay disabled so the animated tide does not interfere with later overlays.

The vertex shader performs the real surface motion. It should displace Y with layered waves using world/grid position, time, and a stable chamber seed. Include at least two wave scales: a broad slow swell and a smaller cross-current. Damp displacement near the outer mesh edge so the surface does not visibly tear against the horizon or room boundary.

The fragment shader performs the liquid material. It should use fbm/noise and slow UV warping to mix:

- near-black glossy base
- deep oxidized red streams and pools
- beige/parchment highlights like reflected pages or memory silt

The surface feels viscous and reflective, not clean water. Highlights are sparse and moving, with stronger red/beige detail in streaks and softer dark areas between them. The shader exposes uniforms for time, seed, wave strength, noise scale, gloss/highlight strength, and edge fade so tuning can happen without rewriting shader logic.

## Scope

Included:

- Replace Lowtide's blank effects strategy with a dedicated Lowtide strategy.
- Render a depth-tested tessellated subfloor lake in the Lowtide theme.
- Add a custom shader with both vertex wave displacement and fragment liquid noise.
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
- Run `./gradlew.bat runClient` for visual validation in the Chamber using `/hemo chamber theme set mnemonic_lowtide`, checking that the floor occludes the lake, the lake is close but unreachable, and the surface shows real wave motion plus black/beige/red texture noise.

## Documentation

Update `docs/HEMOMANCY_REFERENCE.md` after implementation to describe Lowtide as a transitional subfloor tide theme. The wording should preserve the Chamber's inward-refuge framing: this is the first destabilizing blood-memory tide under the refuge, not a new dimension, external ocean, or apocalyptic end state.

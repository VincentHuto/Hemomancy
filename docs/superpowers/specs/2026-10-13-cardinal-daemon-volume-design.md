# Cardinal Rite daemon as a smoke volume

Date: 2026-10-13
Status: approved in conversation (sections 1 and 2)
Supersedes the card drawing in `2026-10-13-cardinal-daemon-haze-design.md`. That spec's look carries over:
- the Humanity-sprite silhouette;
- the black outline;
- three Monolith eyes on a dark face;
- swallow, emergence, recall and merge behaviour.

## Goal

The daemon is billboard-flat, and over bright sky it reads pale because the sky washes through it. Give it real substance: a 3D smoke volume that holds its shape from every side, is dense at the core, and is shaded and self-shadowed.

## Decision (from brainstorming)

**A true 3D volume, ray-marched in a box.** The two rejected options were stacked slices and a card with faked depth.

## Section 1: what you see

- **Shape.** A round head on a teardrop body that is round in cross-section, using the approved profile in height units:
  - head radius 0.111 at 0.85;
  - tip at 0.03;
  - widest 0.151 at the chest (0.58);
  - shoulders capped by `sqrt(1 - a³)` up to the crown (0.79).
- **Smoke.** 3D swirling noise drifts upward. The edges fray, and the lower third breaks into wisps.
- **Substance.** The core is dense, so extinction approaches opaque through the body and the sky no longer washes through.
- **Shading.**
  - Lit from above: the head and shoulders take a dim crimson highlight, while the underside and far side are deep red-black.
  - Self-shadowing comes from the shape's gradient toward the light.
  - The interior is darker, with a faint ember that breathes inside the core.
- **Outline.** A shell of sooty black smoke wraps the surface and replaces the back card.
- **Eyes.** The three Monolith eyes (existing `monolithEye` and layout) sit on the front of the head and turn with the daemon's yaw.
  - They fade as the head's surface turns away, so they are hidden from behind.
  - They are veiled by the smoke in front of them, and their light spills into the haze around them.
  - They blink, flicker on recall, and flare with the world stage.
- **Kept:**
  - emergence (bottom-up);
  - swallow flash and tint;
  - recall thinning;
  - merge shrink;
  - a camera inside the volume sees out through the smoke.
- **Trail.** The trail is 3D: the smoke lags opposite the daemon's motion in world space.

## Section 2: how it is built

- **Box.** Each daemon is a box centred on the entity. Its half-width is 0.24 × height and its half-height is 0.55 × height (`DaemonHaze.box(scale)`). It is queued through `DaemonHazeQueue` and drawn at `AFTER_WEATHER` as today.
- **Vertex data.** Each vertex carries box-local coordinates. UV holds local x and z in blocks; vertex colour red holds the height fraction (0 at the bottom, 255 at the top). The vertex shader rebuilds the local position from these, independent of the view transform.
- **Faces.** All six faces wind counter-clockwise when seen from outside, and the render type has no culling.
  - Outside the box, the shader keeps front faces only (`gl_FrontFacing`) and starts rays at the box entry.
  - Inside the box, it keeps back faces and starts rays at the camera.
- **Uniforms.**
  - `HemoTime`
  - `HazeSeed`
  - `HazeState` (vec4: emergence, strength, swallow, eyes open)
  - `HazeTint` (vec4: swallow tint rgb, flare)
  - `HazeLean` (vec4: trail x, y, z in world axes, recall)
  - `HazeCamera` (vec3: camera minus daemon centre, blocks)
  - `HazeShape` (vec4: height in blocks, yaw in radians, box half-width, box half-height)
- **Ray marching.**
  - At most `MARCH_STEPS = 40` steps per ray, with jitter to hide banding.
  - Accumulation is front to back, with early exit below 2% transmittance.
  - Each step costs two 3D fbm noises (warp with 3 octaves, smoke with 4) and two shape evaluations (one for lighting).
- **Eyes.** They come from the analytic ray–head-sphere hit. They are composited at that depth, using the transmittance built up in front of them.
- **Java.**
  - `DaemonHaze`: `box(scale)` and `trail(motion)` replace `card`, `right`, `lean` and `eyes`. Eye visibility is now geometric, so the uniform carries only `eyesOpen(emergence)`.
  - `DaemonHazeQueue`: `boxVertices(halfWidth, halfHeight)` replaces the cards.
  - `HemoRenderTypes`: the factory takes the queued draw plus the camera offset.
- **Docs.** Update the daemon paragraph in `HEMOMANCY_REFERENCE.md`.

## Testing

- **JVM unit tests.**
  - `box` dimensions and positive sizes at minimum scale.
  - `trail` lags the motion and is capped.
  - Box vertices: 24, wound outward, UV equal to local x/z, red set only on top vertices.
  - The swallow, strength, emergence and sink tests are kept.
- **Source tests.**
  - The new uniforms are declared and registered.
  - The march loop and `gl_FrontFacing` handling are present.
  - The render type has no culling.
  - Three eye calls.
  - Rising smoothsteps.
- **Previews.** A numpy port of the full fragment renders the daemon from the front, the side, three-quarters, behind and above, for review before launch.
- **Shader checks.** glslangValidator, the reserved-word test, and a native client launch on the user's NVIDIA GPU.

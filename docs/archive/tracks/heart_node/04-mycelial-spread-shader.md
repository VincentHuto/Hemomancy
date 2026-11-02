# Track 4: the mycelial spread (a ground-creeping shader, not a bubble)

**Status (2026-10-29): built, uncommitted.** What exists:
- `client/render/world/HeartNodeSpreadGeometry` (pure; `HeartNodeSpreadGeometryTest`): `drawsFor(settings, relation)`, per-corner `bodyStrength` / `edgeDepth` / thread legs (`threads`, `arrivals`, `nearestThread`, signed distance and pulse run) / `wallWeight` / `ceilingWeight`, growth (`heartFront`, `synapseFront`, `witherLife`) and `pack` into the vertex colour.
- `HeartNodeSpreadRenderer`: drawn from `WorldEffectDispatcher` in AFTER_TRANSLUCENT_BLOCKS, first in that stage (before the mausoleum ward and the rite visuals). One `HemoRenderTypes.heartNodeSpread(owner, uniforms)` per sanctum (a `SeededRenderTypes` keyed by owner UUID; the seed tile comes from the UUID in the factory), one draw. The early exit is pinned by `HeartNodeSpreadRendererTest` (the shared `List.of()`).
- Shader `shaders/core/world/heart_node_spread.{json,vsh,fsh}` (`ShaderInit.HEART_NODE_SPREAD`, BLOCK format). Imports `vein_hash1`, `vein_noise`, `value_hash`, `material_noise`, `material_fbm`. Lit through the lightmap (UV2 is each face's sampled light), so the colour channels, UV0.x and Normal carry the data. 8 lobe slots (heart, live synapses, receding synapses).
- Block changes: `MixinClientLevel` (client mixin on `sendBlockUpdated`) marks the section and its neighbours dirty.
- Motes: `HeartNodeSpreadRenderer.tickMotes()` from `ClientTickEvents`.
- Client config `heartNodeSpread` (`HemoClientConfig.RENDER_HEART_NODE_SPREAD`).
- Review: `HeartNodeSpreadScenes` from `HeartNodeClientReview` (captures `heart-node-10` … `-18`). It replants the node in place with an older planting time and re-adds its synapses, so growth and maturity show without waiting 3 minutes. The stranger scenes use Track 2's neighbour sanctum.

**Decisions (review these):**
- **No "skip faces under carpet":** a carpet or snow layer isn't solid, so the floor under it is found; the face is lifted to the layer's top instead of skipped.
- **Thread distance is per corner and signed,** so a 0.25-block core stays sharp inside one face; past a leg's ends it becomes the distance to the end.
- **The heart's front applies to the heart lobe only.** A synapse raised while the node is still growing spreads its own lobe on its own 10-second clock.
- **A stranger sees the pulse ring but no flare and no threads.** Their spread motes follow the owner's Spores bit like the owner's do.
- **A node broken outright** drops the whole envelope from the packet, so its spread vanishes at once with no recede. Track 5's withering presentation covers that beat.

**Depends on:** Track 1's `HeartNodeEnvelopePacket` / `HeartNodeClientState` (heart, synapses, relation, `plantedAt`, stage, settings bits). It reads Track 3's settings; until Track 3 lands, treat every setting as its default.

**Goal:** the sanctum is shown by a living layer of mycelium creeping over the surfaces inside the envelope, drawn by a shader. It is thick and veined near the heart, thins toward the lobes' edges, and frays into feathered fronds at the boundary. Threads run along the ground from the heart to each synapse, and a slow heartbeat wave rolls outward. There's no dome, sphere or wall. You read where your sanctum ends by where the growth stops.

## References already in the mod (reuse their approach, not their art)
- **`MausoleumWardRenderer`** + `mausoleum_ward` shader (`shaders/core/world/mausoleum_ward.*`, `HemoRenderTypes.mausoleumWard`). This is the structural template:
  - exposed faces found once per chunk section through `BlockShellFaces`, kept per anchor, rebuilt on a staggered timer and capped per frame (`BUILDS_PER_FRAME`, `SECTIONS_KEPT`);
  - everything in one render type, one draw;
  - the face reach limited to a radius around the camera;
  - with nothing loaded, the pass returns before allocating.
- **`FaneBoundaryRenderer`** is what this replaces for the heart-node, not something to copy. Its relation handling (`FaneBoundaryRelation`, an alpha fade on crossing) is the model for owner and stranger handling.
- **Infusion veins** (`project_infusion_veins`: a vein tree grows from an impact point across a block) for branching growth from a point.
- **Shared noise**: `shaders/include/noise/vein_noise.glsl`, `flow_noise.glsl`, `material_fbm.glsl`, pulled in with `#moj_import <hemomancy:noise/<name>.glsl>`, one import per include in dependency order (`ShaderNoiseIncludeGuardrailTest`). Seeded noise that reads this shader's own uniforms stays local.

## Target

### What gets drawn
**`HeartNodeSpreadRenderer`** in `client/render/world`, called from `WorldEffectDispatcher` at its slot (after terrain, translucent pass, before the ward; pick and document the order).
- **Faces.** For every envelope in `HeartNodeClientState` within reach, collect exposed faces of solid blocks inside `HeartNodeFootprint.contains`, per chunk section, with the ward's caching. Weight each face by orientation:
  - floors (up faces) get the full layer;
  - walls get the layer climbing up to about 1.5 blocks from the floor below, thinning with height;
  - ceilings (down faces) get only hanging frond tips near walls.
  This keeps it a ground spread in a 3D place.
- **Block filter.** Skip faces of fluids, glass, leaves and the heart-node and synapse blocks themselves; Track 5 draws those. Skip faces hidden under carpet or snow layers (draw on the layer instead).
- **Per-vertex data** (use `MaterialQuads` conventions where they fit; seed tiles through `MaterialQuads.seedTile`):
  - the face's world position relative to the heart;
  - its normalised envelope strength (Track 1's `strength(pos)`, computed on the CPU when the section is built);
  - distance to the nearest lobe edge, for the frond fray;
  - nearest-thread distance (below);
  - face orientation.
- **One render type** built through `SeededRenderTypes` keyed by owner seed (the owner UUID's hash, so two Apotheoi never shimmer in sync), or a static type with the seed in the vertex colour like the ward. Never one per call or frame (`RenderTypeCachingGuardrailTest`). Cutout plus a small additive glow pass in one shader, z-offset like the ward so it never z-fights.

### The shader `heart_node_spread` (`shaders/core/world/heart_node_spread.*`)
- **Body.** A mat of hyphae: layered `vein_noise` at two scales gives branching strands. Between them, a pale felted fill (`material_fbm`) whose coverage rises with envelope strength: near-solid within 4 blocks of the heart, a lace of strands at the lobe edges. Palette: bone-white and pale grey felt, deep crimson veins in the main strands, a faint inner glow in the veins only. Gardens-pale with blood under it, no bright saturation.
- **Edge.** Inside the last ~1.5 blocks before the envelope edge, strands thin into feathered frond tips that reach outward and stop. Coverage drops to nothing on a noise-perturbed line, so the boundary is legible but organic, never a circle.
- **Growth.** The spread grows outward from the heart after planting. A front radius rises with `plantedAt` and stage over `MATURE_TICKS` (Track 1), with a bright leading rim of fresh white hyphae. A newly raised synapse spreads its own lobe outward from itself over ~10 seconds, the moment it is raised (the client sees the synapse appear in the envelope packet and keeps a per-synapse appear time). A withered synapse's lobe recedes and greys over ~3 seconds.
- **Threads.** For each active synapse, a thread is a thicker crimson-cored hypha following the ground from the heart toward the synapse. CPU side, each face gets its distance to the heart→synapse polyline (projected to the ground plane, floors only). The shader draws a glowing core where that distance is under ~0.25 blocks, with noise wobble so it meanders. Off when `threads` is off.
- **Pulse.** Every `HEARTBEAT_TICKS` (start: 50, matching the node's model beat in Track 5), a soft brightening ring travels outward from the heart through the veins at ~6 blocks per second, then down each thread to its synapse, which flares. Off when `pulse` is off.
- **Owner and stranger.** The owner sees the full layer at their `intensity`. A stranger sees nothing unless `showStrangers` is on, and then a desaturated, dimmer layer (alpha × 0.5, no threads, no pulse flare), so a visitor knows they've stepped into someone's place without being told the layout.
- **Proximity.** Within ~2 blocks of the viewer's feet the strands stir slightly away from them, like the ward's player press (a `PlayerOffset` uniform). Cosmetic only.
- **Time** from `AnimationTime` (`gameTime(level, partialTick)` so `/tick freeze` holds still).
- Prove it compiles in a client launch (offline validators miss driver errors; see `project_shader_gpu_check`).

### Motes
When `motes` is on, a client tick spawns a few `HemoParticleData` spore motes (`sporiticSpore` in pale colours) rising slowly from random floor faces in the heart lobe, about 3 per second near the camera, fewer far away. Never vanilla particles. This drives the ambient feel without spamming.

### Performance
- Section builds capped per frame; sections kept per envelope capped (start: 64). With no envelope in reach, the pass returns before allocating (pin this in a guardrail-style unit test on the renderer's early exit, like the ward has).
- Reach: draw faces within 48 blocks of the camera. The envelope itself can be larger.
- Rebuild a section when the envelope packet changes it (a synapse raised or withered) or every 100 ticks staggered, since block changes in the Gardens are rare. Also rebuild on a `BlockUpdate` the client sees inside a kept section (cheap: mark the section dirty).

### Configuration
- A client config `heartNodeSpread` (on/off) in `HemoClientConfig`, beside `renderCardinalRiteFog`. It overrides everything to off for players who want no overlay at all.

## Tasks
1. Renderer skeleton through `WorldEffectDispatcher`: envelope gather, section face collection with the orientation weights, caching, early exit. Draw flat colour first and confirm the shape.
2. CPU per-face data: strength, edge distance, thread distance, orientation; unit-tested in a pure helper `HeartNodeSpreadGeometry` (no client classes in the test).
3. `heart_node_spread` shader, its render type, the noise includes.
4. Growth, synapse appear and wither timing; the pulse.
5. Owner and stranger modes and settings bits; the client config override.
6. Motes.
7. Review client scenes:
   - fresh plant (growth front visible);
   - mature node by day and in the dark;
   - a three-synapse chain from above;
   - owner view and stranger view (`showStrangers` on and off);
   - `intensity` 0 and 4;
   - a withering synapse mid-recede.
8. HR §5.6d visual paragraph; remove any heart-node mention of a bubble or ribs.

## Acceptance
- No dome or sphere anywhere. The envelope reads from the ground growth alone, with the edge visible as fraying fronds.
- Walls carry the growth only near the floor, and ceilings only tips. It reads as creeping from the ground up, not as a box painted on every face.
- A newly raised synapse visibly spreads its lobe. Threads lead from the heart to each synapse. The pulse travels heart → threads → synapses.
- Strangers see nothing with `showStrangers` off.
- One draw call per envelope in view. No frame spike entering an envelope (section builds capped).
- The shader compiles and renders on the dev machine's GPU in the review client.

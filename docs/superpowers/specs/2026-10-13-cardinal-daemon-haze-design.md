# Cardinal Rite daemon as a red haze

Date: 2026-10-13
Status: approved in conversation (sections 1 and 2)

## Goal

Replace the Cardinal Rite daemon's "veiny monster" look with something simpler: a red smoky haze shaped like a tall hooded figure, with two glowing red eyes. Draw it almost entirely in one shader. Only the look changes; the daemon's behaviour stays the same.

## Decisions (from brainstorming)

- **Silhouette:** a hooded haze. The head and shoulders are denser; the robe thins into wisps toward the ground; the eyes sit in the hood's shadow.
- **Offerings:** the haze swallows them. Each arriving offering drifts into the smoke, shrinks and dims, and the haze flashes and swells, tinted toward the offering's colour. There are no nodules.
- **Approach:** a shader billboard. Two camera-facing cards and one new shader (`cardinal_daemon_haze`).

## Section 1: the look

### Shape

- **Cards.** The daemon is two cards, each `CardinalRiteVisualRules.DAEMON_HEIGHT` (4) blocks tall times the sprite scale, and about half that wide. Both turn around the vertical axis to face the camera.
  - The **front card** carries the figure and the eyes.
  - The **back card** sits slightly behind it, relative to the camera. It is about 15% larger, its noise drifts slower, it is darker and it has no eyes, so the smoke shows depth as the viewer moves.
- **Hooded silhouette.** The shader draws it as a soft signed field:
  - a rounded hood and shoulders in the top third;
  - a robe that narrows toward the ground.
- **Edges.** Domain-warped fbm noise rising over time continually erodes the edge, so the figure never has a hard outline. The lower third breaks into wisps that drift upward and outward.

### Colour

- A deep, nearly black red at the dense core.
- Crimson toward the edges.
- A faint ember glow that breathes on the rite's heartbeat (`CardinalRiteMath.heartbeat`).
- No copper, brass, bronze or silver tones.

### Eyes

- Two small, bright red-orange eyes sit at head height in the hood's shadow, with a soft glow around each.
- **Facing:** eyes are fully visible when the daemon faces the camera and fade out as it turns away. They are hidden from behind.
- **Blinking:** a rare blink, timed from a hash of time and seed.
- **Escalation:** the eyes flare slowly brighter as the rite's world stage rises (`RiteWorldState`), up to a cap.

### Moments

- **Emergence:** density is masked from the bottom up by emergence progress, so the smoke rises out of the staff. The eyes open only at the end of emergence.
- **Swallowing:** for about 20 ticks after an offering arrives:
  - its item is drawn moving from the arrival point into the haze's chest, shrinking to nothing and dimming;
  - the haze brightens and swells slightly, tinted toward the offering's colour (`CardinalRiteOfferingMemory.colour`).
- **Culmination merge:** the server's existing shrink toward the caster plays as the smoke collapsing inward. The eyes stay until the haze is nearly gone.
- **Cancellation:** a lower firmness, the existing cancellation progress, thins and dims the haze, and the eyes flicker.
- **Flying** (procession hover, merge): the haze does not pitch over. Its smoke leans and streams opposite its motion, using the lean computed from movement.
- **Accents:** the entity's existing blood-cell shedding (`emitClientAccents`) stays.

## Section 2: code

### New

- **Shader:** `assets/hemomancy/shaders/core/world/cardinal_daemon_haze.{json,vsh,fsh}`, format `POSITION_TEX_COLOR`.
  - UV is the position within the card: x in [-1, 1] across, y in [0, 1] from foot to crown.
  - Vertex colour red carries the card flag: 255 for the front card, 0 for the back.
  - Uniforms:
    - `HemoTime`
    - `HazeSeed`
    - `HazeState` (vec4: emergence, strength, swallow flash, eye visibility)
    - `HazeTint` (vec4: swallow tint rgb, eye flare)
    - `HazeLean` (vec2)
  - The shader must avoid GLSL reserved words and built-in names (`ShaderReservedWordTest`; see the shader GPU memory).
- **Registration:** a `ShaderInit.CARDINAL_DAEMON_HAZE` holder and its registration.
- **Render type:** `HemoRenderTypes.cardinalDaemonHaze(time, seed, state, tint, lean)`. It follows the `cardinalRiteSkyWound` pattern: a cached type per seed, uniforms set in the texturing shard, translucent, no culling, colour-only writes. Each daemon is drawn and ended in its own batch.
- **Maths:** `client/render/world/rite/DaemonHaze.java`, pure and unit-testable:
  - card corners from the camera position, daemon position, scale and front/back flag;
  - eye visibility from the daemon's yaw and the direction to the camera;
  - swallow flash and tint from the age of the newest `OFFERING_DRAWN` arrival;
  - emergence and strength mapped from the renderer's existing emergence, firmness and cancellation inputs;
  - lean from motion;
  - a sinking offering's position and scale against age.
- **Renderer:** `HumanitySpriteRenderer` is rewritten. It keeps its culling box and its rite lookup, emergence, cancellation and firmness inputs. It draws the back card, then the front card, then any offerings still sinking.

### Removed

- `client/render/world/rite/CardinalDaemonGeometry.java` and `CardinalDaemonGeometryTest`. Nothing outside the renderer uses them.
- The renderer's use of `AnimusMortemRenderTypes` and the `RITE_BOUNDARY_GLOW` nodule glow.
- In `CardinalRiteVisualsSourceTest`, the assertions that required `CardinalDaemonGeometry.silhouette/blood` and the MORTEM-before-ANIMUS order are replaced with assertions that the renderer draws the haze. The entity assertions stay.

### Untouched

- `HumanitySpriteEntity`, and all server code that moves, scales, binds or discards the daemon.
- The emergence particles.
- `CardinalRiteOfferingRenderer` (the vessels from braziers into the daemon).
- `CardinalRiteCulminationRenderer`.
- The staff tendrils.
- The unused `DaemonDiffuseGlowParticle` (its resource test requires it to exist).

## Testing

- **JVM:** `DaemonHaze` maths:
  - card corners face the camera;
  - eyes visible facing the camera and hidden from behind;
  - swallow flash peaks after arrival and decays to 0 by about 20 ticks;
  - emergence masks the eyes until it ends;
  - a sinking offering ends at the chest with scale 0.
- **Source test:** the shader is registered, has its JSON, and the renderer draws `cardinalDaemonHaze`.
- **Shader checks:** `ShaderReservedWordTest` covers the new shader automatically, and glslangValidator runs if available.
- **Native check:** launch `runSuccessionReviewClient`, confirm the log has no `Couldn't compile`, then stop only that client's java processes.

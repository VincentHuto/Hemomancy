# Cardinal Daemon Haze Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redraw the Cardinal Rite daemon as a red, hooded smoky haze with glowing red eyes, drawn by one shader on two camera-facing cards. This replaces the ribbon "veiny monster".

**Architecture:**
- A pure maths class, `DaemonHaze`, computes the card corners, eye visibility, the swallow flash, emergence, strength, lean and the sinking offering's path.
- A new core shader, `cardinal_daemon_haze`, draws the hood, smoke, ember glow, eyes, blink and swallow tint from a few uniforms.
- `HumanitySpriteRenderer` is rewritten to feed both from the entity and its rite. It draws a back card and a front card in one per-daemon batch, then the offerings sinking into the smoke.
- The old `CardinalDaemonGeometry` and its test are deleted. Server code is untouched.

**Tech Stack:** NeoForge 1.21.1 (21.1.251), Java 21, GLSL 150 core shaders, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-13-cardinal-daemon-haze-design.md`

## Global Constraints

- Only the look changes: `HumanitySpriteEntity` and all server code stay as they are.
- Colours are deep near-black red at the core, crimson toward the edges and a faint ember glow. No copper, brass, bronze or silver tones.
- The eyes are red-orange. They show when the daemon faces the camera and are hidden from behind.
- Shader identifiers must avoid GLSL reserved words and built-in names (e.g. `packed`, `smooth`, `half`, `distance`, `offset`). Never call `smoothstep` with edge0 >= edge1, which is undefined on NVIDIA.
- A new core shader must be proven to compile on the user's NVIDIA GPU (`runSuccessionReviewClient`), not just with glslangValidator.
- Leave all work uncommitted in the working tree (user preference). Each "Commit" step is a ledger line instead.
- The daemon stays vertically centred on the entity position: height fraction `h` sits at `CardinalRiteVisualRules.bodyY(h, scale)`, i.e. `(h - 0.5) * 4 * scale`.

## Review Focus

1. **A camera directly above or below the daemon.** The horizontal direction to the camera is zero, so the cards must not collapse to NaN; fall back to a fixed axis. *(Task 1 test.)*
2. **Two daemons in view at once** (two rites side by side). Each needs its own render type and batch so their uniforms don't leak into each other. *(Task 3 source test: the type is keyed per seed and the batch ends per daemon.)*
3. **A daemon at its minimum scale during the merge, or with zero emergence.** No NaN or negative sizes, and the eyes stay at or below visibility 1. *(Task 1 tests.)*
4. **Offerings whose beat has no arrival point.** The sink path falls back to the daemon's centre. *(Task 1 test.)*
5. **A daemon with no live rite** (a ghost or orphan for a tick). It draws with full strength, no swallow and no flare, without throwing. *(Task 3: the renderer guards `rite == null`, covered by a source assertion.)*

---

## File Structure

| File | Responsibility |
|---|---|
| `client/render/world/rite/DaemonHaze.java` (new) | Pure maths: card corners, eyes, swallow, emergence, strength, lean, sink path |
| `assets/hemomancy/shaders/core/world/cardinal_daemon_haze.{json,vsh,fsh}` (new) | The haze, hood, eyes and swallow tint |
| `common/init/ShaderInit.java` | `CARDINAL_DAEMON_HAZE` holder and registration |
| `client/render/HemoRenderTypes.java` | `cardinalDaemonHaze(...)` cached per seed |
| `client/render/entity/misc/HumanitySpriteRenderer.java` | Rewritten: two cards, sinking offerings |
| `client/render/world/rite/CardinalDaemonGeometry.java` and its test | Deleted |
| `src/test/.../CardinalRiteVisualsSourceTest.java` | Asserts the haze instead of the old geometry |
| `docs/HEMOMANCY_REFERENCE.md:3491` | Daemon description |

Java paths are under `src/main/java/com/vincenthuto/hemomancy/` and tests under `src/test/java/com/vincenthuto/hemomancy/`.

---

### Task 1: DaemonHaze maths

**Files:**
- Create: `client/render/world/rite/DaemonHaze.java`
- Test: `client/render/world/rite/DaemonHazeTest.java`

**Interfaces:**
- Produces:
  - Constants: `static final double WIDTH_RATIO = 0.5` (card width / height), `static final double BACK_SCALE = 1.15`, `static final double BACK_OFFSET = 0.35`, `static final double CHEST = 0.62`, `static final float SWALLOW_TICKS = 20.0F`
  - `static double[] card(double toCameraX, double toCameraZ, double scale, boolean back)`: 12 doubles, corners bottom-left, bottom-right, top-right, top-left, relative to the entity position
  - `static double[] right(double toCameraX, double toCameraZ)`: the card's unit +u axis {x, z}
  - `static float eyes(double yawRadians, double toCameraX, double toCameraZ)`
  - `static float eyesOpen(float emergence)`
  - `static float swallow(float sinceArrival)`
  - `static float strength(double firmness, float cancellation)`
  - `static float[] lean(double motionX, double motionY, double motionZ, double toCameraX, double toCameraZ)`: {across, up}
  - `static double sinkProgress(float sinceArrival)`
  - `static double[] sink(double fromX, double fromY, double fromZ, double scale, float sinceArrival)`: {x, y, z, itemScale}

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.client.render.world.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonHazeTest {
	@Test
	void theCardStandsUprightFacingTheCamera() {
		double[] c = DaemonHaze.card(0, 10, 1.0, false);
		// bottom-left, bottom-right, top-right, top-left; camera on +z, so the card spans x
		assertEquals(-2.0, c[1], 1e-9, "foot at bodyY(0)");
		assertEquals(2.0, c[7], 1e-9, "crown at bodyY(1)");
		assertEquals(2.0, Math.abs(c[3] - c[0]), 1e-9, "width is half the 4-block height");
		assertEquals(0.0, c[2], 1e-9);
		assertEquals(0.0, c[5], 1e-9, "the front card lies in the plane through the centre");
		double[] right = DaemonHaze.right(0, 10);
		assertEquals(0.0, right[0] * 0 + right[1] * 1, 1e-9, "the card's across axis is perpendicular to the view");
	}

	@Test
	void theBackCardIsLargerAndBehind() {
		double[] front = DaemonHaze.card(10, 0, 2.0, false);
		double[] back = DaemonHaze.card(10, 0, 2.0, true);
		assertTrue(back[7] - back[1] > front[7] - front[1], "taller");
		assertTrue(back[0] < 0 && back[3] < 0, "pushed away from a camera on +x");
		assertEquals(-DaemonHaze.BACK_OFFSET * 2.0, back[0], 1e-9);
	}

	@Test
	void aCameraStraightAboveDoesNotCollapseTheCard() {
		double[] c = DaemonHaze.card(0, 0, 1.0, false);
		for (double value : c) assertFalse(Double.isNaN(value));
		assertEquals(2.0, Math.hypot(c[3] - c[0], c[5] - c[2]), 1e-9);
		assertFalse(Float.isNaN(DaemonHaze.eyes(0.3, 0, 0)));
	}

	@Test
	void eyesShowFacingTheCameraAndHideBehind() {
		// Minecraft yaw 0 faces +z
		assertEquals(1.0F, DaemonHaze.eyes(0.0, 0, 10), 1e-6F);
		assertEquals(0.0F, DaemonHaze.eyes(0.0, 0, -10), 1e-6F);
		assertEquals(0.0F, DaemonHaze.eyes(0.0, 10, 0), 1e-6F, "side-on, the hood hides them");
		float turning = DaemonHaze.eyes(Math.toRadians(40), 0, 10);
		assertTrue(turning > 0.0F && turning < 1.0F);
	}

	@Test
	void eyesOpenOnlyAsEmergenceEnds() {
		assertEquals(0.0F, DaemonHaze.eyesOpen(0.0F), 1e-6F);
		assertEquals(0.0F, DaemonHaze.eyesOpen(0.8F), 1e-6F);
		assertEquals(1.0F, DaemonHaze.eyesOpen(1.0F), 1e-6F);
	}

	@Test
	void theSwallowFlashRisesThenDiesAway() {
		assertEquals(0.0F, DaemonHaze.swallow(-1.0F), 1e-6F);
		float peak = DaemonHaze.swallow(3.0F);
		assertTrue(peak > 0.7F, "bright soon after arrival: " + peak);
		assertTrue(DaemonHaze.swallow(12.0F) < peak);
		assertEquals(0.0F, DaemonHaze.swallow(DaemonHaze.SWALLOW_TICKS), 1e-6F);
	}

	@Test
	void cancellationThinsTheHaze() {
		assertEquals(1.0F, DaemonHaze.strength(1.0, 0.0F), 1e-6F);
		assertEquals(0.25F, DaemonHaze.strength(1.0, 1.0F), 1e-6F);
		assertTrue(DaemonHaze.strength(0.55, 0.0F) < 1.0F, "loose before the seal");
	}

	@Test
	void smokeLeansAgainstItsMotion() {
		float[] lean = DaemonHaze.lean(0.2, 0.0, 0.0, 0, 10);
		assertNotEquals(0.0F, lean[0]);
		assertTrue(Math.abs(DaemonHaze.lean(5.0, 0, 0, 0, 10)[0]) <= 0.6F, "capped");
		assertEquals(0.0F, DaemonHaze.lean(0, 0, 0, 0, 10)[0], 1e-6F);
	}

	@Test
	void anOfferingSinksToTheChestAndVanishes() {
		double[] start = DaemonHaze.sink(1.0, 0.5, 0.0, 1.0, 0.0F);
		assertEquals(1.0, start[0], 1e-9);
		assertTrue(start[3] > 0.0);
		double[] end = DaemonHaze.sink(1.0, 0.5, 0.0, 1.0, DaemonHaze.SWALLOW_TICKS);
		assertEquals(0.0, end[0], 1e-9);
		assertEquals(CardinalRiteVisualRules.bodyY(DaemonHaze.CHEST, 1.0), end[1], 1e-9);
		assertEquals(0.0, end[3], 1e-9, "dissolved");
		double[] tiny = DaemonHaze.sink(0, 0, 0, 0.1, 5.0F);
		assertTrue(tiny[3] > 0.0, "a merging daemon still swallows a visible item");
	}
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeTest"`
Expected: FAIL (compilation: `DaemonHaze` does not exist).

- [ ] **Step 3: Implement**

```java
package com.vincenthuto.hemomancy.client.render.world.rite;

/**
 * The maths behind the daemon's haze: two cards that turn to face the camera around the vertical axis, the eyes
 * that show only while it faces the viewer, and the moments the shader is told about (emergence, thinning, the
 * flash of a swallowed offering, the lean of moving smoke). No rendering here, so all of it is unit-tested.
 */
public final class DaemonHaze {
	/** Card width over height. */
	public static final double WIDTH_RATIO = 0.5D;
	/** The back card's size relative to the front. */
	public static final double BACK_SCALE = 1.15D;
	/** How far the back card sits behind the front, per unit of scale. */
	public static final double BACK_OFFSET = 0.35D;
	/** Height fraction where swallowed offerings come to rest. */
	public static final double CHEST = 0.62D;
	/** How long a swallowed offering takes to dissolve, and the haze to settle after it. */
	public static final float SWALLOW_TICKS = 20.0F;
	private static final float LEAN_GAIN = 2.5F;
	private static final float LEAN_MAX = 0.6F;

	private DaemonHaze() {
	}

	/** The card's unit across axis {x, z}: perpendicular to the horizontal view, or +x straight above or below. */
	public static double[] right(double toCameraX, double toCameraZ) {
		double length = Math.hypot(toCameraX, toCameraZ);
		if (length < 1.0E-6D) return new double[] {1.0D, 0.0D};
		double dirX = toCameraX / length;
		double dirZ = toCameraZ / length;
		return new double[] {dirZ, -dirX};
	}

	/** Corners bottom-left, bottom-right, top-right, top-left as {x, y, z} triples relative to the entity. */
	public static double[] card(double toCameraX, double toCameraZ, double scale, boolean back) {
		double size = back ? BACK_SCALE : 1.0D;
		double[] right = right(toCameraX, toCameraZ);
		double length = Math.hypot(toCameraX, toCameraZ);
		double awayX = 0.0D;
		double awayZ = 0.0D;
		if (back && length >= 1.0E-6D) {
			awayX = -toCameraX / length * BACK_OFFSET * scale;
			awayZ = -toCameraZ / length * BACK_OFFSET * scale;
		}
		double halfWidth = CardinalRiteVisualRules.DAEMON_HEIGHT * scale * WIDTH_RATIO * 0.5D * size;
		double foot = CardinalRiteVisualRules.bodyY(0.0D, scale) * size;
		double crown = CardinalRiteVisualRules.bodyY(1.0D, scale) * size;
		double rx = right[0] * halfWidth;
		double rz = right[1] * halfWidth;
		return new double[] {
				awayX - rx, foot, awayZ - rz,
				awayX + rx, foot, awayZ + rz,
				awayX + rx, crown, awayZ + rz,
				awayX - rx, crown, awayZ - rz};
	}

	/** Eye visibility from the daemon's facing: full facing the camera, gone side-on or from behind. */
	public static float eyes(double yawRadians, double toCameraX, double toCameraZ) {
		double length = Math.hypot(toCameraX, toCameraZ);
		if (length < 1.0E-6D) return 0.0F;
		double facing = (-Math.sin(yawRadians) * toCameraX + Math.cos(yawRadians) * toCameraZ) / length;
		return (float) CardinalRiteVisualRules.smooth(facing);
	}

	/** The eyes open only at the very end of emergence. */
	public static float eyesOpen(float emergence) {
		return (float) CardinalRiteVisualRules.smooth((emergence - 0.8D) / 0.2D);
	}

	/** The haze's flash after an offering arrives: quick to rise, gone by {@link #SWALLOW_TICKS}. */
	public static float swallow(float sinceArrival) {
		if (sinceArrival < 0.0F) return 0.0F;
		return (float) (CardinalRiteVisualRules.smooth(sinceArrival / 3.0D)
				* (1.0D - CardinalRiteVisualRules.smooth(sinceArrival / SWALLOW_TICKS)));
	}

	/** Density of the haze: loose before the seal, thinned to a quarter as it is drawn back into the staff. */
	public static float strength(double firmness, float cancellation) {
		return (float) (Math.max(0.0D, Math.min(1.0D, firmness)) * (1.0D - 0.75D * Math.max(0.0F, Math.min(1.0F, cancellation))));
	}

	/** How far the smoke leans, across the card and upward, against the daemon's motion per tick. */
	public static float[] lean(double motionX, double motionY, double motionZ, double toCameraX, double toCameraZ) {
		double[] right = right(toCameraX, toCameraZ);
		float across = (float) ((motionX * right[0] + motionZ * right[1]) * LEAN_GAIN);
		float up = (float) (motionY * LEAN_GAIN);
		return new float[] {clampLean(across), clampLean(up)};
	}

	public static double sinkProgress(float sinceArrival) {
		return CardinalRiteVisualRules.smooth(sinceArrival / SWALLOW_TICKS);
	}

	/** A swallowed offering's place {x, y, z} relative to the entity and its item scale, drifting to the chest. */
	public static double[] sink(double fromX, double fromY, double fromZ, double scale, float sinceArrival) {
		double t = sinkProgress(Math.max(0.0F, sinceArrival));
		double chest = CardinalRiteVisualRules.bodyY(CHEST, scale);
		double size = 0.3D * Math.max(0.35D, scale) * (1.0D - t);
		return new double[] {fromX * (1.0D - t), fromY + (chest - fromY) * t, fromZ * (1.0D - t), size};
	}

	private static float clampLean(float value) {
		return Math.max(-LEAN_MAX, Math.min(LEAN_MAX, value));
	}
}
```

- [ ] **Step 4: Run it to verify it passes**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeTest"`
Expected: PASS (9 tests). `CardinalRiteVisualRules.smooth` clamps its argument to 0..1, which `eyes`, `eyesOpen` and `swallow` rely on.

- [ ] **Step 5: Ledger**

---

### Task 2: The haze shader and its render type

**Files:**
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_daemon_haze.json`, `.vsh`, `.fsh`
- Modify: `common/init/ShaderInit.java` (holder after `CARDINAL_RITE_SKY_WOUND`; registration after its registration)
- Modify: `client/render/HemoRenderTypes.java` (after `createCardinalSkyWound`)
- Test: `client/render/world/rite/DaemonHazeSourceTest.java`

**Interfaces:**
- Produces:
  - `ShaderInit.CARDINAL_DAEMON_HAZE`
  - `HemoRenderTypes.cardinalDaemonHaze(float gameTime, float seed, float emergence, float strength, float swallow, float eyes, int tintRgb, float flare, float leanAcross, float leanUp)`: returns a `RenderType` cached per seed, POSITION_TEX_COLOR quads. Vertex red is 255 on the front card and 0 on the back; UV x runs -1..1 across and y 0..1 from foot to crown.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.client.render.world.rite;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonHazeSourceTest {
	private static final String ROOT = "src/main/java/com/vincenthuto/hemomancy/";
	private static final String SHADERS = "src/main/resources/assets/hemomancy/shaders/core/world/";

	static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	@Test
	void theHazeShaderIsRegisteredWithItsUniforms() throws IOException {
		String init = read(ROOT + "common/init/ShaderInit.java");
		String json = read(SHADERS + "cardinal_daemon_haze.json");
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		assertTrue(init.contains("registerShader(event, CARDINAL_DAEMON_HAZE.createInstance(provider));"));
		assertTrue(init.contains("\"HemoTime\", \"HazeSeed\", \"HazeState\", \"HazeTint\", \"HazeLean\""));
		for (String uniform : new String[] {"HazeSeed", "HazeState", "HazeTint", "HazeLean"}) {
			assertTrue(json.contains("\"name\": \"" + uniform + "\""), uniform + " declared to Minecraft");
			assertTrue(fragment.contains("uniform") && fragment.contains(uniform), uniform + " read by the shader");
		}
		assertTrue(fragment.contains("discard;"), "empty card corners draw nothing");
	}

	@Test
	void eachDaemonHasItsOwnCachedType() throws IOException {
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		assertTrue(types.contains("public static RenderType cardinalDaemonHaze("));
		assertTrue(types.contains("CARDINAL_DAEMON_HAZE_TYPES.get(seed)"), "one type per daemon seed");
		int start = types.indexOf("private static RenderType createCardinalDaemonHaze(");
		int end = types.indexOf(".createCompositeState(false));", start);
		assertTrue(start > 0 && end > start);
		String created = types.substring(start, end);
		assertTrue(created.contains("RenderType.COLOR_WRITE"), "smoke writes no depth");
		assertTrue(created.contains("RenderType.TRANSLUCENT_TRANSPARENCY"));
	}

	@Test
	void theShaderAvoidsUndefinedSmoothsteps() throws IOException {
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		java.util.regex.Matcher calls = java.util.regex.Pattern
				.compile("smoothstep\\(\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*,").matcher(fragment);
		while (calls.find()) {
			assertTrue(Double.parseDouble(calls.group(1)) < Double.parseDouble(calls.group(2)),
					"smoothstep edges must rise: " + calls.group());
		}
	}
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeSourceTest"`
Expected: FAIL (`NoSuchFileException` for the shader JSON and missing source text).

- [ ] **Step 3: Write the shader files**

`cardinal_daemon_haze.json`:

```json
{
  "blend": {
    "func": "add",
    "srcrgb": "srcalpha",
    "dstrgb": "1-srcalpha"
  },
  "vertex": "hemomancy:world/cardinal_daemon_haze",
  "fragment": "hemomancy:world/cardinal_daemon_haze",
  "attributes": [
    "Position",
    "Color",
    "UV0"
  ],
  "samplers": [],
  "uniforms": [
    { "name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
    { "name": "ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
    { "name": "ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
    { "name": "HemoTime", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "HazeSeed", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "HazeState", "type": "float", "count": 4, "values": [ 1.0, 1.0, 0.0, 1.0 ] },
    { "name": "HazeTint", "type": "float", "count": 4, "values": [ 0.7, 0.1, 0.1, 0.0 ] },
    { "name": "HazeLean", "type": "float", "count": 2, "values": [ 0.0, 0.0 ] }
  ]
}
```

`cardinal_daemon_haze.vsh`:

```glsl
#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
}
```

`cardinal_daemon_haze.fsh`:

```glsl
#version 150

// The Cardinal Rite daemon: a hooded figure of red smoke with two glowing eyes in the hood's shadow.
// UV x runs -1..1 across the card, y 0..1 from foot to crown. Vertex red marks the front card (eyes, full density).

uniform float HemoTime;
uniform float HazeSeed;
uniform vec4 HazeState; // emergence, strength, swallow flash, eye visibility
uniform vec4 HazeTint;  // swallowed offering tint rgb, eye flare
uniform vec2 HazeLean;  // smoke lean across and up
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash12(vec2 p) {
    vec3 q = fract(vec3(p.xyx) * 0.1031);
    q += dot(q, q.yzx + 33.33);
    return fract((q.x + q.y) * q.z);
}

float noise2(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash12(cell);
    float b = hash12(cell + vec2(1.0, 0.0));
    float c = hash12(cell + vec2(0.0, 1.0));
    float d = hash12(cell + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float total = 0.0;
    float amp = 0.5;
    for (int octave = 0; octave < 5; octave++) {
        total += amp * noise2(p);
        p = p * 2.03 + vec2(17.1, 9.2);
        amp *= 0.5;
    }
    return total;
}

// Negative inside the hooded figure: a rounded hood over shoulders and a robe narrowing to the ground.
float hoodField(vec2 q) {
    vec2 hoodAxis = vec2(q.x / 0.40, (q.y - 0.83) / 0.15);
    float hood = (length(hoodAxis) - 1.0) * 0.30;
    float rise = clamp(q.y / 0.66, 0.0, 1.0);
    float robeWidth = mix(0.16, 0.80, rise * rise * (3.0 - 2.0 * rise));
    float shoulder = clamp((q.y - 0.66) / 0.09, 0.0, 1.0);
    robeWidth *= sqrt(max(0.0, 1.0 - shoulder * shoulder));
    float robe = q.y > 0.75 ? 1.0 : abs(q.x) - robeWidth;
    return min(hood, robe);
}

void main() {
    bool frontCard = vertexColor.r > 0.5;
    vec2 q = texCoord0;
    float emergence = HazeState.x;
    float strength = HazeState.y;
    float swallow = HazeState.z;

    // Moving smoke lags behind: the base trails furthest, and rising motion stretches it down.
    q.x -= HazeLean.x * (1.0 - q.y) * 0.6;
    q.y += HazeLean.y * (1.0 - q.y) * 0.25;

    float drift = HemoTime * (frontCard ? 0.035 : 0.022);
    vec2 seedShift = vec2(HazeSeed * 7.31, HazeSeed * 3.17) + (frontCard ? vec2(0.0) : vec2(41.7, 13.9));
    vec2 p = vec2(q.x * 1.6, q.y * 3.2 - drift * 2.0) + seedShift;
    vec2 warp = vec2(fbm(p + vec2(0.0, drift)), fbm(p + vec2(5.2, 1.3 - drift)));
    float smoke = fbm(p + warp * 1.8);

    float field = hoodField(q + (warp - 0.5) * vec2(0.22, 0.10) * (1.15 - q.y));
    float lowerBand = 1.0 - smoothstep(0.0, 0.38, q.y);
    float body = 1.0 - smoothstep(-0.10, 0.12 + 0.18 * lowerBand, field + (smoke - 0.5) * (0.35 + 0.5 * lowerBand));
    float density = body * mix(0.55, 1.0, smoke);
    // The lower third breaks into rising wisps.
    density *= 1.0 - lowerBand * smoothstep(0.35, 0.75, 1.0 - smoke) * 0.9;
    // Never let the card's own edge show.
    density *= 1.0 - smoothstep(0.82, 1.0, abs(q.x));
    density *= smoothstep(0.0, 0.05, q.y) * (1.0 - smoothstep(0.94, 1.0, q.y));
    // Emergence lifts the smoke out of the staff from the bottom up.
    float top = emergence * 1.15;
    density *= 1.0 - smoothstep(top - 0.15, top, q.y);

    density *= strength * (1.0 + swallow * 0.25);
    if (!frontCard) density *= 0.7;

    float core = smoothstep(0.35, 0.95, body * smoke);
    vec3 deepRed = vec3(0.10, 0.008, 0.016);
    vec3 crimson = vec3(0.62, 0.05, 0.08);
    vec3 ember = vec3(1.0, 0.25, 0.12);
    vec3 colour = mix(crimson, deepRed, core);
    float breath = 0.5 + 0.5 * sin(HemoTime * 0.31 + HazeSeed);
    colour += ember * pow(smoke, 3.0) * (1.0 - core) * (0.25 + 0.15 * breath);
    colour = mix(colour, HazeTint.rgb, swallow * 0.45);
    colour *= 1.0 + swallow * 0.8;
    if (!frontCard) colour *= 0.55;
    float alpha = clamp(density * 0.9, 0.0, 0.92);

    if (frontCard) {
        // The hood's shadow, then the eyes inside it.
        vec2 faceAxis = vec2(q.x / 0.30, (q.y - 0.81) / 0.07);
        float hoodShadow = 1.0 - smoothstep(0.6, 1.2, length(faceAxis));
        colour *= 1.0 - 0.6 * hoodShadow;

        float blinkCycle = fract(HemoTime / 173.0 + HazeSeed * 0.13);
        float lid = smoothstep(0.0, 0.008, abs(blinkCycle - 0.5) - 0.004);
        float flicker = mix(1.0, step(0.35, noise2(vec2(HemoTime * 0.9, HazeSeed))), 1.0 - strength);
        float open = HazeState.w * lid * flicker;
        float flare = HazeTint.a;

        float eyeX = abs(q.x) - 0.11;
        vec2 eyeAxis = vec2(eyeX, q.y - 0.815 - eyeX * 0.25);
        float eyeDist = length(eyeAxis / vec2(0.055, 0.020 * lid + 0.0015));
        float eyeCore = 1.0 - smoothstep(0.55, 1.0, eyeDist);
        float halo = 1.0 / (1.0 + eyeDist * eyeDist * 1.5);
        vec3 eyeColour = mix(vec3(1.0, 0.18, 0.08), vec3(1.0, 0.62, 0.35), eyeCore) * (1.0 + flare * 0.5);
        float glow = clamp((eyeCore + halo * (0.35 + 0.4 * flare)) * open, 0.0, 1.0);
        colour = mix(colour, eyeColour, glow);
        alpha = max(alpha, clamp((eyeCore + halo * 0.5) * open, 0.0, 1.0));
    }

    fragColor = vec4(colour, alpha) * ColorModulator;
    if (fragColor.a <= 0.004) {
        discard;
    }
}
```

- [ ] **Step 4: Register the shader and its render type**

In `ShaderInit.java`, after the `CARDINAL_RITE_SKY_WOUND` holder:

```java
	public static ShaderHolder CARDINAL_DAEMON_HAZE = new ShaderHolder(Hemomancy.rloc("world/cardinal_daemon_haze"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HazeSeed", "HazeState", "HazeTint", "HazeLean");
```

and after `registerShader(event, CARDINAL_RITE_SKY_WOUND.createInstance(provider));`:

```java
        registerShader(event, CARDINAL_DAEMON_HAZE.createInstance(provider));
```

In `HemoRenderTypes.java`, after `createCardinalSkyWound`:

```java
	/** One haze type per daemon seed; its uniforms change in place, so the renderer ends its batch per daemon. */
	private static final Map<Float, CardinalDaemonHazeType> CARDINAL_DAEMON_HAZE_TYPES = riteTypeCache();

	private static final class CardinalDaemonHazeType {
		private final float seed;
		private float gameTime;
		private float emergence;
		private float strength;
		private float swallow;
		private float eyes;
		private int tint;
		private float flare;
		private float leanAcross;
		private float leanUp;
		private final RenderType type;

		private CardinalDaemonHazeType(float seed) {
			this.seed = seed;
			this.type = createCardinalDaemonHaze(this);
		}
	}

	public static RenderType cardinalDaemonHaze(float gameTime, float seed, float emergence, float strength,
			float swallow, float eyes, int tintRgb, float flare, float leanAcross, float leanUp) {
		CardinalDaemonHazeType haze = CARDINAL_DAEMON_HAZE_TYPES.get(seed);
		if (haze == null) {
			haze = new CardinalDaemonHazeType(seed);
			CARDINAL_DAEMON_HAZE_TYPES.put(seed, haze);
		}
		haze.gameTime = gameTime;
		haze.emergence = emergence;
		haze.strength = strength;
		haze.swallow = swallow;
		haze.eyes = eyes;
		haze.tint = tintRgb;
		haze.flare = flare;
		haze.leanAcross = leanAcross;
		haze.leanUp = leanUp;
		return haze.type;
	}

	private static RenderType createCardinalDaemonHaze(CardinalDaemonHazeType haze) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_daemon_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_DAEMON_HAZE.getInstance().get();
					setUniform(shader, "HemoTime", haze.gameTime);
					setUniform(shader, "HazeSeed", haze.seed);
					setUniform(shader, "HazeState", haze.emergence, haze.strength, haze.swallow, haze.eyes);
					setUniform(shader, "HazeTint", ((haze.tint >> 16) & 0xFF) / 255.0F,
							((haze.tint >> 8) & 0xFF) / 255.0F, (haze.tint & 0xFF) / 255.0F, haze.flare);
					setUniform(shader, "HazeLean", haze.leanAcross, haze.leanUp);
					setUniform(shader, "ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_DAEMON_HAZE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("cardinal_daemon_haze",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_DAEMON_HAZE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}
```

The one-, two-, three- and four-float `setUniform` overloads all exist at `HemoRenderTypes.java:1190-1230`.

- [ ] **Step 5: Run the tests and validate the shader offline**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeSourceTest" --tests "com.vincenthuto.hemomancy.client.render.ShaderReservedWordTest"`
Expected: PASS.

Run: `glslangValidator -S frag src/main/resources/assets/hemomancy/shaders/core/world/cardinal_daemon_haze.fsh` and `glslangValidator -S vert src/main/resources/assets/hemomancy/shaders/core/world/cardinal_daemon_haze.vsh`
Expected: no errors (the GPU check comes in Task 4).

- [ ] **Step 6: Ledger**

---

### Task 3: The renderer draws the haze

**Files:**
- Modify (rewrite): `client/render/entity/misc/HumanitySpriteRenderer.java`
- Delete: `client/render/world/rite/CardinalDaemonGeometry.java`, `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/CardinalDaemonGeometryTest.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/CardinalRiteVisualsSourceTest.java` (`daemonIsGeometryNotServerParticles`)
- Test: the `DaemonHazeSourceTest` additions below

**Interfaces:**
- Consumes:
  - `DaemonHaze.*` (Task 1)
  - `HemoRenderTypes.cardinalDaemonHaze(...)` (Task 2)
  - `RiteWorldState.at(BlockPos)`, then `World.level(float)`
  - `CardinalRiteOfferingMemory.absorbed(BlockPos)` and `colour(ItemStack)`
  - `CardinalRiteVisualRules.firmness`, `OFFERING_ARRIVE_TICKS`

- [ ] **Step 1: Write the failing tests**

Append to `DaemonHazeSourceTest`:

```java
	@Test
	void theRendererDrawsTwoHazeCardsAndEndsItsBatch() throws IOException {
		String renderer = read(ROOT + "client/render/entity/misc/HumanitySpriteRenderer.java");
		assertTrue(renderer.contains("HemoRenderTypes.cardinalDaemonHaze("));
		assertTrue(renderer.contains("DaemonHaze.card(toCameraX, toCameraZ, scale, true)"), "back card");
		assertTrue(renderer.contains("DaemonHaze.card(toCameraX, toCameraZ, scale, false)"), "front card");
		assertTrue(renderer.indexOf("scale, true)") < renderer.indexOf("scale, false)"), "back before front");
		assertTrue(renderer.contains("source.endBatch(haze)"), "uniforms never leak between daemons");
		assertTrue(renderer.contains("if (rite != null)"), "an orphaned daemon still draws");
		assertFalse(renderer.contains("CardinalDaemonGeometry"));
		assertFalse(renderer.contains("AnimusMortemRenderTypes"));
		assertFalse(Files.exists(Path.of(ROOT + "client/render/world/rite/CardinalDaemonGeometry.java")),
				"the veiny geometry is gone");
	}
```

In `CardinalRiteVisualsSourceTest.daemonIsGeometryNotServerParticles`, replace these lines:

```java
		assertTrue(renderer.contains("CardinalDaemonGeometry.silhouette("));
		assertTrue(renderer.contains("CardinalDaemonGeometry.blood("));
		assertTrue(renderer.indexOf("AnimusMortemRenderTypes.MORTEM") < renderer.indexOf("AnimusMortemRenderTypes.ANIMUS"),
				"the dark silhouette is drawn before the living blood");
```

with

```java
		assertTrue(renderer.contains("HemoRenderTypes.cardinalDaemonHaze("), "the daemon is a shader-drawn haze");
```

- [ ] **Step 2: Run them to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeSourceTest" --tests "com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualsSourceTest"`
Expected: FAIL (the renderer still uses `CardinalDaemonGeometry`).

- [ ] **Step 3: Rewrite the renderer and delete the old geometry**

Delete `CardinalDaemonGeometry.java` and `CardinalDaemonGeometryTest.java`. Replace `HumanitySpriteRenderer.java` with:

```java
package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingMemory;
import com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualRules;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHaze;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.UUID;

/**
 * Draws the Cardinal Rite daemon as a hooded figure of red smoke with glowing eyes: two camera-facing cards (a
 * larger, darker one behind for depth) painted by the {@code cardinal_daemon_haze} shader, then any offering it is
 * still swallowing. The maths lives in {@link DaemonHaze}; this class only reads the entity and its rite.
 */
public final class HumanitySpriteRenderer extends EntityRenderer<HumanitySpriteEntity> {
	private static final ResourceLocation BLANK = Hemomancy.rloc("textures/entity/blank.png");
	private static final int OFFERING_TINT = 0xB01420;

	public HumanitySpriteRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(HumanitySpriteEntity entity, Frustum frustum, double camX, double camY, double camZ) {
		if (!entity.shouldRender(camX, camY, camZ)) return false;
		// The back card and leaning wisps reach a little past the body's box.
		double reach = 1.0D + entity.getSpriteScale() * 1.5D;
		return frustum.isVisible(entity.getBoundingBoxForCulling().inflate(reach));
	}

	@Override
	public void render(HumanitySpriteEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || entity.isInvisible()) return;
		float time = (float) (entity.level().getGameTime() + partialTick);
		Vec3 position = entity.getPosition(partialTick);
		Vec3 camera = entityRenderDispatcher.camera.getPosition();
		double toCameraX = camera.x - position.x;
		double toCameraZ = camera.z - position.z;
		double scale = entity.getSpriteScale();
		float emergence = entity.emergenceProgress(partialTick);
		double yaw = Math.toRadians(Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()));

		ActiveRiteClientData.RiteEntry rite = riteFor(entity.getRiteOwner());
		double firmness = 1.0D;
		float cancellation = 0.0F;
		float flare = 0.0F;
		float swallow = 0.0F;
		int tint = OFFERING_TINT;
		List<CardinalRiteEventClientState.RiteEvent> absorbed = List.of();
		if (rite != null) {
			BlockPos center = rite.getCenter();
			cancellation = Mth.clamp(rite.cancellationRenderTicks(partialTick)
					/ CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS, 0.0F, 1.0F);
			firmness = CardinalRiteVisualRules.firmness(
					CardinalRiteEventClientState.latestAge(center, CardinalRiteEventKind.SEALED, partialTick),
					isPastSealing(rite.getPhase()));
			RiteWorldState.World world = RiteWorldState.at(center);
			if (world != null) flare = world.level(partialTick);
			absorbed = CardinalRiteOfferingMemory.absorbed(center);
			CardinalRiteEventClientState.RiteEvent newest = newestArrival(absorbed, partialTick);
			if (newest != null) {
				swallow = DaemonHaze.swallow(sinceArrival(newest, partialTick));
				tint = CardinalRiteOfferingMemory.colour(newest.item());
			}
		}
		float strength = DaemonHaze.strength(firmness, cancellation);
		float eyes = DaemonHaze.eyes(yaw, toCameraX, toCameraZ) * DaemonHaze.eyesOpen(emergence);
		float[] lean = DaemonHaze.lean(entity.getX() - entity.xo, entity.getY() - entity.yo,
				entity.getZ() - entity.zo, toCameraX, toCameraZ);
		float seed = Math.floorMod(entity.getId(), 113);

		RenderType haze = HemoRenderTypes.cardinalDaemonHaze(time, seed, emergence, strength, swallow, eyes, tint,
				flare, lean[0], lean[1]);
		VertexConsumer consumer = buffer.getBuffer(haze);
		Matrix4f matrix = poseStack.last().pose();
		emitCard(consumer, matrix, DaemonHaze.card(toCameraX, toCameraZ, scale, true), 0);
		emitCard(consumer, matrix, DaemonHaze.card(toCameraX, toCameraZ, scale, false), 255);
		if (buffer instanceof MultiBufferSource.BufferSource source) source.endBatch(haze);

		renderSwallowedOfferings(entity, absorbed, position, scale, partialTick, time, poseStack, buffer);
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	/** One card as a quad; UV x runs -1..1 across and y 0..1 from foot to crown, red marks the front card. */
	private static void emitCard(VertexConsumer consumer, Matrix4f matrix, double[] corners, int front) {
		float[][] uv = {{-1.0F, 0.0F}, {1.0F, 0.0F}, {1.0F, 1.0F}, {-1.0F, 1.0F}};
		for (int corner = 0; corner < 4; corner++) {
			consumer.addVertex(matrix, (float) corners[corner * 3], (float) corners[corner * 3 + 1],
					(float) corners[corner * 3 + 2]).setUv(uv[corner][0], uv[corner][1]).setColor(front, 255, 255, 255);
		}
	}

	/** Each offering still being swallowed drifts into the haze's chest, shrinking and dimming away. */
	private void renderSwallowedOfferings(HumanitySpriteEntity entity,
			List<CardinalRiteEventClientState.RiteEvent> absorbed, Vec3 position, double scale, float partialTick,
			float time, PoseStack poseStack, MultiBufferSource buffer) {
		Minecraft mc = Minecraft.getInstance();
		for (int slot = 0; slot < absorbed.size(); slot++) {
			CardinalRiteEventClientState.RiteEvent event = absorbed.get(slot);
			float since = sinceArrival(event, partialTick);
			if (since < 0.0F || since >= DaemonHaze.SWALLOW_TICKS || event.item().isEmpty()) continue;
			Vec3 from = event.points().size() > 1 ? event.points().get(1).subtract(position) : Vec3.ZERO;
			double[] sink = DaemonHaze.sink(from.x, from.y, from.z, scale, since);
			if (sink[3] <= 0.01D) continue;
			poseStack.pushPose();
			poseStack.translate(sink[0], sink[1], sink[2]);
			poseStack.mulPose(Axis.YP.rotationDegrees(time * 9.0F + slot * 47.0F));
			poseStack.scale((float) sink[3], (float) sink[3], (float) sink[3]);
			mc.getItemRenderer().renderStatic(event.item(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId() + slot);
			poseStack.popPose();
		}
	}

	private static CardinalRiteEventClientState.RiteEvent newestArrival(
			List<CardinalRiteEventClientState.RiteEvent> absorbed, float partialTick) {
		CardinalRiteEventClientState.RiteEvent newest = null;
		float youngest = Float.MAX_VALUE;
		for (CardinalRiteEventClientState.RiteEvent event : absorbed) {
			float since = sinceArrival(event, partialTick);
			if (since >= 0.0F && since < youngest) {
				youngest = since;
				newest = event;
			}
		}
		return newest;
	}

	private static float sinceArrival(CardinalRiteEventClientState.RiteEvent event, float partialTick) {
		return event.age(partialTick) - CardinalRiteVisualRules.OFFERING_ARRIVE_TICKS;
	}

	private static boolean isPastSealing(String phase) {
		if (phase == null) return false;
		return switch (phase) {
			case "ORDEAL", "PUPPET_TRIAL", "STILL_INTERVAL", "OFFERING_PROCESSION", "CULMINATION", "COMPLETE" -> true;
			default -> false;
		};
	}

	private static ActiveRiteClientData.RiteEntry riteFor(UUID owner) {
		if (owner == null) return null;
		for (ActiveRiteClientData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (owner.equals(rite.getOwner())) return rite;
		}
		CardinalRiteEventClientState.Ghost latest = null;
		for (CardinalRiteEventClientState.Ghost ghost : CardinalRiteEventClientState.ghosts()) {
			if (owner.equals(ghost.entry().getOwner())) latest = ghost;
		}
		return latest == null ? null : latest.entry();
	}

	@Override
	public ResourceLocation getTextureLocation(HumanitySpriteEntity entity) {
		return BLANK;
	}
}
```

If `CardinalRiteVisualRules.firmness` returns `double` (it does at the time of writing), the `double firmness` above matches it. If `RiteWorldState.at` is named differently, read `RiteWorldState` and use its lookup by Focus position, ledgering the ruling.

- [ ] **Step 4: Run the tests and compile**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.*"`
Expected: PASS. `CardinalDaemonGeometryTest` is gone; `CardinalRiteVisualRulesTest`, `CardinalRiteDaemonEmergenceTest` and `CardinalRiteCancellationGeometryTest` still pass.

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL. If anything else imported `CardinalDaemonGeometry`, the compile names it; replace that use with `DaemonHaze` and ledger it.

- [ ] **Step 5: Ledger**

---

### Task 4: Docs, full suite and the GPU check

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md` (the paragraph starting `**The daemon** (\`HumanitySpriteRenderer\`)`, line ~3491)

- [ ] **Step 1: Rewrite the daemon paragraph**

Replace the paragraph that begins `**The daemon** (\`HumanitySpriteRenderer\`) is layered geometry:` with:

```markdown
**The daemon** (`HumanitySpriteRenderer`) is a hooded figure of red smoke drawn by one shader (`cardinal_daemon_haze`) on two camera-facing cards, a larger darker one behind for depth (`DaemonHaze`). Domain-warped noise erodes its hood, shoulders and narrowing robe, and its lower third breaks into rising wisps. Two red-orange eyes glow in the hood's shadow while it faces the viewer, blink rarely, flicker while it is recalled, and flare as the rite's world stage rises. It rises out of the staff from the bottom up and opens its eyes last. It thins as Blood Absorption draws it back, leans its smoke against its motion, and flashes and swells, tinted, as it swallows each offering (the item drifts into its chest and dissolves).
```

- [ ] **Step 2: Full JVM suite**

Run: `./gradlew.bat test`
Expected: only the known pre-existing failures:
- BloodProfileMigrationTest, ClientModelTextureCoverageTest, EnglishLocalizationEncodingTest
- GameTestHarnessSourceContractTest ×2, HematicArmatureModelSourceTest ×2
- LegacyMainTestAdapterTest ×4, LegacyMainTestSuiteContractTest
- MnemonicReliquaryModelSourceTest, PhlegethonticBombardierResourceTest, VialCentrifugeModelSourceTest ×2

That is 16 in all, now minus nothing and plus nothing.

- [ ] **Step 3: GameTests**

Run: `./gradlew.bat runGameTestServer`
Expected: only the known failure `blockitemexceptionskeeptheirowners`, plus the known flakes `idlemorphlingsinkstofloor`, `mandatoryjigsawwingsassemble` and `astaffhooksaslackthreadatitsloomsreach`. Rerun once on any other failure before treating it as real.

- [ ] **Step 4: Prove the shader compiles on the user's GPU**

Run in the background: `./gradlew.bat runSuccessionReviewClient`. Wait for `Sound engine started` in `build/succession-client/logs/latest.log`, then for the log to go quiet.

Then run: `grep -n "Couldn't compile\|cardinal_daemon_haze" build/succession-client/logs/latest.log`
Expected: no `Couldn't compile` line. The vanilla "could not find sampler" warnings are benign.

Then stop only the `java.exe` processes whose command line contains `succession-client`.

- [ ] **Step 5: Ledger and finish** (no commit; user preference)

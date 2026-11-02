# Cardinal Daemon Volume Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the daemon's two camera-facing cards with a ray-marched 3D smoke volume. It keeps the same silhouette, outline, Monolith eyes and behaviour, and gains real substance from every side.

**Architecture:**
- Each daemon is queued (as today) and drawn at `AFTER_WEATHER` as a box whose vertices carry box-local coordinates in UV and red.
- The rewritten `cardinal_daemon_haze` shader marches rays front to back through a 3D signed-distance figure. The figure is the Humanity profile revolved, eroded by 3D fbm smoke, lit from above, with a soot shell.
- The eyes come from an analytic hit on the head sphere.

**Tech Stack:** NeoForge 1.21.1, Java 21, GLSL 150, JUnit 5, numpy (preview port).

**Spec:** `docs/superpowers/specs/2026-10-13-cardinal-daemon-volume-design.md`

## Global Constraints

- Only the look changes. `HumanitySpriteEntity` and server code are untouched.
- Shape, in height units:
  - head r 0.111 at y 0.85;
  - tip 0.03;
  - widest 0.151 at chest 0.58;
  - `sqrt(1 - a³)` shoulders to crown 0.79.
- Box half-width 0.24 × height; box half-height 0.55 × height; `MARCH_STEPS = 40`.
- No GLSL reserved words or built-in names (e.g. `noise2`, `sample`, `smooth`, `packed`). `smoothstep` edges always rise. Guard every division and `atan(0,0)`.
- No copper, brass, bronze or silver tones.
- Prove the shader compiles on the user's NVIDIA GPU: run `runSuccessionReviewClient` and confirm a fresh log with no `Couldn't compile`.
- Leave all work uncommitted. Ledger lines replace commits.

## Review Focus

1. **Camera inside the box** (the merge into the player). The daemon stays visible through the back faces and rays start at the eye. *(Task 2 source test: the `inside == gl_FrontFacing` discard.)*
2. **A ray parallel to a box axis.** No division by zero in the slab test. *(Task 2 source test: the guarded inverse.)*
3. **Face winding.** Every face is counter-clockwise from outside, otherwise `gl_FrontFacing` inverts and the daemon vanishes from outside. *(Task 1 test.)*
4. **Two daemons in one frame.** Each draws its own batch with its own uniforms (kept from the haze queue).
5. **Very large scale (up to 8)**, where the box fills the screen: early exit and no NaN. *(Covered by the preview port renders, not unit tests.)*

---

### Task 1: Box maths and vertices

**Files:**
- Modify (replace): `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/DaemonHaze.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/DaemonHazeQueue.java` (add `boxVertices`)
- Test (replace): `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/DaemonHazeTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/DaemonHazeQueueTest.java` (add a test)

**Interfaces:**
- Produces in `DaemonHaze`:
  - `box(double scale)` returning `double[]{halfWidth, halfHeight}` in blocks;
  - `trail(double mx, double my, double mz)` returning `float[3]`;
  - `eyesOpen`, `swallow`, `strength`, `sinkProgress`, `sink` (unchanged);
  - `BOX_HALF_WIDTH`, `BOX_HALF_HEIGHT`, `CHEST`, `SWALLOW_TICKS`.
- Produces `DaemonHazeQueue.boxVertices(double halfWidth, double halfHeight)` returning `float[144]`: 24 vertices × {x, y, z, u, v, red}.

- [ ] **Step 1: Write the failing tests**

Replace `DaemonHazeTest.java` with:

```java
package com.vincenthuto.hemomancy.client.render.world.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonHazeTest {
	@Test
	void theBoxHoldsTheFigureItsShellAndItsTrail() {
		double[] box = DaemonHaze.box(1.0);
		assertEquals(0.96, box[0], 1e-9, "half-width: 0.24 of the 4-block height");
		assertEquals(2.2, box[1], 1e-9, "half-height: 0.55 of the height");
		double[] tiny = DaemonHaze.box(0.1);
		assertTrue(tiny[0] > 0.0 && tiny[1] > 0.0, "a merging daemon keeps a real box");
	}

	@Test
	void theSmokeTrailsOppositeTheMotionAndIsCapped() {
		float[] trail = DaemonHaze.trail(0.2, -0.1, 0.0);
		assertTrue(trail[0] < 0.0F, "moving +x, the smoke trails toward -x");
		assertTrue(trail[1] > 0.0F, "sinking, the smoke trails above");
		assertEquals(0.0F, trail[2], 1e-6F);
		assertEquals(-0.6F, DaemonHaze.trail(9.0, 0, 0)[0], 1e-6F, "capped");
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
	void anOfferingSinksToTheChestAndVanishes() {
		double[] start = DaemonHaze.sink(1.0, 0.5, 0.0, 1.0, 0.0F);
		assertEquals(1.0, start[0], 1e-9);
		assertTrue(start[3] > 0.0);
		double[] end = DaemonHaze.sink(1.0, 0.5, 0.0, 1.0, DaemonHaze.SWALLOW_TICKS);
		assertEquals(0.0, end[0], 1e-9);
		assertEquals(CardinalRiteVisualRules.bodyY(DaemonHaze.CHEST, 1.0), end[1], 1e-9);
		assertEquals(0.0, end[3], 1e-9, "dissolved");
		assertTrue(DaemonHaze.sink(0, 0, 0, 0.1, 5.0F)[3] > 0.0, "a merging daemon still swallows a visible item");
	}
}
```

Replace the `draw(float seed)` helper in `DaemonHazeQueueTest` with the new `Draw` shape, and add a winding test:

```java
	static DaemonHazeQueue.Draw draw(float seed) {
		return new DaemonHazeQueue.Draw(1, 2, 3, 4.0F, 0.0F, 0.96F, 2.2F, 10.0F, seed, 1, 1, 0, 1, 0xB01420, 0,
				0, 0, 0, 0);
	}

	@Test
	void theBoxFacesWindOutwardAndCarryTheirOwnCoordinates() {
		float[] v = DaemonHazeQueue.boxVertices(0.5, 2.0);
		assertEquals(24 * 6, v.length);
		for (int face = 0; face < 6; face++) {
			int a = face * 4 * 6;
			int b = a + 6;
			int c = a + 12;
			double ex = v[b] - v[a], ey = v[b + 1] - v[a + 1], ez = v[b + 2] - v[a + 2];
			double fx = v[c] - v[a], fy = v[c + 1] - v[a + 1], fz = v[c + 2] - v[a + 2];
			double nx = ey * fz - ez * fy, ny = ez * fx - ex * fz, nz = ex * fy - ey * fx;
			double cx = 0, cy = 0, cz = 0;
			for (int corner = 0; corner < 4; corner++) {
				cx += v[a + corner * 6];
				cy += v[a + corner * 6 + 1];
				cz += v[a + corner * 6 + 2];
			}
			assertTrue(nx * cx + ny * cy + nz * cz > 0.0, "face " + face + " is counter-clockwise from outside");
		}
		for (int vertex = 0; vertex < 24; vertex++) {
			int at = vertex * 6;
			assertEquals(v[at], v[at + 3], 1e-6F, "u carries local x");
			assertEquals(v[at + 2], v[at + 4], 1e-6F, "v carries local z");
			assertEquals(v[at + 1] > 0 ? 255.0F : 0.0F, v[at + 5], 1e-6F, "red carries the height");
			assertEquals(0.5F, Math.abs(v[at]), 1e-6F);
			assertEquals(2.0F, Math.abs(v[at + 1]), 1e-6F);
		}
	}
```

- [ ] **Step 2: Run them to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeTest" --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueueTest"`
Expected: FAIL (compilation: `box`, `trail`, `boxVertices` and the new `Draw` shape do not exist).

- [ ] **Step 3: Implement**

Replace `DaemonHaze.java` with:

```java
package com.vincenthuto.hemomancy.client.render.world.rite;

/**
 * The maths behind the daemon's smoke volume: the box it is marched inside, the 3D trail of its moving smoke, and
 * the moments the shader is told about (emergence, thinning, the flash of a swallowed offering). No rendering
 * here, so all of it is unit-tested.
 */
public final class DaemonHaze {
	/** The box's half-width, as a share of the daemon's height: the figure, its soot shell and its trail. */
	public static final double BOX_HALF_WIDTH = 0.24D;
	/** The box's half-height, as a share of the daemon's height. */
	public static final double BOX_HALF_HEIGHT = 0.55D;
	/** Height fraction where swallowed offerings come to rest. */
	public static final double CHEST = 0.62D;
	/** How long a swallowed offering takes to dissolve, and the haze to settle after it. */
	public static final float SWALLOW_TICKS = 20.0F;
	private static final float TRAIL_GAIN = 2.5F;
	private static final float TRAIL_MAX = 0.6F;

	private DaemonHaze() {
	}

	/** The box's half-width and half-height in blocks, centred on the entity. */
	public static double[] box(double scale) {
		double height = CardinalRiteVisualRules.DAEMON_HEIGHT * Math.max(0.0D, scale);
		return new double[] {BOX_HALF_WIDTH * height, BOX_HALF_HEIGHT * height};
	}

	/** Where the smoke trails, in world axes, from the daemon's motion per tick: always opposite the motion. */
	public static float[] trail(double motionX, double motionY, double motionZ) {
		return new float[] {cap(-motionX * TRAIL_GAIN), cap(-motionY * TRAIL_GAIN), cap(-motionZ * TRAIL_GAIN)};
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
		return (float) (Math.max(0.0D, Math.min(1.0D, firmness))
				* (1.0D - 0.75D * Math.max(0.0F, Math.min(1.0F, cancellation))));
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

	private static float cap(double value) {
		return (float) Math.max(-TRAIL_MAX, Math.min(TRAIL_MAX, value));
	}
}
```

In `DaemonHazeQueue.java`, add `boxVertices`:

```java
	/**
	 * The box's 24 vertices as {x, y, z, u, v, red}, each face counter-clockwise seen from outside. UV carries the
	 * local x and z and red the height (0 at the bottom, 255 at the top), so the shader rebuilds the box-local point
	 * whatever the view transform.
	 */
	public static float[] boxVertices(double halfWidth, double halfHeight) {
		double[] extent = {halfWidth, halfHeight, halfWidth};
		// Per face: the outward normal, then two in-face axes whose cross product is that normal.
		int[][] faces = {
				{1, 0, 0, 0, 0, -1, 0, 1, 0}, {-1, 0, 0, 0, 0, 1, 0, 1, 0},
				{0, 0, 1, 1, 0, 0, 0, 1, 0}, {0, 0, -1, -1, 0, 0, 0, 1, 0},
				{0, 1, 0, 1, 0, 0, 0, 0, -1}, {0, -1, 0, 1, 0, 0, 0, 0, 1}};
		int[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
		float[] out = new float[24 * 6];
		int at = 0;
		for (int[] face : faces) {
			for (int[] corner : corners) {
				double[] p = new double[3];
				for (int axis = 0; axis < 3; axis++) {
					p[axis] = (face[axis] + corner[0] * face[3 + axis] + corner[1] * face[6 + axis]) * extent[axis];
				}
				out[at++] = (float) p[0];
				out[at++] = (float) p[1];
				out[at++] = (float) p[2];
				out[at++] = (float) p[0];
				out[at++] = (float) p[2];
				out[at++] = p[1] > 0.0D ? 255.0F : 0.0F;
			}
		}
		return out;
	}
```

Replace the `Draw` record with:

```java
	/** One daemon: its centre, height (blocks), yaw (radians), box half-sizes and the shader's inputs. */
	public record Draw(double x, double y, double z, float height, float yaw, float halfWidth, float halfHeight,
			float time, float seed, float emergence, float strength, float swallow, float eyes, int tint, float flare,
			float trailX, float trailY, float trailZ, float recall) {
	}
```

The queue's `render` and its card emitter are rewritten in Task 2. To keep this task compiling, change `render` to emit `boxVertices(draw.halfWidth(), draw.halfHeight())` with `setUv(u, v).setColor(red, 255, 255, 255)`. Call the factory as `HemoRenderTypes.cardinalDaemonHaze(draw, camX, camY, camZ)`, adding that overload in Task 2's form now, and delete `emitCard`. The renderer must also construct the new `Draw` in this task; see Task 2, Step 4 for its final code, applied now.

- [ ] **Step 4: Run the tests**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeTest" --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueueTest"`
Expected: PASS (6 + 3 tests).

- [ ] **Step 5: Ledger**

---

### Task 2: The volume shader and its wiring

**Files:**
- Modify (replace): `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_daemon_haze.{json,vsh,fsh}`
- Modify: `common/init/ShaderInit.java` (`CARDINAL_DAEMON_HAZE` uniform names)
- Modify: `client/render/HemoRenderTypes.java` (the daemon block between `/** One haze type per daemon seed` and `public static RenderType silentArchonVolumetricFog(`)
- Modify: `client/render/world/rite/DaemonHazeQueue.java` (`render`)
- Modify: `client/render/entity/misc/HumanitySpriteRenderer.java` (`render`)
- Test (replace): `client/render/world/rite/DaemonHazeSourceTest.java`

**Interfaces:**
- Consumes: Task 1.
- Produces: `HemoRenderTypes.cardinalDaemonHaze(DaemonHazeQueue.Draw draw, float cameraX, float cameraY, float cameraZ)`, where the camera position is relative to the daemon's centre.

- [ ] **Step 1: Write the failing source test**

Replace `DaemonHazeSourceTest.java` with:

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
	void theVolumeShaderIsRegisteredWithItsUniforms() throws IOException {
		String init = read(ROOT + "common/init/ShaderInit.java");
		String json = read(SHADERS + "cardinal_daemon_haze.json");
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		assertTrue(init.contains("registerShader(event, CARDINAL_DAEMON_HAZE.createInstance(provider));"));
		assertTrue(init.contains("\"HemoTime\", \"HazeSeed\", \"HazeState\", \"HazeTint\", \"HazeLean\", \"HazeCamera\", \"HazeShape\""));
		assertTrue(json.contains("{ \"name\": \"HazeCamera\", \"type\": \"float\", \"count\": 3"));
		assertTrue(json.contains("{ \"name\": \"HazeShape\", \"type\": \"float\", \"count\": 4"));
		assertTrue(json.contains("{ \"name\": \"HazeLean\", \"type\": \"float\", \"count\": 4"));
		for (String uniform : new String[] {"HazeSeed", "HazeState", "HazeTint", "HazeLean", "HazeCamera", "HazeShape"}) {
			assertTrue(fragment.contains("uniform") && fragment.contains(uniform), uniform + " read by the shader");
		}
	}

	@Test
	void raysAreMarchedFrontToBackThroughTheBox() throws IOException {
		String vertex = read(SHADERS + "cardinal_daemon_haze.vsh");
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		assertTrue(vertex.contains("localPos = vec3(UV0.x, mix(-HazeShape.w, HazeShape.w, Color.r), UV0.y);"),
				"the box's own coordinates, independent of the view");
		assertTrue(fragment.contains("const int MARCH_STEPS = 40;"));
		assertTrue(fragment.contains("for (int i = 0; i < MARCH_STEPS; i++)"));
		assertTrue(fragment.contains("if (inside == gl_FrontFacing) discard;"),
				"one ray per pixel outside the box, rays from the eye inside it");
		assertTrue(fragment.contains("vec3 inv = sgn / max(abs(rd), vec3(0.000001));"), "no division by zero");
		assertTrue(fragment.contains("if (transmit < 0.02) break;"), "dense smoke stops the march early");
	}

	@Test
	void eachDaemonHasItsOwnCachedTypeWithNoCulling() throws IOException {
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		assertTrue(types.contains("public static RenderType cardinalDaemonHaze(DaemonHazeQueue.Draw draw,"));
		assertTrue(types.contains("CARDINAL_DAEMON_HAZE_TYPES.get(draw.seed())"), "one type per daemon seed");
		int start = types.indexOf("private static RenderType createCardinalDaemonHaze(");
		int end = types.indexOf(".createCompositeState(false));", start);
		assertTrue(start > 0 && end > start);
		String created = types.substring(start, end);
		assertTrue(created.contains("RenderType.COLOR_WRITE"), "smoke writes no depth");
		assertTrue(created.contains("RenderType.NO_CULL"), "inside faces carry the rays when the camera is in the box");
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

	@Test
	void theRendererQueuesTheBoxAfterEveryOtherRiteVisual() throws IOException {
		String renderer = read(ROOT + "client/render/entity/misc/HumanitySpriteRenderer.java");
		String events = read(ROOT + "client/event/ClientEvents.java");
		String queue = read(ROOT + "client/render/world/rite/DaemonHazeQueue.java");
		assertTrue(renderer.contains("DaemonHazeQueue.add("));
		assertTrue(renderer.contains("DaemonHaze.box(scale)"));
		assertTrue(renderer.contains("DaemonHaze.trail("));
		assertFalse(renderer.contains("cardinalDaemonHaze("));
		int weather = events.indexOf("RenderLevelStageEvent.Stage.AFTER_WEATHER) {");
		int flush = events.indexOf("DaemonHazeQueue.render(");
		assertTrue(weather > 0 && flush > weather);
		assertTrue(queue.contains("boxVertices(draw.halfWidth(), draw.halfHeight())"));
		assertTrue(queue.contains("buffers.endBatch(type)"), "uniforms never leak between daemons");
		assertTrue(renderer.contains("if (rite != null)"), "an orphaned daemon still draws");
		assertFalse(Files.exists(Path.of(ROOT + "client/render/world/rite/CardinalDaemonGeometry.java")));
	}

	@Test
	void threeMonolithEyesLookOutOfTheHead() throws IOException {
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		assertTrue(fragment.contains("vec4 monolithEye("), "the Sanguine Monolith's almond eye");
		int calls = fragment.split("= monolithEye\\(", -1).length - 1;
		assertEquals(3, calls, "a larger centre eye and two tilted side eyes");
		assertTrue(fragment.contains("float facing = smoothstep(0.25, 0.65, normal.z);"),
				"the eyes fade as the head's surface turns away");
	}
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeSourceTest"`
Expected: FAIL (the uniforms, march loop and box wiring are missing).

- [ ] **Step 3: Write the shader**

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
    { "name": "HazeLean", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] },
    { "name": "HazeCamera", "type": "float", "count": 3, "values": [ 0.0, 0.0, 10.0 ] },
    { "name": "HazeShape", "type": "float", "count": 4, "values": [ 4.0, 0.0, 0.96, 2.2 ] }
  ]
}
```

`cardinal_daemon_haze.vsh`:

```glsl
#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 HazeShape;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec3 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // The box's own coordinates ride in UV (x, z) and red (height), so the volume never depends on the view.
    localPos = vec3(UV0.x, mix(-HazeShape.w, HazeShape.w, Color.r), UV0.y);
}
```

`cardinal_daemon_haze.fsh` (complete; `monolithEye` and `eyeFrame` are carried over unchanged from the current file):

```glsl
#version 150

// The Cardinal Rite daemon as a smoke volume, ray-marched inside its box: a round head on a teardrop body (the
// Humanity sprite, round from every side) of swirling red smoke, lit from above and shadowing itself, an ember
// breathing in its core, wrapped in a shell of black soot, with three Sanguine Monolith eyes on its face.

uniform float HemoTime;
uniform float HazeSeed;
uniform vec4 HazeState;  // emergence, strength, swallow flash, eyes open
uniform vec4 HazeTint;   // swallowed offering tint rgb, eye flare
uniform vec4 HazeLean;   // smoke trail x, y, z in world axes, recall progress
uniform vec3 HazeCamera; // camera relative to the daemon's centre, in blocks
uniform vec4 HazeShape;  // height in blocks, yaw in radians, box half-width, box half-height
uniform vec4 ColorModulator;

in vec3 localPos;

out vec4 fragColor;

const int MARCH_STEPS = 40;
const float EXTINCTION = 16.0;
const float SOOT_EXTINCTION = 10.0;

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

float valueNoise3(vec3 p) {
    vec3 cell = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    float n000 = hash13(cell);
    float n100 = hash13(cell + vec3(1.0, 0.0, 0.0));
    float n010 = hash13(cell + vec3(0.0, 1.0, 0.0));
    float n110 = hash13(cell + vec3(1.0, 1.0, 0.0));
    float n001 = hash13(cell + vec3(0.0, 0.0, 1.0));
    float n101 = hash13(cell + vec3(1.0, 0.0, 1.0));
    float n011 = hash13(cell + vec3(0.0, 1.0, 1.0));
    float n111 = hash13(cell + vec3(1.0, 1.0, 1.0));
    float lowerSheet = mix(mix(n000, n100, u.x), mix(n010, n110, u.x), u.y);
    float upperSheet = mix(mix(n001, n101, u.x), mix(n011, n111, u.x), u.y);
    return mix(lowerSheet, upperSheet, u.z);
}

float fbm3(vec3 p, int octaves) {
    float total = 0.0;
    float amp = 0.5;
    for (int octave = 0; octave < 4; octave++) {
        if (octave >= octaves) break;
        total += amp * valueNoise3(p);
        p = p * 2.03 + vec3(17.1, 9.2, 4.7);
        amp *= 0.5;
    }
    return total;
}

// Smooth minimum, so the head flows into the shoulders without a seam.
float softMin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

// The figure's signed distance in height units (foot at y 0, crown near 1, facing +z): the Humanity sprite's round
// head on a teardrop body with rounded shoulders, widest at the chest, revolved so it is round from every side.
float figure(vec3 f) {
    float head = length(f - vec3(0.0, 0.85, 0.0)) - 0.111;
    float tip = 0.03;
    float chest = 0.58;
    float crown = 0.79;
    float widest = 0.151;
    float below = clamp((f.y - tip) / (chest - tip), 0.0, 1.0);
    float teardrop = widest * pow(sin(below * 1.5707963), 0.85);
    float above = clamp((f.y - chest) / (crown - chest), 0.0, 1.0);
    float shoulders = widest * sqrt(max(0.0, 1.0 - above * above * above));
    float bodyWidth = f.y < chest ? teardrop : shoulders;
    float body = max(length(f.xz) - bodyWidth, max(tip - f.y, f.y - crown));
    return softMin(head, body, 0.02);
}

// <monolithEye(...) and eyeFrame(...) exactly as in the current file>

// A box-local point (blocks) in the figure's frame: height units up from the foot, turned to the daemon's yaw so its
// face looks along +z.
vec3 toFigure(vec3 v) {
    float c = cos(HazeShape.y);
    float s = sin(HazeShape.y);
    vec3 scaled = v / HazeShape.x;
    return vec3(scaled.x * c + scaled.z * s, scaled.y + 0.5, -scaled.x * s + scaled.z * c);
}

// Smoke and soot density at a point of the figure, with the shape's distance and the smoke value there.
float smokeAt(vec3 f, out float soot, out float shapeDist, out float smoke) {
    vec3 n = vec3(f.x * 9.0, f.y * 6.0 - HemoTime * 0.05, f.z * 9.0) + HazeSeed * vec3(7.31, 3.17, 5.3);
    float warp = fbm3(n, 3);
    smoke = fbm3(n + vec3(warp * 2.5, warp * 1.5, -warp * 2.5), 4);
    shapeDist = figure(f + vec3(warp - 0.5) * 0.03);
    float lowerBand = 1.0 - smoothstep(0.0, 0.38, f.y);
    float eroded = shapeDist + (smoke - 0.5) * (0.05 + 0.06 * lowerBand);
    float density = 1.0 - smoothstep(-0.02, 0.025, eroded);
    // The lower third breaks into rising wisps.
    density *= 1.0 - lowerBand * smoothstep(0.35, 0.75, 1.0 - smoke) * 0.9;
    // A shell of black soot just outside the surface.
    soot = smoothstep(-0.01, 0.015, eroded) * (1.0 - smoothstep(0.02, 0.07, eroded));
    // Emergence lifts the smoke out of the staff from the bottom up.
    float top = HazeState.x * 1.15;
    float emerged = 1.0 - smoothstep(top - 0.15, top, f.y);
    density *= emerged * HazeState.y * (1.0 + HazeState.z * 0.25);
    soot *= emerged * HazeState.y;
    return density;
}

void main() {
    float height = HazeShape.x;
    vec3 halfSize = vec3(HazeShape.z, HazeShape.w, HazeShape.z);
    vec3 ro = HazeCamera;
    vec3 rd = normalize(localPos - ro);
    vec3 sgn = step(0.0, rd) * 2.0 - 1.0;
    vec3 inv = sgn / max(abs(rd), vec3(0.000001));
    vec3 t0 = (-halfSize - ro) * inv;
    vec3 t1 = (halfSize - ro) * inv;
    vec3 tLow = min(t0, t1);
    vec3 tHigh = max(t0, t1);
    float tNear = max(max(tLow.x, tLow.y), tLow.z);
    float tFar = min(min(tHigh.x, tHigh.y), tHigh.z);
    bool inside = all(lessThan(abs(ro), halfSize));
    // Outside the box only the near faces start rays, so each pixel is marched once; inside it, the far faces do.
    if (inside == gl_FrontFacing) discard;
    tNear = max(tNear, 0.0);
    if (tFar <= tNear) discard;

    // The eyes: where the ray first meets the head sphere, on its forward-facing surface.
    float c = cos(HazeShape.y);
    float s = sin(HazeShape.y);
    vec3 roF = toFigure(ro);
    vec3 rdF = normalize(vec3(rd.x * c + rd.z * s, rd.y, -rd.x * s + rd.z * c));
    vec3 headCentre = vec3(0.0, 0.85, 0.0);
    vec3 oc = roF - headCentre;
    float b = dot(oc, rdF);
    float cc = dot(oc, oc) - 0.111 * 0.111;
    float disc = b * b - cc;
    float bright = 1.0 + HazeTint.a * 0.4;
    float eyeT = -1.0;
    vec3 eyeColour = vec3(0.0);
    float eyeCover = 0.0;
    float eyeSpill = 0.0;
    if (cc > 0.0 && disc > 0.0) {
        float hitF = -b - sqrt(disc);
        vec3 hit = roF + rdF * hitF;
        vec3 normal = (hit - headCentre) / 0.111;
        float facing = smoothstep(0.25, 0.65, normal.z);
        if (hitF > 0.0 && facing > 0.0) {
            eyeT = hitF * height;
            float blinkCycle = fract(HemoTime / 173.0 + HazeSeed * 0.13);
            float lid = smoothstep(0.0, 0.008, abs(blinkCycle - 0.5) - 0.004);
            float flicker = mix(1.0, step(0.35, valueNoise3(vec3(HemoTime * 0.9, HazeSeed, 0.5))), HazeLean.w);
            float seen = HazeState.w * flicker * facing;
            // Laid out as on the Monolith's face: a larger centre eye, and two smaller ones a little lower, their top
            // tips tilted outward. A blink squashes them shut; each pulses on its own phase.
            float spin = HemoTime * 0.003;
            float squash = max(lid, 0.06);
            vec2 centreAt = eyeFrame(hit.xy, vec2(0.0, 0.865), 0.0, 0.198);
            vec2 leftAt = eyeFrame(hit.xy, vec2(-0.054, 0.850), 0.384, 0.158);
            vec2 rightAt = eyeFrame(hit.xy, vec2(0.054, 0.850), -0.384, 0.158);
            centreAt.y /= squash;
            leftAt.y /= squash;
            rightAt.y /= squash;
            float centreGlow;
            float leftGlow;
            float rightGlow;
            vec4 centreEye = monolithEye(centreAt, 0.75 + 0.25 * sin(HemoTime * 0.05),
                    0.6 + 0.4 * sin(HemoTime * 0.12 + 1.2), spin, centreGlow);
            vec4 leftEye = monolithEye(leftAt, 0.75 + 0.25 * sin(HemoTime * 0.05 + 1.1),
                    0.6 + 0.4 * sin(HemoTime * 0.12 + 2.3), spin, leftGlow);
            vec4 rightEye = monolithEye(rightAt, 0.75 + 0.25 * sin(HemoTime * 0.05 + 2.2),
                    0.6 + 0.4 * sin(HemoTime * 0.12 + 0.5), spin, rightGlow);
            eyeSpill = clamp((centreGlow + leftGlow + rightGlow) * seen, 0.0, 1.0);
            vec3 eyeSum = vec3(0.0);
            for (int eye = 0; eye < 3; eye++) {
                vec4 shown = eye == 0 ? leftEye : eye == 1 ? rightEye : centreEye;
                float cover = clamp(shown.a * seen, 0.0, 1.0);
                eyeSum += min(shown.rgb * bright, vec3(1.0)) * cover;
                eyeCover = max(eyeCover, cover);
            }
            eyeColour = eyeSum / max(eyeCover, 0.0001);
        }
    }

    float stepLen = (tFar - tNear) / float(MARCH_STEPS);
    float jitter = hash13(vec3(gl_FragCoord.xy, fract(HemoTime * 0.13) * 97.0));
    vec3 light = normalize(vec3(0.3, 1.0, 0.25));
    vec3 deepRed = vec3(0.10, 0.008, 0.016);
    vec3 crimson = vec3(0.62, 0.05, 0.08);
    vec3 ember = vec3(1.0, 0.25, 0.12);
    vec3 sootColour = vec3(0.012, 0.003, 0.005);
    float breath = 0.5 + 0.5 * sin(HemoTime * 0.31 + HazeSeed);
    float swallow = HazeState.z;
    vec3 gathered = vec3(0.0);
    float transmit = 1.0;
    bool eyesDone = eyeT < 0.0;
    for (int i = 0; i < MARCH_STEPS; i++) {
        float t = tNear + (float(i) + jitter) * stepLen;
        if (!eyesDone && t >= eyeT) {
            // The eyes' red light, then the eyes, at the depth of the face, veiled by the smoke in front of them.
            gathered += transmit * eyeSpill * 0.35 * vec3(0.80, 0.05, 0.05) * bright;
            gathered += transmit * eyeCover * eyeColour;
            transmit *= 1.0 - eyeCover;
            eyesDone = true;
        }
        vec3 v = ro + rd * t;
        // Moving smoke lags behind: the base trails furthest.
        float rise = clamp(v.y / height + 0.5, 0.0, 1.0);
        v -= HazeLean.xyz * (1.0 - rise) * 0.15 * height;
        vec3 f = toFigure(v);
        float soot;
        float shapeDist;
        float smoke;
        float density = smokeAt(f, soot, shapeDist, smoke);
        if (density + soot < 0.003) continue;
        float bare = figure(f);
        float lit = clamp((figure(f + light * 0.03) - bare) / 0.03, -1.0, 1.0);
        float interior = 1.0 - smoothstep(-0.08, 0.0, shapeDist);
        vec3 colour = mix(deepRed, crimson, 0.2 + 0.65 * max(lit, 0.0));
        colour *= 1.0 - 0.55 * interior;
        colour += ember * pow(smoke, 3.0) * interior * (0.35 + 0.2 * breath);
        colour = mix(colour, HazeTint.rgb, swallow * 0.45) * (1.0 + swallow * 0.8);
        // A dark cavity in the face, like the Monolith's black stone, for the eyes to look out of.
        float face = (1.0 - smoothstep(0.40, 1.10, length((f.xy - vec2(0.0, 0.857)) / 0.115)))
                * smoothstep(0.0, 0.06, f.z);
        colour = mix(colour, vec3(0.025, 0.0, 0.006), 0.9 * face);
        float dt = stepLen / height;
        float absorb = 1.0 - exp(-(density * EXTINCTION + soot * SOOT_EXTINCTION) * dt);
        vec3 sampleColour = mix(colour, sootColour, soot / (density + soot + 0.0001));
        gathered += transmit * absorb * sampleColour;
        transmit *= 1.0 - absorb;
        if (transmit < 0.02) break;
    }
    if (!eyesDone) {
        gathered += transmit * eyeCover * eyeColour;
        transmit *= 1.0 - eyeCover;
    }

    float alpha = 1.0 - transmit;
    if (alpha <= 0.004) {
        discard;
    }
    fragColor = vec4(gathered / alpha, alpha) * ColorModulator;
}
```

- [ ] **Step 4: Wire it**

`ShaderInit.java`: change the `CARDINAL_DAEMON_HAZE` uniform list to `"HemoTime", "HazeSeed", "HazeState", "HazeTint", "HazeLean", "HazeCamera", "HazeShape"`.

`HemoRenderTypes.java`: replace everything from `/** One haze type per daemon seed` up to, but not including, `public static RenderType silentArchonVolumetricFog(` with:

```java
	/** One haze type per daemon seed; its uniforms change in place, so the queue ends its batch per daemon. */
	private static final Map<Float, CardinalDaemonHazeType> CARDINAL_DAEMON_HAZE_TYPES = riteTypeCache();

	private static final class CardinalDaemonHazeType {
		private DaemonHazeQueue.Draw draw;
		private float cameraX;
		private float cameraY;
		private float cameraZ;
		private final RenderType type;

		private CardinalDaemonHazeType() {
			this.type = createCardinalDaemonHaze(this);
		}
	}

	/** The daemon's smoke volume; the camera position is relative to the daemon's centre. */
	public static RenderType cardinalDaemonHaze(DaemonHazeQueue.Draw draw, float cameraX, float cameraY,
			float cameraZ) {
		CardinalDaemonHazeType haze = CARDINAL_DAEMON_HAZE_TYPES.get(draw.seed());
		if (haze == null) {
			haze = new CardinalDaemonHazeType();
			CARDINAL_DAEMON_HAZE_TYPES.put(draw.seed(), haze);
		}
		haze.draw = draw;
		haze.cameraX = cameraX;
		haze.cameraY = cameraY;
		haze.cameraZ = cameraZ;
		return haze.type;
	}

	private static RenderType createCardinalDaemonHaze(CardinalDaemonHazeType haze) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_daemon_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_DAEMON_HAZE.getInstance().get();
					DaemonHazeQueue.Draw draw = haze.draw;
					if (draw == null) return;
					setUniform(shader, "HemoTime", draw.time());
					setUniform(shader, "HazeSeed", draw.seed());
					setUniform(shader, "HazeState", draw.emergence(), draw.strength(), draw.swallow(), draw.eyes());
					setUniform(shader, "HazeTint", ((draw.tint() >> 16) & 0xFF) / 255.0F,
							((draw.tint() >> 8) & 0xFF) / 255.0F, (draw.tint() & 0xFF) / 255.0F, draw.flare());
					setUniform(shader, "HazeLean", draw.trailX(), draw.trailY(), draw.trailZ(), draw.recall());
					setUniform(shader, "HazeCamera", haze.cameraX, haze.cameraY, haze.cameraZ);
					setUniform(shader, "HazeShape", draw.height(), draw.yaw(), draw.halfWidth(), draw.halfHeight());
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

Add `import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue;` if the file does not already import it.

`DaemonHazeQueue.render`: replace the loop body with:

```java
		for (Draw draw : draws) {
			RenderType type = HemoRenderTypes.cardinalDaemonHaze(draw, (float) (camera.x - draw.x()),
					(float) (camera.y - draw.y()), (float) (camera.z - draw.z()));
			VertexConsumer consumer = buffers.getBuffer(type);
			poseStack.pushPose();
			poseStack.translate(draw.x() - camera.x, draw.y() - camera.y, draw.z() - camera.z);
			Matrix4f matrix = poseStack.last().pose();
			float[] box = boxVertices(draw.halfWidth(), draw.halfHeight());
			for (int at = 0; at < box.length; at += 6) {
				consumer.addVertex(matrix, box[at], box[at + 1], box[at + 2]).setUv(box[at + 3], box[at + 4])
						.setColor((int) box[at + 5], 255, 255, 255);
			}
			poseStack.popPose();
			buffers.endBatch(type);
		}
```

Delete `emitCard` and update the class javadoc ("the box is drawn after the clouds and every other rite visual").

`HumanitySpriteRenderer.render`:
- Remove the `camera`, `toCameraX` and `toCameraZ` locals.
- Replace the `eyes`, `lean`, `seed` and `DaemonHazeQueue.add(...)` lines with:

```java
		float eyes = DaemonHaze.eyesOpen(emergence);
		float[] trail = DaemonHaze.trail(entity.getX() - entity.xo, entity.getY() - entity.yo,
				entity.getZ() - entity.zo);
		float seed = Math.floorMod(entity.getId(), 113);
		double[] box = DaemonHaze.box(scale);

		// Queued, and drawn after every rite visual and the clouds: the smoke writes no depth, so anything drawn
		// after it would paint over it.
		DaemonHazeQueue.add(new DaemonHazeQueue.Draw(position.x, position.y, position.z,
				(float) (CardinalRiteVisualRules.DAEMON_HEIGHT * scale), (float) yaw, (float) box[0], (float) box[1],
				time, seed, emergence, strength, swallow, eyes, tint, flare, trail[0], trail[1], trail[2],
				cancellation));
```

- Update the class javadoc to describe a smoke volume marched inside a box. Inflate the culling reach to `1.0D + entity.getSpriteScale() * 2.5D`, so the trailing box stays inside it.

- [ ] **Step 5: Run the tests, validate the shader and compile**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.*" --tests "com.vincenthuto.hemomancy.client.render.ShaderReservedWordTest"`
Expected: PASS.

Run: `glslangValidator -S frag …/cardinal_daemon_haze.fsh` and `glslangValidator -S vert …/cardinal_daemon_haze.vsh`
Expected: exit 0.

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Ledger**

---

### Task 3: Preview, docs, suite and the GPU check

- [ ] **Step 1: Preview port**

Port the new fragment shader to numpy in the session scratchpad (`volume_port.py`), at 40 steps. Render the daemon at height 4 from five cameras looking at its centre: front (0, 0, 10), side (10, 0, 0), three-quarter (7, 1, 7), behind (0, 0, -10) and above (0.5, 10, 0.5), over the sky colour. Save one strip to `volume_views.png`, read it and check that:
- the figure keeps its silhouette from every side;
- the core is dense, not pale;
- the light comes from above;
- there is a dark soot rim;
- the eyes show only from the front and fade toward the side.

Tune only the named constants (`EXTINCTION`, `SOOT_EXTINCTION`, the colours, the light) if something reads wrong, mirroring each change in the shader. Ledger every tuning change.

- [ ] **Step 2: Docs**

In `docs/HEMOMANCY_REFERENCE.md`, replace the paragraph starting `**The daemon** (\`HumanitySpriteRenderer\`)` with:

```markdown
**The daemon** (`HumanitySpriteRenderer`) is a volume of red smoke ray-marched inside a box by one shader (`cardinal_daemon_haze`; `DaemonHaze`, `DaemonHazeQueue`): the Humanity sprite's round head on a teardrop body with rounded shoulders, widest at the chest and tapering to a point, round from every side. Swirling 3D noise drifts up through it and its lower third breaks into rising wisps; its core is dense, it is lit from above and shadows itself, an ember breathes inside it, and a shell of black soot outlines it. Three eyes like the Sanguine Monolith's look out of a dark face on the front of its head and turn with it (a larger centre eye, two smaller side eyes tilted outward; almond lids, red irises with slowly turning spokes, dark pupils and white glints), fading as the head turns away; they blink rarely, flicker while it is recalled, and flare as the rite's world stage rises. It rises out of the staff from the bottom up and opens its eyes last. It thins as Blood Absorption draws it back, trails its smoke behind its motion, and flashes, tinted, as it swallows each offering (the item drifts into its chest and dissolves). A camera inside the volume looks out through its smoke.
```

- [ ] **Step 3: Full suite and GameTests**

Run: `./gradlew.bat test`
Expected: only the 16 known pre-existing failures.

Run: `./gradlew.bat runGameTestServer`
Expected: only the known failures and flakes.

- [ ] **Step 4: GPU check**

Launch `runSuccessionReviewClient` in the background and wait for a fresh `Sound engine started` log. Grep the log for `Couldn't compile`; there must be none. Stop the client's `java.exe`: its command line matches `succession` and `fml.modFolders`.

- [ ] **Step 5: Ledger, final review and finish** (no commit)

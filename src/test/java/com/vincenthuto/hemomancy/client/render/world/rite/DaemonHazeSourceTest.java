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
		String init = read(ROOT + "client/render/shader/ShaderInit.java");
		String json = read(SHADERS + "cardinal_daemon_haze.json");
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		assertTrue(init.contains("registerShader(event, CARDINAL_DAEMON_HAZE.createInstance(provider));"));
		assertTrue(init.contains("\"HemoTime\", \"HazeSeed\", \"HazeState\", \"HazeTint\", \"HazeLean\", \"HazeCamera\", \"HazeShape\", \"HazeHead\""));
		assertTrue(json.contains("{ \"name\": \"HazeCamera\", \"type\": \"float\", \"count\": 3"));
		assertTrue(json.contains("{ \"name\": \"HazeShape\", \"type\": \"float\", \"count\": 4"));
		assertTrue(json.contains("{ \"name\": \"HazeLean\", \"type\": \"float\", \"count\": 4"));
		assertTrue(json.contains("{ \"name\": \"HazeHead\", \"type\": \"float\", \"count\": 1"));
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
		assertTrue(fragment.contains("const int MARCH_STEPS = 56;"));
		assertTrue(fragment.contains("for (int i = 0; i < MARCH_STEPS; i++)"));
		assertTrue(fragment.contains("if (inside == gl_FrontFacing) discard;"),
				"one ray per pixel outside the box, rays from the eye inside it");
		assertTrue(fragment.contains("vec3 inv = sgn / max(abs(rd), vec3(0.000001));"), "no division by zero");
		assertTrue(fragment.contains("if (transmit < 0.02) break;"), "dense smoke stops the march early");
		assertTrue(fragment.contains("bool inside = all(lessThan(abs(ro), halfSize + vec3(0.15)));"),
				"a camera at a face still marches from the eye, so the daemon never blinks out as it passes into you");
		assertTrue(fragment.contains("float stepLen = STEP_HEIGHT * height;"), "a fixed step: short rays cost less");
		assertTrue(fragment.contains("if (t > tFar) break;"));
		assertTrue(fragment.contains("float clearance = figure(f);"));
		assertTrue(fragment.contains("tNext = t + max(stepLen, (clearance - EMPTY_DISTANCE) * SKIP_SAFETY * height);"),
				"empty space is strided over, so a long streaming box still reaches the smoke");
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		assertTrue(types.contains("DaemonHaze.cameraInside("), "inside the box the smoke is not hidden by what shares it");
		assertTrue(types.contains("RenderType.NO_DEPTH_TEST"));
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
		String events = read(ROOT + "client/render/world/WorldEffectDispatcher.java");
		String queue = read(ROOT + "client/render/world/rite/DaemonHazeQueue.java");
		assertTrue(renderer.contains("DaemonHazeQueue.add("));
		assertTrue(renderer.contains("DaemonHaze.box(scale, tilt[3], stream)"));
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
	void theHeadTurnsAndMovesAheadOfTheBody() throws IOException {
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		String renderer = read(ROOT + "client/render/entity/misc/HumanitySpriteRenderer.java");
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		String entity = read(ROOT + "common/entity/utility/HumanitySpriteEntity.java");
		assertTrue(fragment.contains("float twist = (HazeHead - HazeShape.y) * smoothstep(0.35, 0.80, up);"),
				"the smoke twists from the body's facing to the head's");
		assertTrue(fragment.contains("v += HazeLean.xyz * smoothstep(0.6, 0.9, rise) * HEAD_LEAD * height;"),
				"the head drifts ahead along its motion while the base trails");
		assertTrue(fragment.contains("vec3 roF = toHead(ro + HazeLean.xyz * HEAD_LEAD * height);"),
				"the eyes ride the leading head");
		assertTrue(types.contains("setUniform(shader, \"HazeHead\", draw.headYaw());"));
		assertTrue(renderer.contains("entity.renderHeadYaw(partialTick)") && renderer.contains("entity.renderBodyYaw(partialTick)"));
		assertTrue(entity.contains("DaemonTurn.toward(headTurn, getYRot(), DaemonTurn.HEAD_RATE)"));
		assertTrue(entity.contains("DaemonTurn.toward(bodyTurn, getYRot(), DaemonTurn.BODY_RATE)"));
	}

	@Test
	void inFlightTheBodyLiesAlongItsPathHeadFirstLookingWhereItGoes() throws IOException {
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		String json = read(SHADERS + "cardinal_daemon_haze.json");
		String renderer = read(ROOT + "client/render/entity/misc/HumanitySpriteRenderer.java");
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		String init = read(ROOT + "client/render/shader/ShaderInit.java");
		assertTrue(fragment.contains("uniform vec4 HazeTilt;"));
		assertTrue(json.contains("{ \"name\": \"HazeTilt\", \"type\": \"float\", \"count\": 4"));
		assertTrue(fragment.contains("vec3 upright = unyaw(turnAbout(v / HazeShape.x, HazeTilt.xyz, -HazeTilt.w), HazeShape.y);"),
				"the body is stood back up from its tilt along its path, diving or climbing, crown first");
		assertTrue(fragment.contains("vec3 centre = turnAbout(vec3(0.0, 0.35, 0.0), HazeTilt.xyz, HazeTilt.w);"),
				"the head rides the tilted body");
		assertTrue(fragment.contains("vec3 roF = toHead("), "the eyes are found in the head's own frame");
		assertTrue(fragment.contains("vec3 lightF = unyaw(turnAbout(light, HazeTilt.xyz, -HazeTilt.w), HazeShape.y);"),
				"lit from the sky above, whichever way the body lies");
		assertTrue(types.contains("setUniform(shader, \"HazeTilt\", draw.tiltX(), 0.0F, draw.tiltZ(), draw.tiltAngle());"));
		assertTrue(init.contains("\"HazeHead\", \"HazeTilt\", \"HazeFlow\""));
		assertTrue(renderer.contains("DaemonHaze.tilt(crown[0], crown[1], crown[2], yaw)"), "posed by how it moves");
		assertTrue(renderer.contains("DaemonHaze.box(scale, tilt[3], stream)"));
		for (String source : new String[] {fragment, json, renderer, types, init}) {
			assertFalse(source.contains("HazePitch"), "no prone flag: the pose follows the motion");
		}
	}

	@Test
	void inMotionTheSmokeStreamsCoilsAndFrays() throws IOException {
		String fragment = read(SHADERS + "cardinal_daemon_haze.fsh");
		String json = read(SHADERS + "cardinal_daemon_haze.json");
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		assertTrue(fragment.contains("uniform vec2 HazeFlow;"));
		assertTrue(json.contains("{ \"name\": \"HazeFlow\", \"type\": \"float\", \"count\": 2"));
		assertTrue(types.contains("setUniform(shader, \"HazeFlow\", draw.flow(), draw.stream());"));
		assertTrue(fragment.contains("vec3 streaming(vec3 f)"), "the tail stretches, coils and fans out behind");
		assertTrue(fragment.contains("const float TAIL_STRETCH = " + DaemonHaze.TAIL_STRETCH + ";"),
				"the box is sized for the same stretch");
		assertTrue(fragment.contains("(f.y * 6.0 + HazeFlow.x * 3.6) / (1.0 + 1.5 * HazeFlow.y)"),
				"the smoke flows back over the body and draws out into streaks along it");
		assertTrue(fragment.contains("HazeFlow.x * 1.8"), "its lumps flow back with it");
	}

	@Test
	void theDaemonGlidesBetweenServerUpdates() throws IOException {
		String entity = read(ROOT + "common/entity/utility/HumanitySpriteEntity.java");
		String renderer = read(ROOT + "client/render/entity/misc/HumanitySpriteRenderer.java");
		assertTrue(entity.contains("public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps)"),
				"a plain entity snaps to each update; the daemon eases toward it");
		assertTrue(entity.contains("lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);"));
		assertTrue(entity.contains("flight.tick(getX() - xo, getY() - yo, getZ() - zo,"),
				"its pose and streaming follow how it actually moves");
		assertTrue(renderer.contains("entity.renderScale(partialTick)"), "size changes ease instead of jumping");
		assertTrue(renderer.contains("DaemonHaze.trail(velocity[0], velocity[1], velocity[2])"),
				"the trail follows the smoothed motion, not one tick's");
		assertFalse(entity.contains("setFlying"), "no prone flag");
		assertFalse(entity.contains("PRONE_PITCH_DEGREES"));
	}

	@Test
	void theDaemonKeepsOneSizeInsteadOfGrowing() throws IOException {
		String events = read(ROOT + "common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		String engine = read(ROOT + "common/rite/harbinger/CardinalRiteOrdealEngine.java");
		String entity = read(ROOT + "common/entity/utility/HumanitySpriteEntity.java");
		assertFalse(events.contains("contraction * emergence"), "it does not inflate out of the staff");
		assertTrue(events.contains("CardinalRiteFinaleTiming.daemonScale(0) * contraction"),
				"nor grow with the rite's progress");
		assertFalse(engine.contains("matureDaemonScale"), "nor grow before merging");
		assertTrue(engine.contains("CardinalRiteFinaleTiming.daemonScale(total)"),
				"it merges at the size the offerings left it");
		assertFalse(events.contains("The daemon grows as"));
		assertTrue(entity.contains("entityData.get(EMERGED)"),
				"a client arriving late skips the emergence by the server's word, not by the daemon's size");
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

	@Test
	void theStaffSmokesWhileTheDaemonIsAbroad() throws IOException {
		String init = read(ROOT + "client/render/shader/ShaderInit.java");
		String json = read(SHADERS + "cardinal_staff_haze.json");
		String fragment = read(SHADERS + "cardinal_staff_haze.fsh");
		String renderer = read(ROOT + "client/render/entity/misc/HumanitySpriteRenderer.java");
		String queue = read(ROOT + "client/render/world/rite/DaemonHazeQueue.java");
		String types = read(ROOT + "client/render/HemoRenderTypes.java");
		assertTrue(init.contains("registerShader(event, CARDINAL_STAFF_HAZE.createInstance(provider));"));
		assertTrue(json.contains("\"vertex\": \"hemomancy:world/cardinal_daemon_haze\""), "the same box vertex shader");
		assertTrue(json.contains("\"fragment\": \"hemomancy:world/cardinal_staff_haze\""));
		assertTrue(fragment.contains("float turn = rise * 3.0"), "the smoke curls as it rises");
		assertTrue(fragment.contains("if (inside == gl_FrontFacing) discard;"));
		assertTrue(renderer.contains("DaemonHazeQueue.addStaff("), "only while the daemon is drawn");
		assertTrue(queue.contains("HemoRenderTypes.cardinalStaffHaze("));
		assertTrue(types.contains("public static RenderType cardinalStaffHaze("));
		assertTrue(queue.contains("DaemonHaze.eyeInBox(RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix(),"),
				"rays start at the eye the frame is drawn from, view bobbing included");
		assertFalse(queue.contains("(float) (camera.x - draw.x())"), "not the bob-less camera position");
	}
}

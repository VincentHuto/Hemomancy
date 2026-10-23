package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class RiteWorldSourceTest {
	static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	@Test
	void everyRiteTickAnnouncesItsStep() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");

		// That ceremonies and successions raise their stage through the tick, and that the stage reaches the caster,
		// is behaviour: rite_kind_validation's ceremonyStageReachesItsCasterOnLogin and
		// successionRunsItsCourseThroughTheTick.
		assertTrue(events.contains("CardinalRiteEventKind.ESCALATION, rite.getEscalationStage()"),
				"each step is sent as a beat carrying the new stage");
	}

	@Test
	void thePanelOnlyDrawsWhenTheConfigAsksForIt() throws IOException {
		String overlay = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/CardinalRiteOverlay.java");

		assertTrue(overlay.contains("if (HemoClientConfig.cardinalRiteHud() != HemoClientConfig.CardinalRiteHudMode.FULL) return;"));
	}

	@Test
	void theFloorShaderReceivesTheRitesBlood() throws IOException {
		String shaders = read("src/main/java/com/vincenthuto/hemomancy/client/render/shader/ShaderInit.java");
		String json = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.json");
		String fragment = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh");
		String types = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/CardinalFloorRenderTypes.java");
		String response = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/FloorRiteResponse.java");

		assertTrue(shaders.contains("\"WoundAngles\", \"WoundLevels\", \"RiteBlood\""), "uniform cached by the holder");
		assertTrue(json.contains("{ \"name\": \"RiteBlood\", \"type\": \"float\", \"count\": 4"), "uniform declared to Minecraft");
		assertTrue(fragment.contains("uniform vec4 RiteBlood;"), "uniform declared to GLSL");
		assertTrue(types.contains("set(shader, \"RiteBlood\", blood);"), "uniform loaded per Focus");
		assertTrue(response.contains("CardinalRiteEventKind.ESCALATION"), "each step strikes the floor like the staff");
		assertTrue(response.contains("RiteWorldState.bloodUniform("), "the trench and creep follow the world stage");
	}

	@Test
	void theCreepIsDrawnWithTheFloor() throws IOException {
		String fragment = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/CardinalFloorConsecrationRenderer.java");

		assertTrue(fragment.contains("vec4 ring = vertexColor.r < 0.5 ? creepGround(p) : groundRing(p);"),
				"red 0 marks creep cells on the ground layer");
		assertTrue(renderer.contains("mesh.emitCreep(consumer, matrix, level, origin, rite.creepRadius(), 255);"));
		assertTrue(renderer.contains("RiteWorldMath.creepReach("), "the Focus's render bounds cover the creep");
	}

	@Test
	void theSkyWoundIsRegisteredAndDrawn() throws IOException {
		String shaders = read("src/main/java/com/vincenthuto/hemomancy/client/render/shader/ShaderInit.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/WorldEffectDispatcher.java");
		String tick = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientTickEvents.java");
		String session = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientSessionEvents.java");

		assertTrue(shaders.contains("registerShader(event, CARDINAL_RITE_SKY_WOUND.createInstance(provider));"));
		int weather = events.indexOf("RenderLevelStageEvent.Stage.AFTER_WEATHER) {");
		int wound = events.indexOf("RiteSkyWound.render(");
		assertTrue(weather >= 0 && wound > weather && !events.substring(weather, wound).contains("event.getStage()"),
				"the wound draws after the clouds, which vanilla renders after translucent blocks");
		assertEquals(wound, events.lastIndexOf("RiteSkyWound.render("), "and only there");
		String types = read("src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
		int skyType = types.indexOf("\"cardinal_rite_sky_wound\",");
		int skyEnd = types.indexOf(".createCompositeState(false));", skyType);
		int depth = types.indexOf("RenderType.COLOR_DEPTH_WRITE", skyType);
		assertTrue(skyType >= 0 && depth > skyType && depth < skyEnd,
				"it writes depth, so Fabulous graphics composite it in front of the clouds behind it");
		String woundShader = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_rite_sky_wound.fsh");
		assertTrue(woundShader.contains("if (alpha <= 0.004) {\n        discard;"), "empty sky around it writes no depth");
		assertTrue(tick.contains("RiteSkyWound.tick(Minecraft.getInstance());"));
		assertTrue(session.contains("RiteSkyWound.clear();"));
	}

	@Test
	void braziersFollowTheWorldStage() throws IOException {
		String flames = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/BrazierFlames.java");

		assertTrue(flames.contains("RiteWorldState.flameAt(pos, time)"), "the fire reads the rite's world");
		assertTrue(flames.contains("Vec3 up = new Vec3(flame.leanX(), 1, flame.leanZ()).normalize();"), "it leans");
		assertTrue(flames.contains("if (!hot) ThermalGeometry.smoke("), "sanguine fire does not smoke");
	}

	@Test
	void bloodRainUsesHemomancyParticlesOnly() throws IOException {
		String rain = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteBloodRain.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientTickEvents.java");

		assertTrue(rain.contains("HemoParticleData.bloodCell("));
		assertFalse(rain.contains("ParticleTypes."), "no vanilla particles");
		assertTrue(events.contains("RiteBloodRain.tick(Minecraft.getInstance());"));
	}

	@Test
	void sleepingSocketsAreMarkedInTheWorld() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/WorldEffectDispatcher.java");
		String marker = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteSocketMarkerRenderer.java");

		assertTrue(events.contains("RiteSocketMarkerRenderer.render(event.getPoseStack(), partialTick);"));
		assertTrue(marker.contains("rite.getSocketMarkers()"));
		assertTrue(marker.contains("AnimusMortemRenderTypes.ANIMUS"), "living blood, like the anchor jets");
	}
}

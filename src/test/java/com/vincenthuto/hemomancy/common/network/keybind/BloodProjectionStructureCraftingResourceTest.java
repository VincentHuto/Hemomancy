package com.vincenthuto.hemomancy.common.network.keybind;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodProjectionStructureCraftingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BloodProjectionStructureCraftingResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String projectionItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java"));
		String keyPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/keybind/BloodCraftingKeyPressPacket.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String feedPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/PacketBloodStructureFeed.java"));
		String feedManager = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/BloodStructureFeedManager.java"));
		String clientData = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/data/ActiveBloodStructureFeedClientData.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/world/BloodStructureFeedWarpRenderer.java"));
		String spiralParticles = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/world/BloodStructureFeedSpiralParticles.java"));
		String feedAbocipherParticle = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/particle/BloodStructureFeedAbocipherParticle.java"));
		String abocipherFactory = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/particle/factory/AbocipherParticleFactory.java"));
		String bloodCellFactory = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/particle/factory/BloodCellParticleFactory.java"));
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String shaderInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ShaderInit.java"));
		String renderTypes = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java"));
		String shaderJson = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/world/blood_structure_warp.json"));
		String vertexShader = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/world/blood_structure_warp.vsh"));
		String fragmentShader = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/world/blood_structure_warp.fsh"));

		assertContains("projection item tries structure feed before tile transfer", projectionItem,
				"BloodStructureFeedManager.feedStructure");
		assertContains("projection use starts while player has blood", projectionItem,
				"volume.getBloodVolume() > 0");
		assertContains("key packet leaves normal Harbinger structures to projection", keyPacket,
				"!targetPattern.isUnstained()");
		assertContains("key packet keeps unstained structure path", keyPacket,
				"targetPattern.isUnstained()");
		assertContains("key packet hints projection use", keyPacket,
				"use Blood Projection");
		assertContains("puppeteer trials remain key based", keyPacket,
				"tryActivatePuppeteerTrial");
		assertContains("cardinal rites remain key based", keyPacket,
				"tryStartCardinalRite");
		assertContains("feed manager uses current projection feed rate", feedManager,
				"BloodStructureFeedRules.STRUCTURE_FEED_RATE");
		assertContains("feed manager locks completed structures during collapse", feedManager,
				"COMPLETING_FEEDS");
		assertContains("feed manager checks completion lock before draining", feedManager,
				"BloodStructureFeedRules.isCompletionLocked");
		assertContains("feed manager expires partial progress without refunds", feedManager,
				"BloodStructureFeedRules.isExpired");
		assertContains("feed manager completes through pending collapse flow", feedManager,
				"PendingBloodCraftManager.PendingCraft");
		assertContains("feed manager keeps warp visible during collapse", feedManager,
				"COMPLETION_VISIBLE_TICKS");
		assertContains("completion feed sync should linger instead of clearing immediately", feedManager,
				"sendFeedSync(level, positions, 1.0f, false, COMPLETION_VISIBLE_TICKS)");
		assertContains("feed manager consumes offhand catalyst in survival", feedManager,
				"offhandCatalyst.shrink(1)");
		assertContains("feed sync packet registered", packetHandler,
				"PacketBloodStructureFeed.TYPE");
		assertContains("feed packet targets client cache", feedPacket,
				"ActiveBloodStructureFeedClientData");
		assertContains("client feed cache tracks positions", clientData,
				"List<BlockPos>");
		assertContains("world renderer renders connected exposed shell", renderer,
				"renderConnectedShell");
		assertContains("world renderer skips hidden shared faces", renderer,
				"isInternalFace");
		assertContains("world renderer checks adjacent matched positions", renderer,
				"positions.contains(pos.relative(direction))");
		assertContains("world renderer emits unit shell faces to avoid diagonal quad splits", renderer,
				"emitUnitFace");
		assertContains("world renderer uses warp render type", renderer,
				"HemoRenderTypes.bloodStructureWarp");
		assertContains("world renderer passes a coherent structure center", renderer,
				"bounds.centerX(camera)");
		assertNotContains("world renderer should not draw duplicate block models", renderer,
				"renderSingleBlock");
		assertNotContains("world renderer should not merge faces into slash-prone giant quads", renderer,
				"emitMergedPlaneFaces");
		assertNotContains("world renderer should not greedily extend giant rectangles", renderer,
				"canExtendRectangle");
		assertContains("client events tick feed cache", clientEvents,
				"ActiveBloodStructureFeedClientData.tick()");
		assertContains("client events tick structure feed spiral particles", clientEvents,
				"BloodStructureFeedSpiralParticles.tick()");
		assertContains("client events render feed warp before collapse ring", clientEvents,
				"BloodStructureFeedWarpRenderer.render");
		assertContains("feed spiral uses active client feeds", spiralParticles,
				"ActiveBloodStructureFeedClientData.getActiveFeeds()");
		assertContains("feed spiral throttles Abocipher particles", spiralParticles,
				"ABOCIPHER_INTERVAL_TICKS");
		assertContains("feed spiral throttles blood cell particles", spiralParticles,
				"BLOOD_CELL_INTERVAL_TICKS");
		assertContains("feed spiral spawns feed-tuned Abocipher particles", spiralParticles,
				"spawnAbocipher");
		assertContains("feed spiral spawns blood cell particles", spiralParticles,
				"spawnBloodCell");
		assertContains("feed spiral marks Abocipher particles as feed particles", spiralParticles,
				"AbocipherParticleFactory.FEED_SPIRAL_MARKER");
		assertContains("feed spiral uses visible feed blood cells", spiralParticles,
				"BloodCellParticleFactory.createFeedData(ParticleColor.BLOOD)");
		assertContains("feed spiral expands around structure bounds", spiralParticles,
				"bounds.radius()");
		assertContains("feed spiral computes a large spiral angle", spiralParticles,
				"spiralAngle");
		assertContains("Abocipher factory has a feed marker", abocipherFactory,
				"FEED_SPIRAL_MARKER");
		assertContains("Abocipher factory dispatches short feed particles", abocipherFactory,
				"new BloodStructureFeedAbocipherParticle");
		assertContains("feed Abocipher particle has short lifetime", feedAbocipherParticle,
				"LIFETIME_TICKS = 28");
		assertContains("feed Abocipher particle damps travel", feedAbocipherParticle,
				"this.xd *= DRIFT_DAMPING");
		assertContains("blood cell factory exposes feed data helper", bloodCellFactory,
				"createFeedData");
		assertContains("blood cell factory scales feed cells visibly", bloodCellFactory,
				"FEED_SCALE");
		assertContains("shader holder registered", shaderInit,
				"BLOOD_STRUCTURE_WARP = new ShaderHolder(Hemomancy.rloc(\"world/blood_structure_warp\")");
		assertContains("shader registered on reload", shaderInit,
				"registerShader(event, BLOOD_STRUCTURE_WARP.createInstance(provider));");
		assertContains("dynamic render type sets time", renderTypes,
				"setUniform(shader, \"HemoTime\"");
		assertContains("dynamic render type sets progress", renderTypes,
				"setUniform(shader, \"Progress\"");
		assertContains("dynamic render type sets coherent warp center", renderTypes,
				"setUniform(shader, \"WarpCenter\"");
		assertContains("dynamic render type uses warp shader", renderTypes,
				"ShaderInit.BLOOD_STRUCTURE_WARP.getShard()");
		assertContains("dynamic render type uses view offset layering to avoid z-fighting", renderTypes,
				"RenderType.VIEW_OFFSET_Z_LAYERING");
		assertContains("shader json references vertex program", shaderJson,
				"\"vertex\": \"hemomancy:world/blood_structure_warp\"");
		assertContains("shader json references fragment program", shaderJson,
				"\"fragment\": \"hemomancy:world/blood_structure_warp\"");
		assertContains("shader json exposes progress uniform", shaderJson,
				"\"name\": \"Progress\"");
		assertContains("shader json exposes warp center uniform", shaderJson,
				"\"name\": \"WarpCenter\"");
		assertContains("vertex shader wiggles geometry", vertexShader,
				"wiggleOffset");
		assertContains("vertex shader lifts geometry outward to avoid clipping", vertexShader,
				"surfaceLift");
		assertContains("vertex shader keeps shared cube edges coherent", vertexShader,
				"coherentWarpDirection");
		assertNotContains("vertex shader should not split shared vertices by face normal", vertexShader,
				"normalize(Normal");
		assertContains("fragment shader glows red", fragmentShader,
				"bloodGlow");
		assertContains("fragment shader uses non-directional mottle instead of slash streaks", fragmentShader,
				"mottleNoise");
		assertContains("fragment shader applies soft blood mottle", fragmentShader,
				"bloodMottle");
		assertNotContains("fragment shader should not draw diagonal vein streaks", fragmentShader,
				"veinNoise");
		assertNotContains("fragment shader should not key glow off diagonal uv sums", fragmentShader,
				"p.x + p.y");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": contained " + unexpected);
		}
	}
}

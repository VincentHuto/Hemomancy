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
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingKeyPressPacket.java"));
		String livingStaff = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String feedPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketBloodStructureFeed.java"));
		String feedManager = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/BloodStructureFeedManager.java"));
		String craftingHelper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodStructureCraftingHelper.java"));
		String visiblePositionRules = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodStructureVisiblePositionRules.java"));
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
		assertContains("projection transfer is server authoritative", projectionItem,
				"if (worldIn.isClientSide)");
		assertContains("projection use starts while player has blood", projectionItem,
				"volume.getBloodVolume() > 0");
		assertContains("key packet leaves normal Harbinger structures to projection", keyPacket,
				"!targetPattern.isUnstained()");
		assertContains("key packet keeps unstained structure path", keyPacket,
				"targetPattern.isUnstained()");
		assertContains("key packet hints projection use", keyPacket,
				"use Blood Projection");
		assertNotContains("puppeteer trials are no longer a Blood Crafting key path", keyPacket,
				"tryActivatePuppeteerTrial");
		assertContains("blood crafting key is restricted to unstained rite activation", keyPacket,
				"CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY");
		assertContains("living staff block use initiates Harbinger cardinal rites", livingStaff,
				"CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE");
		assertContains("feed manager uses current projection feed rate", feedManager,
				"BloodStructureFeedRules.STRUCTURE_FEED_RATE");
		assertContains("feed manager sends only visible pattern cells to the warp renderer", feedManager,
				"BloodStructureCraftingHelper.getVisibleMatchPositions");
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
		assertContains("feed manager gives completion melt time after collapse", feedManager,
				"COMPLETION_VISIBLE_TICKS = CRAFT_ANIMATION_TICKS + 22");
		assertContains("completion feed sync should linger instead of clearing immediately", feedManager,
				"sendFeedSync(level, positions, 1.0f, false, COMPLETION_VISIBLE_TICKS)");
		assertContains("feed manager consumes offhand catalyst in survival", feedManager,
				"offhandCatalyst.shrink(1)");
		assertContains("crafting helper exposes visible matched structure positions", craftingHelper,
				"getVisibleMatchPositions");
		assertContains("crafting helper filters wildcard air cells from feed visuals", craftingHelper,
				"BloodStructureVisiblePositionRules.isVisiblePatternCell");
		assertContains("visible position rules treat space cells as empty", visiblePositionRules,
				"return row.charAt(x) != ' '");
		assertContains("visible position rules bounds-check sparse pattern rows", visiblePositionRules,
				"x >= row.length()");
		assertContains("feed sync packet registered", packetHandler,
				"PacketBloodStructureFeed.TYPE");
		assertContains("feed packet targets client cache", feedPacket,
				"ActiveBloodStructureFeedClientData");
		assertContains("client feed cache tracks positions", clientData,
				"List<BlockPos>");
		assertContains("client feed cache remembers visible lifetime", clientData,
				"initialVisibleTicks");
		assertContains("client feed cache exposes completion fade progress", clientData,
				"getFinalizeProgress");
		assertContains("client feed cache only melts completion linger effects", clientData,
				"isCompletionLinger");
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
		assertContains("world renderer passes completion melt progress", renderer,
				"feed.getFinalizeProgress()");
		assertContains("world renderer passes the melt ground plane", renderer,
				"bounds.bottomY(camera)");
		assertContains("world renderer passes the structure melt height", renderer,
				"(float) bounds.height()");
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
		assertContains("dynamic render type sets finalizing melt", renderTypes,
				"setUniform(shader, \"FinalizeProgress\"");
		assertContains("dynamic render type sets melt ground", renderTypes,
				"setUniform(shader, \"MeltGroundY\"");
		assertContains("dynamic render type sets melt height", renderTypes,
				"setUniform(shader, \"MeltHeight\"");
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
		assertContains("shader json exposes finalizing melt uniform", shaderJson,
				"\"name\": \"FinalizeProgress\"");
		assertContains("shader json exposes melt ground uniform", shaderJson,
				"\"name\": \"MeltGroundY\"");
		assertContains("shader json exposes melt height uniform", shaderJson,
				"\"name\": \"MeltHeight\"");
		assertContains("vertex shader wiggles geometry", vertexShader,
				"wiggleOffset");
		assertContains("vertex shader lifts geometry outward to avoid clipping", vertexShader,
				"surfaceLift");
		assertContains("vertex shader keeps shared cube edges coherent", vertexShader,
				"coherentWarpDirection");
		assertContains("vertex shader melts completed overlay downward", vertexShader,
				"liquidMelt");
		assertContains("vertex shader uses the melt ground plane", vertexShader,
				"MeltGroundY");
		assertContains("vertex shader uses structure height for bottom-out melt", vertexShader,
				"MeltHeight");
		assertContains("vertex shader delays upper geometry collapse", vertexShader,
				"heightDelay");
		assertContains("vertex shader melts lower geometry first", vertexShader,
				"bottomOutMelt");
		assertContains("vertex shader grows the ground puddle first", vertexShader,
				"bottomPuddleSpread");
		assertContains("vertex shader spreads finalizing shell into a puddle", vertexShader,
				"puddleSpread");
		assertContains("vertex shader flattens finalizing shell to puddle thickness", vertexShader,
				"puddleThickness");
		assertContains("vertex shader expands puddle outward from structure center", vertexShader,
				"p.xz += outward * spread");
		assertContains("vertex shader blends bottom-out geometry into the puddle plane", vertexShader,
				"mix(p.y, targetY, bottomOutMelt)");
		assertNotContains("vertex shader should not collapse every height uniformly", vertexShader,
				"mix(p.y, targetY, liquidMelt)");
		assertNotContains("vertex shader should not split shared vertices by face normal", vertexShader,
				"normalize(Normal");
		assertContains("fragment shader glows red", fragmentShader,
				"bloodGlow");
		assertContains("fragment shader uses non-directional mottle instead of slash streaks", fragmentShader,
				"mottleNoise");
		assertContains("fragment shader applies soft blood mottle", fragmentShader,
				"bloodMottle");
		assertContains("fragment shader fades finalizing overlay", fragmentShader,
				"finalizeFade");
		assertContains("fragment shader waits to fade until puddle is formed", fragmentShader,
				"smoothstep(0.84, 1.0, FinalizeProgress)");
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

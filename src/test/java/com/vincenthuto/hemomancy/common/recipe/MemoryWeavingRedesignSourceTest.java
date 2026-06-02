package com.vincenthuto.hemomancy.common.recipe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MemoryWeavingRedesignSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private MemoryWeavingRedesignSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String recipe = read("src/main/java/com/vincenthuto/hemomancy/common/recipe/MemoryWeavingRecipe.java");
		String serializer = read("src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/MemoryWeavingRecipeSerializer.java");
		String loom = read("src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/SomaticLoomBlockEntity.java");
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomBlock.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/tile/crafting/SomaticLoomRenderer.java");
		String renderTypes = read("src/main/java/com/vincenthuto/hemomancy/common/init/RenderTypeInit.java");
		String hemoRenderTypes = read("src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
		String shaderInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
		String loomOrbShaderJson = read("src/main/resources/assets/hemomancy/shaders/core/world/loom_orb.json");
		String loomOrbVertexShader = read("src/main/resources/assets/hemomancy/shaders/core/world/loom_orb.vsh");
		String loomOrbFragmentShader = read("src/main/resources/assets/hemomancy/shaders/core/world/loom_orb.fsh");
		String enzyme = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/EnzymeItem.java");
		String loomEvents = read("src/main/java/com/vincenthuto/hemomancy/common/event/SomaticLoomInteractionEvents.java");
		String projectionEvents = read("src/main/java/com/vincenthuto/hemomancy/common/event/BloodProjectionInteractionEvents.java");
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java");
		String bareProjection = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");

		assertContains("recipe exposes required catalyst list", recipe, "getCatalysts()");
		assertContains("recipe exposes integer enzyme requirements", recipe, "getEnzymeRequirements()");
		assertContains("recipe validates max eight enzymes per tendency", recipe, "MAX_ENZYMES_PER_TENDENCY = 8");
		assertContains("recipe stores explicit blood cost", recipe, "bloodCost");
		assertContains("recipe computes orb count from enzyme amount", recipe, "getOrbCountForRequirement");
		assertContains("recipe creates one orb per required enzyme", recipe,
				"return Mth.clamp(requirement, 0, MAX_ENZYMES_PER_TENDENCY);");

		assertContains("serializer reads new catalysts field", serializer, "\"catalysts\"");
		assertContains("serializer reads new enzymes field", serializer, "\"enzymes\"");
		assertContains("serializer reads explicit blood field", serializer, "\"blood\"");
		assertContains("serializer keeps legacy ingredient fallback", serializer, "has(\"ingredient\")");

		assertContains("loom stores sixty-four enzymes per tendency", loom, "ENZYME_CAPACITY = 64");
		assertContains("loom has awaiting blood phase", loom, "PHASE_AWAITING_BLOOD");
		assertContains("loom has weaving orb phase", loom, "PHASE_WEAVING_ORBS");
		assertContains("loom tracks ritual blood charge", loom, "ritualBloodCharged");
		assertContains("loom stores ritual orb state", loom, "ritualOrbs");
		assertContains("loom uses mutable variable catalyst storage", loom, "createMutableContents");
		assertDoesNotContain("loom does not use fixed-size one-slot contents", loom,
				"contents = NonNullList.withSize(1");
		assertContains("loom deposits one enzyme normally or a shifted stack in bulk", loom,
				"depositEnzyme(player, stack, enzyme, player != null && player.isShiftKeyDown())");
		assertDoesNotContain("loom enzyme bulk deposit no longer uses crouching state", loom, "player.isCrouching()");
		assertContains("enzyme items let shift-click reach loom block use", enzyme, "doesSneakBypassUse");
		assertContains("enzyme items bypass sneak block-use suppression", enzyme, "return true;");
		assertContains("loom force-enables shifted enzyme block interaction", loomEvents, "event.setUseBlock(TriState.TRUE)");
		assertContains("loom shifted enzyme handler detects enzymes", loomEvents, "instanceof EnzymeItem");
		assertContains("loom shifted enzyme handler accepts filler clicks", loomEvents, "FillerBlockEntity");
		assertContains("loom supports staff orb dragging", loom, "dragSelectedOrb");
		assertContains("loom keeps a grabbed orb active while the staff is held", loom, "getActiveOrTargetedOrb(player)");
		assertContains("loom releases grabbed orbs when staff dragging stops", loom, "lastOrbDragGameTime");
		assertContains("loom drags grabbed orbs toward a player aim tether", loom, "findOrbDragTarget(player, orb, selected)");
		assertContains("loom captures a stable look-ray drag depth when grabbing an orb", loom, "captureDragDepth(player, worldPosition)");
		assertContains("loom smooths incoming synced orb corrections on the client", loom, "mergeSyncedRitualOrbs(orbs)");
		assertContains("loom blends server orb updates from the local visual position", loom, "adoptServerOffsetSmoothly");
		assertDoesNotContain("loom weaving return motion is not periodically snapped by server sync", loom,
				"self.tickOrbDrift();\r\n\t\t\tif (level.getGameTime() % 10L == 0L)");
		assertContains("loom clamps grabbed orbs within ritual bounds", loom, "clampOrbOffsetToRitualBounds");
		assertContains("loom bounds orb wandering to ten blocks in x and z", loom, "ORB_WANDER_XZ_RANGE = 10.0D");
		assertContains("loom bounds orb wandering to three blocks vertically", loom, "ORB_WANDER_Y_MAX = 3.0D");
		assertContains("idle loom orbs update bounded wander targets", loom,
				"updateWanderTargetWithinRitualBounds");
		assertContains("loom advances each wandering orb through one shared tick path", loom,
				"tickWanderingOrb(orb)");
		assertContains("loom syncs when a server orb chooses a new wander target", loom, "needsWanderSync");
		assertDoesNotContain("server orb wandering no longer double-steps through the old target updater", loom,
				"wanderWithinRitualBounds(serverLevel)");
		assertContains("client orb sync keeps renderable orbs server-authoritative", loom,
				"synced.adoptServerOffsetSmoothly(local);");
		assertDoesNotContain("client orb sync no longer preserves divergent non-active positions", loom,
				"adoptServerWanderTargetWithoutPositionSnap");
		assertContains("loom splits enzyme requirements into unit-cost orbs", loom, "costs.add(1);");
		assertDoesNotContain("loom no longer compresses expensive enzyme requirements into fewer orbs", loom,
				"Math.ceil(remaining / (double) i)");
		assertContains("loom orb drag uses a slower maximum step", loom, "ORB_DRAG_MAX_STEP = 0.055D");
		assertContains("released loom orbs keep wandering instead of snapping home", loom, "wanderTowardTarget()");
		assertContains("client loom tick advances orb visuals for stable interpolation", loom, "tickClientOrbMotion()");
		assertContains("loom syncs active drag ticks to prevent client-side release jitter", loom,
				"TAG_LAST_ORB_DRAG_GAME_TIME");
		assertContains("loom orbs retain previous positions for smooth rendering", loom, "previousOffset()");
		assertContains("loom orbs expose partial-tick render interpolation", loom, "renderOffset(float partialTicks)");
		assertContains("loom consumes orbs when they intersect the block bounds", loom, "isOrbWithinLoomAbsorptionBounds");
		assertContains("loom orb absorption uses inflated block bounds", loom, "ORB_ABSORPTION_MARGIN = 0.18D");
		assertContains("loom absorption gives immediate per-orb feedback", loom, "playOrbAbsorptionFeedback(orb)");
		assertContains("loom absorption sends a visible particle burst", loom, "sendParticles(ParticleTypes.POOF");
		assertDoesNotContain("loom orb absorption no longer snaps every remaining orb back to start", loom,
				"ritualOrbs.get(i).reset();");
		assertContains("loom dragged orbs emit colored particle trails", loom, "spawnOrbDragTrail(orb)");
		assertContains("loom orb particle effects use tendency color", loom, "createOrbDust(orb)");
		assertDoesNotContain("loom orb completion no longer requires center-point reach radius", loom,
				"distance <= ORB_REACH_RADIUS");
		assertDoesNotContain("loom orb drag no longer auto-seeks the center each staff tick", loom,
				"offset.subtract(offset.normalize().scale(step))");
		assertContains("loom consumes catalysts only on completion", loom, "consumeCatalystsForRecipe");
		assertContains("loom completion spawns memory result in-world", loom, "spawnMemoryResult");
		assertDoesNotContain("loom completion does not silently hide result in player inventory", loom,
				"player.getInventory().add(result.copy())");
		assertContains("loom renders recipe-ready glow state", loom, "isAwaitingBlood()");
		assertContains("loom renderer stays visible off screen", renderer, "shouldRenderOffScreen");
		assertContains("loom renderer has expanded renderer bounds", renderer, "getRenderBoundingBox");
		assertContains("loom renderer bounds include ritual orb radius", renderer, "ORB_RENDER_BOUNDS");
		assertContains("loom renderer uses shader-backed orb shell render type", renderer,
				"HemoRenderTypes.loomOrbShell");
		assertContains("loom renderer defers ritual orb buffer switching until orb drawing", renderer,
				"drawRitualOrbs(buffer, stack.last().pose(), te, currentTime, partialTicks)");
		assertContains("loom renderer cancels flat-effect y shift before drawing ritual orbs", renderer,
				"stack.translate(0F, 0.5F, 0F);");
		assertContains("loom renderer draws orb strands before switching to shader shell buffers", renderer,
				"drawRitualOrbEffects");
		assertContains("loom renderer draws shader shells in a separate pass", renderer, "drawRitualOrbShells");
		assertContains("loom renderer draws ritual orb shells through shader", renderer, "drawShaderWrithedOrbShell");
		assertContains("loom renderer uses per-orb shader batches for unique writhe", renderer,
				"HemoRenderTypes.loomOrbShell");
		assertContains("loom renderer passes a stable per-orb shader seed", renderer, "orbShaderSeed(orb)");
		assertContains("loom renderer still emits sphere geometry for shader deformation", renderer, "drawOrbSphere");
		assertContains("loom renderer interpolates ritual orb movement", renderer, "orb.renderOffset(partialTicks)");
		assertContains("loom renderer draws a subtle drag trail", renderer, "drawOrbTrail");
		assertContains("loom renderer draws center-to-orb weaving strands", renderer, "drawOrbCenterStrand");
		assertContains("loom renderer draws unraveled fabric strands around orbs", renderer, "drawOrbUnraveledStrands");
		assertContains("loom renderer uses straight endpoint strands for the reverted unraveled orb effect",
				renderer, "fracLine(vc, mat, sx, sy, sz, ex, ey, ez");
		assertContains("loom renderer uses a small swarm of unraveled strands", renderer, "ORB_UNRAVEL_STRANDS = 9");
		assertContains("loom renderer keeps orb strands thin", renderer, "ORB_CENTER_STRAND_WIDTH = 0.012f");
		assertDoesNotContain("loom unraveled strands no longer reseed from animation time", renderer, "flickerFrame");
		assertDoesNotContain("loom unraveled strand random seed does not include a time bucket", renderer, "+ 313L *");
		assertContains("loom unraveled strands use continuous strand-wave motion", renderer, "strandWave");
		assertContains("loom renderer tessellates ritual orb latitude bands", renderer, "ORB_LAT_SEGMENTS");
		assertDoesNotContain("loom renderer no longer draws ritual orbs as crossed planes", renderer,
				"drawOrbBillboard");
		assertContains("loom effect has a depth-writing core layer", renderTypes, "LOOM_EFFECT_CORE");
		assertContains("loom effect core writes depth for translucent-world ordering", renderTypes,
				"COLOR_DEPTH_WRITE");
		assertContains("loom orb shader core writes depth for translucent-world ordering", hemoRenderTypes,
				"glowLayer ? RenderType.COLOR_WRITE : RenderType.COLOR_DEPTH_WRITE");
		assertContains("loom orb shader is registered", shaderInit, "LOOM_ORB");
		assertContains("loom orb shader caches animation uniforms", shaderInit, "\"WritheStrength\"");
		assertContains("loom orb render type uses custom shader", hemoRenderTypes, "ShaderInit.LOOM_ORB.getShard()");
		assertContains("loom orb render type sets per-orb center uniform", hemoRenderTypes, "\"OrbCenter\"");
		assertContains("loom orb shader json points to vertex shader", loomOrbShaderJson, "\"vertex\": \"hemomancy:world/loom_orb\"");
		assertContains("loom orb shader json accepts position-color geometry", loomOrbShaderJson, "\"Position\"");
		assertContains("loom orb vertex shader writhes vertices outward from orb center", loomOrbVertexShader,
				"vec3 dir = normalize(Position - OrbCenter");
		assertContains("loom orb vertex shader uses stable seed-driven motion", loomOrbVertexShader, "OrbSeed");
		assertDoesNotContain("loom orb vertex shader does not use high-contrast knot deformation", loomOrbVertexShader,
				"float knot");
		assertContains("loom orb fragment shader renders thin stripe-style thread bands", loomOrbFragmentShader, "lineBand");
		assertContains("loom orb fragment shader keeps yarn stripes narrow", loomOrbFragmentShader, "threadWidth");
		assertDoesNotContain("loom orb fragment shader does not use broad filled region masks",
				loomOrbFragmentShader, "smoothstep(0.60, 0.96, abs(");
		assertContains("loom orb fragment shader preserves tendency color", loomOrbFragmentShader, "vertexColor.rgb");
		assertDoesNotContain("loom orb fragment shader does not paint sponge-like dark knots",
				loomOrbFragmentShader, "knotShadow");
		assertContains("input edits reset partial awaiting-blood progress", loom, "resetEditableRitualProgress()");
		assertContains("memory-only loom feedback asks for catalysts", loom, "Add catalyst items to shape the memory.");
		assertContains("bad catalyst pattern feedback is specific", loom, "No recipe accepts this catalyst pattern.");
		assertDoesNotContain("loom feedback no longer says no matching recipe found", loom, "No matching recipe found.");
		assertDoesNotContain("loom no longer fills a legacy blood tank from flasks", loom, "BloodyFlaskItem");
		assertDoesNotContain("loom no longer fills a legacy blood tank from gourds", loom, "BloodGourdItem");
		assertDoesNotContain("loom no longer exposes empty-hand projection helper", loom, "startHandProjection");
		assertDoesNotContain("loom no longer has an empty-hand projection rate", loom, "HAND_PROJECTION_RATE");
		assertContains("serializer rejects catalyst-free recipes", serializer,
				"Memory weaving recipes require at least one catalyst");

		assertDoesNotContain("empty hand does not project blood into the loom", block, "startHandProjection(player)");
		assertContains("shift empty hand uses modern shift state", block, "player.isShiftKeyDown()");
		assertDoesNotContain("shift empty hand no longer uses crouching state", block, "player.isCrouching()");
		assertDoesNotContain("empty hand feedback does not treat awaiting blood as locked crafting", block, "te.isCrafting()");
		assertDoesNotContain("loom item use is not blocked by generic crafting state", block,
				"if (te.isCrafting()) return InteractionResult.PASS");
		assertContains("active orb weaving passes held item behavior through", block, "te.isWeavingOrbs()");
		assertContains("projection tools pass through while loom awaits blood", block, "isProjectionTool(stack)");
		assertContains("projection tools skip default block feedback before item use", block,
				"ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION");
		assertContains("projection tool event disables blood tile block interaction", projectionEvents,
				"event.setUseBlock(TriState.FALSE)");
		assertContains("projection tool event allows item projection use", projectionEvents,
				"event.setUseItem(TriState.TRUE)");
		assertContains("projection tool event recognizes blood-volume tiles", projectionEvents,
				"HemoCapabilityAccess.getBloodVolume");
		assertContains("projection tool event recognizes filler-linked blood tiles", projectionEvents,
				"FillerBlockEntity");
		assertContains("living staff feeds loom orb dragging during held use", staff, "dragSelectedOrb");
		assertContains("bare projection detects loom ritual charging", bareProjection, "tryChargeRitualBlood");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + " (unexpected '" + forbidden + "')");
		}
	}
}

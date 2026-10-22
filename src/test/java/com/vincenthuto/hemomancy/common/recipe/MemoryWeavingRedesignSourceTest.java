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
		String loom = read("src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java");
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomBlock.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/SomaticLoomRenderer.java");
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

		String rules = read("src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/engram/EngramWeaveRules.java");
		assertContains("loom stores sixty-four enzymes' thread per tendency", loom, "ENZYME_CAPACITY = EngramWeaveRules.SPAN_CAPACITY");
		assertContains("a vial holds sixty-four enzymes of three spans", rules, "SPAN_CAPACITY = 64 * SPANS_PER_ENZYME");
		assertContains("loom has awaiting blood phase", loom, "PHASE_AWAITING_BLOOD");
		assertContains("loom has a weaving phase", loom, "PHASE_WEAVING");
		assertContains("loom tracks ritual blood charge", loom, "ritualBloodCharged");
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
		assertContains("loom absorption sends a visible particle burst", loom, "sendParticles(HemoParticleData.darkGlow(STRAND_SOOT)");
		// The client never resolves curRecipe; the echo and the warp must find the weave from the synced selection.
		assertContains("the weave's recipe resolves on the client from the synced selection", loom,
				"public MemoryWeavingRecipe getWeaveRecipe()");
		assertContains("the echo's manipulation comes from the client-resolvable weave recipe", loom,
				"MemoryWeavingRecipe recipe = getWeaveRecipe();");
		assertContains("the warp tints from the client-resolvable weave recipe", renderer,
				"weaving ? te.getWeaveRecipe() : te.getPreviewRecipe()");
		assertContains("payment unfurls the payer's own engram", loom, "engram.start(board)");
		assertContains("payment is gated by the payer's engram reference, not the recipe", loom,
				"EngramWeaveRules.shortTendency(storedSpans(), board.reference())");
		assertContains("the engram completes by the rules", loom, "EngramWeaveRules.complete(engram.board(), enzymeCounts(curRecipe))");
		assertContains("pausing lets go of the held lane", loom, "if (paused) engram.release();");
		assertContains("loom migrates pre-witnessed orb saves", loom, "TAG_LEGACY_RITUAL_ORBS");
		assertContains("loom migrates witnessed-weave saves", loom, "TAG_LEGACY_WEAVE_THREADS");
		for (String retired : new String[]{"frayDue", "recallLooseThreads", "weaveHome", "catchWithFeelers", "WeaveThread"})
			assertDoesNotContain("the witnessed weave's fight is retired: " + retired, loom, retired);
		assertContains("the living staff threads engrams", staff, "EngramTracing.begin(playerIn, false)");
		assertContains("a tracing staff opens no blood channel", staff, "EngramTracing.isTracing(player)");
		assertDoesNotContain("the living staff no longer hooks threads", staff, "lookedAtSlackThread");
		assertContains("the engram sigil shader is registered", shaderInit, "ENGRAM_SIGIL");
		assertContains("the loom draws its engram", renderer, "EngramSigilRenderer.render(te, partialTicks, stack, buffer, dissolve)");
		for (String engramClient : new String[]{"EngramSigilRenderer", "EngramSigilMesh", "EngramGlyphs", "EngramBeatEffects",
				"EngramAimClient"}) {
			String source = read("src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/engram/" + engramClient + ".java");
			assertDoesNotContain(engramClient + " uses Hemomancy/HutosLib particles, not vanilla ones", source, "ParticleTypes.");
			assertDoesNotContain(engramClient + " marks the staff's point with fine pixel motes, not the large animated hit burst",
					source, "hitGlow(");
		}
		assertDoesNotContain("the loom renderer uses no vanilla particles", renderer, "ParticleTypes.");
		assertDoesNotContain("loom no longer drags orbs at a fixed depth", loom, "dragSelectedOrb");
		assertDoesNotContain("loom instructions no longer teach the retired strand drag", loom, "captured distance");
		assertDoesNotContain("the loom tier refusal never floods chat while blood is channelled", loom, "msg(player, tierRequired(held), ChatFormatting.YELLOW, false)");
		assertContains("the loom tier refusal shows on the action bar", loom, "msg(player, tierRequired(held), ChatFormatting.YELLOW, true)");
		assertDoesNotContain("the living staff no longer scans for looms every tick", staff, "dragNearestLoomOrb");
		assertContains("loom consumes catalysts only on completion", loom, "consumeCatalystsForRecipe");
		assertContains("loom completion spawns memory result in-world", loom, "spawnMemoryResult");
		assertDoesNotContain("loom completion does not silently hide result in player inventory", loom,
				"player.getInventory().add(result.copy())");
		assertContains("loom renders recipe-ready glow state", loom, "isAwaitingBlood()");
		assertContains("loom renderer stays visible off screen", renderer, "shouldRenderOffScreen");
		assertContains("loom renderer has expanded renderer bounds", renderer, "getRenderBoundingBox");
		for (String yarnBall : new String[]{"loomOrbShell", "drawOrbSphere", "drawOrbUnraveledStrands", "fracLine",
				"FRAC_RAND", "ORB_UNRAVEL_STRANDS"})
			assertDoesNotContain("loom strands should not be drawn as writhing yarn balls: " + yarnBall, renderer,
					yarnBall);
		String threadRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/weave/FrayedThreadRenderer.java");
		assertContains("intrusions draw as knotted thread-ends", threadRenderer, "ThreadKnotGeometry.knot(");
		assertContains("frayed threads stay tethered to the echo", threadRenderer, "ThreadKnotGeometry.tether(");
		assertContains("frayed threads telegraph their attacks", threadRenderer, "thread.windup()");
		assertContains("a spent intrusion comes apart", threadRenderer, "thread.unravelling()");
		for (String yarnBall : new String[]{"loomOrbShell", "drawOrbSphere", "drawOrbUnraveledStrands", "fracLine"})
			assertDoesNotContain("frayed threads are not writhing yarn balls: " + yarnBall, threadRenderer, yarnBall);
		for (String weaveSource : new String[]{"FrayedThreadEntity", "ThreadBoltEntity", "ThreadHazardEntity", "ThreadAttacks"})
			assertDoesNotContain(weaveSource + " uses Hemomancy/HutosLib particles, not vanilla ones",
					read("src/main/java/com/vincenthuto/hemomancy/common/entity/weave/" + weaveSource + ".java"), "ParticleTypes.");
		assertDoesNotContain("thread visuals stay custom", threadRenderer, "ParticleTypes.");
		assertDoesNotContain("loom strands never run the loom dry mid-weave", loom, "The loom lacks");
		assertDoesNotContain("loom final strands never fray for want of enzyme", loom, "frays without enough enzyme");
		assertDoesNotContain("loom strands carry no drift surcharge the player cannot top up", loom, "driftPenalty");
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
		assertContains("memory-only loom feedback asks for catalysts", loom, "Add catalysts for a known recipe.");
		assertContains("valid catalyst patterns prompt output selection before blood payment", loom,
				"Use the loom with an empty hand to choose an output before paying blood.");
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
		assertContains("an active weave passes held item behavior through", block, "te.isWeaving()");
		assertContains("projection tools pass through while loom awaits blood", block, "isProjectionTool(stack)");
		assertContains("projection tools skip default block feedback before item use", block,
				"ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION");
		assertContains("projection tool event disables reservoir block interaction", projectionEvents,
				"event.setUseBlock(TriState.FALSE)");
		assertContains("projection tool event allows item projection use", projectionEvents,
				"event.setUseItem(TriState.TRUE)");
		assertContains("projection tool event recognizes blood reservoirs", projectionEvents,
				"HemoCapabilityAccess.getBloodVolume");
		assertContains("projection tool event recognizes filler-linked blood reservoirs", projectionEvents,
				"FillerBlockEntity");
		assertContains("bare projection hands blood to projected-blood sinks", bareProjection, "ProjectedBloodSink");
		assertContains("projection into the loom charges its ritual", loom,
				"if (!tryChargeRitualBlood(player, rate, livingStaff)) return 0.0D;");
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

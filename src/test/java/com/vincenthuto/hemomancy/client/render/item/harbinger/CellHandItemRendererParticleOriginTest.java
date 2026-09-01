package com.vincenthuto.hemomancy.client.render.item.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CellHandItemRendererParticleOriginTest {
	private static final Path EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/CellHandParticleEffects.java");
	private static final Path ITEM_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/CellHandItemRenderer.java");
	private static final Path THIRD_PERSON_LAYER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/CellHandLayer.java");
	private static final Path PROJECTION_ITEM = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");
	private static final Path BLOCK_INTERACTIONS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/shared/BlockBloodInteractions.java");
	private static final Path FORMATION_HANDLER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/SanguineFormationProjectionHandler.java");
	private static final Path CARDINAL_RITE_HANDLER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteInteractionHandler.java");

	@Test
	void projectionParticlesUseExplicitSurfaceEndpointsAndRenderedPalmOrigins() throws IOException {
		verifyProjectionParticleOrigin();
	}

	public static void main(String[] args) throws IOException {
		verifyProjectionParticleOrigin();
	}

	private static void verifyProjectionParticleOrigin() throws IOException {
		String effects = Files.readString(EFFECTS).replace("\r\n", "\n");
		String itemRenderer = Files.readString(ITEM_RENDERER).replace("\r\n", "\n");
		String thirdPersonLayer = Files.readString(THIRD_PERSON_LAYER).replace("\r\n", "\n");
		String projectionItem = Files.readString(PROJECTION_ITEM).replace("\r\n", "\n");
		String blockInteractions = Files.readString(BLOCK_INTERACTIONS).replace("\r\n", "\n");
		String formationHandler = Files.readString(FORMATION_HANDLER).replace("\r\n", "\n");
		String cardinalRiteHandler = Files.readString(CARDINAL_RITE_HANDLER).replace("\r\n", "\n");

		assertContains("projection particles configure an explicit source and target",
				effects, "particle.setProjectionPath(origin, trace.getLocation()");
		assertContains("projection particles use the shared gameplay reach",
				effects, "SanguineProjectionTargeting.PROJECTION_REACH");
		assertDoesNotContain("projection endpoints must not be lifted above the selected surface",
				effects, "trace.getLocation().add(0.0D, 1.05D, 0.0D)");
		assertOccurrences("only absorption may turn the animated hand into a particle destination",
				effects, "particle.setFirstPersonTargetAnchor(anchor);", 1);
		assertContains("first-person origin is captured from the rendered arm pose",
				itemRenderer, "CellHandRenderOrigin.fromPose");
		assertContains("third-person origin follows the parent model arm",
				thirdPersonLayer, "getParentModel().translateToHand(side, poseStack)");
		assertContains("third-person origin is captured from the rendered pose",
				thirdPersonLayer, "CellHandRenderOrigin.fromPose");
		assertDoesNotContain("third-person origin must not use a fixed body-yaw approximation",
				thirdPersonLayer, "double bodyYaw = Math.toRadians(living.yBodyRot)");
		assertSharedProjectionTargeting("blood projection fallback", projectionItem);
		assertSharedProjectionTargeting("block projection endpoints", blockInteractions);
		assertSharedProjectionTargeting("formation projection", formationHandler);
		assertSharedProjectionTargeting("cardinal rite projection", cardinalRiteHandler);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}

	private static void assertSharedProjectionTargeting(String label, String source) {
		assertContains(label + " uses the common ray", source, "SanguineProjectionTargeting.pick");
		assertContains(label + " uses the common reach", source, "SanguineProjectionTargeting.PROJECTION_REACH");
	}

	private static void assertOccurrences(String label, String text, String expected, int count) {
		int actual = 0;
		int offset = 0;
		while ((offset = text.indexOf(expected, offset)) >= 0) {
			actual++;
			offset += expected.length();
		}
		if (actual != count) {
			throw new AssertionError(label + ": expected " + count + " occurrences but got " + actual);
		}
	}
}

package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class CardinalRiteSigilFeedbackSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void correctAndFalseStrokeFeedbackAreConnectedToRuntimeState() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		String interactions = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteInteractionHandler.java");

		assertTrue(events.contains("CardinalRiteSigilRules.formingNodeRadius(nodeBlood)"),
				"partial correct-node blood must control the synchronized blob size");
		assertTrue(interactions.contains("spawnCorrectNodeFeedFeedback("),
				"each accepted feed tick must visibly grow the current node between client syncs");
		assertTrue(interactions.contains("spawnFalseStrokeBolts("),
				"false strokes must emit a visible lightning response");
		assertTrue(interactions.contains("LightningTesterSpawner.spawn"),
				"false-stroke feedback must use HutosLib's shared lightning renderer");
		assertTrue(interactions.contains("FALSE_STROKE_OUTER_BLACK"),
				"false-stroke lightning must retain its black outer arc");
		assertTrue(interactions.contains("FALSE_STROKE_INNER_PURPLE"),
				"false-stroke lightning must retain its purple core");
		assertFalse(interactions.contains("DaemonDiffuseGlowParticleFactory.createData(0.055F)"),
				"false-stroke feedback must not return to the diffuse particle stream");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}
}

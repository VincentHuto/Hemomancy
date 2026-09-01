package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperEveningStarPresentationRenderSourceTest {
	private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void lowHealthLinesUseDimTranslucencyAndAreSuppressedWhileShamed() throws Exception {
		String lines = read("client/render/layer/mob/endgame/VesperEveningStarLinesLayer.java");
		assertFalse(lines.contains("RenderType.eyes"));
		assertFalse(lines.contains("FULL_BRIGHT"));
		assertTrue(lines.contains("RenderType.entityTranslucentEmissive"));
		assertTrue(lines.contains("VesperEveningStarPresentationRules.shouldRenderRedLines"));
		assertTrue(lines.contains("VesperEveningStarPresentationRules.redLineAlpha"));
		assertTrue(lines.contains("RED_LINE_LIGHT"));
	}

	@Test
	void shamedBodyUsesAVesperSpecificMonolithShellAndSilentSeveranceDissolve() throws Exception {
		String renderer = read("client/render/entity/boss/endgame/VesperTheEveningStarRenderer.java");
		String shell = read("client/render/layer/mob/endgame/VesperShamedDissolutionLayer.java");
		assertTrue(renderer.contains("VesperShamedDissolutionLayer"));
		assertTrue(renderer.contains("absorptionScale"));
		assertTrue(renderer.contains("absorptionLowering"));
		assertTrue(renderer.contains("hermitFarewellDissolve"));
		assertTrue(shell.contains("monolithicDislocationShell"));
		assertTrue(shell.contains("entity.isAwaitingAbsorption()"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(JAVA.resolve(relative)).replace("\r\n", "\n");
	}
}

package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecimenJarRendererFitTest {
	@Test
	void prismCuttleUsesItsRenderedLengthInsteadOfItsCompactHitbox() {
		float footprint = SpecimenJarRenderer.longBodiedVisualLength("prism_cuttle", 0.7F, 0.55F);

		assertTrue(footprint >= 1.2F,
				"The five-segment tentacles make the rendered cuttle at least 1.2 blocks long");
	}

	@Test
	void prismCuttleSitsBehindTheJarFrontWithoutMovingOtherLongSpecimensBack() {
		assertTrue(SpecimenJarRenderer.specimenVisualDepthOffset("prism_cuttle", 0.7F) > 0.0D);
		assertTrue(SpecimenJarRenderer.specimenVisualDepthOffset("scarlet_serpent", 0.7F) < 0.0D);
	}
}

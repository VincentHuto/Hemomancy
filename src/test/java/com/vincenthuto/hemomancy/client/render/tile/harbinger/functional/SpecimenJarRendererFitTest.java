package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecimenJarRendererFitTest {
    @Test void iceFishFitsItsLongHeadAndTail() {
        assertTrue(SpecimenJarRenderer.longBodiedVisualLength("ice_fish", .5F, .35F) >= 1.25F,
                "Ice Fish must fit its snout and caudal fin inside the jar");
    }
    @Test void osteophageFitsWithWingsSpread() {
        assertTrue(SpecimenJarRenderer.longBodiedVisualLength("osteophage", .8F, 1.2F) >= 2.3F,
                "Captured Osteophage wings must remain inside the jar");
    }
    @org.junit.jupiter.api.Test void pelagicLongBodiesFitTheirActualVisualExtent() {
        org.junit.jupiter.api.Assertions.assertTrue(SpecimenJarRenderer.longBodiedVisualLength("siphonophore", .65F, 1.25F) >= 3.4F);
        org.junit.jupiter.api.Assertions.assertTrue(SpecimenJarRenderer.longBodiedVisualLength("hagfish", .6F, .3F) >= 1.8F);
        org.junit.jupiter.api.Assertions.assertTrue(SpecimenJarRenderer.longBodiedVisualLength("chiton", .65F, .22F) >= 1F);
        org.junit.jupiter.api.Assertions.assertTrue(SpecimenJarRenderer.longBodiedVisualLength("pelagic_herring", .45F, .32F) >= .85F);
    }
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

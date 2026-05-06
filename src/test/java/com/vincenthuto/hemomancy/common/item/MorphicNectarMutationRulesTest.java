package com.vincenthuto.hemomancy.common.item;

public final class MorphicNectarMutationRulesTest {
	private MorphicNectarMutationRulesTest() {
	}

	public static void main(String[] args) {
		assertTrue("explicit nectar marker shows mutation",
				MorphicNectarMutationRules.shouldShowMutation(true, false));
		assertTrue("primal morphling implies nectar mutation",
				MorphicNectarMutationRules.shouldShowMutation(false, true));
		assertFalse("unmarked non-primal item is not mutated",
				MorphicNectarMutationRules.shouldShowMutation(false, false));
		assertEquals("generic mutated border color", 0xAA516414,
				MorphicNectarMutationRules.borderColor(false));
		assertEquals("primal border color", 0xDDD13218,
				MorphicNectarMutationRules.borderColor(true));
		assertEquals("blood tendril color", 0xDDD13218,
				MorphicNectarMutationRules.tendrilColor(0));
		assertEquals("fungal tendril color", 0xCC60791C,
				MorphicNectarMutationRules.tendrilColor(1));
		assertEquals("bile tendril color", 0xFFE0B536,
				MorphicNectarMutationRules.tendrilColor(2));
		assertEquals("primal tendril count gives a layered organic silhouette", 5,
				MorphicNectarMutationRules.primalTendrilCount());
		assertEquals("primal tendril animation moves like the pylon and centrifuge screens", 55,
				MorphicNectarMutationRules.primalTendrilFrameMillis());
		assertEquals("primal tendrils use enough samples for curved bodies", 18,
				MorphicNectarMutationRules.primalTendrilSegments());
		assertEquals("texture-backed primal overlay has a longer animation cycle", 16,
				MorphicNectarMutationRules.primalOverlayFrameCount());
		assertEquals("texture-backed primal overlay uses high source resolution", 64,
				MorphicNectarMutationRules.primalOverlayFrameSize());
		assertEquals("texture-backed primal overlay has independent tendril sprites", 4,
				MorphicNectarMutationRules.primalOverlaySpriteCount());
		assertEquals("procedural primal tendrils use high-resolution curve samples", 42,
				MorphicNectarMutationRules.primalProceduralCurveSamples());
		assertEquals("procedural primal tendrils use layered glow, body, and highlight passes", 3,
				MorphicNectarMutationRules.primalProceduralLayerCount());
		assertEquals("procedural primal overlay does not paint a full-slot core mask", 0,
				MorphicNectarMutationRules.primalProceduralCoreFillAlpha());
		assertEquals("procedural primal overlay renders on the active gui plane", 0,
				MorphicNectarMutationRules.primalProceduralOverlayZ());
		assertTrue("procedural primal overlay uses the managed gui buffer",
				MorphicNectarMutationRules.primalProceduralUsesManagedGuiBuffer());
		assertTrue("procedural primal overlay uses the vanilla gui render type",
				MorphicNectarMutationRules.primalProceduralUsesVanillaGuiRenderType());
		assertTrue("procedural primal overlay uses vanilla gui quad winding",
				MorphicNectarMutationRules.primalProceduralUsesVanillaGuiQuadWinding());
		assertEquals("procedural primal body tendrils stay thin at inventory scale", 125,
				MorphicNectarMutationRules.primalProceduralBodyWidthCentipixels());
		assertEquals("procedural primal tendrils trim unstable root and tip samples", 8,
				MorphicNectarMutationRules.primalProceduralEndpointTrimPercent());
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}

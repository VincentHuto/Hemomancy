package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public final class SkillTreeLayerRulesTest {
	private SkillTreeLayerRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("degree zero belongs to surface", SkillTreeLayer.SURFACE, SkillTreeLayerRules.layerForDegree(0));
		assertEquals("degree four belongs to surface", SkillTreeLayer.SURFACE, SkillTreeLayerRules.layerForDegree(4));
		assertEquals("degree five belongs to deep", SkillTreeLayer.DEEP, SkillTreeLayerRules.layerForDegree(5));
		assertEquals("degree eight belongs to deep", SkillTreeLayer.DEEP, SkillTreeLayerRules.layerForDegree(8));

		assertFalse("degree four cannot enter deep tree", SkillTreeLayerRules.isDeepUnlocked(4));
		assertTrue("degree five can enter deep tree", SkillTreeLayerRules.isDeepUnlocked(5));

		assertNear("zoom below dive threshold stays on surface",
				0.0f, SkillTreeLayerRules.diveProgress(5, 1.7f, 1.0f), 0.001f);
		assertNear("zoom above dive threshold reaches deep tree",
				1.0f, SkillTreeLayerRules.diveProgress(5, 2.35f, 1.0f), 0.001f);
		assertNear("center focus scales dive progress",
				0.25f, SkillTreeLayerRules.diveProgress(5, 2.35f, 0.25f), 0.001f);
		assertNear("locked players cannot fade into the deep tree",
				0.0f, SkillTreeLayerRules.diveProgress(4, 2.35f, 1.0f), 0.001f);

		assertNear("viewport centered on the heart has full focus",
				1.0f, SkillTreeLayerRules.centerFocus(480, 360, 960, 720), 0.001f);
		assertNear("far-away heart has no focus",
				0.0f, SkillTreeLayerRules.centerFocus(40, 40, 960, 720), 0.001f);
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + " expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}

	private static void assertNear(String label, float expected, float actual, float tolerance) {
		if (Math.abs(expected - actual) > tolerance) {
			throw new AssertionError(label + " expected " + expected + " but got " + actual);
		}
	}
}

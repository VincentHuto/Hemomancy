package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public final class SkillTreeDiveStateTest {
	private SkillTreeDiveStateTest() {
	}

	public static void main(String[] args) {
		SkillTreeDiveState state = new SkillTreeDiveState();

		assertEquals("starts on surface", SkillTreeLayer.SURFACE, state.activeLayer());
		assertFalse("degree four cannot latch deep", state.updateSurfaceDive(4, 1.0f));
		assertEquals("degree four stays on surface", SkillTreeLayer.SURFACE, state.activeLayer());

		assertFalse("partial dive does not latch", state.updateSurfaceDive(5, 0.75f));
		assertEquals("partial dive stays surface", SkillTreeLayer.SURFACE, state.activeLayer());

		assertTrue("complete dive enters deep", state.updateSurfaceDive(5, 1.0f));
		assertEquals("deep layer stays active", SkillTreeLayer.DEEP, state.activeLayer());
		assertNear("latched deep remains fully visible", 1.0f, state.deepFade(0.0f), 0.001f);
		assertNear("entering deep starts transition pulse", 1.0f, state.transitionPulseProgress(), 0.001f);
		state.tickTransitionPulse();
		assertTrue("transition pulse decays after a tick", state.transitionPulseProgress() < 1.0f);

		assertFalse("normal deep zoom keeps deep active", state.updateDeepZoom(1.0f));
		assertEquals("panning/focus loss does not leave deep", SkillTreeLayer.DEEP, state.activeLayer());
		assertFalse("forty percent zoom still keeps deep active", state.updateDeepZoom(0.40f));
		assertEquals("deep remains active near the surface zoom floor", SkillTreeLayer.DEEP, state.activeLayer());

		assertTrue("zooming out far enough exits deep", state.updateDeepZoom(SkillTreeDiveState.DEEP_EXIT_ZOOM));
		assertEquals("returns to surface", SkillTreeLayer.SURFACE, state.activeLayer());
		assertNear("surface fade resumes from dive progress", 0.25f, state.deepFade(0.25f), 0.001f);
		assertNear("leaving deep starts transition pulse", 1.0f, state.transitionPulseProgress(), 0.001f);
		for (int i = 0; i < SkillTreeDiveState.TRANSITION_PULSE_TICKS; i++) {
			state.tickTransitionPulse();
		}
		assertNear("transition pulse eventually ends", 0.0f, state.transitionPulseProgress(), 0.001f);
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

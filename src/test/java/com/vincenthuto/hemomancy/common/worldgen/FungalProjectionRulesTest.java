package com.vincenthuto.hemomancy.common.worldgen;

public final class FungalProjectionRulesTest {
	private FungalProjectionRulesTest() {}

	public static void main(String[] args) {
		assertEquals("first projection lasts two minutes", 2400, FungalProjectionRules.FIRST_VISIT_TICKS);
		assertTrue("first visit starts a projection", FungalProjectionRules.shouldStartProjection(false, 7, true));
		assertFalse("witnessed players use normal travel", FungalProjectionRules.shouldStartProjection(true, 7, true));
		assertFalse("pre-Archons cannot project", FungalProjectionRules.shouldStartProjection(false, 6, true));
		assertFalse("a Spine is required", FungalProjectionRules.shouldStartProjection(false, 7, false));
		assertTrue("projection expires at zero", FungalProjectionRules.shouldForceReturn(0));
		assertFalse("positive time remains active", FungalProjectionRules.shouldForceReturn(1));
		assertTrue("sync cadence includes final second", FungalProjectionRules.shouldSync(20));
		assertFalse("sync cadence skips intermediate ticks", FungalProjectionRules.shouldSync(19));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) throw new AssertionError(label + ": expected " + expected + " but got " + actual);
	}
	private static void assertTrue(String label, boolean actual) {
		if (!actual) throw new AssertionError(label + ": expected true");
	}
	private static void assertFalse(String label, boolean actual) {
		if (actual) throw new AssertionError(label + ": expected false");
	}
}

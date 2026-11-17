package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay;
import org.junit.jupiter.api.Test;

public final class FungalProjectionRulesTest {
	private FungalProjectionRulesTest() {}

	@Test
	void clientProjectionExpiresAndClearsOnDisconnect() {
		FungalWhisperVignetteOverlay.clear();
		FungalWhisperVignetteOverlay overlay = new FungalWhisperVignetteOverlay();
		FungalWhisperVignetteOverlay.setProjectionState(true, 2, 2);
		assertTrue("projection starts active", FungalWhisperVignetteOverlay.isProjectionActive());
		overlay.tick();
		assertTrue("projection remains active before expiry", FungalWhisperVignetteOverlay.isProjectionActive());
		overlay.tick();
		assertFalse("projection expires at zero", FungalWhisperVignetteOverlay.isProjectionActive());
		FungalWhisperVignetteOverlay.setProjectionState(true, 20, 20);
		FungalWhisperVignetteOverlay.clear();
		assertFalse("logout clears the projection", FungalWhisperVignetteOverlay.isProjectionActive());
	}

	@Test
	void theFirstCrossingIsAProjectionAndEveryWayInIsDormantAfterIt() {
		org.junit.jupiter.api.Assertions.assertEquals(FungalProjectionRules.TravelKind.FIRST_PROJECTION,
				FungalProjectionRules.travelKind(false, 7, true));
		// D-27: after the first crossing the Podium lies dormant like the Spine, for an Apotheos too.
		org.junit.jupiter.api.Assertions.assertEquals(FungalProjectionRules.TravelKind.DORMANT,
				FungalProjectionRules.travelKind(true, 7, true));
		org.junit.jupiter.api.Assertions.assertEquals(FungalProjectionRules.TravelKind.DORMANT,
				FungalProjectionRules.travelKind(true, 8, true));
		org.junit.jupiter.api.Assertions.assertEquals(FungalProjectionRules.TravelKind.REFUSED,
				FungalProjectionRules.travelKind(false, 6, true));
		org.junit.jupiter.api.Assertions.assertEquals(FungalProjectionRules.TravelKind.REFUSED,
				FungalProjectionRules.travelKind(false, 7, false));
	}

	@Test
	void allChecksPass() {
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

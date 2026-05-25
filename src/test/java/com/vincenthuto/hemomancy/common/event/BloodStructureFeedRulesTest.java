package com.vincenthuto.hemomancy.common.event;

public final class BloodStructureFeedRulesTest {
	private BloodStructureFeedRulesTest() {
	}

	public static void main(String[] args) {
		feedsAtVisibleStructureRate();
		tileTransferKeepsCurrentProjectionRate();
		capsFeedToRemainingCost();
		capsFeedToAvailableBlood();
		doesNotFeedCompleteRecipe();
		expiresAfterTimeout();
		completionLockCoversCollapseWindow();
		fadeAlphaTracksRemainingResumeWindow();
	}

	private static void feedsAtVisibleStructureRate() {
		assertEquals("structure projection feed rate", 5.0,
				BloodStructureFeedRules.feedAmount(0.0, 400.0, 1000.0));
	}

	private static void tileTransferKeepsCurrentProjectionRate() {
		assertEquals("tile transfer rate stays at the current projection amount", 100.0,
				BloodStructureFeedRules.BLOOD_TILE_TRANSFER_RATE);
	}

	private static void capsFeedToRemainingCost() {
		assertEquals("exact completion should only drain remaining cost", 3.0,
				BloodStructureFeedRules.feedAmount(397.0, 400.0, 1000.0));
	}

	private static void capsFeedToAvailableBlood() {
		assertEquals("insufficient blood should drain only what exists", 3.0,
				BloodStructureFeedRules.feedAmount(200.0, 400.0, 3.0));
	}

	private static void doesNotFeedCompleteRecipe() {
		assertEquals("complete recipe should not keep draining", 0.0,
				BloodStructureFeedRules.feedAmount(400.0, 400.0, 1000.0));
	}

	private static void expiresAfterTimeout() {
		assertFalse("progress should remain at exactly the timeout window",
				BloodStructureFeedRules.isExpired(200L, 100L));
		assertTrue("progress should expire once the timeout window is exceeded",
				BloodStructureFeedRules.isExpired(201L, 100L));
	}

	private static void completionLockCoversCollapseWindow() {
		assertTrue("completion should stay locked through the collapse window",
				BloodStructureFeedRules.isCompletionLocked(130L, 130L));
		assertFalse("completion lock should release after its expiry tick",
				BloodStructureFeedRules.isCompletionLocked(131L, 130L));
	}

	private static void fadeAlphaTracksRemainingResumeWindow() {
		int resumeTicks = BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS;
		assertEquals("freshly fed structures should render at full opacity", 1.0f,
				BloodStructureFeedRules.resumeFadeAlpha(resumeTicks, resumeTicks, 0.0f));
		assertEquals("half of the resume window should render at half opacity", 0.5f,
				BloodStructureFeedRules.resumeFadeAlpha(resumeTicks / 2, resumeTicks, 0.0f));
		assertEquals("partial ticks should smooth the opacity between client ticks", 0.495f,
				BloodStructureFeedRules.resumeFadeAlpha(resumeTicks / 2, resumeTicks, 0.5f));
		assertEquals("expired structures should render fully transparent", 0.0f,
				BloodStructureFeedRules.resumeFadeAlpha(0, resumeTicks, 0.0f));
	}

	private static void assertEquals(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001) {
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

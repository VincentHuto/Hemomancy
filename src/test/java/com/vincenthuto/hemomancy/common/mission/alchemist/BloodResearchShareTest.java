package com.vincenthuto.hemomancy.common.mission.alchemist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** CL-12 with NQ-03-1 A and NQ-03-2: a draw from an unread kind gives half its Temper; a read kind gives all of it. */
class BloodResearchShareTest {
	@Test
	void anUnreadKindGivesHalfItsTemper() {
		assertEquals(0.5D, BloodResearch.UNREAD_SHARE);
		assertEquals(0.5D, BloodResearch.temperShare(false));
		assertEquals(1.0D, BloodResearch.temperShare(true));
	}
}

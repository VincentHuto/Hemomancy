package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class HarbingerChapterMilestoneTest {

	@Test
	void eachPublicRankAfterNeophyteRequiresExactlyThePreviousDegreesDefiningChapter() {
		assertEquals(HarbingerChapterMilestone.FIRST_BLOODCRAFT,
				HarbingerChapterMilestone.requiredForTargetDegree(2));
		assertEquals(HarbingerChapterMilestone.FIRST_SEPARATION,
				HarbingerChapterMilestone.requiredForTargetDegree(3));
		assertEquals(HarbingerChapterMilestone.WOVEN_VESSEL,
				HarbingerChapterMilestone.requiredForTargetDegree(4));
		assertEquals(HarbingerChapterMilestone.VEIN_MASON,
				HarbingerChapterMilestone.requiredForTargetDegree(5));
		assertEquals(HarbingerChapterMilestone.COVENANT_WRITTEN_IN_PLACE,
				HarbingerChapterMilestone.requiredForTargetDegree(6));
		assertEquals(HarbingerChapterMilestone.LIVING_COVENANT,
				HarbingerChapterMilestone.requiredForTargetDegree(7));
	}

	@Test
	void aRankIsLockedUntilItsSingleDefiningProofIsPresent() {
		EnumSet<HarbingerChapterMilestone> completed =
				EnumSet.of(HarbingerChapterMilestone.FIRST_BLOODCRAFT);

		assertTrue(HarbingerChapterMilestone.isRankUnlocked(2, completed));
		assertFalse(HarbingerChapterMilestone.isRankUnlocked(3, completed));
	}

	@Test
	void initiationAndApotheosisRemainOutsideThePublicChapterGate() {
		assertEquals(null, HarbingerChapterMilestone.requiredForTargetDegree(1));
		assertEquals(null, HarbingerChapterMilestone.requiredForTargetDegree(8));
		assertTrue(HarbingerChapterMilestone.isRankUnlocked(1, EnumSet.noneOf(HarbingerChapterMilestone.class)));
		assertTrue(HarbingerChapterMilestone.isRankUnlocked(8, EnumSet.noneOf(HarbingerChapterMilestone.class)));
	}
}

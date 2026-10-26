package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantControllerFlowTest {
	@Test
	void followUpDepthAndInterrupt() {
		assertTrue(MycophantController.mayFollowUp(0, false));
		assertTrue(MycophantController.mayFollowUp(1, false));
		assertFalse(MycophantController.mayFollowUp(2, false));
		assertFalse(MycophantController.mayFollowUp(0, true));
	}
}

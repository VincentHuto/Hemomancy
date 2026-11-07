package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumMeterStyleTest {
	@Test
	void theFrostReachesItsShareOfHalfTheVessel() {
		assertEquals(0.0F, VelorumMeterStyle.reach(0));
		assertEquals(30.0F, VelorumMeterStyle.reach(50), 1.0E-4F);
		assertEquals(VelorumMeterStyle.HALF, VelorumMeterStyle.reach(100), 1.0E-4F);
		assertEquals(VelorumMeterStyle.HALF, VelorumMeterStyle.reach(140), 1.0E-4F);
	}

	@Test
	void ticksAtColdAndStilling() {
		assertEquals(0, VelorumMeterStyle.ticks(39));
		assertEquals(1, VelorumMeterStyle.ticks(40));
		assertEquals(2, VelorumMeterStyle.ticks(70));
	}

	@Test
	void theScreenEdgeStaysFaint() {
		assertEquals(0.0F, VelorumMeterStyle.edgeAlpha(39));
		assertEquals(0.10F, VelorumMeterStyle.edgeAlpha(40), 1.0E-4F);
		assertEquals(0.25F, VelorumMeterStyle.edgeAlpha(99), 1.0E-4F);
		assertTrue(VelorumMeterStyle.edgeAlpha(100) <= 0.25F);
		assertTrue(VelorumMeterStyle.MUFFLE_HOUR > 0.0F, "darkness is sensory, never silence");
	}

	@Test
	void theFrameHasThreePhasesAndThreeNotches() {
		assertEquals(1, VelorumFrame.phaseOf(0.61F));
		assertEquals(2, VelorumFrame.phaseOf(0.6F));
		assertEquals(3, VelorumFrame.phaseOf(0.25F));
		assertEquals(3, new VelorumFrame().notches().length);
	}
}

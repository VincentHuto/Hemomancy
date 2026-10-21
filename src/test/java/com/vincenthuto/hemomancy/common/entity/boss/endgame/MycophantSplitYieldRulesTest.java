package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.util.function.IntToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantSplitYieldRulesTest {
	private static void assertWindow(IntToDoubleFunction curve, int start, int end, int length) {
		assertEquals(0.0, curve.applyAsDouble(start), 1.0E-5, "starts at 0");
		assertEquals(1.0, curve.applyAsDouble(end), 1.0E-5, "ends at 1");
		assertEquals(0.0, curve.applyAsDouble(-5), 1.0E-5);
		assertEquals(1.0, curve.applyAsDouble(length + 40), 1.0E-5);
		double last = -1.0;
		for (int t = -5; t <= length + 5; t++) {
			double v = curve.applyAsDouble(t);
			assertTrue(v >= last - 1.0E-6, "monotonic at " + t);
			assertTrue(v >= 0.0 && v <= 1.0);
			last = v;
		}
	}

	@Test
	void splitBeats() {
		assertEquals(80, MycophantSplitRules.length());
		assertEquals(40, MycophantSplitRules.pushTick());
		assertEquals(12, MycophantSplitRules.snapTick());
		assertWindow(MycophantSplitRules::crack, 0, 12, 80);
		assertWindow(MycophantSplitRules::halves, 12, 30, 80);
		assertWindow(MycophantSplitRules::droop, 15, 34, 80);
		assertWindow(MycophantSplitRules::pulp, 12, 42, 80);
		assertWindow(MycophantSplitRules::wings, 34, 70, 80);
		assertWindow(MycophantSplitRules::settle, 66, 80, 80);
	}

	@Test
	void theHalvesSnapOpenPastTheirRestThenSettleToHang() {
		assertEquals(0.0F, MycophantSplitRules.hinge(MycophantSplitRules.snapTick()), 1.0E-6F);
		assertEquals(0.0F, MycophantSplitRules.hinge(0.0F), 1.0E-6F);
		float peak = 0.0F;
		for (float t = 0.0F; t <= 80.0F; t += 0.25F) {
			float hinge = MycophantSplitRules.hinge(t);
			assertTrue(hinge >= 0.0F, "never closes past the seam at " + t);
			peak = Math.max(peak, hinge);
		}
		assertTrue(peak > 1.05F && peak < 1.25F, "a visible but bounded overshoot: " + peak);
		assertTrue(MycophantSplitRules.hinge(MycophantSplitRules.snapTick() + 5.0F) > 0.9F, "the snap is fast");
		assertEquals(1.0F, MycophantSplitRules.hinge(36.0F), 0.01F);
		assertEquals(1.0F, MycophantSplitRules.hinge(MycophantSplitRules.length()), 1.0E-3F);
	}

	@Test
	void theSplitBeatsLineUp() {
		// the pulp is at full bloom by the push's throb end, the wings open before she settles, the settle ends the split
		assertTrue(MycophantSplitRules.pulp(MycophantSplitRules.pushTick()) > 0.9F);
		assertEquals(1.0F, MycophantSplitRules.wings(70.0F), 1.0E-6F);
		assertEquals(0.0F, MycophantSplitRules.settle(66.0F), 1.0E-6F);
		assertEquals(1.0F, MycophantSplitRules.settle(MycophantSplitRules.length()), 1.0E-6F);
	}

	@Test
	void yieldBeats() {
		assertEquals(120, MycophantYieldRules.length());
		assertWindow(MycophantYieldRules::fold, 0, 20, 120);
		assertWindow(MycophantYieldRules::lower, 20, 45, 120);
		assertWindow(MycophantYieldRules::offer, 40, 75, 120);
		assertWindow(MycophantYieldRules::sink, 70, 120, 120);
	}
}

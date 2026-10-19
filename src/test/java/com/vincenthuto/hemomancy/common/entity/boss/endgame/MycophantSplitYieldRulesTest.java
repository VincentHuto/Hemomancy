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
		assertWindow(MycophantSplitRules::crack, 0, 10, 80);
		assertWindow(MycophantSplitRules::halves, 10, 35, 80);
		assertWindow(MycophantSplitRules::pulp, 25, 50, 80);
		assertWindow(MycophantSplitRules::wings, 35, 70, 80);
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

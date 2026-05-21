package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.MonolithFragmentDropRules;

public final class MonolithFragmentDropRulesTest {

	private MonolithFragmentDropRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("minimum fragment drop", 5, MonolithFragmentDropRules.MIN_FRAGMENTS);
		assertEquals("maximum fragment drop", 8, MonolithFragmentDropRules.MAX_FRAGMENTS);
		assertEquals("roll zero yields minimum", 5, MonolithFragmentDropRules.fragmentCountFromRoll(0));
		assertEquals("roll one advances count", 6, MonolithFragmentDropRules.fragmentCountFromRoll(1));
		assertEquals("roll two advances count", 7, MonolithFragmentDropRules.fragmentCountFromRoll(2));
		assertEquals("roll three reaches maximum", 8, MonolithFragmentDropRules.fragmentCountFromRoll(3));
		assertEquals("roll four wraps to minimum", 5, MonolithFragmentDropRules.fragmentCountFromRoll(4));
		assertEquals("negative rolls still stay in the inclusive range", 8, MonolithFragmentDropRules.fragmentCountFromRoll(-1));

		for (int roll = -64; roll <= 64; roll++) {
			int count = MonolithFragmentDropRules.fragmentCountFromRoll(roll);
			if (count < MonolithFragmentDropRules.MIN_FRAGMENTS || count > MonolithFragmentDropRules.MAX_FRAGMENTS) {
				throw new AssertionError("fragment count out of range for roll " + roll + ": " + count);
			}
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

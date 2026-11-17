package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MonolithFragmentDropRules;

public final class MonolithFragmentDropRulesTest {

	private MonolithFragmentDropRulesTest() {
	}

	@Test
	void allChecksPass() {
		assertEquals("NQ-12-1: every shatter gives eight fragments", 8, MonolithFragmentDropRules.FRAGMENTS);
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

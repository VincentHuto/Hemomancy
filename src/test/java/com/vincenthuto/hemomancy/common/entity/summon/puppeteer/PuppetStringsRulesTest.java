package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PuppetStringsRulesTest {
	@Test
	void arrivingBodyHangsWhileStringsFallThenIsLoweredAndSettles() {
		assertEquals(PuppetStringsRules.DESCEND_HEIGHT, PuppetStringsRules.descendOffset(0.0D), 1.0E-9D);
		assertEquals(PuppetStringsRules.DESCEND_HEIGHT, PuppetStringsRules.descendOffset(PuppetStringsRules.DROP_END - 0.01D), 1.0E-9D);
		double previous = PuppetStringsRules.DESCEND_HEIGHT;
		for (double age = PuppetStringsRules.DROP_END; age <= PuppetStringsRules.LOWER_END; age += 0.5D) {
			double offset = PuppetStringsRules.descendOffset(age);
			assertTrue(offset <= previous + 1.0E-9D, "the body only ever comes down while lowered");
			previous = offset;
		}
		assertEquals(0.0D, PuppetStringsRules.descendOffset(PuppetStringsRules.LOWER_END), 1.0E-9D);
		assertTrue(PuppetStringsRules.descendOffset(11.0D) < 0.0D, "it dips onto its feet before settling");
		assertEquals(0.0D, PuppetStringsRules.descendOffset(PuppetStringsRules.DESCEND_TICKS), 1.0E-9D);
		assertEquals(1.0D, PuppetStringsRules.descendScale(PuppetStringsRules.LOWER_END), 1.0E-9D);
		assertEquals(1.0D, PuppetStringsRules.stringReach(PuppetStringsRules.DROP_END), 1.0E-9D);
		assertEquals(0.0D, PuppetStringsRules.knit(2.0D), 1.0E-9D);
		assertEquals(1.0D, PuppetStringsRules.knit(PuppetStringsRules.LOWER_END), 1.0E-9D);
		assertEquals(0.0D, PuppetStringsRules.descendFray(PuppetStringsRules.LOWER_END), 1.0E-9D);
		assertEquals(1.0D, PuppetStringsRules.descendFray(PuppetStringsRules.DESCEND_TICKS), 1.0E-9D);
	}

	@Test
	void departingBodyIsHeldTautThenHauledUpShrinkingUnderTheFlash() {
		assertEquals(0.0D, PuppetStringsRules.taut(0.0D), 1.0E-9D);
		assertTrue(PuppetStringsRules.taut(2.25D) > 1.0D, "the strings overshoot as they snap");
		assertEquals(1.0D, PuppetStringsRules.taut(PuppetStringsRules.SNAP_END), 1.0E-9D);
		assertEquals(0.0D, PuppetStringsRules.unspoolLift(PuppetStringsRules.SNAP_END - 0.01D), 1.0E-9D);
		double previous = 0.0D;
		for (double age = PuppetStringsRules.SNAP_END; age <= PuppetStringsRules.UNSPOOL_TICKS; age += 0.5D) {
			double lift = PuppetStringsRules.unspoolLift(age);
			assertTrue(lift >= previous, "the haul never drops the body back");
			previous = lift;
		}
		assertEquals(PuppetStringsRules.HAUL_HEIGHT, PuppetStringsRules.unspoolLift(PuppetStringsRules.HAUL_END), 1.0E-9D);
		assertEquals(1.0D, PuppetStringsRules.unspoolScale(0.0D), 1.0E-9D);
		assertTrue(PuppetStringsRules.unspoolScale(PuppetStringsRules.HAUL_END) < 0.2D, "almost nothing is left to pop");
		assertTrue(PuppetStringsRules.flash(PuppetStringsRules.HAUL_END + 1.0D) > 0.5D,
				"the flash peaks as the shrunken body reaches its strings");
		assertEquals(0.0D, PuppetStringsRules.flash(PuppetStringsRules.SNAP_END), 1.0E-9D);
		assertTrue(PuppetStringsRules.UNSPOOL_TICKS > PuppetStringsRules.HAUL_END,
				"the server keeps the shell until the haul has finished");
	}

	@Test
	void controlThreadLetsGoAndStringCountStaysInRange() {
		assertEquals(1.0D, PuppetStringsRules.controlThreadFade(0.0D), 1.0E-9D);
		assertEquals(0.0D, PuppetStringsRules.controlThreadFade(PuppetStringsRules.HAUL_END), 1.0E-9D);
		assertEquals(3, PuppetStringsRules.strings(1));
		assertEquals(4, PuppetStringsRules.strings(4));
		assertEquals(5, PuppetStringsRules.strings(9));
	}
}

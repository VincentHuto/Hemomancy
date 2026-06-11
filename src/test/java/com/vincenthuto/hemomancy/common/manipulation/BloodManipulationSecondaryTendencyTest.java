package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;

public final class BloodManipulationSecondaryTendencyTest {
	private BloodManipulationSecondaryTendencyTest() {
	}

	public static void main(String[] args) {
		secondaryTendencyIsOptionalAndFluent();
	}

	private static void secondaryTendencyIsOptionalAndFluent() {
		BloodManipulation manip = newManip();
		assertNull("new manipulation has no secondary tendency", manip.getSecondaryTend());
		assertSame("setter is fluent", manip, manip.setSecondaryTend(EnumBloodTendency.LUX));
		assertEquals("secondary tendency stored", EnumBloodTendency.LUX, manip.getSecondaryTend());
	}

	private static BloodManipulation newManip() {
		return new BloodManipulation("test_manip", 10, 0, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + " (expected " + expected + ", got " + actual + ")");
		}
	}

	private static void assertNull(String label, Object value) {
		if (value != null) {
			throw new AssertionError(label + " (expected null, got " + value + ")");
		}
	}

	private static void assertSame(String label, Object expected, Object actual) {
		if (expected != actual) {
			throw new AssertionError(label);
		}
	}
}

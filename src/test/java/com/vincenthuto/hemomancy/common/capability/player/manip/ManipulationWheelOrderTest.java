package com.vincenthuto.hemomancy.common.capability.player.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationWheelOrder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class ManipulationWheelOrderTest {
	private ManipulationWheelOrderTest() {
	}

	@Test
	void wheelUsesMemorizedOrderAndRetainsKnownIndices() {
		BloodManipulation firstKnown = manipulation("first_known");
		BloodManipulation secondKnown = manipulation("second_known");
		BloodManipulation thirdKnown = manipulation("third_known");

		var ordered = ManipulationWheelOrder.resolve(
				List.of(firstKnown, secondKnown, thirdKnown),
				List.of("blood_absorption", "third_known", "first_known"));

		assertEquals("wheel count", 2, ordered.size());
		assertEquals("first wheel entry follows memorized order", thirdKnown, ordered.get(0).manipulation());
		assertEquals("first wheel entry keeps selection index", 2, ordered.get(0).knownIndex());
		assertEquals("second wheel entry follows memorized order", firstKnown, ordered.get(1).manipulation());
		assertEquals("second wheel entry keeps selection index", 0, ordered.get(1).knownIndex());
	}

	private static BloodManipulation manipulation(String name) {
		return new BloodManipulation(name, 10, 0, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

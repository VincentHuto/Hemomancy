package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;

import java.util.EnumMap;
import java.util.Map;

public final class VascularPoulticeRulesTest {
	private VascularPoulticeRulesTest() {
	}

	public static void main(String[] args) {
		selectsMostDamagedSection();
		healsMostDamagedSectionWithoutExceedingMaximum();
		healsTwoDistinctDamagedSections();
		refusesAlreadyHealthySystems();
	}

	private static void selectsMostDamagedSection() {
		Map<EnumVeinSections, Float> vascular = healthySystem();
		vascular.put(EnumVeinSections.ARMS, 38F);
		vascular.put(EnumVeinSections.LEGS, 12F);
		vascular.put(EnumVeinSections.HEAD, 64F);

		assertEquals("most damaged section", EnumVeinSections.LEGS,
				VascularPoulticeRules.mostDamagedSection(vascular).orElseThrow());
	}

	private static void healsMostDamagedSectionWithoutExceedingMaximum() {
		Map<EnumVeinSections, Float> vascular = healthySystem();
		vascular.put(EnumVeinSections.HEART, 92F);

		VascularPoulticeRules.HealResult result = VascularPoulticeRules.healMostDamaged(vascular, 12F).orElseThrow();

		assertEquals("healed section", EnumVeinSections.HEART, result.section());
		assertDouble("healed amount is capped", 8.0, result.amountHealed());
		assertDouble("section is capped at maximum", 100.0, vascular.get(EnumVeinSections.HEART));
	}

	private static void refusesAlreadyHealthySystems() {
		if (!VascularPoulticeRules.healTwoMostDamaged(healthySystem(), 12F).isEmpty()) {
			throw new AssertionError("healthy vascular systems should not consume poultices");
		}
	}

	private static void healsTwoDistinctDamagedSections() {
		Map<EnumVeinSections, Float> vascular = healthySystem();
		vascular.put(EnumVeinSections.LEGS, 20F);
		vascular.put(EnumVeinSections.ARMS, 40F);
		vascular.put(EnumVeinSections.HEAD, 50F);

		var results = VascularPoulticeRules.healTwoMostDamaged(vascular, 12F);

		assertEquals("two sections healed", 2, results.size());
		assertEquals("worst section healed first", EnumVeinSections.LEGS, results.get(0).section());
		assertEquals("second-worst section also healed", EnumVeinSections.ARMS, results.get(1).section());
		assertDouble("leg healing", 32.0, vascular.get(EnumVeinSections.LEGS));
		assertDouble("arm healing", 52.0, vascular.get(EnumVeinSections.ARMS));
		assertDouble("third section untouched", 50.0, vascular.get(EnumVeinSections.HEAD));
	}

	private static Map<EnumVeinSections, Float> healthySystem() {
		Map<EnumVeinSections, Float> vascular = new EnumMap<>(EnumVeinSections.class);
		for (EnumVeinSections section : EnumVeinSections.values()) {
			vascular.put(section, 100F);
		}
		return vascular;
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

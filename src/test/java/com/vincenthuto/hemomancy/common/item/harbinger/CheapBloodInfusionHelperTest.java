package com.vincenthuto.hemomancy.common.item.harbinger;

public final class CheapBloodInfusionHelperTest {
	private CheapBloodInfusionHelperTest() {
	}

	public static void main(String[] args) {
		assertEquals("first infusion amplifier", 0,
				CheapBloodInfusionRules.nextAmplifier(-1));
		assertEquals("second infusion amplifier", 1,
				CheapBloodInfusionRules.nextAmplifier(0));
		assertEquals("amplifier caps at fourth dose", 3,
				CheapBloodInfusionRules.nextAmplifier(3));
		assertEquals("amplifier remains capped", 3,
				CheapBloodInfusionRules.nextAmplifier(9));

		assertEquals("no drunkenness cost multiplier", 1.0,
				CheapBloodInfusionRules.getManipulationCostMultiplier(-1));
		assertEquals("amplifier zero cost multiplier", 1.15,
				CheapBloodInfusionRules.getManipulationCostMultiplier(0));
		assertEquals("amplifier three cost multiplier", 1.60,
				CheapBloodInfusionRules.getManipulationCostMultiplier(3));
		assertEquals("overcap cost multiplier clamps", 1.60,
				CheapBloodInfusionRules.getManipulationCostMultiplier(8));

		assertEquals("nonmax cooldown multiplier", 1.0,
				CheapBloodInfusionRules.getManipulationCooldownMultiplier(2));
		assertEquals("max cooldown multiplier", 1.25,
				CheapBloodInfusionRules.getManipulationCooldownMultiplier(3));
		assertEquals("overcap cooldown multiplier clamps", 1.25,
				CheapBloodInfusionRules.getManipulationCooldownMultiplier(8));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, double expected, double actual) {
		if (Double.compare(expected, actual) != 0) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

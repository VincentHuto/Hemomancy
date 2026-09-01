package com.vincenthuto.hemomancy.common.item.harbinger.tool;

public final class TerrestrialSpeculumRulesTest {
	private TerrestrialSpeculumRulesTest() {
	}

	public static void main(String[] args) {
		manifestationRequiresProgressionBloodDestinationAndSpace();
		travelRequiresOwnedNearbyAnchorsAndEnoughBlood();
	}

	private static void manifestationRequiresProgressionBloodDestinationAndSpace() {
		assertTrue("valid manifestation",
				TerrestrialSpeculumRules.canManifest(2, true, 1, true));
		assertFalse("degree one blocked",
				TerrestrialSpeculumRules.canManifest(1, true, 1, true));
		assertFalse("inactive blood blocked",
				TerrestrialSpeculumRules.canManifest(2, false, 1, true));
		assertFalse("no claimed destinations blocked",
				TerrestrialSpeculumRules.canManifest(2, true, 0, true));
		assertFalse("occupied manifestation space blocked",
				TerrestrialSpeculumRules.canManifest(2, true, 1, false));
	}

	private static void travelRequiresOwnedNearbyAnchorsAndEnoughBlood() {
		assertTrue("valid travel",
				TerrestrialSpeculumRules.canTravel(true, true, 16.0D, true, true, 1_000.0D));
		assertFalse("speculum must remain held",
				TerrestrialSpeculumRules.canTravel(false, true, 16.0D, true, true, 1_000.0D));
		assertFalse("foreign manifestation blocked",
				TerrestrialSpeculumRules.canTravel(true, false, 16.0D, true, true, 1_000.0D));
		assertFalse("distant manifestation blocked",
				TerrestrialSpeculumRules.canTravel(true, true, 64.01D, true, true, 1_000.0D));
		assertFalse("unclaimed destination blocked",
				TerrestrialSpeculumRules.canTravel(true, true, 16.0D, false, true, 1_000.0D));
		assertFalse("unstented destination blocked",
				TerrestrialSpeculumRules.canTravel(true, true, 16.0D, true, false, 1_000.0D));
		assertFalse("insufficient blood blocked",
				TerrestrialSpeculumRules.canTravel(true, true, 16.0D, true, true, 999.0D));
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}
}

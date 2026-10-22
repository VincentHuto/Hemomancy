package com.vincenthuto.hemomancy.common.item.harbinger.tool;

public final class TerrestrialSpeculumRulesTest {
	private TerrestrialSpeculumRulesTest() {
	}

	public static void main(String[] args) {
		manifestationRequiresProgressionBloodDestinationAndSpace();
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

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}
}

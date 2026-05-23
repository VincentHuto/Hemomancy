package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

public final class VasculariumCharmRulesTest {
	private VasculariumCharmRulesTest() {
	}

	public static void main(String[] args) {
		assertFalse("equipped vascularium charms are not emitted from scar death drops",
				VasculariumCharmRules.shouldDropEquippedScarSlot(true));
		assertTrue("non-vascularium equipped scars still follow normal death drops",
				VasculariumCharmRules.shouldDropEquippedScarSlot(false));

		assertFalse("the charm cannot be removed from an unvalidated equipment menu",
				VasculariumCharmRules.canRemoveFromEquipmentMenu(false));
		assertTrue("the charm can be removed from a validated Scarlet Vanity menu",
				VasculariumCharmRules.canRemoveFromEquipmentMenu(true));
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}

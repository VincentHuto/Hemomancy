package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

public final class VasculariumCharmRulesTest {
	private VasculariumCharmRulesTest() {
	}

	public static void main(String[] args) {
		assertFalse("equipped vascularium charms stay in Scarlet Vanity through death",
				VasculariumCharmRules.shouldDropEquippedSlot(true));
		assertFalse("all other Scarlet Vanity equipment stays through death",
				VasculariumCharmRules.shouldDropEquippedSlot(false));

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

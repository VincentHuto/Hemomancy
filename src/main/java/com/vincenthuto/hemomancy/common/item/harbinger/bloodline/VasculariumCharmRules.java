package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

public final class VasculariumCharmRules {
	private VasculariumCharmRules() {
	}

	/**
	 * Scarlet Vanity equipment is attached to the player and is retained through
	 * death. Keep this compatibility method for older call sites.
	 */
	public static boolean shouldDropEquippedSlot(boolean isVasculariumCharm) {
		return false;
	}

	public static boolean canRemoveFromEquipmentMenu(boolean openedFromScarletVanity) {
		return openedFromScarletVanity;
	}
}

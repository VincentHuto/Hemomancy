package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

public final class VasculariumCharmRules {
	private VasculariumCharmRules() {
	}

	public static boolean shouldDropEquippedScarSlot(boolean isVasculariumCharm) {
		return !isVasculariumCharm;
	}

	public static boolean canRemoveFromEquipmentMenu(boolean openedFromScarletVanity) {
		return openedFromScarletVanity;
	}
}

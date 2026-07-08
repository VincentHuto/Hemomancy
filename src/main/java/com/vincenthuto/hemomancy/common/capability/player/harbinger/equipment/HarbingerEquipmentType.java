package com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment;

public enum HarbingerEquipmentType {
	VASC(5), GOURD(6), JAR(7), FITTING(8);

	int[] validSlots;

	HarbingerEquipmentType(int... validSlots) {
		this.validSlots = validSlots;
	}

	public int[] getValidSlots() {
		return validSlots;
	}

	public boolean hasSlot(int slot) {
		for (int s : validSlots) {
			if (s == slot)
				return true;
		}
		return false;
	}

}

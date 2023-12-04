package com.vincenthuto.hemomancy.common.capability.player.rune;

public enum RuneType {
	CONTRACT(0), RUNE(1, 2, 3), OVERRIDE(0, 1, 2, 3), VASC(4), GOURD(5);

	int[] validSlots;

	RuneType(int... validSlots) {
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
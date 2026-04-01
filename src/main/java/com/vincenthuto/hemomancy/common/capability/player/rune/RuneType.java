package com.vincenthuto.hemomancy.common.capability.player.rune;

public enum RuneType {
	FUNGAL(0), RUNE(1, 2, 3, 4), OVERRIDE(0, 1, 2, 3), VASC(5), GOURD(6), JAR(7);

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

	/**
	 * Parses a RuneType from user/data strings, supporting legacy names.
	 */
	public static RuneType fromString(String raw) {
		if (raw == null)
			return OVERRIDE;
		String s = raw.trim().toUpperCase();
		if (s.equals("CONTRACT"))
			return FUNGAL;
		try {
			return RuneType.valueOf(s);
		} catch (IllegalArgumentException ex) {
			return OVERRIDE;
		}
	}
}
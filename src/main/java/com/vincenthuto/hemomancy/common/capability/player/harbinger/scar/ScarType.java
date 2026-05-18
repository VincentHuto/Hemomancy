package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

public enum ScarType {
	FUNGAL(0), SCAR(1, 2, 3, 4), OVERRIDE(0, 1, 2, 3), VASC(5), GOURD(6), JAR(7);

	int[] validSlots;

	ScarType(int... validSlots) {
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
	 * Parses a ScarType from user/data strings, supporting legacy names.
	 */
	public static ScarType fromString(String raw) {
		if (raw == null)
			return OVERRIDE;
		String s = raw.trim().toUpperCase();
		if (s.equals("CONTRACT"))
			return FUNGAL;
		try {
			return ScarType.valueOf(s);
		} catch (IllegalArgumentException ex) {
			return OVERRIDE;
		}
	}
}
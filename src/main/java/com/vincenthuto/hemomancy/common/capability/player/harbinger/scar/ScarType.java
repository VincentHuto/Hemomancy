package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

public enum ScarType {
	CEREBRAL, FUNGAL;

	/**
	 * Parses a scar-domain type from user/data strings, supporting legacy recipe names.
	 */
	public static ScarType fromString(String raw) {
		if (raw == null)
			return CEREBRAL;
		String s = raw.trim().toUpperCase();
		if (s.equals("CONTRACT"))
			return FUNGAL;
		if (s.equals("SCAR"))
			return CEREBRAL;
		try {
			return ScarType.valueOf(s);
		} catch (IllegalArgumentException ex) {
			return CEREBRAL;
		}
	}
}

package com.vincenthuto.hemomancy.common.rite;

public enum CardinalRiteCeremonyProfile {
	SIMPLE,
	STANDARD,
	CEREMONIAL;

	public static CardinalRiteCeremonyProfile byName(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Cardinal rite ceremony profile is required");
		}
		try {
			return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unknown cardinal rite ceremony profile: " + value, exception);
		}
	}
}

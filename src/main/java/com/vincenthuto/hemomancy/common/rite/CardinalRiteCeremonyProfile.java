package com.vincenthuto.hemomancy.common.rite;

public enum CardinalRiteCeremonyProfile {
	FULL,
	ABBREVIATED;

	public static CardinalRiteCeremonyProfile byName(String value) {
		return "abbreviated".equalsIgnoreCase(value) ? ABBREVIATED : FULL;
	}
}

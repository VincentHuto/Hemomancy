package com.vincenthuto.hemomancy.common.rite;

/**
 * Server-authoritative phases of a Cardinal Rite. LEGACY is retained so rites
 * saved by releases predating interactive ceremonies can finish unchanged.
 */
public enum CardinalRitePhase {
	LEGACY,
	CONSECRATION,
	INSCRIPTION,
	ORDEAL,
	STILL_INTERVAL,
	PROFESSION,
	OFFERING_PROCESSION,
	CULMINATION,
	COMPLETE,
	COLLAPSED;

	public static CardinalRitePhase byName(String name) {
		if (name == null || name.isBlank()) {
			return LEGACY;
		}
		try {
			return valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return LEGACY;
		}
	}
}

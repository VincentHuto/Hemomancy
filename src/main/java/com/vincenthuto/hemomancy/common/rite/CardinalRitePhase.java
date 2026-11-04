package com.vincenthuto.hemomancy.common.rite;

/**
 * Server-authoritative phases of a Cardinal Rite. COUNTDOWN is the phase of every countdown rite, the
 * ones with no interactive ceremony.
 */
public enum CardinalRitePhase {
	COUNTDOWN,
	CONSECRATION,
	INSCRIPTION,
	STATION_PROJECTION,
	/** A Working craft: the caster feeds blood into the built subject. */
	SUBJECT_FEEDING,
	ORDEAL,
	PUPPET_TRIAL,
	STILL_INTERVAL,
	OFFERING_PROCESSION,
	CULMINATION,
	COMPLETE,
	COLLAPSED;

	public static CardinalRitePhase byName(String name) {
		if (name == null || name.isBlank()) {
			return COUNTDOWN;
		}
		try {
			return valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return COUNTDOWN;
		}
	}
}

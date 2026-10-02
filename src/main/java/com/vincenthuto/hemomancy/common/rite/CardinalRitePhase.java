package com.vincenthuto.hemomancy.common.rite;

/**
 * Server-authoritative phases of a Cardinal Rite. LEGACY is retained so rites
 * saved by releases predating interactive ceremonies can finish unchanged.
 */
public enum CardinalRitePhase {
	LEGACY,
	CONSECRATION,
	INSCRIPTION,
	STATION_PROJECTION,
	ORDEAL,
	PUPPET_TRIAL,
	STILL_INTERVAL,
	OFFERING_PROCESSION,
	CULMINATION,
	COMPLETE,
	COLLAPSED;

	public static CardinalRitePhase byName(String name) {
		if (name == null || name.isBlank()) {
			return LEGACY;
		}
		if (name.equals("ALEMBIC_PROJECTION")) return STATION_PROJECTION;
		// The Scriptorium's orb inscription was removed; its rites never escrowed offerings, so collapse safely.
		if (name.equals("SCRIPTORIAL_INSCRIPTION")) return COLLAPSED;
		try {
			return valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return LEGACY;
		}
	}
}

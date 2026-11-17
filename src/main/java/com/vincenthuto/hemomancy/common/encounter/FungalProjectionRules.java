package com.vincenthuto.hemomancy.common.encounter;

public final class FungalProjectionRules {
	public static final int FIRST_VISIT_TICKS = 20 * 60 * 2;

	private FungalProjectionRules() {}

	public static boolean shouldStartProjection(boolean revelationWitnessed, int degree, boolean spineGranted) {
		return !revelationWitnessed && degree >= 7 && spineGranted;
	}

	/**
	 * How a crossing carries its user: the first visit is always a projection. After it every way in lies dormant,
	 * the Spine (D-8, ruled 2026-11-06) and the Podium (D-27, ruled 2026-11-13) alike: the Apotheos crosses by the
	 * Rite of Visitation instead.
	 */
	public enum TravelKind { FIRST_PROJECTION, DORMANT, REFUSED }

	public static TravelKind travelKind(boolean revelationWitnessed, int degree, boolean spineGranted) {
		if (shouldStartProjection(revelationWitnessed, degree, spineGranted)) return TravelKind.FIRST_PROJECTION;
		if (revelationWitnessed && spineGranted) return TravelKind.DORMANT;
		return TravelKind.REFUSED;
	}

	public static boolean shouldForceReturn(int remainingTicks) {
		return remainingTicks <= 0;
	}

	public static boolean shouldSync(int remainingTicks) {
		return remainingTicks <= 0 || remainingTicks % 20 == 0;
	}
}

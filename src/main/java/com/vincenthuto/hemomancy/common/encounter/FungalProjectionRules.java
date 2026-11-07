package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

public final class FungalProjectionRules {
	public static final int FIRST_VISIT_TICKS = 20 * 60 * 2;

	private FungalProjectionRules() {}

	public static boolean shouldStartProjection(boolean revelationWitnessed, int degree, boolean spineGranted) {
		return !revelationWitnessed && degree >= 7 && spineGranted;
	}

	/**
	 * How a crossing carries its user: the first visit is always a projection. After it the Spine lies dormant (D-8,
	 * ruled 2026-11-06: the Apotheos crosses by the Rite of Visitation instead), and only a Podium still carries the
	 * Apotheos bodily.
	 */
	public enum TravelKind { FIRST_PROJECTION, PHYSICAL, DORMANT, REFUSED }

	public static TravelKind travelKind(boolean revelationWitnessed, int degree, boolean spineGranted,
			EnumArchonPath path) {
		return travelKind(revelationWitnessed, degree, spineGranted, path, false);
	}

	public static TravelKind travelKind(boolean revelationWitnessed, int degree, boolean spineGranted,
			EnumArchonPath path, boolean bySpine) {
		if (shouldStartProjection(revelationWitnessed, degree, spineGranted)) return TravelKind.FIRST_PROJECTION;
		if (bySpine && revelationWitnessed && spineGranted) return TravelKind.DORMANT;
		if (revelationWitnessed && spineGranted && path == EnumArchonPath.APOTHEOS) return TravelKind.PHYSICAL;
		return TravelKind.REFUSED;
	}

	public static boolean shouldForceReturn(int remainingTicks) {
		return remainingTicks <= 0;
	}

	public static boolean shouldSync(int remainingTicks) {
		return remainingTicks <= 0 || remainingTicks % 20 == 0;
	}
}

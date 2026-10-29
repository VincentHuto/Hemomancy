package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

public final class FungalProjectionRules {
	public static final int FIRST_VISIT_TICKS = 20 * 60 * 2;

	private FungalProjectionRules() {}

	public static boolean shouldStartProjection(boolean revelationWitnessed, int degree, boolean spineGranted) {
		return !revelationWitnessed && degree >= 7 && spineGranted;
	}

	/** How a Spine carries its user: the first visit is always a projection; after it only the Apotheos goes bodily. */
	public enum TravelKind { FIRST_PROJECTION, PHYSICAL, REFUSED }

	public static TravelKind travelKind(boolean revelationWitnessed, int degree, boolean spineGranted,
			EnumArchonPath path) {
		if (shouldStartProjection(revelationWitnessed, degree, spineGranted)) return TravelKind.FIRST_PROJECTION;
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

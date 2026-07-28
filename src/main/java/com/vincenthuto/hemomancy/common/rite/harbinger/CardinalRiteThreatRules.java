package com.vincenthuto.hemomancy.common.rite.harbinger;

public final class CardinalRiteThreatRules {
	public static final String RITE_BOUND_TAG = "HemomancyRiteBound";
	public static final double BLOODLICKER_MAX_HEALTH = 16.0D;
	public static final float BASTION_DAMAGE_PER_PULSE = 2.0F;
	private static final double SIPHON_RANGE_SQR = 2.25D;
	private static final double CASTER_DRAIN_RANGE_SQR = 9.0D;

	private CardinalRiteThreatRules() {}

	public static boolean canSiphonAnchor(double distanceSqr) {
		return distanceSqr <= SIPHON_RANGE_SQR;
	}

	public static boolean canDrainCaster(double distanceSqr) {
		return distanceSqr <= CASTER_DRAIN_RANGE_SQR;
	}

	public static boolean isProtectedFromPassiveRiteDamage(boolean riteBound) {
		return riteBound;
	}
}

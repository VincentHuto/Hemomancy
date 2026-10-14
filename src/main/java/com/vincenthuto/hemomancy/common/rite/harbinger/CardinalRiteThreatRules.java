package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;

public final class CardinalRiteThreatRules {
	public static final String RITE_BOUND_TAG = "HemomancyRiteBound";
	/** Blood a sacrifice rite must draw from its named creature before the altar can be sealed. */
	public static final int SACRIFICE_REQUIRED_ML = 200;
	/** Ticks between a wave's warning strike and its threats stepping out of the fog. */
	public static final int WAVE_TELEGRAPH_TICKS = 20;
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

	public static boolean allowsPassiveSacrifice(String ritePath) {
		return "cardinal_rite/exsanguination".equals(ritePath);
	}

	/** A sacrifice bleeds from consecration until the daemon is sealed (and through legacy countdowns). */
	public static boolean acceptsSacrifice(CardinalRitePhase phase) {
		return phase == CardinalRitePhase.LEGACY || phase == CardinalRitePhase.CONSECRATION
				|| phase == CardinalRitePhase.INSCRIPTION;
	}

	public static boolean isEligiblePassiveSacrifice(boolean riteBound, boolean player,
			boolean allied, boolean tamed, boolean boss) {
		return !riteBound && !player && !allied && !tamed && !boss;
	}
}

package com.vincenthuto.hemomancy.common.entity.summon;

public final class ScarletMummerRules {
	public static final int PERFORMANCE_INTERVAL_TICKS = 160;
	public static final int PERFORMANCE_DURATION_TICKS = 80;
	public static final int MAX_ATTENTION_TARGETS = 4;
	public static final double ATTENTION_RADIUS = 8.0D;
	public static final double EVADE_DISTANCE = 2.5D;

	private ScarletMummerRules() {
	}

	public static boolean mayRedirect(boolean ownerThreat, boolean immune, boolean canAttackMummer) {
		return ownerThreat && !immune && canAttackMummer;
	}

	public static boolean mayEvade(boolean performing, boolean evadeAvailable, boolean directMelee,
			boolean safePosition) {
		return performing && evadeAvailable && directMelee && safePosition;
	}
}

package com.vincenthuto.hemomancy.common.manipulation;

public final class HematicCommandRules {
	public static final int REBUKE_DURATION_TICKS = 160;
	public static final float MAX_COMMANDABLE_HEALTH = 80.0F;

	private HematicCommandRules() {
	}

	public static boolean canCommand(boolean mob, boolean player, boolean boss, boolean bloodless,
			float maxHealth) {
		return mob && !player && !boss && !bloodless && maxHealth <= MAX_COMMANDABLE_HEALTH;
	}

	public static int impressmentDurationTicks(float maxHealth) {
		return Math.max(300, Math.min(600, 600 - Math.round(Math.max(0.0F, maxHealth) * 5.0F)));
	}
}

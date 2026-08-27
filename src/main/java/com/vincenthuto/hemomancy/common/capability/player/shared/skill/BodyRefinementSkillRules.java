package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

public final class BodyRefinementSkillRules {
	private BodyRefinementSkillRules() {
	}

	public static double knockbackMultiplier(int level) {
		return Math.max(0.4D, 1.0D - Math.max(0, level) * 0.2D);
	}

	public static int retainedChargeTicks(int ticks, int level) {
		return (int) Math.floor(Math.max(0, ticks) * Math.min(0.75D, Math.max(0, level) * 0.25D));
	}

	public static double meleeKnockbackBonus(int level) {
		return Math.max(0, level) * 0.5D;
	}

	public static double attackSpeedModifier(int level) {
		return -Math.min(0.225D, Math.max(0, level) * 0.075D);
	}

	public static int signalDebuffTicks(int ticks, int level) {
		return Math.max(1, (int) Math.ceil(Math.max(0, ticks) * Math.max(0.55D, 1.0D - Math.max(0, level) * 0.15D)));
	}

	public static int visionDebuffTicks(int ticks, int level) {
		return Math.max(1, (int) Math.ceil(Math.max(0, ticks) * Math.max(0.4D, 1.0D - Math.max(0, level) * 0.2D)));
	}

	public static int revealTicks(int ticks, int level) {
		return Math.max(0, (int) Math.ceil(Math.max(0, ticks) * (1.0D + Math.max(0, level) * 0.2D)));
	}

	public static double perceptionRangeMultiplier(int level) {
		return 1.0D + Math.max(0, level) * 0.1D;
	}

	public static boolean strongLight(int lightLevel) {
		return lightLevel >= 12;
	}

	public static double lightMovementModifier(int level) {
		return Math.min(0.15D, Math.max(0, level) * 0.05D);
	}

	public static double lightStepHeightBonus(int level) {
		return Math.min(0.45D, Math.max(0, level) * 0.15D);
	}
}

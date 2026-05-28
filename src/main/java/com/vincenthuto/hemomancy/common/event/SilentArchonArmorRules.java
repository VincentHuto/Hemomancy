package com.vincenthuto.hemomancy.common.event;

public final class SilentArchonArmorRules {
	public static final String SILENT_CHOICE = "silent";
	public static final int SILENT_ARCHON_DEGREE = 7;
	public static final int APOTHEOS_DEGREE = 8;
	public static final int DEATH_REFUSAL_COOLDOWN_TICKS = 12_000;
	public static final double DEATH_REFUSAL_BLOOD_COST = 3_000D;

	private SilentArchonArmorRules() {
	}

	public static boolean canRefuseDeath(boolean hasFullSet, int degree, String archonChoice,
			double bloodVolume, double bloodCost, long now, long cooldownUntil) {
		if (!hasFullSet) {
			return false;
		}
		if (degree < SILENT_ARCHON_DEGREE || degree >= APOTHEOS_DEGREE) {
			return false;
		}
		if (!SILENT_CHOICE.equals(archonChoice)) {
			return false;
		}
		if (bloodVolume < bloodCost) {
			return false;
		}
		return cooldownUntil <= now;
	}

	public static float damageLeavingBarelyAlive(float health) {
		return Math.max(0.0F, health - 1.0F);
	}

	public static long nextCooldownUntil(long now) {
		return now + DEATH_REFUSAL_COOLDOWN_TICKS;
	}
}

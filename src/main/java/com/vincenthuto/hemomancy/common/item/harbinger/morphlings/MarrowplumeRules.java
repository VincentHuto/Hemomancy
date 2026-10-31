package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Marrowplume's first-pass numbers (spec, "Marrowplume - Last Marrow"). */
public final class MarrowplumeRules {
	public static final int PROC_TICKS = 40;
	private static final float[] FEEDING = { 0.5F, 0.75F, 1.0F, 1.25F, 1.5F, 1.75F };
	public static final float ALONE_FEEDING_BONUS = 0.25F;

	public static final double PLUME_RADIUS = 5.0D;
	public static final double PLUME_MATURE_RADIUS = 6.0D;
	public static final int PLUME_WEAKNESS_TICKS = 2 * 20;
	public static final int PLUME_SLOWNESS_TICKS = 30;
	public static final float PLUME_HEAL_PER_HOSTILE = 0.5F;
	public static final float PLUME_HEAL_CAP = 2.0F;
	public static final int PLUME_COOLDOWN = 5 * 20;

	public static final int ROOT_NETWORK_TICKS = 4 * 20;
	public static final double ROOT_NETWORK_RANGE = 8.0D;
	public static final float ROOT_NETWORK_HEAL = 0.5F;

	public static final float INHERITANCE_CHANCE = 0.25F;
	public static final float INHERITANCE_PRIMAL_CHANCE = 0.40F;

	public static final float WHALE_FALL_MIN_HEALTH = 20.0F;
	public static final double WHALE_FALL_RADIUS = 6.0D;
	public static final int WHALE_FALL_TICKS = 12 * 20;
	public static final float WHALE_FALL_HEAL = 3.0F;
	public static final double WHALE_FALL_ANCHOR_BONUS = 1.5D;
	public static final int WHALE_FALL_COOLDOWN = 30 * 20;
	public static final int WHALE_FALL_STRAIN = 11 * 20;
	public static final float SPORE_SAC_BASE_CHANCE = 0.12F;
	public static final float SPORE_SAC_PER_SKILL = 0.03F;

	public static final double HOME_BONE_RANGE = 8.0D;
	public static final int HOME_KILL_WINDOW = 30 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("marrow_feeding"),
			MorphlingPower.reactive("plume_cloud", 2, PLUME_COOLDOWN),
			MorphlingPower.conditional("root_network", 3, ROOT_NETWORK_TICKS),
			MorphlingPower.conditional("falls_inheritance", 4, 0),
			MorphlingPower.triggeredPrimal("whale_fall", WHALE_FALL_COOLDOWN));

	private MarrowplumeRules() {
	}

	/** Marrow Feeding per proc, +50% at home. */
	public static float feeding(int stage, boolean home) {
		float base = FEEDING[SunburstRules.clamp(stage)];
		return home ? base * 1.5F : base;
	}

	public static float plumeHeal(int hostilesCaught) {
		return Math.min(PLUME_HEAL_CAP, PLUME_HEAL_PER_HOSTILE * Math.max(0, hostilesCaught));
	}

	public static double plumeRadius(int stage) {
		return stage >= 3 ? PLUME_MATURE_RADIUS : PLUME_RADIUS;
	}

	public static float inheritanceChance(int stage) {
		return stage >= 5 ? INHERITANCE_PRIMAL_CHANCE : stage >= 4 ? INHERITANCE_CHANCE : 0.0F;
	}

	/** Economy guard (balance rule 7): no bonus loot from players, bosses or spawner mobs. */
	public static boolean mayInherit(boolean player, boolean boss, boolean spawnerMob) {
		return !player && !boss && !spawnerMob;
	}

	public static float sporeSacChance(int hyphalLevel) {
		return SPORE_SAC_BASE_CHANCE + SPORE_SAC_PER_SKILL * Math.max(0, hyphalLevel);
	}
}

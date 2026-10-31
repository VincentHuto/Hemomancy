package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Hushbell's first-pass numbers (spec, "Hushbell - The Still Bell"). */
public final class HushbellRules {
	private static final double[] HUSH = { 0.10D, 0.20D, 0.30D, 0.40D, 0.50D, 0.60D };
	public static final float HIDE_TRIGGER = 0.45F;
	public static final float HIDE_DEEP_TRIGGER = 0.30F;
	public static final int COLD_CLEANSE_TICKS = 5 * 20;

	public static final float FOLD_TRIGGER = 0.40F;
	public static final int FOLD_TICKS = 3 * 20;
	public static final int FOLD_HOME_TICKS = 4 * 20;
	public static final int FOLD_COOLDOWN = 30 * 20;

	public static final double CRYPTOBIOSIS_COST = 650.0D;
	public static final float CRYPTOBIOSIS_HEALTH = 10.0F;
	public static final int CRYPTOBIOSIS_SEALED_TICKS = 5 * 20;
	public static final int CRYPTOBIOSIS_STRAIN = 35 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("hush"),
			MorphlingPower.conditional("cryptobiotic_hide", 2, 0),
			MorphlingPower.conditional("cold_cleanse", 3, COLD_CLEANSE_TICKS),
			MorphlingPower.reactive("fold_shut", 4, FOLD_COOLDOWN),
			MorphlingPower.deathSave("cryptobiosis", PrimalMorphlingRules.PRIMAL_LEVEL, CRYPTOBIOSIS_COST));

	private HushbellRules() {
	}

	/** How much slower hunger and air loss run: 10% at Unfed to 60% at Primal. */
	public static double hush(int stage) {
		return HUSH[SunburstRules.clamp(stage)];
	}

	/**
	 * Cryptobiotic Hide's Resistance amplifier from Developing, or -1 for none: Resistance I while still or at 45%
	 * health, Resistance II at 30%. The old later-stage Resistance II baseline is cut.
	 */
	public static int hideAmplifier(int stage, boolean still, float health, float maxHealth) {
		if (stage < 2 || maxHealth <= 0.0F) return -1;
		float ratio = health / maxHealth;
		if (ratio <= HIDE_DEEP_TRIGGER) return 1;
		return still || ratio <= HIDE_TRIGGER ? 0 : -1;
	}

	public static boolean foldShuts(int stage, float health, float maxHealth) {
		return stage >= 4 && maxHealth > 0.0F && health > 0.0F && health / maxHealth <= FOLD_TRIGGER;
	}
}

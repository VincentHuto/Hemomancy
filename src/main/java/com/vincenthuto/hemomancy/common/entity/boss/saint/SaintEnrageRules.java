package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;

import java.util.List;

/**
 * A Saint woken through a shattered ring door fights enraged: in gameplay terms a harder fight, in lore terms the sum
 * of what the player refused to see. Each Chapel skipped lends its deed: the Downfall its fall (more health), the
 * Repose its sleeplessness (never pausing), the Beholding its hidden tells (resistance), the Litany its unheard words
 * (regeneration). Only a fragment of Hallowed Residuum comes out of such a fight.
 */
public final class SaintEnrageRules {
	public static final double BASE_DAMAGE = 0.3;
	public static final double BASE_SPEED = 0.15;
	public static final double FALL_HEALTH = 0.5;
	public static final double SLEEPLESS_SPEED = 0.2;
	public static final int FRAGMENTS_PER_RESIDUUM = 3;

	private SaintEnrageRules() {}

	public static boolean enraged(String doorMode) {
		return "FORCED".equals(doorMode);
	}

	/** An Apotheos who reaches the tomb without opening its door (pistons, machines) still wakes the Saint enraged. */
	public static String effectiveMode(String doorMode, boolean apotheosWaker) {
		return apotheosWaker && "NONE".equals(doorMode) ? "FORCED" : doorMode;
	}

	/** What goes back onto the tomb when the Saint returns: only a door actually opened, never an inferred wake. */
	public static String recordToKeep(String rawRecord) {
		return "NONE".equals(rawRecord) ? null : rawRecord;
	}

	public static boolean fragmentOnly(String doorMode) {
		return enraged(doorMode);
	}

	public static double damageBonus() {
		return BASE_DAMAGE;
	}

	public static double speedBonus(List<ChapelRules.Kind> skipped) {
		return BASE_SPEED + (skipped.contains(ChapelRules.Kind.REPOSE) ? SLEEPLESS_SPEED : 0.0);
	}

	public static double healthBonus(List<ChapelRules.Kind> skipped) {
		return skipped.contains(ChapelRules.Kind.DOWNFALL) ? FALL_HEALTH : 0.0;
	}

	public static boolean resists(List<ChapelRules.Kind> skipped) {
		return skipped.contains(ChapelRules.Kind.BEHOLDING);
	}

	public static boolean regenerates(List<ChapelRules.Kind> skipped) {
		return skipped.contains(ChapelRules.Kind.LITANY);
	}
}

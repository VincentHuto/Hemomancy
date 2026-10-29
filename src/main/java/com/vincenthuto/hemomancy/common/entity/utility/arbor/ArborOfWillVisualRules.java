package com.vincenthuto.hemomancy.common.entity.utility.arbor;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

/** Pure presentation rules for Degree, skill, Pome, and Apotheosis states. */
public final class ArborOfWillVisualRules {
	private ArborOfWillVisualRules() {
	}

	public enum GrowthState {
		DEGREE_SEALED_BUD,
		DORMANT_BUD,
		RIPE_FRUIT,
		CLOSED_CALYX
	}

	/** The degree the Arbor shows: only the Apotheos grows the eighth crown; a Silentium keeps the seventh. */
	public static int visualDegree(int degree, EnumArchonPath path) {
		return path == EnumArchonPath.APOTHEOS ? degree : Math.min(degree, 7);
	}

	public static double treeHeight(int degree) {
		int clamped = Math.max(0, Math.min(8, degree));
		if (clamped == 0) return 1.0;
		return 3.6 + (Math.min(7, clamped) - 1) * 1.15;
	}

	public static double rootRadius(int degree, double chamberRadius) {
		int clamped = Math.max(0, Math.min(8, degree));
		double desired = clamped == 0 ? 0.6 : clamped == 8 ? 6.5 : 1.2 + (clamped - 1) * 0.52;
		return Math.min(Math.max(0.75, chamberRadius * 0.67), desired);
	}

	public static int visibleWhorls(int degree) {
		return Math.max(0, Math.min(7, degree));
	}

	public static int woundCount(int pomesConsumed) {
		return Math.max(0, Math.min(9, pomesConsumed));
	}

	public static double foliageFraction(int unlockedSkills, int totalSkills, int pomesConsumed, boolean apotheosis) {
		if (apotheosis) return 1.0;
		double skillHealth = totalSkills <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0,
				(double) unlockedSkills / totalSkills));
		double pomeHealth = 1.0 - woundCount(pomesConsumed) / 9.0 * 0.92;
		return Math.max(0.0, Math.min(1.0, skillHealth * pomeHealth));
	}

	/** Foliage the Arbor carries from Degree growth alone; Pome wounds still strip it toward bare wood. */
	public static double minimumFoliage(int visibleWhorls, int pomesConsumed) {
		double growth = Math.max(0, Math.min(7, visibleWhorls)) / 7.0;
		double pomeHealth = 1.0 - woundCount(pomesConsumed) / 9.0 * 0.92;
		return (0.40 + 0.40 * growth) * pomeHealth;
	}

	public static GrowthState growthState(boolean unlocked, boolean enabled,
			int requiredDegree, int playerDegree) {
		if (!unlocked) {
			return requiredDegree > playerDegree ? GrowthState.DEGREE_SEALED_BUD : GrowthState.DORMANT_BUD;
		}
		return enabled ? GrowthState.RIPE_FRUIT : GrowthState.CLOSED_CALYX;
	}

	public static double fruitScale(int level, int maxLevel) {
		if (maxLevel <= 1) return 1.0;
		double progress = (Math.max(1, Math.min(maxLevel, level)) - 1.0) / (maxLevel - 1.0);
		return 0.55 + progress * 0.45;
	}

	public static int seedChambers(int level, int maxLevel) {
		return Math.max(1, Math.min(Math.max(1, maxLevel), level));
	}
}

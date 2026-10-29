package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

/** What an Apotheos body refuses, and what it no longer pays in hunger. */
public final class ApotheosBodyRules {
	/** High enough to sprint, below the natural-regeneration threshold of 18. */
	public static final int HUNGER_PIN = 17;

	private ApotheosBodyRules() {}

	/** Food is refused everywhere; potions and milk only in the Gardens, where the tonic is the one remedy. */
	public static boolean refusesUse(boolean apotheos, boolean inGardens, boolean isFood, boolean isPotionOrMilk,
			boolean isTonic) {
		if (isTonic || !apotheos) return false;
		if (isFood) return true;
		return isPotionOrMilk && inGardens;
	}

	/** Symbiotic Metabolism moves morphling upkeep onto hunger; a body pinned at 17 must pay it in blood. */
	public static boolean symbioticSplit(boolean symbioticSkill, boolean apotheos) {
		return symbioticSkill && !apotheos;
	}
}

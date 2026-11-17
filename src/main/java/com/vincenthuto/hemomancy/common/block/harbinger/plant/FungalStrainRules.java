package com.vincenthuto.hemomancy.common.block.harbinger.plant;

/**
 * The fungal strains' numbers (core-loop round two, P7), pure. A strain advances one stage on a growth check it wins,
 * one in {@link #GROW_ROLL}; fed ground decides whether it gets the check at all ({@code BloodFedGrowthRules}: never
 * unfed, a third of them under a drip, all of them beside a fresh basin or a feeding run). Bone meal adds one stage on
 * fed ground. A ripe strain returns its spore and sometimes a second (the block's loot table holds the chance; the
 * numbers here are what the table and the Liber say).
 */
public final class FungalStrainRules {
	/** The ripe stage. */
	public static final int MAX_AGE = 3;
	/** A growth check grows a strain one time in this many (a little slower than wheat's best, faster than a stem). */
	public static final int GROW_ROLL = 6;
	/** Spores a ripe strain always returns. */
	public static final int RIPE_SPORES = 1;
	/** The chance a ripe strain returns one more. */
	public static final float BONUS_SPORE_CHANCE = 0.5F;
	/** Stages one bone meal adds. */
	public static final int BONEMEAL_STAGES = 1;

	private FungalStrainRules() {
	}

	/** Whether a growth check with uniform {@code roll} in {@code [0, GROW_ROLL)} grows the strain. */
	public static boolean ownRollGrows(int roll) {
		return roll == 0;
	}

	/** The stage after {@code stages} more, never past ripe. */
	public static int advance(int age, int stages) {
		return Math.min(MAX_AGE, Math.max(0, age) + Math.max(0, stages));
	}

	public static boolean ripe(int age) {
		return age >= MAX_AGE;
	}

	/** The expected spores one harvest returns: what was planted for an unripe strain, more for a ripe one. */
	public static double expectedSpores(int age) {
		return ripe(age) ? RIPE_SPORES + BONUS_SPORE_CHANCE : 1.0D;
	}
}

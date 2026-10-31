package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

/**
 * The Brined Court spec's balance rules 1, 2 and 4 as numbers, so a tuning pass cannot drift out of band unnoticed
 * ({@code MorphlingBalanceBandsTest} checks every strain's {@link MorphlingPower} list against them).
 */
public final class MorphlingBalanceBands {
	public static final double PRIMAL_COST_MIN = 250.0D;
	public static final double PRIMAL_COST_MAX = 450.0D;
	public static final int PRIMAL_COOLDOWN_MIN = 12 * 20;
	public static final int PRIMAL_COOLDOWN_MAX = 90 * 20;
	public static final double DEATH_SAVE_COST_MIN = 500.0D;
	public static final double DEATH_SAVE_COST_MAX = 650.0D;
	public static final int LAST_RITE_COOLDOWN_TICKS = 10 * 60 * 20;

	/** Giving health never takes the giver below these fractions of maximum health (rule 4). */
	public static final float SHARED_TABLE_GIVER_FLOOR = 0.5F;
	public static final float COLONY_GIVER_FLOOR = 0.5F;
	public static final float BREAKWATER_GIVER_FLOOR = 0.3F;

	private MorphlingBalanceBands() {
	}

	/** Minimum cooldown in ticks for a reactive power at this stage (rule 2). */
	public static int reactiveCooldownMin(int stage) {
		return switch (stage) {
			case 2 -> 3 * 20;
			case 3 -> 8 * 20;
			case 4 -> 20 * 20;
			default -> throw new IllegalArgumentException("no reactive band at stage " + stage);
		};
	}

	public static int reactiveCooldownMax(int stage) {
		return switch (stage) {
			case 2 -> 15 * 20;
			case 3 -> 20 * 20;
			case 4 -> 60 * 20;
			default -> throw new IllegalArgumentException("no reactive band at stage " + stage);
		};
	}

	/** Health a giver may part with: what lies above the floor, never negative. */
	public static float givable(float health, float maxHealth, float floorFraction, float wanted) {
		float spare = health - maxHealth * floorFraction;
		return Math.max(0.0F, Math.min(wanted, spare));
	}
}

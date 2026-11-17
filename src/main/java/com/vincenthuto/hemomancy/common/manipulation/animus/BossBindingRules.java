package com.vincenthuto.hemomancy.common.manipulation.animus;

/**
 * How far Blood Binding holds a boss (LP-16, NQ-04-1 to NQ-04-4). Pure: no level, no entity.
 *
 * <ul>
 * <li>One binding is one hold of the boss length; it then drops, whatever the binder holds, and the next hold is a new
 * attempt.</li>
 * <li>A boss takes {@link #total} successful bindings for that body's life, shared by every player.</li>
 * <li>Each attempt can slip by {@link #failChance}. A slip still costs the blood, and the held form waits
 * {@link #RETRY_WAIT_TICKS} before trying that boss again.</li>
 * </ul>
 */
public final class BossBindingRules {
	/** One binding for each this many points of maximum health. */
	public static final double HEALTH_PER_BINDING = 100.0D;
	/** The slip chance's base: maximum health over this. */
	public static final double HEALTH_PER_FAIL_CHANCE = 2000.0D;
	public static final double MIN_BASE_FAIL_CHANCE = 0.10D;
	public static final double MAX_BASE_FAIL_CHANCE = 0.60D;
	/** How long the held form waits after a slip before it tries the same boss again. */
	public static final int RETRY_WAIT_TICKS = 60;

	private BossBindingRules() {
	}

	/** The bindings a boss of {@code maxHealth} takes in its life: one per hundred, at least one. */
	public static int total(double maxHealth) {
		if (!(maxHealth > 0.0D)) return 1;
		return Math.max(1, (int) Math.floor(maxHealth / HEALTH_PER_BINDING));
	}

	/**
	 * The chance an attempt slips: maximum health over 2,000, held between 10% and 60%, times
	 * {@code 0.5 + 0.5 x health fraction}. A 1,000 HP boss at full health slips half the time.
	 */
	public static double failChance(double maxHealth, double health) {
		if (!(maxHealth > 0.0D)) return MIN_BASE_FAIL_CHANCE * 0.5D;
		double base = Math.clamp(maxHealth / HEALTH_PER_FAIL_CHANCE, MIN_BASE_FAIL_CHANCE, MAX_BASE_FAIL_CHANCE);
		double fraction = Math.clamp(health / maxHealth, 0.0D, 1.0D);
		return base * (0.5D + 0.5D * fraction);
	}
}

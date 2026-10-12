package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;

/**
 * Numbers and timing for the Somatic Loom's witnessed weave. Counted in weave ticks, which stop while the
 * weave is paused, so a pause never shifts a thread's place in the echo's loop.
 */
public final class SomaticWeaveRules {
	/** One echo cast: opening, hold, release, recovery, then a breath before the next. */
	public static final int LOOP_TICKS = 80;
	public static final int RELEASE_TICK = 40;
	/** A thread tears loose just after the release, when the echo is most spent. */
	public static final int FRAY_TICK = 46;
	public static final int CLIMAX_TICKS = 60;
	public static final int BLACKOUT_TICKS = 100;
	public static final int WINDUP_TICKS = 20;
	public static final double RITUAL_RADIUS = 12.0D;
	public static final float NON_LETHAL_FLOOR = 4.0F;
	/** How long after a thread's attack its lingering fire, frost or poison still counts as the weave's. */
	public static final int NON_LETHAL_WINDOW_TICKS = 200;
	private static final int MAX_THREADS_PER_TENDENCY = 8;

	private SomaticWeaveRules() {
	}

	public static boolean frayDue(long weaveTicks) {
		return weaveTicks % LOOP_TICKS == FRAY_TICK;
	}

	public static int maxLoose(EnumManipulationRank rank) {
		return lethal(rank) ? 3 : 2;
	}

	public static boolean lethal(EnumManipulationRank rank) {
		return rank.ordinal() >= EnumManipulationRank.SUMMA.ordinal();
	}

	/** Attacks a thread makes before it goes slack on its own. */
	private static int attacksToSlack(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> 2;
			case MEDIOCRITAS, SUMMA -> 3;
			case MAGISTER -> 4;
			case PERFECTUS -> 5;
		};
	}

	public static float strainPerAttack(EnumManipulationRank rank) {
		return 1.0F / attacksToSlack(rank);
	}

	/** A staff hit strains a thread half as much as its own attack, but hits come far faster than attacks. */
	public static float strainPerHit(EnumManipulationRank rank) {
		return 0.5F / attacksToSlack(rank);
	}

	public static float attackPower(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> 1.0F;
			case MEDIOCRITAS -> 1.25F;
			case SUMMA -> 1.6F;
			case MAGISTER -> 2.0F;
			case PERFECTUS -> 2.5F;
		};
	}

	public static int attackInterval(EnumManipulationRank rank) {
		return Math.max(40, 70 - 6 * rank.ordinal());
	}

	public static int threadsFor(int requirement) {
		return Math.max(0, Math.min(MAX_THREADS_PER_TENDENCY, requirement));
	}

	/** Damage a non-lethal weave lets through: never past the floor. */
	public static float flooredDamage(float health, float amount) {
		return Math.max(0.0F, Math.min(amount, health - NON_LETHAL_FLOOR));
	}

	public static boolean blacksOut(float health, float amount) {
		return health - amount <= NON_LETHAL_FLOOR;
	}

	public static boolean weaverPresent(boolean alive, boolean sameDimension, double distanceSqr) {
		return alive && sameDimension && distanceSqr <= RITUAL_RADIUS * RITUAL_RADIUS;
	}

	/** Ticks a thread needs after a staff strike before another one strains it. */
	public static final int STRIKE_COOLDOWN_TICKS = 10;
	/** How close a thread dives while it winds up, so the tell is also the opening for a staff strike. */
	public static final double STRIKE_DISTANCE = 2.2D;
	/** How long after a Ferric launch the landing still counts as the weave's harm. */
	public static final int LAUNCH_WINDOW_TICKS = 60;

	public static double holdDistance(double preferred, boolean windingUp) {
		return windingUp ? Math.min(preferred, STRIKE_DISTANCE) : preferred;
	}

	/** The loom stage a memory of this rank needs, matching the degrees that teach it: SUMMA D3, MAGISTER D5, PERFECTUS D6. */
	public static int stageFor(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS, MEDIOCRITAS, SUMMA -> 0;
			case MAGISTER -> 1;
			case PERFECTUS -> 2;
		};
	}

	public static boolean rankAllowed(EnumManipulationRank rank, int stage) {
		return stageFor(rank) <= stage;
	}

	/** How near the echo a slack thread must be for Ganglionic feelers to take it home unaided. */
	public static double autoCatchRadius(int stage) {
		return stage >= 1 ? 4.0D : 0.0D;
	}

	/** How far a Living Staff can hook a slack thread. */
	public static double hookReach(int stage) {
		return stage >= 1 ? 8.0D : 4.5D;
	}
}

package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;

/**
 * The Somatic Loom's ritual outside the engram itself: the weaver's ritual area, the climax, the blackout floor that
 * keeps any weave from killing, a Perfectus intrusion's bite, and which memory ranks each loom stage can weave.
 * The engram's own rules live in {@code engram.EngramWeaveRules}.
 */
public final class SomaticWeaveRules {
	public static final int CLIMAX_TICKS = 60;
	public static final int BLACKOUT_TICKS = 100;
	/** The tell before an intrusion's bite. */
	public static final int WINDUP_TICKS = 20;
	public static final double RITUAL_RADIUS = 12.0D;
	public static final float NON_LETHAL_FLOOR = 4.0F;
	/** How long after an intrusion's bite its lingering fire, frost or poison still counts as the weave's. */
	public static final int NON_LETHAL_WINDOW_TICKS = 200;
	/** How long after a Ferric launch the landing still counts as the weave's harm. */
	public static final int LAUNCH_WINDOW_TICKS = 60;

	private SomaticWeaveRules() {
	}

	/** No weave kills: a misread engram blacks the weaver out at the floor, at every rank. */
	public static boolean lethal(EnumManipulationRank rank) {
		return false;
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

	/** Damage a weave lets through: never past the floor. */
	public static float flooredDamage(float health, float amount) {
		return Math.max(0.0F, Math.min(amount, health - NON_LETHAL_FLOOR));
	}

	public static boolean blacksOut(float health, float amount) {
		return health - amount <= NON_LETHAL_FLOOR;
	}

	public static boolean weaverPresent(boolean alive, boolean sameDimension, double distanceSqr) {
		return alive && sameDimension && distanceSqr <= RITUAL_RADIUS * RITUAL_RADIUS;
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
}

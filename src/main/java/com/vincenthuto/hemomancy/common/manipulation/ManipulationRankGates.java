package com.vincenthuto.hemomancy.common.manipulation;

/**
 * Central degree gates for learning blood manipulations by rank, raised by a manipulation's own degree floor.
 */
public final class ManipulationRankGates {
	private ManipulationRankGates() {
	}

	public static int minDegreeForRank(EnumManipulationRank rank) {
		if (rank == null) return 0;
		return switch (rank) {
			case HUMILIS -> 0;
			case MEDIOCRITAS -> 1;
			case SUMMA -> 3;
			case MAGISTER -> 5;
			case PERFECTUS -> 6;
		};
	}

	public static boolean playerMeetsRank(int degree, EnumManipulationRank rank) {
		return degree >= minDegreeForRank(rank);
	}

	/** The degree a manipulation is learned at: its rank's gate, or its own floor when that is higher. */
	public static int minDegreeFor(BloodManipulation manipulation) {
		if (manipulation == null) return 0;
		return Math.max(minDegreeForRank(manipulation.getRank()), manipulation.getRequiredDegree());
	}

	public static boolean playerMeets(int degree, BloodManipulation manipulation) {
		return degree >= minDegreeFor(manipulation);
	}
}

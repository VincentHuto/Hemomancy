package com.vincenthuto.hemomancy.common.manipulation;

/**
 * Central degree gates for learning blood manipulations by rank.
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
}

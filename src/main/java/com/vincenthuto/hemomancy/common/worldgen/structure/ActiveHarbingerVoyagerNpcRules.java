package com.vincenthuto.hemomancy.common.worldgen.structure;

public final class ActiveHarbingerVoyagerNpcRules {
	public static final int VOTARY_WAYFARER_CHANCE = 5;

	private ActiveHarbingerVoyagerNpcRules() {
	}

	public static boolean shouldSpawnVoyager() {
		return true;
	}

	public static boolean shouldSpawnVotaryWayfarer(int roll) {
		return roll == 0;
	}
}

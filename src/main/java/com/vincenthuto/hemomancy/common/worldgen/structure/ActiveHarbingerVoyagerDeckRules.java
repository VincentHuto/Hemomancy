package com.vincenthuto.hemomancy.common.worldgen.structure;

final class ActiveHarbingerVoyagerDeckRules {
	private ActiveHarbingerVoyagerDeckRules() {
	}

	static boolean canSpawnOnDeck(boolean inBounds, boolean spaceAir, boolean drySpace, boolean sturdyFloor) {
		return inBounds && spaceAir && drySpace && sturdyFloor;
	}
}

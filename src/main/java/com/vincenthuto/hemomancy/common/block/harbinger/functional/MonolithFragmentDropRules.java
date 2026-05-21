package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import net.minecraft.util.RandomSource;

public final class MonolithFragmentDropRules {
	public static final int MIN_FRAGMENTS = 5;
	public static final int MAX_FRAGMENTS = 8;

	private static final int FRAGMENT_RANGE = MAX_FRAGMENTS - MIN_FRAGMENTS + 1;

	private MonolithFragmentDropRules() {
	}

	public static int rollFragmentCount(RandomSource random) {
		return fragmentCountFromRoll(random.nextInt(FRAGMENT_RANGE));
	}

	public static int fragmentCountFromRoll(int roll) {
		return MIN_FRAGMENTS + Math.floorMod(roll, FRAGMENT_RANGE);
	}
}

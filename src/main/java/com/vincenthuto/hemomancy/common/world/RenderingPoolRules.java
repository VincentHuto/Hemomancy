package com.vincenthuto.hemomancy.common.world;

import net.minecraft.util.RandomSource;

/** A rendering pool breaks down only what an Apotheos offers it; each offered item rolls each result on its own. */
public final class RenderingPoolRules {
	public static final int PULSE_TICKS = 10;

	private RenderingPoolRules() {}

	public static boolean mayRender(boolean throwerApotheos) {
		return throwerApotheos;
	}

	public static int yield(int count, int stackCount, float chance, RandomSource random) {
		if (chance <= 0f || count <= 0 || stackCount <= 0) return 0;
		if (chance >= 1f) return count * stackCount;
		int total = 0;
		for (int i = 0; i < stackCount; i++) {
			if (random.nextFloat() < chance) total += count;
		}
		return total;
	}
}

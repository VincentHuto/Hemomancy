package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.OptionalInt;

/**
 * Where a Saint mausoleum's entrance pavilion sits. Vanilla projects a jigsaw start onto the ground under its centre
 * only, and {@code beard_thin} then flattens the land around the piece to that height: on a slope the uphill side is
 * cut away into a pit around the pavilion. Instead the pavilion stands on the highest ground under its footprint, so
 * nothing inside the footprint is cut and the beard only fills the low side, and a site whose ground rises more than
 * {@link #MAX_RISE} across the footprint and the ring the beard reshapes around it is refused: a hill rising just
 * past the pavilion's edge would otherwise still be cut into a wall.
 */
public final class MausoleumEntranceRules {
	/** The entrance piece ({@code top}) is 17 x 17 (eight blocks either side of its centre), plus six of its surroundings. */
	public static final int REACH = 14;
	public static final int SAMPLE_STEP = 7;
	/** The most the ground may rise across that area; more and the pavilion would be cut into a hill or stand on a plinth. */
	public static final int MAX_RISE = 4;

	private MausoleumEntranceRules() {
	}

	/**
	 * How far to raise the complex from where vanilla projected it.
	 *
	 * @param footprint the first free block of each sampled column under the pavilion
	 * @param centre    the first free block under the pavilion's centre, where vanilla projected it
	 * @return the rise, or empty when the ground is too uneven to build on
	 */
	public static OptionalInt rise(int[] footprint, int centre) {
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;
		for (int height : footprint) {
			lowest = Math.min(lowest, height);
			highest = Math.max(highest, height);
		}
		return highest - lowest > MAX_RISE ? OptionalInt.empty() : OptionalInt.of(highest - centre);
	}
}

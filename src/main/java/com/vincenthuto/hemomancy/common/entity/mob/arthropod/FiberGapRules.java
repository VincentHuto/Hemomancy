package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

/**
 * Decides whether an empty position is a severed cable run the Borer should mend. Minecraft-free so it
 * is unit testable; the caller samples the world and passes the six neighbour flags in.
 */
public final class FiberGapRules {
	public static final int WEST = 0;
	public static final int EAST = 1;
	public static final int DOWN = 2;
	public static final int UP = 3;
	public static final int NORTH = 4;
	public static final int SOUTH = 5;

	private FiberGapRules() {
	}

	/** A gap is air with network blocks on exactly one opposing pair, so the run is genuinely severed. */
	public static boolean isGap(boolean[] crawlableNeighbours) {
		return gapAxis(crawlableNeighbours) >= 0;
	}

	/** 0 = X, 1 = Y, 2 = Z, -1 = not a gap. */
	public static int gapAxis(boolean[] n) {
		boolean x = n[WEST] && n[EAST];
		boolean y = n[DOWN] && n[UP];
		boolean z = n[NORTH] && n[SOUTH];
		int pairs = (x ? 1 : 0) + (y ? 1 : 0) + (z ? 1 : 0);
		if (pairs != 1) {
			return -1;
		}
		return x ? 0 : (y ? 1 : 2);
	}
}

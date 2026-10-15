package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

/**
 * Where a Working craft's subject may stand: anywhere on the floor that leaves
 * the Focus clear, on the floor's upper layer. Any such footprint lies wholly on
 * one side of the Focus, so each side's bed spans the floor's whole width and
 * runs out to its edge: one block from the Focus on a 3x3 floor, two on a 5x5,
 * three on a 7x7, four on a 9x9. Cells are measured from the Focus: lateral
 * across the side, outward away from the Focus, up from the floor plane.
 */
public record WorkingCraftBed(int width, int depth) {
	public static final int MAX_HEIGHT = 3;

	public static WorkingCraftBed forTier(CardinalRiteType tier) {
		return new WorkingCraftBed(tier.getSize(), tier.getSize() / 2);
	}

	public boolean fits(Footprint footprint) {
		if (footprint.height() > MAX_HEIGHT) return false;
		return footprint.width() <= width && footprint.depth() <= depth
				|| footprint.depth() <= width && footprint.width() <= depth;
	}

	public boolean contains(int lateral, int outward, int up) {
		return Math.abs(lateral) <= width / 2 && outward >= 1 && outward <= depth && up >= 1 && up <= MAX_HEIGHT;
	}

	/**
	 * Whether {@code target} lies on a Working floor centred on {@code focus}, or in the space its bed can fill:
	 * within the floor's round footprint, from the floor layer below the Focus up to the bed's height.
	 */
	public static boolean withinFloor(net.minecraft.core.BlockPos focus, net.minecraft.core.BlockPos target,
			float footprintRadius) {
		int dx = target.getX() - focus.getX();
		int dz = target.getZ() - focus.getZ();
		int up = target.getY() - focus.getY();
		return up >= -1 && up <= MAX_HEIGHT && dx * dx + dz * dz <= footprintRadius * footprintRadius;
	}

	public static CardinalRiteType smallestFitting(Footprint footprint) {
		for (CardinalRiteType tier : CardinalRiteType.values()) {
			if (forTier(tier).fits(footprint)) return tier;
		}
		return null;
	}

	/** The bounding box of a pattern's non-space cells. Patterns are {@code [z][y]} rows of x characters. */
	public record Footprint(int width, int depth, int height) {
		public static Footprint of(String[][] pattern) {
			int minX = Integer.MAX_VALUE, maxX = -1;
			int minY = Integer.MAX_VALUE, maxY = -1;
			int minZ = Integer.MAX_VALUE, maxZ = -1;
			for (int z = 0; z < pattern.length; z++) {
				for (int y = 0; y < pattern[z].length; y++) {
					String row = pattern[z][y];
					for (int x = 0; x < row.length(); x++) {
						if (row.charAt(x) == ' ') continue;
						minX = Math.min(minX, x);
						maxX = Math.max(maxX, x);
						minY = Math.min(minY, y);
						maxY = Math.max(maxY, y);
						minZ = Math.min(minZ, z);
						maxZ = Math.max(maxZ, z);
					}
				}
			}
			if (maxX < 0) return new Footprint(0, 0, 0);
			return new Footprint(maxX - minX + 1, maxZ - minZ + 1, maxY - minY + 1);
		}
	}
}

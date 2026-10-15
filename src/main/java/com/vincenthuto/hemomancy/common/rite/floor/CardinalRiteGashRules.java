package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Where braziers stand on a Cardinal Rite floor: anywhere along its gash, a circle
 * just inside the floor's edge. Every floor cell the gash runs across takes a
 * brazier, so the floor needs no ground beyond itself and the trench stays round,
 * except the cells where a ceremony anchor sore can open, which stay clear.
 * Cells are offsets from the Focus in the floor's socket frame (x right, y up one
 * block onto the floor, z toward the back), ordered clockwise round the gash from
 * the Focus's left, so the back and sides fill before the front where a Working
 * craft stands. Shared by rite matching and the floor's rendering.
 */
public final class CardinalRiteGashRules {
	/** How far inside the floor's edge the gash runs. */
	public static final double EDGE_MARGIN = 0.26D;
	/** A cell takes a brazier if the gash passes within this of its centre. */
	private static final double CELL_REACH = 0.5D;

	private CardinalRiteGashRules() {
	}

	/** The gash's radius about the Focus centre, for a floor {@code half} blocks from Focus to edge. */
	public static double radius(int half) {
		return half + 0.5D - EDGE_MARGIN;
	}

	/** Every cell a brazier may stand on, clockwise round the gash from the Focus's left. */
	public static List<BlockPos> cells(int half) {
		double radius = radius(half);
		Set<BlockPos> anchors = anchorCells();
		List<BlockPos> cells = new ArrayList<>();
		for (int x = -half; x <= half; x++) {
			for (int z = -half; z <= half; z++) {
				if (x == 0 && z == 0) continue;
				BlockPos cell = new BlockPos(x, 1, z);
				if (Math.abs(Math.hypot(x, z) - radius) <= CELL_REACH && !anchors.contains(cell)) cells.add(cell);
			}
		}
		cells.sort(Comparator.comparingDouble((BlockPos cell) -> around(cell.getX(), cell.getZ()))
				.thenComparingDouble(cell -> Math.hypot(cell.getX(), cell.getZ())));
		return List.copyOf(cells);
	}

	/**
	 * Every cell any ceremony anchor can be planted on, at any ring. The anchor rings are four-fold symmetric on the
	 * axes and diagonals, so the set is the same in the socket frame as in the anchors' own.
	 */
	static Set<BlockPos> anchorCells() {
		return ANCHOR_CELLS;
	}

	private static final Set<BlockPos> ANCHOR_CELLS = computeAnchorCells();

	private static Set<BlockPos> computeAnchorCells() {
		Set<BlockPos> anchors = new HashSet<>();
		for (int ring = 0; ring < CardinalRiteRingTuning.RADIUS_BLOCKS.length; ring++) {
			for (int order = 0; order < 4; order++) {
				var anchor = CardinalRiteRingTuning.anchor(ring, order, 1);
				anchors.add(new BlockPos(anchor.x(), 1, anchor.z()));
			}
		}
		return anchors;
	}

	/** How far round the gash a socket-frame offset lies, clockwise from the Focus's left, in radians [0, 2pi). */
	public static double around(int x, int z) {
		double angle = Math.atan2(z, -x);
		return angle < 0.0D ? angle + Math.PI * 2.0D : angle;
	}
}

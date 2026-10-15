package com.vincenthuto.hemomancy.common.rite.floor;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Where a Cardinal Rite reaches for its braziers: anywhere in the N×N×N space above
 * its floor, the Focus at the centre of its bottom layer, whether a brazier stands
 * on the floor, mounts on a wall or hangs from a ceiling. Two kinds of cell stay
 * clear: the column over the Focus, where the staff stands and the daemon rises,
 * and the floor-level cells where a ceremony anchor sore opens. Cells are offsets
 * in the floor's socket frame (x right, y up, z along its forwards), ordered
 * clockwise round the Focus from its left, then nearest, then lowest: the order
 * the station matcher reports offerings in and succession rites read roles by.
 */
public final class CardinalRiteBrazierSpace {
	private static final ConcurrentHashMap<Integer, List<BlockPos>> CELLS = new ConcurrentHashMap<>();

	private CardinalRiteBrazierSpace() {
	}

	/** Every cell a brazier may count from above a floor {@code half} blocks from Focus to edge. */
	public static List<BlockPos> cells(int half) {
		return CELLS.computeIfAbsent(half, CardinalRiteBrazierSpace::compute);
	}

	/** Whether a socket-frame offset lies in the space above a floor {@code half} blocks from Focus to edge. */
	public static boolean contains(int half, int x, int y, int z) {
		if (Math.abs(x) > half || Math.abs(z) > half || y < 1 || y > 2 * half || x == 0 && z == 0) return false;
		return y != 1 || !CardinalRiteGashRules.anchorCells().contains(new BlockPos(x, 1, z));
	}

	private static List<BlockPos> compute(int half) {
		Set<BlockPos> anchors = CardinalRiteGashRules.anchorCells();
		List<BlockPos> cells = new ArrayList<>();
		for (int x = -half; x <= half; x++) {
			for (int z = -half; z <= half; z++) {
				if (x == 0 && z == 0) continue;
				for (int y = 1; y <= 2 * half; y++) {
					BlockPos cell = new BlockPos(x, y, z);
					if (y == 1 && anchors.contains(cell)) continue;
					cells.add(cell);
				}
			}
		}
		cells.sort(Comparator.comparingDouble((BlockPos cell) -> CardinalRiteGashRules.around(cell.getX(), cell.getZ()))
				.thenComparingDouble(cell -> Math.hypot(cell.getX(), cell.getZ()))
				.thenComparingInt(BlockPos::getY));
		return List.copyOf(cells);
	}
}

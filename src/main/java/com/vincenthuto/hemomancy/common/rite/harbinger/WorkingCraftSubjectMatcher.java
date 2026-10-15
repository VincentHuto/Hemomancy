package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.WorkingCraftBed;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Finds a Working craft's subject standing in one side's bed beside the
 * Cardinal Focus, in any horizontal rotation, resting on the floor.
 */
public final class WorkingCraftSubjectMatcher {
	public record SubjectMatch(Direction side, List<BlockPos> positions, List<BlockState> states) {
		public SubjectMatch {
			positions = List.copyOf(positions);
			states = List.copyOf(states);
		}
	}

	private WorkingCraftSubjectMatcher() {
	}

	public static Optional<SubjectMatch> find(LevelReader level, BlockPos focusPos, CardinalRiteType floorTier,
			MultiblockPattern subject) {
		if (subject == null) return Optional.empty();
		WorkingCraftBed bed = WorkingCraftBed.forTier(floorTier);
		BlockPattern pattern = subject.getBlockPattern();
		List<int[]> solid = solidCells(subject.getPatternArray());
		if (solid.isEmpty()) return Optional.empty();
		int[] first = solid.getFirst();
		for (Direction side : Direction.Plane.HORIZONTAL) {
			Direction across = side.getClockWise();
			for (int outward = 1; outward <= bed.depth(); outward++) {
				for (int lateral = -bed.width() / 2; lateral <= bed.width() / 2; lateral++) {
					for (int up = 1; up <= WorkingCraftBed.MAX_HEIGHT; up++) {
						BlockPos bedCell = focusPos.relative(side, outward).relative(across, lateral).above(up);
						for (Direction forwards : Direction.Plane.HORIZONTAL) {
							BlockPos origin = bedCell.subtract(cellOffset(first, forwards));
							BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, Direction.UP);
							if (match == null) continue;
							List<BlockPos> positions = new ArrayList<>(solid.size());
							for (int[] cell : solid) positions.add(match.getBlock(cell[0], cell[1], cell[2]).getPos().immutable());
							if (!restsInBed(bed, focusPos, side, positions)) continue;
							return Optional.of(new SubjectMatch(side, positions,
									positions.stream().map(level::getBlockState).toList()));
						}
					}
				}
			}
		}
		return Optional.empty();
	}

	/** Every cell lies in this side's bed, and the lowest cells stand on the floor's upper layer. */
	static boolean restsInBed(WorkingCraftBed bed, BlockPos focusPos, Direction side, List<BlockPos> positions) {
		Direction across = side.getClockWise();
		int lowest = Integer.MAX_VALUE;
		for (BlockPos pos : positions) {
			int dx = pos.getX() - focusPos.getX();
			int dz = pos.getZ() - focusPos.getZ();
			int outward = dx * side.getStepX() + dz * side.getStepZ();
			int lateral = dx * across.getStepX() + dz * across.getStepZ();
			int up = pos.getY() - focusPos.getY();
			if (!bed.contains(lateral, outward, up)) return false;
			lowest = Math.min(lowest, up);
		}
		return lowest == 1;
	}

	/**
	 * Where pattern cell (x, y, z) sits relative to the match origin, following
	 * vanilla {@link BlockPattern}: x runs along forwards × up, y runs down from
	 * the top row, z runs along forwards.
	 */
	private static Vec3i cellOffset(int[] cell, Direction forwards) {
		Vec3i right = forwards.getNormal().cross(Direction.UP.getNormal());
		return new Vec3i(right.getX() * cell[0] + forwards.getStepX() * cell[2],
				-cell[1],
				right.getZ() * cell[0] + forwards.getStepZ() * cell[2]);
	}

	/** The {x, y, z} index of every non-space cell, in pattern order. */
	static List<int[]> solidCells(String[][] pattern) {
		List<int[]> cells = new ArrayList<>();
		for (int z = 0; z < pattern.length; z++) {
			for (int y = 0; y < pattern[z].length; y++) {
				String row = pattern[z][y];
				for (int x = 0; x < row.length(); x++) {
					if (row.charAt(x) != ' ') cells.add(new int[] {x, y, z});
				}
			}
		}
		return cells;
	}
}

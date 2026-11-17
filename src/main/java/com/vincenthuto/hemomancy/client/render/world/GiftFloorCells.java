package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The real floor under a Saint gift's field (Bloom of Rot, Endless Hour): the top faces round a centre within a few
 * blocks of its height, with room above and no water or lava, nearest first and at most 260. Cached per centre and
 * reach, so a field scans its floor once. The gifts draw over these cells and never change a block.
 */
final class GiftFloorCells {
	private static final int MAX_CELLS = 260;
	private static final Map<String, List<Vec3>> CELLS = new LinkedHashMap<>(16, 0.75F, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, List<Vec3>> eldest) {
			return size() > 12;
		}
	};

	private GiftFloorCells() {
	}

	/** The cells round {@code centre} out to {@code radius}, each the middle of its top face, offset from {@code anchor}. */
	static List<Vec3> cells(Vec3 centre, Vec3 anchor, double radius, double depth) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return List.of();
		BlockPos base = BlockPos.containing(centre.x, centre.y + 0.01D, centre.z);
		List<Vec3> world = CELLS.computeIfAbsent(base.asLong() + ":" + radius, key -> scan(level, base, radius, (int) depth));
		List<Vec3> out = new ArrayList<>(world.size());
		for (Vec3 cell : world) out.add(cell.subtract(anchor));
		return out;
	}

	private static List<Vec3> scan(ClientLevel level, BlockPos base, double radius, int depth) {
		int r = (int) Math.ceil(radius);
		List<Vec3> cells = new ArrayList<>();
		for (int dx = -r; dx <= r; dx++)
			for (int dz = -r; dz <= r; dz++) {
				if (dx * dx + dz * dz > radius * radius) continue;
				for (int dy = depth; dy >= -depth - 1; dy--) {
					BlockPos floor = base.offset(dx, dy - 1, dz);
					BlockState state = level.getBlockState(floor);
					BlockState above = level.getBlockState(floor.above());
					if (!state.getFluidState().isEmpty() || !above.getFluidState().isEmpty()) continue;
					if (state.isFaceSturdy(level, floor, Direction.UP) && !above.isSolidRender(level, floor.above())) {
						cells.add(new Vec3(floor.getX() + 0.5D, floor.getY() + 1.0D, floor.getZ() + 0.5D));
						break;
					}
				}
			}
		Vec3 middle = new Vec3(base.getX() + 0.5D, base.getY(), base.getZ() + 0.5D);
		cells.sort(Comparator.comparingDouble(cell -> cell.distanceToSqr(middle)));
		return cells.size() > MAX_CELLS ? List.copyOf(cells.subList(0, MAX_CELLS)) : List.copyOf(cells);
	}
}

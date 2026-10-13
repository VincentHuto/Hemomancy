package com.vincenthuto.hemomancy.common.rite.floor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/** World positions of a floor built around a Focus, in the orientation the station matcher accepted. */
public final class CardinalRiteFloorGeometry {
	private CardinalRiteFloorGeometry() {
	}

	/** Every floor block except the Focus itself. */
	public static List<BlockPos> cells(CardinalRiteFloorDefinition floor, BlockPos focus, Direction forwards) {
		String[][] layout = floor.pattern().getPatternArray();
		List<BlockPos> cells = new ArrayList<>();
		for (int z = 0; z < layout.length; z++) {
			String row = layout[z][floor.focus().getY()];
			for (int x = 0; x < row.length(); x++) {
				if (row.charAt(x) == ' ' || x == floor.focus().getX() && z == floor.focus().getZ()) continue;
				cells.add(toWorld(focus, forwards, x - floor.focus().getX(), 0, z - floor.focus().getZ()));
			}
		}
		return cells;
	}

	/** The air block a brazier stands in, matching the station matcher's socket offsets. */
	public static List<BlockPos> sockets(CardinalRiteFloorDefinition floor, BlockPos focus, Direction forwards) {
		List<BlockPos> sockets = new ArrayList<>(floor.brazierSockets().size());
		for (BlockPos socket : floor.brazierSockets()) {
			sockets.add(toWorld(focus, forwards, socket.getX(), socket.getY(), socket.getZ()));
		}
		return sockets;
	}

	/** The ring of blocks just outside the floor's square, level with it. */
	public static List<BlockPos> margin(CardinalRiteFloorDefinition floor, BlockPos focus) {
		int reach = halfWidth(floor) + 1;
		List<BlockPos> margin = new ArrayList<>();
		for (int x = -reach; x <= reach; x++) {
			for (int z = -reach; z <= reach; z++) {
				if (Math.max(Math.abs(x), Math.abs(z)) == reach) margin.add(focus.offset(x, 0, z));
			}
		}
		return margin;
	}

	public static int halfWidth(CardinalRiteFloorDefinition floor) {
		return Math.max(floor.focus().getX(), floor.focus().getZ());
	}

	private static BlockPos toWorld(BlockPos focus, Direction forwards, int right, int up, int ahead) {
		var r = forwards.getNormal().cross(Direction.UP.getNormal());
		return focus.offset(r.getX() * right + forwards.getStepX() * ahead, up,
				r.getZ() * right + forwards.getStepZ() * ahead);
	}
}

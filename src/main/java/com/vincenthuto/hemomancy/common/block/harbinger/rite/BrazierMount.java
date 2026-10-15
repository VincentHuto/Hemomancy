package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * How an Iron Brazier is mounted: standing on a block, on the side of one (a sconce on a bracket arm, its spout
 * facing out from the wall) or hanging on chains from the underside of one. The bowl is the same in each; a sconce
 * holds it a quarter-block lower and a hanging brazier lower still, so its talons clear the ceiling. Everything that
 * reaches for the bowl (the floating offering, the fire, the daemon) asks here. The numbers match
 * tools/model_export/iron_brazier.py.
 */
public final class BrazierMount {
	/** How far a sconce's bowl sits below a standing one's, in blocks. */
	public static final double WALL_DROP = -4.0D / 16.0D;
	/** How far a hanging bowl sits below a standing one's, in blocks. */
	public static final double CEILING_DROP = -6.0D / 16.0D;
	/** How far above a standing bowl, out from a sconce or beside a hanging bowl the daemon hangs to drink. */
	private static final double DAEMON_REACH = 1.2D;
	/** A hanging bowl is drunk from below, so the daemon stays clear of the ceiling it hangs from. */
	private static final double DAEMON_BELOW_HANGING = 0.9D;

	// Model-space boxes in sixteenths, for the north-facing brazier (spout at z = 0; a sconce's wall at z = 16).
	private static final double[][] FLOOR = {{3, 0, 3, 13, 2.2, 13}, {6, 2.2, 6, 10, 8.4, 10},
			{1.5, 8.4, 1.5, 14.5, 18.5, 14.5}};
	private static final double[][] WALL = {{1.5, 4.4, 1.5, 14.5, 14.5, 14.5}, {6.5, 2.0, 9.0, 9.5, 4.4, 16},
			{4, 1, 15, 12, 13, 16}};
	private static final double[][] CEILING = {{1.5, 2.4, 1.5, 14.5, 12.5, 14.5}, {3, 12.5, 3, 13, 16, 13}};
	private static final Map<AttachFace, Map<Direction, VoxelShape>> SHAPES = new EnumMap<>(AttachFace.class);

	static {
		for (AttachFace face : AttachFace.values()) {
			Map<Direction, VoxelShape> byFacing = new EnumMap<>(Direction.class);
			double[][] boxes = face == AttachFace.FLOOR ? FLOOR : face == AttachFace.WALL ? WALL : CEILING;
			for (Direction facing : Direction.Plane.HORIZONTAL) byFacing.put(facing, rotated(boxes, facing));
			SHAPES.put(face, byFacing);
		}
	}

	private BrazierMount() {
	}

	/** The mount of a brazier state; anything without one (older states) stands. */
	public static AttachFace face(BlockState state) {
		return state.hasProperty(BlockStateProperties.ATTACH_FACE) ? state.getValue(BlockStateProperties.ATTACH_FACE)
				: AttachFace.FLOOR;
	}

	/** Which way the brazier's spout faces: out from a sconce's wall. */
	public static Direction facing(BlockState state) {
		return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
				? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
	}

	/** How far this mount holds the bowl below a standing brazier's, in blocks. */
	public static double drop(AttachFace face) {
		return switch (face) {
			case FLOOR -> 0.0D;
			case WALL -> WALL_DROP;
			case CEILING -> CEILING_DROP;
		};
	}

	/** A point over the bowl's centre {@code height} above a standing brazier's block centre, moved with the bowl. */
	public static Vec3 bowl(AttachFace face, BlockPos pos, double height) {
		return Vec3.atCenterOf(pos).add(0.0D, height + drop(face), 0.0D);
	}

	/** {@link #bowl(AttachFace, BlockPos, double)} for a placed brazier. */
	public static Vec3 bowl(BlockState state, BlockPos pos, double height) {
		return bowl(face(state), pos, height);
	}

	/** Where the daemon hangs to drink a brazier's offering: never inside the wall or ceiling it is mounted on. */
	public static Vec3 daemonStop(AttachFace face, Direction facing, BlockPos pos, Vec3 focus) {
		return switch (face) {
			case FLOOR -> bowl(face, pos, DAEMON_REACH);
			case WALL -> bowl(face, pos, 0.75D).add(facing.getStepX() * DAEMON_REACH, 0.0D,
					facing.getStepZ() * DAEMON_REACH);
			case CEILING -> {
				Vec3 bowl = bowl(face, pos, 0.75D);
				Vec3 toward = new Vec3(focus.x - bowl.x, 0.0D, focus.z - bowl.z);
				toward = toward.lengthSqr() < 1.0e-4D ? new Vec3(facing.getStepX(), 0.0D, facing.getStepZ())
						: toward.normalize();
				yield bowl.add(toward.scale(DAEMON_REACH)).add(0.0D, -DAEMON_BELOW_HANGING, 0.0D);
			}
		};
	}

	/** {@link #daemonStop(AttachFace, Direction, BlockPos, Vec3)} for a placed brazier. */
	public static Vec3 daemonStop(BlockState state, BlockPos pos, Vec3 focus) {
		return daemonStop(face(state), facing(state), pos, focus);
	}

	/** The brazier's outline and collision for a mount and facing. */
	public static VoxelShape shape(AttachFace face, Direction facing) {
		return SHAPES.get(face).get(facing.getAxis().isHorizontal() ? facing : Direction.NORTH);
	}

	/**
	 * The mount a brazier takes when placed toward {@code looking} (the first of the player's nearest looking
	 * directions, which leads with the clicked face's opposite): onto a block's top, a wall's side or a ceiling.
	 */
	public static AttachFace placement(Direction looking) {
		return switch (looking) {
			case DOWN -> AttachFace.FLOOR;
			case UP -> AttachFace.CEILING;
			default -> AttachFace.WALL;
		};
	}

	/** The direction from a brazier to the block holding it up. */
	public static Direction support(AttachFace face, Direction facing) {
		return switch (face) {
			case FLOOR -> Direction.DOWN;
			case CEILING -> Direction.UP;
			case WALL -> facing.getOpposite();
		};
	}

	/** Model-space boxes turned about the block's vertical axis as the blockstate turns the model for {@code facing}. */
	private static VoxelShape rotated(double[][] boxes, Direction facing) {
		int turns = switch (facing) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
		VoxelShape shape = Shapes.empty();
		for (double[] box : boxes) {
			double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
			for (int i = 0; i < turns; i++) {
				// A quarter turn clockwise seen from above: north becomes east.
				double nx0 = 16.0D - z1, nx1 = 16.0D - z0;
				z0 = x0;
				z1 = x1;
				x0 = nx0;
				x1 = nx1;
			}
			shape = Shapes.or(shape, Shapes.box(x0 / 16.0D, box[1] / 16.0D, z0 / 16.0D, x1 / 16.0D, box[4] / 16.0D,
					z1 / 16.0D));
		}
		return shape.optimize();
	}
}

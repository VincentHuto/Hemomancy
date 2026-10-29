package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.BloodBasinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Hemorath's floor gutter: a deepslate-tile floor block with a blood channel cut into it, joining its neighbours
 * like a pane. It connects to other gutters, to a {@link VenousBasinBlock} whose spout faces it, and to a
 * {@link BloodBasinBlock}. {@link #FILL} is how wet the channel is (0 dry, 1 stained, 2 half, 3 full); structures
 * set it, nothing changes it. A full block to walk on. Model from tools/model_export/blood_gutter.py.
 */
public class BloodGutterBlock extends Block {
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	/** How much blood stands in a gutter or basin: 0 dry, 1 stained, 2 half, 3 full. */
	public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 3);
	/** Below the channel's sump the block is solid; light and sight pass over it through the channel. */
	private static final VoxelShape OCCLUSION = Block.box(0, 0, 0, 16, 8, 16);

	public BloodGutterBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false)
				.setValue(SOUTH, false).setValue(WEST, false).setValue(FILL, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, FILL);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState();
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos next = context.getClickedPos().relative(side);
			state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side),
					connectsTo(context.getLevel().getBlockState(next), side));
		}
		return state;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction side, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (side.getAxis().isHorizontal()) {
			return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side), connectsTo(neighbour, side));
		}
		return state;
	}

	/** Whether a gutter's channel runs on into the block on its {@code side}. */
	public static boolean connectsTo(BlockState neighbour, Direction side) {
		if (neighbour.getBlock() instanceof BloodGutterBlock || neighbour.getBlock() instanceof BloodBasinBlock) {
			return true;
		}
		return neighbour.getBlock() instanceof VenousBasinBlock
				&& neighbour.getValue(VenousBasinBlock.FACING) == side.getOpposite();
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return OCCLUSION;
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		BlockState turned = state;
		for (Direction side : Direction.Plane.HORIZONTAL) {
			turned = turned.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(rotation.rotate(side)),
					state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side)));
		}
		return turned;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		BlockState mirrored = state;
		for (Direction side : Direction.Plane.HORIZONTAL) {
			mirrored = mirrored.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(mirror.mirror(side)),
					state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side)));
		}
		return mirrored;
	}
}

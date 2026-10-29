package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Runoff frozen down a wall face in Velorum's monastery: a thin film of ice with ribbons standing proud of it.
 * {@link #FACING} is the side it faces into the room; it hangs on the face behind, which must be sturdy. {@link #END}
 * is the lowest cell of a run, where the film gives out and the ribbons taper into beads and short icicles; a drip
 * placed under another of the same facing turns the one above into sheet, and a drip left over air ends. No
 * collision; drops only with Silk Touch. Models from tools/model_export/velorum_materials.py.
 */
public class FrozenDripBlock extends Block {
	public static final MapCodec<FrozenDripBlock> CODEC = simpleCodec(FrozenDripBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty END = BooleanProperty.create("end");
	// The models' extent for facing=north (velorum_materials.py prints it): x 2-14, z 12.25-16.
	private static final VoxelShape NORTH = box(2, 0, 12, 14, 16, 16);
	private static final VoxelShape SOUTH = box(2, 0, 0, 14, 16, 4);
	private static final VoxelShape WEST = box(12, 0, 2, 16, 16, 14);
	private static final VoxelShape EAST = box(0, 0, 2, 4, 16, 14);

	public FrozenDripBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(END, true));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, END);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> NORTH;
		};
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos support = pos.relative(facing.getOpposite());
		return level.getBlockState(support).isFaceSturdy(level, support, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction side, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (side == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		if (side == Direction.DOWN) {
			if (continues(state, neighbour)) {
				return state.setValue(END, false);
			}
			if (neighbour.isAir()) {
				return state.setValue(END, true);
			}
		}
		return super.updateShape(state, side, neighbour, level, pos, neighbourPos);
	}

	private boolean continues(BlockState state, BlockState below) {
		return below.is(this) && below.getValue(FACING) == state.getValue(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState below = level.getBlockState(pos.below());
		for (Direction looking : context.getNearestLookingDirections()) {
			if (looking.getAxis().isHorizontal()) {
				BlockState placed = defaultBlockState().setValue(FACING, looking.getOpposite());
				if (placed.canSurvive(level, pos)) {
					return placed.setValue(END, !continues(placed, below));
				}
			}
		}
		return null;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}

package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mausoleum set piece several blocks tall, one block per storey, each storey carrying its own model. The structure
 * templates place every storey; when one goes, the rest follow, so a broken piece never leaves half a figure standing.
 * Placed by hand, the item puts down the floor storey facing the player and stacks the rest above it, if they fit.
 */
public abstract class StackedSetPieceBlock extends Block {
	protected StackedSetPieceBlock(Properties properties) {
		super(properties);
	}

	/** This cell's storey, 0 at the floor. */
	protected abstract int storey(BlockState state);

	protected abstract int storeys();

	/** {@code state} as the given storey, keeping its other properties. */
	protected abstract BlockState withStorey(BlockState state, int storey);

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() + storeys() > level.getMaxBuildHeight()) return null;
		for (int storey = 1; storey < storeys(); storey++) {
			if (!level.getBlockState(pos.above(storey)).canBeReplaced(context)) return null;
		}
		BlockState state = withStorey(defaultBlockState(), 0);
		return state.hasProperty(HorizontalDirectionalBlock.FACING)
				? state.setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite())
				: state;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		for (int storey = 1; storey < storeys(); storey++) {
			level.setBlock(pos.above(storey), withStorey(state, storey), Block.UPDATE_ALL);
		}
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (direction.getAxis() == Direction.Axis.Y) {
			int partner = storey(state) + (direction == Direction.UP ? 1 : -1);
			if (partner >= 0 && partner < storeys() && (!neighbour.is(this) || storey(neighbour) != partner)) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}
}

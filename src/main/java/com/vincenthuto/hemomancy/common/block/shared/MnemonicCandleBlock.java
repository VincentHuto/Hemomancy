package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MnemonicCandleBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 13.0D, 13.5D);

	public MnemonicCandleBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context))
				.setValue(LIT, false);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		toggle(state, level, pos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		toggle(state, level, pos);
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	private void toggle(BlockState state, Level level, BlockPos pos) {
		if (level.isClientSide) {
			return;
		}
		boolean lit = !state.getValue(LIT);
		level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL);
		level.playSound(null, pos, lit ? SoundEvents.CANDLE_PLACE : SoundEvents.CANDLE_EXTINGUISH,
				SoundSource.BLOCKS, 0.65F, lit ? 0.85F : 0.7F);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT) || state.getValue(WATERLOGGED)) {
			return;
		}
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.84D;
		double z = pos.getZ() + 0.5D;
		if (random.nextFloat() < 0.85F) {
			level.addParticle(ParticleTypes.SMOKE, x, y + 0.05D, z,
					(random.nextDouble() - 0.5D) * 0.01D, 0.018D, (random.nextDouble() - 0.5D) * 0.01D);
		}
		if (random.nextFloat() < 0.45F) {
			level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0D, 0.004D, 0.0D);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}

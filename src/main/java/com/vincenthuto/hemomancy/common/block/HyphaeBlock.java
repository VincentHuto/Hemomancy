package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HyphaeBlock extends BushBlock implements BonemealableBlock, net.minecraftforge.common.IForgeShearable {
	protected static final float AABB_OFFSET = 6.0F;
	protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

	public HyphaeBlock(BlockBehaviour.Properties pProperties) {
		super(pProperties);
	}

	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}

	/**
	 * @return whether bonemeal can be used on this block
	 */
	public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
		return true;
	}

	public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
		return true;
	}

	public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
		DoublePlantBlock doubleplantblock = (DoublePlantBlock) (pState.is(Blocks.FERN) ? Blocks.LARGE_FERN
				: Blocks.TALL_GRASS);
		if (doubleplantblock.defaultBlockState().canSurvive(pLevel, pPos) && pLevel.isEmptyBlock(pPos.above())) {
			DoublePlantBlock.placeAt(pLevel, doubleplantblock.defaultBlockState(), pPos, 2);
		}

	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockPos blockpos = pPos.below();
		if (pState.getBlock() == this) // Forge: This function is called during world gen and placement, before this
										// block is set, so if we are not 'here' then assume it's the pre-check.
			return pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this)
					|| pState.is(BlockInit.erythrocytic_mycelium.get());
		return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
	}

	@Override
	protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return pState.is(BlockTags.DIRT) || pState.is(Blocks.FARMLAND)
				|| pState.is(BlockInit.erythrocytic_mycelium.get());
	}
}

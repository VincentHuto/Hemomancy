package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedFungusBlock extends FlowerBlock {

	public InfectedFungusBlock(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public MobEffect getSuspiciousEffect() {
		return EffectInit.blood_loss.get();
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockPos blockpos = pPos.below();
		if (pState.getBlock() == this) // Forge: This function is called during world gen and placement, before this
										// block is set, so if we are not 'here' then assume it's the pre-check.
			return pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this)
					|| pLevel.getBlockState(blockpos).getBlock() == BlockInit.erythrocytic_mycelium.get();
		return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
	}
}

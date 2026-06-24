package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

public class BleedingHeartBlock extends FlowerBlock {

	public BleedingHeartBlock(Holder<MobEffect> effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockPos blockpos = pPos.below();
		BlockState belowState = pLevel.getBlockState(blockpos);
		if (pState.getBlock() == this) // Forge: This function is called during world gen and placement, before this
										// block is set, so if we are not 'here' then assume it's the pre-check.
		{
			TriState soilDecision = belowState.canSustainPlant(pLevel, blockpos, Direction.UP, pState);
			if (!soilDecision.isDefault()) {
				return soilDecision.isTrue();
			}
			return this.mayPlaceOn(belowState, pLevel, blockpos)
					|| belowState.getBlock() == BlockInit.erythrocytic_mycelium.get();
		}
		return this.mayPlaceOn(belowState, pLevel, blockpos);
	}
}

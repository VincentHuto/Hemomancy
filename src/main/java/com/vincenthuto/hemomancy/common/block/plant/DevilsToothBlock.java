package com.vincenthuto.hemomancy.common.block.plant;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DevilsToothBlock extends FlowerBlock {

    public DevilsToothBlock(MobEffect effect, int effectDuration, Properties properties) {
        super(effect, effectDuration, properties);
    }

    @Override
    public MobEffect getSuspiciousEffect() {
        return EffectInit.blood_loss.get();
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos below = pPos.below();
        BlockState belowState = pLevel.getBlockState(below);
        if (pState.getBlock() == this)
            return belowState.canSustainPlant(pLevel, below, Direction.UP, this)
                    || belowState.is(BlockTags.LOGS);
        return this.mayPlaceOn(belowState, pLevel, below);
    }
}

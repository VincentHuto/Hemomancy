package com.vincenthuto.hemomancy.common.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

public class DevilsToothBlock extends FlowerBlock {

    public DevilsToothBlock(Holder<MobEffect> effect, int effectDuration, Properties properties) {
        super(effect, effectDuration, properties);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos below = pPos.below();
        BlockState belowState = pLevel.getBlockState(below);
        if (pState.getBlock() == this)
            return belowState.canSustainPlant(pLevel, below, Direction.UP, this.defaultBlockState()) == TriState.TRUE
                    || belowState.is(BlockTags.LOGS);
        return this.mayPlaceOn(belowState, pLevel, below);
    }
}

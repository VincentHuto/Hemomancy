package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.init.EntityInit;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.TriState;

public class DevilsToothBlock extends FlowerBlock {

    private static final float TOOTH_PECK_SPAWN_CHANCE = 0.08F;

    public DevilsToothBlock(Holder<MobEffect> effect, int effectDuration, Properties properties) {
        super(effect, effectDuration, properties);
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState,
            @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        super.playerDestroy(pLevel, pPlayer, pPos, pState, pBlockEntity, pTool);
        if (!(pLevel instanceof ServerLevel serverLevel) || pPlayer.isCreative()) {
            return;
        }
        if (serverLevel.getRandom().nextFloat() >= TOOTH_PECK_SPAWN_CHANCE) {
            return;
        }

        var toothPeck = EntityInit.tooth_pecks.get().create(serverLevel);
        if (toothPeck == null) {
            return;
        }

        toothPeck.moveTo(pPos.getX() + 0.5D, pPos.getY(), pPos.getZ() + 0.5D,
                serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
        toothPeck.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pPos), MobSpawnType.TRIGGERED, null);
        serverLevel.addFreshEntityWithPassengers(toothPeck);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos below = pPos.below();
        BlockState belowState = pLevel.getBlockState(below);
        if (pState.getBlock() == this)
            return belowState.canSustainPlant(pLevel, below, Direction.UP, this.defaultBlockState()) == TriState.TRUE
                    || belowState.is(BlockTags.LOGS)
                    || this.mayPlaceOn(belowState, pLevel, below);
        return this.mayPlaceOn(belowState, pLevel, below);
    }
}

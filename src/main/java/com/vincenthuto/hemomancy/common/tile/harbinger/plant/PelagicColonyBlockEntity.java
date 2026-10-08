package com.vincenthuto.hemomancy.common.tile.harbinger.plant;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.PelagicColonyBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class PelagicColonyBlockEntity extends BlockEntity {
    private int retractTicks;
    public float extension = 1, previousExtension = 1;
    public PelagicColonyBlockEntity(BlockPos pos, BlockState state) { super(BlockEntityInit.pelagic_colony.get(), pos, state); }
    public void retract() { retractTicks = 60; setChanged(); setRetracted(true); }
    private void setRetracted(boolean retracted) {
        var state = getBlockState();
        if (level == null || state.getValue(PelagicColonyBlock.RETRACTED) == retracted) return;
        level.setBlock(worldPosition, state.setValue(PelagicColonyBlock.RETRACTED, retracted), 3);
        if (((PelagicColonyBlock)state.getBlock()).kind == PelagicColonyBlock.Kind.TUBE_WORM) {
            var upper = level.getBlockState(worldPosition.above());
            if (upper.is(state.getBlock())) level.setBlock(worldPosition.above(), upper.setValue(PelagicColonyBlock.RETRACTED, retracted), 3);
        }
    }
    public static void tick(Level level, BlockPos pos, BlockState state, PelagicColonyBlockEntity colony) {
        if (level.isClientSide) {
            colony.previousExtension = colony.extension;
            colony.extension += ((state.getValue(PelagicColonyBlock.RETRACTED) ? .06F : 1F) - colony.extension) * .18F;
            return;
        }
        if (colony.retractTicks > 0 && --colony.retractTicks == 0) colony.setRetracted(false);
        if ((level.getGameTime() + pos.asLong()) % 10 != 0) return;
        if (!state.canSurvive(level, pos)) { level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3); return; }
        var player = level.getNearestPlayer(pos.getX() + .5, pos.getY() + .6, pos.getZ() + .5, 1.6, false);
        if (player != null && !player.isSpectator() && !player.isCreative()) colony.retract();
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.putInt("RetractTicks", retractTicks);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); retractTicks = Math.max(0, tag.getInt("RetractTicks"));
    }
}

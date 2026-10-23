package com.vincenthuto.hemomancy.common.tile.shared;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink;
import net.minecraft.world.entity.player.Player;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;

import javax.annotation.Nullable;

public class FillerBlockEntity extends HemoBlockEntity implements IBloodReservoir {

    private BlockPos mainBlockPos = null;

    public FillerBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityInit.filler_block.get(), pos, state);
    }

    protected FillerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setMainBlockPos(BlockPos mainPos) {
        this.mainBlockPos = mainPos;
        setChanged();
    }

    @Nullable
    public BlockPos getMainBlockPos() {
        return mainBlockPos;
    }

    private IBloodReservoir mainReservoir() {
        if (level == null || mainBlockPos == null) return null;
        return level.getBlockEntity(mainBlockPos) instanceof IBloodReservoir reservoir ? reservoir : null;
    }

    @Override public IBloodVolume getBloodCapability() {
        IBloodReservoir reservoir = mainReservoir();
        return reservoir == null ? null : reservoir.getBloodCapability();
    }

    @Override public boolean canReceiveBlood() {
        IBloodReservoir reservoir = mainReservoir();
        return reservoir != null && reservoir.canReceiveBlood();
    }

    @Override public boolean canProvideBlood() {
        IBloodReservoir reservoir = mainReservoir();
        return reservoir != null && reservoir.canProvideBlood();
    }

    /** Projection aimed at a filler reaches the multiblock it belongs to. */
    @Override public double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff) {
        if (mainBlockPos == null) return IBloodReservoir.super.takeProjectedBlood(player, hitPos, rate, livingStaff);
        return level != null && level.getBlockEntity(mainBlockPos) instanceof ProjectedBloodSink sink
                ? sink.takeProjectedBlood(player, hitPos, rate, livingStaff) : 0.0D;
    }

    @Override public void sendUpdates() {
        IBloodReservoir reservoir = mainReservoir();
        if (reservoir != null) reservoir.sendUpdates();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (mainBlockPos != null) {
            tag.putInt("MainX", mainBlockPos.getX());
            tag.putInt("MainY", mainBlockPos.getY());
            tag.putInt("MainZ", mainBlockPos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("MainX")) {
            mainBlockPos = new BlockPos(tag.getInt("MainX"), tag.getInt("MainY"), tag.getInt("MainZ"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

}


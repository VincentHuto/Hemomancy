package com.huto.hemomancy.capabilities.mindrunes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class RunesContainerProvider implements INBTSerializable<CompoundNBT>, ICapabilityProvider {

    private RunesContainer inner;
    private LazyOptional<IRunesItemHandler> opt;

    public RunesContainerProvider(PlayerEntity player) {
        this.inner = new RunesContainer(player);
        this.opt = LazyOptional.of(() -> inner);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        return RunesCapabilities.RUNES.orEmpty(capability, opt);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return this.inner.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.inner.deserializeNBT(nbt);
    }
}
package com.huto.hemomancy.capa.rune;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class RunesContainerProvider implements INBTSerializable<CompoundTag>, ICapabilityProvider {

	private RunesContainer inner;
	private LazyOptional<IRunesItemHandler> opt;

	public RunesContainerProvider(Player player) {
		this.inner = new RunesContainer(player);
		this.opt = LazyOptional.of(() -> inner);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
		return RunesCapabilities.RUNES.orEmpty(capability, opt);
	}

	@Override
	public CompoundTag serializeNBT() {
		return this.inner.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.inner.deserializeNBT(nbt);
	}
}
package com.huto.hemomancy.capa.vascular;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class VascularSystemProvider implements ICapabilitySerializable<INBT> {
	@CapabilityInject(IVascularSystem.class)
	public static final Capability<IVascularSystem> VASCULAR_CAPA = null;
	private LazyOptional<IVascularSystem> instance = LazyOptional.of(VASCULAR_CAPA::getDefaultInstance);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return VASCULAR_CAPA.orEmpty(cap, instance);

	}

	@Override
	public INBT serializeNBT() {
		return (INBT) VASCULAR_CAPA.getStorage().writeNBT(VASCULAR_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		VASCULAR_CAPA.getStorage().readNBT(VASCULAR_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static Map<EnumVeinSections, Float> getPlayerVascularSystem(PlayerEntity player) {
		return player.getCapability(VASCULAR_CAPA).orElseThrow(IllegalStateException::new).getVascularSystem();
	}

}

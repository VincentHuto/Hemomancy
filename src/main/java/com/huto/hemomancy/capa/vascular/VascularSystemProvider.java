package com.huto.hemomancy.capa.vascular;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class VascularSystemProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IVascularSystem.class)
	public static final Capability<IVascularSystem> VASCULAR_CAPA = null;
	private LazyOptional<IVascularSystem> instance = LazyOptional.of(VASCULAR_CAPA::getDefaultInstance);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return VASCULAR_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return (Tag) VASCULAR_CAPA.getStorage().writeNBT(VASCULAR_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		VASCULAR_CAPA.getStorage().readNBT(VASCULAR_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static Map<EnumVeinSections, Float> getPlayerVascularSystem(Player player) {
		return player.getCapability(VASCULAR_CAPA).orElseThrow(IllegalStateException::new).getVascularSystem();
	}

}

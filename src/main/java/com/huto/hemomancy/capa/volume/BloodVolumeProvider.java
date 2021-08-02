package com.huto.hemomancy.capa.volume;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class BloodVolumeProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IBloodVolume.class)
	public static final Capability<IBloodVolume> VOLUME_CAPA = null;
	private LazyOptional<IBloodVolume> instance = LazyOptional.of(VOLUME_CAPA::getDefaultInstance);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return VOLUME_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return (Tag) VOLUME_CAPA.getStorage().writeNBT(VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		VOLUME_CAPA.getStorage().readNBT(VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static float getPlayerbloodVolume(Player player) {
		return player.getCapability(VOLUME_CAPA).orElseThrow(IllegalStateException::new).getBloodVolume();
	}

}

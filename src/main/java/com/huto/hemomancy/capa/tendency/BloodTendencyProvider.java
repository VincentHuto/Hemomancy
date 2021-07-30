package com.huto.hemomancy.capa.tendency;

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

public class BloodTendencyProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IBloodTendency.class)
	public static final Capability<IBloodTendency> TENDENCY_CAPA = null;
	private LazyOptional<IBloodTendency> instance = LazyOptional.of(TENDENCY_CAPA::getDefaultInstance);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return TENDENCY_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return (Tag) TENDENCY_CAPA.getStorage().writeNBT(TENDENCY_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		TENDENCY_CAPA.getStorage().readNBT(TENDENCY_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static Map<EnumBloodTendency, Float> getPlayerTendency(Player player) {
		return player.getCapability(TENDENCY_CAPA).orElseThrow(IllegalStateException::new).getTendency();
	}

}

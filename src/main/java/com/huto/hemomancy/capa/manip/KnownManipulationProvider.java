package com.huto.hemomancy.capa.manip;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class KnownManipulationProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IKnownManipulations.class)
	public static final Capability<IKnownManipulations> MANIP_CAPA = null;
	private LazyOptional<IKnownManipulations> instance = LazyOptional.of(MANIP_CAPA::getDefaultInstance);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return MANIP_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return (Tag) MANIP_CAPA.getStorage().writeNBT(MANIP_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		MANIP_CAPA.getStorage().readNBT(MANIP_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static List<BloodManipulation> getPlayerManips(Player player) {
		return player.getCapability(MANIP_CAPA).orElseThrow(IllegalStateException::new).getKnownManips();
	}
}

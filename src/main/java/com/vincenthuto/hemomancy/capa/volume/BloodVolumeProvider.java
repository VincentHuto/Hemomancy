package com.vincenthuto.hemomancy.capa.volume;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class BloodVolumeProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IBloodVolume.class)
	public static final Capability<IBloodVolume> VOLUME_CAPA = null;
	BloodVolume capability = new BloodVolume();
	private LazyOptional<IBloodVolume> instance = LazyOptional.of(() -> capability);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == VOLUME_CAPA ? instance.cast() : LazyOptional.empty();
	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(VOLUME_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);
	}

	public static float getPlayerbloodVolume(Player player) {
		return player.getCapability(VOLUME_CAPA).orElseThrow(IllegalStateException::new).getBloodVolume();
	}

	public CompoundTag writeNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", instance.isActive());
		entry.putFloat("Max", instance.getMaxBloodVolume());
		entry.putFloat("Volume", instance.getBloodVolume());
		return entry;
	}

	public void readNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side, Tag nbt) {
		if (!(instance instanceof BloodVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundTag entry) {
			if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxBloodVolume(entry.getFloat("Max"));
				instance.setBloodVolume(entry.getFloat("Volume"));
			}
		}

	}
}

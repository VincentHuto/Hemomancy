package com.vincenthuto.hemomancy.common.capability.player.silthmere;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SilthmereVolumeProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<ISilthmereVolume> SILTHMERE_VOLUME_CAPA = CapabilityManager
			.get(new CapabilityToken<ISilthmereVolume>() {
			});

	SilthmereVolume capability = new SilthmereVolume();

	private LazyOptional<ISilthmereVolume> instance = LazyOptional.of(() -> capability);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(SILTHMERE_VOLUME_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == SILTHMERE_VOLUME_CAPA ? instance.cast() : LazyOptional.empty();
	}

	public void readNBT(Capability<ISilthmereVolume> capability, ISilthmereVolume instance, Direction side, Tag nbt) {
		if (!(instance instanceof SilthmereVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundTag entry) {
			if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")
					&& entry.contains("Bloodline")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxSilthmereVolume(entry.getDouble("Max"));
				instance.setSilthmereVolume(entry.getDouble("Volume"));
				instance.setSilthmereLine(Bloodline.deserialize(entry.getCompound("Bloodline")));
				// Bloodline pool donation & auto-draw settings
				instance.setTrickleEnabled(entry.getBoolean("TrickleEnabled"));
				instance.setTrickleRate(entry.getDouble("TrickleRate"));
				instance.setAutoDrawEnabled(entry.getBoolean("AutoDrawEnabled"));
				instance.setAutoDrawThreshold(entry.getDouble("AutoDrawThreshold"));
			}
		}

	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(SILTHMERE_VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	public CompoundTag writeNBT(Capability<ISilthmereVolume> capability, ISilthmereVolume instance, Direction side) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", instance.isActive());
		entry.putDouble("Max", instance.getMaxSilthmereVolume());
		entry.putDouble("Volume", instance.getSilthmereVolume());
		entry.put("Bloodline", instance.getSilthmereLine().serialize());
		// Bloodline pool donation & auto-draw settings
		entry.putBoolean("TrickleEnabled", instance.isTrickleEnabled());
		entry.putDouble("TrickleRate", instance.getTrickleRate());
		entry.putBoolean("AutoDrawEnabled", instance.isAutoDrawEnabled());
		entry.putDouble("AutoDrawThreshold", instance.getAutoDrawThreshold());
		return entry;
	}
}

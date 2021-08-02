package com.huto.hemomancy.capa.volume;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class BloodVolumeStorage implements IStorage<IBloodVolume> {

	@Override
	public CompoundTag writeNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", instance.isActive());
		entry.putFloat("Max", instance.getMaxBloodVolume());
		entry.putFloat("Volume", instance.getBloodVolume());
		return entry;
	}

	@Override
	public void readNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side, Tag nbt) {
		if (!(instance instanceof BloodVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundTag) {
			CompoundTag entry = (CompoundTag) nbt;
			if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxBloodVolume(entry.getFloat("Max"));
				instance.setBloodVolume(entry.getFloat("Volume"));
			}
		}

	}
}

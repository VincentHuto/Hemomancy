package com.huto.hemomancy.capa.volume;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class BloodVolumeStorage implements IStorage<IBloodVolume> {

	@Override
	public CompoundNBT writeNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side) {
		CompoundNBT entry = new CompoundNBT();
		entry.putBoolean("Active", instance.isActive());
		entry.putFloat("Max", instance.getMaxBloodVolume());
		entry.putFloat("Volume", instance.getBloodVolume());
		return entry;
	}

	@Override
	public void readNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side, INBT nbt) {
		if (!(instance instanceof BloodVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundNBT) {
			CompoundNBT entry = (CompoundNBT) nbt;
			if (entry.contains("Max") && entry.contains("Volume")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxBloodVolume(entry.getFloat("Max"));
				instance.setBloodVolume(entry.getFloat("Volume"));
			}
		}

	}
}

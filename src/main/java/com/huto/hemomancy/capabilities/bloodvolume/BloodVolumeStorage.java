package com.huto.hemomancy.capabilities.bloodvolume;

import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class BloodVolumeStorage implements IStorage<IBloodVolume> {

	@Override
	public INBT writeNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side) {
		return FloatNBT.valueOf(instance.getBloodVolume());
	}

	@Override
	public void readNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side, INBT nbt) {
		   if (!(instance instanceof BloodVolume))
	            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
	        instance.setBloodVolume(((FloatNBT)nbt).getFloat());
	    }
}

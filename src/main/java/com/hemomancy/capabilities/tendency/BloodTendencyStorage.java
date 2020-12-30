package com.hemomancy.capabilities.tendency;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class BloodTendencyStorage implements IStorage<IBloodTendency> {

	@Override
	public CompoundNBT writeNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side) {
		CompoundNBT covenTag = new CompoundNBT();
		for (EnumBloodTendency key : instance.getDevotion().keySet()) {
			if (instance.getDevotion().get(key) != null) {
				covenTag.putFloat(key.toString(), instance.getDevotion().get(key));
			} else {
				covenTag.putFloat(key.toString(), 0);

			}
		}
		return covenTag;
	}

	@Override
	public void readNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side, INBT nbt) {
		if (!(instance instanceof BloodTendency))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		CompoundNBT test = (CompoundNBT) nbt;
		for (EnumBloodTendency coven : EnumBloodTendency.values()) {
			instance.getDevotion().put(coven, test.getFloat(coven.toString()));
		}
	}

}

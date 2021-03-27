package com.huto.hemomancy.capa.tendency;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class BloodTendencyStorage implements IStorage<IBloodTendency> {

	@Override
	public CompoundNBT writeNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side) {
		CompoundNBT covenTag = new CompoundNBT();
		for (EnumBloodTendency key : instance.getTendency().keySet()) {
			if (instance.getTendency().get(key) != null) {
				covenTag.putFloat(key.toString(), instance.getTendency().get(key));
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
			instance.getTendency().put(coven, test.getFloat(coven.toString()));
		}
	}

}

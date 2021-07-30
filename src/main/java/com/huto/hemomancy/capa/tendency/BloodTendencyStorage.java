package com.huto.hemomancy.capa.tendency;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import CompoundTag;

public class BloodTendencyStorage implements IStorage<IBloodTendency> {

	@Override
	public CompoundTag writeNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side) {
		CompoundTag covenTag = new CompoundTag();
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
	public void readNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side, Tag nbt) {
		if (!(instance instanceof BloodTendency))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		CompoundTag test = (CompoundTag) nbt;
		for (EnumBloodTendency coven : EnumBloodTendency.values()) {
			instance.getTendency().put(coven, test.getFloat(coven.toString()));
		}
	}

}

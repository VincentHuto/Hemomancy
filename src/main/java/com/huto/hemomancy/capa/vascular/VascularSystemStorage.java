package com.huto.hemomancy.capa.vascular;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import CompoundTag;

public class VascularSystemStorage implements IStorage<IVascularSystem> {

	@Override
	public CompoundTag writeNBT(Capability<IVascularSystem> capability, IVascularSystem instance, Direction side) {
		CompoundTag covenTag = new CompoundTag();
		for (EnumVeinSections key : instance.getVascularSystem().keySet()) {
			if (instance.getVascularSystem().get(key) != null) {
				covenTag.putFloat(key.toString(), instance.getVascularSystem().get(key));
			} else {
				covenTag.putFloat(key.toString(), 100f);

			}
		}
		return covenTag;
	}

	@Override
	public void readNBT(Capability<IVascularSystem> capability, IVascularSystem instance, Direction side, Tag nbt) {
		if (!(instance instanceof VascularSystem))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		CompoundTag test = (CompoundTag) nbt;
		for (EnumVeinSections key : EnumVeinSections.values()) {
			instance.getVascularSystem().put(key, test.getFloat(key.toString()));
		}
	}

}

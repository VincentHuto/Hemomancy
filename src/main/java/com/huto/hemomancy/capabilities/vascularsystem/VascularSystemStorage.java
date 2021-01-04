package com.huto.hemomancy.capabilities.vascularsystem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class VascularSystemStorage implements IStorage<IVascularSystem> {

	@Override
	public CompoundNBT writeNBT(Capability<IVascularSystem> capability, IVascularSystem instance, Direction side) {
		CompoundNBT covenTag = new CompoundNBT();
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
	public void readNBT(Capability<IVascularSystem> capability, IVascularSystem instance, Direction side, INBT nbt) {
		if (!(instance instanceof VascularSystem))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		CompoundNBT test = (CompoundNBT) nbt;
		for (EnumVeinSections key : EnumVeinSections.values()) {
			instance.getVascularSystem().put(key, test.getFloat(key.toString()));
		}
	}

}

package com.huto.hemomancy.capabilities.manipulation;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class KnownManipulationStorage implements IStorage<IKnownManipulations> {

	@Override
	public INBT writeNBT(Capability<IKnownManipulations> capability, IKnownManipulations instance, Direction side) {

		ListNBT list = new ListNBT();
		for (int i = 0; i < instance.getKnownManips().size(); i++) {
			BloodManipulation manip = instance.getKnownManips().get(i);
			if (manip != null) {
				CompoundNBT entry = new CompoundNBT();
				entry.put("Manip" + i, manip.serialize());
				list.add(entry);
			}
		}
		return list;
	}

	@Override
	public void readNBT(Capability<IKnownManipulations> capability, IKnownManipulations instance, Direction side,
			INBT nbt) {

		List<BloodManipulation> list = new ArrayList<BloodManipulation>();
		if (nbt instanceof ListNBT) {
			ListNBT listNbt = (ListNBT) nbt;
			for (int i = 0; i < listNbt.size(); i++) {
				CompoundNBT parsedNbt = (CompoundNBT) listNbt.get(i);
				if (parsedNbt != null && !parsedNbt.isEmpty()) {
					if (parsedNbt.contains("Manip" + i)) {
						CompoundNBT finalNbt = parsedNbt.getCompound("Manip" + i);
						BloodManipulation bloodManip = BloodManipulation.deserialize(finalNbt);
						list.add(bloodManip);
					}
				}
			}
		}
		instance.setKnownManips(list);
	}

}

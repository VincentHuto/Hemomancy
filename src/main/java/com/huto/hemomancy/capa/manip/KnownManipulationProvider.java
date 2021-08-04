package com.huto.hemomancy.capa.manip;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class KnownManipulationProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IKnownManipulations.class)
	public static final Capability<IKnownManipulations> MANIP_CAPA = null;
	private LazyOptional<IKnownManipulations> instance = LazyOptional.of(KnownManipulations::new);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return MANIP_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(MANIP_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(MANIP_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);

	}

	public static List<BloodManipulation> getPlayerManips(Player player) {
		return player.getCapability(MANIP_CAPA).orElseThrow(IllegalStateException::new).getKnownManips();
	}

	public Tag writeNBT(Capability<IKnownManipulations> capability, IKnownManipulations instance, Direction side) {

		ListTag list = new ListTag();
		CompoundTag selectedManip = new CompoundTag();
		selectedManip.put("Selected", instance.getSelectedManip().serialize());
		list.add(selectedManip);
		for (int i = 0; i < instance.getKnownManips().size(); i++) {
			BloodManipulation manip = instance.getKnownManips().get(i);
			if (manip != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Manip" + i, manip.serialize());
				list.add(entry);
			}
		}
		return list;
	}

	public void readNBT(Capability<IKnownManipulations> capability, IKnownManipulations instance, Direction side,
			Tag nbt) {

		List<BloodManipulation> list = new ArrayList<BloodManipulation>();
		if (nbt instanceof ListTag) {
			ListTag listNbt = (ListTag) nbt;
			for (int i = 0; i < listNbt.size(); i++) {
				CompoundTag parsedNbt = (CompoundTag) listNbt.get(i);
				if (parsedNbt != null && !parsedNbt.isEmpty()) {
					if (parsedNbt.contains("Selected")) {
						CompoundTag selectedNbt = parsedNbt.getCompound("Selected");
						BloodManipulation selectedManip = BloodManipulation.deserialize(selectedNbt);
						instance.setSelectedManip(selectedManip);
					}

					if (parsedNbt.contains("Manip" + (i - 1))) {
						CompoundTag finalNbt = parsedNbt.getCompound("Manip" + (i - 1));
						BloodManipulation bloodManip = BloodManipulation.deserialize(finalNbt);
						list.add(bloodManip);
					}
				}
			}
		}
		instance.setKnownManips(list);
	}

}

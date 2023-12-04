package com.vincenthuto.hemomancy.common.capability.player.manip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
public class KnownManipulationProvider implements ICapabilitySerializable<Tag> {
	//@CapabilityInject(IKnownManipulations.class)
	public static final Capability<IKnownManipulations> MANIP_CAPA = CapabilityManager.get(new CapabilityToken<IKnownManipulations>() {});
	public static LinkedHashMap<BloodManipulation, ManipLevel> getPlayerManips(Player player) {
		return player.getCapability(MANIP_CAPA).orElseThrow(IllegalStateException::new).getKnownManips();
	}

	private LazyOptional<IKnownManipulations> instance = LazyOptional.of(KnownManipulations::new);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(MANIP_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);

	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return MANIP_CAPA.orEmpty(cap, instance);

	}

	public void readNBT(Capability<IKnownManipulations> capability, IKnownManipulations instance, Direction side,
			Tag nbt) {
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		List<VeinLocation> veinList = new ArrayList<>();
		if (nbt instanceof ListTag) {
			ListTag listNbt = (ListTag) nbt;
			int veinCount = 0;
			for (int i = 0; i < listNbt.size(); i++) {
				CompoundTag parsedNbt = (CompoundTag) listNbt.get(i);
				System.out.println("Parsed");
				System.out.println(parsedNbt);
				if (parsedNbt != null && !parsedNbt.isEmpty()) {
					if (parsedNbt.contains("Selected")) {
						CompoundTag selectedNbt = parsedNbt.getCompound("Selected");
						BloodManipulation selectedManip = BloodManipulation.deserialize(selectedNbt);
						instance.setSelectedManip(selectedManip);
					}

					if (parsedNbt.contains("Manip" + (i - 1))) {

						CompoundTag manipNbt = parsedNbt.getCompound("Manip" + (i - 1));
						CompoundTag levleNbt = parsedNbt.getCompound("Level" + (i - 1));
						BloodManipulation bloodManip = BloodManipulation.deserialize(manipNbt);
						ManipLevel level = ManipLevel.deserialize(levleNbt);
						map.put(bloodManip, level);

					}

					if (parsedNbt.contains("SelectedVein")) {
						CompoundTag selectedNbt = parsedNbt.getCompound("SelectedVein");
						VeinLocation selectedVein = VeinLocation.deserializeToLoc(selectedNbt);
						instance.setSelectedVein(selectedVein);
					}

					if (parsedNbt.contains("veinCount")) {
						veinCount = parsedNbt.getInt("veinCount");
					}
					for (int j = 0; j < veinCount; j++) {
						if (parsedNbt.contains("Vein" + (j))) {
							CompoundTag veinNbt = parsedNbt.getCompound("Vein" + (j));
							VeinLocation vein = VeinLocation.deserializeToLoc(veinNbt);
							veinList.add(vein);
						}
					}

					if (parsedNbt.contains("avatarActive")) {
						instance.setAvatarActive(parsedNbt.getBoolean("avatarActive"));
					}

				}

			}
			
			instance.setKnownManips(map);
			instance.setVeinList(veinList);
		}

	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(MANIP_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	public Tag writeNBT(Capability<IKnownManipulations> capability, IKnownManipulations instance, Direction side) {
		ListTag list = new ListTag();
		CompoundTag selectedManip = new CompoundTag();
		selectedManip.put("Selected", instance.getSelectedManip().serialize());
		list.add(selectedManip);

		for (int i = 0; i < instance.getKnownManips().size(); i++) {
			BloodManipulation manip = instance.getManipList().get(i);
			ManipLevel level = instance.getLevelList().get(i);
			if (manip != null && level != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Manip" + i, manip.serialize());
				entry.put("Level" + i, level.serialize());
				list.add(entry);
			}
		}
		CompoundTag selectedVein = new CompoundTag();
		selectedManip.put("SelectedVein", instance.getSelectedVein().serializeNBT());
		list.add(selectedVein);

		CompoundTag veinCount = new CompoundTag();
		veinCount.putInt("veinCount", instance.getVeinList().size());
		list.add(veinCount);
		for (int i = 0; i < instance.getVeinList().size(); i++) {
			VeinLocation loc = instance.getVeinList().get(i);
			if (loc != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Vein" + i, loc.serializeNBT());
				list.add(entry);
			}
		}
		CompoundTag avatarActive = new CompoundTag();
		avatarActive.putBoolean("avatarActive", instance.isAvatarActive());
		list.add(avatarActive);

		return list;
	}

}
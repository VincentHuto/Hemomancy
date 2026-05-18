package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class KnownManipulations implements IKnownManipulations, INBTSerializable<ListTag> {

	BlockPos lastVeinMineStart = BlockPos.ZERO;
	BloodManipulation selectedManip = BloodManipulation.BLANK;
	ManipLevel manipLevel = ManipLevel.BLANK;
	LinkedHashMap<BloodManipulation, ManipLevel> knownManips = new LinkedHashMap<>();
	List<VeinLocation> veinList = new ArrayList<>();
	VeinLocation selectedVein = VeinLocation.BLANK;
	boolean avatarActive = false;
	List<String> equippedManipNames = new ArrayList<>();

	@Override
	public BlockPos getLastVeinMineStart() {
		return lastVeinMineStart;
	}

	@Override
	public void setLastVeinMineStart(BlockPos newPos) {
		this.lastVeinMineStart = newPos;
	}

	/*
	 * Used because saving the manip objects means its not as easy as just comparing
	 * them 1:1
	 */
	@Override
	public boolean doesListContainName(LinkedHashMap<BloodManipulation, ManipLevel> map, BloodManipulation manipIn) {
		for (BloodManipulation curr : map.keySet()) {
			if (manipIn.getName().equals(curr.getName())) {
				return true;
			}
		}
		return false;

	}

	@Override
	public LinkedHashMap<BloodManipulation, ManipLevel> getKnownManips() {
		return knownManips;
	}

	@Override
	public List<ManipLevel> getLevelList() {
		return new ArrayList<>(knownManips.values());
	}

	@Override
	public ManipLevel getManipLevel(BloodManipulation manip) {
		if (knownManips.get(manip) != null) {
			return knownManips.get(manip);
		}
		return ManipLevel.BLANK;

	}

	@Override
	public List<BloodManipulation> getManipList() {
		return new ArrayList<>(knownManips.keySet());

	}

	@Override
	public BloodManipulation getSelectedManip() {
		if (selectedManip == null) {
			selectedManip = getManipList().get(0);
		}
		return selectedManip;

	}

	@Override
	public ManipLevel getSelectedManipLevel() {
		return knownManips.get(selectedManip);
	}

	@Override
	public VeinLocation getSelectedVein() {
		if (selectedVein == null) {
			selectedVein = getVeinList().get(0);
		}
		return selectedVein;
	}

	@Override
	public List<BlockPos> getVeinBlockList() {
		List<BlockPos> list = new ArrayList<>();
		for (VeinLocation loc : getVeinList()) {
			list.add(loc.getPosition());
		}
		return list;
	}

	@Override
	public List<VeinLocation> getVeinList() {
		return veinList;
	}

	@Override
	public List<String> getVeinNameList() {
		List<String> list = new ArrayList<>();
		for (VeinLocation loc : getVeinList()) {
			list.add(loc.getName());
		}
		return list;
	}

	@Override
	public void incrSelectedManipLevel(int incr) {
		ManipLevel sel = getManipLevel(selectedManip);
		int currLevel = sel.getCurrentLevel();
		sel.setCurrentLevel(currLevel += incr);

	}

	@Override
	public boolean isAvatarActive() {
		return avatarActive;
	}

	@Override
	public void setAvatarActive(boolean avatarActive) {
		this.avatarActive = avatarActive;
	}

	@Override
	public void setKnownManips(LinkedHashMap<BloodManipulation, ManipLevel> knownManips) {
		this.knownManips = knownManips;
	}

	@Override
	public void setSelectedManip(BloodManipulation selectedManip) {
		this.selectedManip = selectedManip;
	}

	@Override
	public void setSelectedManipLevel(int level) {
		knownManips.get(selectedManip).setCurrentLevel(level);
	}

	@Override
	public void setSelectedVein(VeinLocation selectedVein) {
		this.selectedVein = selectedVein;

	}

	@Override
	public void setVeinList(List<VeinLocation> dimPos) {
		this.veinList = dimPos;
	}

	@Override
	public void setCapa(IKnownManipulations old) {
		this.knownManips = old.getKnownManips();
		this.selectedManip = old.getSelectedManip();
		this.veinList = old.getVeinList();
		this.avatarActive = old.isAvatarActive();
		this.equippedManipNames = new ArrayList<>(old.getEquippedManipNames());
	}

	// ── Equipped manipulation slots ──

	@Override
	public List<String> getEquippedManipNames() {
		return equippedManipNames;
	}

	@Override
	public void setEquippedManipNames(List<String> names) {
		this.equippedManipNames = names != null ? names : new ArrayList<>();
	}

	@Override
	public boolean isManipEquipped(BloodManipulation manip) {
		if (manip == null || manip == BloodManipulation.BLANK) return false;
		return equippedManipNames.contains(manip.getName());
	}

	@Override
	public boolean equipManip(String manipName, int maxSlots) {
		if (manipName == null || manipName.isEmpty()) return false;
		if (equippedManipNames.contains(manipName)) return false;
		if (equippedManipNames.size() >= maxSlots) return false;
		equippedManipNames.add(manipName);
		return true;
	}

	@Override
	public boolean unequipManip(String manipName) {
		return equippedManipNames.remove(manipName);
	}

	@Override
	public ListTag serializeNBT(HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		CompoundTag selectedManip = new CompoundTag();
		selectedManip.put("Selected", getSelectedManip().serialize());
		selectedManip.put("SelectedVein", getSelectedVein().serializeNBT());
		list.add(selectedManip);

		for (int i = 0; i < getKnownManips().size(); i++) {
			BloodManipulation manip = getManipList().get(i);
			ManipLevel level = getLevelList().get(i);
			if (manip != null && level != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Manip" + i, manip.serialize());
				entry.put("Level" + i, level.serialize());
				list.add(entry);
			}
		}

		CompoundTag veinCount = new CompoundTag();
		veinCount.putInt("veinCount", getVeinList().size());
		list.add(veinCount);
		for (int i = 0; i < getVeinList().size(); i++) {
			VeinLocation loc = getVeinList().get(i);
			if (loc != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Vein" + i, loc.serializeNBT());
				list.add(entry);
			}
		}

		CompoundTag avatarActive = new CompoundTag();
		avatarActive.putBoolean("avatarActive", isAvatarActive());
		avatarActive.put("lastVeinMineStart", NbtUtils.writeBlockPos(getLastVeinMineStart()));
		list.add(avatarActive);

		CompoundTag equippedEntry = new CompoundTag();
		ListTag equippedTag = new ListTag();
		for (String name : getEquippedManipNames()) {
			equippedTag.add(StringTag.valueOf(name));
		}
		equippedEntry.put("equippedManips", equippedTag);
		list.add(equippedEntry);

		return list;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, ListTag listNbt) {
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		List<VeinLocation> veinList = new ArrayList<>();
		int veinCount = 0;
		for (int i = 0; i < listNbt.size(); i++) {
			CompoundTag parsedNbt = (CompoundTag) listNbt.get(i);
			if (parsedNbt != null && !parsedNbt.isEmpty()) {
				if (parsedNbt.contains("Selected")) {
					BloodManipulation selectedManip = BloodManipulation.deserialize(parsedNbt.getCompound("Selected"));
					setSelectedManip(selectedManip);
				}
				if (parsedNbt.contains("SelectedVein")) {
					VeinLocation selectedVein = VeinLocation.deserializeToLoc(parsedNbt.getCompound("SelectedVein"));
					setSelectedVein(selectedVein);
				}
				if (parsedNbt.contains("Manip" + (i - 1))) {
					CompoundTag manipNbt = parsedNbt.getCompound("Manip" + (i - 1));
					CompoundTag levelNbt = parsedNbt.getCompound("Level" + (i - 1));
					BloodManipulation bloodManip = BloodManipulation.deserialize(manipNbt);
					ManipLevel level = ManipLevel.deserialize(levelNbt);
					map.put(bloodManip, level);
				}
				if (parsedNbt.contains("veinCount")) {
					veinCount = parsedNbt.getInt("veinCount");
				}
				for (int j = 0; j < veinCount; j++) {
					if (parsedNbt.contains("Vein" + j)) {
						VeinLocation vein = VeinLocation.deserializeToLoc(parsedNbt.getCompound("Vein" + j));
						veinList.add(vein);
					}
				}
				if (parsedNbt.contains("avatarActive")) {
					setAvatarActive(parsedNbt.getBoolean("avatarActive"));
				}
				if (parsedNbt.contains("lastVeinMineStart")) {
					setLastVeinMineStart(NbtUtils.readBlockPos(parsedNbt, "lastVeinMineStart").orElse(BlockPos.ZERO));
				}
				if (parsedNbt.contains("equippedManips")) {
					ListTag equippedTag = parsedNbt.getList("equippedManips", Tag.TAG_STRING);
					List<String> equipped = new ArrayList<>();
					for (int j = 0; j < equippedTag.size(); j++) {
						equipped.add(equippedTag.getString(j));
					}
					setEquippedManipNames(equipped);
				}
			}
		}
		setKnownManips(map);
		setVeinList(veinList);
	}
}
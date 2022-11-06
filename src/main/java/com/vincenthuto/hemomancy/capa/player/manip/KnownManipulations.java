package com.vincenthuto.hemomancy.capa.player.manip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vincenthuto.hemomancy.capa.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

import net.minecraft.core.BlockPos;

public class KnownManipulations implements IKnownManipulations {

	BloodManipulation selectedManip = BloodManipulation.BLANK;
	ManipLevel manipLevel = ManipLevel.BLANK;
	LinkedHashMap<BloodManipulation, ManipLevel> knownManips = new LinkedHashMap<>();
	List<VeinLocation> veinList = new ArrayList<>();
	VeinLocation selectedVein = VeinLocation.BLANK;
	boolean avatarActive = false;

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

}
package com.vincenthuto.hemomancy.capa.manip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

public class KnownManipulations implements IKnownManipulations {

	BloodManipulation selectedManip = BloodManipulation.BLANK;
	ManipLevel manipLevel = ManipLevel.BLANK;
	public LinkedHashMap<BloodManipulation, ManipLevel> knownManips = new LinkedHashMap<BloodManipulation, ManipLevel>();

	@Override
	public BloodManipulation getSelectedManip() {
		if (selectedManip == null) {
			selectedManip = getManipList().get(0);
		}
		return selectedManip;

	}

	@Override
	public void setSelectedManip(BloodManipulation selectedManip) {
		this.selectedManip = selectedManip;
	}

	@Override
	public LinkedHashMap<BloodManipulation, ManipLevel> getKnownManips() {
		return knownManips;
	}

	@Override
	public void setKnownManips(LinkedHashMap<BloodManipulation, ManipLevel> knownManips) {
		this.knownManips = knownManips;
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
		return new ArrayList<BloodManipulation>(knownManips.keySet());

	}

	@Override
	public List<ManipLevel> getLevelList() {
		return new ArrayList<ManipLevel>(knownManips.values());
	}

	@Override
	public ManipLevel getSelectedManipLevel() {
		return knownManips.get(selectedManip);
	}

	@Override
	public void setSelectedManipLevel(int level) {
		knownManips.get(selectedManip).setCurrentLevel(level);
	}

	@Override
	public void incrSelectedManipLevel(int incr) {
		ManipLevel sel = getManipLevel(selectedManip);
		int currLevel = sel.getCurrentLevel();
		sel.setCurrentLevel(currLevel += incr); 

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

}
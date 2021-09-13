package com.vincenthuto.hemomancy.capa.manip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

public class KnownManipulations implements IKnownManipulations {

	List<BloodManipulation> knownManips = new ArrayList<BloodManipulation>();
	BloodManipulation selectedManip = BloodManipulation.BLANK;

	public HashMap<BloodManipulation, ManipLevel> skillsMap = new HashMap<BloodManipulation, ManipLevel>();

	@Override
	public BloodManipulation getSelectedManip() {
		return selectedManip;
	}

	@Override
	public void setSelectedManip(BloodManipulation selectedManip) {
		this.selectedManip = selectedManip;
	}

	@Override
	public List<BloodManipulation> getKnownManips() {
		return knownManips;
	}

	@Override
	public void setKnownManips(List<BloodManipulation> knownManips) {
		this.knownManips = knownManips;
	}

	public boolean doesMapContainName(HashMap<BloodManipulation, ManipLevel> map, BloodManipulation manipIn) {
		for (BloodManipulation curr : map.keySet()) {
			if (manipIn.getName().equals(curr.getName())) {
				return true;
			}
		}
		return false;

	}

	/*
	 * Used because saving the manip objects means its not as easy as just comparing
	 * them 1:1
	 */
	@Override
	public boolean doesListContainName(List<BloodManipulation> list, BloodManipulation manipIn) {
		for (BloodManipulation current : list) {
			if (manipIn.getName().equals(current.getName())) {
				return true;
			}
		}
		return false;
	}

}

package com.huto.hemomancy.capa.manip;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

public class KnownManipulations implements IKnownManipulations {

	List<BloodManipulation> knownManips = new ArrayList<BloodManipulation>();
	BloodManipulation selectedManip = new BloodManipulation("No Selected", 0, 0, EnumManipulationType.QUICK,
			EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);

	public KnownManipulations() {
	}

	public BloodManipulation getSelectedManip() {
		return selectedManip;
	}

	public void setSelectedManip(BloodManipulation selectedManip) {
		this.selectedManip = selectedManip;
	}

	public List<BloodManipulation> getKnownManips() {
		return knownManips;
	}

	public void setKnownManips(List<BloodManipulation> knownManips) {
		this.knownManips = knownManips;
	}

	/*
	 * Used because saving the manip objects means its not as easy as just comparing
	 * them 1:1
	 */
	public boolean doesListContainName(List<BloodManipulation> list, BloodManipulation manipIn) {
		for (BloodManipulation current : list) {
			if (manipIn.getName().equals(current.getName())) {
				return true;
			}
		}
		return false;
	}

}

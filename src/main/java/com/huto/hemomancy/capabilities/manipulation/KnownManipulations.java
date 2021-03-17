package com.huto.hemomancy.capabilities.manipulation;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;

public class KnownManipulations implements IKnownManipulations {

	List<BloodManipulation> knownManips = new ArrayList<BloodManipulation>();
	BloodManipulation selectedManip = new BloodManipulation("No Selected", 0, 0, EnumManipulationRank.HUMILIS,
			EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);

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

}

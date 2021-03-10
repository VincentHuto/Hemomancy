package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;

public class ManipulationInit {

	public List<BloodManipulation> MANIPULATIONS = new ArrayList<BloodManipulation>();
	static BloodManipulation blood_shot, blood_rush;

	public static void init() {
		blood_shot = new BloodManipulation("blood_shot", 100, 0, EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS,
				EnumVeinSections.HEAD);
		blood_rush = new BloodManipulation("blood_rush", 100, 0, EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS,
				EnumVeinSections.BODY);

	}

	public BloodManipulation getManupulationFromName(String name) {
		for (BloodManipulation mani : MANIPULATIONS) {
			if (mani.getName().contains(name)) {
				return mani;
			}
		}
		return null;
	}

}

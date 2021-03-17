package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;

public class ManipulationInit {

	public static List<BloodManipulation> MANIPULATIONS = new ArrayList<BloodManipulation>();
	public static BloodManipulation blood_shot, blood_rush, aneurysm;

	public static void init() {
		blood_shot = register(new BloodManipulation("blood_shot", 100, 0, EnumManipulationRank.HUMILIS,
				EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
		blood_rush = register(new BloodManipulation("blood_rush", 100, 0, EnumManipulationRank.HUMILIS,
				EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));
		aneurysm = register(new BloodManipulation("aneurysm", 100, 0, EnumManipulationRank.HUMILIS,
				EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));
	}

	public static BloodManipulation register(BloodManipulation manip) {
		MANIPULATIONS.add(manip);
		return manip;
	}

}

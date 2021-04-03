package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;

public class ManipulationInit {

	public static List<BloodManipulation> MANIPULATIONS = new ArrayList<BloodManipulation>();
	public static BloodManipulation blood_shot, blood_rush, aneurysm;
	public static BloodManipulation ferric_transmutation;
	public static BloodManipulation activation_potential;

	public static void init() {
		blood_shot = register(new BloodManipulation("blood_shot", 100, 0, EnumManipulationRank.HUMILIS,
				EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
		blood_rush = register(new BloodManipulation("blood_rush", 100, 0, EnumManipulationRank.HUMILIS,
				EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));
		aneurysm = register(new BloodManipulation("aneurysm", 100, 0, EnumManipulationRank.HUMILIS,
				EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));

		ferric_transmutation = register(new BloodManipulation("ferric_transmutation", 1000, 0,
				EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY));

		activation_potential = register(new BloodManipulation("activation_potential", 250, 0,
				EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY));
	}

	public static BloodManipulation register(BloodManipulation manip) {
		MANIPULATIONS.add(manip);
		return manip;
	}

}

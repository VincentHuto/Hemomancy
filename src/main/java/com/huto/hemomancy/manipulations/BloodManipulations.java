package com.huto.hemomancy.manipulations;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;

public class BloodManipulations {

	List<BloodManipulationBase> MANIPULATIONS = new ArrayList<BloodManipulationBase>();

	public static BloodManipulationBase bloodRush = new BloodManipulationBase("bloodRush",
			EnumManipulationLevel.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY, 100f);

}

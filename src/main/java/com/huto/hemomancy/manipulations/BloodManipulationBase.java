package com.huto.hemomancy.manipulations;

import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;

public class BloodManipulationBase {
	String name;
	EnumManipulationLevel level;
	EnumBloodTendency tendency;
	EnumVeinSections section;
	float bloodCost;

	public BloodManipulationBase(String name, EnumManipulationLevel level, EnumBloodTendency tendency,
			EnumVeinSections section, float bloodCost) {
		this.name = name;
		this.level = level;
		this.tendency = tendency;
		this.section = section;
		this.bloodCost = bloodCost;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EnumManipulationLevel getLevel() {
		return level;
	}

	public void setLevel(EnumManipulationLevel level) {
		this.level = level;
	}

	public EnumBloodTendency getTendency() {
		return tendency;
	}

	public void setTendency(EnumBloodTendency tendency) {
		this.tendency = tendency;
	}

	public EnumVeinSections getSection() {
		return section;
	}

	public void setSection(EnumVeinSections section) {
		this.section = section;
	}

	public float getBloodCost() {
		return bloodCost;
	}

	public void setBloodCost(float bloodCost) {
		this.bloodCost = bloodCost;
	}

}

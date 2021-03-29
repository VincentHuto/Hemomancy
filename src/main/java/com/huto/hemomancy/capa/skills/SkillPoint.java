package com.huto.hemomancy.capa.skills;

import javax.annotation.Nullable;

public class SkillPoint {
	int id, maxLevels;
	String name;
	double cost;
	EnumSkillStates state;
	SkillPoint parent;

	public SkillPoint(int id, String name, double cost, int maxLevel, EnumSkillStates state,
			@Nullable SkillPoint parent) {
		this.id = id;
		this.name = name;
		this.maxLevels = maxLevel;
		this.cost = cost;
		this.state = state;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public EnumSkillStates getState() {
		return state;
	}

	public void setState(EnumSkillStates state) {
		this.state = state;
	}

	public SkillPoint getParent() {
		return parent;
	}

	public void setParent(SkillPoint parent) {
		this.parent = parent;
	}

}

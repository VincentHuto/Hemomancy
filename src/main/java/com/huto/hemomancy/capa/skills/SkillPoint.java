package com.huto.hemomancy.capa.skills;

public class SkillPoint {

	String name;
	double cost;
	EnumSkillStates state;
	SkillPoint parent;

	public SkillPoint(String name) {
		this.name = name;
		this.cost = 0;
		this.state = EnumSkillStates.BASE;
		this.parent = getBaseSkill();
	}

	public SkillPoint(String name, double cost) {
		this.name = name;
		this.cost = cost;
		this.state = EnumSkillStates.BASE;
		this.parent = getBaseSkill();
	}

	public SkillPoint(String name, double cost, EnumSkillStates state) {
		this.name = name;
		this.cost = cost;
		this.state = state;
		this.parent = getBaseSkill();
	}

	public SkillPoint(String name, double cost, EnumSkillStates state, SkillPoint parent) {
		this.name = name;
		this.cost = cost;
		this.state = state;
		this.parent = parent;
	}

	public SkillPoint getBaseSkill() {
		return new SkillPoint("base", 0, EnumSkillStates.BASE);
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

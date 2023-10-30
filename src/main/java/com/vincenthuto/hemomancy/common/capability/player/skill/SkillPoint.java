package com.vincenthuto.hemomancy.common.capability.player.skill;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

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

	public double getCost() {
		return cost;
	}

	public String getName() {
		return name;
	}

	public SkillPoint getParent() {
		return parent;
	}

	public EnumSkillStates getState() {
		return state;
	}

	/*
	 * Writes a NBT tag from this manipulation
	 */
	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("name", name);

		nbt.putString("state", state.name());

		return nbt;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(SkillPoint parent) {
		this.parent = parent;
	}

	public void setState(EnumSkillStates state) {
		this.state = state;
	}

}

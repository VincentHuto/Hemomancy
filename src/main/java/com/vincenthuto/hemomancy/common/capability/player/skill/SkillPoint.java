package com.vincenthuto.hemomancy.common.capability.player.skill;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

public class SkillPoint {
	int id, maxLevels;
	String name;
	double cost;           // Blood cost per level-up
	int currentLevel;      // How many times this skill has been leveled
	EnumSkillStates state;
	SkillPoint parent;

	public SkillPoint(int id, String name, double cost, int maxLevel, EnumSkillStates state,
			@Nullable SkillPoint parent) {
		this.id = id;
		this.name = name;
		this.maxLevels = maxLevel;
		this.cost = cost;
		this.currentLevel = 0;
		this.state = state;
		this.parent = parent;
	}

	// ── Getters ──

	public double getCost() {
		return cost;
	}

	/**
	 * The actual blood price to go from currentLevel to currentLevel+1.
	 * Each successive level costs 50% more than the base.
	 */
	public double getLevelUpCost() {
		return cost * (1.0 + 0.5 * currentLevel);
	}

	public int getId() {
		return id;
	}

	public int getMaxLevels() {
		return maxLevels;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public boolean isMaxed() {
		return currentLevel >= maxLevels;
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

	// ── Level-up ──

	/**
	 * Attempt to level this skill up. Returns true on success.
	 * Prerequisites: state must be UNLOCKED and not yet maxed.
	 */
	public boolean tryLevelUp() {
		if (state != EnumSkillStates.UNLOCKED) return false;
		if (isMaxed()) return false;
		currentLevel++;
		return true;
	}

	// ── Serialisation ──

	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("id", id);
		nbt.putString("name", name);
		nbt.putString("state", state.name());
		nbt.putInt("level", currentLevel);
		return nbt;
	}

	/**
	 * Applies serialised level and state data onto this skill point
	 * (matched by name from the registered tree).
	 */
	public void deserialize(CompoundTag nbt) {
		if (nbt.contains("state")) {
			try {
				this.state = EnumSkillStates.valueOf(nbt.getString("state"));
			} catch (IllegalArgumentException ignored) {}
		}
		if (nbt.contains("level")) {
			this.currentLevel = nbt.getInt("level");
		}
	}

	// ── Setters ──

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setCurrentLevel(int level) {
		this.currentLevel = level;
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

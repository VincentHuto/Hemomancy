package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.nbt.CompoundTag;

public class ManipLevel {

	public static ManipLevel BLANK = new ManipLevel(0, 0);
	public static ManipLevel deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("level") && nbt.contains("xpcost")) {
				ManipLevel manip = new ManipLevel(nbt.getInt("level"), nbt.getDouble("xpcost"));
				return manip;
			}
		}
		return null;
	}
	int currentLevel;

	double xp;

	public ManipLevel(int currentLevel, double xp) {
		this.currentLevel = currentLevel;
		this.xp = xp;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public double getXp() {
		return xp;
	}

	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("level", currentLevel);
		nbt.putDouble("xpcost", xp);
		return nbt;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public void setXp(double xp) {
		this.xp = xp;
	}

	@Override
	public String toString() {
		return HLTextUtils.convertInitToLang("Level: " + currentLevel + ", Xp: " + xp);
	}
}

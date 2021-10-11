package com.vincenthuto.hemomancy.manipulation;

import com.vincenthuto.hutoslib.client.TextUtils;

import net.minecraft.nbt.CompoundTag;

public class ManipLevel {

	int currentLevel;
	double xp;
	public static ManipLevel BLANK = new ManipLevel(0, 0);

	public ManipLevel(int currentLevel, double xp) {
		this.currentLevel = currentLevel;
		this.xp = xp;
	}

	public static ManipLevel deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("level") && nbt.contains("xpcost")) {
				ManipLevel manip = new ManipLevel(nbt.getInt("level"), nbt.getDouble("xpcost"));
				return manip;
			}
		}
		return null;
	}

	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("level", currentLevel);
		nbt.putDouble("xpcost", xp);
		return nbt;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public double getXp() {
		return xp;
	}

	public void setXp(double xp) {
		this.xp = xp;
	}

	@Override
	public String toString() {
		return TextUtils.convertInitToLang("Level: " + currentLevel + ", Xp: " + xp);
	}
}

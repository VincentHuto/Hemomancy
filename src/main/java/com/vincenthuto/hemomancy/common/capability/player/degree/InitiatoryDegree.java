package com.vincenthuto.hemomancy.common.capability.player.degree;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class InitiatoryDegree implements IInitiatoryDegree, INBTSerializable<CompoundTag> {

	/** 0 = uninitiated; 1–8 = an actual degree */
	private int degreeNumber = 0;

	@Override
	public int getDegreeNumber() {
		return degreeNumber;
	}

	@Override
	@Nullable
	public EnumInitiatoryDegree getDegree() {
		return EnumInitiatoryDegree.byNumber(degreeNumber);
	}

	@Override
	public boolean isInitiated() {
		return degreeNumber >= 1;
	}

	@Override
	public boolean isMaxDegree() {
		return degreeNumber >= 8;
	}

	@Override
	public void setDegreeNumber(int degree) {
		this.degreeNumber = Math.max(0, Math.min(8, degree));
	}

	@Override
	public boolean advanceDegree() {
		if (degreeNumber >= 8) return false;
		degreeNumber++;
		return true;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("degree", degreeNumber);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		degreeNumber = nbt.getInt("degree");
	}
}

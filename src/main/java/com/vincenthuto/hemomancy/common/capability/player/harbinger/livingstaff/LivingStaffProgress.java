package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class LivingStaffProgress implements ILivingStaffProgress, INBTSerializable<CompoundTag> {
	private static final String HAS_LIVING_STAFF_BOND_KEY = "hasLivingStaffBond";
	private static final String BLOOD_HANDLED_KEY = "bloodHandled";
	private static final String VESPER_MEMORY_AWAKENED_KEY = "vesperMemoryAwakened";

	private boolean hasLivingStaffBond;
	private double bloodHandled;
	private boolean vesperMemoryAwakened;

	@Override
	public boolean hasLivingStaffBond() {
		return hasLivingStaffBond;
	}

	@Override
	public void setLivingStaffBond(boolean value) {
		this.hasLivingStaffBond = value;
	}

	@Override
	public boolean unlockLivingStaffBond() {
		if (hasLivingStaffBond) {
			return false;
		}
		hasLivingStaffBond = true;
		return true;
	}

	@Override
	public double getBloodHandled() {
		return bloodHandled;
	}

	@Override
	public void setBloodHandled(double value) {
		this.bloodHandled = Math.max(0.0D, value);
	}

	@Override
	public void addBloodHandled(double amount) {
		if (amount <= 0.0D) {
			return;
		}
		setBloodHandled(bloodHandled + amount);
	}

	@Override
	public boolean isVesperMemoryAwakened() {
		return vesperMemoryAwakened;
	}

	@Override
	public void setVesperMemoryAwakened(boolean value) {
		this.vesperMemoryAwakened = value;
	}

	@Override
	public boolean awakenVesperMemory() {
		if (vesperMemoryAwakened) {
			return false;
		}
		vesperMemoryAwakened = true;
		return true;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(HAS_LIVING_STAFF_BOND_KEY, hasLivingStaffBond);
		tag.putDouble(BLOOD_HANDLED_KEY, Math.max(0.0D, bloodHandled));
		tag.putBoolean(VESPER_MEMORY_AWAKENED_KEY, vesperMemoryAwakened);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		hasLivingStaffBond = nbt.getBoolean(HAS_LIVING_STAFF_BOND_KEY);
		bloodHandled = Math.max(0.0D, nbt.getDouble(BLOOD_HANDLED_KEY));
		vesperMemoryAwakened = nbt.getBoolean(VESPER_MEMORY_AWAKENED_KEY);
	}
}

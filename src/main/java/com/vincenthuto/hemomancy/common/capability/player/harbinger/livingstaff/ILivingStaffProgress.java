package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

public interface ILivingStaffProgress {
	boolean hasLivingStaffBond();

	void setLivingStaffBond(boolean value);

	boolean unlockLivingStaffBond();

	double getBloodHandled();

	void setBloodHandled(double value);

	void addBloodHandled(double amount);

	boolean isVesperMemoryAwakened();

	void setVesperMemoryAwakened(boolean value);

	boolean awakenVesperMemory();
}

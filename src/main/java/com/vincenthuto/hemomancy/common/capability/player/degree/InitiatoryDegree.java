package com.vincenthuto.hemomancy.common.capability.player.degree;

import javax.annotation.Nullable;

public class InitiatoryDegree implements IInitiatoryDegree {

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
}

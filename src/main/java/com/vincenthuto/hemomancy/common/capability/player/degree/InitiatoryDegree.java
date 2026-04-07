package com.vincenthuto.hemomancy.common.capability.player.degree;

import javax.annotation.Nullable;

public class InitiatoryDegree implements IInitiatoryDegree {

	/** 0 = uninitiated; 1–7 = an actual degree */
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
		return degreeNumber >= 7;
	}

	@Override
	public void setDegreeNumber(int degree) {
		this.degreeNumber = Math.max(0, Math.min(7, degree));
	}

	@Override
	public boolean advanceDegree() {
		if (degreeNumber >= 7) return false;
		degreeNumber++;
		return true;
	}
}

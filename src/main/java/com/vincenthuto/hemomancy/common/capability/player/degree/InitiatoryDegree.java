package com.vincenthuto.hemomancy.common.capability.player.degree;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class InitiatoryDegree implements IInitiatoryDegree {

	/** 0 = uninitiated; 1–8 = an actual degree */
	private int degreeNumber = 0;
	private final Map<Long, Integer> pomeCommunionProgress = new HashMap<>();
	private boolean qliphothCommunionDone = false;
	private long pomeEmpowermentExpiry = 0L;
	private int totalPomesConsumed = 0;

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
	public boolean isQliphothCommunionDone() {
		return qliphothCommunionDone;
	}

	@Override
	public void setQliphothCommunionDone(boolean done) {
		this.qliphothCommunionDone = done;
	}

	@Override
	public int recordPomeConsumed(long bloomOrigin) {
		int newCount = pomeCommunionProgress.getOrDefault(bloomOrigin, 0) + 1;
		pomeCommunionProgress.put(bloomOrigin, newCount);
		return newCount;
	}

	@Override
	public int getTotalPomesConsumed() {
		return totalPomesConsumed;
	}

	@Override
	public void incrementTotalPomesConsumed() {
		if (totalPomesConsumed < 9) {
			totalPomesConsumed++;
		}
	}

	@Override
	public void syncTotalPomesConsumed(int count) {
		totalPomesConsumed = Math.max(0, Math.min(9, count));
	}

	@Override
	public long getPomeEmpowermentExpiry() {
		return pomeEmpowermentExpiry;
	}

	@Override
	public void setPomeEmpowermentExpiry(long tick) {
		this.pomeEmpowermentExpiry = tick;
	}

	public Map<Long, Integer> getPomeCommunionProgress() {
		return pomeCommunionProgress;
	}
}

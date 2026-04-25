package com.vincenthuto.hemomancy.common.capability.player.degree;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class InitiatoryDegree implements IInitiatoryDegree, INBTSerializable<CompoundTag> {

	/** 0 = uninitiated; 1–8 = an actual degree */
	private int degreeNumber = 0;

	// Pome communion tracking
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
	public boolean isQliphothCommunionDone() { return qliphothCommunionDone; }

	@Override
	public void setQliphothCommunionDone(boolean done) { this.qliphothCommunionDone = done; }

	@Override
	public int recordPomeConsumed(long bloomOrigin) {
		int newCount = pomeCommunionProgress.getOrDefault(bloomOrigin, 0) + 1;
		pomeCommunionProgress.put(bloomOrigin, newCount);
		return newCount;
	}

	@Override
	public int getTotalPomesConsumed() { return totalPomesConsumed; }

	@Override
	public void incrementTotalPomesConsumed() { if (totalPomesConsumed < 9) totalPomesConsumed++; }

	@Override
	public void syncTotalPomesConsumed(int count) { totalPomesConsumed = Math.min(9, count); }

	@Override
	public long getPomeEmpowermentExpiry() { return pomeEmpowermentExpiry; }

	@Override
	public void setPomeEmpowermentExpiry(long tick) { this.pomeEmpowermentExpiry = tick; }

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("degree", degreeNumber);
		tag.putBoolean("pome_communion_done", qliphothCommunionDone);
		tag.putLong("pome_empowerment_expiry", pomeEmpowermentExpiry);
		tag.putInt("pome_total_consumed", totalPomesConsumed);
		CompoundTag progressTag = new CompoundTag();
		pomeCommunionProgress.forEach((origin, count) ->
				progressTag.putInt(String.valueOf(origin), count));
		tag.put("pome_communion_progress", progressTag);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		degreeNumber = nbt.getInt("degree");
		qliphothCommunionDone = nbt.getBoolean("pome_communion_done");
		pomeEmpowermentExpiry = nbt.getLong("pome_empowerment_expiry");
		totalPomesConsumed = Math.min(9, nbt.getInt("pome_total_consumed"));
		pomeCommunionProgress.clear();
		CompoundTag progressTag = nbt.getCompound("pome_communion_progress");
		for (String key : progressTag.getAllKeys()) {
			try {
				pomeCommunionProgress.put(Long.parseLong(key), progressTag.getInt(key));
			} catch (NumberFormatException ignored) {}
		}
	}
}

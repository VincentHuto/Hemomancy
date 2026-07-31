package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class InitiatoryDegree implements IInitiatoryDegree, INBTSerializable<CompoundTag> {

	/** 0 = uninitiated; 1–8 = an actual degree */
	private int degreeNumber = 0;

	// Pome communion tracking
	private final Map<Long, Integer> pomeCommunionProgress = new HashMap<>();
	private boolean qliphothCommunionDone = false;
	private long pomeEmpowermentExpiry = 0L;
	private int totalPomesConsumed = 0;
	private boolean hasFoundedBloodline = false;
	private boolean founderIntegrationSevered = false;
	private boolean fungalRevelationWitnessed = false;
	private boolean fungalSpineGranted = false;
	private boolean hematicFortification = false;
	private int ancestralCommunions = 0;
	private EnumArchonPath archonPath = EnumArchonPath.NONE;

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
	public void resetPomeCommunion() {
		pomeCommunionProgress.clear();
		qliphothCommunionDone = false;
		pomeEmpowermentExpiry = 0L;
		totalPomesConsumed = 0;
	}

	@Override
	public long getPomeEmpowermentExpiry() { return pomeEmpowermentExpiry; }

	@Override
	public void setPomeEmpowermentExpiry(long tick) { this.pomeEmpowermentExpiry = tick; }

	@Override public boolean hasFoundedBloodline() { return hasFoundedBloodline; }
	@Override public void setHasFoundedBloodline(boolean founded) { hasFoundedBloodline = founded; }
	@Override public boolean isFounderIntegrationSevered() { return founderIntegrationSevered; }
	@Override public void setFounderIntegrationSevered(boolean severed) { founderIntegrationSevered = severed; }
	@Override public boolean hasWitnessedFungalRevelation() { return fungalRevelationWitnessed; }
	@Override public void setFungalRevelationWitnessed(boolean witnessed) { fungalRevelationWitnessed = witnessed; }
	@Override public boolean hasFungalSpineGranted() { return fungalSpineGranted; }
	@Override public void setFungalSpineGranted(boolean granted) { fungalSpineGranted = granted; }
	@Override public boolean hasHematicFortification() { return hematicFortification; }
	@Override public void setHematicFortification(boolean fortified) { hematicFortification = fortified; }
	@Override public int getAncestralCommunions() { return ancestralCommunions; }
	@Override public void setAncestralCommunions(int communions) {
		ancestralCommunions = Math.max(0, communions);
	}
	@Override public EnumArchonPath getArchonPath() { return archonPath; }
	@Override public void setArchonPath(EnumArchonPath path) { archonPath = path == null ? EnumArchonPath.NONE : path; }

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("degree", degreeNumber);
		tag.putBoolean("pome_communion_done", qliphothCommunionDone);
		tag.putLong("pome_empowerment_expiry", pomeEmpowermentExpiry);
		tag.putInt("pome_total_consumed", totalPomesConsumed);
		tag.putBoolean("has_founded_bloodline", hasFoundedBloodline);
		tag.putBoolean("founder_integration_severed", founderIntegrationSevered);
		tag.putBoolean("fungal_revelation_witnessed", fungalRevelationWitnessed);
		tag.putBoolean("fungal_spine_granted", fungalSpineGranted);
		tag.putBoolean("hematic_fortification", hematicFortification);
		tag.putInt("ancestral_communions", ancestralCommunions);
		tag.putString("archon_path", archonPath.name());
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
		hasFoundedBloodline = nbt.getBoolean("has_founded_bloodline");
		founderIntegrationSevered = nbt.getBoolean("founder_integration_severed");
		fungalRevelationWitnessed = nbt.getBoolean("fungal_revelation_witnessed");
		fungalSpineGranted = nbt.getBoolean("fungal_spine_granted");
		hematicFortification = nbt.getBoolean("hematic_fortification");
		ancestralCommunions = Math.max(0, nbt.getInt("ancestral_communions"));
		archonPath = EnumArchonPath.byName(nbt.getString("archon_path"));
		pomeCommunionProgress.clear();
		CompoundTag progressTag = nbt.getCompound("pome_communion_progress");
		for (String key : progressTag.getAllKeys()) {
			try {
				pomeCommunionProgress.put(Long.parseLong(key), progressTag.getInt(key));
			} catch (NumberFormatException ignored) {}
		}
	}
}

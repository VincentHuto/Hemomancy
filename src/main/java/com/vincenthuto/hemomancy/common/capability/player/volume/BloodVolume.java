package com.vincenthuto.hemomancy.common.capability.player.volume;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class BloodVolume implements IBloodVolume, INBTSerializable<CompoundTag> {
	private boolean active = false;
	private double bloodVolume = 0.0;
	private double maxBloodVolume = 5000.0;
	private Bloodline bloodLine = Bloodline.NOBLOODLINE;

	// ── Blood Debt Tracking (Hemorath Encounter) ──
	private double bloodDebt = 0.0;

	// ── Bloodline Pool Donation & Auto-Draw Settings ──
	private boolean trickleEnabled = false;
	private double trickleRate = 0.5;
	private boolean autoDrawEnabled = false;
	private double autoDrawThreshold = 0.25;

	/***
	 * only use if you want to explicitly bypass max volume limits
	 */
	@Override
	public void addBloodVolume(double points) {
		this.bloodVolume += points;
	}

	@Override
	public void addMaxBloodVolume(double points) {
		this.maxBloodVolume += points;
	}

	@Override
	public boolean drain(double points) {
		if (!wouldOverstrain(points)) {
			bloodVolume -= points;
			return true;
		} else {
			bloodVolume = 0;
			return false;
		}
	}

	@Override
	public boolean drainFromSource(IBloodVolume src, double points) {
		if (src.fill(points) ) {
			drain(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean fill(double points) {
		if (!wouldOverflow(points)) {
			bloodVolume += points;
			return true;
		} else {
			bloodVolume = maxBloodVolume;
			return false;
		}

	}

	@Override
	public boolean fillFromSource(IBloodVolume src, double points) {
		if (src.drain(points) && src.getBloodVolume() > points) {
			fill(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Bloodline getBloodLine() {
		return bloodLine;
	}

	@Override
	public double getBloodVolume() {
		return this.bloodVolume;
	}

	@Override
	public double getMaxBloodVolume() {

		return this.maxBloodVolume;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isEmpty() {
		return bloodVolume <= 0;
	}

	@Override
	public boolean isFull() {
		return bloodVolume >= maxBloodVolume;
	}

	@Override
	public void setActive(boolean set) {
		this.active = set;
	}

	@Override
	public void setBloodLine(Bloodline bloodLine) {
		this.bloodLine = bloodLine;
	}

	@Override
	public void setBloodVolume(double points) {
		this.bloodVolume = points;
	}

	@Override
	public void setMaxBloodVolume(double points) {
		this.maxBloodVolume = points;
	}

	/***
	 * only use if you want to explicitly bypass min volume limits
	 */
	@Override
	public void subtractBloodVolume(double points) {
		this.bloodVolume -= points;

	}

	@Override
	public void subtractMaxBloodVolume(double points) {
		this.maxBloodVolume -= points;
	}

	@Override
	public void toggleActive() {
		this.active = !active;
	}

	@Override
	public boolean wouldOverflow(double points) {
		return bloodVolume + points > maxBloodVolume;
	}

	@Override
	public boolean wouldOverstrain(double points) {
		return bloodVolume - points < 0;
	}

	// ── Blood Debt Tracking ──

	@Override
	public void addDamage(double amount) {
		this.bloodDebt += amount;
	}

	@Override
	public void addBloodSpend(double amount) {
		this.bloodDebt += amount;
	}

	@Override
	public double consumeDebt() {
		double debt = this.bloodDebt;
		this.bloodDebt = 0.0;
		return debt;
	}

	@Override
	public double getBloodDebt() {
		return this.bloodDebt;
	}

	@Override
	public void resetBloodDebt() {
		this.bloodDebt = 0.0;
	}

	// ── Bloodline Pool Donation & Auto-Draw ──

	@Override
	public boolean isTrickleEnabled() {
		return trickleEnabled;
	}

	@Override
	public void setTrickleEnabled(boolean enabled) {
		this.trickleEnabled = enabled;
	}

	@Override
	public double getTrickleRate() {
		return trickleRate;
	}

	@Override
	public void setTrickleRate(double rate) {
		this.trickleRate = rate;
	}

	@Override
	public boolean isAutoDrawEnabled() {
		return autoDrawEnabled;
	}

	@Override
	public void setAutoDrawEnabled(boolean enabled) {
		this.autoDrawEnabled = enabled;
	}

	@Override
	public double getAutoDrawThreshold() {
		return autoDrawThreshold;
	}

	@Override
	public void setAutoDrawThreshold(double threshold) {
		this.autoDrawThreshold = threshold;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", active);
		entry.putDouble("Max", maxBloodVolume);
		entry.putDouble("Volume", bloodVolume);
		entry.put("Bloodline", bloodLine.serialize());
		entry.putBoolean("TrickleEnabled", trickleEnabled);
		entry.putDouble("TrickleRate", trickleRate);
		entry.putBoolean("AutoDrawEnabled", autoDrawEnabled);
		entry.putDouble("AutoDrawThreshold", autoDrawThreshold);
		return entry;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag entry) {
		if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")
				&& entry.contains("Bloodline")) {
			active = entry.getBoolean("Active");
			maxBloodVolume = entry.getDouble("Max");
			bloodVolume = entry.getDouble("Volume");
			bloodLine = Bloodline.deserialize(entry.getCompound("Bloodline"));
			trickleEnabled = entry.getBoolean("TrickleEnabled");
			trickleRate = entry.getDouble("TrickleRate");
			autoDrawEnabled = entry.getBoolean("AutoDrawEnabled");
			autoDrawThreshold = entry.getDouble("AutoDrawThreshold");
		}
	}

}

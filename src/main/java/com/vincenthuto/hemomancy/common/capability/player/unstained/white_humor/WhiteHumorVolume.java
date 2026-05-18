package com.vincenthuto.hemomancy.common.capability.player.unstained.white_humor;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class WhiteHumorVolume implements IWhiteHumorVolume, INBTSerializable<CompoundTag> {
	private boolean active = false;
	private double whiteHumorVolume = 0.0;
	private double maxWhiteHumorVolume = 5000.0;
	private Bloodline whiteHumorLine = Bloodline.NOBLOODLINE;

	// ── Bloodline Pool Donation & Auto-Draw Settings ──
	private boolean trickleEnabled = false;
	private double trickleRate = 0.5;
	private boolean autoDrawEnabled = false;
	private double autoDrawThreshold = 0.25;

	/***
	 * only use if you want to explicitly bypass max volume limits
	 */
	@Override
	public void addWhiteHumorVolume(double points) {
		this.whiteHumorVolume += points;
	}

	@Override
	public void addMaxWhiteHumorVolume(double points) {
		this.maxWhiteHumorVolume += points;
	}

	@Override
	public boolean drain(double points) {
		if (!wouldOverstrain(points)) {
			whiteHumorVolume -= points;
			return true;
		} else {
			whiteHumorVolume = 0;
			return false;
		}
	}

	@Override
	public boolean drainFromSource(IWhiteHumorVolume src, double points) {
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
			whiteHumorVolume += points;
			return true;
		} else {
			whiteHumorVolume = maxWhiteHumorVolume;
			return false;
		}

	}

	@Override
	public boolean fillFromSource(IWhiteHumorVolume src, double points) {
		if (src.drain(points) && src.getWhiteHumorVolume() > points) {
			fill(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Bloodline getWhiteHumorLine() {
		return whiteHumorLine;
	}

	@Override
	public double getWhiteHumorVolume() {
		return this.whiteHumorVolume;
	}

	@Override
	public double getMaxWhiteHumorVolume() {

		return this.maxWhiteHumorVolume;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isEmpty() {
		return whiteHumorVolume <= 0;
	}

	@Override
	public boolean isFull() {
		return whiteHumorVolume >= maxWhiteHumorVolume;
	}

	@Override
	public void setActive(boolean set) {
		this.active = set;
	}

	@Override
	public void setWhiteHumorLine(Bloodline whiteHumorLine) {
		this.whiteHumorLine = whiteHumorLine;
	}

	@Override
	public void setWhiteHumorVolume(double points) {
		this.whiteHumorVolume = points;
	}

	@Override
	public void setMaxWhiteHumorVolume(double points) {
		this.maxWhiteHumorVolume = points;
	}

	/***
	 * only use if you want to explicitly bypass min volume limits
	 */
	@Override
	public void subtractWhiteHumorVolume(double points) {
		this.whiteHumorVolume -= points;

	}

	@Override
	public void subtractMaxWhiteHumorVolume(double points) {
		this.maxWhiteHumorVolume -= points;
	}

	@Override
	public void toggleActive() {
		this.active = !active;
	}

	@Override
	public boolean wouldOverflow(double points) {
		return whiteHumorVolume + points > maxWhiteHumorVolume;
	}

	@Override
	public boolean wouldOverstrain(double points) {
		return whiteHumorVolume - points < 0;
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
		entry.putBoolean("Active", isActive());
		entry.putDouble("Max", getMaxWhiteHumorVolume());
		entry.putDouble("Volume", getWhiteHumorVolume());
		entry.put("Bloodline", getWhiteHumorLine().serialize());
		entry.putBoolean("TrickleEnabled", isTrickleEnabled());
		entry.putDouble("TrickleRate", getTrickleRate());
		entry.putBoolean("AutoDrawEnabled", isAutoDrawEnabled());
		entry.putDouble("AutoDrawThreshold", getAutoDrawThreshold());
		return entry;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag entry) {
		if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")
				&& entry.contains("Bloodline")) {
			setActive(entry.getBoolean("Active"));
			setMaxWhiteHumorVolume(entry.getDouble("Max"));
			setWhiteHumorVolume(entry.getDouble("Volume"));
			setWhiteHumorLine(Bloodline.deserialize(entry.getCompound("Bloodline")));
			setTrickleEnabled(entry.getBoolean("TrickleEnabled"));
			setTrickleRate(entry.getDouble("TrickleRate"));
			setAutoDrawEnabled(entry.getBoolean("AutoDrawEnabled"));
			setAutoDrawThreshold(entry.getDouble("AutoDrawThreshold"));
		}
	}

}

package com.vincenthuto.hemomancy.common.capability.player.silthmere;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

public class SilthmereVolume implements ISilthmereVolume {
	private boolean active = false;
	private double silthmereVolume = 0.0;
	private double maxSilthmereVolume = 5000.0;
	private Bloodline silthmereLine = Bloodline.NOBLOODLINE;

	// ── Bloodline Pool Donation & Auto-Draw Settings ──
	private boolean trickleEnabled = false;
	private double trickleRate = 0.5;
	private boolean autoDrawEnabled = false;
	private double autoDrawThreshold = 0.25;

	/***
	 * only use if you want to explicitly bypass max volume limits
	 */
	@Override
	public void addSilthmereVolume(double points) {
		this.silthmereVolume += points;
	}

	@Override
	public void addMaxSilthmereVolume(double points) {
		this.maxSilthmereVolume += points;
	}

	@Override
	public boolean drain(double points) {
		if (!wouldOverstrain(points)) {
			silthmereVolume -= points;
			return true;
		} else {
			silthmereVolume = 0;
			return false;
		}
	}

	@Override
	public boolean drainFromSource(ISilthmereVolume src, double points) {
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
			silthmereVolume += points;
			return true;
		} else {
			silthmereVolume = maxSilthmereVolume;
			return false;
		}

	}

	@Override
	public boolean fillFromSource(ISilthmereVolume src, double points) {
		if (src.drain(points) && src.getSilthmereVolume() > points) {
			fill(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Bloodline getSilthmereLine() {
		return silthmereLine;
	}

	@Override
	public double getSilthmereVolume() {
		return this.silthmereVolume;
	}

	@Override
	public double getMaxSilthmereVolume() {

		return this.maxSilthmereVolume;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isEmpty() {
		return silthmereVolume <= 0;
	}

	@Override
	public boolean isFull() {
		return silthmereVolume >= maxSilthmereVolume;
	}

	@Override
	public void setActive(boolean set) {
		this.active = set;
	}

	@Override
	public void setSilthmereLine(Bloodline silthmereLine) {
		this.silthmereLine = silthmereLine;
	}

	@Override
	public void setSilthmereVolume(double points) {
		this.silthmereVolume = points;
	}

	@Override
	public void setMaxSilthmereVolume(double points) {
		this.maxSilthmereVolume = points;
	}

	/***
	 * only use if you want to explicitly bypass min volume limits
	 */
	@Override
	public void subtractSilthmereVolume(double points) {
		this.silthmereVolume -= points;

	}

	@Override
	public void subtractMaxSilthmereVolume(double points) {
		this.maxSilthmereVolume -= points;
	}

	@Override
	public void toggleActive() {
		this.active = !active;
	}

	@Override
	public boolean wouldOverflow(double points) {
		return silthmereVolume + points > maxSilthmereVolume;
	}

	@Override
	public boolean wouldOverstrain(double points) {
		return silthmereVolume - points < 0;
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

}

package com.vincenthuto.hemomancy.common.capability.player.lethe;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

public class LetheVolume implements ILetheVolume {
	private boolean active = false;
	private double letheVolume = 0.0;
	private double maxLetheVolume = 5000.0;
	private Bloodline letheLine = Bloodline.NOBLOODLINE;

	// ── Bloodline Pool Donation & Auto-Draw Settings ──
	private boolean trickleEnabled = false;
	private double trickleRate = 0.5;
	private boolean autoDrawEnabled = false;
	private double autoDrawThreshold = 0.25;

	/***
	 * only use if you want to explicitly bypass max volume limits
	 */
	@Override
	public void addLetheVolume(double points) {
		this.letheVolume += points;
	}

	@Override
	public void addMaxLetheVolume(double points) {
		this.maxLetheVolume += points;
	}

	@Override
	public boolean drain(double points) {
		if (!wouldOverstrain(points)) {
			letheVolume -= points;
			return true;
		} else {
			letheVolume = 0;
			return false;
		}
	}

	@Override
	public boolean drainFromSource(ILetheVolume src, double points) {
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
			letheVolume += points;
			return true;
		} else {
			letheVolume = maxLetheVolume;
			return false;
		}

	}

	@Override
	public boolean fillFromSource(ILetheVolume src, double points) {
		if (src.drain(points) && src.getLetheVolume() > points) {
			fill(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Bloodline getLetheLine() {
		return letheLine;
	}

	@Override
	public double getLetheVolume() {
		return this.letheVolume;
	}

	@Override
	public double getMaxLetheVolume() {

		return this.maxLetheVolume;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isEmpty() {
		return letheVolume <= 0;
	}

	@Override
	public boolean isFull() {
		return letheVolume >= maxLetheVolume;
	}

	@Override
	public void setActive(boolean set) {
		this.active = set;
	}

	@Override
	public void setLetheLine(Bloodline letheLine) {
		this.letheLine = letheLine;
	}

	@Override
	public void setLetheVolume(double points) {
		this.letheVolume = points;
	}

	@Override
	public void setMaxLetheVolume(double points) {
		this.maxLetheVolume = points;
	}

	/***
	 * only use if you want to explicitly bypass min volume limits
	 */
	@Override
	public void subtractLetheVolume(double points) {
		this.letheVolume -= points;

	}

	@Override
	public void subtractMaxLetheVolume(double points) {
		this.maxLetheVolume -= points;
	}

	@Override
	public void toggleActive() {
		this.active = !active;
	}

	@Override
	public boolean wouldOverflow(double points) {
		return letheVolume + points > maxLetheVolume;
	}

	@Override
	public boolean wouldOverstrain(double points) {
		return letheVolume - points < 0;
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

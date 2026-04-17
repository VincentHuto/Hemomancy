package com.vincenthuto.hemomancy.common.capability.player.silthmere;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

public interface ISilthmereVolume {

	public void addSilthmereVolume(double points);

	public void addMaxSilthmereVolume(double points);

	public boolean drain(double points);

	public boolean drainFromSource(ISilthmereVolume src, double points);

	public boolean fill(double points);

	public boolean fillFromSource(ISilthmereVolume src, double points);

	public Bloodline getSilthmereLine();

	public double getSilthmereVolume();

	public double getMaxSilthmereVolume();

	public boolean isActive();

	public boolean isEmpty();

	public boolean isFull();

	public void setActive(boolean set);

	public void setSilthmereLine(Bloodline bloodLine);

	public void setSilthmereVolume(double points);

	public void setMaxSilthmereVolume(double points);

	public void subtractSilthmereVolume(double points);

	public void subtractMaxSilthmereVolume(double points);

	public void toggleActive();

	public boolean wouldOverflow(double points);

	public boolean wouldOverstrain(double points);

	// ── Bloodline Pool Donation & Auto-Draw Settings ──

	public boolean isTrickleEnabled();

	public void setTrickleEnabled(boolean enabled);

	public double getTrickleRate();

	public void setTrickleRate(double rate);

	public boolean isAutoDrawEnabled();

	public void setAutoDrawEnabled(boolean enabled);

	public double getAutoDrawThreshold();

	public void setAutoDrawThreshold(double threshold);

}

package com.vincenthuto.hemomancy.common.capability.player.lethe;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

public interface ILetheVolume {

	public void addLetheVolume(double points);

	public void addMaxLetheVolume(double points);

	public boolean drain(double points);

	public boolean drainFromSource(ILetheVolume src, double points);

	public boolean fill(double points);

	public boolean fillFromSource(ILetheVolume src, double points);

	public Bloodline getLetheLine();

	public double getLetheVolume();

	public double getMaxLetheVolume();

	public boolean isActive();

	public boolean isEmpty();

	public boolean isFull();

	public void setActive(boolean set);

	public void setLetheLine(Bloodline bloodLine);

	public void setLetheVolume(double points);

	public void setMaxLetheVolume(double points);

	public void subtractLetheVolume(double points);

	public void subtractMaxLetheVolume(double points);

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

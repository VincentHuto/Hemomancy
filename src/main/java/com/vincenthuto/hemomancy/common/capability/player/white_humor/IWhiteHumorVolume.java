package com.vincenthuto.hemomancy.common.capability.player.white_humor;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

public interface IWhiteHumorVolume {

	public void addWhiteHumorVolume(double points);

	public void addMaxWhiteHumorVolume(double points);

	public boolean drain(double points);

	public boolean drainFromSource(IWhiteHumorVolume src, double points);

	public boolean fill(double points);

	public boolean fillFromSource(IWhiteHumorVolume src, double points);

	public Bloodline getWhiteHumorLine();

	public double getWhiteHumorVolume();

	public double getMaxWhiteHumorVolume();

	public boolean isActive();

	public boolean isEmpty();

	public boolean isFull();

	public void setActive(boolean set);

	public void setWhiteHumorLine(Bloodline bloodLine);

	public void setWhiteHumorVolume(double points);

	public void setMaxWhiteHumorVolume(double points);

	public void subtractWhiteHumorVolume(double points);

	public void subtractMaxWhiteHumorVolume(double points);

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

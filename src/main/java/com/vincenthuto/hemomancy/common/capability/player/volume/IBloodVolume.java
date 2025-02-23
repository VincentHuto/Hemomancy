package com.vincenthuto.hemomancy.common.capability.player.volume;

public interface IBloodVolume {

	public void addBloodVolume(double points);

	public void addMaxBloodVolume(double points);

	public boolean drain(double points);

	public boolean drainFromSource(IBloodVolume src, double points);

	public boolean fill(double points);

	public boolean fillFromSource(IBloodVolume src, double points);

	public Bloodline getBloodLine();

	public double getBloodVolume();

	public double getMaxBloodVolume();

	public boolean isActive();

	public boolean isEmpty();

	public boolean isFull();

	public void setActive(boolean set);

	public void setBloodLine(Bloodline bloodLine);

	public void setBloodVolume(double points);

	public void setMaxBloodVolume(double points);

	public void subtractBloodVolume(double points);

	public void subtractMaxBloodVolume(double points);

	public void toggleActive();

	public boolean wouldOverflow(double points);

	public boolean wouldOverstrain(double points);

}

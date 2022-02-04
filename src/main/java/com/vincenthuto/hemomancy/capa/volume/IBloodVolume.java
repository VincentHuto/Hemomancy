package com.vincenthuto.hemomancy.capa.volume;

public interface IBloodVolume {

	public boolean isActive();

	public void subtractBloodVolume(double points);

	public void addBloodVolume(double points);

	public void setBloodVolume(double points);

	public void setActive(boolean set);

	public void toggleActive();

	public double getBloodVolume();

	public double getMaxBloodVolume();

	public void setMaxBloodVolume(double points);

	public void subtractMaxBloodVolume(double points);

	public void addMaxBloodVolume(double points);

	public void setBloodLine(Bloodline bloodLine);

	public boolean isFull();

	public boolean isEmpty();

	public boolean canAcceptFill(double points);

	public boolean canAcceptDrain(double points);

	public boolean fill(double points);

	public boolean drain(double points);

	public boolean drainFromSource(IBloodVolume src, double points);

	public boolean fillFromSource(IBloodVolume src, double points);

	public Bloodline getBloodLine();

}

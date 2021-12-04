package com.vincenthuto.hemomancy.capa.player.volume;

public interface IBloodVolume {

	public boolean isActive();

	public void subtractBloodVolume(float points);

	public void addBloodVolume(float points);

	public void setBloodVolume(float points);

	public void setActive(boolean set);

	public void toggleActive();

	public float getBloodVolume();

	public float getMaxBloodVolume();

	public void setMaxBloodVolume(float points);

	public void subtractMaxBloodVolume(float points);

	public void addMaxBloodVolume(float points);

	public void setBloodLine(Bloodline bloodLine);

	public Bloodline getBloodLine();

}

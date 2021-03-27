package com.huto.hemomancy.capa.volume;

public interface IBloodVolume {

	public void subtractBloodVolume(float points);

	public void addBloodVolume(float points);

	public void setBloodVolume(float points);

	public float getBloodVolume();

	public float getMaxBloodVolume();

	public void setMaxBloodVolume(float points);

	public void subtractMaxBloodVolume(float points);

	public void addMaxBloodVolume(float points);

}

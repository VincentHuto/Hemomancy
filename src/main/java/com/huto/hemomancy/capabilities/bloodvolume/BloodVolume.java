package com.huto.hemomancy.capabilities.bloodvolume;

public class BloodVolume implements IBloodVolume {
	private float bloodVolume = 50000.0F;

	public void subtractBloodVolume(float points) {
		this.bloodVolume -= points;
	}

	public void addBloodVolume(float points) {
		this.bloodVolume += points;
	}

	public void setBloodVolume(float points) {
		this.bloodVolume = points;
	}

	public float getBloodVolume() {
		return this.bloodVolume;
	}

}

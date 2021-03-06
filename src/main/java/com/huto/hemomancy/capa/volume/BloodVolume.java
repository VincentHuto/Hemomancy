package com.huto.hemomancy.capa.volume;

public class BloodVolume implements IBloodVolume {
	private boolean active = false;
	private float bloodVolume = 0.0F;
	private float maxBloodVolume = 5000.0F;

	public void subtractBloodVolume(float points) {
		float curr = this.bloodVolume;
		if (curr - points > 0) {
			this.bloodVolume -= points;
		} else {
			this.bloodVolume = 0;
		}

	}

	public void addBloodVolume(float points) {
		float curr = this.bloodVolume;
		if (curr >= this.maxBloodVolume) {
		}
		if (curr + points < this.maxBloodVolume) {
			this.bloodVolume += points;
		} else {
			this.bloodVolume = this.maxBloodVolume;
		}
	}

	public void setBloodVolume(float points) {
		this.bloodVolume = points;
	}

	public float getBloodVolume() {
		return this.bloodVolume;
	}

	public float getMaxBloodVolume() {

		return this.maxBloodVolume;
	}

	public void setMaxBloodVolume(float points) {
		this.maxBloodVolume = points;
	}

	public void addMaxBloodVolume(float points) {
		this.maxBloodVolume += points;
	}

	public void subtractMaxBloodVolume(float points) {
		this.maxBloodVolume -= points;
	}

	@Override
	public void setActive(boolean set) {
		this.active = set;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void toggleActive() {
		this.active = !active;
	}
}

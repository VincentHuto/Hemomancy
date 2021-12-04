package com.vincenthuto.hemomancy.capa.player.volume;

public class BloodVolume implements IBloodVolume {
	private boolean active = false;
	private float bloodVolume = 0.0F;
	private float maxBloodVolume = 5000.0F;
	private Bloodline bloodLine = Bloodline.NOBLOODLINE;

	@Override
	public Bloodline getBloodLine() {
		return bloodLine;
	}

	@Override
	public void setBloodLine(Bloodline bloodLine) {
		this.bloodLine = bloodLine;
	}

	@Override
	public void subtractBloodVolume(float points) {
		float curr = this.bloodVolume;
		if (curr - points > 0) {
			this.bloodVolume -= points;
		} else {
			this.bloodVolume = 0;
		}

	}

	@Override
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

	@Override
	public void setBloodVolume(float points) {
		this.bloodVolume = points;
	}

	@Override
	public float getBloodVolume() {
		return this.bloodVolume;
	}

	@Override
	public float getMaxBloodVolume() {

		return this.maxBloodVolume;
	}

	@Override
	public void setMaxBloodVolume(float points) {
		this.maxBloodVolume = points;
	}

	@Override
	public void addMaxBloodVolume(float points) {
		this.maxBloodVolume += points;
	}

	@Override
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

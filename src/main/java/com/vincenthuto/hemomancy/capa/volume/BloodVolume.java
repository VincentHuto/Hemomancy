package com.vincenthuto.hemomancy.capa.volume;

public class BloodVolume implements IBloodVolume {
	private boolean active = false;
	private double bloodVolume = 0.0F;
	private double maxBloodVolume = 5000.0F;
	private Bloodline bloodLine = Bloodline.NOBLOODLINE;

	/***
	 * only use if you want to explicitly bypass max volume limits
	 */
	@Override
	public void addBloodVolume(double points) {
		this.bloodVolume += points;
	}

	@Override
	public void addMaxBloodVolume(double points) {
		this.maxBloodVolume += points;
	}

	@Override
	public boolean drain(double points) {
		if (!wouldOverstrain(points)) {
			bloodVolume -= points;
			return true;
		} else {
			bloodVolume = 0;
			return false;
		}
	}

	@Override
	public boolean drainFromSource(IBloodVolume src, double points) {
		if (src.fill(points)) {
			drain(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean fill(double points) {
		if (!wouldOverflow(points)) {
			bloodVolume += points;
			return true;
		} else {
			bloodVolume = maxBloodVolume;
			return false;
		}

	}

	@Override
	public boolean fillFromSource(IBloodVolume src, double points) {
		if (src.drain(points)) {
			fill(points);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Bloodline getBloodLine() {
		return bloodLine;
	}

	@Override
	public double getBloodVolume() {
		return this.bloodVolume;
	}

	@Override
	public double getMaxBloodVolume() {

		return this.maxBloodVolume;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isEmpty() {
		return bloodVolume > 0;
	}

	@Override
	public boolean isFull() {
		return bloodVolume >= maxBloodVolume;
	}

	@Override
	public void setActive(boolean set) {
		this.active = set;
	}

	@Override
	public void setBloodLine(Bloodline bloodLine) {
		this.bloodLine = bloodLine;
	}

	@Override
	public void setBloodVolume(double points) {
		this.bloodVolume = points;
	}

	@Override
	public void setMaxBloodVolume(double points) {
		this.maxBloodVolume = points;
	}

	/***
	 * only use if you want to explicitly bypass min volume limits
	 */
	@Override
	public void subtractBloodVolume(double points) {
		this.bloodVolume -= points;

	}

	@Override
	public void subtractMaxBloodVolume(double points) {
		this.maxBloodVolume -= points;
	}

	@Override
	public void toggleActive() {
		this.active = !active;
	}

	@Override
	public boolean wouldOverflow(double points) {
		return bloodVolume + points > maxBloodVolume;
	}

	@Override
	public boolean wouldOverstrain(double points) {
		return bloodVolume - points < 0;
	}

}

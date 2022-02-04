package com.vincenthuto.hemomancy.capa.volume;

public class BloodVolume implements IBloodVolume {
	private boolean active = false;
	private double bloodVolume = 0.0F;
	private double maxBloodVolume = 5000.0F;
	private Bloodline bloodLine = Bloodline.NOBLOODLINE;

	@Override
	public Bloodline getBloodLine() {
		return bloodLine;
	}

	
	@Override
	public void setBloodLine(Bloodline bloodLine) {
		this.bloodLine = bloodLine;
	}

	/***
	 * only use if you want to explicitly bypass min volume limits
	 */
	@Override
	public void subtractBloodVolume(double points) {
		this.bloodVolume -= points;

	}
	/***
	 * only use if you want to explicitly bypass max volume limits
	 */
	@Override
	public void addBloodVolume(double points) {
		this.bloodVolume += points;
	}

	@Override
	public void setBloodVolume(double points) {
		this.bloodVolume = points;
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
	public void setMaxBloodVolume(double points) {
		this.maxBloodVolume = points;
	}

	@Override
	public void addMaxBloodVolume(double points) {
		this.maxBloodVolume += points;
	}

	@Override
	public void subtractMaxBloodVolume(double points) {
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

	@Override
	public boolean isFull() {
		return bloodVolume >= maxBloodVolume;
	}

	@Override
	public boolean isEmpty() {
		return bloodVolume > 0;
	}

	@Override
	public boolean canAcceptFill(double points) {
		return !isFull() && maxBloodVolume >= bloodVolume + points;
	}

	@Override
	public boolean canAcceptDrain(double points) {
		return bloodVolume - points >= 0f;
	}

	@Override
	public boolean fill(double points) {
		if (canAcceptFill(points)) {
			bloodVolume += points;
			return true;
		} else {
			return false;
		}

	}

	@Override
	public boolean drain(double points) {
		if (canAcceptDrain(points)) {
			bloodVolume -= points;
			return true;
		} else {
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
	public boolean fillFromSource(IBloodVolume src, double points) {
		if (src.drain(points)) {
			fill(points);
			return true;
		} else {
			return false;
		}
	}
}

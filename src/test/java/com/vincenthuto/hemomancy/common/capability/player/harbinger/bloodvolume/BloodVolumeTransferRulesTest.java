package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public final class BloodVolumeTransferRulesTest {
	private BloodVolumeTransferRulesTest() {
	}

	public static void main(String[] args) {
		requestCanTakeAllTheSourceHolds();
		requestClampsToRemainingTargetCapacity();
		emptyOrFullVolumesMoveNothing();
	}

	private static void requestCanTakeAllTheSourceHolds() {
		assertEquals("exact available amount can move", 150.0,
				BloodVolumeTransferRules.transferableAmount(volume(0, 500), volume(150, 500), 150));
		assertEquals("never more than the source holds", 100.0,
				BloodVolumeTransferRules.transferableAmount(volume(0, 500), volume(100, 500), 150));
	}

	private static void requestClampsToRemainingTargetCapacity() {
		assertEquals("only what fits", 50.0,
				BloodVolumeTransferRules.transferableAmount(volume(450, 500), volume(500, 500), 150));
	}

	private static void emptyOrFullVolumesMoveNothing() {
		assertEquals("empty source", 0.0,
				BloodVolumeTransferRules.transferableAmount(volume(0, 500), volume(0, 500), 150));
		assertEquals("full target", 0.0,
				BloodVolumeTransferRules.transferableAmount(volume(500, 500), volume(500, 500), 150));
		assertEquals("no request", 0.0,
				BloodVolumeTransferRules.transferableAmount(volume(0, 500), volume(500, 500), 0));
	}

	private static TestBloodVolume volume(double current, double max) {
		return new TestBloodVolume(current, max);
	}

	private static void assertEquals(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static final class TestBloodVolume implements IBloodVolume {
		private double blood;
		private double max;

		private TestBloodVolume(double blood, double max) {
			this.blood = blood;
			this.max = max;
		}

		@Override
		public void addBloodVolume(double points) {
			blood += points;
		}

		@Override
		public void addMaxBloodVolume(double points) {
			max += points;
		}

		@Override
		public boolean drain(double points) {
			if (wouldOverstrain(points)) {
				blood = 0;
				return false;
			}
			blood -= points;
			return true;
		}

		@Override
		public boolean fill(double points) {
			if (wouldOverflow(points)) {
				blood = max;
				return false;
			}
			blood += points;
			return true;
		}

		@Override
		public Bloodline getBloodLine() {
			return Bloodline.NOBLOODLINE;
		}

		@Override
		public double getBloodVolume() {
			return blood;
		}

		@Override
		public double getMaxBloodVolume() {
			return max;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return blood <= 0;
		}

		@Override
		public boolean isFull() {
			return blood >= max;
		}

		@Override
		public void setActive(boolean set) {
		}

		@Override
		public void setBloodLine(Bloodline bloodLine) {
		}

		@Override
		public void setBloodVolume(double points) {
			blood = Math.max(0, Math.min(points, max));
		}

		@Override
		public void setMaxBloodVolume(double points) {
			max = points;
		}

		@Override
		public void subtractBloodVolume(double points) {
			blood -= points;
		}

		@Override
		public void subtractMaxBloodVolume(double points) {
			max -= points;
		}

		@Override
		public void toggleActive() {
		}

		@Override
		public boolean wouldOverflow(double points) {
			return blood + points > max;
		}

		@Override
		public boolean wouldOverstrain(double points) {
			return blood - points < 0;
		}

		@Override
		public void addDamage(double amount) {
		}

		@Override
		public void addBloodSpend(double amount) {
		}

		@Override
		public double consumeDebt() {
			return 0;
		}

		@Override
		public double getBloodDebt() {
			return 0;
		}

		@Override
		public void resetBloodDebt() {
		}

		@Override
		public boolean isTrickleEnabled() {
			return false;
		}

		@Override
		public void setTrickleEnabled(boolean enabled) {
		}

		@Override
		public double getTrickleRate() {
			return 0;
		}

		@Override
		public void setTrickleRate(double rate) {
		}

		@Override
		public boolean isAutoDrawEnabled() {
			return false;
		}

		@Override
		public void setAutoDrawEnabled(boolean enabled) {
		}

		@Override
		public double getAutoDrawThreshold() {
			return 0;
		}

		@Override
		public void setAutoDrawThreshold(double threshold) {
		}

		@Override
		public boolean isBloodRoutingOptInEnabled() {
			return false;
		}

		@Override
		public void setBloodRoutingOptInEnabled(boolean enabled) {
		}
	}
}

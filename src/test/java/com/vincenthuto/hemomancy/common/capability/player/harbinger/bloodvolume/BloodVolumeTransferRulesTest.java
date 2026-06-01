package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public final class BloodVolumeTransferRulesTest {
	private BloodVolumeTransferRulesTest() {
	}

	public static void main(String[] args) {
		requestCanDrainTheSourceToZeroWhenThatBloodActuallyMoves();
		requestClampsToRemainingTargetCapacityBeforeDrainingSource();
		emptyOrFullVolumesDoNotMoveBlood();
	}

	private static void requestCanDrainTheSourceToZeroWhenThatBloodActuallyMoves() {
		TestBloodVolume source = volume(150, 500);
		TestBloodVolume target = volume(0, 500);

		boolean moved = BloodVolumeTransferRules.fillFromSource(target, source, 150);

		assertTrue("exact available amount should move", moved);
		assertEquals("source may be drained to zero", 0.0, source.getBloodVolume());
		assertEquals("target receives the requested amount", 150.0, target.getBloodVolume());
	}

	private static void requestClampsToRemainingTargetCapacityBeforeDrainingSource() {
		TestBloodVolume source = volume(500, 500);
		TestBloodVolume target = volume(450, 500);

		boolean moved = BloodVolumeTransferRules.fillFromSource(target, source, 150);

		assertTrue("partial target capacity should still move blood", moved);
		assertEquals("source only loses what can fit", 450.0, source.getBloodVolume());
		assertEquals("target fills exactly to capacity", 500.0, target.getBloodVolume());
	}

	private static void emptyOrFullVolumesDoNotMoveBlood() {
		TestBloodVolume emptySource = volume(0, 500);
		TestBloodVolume target = volume(0, 500);
		assertFalse("empty source cannot transfer",
				BloodVolumeTransferRules.fillFromSource(target, emptySource, 150));

		TestBloodVolume source = volume(500, 500);
		TestBloodVolume fullTarget = volume(500, 500);
		assertFalse("full target cannot receive",
				BloodVolumeTransferRules.fillFromSource(fullTarget, source, 150));
		assertEquals("failed full-target transfer does not drain source", 500.0, source.getBloodVolume());
	}

	private static TestBloodVolume volume(double current, double max) {
		return new TestBloodVolume(current, max);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
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
		public boolean drainFromSource(IBloodVolume src, double points) {
			return false;
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
		public boolean fillFromSource(IBloodVolume src, double points) {
			return BloodVolumeTransferRules.fillFromSource(this, src, points);
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

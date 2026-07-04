package com.vincenthuto.hemomancy.common.event;

public final class TriadAttributeCapsTest {
	private TriadAttributeCapsTest() {
	}

	public static void main(String[] args) {
		aLoneLayerPassesThroughUnchanged();
		stackedLayersAreClampedToTheBudget();
		degenerateInputsAreSafe();
	}

	private static void aLoneLayerPassesThroughUnchanged() {
		assertDouble("chitinite toughness alone is untouched", 2.0D,
				TriadAttributeCaps.clampToughness(0.0D, 2.0D));
		assertDouble("a single speed source under the cap is untouched", 0.15D,
				TriadAttributeCaps.clampMoveSpeed(0.0D, 0.15D));
	}

	private static void stackedLayersAreClampedToTheBudget() {
		assertDouble("third toughness layer only gets the room left", 1.0D,
				TriadAttributeCaps.clampToughness(3.0D, 2.0D));
		assertDouble("a full budget grants nothing more", 0.0D,
				TriadAttributeCaps.clampToughness(TriadAttributeCaps.MAX_TRIAD_TOUGHNESS_BONUS, 2.0D));
		assertDouble("speed stacking clips at thirty percent", 0.10D,
				TriadAttributeCaps.clampMoveSpeed(0.20D, 0.15D));
	}

	private static void degenerateInputsAreSafe() {
		assertDouble("negative requests grant nothing", 0.0D,
				TriadAttributeCaps.clampContribution(0.0D, -1.0D, 4.0D));
		assertDouble("negative existing contribution does not inflate the room", 2.0D,
				TriadAttributeCaps.clampContribution(-3.0D, 2.0D, 4.0D));
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

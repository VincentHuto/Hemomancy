package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

public final class FungalScarHelperRulesTest {
	private FungalScarHelperRulesTest() {
	}

	public static void main(String[] args) {
		rhizovittaRefundOnlyAppliesWhenRooted();
		putrivoraDigestsOnlyPositiveDurationsAndCapsHealing();
		cryostromaConserveRampRequiresStillnessAndCapsAtMax();
	}

	private static void rhizovittaRefundOnlyAppliesWhenRooted() {
		assertDouble("rooted refund", 15.0D, RootedStateHelper.refundAmount(100.0D, true));
		assertDouble("not rooted refund", 0.0D, RootedStateHelper.refundAmount(100.0D, false));
		assertDouble("rooted regen per second", 4.0D, RootedStateHelper.regenPerSecond(true));
		assertDouble("unrooted regen per second", 0.0D, RootedStateHelper.regenPerSecond(false));
	}

	private static void putrivoraDigestsOnlyPositiveDurationsAndCapsHealing() {
		assertEquals("duration step", 160, AfflictionDigestHelper.stepDurationTicks(200));
		assertEquals("duration clamps", 0, AfflictionDigestHelper.stepDurationTicks(12));
		assertDouble("two effects feed", 4.0D, AfflictionDigestHelper.bloodFeedForEffectSeconds(2));
		assertDouble("healing cap", 0.5D, AfflictionDigestHelper.healForEffectSeconds(4));
	}

	private static void cryostromaConserveRampRequiresStillnessAndCapsAtMax() {
		assertEquals("still ticks increment", 61, ConserveStateHelper.nextStillTicks(60, true, false));
		assertEquals("movement resets", 0, ConserveStateHelper.nextStillTicks(60, false, false));
		assertEquals("casting resets", 0, ConserveStateHelper.nextStillTicks(60, true, true));
		assertDouble("entry multiplier", 1.0D, ConserveStateHelper.multiplierForStillTicks(60));
		assertDouble("half ramp multiplier", 2.5D, ConserveStateHelper.multiplierForStillTicks(360));
		assertDouble("max ramp multiplier", 4.0D, ConserveStateHelper.multiplierForStillTicks(900));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

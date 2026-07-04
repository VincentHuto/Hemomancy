package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusProfile;

public final class WillAbsorptionRulesTest {
	private WillAbsorptionRulesTest() {
	}

	public static void main(String[] args) {
		defaultsMatchStagedStrugglePlan();
		stageThresholdsTrackProgress();
		progressRatesScaleAndClamp();
		escapeHealthUsesConfiguredFloor();
	}

	private static void defaultsMatchStagedStrugglePlan() {
		assertClose("default progress required", 100.0F, WillAbsorptionRules.progressRequired(0.0D));
		assertEquals("default grace", 20, WillAbsorptionRules.graceTicks(0));
		assertEquals("default rage", 160, WillAbsorptionRules.rageTicks(0));
		assertClose("default escape floor", 0.4F, (float) WillAbsorptionRules.escapeHealthFraction(-1.0D));
	}

	private static void stageThresholdsTrackProgress() {
		assertEquals("empty progress is latch stage", 0, WillAbsorptionRules.stageForProgress(0.0F, 100.0F));
		assertEquals("below first threshold is latch stage", 0, WillAbsorptionRules.stageForProgress(32.9F, 100.0F));
		assertEquals("first threshold is unravel stage", 1, WillAbsorptionRules.stageForProgress(33.0F, 100.0F));
		assertEquals("below second threshold is unravel stage", 1, WillAbsorptionRules.stageForProgress(66.9F, 100.0F));
		assertEquals("second threshold is consume stage", 2, WillAbsorptionRules.stageForProgress(67.0F, 100.0F));
		assertEquals("complete progress stays consume stage", 2, WillAbsorptionRules.stageForProgress(100.0F, 100.0F));
	}

	private static void progressRatesScaleAndClamp() {
		assertClose("bare progress rate", 1.0F, WillAbsorptionRules.bareProgressPerTick());
		assertClose("unfocused staff progress rate", 1.5F,
				WillAbsorptionRules.staffProgressPerTick(LivingStaffFocusProfile.NONE));
		LivingStaffFocusProfile heavilyFocused = new LivingStaffFocusProfile(0, 5, 5, 0, 5, true);
		assertClose("staff progress rate clamps", 2.25F, WillAbsorptionRules.staffProgressPerTick(heavilyFocused));
		LivingStaffFocusProfile modestFocus = new LivingStaffFocusProfile(0, 1, 0, 1, 0, false);
		assertClose("staff progress rate scales with draw and focus", 1.65F,
				WillAbsorptionRules.staffProgressPerTick(modestFocus));
	}

	private static void escapeHealthUsesConfiguredFloor() {
		assertClose("escape floor restores at least configured fraction", 12.0F,
				WillAbsorptionRules.escapeHealthFloor(30.0F, 0.4D));
		assertClose("escape floor clamps low config", 12.0F,
				WillAbsorptionRules.escapeHealthFloor(30.0F, -1.0D));
		assertClose("escape floor clamps high config", 30.0F,
				WillAbsorptionRules.escapeHealthFloor(30.0F, 2.0D));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertClose(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

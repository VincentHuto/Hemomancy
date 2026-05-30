package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffFocusRulesTest {
	private LivingStaffFocusRulesTest() {
	}

	public static void main(String[] args) {
		bareAbsorptionOnlyTargetsOneCreature();
		unskilledStaffAbsorbsAndProjectsFasterThanBareHands();
		livingConduitIncreasesAbsorptionTargetsAndRange();
		vascularDrawIncreasesAbsorptionAmountAndPulseSpeed();
		crimsonProjectionIncreasesProjectionRates();
		vesperMemoryIsTheHighestFocusTier();
	}

	private static void bareAbsorptionOnlyTargetsOneCreature() {
		assertEquals("bare absorption target cap", 1,
				LivingStaffFocusRules.absorptionTargetCap(false, LivingStaffFocusProfile.NONE));
	}

	private static void unskilledStaffAbsorbsAndProjectsFasterThanBareHands() {
		int bareTargets = LivingStaffFocusRules.absorptionTargetCap(false, LivingStaffFocusProfile.NONE);
		int staffTargets = LivingStaffFocusRules.absorptionTargetCap(true, LivingStaffFocusProfile.NONE);
		assertTrue("living staff can absorb from multiple targets", staffTargets > bareTargets);

		double bareProjection = LivingStaffFocusRules.structureProjectionRate(false, LivingStaffFocusProfile.NONE);
		double staffProjection = LivingStaffFocusRules.structureProjectionRate(true, LivingStaffFocusProfile.NONE);
		assertTrue("living staff projects faster than bare blood projection", staffProjection > bareProjection);
	}

	private static void livingConduitIncreasesAbsorptionTargetsAndRange() {
		LivingStaffFocusProfile none = LivingStaffFocusProfile.NONE;
		LivingStaffFocusProfile maxConduit = new LivingStaffFocusProfile(3, 0, 0, false);
		assertTrue("living conduit increases absorption target cap",
				LivingStaffFocusRules.absorptionTargetCap(true, maxConduit)
						> LivingStaffFocusRules.absorptionTargetCap(true, none));
		assertTrue("living conduit increases absorption range",
				LivingStaffFocusRules.absorptionRange(maxConduit)
						> LivingStaffFocusRules.absorptionRange(none));
	}

	private static void vascularDrawIncreasesAbsorptionAmountAndPulseSpeed() {
		LivingStaffFocusProfile none = LivingStaffFocusProfile.NONE;
		LivingStaffFocusProfile maxDraw = new LivingStaffFocusProfile(0, 3, 0, false);
		assertTrue("vascular draw increases absorption amount",
				LivingStaffFocusRules.absorptionDamagePerTarget(maxDraw)
						> LivingStaffFocusRules.absorptionDamagePerTarget(none));
		assertTrue("vascular draw makes absorption pulse faster",
				LivingStaffFocusRules.absorptionPulseIntervalTicks(maxDraw)
						< LivingStaffFocusRules.absorptionPulseIntervalTicks(none));
	}

	private static void crimsonProjectionIncreasesProjectionRates() {
		LivingStaffFocusProfile none = LivingStaffFocusProfile.NONE;
		LivingStaffFocusProfile maxProjection = new LivingStaffFocusProfile(0, 0, 3, false);
		assertTrue("crimson projection increases structure feed rate",
				LivingStaffFocusRules.structureProjectionRate(true, maxProjection)
						> LivingStaffFocusRules.structureProjectionRate(true, none));
		assertTrue("crimson projection increases blood tile feed rate",
				LivingStaffFocusRules.bloodTileProjectionRate(true, maxProjection)
						> LivingStaffFocusRules.bloodTileProjectionRate(true, none));
	}

	private static void vesperMemoryIsTheHighestFocusTier() {
		LivingStaffFocusProfile maxSkills = new LivingStaffFocusProfile(3, 3, 3, false);
		LivingStaffFocusProfile vesper = new LivingStaffFocusProfile(3, 3, 3, true);
		int normalTargets = LivingStaffFocusRules.absorptionTargetCap(true, maxSkills);
		int vesperTargets = LivingStaffFocusRules.absorptionTargetCap(true, vesper);
		assertTrue("Vesper-awakened staff has highest absorption target cap", vesperTargets > normalTargets);

		double normalProjection = LivingStaffFocusRules.structureProjectionRate(true, maxSkills);
		double vesperProjection = LivingStaffFocusRules.structureProjectionRate(true, vesper);
		assertTrue("Vesper-awakened staff has highest projection rate", vesperProjection > normalProjection);
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}
}

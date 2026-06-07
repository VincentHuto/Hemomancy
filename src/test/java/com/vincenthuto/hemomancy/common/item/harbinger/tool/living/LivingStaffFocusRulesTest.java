package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffFocusRulesTest {
	private LivingStaffFocusRulesTest() {
	}

	public static void main(String[] args) {
		bareAbsorptionOnlyTargetsOneCreature();
		unskilledStaffAbsorbsAndProjectsFasterThanBareHands();
		unskilledStaffAbsorptionMatchesBareHandOnOneTargetAndBeatsItWithTwo();
		livingConduitIncreasesAbsorptionTargetsAndRange();
		vascularDrawIncreasesAbsorptionAmountAndPulseSpeed();
		staffBlockAbsorptionPerTickScalesWithFocus();
		crimsonProjectionIncreasesProjectionRates();
		vesperMemoryIsTheHighestFocusTier();
		hematicFocusImprovesMidgameStaffFocus();
		vespersRefusalOnlyAmplifiesAwakenedMemory();
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

	private static void unskilledStaffAbsorptionMatchesBareHandOnOneTargetAndBeatsItWithTwo() {
		double bareRate = LivingStaffFocusRules.bareAbsorptionPerSecond();
		double oneTargetStaffRate = LivingStaffFocusRules.staffAbsorptionPerSecond(LivingStaffFocusProfile.NONE, 1);
		double twoTargetStaffRate = LivingStaffFocusRules.staffAbsorptionPerSecond(LivingStaffFocusProfile.NONE, 2);
		assertTrue("unskilled staff should at least match bare absorption on one target",
				oneTargetStaffRate >= bareRate);
		assertTrue("unskilled staff should beat bare absorption when two targets are available",
				twoTargetStaffRate > bareRate);
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

	private static void staffBlockAbsorptionPerTickScalesWithFocus() {
		LivingStaffFocusProfile none = LivingStaffFocusProfile.NONE;
		LivingStaffFocusProfile maxDraw = new LivingStaffFocusProfile(0, 3, 0, false);
		LivingStaffFocusProfile focused = new LivingStaffFocusProfile(3, 3, 3, 3, 0, false);
		assertDouble("bare block absorption uses the dedicated bare tile rate",
				LivingStaffFocusRules.bloodTileProjectionRate(false, LivingStaffFocusProfile.NONE),
				LivingStaffFocusRules.bareBlockAbsorptionPerTick());
		assertTrue("staff block absorption is much faster than entity pulse absorption",
				LivingStaffFocusRules.blockAbsorptionPerTick(none)
						> LivingStaffFocusRules.staffAbsorptionPerSecond(none, 1) / 20.0D);
		assertTrue("vascular draw strongly increases staff block absorption",
				LivingStaffFocusRules.blockAbsorptionPerTick(maxDraw)
						> LivingStaffFocusRules.blockAbsorptionPerTick(none));
		assertTrue("staff block absorption scales with focus",
				LivingStaffFocusRules.blockAbsorptionPerTick(focused)
						> LivingStaffFocusRules.blockAbsorptionPerTick(none));
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

	private static void hematicFocusImprovesMidgameStaffFocus() {
		LivingStaffFocusProfile normal = new LivingStaffFocusProfile(3, 3, 3, 0, 0, false);
		LivingStaffFocusProfile focused = new LivingStaffFocusProfile(3, 3, 3, 3, 0, false);
		assertTrue("hematic focus increases staff target cap",
				LivingStaffFocusRules.absorptionTargetCap(true, focused)
						> LivingStaffFocusRules.absorptionTargetCap(true, normal));
		assertTrue("hematic focus increases staff projection rate",
				LivingStaffFocusRules.structureProjectionRate(true, focused)
						> LivingStaffFocusRules.structureProjectionRate(true, normal));
	}

	private static void vespersRefusalOnlyAmplifiesAwakenedMemory() {
		LivingStaffFocusProfile inert = new LivingStaffFocusProfile(3, 3, 3, 3, 3, false);
		LivingStaffFocusProfile focused = new LivingStaffFocusProfile(3, 3, 3, 3, 0, false);
		assertDouble("vesper refusal is inert without awakened memory",
				LivingStaffFocusRules.structureProjectionRate(true, focused),
				LivingStaffFocusRules.structureProjectionRate(true, inert));

		LivingStaffFocusProfile awakened = new LivingStaffFocusProfile(3, 3, 3, 3, 0, true);
		LivingStaffFocusProfile refused = new LivingStaffFocusProfile(3, 3, 3, 3, 3, true);
		assertTrue("vesper refusal amplifies awakened memory",
				LivingStaffFocusRules.structureProjectionRate(true, refused)
						> LivingStaffFocusRules.structureProjectionRate(true, awakened));
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

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

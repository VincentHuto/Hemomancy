package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffFocusRulesTest {
	private LivingStaffFocusRulesTest() {
	}

	public static void main(String[] args) {
		bareAbsorptionOnlyTargetsOneCreature();
		normalStaffAbsorbsAndProjectsFasterThanBareHands();
		vesperMemoryIsTheHighestFocusTier();
	}

	private static void bareAbsorptionOnlyTargetsOneCreature() {
		assertEquals("bare absorption target cap", 1,
				LivingStaffFocusRules.absorptionTargetCap(false, false, 0.0));
	}

	private static void normalStaffAbsorbsAndProjectsFasterThanBareHands() {
		int bareTargets = LivingStaffFocusRules.absorptionTargetCap(false, false, 0.0);
		int staffTargets = LivingStaffFocusRules.absorptionTargetCap(true, false, 0.0);
		assertTrue("living staff can absorb from multiple targets", staffTargets > bareTargets);

		double bareProjection = LivingStaffFocusRules.structureProjectionRate(false, false, 0.0);
		double staffProjection = LivingStaffFocusRules.structureProjectionRate(true, false, 0.0);
		assertTrue("living staff projects faster than bare blood projection", staffProjection > bareProjection);
	}

	private static void vesperMemoryIsTheHighestFocusTier() {
		int normalTargets = LivingStaffFocusRules.absorptionTargetCap(true, false,
				LivingStaffFocusRules.GRAND_FOCUS_BLOOD_HANDLED);
		int vesperTargets = LivingStaffFocusRules.absorptionTargetCap(true, true, 0.0);
		assertTrue("Vesper-awakened staff has highest absorption target cap", vesperTargets > normalTargets);

		double normalProjection = LivingStaffFocusRules.structureProjectionRate(true, false,
				LivingStaffFocusRules.GRAND_FOCUS_BLOOD_HANDLED);
		double vesperProjection = LivingStaffFocusRules.structureProjectionRate(true, true, 0.0);
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

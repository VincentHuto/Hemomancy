package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingAbilityRulesTest {
	private MorphlingAbilityRulesTest() {
	}

	public static void main(String[] args) {
		lumenlaceCamouflageRequiresMaturityAndCover();
		deadmansPurseFeedBankScalesAndCaps();
		winterShroudResilienceUsesStillnessOrLowHealth();
	}

	private static void lumenlaceCamouflageRequiresMaturityAndCover() {
		assertFalse("immature no camouflage", LumenlaceCamouflageRules.isEligible(2, true, 0));
		assertTrue("water camouflage", LumenlaceCamouflageRules.isEligible(3, true, 15));
		assertTrue("low light camouflage", LumenlaceCamouflageRules.isEligible(3, false, 7));
		assertFalse("bright dry no camouflage", LumenlaceCamouflageRules.isEligible(3, false, 8));
		assertTrue("small movement counts still", LumenlaceCamouflageRules.isStillEnough(0.003D));
		assertFalse("large movement breaks stillness", LumenlaceCamouflageRules.isStillEnough(0.02D));
		assertTrue("stillness window matures", LumenlaceCamouflageRules.shouldCamouflage(100L,
				100L - LumenlaceCamouflageRules.STILLNESS_REQUIRED_TICKS));
	}

	private static void deadmansPurseFeedBankScalesAndCaps() {
		assertClose("immature attack deposits nothing", 0.0D,
				DeadmansPurseFeedBankRules.attackDeposit(1, 10.0F));
		assertClose("developing attack deposit", 60.0D,
				DeadmansPurseFeedBankRules.attackDeposit(2, 10.0F));
		assertClose("apex attack deposit scales", 84.0D,
				DeadmansPurseFeedBankRules.attackDeposit(4, 10.0F));
		assertClose("mature kill deposit", 80.0D,
				DeadmansPurseFeedBankRules.killDeposit(3, 20.0F));
		assertClose("large kill deposit caps", 160.0D,
				DeadmansPurseFeedBankRules.killDeposit(5, 200.0F));
	}

	private static void winterShroudResilienceUsesStillnessOrLowHealth() {
		assertFalse("immature no hide", WinterShroudResilienceRules.shouldApplyHide(0,
				true, 20.0F, 20.0F));
		assertTrue("stillness enables hide", WinterShroudResilienceRules.shouldApplyHide(2,
				true, 20.0F, 20.0F));
		assertTrue("low health enables hide", WinterShroudResilienceRules.shouldApplyHide(2,
				false, 8.0F, 20.0F));
		assertEquals("low health improves resistance", 2,
				WinterShroudResilienceRules.resistanceAmplifier(4, false, 4.0F, 20.0F));
		assertTrue("apex low health can molt", WinterShroudResilienceRules.canTunMolt(4,
				8.0F, 20.0F));
		assertFalse("mature cannot molt", WinterShroudResilienceRules.canTunMolt(3,
				8.0F, 20.0F));
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

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertClose(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

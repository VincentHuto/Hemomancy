package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingAbilityRulesTest {
	private MorphlingAbilityRulesTest() {
	}

	public static void main(String[] args) {
		feedBankScalesAndCaps();
	}

	private static void feedBankScalesAndCaps() {
		assertClose("immature attack deposits nothing", 0.0D,
				MorphlingFeedBankRules.attackDeposit(1, 10.0F));
		assertClose("developing attack deposit", 60.0D,
				MorphlingFeedBankRules.attackDeposit(2, 10.0F));
		assertClose("apex attack deposit scales", 84.0D,
				MorphlingFeedBankRules.attackDeposit(4, 10.0F));
		assertClose("mature kill deposit", 80.0D,
				MorphlingFeedBankRules.killDeposit(3, 20.0F));
		assertClose("large kill deposit caps", 160.0D,
				MorphlingFeedBankRules.killDeposit(5, 200.0F));
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

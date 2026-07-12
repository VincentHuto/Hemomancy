package com.vincenthuto.hemomancy.common.item.harbinger;

public final class QliphothPomeRulesTest {
	private QliphothPomeRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("base empowerment duration", 3600L, QliphothPomeRules.empowermentDurationTicks(0));
		assertEquals("gestation extends empowerment duration", 4680L, QliphothPomeRules.empowermentDurationTicks(3));
		assertDouble("gestation deepens empowerment discount", 0.66, QliphothPomeRules.empowermentCostMultiplier(3));
		assertTrue("first pome can ripen", QliphothPomeRules.canRipenPome(0, false));
		assertFalse("pending pome blocks next ripening", QliphothPomeRules.canRipenPome(1, true));
		assertFalse("exhausted bloom cannot ripen", QliphothPomeRules.canRipenPome(9, false));
		assertEquals("next ripe husk is current ripened count", 4L, QliphothPomeRules.nextRipeHuskIndex(4));
		assertTrue("unclaimed pending pome can be picked", QliphothPomeRules.canPickPendingPome(true, false));
		assertFalse("claimed pending pome cannot be picked again", QliphothPomeRules.canPickPendingPome(true, true));
		assertTrue("unbound pome can be consumed normally", QliphothPomeRules.canConsumePickedPome(false, false, false));
		assertTrue("bound pome can be consumed by its owner", QliphothPomeRules.canConsumePickedPome(true, true, true));
		assertFalse("bound pome cannot be consumed without owner data", QliphothPomeRules.canConsumePickedPome(true, false, false));
		assertFalse("bound pome cannot be consumed by another player", QliphothPomeRules.canConsumePickedPome(true, true, false));
		assertTrue("creative players bypass bound pome consumption ownership", QliphothPomeRules.canConsumePickedPome(true, true, false, true));
		assertTrue("survival bound pomes are protected", QliphothPomeRules.shouldProtectBoundPome(true, false));
		assertFalse("creative bound pomes are not protected", QliphothPomeRules.shouldProtectBoundPome(true, true));
		assertFalse("ordinary pomes are not protected", QliphothPomeRules.shouldProtectBoundPome(false, false));
		assertTrue("pruned bloom resets existing pome progress", QliphothPomeRules.shouldResetProgressOnPrune(true, 3));
		assertFalse("missing bloom does not reset pome progress", QliphothPomeRules.shouldResetProgressOnPrune(false, 3));
		assertFalse("no pome progress needs no reset", QliphothPomeRules.shouldResetProgressOnPrune(true, 0));
		assertFalse("eighth pome does not grant the spine", QliphothPomeRules.shouldGrantFungalSpine(8, false));
		assertTrue("ninth pome grants the spine once", QliphothPomeRules.shouldGrantFungalSpine(9, false));
		assertFalse("an already granted spine is not duplicated", QliphothPomeRules.shouldGrantFungalSpine(9, true));
	}

	private static void assertEquals(String label, long expected, long actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean actual) {
		if (!actual) {
			throw new AssertionError(label + ": expected true");
		}
	}

	private static void assertFalse(String label, boolean actual) {
		if (actual) {
			throw new AssertionError(label + ": expected false");
		}
	}
}

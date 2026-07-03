package com.vincenthuto.hemomancy.common.event;

public final class BorrowedBloodRulesTest {
	private BorrowedBloodRulesTest() {
	}

	public static void main(String[] args) {
		depositsRespectTheCap();
		drainsNeverExceedTheReserve();
		overkillIsOnlyTheWastedSlice();
	}

	private static void depositsRespectTheCap() {
		assertDouble("empty reserve accepts a normal deposit", 100.0D,
				BorrowedBloodRules.acceptedDeposit(0.0D, 100.0D, BorrowedBloodRules.DEFAULT_CAP));
		assertDouble("a nearly full reserve accepts only the room left", 50.0D,
				BorrowedBloodRules.acceptedDeposit(450.0D, 100.0D, BorrowedBloodRules.DEFAULT_CAP));
		assertDouble("a full reserve accepts nothing", 0.0D,
				BorrowedBloodRules.acceptedDeposit(500.0D, 100.0D, BorrowedBloodRules.DEFAULT_CAP));
		assertDouble("negative deposits are ignored", 0.0D,
				BorrowedBloodRules.acceptedDeposit(0.0D, -5.0D, BorrowedBloodRules.DEFAULT_CAP));
		assertDouble("a zero cap disables the reserve", 0.0D,
				BorrowedBloodRules.acceptedDeposit(0.0D, 100.0D, 0.0D));
	}

	private static void drainsNeverExceedTheReserve() {
		assertDouble("partial cover", 120.0D, BorrowedBloodRules.drained(120.0D, 300.0D));
		assertDouble("full cover", 300.0D, BorrowedBloodRules.drained(450.0D, 300.0D));
		assertDouble("empty reserve covers nothing", 0.0D, BorrowedBloodRules.drained(0.0D, 300.0D));
		assertDouble("negative requests drain nothing", 0.0D, BorrowedBloodRules.drained(450.0D, -1.0D));
	}

	private static void overkillIsOnlyTheWastedSlice() {
		assertDouble("heal fully absorbed leaves no overkill", 0.0D,
				BorrowedBloodRules.overkillHealing(10.0F, 20.0F, 4.0F));
		assertDouble("heal past max health banks the excess", 3.0D,
				BorrowedBloodRules.overkillHealing(18.0F, 20.0F, 5.0F));
		assertDouble("full-health target banks the whole heal", 5.0D,
				BorrowedBloodRules.overkillHealing(20.0F, 20.0F, 5.0F));
		assertDouble("conversion constant stays stable", 25.0D, BorrowedBloodRules.BLOOD_PER_OVERKILL_HEALTH);
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}

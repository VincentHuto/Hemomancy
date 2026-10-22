package com.vincenthuto.hemomancy.common.capability;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** S-5: the pending-delivery rule the six earned claims share. */
class PendingDeliveryTest {
	@Test
	void aClosedGateDeliversNothingAndKeepsTheClaim() {
		var settlement = PendingDelivery.settle(false, () -> fail("held is not consulted"), new int[] {5},
				index -> fail("nothing is inserted"));
		assertArrayEquals(new int[] {5}, settlement.remaining());
		assertFalse(settlement.moved());
	}

	@Test
	void aCarriedItemSettlesTheClaimWithoutADuplicate() {
		var settlement = PendingDelivery.settle(true, () -> true, new int[] {1}, index -> fail("nothing is inserted"));
		assertArrayEquals(new int[] {0}, settlement.remaining());
		assertTrue(settlement.moved());
	}

	@Test
	void aFullInsertionSettlesTheClaim() {
		var settlement = PendingDelivery.settle(true, () -> false, new int[] {5}, index -> 5);
		assertArrayEquals(new int[] {0}, settlement.remaining());
		assertTrue(settlement.moved());
	}

	@Test
	void aPartialInsertionKeepsOnlyTheRemainder() {
		var settlement = PendingDelivery.settle(true, () -> false, new int[] {5}, index -> 3);
		assertArrayEquals(new int[] {2}, settlement.remaining());
		assertTrue(settlement.moved());
	}

	@Test
	void nothingLandingLeavesTheClaimUntouched() {
		var settlement = PendingDelivery.settle(true, () -> false, new int[] {2}, index -> 0);
		assertArrayEquals(new int[] {2}, settlement.remaining());
		assertFalse(settlement.moved());
	}

	@Test
	void eachEntryRetriesOnlyWhatDidNotLand() {
		var settlement = PendingDelivery.settle(true, () -> false, new int[] {1, 1}, index -> index == 0 ? 1 : 0);
		assertArrayEquals(new int[] {0, 1}, settlement.remaining());
		assertTrue(settlement.moved());
	}

	@Test
	void moreThanOwedNeverMakesTheClaimNegative() {
		var settlement = PendingDelivery.settle(true, () -> false, new int[] {1}, index -> 64);
		assertArrayEquals(new int[] {0}, settlement.remaining());
	}

	@Test
	void creativeDiscardIsNotADelivery() {
		AtomicInteger carried = new AtomicInteger(64);
		// A Creative player's full inventory swallows the stack and reports success, but holds no more items.
		int landed = PendingDelivery.landed(carried::get, () -> {});
		assertEquals(0, landed);
	}

	@Test
	void landedCountsOnlyWhatReachedTheInventory() {
		AtomicInteger carried = new AtomicInteger(3);
		assertEquals(2, PendingDelivery.landed(carried::get, () -> carried.addAndGet(2)));
		assertEquals(0, PendingDelivery.landed(carried::get, () -> carried.addAndGet(-1)), "never negative");
	}
}

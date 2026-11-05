package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MonolithEyeRulesTest {
	private static final int FULL = MonolithEyeRules.CHARGE_TICKS;

	@Test
	void theShownChargeWalksTowardTheSyncedCountOnePerTick() {
		// The server's count lands every fourth tick; between landings the shown count keeps walking.
		assertEquals(1, MonolithEyeRules.shownCharge(0, 4, false, 0.0F));
		assertEquals(4, MonolithEyeRules.shownCharge(3, 4, false, 0.0F));
		assertEquals(4, MonolithEyeRules.shownCharge(4, 4, false, 0.0F), "caught up, it waits for the next landing");
		assertEquals(16, MonolithEyeRules.shownCharge(0, 20, false, 0.0F), "met far behind, it jumps to a landing's distance");
	}

	@Test
	void aLapsedChargeFallsAsTheServersDoes() {
		assertEquals(10, MonolithEyeRules.shownCharge(12, 8, false, 0.0F));
		assertEquals(8, MonolithEyeRules.shownCharge(9, 8, false, 0.0F), "never below the synced count");
	}

	@Test
	void whenTheGateOpensTheTrembleHoldsUntilTheOpeningEyeHasCaughtUp() {
		// The server resets its count to 0 the tick the gate opens; the shown count climbs to full and holds there.
		assertEquals(37, MonolithEyeRules.shownCharge(36, 0, true, 0.0F));
		assertEquals(FULL, MonolithEyeRules.shownCharge(FULL, 0, true, MonolithEyeRules.tremble(FULL) - 0.01F));
		assertEquals(0, MonolithEyeRules.shownCharge(FULL, 0, true, MonolithEyeRules.tremble(FULL)), "then lets go, with no step in the opening");
	}

	@Test
	void theTrembleIsASliverOfTheOpening() {
		assertEquals(0.0F, MonolithEyeRules.tremble(0), 1e-6);
		assertEquals(0.14F, MonolithEyeRules.tremble(FULL), 1e-6);
	}

	@Test
	void theCracksLightFillsWithTheShownChargeAndStaysLitWhileTheGateIsOpen() {
		assertEquals(0, MonolithEyeRules.glowTicks(0, 0, false), "shut and unfed, dark");
		assertEquals(1, MonolithEyeRules.glowTicks(0, 7, false), "fed, the light climbs a tick at a time toward the shown charge");
		assertEquals(7, MonolithEyeRules.glowTicks(7, 7, false));
		assertEquals(37, MonolithEyeRules.glowTicks(36, 36, true), "open, it climbs on to full however the charge is shown");
		assertEquals(FULL, MonolithEyeRules.glowTicks(FULL, 0, true), "and holds there");
	}

	@Test
	void theCracksLightDiesAsTheChargeLapsesOrTheGateShuts() {
		assertEquals(38, MonolithEyeRules.glowTicks(FULL, 0, false), "shut, it goes out two a tick");
		assertEquals(8, MonolithEyeRules.glowTicks(10, 8, false), "never below the shown charge");
	}
}

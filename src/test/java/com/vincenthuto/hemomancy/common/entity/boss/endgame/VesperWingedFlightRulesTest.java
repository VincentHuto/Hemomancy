package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class VesperWingedFlightRulesTest {
	@Test
	void growthTriggersOnceAtExactlyHalfHealthWithoutBypassingEncounterLocks() {
		assertFalse(VesperWingedFlightRules.shouldStartWingGrowth(260.01F, 520.0F, false, false, false));
		assertTrue(VesperWingedFlightRules.shouldStartWingGrowth(260.0F, 520.0F, false, false, false));
		assertFalse(VesperWingedFlightRules.shouldStartWingGrowth(200.0F, 520.0F, true, false, false));
		assertFalse(VesperWingedFlightRules.shouldStartWingGrowth(200.0F, 520.0F, false, true, false));
		assertFalse(VesperWingedFlightRules.shouldStartWingGrowth(200.0F, 520.0F, false, false, true));
		assertTrue(VesperWingedFlightRules.shouldDeferAnchorExposure(
				VesperWingedFlightRules.FlightMode.WING_GROWTH));
		assertFalse(VesperWingedFlightRules.shouldDeferAnchorExposure(
				VesperWingedFlightRules.FlightMode.GROUNDED));
	}

	@Test
	void exactStageDurationsAndGroundedCooldownBoundEverySortie() {
		assertFalse(VesperWingedFlightRules.mayStartSortie(159, true, true));
		assertTrue(VesperWingedFlightRules.mayStartSortie(160, true, true));
		assertFalse(VesperWingedFlightRules.mayStartSortie(999, true, false));
		assertEquals(60, VesperWingedFlightRules.WING_GROWTH_TICKS);
		assertEquals(20, VesperWingedFlightRules.TAKEOFF_TICKS);
		assertEquals(120, VesperWingedFlightRules.MAX_AIRBORNE_TICKS);
		assertEquals(25, VesperWingedFlightRules.LANDING_TICKS);
	}

	@Test
	void arenaAndAltitudeClampsKeepTheWholeMountInsideTheOrdeal() {
		VesperWingedFlightRules.Point clamped = VesperWingedFlightRules.clampFlightPoint(
				100.0D, -10.0D, -100.0D, 10.0D, 64.0D, -20.0D);
		assertEquals(31.0D, clamped.x(), 0.0001D);
		assertEquals(68.0D, clamped.y(), 0.0001D);
		assertEquals(-41.0D, clamped.z(), 0.0001D);
		assertEquals(74.0D, VesperWingedFlightRules.clampFlightPoint(10, 999, -20, 10, 64, -20).y(), 0.0001D);
		VesperWingedFlightRules.Point ground = VesperWingedFlightRules.clampGroundPoint(
				100.0D, -100.0D, 10.0D, 64.0D, -20.0D);
		assertEquals(31.0D, ground.x(), 0.0001D);
		assertEquals(65.0D, ground.y(), 0.0001D);
		assertEquals(-41.0D, ground.z(), 0.0001D);
	}

	@Test
	void sortiesChooseAnAttackAndNeverHoverPastTheAirborneLimit() {
		assertEquals(VesperWingedFlightRules.AerialAttack.DIVING_REND,
				VesperWingedFlightRules.selectAerialAttack(0, 100));
		assertEquals(VesperWingedFlightRules.AerialAttack.TAIL_NEEDLE_FUSILLADE,
				VesperWingedFlightRules.selectAerialAttack(1, 100));
		assertEquals(VesperWingedFlightRules.AerialAttack.DIVING_REND,
				VesperWingedFlightRules.selectAerialAttack(1, 38));
		assertTrue(VesperWingedFlightRules.mustLand(120, true));
		assertTrue(VesperWingedFlightRules.mustLand(40, false));
		assertFalse(VesperWingedFlightRules.mustLand(119, true));
	}

	@Test
	void fusilladeHasThreeFiveNeedleBurstsAtFourTickSpacing() {
		assertEquals(0, VesperWingedFlightRules.fusilladeNeedleCount(11));
		assertEquals(5, VesperWingedFlightRules.fusilladeNeedleCount(12));
		assertEquals(5, VesperWingedFlightRules.fusilladeNeedleCount(16));
		assertEquals(5, VesperWingedFlightRules.fusilladeNeedleCount(20));
		assertEquals(0, VesperWingedFlightRules.fusilladeNeedleCount(24));
	}

	@Test
	void everyPersistedFlightStageRecoversDeterministically() {
		for (VesperWingedFlightRules.FlightMode mode : VesperWingedFlightRules.FlightMode.values()) {
			VesperWingedFlightRules.FlightMode valid = VesperWingedFlightRules.recoverMode(mode, true, true);
			assertEquals(mode, valid, "valid reload must retain " + mode);
			VesperWingedFlightRules.FlightMode invalid = VesperWingedFlightRules.recoverMode(mode, false, true);
			if (mode.airborne()) assertEquals(VesperWingedFlightRules.FlightMode.LANDING, invalid);
		}
		assertEquals(VesperWingedFlightRules.FlightMode.LANDING,
				VesperWingedFlightRules.recoverMode(VesperWingedFlightRules.FlightMode.DIVING_REND, true, false));
		assertEquals(VesperWingedFlightRules.FlightMode.WING_GROWTH,
				VesperWingedFlightRules.recoverMode(VesperWingedFlightRules.FlightMode.WING_GROWTH, false, false));
	}

	@Test
	void summonArenaRunsTheFullFlightStateMachineWithoutWeakeningOwnedOrdeals() {
		assertEquals(VesperWingedFlightRules.ArenaAuthority.SUMMONED,
				VesperWingedFlightRules.arenaAuthority(false, false, true));
		assertEquals(VesperWingedFlightRules.ArenaAuthority.NONE,
				VesperWingedFlightRules.arenaAuthority(false, false, false));
		assertEquals(VesperWingedFlightRules.ArenaAuthority.OWNED_ORDEAL,
				VesperWingedFlightRules.arenaAuthority(true, true, true));
		assertEquals(VesperWingedFlightRules.ArenaAuthority.NONE,
				VesperWingedFlightRules.arenaAuthority(true, false, true));
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperSickleRules.SickleString;
import static org.junit.jupiter.api.Assertions.*;

final class VesperSickleRulesTest {
	private static RandomSource random(long seed) {
		return RandomSource.create(seed);
	}

	@Test
	void reapingStringIsTwoToFiveAlternatingReaps() {
		for (long seed = 0; seed < 60; seed++) {
			int count = VesperSickleRules.reapCount(random(seed));
			assertTrue(count >= 2 && count <= 5, "count " + count);
		}
		assertEquals(VesperWeaponAction.SICKLE_REAP_L, VesperSickleRules.branch(SickleString.REAPING, 0, 3, true, false,
				3.0D, random(1)));
		assertEquals(VesperWeaponAction.SICKLE_REAP_R, VesperSickleRules.branch(SickleString.REAPING, 1, 3, true, false,
				3.0D, random(1)));
		VesperWeaponAction ender = VesperSickleRules.branch(SickleString.REAPING, 3, 3, true, false, 3.0D, random(1));
		assertTrue(ender == VesperWeaponAction.SICKLE_CROSS_REND || ender == VesperWeaponAction.SICKLE_FEINT_CYCLONE
				|| ender == VesperWeaponAction.SICKLE_CYCLONE, "close ender " + ender);
		assertEquals(VesperWeaponAction.SICKLE_HOOK_REEL,
				VesperSickleRules.branch(SickleString.REAPING, 3, 3, false, false, 9.0D, random(1)),
				"a target backing off out of a string gets the hook thrown after it");
		assertEquals(VesperWeaponAction.NONE, VesperSickleRules.branch(SickleString.REAPING, 4, 3, true, false, 3.0D, random(1)));
	}

	@Test
	void hookCatchBranchesToReelFollowUps() {
		Set<VesperWeaponAction> reels = EnumSet.of(VesperWeaponAction.SICKLE_REEL_CYCLONE,
				VesperWeaponAction.SICKLE_REAPERS_RISE, VesperWeaponAction.SICKLE_PASS_THROUGH);
		Set<VesperWeaponAction> seen = EnumSet.noneOf(VesperWeaponAction.class);
		for (long seed = 0; seed < 80; seed++) {
			VesperWeaponAction next = VesperSickleRules.branch(SickleString.HOOK_AND_REEL, 1, 0, true, false, 2.0D,
					random(seed));
			assertTrue(reels.contains(next), "caught -> " + next);
			seen.add(next);
		}
		assertEquals(reels, seen, "all three reel follow-ups appear");
	}

	@Test
	void hookMissRecoversIntoReaping() {
		assertEquals(VesperWeaponAction.SICKLE_REAP_L,
				VesperSickleRules.branch(SickleString.HOOK_AND_REEL, 1, 0, false, false, 5.0D, random(3)));
	}

	@Test
	void shieldingFavorsTheHookAndCornersFavorTheAnchor() {
		for (long seed = 0; seed < 30; seed++) {
			assertEquals(SickleString.HOOK_AND_REEL,
					VesperSickleRules.choose(3.0D, true, false, SickleString.REAPING, random(seed)));
			assertEquals(SickleString.ANCHOR_HOOK,
					VesperSickleRules.choose(5.0D, false, true, SickleString.CRESCENT, random(seed)));
		}
	}

	@Test
	void choiceNeverRepeatsTheLastString() {
		for (SickleString last : SickleString.values()) {
			for (long seed = 0; seed < 40; seed++) {
				for (double distance : new double[] { 2.0D, 6.0D, 12.0D }) {
					SickleString next = VesperSickleRules.choose(distance, false, false, last, random(seed));
					assertNotEquals(last, next);
					assertNotEquals(SickleString.HARVEST, next, "Harvest is only ever chosen by its trigger");
				}
			}
		}
	}

	@Test
	void spinRateRampsUpAndDown() {
		assertEquals(0.0F, VesperSickleRules.spinRate(0.0F), 1.0E-5F);
		assertTrue(VesperSickleRules.spinRate(0.5F) > VesperSickleRules.spinRate(0.15F));
		assertTrue(VesperSickleRules.spinRate(0.5F) > VesperSickleRules.spinRate(0.9F));
		assertEquals(0.0F, VesperSickleRules.spinRate(1.0F), 1.0E-5F);
	}

	@Test
	void spinsCarryMomentumIntoTheNextMove() {
		assertTrue(VesperSickleRules.carryVelocity(VesperWeaponAction.SICKLE_TRAVELLING_CYCLONE) > 0.0D);
		assertTrue(VesperSickleRules.carryVelocity(VesperWeaponAction.SICKLE_FEINT_CYCLONE) > 0.0D);
		assertEquals(0.0D, VesperSickleRules.carryVelocity(VesperWeaponAction.SICKLE_REAP_L), 1.0E-9D);
	}

	@Test
	void reelBreakThresholds() {
		assertFalse(VesperSickleRules.reelBreaks(11.9F, 29));
		assertTrue(VesperSickleRules.reelBreaks(12.0F, 0));
		assertTrue(VesperSickleRules.reelBreaks(0.0F, 30));
	}

	@Test
	void harvestOnlyOnceBelowTenPercent() {
		assertFalse(VesperSickleRules.harvestReady(61.0F, 600.0F, false));
		assertTrue(VesperSickleRules.harvestReady(59.0F, 600.0F, false));
		assertFalse(VesperSickleRules.harvestReady(59.0F, 600.0F, true));
	}

	@Test
	void longerStringsRecoverLonger() {
		assertTrue(VesperSickleRules.recoveryTicks(5) > VesperSickleRules.recoveryTicks(2));
		assertTrue(VesperSickleRules.recoveryTicks(1) >= 3);
		assertTrue(VesperSickleRules.HARVEST_RECOVERY_TICKS > VesperSickleRules.recoveryTicks(8));
	}

	@Test
	void committedSideHoldsTwentyToFortyTicks() {
		assertFalse(VesperSickleRules.committedSideHolds(19));
		assertTrue(VesperSickleRules.committedSideHolds(20));
		assertTrue(VesperSickleRules.committedSideHolds(40));
		assertFalse(VesperSickleRules.committedSideHolds(41));
	}

	@Test
	void everySickleMoveKeepsAReadableWindup() {
		for (VesperWeaponAction action : VesperSickleRules.ALL_MOVES) {
			assertNull(action.tendency(), action + " is a sickle move");
			assertTrue(action.impactTick() >= 10, action.name());
		}
	}

	@Test
	void hookThrowHoldsItsTetherIntoTheReelFollowUp() {
		assertTrue(VesperSickleRules.holdsTether(VesperWeaponAction.SICKLE_HOOK_REEL));
		assertFalse(VesperSickleRules.holdsTether(VesperWeaponAction.SICKLE_REEL_CYCLONE));
		assertFalse(VesperSickleRules.holdsTether(VesperWeaponAction.SICKLE_REAP_L));
	}

	@Test
	void hookCatchCountsAsLandedEvenWhenTheCloseCutMisses() {
		assertTrue(VesperSickleRules.landed(VesperWeaponAction.SICKLE_HOOK_REEL, false, true));
		assertFalse(VesperSickleRules.landed(VesperWeaponAction.SICKLE_HOOK_REEL, false, false));
		assertFalse(VesperSickleRules.landed(VesperWeaponAction.SICKLE_REAP_L, false, true));
		assertTrue(VesperSickleRules.landed(VesperWeaponAction.SICKLE_REAP_L, true, false));
	}
}

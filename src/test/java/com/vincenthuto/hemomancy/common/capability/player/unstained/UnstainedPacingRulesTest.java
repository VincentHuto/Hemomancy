package com.vincenthuto.hemomancy.common.capability.player.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class UnstainedPacingRulesTest {
	@Test
	void dominantOfferingsHaveFinitePerPlayerRewards() {
		assertEquals(30.0F, UnstainedPacingRules.paleSilverBellReward(false));
		assertEquals(0.0F, UnstainedPacingRules.paleSilverBellReward(true));
		assertEquals(10.0F, UnstainedPacingRules.silverChaliceReward(false));
		assertEquals(0.0F, UnstainedPacingRules.silverChaliceReward(true));
		assertEquals(8.0F, UnstainedPacingRules.letheanBrewReward(0));
		assertEquals(6.0F, UnstainedPacingRules.letheanBrewReward(1));
		assertEquals(4.0F, UnstainedPacingRules.letheanBrewReward(2));
		assertEquals(2.0F, UnstainedPacingRules.letheanBrewReward(3));
		assertEquals(1.0F, UnstainedPacingRules.letheanBrewReward(4));
		assertEquals(0.0F, UnstainedPacingRules.letheanBrewReward(5));
	}

	@Test
	void repeatableEventShapesUsePersistableWorldTimeCooldowns() {
		assertTrue(UnstainedPacingRules.cooldownReady(12_000L, 0L,
				UnstainedPacingRules.XP_REWARD_COOLDOWN_TICKS));
		assertFalse(UnstainedPacingRules.cooldownReady(12_099L, 12_000L,
				UnstainedPacingRules.XP_REWARD_COOLDOWN_TICKS));
		assertTrue(UnstainedPacingRules.cooldownReady(12_100L, 12_000L,
				UnstainedPacingRules.XP_REWARD_COOLDOWN_TICKS));
		assertTrue(UnstainedPacingRules.cooldownReady(5L, 12_000L,
				UnstainedPacingRules.XP_REWARD_COOLDOWN_TICKS));
	}

	@Test
	void pacingProofsSurviveSaveReloadDeathAndDimensionTravel() {
		UnstainedProgress original = new UnstainedProgress();
		original.setClaimedPaleSilverBellReward(true);
		original.setOfferedSilverChalice(true);
		original.setOfferedPoppyWreath(true);
		original.setOfferedPallidIcon(true);
		original.setLetheanBrewOfferings(4);
		original.setLastXpRewardGameTime(1_000L);
		original.setLastCropRewardGameTime(2_000L);
		original.setLastPetHealRewardGameTime(3_000L);
		original.setLastEmptyBloodRewardGameTime(4_000L);

		UnstainedProgress restored = new UnstainedProgress();
		restored.deserializeNBT(null, original.serializeNBT(null));

		assertTrue(restored.hasClaimedPaleSilverBellReward());
		assertTrue(restored.hasOfferedSilverChalice());
		assertTrue(restored.hasOfferedPoppyWreath());
		assertTrue(restored.hasOfferedPallidIcon());
		assertEquals(4, restored.getLetheanBrewOfferings());
		assertEquals(1_000L, restored.getLastXpRewardGameTime());
		assertEquals(2_000L, restored.getLastCropRewardGameTime());
		assertEquals(3_000L, restored.getLastPetHealRewardGameTime());
		assertEquals(4_000L, restored.getLastEmptyBloodRewardGameTime());
	}
}

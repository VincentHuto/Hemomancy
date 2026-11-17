package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core loop Track 8: the Living Staff's growth rings and what each one gives. */
class LivingStaffGrowthRulesTest {
	private static final double EPSILON = 1.0E-9D;

	@Test
	void ringsCloseAtTheirThresholds() {
		assertEquals(0, LivingStaffGrowthRules.ring(0.0D));
		assertEquals(0, LivingStaffGrowthRules.ring(4_999.0D));
		assertEquals(1, LivingStaffGrowthRules.ring(5_000.0D));
		assertEquals(1, LivingStaffGrowthRules.ring(24_999.0D));
		assertEquals(2, LivingStaffGrowthRules.ring(25_000.0D));
		assertEquals(3, LivingStaffGrowthRules.ring(100_000.0D));
		assertEquals(4, LivingStaffGrowthRules.ring(300_000.0D));
		assertEquals(5, LivingStaffGrowthRules.ring(750_000.0D));
		assertEquals(5, LivingStaffGrowthRules.ring(9_000_000.0D), "five rings at most");
		assertEquals(0, LivingStaffGrowthRules.ring(Double.NaN));
		assertEquals(0, LivingStaffGrowthRules.ring(-10.0D));
	}

	@Test
	void theNextRingIsCountedFromWhatHasBeenHandled() {
		assertEquals(5_000.0D, LivingStaffGrowthRules.untilNextRing(0.0D), EPSILON);
		assertEquals(20_000.0D, LivingStaffGrowthRules.untilNextRing(5_000.0D), EPSILON);
		assertEquals(0.0D, LivingStaffGrowthRules.untilNextRing(800_000.0D), EPSILON);
		assertEquals(100_000.0D, LivingStaffGrowthRules.threshold(3), EPSILON);
	}

	@Test
	void reachGrowsAtTheFirstThirdAndFifthRings() {
		assertEquals(0.0D, LivingStaffGrowthRules.reachBonus(0), EPSILON);
		assertEquals(0.5D, LivingStaffGrowthRules.reachBonus(1), EPSILON);
		assertEquals(0.5D, LivingStaffGrowthRules.reachBonus(2), EPSILON);
		assertEquals(1.5D, LivingStaffGrowthRules.reachBonus(3), EPSILON);
		assertEquals(1.5D, LivingStaffGrowthRules.reachBonus(4), EPSILON);
		assertEquals(2.5D, LivingStaffGrowthRules.reachBonus(5), EPSILON);
	}

	@Test
	void theThirdRingQuickensTheChannel() {
		assertEquals(1.0D, LivingStaffGrowthRules.channelRateMultiplier(2), EPSILON);
		assertEquals(1.1D, LivingStaffGrowthRules.channelRateMultiplier(3), EPSILON);
		LivingStaffFocusProfile bare = new LivingStaffFocusProfile(0, 0, 0, 0, 0, false);
		LivingStaffFocusProfile ringed = bare.withRing(3);
		assertEquals(LivingStaffFocusRules.bloodTileProjectionRate(true, bare) * 1.1D,
				LivingStaffFocusRules.bloodTileProjectionRate(true, ringed), EPSILON);
		assertEquals(LivingStaffFocusRules.absorptionRange(bare) + 1.5D,
				LivingStaffFocusRules.absorptionRange(ringed), EPSILON);
		assertEquals(LivingStaffFocusRules.bloodTileProjectionRate(false, bare),
				LivingStaffFocusRules.bloodTileProjectionRate(false, ringed), EPSILON, "a bare hand never ages");
	}

	@Test
	void diggingFormsTakeIronOreAtTheBondDiamondsFromTheSecondRingAndNeverNetherite() {
		assertEquals(LivingStaffGrowthRules.Harvest.STONE, LivingStaffGrowthRules.digTier(0).harvest(),
				"stone-tier harvest already takes iron ore, but not diamonds");
		assertEquals(LivingStaffGrowthRules.Harvest.IRON, LivingStaffGrowthRules.digTier(2).harvest());
		assertEquals(LivingStaffGrowthRules.Harvest.DIAMOND, LivingStaffGrowthRules.digTier(4).harvest());
		assertEquals(LivingStaffGrowthRules.DigTier.BONDED, LivingStaffGrowthRules.digTier(1));
		assertEquals(LivingStaffGrowthRules.DigTier.IRON, LivingStaffGrowthRules.digTier(2));
		assertTrue(LivingStaffGrowthRules.digTier(2).speed() > LivingStaffGrowthRules.digTier(1).speed(),
				"the second ring digs faster");
		assertEquals(LivingStaffGrowthRules.DigTier.IRON, LivingStaffGrowthRules.digTier(3));
		assertEquals(LivingStaffGrowthRules.DigTier.DIAMOND, LivingStaffGrowthRules.digTier(4));
		assertEquals(LivingStaffGrowthRules.DigTier.DIAMOND, LivingStaffGrowthRules.digTier(5));
		assertTrue(LivingStaffGrowthRules.DigTier.DIAMOND.speed() < 9.0F, "never a netherite tool's speed");
	}

	@Test
	void theFifthRingLowersTheSwapFloor() {
		assertEquals(50.0D, LivingStaffGrowthRules.hotSwapFloor(4), EPSILON);
		assertEquals(25.0D, LivingStaffGrowthRules.hotSwapFloor(5), EPSILON);
	}

	@Test
	void diggingCostsBloodByHardness() {
		assertEquals(3.0D, LivingDigRules.bloodPerBlock(1.5F), EPSILON, "stone");
		assertEquals(6.0D, LivingDigRules.bloodPerBlock(3.0F), EPSILON, "an ore");
		assertEquals(9.0D, LivingDigRules.bloodPerBlock(4.5F), EPSILON, "a deepslate ore");
		assertEquals(1.0D, LivingDigRules.bloodPerBlock(0.3F), EPSILON, "never less than 1 mL");
		assertEquals(40.0D, LivingDigRules.bloodPerBlock(50.0F), EPSILON, "obsidian is capped");
		assertEquals(0.0D, LivingDigRules.bloodPerBlock(0.0F), EPSILON, "what breaks at once is free");
		assertEquals(0.0D, LivingDigRules.bloodPerBlock(-1.0F), EPSILON, "bedrock is never charged");
	}
}

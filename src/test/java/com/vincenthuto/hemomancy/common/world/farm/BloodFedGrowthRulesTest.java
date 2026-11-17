package com.vincenthuto.hemomancy.common.world.farm;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 7: one rule for fed ground. The best source sets the level; each level buys more growth; a crop that
 * needs blood never grows unfed and only slowly on a drip; a chill always wins; stamps never shorten; the cloche's
 * price and pace are fixed per stage.
 */
class BloodFedGrowthRulesTest {
	@Test
	void theBestSourceSetsTheLevel() {
		assertEquals(BloodFedGrowthRules.UNFED, BloodFedGrowthRules.level(false, false, false, false));
		assertEquals(BloodFedGrowthRules.DRIP, BloodFedGrowthRules.level(false, false, true, false), "a drip");
		assertEquals(BloodFedGrowthRules.DRIP, BloodFedGrowthRules.level(false, false, false, true), "a wet stain");
		assertEquals(BloodFedGrowthRules.BASIN, BloodFedGrowthRules.level(false, true, true, true), "sources do not add");
		assertEquals(BloodFedGrowthRules.CLOCHE, BloodFedGrowthRules.level(true, true, true, true));
	}

	@Test
	void eachLevelBuysMoreGrowth() {
		float last = -1.0F;
		for (int level = BloodFedGrowthRules.UNFED; level <= BloodFedGrowthRules.CLOCHE; level++) {
			assertTrue(BloodFedGrowthRules.bonusChance(level) > last, "level " + level);
			last = BloodFedGrowthRules.bonusChance(level);
		}
		assertEquals(0.0F, BloodFedGrowthRules.bonusChance(BloodFedGrowthRules.UNFED), "unfed ground changes nothing");
		assertEquals(0.0F, BloodFedGrowthRules.bonusChance(-3), "below the table is unfed");
		assertEquals(1.0F, BloodFedGrowthRules.bonusChance(9), "above it is the cloche");
	}

	@Test
	void anOrdinaryCropKeepsItsRollAndGainsTheBonus() {
		assertEquals(BloodFedGrowthRules.Verdict.DEFAULT, BloodFedGrowthRules.verdict(0, false, false, 0.0F), "unfed: its own roll");
		assertEquals(BloodFedGrowthRules.Verdict.GROW, BloodFedGrowthRules.verdict(1, false, false, 0.24F));
		assertEquals(BloodFedGrowthRules.Verdict.DEFAULT, BloodFedGrowthRules.verdict(1, false, false, 0.26F));
		assertEquals(BloodFedGrowthRules.Verdict.GROW, BloodFedGrowthRules.verdict(2, false, false, 0.49F));
	}

	@Test
	void aCropThatNeedsBloodWaitsForIt() {
		for (float roll : new float[]{0.0F, 0.3F, 0.99F}) {
			assertEquals(BloodFedGrowthRules.Verdict.DO_NOT_GROW, BloodFedGrowthRules.verdict(0, true, false, roll), "never unfed");
			assertEquals(BloodFedGrowthRules.Verdict.DEFAULT, BloodFedGrowthRules.verdict(2, true, false, roll), "its own speed in a basin");
		}
		assertEquals(BloodFedGrowthRules.Verdict.DEFAULT, BloodFedGrowthRules.verdict(1, true, false, 0.3F), "a third of its checks on a drip");
		assertEquals(BloodFedGrowthRules.Verdict.DO_NOT_GROW, BloodFedGrowthRules.verdict(1, true, false, 0.34F));
		assertEquals(0.0D, BloodFedGrowthRules.expectedGrowth(0.3D, 0, true));
		assertEquals(0.1D, BloodFedGrowthRules.expectedGrowth(0.3D, 1, true), 1.0E-6D);
		assertEquals(0.3D, BloodFedGrowthRules.expectedGrowth(0.3D, 2, true), 1.0E-6D);
	}

	@Test
	void aChillAlwaysWins() {
		assertEquals(BloodFedGrowthRules.Verdict.DO_NOT_GROW, BloodFedGrowthRules.verdict(3, false, true, 0.0F));
		assertEquals(BloodFedGrowthRules.Verdict.DO_NOT_GROW, BloodFedGrowthRules.verdict(2, true, true, 0.0F));
	}

	@Test
	void aDripCeilingRoughlyDoublesAFieldsGrowth() {
		double rain = 0.25D;
		assertEquals(0.4375D, BloodFedGrowthRules.expectedGrowth(rain, BloodFedGrowthRules.DRIP, false), 1.0E-6D);
		assertTrue(BloodFedGrowthRules.expectedGrowth(rain, BloodFedGrowthRules.DRIP, false) / rain > 1.7D);
		assertEquals(1.0D, BloodFedGrowthRules.expectedGrowth(rain, BloodFedGrowthRules.CLOCHE, false), 1.0E-6D);
	}

	@Test
	void stampsNeverShortenAndLapseOnTime() {
		assertEquals(1_000L + BloodFedGrowthRules.DRIP_FEED_TICKS,
				BloodFedGrowthRules.restamp(Long.MIN_VALUE, 1_000L, BloodFedGrowthRules.DRIP_FEED_TICKS));
		assertEquals(50_000L, BloodFedGrowthRules.restamp(50_000L, 1_000L, 600), "a later stamp stands");
		assertTrue(BloodFedGrowthRules.stampFeeds(10L, 9L));
		assertFalse(BloodFedGrowthRules.stampFeeds(10L, 10L));
		assertFalse(BloodFedGrowthRules.stampFeeds(Long.MIN_VALUE, 0L), "never stamped");
	}

	@Test
	void aChunkKeepsItsStampsAndDropsTheStale() {
		FedGroundStamps stamps = new FedGroundStamps();
		BlockPos a = new BlockPos(1, 64, 2);
		BlockPos b = new BlockPos(3, 64, 2);
		stamps.stamp(a, 0L, 100);
		assertEquals(100L, stamps.until(a));
		assertEquals(Long.MIN_VALUE, stamps.until(b));
		stamps.stamp(b, 200L, 100);
		assertEquals(1, stamps.size(), "the lapsed stamp went on the next write");
		FedGroundStamps copy = new FedGroundStamps();
		copy.deserializeNBT(null, stamps.serializeNBT(null));
		assertEquals(300L, copy.until(b), "a stamp survives a save");
	}

	@Test
	void theClocheIsPaidByTheStage() {
		assertTrue(BloodFedGrowthRules.clocheStageDue(BloodFedGrowthRules.CLOCHE_STAGE_TICKS));
		assertFalse(BloodFedGrowthRules.clocheStageDue(BloodFedGrowthRules.CLOCHE_STAGE_TICKS - 1));
		assertEquals(4_200, BloodFedGrowthRules.clocheTicksToHarvest(7), "wheat in three and a half minutes");
		assertEquals(175.0D, BloodFedGrowthRules.clocheBloodToHarvest(7), "for 175 mL");
	}

	/** Round two, P7: only a ripe crop on over-fed ground (a basin or a feeding run) taints, on a roll under the chance. */
	@Test
	void onlyARipeCropOnOverFedGroundTaints() {
		assertTrue(BloodFedGrowthRules.taints(BloodFedGrowthRules.BASIN, true, 0.0F));
		assertTrue(BloodFedGrowthRules.taints(BloodFedGrowthRules.BASIN, true, BloodFedGrowthRules.TAINT_CHANCE - 0.001F));
		assertFalse(BloodFedGrowthRules.taints(BloodFedGrowthRules.BASIN, true, BloodFedGrowthRules.TAINT_CHANCE), "the roll is strict");
		assertFalse(BloodFedGrowthRules.taints(BloodFedGrowthRules.BASIN, false, 0.0F), "an unripe crop never taints");
		assertFalse(BloodFedGrowthRules.taints(BloodFedGrowthRules.DRIP, true, 0.0F), "a drip never over-feeds");
		assertFalse(BloodFedGrowthRules.taints(BloodFedGrowthRules.UNFED, true, 0.0F));
		assertFalse(BloodFedGrowthRules.taints(BloodFedGrowthRules.CLOCHE, true, 0.0F), "the cloche's sealed bed never taints");
		assertTrue(BloodFedGrowthRules.taints(BloodFedGrowthRules.BASIN, true, 0.5F, 0.6F), "the data's chance is used");
	}

	/** Round two, P7: a floor spike rises one block at a time, up to its height, and never closes the gap to the tip. */
	@Test
	void aFloorSpikeRisesSlowlyAndStopsShortOfTheTip() {
		assertTrue(BloodFedGrowthRules.floorSpikeMayRise(0, 5, 0.0F), "bare floor under a high tip");
		assertFalse(BloodFedGrowthRules.floorSpikeMayRise(0, 5, BloodFedGrowthRules.FLOOR_GROW_CHANCE), "only on a won roll");
		assertTrue(BloodFedGrowthRules.floorSpikeMayRise(BloodFedGrowthRules.MAX_FLOOR_SPIKE - 1, 5, 0.0F));
		assertFalse(BloodFedGrowthRules.floorSpikeMayRise(BloodFedGrowthRules.MAX_FLOOR_SPIKE, 5, 0.0F), "no taller than its height");
		assertTrue(BloodFedGrowthRules.floorSpikeMayRise(1, BloodFedGrowthRules.FLOOR_SPIKE_GAP, 0.0F), "the last block that keeps the gap");
		assertFalse(BloodFedGrowthRules.floorSpikeMayRise(1, BloodFedGrowthRules.FLOOR_SPIKE_GAP - 1, 0.0F), "never closer to the tip");
	}
}

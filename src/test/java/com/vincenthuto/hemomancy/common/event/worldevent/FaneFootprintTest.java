package com.vincenthuto.hemomancy.common.event.worldevent;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pins the Founding Fane's envelope to the block, so moving its geometry into {@code LobeEnvelope} changes nothing. */
class FaneFootprintTest {
	private static final BlockPos HEART = new BlockPos(100, 64, -200);

	@Test
	void theHeartClaimsFortyBlocksAtAnyHeight() {
		FaneFootprint fane = new FaneFootprint(HEART, List.of(), null);
		assertTrue(fane.contains(HEART.offset(40, 0, 0)));
		assertFalse(fane.contains(HEART.offset(41, 0, 0)));
		assertTrue(fane.contains(HEART.offset(28, 0, 28)));
		assertFalse(fane.contains(HEART.offset(29, 0, 29)));
		assertTrue(fane.contains(HEART.offset(0, 300, 0)), "the Fane is horizontal only");
		assertTrue(fane.contains(HEART.offset(0, -120, 0)));
	}

	@Test
	void stakesAddThirtyTwoBlockLobes() {
		BlockPos stake = HEART.offset(60, 0, 0);
		FaneFootprint fane = new FaneFootprint(HEART, List.of(stake), null);
		assertTrue(fane.contains(HEART.offset(92, 0, 0)));
		assertFalse(fane.contains(HEART.offset(93, 0, 0)));
		assertTrue(fane.containsStake(stake));
	}

	@Test
	void aLegacyCentreClaimsOnlyWithoutAHeart() {
		BlockPos legacy = HEART.offset(500, 0, 0);
		assertTrue(new FaneFootprint(null, List.of(), legacy).contains(legacy.offset(40, 0, 0)));
		assertFalse(new FaneFootprint(HEART, List.of(), legacy).contains(legacy));
		assertFalse(new FaneFootprint(null, List.of(), null).contains(HEART));
	}

	@Test
	void strengthTapersFromTheHeartToTheFarEdge() {
		FaneFootprint fane = new FaneFootprint(HEART, List.of(), null);
		assertEquals(1.0D, fane.effectStrength(HEART), 1e-12);
		assertEquals(1.0D - 20.0D / 40.0D * 0.65D, fane.effectStrength(HEART.offset(20, 0, 0)), 1e-12);
		assertEquals(0.35D, fane.effectStrength(HEART.offset(40, 0, 0)), 1e-12);
		assertEquals(0.0D, fane.effectStrength(HEART.offset(41, 0, 0)));

		FaneFootprint staked = fane.withStake(HEART.offset(60, 0, 0));
		assertEquals(1.0D - 46.0D / 92.0D * 0.65D, staked.effectStrength(HEART.offset(46, 0, 0)), 1e-12);
		assertEquals(0.35D, staked.effectStrength(HEART.offset(92, 0, 0)), 1e-12);

		FaneFootprint legacy = new FaneFootprint(null, List.of(), HEART);
		assertEquals(1.0D - 10.0D / 40.0D * 0.65D, legacy.effectStrength(HEART.offset(10, 0, 0)), 1e-12);
	}

	@Test
	void stakesChainByOverlapWithinTheBudget() {
		FaneFootprint fane = new FaneFootprint(HEART, List.of(), null);
		assertTrue(fane.canAddStake(HEART.offset(72, 0, 0), 3));
		assertFalse(fane.canAddStake(HEART.offset(73, 0, 0), 3), "too far from the heart");
		assertTrue(fane.canAddStake(HEART.offset(0, 200, 72), 3), "height never matters");

		FaneFootprint one = fane.withStake(HEART.offset(72, 0, 0));
		assertTrue(one.canAddStake(HEART.offset(136, 0, 0), 3), "chains off the stake");
		assertFalse(one.canAddStake(HEART.offset(137, 0, 0), 3));
		assertFalse(one.canAddStake(HEART.offset(72, 0, 0), 3), "a stake is placed once");
		assertFalse(one.canAddStake(HEART.offset(10, 0, 0), 1), "the budget is spent");
		assertFalse(new FaneFootprint(null, List.of(), HEART).canAddStake(HEART, 3), "no heart, no stakes");

		FaneFootprint full = fane;
		for (int i = 0; i < FaneFootprint.MAX_STAKE_BUDGET; i++) full = full.withStake(HEART.offset(i, 0, 1));
		assertFalse(full.canAddStake(HEART.offset(20, 0, 20), 99), "the cap holds over a larger budget");
	}

	@Test
	void stakesChainBackToTheHeartForTheMap() {
		BlockPos near = HEART.offset(60, 0, 0);
		BlockPos far = HEART.offset(120, 0, 0);
		FaneFootprint fane = new FaneFootprint(HEART, List.of(near, far), null);
		assertEquals(HEART, fane.chainedFrom(near));
		assertEquals(near, fane.chainedFrom(far), "a stake past the heart's reach chains through its neighbour");
		assertEquals(null, fane.withoutStake(near).chainedFrom(far), "removing the link strands the far stake");
		assertEquals(null, fane.chainedFrom(HEART.offset(5, 0, 5)), "not a stake of this fane");
		assertEquals(152.0D, fane.maxRadius(), 1e-9);
	}

	@Test
	void editsKeepTheRecordImmutable() {
		BlockPos stake = HEART.offset(10, 0, 0);
		FaneFootprint staked = new FaneFootprint(HEART, List.of(), HEART).withStake(stake).withStake(stake);
		assertEquals(List.of(stake), staked.stakes(), "a stake is listed once");
		assertEquals(List.of(), staked.withoutStake(stake).stakes());
		FaneFootprint gone = staked.withoutHeart();
		assertEquals(null, gone.heart());
		assertEquals(List.of(), gone.stakes());
		assertEquals(HEART, gone.legacyCenter());
		assertEquals(HEART.above(), gone.withHeart(HEART.above()).heart());
		assertEquals(4, FaneFootprint.stakeBudget(1, 0));
		assertEquals(FaneFootprint.MAX_STAKE_BUDGET, FaneFootprint.stakeBudget(20, 20));
		assertEquals(3, FaneFootprint.stakeBudget(-4, -4));
	}
}

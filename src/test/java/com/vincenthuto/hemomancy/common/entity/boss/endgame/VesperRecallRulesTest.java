package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.summon.PuppetStringsRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperRecallRulesTest {
	@Test
	void haulLandsOnTheRecallCommitTick() {
		BossMoveTimeline timeline = VesperMountControllerRules.timeline(VesperPhaseOneAttack.RECALL);
		assertEquals(timeline.windup(), VesperRecallRules.HAUL_TICK);
		assertTrue(VesperRecallRules.CUE_TICKS >= VesperRecallRules.HAUL_TICK + PuppetStringsRules.UNSPOOL_TICKS);
	}

	@Test
	void stringsCastOutThenHookBeforeTheHaul() {
		assertEquals(0.0D, VesperRecallRules.reach(0.0D), 1.0E-9);
		assertTrue(VesperRecallRules.reach(VesperRecallRules.CAST_END * 0.5D) > 0.0D);
		assertEquals(1.0D, VesperRecallRules.reach(VesperRecallRules.CAST_END), 1.0E-9);
		assertTrue(VesperRecallRules.CAST_END < VesperRecallRules.HAUL_TICK);
	}

	@Test
	void hookedStringsTightenUntilTheHaul() {
		double cast = VesperRecallRules.slack(VesperRecallRules.CAST_END);
		double mid = VesperRecallRules.slack((VesperRecallRules.CAST_END + VesperRecallRules.HAUL_TICK) / 2.0D);
		double haul = VesperRecallRules.slack(VesperRecallRules.HAUL_TICK);
		assertTrue(cast > mid && mid > haul, cast + " > " + mid + " > " + haul);
		assertEquals(0.0D, VesperRecallRules.slack(VesperRecallRules.HAUL_TICK + 4.0D), 1.0E-9);
	}

	@Test
	void bodyIsReeledIntoTheCrossbarOnlyAfterItIsHauledUp() {
		assertEquals(0.0D, VesperRecallRules.reelIn(VesperRecallRules.HAUL_TICK), 1.0E-9);
		assertEquals(0.0D, VesperRecallRules.reelIn(VesperRecallRules.HAUL_TICK + PuppetStringsRules.SNAP_END), 1.0E-9);
		assertEquals(1.0D, VesperRecallRules.reelIn(VesperRecallRules.CUE_TICKS), 1.0E-9);
		assertTrue(VesperRecallRules.arrival(VesperRecallRules.CUE_TICKS - 2.0D) > 0.0D);
		assertEquals(0.0D, VesperRecallRules.arrival(VesperRecallRules.HAUL_TICK), 1.0E-9);
	}

	@Test
	void crossbarRidesAboveTheThroneAndTurnsWithTheMount() {
		double[] south = VesperRecallRules.crossbarOffset(0.0F);
		double[] west = VesperRecallRules.crossbarOffset(90.0F);
		assertTrue(south[1] > 4.0D, "raised above the rider's head");
		assertEquals(Math.hypot(south[0], south[2]), Math.hypot(west[0], west[2]), 1.0E-9);
		assertNotEquals(south[0], west[0], 1.0E-6);
	}
}

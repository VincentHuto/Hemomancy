package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Track 1: the dispatcher's pure rules. A player pulses at once and then once per cadence, across targets; held work earns
 * one ordinary use per {@link SculptingRules#TICKS_PER_CREDITED_USE} ticks; the Staff raises the work per pulse, never
 * the price. The chain order and the claim itself are checked in a running world ({@code SculptingValidationGameTests}).
 */
class SculptingDispatcherTest {
	@Test
	void aPlayerPulsesAtOnceAndThenOncePerCadenceWhateverTheTarget() {
		assertTrue(SculptingRules.pulseDue(Long.MIN_VALUE, 100L, 10), "the first pulse is immediate");
		assertFalse(SculptingRules.pulseDue(100L, 100L, 10), "never twice in one tick");
		assertFalse(SculptingRules.pulseDue(100L, 105L, 10));
		assertTrue(SculptingRules.pulseDue(100L, 110L, 10));
		assertTrue(SculptingRules.pulseDue(100L, 137L, 10), "a late tick still pulses");
		assertFalse(SculptingRules.pulseDue(100L, 100L, 0), "a cadence below one still waits a tick");
		assertTrue(SculptingRules.pulseDue(100L, 101L, 0));
	}

	@Test
	void heldWorkEarnsOneUsePerFourSeconds() {
		assertEquals(80, SculptingRules.TICKS_PER_CREDITED_USE);
		int credited = 0;
		int banked = 0;
		for (int pulse = 0; pulse < 16; pulse++) {
			credited += SculptingRules.creditedUses(banked, 10);
			banked += 10;
		}
		assertEquals(2, credited, "16 pulses of 10 ticks are 160 ticks of work");
		assertEquals(0, SculptingRules.creditedUses(0, 10), "the first pulse earns nothing yet");
		assertEquals(1, SculptingRules.creditedUses(70, 10), "the pulse that crosses 80 ticks earns one");
		assertEquals(2, SculptingRules.creditedUses(0, 160), "a slow, long pulse can earn two");
	}

	@Test
	void theHudPriceIsPerSecond() {
		assertEquals(30.0D, SculptingRules.bloodPerSecond(15.0D, 10), 1.0E-9D);
		assertEquals(0.0D, SculptingRules.bloodPerSecond(-4.0D, 10), 1.0E-9D);
	}

	@Test
	void theStaffRaisesTheWorkNotThePrice() {
		assertEquals(1.0D, SculptingDispatcher.workScale(null, Verb.PROJECT), 1.0E-9D);
		assertEquals(1.0D, SculptingDispatcher.workScale(null, Verb.ABSORB), 1.0E-9D);
		double unskilled = SculptingDispatcher.workScale(LivingStaffFocusProfile.NONE, Verb.PROJECT);
		double skilled = SculptingDispatcher.workScale(new LivingStaffFocusProfile(0, 0, 3, 3, 0, false), Verb.PROJECT);
		assertTrue(unskilled > 1.0D, "an unskilled Staff already works harder than a bare hand");
		assertTrue(skilled > unskilled, "Crimson Projection and Hematic Focus raise projection work");
		double drawing = SculptingDispatcher.workScale(new LivingStaffFocusProfile(0, 3, 0, 0, 0, false), Verb.ABSORB);
		assertTrue(drawing > SculptingDispatcher.workScale(LivingStaffFocusProfile.NONE, Verb.ABSORB),
				"Vascular Draw raises absorption work");
	}

	/** Round two P4: a manipulation's narrow Sculptings claim ahead of its main one, in the order attached. */
	@Test
	void narrowSculptingsClaimAheadOfTheMainOne() {
		assertEquals(java.util.List.of("freeze", "plant"), SculptingRules.claimOrder(java.util.List.of("freeze"), "plant"));
		assertEquals(java.util.List.of("a", "b", "main"), SculptingRules.claimOrder(java.util.List.of("a", "b"), "main"));
		assertEquals(java.util.List.of("only"), SculptingRules.claimOrder(java.util.List.of("only"), null),
				"a manipulation with only a narrow Sculpting still has it");
		assertEquals(java.util.List.of("main"), SculptingRules.claimOrder(java.util.List.of(), "main"));
		assertTrue(SculptingRules.claimOrder(java.util.List.<String>of(), null).isEmpty());
	}

	/** A sweep faster than the pulse is joined across the cells between, up to the join distance. */
	@Test
	void aSweepIsJoinedAcrossTheCellsBetween() {
		var origin = new net.minecraft.core.BlockPos(0, 64, 0);
		assertEquals(java.util.List.of(new net.minecraft.core.BlockPos(1, 64, 0), new net.minecraft.core.BlockPos(2, 64, 0)),
				SculptingRules.sweepBetween(origin, new net.minecraft.core.BlockPos(3, 64, 0), 3));
		assertTrue(SculptingRules.sweepBetween(origin, new net.minecraft.core.BlockPos(4, 64, 0), 3).isEmpty(),
				"too far apart: a new line, not a join");
		assertTrue(SculptingRules.sweepBetween(origin, origin, 3).isEmpty());
		assertTrue(SculptingRules.sweepBetween(null, origin, 3).isEmpty());
		assertTrue(SculptingRules.sweepBetween(origin, new net.minecraft.core.BlockPos(1, 64, 1), 3).isEmpty(),
				"neighbours need nothing between them");
		assertEquals(1, SculptingRules.sweepBetween(origin, new net.minecraft.core.BlockPos(2, 64, 2), 3).size(),
				"a diagonal is joined through its middle");
	}

	/** Temper never refuses: a dry pulse still raises a wall, but one that stands for less time. */
	@Test
	void aDryPulseRaisesAWallThatStandsShorter() {
		assertEquals(240, SculptingRules.standingTicks(240, 1.0D));
		assertEquals(240, SculptingRules.standingTicks(240, 2.5D), "the Staff raises more, not longer");
		assertEquals(120, SculptingRules.standingTicks(240, 0.5D));
		assertEquals(60, SculptingRules.standingTicks(240, 0.0D), "never under a quarter");
		assertTrue(SculptingRules.standingTicks(200, 0.6D) < SculptingRules.standingTicks(200, 1.0D));
	}

	@Test
	void theVerbsSwitchAndReadBack() {
		assertEquals(Verb.ABSORB, Verb.PROJECT.other());
		assertEquals(Verb.PROJECT, Verb.ABSORB.other());
		assertEquals(Verb.ABSORB, Verb.byName("ABSORB"));
		assertEquals(Verb.PROJECT, Verb.byName("anything else"));
		assertEquals(Verb.ABSORB, Verb.byId(Verb.ABSORB.ordinal()));
		assertEquals(Verb.PROJECT, Verb.byId(99));
		assertEquals("verb.hemomancy.absorb", Verb.ABSORB.langKey());
	}
}

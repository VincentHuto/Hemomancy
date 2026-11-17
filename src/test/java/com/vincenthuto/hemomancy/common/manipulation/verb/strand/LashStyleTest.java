package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.manipulation.verb.Sculptings;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Each lash has a rope and a signature of its own, and the three registered lashes share none of them. */
class LashStyleTest {
	private static LashStyle style(Object sculpting) {
		return ((StrandSculpting) sculpting).spec().lash().style();
	}

	@Test
	void aPlainLashHasOneTipAndNothingElse() {
		LashStyle plain = new LashProfile(4.5F, 20.0D, 12, 0, 30.0D, 40, 1.75F).style();
		assertEquals(LashStyle.PLAIN, plain);
		assertEquals(1, plain.tails());
		assertEquals(6.0F, plain.tailDamage(6.0F));
		assertEquals(LashRules.CRACK_RADIUS, plain.crackRadius(1.0D), 1.0E-9D);
		assertEquals(1.0F, plain.crackHeat(9));
		assertFalse(plain.pierces(1.0D));
	}

	@Test
	void theTetherRootsReturnsBloodAndLingers() {
		LashStyle blood = style(Sculptings.bloodLash());
		assertEquals(40, blood.rootTicks());
		assertEquals(4.0D, blood.sweepReturn());
		assertTrue(blood.fade() > 1.0D);
		assertEquals(1, blood.tails());
	}

	@Test
	void theHeavyWhipIsLongSlowAndBuildsHeat() {
		LashStyle searing = style(Sculptings.searingLashStrand());
		assertTrue(searing.length() > 1.0D && searing.taut() < 1.0D && searing.damping() > 1.0D);
		assertEquals(LashRules.CRACK_RADIUS * 2.0D, searing.crackRadius(1.0D), 1.0E-9D);
		assertEquals(LashRules.CRACK_RADIUS, searing.crackRadius(0.0D), 1.0E-9D);
		assertEquals(1.0F, searing.crackHeat(0));
		assertEquals(1.3F, searing.crackHeat(2), 1.0E-6F);
		assertEquals(1.6F, searing.crackHeat(40), 1.0E-6F, "heat stops at four cracks in a row");
	}

	@Test
	void theRazorIsShortTautThreeTailedAndGoneAtOnce() {
		LashStyle gloam = style(Sculptings.gloamLacerationLash());
		assertTrue(gloam.length() < 1.0D && gloam.taut() > 1.0D && gloam.fade() < 0.5D);
		assertEquals(3, gloam.tails());
		assertEquals(2.4F, gloam.tailDamage(6.0F), 1.0E-6F, "each of three tails lands two fifths of the blow");
		assertEquals(1.75F, gloam.ambush());
		assertEquals(3, gloam.tapChain());
		assertTrue(gloam.faint());
		assertFalse(gloam.pierces(0.5D));
		assertTrue(gloam.pierces(0.6D));
	}

	@Test
	void behindIsOutsideTheFrontHundredAndTwentyDegrees() {
		Vec3 north = new Vec3(0.0D, 0.0D, -1.0D);
		assertFalse(LashStyle.behind(north, new Vec3(0.0D, 0.0D, -3.0D)), "face to face");
		assertFalse(LashStyle.behind(north, new Vec3(1.0D, 5.0D, -1.0D)), "45 degrees off the front, from above");
		assertTrue(LashStyle.behind(north, new Vec3(1.0D, 0.0D, 0.0D)), "square on the flank");
		assertTrue(LashStyle.behind(north, new Vec3(0.0D, 0.0D, 2.0D)), "dead behind");
		assertFalse(LashStyle.behind(north, new Vec3(0.0D, 4.0D, 0.0D)), "straight above is neither");
	}

	@Test
	void aThinChargeBuysNoRootOrFire() {
		assertEquals(0, LashStyle.charged(40, 0.2D));
		assertEquals(20, LashStyle.charged(40, 0.5D));
		assertEquals(40, LashStyle.charged(40, 3.0D));
	}

	@Test
	void aRazorDissolvesInAFewTicksWhereAPlainLashTakesThirty() {
		LashProfile plain = new LashProfile(4.5F, 20.0D, 12, 0, 10.0D, 20, 2.0F);
		LashProfile razor = plain.with(LashStyle.PLAIN.rope(1.0D, 1.0D, 1.0D, 0.15D));
		int rest = LashRules.tuning().restTicksToFade();
		int slow = ticksToOrb(plain) - rest, quick = ticksToOrb(razor) - rest;
		assertTrue(slow >= 29 && slow <= 32, "a plain lash faded in " + slow);
		assertTrue(quick >= 4 && quick <= 6, "a razor faded in " + quick);
	}

	/** Ticks a released lash held dead still takes to be back in the hand. */
	private static int ticksToOrb(LashProfile profile) {
		LashRules.State state = new LashRules.State(false);
		Vec3 hand = new Vec3(0.0D, 1.5D, 0.0D);
		LashRules.release(state, 0.0F, 0.0F, 0L);
		for (int tick = 1; tick < 200; tick++) {
			LashRules.step(state, hand, 0.0F, 0.0F, 0.0F, 0.0F, 7.5D, tick, LashRules.Clipper.NONE, 1.0D, profile);
			if (state.phase() == LashRules.Phase.CHARGING) return tick;
		}
		return -1;
	}
}

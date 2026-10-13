package com.vincenthuto.hemomancy.common.rite.floor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteFloorVisualRulesTest {
	@Test
	void membraneGrowsFromTheFocusToPastTheFarCorner() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.spreadRadius(0.0F, 4), 1.0e-6F);
		float settled = CardinalRiteFloorVisualRules.spreadRadius(CardinalRiteFloorVisualRules.FORM_TICKS, 4);
		assertTrue(settled > 4.5F * Math.sqrt(2.0D), "grand floor corners must be covered");
		float previous = -1.0F;
		for (int tick = 0; tick <= CardinalRiteFloorVisualRules.FORM_TICKS; tick += 5) {
			float radius = CardinalRiteFloorVisualRules.spreadRadius(tick, 2);
			assertTrue(radius >= previous, "spread must never recede while forming");
			previous = radius;
		}
		assertEquals(settled, CardinalRiteFloorVisualRules.spreadRadius(10_000.0F, 4), 1.0e-6F);
	}

	@Test
	void spreadingEdgeOnlyGlowsWhileForming() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.frontIntensity(0.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.frontIntensity(CardinalRiteFloorVisualRules.FORM_TICKS / 2.0F) > 0.5F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.frontIntensity(CardinalRiteFloorVisualRules.FORM_TICKS), 1.0e-5F);
	}

	@Test
	void focusBraidSwellsInBeforeTheMembraneSettles() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.braidGrowth(0.0F), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.braidGrowth(20.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.braidGrowth(10.0F) < 1.0F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.braidGrowth(CardinalRiteFloorVisualRules.FORM_TICKS), 1.0e-6F);
	}

	@Test
	void eachBraidStrandDivesAtItsOwnPairOfOpposedAngles() {
		for (int strand = 0; strand < 4; strand++) {
			float angle = (float) (strand * Math.PI / 4.0D);
			assertEquals(1.0F, CardinalRiteFloorVisualRules.braidDive(strand, angle), 1.0e-5F);
			assertEquals(1.0F, CardinalRiteFloorVisualRules.braidDive(strand, angle + (float) Math.PI), 1.0e-5F);
			assertEquals(1.0F, CardinalRiteFloorVisualRules.braidDive(strand, angle + (float) (Math.PI * 2.0D)), 1.0e-5F);
			// Halfway to the next strand's dive, this strand is back up in the braid.
			assertEquals(0.0F, CardinalRiteFloorVisualRules.braidDive(strand, angle + (float) (Math.PI / 2.0D)), 1.0e-6F);
			for (int other = 0; other < 4; other++) {
				if (other != strand) {
					assertEquals(0.0F, CardinalRiteFloorVisualRules.braidDive(other, angle), 1.0e-6F,
							"strands never dive at the same place");
				}
			}
		}
	}

	@Test
	void moundRisesFromTheLipToACrestAndSettlesIntoTheFloor() {
		for (int step = 0; step < 16; step++) {
			float theta = (float) (Math.PI * 2.0D * step / 16);
			float lip = CardinalRiteFloorVisualRules.moundHeight(CardinalRiteFloorVisualRules.MOUND_INNER, theta);
			float crest = CardinalRiteFloorVisualRules.moundHeight(0.58F, theta);
			assertTrue(lip > 0.0F && crest > lip, "the mound rises from its lip to the crest at " + theta);
			assertEquals(0.0F, CardinalRiteFloorVisualRules.moundHeight(CardinalRiteFloorVisualRules.MOUND_OUTER, theta),
					1.0e-6F, "the mound meets the flat floor without a step");
			assertTrue(CardinalRiteFloorVisualRules.moundHeight(CardinalRiteFloorVisualRules.MOUND_OUTER - 0.01F, theta)
					< 0.002F, "the mound eases into the floor");
		}
	}

	@Test
	void brokenFloorPlaysItsFormationBackwards() {
		int ticks = CardinalRiteFloorVisualRules.COLLAPSE_TICKS;
		assertEquals(CardinalRiteFloorVisualRules.FORM_TICKS, CardinalRiteFloorVisualRules.collapseAge(0.0F), 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.collapseAge(ticks), 1.0e-6F);
		float previous = Float.MAX_VALUE;
		for (int tick = 0; tick <= ticks; tick += 5) {
			float radius = CardinalRiteFloorVisualRules.spreadRadius(CardinalRiteFloorVisualRules.collapseAge(tick), 3);
			assertTrue(radius <= previous, "the membrane only ever draws back while collapsing");
			previous = radius;
		}
		assertFalse(CardinalRiteFloorVisualRules.collapsed(ticks - 1));
		assertTrue(CardinalRiteFloorVisualRules.collapsed(ticks));
	}

	@Test
	void seatMoundsRiseOnlyOnceTheMembraneReachesThem() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.seatRise(1.0F, 5.0F), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.seatRise(7.0F, 5.0F), 1.0e-6F);
		float partly = CardinalRiteFloorVisualRules.seatRise(5.0F, 5.0F);
		assertTrue(partly > 0.0F && partly < 1.0F);
	}

	@Test
	void heartbeatHasAStrongBeatAnEchoAndARest() {
		int period = CardinalRiteFloorVisualRules.HEARTBEAT_TICKS;
		assertEquals(1.0F, CardinalRiteFloorVisualRules.heartbeat(0.0F), 1.0e-4F);
		float echo = CardinalRiteFloorVisualRules.heartbeat(period * 0.17F);
		assertTrue(echo > 0.4F && echo < 1.0F, "echo " + echo);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.heartbeat(period * 0.6F), 1.0e-6F);
		assertEquals(CardinalRiteFloorVisualRules.heartbeat(3.0F),
				CardinalRiteFloorVisualRules.heartbeat(3.0F + period * 5), 1.0e-4F);
	}

	@Test
	void trenchIsDugAroundTheLoopByAGlowingFront() {
		int samples = 96;
		assertEquals(0.0F, CardinalRiteFloorVisualRules.digProgress(0.0F, 0, samples), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.digProgress(CardinalRiteFloorVisualRules.FORM_TICKS, samples,
				samples), 1.0e-6F);
		float tick = CardinalRiteFloorVisualRules.FORM_TICKS * 0.7F;
		assertTrue(CardinalRiteFloorVisualRules.digProgress(tick, 0, samples)
				> CardinalRiteFloorVisualRules.digProgress(tick, samples - 1, samples), "the front runs around the loop");
		assertEquals(0.0F, CardinalRiteFloorVisualRules.digGlow(0.0F), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.digGlow(0.5F), 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.digGlow(1.0F), 1.0e-6F);
	}

	@Test
	void everyFloorStyleHasItsOwnPalette() {
		assertEquals(CardinalRiteFloorVisualRules.STYLE_THRESHOLD, CardinalRiteFloorVisualRules.styleIndex("threshold"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_DOMINION, CardinalRiteFloorVisualRules.styleIndex("dominion"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_COMMUNION, CardinalRiteFloorVisualRules.styleIndex("communion"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_WORKING, CardinalRiteFloorVisualRules.styleIndex("working"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_THRESHOLD, CardinalRiteFloorVisualRules.styleIndex(null));
		var seen = new java.util.HashSet<CardinalRiteFloorVisualRules.Palette>();
		for (int style = 0; style < 4; style++) assertTrue(seen.add(CardinalRiteFloorVisualRules.palette(style)));
	}
}

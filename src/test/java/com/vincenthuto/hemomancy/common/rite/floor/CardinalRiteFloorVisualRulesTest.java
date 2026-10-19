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
	void focusPadSwellsInBeforeTheMembraneSettles() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.padGrowth(0.0F), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.padGrowth(20.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.padGrowth(10.0F) < 1.0F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.padGrowth(CardinalRiteFloorVisualRules.FORM_TICKS), 1.0e-6F);
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
		assertEquals(CardinalRiteFloorVisualRules.STYLE_APOTHEOS, CardinalRiteFloorVisualRules.styleIndex("apotheos"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_QLIPHOTH, CardinalRiteFloorVisualRules.styleIndex("qliphoth"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_ANCESTRAL, CardinalRiteFloorVisualRules.styleIndex("ancestral"));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_THRESHOLD, CardinalRiteFloorVisualRules.styleIndex(null));
		assertEquals(CardinalRiteFloorVisualRules.STYLE_THRESHOLD, CardinalRiteFloorVisualRules.styleIndex("pale"));
		var seen = new java.util.HashSet<CardinalRiteFloorVisualRules.Palette>();
		for (int style = 0; style < 7; style++) assertTrue(seen.add(CardinalRiteFloorVisualRules.palette(style)));
	}

	@Test
	void riteQuickensTheHeartFromRestThroughActiveToRacing() {
		assertEquals(CardinalRiteFloorVisualRules.TEMPO_IDLE, CardinalRiteFloorVisualRules.riteTempo(null));
		assertEquals(CardinalRiteFloorVisualRules.TEMPO_IDLE, CardinalRiteFloorVisualRules.riteTempo("COMPLETE"));
		assertEquals(CardinalRiteFloorVisualRules.TEMPO_ACTIVE, CardinalRiteFloorVisualRules.riteTempo("CONSECRATION"));
		assertEquals(CardinalRiteFloorVisualRules.TEMPO_INTENSE, CardinalRiteFloorVisualRules.riteTempo("INSCRIPTION"));
		assertEquals(CardinalRiteFloorVisualRules.TEMPO_INTENSE, CardinalRiteFloorVisualRules.riteTempo("ORDEAL"));
		assertEquals(1.5F, CardinalRiteFloorVisualRules.tempoRate(CardinalRiteFloorVisualRules.TEMPO_ACTIVE), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.tempoVigor(CardinalRiteFloorVisualRules.TEMPO_INTENSE)
				> CardinalRiteFloorVisualRules.tempoVigor(CardinalRiteFloorVisualRules.TEMPO_ACTIVE));
		assertEquals(0.0F, CardinalRiteFloorVisualRules.tempoVigor(CardinalRiteFloorVisualRules.TEMPO_IDLE), 1.0e-6F);
		// At rest the rite heartbeat is the floor's own, even at large game times.
		for (double time : new double[] {0.0D, 5.25D, 1_234_567.0D, 1_234_590.5D}) {
			assertEquals(CardinalRiteFloorVisualRules.heartbeat((float) (time % 72.0D)),
					CardinalRiteFloorVisualRules.riteHeartbeat(time, 0.0F, 0.0F), 1.0e-4F);
		}
		assertEquals(2, strongBeats(0.0F, 0.0F));
		assertEquals(3, strongBeats(1.0F, 0.0F));
		assertEquals(4, strongBeats(1.0F, 1.0F));
	}

	/** Strong beats over the two-resting-beat span in which every tempo lines up. */
	private static int strongBeats(float quick, float intense) {
		int beats = 0;
		boolean high = CardinalRiteFloorVisualRules.riteHeartbeat(-0.05D, quick, intense) > 0.95F;
		for (int step = 0; step < 72 * 20; step++) {
			boolean now = CardinalRiteFloorVisualRules.riteHeartbeat(step / 20.0D, quick, intense) > 0.95F;
			if (now && !high) beats++;
			high = now;
		}
		return beats;
	}

	@Test
	void staffImpactSendsOneHardShockPastTheFloorEdge() {
		float reach = CardinalRiteFloorVisualRules.reach(3);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.shockStrength(-1.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.shockStrength(1.5F) > 0.8F, "the shock hits hard at once");
		assertEquals(0.0F, CardinalRiteFloorVisualRules.shockStrength(CardinalRiteFloorVisualRules.SHOCK_TICKS), 1.0e-6F);
		float previous = -1.0F;
		for (int tick = 0; tick <= CardinalRiteFloorVisualRules.SHOCK_TICKS; tick++) {
			float radius = CardinalRiteFloorVisualRules.shockRadius(tick, reach);
			assertTrue(radius >= previous, "the shock only runs outward");
			previous = radius;
		}
		assertTrue(previous > reach, "the shock outruns the floor's edge");
	}

	@Test
	void filledAnchorPulseRunsOutToTheAnchorAndFades() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.surgeRadius(0.0F, 5.0F), 1.0e-6F);
		assertEquals(5.0F, CardinalRiteFloorVisualRules.surgeRadius(CardinalRiteFloorVisualRules.SURGE_TICKS, 5.0F),
				1.0e-5F);
		assertTrue(CardinalRiteFloorVisualRules.surgeStrength(6.0F) > 0.9F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.surgeStrength(CardinalRiteFloorVisualRules.SURGE_TICKS + 6.0F),
				1.0e-6F);
	}

	@Test
	void drawnOfferingLightsItsTrenchThenRunsInToTheFocus() {
		float ticks = CardinalRiteFloorVisualRules.OFFERING_TICKS;
		assertTrue(CardinalRiteFloorVisualRules.offeringTrenchGlow(3.0F, 0.0F) > 0.8F, "the stretch by the brazier lights");
		assertTrue(CardinalRiteFloorVisualRules.offeringTrenchGlow(3.0F, 0.0F)
				> CardinalRiteFloorVisualRules.offeringTrenchGlow(3.0F, 2.5F));
		assertTrue(CardinalRiteFloorVisualRules.offeringTrenchGlow(10.0F, 2.5F)
				> CardinalRiteFloorVisualRules.offeringTrenchGlow(2.0F, 2.5F), "the glow runs along the trench");
		assertEquals(0.0F, CardinalRiteFloorVisualRules.offeringTrenchGlow(ticks, 0.0F), 1.0e-6F);
		assertEquals(6.0F, CardinalRiteFloorVisualRules.offeringDrawRadius(5.0F, 6.0F), 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.offeringDrawRadius(ticks - 3.0F, 6.0F), 1.0e-6F);
		float previous = Float.MAX_VALUE;
		for (int tick = 0; tick <= ticks; tick++) {
			float radius = CardinalRiteFloorVisualRules.offeringDrawRadius(tick, 6.0F);
			assertTrue(radius <= previous, "the offering only runs inward");
			previous = radius;
		}
		assertEquals(0.0F, CardinalRiteFloorVisualRules.offeringDrawStrength(0.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.offeringDrawStrength(12.0F) > 0.9F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.offeringFocusFlare(ticks - 3.0F), 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.offeringFocusFlare(5.0F), 1.0e-6F);
	}

	@Test
	void waveArrivalFlinchesTheMembraneAndClearingItFlushes() {
		float ticks = CardinalRiteFloorVisualRules.FLINCH_TICKS;
		assertTrue(CardinalRiteFloorVisualRules.flinchBlanch(2.0F) > 0.5F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.flinchBlanch(ticks), 1.0e-6F);
		assertEquals(6.0F, CardinalRiteFloorVisualRules.flinchRippleRadius(0.0F, 6.0F), 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.flinchRippleRadius(ticks, 6.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.flinchRippleStrength(ticks / 2.0F) > 0.9F);
		assertTrue(CardinalRiteFloorVisualRules.reliefFlush(5.0F) > 0.5F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.reliefFlush(CardinalRiteFloorVisualRules.RELIEF_TICKS), 1.0e-6F);
	}

	@Test
	void culminationSwellsAndCompletionFlushesThenSettles() {
		float previous = -1.0F;
		for (int tick = 0; tick <= CardinalRiteFloorVisualRules.CULMINATION_SWELL_TICKS; tick += 5) {
			float swell = CardinalRiteFloorVisualRules.culminationSwell(tick);
			assertTrue(swell >= previous, "culmination only swells");
			previous = swell;
		}
		assertEquals(1.0F, previous, 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.completionSwell(0.0F), 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.completionSwell(CardinalRiteFloorVisualRules.COMPLETION_SETTLE_TICKS),
				1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.completionFlush(5.0F) > 0.9F);
		assertTrue(CardinalRiteFloorVisualRules.completionFlush(60.0F) < CardinalRiteFloorVisualRules.completionFlush(5.0F));
		assertEquals(0.0F, CardinalRiteFloorVisualRules.completionFlush(CardinalRiteFloorVisualRules.COMPLETION_SETTLE_TICKS),
				1.0e-6F);
	}

	@Test
	void groundingRingRunsOutPastTheFloorIntoTheGround() {
		float reach = CardinalRiteFloorVisualRules.reach(4);
		float ticks = CardinalRiteFloorVisualRules.GROUND_RING_TICKS;
		assertEquals(0.0F, CardinalRiteFloorVisualRules.groundRingRadius(0.0F, reach), 1.0e-6F);
		assertEquals(reach + CardinalRiteFloorVisualRules.GROUND_RING_BEYOND,
				CardinalRiteFloorVisualRules.groundRingRadius(ticks, reach), 1.0e-5F);
		assertTrue(CardinalRiteFloorVisualRules.groundRingStrength(ticks * 0.5F) > 0.5F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.groundRingStrength(ticks), 1.0e-6F);
	}

	@Test
	void failedRiteTearsTheMembraneBackFastThenRegrowsIt() {
		float torn = CardinalRiteFloorVisualRules.TEAR_TICKS;
		float end = torn + CardinalRiteFloorVisualRules.TEAR_HOLD_TICKS + CardinalRiteFloorVisualRules.TEAR_REGROW_TICKS;
		float form = CardinalRiteFloorVisualRules.FORM_TICKS;
		assertEquals(form, CardinalRiteFloorVisualRules.tearAge(0.0F), 1.0e-5F);
		assertEquals(CardinalRiteFloorVisualRules.TEAR_EXTENT,
				CardinalRiteFloorVisualRules.formProgress(CardinalRiteFloorVisualRules.tearAge(torn)), 1.0e-3F);
		// About three times as fast as a broken floor's collapse.
		float tearRate = (form - CardinalRiteFloorVisualRules.tearAge(torn)) / torn;
		float collapseRate = form / (float) CardinalRiteFloorVisualRules.COLLAPSE_TICKS;
		assertEquals(3.0F, tearRate / collapseRate, 0.3F);
		for (float tick = 0.0F; tick <= end; tick += 1.0F) {
			assertTrue(CardinalRiteFloorVisualRules.formProgress(CardinalRiteFloorVisualRules.tearAge(tick))
					>= CardinalRiteFloorVisualRules.TEAR_EXTENT - 1.0e-3F, "the tear stops at its extent");
		}
		assertTrue(CardinalRiteFloorVisualRules.tearBruise(torn) > 0.5F, "the tear bruises");
		assertFalse(CardinalRiteFloorVisualRules.tearing(end));
		assertEquals(form, CardinalRiteFloorVisualRules.tearAge(end), 1.0e-5F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.tearBruise(end), 1.0e-6F);
		assertFalse(CardinalRiteFloorVisualRules.tearing(-1.0F));
	}

	@Test
	void cancellationDimsGentlyAndRecovers() {
		float deepest = 0.0F;
		for (int tick = 0; tick <= CardinalRiteFloorVisualRules.CANCEL_TICKS; tick++) {
			deepest = Math.max(deepest, CardinalRiteFloorVisualRules.cancelDim(tick));
		}
		assertTrue(deepest > 0.2F && deepest <= 0.35F + 1.0e-6F, "dim " + deepest);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.cancelDim(CardinalRiteFloorVisualRules.CANCEL_TICKS), 1.0e-6F);
	}

	@Test
	void instabilityAndBrokenAnchorsBruiseTheMembrane() {
		assertEquals(0.0F, CardinalRiteFloorVisualRules.instabilityBruise(0), 1.0e-6F);
		float previous = -1.0F;
		for (int instability = 0; instability <= 100; instability += 5) {
			float bruise = CardinalRiteFloorVisualRules.instabilityBruise(instability);
			assertTrue(bruise >= previous, "bruising follows instability");
			previous = bruise;
		}
		assertTrue(previous > 0.8F && previous <= 0.85F + 1.0e-6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.anchorWound(1.0F), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.anchorWound(0.0F), 1.0e-6F);
		assertEquals(1.0F, CardinalRiteFloorVisualRules.brokenWound(0.0F), 1.0e-6F);
		assertTrue(CardinalRiteFloorVisualRules.brokenWound(60.0F) < 0.6F);
		assertEquals(0.0F, CardinalRiteFloorVisualRules.brokenWound(160.0F), 1.0e-6F);
	}
}

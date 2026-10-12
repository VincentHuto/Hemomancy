package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SomaticLoomEffectRulesTest {
	@Test
	void warpStaysBareWithoutARecipe() {
		int[] threads = SomaticLoomEffectRules.threadTendencies(new int[8], 15);
		assertTrue(Arrays.stream(threads).allMatch(t -> t == -1));
	}

	@Test
	void singleTendencyTakesTheWholeWarp() {
		int[] req = new int[8];
		req[2] = 4;
		int[] threads = SomaticLoomEffectRules.threadTendencies(req, 15);
		assertTrue(Arrays.stream(threads).allMatch(t -> t == 2));
	}

	@Test
	void sharesAreProportionalContiguousAndInTendencyOrder() {
		int[] req = new int[8];
		req[1] = 4;
		req[5] = 12;
		int[] threads = SomaticLoomEffectRules.threadTendencies(req, 15);
		assertEquals(4, count(threads, 1));
		assertEquals(11, count(threads, 5));
		assertArrayEquals(new int[]{1, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5}, threads);
	}

	@Test
	void everyRequiredTendencyGetsAThreadWhileThreadsLast() {
		int[] req = new int[8];
		req[0] = 1;
		req[7] = 60;
		int[] threads = SomaticLoomEffectRules.threadTendencies(req, 15);
		assertEquals(1, count(threads, 0));
		assertEquals(14, count(threads, 7));

		int[] many = {1, 1, 1, 1, 1, 0, 0, 0};
		assertArrayEquals(new int[]{0, 1, 2}, SomaticLoomEffectRules.threadTendencies(many, 3));
	}

	@Test
	void fillIsClampedAndSafe() {
		assertEquals(.5f, SomaticLoomEffectRules.fill(5, 10), 1e-6);
		assertEquals(1f, SomaticLoomEffectRules.fill(20, 10), 1e-6);
		assertEquals(0f, SomaticLoomEffectRules.fill(1, 0), 1e-6);
		assertEquals(0f, SomaticLoomEffectRules.fill(Double.NaN, 10), 1e-6);
		assertEquals(0f, SomaticLoomEffectRules.fill(-3, 10), 1e-6);
	}

	@Test
	void vialsShowStockAndPirnsShowWhatIsSpunForTheWeave() {
		assertEquals(0f, SomaticLoomEffectRules.vialFill(0f, 64), 1e-6);
		assertEquals(0.5f, SomaticLoomEffectRules.vialFill(16f, 64), 1e-6);
		assertEquals(1f, SomaticLoomEffectRules.vialFill(64f, 64), 1e-6);
		assertTrue(SomaticLoomEffectRules.vialFill(1f, 64) >= .125f, "a single enzyme still shows in its vial");
		assertEquals(0f, SomaticLoomEffectRules.spunFill(0f), 1e-6);
		assertEquals(0.5f, SomaticLoomEffectRules.spunFill(4f), 1e-6);
		assertEquals(1f, SomaticLoomEffectRules.spunFill(12f), 1e-6);
	}

	@Test
	void heartbeatStaysInRangeAndQuickensWithCharge() {
		assertTrue(beats(0f) < beats(1f));
		for (float t = 0; t < 200; t += .37f) {
			float beat = SomaticLoomEffectRules.heartbeat(t, .5f);
			assertTrue(beat >= 0 && beat <= 1, "beat " + beat);
		}
	}

	@Test
	void pulseClimbsOncePerPeriod() {
		assertEquals(0f, SomaticLoomEffectRules.pulseFront(0, 40), 1e-6);
		assertEquals(.5f, SomaticLoomEffectRules.pulseFront(20, 40), 1e-6);
		assertEquals(.25f, SomaticLoomEffectRules.pulseFront(90, 40), 1e-6);
	}

	@Test
	void wovenBandsNarrowToFitTheCloth() {
		assertEquals(.7f, SomaticLoomEffectRules.bandStep(3, 16), 1e-6);
		assertEquals(.16f, SomaticLoomEffectRules.bandStep(100, 16), 1e-6);
	}

	@Test
	void catalystsSpreadAcrossTheCloth() {
		assertEquals(8f, SomaticLoomEffectRules.catalystX(0, 1, 3, 13), 1e-6);
		assertEquals(3f, SomaticLoomEffectRules.catalystX(0, 2, 3, 13), 1e-6);
		assertEquals(13f, SomaticLoomEffectRules.catalystX(1, 2, 3, 13), 1e-6);
		assertEquals(8f, SomaticLoomEffectRules.catalystX(1, 3, 3, 13), 1e-6);
	}

	@Test
	void threadsLightAsTheirStrandsAreWovenAndGutterWhenShort() {
		float faint = SomaticLoomEffectRules.threadGlow(0f, false, 10f, 3);
		float woven = SomaticLoomEffectRules.threadGlow(1f, false, 10f, 3);
		assertTrue(woven > faint * 2, faint + " -> " + woven);
		for (float t = 0; t < 60; t += .5f)
			assertTrue(SomaticLoomEffectRules.threadGlow(0f, true, t, 3) < faint);
	}

	@Test
	void tethersSagWithLengthWithinLimits() {
		assertEquals(.15f, SomaticLoomEffectRules.tetherSag(.5f), 1e-6);
		assertEquals(.6f, SomaticLoomEffectRules.tetherSag(5f), 1e-6);
		assertEquals(.9f, SomaticLoomEffectRules.tetherSag(40f), 1e-6);
	}

	@Test
	void shimmerRunsFromStrandToLoomAndPeaksOnItsBead() {
		float bead = SomaticLoomEffectRules.shimmerPosition(10f, .3f);
		assertTrue(bead >= 0 && bead < 1);
		assertTrue(SomaticLoomEffectRules.shimmerPosition(11f, .3f) > bead, "the bead travels loomward");
		assertEquals(1f, SomaticLoomEffectRules.shimmer(bead, bead), 1e-6);
		assertEquals(0f, SomaticLoomEffectRules.shimmer(bead, bead + .2f), 1e-6);
	}

	private static float beats(float charge) {
		int peaks = 0;
		boolean high = false;
		for (float t = 0; t < 400; t += .25f) {
			boolean now = SomaticLoomEffectRules.heartbeat(t, charge) > .5f;
			if (now && !high) peaks++;
			high = now;
		}
		return peaks;
	}

	private static int count(int[] threads, int tendency) {
		return (int) Arrays.stream(threads).filter(t -> t == tendency).count();
	}

	@Test
	void theEchoGrowsWithTheLoomUntilItStandsLifeSize() {
		var base = SomaticLoomEffectRules.echoLook(0);
		var ganglionic = SomaticLoomEffectRules.echoLook(1);
		var cortical = SomaticLoomEffectRules.echoLook(2);
		assertTrue(base.weavingScale() < ganglionic.weavingScale() && ganglionic.weavingScale() < cortical.weavingScale());
		assertEquals(1.0f, cortical.weavingScale(), 1e-6f, "a Cortical echo stands at full stature");
		assertEquals(cortical.weavingScale(), cortical.climaxScale(), 1e-6f, "a whole figure brightens at the climax instead of growing");
		assertTrue(base.climaxScale() > base.weavingScale() && ganglionic.climaxScale() > ganglionic.weavingScale());
		assertEquals(cortical, SomaticLoomEffectRules.echoLook(7), "stages past Cortical clamp");
	}

	@Test
	void aBaseEchoIsGreyAndStuttersWhileUpgradedEchoesHoldTheirColour() {
		var base = SomaticLoomEffectRules.echoLook(0);
		var ganglionic = SomaticLoomEffectRules.echoLook(1);
		var cortical = SomaticLoomEffectRules.echoLook(2);
		assertTrue(base.washMax() <= 0.12f, "a base echo is all but monochrome");
		assertTrue(ganglionic.washMin() >= 0.3f && cortical.washMin() > ganglionic.washMin());
		float lowest = 1f;
		for (int t = 0; t < 400; t++) lowest = Math.min(lowest, SomaticLoomEffectRules.echoStutter(t, base.stutter()));
		assertTrue(lowest < 0.6f, "a memory barely held drops out now and then");
		for (int t = 0; t < 400; t++) {
			assertEquals(1f, SomaticLoomEffectRules.echoStutter(t, cortical.stutter()), 1e-6f);
			assertTrue(SomaticLoomEffectRules.echoStutter(t, ganglionic.stutter()) >= 0.75f, "a Ganglionic echo holds steady");
		}
	}

	@Test
	void theDomeHoldsTheWholeRitualSoNoThreadHidesBeyondIt() {
		assertTrue(SomaticLoomEffectRules.DOME_RADIUS
				> com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules.RITUAL_RADIUS,
				"threads roam the whole ritual radius and must never fall behind the dome's far wall");
	}

	@Test
	void onlyACorticalWeaveRaisesTheChamberDome() {
		assertEquals(0f, SomaticLoomEffectRules.domeRadius(1, true, 200, false, 0), 1e-6f);
		assertEquals(0f, SomaticLoomEffectRules.domeRadius(2, false, 200, false, 0), 1e-6f);
		assertEquals(SomaticLoomEffectRules.DOME_RADIUS, SomaticLoomEffectRules.domeRadius(2, true, 200, false, 0), 1e-6f);
	}

	@Test
	void theDomeRisesHoldsAndSinksWithTheEcho() {
		float early = SomaticLoomEffectRules.domeRadius(2, true, 10, false, 0);
		float later = SomaticLoomEffectRules.domeRadius(2, true, 30, false, 0);
		assertTrue(early > 0 && early < later && later < SomaticLoomEffectRules.DOME_RADIUS, "it rises over the first ticks");
		assertEquals(SomaticLoomEffectRules.DOME_RADIUS, SomaticLoomEffectRules.domeRadius(2, true, 0, true, 0.5f), 1e-6f,
				"the climax opens under the full dome");
		assertTrue(SomaticLoomEffectRules.domeRadius(2, true, 0, true, 0.85f) < SomaticLoomEffectRules.DOME_RADIUS);
		assertEquals(0f, SomaticLoomEffectRules.domeRadius(2, true, 0, true, 1f), 1e-6f, "it closes as the echo sinks");
	}

	@Test
	void theSeamTearsOpenAroundTheDomeAndKnitsShutAsTheEchoSinks() {
		assertEquals(0f, SomaticLoomEffectRules.seamOpen(0, false, 0), 1e-6f);
		float partway = SomaticLoomEffectRules.seamOpen(15, false, 0);
		assertTrue(partway > 0f && partway < 1f, "the tear runs around the circle as the dome rises");
		assertEquals(1f, SomaticLoomEffectRules.seamOpen(200, false, 0), 1e-6f);
		assertEquals(1f, SomaticLoomEffectRules.seamOpen(0, true, 0.6f), 1e-6f, "the climax opens under a whole seam");
		assertEquals(0f, SomaticLoomEffectRules.seamOpen(0, true, 1f), 1e-6f, "it has knitted shut when the echo is gone");
	}

	@Test
	void veinsCreepOutAfterTheTearAndWithdrawBeforeItCloses() {
		assertEquals(0f, SomaticLoomEffectRules.veinGrowth(5, false, 0), 1e-6f, "veins wait for the ground to tear");
		assertTrue(SomaticLoomEffectRules.veinGrowth(40, false, 0) > 0f);
		assertEquals(1f, SomaticLoomEffectRules.veinGrowth(400, false, 0), 1e-6f);
		assertTrue(SomaticLoomEffectRules.veinGrowth(0, true, 0.8f) < 1f, "they withdraw as the echo sinks");
		assertEquals(0f, SomaticLoomEffectRules.veinGrowth(0, true, 0.95f), 1e-6f, "they are gone before the seam knits shut");
	}

	@Test
	void sinewsHoldTheSkyDownThenSnapAtTheClimax() {
		assertEquals(0f, SomaticLoomEffectRules.sinewTension(10, false, 0), 1e-6f, "no sinew before the seam is open");
		assertEquals(1f, SomaticLoomEffectRules.sinewTension(400, false, 0), 1e-6f);
		assertEquals(1f, SomaticLoomEffectRules.sinewTension(0, true, 0.6f), 1e-6f);
		assertEquals(0f, SomaticLoomEffectRules.sinewTension(0, true, 0.8f), 1e-6f, "they snap before the dome closes");
	}
}

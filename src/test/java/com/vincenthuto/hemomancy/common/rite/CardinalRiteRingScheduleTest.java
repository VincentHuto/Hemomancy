package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteRingScheduleTest {
	private static final ResourceLocation PLAIN = ResourceLocation.parse("hemomancy:cardinal_rite/ring_test");
	private static final ResourceLocation STATION = ResourceLocation.parse("hemomancy:cardinal_rite/first_condensation");

	private static ActiveCardinalRite rite(ResourceLocation id, int rings, int waves, boolean stillIntervals) {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, id, 1200, 5, 6,
				false, waves, rings * 4);
		int[] ranks = new int[rings * 4];
		for (int i = 0; i < ranks.length; i++) ranks[i] = i / 4;
		rite.configureRings(ranks, stillIntervals);
		return rite;
	}

	private static void feedOpen(ActiveCardinalRite rite) {
		for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, rite.bloodNeededForAnchor(i));
	}

	private static void consecrateAndSeal(ActiveCardinalRite rite, boolean stillIntervals) {
		feedOpen(rite);
		assertTrue(rite.enterInscription());
		assertTrue(rite.sealAltar(stillIntervals));
	}

	@Test
	void theSecondRingWakesOnTheFirstWaveAndHoldsIt() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 2, false);
		feedOpen(rite);
		rite.enterInscription();
		assertEquals(1, rite.getOpenRings(), "a rite with openers keeps its later rings asleep at Inscription");
		rite.sealAltar(false);

		assertEquals(CardinalRitePhase.ORDEAL, rite.getPhase());
		assertEquals(2, rite.getOpenRings());
		assertFalse(rite.areAnchorsConsecrated(), "the ordeal's wave cannot clear while the new ring hungers");
		assertEquals(4, rite.hungryLaterRingAnchors());
		assertTrue(rite.fillAnchor(4, 63));
		assertEquals(63, rite.getAnchorBloodMl()[4], "ring 1 asks 63 mL");
		feedOpen(rite);
		assertTrue(rite.areAnchorsConsecrated());
		assertEquals(1, rite.nextRingToAnnounce());
		assertEquals(-1, rite.nextRingToAnnounce(), "each ring is announced once");
	}

	@Test
	void ringsSpreadAcrossWavesAndTheIntervalsBetweenThem() {
		ActiveCardinalRite rite = rite(PLAIN, 3, 2, true);
		consecrateAndSeal(rite, true);
		assertEquals(2, rite.getOpenRings(), "wave one wakes ring 1");

		rite.completeWave();
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());
		assertEquals(3, rite.getOpenRings(), "the interval wakes ring 2");
		rite.finishStillInterval();
		assertEquals(3, rite.getOpenRings(), "nothing left to wake on wave two");
	}

	@Test
	void ringsBeyondTheLastOpenerAllWakeThere() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 1, false);
		consecrateAndSeal(rite, false);

		assertEquals(4, rite.getOpenRings());
		assertEquals(1, rite.nextRingToAnnounce());
		assertEquals(2, rite.nextRingToAnnounce());
		assertEquals(3, rite.nextRingToAnnounce());
		assertEquals(-1, rite.nextRingToAnnounce());
	}

	@Test
	void aRiteWithNoOpenersWakesItsLaterRingsAtInscription() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 0, false);
		feedOpen(rite);
		assertTrue(rite.enterInscription());

		assertEquals(2, rite.getOpenRings());
		assertFalse(rite.areAnchorsConsecrated(), "the seal waits for the woken ring");
		feedOpen(rite);
		assertTrue(rite.areAnchorsConsecrated(), "anchors are fed during Inscription");
	}

	@Test
	void theProcessionCannotEndWhileTheRingHungers() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 0, false);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(new BlockPos(2, 0, 0), null, true)));
		consecrateAndSeal(rite, false);
		assertEquals(CardinalRitePhase.OFFERING_PROCESSION, rite.getPhase());
		assertEquals(2, rite.getOpenRings());

		rite.markOfferingDrawn();
		assertFalse(rite.finishOfferingProcession());
		feedOpen(rite);
		assertTrue(rite.finishOfferingProcession());
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void theLastCircuitWaitsForTheWokenRing() {
		ActiveCardinalRite rite = rite(STATION, 2, 0, false);
		consecrateAndSeal(rite, false);
		assertEquals(CardinalRitePhase.STATION_PROJECTION, rite.getPhase());
		assertEquals(2, rite.getOpenRings());

		int circuits = StationUpgradeRites.circuits(STATION);
		for (int circuit = 0; circuit < circuits; circuit++) {
			assertTrue(rite.fillUpgradeCircuit(circuit, rite.upgradeBloodNeeded()));
		}
		assertEquals(CardinalRitePhase.STATION_PROJECTION, rite.getPhase(), "held for the ring");
		assertEquals(0, rite.upgradeBloodNeeded(), "no circuit is left to feed");
		assertFalse(rite.tryFinishStationProjection());
		feedOpen(rite);
		assertTrue(rite.tryFinishStationProjection());
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void anUnconfiguredStationRiteStillHandsOffAtItsLastCircuit() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, STATION, 1200, 5,
				6, false, 0, 8);
		feedOpen(rite);
		rite.enterInscription();
		rite.sealAltar(false);
		int circuits = StationUpgradeRites.circuits(STATION);
		for (int circuit = 0; circuit < circuits; circuit++) rite.fillUpgradeCircuit(circuit, rite.upgradeBloodNeeded());

		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void ringStateAndHoldSurviveASaveMidHold() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 2, false);
		consecrateAndSeal(rite, false);
		assertEquals(1, rite.nextRingToAnnounce());

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertEquals(2, copy.getOpenRings());
		assertFalse(copy.areAnchorsConsecrated());
		assertEquals(-1, copy.nextRingToAnnounce(), "the woken ring is not announced again after loading");
	}
	@Test
	void aFinaleHoldWithNoStillIntervalPressesOnlyUpToTheCap() {
		ActiveCardinalRite rite = rite(PLAIN, 3, 0, false);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(new BlockPos(2, 0, 0), null, true)));
		consecrateAndSeal(rite, false);
		assertEquals(3, rite.getOpenRings(), "the procession wakes every ring left");
		assertTrue(rite.holdBeforeFinale(40));

		for (int second = 0; second < 30; second++) rite.applyStillIntervalPressure(0);

		assertTrue(rite.getInstability() <= 10, "a hold with no still interval must not collapse the rite: "
				+ rite.getInstability());
		assertNotEquals(CardinalRitePhase.COLLAPSED, rite.getPhase());
	}

	@Test
	void aRealStillIntervalStillPunishesDryStations() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 0, true);
		consecrateAndSeal(rite, true);
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());

		for (int second = 0; second < 3; second++) rite.applyStillIntervalPressure(120);

		assertTrue(rite.getInstability() > 10, "past its duration a still interval keeps pressing");
	}
}

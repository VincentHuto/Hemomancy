package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteEscalationTest {
	private static final ResourceLocation PLAIN = ResourceLocation.parse("hemomancy:cardinal_rite/escalation_test");
	private static final ResourceLocation STATION = ResourceLocation.parse("hemomancy:cardinal_rite/first_condensation");

	private static ActiveCardinalRite rite(ResourceLocation id, int anchors, int waves) {
		return ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, id, 1200, 5, 4, false, waves, anchors);
	}

	private static void fill(ActiveCardinalRite rite, int from, int to) {
		for (int i = from; i < to; i++) rite.fillAnchor(i, 50);
	}

	@Test
	void totalsCountRingsCircuitsOpeningProcessionAndCulmination() {
		assertEquals(4, new CardinalRiteEscalation.Plan(1, 0, true, false).total());
		assertEquals(3, new CardinalRiteEscalation.Plan(1, 0, false, false).total());
		assertEquals(5, new CardinalRiteEscalation.Plan(2, 0, true, false).total());
		assertEquals(6, new CardinalRiteEscalation.Plan(3, 0, true, false).total());
		assertEquals(11, new CardinalRiteEscalation.Plan(8, 0, true, false).total());
		assertEquals(2, new CardinalRiteEscalation.Plan(0, 0, false, false).total());
		assertEquals(6, new CardinalRiteEscalation.Plan(1, 2, true, false).total());
		assertEquals(5, new CardinalRiteEscalation.Plan(3, 0, true, true).total(), "succession always has five steps");
	}

	@Test
	void eachRingCountsOnceAndTheStageNeverFalls() {
		ActiveCardinalRite rite = rite(PLAIN, 8, 1);
		fill(rite, 0, 4);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(1, rite.getEscalationStage());
		assertFalse(CardinalRiteEscalation.update(rite), "a counted ring does not step again");

		rite.drainAnchor(0, 30);
		assertFalse(CardinalRiteEscalation.update(rite));
		assertEquals(1, rite.getEscalationStage(), "a drained anchor never lowers the stage");

		rite.fillAnchor(0, 30);
		assertFalse(CardinalRiteEscalation.update(rite), "refilling a counted ring is not a new step");
		fill(rite, 4, 8);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage());
	}

	@Test
	void sealProcessionAndCulminationEachCountOnceAndLandingTogetherStepsOnce() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 0);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(new BlockPos(2, 0, 0), null, true)));
		fill(rite, 0, 4);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertTrue(rite.enterInscription());
		assertFalse(CardinalRiteEscalation.update(rite), "inscription alone is not a milestone");

		assertTrue(rite.sealAltar(false));
		assertEquals(CardinalRitePhase.OFFERING_PROCESSION, rite.getPhase());
		assertTrue(CardinalRiteEscalation.update(rite), "seal and procession arrive in one update");
		assertEquals(3, rite.getEscalationStage());
		assertFalse(CardinalRiteEscalation.update(rite));

		assertTrue(rite.absorbCurrentOffering());
		assertTrue(rite.finishOfferingProcession());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(4, rite.getEscalationStage());
		assertEquals(4, rite.getEscalationTotal());
	}

	@Test
	void riteWithoutOfferingsHasNoProcessionStep() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 0);
		fill(rite, 0, 4);
		rite.enterInscription();
		rite.sealAltar(false);
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(3, rite.getEscalationStage());
		assertEquals(3, rite.getEscalationTotal());
	}

	@Test
	void stationCircuitsAreSteps() {
		ActiveCardinalRite rite = rite(STATION, 4, 0);
		fill(rite, 0, 4);
		rite.enterInscription();
		assertTrue(rite.sealAltar(true));
		assertEquals(CardinalRitePhase.STATION_PROJECTION, rite.getPhase());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage(), "ring and seal");

		assertTrue(rite.fillUpgradeCircuit(0, rite.upgradeBloodNeeded()));
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(3, rite.getEscalationStage());
		assertEquals(5, rite.getEscalationTotal(), "one ring, two circuits, seal and culmination");
	}

	@Test
	void puppeteerTrialStepsOnManifestAndDefeat() {
		UUID entity = UUID.randomUUID();
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/puppeteer_trial_marrow_spitter"), 5);
		assertFalse(CardinalRiteEscalation.update(rite), "nothing before the puppet manifests");
		rite.beginPuppeteerTrial("marrow_spitter", entity, UUID.randomUUID());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertTrue(rite.markPuppeteerTrialDefeated(entity));
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage());
		assertEquals(2, rite.getEscalationTotal());
	}

	@Test
	void successionStepsWithItsCues() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 0);
		CompoundTag context = new CompoundTag();
		context.putInt("Ticks", 0);
		rite.beginSuccession(context);
		assertFalse(CardinalRiteEscalation.update(rite), "culmination does not count for succession");
		rite.succession().tag().putInt("Ticks", 450);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage());
		rite.succession().tag().putInt("Ticks", 5000);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(5, rite.getEscalationStage());
		assertEquals(5, rite.getEscalationTotal());
	}

	@Test
	void legacyRitesNeverEscalate() {
		ActiveCardinalRite rite = new ActiveCardinalRite(UUID.randomUUID(), BlockPos.ZERO, PLAIN, 100, 5);
		assertFalse(CardinalRiteEscalation.update(rite));
		assertEquals(0, rite.getEscalationStage());
	}

	@Test
	void marksAndStageSurviveSaving() {
		ActiveCardinalRite rite = rite(PLAIN, 8, 1);
		fill(rite, 0, 4);
		CardinalRiteEscalation.update(rite);
		rite.drainAnchor(1, 50);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertEquals(1, copy.getEscalationStage());
		assertEquals(rite.getEscalationMarks(), copy.getEscalationMarks());
		assertFalse(CardinalRiteEscalation.update(copy), "the drained ring stays counted after loading");
	}

	@Test
	void olderSavesRebuildTheStageFromTheirState() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 1);
		fill(rite, 0, 4);
		rite.enterInscription();
		rite.sealAltar();
		CompoundTag saved = rite.serialize();
		saved.remove("EscalationRings");
		saved.remove("EscalationCircuits");
		saved.remove("EscalationFlags");
		saved.remove("EscalationStage");

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(saved);

		assertEquals(2, copy.getEscalationStage(), "full ring plus the seal");
		assertFalse(CardinalRiteEscalation.update(copy), "rebuilding announces nothing");
	}
}

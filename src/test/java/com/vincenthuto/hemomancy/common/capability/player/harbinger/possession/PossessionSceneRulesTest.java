package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Scene;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Sequence;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Step;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PossessionSceneRulesTest {
	@Test
	void eachSequencePairsARiteHalfWithAGardensHalf() {
		assertEquals(Scene.DEPARTURE, Sequence.BEGIN.first());
		assertEquals(Scene.ARRIVAL, Sequence.BEGIN.second());
		assertEquals(Scene.BROKEN_GARDENS, Sequence.RETURN_BROKEN.first());
		assertEquals(Scene.BROKEN_RITE, Sequence.RETURN_BROKEN.second());
		assertEquals(Scene.CLEAN_GARDENS, Sequence.RETURN_CLEAN.first());
		assertEquals(Scene.CLEAN_RITE, Sequence.RETURN_CLEAN.second());
		for (Sequence sequence : Sequence.values()) {
			assertTrue(sequence.first().atRite() != sequence.second().atRite(), sequence.name());
		}
		assertEquals(Sequence.RETURN_BROKEN, Sequence.returning(true));
		assertEquals(Sequence.RETURN_CLEAN, Sequence.returning(false));
	}

	@Test
	void theWillCrossesAtTheEndOfTheFirstSceneAndSettlesInsideTheSecond() {
		assertEquals(110, Sequence.BEGIN.cut());
		assertEquals(110 + PossessionSceneRules.ARRIVAL_CONTROL, Sequence.BEGIN.settle());
		assertEquals(40, Sequence.RETURN_BROKEN.cut());
		assertEquals(40 + PossessionSceneRules.BROKEN_REMANIFEST_END, Sequence.RETURN_BROKEN.settle());
		assertEquals(50, Sequence.RETURN_CLEAN.cut());
		assertEquals(50 + PossessionSceneRules.CLEAN_REMANIFEST_END, Sequence.RETURN_CLEAN.settle());
		for (Sequence sequence : Sequence.values()) {
			assertTrue(sequence.settle() > sequence.cut(), sequence.name());
			assertTrue(sequence.settle() <= sequence.cut() + sequence.second().length(), sequence.name());
		}
	}

	@Test
	void theDepartureBeatsLandInOrderBeforeTheBlack() {
		assertTrue(PossessionSceneRules.DEPARTURE_FADE_END <= PossessionSceneRules.DEPARTURE_DAEMON_START);
		assertTrue(PossessionSceneRules.DEPARTURE_DAEMON_START < PossessionSceneRules.DEPARTURE_DAEMON_ARRIVES);
		assertTrue(PossessionSceneRules.DEPARTURE_DAEMON_ARRIVES < PossessionSceneRules.DEPARTURE_TENDRILS_RISE);
		assertTrue(PossessionSceneRules.DEPARTURE_TENDRILS_RISE < PossessionSceneRules.DEPARTURE_TENDRILS_CLOSE);
		assertTrue(PossessionSceneRules.DEPARTURE_TENDRILS_CLOSE < PossessionSceneRules.DEPARTURE_BLACK);
		assertTrue(PossessionSceneRules.DEPARTURE_BLACK < Scene.DEPARTURE.length());
	}

	@Test
	void stepsRunOnceEachInOrderWhetherPlayedTickByTickOrSkipped() {
		for (Sequence sequence : Sequence.values()) {
			List<Step> played = new ArrayList<>();
			for (int t = 0; t <= sequence.settle(); t++) played.addAll(PossessionSceneRules.due(sequence, t - 1, t));
			assertEquals(List.of(Step.CROSS, Step.SETTLE), played, sequence.name());
			assertEquals(played, PossessionSceneRules.due(sequence, -1, sequence.settle()), sequence.name());
		}
	}

	@Test
	void aSkipMidwayRunsOnlyWhatIsLeft() {
		Sequence sequence = Sequence.BEGIN;
		assertEquals(List.of(Step.SETTLE), PossessionSceneRules.due(sequence, sequence.cut(), sequence.settle()));
		assertEquals(List.of(), PossessionSceneRules.due(sequence, sequence.settle(), sequence.settle()));
	}

	@Test
	void theSequenceIsFinishedAtTheSettleTick() {
		for (Sequence sequence : Sequence.values()) {
			assertFalse(PossessionSceneRules.finished(sequence, sequence.settle() - 1), sequence.name());
			assertTrue(PossessionSceneRules.finished(sequence, sequence.settle()), sequence.name());
		}
	}

	@Test
	void theSceneSwitchesAtTheCut() {
		for (Sequence sequence : Sequence.values()) {
			assertEquals(sequence.first(), PossessionSceneRules.sceneAt(sequence, sequence.cut() - 1));
			assertEquals(sequence.second(), PossessionSceneRules.sceneAt(sequence, sequence.cut()));
		}
	}

	@Test
	void theEchoFadesToShadowOnDepartureAndBackToSkinBySettle() {
		assertEquals(1.0F, PossessionSceneRules.manifest(Sequence.BEGIN, 0));
		assertEquals(0.5F, PossessionSceneRules.manifest(Sequence.BEGIN, PossessionSceneRules.DEPARTURE_FADE_END / 2), 1.0E-4F);
		assertEquals(0.0F, PossessionSceneRules.manifest(Sequence.BEGIN, PossessionSceneRules.DEPARTURE_FADE_END));
		assertEquals(0.0F, PossessionSceneRules.manifest(Sequence.BEGIN, Sequence.BEGIN.settle()));
		for (Sequence sequence : List.of(Sequence.RETURN_BROKEN, Sequence.RETURN_CLEAN)) {
			assertEquals(0.0F, PossessionSceneRules.manifest(sequence, 0), sequence.name());
			assertEquals(0.0F, PossessionSceneRules.manifest(sequence, sequence.cut()), sequence.name());
			assertEquals(1.0F, PossessionSceneRules.manifest(sequence, sequence.settle()), sequence.name());
		}
	}

	@Test
	void theBrokenRiteHalfBlastsBeforeTheSlamAndTheCleanOneReformsBeforeTheDrift() {
		assertTrue(PossessionSceneRules.BROKEN_ERUPT_END <= PossessionSceneRules.BROKEN_BLAST);
		assertTrue(PossessionSceneRules.BROKEN_BLAST < PossessionSceneRules.BROKEN_SLAM);
		assertTrue(PossessionSceneRules.BROKEN_SLAM < PossessionSceneRules.BROKEN_REMANIFEST_END);
		assertTrue(PossessionSceneRules.CLEAN_TENDRILS_RISE_END < PossessionSceneRules.CLEAN_REFORM_END);
		assertTrue(PossessionSceneRules.CLEAN_REFORM_END < PossessionSceneRules.CLEAN_DRIFT_END);
		assertTrue(PossessionSceneRules.CLEAN_DRIFT_END < PossessionSceneRules.CLEAN_REMANIFEST_END);
		assertTrue(PossessionSceneRules.CLEAN_REFORM_END - PossessionSceneRules.CLEAN_TENDRILS_RISE_END >= 50,
				"the daemon re-forms over about sixty ticks");
	}
}

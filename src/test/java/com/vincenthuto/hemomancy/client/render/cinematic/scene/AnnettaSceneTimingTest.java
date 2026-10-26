package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneRegistry;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Annetta arc's scenes keep to the server's own beats: no lock outlives the boss's passive, untouchable time. */
class AnnettaSceneTimingTest {
	@Test
	void introWindowMatchesScene() {
		BossScene scene = new AnnettaKnowlesScene();
		assertEquals(AnnettaEncounterRules.HARBINGER_INTRO_TICKS, scene.length());
		// the released Tooth Pecks hunts any player without Clarity, so the player keeps their hands and their eyes
		for (int t = 0; t <= scene.length(); t++) assertFalse(scene.inputLocked(t), "tick " + t);
		assertNull(scene.shot());
		assertEquals(AnnettaEncounterRules.HARBINGER_SCREAM_AT, scene.card(null).start(), "her name over the scream");
	}

	@Test
	void annettaHoldsStillAndUntouchableThroughTheWindow() {
		String entity = SourceAssertions.readMainJava("common/entity/boss/annetta/AnnettaKnowlesEntity.java");
		String hurt = SourceAssertions.methodBody(entity, "public boolean hurt(DamageSource source, float amount)");
		assertTrue(hurt.contains("harbingerIntro.active()"), "untouchable during her opening");
		String route = SourceAssertions.methodBody(entity, "private void startHarbingerRoute(");
		assertTrue(route.contains("harbingerIntro.begin()"));
		assertTrue(route.contains("BossScenes.play(this, BossPresentationIds.SCENE_ANNETTA_KNOWLES)"));
		String intro = SourceAssertions.methodBody(entity, "private void tickHarbingerIntro(ServerLevel server)");
		assertTrue(intro.contains("moveCooldown = 10"), "her first move comes as soon after the window as it did after the old scream");
		assertTrue(SourceAssertions.methodBody(entity, "private void startBreak(ServerLevel server)")
				.contains("BossPresentationIds.SCENE_ANNETTA_BREAK_CARD"));
	}

	@Test
	void priestessShotEndsAtEmergeLength() {
		BossScene scene = new StainedPriestessScene();
		assertTrue(scene.shotActive(PriestessCombatRules.EMERGE_LENGTH - BossScene.HANDBACK - 1));
		assertFalse(scene.shotActive(PriestessCombatRules.EMERGE_LENGTH - BossScene.HANDBACK));
		assertFalse(scene.inputLocked(PriestessCombatRules.EMERGE_LENGTH - BossScene.HANDBACK), "free before she can fight");
	}

	@Test
	void priestessCardAtStandEnd() {
		TitleCard card = new StainedPriestessScene().card(null);
		assertEquals(PriestessCombatRules.STAND_END, card.start());
		assertEquals("boss.hemomancy.stained_priestess.name", card.nameKey());
	}

	@Test
	void latentShotSpansTheTear() {
		BossScene scene = new LatentInfectionScene();
		assertFalse(scene.shotActive(LatentCombatRules.EMERGE_HIDDEN - 3), "the player's own view while Annetta drinks");
		assertTrue(scene.shotActive(LatentCombatRules.EMERGE_HIDDEN));
		assertTrue(scene.inputLocked(LatentCombatRules.EMERGE_HIDDEN));
		assertFalse(scene.inputLocked(LatentCombatRules.EMERGE_END - BossScene.HANDBACK), "free before it has risen");
		assertEquals(LatentCombatRules.EMERGE_END, scene.card(null).start());
	}

	@Test
	void phaseCardsAreShortAndLeaveTheCamera() {
		BossScene card = BossSceneRegistry.get(BossPresentationIds.SCENE_ANNETTA_BREAK_CARD).get();
		assertEquals(PhaseCardScene.LENGTH, card.length());
		assertNull(card.shot());
		assertFalse(card.inputLocked(10));
		assertFalse(card.hudHidden(10));
		assertNotNull(card.card(null));
		assertEquals(0.0F, card.letterbox(10), 1.0E-5F, "mid-fight, no band over the hotbar or the bars");
		assertFalse(card.skippable(30), "nothing to skip, so no hint and no sneak hijack mid-fight");
		assertFalse(new VesperEveningStarScene().skippable(30));
	}
}

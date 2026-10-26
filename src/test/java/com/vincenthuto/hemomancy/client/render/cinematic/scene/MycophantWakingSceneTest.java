package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules.Beat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Mycophant's scenes keep the server's beats: the player is held exactly as long as the server binds them. */
class MycophantWakingSceneTest {
	private static final int CONTROL = MycophantIntroRules.tickOf(Beat.CONTROL, false);
	private static final int ROAR = MycophantIntroRules.tickOf(Beat.ROAR, false);
	private static final int REMATCH_ROAR = MycophantIntroRules.tickOf(Beat.ROAR, true);
	private static final int REMATCH_RUSH = MycophantIntroRules.tickOf(Beat.RUSH, true);

	@Test
	void lockEndsAtControl() {
		MycophantWakingScene scene = new MycophantWakingScene();
		assertEquals(MycophantIntroClientState.WAKING_LENGTH, scene.length());
		assertTrue(scene.inputLocked(CONTROL - 0.5F));
		assertFalse(scene.inputLocked(CONTROL));
		for (int t = 0; t < 300; t++)
			assertEquals(MycophantIntroRules.bound(t, false), scene.inputLocked(t), "tick " + t + " matches the server");
		assertTrue(scene.hudHidden(CONTROL - 1));
		assertFalse(scene.hudHidden(CONTROL));
	}

	@Test
	void skipKeepsServerBinding() {
		MycophantWakingScene scene = new MycophantWakingScene();
		assertTrue(scene.keepsInputAfterSkip(60), "skipped at 40, still bound at 60");
		assertFalse(scene.keepsInputAfterSkip(CONTROL), "free once the server frees them");
	}

	@Test
	void letterboxClearsWithControl() {
		MycophantWakingScene scene = new MycophantWakingScene();
		assertEquals(1.0F, scene.letterbox(0), 1.0E-5F);
		assertEquals(1.0F, scene.letterbox(CONTROL), 1.0E-5F);
		assertEquals(0.0F, scene.letterbox(CONTROL + 12), 1.0E-5F);
	}

	@Test
	void cardAtRoar() {
		TitleCard card = new MycophantWakingScene().card(null);
		assertNotNull(card);
		assertEquals(ROAR, card.start());
		assertEquals("boss.hemomancy.mycophant.name", card.nameKey());
		assertEquals(0.0F, card.alpha(ROAR - 1), 1.0E-5F);
		assertEquals(1.0F, card.alpha(ROAR + TitleCard.FADE), 1.0E-5F);
	}

	@Test
	void rematchCardAtRematchRoarAndFreeBeforeTheRush() {
		MycophantRematchScene scene = new MycophantRematchScene();
		assertEquals(REMATCH_RUSH + 1, scene.length());
		assertEquals(REMATCH_ROAR, scene.card(null).start());
		assertNotNull(scene.shot());
		for (int t = 0; t <= scene.length(); t++) {
			if (scene.inputLocked(t)) assertTrue(MycophantIntroRules.invulnerable(t, true) || t < REMATCH_RUSH - 10,
					"never held once she can act, tick " + t);
		}
		assertFalse(scene.inputLocked(REMATCH_RUSH - 10));
		assertFalse(scene.shotActive(REMATCH_RUSH - 10), "the player's own view before the rush");
	}

	@Test
	void captureLocksThroughout() {
		MycophantCaptureScene scene = new MycophantCaptureScene();
		assertEquals(MycophantIntroClientState.CAPTURE_LENGTH, scene.length());
		for (int t = 0; t < scene.length(); t++) {
			assertTrue(scene.inputLocked(t));
			assertTrue(scene.keepsInputAfterSkip(t));
		}
		assertFalse(scene.skippable(10));
		assertTrue(scene.keepsAcrossDimensions(), "the dark holds across the transfer to the Nursery");
	}
}

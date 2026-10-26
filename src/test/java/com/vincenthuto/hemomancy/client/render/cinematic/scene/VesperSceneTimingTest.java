package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Vesper's arrival and his awakening as the Evening Star, held to the server's own windows. */
class VesperSceneTimingTest {
	private static final String CROWNED = "common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java";

	@Test
	void arrivalWindowMatchesScene() {
		BossScene scene = new VesperCrownedRefusalScene();
		assertTrue(scene.inputLocked(VesperCombatRules.ARRIVAL_TICKS - BossScene.HANDBACK - 1));
		assertFalse(scene.inputLocked(VesperCombatRules.ARRIVAL_TICKS - BossScene.HANDBACK));
		assertFalse(scene.shotActive(VesperCombatRules.ARRIVAL_TICKS - BossScene.HANDBACK));
		assertTrue(scene.card(null).start() >= VesperCombatRules.ARRIVAL_REAR_START, "his name once the mount rears");
	}

	@Test
	void theCrownedRefusalHoldsThroughHisArrival() {
		String entity = SourceAssertions.readMainJava(CROWNED);
		assertTrue(SourceAssertions.methodBody(entity, "public boolean hurt(DamageSource source, float amount)")
				.contains("arrival.active()"));
		String encounter = SourceAssertions.methodBody(entity, "private void tickEncounter()");
		assertTrue(encounter.indexOf("arrival.active()") < encounter.indexOf("controller.request"), "no moves before he has arrived");
		String manager = SourceAssertions.readMainJava("common/encounter/VesperOrdealManager.java");
		assertTrue(manager.contains("BossPresentationIds.SCENE_VESPER_CROWNED_REFUSAL"));
	}

	@Test
	void theEveningStarIsNamedButNeverHeld() {
		// the cocoon delivers him complete and he fights at once, with his puppets maybe still about
		BossScene scene = new VesperEveningStarScene();
		for (int t = 0; t <= scene.length(); t++) assertFalse(scene.inputLocked(t));
		assertNull(scene.shot());
		assertFalse(scene.hudHidden(5));
		assertEquals("boss.hemomancy.vesper_evening_star.name", scene.card(null).nameKey());
	}

	@Test
	void spawnEveningStarNamesHimOnceHeExists() {
		String body = SourceAssertions.methodBody(SourceAssertions.readMainJava(CROWNED), "private boolean spawnEveningStar()");
		int added = body.indexOf("addFreshEntity(eveningStar)");
		assertTrue(added > 0 && body.indexOf("BossPresentationIds.SCENE_VESPER_EVENING_STAR") > added);
		assertFalse(body.contains("beginAwakening()"), "the hand-off keeps the completed frame");
		assertTrue(SourceAssertions.readMainJava(CROWNED).contains("BossPresentationIds.SCENE_VESPER_COCOON_CARD"));
	}
}

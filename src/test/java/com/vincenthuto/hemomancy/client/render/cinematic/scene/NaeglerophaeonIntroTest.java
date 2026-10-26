package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Naeglerophaeon gathers out of its node before it hunts, and the scene holds the player exactly that long. */
class NaeglerophaeonIntroTest {
	@Test
	void introWindowMatchesScene() {
		BossScene scene = new NaeglerophaeonScene();
		assertTrue(scene.inputLocked(NaeglerophaeonCombatRules.INTRO_TICKS - BossScene.HANDBACK - 1));
		assertFalse(scene.inputLocked(NaeglerophaeonCombatRules.INTRO_TICKS - BossScene.HANDBACK),
				"free a little before it can act, whatever the packet's delay");
		assertFalse(scene.shotActive(NaeglerophaeonCombatRules.INTRO_TICKS - BossScene.HANDBACK),
				"the view is back before it starts hunting");
		assertEquals(NaeglerophaeonCombatRules.INTRO_EMERGE_AT, scene.card(null).start(), "its name as it comes out of the node");
		assertTrue(NaeglerophaeonCombatRules.INTRO_EMERGE_AT < NaeglerophaeonCombatRules.INTRO_TICKS);
	}

	@Test
	void itIsUntouchableAndPassiveThroughTheWindow() {
		String entity = SourceAssertions.readMainJava("common/entity/boss/endgame/NaeglerophaeonEntity.java");
		assertTrue(SourceAssertions.methodBody(entity, "public boolean isInvulnerableTo(DamageSource source)").contains("intro.active()"));
		String tick = SourceAssertions.methodBody(entity, "public void tick()");
		int intro = tick.indexOf("intro.active()");
		assertTrue(intro > 0 && intro < tick.indexOf("tickRetaliation(server)"), "no attacks, sparks or retaliation before it emerges");
		assertTrue(SourceAssertions.methodBody(entity, "private void beginTransition(ServerLevel server)")
				.contains("BossPresentationIds.SCENE_NAEGLEROPHAEON_TRANSITION_CARD"));
		String manager = SourceAssertions.readMainJava("common/encounter/VagrantMindEncounterManager.java");
		assertTrue(manager.indexOf("boss.beginIntro()") < manager.indexOf("addFreshEntity(boss)"), "spawns already gathering");
		assertTrue(manager.indexOf("BossPresentationIds.SCENE_NAEGLEROPHAEON") > manager.indexOf("addFreshEntity(boss)"),
				"the scene plays only once it exists");
	}
}

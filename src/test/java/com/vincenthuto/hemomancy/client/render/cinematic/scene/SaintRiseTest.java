package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every Saint rises out of its sarcophagus the same way, untouchable and still, before it fights. */
class SaintRiseTest {
	private static final List<String> SAINTS = List.of("hemorath", "putriciel", "velorum", "seraphae");

	@Test
	void riseWindowMatchesScene() {
		BossScene scene = new SaintRiseScene();
		assertTrue(scene.inputLocked(0));
		assertFalse(scene.inputLocked(SaintRise.TICKS - BossScene.HANDBACK), "free before the Saint can act");
		assertFalse(scene.shotActive(SaintRise.TICKS - BossScene.HANDBACK));
		assertNull(scene.card(null), "no name until the client knows which Saint it is");
	}

	@Test
	void theRiseSinksThenSurfaces() {
		assertTrue(SaintRise.depth(0.0F, 2.6F) >= 2.6F, "starts with its whole body under the floor");
		assertEquals(0.0F, SaintRise.depth(SaintRise.SURFACED, 2.6F), 1.0E-5F);
		assertEquals(0.0F, SaintRise.depth(SaintRise.TICKS + 10, 2.6F), 1.0E-5F);
		assertEquals(0.0F, SaintRise.depth(-1.0F, 2.6F), 1.0E-5F, "a Saint not rising stands where it is");
	}

	@Test
	void aSaintThatIsNotRisingNeverSinks() {
		// the replay clock predicts forward from -1, so an unguarded sample turns positive every few ticks
		BossReplayClock clock = new BossReplayClock();
		for (int tick = 0; tick < 30; tick++)
			for (float partial : new float[] { 0.0F, 0.5F, 0.99F })
				assertEquals(-1.0F, SaintRise.time(clock, -1, tick, partial), 1.0E-5F, "tick " + tick);
		assertTrue(SaintRise.time(new BossReplayClock(), 10, 5, 0.5F) >= 10.0F);
	}

	@Test
	void everySaintDefinesItsRiseAndIsUntouchableWhileRising() {
		// every Saint, Seraphae included, rises through SaintTrialEntity (BossFrameworkGuardrailTest pins the base)
		String base = SourceAssertions.readMainJava("common/entity/boss/saint/SaintTrialEntity.java");
		assertTrue(SourceAssertions.methodBody(base, "protected void defineSynchedData(").contains("DATA_RISE_TICK"),
				"SaintTrialEntity defines its rise tick, or constructing it throws");
		assertTrue(SourceAssertions.methodBody(base, "public float riseTime(float partialTick)").contains("SaintRise.time("),
				"SaintTrialEntity reads the rise through the guarded clock");
		for (String path : List.of("putriciel/PutricielEntity", "seraphae/SeraphaeEntity")) {
			String hurt = SourceAssertions.methodBody(
					SourceAssertions.readMainJava("common/entity/boss/saint/" + path + ".java"),
					"public boolean hurt(DamageSource source, float amount)");
			assertTrue(hurt.substring(1).trim().startsWith("if (rising()) return false;"),
					path + " checks the rise before its own health rules");
		}
		String hooks = SourceAssertions.readMainJava("common/entity/boss/saint/HarbingerSaintEncounterHooks.java");
		assertTrue(hooks.contains("boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5"),
				"it rises from the floor the sarcophagus stood on, not from its lid");
	}

	@Test
	void everySaintHasCardKeys() {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		for (String saint : SAINTS) {
			assertTrue(lang.has("boss.hemomancy." + saint + ".name"), saint);
			assertTrue(lang.has("boss.hemomancy." + saint + ".epithet"), saint);
		}
	}

	@Test
	void everySaintHoldsThroughItsRise() {
		for (String path : List.of("hemorath/HemorathEntity", "putriciel/PutricielEntity", "velorum/VelorumEntity",
				"seraphae/SeraphaeEntity")) {
			String aiStep = SourceAssertions.methodBody(
					SourceAssertions.readMainJava("common/entity/boss/saint/" + path + ".java"), "public void aiStep()");
			assertTrue(aiStep.contains("tickRise()"), path + " skips its attacks while it rises");
		}
		String hooks = SourceAssertions.readMainJava("common/entity/boss/saint/HarbingerSaintEncounterHooks.java");
		int added = hooks.indexOf("level.addFreshEntity(boss)");
		assertTrue(hooks.indexOf("beginRise()") < added && hooks.indexOf("BossPresentationIds.SCENE_SAINT_RISE") > added);
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperEveningStarPresentationSourceTest {
	private static final Path ROOT = Path.of("src/main");

	@Test
	void hoodAndAbsorptionPresentationStateIsSynchronizedAndPersisted() throws Exception {
		String entity = read("java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		for (String contract : new String[] {
				"DATA_HOOD_REMOVED", "DATA_HOOD_REMOVAL_TICK", "DATA_DEFEAT_ABSORPTION_PROGRESS",
				"DATA_FINAL_COLLAPSE_TICK",
				"HoodRemoved", "HoodRemovalTick", "DefeatAbsorptionProgress",
				"getHoodRemovalTick()", "getDefeatAbsorptionProgress()",
				"VesperEveningStarPresentationRules.shouldBeginHoodRemoval",
				"VesperEveningStarPresentationRules.isHoodRemovalActive"
		}) assertTrue(entity.contains(contract), contract);
	}

	@Test
	void hoodRemovalOwnsAThirtyTickCombatPauseAndOneShotAuthoredFeedback() throws Exception {
		String entity = read("java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String actions = read("java/com/vincenthuto/hemomancy/common/entity/boss/endgame/EndgameBossActions.java");
		String soundInit = read("java/com/vincenthuto/hemomancy/common/init/SoundInit.java");
		String sounds = read("resources/assets/hemomancy/sounds.json");
		assertTrue(entity.contains("tickHoodRemoval"));
		assertTrue(entity.contains("cancelForHoodRemoval"));
		assertTrue(entity.contains("ENTITY_VESPER_HOOD_REMOVE"));
		assertTrue(actions.contains("tickVesperHoodRemoval"));
		assertTrue(actions.contains("VesperVisualEffects.bloodCells"));
		assertTrue(soundInit.contains("ENTITY_VESPER_HOOD_REMOVE"));
		assertTrue(sounds.contains("\"entity.vesper.hood_remove\""));
	}

	@Test
	void shamedAbsorptionKeepsEligibilityPolicyAndCompletesOnlyAfterFinalCollapse() throws Exception {
		String entity = read("java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		assertTrue(entity.contains("VesperAbsorptionEligibilityRules.canAbsorb"));
		assertTrue(entity.contains("VesperEveningStarPresentationRules.isFinalCollapseComplete"));
		assertTrue(entity.contains("EndgameBossActions.tickVesperBloodAbsorption"));
		assertTrue(entity.indexOf("VesperOrdealManager.completeVictory(this)") < entity.indexOf("discard()"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(ROOT.resolve(relative)).replace("\r\n", "\n");
	}
}

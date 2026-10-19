package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class VesperEveningStarMotionTest {
	private static final Path MOVES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperEveningStarMoves.java");

	@Test
	void everyWeaponActionHasMotion() {
		for (VesperWeaponAction action : VesperWeaponAction.values()) {
			if (action == VesperWeaponAction.NONE) continue;
			assertTrue(VesperEveningStarMotion.supports(action), action + " has no animation");
		}
	}

	@Test
	void everyProceduralActionIsActuallyPosedByTheModel() throws Exception {
		String model = Files.readString(MOVES);
		for (VesperWeaponAction action : VesperEveningStarMotion.PROCEDURAL) {
			assertTrue(model.contains("case " + action.name()) || model.contains(", " + action.name() + " ->")
					|| model.contains(", " + action.name() + ",") || model.contains("case " + action.name() + ","),
					action + " is listed as procedural but no keyed move poses it");
		}
	}
}

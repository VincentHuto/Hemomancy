package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class InitiatoryDegreeEndgameResetTest {
	@Test
	void resetEndgameStateReturnsAFinishedPathToNothing() {
		InitiatoryDegree degree = new InitiatoryDegree();
		degree.setArchonPath(EnumArchonPath.SILENTIUM);
		degree.setFungalRevelationWitnessed(true);
		degree.setFungalSpineGranted(true);
		degree.setMycophantExposureTicks(100);
		degree.setMycophantRetryCooldownTicks(50);
		degree.setMycophantDefeated(true);

		degree.resetEndgameState();

		assertEquals(EnumArchonPath.NONE, degree.getArchonPath());
		assertFalse(degree.hasWitnessedFungalRevelation());
		assertFalse(degree.hasFungalSpineGranted());
		assertEquals(0, degree.getMycophantExposureTicks());
		assertEquals(0, degree.getMycophantRetryCooldownTicks());
		assertFalse(degree.isMycophantDefeated());
	}
}

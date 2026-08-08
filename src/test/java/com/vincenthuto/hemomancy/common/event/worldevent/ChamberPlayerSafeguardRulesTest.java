package com.vincenthuto.hemomancy.common.event.worldevent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ChamberPlayerSafeguardRulesTest {
	@Test
	void survivalPlayersUsePersonalCellSafeguardsOutsideTheVesperOrdeal() {
		assertTrue(ChamberPlayerSafeguardRules.shouldApply(false, false, false));
	}

	@Test
	void activeVesperOrdealsDisablePersonalCellSafeguards() {
		assertFalse(ChamberPlayerSafeguardRules.shouldApply(false, false, true));
	}

	@Test
	void creativeAndSpectatorPlayersRemainExempt() {
		assertFalse(ChamberPlayerSafeguardRules.shouldApply(true, false, false));
		assertFalse(ChamberPlayerSafeguardRules.shouldApply(false, true, false));
	}
}

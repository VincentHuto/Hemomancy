package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PallidShadowRulesTest {
	@Test
	void hostilePurityResetRespectsPvpAndProtectedPlayers() {
		assertTrue(PallidShadowRules.canTarget(true, false, false, false, true));
		assertFalse(PallidShadowRules.canTarget(false, false, false, false, true));
		assertFalse(PallidShadowRules.canTarget(true, true, false, false, true));
		assertFalse(PallidShadowRules.canTarget(true, false, true, false, true));
		assertFalse(PallidShadowRules.canTarget(true, false, false, true, true));
		assertFalse(PallidShadowRules.canTarget(true, false, false, false, false));
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HematicFortificationRulesTest {
	@Test
	void completedFortificationReducesVascularStrainByFifteenPercent() {
		assertEquals(10.0F, HematicFortificationRules.adjustedStrain(10.0F, false));
		assertEquals(8.5F, HematicFortificationRules.adjustedStrain(10.0F, true));
		assertEquals(0.0F, HematicFortificationRules.adjustedStrain(-4.0F, true));
	}
}

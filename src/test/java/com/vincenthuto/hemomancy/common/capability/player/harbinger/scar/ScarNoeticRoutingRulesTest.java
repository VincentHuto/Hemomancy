package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.HematicFortificationRules;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScarNoeticRoutingRulesTest {

	@Test
	void bestMatchingTierDoesNotStackSameTemperScars() {
		List<ScarDefinition> scars = List.of(
				new ScarDefinition(ScarType.CEREBRAL, EnumBloodTemper.LUX, 1F, 1),
				new ScarDefinition(ScarType.CEREBRAL, EnumBloodTemper.LUX, 3F, 3),
				new ScarDefinition(ScarType.CEREBRAL, EnumBloodTemper.FERRIC, 2F, 2),
				new ScarDefinition(ScarType.FUNGAL, EnumBloodTemper.LUX, 1F, 3));

		assertEquals(3, ScarNoeticRoutingRules.bestMatchingTier(EnumBloodTemper.LUX, scars));
		assertEquals(2, ScarNoeticRoutingRules.bestMatchingTier(EnumBloodTemper.FERRIC, scars));
		assertEquals(0, ScarNoeticRoutingRules.bestMatchingTier(EnumBloodTemper.MORTEM, scars));
	}

	@Test
	void strainReductionIsFivePercentPerBestTier() {
		assertEquals(20F, ScarNoeticRoutingRules.adjustedStrain(20F, 0));
		assertEquals(19F, ScarNoeticRoutingRules.adjustedStrain(20F, 1));
		assertEquals(18F, ScarNoeticRoutingRules.adjustedStrain(20F, 2));
		assertEquals(17F, ScarNoeticRoutingRules.adjustedStrain(20F, 3));
	}

	@Test
	void fixedMechanicalManipulationsAreNotNoeticRoutes() {
		assertFalse(ScarNoeticRoutingRules.qualifies("blood_absorption"));
		assertFalse(ScarNoeticRoutingRules.qualifies("blood_projection"));
		assertTrue(ScarNoeticRoutingRules.qualifies("conductive_mark"));
	}

	@Test
	void fortificationAndScarRoutingComposeMultiplicatively() {
		float fortified = HematicFortificationRules.adjustedStrain(100F, true);
		assertEquals(72.25F, ScarNoeticRoutingRules.adjustedStrain(fortified, 3), 0.0001F);
	}
}

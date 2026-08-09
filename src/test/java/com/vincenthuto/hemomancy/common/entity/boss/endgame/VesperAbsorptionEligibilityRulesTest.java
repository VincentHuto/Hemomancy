package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

final class VesperAbsorptionEligibilityRulesTest {
	private static final UUID OWNER = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID OTHER_PLAYER = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Test
	void commandSpawnedVesperWithoutAnOrdealOwnerCanBeAbsorbed() {
		assertTrue(VesperAbsorptionEligibilityRules.canAbsorb(true, 40, false, null, OTHER_PLAYER));
	}

	@Test
	void ordealVesperRemainsRestrictedToItsOwner() {
		assertTrue(VesperAbsorptionEligibilityRules.canAbsorb(true, 40, false, OWNER, OWNER));
		assertFalse(VesperAbsorptionEligibilityRules.canAbsorb(true, 40, false, OWNER, OTHER_PLAYER));
	}

	@Test
	void activeAnimatingOrAlreadyResolvedVesperCannotBeAbsorbed() {
		assertFalse(VesperAbsorptionEligibilityRules.canAbsorb(false, 40, false, null, OTHER_PLAYER));
		assertFalse(VesperAbsorptionEligibilityRules.canAbsorb(true, 39, false, null, OTHER_PLAYER));
		assertFalse(VesperAbsorptionEligibilityRules.canAbsorb(true, 40, true, null, OTHER_PLAYER));
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class LivingSickleInnerRingRulesTest {
	@Test
	void optionalSickleIsFixedWithoutConsumingANormalLoadoutSlot() {
		List<String> equipped = new ArrayList<>(List.of("some_spell"));
		assertTrue(ManipulationEquipHelper.equipNameIfPossible(equipped,
				ManipulationEquipHelper.CONJURE_SICKLE, 1));
		assertTrue(equipped.contains(ManipulationEquipHelper.CONJURE_SICKLE));
		assertEquals(1, ManipulationEquipHelper.countNormalEquippedNames(equipped));
		assertTrue(ManipulationEquipHelper.unequipNameIfAllowed(equipped,
				ManipulationEquipHelper.CONJURE_SICKLE));
		assertTrue(equipped.contains(ManipulationEquipHelper.CONJURE_SICKLE));
	}

	@Test
	void normalizationPreservesAnUnlockedSickleButDoesNotGrantItEarly() {
		List<String> unlocked = new ArrayList<>(List.of(ManipulationEquipHelper.CONJURE_SICKLE));
		ManipulationEquipHelper.normalizeEquippedNames(unlocked);
		assertEquals(List.of(ManipulationEquipHelper.BLOOD_ABSORPTION,
				ManipulationEquipHelper.BLOOD_PROJECTION,
				ManipulationEquipHelper.CONJURE_SICKLE), unlocked);

		List<String> locked = new ArrayList<>();
		ManipulationEquipHelper.normalizeEquippedNames(locked);
		assertFalse(locked.contains(ManipulationEquipHelper.CONJURE_SICKLE));
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class LivingSickleInnerRingRulesTest {
	@Test
	void sickleUsesANormalLoadoutSlotAndCanBeUnequipped() {
		List<String> equipped = new ArrayList<>();
		assertTrue(ManipulationEquipHelper.equipNameIfPossible(equipped, ManipulationEquipHelper.CONJURE_SICKLE, 1));
		assertTrue(equipped.contains(ManipulationEquipHelper.CONJURE_SICKLE));
		assertEquals(1, ManipulationEquipHelper.countNormalEquippedNames(equipped));
		assertTrue(ManipulationEquipHelper.unequipNameIfAllowed(equipped,
				ManipulationEquipHelper.CONJURE_SICKLE));
		assertFalse(equipped.contains(ManipulationEquipHelper.CONJURE_SICKLE));
		assertFalse(ManipulationEquipHelper.isFixedMechanicalManip(ManipulationEquipHelper.CONJURE_SICKLE));

		List<String> full = new ArrayList<>(List.of(ManipulationEquipHelper.BLOOD_ABSORPTION,
				ManipulationEquipHelper.BLOOD_PROJECTION, ManipulationEquipHelper.CONJURE_STAFF, "some_spell"));
		assertFalse(ManipulationEquipHelper.equipNameIfPossible(full, ManipulationEquipHelper.CONJURE_SICKLE, 1));
	}

	@Test
	void normalizationTreatsAnEquippedSickleLikeAnyOtherLoadoutEntryAndDoesNotGrantItEarly() {
		List<String> unlocked = new ArrayList<>(List.of(ManipulationEquipHelper.CONJURE_SICKLE));
		ManipulationEquipHelper.normalizeEquippedNames(unlocked);
		assertEquals(List.of(ManipulationEquipHelper.BLOOD_ABSORPTION,
				ManipulationEquipHelper.BLOOD_PROJECTION,
				ManipulationEquipHelper.CONJURE_STAFF,
				ManipulationEquipHelper.CONJURE_SICKLE), unlocked);

		List<String> locked = new ArrayList<>();
		ManipulationEquipHelper.normalizeEquippedNames(locked);
		assertFalse(locked.contains(ManipulationEquipHelper.CONJURE_SICKLE));
	}
}

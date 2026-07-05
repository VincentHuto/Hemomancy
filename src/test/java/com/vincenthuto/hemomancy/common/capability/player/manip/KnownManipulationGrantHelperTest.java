package com.vincenthuto.hemomancy.common.capability.player.manip;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;

import java.util.ArrayList;
import java.util.List;

public final class KnownManipulationGrantHelperTest {
	private KnownManipulationGrantHelperTest() {
	}

	public static void main(String[] args) {
		autoEquipUsesFreeSlotsOnly();
		fullSlotsDoNotReplace();
		duplicateEquipDoesNotAddAgain();
		mechanicalManipulationsAreRestoredAndLocked();
		mechanicalManipulationsDoNotConsumeNormalSlots();
	}

	private static void autoEquipUsesFreeSlotsOnly() {
		List<String> equipped = new ArrayList<>();

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3);

		assertTrue("equip changed state", changed);
		assertEquals("equipped count includes fixed mechanics", 3, equipped.size());
		assertEquals("absorption is fixed first", ManipulationEquipHelper.BLOOD_ABSORPTION, equipped.get(0));
		assertEquals("projection is fixed second", ManipulationEquipHelper.BLOOD_PROJECTION, equipped.get(1));
		assertEquals("equipped name", "blood_shot", equipped.get(2));
	}

	private static void fullSlotsDoNotReplace() {
		List<String> equipped = new ArrayList<>();
		equipped.add(ManipulationEquipHelper.BLOOD_ABSORPTION);
		equipped.add(ManipulationEquipHelper.BLOOD_PROJECTION);
		equipped.add("existing_one");
		equipped.add("existing_two");

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "deadly_gaze", 2);

		assertFalse("full slots unchanged", changed);
		assertEquals("equipped count unchanged", 4, equipped.size());
		assertEquals("first fixed mechanic preserved", ManipulationEquipHelper.BLOOD_ABSORPTION, equipped.get(0));
		assertEquals("second fixed mechanic preserved", ManipulationEquipHelper.BLOOD_PROJECTION, equipped.get(1));
		assertEquals("first equipped preserved", "existing_one", equipped.get(2));
		assertEquals("second equipped preserved", "existing_two", equipped.get(3));
	}

	private static void duplicateEquipDoesNotAddAgain() {
		List<String> equipped = new ArrayList<>();
		equipped.add(ManipulationEquipHelper.BLOOD_ABSORPTION);
		equipped.add(ManipulationEquipHelper.BLOOD_PROJECTION);
		equipped.add("blood_shot");

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3);

		assertFalse("duplicate unchanged", changed);
		assertEquals("equipped count unchanged", 3, equipped.size());
	}

	private static void mechanicalManipulationsAreRestoredAndLocked() {
		List<String> equipped = new ArrayList<>();
		equipped.add("blood_shot");

		boolean normalized = ManipulationEquipHelper.normalizeEquippedNames(equipped);
		boolean removedAbsorption = ManipulationEquipHelper.unequipNameIfAllowed(equipped,
				ManipulationEquipHelper.BLOOD_ABSORPTION);
		boolean removedProjection = ManipulationEquipHelper.unequipNameIfAllowed(equipped,
				ManipulationEquipHelper.BLOOD_PROJECTION);

		assertTrue("normalization changed state", normalized);
		assertFalse("absorption cannot be unequipped", removedAbsorption);
		assertFalse("projection cannot be unequipped", removedProjection);
		assertEquals("absorption stays first", ManipulationEquipHelper.BLOOD_ABSORPTION, equipped.get(0));
		assertEquals("projection stays second", ManipulationEquipHelper.BLOOD_PROJECTION, equipped.get(1));
		assertEquals("normal manip preserved after mechanics", "blood_shot", equipped.get(2));
	}

	private static void mechanicalManipulationsDoNotConsumeNormalSlots() {
		List<String> equipped = new ArrayList<>();

		assertTrue("first normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3));
		assertTrue("second normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "deadly_gaze", 3));
		assertTrue("third normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_needle", 3));
		assertFalse("fourth normal blocked by normal slot limit",
				ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_burst", 3));

		assertEquals("total count includes two fixed mechanics", 5, equipped.size());
		assertEquals("normal count respects memorized slots", 3,
				ManipulationEquipHelper.countNormalEquippedNames(equipped));
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}

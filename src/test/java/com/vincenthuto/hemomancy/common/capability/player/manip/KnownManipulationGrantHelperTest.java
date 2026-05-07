package com.vincenthuto.hemomancy.common.capability.player.manip;

import java.util.ArrayList;
import java.util.List;

public final class KnownManipulationGrantHelperTest {
	private KnownManipulationGrantHelperTest() {
	}

	public static void main(String[] args) {
		autoEquipUsesFreeSlotsOnly();
		fullSlotsDoNotReplace();
		duplicateEquipDoesNotAddAgain();
	}

	private static void autoEquipUsesFreeSlotsOnly() {
		List<String> equipped = new ArrayList<>();

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3);

		assertTrue("equip changed state", changed);
		assertEquals("equipped count", 1, equipped.size());
		assertEquals("equipped name", "blood_shot", equipped.get(0));
	}

	private static void fullSlotsDoNotReplace() {
		List<String> equipped = new ArrayList<>();
		equipped.add("existing_one");
		equipped.add("existing_two");

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_lamp", 2);

		assertFalse("full slots unchanged", changed);
		assertEquals("equipped count unchanged", 2, equipped.size());
		assertEquals("first equipped preserved", "existing_one", equipped.get(0));
		assertEquals("second equipped preserved", "existing_two", equipped.get(1));
	}

	private static void duplicateEquipDoesNotAddAgain() {
		List<String> equipped = new ArrayList<>();
		equipped.add("blood_shot");

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3);

		assertFalse("duplicate unchanged", changed);
		assertEquals("equipped count unchanged", 1, equipped.size());
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

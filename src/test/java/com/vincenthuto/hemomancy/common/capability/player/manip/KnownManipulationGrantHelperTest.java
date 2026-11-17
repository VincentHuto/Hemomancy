package com.vincenthuto.hemomancy.common.capability.player.manip;

import org.junit.jupiter.api.Test;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class KnownManipulationGrantHelperTest {
	private KnownManipulationGrantHelperTest() {
	}

	@Test
	void allChecksPass() {
		autoEquipUsesFreeSlotsOnly();
		fullSlotsDoNotReplace();
		duplicateEquipDoesNotAddAgain();
		mechanicalManipulationsAreRestoredAndLocked();
		mechanicalManipulationsDoNotConsumeNormalSlots();
		newlyLearnedManipulationsHaveIndependentMastery();
	}

	private static void autoEquipUsesFreeSlotsOnly() {
		List<String> equipped = new ArrayList<>();

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3);

		assertTrue("equip changed state", changed);
		assertEquals("equipped count includes the fixed Conjure Staff", 2, equipped.size());
		assertEquals("Conjure Staff is fixed first", ManipulationEquipHelper.CONJURE_STAFF, equipped.get(0));
		assertEquals("equipped name", "blood_shot", equipped.get(1));
	}

	private static void fullSlotsDoNotReplace() {
		List<String> equipped = new ArrayList<>();
		equipped.add(ManipulationEquipHelper.CONJURE_STAFF);
		equipped.add("existing_one");
		equipped.add("existing_two");

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "deadly_gaze", 2);

		assertFalse("full slots unchanged", changed);
		assertEquals("equipped count unchanged", 3, equipped.size());
		assertEquals("fixed Conjure Staff preserved", ManipulationEquipHelper.CONJURE_STAFF, equipped.get(0));
		assertEquals("first equipped preserved", "existing_one", equipped.get(1));
		assertEquals("second equipped preserved", "existing_two", equipped.get(2));
	}

	private static void duplicateEquipDoesNotAddAgain() {
		List<String> equipped = new ArrayList<>();
		equipped.add(ManipulationEquipHelper.CONJURE_STAFF);
		equipped.add("blood_shot");

		boolean changed = ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3);

		assertFalse("duplicate unchanged", changed);
		assertEquals("equipped count unchanged", 2, equipped.size());
	}

	/** Core-loop Track 1 (CL-2): Projection and Absorption are verbs, never equipped; a saved list naming them drops them. */
	private static void mechanicalManipulationsAreRestoredAndLocked() {
		List<String> equipped = new ArrayList<>();
		equipped.add(ManipulationEquipHelper.BLOOD_ABSORPTION);
		equipped.add(ManipulationEquipHelper.BLOOD_PROJECTION);
		equipped.add("blood_shot");

		boolean normalized = ManipulationEquipHelper.normalizeEquippedNames(equipped);
		boolean removedStaff = ManipulationEquipHelper.unequipNameIfAllowed(equipped,
				ManipulationEquipHelper.CONJURE_STAFF);
		boolean equippedProjection = ManipulationEquipHelper.equipNameIfPossible(equipped,
				ManipulationEquipHelper.BLOOD_PROJECTION, 3);

		assertTrue("normalization changed state", normalized);
		assertFalse("Conjure Staff cannot be unequipped", removedStaff);
		assertFalse("Projection cannot be equipped", equippedProjection);
		assertEquals("only Conjure Staff and the normal memory remain", List.of(ManipulationEquipHelper.CONJURE_STAFF,
				"blood_shot"), equipped);
		assertTrue("the verbs are verbs", ManipulationEquipHelper.isVerb(ManipulationEquipHelper.BLOOD_ABSORPTION)
				&& ManipulationEquipHelper.isVerb(ManipulationEquipHelper.BLOOD_PROJECTION));
	}

	private static void mechanicalManipulationsDoNotConsumeNormalSlots() {
		List<String> equipped = new ArrayList<>();

		assertTrue("first normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3));
		assertTrue("second normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "deadly_gaze", 3));
		assertTrue("third normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_needle", 3));
		assertFalse("fourth normal blocked by normal slot limit",
				ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_burst", 3));

		assertEquals("total count includes the fixed Conjure Staff", 4, equipped.size());
		assertEquals("normal count respects memorized slots", 3,
				ManipulationEquipHelper.countNormalEquippedNames(equipped));
	}

	private static void newlyLearnedManipulationsHaveIndependentMastery() {
		var known = new LinkedHashMap<BloodManipulation, ManipLevel>();
		var equipped = new ArrayList<String>();
		BloodManipulation first = manipulation("mastery_test_first");
		BloodManipulation second = manipulation("mastery_test_second");

		KnownManipulationGrantHelper.learnAndEquipIfPossible(known, equipped, first, 2);
		KnownManipulationGrantHelper.learnAndEquipIfPossible(known, equipped, second, 2);

		ManipLevel firstLevel = known.get(first);
		ManipLevel secondLevel = known.get(second);
		assertTrue("first learned manipulation has mastery state", firstLevel != null);
		assertTrue("second learned manipulation has mastery state", secondLevel != null);
		assertFalse("learned manipulations do not share mastery state", firstLevel == secondLevel);
	}

	private static BloodManipulation manipulation(String name) {
		return new BloodManipulation(name, 10, 0, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTemper.ANIMUS, EnumVeinSections.HEAD);
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

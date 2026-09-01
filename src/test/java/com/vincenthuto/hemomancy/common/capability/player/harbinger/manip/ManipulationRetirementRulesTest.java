package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.ArrayList;
import java.util.List;

public final class ManipulationRetirementRulesTest {
	private ManipulationRetirementRulesTest() {
	}

	public static void main(String[] args) {
		retiredManipulationIdsAreBlocked();
		retiredMemoryItemsIncludeMechanicalAndRetiredManipMemories();
		equipNormalizationRemovesRetiredManipulations();
		loadoutsDropRetiredManipulations();
	}

	private static void retiredManipulationIdsAreBlocked() {
		assertTrue("blood lamp retired", ManipulationRetirementRules.isRetiredManipulation("blood_lamp"));
		assertTrue("crimson harvest retired", ManipulationRetirementRules.isRetiredManipulation("crimson_harvest"));
		assertTrue("hemosynthesis retired", ManipulationRetirementRules.isRetiredManipulation("hemosynthesis"));
		assertTrue("vital reservoir retired", ManipulationRetirementRules.isRetiredManipulation("vital_reservoir"));
		assertTrue("sanguine excavation retired", ManipulationRetirementRules.isRetiredManipulation("sanguine_excavation"));
		assertTrue("ferric resonance retired", ManipulationRetirementRules.isRetiredManipulation("ferric_resonance"));
		assertTrue("glacial bastion retired", ManipulationRetirementRules.isRetiredManipulation("glacial_bastion"));
		assertTrue("blood eclipse mantle retired", ManipulationRetirementRules.isRetiredManipulation("blood_eclipse_mantle"));
		assertTrue("crimson sight retired", ManipulationRetirementRules.isRetiredManipulation("crimson_sight"));
		assertTrue("glacial circulation retired", ManipulationRetirementRules.isRetiredManipulation("glacial_circulation"));
		assertTrue("ferric transmutation retired", ManipulationRetirementRules.isRetiredManipulation("ferric_transmutation"));
		assertTrue("vigil of glass retired", ManipulationRetirementRules.isRetiredManipulation("vigil_of_glass"));
		assertTrue("hematic ballast retired", ManipulationRetirementRules.isRetiredManipulation("hematic_ballast"));
		assertTrue("summon thrall manipulation retired", ManipulationRetirementRules.isRetiredManipulation("summon_thrall"));
		assertTrue("venous travel manipulation retired", ManipulationRetirementRules.isRetiredManipulation("venous_travel"));
		assertFalse("summon avatar remains active", ManipulationRetirementRules.isRetiredManipulation("summon_avatar"));
		assertFalse("blood absorption remains functional",
				ManipulationRetirementRules.isRetiredManipulation(ManipulationEquipHelper.BLOOD_ABSORPTION));
		assertFalse("blood projection remains functional",
				ManipulationRetirementRules.isRetiredManipulation(ManipulationEquipHelper.BLOOD_PROJECTION));
		assertFalse("conjure staff remains functional",
				ManipulationRetirementRules.isRetiredManipulation("conjure_staff"));
		assertFalse("sanguine mending remains functional",
				ManipulationRetirementRules.isRetiredManipulation("sanguine_mending"));
		assertFalse("pyretic forge remains functional",
				ManipulationRetirementRules.isRetiredManipulation("pyretic_forge"));
	}

	private static void retiredMemoryItemsIncludeMechanicalAndRetiredManipMemories() {
		assertTrue("conjure living staff memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_conjure_living_staff"));
		assertTrue("blood absorption memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_blood_absorption"));
		assertTrue("blood projection memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_blood_projection"));
		assertTrue("blood lamp memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_blood_lamp"));
		assertTrue("blood lamp crude memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("crude_memory_blood_lamp"));
		assertTrue("crimson harvest memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_crimson_harvest"));
		assertTrue("crimson harvest crude memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("crude_memory_crimson_harvest"));
		assertTrue("hemosynthesis memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_hemosynthesis"));
		assertTrue("vital reservoir memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_vital_reservoir"));
		assertTrue("sanguine excavation memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_sanguine_excavation"));
		assertTrue("ferric resonance memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_ferric_resonance"));
		assertTrue("glacial bastion memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_glacial_bastion"));
		assertTrue("blood eclipse mantle memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_blood_eclipse_mantle"));
		assertTrue("crimson sight memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_crimson_sight"));
		assertTrue("glacial circulation memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_glacial_circulation"));
		assertTrue("ferric transmutation memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_ferric_transmutation"));
		assertTrue("vigil of glass memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_vigil_of_glass"));
		assertTrue("hematic ballast memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_hematic_ballast"));
		assertTrue("summon thrall memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_summon_thrall"));
		assertTrue("venous travel memory retired",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_venous_travel"));
		assertFalse("sanguine mending memory remains active",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_sanguine_mending"));
		assertFalse("pyretic forge memory remains active",
				ManipulationRetirementRules.isRetiredMemoryItemId("memory_pyretic_forge"));
	}

	private static void equipNormalizationRemovesRetiredManipulations() {
		List<String> equipped = new ArrayList<>(List.of(
				"blood_lamp",
				"blood_shot",
				"crimson_harvest",
				"sanguine_mending"));

		boolean changed = ManipulationEquipHelper.normalizeEquippedNames(equipped);
		boolean equippedRetired = ManipulationEquipHelper.equipNameIfPossible(equipped, "hemosynthesis", 5);

		assertTrue("normalization changed retired list", changed);
		assertFalse("retired manipulation cannot be equipped", equippedRetired);
		assertEquals("fixed absorption first", ManipulationEquipHelper.BLOOD_ABSORPTION, equipped.get(0));
		assertEquals("fixed projection second", ManipulationEquipHelper.BLOOD_PROJECTION, equipped.get(1));
		assertEquals("blood shot preserved", "blood_shot", equipped.get(2));
		assertEquals("sanguine mending preserved", "sanguine_mending", equipped.get(3));
		assertEquals("retired entries removed", 4, equipped.size());
		assertEquals("normal count ignores fixed and retired entries", 2,
				ManipulationEquipHelper.countNormalEquippedNames(equipped));
	}

	private static void loadoutsDropRetiredManipulations() {
		ManipulationLoadout loadout = ManipulationLoadout.of("Old Utility",
				"blood_lamp",
				List.of("blood_lamp", "blood_shot", "vital_reservoir", "hematic_flare"),
				0);

		assertEquals("selected falls back to first active manipulation", "blood_shot", loadout.selectedManipName());
		assertEquals("retired names are removed from loadout", List.of("blood_shot", "hematic_flare"),
				loadout.manipNames());
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

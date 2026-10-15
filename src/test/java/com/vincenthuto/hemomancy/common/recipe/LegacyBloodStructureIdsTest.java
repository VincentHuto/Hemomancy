package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

final class LegacyBloodStructureIdsTest {
	@Test
	void everyMovedMachineRemapsToItsWorkingCraft() {
		assertEquals(20, LegacyBloodStructureIds.MOVED.size());
		for (String name : LegacyBloodStructureIds.MOVED) {
			assertEquals(Optional.of(ResourceLocation.parse("hemomancy:cardinal_rite/working/" + name)),
					LegacyBloodStructureIds.remap(ResourceLocation.parse("hemomancy:blood_structure/" + name)), name);
		}
	}

	@Test
	void theRetiredBloodwellRemapsToTheFoundingFane() {
		assertEquals(Optional.of(ResourceLocation.parse("hemomancy:cardinal_rite/founding_fane")),
				LegacyBloodStructureIds.remap(ResourceLocation.parse("hemomancy:blood_structure/consecrated_bloodwell")));
		MnemonicBlueprintTarget bloodwell = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
				ResourceLocation.parse("hemomancy:blood_structure/consecrated_bloodwell"));
		assertEquals(MnemonicBlueprintTarget.Type.CARDINAL_RITE, bloodwell.type());
		assertEquals(ResourceLocation.parse("hemomancy:cardinal_rite/founding_fane"), bloodwell.recipeId());
	}

	@Test
	void floorlessAndForeignIdsDoNotRemap() {
		assertTrue(LegacyBloodStructureIds.remap(ResourceLocation.parse("hemomancy:blood_structure/living_staff")).isEmpty());
		assertTrue(LegacyBloodStructureIds.remap(ResourceLocation.parse("other:blood_structure/somatic_loom")).isEmpty());
		assertTrue(LegacyBloodStructureIds.remap(ResourceLocation.parse("hemomancy:cardinal_rite/rooted_vein")).isEmpty());
	}

	@Test
	void pathsRemapWithOrWithoutTheStructurePrefix() {
		assertEquals("cardinal_rite/working/somatic_loom", LegacyBloodStructureIds.remapPath("blood_structure/somatic_loom"));
		assertEquals("cardinal_rite/working/somatic_loom", LegacyBloodStructureIds.remapPath("somatic_loom"));
		assertEquals("blood_structure/living_staff", LegacyBloodStructureIds.remapPath("blood_structure/living_staff"));
	}

	@Test
	void oldBlueprintsForMovedMachinesBecomeRiteBlueprints() {
		MnemonicBlueprintTarget loom = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
				ResourceLocation.parse("hemomancy:blood_structure/somatic_loom"));
		assertEquals(MnemonicBlueprintTarget.Type.CARDINAL_RITE, loom.type());
		assertEquals(ResourceLocation.parse("hemomancy:cardinal_rite/working/somatic_loom"), loom.recipeId());

		MnemonicBlueprintTarget staff = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
				ResourceLocation.parse("hemomancy:blood_structure/living_staff"));
		assertEquals(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, staff.type());
	}
}

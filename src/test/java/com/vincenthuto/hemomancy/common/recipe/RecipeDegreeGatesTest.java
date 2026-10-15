package com.vincenthuto.hemomancy.common.recipe;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeDegreeGatesTest {
	@Test
	void rankRiteParticipatesOnlyUntilItsTargetDegreeIsReached() {
		assertTrue(RecipeDegreeGates.rankupWindowOpen(2, 3));
		assertFalse(RecipeDegreeGates.rankupWindowOpen(3, 3));
		assertFalse(RecipeDegreeGates.rankupWindowOpen(4, 3));
	}

	@Test
	void onlyTheReliquaryWorkingCraftWaitsForTheMnemonistsLesson() {
		assertTrue(RecipeDegreeGates.needsReliquaryLesson(
				ResourceLocation.parse("hemomancy:cardinal_rite/working/mnemonic_reliquary")));
		assertFalse(RecipeDegreeGates.needsReliquaryLesson(
				ResourceLocation.parse("hemomancy:cardinal_rite/working/somatic_loom")));
		assertFalse(RecipeDegreeGates.needsReliquaryLesson(
				ResourceLocation.parse("other:cardinal_rite/working/mnemonic_reliquary")));
		assertFalse(RecipeDegreeGates.needsReliquaryLesson(null));
	}
}

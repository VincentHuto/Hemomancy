package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftRiteStateTest {
	private static final ResourceLocation LOOM = ResourceLocation.parse("hemomancy:cardinal_rite/working/somatic_loom");

	private static ActiveCardinalRite loom(int feedMl) {
		return ActiveCardinalRite.workingCraft(UUID.randomUUID(), BlockPos.ZERO, LOOM, 5, feedMl);
	}

	@Test
	void aCraftStartsByFeedingItsSubject() {
		ActiveCardinalRite rite = loom(150);
		assertTrue(rite.isWorkingCraft());
		assertEquals(CardinalRitePhase.SUBJECT_FEEDING, rite.getPhase());
		assertEquals(150, rite.subjectFeedNeeded());
		assertEquals(0.0F, rite.subjectFill());
		assertFalse(rite.areAnchorsConsecrated(), "a craft has no anchors to consecrate");
	}

	@Test
	void feedingIsCappedAtTheSubjectsRequirement() {
		ActiveCardinalRite rite = loom(150);
		assertTrue(rite.feedSubject(100));
		assertEquals(50, rite.subjectFeedNeeded());
		assertEquals(100.0F / 150.0F, rite.subjectFill(), 0.0001F);
		assertTrue(rite.feedSubject(500));
		assertEquals(0, rite.subjectFeedNeeded());
		assertEquals(150, rite.getCommittedBloodMl(), "only the blood the subject took is committed");
		assertFalse(rite.feedSubject(10), "a full subject takes nothing more");
	}

	@Test
	void aFullSubjectSealsIntoItsOfferings() {
		ActiveCardinalRite rite = loom(50);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(BlockPos.ZERO.east(4), null, true)));
		rite.feedSubject(50);
		assertTrue(rite.isAltarSealed());
		assertEquals(CardinalRitePhase.OFFERING_PROCESSION, rite.getPhase());
	}

	@Test
	void aFullSubjectWithoutOfferingsGoesStraightToCulmination() {
		ActiveCardinalRite rite = loom(50);
		rite.captureOfferingItinerary(List.of());
		rite.feedSubject(50);
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void craftStateSurvivesSaving() {
		ActiveCardinalRite rite = loom(150);
		rite.feedSubject(60);
		rite.craft().tag().putString("EscrowState", "ESCROWED");
		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertTrue(copy.isWorkingCraft());
		assertEquals(CardinalRitePhase.SUBJECT_FEEDING, copy.getPhase());
		assertEquals(90, copy.subjectFeedNeeded());
		assertEquals("ESCROWED", copy.craft().tag().getString("EscrowState"));
	}

	@Test
	void ordinaryRitesAreNotCrafts() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/rooted_vein"), 400, 5, 2, false, 0, 4);
		assertFalse(rite.isWorkingCraft());
		assertEquals(0, rite.subjectFeedNeeded());
		assertFalse(rite.feedSubject(10));
	}
}

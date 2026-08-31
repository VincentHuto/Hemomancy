package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class LivingStaffMorphSequenceTest {

	@Test
	void conjuringOnlyRunsTheFormationHalf() {
		assertEquals(8, LivingStaffMorphSequence.durationTicks(false, true));
		assertEquals(LivingStaffMorphSequence.Phase.FORM,
				LivingStaffMorphSequence.phase(0.0F, false, true));
		assertEquals(0.5F, LivingStaffMorphSequence.phaseProgress(4.0F, false, true), 0.0001F);
		assertEquals(LivingStaffMorphSequence.Phase.COMPLETE,
				LivingStaffMorphSequence.phase(8.0F, false, true));
	}

	@Test
	void dispellingOnlyRunsTheDissolveHalf() {
		assertEquals(8, LivingStaffMorphSequence.durationTicks(true, false));
		assertEquals(LivingStaffMorphSequence.Phase.DISSOLVE,
				LivingStaffMorphSequence.phase(0.0F, true, false));
		assertEquals(0.5F, LivingStaffMorphSequence.phaseProgress(4.0F, true, false), 0.0001F);
		assertEquals(LivingStaffMorphSequence.Phase.COMPLETE,
				LivingStaffMorphSequence.phase(8.0F, true, false));
	}

	@Test
	void swappingDissolvesBeforeForming() {
		assertEquals(16, LivingStaffMorphSequence.durationTicks(true, true));
		assertEquals(LivingStaffMorphSequence.Phase.DISSOLVE,
				LivingStaffMorphSequence.phase(7.99F, true, true));
		assertEquals(LivingStaffMorphSequence.Phase.FORM,
				LivingStaffMorphSequence.phase(8.0F, true, true));
		assertEquals(0.5F, LivingStaffMorphSequence.phaseProgress(12.0F, true, true), 0.0001F);
		assertEquals(LivingStaffMorphSequence.Phase.COMPLETE,
				LivingStaffMorphSequence.phase(16.0F, true, true));
	}

	@Test
	void emptyTransitionCompletesImmediately() {
		assertEquals(0, LivingStaffMorphSequence.durationTicks(false, false));
		assertEquals(LivingStaffMorphSequence.Phase.COMPLETE,
				LivingStaffMorphSequence.phase(0.0F, false, false));
	}

	@Test
	void unchangedOccupiedOffhandDoesNotAddAMorphPhase() {
		assertEquals(false, LivingStaffMorphSequence.hasChangedStack(false, false, false, true));
		assertEquals(true, LivingStaffMorphSequence.hasChangedStack(true, true, false, true));
	}
}

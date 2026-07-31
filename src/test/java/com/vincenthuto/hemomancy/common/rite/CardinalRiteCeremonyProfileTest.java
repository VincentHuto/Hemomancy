package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CardinalRiteCeremonyProfileTest {
	@Test
	void parsesAllThreeAuthoredProfiles() {
		assertEquals("SIMPLE", CardinalRiteCeremonyProfile.byName("simple").name());
		assertEquals("STANDARD", CardinalRiteCeremonyProfile.byName("standard").name());
		assertEquals("CEREMONIAL", CardinalRiteCeremonyProfile.byName("ceremonial").name());
	}

	@Test
	void rejectsUnknownProfilesInsteadOfSilentlySelectingAComplexCeremony() {
		assertThrows(IllegalArgumentException.class,
				() -> CardinalRiteCeremonyProfile.byName("full"));
	}
}

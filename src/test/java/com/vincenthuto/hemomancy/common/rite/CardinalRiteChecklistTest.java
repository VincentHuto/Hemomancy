package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteChecklistTest {
    @Test
    void requiredPreparationDoesNotMasqueradeAsOptionalOrReady() {
        List<String> blocked = CardinalRiteChecklist.inscription(1, 1, 1, 0, 0, 1, true);
        assertTrue(blocked.contains("Required sigils: 0/1"));
        assertTrue(blocked.contains("Required allies: 0/1"));
        assertEquals("Fulfil required preparations before sealing", blocked.getLast());
        assertEquals("Project into the daemon to begin",
                CardinalRiteChecklist.inscription(1, 0, 1, 1, 1, 1, true).getLast());
    }

	@Test
	void inscriptionNamesOptionalPreparationAndDaemonProjectionAction() {
		List<String> lines = CardinalRiteChecklist.inscription(2, 1, 1, true);

		assertEquals("Optional sigils: 1/2", lines.get(0));
		assertTrue(lines.contains("Optional allies: 1 assigned"));
		assertTrue(lines.contains("Medium seated"));
		assertEquals("Project into the daemon to begin", lines.get(lines.size() - 1));
	}

	@Test
	void missingCatalystIsReportedAsABlocker() {
		assertTrue(CardinalRiteChecklist.inscription(0, 0, false)
				.contains("Required medium missing"));
	}

	@Test
	void internalWaveIdsBecomeReadableObjectives() {
		assertEquals("Trace within 18s or the rite fails",
				CardinalRiteChecklist.ordealObjective("response_sigil"));
		assertEquals("Defend the boundary from Bloodlickers",
				CardinalRiteChecklist.ordealObjective("bloodlicker_siphon"));
	}
}

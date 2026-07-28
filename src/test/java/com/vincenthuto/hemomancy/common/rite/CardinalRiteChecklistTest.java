package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteChecklistTest {
	@Test
	void inscriptionNamesOptionalPreparationAndDaemonProjectionAction() {
		List<String> lines = CardinalRiteChecklist.inscription(2, 1, 1, true);

		assertEquals("Optional sigils: 1/2", lines.get(0));
		assertTrue(lines.contains("Optional allies: 1 assigned"));
		assertTrue(lines.contains("Catalyst ready"));
		assertEquals("Project into the daemon to begin", lines.get(lines.size() - 1));
	}

	@Test
	void missingCatalystIsReportedAsABlocker() {
		assertTrue(CardinalRiteChecklist.inscription(0, 0, false)
				.contains("Required catalyst missing"));
	}

	@Test
	void internalWaveIdsBecomeReadableObjectives() {
		assertEquals("Trace the demanded response sigil",
				CardinalRiteChecklist.ordealObjective("response_sigil"));
		assertEquals("Defend the boundary from Bloodlickers",
				CardinalRiteChecklist.ordealObjective("bloodlicker_siphon"));
	}
}

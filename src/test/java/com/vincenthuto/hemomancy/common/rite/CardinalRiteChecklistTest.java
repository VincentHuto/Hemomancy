package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.support.EnglishText;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteChecklistTest {
    @Test
    void requiredPreparationDoesNotMasqueradeAsOptionalOrReady() {
        List<String> blocked = english(CardinalRiteChecklist.inscription(1, 1, 1, 0, 0, 1, true));
        assertTrue(blocked.contains("Required sigils: 0/1"));
        assertTrue(blocked.contains("Required allies: 0/1"));
        assertEquals("Fulfil required preparations before sealing", blocked.getLast());
        assertEquals("Project into the daemon to begin",
                english(CardinalRiteChecklist.inscription(1, 0, 1, 1, 1, 1, true)).getLast());
    }

	@Test
	void inscriptionNamesOptionalPreparationAndDaemonProjectionAction() {
		List<String> lines = english(CardinalRiteChecklist.inscription(2, 1, 1, true));

		assertEquals("Optional sigils: 1/2", lines.get(0));
		assertTrue(lines.contains("Optional allies: 1 assigned"));
		assertTrue(lines.contains("Focus ready"));
		assertEquals("Project into the daemon to begin", lines.get(lines.size() - 1));
	}

	@Test
	void missingCatalystIsReportedAsABlocker() {
		assertTrue(english(CardinalRiteChecklist.inscription(0, 0, false))
				.contains("Focus medium mismatch"));
	}

	@Test
	void internalWaveIdsBecomeReadableObjectives() {
		assertEquals("Trace within 18s or the rite fails",
				EnglishText.render(CardinalRiteChecklist.ordealObjective("response_sigil")));
		assertEquals("Defend the boundary from Bloodlickers",
				EnglishText.render(CardinalRiteChecklist.ordealObjective("bloodlicker_siphon")));
	}

	private static List<String> english(List<Component> lines) {
		return lines.stream().map(EnglishText::render).toList();
	}
}

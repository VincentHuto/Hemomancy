package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ScarsTabStateSelectionTest {
	@Test
	void clickingTheSelectedNodeAgainClosesItsDetails() {
		ScarsTabState state = new ScarsTabState();

		state.toggleSelection("hemomancy:scar_heart");
		assertEquals("hemomancy:scar_heart", state.selectedScarId());

		state.toggleSelection("hemomancy:scar_heart");
		assertNull(state.selectedScarId());
	}

	@Test
	void escapeOnlyConsumesInputWhenDetailsWereOpen() {
		ScarsTabState state = new ScarsTabState();
		assertFalse(state.closeDetails());

		state.toggleSelection("hemomancy:scar_heart");
		assertTrue(state.closeDetails());
		assertNull(state.selectedScarId());
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

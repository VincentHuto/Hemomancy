package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeveredQliphothStateTest {
	@Test
	void pruningOpensTheOrdealWithoutFinalizingTheTrophy() {
		SeveredQliphothState state = SeveredQliphothState.LIVING.sever();

		assertTrue(state.isPortalOpen());
		assertFalse(state.isSealedTrophy());
	}

	@Test
	void finalVesperVictorySealsThePortalPermanently() {
		SeveredQliphothState state = SeveredQliphothState.OPEN.seal();

		assertFalse(state.isPortalOpen());
		assertTrue(state.isSealedTrophy());
		assertTrue(state.seal().isSealedTrophy());
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnownManipulationsPassiveStateTest {
	@Test
	void passiveTogglePersistsAndCannotRemainActiveWhenUnequipped() {
		KnownManipulations known = new KnownManipulations();
		known.setEquippedManipNames(List.of("blackhearted"));

		assertTrue(known.togglePassive("blackhearted"));
		assertTrue(known.isPassiveActive("blackhearted"));

		KnownManipulations restored = new KnownManipulations();
		restored.deserializeNBT(null, known.serializeNBT(null));
		assertTrue(restored.isPassiveActive("blackhearted"));

		restored.setEquippedManipNames(List.of());
		assertFalse(restored.isPassiveActive("blackhearted"));
	}
}

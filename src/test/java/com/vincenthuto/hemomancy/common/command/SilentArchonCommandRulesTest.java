package com.vincenthuto.hemomancy.common.command;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class SilentArchonCommandRulesTest {
	@Test
	void pendingForcesDegreeSevenCompleteForcesEightAndClearOnlyRemovesThePath() {
		SilentArchonCommandRules.Transition pending = SilentArchonCommandRules.transition("pending", 3);
		assertEquals(7, pending.degreeNumber());
		assertEquals(EnumArchonPath.SILENT_PENDING, pending.archonPath());
		assertFalse(pending.clearLegacyChoice());

		SilentArchonCommandRules.Transition complete = SilentArchonCommandRules.transition("complete", 5);
		assertEquals(8, complete.degreeNumber());
		assertEquals(EnumArchonPath.SILENTIUM, complete.archonPath());
		assertFalse(complete.clearLegacyChoice());

		SilentArchonCommandRules.Transition cleared = SilentArchonCommandRules.transition("clear", 7);
		assertEquals(7, cleared.degreeNumber());
		assertEquals(EnumArchonPath.NONE, cleared.archonPath());
		assertTrue(cleared.clearLegacyChoice());
	}
}

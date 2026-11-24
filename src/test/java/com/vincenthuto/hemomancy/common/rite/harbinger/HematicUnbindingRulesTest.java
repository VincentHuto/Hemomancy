package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HematicUnbindingRulesTest {
	@AfterEach
	void clearWarnings() {
		HematicUnbindingRules.clearSessionState();
	}

	@Test
	void onlyASecondTimelyPerformanceForTheSameBloodlineConfirmsDissolution() {
		UUID line = UUID.randomUUID();
		assertEquals(HematicUnbindingRules.Decision.WARN,
				HematicUnbindingRules.decision(line, null, 0, 100));
		assertEquals(HematicUnbindingRules.Decision.CONFIRM,
				HematicUnbindingRules.decision(line, line, 500, 500));
		assertEquals(HematicUnbindingRules.Decision.WARN,
				HematicUnbindingRules.decision(line, line, 499, 500));
		assertEquals(HematicUnbindingRules.Decision.WARN,
				HematicUnbindingRules.decision(line, UUID.randomUUID(), 500, 200));
	}

	@Test
	void aLeaderWhoLogsOutIsWarnedAgain() {
		UUID leader = UUID.randomUUID();
		UUID line = UUID.randomUUID();
		assertEquals(HematicUnbindingRules.Decision.WARN, HematicUnbindingRules.activate(leader, line, 100));
		HematicUnbindingRules.logout(leader);
		assertEquals(HematicUnbindingRules.Decision.WARN, HematicUnbindingRules.activate(leader, line, 200),
				"the warning is session state and goes with the session");
	}

	@Test
	void theFirstActivationWarnsAndASecondInsideTheWindowStartsTheRiteOnce() {
		UUID leader = UUID.randomUUID();
		UUID line = UUID.randomUUID();
		assertEquals(HematicUnbindingRules.Decision.WARN, HematicUnbindingRules.activate(leader, line, 100));
		assertEquals(HematicUnbindingRules.Decision.CONFIRM,
				HematicUnbindingRules.activate(leader, line, 100 + HematicUnbindingRules.CONFIRMATION_TICKS));
		assertEquals(HematicUnbindingRules.Decision.WARN, HematicUnbindingRules.activate(leader, line, 200),
				"a started rite spends the warning");
	}

	@Test
	void anExpiredWarningOrAnotherLineWarnsAgain() {
		UUID leader = UUID.randomUUID();
		UUID line = UUID.randomUUID();
		HematicUnbindingRules.activate(leader, line, 0);
		assertEquals(HematicUnbindingRules.Decision.WARN,
				HematicUnbindingRules.activate(leader, line, HematicUnbindingRules.CONFIRMATION_TICKS + 1));
		assertEquals(HematicUnbindingRules.Decision.WARN,
				HematicUnbindingRules.activate(leader, UUID.randomUUID(), HematicUnbindingRules.CONFIRMATION_TICKS + 2));
		assertEquals(HematicUnbindingRules.Decision.WARN,
				HematicUnbindingRules.activate(UUID.randomUUID(), line, HematicUnbindingRules.CONFIRMATION_TICKS + 3),
				"another player's activation never confirms this leader's warning");
	}
}

package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HematicUnbindingRulesTest {
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
}

package com.vincenthuto.hemomancy.common.entity.summon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaleIntercessionRulesTest {
	@Test
	void castAndCooldownTimingMatchThePlayerContract() {
		assertEquals(1200, PaleIntercessionRules.DURATION_TICKS);
		assertEquals(600, PaleIntercessionRules.COOLDOWN_TICKS);
	}
	@Test
	void hostileDamageConsumesAtLeastOneSecond() {
		assertEquals(1180, PaleIntercessionRules.remainingAfterDamage(1200, 0.1f));
	}

	@Test
	void hostileDamageConsumesOneSecondPerDamagePoint() {
		assertEquals(1089, PaleIntercessionRules.remainingAfterDamage(1200, 5.55f));
	}

	@Test
	void durationCannotBecomeNegative() {
		assertEquals(0, PaleIntercessionRules.remainingAfterDamage(30, 9.0f));
	}

	@Test
	void nonPositiveDamageDoesNotConsumeTime() {
		assertEquals(1200, PaleIntercessionRules.remainingAfterDamage(1200, 0.0f));
		assertEquals(1200, PaleIntercessionRules.remainingAfterDamage(1200, -2.0f));
	}
}

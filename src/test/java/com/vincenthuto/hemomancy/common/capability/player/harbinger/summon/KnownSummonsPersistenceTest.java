package com.vincenthuto.hemomancy.common.capability.player.harbinger.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnownSummonsPersistenceTest {
	@Test
	void learnedTrialUnlockRoundTripsThroughNbt() {
		PuppeteerSummonDefinition vulture = PuppeteerSummonDefinitions
				.byName(PuppeteerSummonDefinitions.VEINWING_VULTURE).orElseThrow();
		KnownSummons original = new KnownSummons();
		assertTrue(original.learn(vulture));
		assertFalse(original.learn(vulture), "duplicate learning must be idempotent");

		KnownSummons restored = new KnownSummons();
		restored.deserializeNBT(null, original.serializeNBT(null));

		assertTrue(restored.isKnown(vulture));
		assertFalse(restored.learn(vulture), "round-tripped unlock must remain unique");
	}
}

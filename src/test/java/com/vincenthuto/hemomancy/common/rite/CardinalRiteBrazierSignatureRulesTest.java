package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteBrazierSignatureRulesTest {
	@Test
	void offeringsMatchAsExactUnorderedMultiset() {
		Map<String, Integer> required = Map.of("hemomancy:cruor", 2, "minecraft:bone", 1);

		assertTrue(CardinalRiteBrazierSignatureRules.exactMatch(
				required, Map.of("minecraft:bone", 1, "hemomancy:cruor", 2)));
		assertFalse(CardinalRiteBrazierSignatureRules.exactMatch(
				required, Map.of("hemomancy:cruor", 2)));
		assertFalse(CardinalRiteBrazierSignatureRules.exactMatch(
				required, Map.of("minecraft:bone", 1, "hemomancy:cruor", 2, "minecraft:string", 1)));
	}
}

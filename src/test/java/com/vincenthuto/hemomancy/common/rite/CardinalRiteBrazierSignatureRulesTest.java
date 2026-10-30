package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBrazierSignatureRules.Assignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteBrazierSignatureRulesTest {
	private static final BiPredicate<String, String> SAME = String::equals;

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

	@Test
	void aSignatureRiteFillsEverySlotFromEveryBrazier() {
		List<Assignment> assigned = CardinalRiteBrazierSignatureRules.assign(
				List.of("bone", "cruor"), List.of(), List.of("cruor", "bone"), SAME);

		assertEquals(List.of(new Assignment(0, 1), new Assignment(1, 0)), assigned);
	}

	@Test
	void aSignatureRiteStillRefusesAnExtraOrMissingBrazier() {
		assertNull(CardinalRiteBrazierSignatureRules.assign(
				List.of("bone", "cruor"), List.of(), List.of("cruor", "bone", "ambergris"), SAME));
		assertNull(CardinalRiteBrazierSignatureRules.assign(
				List.of("bone", "cruor"), List.of(), List.of("cruor"), SAME));
		assertNull(CardinalRiteBrazierSignatureRules.assign(
				List.of("bone", "cruor"), List.of(), List.of("cruor", "string"), SAME));
	}

	@Test
	void aRiteWithNoSignatureAndNoOptionalOfferingsWantsNoLitBrazier() {
		assertEquals(List.of(), CardinalRiteBrazierSignatureRules.assign(List.of(), List.of(), List.of(), SAME));
		assertNull(CardinalRiteBrazierSignatureRules.assign(List.of(), List.of(), List.of("bone"), SAME));
	}

	@Test
	void optionalOfferingsMatchTwoOfFourBraziersAndIgnoreTheRest() {
		List<String> optional = List.of("ambergris", "iron", "totem", "adipocere");

		List<Assignment> assigned = CardinalRiteBrazierSignatureRules.assign(
				List.of(), optional, List.of("dirt", "adipocere", "ambergris", "bone"), SAME);

		assertEquals(List.of(new Assignment(2, 0), new Assignment(1, 3)), assigned);
	}

	@Test
	void optionalOfferingsMatchWithNoBraziersAtAll() {
		assertEquals(List.of(), CardinalRiteBrazierSignatureRules.assign(
				List.of(), List.of("ambergris"), List.of(), SAME));
		assertEquals(List.of(), CardinalRiteBrazierSignatureRules.assign(
				List.of(), List.of("ambergris"), List.of("dirt"), SAME));
	}

	@Test
	void eachOptionalOfferingBurnsOneBrazierOnly() {
		List<Assignment> assigned = CardinalRiteBrazierSignatureRules.assign(
				List.of(), List.of("ambergris"), List.of("ambergris", "ambergris"), SAME);

		assertEquals(List.of(new Assignment(0, 0)), assigned);
	}
}

package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustLineageRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BloodLustLineageRulesTest {
	@Test
	void uniformLineageRequiresFourMatchingKnownPieces() {
		assertEquals("barbed", BloodLustLineageRules.uniformLineage(
				List.of("barbed", "barbed", "barbed", "barbed")));
		assertEquals("", BloodLustLineageRules.uniformLineage(
				List.of("barbed", "barbed", "chitinite", "barbed")));
		assertEquals("", BloodLustLineageRules.uniformLineage(
				List.of("unknown", "unknown", "unknown", "unknown")));
	}

	@Test
	void platingLevelsFollowTheBloodLustHierarchy() {
		assertEquals(1, BloodLustLineageRules.platingLevel("prismatic"));
		assertEquals(2, BloodLustLineageRules.platingLevel("barbed"));
		assertEquals(3, BloodLustLineageRules.platingLevel("chitinite"));
		assertEquals(3, BloodLustLineageRules.platingLevel(""));
	}

	@Test
	void inheritedTraitsAreHalfStrength() {
		assertEquals(new BloodLustLineageRules.InheritedTrait(1, 30, 40, 0, 0, 0, 0, 0),
				BloodLustLineageRules.inheritedTrait("barbed"));
		assertEquals(new BloodLustLineageRules.InheritedTrait(0, 0, 0, 1, .125f, 0, 0, 0),
				BloodLustLineageRules.inheritedTrait("chitinite"));
		assertEquals(new BloodLustLineageRules.InheritedTrait(0, 0, 0, 0, 0, 30, 20, 40),
				BloodLustLineageRules.inheritedTrait("prismatic"));
		assertEquals(BloodLustLineageRules.InheritedTrait.NONE,
				BloodLustLineageRules.inheritedTrait(""));
	}
}

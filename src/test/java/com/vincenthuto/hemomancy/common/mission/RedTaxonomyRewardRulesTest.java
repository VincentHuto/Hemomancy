package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.RedTaxonomyRewardRules;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RedTaxonomyRewardRulesTest {

	@Test
	void firstUniqueSubmissionGrantsFieldVial() {
		assertTrue(RedTaxonomyRewardRules.grantsFirstFieldVial(0, true));
	}

	@Test
	void repeatAndLaterSubmissionsDoNotGrantAnotherFieldVial() {
		assertFalse(RedTaxonomyRewardRules.grantsFirstFieldVial(0, false));
		assertFalse(RedTaxonomyRewardRules.grantsFirstFieldVial(1, true));
		assertFalse(RedTaxonomyRewardRules.grantsFirstFieldVial(3, true));
	}
}

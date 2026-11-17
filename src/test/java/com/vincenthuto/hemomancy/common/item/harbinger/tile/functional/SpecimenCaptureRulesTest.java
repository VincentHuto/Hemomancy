package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenCaptureRules.Candidate;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenCaptureRules.Verdict;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 0: what an empty Specimen Jar refuses beyond the capturable tag. */
class SpecimenCaptureRulesTest {
	private static Candidate creature() {
		return new Candidate(true, true, 0.6F, 0.5F, 12.0F, false, false, false);
	}

	@Test
	void aSmallListedLivingCreatureIsCaptured() {
		assertEquals(Verdict.CAPTURE, SpecimenCaptureRules.judge(creature()));
	}

	@Test
	void anUnlistedOrDeadCreatureIsIgnoredWithoutAWord() {
		assertEquals(Verdict.NOT_A_SPECIMEN,
				SpecimenCaptureRules.judge(new Candidate(false, true, 0.6F, 0.5F, 12.0F, false, false, false)));
		assertEquals(Verdict.DEAD,
				SpecimenCaptureRules.judge(new Candidate(true, false, 0.6F, 0.5F, 12.0F, false, false, false)));
		assertNull(Verdict.NOT_A_SPECIMEN.messageKey());
		assertNull(Verdict.DEAD.messageKey());
	}

	@Test
	void bossesOwnedCreaturesAndMountsAreRefused() {
		assertEquals(Verdict.BOSS,
				SpecimenCaptureRules.judge(new Candidate(true, true, 0.6F, 0.5F, 12.0F, true, false, false)));
		assertEquals(Verdict.OWNED,
				SpecimenCaptureRules.judge(new Candidate(true, true, 0.6F, 0.5F, 12.0F, false, true, false)));
		assertEquals(Verdict.CARRYING_RIDERS,
				SpecimenCaptureRules.judge(new Candidate(true, true, 0.6F, 0.5F, 12.0F, false, false, true)));
	}

	@Test
	void sizeAndStrengthHaveCeilings() {
		assertEquals(Verdict.TOO_LARGE, SpecimenCaptureRules.judge(new Candidate(true, true,
				SpecimenCaptureRules.MAX_WIDTH + 0.1F, 0.5F, 12.0F, false, false, false)));
		assertEquals(Verdict.TOO_LARGE, SpecimenCaptureRules.judge(new Candidate(true, true, 0.6F,
				SpecimenCaptureRules.MAX_HEIGHT + 0.1F, 12.0F, false, false, false)));
		assertEquals(Verdict.TOO_STRONG, SpecimenCaptureRules.judge(new Candidate(true, true, 0.6F, 0.5F,
				SpecimenCaptureRules.MAX_HEALTH + 1.0F, false, false, false)));
		assertEquals(Verdict.CAPTURE, SpecimenCaptureRules.judge(new Candidate(true, true,
				SpecimenCaptureRules.MAX_WIDTH, SpecimenCaptureRules.MAX_HEIGHT, SpecimenCaptureRules.MAX_HEALTH,
				false, false, false)), "the ceilings themselves still fit");
	}

	@Test
	void everyRefusalTheJarSpeaksHasALangKey() {
		for (Verdict verdict : Verdict.values()) {
			if (verdict == Verdict.CAPTURE || verdict == Verdict.NOT_A_SPECIMEN || verdict == Verdict.DEAD) continue;
			assertTrue(verdict.messageKey().startsWith("message.hemomancy.specimen_jar.refused."), verdict.name());
		}
	}
}

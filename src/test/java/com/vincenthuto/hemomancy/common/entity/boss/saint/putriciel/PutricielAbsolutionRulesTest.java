package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules.Act;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules.Glyph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The five absolutions (design sections 6 and 10, resolution D-4). */
final class PutricielAbsolutionRulesTest {
	@Test
	void aHitOfFourAbsolves() {
		assertTrue(PutricielAbsolutionRules.hitQualifies(4.0F));
		assertFalse(PutricielAbsolutionRules.hitQualifies(3.9F));
	}

	@Test
	void aProjectionPoursAHundredAtTheItemsRateCappedAtFive() {
		double poured = 0.0D;
		int ticks = 0;
		while (!PutricielAbsolutionRules.pourComplete(poured)) {
			poured += PutricielAbsolutionRules.pourStep(8.0D, poured);
			ticks++;
		}
		assertEquals(20, ticks, "a fast item still takes 20 ticks: the palm takes at most 5 a tick");
		assertEquals(2.0D, PutricielAbsolutionRules.pourStep(2.0D, 0.0D), 1.0E-9D, "never more than the item moves");
		assertEquals(3.0D, PutricielAbsolutionRules.pourStep(5.0D, 97.0D), 1.0E-9D);
	}

	@Test
	void offeringLengths() {
		assertEquals(50, PutricielAbsolutionRules.offeringTicks(false, false));
		assertEquals(60, PutricielAbsolutionRules.offeringTicks(false, true));
		assertEquals(60, PutricielAbsolutionRules.offeringTicks(true, false), "Litany keeps 60-tick Offerings (D-4)");
	}

	@Test
	void theFullPurgeComesBeforeTheFifthOnceAnInterval() {
		assertFalse(PutricielAbsolutionRules.fullPurgeDue(3, false));
		assertTrue(PutricielAbsolutionRules.fullPurgeDue(4, false));
		assertFalse(PutricielAbsolutionRules.fullPurgeDue(4, true));
		assertTrue(PutricielAbsolutionRules.done(5));
		assertFalse(PutricielAbsolutionRules.done(4));
	}

	@Test
	void theGlyphNamesOneActAndNeverRepeatsThreeTimes() {
		assertTrue(Glyph.NONE.accepts(Act.STRIKE) && Glyph.NONE.accepts(Act.POUR));
		assertTrue(Glyph.STRIKE.accepts(Act.STRIKE) && !Glyph.STRIKE.accepts(Act.POUR));
		assertTrue(Glyph.POUR.accepts(Act.POUR) && !Glyph.POUR.accepts(Act.STRIKE));
		Glyph before = Glyph.NONE, last = Glyph.NONE;
		for (int roll = 0; roll < 200; roll++) {
			Glyph next = PutricielAbsolutionRules.nextGlyph(last, before, roll * 2);
			if (last == before && last != Glyph.NONE) assertNotEquals(last, next);
			before = last;
			last = next;
		}
	}
}

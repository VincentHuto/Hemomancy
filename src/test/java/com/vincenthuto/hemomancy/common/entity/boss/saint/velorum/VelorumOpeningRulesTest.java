package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpenings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumOpeningRulesTest {
	@Test
	void theGutterShortensByPhaseAndHalvesAfterTheHour() {
		assertEquals(60, VelorumOpeningRules.gutterTicks(1, false));
		assertEquals(50, VelorumOpeningRules.gutterTicks(2, false));
		assertEquals(40, VelorumOpeningRules.gutterTicks(3, false));
		assertEquals(20, VelorumOpeningRules.gutterTicks(3, true));
		assertEquals(VelorumOpeningRules.GUTTER_ID, VelorumOpeningRules.gutter(25).id());
		assertEquals(25, VelorumOpeningRules.gutter(25).ticks());
	}

	@Test
	void theGuttersNewEntriesAndTheVeinsOverheld() {
		// the Eclipse relit opens a 60-tick Gutter whatever the phase; unbroken, a half one
		assertEquals(60, VelorumEclipseRules.gutterTicks(3, true));
		assertEquals(VelorumOpeningRules.gutterTicks(3, true), VelorumEclipseRules.gutterTicks(3, false));
		// Veins of Frost overholds by the invocation's own rule, over its 36 ticks of wind-up and active span
		assertEquals(36, VelorumMove.VEINS_OF_FROST.authored().windup() + VelorumMove.VEINS_OF_FROST.authored().active());
		assertTrue(VelorumOpeningRules.overheld(3, 0.0F, 640.0F));
		assertTrue(VelorumOpeningRules.overheld(0, 25.6F, 640.0F));
		assertFalse(VelorumOpeningRules.overheld(2, 25.0F, 640.0F));
	}

	@Test
	void theBowAndTheReposeDeedAdmitTheSameDamage() {
		assertEquals(70, VelorumOpeningRules.bowTicks(false, false));
		assertEquals(100, VelorumOpeningRules.bowTicks(false, true));
		assertEquals(40, VelorumOpeningRules.bowTicks(true, true));
		SaintOpening.Spec bow = VelorumOpeningRules.bow(false), repose = VelorumOpeningRules.bow(true);
		assertEquals(bow.ticks() * bow.damageTaken(), repose.ticks() * repose.damageTaken(), 1.0E-3F);
		assertEquals(VelorumOpeningRules.BOW_ID, VelorumOpeningRules.bow(100, false).id());
		assertEquals(1.5F, VelorumOpeningRules.bow(100, false).damageTaken());
	}

	@Test
	void theOverheldInvocationAndTheChainIntoTheBow() {
		assertFalse(VelorumOpeningRules.overheld(2, 25.0F, 640.0F));
		assertTrue(VelorumOpeningRules.overheld(3, 0.0F, 640.0F));
		assertTrue(VelorumOpeningRules.overheld(1, 25.6F, 640.0F));
		assertTrue(VelorumOpeningRules.chainsToBow(VelorumOpeningRules.GUTTER_ID, 38.4F, 640.0F));
		assertTrue(VelorumOpeningRules.chainsToBow(VelorumOpeningRules.OVERHELD_ID, 38.4F, 640.0F));
		assertFalse(VelorumOpeningRules.chainsToBow(VelorumOpeningRules.BOW_ID, 100.0F, 640.0F));
		assertFalse(VelorumOpeningRules.chainsToBow(VelorumOpeningRules.GUTTER_ID, 38.0F, 640.0F));
	}

	@Test
	void theHourBreaksAtEightPercentWithSearingDouble() {
		assertFalse(VelorumOpeningRules.hourBreaks(51.0F, 640.0F));
		assertTrue(VelorumOpeningRules.hourBreaks(51.2F, 640.0F));
		assertEquals(20.0F, VelorumOpeningRules.hourWeight(10.0F, true));
		assertEquals(10.0F, VelorumOpeningRules.hourWeight(10.0F, false));
	}

	@Test
	void projectionStretchesTheGutterOneTickPerFourFedUpToTwenty() {
		assertEquals(0, VelorumOpeningRules.projectionExtension(3));
		assertEquals(1, VelorumOpeningRules.projectionExtension(4));
		assertEquals(20, VelorumOpeningRules.projectionExtension(200));
	}

	@Test
	void oneWindowAtATimeAndOnlyIntoTheBow() {
		SaintOpenings openings = new SaintOpenings();
		assertTrue(openings.open(VelorumOpeningRules.GUTTER, 60));
		assertFalse(openings.open(VelorumOpeningRules.OVERHELD, 50));
		openings.chainTo(VelorumOpeningRules.BOW, 70);
		assertTrue(openings.isOpen(VelorumOpeningRules.BOW));
		assertEquals(1.5F, openings.damageTaken());
		assertEquals(20, openings.extend(30, 20));
	}
}

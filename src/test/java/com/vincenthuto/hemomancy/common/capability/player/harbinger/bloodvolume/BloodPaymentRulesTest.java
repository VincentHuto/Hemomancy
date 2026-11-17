package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodPaymentRules.Outcome;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodPaymentRules.Terms;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 7: every flag combination of a blood payment. A volume of 1000 / 1000 has a Sanguine Reserve floor of 150, so
 * 900 asked is "covered" (1000 held), "reserve-limited" (only 850 above the floor) and never "overstrained".
 */
class BloodPaymentRulesTest {
	private static final Terms UPKEEP = new Terms(true, false);
	private static final Terms UPKEEP_FULL = new Terms(true, true);
	private static final Terms[] ALL_TERMS = { Terms.ONE_OFF, new Terms(false, true), UPKEEP, UPKEEP_FULL };

	private static BloodVolume volume(double blood, boolean active) {
		BloodVolume volume = new BloodVolume();
		volume.setMaxBloodVolume(1000.0D);
		volume.setBloodVolume(blood);
		volume.setActive(active);
		return volume;
	}

	private static Outcome settle(BloodVolume volume, double amount, boolean reserveProtected, boolean reserveEnabled,
			Terms terms) {
		return BloodPaymentRules.settle(volume, amount, reserveProtected, reserveEnabled, terms);
	}

	@Test
	void aCoveredPaymentIsTheSameUnderEveryFlag() {
		for (Terms terms : ALL_TERMS) {
			for (boolean reserveProtected : new boolean[] { false, true }) {
				for (boolean reserveEnabled : new boolean[] { false, true }) {
					BloodVolume volume = volume(1000.0D, true);
					Outcome outcome = settle(volume, 200.0D, reserveProtected, reserveEnabled, terms);
					String flags = terms + " protected=" + reserveProtected + " enabled=" + reserveEnabled;
					assertEquals(800.0D, volume.getBloodVolume(), flags);
					assertEquals(200.0D, outcome.paid(), flags);
					assertTrue(outcome.satisfied(), flags);
					assertEquals("", outcome.condition(), flags);
				}
			}
		}
	}

	@Test
	void aOneOffOverstrainEmptiesTheVolumeLikeDrain() {
		BloodVolume volume = volume(100.0D, true);
		Outcome outcome = settle(volume, 250.0D, false, false, Terms.ONE_OFF);
		assertEquals(0.0D, volume.getBloodVolume(), "drain() empties the volume on overstrain");
		assertEquals(100.0D, outcome.paid());
		assertFalse(outcome.satisfied(), "drain() reports overstrain as failure");
		assertEquals(BloodPaymentRules.EXHAUSTED, outcome.condition());
	}

	@Test
	void anUpkeepWithoutFullAmountAlsoEmptiesTheVolume() {
		BloodVolume volume = volume(100.0D, true);
		Outcome outcome = settle(volume, 250.0D, false, false, UPKEEP);
		assertEquals(0.0D, volume.getBloodVolume());
		assertEquals(100.0D, outcome.paid());
		assertFalse(outcome.satisfied());
	}

	@Test
	void requiringTheFullAmountRefusesAShortPaymentUntouched() {
		for (Terms terms : new Terms[] { new Terms(false, true), UPKEEP_FULL }) {
			BloodVolume volume = volume(100.0D, true);
			Outcome outcome = settle(volume, 250.0D, false, false, terms);
			assertEquals(100.0D, volume.getBloodVolume(), terms.toString());
			assertEquals(0.0D, outcome.paid());
			assertFalse(outcome.satisfied());
			assertEquals(BloodPaymentRules.INSUFFICIENT, outcome.condition());
		}
	}

	@Test
	void theReserveLimitsOnlyAProtectedSourceWithTheTechniqueEnabled() {
		BloodVolume unprotected = volume(1000.0D, true);
		assertTrue(settle(unprotected, 900.0D, false, true, Terms.ONE_OFF).satisfied());
		assertEquals(100.0D, unprotected.getBloodVolume(), "an unprotected source ignores the reserve");

		BloodVolume techniqueOff = volume(1000.0D, true);
		assertTrue(settle(techniqueOff, 900.0D, true, false, Terms.ONE_OFF).satisfied());
		assertEquals(100.0D, techniqueOff.getBloodVolume(), "the reserve needs the technique enabled");

		BloodVolume limited = volume(1000.0D, true);
		Outcome outcome = settle(limited, 900.0D, true, true, Terms.ONE_OFF);
		assertEquals(150.0D, limited.getBloodVolume(), "a protected payment stops at the 15% floor");
		assertEquals(850.0D, outcome.paid());
		assertFalse(outcome.satisfied());
		assertEquals(BloodPaymentRules.RESERVE_PROTECTED, outcome.condition());
	}

	@Test
	void requiringTheFullAmountRefusesAReserveLimitedPayment() {
		BloodVolume volume = volume(1000.0D, true);
		Outcome outcome = settle(volume, 900.0D, true, true, UPKEEP_FULL);
		assertEquals(1000.0D, volume.getBloodVolume());
		assertEquals(0.0D, outcome.paid());
		assertEquals(BloodPaymentRules.RESERVE_PROTECTED, outcome.condition());
	}

	@Test
	void atTheFloorAProtectedPaymentTakesNothing() {
		BloodVolume volume = volume(150.0D, true);
		Outcome outcome = settle(volume, 50.0D, true, true, Terms.ONE_OFF);
		assertEquals(150.0D, volume.getBloodVolume());
		assertEquals(0.0D, outcome.paid());
		assertFalse(outcome.satisfied());
		assertEquals(BloodPaymentRules.RESERVE_PROTECTED, outcome.condition());
	}

	@Test
	void onlyUpkeepTermsSkipADormantVolume() {
		BloodVolume oneOff = volume(500.0D, false);
		assertTrue(settle(oneOff, 100.0D, false, false, Terms.ONE_OFF).satisfied());
		assertEquals(400.0D, oneOff.getBloodVolume(), "a one-off payment never asked isActive()");

		for (Terms terms : new Terms[] { UPKEEP, UPKEEP_FULL }) {
			BloodVolume dormant = volume(500.0D, false);
			Outcome outcome = settle(dormant, 100.0D, true, true, terms);
			assertEquals(500.0D, dormant.getBloodVolume(), terms.toString());
			assertEquals(BloodPaymentRules.DORMANT, outcome.condition());
		}
	}

	@Test
	void sourcesAreLabelledByTheirIdKey() {
		BloodAccounting.Source source = BloodAccounting.Source.of("living_staff_hot_swap", Category.TOOL, false);
		assertEquals("blood_source.hemomancy.living_staff_hot_swap", source.label());
		assertEquals("Living Staff Hot Swap", EnglishText.of(source.label()));
		assertFalse(source.reserveProtected());
	}
}

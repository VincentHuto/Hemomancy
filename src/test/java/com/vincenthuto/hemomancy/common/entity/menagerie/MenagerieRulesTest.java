package com.vincenthuto.hemomancy.common.entity.menagerie;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core-loop Track 5: one rule for who may be kept, by leash, jar or vat, and the numbers of keeping. */
class MenagerieRulesTest {
	/** A candidate with every flag false except the ones named by position: alive, mob, then the refusals. */
	private static MenagerieRules.Candidate candidate(boolean alive, boolean mob, boolean player, boolean boss,
			boolean owned, boolean summoned, boolean will, boolean person, boolean saint, boolean warden) {
		return new MenagerieRules.Candidate(alive, mob, player, boss, owned, summoned, will, person, saint, warden, false);
	}

	@Test
	void aKindInTheUnkeepableTagIsRefusedEvenWithNoOtherReason() {
		assertEquals(MenagerieRules.Verdict.UNKEEPABLE, MenagerieRules.canKeep(new MenagerieRules.Candidate(
				true, true, false, false, false, false, false, false, false, false, true)));
		assertEquals(MenagerieRules.Verdict.WILL, MenagerieRules.canKeep(new MenagerieRules.Candidate(
				true, true, false, false, false, false, true, false, false, false, true)), "a tagged Will keeps its own line");
		assertEquals(MenagerieRules.Verdict.PERSON, MenagerieRules.canKeep(new MenagerieRules.Candidate(
				true, true, false, false, false, false, false, true, false, false, true)), "a tagged person keeps its own line");
	}

	@Test
	void anOrdinaryLivingCreatureMayBeKept() {
		assertEquals(MenagerieRules.Verdict.KEEP,
				MenagerieRules.canKeep(candidate(true, true, false, false, false, false, false, false, false, false)));
	}

	@Test
	void eachRefusalHasItsOwnVerdictAndLine() {
		assertEquals(MenagerieRules.Verdict.PLAYER,
				MenagerieRules.canKeep(candidate(true, false, true, false, false, false, false, false, false, false)));
		assertEquals(MenagerieRules.Verdict.BOSS,
				MenagerieRules.canKeep(candidate(true, true, false, true, false, false, false, false, false, false)));
		assertEquals(MenagerieRules.Verdict.OWNED,
				MenagerieRules.canKeep(candidate(true, true, false, false, true, false, false, false, false, false)));
		assertEquals(MenagerieRules.Verdict.SUMMONED,
				MenagerieRules.canKeep(candidate(true, true, false, false, false, true, false, false, false, false)));
		assertEquals(MenagerieRules.Verdict.WILL,
				MenagerieRules.canKeep(candidate(true, true, false, false, false, false, true, false, false, false)));
		assertEquals(MenagerieRules.Verdict.PERSON,
				MenagerieRules.canKeep(candidate(true, true, false, false, false, false, false, true, false, false)));
		assertEquals(MenagerieRules.Verdict.SAINT,
				MenagerieRules.canKeep(candidate(true, true, false, false, false, false, false, false, true, false)));
		assertEquals(MenagerieRules.Verdict.WARDEN,
				MenagerieRules.canKeep(candidate(true, true, false, false, false, false, false, false, false, true)));
		for (MenagerieRules.Verdict verdict : MenagerieRules.Verdict.values()) {
			if (verdict == MenagerieRules.Verdict.KEEP || verdict == MenagerieRules.Verdict.DEAD
					|| verdict == MenagerieRules.Verdict.NOT_A_CREATURE) {
				assertNull(verdict.messageKey(), verdict.name());
			} else {
				assertEquals("message.hemomancy.menagerie.refused." + verdict.name().toLowerCase(Locale.ROOT),
						verdict.messageKey());
			}
		}
	}

	@Test
	void aBossThatIsAlsoOwnedIsRefusedAsABoss() {
		assertEquals(MenagerieRules.Verdict.BOSS,
				MenagerieRules.canKeep(candidate(true, true, false, true, true, false, false, false, false, false)));
	}

	@Test
	void theDeadAndTheMindlessAreNotKeptAndSayNothing() {
		assertEquals(MenagerieRules.Verdict.DEAD,
				MenagerieRules.canKeep(candidate(false, true, false, false, false, false, false, false, false, false)));
		assertEquals(MenagerieRules.Verdict.NOT_A_CREATURE,
				MenagerieRules.canKeep(candidate(true, false, false, false, false, false, false, false, false, false)));
	}

	@Test
	void aHostileCreatureMustBeWornToAThird() {
		assertTrue(MenagerieRules.weakEnough(20.0F, 20.0F, false), "a passive creature at full health");
		assertFalse(MenagerieRules.weakEnough(20.0F, 20.0F, true));
		assertFalse(MenagerieRules.weakEnough(7.0F, 20.0F, true));
		assertTrue(MenagerieRules.weakEnough(20.0F / 3.0F, 20.0F, true));
		assertTrue(MenagerieRules.weakEnough(4.0F, 20.0F, true));
	}

	@Test
	void ledCreaturesByDegreeFollowCl13() {
		assertEquals(0, MenagerieRules.maxLed(2));
		assertEquals(1, MenagerieRules.maxLed(3));
		assertEquals(1, MenagerieRules.maxLed(4));
		assertEquals(2, MenagerieRules.maxLed(5));
		assertEquals(2, MenagerieRules.maxLed(6));
		assertEquals(3, MenagerieRules.maxLed(7));
		assertEquals(3, MenagerieRules.maxLed(8));
	}

	@Test
	void upkeepScalesWithTheBodyAndNeverFallsUnderOneMl() {
		assertEquals(5.0D, MenagerieRules.upkeepPerSecond(20.0F), 1.0E-9D, "a blaze");
		assertEquals(2.5D, MenagerieRules.upkeepPerSecond(10.0F), 1.0E-9D, "a cow");
		assertEquals(1.0D, MenagerieRules.upkeepPerSecond(2.0F), 1.0E-9D, "a small creature");
	}

	@Test
	void aVesselHoldsWhatFitsItsClearInsideAndFortyHealthAVat() {
		assertTrue(MenagerieRules.vatFits(0.6F, 1.8F, 20.0F, 1, 1), "a blaze in one vat");
		assertTrue(MenagerieRules.vatFits(0.9F, 1.4F, 10.0F, 1, 1), "a cow in one vat");
		assertTrue(MenagerieRules.vatFits(0.6F, 1.95F, 20.0F, 1, 1), "a zombie in one vat");
		assertFalse(MenagerieRules.vatFits(1.4F, 1.6F, 30.0F, 1, 1), "a horse is too wide for one vat");
		assertTrue(MenagerieRules.vatFits(1.4F, 1.6F, 30.0F, 2, 1), "a horse in a vessel two across");
		assertFalse(MenagerieRules.vatFits(0.6F, 2.9F, 40.0F, 1, 1), "an enderman is too tall for one vat");
		assertTrue(MenagerieRules.vatFits(0.6F, 2.9F, 40.0F, 1, 2), "an enderman in two vats stacked");
		assertFalse(MenagerieRules.vatFits(0.9F, 1.4F, 41.0F, 1, 1), "too strong for one vat");
		assertTrue(MenagerieRules.vatFits(0.9F, 1.4F, 41.0F, 1, 2), "two vats hold twice the strength");
		assertFalse(MenagerieRules.vatFits(1.95F, 2.2F, 100.0F, 2, 1), "a ravager is too tall for one layer");
		assertTrue(MenagerieRules.vatFits(1.95F, 2.2F, 100.0F, 2, 2), "a ravager in two by two by two");
		assertFalse(MenagerieRules.vatFits(4.0F, 4.0F, 10.0F, 3, 2), "a ghast fits nothing");
	}

	@Test
	void theSmallestVatIsTheOneWithFewestVats() {
		assertArrayEquals(new int[]{1, 1}, MenagerieRules.smallestVat(0.9F, 1.4F, 10.0F), "a cow");
		assertArrayEquals(new int[]{2, 1}, MenagerieRules.smallestVat(1.4F, 1.6F, 30.0F), "a horse");
		assertArrayEquals(new int[]{1, 2}, MenagerieRules.smallestVat(0.6F, 2.9F, 40.0F), "an enderman");
		assertArrayEquals(new int[]{2, 2}, MenagerieRules.smallestVat(1.95F, 2.2F, 100.0F), "a ravager");
		assertNull(MenagerieRules.smallestVat(4.0F, 4.0F, 10.0F), "a ghast");
	}
}

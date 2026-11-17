package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 12: the jet's pressure climbs on one target, bleeds off the aim, and pierces at full. */
class JetRulesTest {
	@Test
	void pressureFillsOverTwoSecondsOnOneTarget() {
		JetRules.State state = new JetRules.State();
		JetRules.tick(state, 7);
		for (int tick = 0; tick < JetRules.FILL_TICKS; tick++) JetRules.tick(state, 7);
		assertEquals(1.0D, state.pressure(), 1.0E-9D);
		assertTrue(JetRules.pierces(state.pressure()));
	}

	@Test
	void aNewTargetStartsFromNothing() {
		JetRules.State state = new JetRules.State();
		for (int tick = 0; tick < 30; tick++) JetRules.tick(state, 7);
		JetRules.tick(state, 9);
		assertEquals(0.0D, state.pressure(), 1.0E-9D);
		assertEquals(9, state.target());
	}

	@Test
	void lookingAwayBleedsThePressureQuickly() {
		JetRules.State state = new JetRules.State();
		for (int tick = 0; tick < 50; tick++) JetRules.tick(state, 7);
		for (int tick = 0; tick < 3; tick++) JetRules.tick(state, -1);
		assertTrue(state.pressure() < 1.0D && state.pressure() > 0.0D);
		JetRules.tick(state, 7);
		assertTrue(state.pressure() > 0.6D, "a glance away kept most of the pressure on the same target");
		for (int tick = 0; tick < 10; tick++) JetRules.tick(state, -1);
		assertEquals(0.0D, state.pressure(), 1.0E-9D);
		assertEquals(-1, state.target());
	}

	@Test
	void damageClimbsWithPressure() {
		assertEquals(JetRules.BASE_DAMAGE, JetRules.pulseDamage(0.0D), 1.0E-6F);
		assertEquals(JetRules.FULL_DAMAGE, JetRules.pulseDamage(1.0D), 1.0E-6F);
		assertTrue(JetRules.pulseDamage(0.5D) > JetRules.BASE_DAMAGE);
		assertFalse(JetRules.pierces(0.99D));
		assertTrue(JetRules.snapDamage(1.0D) > JetRules.snapDamage(0.0D));
	}

	@Test
	void aSnapSpendsThePressure() {
		JetRules.State state = new JetRules.State();
		for (int tick = 0; tick < 50; tick++) JetRules.tick(state, 3);
		JetRules.spend(state);
		assertEquals(0.0D, state.pressure(), 1.0E-9D);
	}

	/** Round two P5, Taut Line: a quickened jet fills sooner and bleeds the same. */
	@Test
	void aQuickenedJetFillsSoonerAndBleedsTheSame() {
		JetRules.State plain = new JetRules.State(), quick = new JetRules.State();
		double quicken = StrandSkills.quicken(2);
		JetRules.tick(plain, 4);
		JetRules.tick(quick, 4, quicken);
		int ticks = JetRules.fillTicks(quicken);
		for (int i = 0; i < ticks; i++) {
			JetRules.tick(plain, 4);
			JetRules.tick(quick, 4, quicken);
		}
		assertEquals(1.0D, quick.pressure(), 1.0E-9D, "the quickened jet was not full after " + ticks + " ticks");
		assertTrue(plain.pressure() < 1.0D, "the plain jet filled as fast");
		JetRules.tick(quick, -1, quicken);
		assertEquals(1.0D - JetRules.BLEED_PER_TICK, quick.pressure(), 1.0E-9D, "the bleed changed with the quicken");
	}
}

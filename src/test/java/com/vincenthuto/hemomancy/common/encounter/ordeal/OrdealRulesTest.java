package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Outcome;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import org.junit.jupiter.api.Test;

class OrdealRulesTest {
	@Test
	void rampFollowsHulkHealth() {
		assertEquals(Ramp.OPENING, OrdealRules.ramp(0.9F));
		assertEquals(Ramp.PRESSED, OrdealRules.ramp(0.5F));
		assertEquals(Ramp.DESPERATE, OrdealRules.ramp(0.2F));
		assertEquals(1, OrdealRules.concurrent(Ramp.OPENING));
		assertEquals(3, OrdealRules.concurrent(Ramp.DESPERATE));
		assertTrue(OrdealRules.cadence(Ramp.DESPERATE) < OrdealRules.cadence(Ramp.OPENING));
	}

	@Test
	void interventionsUnlockByRamp() {
		Set<String> opening = new HashSet<>(), desperate = new HashSet<>();
		for (long tick = 0; tick < 400; tick++) {
			opening.add(OrdealRules.next(OrdealProfessor.STRONGMAN, Ramp.OPENING, tick, 7L).id());
			desperate.add(OrdealRules.next(OrdealProfessor.STRONGMAN, Ramp.DESPERATE, tick, 7L).id());
		}
		assertFalse(opening.contains("bar_sweep"));
		assertFalse(opening.contains("hold_the_line"));
		assertEquals(Set.of("dropped_weight", "ground_heave", "bar_sweep", "hold_the_line"), desperate);
		assertTrue(OrdealProfessor.forSummon("gorebound_hulk").isPresent());
		assertTrue(OrdealProfessor.forSummon("ringmaster_pattern").isEmpty(), "a body no professor teaches has no ordeal");
	}

	@Test
	void stagesNeverMeetRooms() {
		for (int id = 0; id <= 1000; id++) assertTrue(OrdealRules.stageOrigin(id).getX() >= OrdealRules.ORDEAL_X);
		assertEquals(OrdealRules.stageOrigin(3).getZ(), 3 * 256);
	}

	@Test
	void onlyAWinCompletes() {
		for (Outcome outcome : Outcome.values()) assertEquals(outcome == Outcome.WON, OrdealRules.completesRite(outcome));
	}

	@Test
	void keyOncePerDay() {
		assertFalse(OrdealRules.keyRegivenToday(12L, 12L));
		assertTrue(OrdealRules.keyRegivenToday(12L, 13L));
		assertTrue(OrdealRules.keyRegivenToday(-1L, 0L));
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import net.minecraft.world.BossEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Phases, the Last Thirst, the two roads to the ending, and the damage he takes. */
class HemorathPhaseRulesTest {
	private static SaintPhaseTable table() {
		return SaintPhaseTable.byHealth(new SaintPhaseTable.Phase(1, HemorathEntity.NAME_KEY, BossEvent.BossBarColor.RED, 1.0F, null),
				new SaintPhaseTable.Phase(2, HemorathEntity.EMPTIES_KEY, BossEvent.BossBarColor.PURPLE,
						HemorathPhaseRules.PHASE_2_HEALTH, null));
	}

	@Test
	void phaseTwoComesAtHalfHealth() {
		SaintPhaseTable phases = table();
		assertEquals(1, phases.target(0.51F, 0.0F, 1));
		assertEquals(2, phases.target(0.5F, 0.0F, 1));
	}

	@Test
	void theFirstFullMeterForcesPhaseTwoAtOnceAndTearsHisHealth() {
		SaintPhaseTable phases = table();
		phases.advance(2, SaintCombatController.Cut.IMMEDIATE);
		assertTrue(phases.update(phases.target(0.9F, 1.0F, 1), false), "an immediate advance does not wait for the boundary");
		assertEquals(2, phases.phase());
		assertEquals(2, phases.target(0.95F, 0.3F, 2), "health above half cannot take phase 2 back");
		assertEquals(510.0F, HemorathPhaseRules.tornHealth(600.0F, 600.0F), 1.0E-3F);
		assertEquals(1.0F, HemorathPhaseRules.tornHealth(50.0F, 600.0F), "the tear never kills him");
		assertEquals(0.30F, HemorathMeterRules.AFTER_EMPTIES);
	}

	@Test
	void theLastThirstRunsOnceAtAFifthInPhaseTwo() {
		assertFalse(HemorathPhaseRules.thirstDue(1, 0.1F, false));
		assertFalse(HemorathPhaseRules.thirstDue(2, 0.21F, false));
		assertTrue(HemorathPhaseRules.thirstDue(2, 0.2F, false));
		assertFalse(HemorathPhaseRules.thirstDue(2, 0.1F, true));
		assertEquals(HemorathPhaseRules.ThirstExit.NONE, HemorathPhaseRules.thirstExit(100, false, false));
		assertEquals(HemorathPhaseRules.ThirstExit.OVERLOAD, HemorathPhaseRules.thirstExit(100, true, false));
		assertEquals(HemorathPhaseRules.ThirstExit.FALL, HemorathPhaseRules.thirstExit(100, false, true));
		assertEquals(HemorathPhaseRules.ThirstExit.TIMEOUT, HemorathPhaseRules.thirstExit(200, false, false));
		assertEquals(30, HemorathPhaseRules.decisionDelay(14, 2, true));
		assertEquals(10, HemorathPhaseRules.decisionDelay(14, 2, false));
		assertEquals(14, HemorathPhaseRules.decisionDelay(14, 1, false));
	}

	@Test
	void theDamageTakenStacksStatePatchesAndTheOpening() {
		assertEquals(0.70F, HemorathDamageRules.factor(0.2F, 0, false), 1.0E-5F);
		assertEquals(1.30F, HemorathDamageRules.factor(0.8F, 0, false), 1.0E-5F);
		assertEquals(1.30F * 0.64F, HemorathDamageRules.factor(0.8F, 9, false), 1.0E-5F);
		assertEquals(0.5F, HemorathDamageRules.factor(0.5F, 0, true), 1.0E-5F);
		float withGorged = SaintDamageRules.factor(HemorathDamageRules.factor(0.8F, 0, false),
				HemorathOpenings.GORGED.damageTaken());
		assertEquals(1.30F * 1.25F, withGorged, 1.0E-5F);
		assertEquals(1.0F, SaintDamageRules.floor(0.0F), "a Saint is never killed: the kit's floor of 1 ends the fight");
	}
}

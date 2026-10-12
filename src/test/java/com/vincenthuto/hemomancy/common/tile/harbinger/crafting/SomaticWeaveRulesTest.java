package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class SomaticWeaveRulesTest {
	@Test
	void threadsTearLooseOncePerEchoLoopAfterTheRelease() {
		assertFalse(SomaticWeaveRules.frayDue(0));
		assertTrue(SomaticWeaveRules.frayDue(SomaticWeaveRules.FRAY_TICK));
		assertTrue(SomaticWeaveRules.frayDue(SomaticWeaveRules.FRAY_TICK + SomaticWeaveRules.LOOP_TICKS));
		assertFalse(SomaticWeaveRules.frayDue(SomaticWeaveRules.FRAY_TICK + 1));
		assertTrue(SomaticWeaveRules.FRAY_TICK > SomaticWeaveRules.RELEASE_TICK);
	}

	@Test
	void lowRanksHoldTwoThreadsAndCannotKill() {
		for (EnumManipulationRank r : new EnumManipulationRank[]{HUMILIS, MEDIOCRITAS}) {
			assertEquals(2, SomaticWeaveRules.maxLoose(r));
			assertFalse(SomaticWeaveRules.lethal(r));
		}
		for (EnumManipulationRank r : new EnumManipulationRank[]{SUMMA, MAGISTER, PERFECTUS}) {
			assertEquals(3, SomaticWeaveRules.maxLoose(r));
			assertTrue(SomaticWeaveRules.lethal(r));
		}
	}

	@Test
	void strikingStrainsFasterThanWaitingAndHigherRanksEndureMore() {
		for (EnumManipulationRank r : EnumManipulationRank.values()) {
			float perAttack = SomaticWeaveRules.strainPerAttack(r);
			float perHit = SomaticWeaveRules.strainPerHit(r);
			assertTrue(perHit > 0 && perHit < perAttack, r.name());
			assertTrue(perAttack <= 0.5f, "every thread attacks at least twice: " + r);
		}
		assertTrue(SomaticWeaveRules.strainPerAttack(PERFECTUS) < SomaticWeaveRules.strainPerAttack(HUMILIS));
		assertTrue(SomaticWeaveRules.attackPower(PERFECTUS) > SomaticWeaveRules.attackPower(HUMILIS));
		assertTrue(SomaticWeaveRules.attackInterval(PERFECTUS) < SomaticWeaveRules.attackInterval(HUMILIS));
		assertTrue(SomaticWeaveRules.attackInterval(PERFECTUS) >= 40);
	}

	@Test
	void eachEnzymeUnitIsOneThreadCappedPerTendency() {
		assertEquals(0, SomaticWeaveRules.threadsFor(0));
		assertEquals(4, SomaticWeaveRules.threadsFor(4));
		assertEquals(8, SomaticWeaveRules.threadsFor(20));
	}

	@Test
	void theFloorClampsDamageAndBlacksOut() {
		assertEquals(3f, SomaticWeaveRules.flooredDamage(10f, 3f), 1e-6);
		assertFalse(SomaticWeaveRules.blacksOut(10f, 3f));
		assertEquals(6f, SomaticWeaveRules.flooredDamage(10f, 9f), 1e-6);
		assertTrue(SomaticWeaveRules.blacksOut(10f, 9f));
		assertEquals(0f, SomaticWeaveRules.flooredDamage(3f, 5f), 1e-6);
		assertTrue(SomaticWeaveRules.blacksOut(3f, 5f));
	}

	@Test
	void theWeaverMustBeAliveHereAndClose() {
		double r2 = SomaticWeaveRules.RITUAL_RADIUS * SomaticWeaveRules.RITUAL_RADIUS;
		assertTrue(SomaticWeaveRules.weaverPresent(true, true, r2 - 1));
		assertFalse(SomaticWeaveRules.weaverPresent(true, true, r2 + 1));
		assertFalse(SomaticWeaveRules.weaverPresent(false, true, 1));
		assertFalse(SomaticWeaveRules.weaverPresent(true, false, 1));
	}

	@Test
	void loomStagesWeaveTheRanksTheirDegreeTeaches() {
		assertTrue(SomaticWeaveRules.rankAllowed(SUMMA, 0));
		assertFalse(SomaticWeaveRules.rankAllowed(MAGISTER, 0));
		assertTrue(SomaticWeaveRules.rankAllowed(MAGISTER, 1));
		assertFalse(SomaticWeaveRules.rankAllowed(PERFECTUS, 1));
		assertTrue(SomaticWeaveRules.rankAllowed(PERFECTUS, 2));
		assertEquals(0, SomaticWeaveRules.stageFor(HUMILIS));
		assertEquals(0, SomaticWeaveRules.stageFor(SUMMA));
		assertEquals(1, SomaticWeaveRules.stageFor(MAGISTER));
		assertEquals(2, SomaticWeaveRules.stageFor(PERFECTUS));
	}

	@Test
	void windingUpThreadsDiveIntoStaffReach() {
		assertTrue(SomaticWeaveRules.holdDistance(5.5, true) < 3.0, "a winding thread comes within melee reach");
		assertEquals(5.5, SomaticWeaveRules.holdDistance(5.5, false), 1e-9);
		assertEquals(1.5, SomaticWeaveRules.holdDistance(1.5, true), 1e-9);
	}

	@Test
	void ganglionicLoomsCatchSlackThreadsAndReachFurther() {
		assertEquals(0.0, SomaticWeaveRules.autoCatchRadius(0), 1e-9);
		assertTrue(SomaticWeaveRules.autoCatchRadius(1) >= 4.0);
		assertEquals(SomaticWeaveRules.autoCatchRadius(1), SomaticWeaveRules.autoCatchRadius(2), 1e-9);
		assertTrue(SomaticWeaveRules.hookReach(1) > SomaticWeaveRules.hookReach(0));
		assertEquals(SomaticWeaveRules.hookReach(1), SomaticWeaveRules.hookReach(2), 1e-9);
	}
}

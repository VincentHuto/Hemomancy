package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core loop Track 4: the pure Temper numbers (cap, fill, pay order, dry multipliers, scars, death). */
class TemperRulesTest {
	private static final float EPSILON = 1.0E-5F;

	@Test
	void capGrowsWithAlignmentBetweenTwentyAndEighty() {
		assertEquals(20.0F, TemperRules.cap(0.0F), EPSILON);
		assertEquals(20.0F, TemperRules.cap(-40.0F), EPSILON, "negative alignment never shrinks the floor cap");
		assertEquals(35.0F, TemperRules.cap(25.0F), EPSILON);
		assertEquals(65.0F, TemperRules.cap(75.0F), EPSILON);
		assertEquals(80.0F, TemperRules.cap(100.0F), EPSILON);
		assertEquals(80.0F, TemperRules.cap(500.0F), EPSILON);
		assertEquals(20.0F, TemperRules.cap(Float.NaN), EPSILON);
	}

	@Test
	void aWillTakenWholeLeavesAnEnzymesWorthPerTier() {
		assertEquals(10.0F, TemperRules.fromWill(1), EPSILON);
		assertEquals(40.0F, TemperRules.fromWill(4), EPSILON);
		assertEquals(0.0F, TemperRules.fromWill(0), EPSILON, "no tier, no Temper");
	}

	@Test
	void castsCostByRankAndPulsesByCadence() {
		assertEquals(2.0F, TemperRules.castCost(EnumManipulationRank.HUMILIS), EPSILON);
		assertEquals(3.5F, TemperRules.castCost(EnumManipulationRank.MEDIOCRITAS), EPSILON);
		assertEquals(5.0F, TemperRules.castCost(EnumManipulationRank.SUMMA), EPSILON);
		assertEquals(6.5F, TemperRules.castCost(EnumManipulationRank.MAGISTER), EPSILON);
		assertEquals(8.0F, TemperRules.castCost(EnumManipulationRank.PERFECTUS), EPSILON);
		assertEquals(1.0F, TemperRules.pulseCost(20, 20.0D), EPSILON);
		assertEquals(0.5F, TemperRules.pulseCost(10, 20.0D), EPSILON);
		assertEquals(0.05F, TemperRules.pulseCost(1, 20.0D), EPSILON, "a per-tick haul burns a twentieth of a second's burn each tick");
		assertEquals(0.05F, TemperRules.pulseCost(0, 20.0D), EPSILON, "a cadence below one tick counts as one");
	}

	@Test
	void heldFormsBurnByPriceBand() {
		assertEquals(0.5F, TemperRules.burnPerSecond(8.0D), EPSILON);
		assertEquals(0.5F, TemperRules.burnPerSecond(10.0D), EPSILON, "a band's edge belongs to the band below");
		assertEquals(1.0F, TemperRules.burnPerSecond(10.5D), EPSILON);
		assertEquals(1.0F, TemperRules.burnPerSecond(25.0D), EPSILON);
		assertEquals(1.5F, TemperRules.burnPerSecond(36.0D), EPSILON);
		assertEquals(1.5F, TemperRules.burnPerSecond(45.0D), EPSILON);
		assertEquals(2.0F, TemperRules.burnPerSecond(60.0D), EPSILON);
		assertEquals(0.5F, TemperRules.burnPerSecond(0.0D), EPSILON);
		assertEquals(1.0F, TemperRules.pulseCost(10, 60.0D), EPSILON, "a 60 mL/s form burns 2 a second in 10-tick pulses");
	}

	@Test
	void creaturesFillTheirSchoolsInProportionToTheBloodDrawn() {
		assertEquals(10.0F, TemperRules.fromCreature(10.0D, 1), EPSILON);
		assertEquals(5.0F, TemperRules.fromCreature(10.0D, 2), EPSILON, "a blaze's 10 mL is 5 Flammeus and 5 Lux");
		assertEquals(0.0F, TemperRules.fromCreature(10.0D, 0), EPSILON, "a creature with no schools gives none");
		assertEquals(0.0F, TemperRules.fromCreature(0.0D, 2), EPSILON);
		assertEquals(0.0F, TemperRules.fromCreature(Double.NaN, 2), EPSILON);
	}

	@Test
	void thePoolPaysFirst() {
		TemperRules.Plan plan = TemperRules.plan(10.0F, 2.0F, 10.0F, 50.0F);
		assertEquals(TemperRules.Outcome.TEMPERED, plan.outcome());
		assertEquals(8.0F, TemperRules.settle(10.0F, plan, 50.0F), EPSILON);
		assertEquals(1.0D, TemperRules.costMultiplier(TemperRules.Kind.CAST, plan), EPSILON);
		assertEquals(1.0F, TemperRules.strength(TemperRules.Kind.CAST, plan), EPSILON);
	}

	@Test
	void aShortPoolDissolvesOneCatalystAndKeepsTheRest() {
		TemperRules.Plan coal = TemperRules.plan(0.0F, 0.5F, 1.0F, 20.0F);
		assertEquals(TemperRules.Outcome.CATALYST, coal.outcome(), "coal covers a Pyretic Forge pulse");
		assertEquals(0.5F, TemperRules.settle(0.0F, coal, 20.0F), EPSILON);
		assertFalse(coal.dry());
		assertEquals(1.0D, TemperRules.costMultiplier(TemperRules.Kind.SCULPTING, coal), EPSILON);
		TemperRules.Plan topUp = TemperRules.plan(19.0F, 2.0F, 10.0F, 20.0F);
		assertEquals(TemperRules.Outcome.TEMPERED, topUp.outcome(), "a pool that holds enough never reaches for a catalyst");
		TemperRules.Plan atCap = TemperRules.plan(1.0F, 2.0F, 10.0F, 5.0F);
		assertEquals(TemperRules.Outcome.CATALYST, atCap.outcome());
		assertEquals(3.0F, TemperRules.settle(1.0F, atCap, 5.0F), EPSILON, "a catalyst fills no further than the cap");
	}

	@Test
	void nothingToPayWithRunsDryAndNeverRefuses() {
		TemperRules.Plan dry = TemperRules.plan(0.3F, 1.0F, 0.0F, 20.0F);
		assertTrue(dry.dry());
		assertEquals(0.3F, TemperRules.settle(0.3F, dry, 20.0F), EPSILON, "a dry cast leaves the pool as it was");
		assertEquals(1.25D, TemperRules.costMultiplier(TemperRules.Kind.CAST, dry), EPSILON);
		assertEquals(0.9F, TemperRules.strength(TemperRules.Kind.CAST, dry), EPSILON);
		assertEquals(1.5D, TemperRules.costMultiplier(TemperRules.Kind.SCULPTING, dry), EPSILON);
		assertEquals(0.75F, TemperRules.strength(TemperRules.Kind.SCULPTING, dry), EPSILON);
		TemperRules.Plan tooSmall = TemperRules.plan(0.0F, 5.0F, 3.0F, 20.0F);
		assertTrue(tooSmall.dry(), "a catalyst worth less than the cost does not cover it");
		assertEquals(TemperRules.Plan.FREE, TemperRules.plan(0.0F, 0.0F, 0.0F, 20.0F), "a free effect is tempered");
	}

	@Test
	void scarsCutTheBurnAndRefillTowardTheirFloor() {
		assertEquals(2.0F, TemperRules.scarred(2.0F, 0), EPSILON);
		assertEquals(1.5F, TemperRules.scarred(2.0F, 1), EPSILON);
		assertEquals(1.0F, TemperRules.scarred(2.0F, 2), EPSILON);
		assertEquals(0.5F, TemperRules.scarred(2.0F, 3), EPSILON);
		assertEquals(15.0F, TemperRules.scarFloor(3), EPSILON);
		assertEquals(0.0F, TemperRules.scarFloor(0), EPSILON);
		assertEquals(0.3F, TemperRules.refillTowardFloor(0.0F, 3, 1.0F), EPSILON, "LP-13: 0.1 a second per tier");
		assertEquals(0.1F, TemperRules.refillTowardFloor(0.0F, 1, 1.0F), EPSILON);
		assertEquals(15.0F, TemperRules.refillTowardFloor(14.9F, 3, 1.0F), EPSILON, "the refill stops at the floor");
		assertEquals(40.0F, TemperRules.refillTowardFloor(40.0F, 3, 1.0F), EPSILON, "a pool above the floor is left alone");
		assertEquals(3.0F, TemperRules.refillTowardFloor(3.0F, 0, 10.0F), EPSILON, "no scar, no refill");
	}

	/**
	 * LP-13: a tier 3 scar refills 0.3 a second, less than a Mediocritas cast a second burns after the scar's cut
	 * (0.875 of its 3.5). The scar slows the drain a long way but no longer holds the school for ever.
	 */
	@Test
	void aTierThreeScarSlowsTheDrainButDoesNotHoldIt() {
		float cap = 50.0F;
		int scarredFirstDry = firstDrySecond(TemperRules.scarred(TemperRules.castCost(EnumManipulationRank.MEDIOCRITAS), 3), 3, cap);
		int unscarredFirstDry = firstDrySecond(TemperRules.castCost(EnumManipulationRank.MEDIOCRITAS), 0, cap);
		assertTrue(scarredFirstDry < 600, "ten minutes of a cast a second should run a tier 3 scar dry");
		assertTrue(scarredFirstDry > 5 * unscarredFirstDry,
				"the scar should hold far longer: " + scarredFirstDry + " against " + unscarredFirstDry + " seconds");
	}

	private static int firstDrySecond(float costPerSecond, int scarTier, float cap) {
		float pool = 15.0F;
		for (int second = 0; second < 600; second++) {
			TemperRules.Plan plan = TemperRules.plan(pool, costPerSecond, 0.0F, cap);
			if (plan.dry()) return second;
			pool = TemperRules.refillTowardFloor(TemperRules.settle(pool, plan, cap), scarTier, 1.0F);
		}
		return 600;
	}

	@Test
	void deathKeepsHalf() {
		assertEquals(20.0F, TemperRules.afterDeath(40.0F), EPSILON);
		assertEquals(0.0F, TemperRules.afterDeath(-1.0F), EPSILON);
	}
}

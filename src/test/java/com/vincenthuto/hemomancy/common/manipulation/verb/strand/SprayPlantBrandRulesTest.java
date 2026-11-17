package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 12: the spray's cone, the plant's sizing by blood sunk, and the brand's hold. */
class SprayPlantBrandRulesTest {
	@Test
	void theSprayConeIsShortAndThirtyFiveDegreesWide() {
		Vec3 origin = Vec3.ZERO, axis = new Vec3(0, 0, 1);
		assertTrue(SprayRules.inCone(origin, axis, new Vec3(0, 0, 3), 3.3D, SprayRules.MIN_DOT));
		assertTrue(SprayRules.inCone(origin, axis, new Vec3(1.5, 0, 3), 3.5D, SprayRules.MIN_DOT), "26 degrees off the aim");
		assertFalse(SprayRules.inCone(origin, axis, new Vec3(3, 0, 3), 5.0D, SprayRules.MIN_DOT), "45 degrees off the aim");
		assertFalse(SprayRules.inCone(origin, axis, new Vec3(0, 0, 4), 3.3D, SprayRules.MIN_DOT), "past the reach");
		assertEquals(3.3D, StrandForm.SPRAY.length(5.5D), 1.0E-9D);
	}

	@Test
	void thePlantGrowsWithBloodSunkUpToItsCap() {
		assertFalse(PlantRules.primed(0), "an empty plant erupted");
		assertTrue(PlantRules.primed(1));
		assertEquals(PlantRules.MIN_RADIUS, PlantRules.radius(0), 1.0E-9D);
		assertEquals(PlantRules.MAX_RADIUS, PlantRules.radius(PlantRules.CAP_PULSES), 1.0E-9D);
		assertEquals(PlantRules.MAX_RADIUS, PlantRules.radius(PlantRules.CAP_PULSES * 3), 1.0E-9D, "the cap holds");
		assertTrue(PlantRules.damage(4) > PlantRules.damage(1));
		assertEquals(PlantRules.MAX_DAMAGE, PlantRules.damage(PlantRules.CAP_PULSES), 1.0E-6F);
		assertTrue(PlantRules.full(PlantRules.CAP_PULSES) && !PlantRules.full(PlantRules.CAP_PULSES - 1));
	}

	@Test
	void aBrandTakesAfterASecondOnOneCreature() {
		BrandRules.State state = new BrandRules.State();
		boolean set = false;
		for (int tick = 0; tick < BrandRules.SET_TICKS - 1; tick++) set |= BrandRules.tick(state, 5);
		assertFalse(set, "the brand set early");
		assertTrue(BrandRules.tick(state, 5), "the brand did not set after a second");
		assertEquals(0.0D, BrandRules.progress(state), 1.0E-9D, "holding on starts the next brand over");
	}

	@Test
	void aBriefGlanceAwayKeepsTheProgressAndALongOneLosesIt() {
		BrandRules.State state = new BrandRules.State();
		for (int tick = 0; tick < 10; tick++) BrandRules.tick(state, 5);
		for (int tick = 0; tick < BrandRules.GRACE_TICKS; tick++) BrandRules.tick(state, -1);
		assertEquals(10, state.held(), "a glance inside the grace lost the progress");
		for (int tick = 0; tick < 3; tick++) BrandRules.tick(state, -1);
		assertEquals(0, state.held(), "looking away for long kept the progress");
	}

	@Test
	void aNewCreatureStartsTheBrandOver() {
		BrandRules.State state = new BrandRules.State();
		for (int tick = 0; tick < 15; tick++) BrandRules.tick(state, 5);
		BrandRules.tick(state, 6);
		assertEquals(1, state.held());
		assertEquals(6, state.target());
	}

	@Test
	void aBrandHoldsUntilItLapses() {
		assertTrue(BrandRules.holds(100, 99));
		assertFalse(BrandRules.holds(100, 100));
	}

	/** Round two P5: a run along a stain reaches more cells the more is sunk, to its cap. */
	@Test
	void aStainRunGrowsWithBloodSunkUpToItsCap() {
		assertEquals(0, PlantRules.stainCells(0));
		assertEquals(6, PlantRules.stainCells(1));
		assertEquals(20, PlantRules.stainCells(8));
		assertEquals(PlantRules.STAIN_MAX_CELLS, PlantRules.stainCells(40));
	}

	/** Round two P5: the run floods the wet cells joined to the start, nearest first, and nothing past a gap. */
	@Test
	void aStainRunFloodsOnlyJoinedWetCellsInWalkOrder() {
		java.util.Set<BlockPos> wet = new java.util.HashSet<>();
		for (int x = 0; x <= 6; x++) wet.add(new BlockPos(x, 1, 0));
		wet.add(new BlockPos(9, 1, 0));
		java.util.List<BlockPos> run = PlantRules.stainRun(new BlockPos(0, 1, 0), wet::contains, 20);
		assertEquals(7, run.size(), "the run crossed a dry gap: " + run);
		for (int i = 0; i < run.size(); i++) assertEquals(new BlockPos(i, 1, 0), run.get(i), "out of walk order: " + run);
		assertEquals(3, PlantRules.stainRun(new BlockPos(0, 1, 0), wet::contains, 3).size(), "the run outgrew its blood");
		assertTrue(PlantRules.stainRun(new BlockPos(0, 2, 0), wet::contains, 20).isEmpty(), "a dry start ran");
	}

	/** Round two P5: the run reaches no further than its reach from the planted cell, however long the stain. */
	@Test
	void aStainRunStaysWithinItsReach() {
		java.util.List<BlockPos> run = PlantRules.stainRun(BlockPos.ZERO, pos -> pos.getY() == 0 && pos.getZ() == 0, 100);
		assertEquals(PlantRules.STAIN_REACH * 2 + 1, run.size());
		assertTrue(run.stream().allMatch(pos -> Math.abs(pos.getX()) <= PlantRules.STAIN_REACH));
	}

	/** Round two P5, Deep Brand: a brand at full Deep Brand takes in half the time. */
	@Test
	void aDeepBrandTakesSooner() {
		BrandRules.State state = new BrandRules.State();
		int set = StrandSkills.brandSetTicks(2);
		boolean took = false;
		for (int i = 0; i < set && !took; i++) took = BrandRules.tick(state, 3, set);
		assertTrue(took, "a deep brand did not take in " + set + " ticks");
		assertTrue(set < BrandRules.SET_TICKS);
		BrandRules.State plain = new BrandRules.State();
		for (int i = 0; i < 5; i++) BrandRules.tick(plain, 3, set);
		assertEquals(5.0D / set, BrandRules.progress(plain, set), 1.0E-9D);
	}

	/** Round two P5, Second Strand: two hands fan a wider cone. */
	@Test
	void theTwinSprayConeIsFiftyDegrees() {
		Vec3 axis = new Vec3(0, 0, 1);
		Vec3 at45 = new Vec3(Math.sin(Math.toRadians(45)), 0, Math.cos(Math.toRadians(45))).scale(2.0D);
		assertFalse(SprayRules.inCone(Vec3.ZERO, axis, at45, 3.0D, SprayRules.MIN_DOT), "45 degrees is in the plain cone");
		assertTrue(SprayRules.inCone(Vec3.ZERO, axis, at45, 3.0D, SprayRules.TWIN_MIN_DOT), "45 degrees is not in the twin cone");
	}
}

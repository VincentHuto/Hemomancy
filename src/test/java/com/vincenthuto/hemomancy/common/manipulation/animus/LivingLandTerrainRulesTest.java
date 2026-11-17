package com.vincenthuto.hemomancy.common.manipulation.animus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivingLandTerrainRulesTest {
	/** A flat world: solid at y <= 0, air above. */
	private static final Predicate<BlockPos> GROUND = pos -> pos.getY() <= 0;
	private static final Predicate<BlockPos> OPEN = pos -> pos.getY() > 0;

	@Test
	void theStruckFacePicksTheMode() {
		assertEquals(LivingLandTerrainRules.Mode.FLOOR_TEETH, LivingLandTerrainRules.Mode.forFace(Direction.UP));
		assertEquals(LivingLandTerrainRules.Mode.CEILING_CRUSH, LivingLandTerrainRules.Mode.forFace(Direction.DOWN));
		for (Direction side : Direction.Plane.HORIZONTAL) {
			assertEquals(LivingLandTerrainRules.Mode.WALL_LANCES, LivingLandTerrainRules.Mode.forFace(side));
		}
	}

	@Test
	void columnsGrowWithMasteryAndShrinkWhenDry() {
		assertEquals(3, LivingLandTerrainRules.pillarLength(1, 1.0D));
		assertEquals(4, LivingLandTerrainRules.pillarLength(2, 1.0D));
		assertEquals(5, LivingLandTerrainRules.pillarLength(4, 1.0D));
		assertEquals(2, LivingLandTerrainRules.pillarLength(1, 0.75D), "a dry pulse lifts a shorter column");
		assertEquals(4, LivingLandTerrainRules.pillarLength(4, 0.75D));
		assertEquals(5, LivingLandTerrainRules.pillarLength(4, 3.0D), "never more than five");
		assertEquals(2, LivingLandTerrainRules.pillarLength(0, 0.1D), "never fewer than two");
	}

	@Test
	void damageFollowsTheColumnAndALaterRankSendsTwo() {
		assertEquals(4.0F, LivingLandTerrainRules.damage(2));
		assertEquals(6.0F, LivingLandTerrainRules.damage(3), "four plus two for each block past the second");
		assertEquals(10.0F, LivingLandTerrainRules.damage(5));
		assertEquals(1, LivingLandTerrainRules.strikesInFlight(1));
		assertEquals(1, LivingLandTerrainRules.strikesInFlight(2));
		assertEquals(2, LivingLandTerrainRules.strikesInFlight(3));
		assertEquals(3, LivingLandTerrainRules.maxRaise(1.0D));
		assertEquals(2, LivingLandTerrainRules.maxRaise(0.75D), "a dry raise stands lower");
	}

	@Test
	void anEnemyMustStandInFrontOfThePatch() {
		BlockPos floor = new BlockPos(0, 0, 0);
		assertTrue(LivingLandTerrainRules.inFront(floor, Direction.UP, new Vec3(2.5, 1.9, 0.5)));
		assertFalse(LivingLandTerrainRules.inFront(floor, Direction.UP, new Vec3(2.5, -2.0, 0.5)), "below the floor");
		assertFalse(LivingLandTerrainRules.inFront(floor, Direction.UP, new Vec3(2.5, 12.0, 0.5)), "too high above it");
		BlockPos wall = new BlockPos(0, 2, 0);
		assertEquals(3.0D, LivingLandTerrainRules.frontDistance(wall, Direction.EAST, new Vec3(4.0, 2.5, 0.5)), 1.0E-9D);
		assertTrue(LivingLandTerrainRules.frontDistance(wall, Direction.EAST, new Vec3(-2.0, 2.5, 0.5)) < 0.0D);
	}

	@Test
	void theFloorColumnEruptsBesideTheEnemySoAllOfItComesOut() {
		BlockPos patch = new BlockPos(0, 0, 0);
		Optional<List<BlockPos>> column = LivingLandTerrainRules.sourceColumn(patch, Direction.UP,
				new Vec3(3.5, 1.9, 1.5), 3, GROUND, OPEN);
		assertTrue(column.isPresent());
		// Two blocks off the enemy's shadow: about a column's length (2.5) from the target. Of the four such cells the
		// order is fixed by coordinates.
		assertEquals(List.of(new BlockPos(1, 0, 1), new BlockPos(1, -1, 1), new BlockPos(1, -2, 1)), column.get(),
				"the surface block first, then straight down");
	}

	@Test
	void aColumnTakesOnlyWhatCanBeLiftedAndNeedsTwo() {
		BlockPos patch = new BlockPos(0, 0, 0);
		// Only the surface and one block under it can be lifted.
		Predicate<BlockPos> shallow = pos -> pos.getY() <= 0 && pos.getY() >= -1;
		List<BlockPos> column = LivingLandTerrainRules.sourceColumn(patch, Direction.UP, new Vec3(3.5, 1.9, 0.5), 5,
				shallow, OPEN).orElseThrow();
		assertEquals(2, column.size());
		Predicate<BlockPos> single = pos -> pos.getY() == 0;
		assertTrue(LivingLandTerrainRules.sourceColumn(patch, Direction.UP, new Vec3(3.5, 1.9, 0.5), 3, single, OPEN).isEmpty(),
				"a column of one is no strike");
	}

	@Test
	void aSourceMustLieWithinThePatch() {
		BlockPos patch = new BlockPos(0, 0, 0);
		assertTrue(LivingLandTerrainRules.sourceColumn(patch, Direction.UP, new Vec3(15.5, 1.9, 0.5), 3, GROUND, OPEN).isEmpty());
	}

	@Test
	void aWallLanceRunsBackIntoTheWall() {
		// A wall whose face looks east (+X), the wall solid at x <= 0.
		BlockPos patch = new BlockPos(0, 3, 0);
		Predicate<BlockPos> wall = pos -> pos.getX() <= 0;
		Predicate<BlockPos> open = pos -> pos.getX() > 0;
		List<BlockPos> column = LivingLandTerrainRules.sourceColumn(patch, Direction.EAST, new Vec3(4.0, 3.9, 2.5), 3,
				wall, open).orElseThrow();
		assertEquals(new BlockPos(0, 3, 2), column.getFirst());
		assertEquals(new BlockPos(-2, 3, 2), column.getLast());
	}

	@Test
	void aRaiseStacksGroundFromBehindThePatch() {
		BlockPos patch = new BlockPos(0, 0, 0);
		List<LivingLandTerrainRules.RaiseStep> first = LivingLandTerrainRules.raiseSteps(patch, Direction.UP, 0, 1,
				GROUND, OPEN, pos -> false);
		assertEquals(List.of(new LivingLandTerrainRules.RaiseStep(new BlockPos(0, -1, 0), new BlockPos(0, 1, 0))), first,
				"the step stands on the struck block and its stone comes from under it");
		List<LivingLandTerrainRules.RaiseStep> second = LivingLandTerrainRules.raiseSteps(patch, Direction.UP, 1, 2,
				GROUND, OPEN, Set.of(new BlockPos(0, -1, 0))::contains);
		assertEquals(new BlockPos(0, -2, 0), second.getFirst().from(), "a hole already taken is skipped");
		assertEquals(new BlockPos(0, 2, 0), second.getFirst().into());
		assertTrue(LivingLandTerrainRules.raiseSteps(patch, Direction.UP, 0, 1, GROUND, pos -> false, pos -> false).isEmpty(),
				"no raise into a filled cell");
	}

	@Test
	void aDisplacedBlockFindsTheNearestFreeCellTheSameWayEveryTime() {
		BlockPos origin = new BlockPos(0, 0, 0);
		Set<BlockPos> free = Set.of(new BlockPos(0, 1, 0), new BlockPos(1, 0, 0), new BlockPos(2, 0, 0));
		assertEquals(new BlockPos(1, 0, 0), LivingLandTerrainRules.nearestFree(origin, 3, free::contains).orElseThrow(),
				"of two cells one block away, the lower comes first");
		assertTrue(LivingLandTerrainRules.nearestFree(origin, 3, pos -> false).isEmpty());
	}

	@Test
	void blocksSlideOutOfTheirSocketAsTheHeadAdvances() {
		Vec3 root = new Vec3(0.5, 1.0, 0.5);
		Vec3 head = new Vec3(0.5, 3.0, 0.5);
		Vec3 first = LivingLandTerrainRules.blockCentre(root, Direction.UP, head, 0);
		assertEquals(head.y, first.y, 1.0E-6D, "the head block is at the head");
		Vec3 deep = LivingLandTerrainRules.blockCentre(root, Direction.UP, root.add(0, 0.5, 0), 2);
		assertTrue(deep.y < root.y, "a block the head has not drawn out sits back in its socket");
		Vec3 second = LivingLandTerrainRules.blockCentre(root, Direction.UP, head, 1);
		assertEquals(1.0D, head.distanceTo(second), 0.05D, "blocks follow a block apart");
	}
}

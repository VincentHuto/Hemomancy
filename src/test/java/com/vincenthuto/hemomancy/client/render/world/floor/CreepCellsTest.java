package com.vincenthuto.hemomancy.client.render.world.floor;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CreepCellsTest {
	@Test
	void groundAtTheFocusLevelWins() {
		assertEquals(0, CreepCells.groundOffset(dy -> dy == 0 || dy == 1, 3));
	}

	@Test
	void theNearestGroundIsFoundUpBeforeDown() {
		assertEquals(1, CreepCells.groundOffset(Set.of(1, -1)::contains, 3));
		assertEquals(-2, CreepCells.groundOffset(Set.of(-2, 3)::contains, 3));
	}

	@Test
	void cliffsAndCavesCarryNoCreep() {
		assertEquals(CreepCells.NO_GROUND, CreepCells.groundOffset(Set.of(4, -4)::contains, 3));
	}

	@Test
	void creepMeshIsBoundedForGrandFootprints() {
		int grand = CreepCells.maxCells(9, FloorMesh.CREEP_MARGIN);
		assertTrue(grand < 2000, "a grand floor's creep stays under two thousand cells, was " + grand);
		assertTrue(CreepCells.maxCells(2, FloorMesh.CREEP_MARGIN) < grand);
	}

	@Test
	void theCreepFollowsGroundThatChangesUnderIt() {
		Set<Long> solid = new HashSet<>();
		for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) solid.add(key(x, z));
		CreepCells creep = new CreepCells(2, 4, 3);
		CreepCells.Ground ground = (x, dy, z) -> dy == 0 && solid.contains(key(x, z));

		int before = creep.cells(0L, ground, CreepCellsTest::beyondFloor, (x, z) -> 0.0F).size();
		solid.remove(key(5, 0));

		assertEquals(before, creep.cells(10L, ground, CreepCellsTest::beyondFloor, (x, z) -> 0.0F).size(),
				"the ground is re-read only every so often");
		assertEquals(before - 1, creep.cells(CreepCells.RECHECK_TICKS, ground, CreepCellsTest::beyondFloor,
				(x, z) -> 0.0F).size(), "a broken block drops its cell");
	}

	@Test
	void groundThatLoadsLaterGainsItsCells() {
		Set<Long> solid = new HashSet<>();
		CreepCells creep = new CreepCells(2, 4, 3);
		CreepCells.Ground ground = (x, dy, z) -> dy == 0 && solid.contains(key(x, z));

		assertTrue(creep.cells(0L, ground, CreepCellsTest::beyondFloor, (x, z) -> 0.0F).isEmpty());
		for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) solid.add(key(x, z));

		assertTrue(creep.cells(CreepCells.RECHECK_TICKS, ground, CreepCellsTest::beyondFloor, (x, z) -> 0.0F)
				.size() > 20, "a chunk that loads after the rite is first seen still gets its creep");
	}

	private static boolean beyondFloor(int x, int z) {
		return Math.abs(x) > 2 || Math.abs(z) > 2;
	}

	private static long key(int x, int z) {
		return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
	}
}

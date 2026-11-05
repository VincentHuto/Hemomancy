package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ChamberRoomShapeTest {
	@Test
	void corridorGrowsWiderAndLongerWithTheRoom() {
		assertEquals(1, ChamberRoomShape.halfWidth(3));
		assertEquals(2, ChamberRoomShape.halfWidth(5));
		assertEquals(3, ChamberRoomShape.halfWidth(8));
		assertEquals(4, ChamberRoomShape.halfWidth(10));
		assertEquals(7, ChamberRoomShape.length(3));
		assertEquals(14, ChamberRoomShape.length(10));
		assertEquals(24, ChamberRoomShape.reach(10));
	}

	@Test
	void keepsRoughlyTheBuildingSpaceOfTheOldSquare() {
		for (int radius = 3; radius <= 10; radius++) {
			int square = (2 * radius + 1) * (2 * radius + 1);
			int cells = 0;
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= ChamberRoomShape.reach(radius); dz++) {
					if (ChamberRoomShape.contains(dx, dz, radius)) cells++;
				}
			}
			assertTrue(cells >= square * 0.95 && cells <= square * 1.2,
					"radius " + radius + ": " + cells + " cells against the square's " + square);
		}
	}

	@Test
	void holdsTheArborTheEntryAndTheCorridorEnd() {
		assertTrue(ChamberRoomShape.contains(0, 0, 3));
		assertTrue(ChamberRoomShape.contains(0, 3, 3));
		assertTrue(ChamberRoomShape.contains(1, ChamberRoomShape.reach(3), 3));
		assertFalse(ChamberRoomShape.contains(0, ChamberRoomShape.reach(3) + 1, 3));
		assertFalse(ChamberRoomShape.contains(2, ChamberRoomShape.reach(3), 3));
		assertFalse(ChamberRoomShape.contains(3, 3, 3));
		assertFalse(ChamberRoomShape.contains(0, -4, 3));
	}

	@Test
	void theGateStandsOnTheLastCorridorRow() {
		assertTrue(ChamberRoomShape.contains(0, ChamberRoomShape.reach(7), 7));
	}

	@Test
	void clampLeavesAPointInsideAlone() {
		ChamberRoomShape.Point inside = ChamberRoomShape.clamp(1.0, 2.0, 5);
		assertEquals(1.0, inside.dx());
		assertEquals(2.0, inside.dz());
		ChamberRoomShape.Point corridor = ChamberRoomShape.clamp(0.5, ChamberRoomShape.reach(5) - 1.0, 5);
		assertEquals(ChamberRoomShape.reach(5) - 1.0, corridor.dz());
	}

	@Test
	void clampStopsAtTheCircleAndTheCorridorWalls() {
		ChamberRoomShape.Point north = ChamberRoomShape.clamp(0.0, -20.0, 5);
		assertEquals(-5.5, north.dz(), 1e-6);
		assertEquals(0.0, north.dx(), 1e-6);
		ChamberRoomShape.Point wall = ChamberRoomShape.clamp(9.0, ChamberRoomShape.reach(5) - 2.0, 5);
		assertEquals(ChamberRoomShape.halfWidth(5) + 0.5, wall.dx(), 1e-6);
		assertEquals(ChamberRoomShape.reach(5) - 2.0, wall.dz(), 1e-6);
		ChamberRoomShape.Point end = ChamberRoomShape.clamp(0.0, ChamberRoomShape.reach(5) + 4.0, 5);
		assertEquals(ChamberRoomShape.reach(5), end.dz(), 1e-6, "the gate's plane is as far as anyone walks");
	}

	@Test
	void outlineIsOpenAtTheGateAndClosedEverywhereElse() {
		var outline = ChamberRoomShape.outline(5, 24);
		var first = outline.get(0);
		var last = outline.get(outline.size() - 1);
		assertEquals(ChamberRoomShape.reach(5), first.dz(), 1e-6);
		assertEquals(ChamberRoomShape.reach(5), last.dz(), 1e-6);
		assertEquals(-first.dx(), last.dx(), 1e-6);
		for (var point : outline) {
			assertTrue(ChamberRoomShape.near(point.dx(), point.dz(), 5, 1.0), "the aura follows the floor's edge: " + point);
		}
	}

	private static boolean overFloor(ChamberRoomShape.Point point, int radius) {
		// A point exactly on the line between two blocks stands on either of them.
		for (int x : new int[] { (int) Math.floor(point.dx() + 0.5), (int) Math.ceil(point.dx() - 0.5) }) {
			for (int z : new int[] { (int) Math.floor(point.dz() + 0.5), (int) Math.ceil(point.dz() - 0.5) }) {
				if (ChamberRoomShape.contains(x, z, radius)) return true;
			}
		}
		return false;
	}

	@Test
	void clampNeverLeavesABodyOverAMissingBlock() {
		for (int radius = 3; radius <= 10; radius++) {
			int reach = ChamberRoomShape.reach(radius);
			for (double dx = -radius - 3; dx <= radius + 3; dx += 0.07) {
				for (double dz = -radius - 3; dz <= reach + 3; dz += 0.07) {
					ChamberRoomShape.Point held = ChamberRoomShape.clamp(dx, dz, radius);
					assertTrue(overFloor(held, radius), "radius " + radius + ": (" + dx + ", " + dz + ") held at " + held);
				}
			}
		}
	}

	@Test
	void clampIsSettledOnceItHasHeld() {
		for (int radius = 3; radius <= 10; radius++) {
			for (double dx = -radius - 2; dx <= radius + 2; dx += 0.13) {
				for (double dz = -radius - 2; dz <= ChamberRoomShape.reach(radius) + 2; dz += 0.13) {
					ChamberRoomShape.Point held = ChamberRoomShape.clamp(dx, dz, radius);
					assertEquals(held, ChamberRoomShape.clamp(held.dx(), held.dz(), radius), "a held body is not moved again");
				}
			}
		}
	}

	@Test
	void everyFloorBlockCanBeStoodOn() {
		for (int radius = 3; radius <= 10; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= ChamberRoomShape.reach(radius); dz++) {
					if (!ChamberRoomShape.contains(dx, dz, radius)) continue;
					assertEquals(new ChamberRoomShape.Point(dx, dz), ChamberRoomShape.clamp(dx, dz, radius));
				}
			}
		}
	}
}

package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Brined Court Track 9: the Shared Wake school's rules (size, regions, where a natural one may start, the loop). */
final class CourtSchoolRulesTest {
	@Test
	void aSchoolIsFourToSixMembers() {
		assertEquals(4, CourtSchoolRules.size(0.0D));
		assertEquals(6, CourtSchoolRules.size(0.9999D));
		for (double roll = 0; roll < 1; roll += 0.01) {
			int size = CourtSchoolRules.size(roll);
			assertTrue(size >= 4 && size <= 6, "size " + size);
		}
	}

	@Test
	void regionsAre1024BlocksSquare() {
		assertEquals(CourtSchoolRules.region(0, 0), CourtSchoolRules.region(1023, 1023));
		assertNotEquals(CourtSchoolRules.region(0, 0), CourtSchoolRules.region(1024, 0));
		assertNotEquals(CourtSchoolRules.region(0, 0), CourtSchoolRules.region(0, 1024));
		assertNotEquals(CourtSchoolRules.region(0, 0), CourtSchoolRules.region(-1, 0));
		assertNotEquals(CourtSchoolRules.region(1024, 0), CourtSchoolRules.region(0, 1024), "x and z don't alias");
	}

	@Test
	void onlySomeRegionsHaveANaturalSchoolAndTheSeedFixesWhich() {
		int with = 0;
		Set<Boolean> seen = new HashSet<>();
		for (int x = -20; x < 20; x++) for (int z = -20; z < 20; z++) {
			long region = CourtSchoolRules.region(x * 1024, z * 1024);
			boolean has = CourtSchoolRules.regionHasSchool(42L, region);
			assertEquals(has, CourtSchoolRules.regionHasSchool(42L, region), "fixed by the seed");
			seen.add(has);
			if (has) with++;
		}
		assertEquals(Set.of(true, false), seen);
		double share = with / 1600.0D;
		assertTrue(share > 0.25D && share < 0.42D, "about one region in three: " + share);
	}

	@Test
	void aNaturalSchoolStartsOnlyInOpenPelagicWaterClearOfSeatsInAFreeRegion() {
		assertTrue(CourtSchoolRules.mayStartNaturally(false, true, PelagicLayer.OPEN, false));
		assertFalse(CourtSchoolRules.mayStartNaturally(true, true, PelagicLayer.OPEN, false), "the region has had one");
		assertFalse(CourtSchoolRules.mayStartNaturally(false, false, PelagicLayer.OPEN, false), "not a school region");
		assertFalse(CourtSchoolRules.mayStartNaturally(false, true, PelagicLayer.REEF, false), "not open water");
		assertFalse(CourtSchoolRules.mayStartNaturally(false, true, null, false), "not Pelagic at all");
		assertFalse(CourtSchoolRules.mayStartNaturally(false, true, PelagicLayer.OPEN, true), "a Seat is near");
		assertEquals(256, CourtSchoolRules.SEAT_CLEARANCE);
	}

	@Test
	void theLoopIsACircleOfWaypointsAndStationsTrailTheLeader() {
		int radius = CourtSchoolRules.LOOP_RADIUS;
		int count = CourtSchoolRules.waypoints(radius);
		assertTrue(count >= 8);
		for (int i = 0; i < count; i++) {
			double[] point = CourtSchoolRules.waypoint(100, -50, radius, i);
			assertEquals(radius, Math.hypot(point[0] - 100, point[1] + 50), 1e-6);
		}
		double[] a = CourtSchoolRules.waypoint(0, 0, radius, 0), b = CourtSchoolRules.waypoint(0, 0, radius, count);
		assertEquals(a[0], b[0], 1e-9, "the loop closes");
		// A leader facing south (yaw 0, +z): every station is behind it (smaller z), alternating sides.
		for (int slot = 1; slot < CourtSchoolRules.MAX_SIZE; slot++) {
			double[] station = CourtSchoolRules.stationAt(0, 0, 0.0F, slot);
			assertTrue(station[2] < -1.0D, "slot " + slot + " trails the leader");
			assertTrue(Math.abs(station[0]) > 1.0D, "slot " + slot + " is off to one side");
		}
		assertTrue(Math.signum(CourtSchoolRules.stationAt(0, 0, 0.0F, 1)[0]) != Math.signum(CourtSchoolRules.stationAt(0, 0, 0.0F, 2)[0]),
				"the first two flank the leader on both sides");
	}
}

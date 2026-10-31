package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;

import javax.annotation.Nullable;

/**
 * The Shared Wake's school, as rules: where one may start, how big it is and the loop its leader swims. A school is
 * four to six Edge-swimmers with a leader on a wide loop through Pelagic Ocean. At most one ever starts in each
 * 1024 x 1024 region (whether from a nursery ground or out of open water), only one region in three has a natural one
 * at all, and a natural school never starts within 256 blocks of a Seat.
 */
public final class CourtSchoolRules {
	public static final int REGION = 1024;
	public static final int SEAT_CLEARANCE = 256;
	public static final int MIN_SIZE = 4;
	public static final int MAX_SIZE = 6;
	/** The loop's radius round the point the school started from. */
	public static final int LOOP_RADIUS = 48;
	/** Roughly how far apart the loop's waypoints lie along it. */
	public static final double WAYPOINT_SPACING = 14.0D;
	/** One region in this many has a natural school. */
	public static final int NATURAL_ODDS = 3;
	/** A natural school starts this far from the player who is in its water, out of sight. */
	public static final int SPAWN_MIN = 40;
	public static final int SPAWN_MAX = 64;
	/** Water at least this deep, floor to surface, so the school has room to swim at mid-depth. */
	public static final int MIN_DEPTH = 16;
	private static final long SALT = 0x5EA5C4001L;

	private CourtSchoolRules() {
	}

	/** The region holding block (x, z), packed in one long. */
	public static long region(int x, int z) {
		return ((long) Math.floorDiv(x, REGION) << 32) ^ (Math.floorDiv(z, REGION) & 0xFFFFFFFFL);
	}

	/** Whether this region has a natural school at all, fixed by the world seed. */
	public static boolean regionHasSchool(long seed, long region) {
		return Math.floorMod(PelagicTerrainSampler.mix(seed ^ SALT ^ region * 0x9E3779B97F4A7C15L), NATURAL_ODDS) == 0;
	}

	/**
	 * Whether a natural school may start here: the region hasn't had one, it is one of the regions that has one, the
	 * water is Pelagic Ocean and no Seat is within {@link #SEAT_CLEARANCE}.
	 */
	public static boolean mayStartNaturally(boolean regionClaimed, boolean regionHasSchool, @Nullable PelagicLayer layer,
			boolean nearSeat) {
		return !regionClaimed && regionHasSchool && layer == PelagicLayer.OPEN && !nearSeat;
	}

	/** How many members a school gets from a roll in [0, 1). */
	public static int size(double roll) {
		return MIN_SIZE + Math.min(MAX_SIZE - MIN_SIZE, (int) (roll * (MAX_SIZE - MIN_SIZE + 1)));
	}

	/** How many waypoints a loop of this radius has. */
	public static int waypoints(int radius) {
		return Math.max(8, (int) Math.round(2.0D * Math.PI * radius / WAYPOINT_SPACING));
	}

	/** Waypoint {@code index} of the loop round (x, z): {x, z}, going round clockwise seen from above. */
	public static double[] waypoint(double x, double z, int radius, int index) {
		int count = waypoints(radius);
		double angle = 2.0D * Math.PI * Math.floorMod(index, count) / count;
		return new double[] {x + radius * Math.cos(angle), z + radius * Math.sin(angle)};
	}

	/**
	 * Where a follower holds station behind the leader: {lateral, up, back} in the leader's frame. Slot 1 and 2 flank it
	 * close behind, 3 and 4 a row further back and wider, and so on, staggered up and down a little.
	 */
	public static double[] station(int slot) {
		int row = (slot + 1) / 2;
		double side = slot % 2 == 0 ? 1.0D : -1.0D;
		return new double[] {side * (1.6D + 0.6D * row), ((slot % 3) - 1) * 0.7D, 2.2D * row};
	}

	/** A follower's station in the world: {x, y offset, z} behind a leader at (x, z) facing {@code yawDegrees}. */
	public static double[] stationAt(double x, double z, float yawDegrees, int slot) {
		double[] station = station(slot);
		double rad = Math.toRadians(yawDegrees);
		double forwardX = -Math.sin(rad), forwardZ = Math.cos(rad);
		return new double[] {x + station[0] * forwardZ - station[2] * forwardX, station[1],
				z - station[0] * forwardX - station[2] * forwardZ};
	}
}

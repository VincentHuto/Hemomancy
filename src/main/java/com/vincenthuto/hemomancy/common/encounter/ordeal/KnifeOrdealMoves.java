package com.vincenthuto.hemomancy.common.encounter.ordeal;

import net.minecraft.world.phys.Vec3;

/**
 * The Dagger Thrower's ordeal as pure geometry and timing (2026-11-17): where they pace on their gallery, the lanes of a
 * fan, the closing ring, the pin and the board, and the lane the Marrow Juggler's volley runs down. Read by the server
 * (what each move hits) and by the true self's model (where the spectral blades fly), so a drawn blade is a hit blade.
 * Positions are feet; {@code floor} is the top of the ring's plate at its centre.
 */
public final class KnifeOrdealMoves {
	/** The Dagger Thrower's gestures ({@code CircusPerformerEntity.gesture}), one per intervention. */
	public static final int GESTURE_FAN = 0, GESTURE_RING = 1, GESTURE_PIN = 2, GESTURE_BOARD = 3;
	/** Their block of ground-hazard kinds: four interventions, then the Juggler's three moves. */
	public static final int KIND_FAN = OrdealHazardKinds.KNIFE_THROWER, KIND_RING = KIND_FAN + 1, KIND_PIN = KIND_FAN + 2,
			KIND_BOARD = KIND_FAN + 3, KIND_HAIL = KIND_FAN + 4, KIND_VOLLEY = KIND_FAN + 5, KIND_WHIRL = KIND_FAN + 6;

	/** The gallery: its radius, its height over the ring's floor, the arc they pace either side of north and its period. */
	public static final double GALLERY_RADIUS = 15.0D, GALLERY_HEIGHT = 2.0D, PACE_DEGREES = 40.0D;
	public static final float PACE_PERIOD = 1600.0F;
	/** How many times over the true self is drawn, and its eyes over its feet in model blocks. */
	public static final float SCALE = 2.2F;
	public static final double EYES = 3.65D;
	/** Ticks a spectral blade takes from the hand to the floor; the pin's heavy blade is lobbed, and takes longer. */
	public static final int FLIGHT = 3, PIN_FLIGHT = 6;

	public static final int FAN_WINDUP = 22, FAN_LANES = 5;
	public static final double FAN_SPREAD = 11.0D, FAN_HALF_WIDTH = 0.6D, FAN_HEIGHT = 2.4D;
	public static final float FAN_DAMAGE = 5.0F;

	public static final int RING_WINDUP = 24, RING_CLOSE = 8, RING_BLADES = 12;
	public static final double RING_RADIUS = 4.5D, RING_BAND = 1.0D;
	/** Feet this far over the floor clear the closing ring's blades: a jump. */
	public static final double CLEARANCE = 1.1D;
	public static final float RING_DAMAGE = 4.0F;

	public static final int PIN_WINDUP = 18, PIN_STUCK = 30;
	public static final double PIN_RADIUS = 1.4D;
	public static final float PIN_DAMAGE = 4.0F;

	public static final int BOARD_WINDUP = 24, BOARD_GAP = 4, BOARD_KNIVES = 6;
	public static final double BOARD_OUTLINE = 2.2D, BOARD_SPOT = 0.9D, BULLSEYE = 1.1D, BOARD_EDGE = 3.4D;
	public static final float OUTLINE_DAMAGE = 3.0F, BULLSEYE_DAMAGE = 7.0F;
	/** The bullseye lands a beat after the last outline blade; the board's blades stay until this long after it. */
	public static final int BULLSEYE_AT = BOARD_WINDUP + BOARD_KNIVES * BOARD_GAP + 2, BOARD_LINGER = 20;

	private KnifeOrdealMoves() {
	}

	/** Where they stand on their gallery at {@code time}: pacing an arc either side of north (-z) of the ring. */
	public static Vec3 professorAt(Vec3 floor, double time) {
		double angle = Math.toRadians(PACE_DEGREES) * Math.sin(time / PACE_PERIOD * Math.PI * 2.0D);
		return new Vec3(floor.x + Math.sin(angle) * GALLERY_RADIUS, floor.y + GALLERY_HEIGHT,
				floor.z - Math.cos(angle) * GALLERY_RADIUS);
	}

	/** A fan lane's direction: lane 2 runs from {@code origin} straight at {@code aim}, the others turned 11 degrees apart. */
	public static Vec3 fanDirection(Vec3 origin, Vec3 aim, int lane) {
		Vec3 toward = OrdealKit.flat(aim.subtract(origin));
		if (toward.lengthSqr() < 1.0E-6D) toward = new Vec3(0.0D, 0.0D, 1.0D);
		return toward.yRot((float) Math.toRadians((lane - FAN_LANES / 2) * FAN_SPREAD));
	}

	/**
	 * Where a line from {@code origin} along the flat {@code dir} enters and leaves a circle round {@code centre}, as
	 * distances along it, or null when it misses.
	 */
	public static double[] span(Vec3 origin, Vec3 dir, Vec3 centre, double radius) {
		double ox = origin.x - centre.x, oz = origin.z - centre.z;
		double b = ox * dir.x + oz * dir.z, c = ox * ox + oz * oz - radius * radius;
		double disc = b * b - c;
		if (disc < 0.0D) return null;
		double root = Math.sqrt(disc);
		double enter = -b - root, leave = -b + root;
		return leave <= 0.0D ? null : new double[]{Math.max(0.0D, enter), leave};
	}

	/** Whether feet stand in a lane from {@code from} along the flat {@code dir}, {@code length} long. */
	public static boolean laneHits(Vec3 from, Vec3 dir, double length, double halfWidth, Vec3 feet, double floorY,
			double height) {
		double dx = feet.x - from.x, dz = feet.z - from.z;
		double along = dx * dir.x + dz * dir.z, across = Math.abs(dx * dir.z - dz * dir.x);
		return along >= 0.0D && along <= length && across <= halfWidth + 0.3D && feet.y - floorY < height
				&& feet.y >= floorY - 0.5D;
	}

	/** Whether any lane of a fan thrown from {@code origin} at {@code aim} strikes feet. Blades fly at chest height. */
	public static boolean fanHits(Vec3 origin, Vec3 aim, Vec3 feet, double floorY) {
		for (int lane = 0; lane < FAN_LANES; lane++)
			if (laneHits(origin, fanDirection(origin, aim, lane), 64.0D, FAN_HALF_WIDTH, feet, floorY, FAN_HEIGHT)) return true;
		return false;
	}

	/** The closing ring's blades, {@code ticks} after they start to slide: from the ring's radius to its centre. */
	public static double ringRadius(int ticks) {
		return Math.max(0.0D, RING_RADIUS * (1.0D - ticks / (double) RING_CLOSE));
	}

	/** Whether the closing ring's blades at {@code wave} strike feet: within their band and not jumping them. */
	public static boolean ringHits(Vec3 centre, double wave, Vec3 feet) {
		return Math.abs(horizontal(centre, feet) - wave) <= RING_BAND / 2.0D + 0.3D && feet.y - centre.y < CLEARANCE
				&& feet.y >= centre.y - 0.5D;
	}

	/** Whether a blade landing on a disc of {@code radius} at {@code mark} strikes feet. */
	public static boolean spotHits(Vec3 mark, double radius, Vec3 feet) {
		return horizontal(mark, feet) <= radius && feet.y - mark.y < 2.5D && feet.y >= mark.y - 0.5D;
	}

	/** The board's outline blade {@code k} (0 to 5): round the clock from the side facing north, 60 degrees apart. */
	public static Vec3 boardSpot(Vec3 centre, int k) {
		double angle = Math.toRadians(-90.0D + 60.0D * k);
		return centre.add(Math.cos(angle) * BOARD_OUTLINE, 0.0D, Math.sin(angle) * BOARD_OUTLINE);
	}

	/** The tick (from the board's wind-up) outline blade {@code k} lands on. */
	public static int boardLanding(int k) {
		return BOARD_WINDUP + k * BOARD_GAP;
	}

	/** Where the closing ring's blade {@code k} stands before it slides. */
	public static Vec3 ringBlade(Vec3 centre, int k, double radius) {
		double angle = Math.PI * 2.0D * k / RING_BLADES;
		return centre.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
	}

	public static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}

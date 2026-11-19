package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.phys.Vec3;

/**
 * The Fire Breather's ordeal as pure geometry (2026-11-17): what each of their interventions and each of the Cinder
 * Bellows' moves burns, so it can be read and dodged. Positions are feet; {@code floor} is the top of the ring's plate at
 * its centre. Nothing of the breath is jumped except the Bellows' coil lash: fire is stepped out of, not hopped.
 */
public final class FireEaterOrdealGeometry {
	/** Feet this far above the floor clear a jumpable move (the coil lash). */
	public static final double CLEARANCE = 1.1D;
	/** Feet this far above the floor are over a standing fire (a jump does not clear the breath or the ring). */
	public static final double FLAME_HEIGHT = 2.5D;
	/** The ring a mark is kept inside, short of the curb. */
	public static final double MARK_LIMIT = 8.0D;

	// --- the Fire Breather -----------------------------------------------------------------------------------------

	/** The long breath: a lane across the ring, its front running out from the near rim, burning where it has passed. */
	public static final double BREATH_LENGTH = 19.0D, BREATH_HALF_WIDTH = 1.25D, BREATH_SPEED = 1.6D;
	/** Spat embers: three marks, one on the fighter's line and two flanking it, each this wide. */
	public static final double EMBER_RADIUS = 1.5D, EMBER_LEAD = 6.0D, EMBER_FLANK = 2.75D;
	/** The ring of fire: a wall at the rim closing to {@link #RING_CLOSED} over {@link #RING_CLOSE_TICKS}. */
	public static final double RING_OPEN = 9.0D, RING_CLOSED = 4.0D, RING_BAND = 1.2D;
	public static final int RING_CLOSE_TICKS = 50;
	/** The cinder rain: this many marks over the ring, each this wide, the burning pools they leave this wide. */
	public static final int RAIN_MARKS = 8;
	public static final double RAIN_RADIUS = 1.3D;

	// --- the Cinder Bellows ------------------------------------------------------------------------------------------

	/** The blast: a cone from the Bellows' feet. The inhale before it draws a fighter in front of it this hard. */
	public static final double BLAST_RADIUS = 6.0D, BLAST_DEGREES = 60.0D, INHALE_REACH = 9.0D, INHALE_PULL = 0.045D;
	/** The vent: a burst all round it. */
	public static final double VENT_RADIUS = 3.5D;
	/** The coil lash: a ring wave out from its coil, jumped. */
	public static final double LASH_FROM = 1.0D, LASH_TO = 4.75D, LASH_BAND = 1.4D;
	public static final int LASH_TICKS = 6;

	private FireEaterOrdealGeometry() {
	}

	/** How far the breath's front has run along its lane {@code ticks} after it is loosed. */
	public static double breathFront(int ticks) {
		return Math.min(BREATH_LENGTH, Math.max(0, ticks) * BREATH_SPEED);
	}

	/** Whether feet stand on the breath's lane between its start and {@code reach} along it. */
	public static boolean inBreath(Vec3 start, Vec3 direction, double reach, Vec3 feet) {
		Vec3 along = flat(direction);
		double dx = feet.x - start.x, dz = feet.z - start.z;
		double forward = dx * along.x + dz * along.z, side = Math.abs(dx * along.z - dz * along.x);
		return forward >= -0.3D && forward <= reach && side <= BREATH_HALF_WIDTH && standing(start.y, feet);
	}

	/**
	 * Where the long breath first touches the floor: the near rim on the line from {@code from} (where the Fire Breather
	 * stands) through {@code mark} (the fighter), so the lane crosses the ring through the fighter.
	 */
	public static Vec3 breathStart(Vec3 floor, Vec3 mark, Vec3 from) {
		Vec3 direction = flat(new Vec3(mark.x - from.x, 0, mark.z - from.z));
		if (direction.lengthSqr() < 1.0E-6D) direction = new Vec3(0, 0, 1);
		double mx = mark.x - floor.x, mz = mark.z - floor.z;
		double b = mx * direction.x + mz * direction.z;
		double room = b * b - (mx * mx + mz * mz) + RING_OPEN * RING_OPEN;
		double back = b + Math.sqrt(Math.max(0.0D, room));
		return new Vec3(mark.x - direction.x * back, floor.y, mark.z - direction.z * back);
	}

	/** The first ember's mark: where the fighter will be in {@link #EMBER_LEAD} ticks at their pace, inside the ring. */
	public static Vec3 emberLead(Vec3 feet, Vec3 velocity, Vec3 floor) {
		return inRing(new Vec3(feet.x + velocity.x * EMBER_LEAD, floor.y, feet.z + velocity.z * EMBER_LEAD), floor);
	}

	/** The three ember marks: the lead and two flanking it across {@code line} (the spit's line from the pulpit). */
	public static List<Vec3> emberMarks(Vec3 lead, Vec3 line, Vec3 floor) {
		Vec3 along = flat(line);
		Vec3 side = along.lengthSqr() < 1.0E-6D ? new Vec3(1, 0, 0) : new Vec3(-along.z, 0, along.x);
		List<Vec3> marks = new ArrayList<>();
		marks.add(inRing(lead, floor));
		marks.add(inRing(lead.add(side.scale(EMBER_FLANK)), floor));
		marks.add(inRing(lead.subtract(side.scale(EMBER_FLANK)), floor));
		return marks;
	}

	public static boolean emberHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) <= EMBER_RADIUS && standing(mark.y, feet);
	}

	/** The wall's radius {@code ticks} after the ring is lit: closing from the rim, then held. */
	public static double ringRadius(int ticks) {
		double t = Math.min(1.0D, Math.max(0, ticks) / (double) RING_CLOSE_TICKS);
		return RING_OPEN + (RING_CLOSED - RING_OPEN) * t;
	}

	public static boolean ringHits(Vec3 floor, double radius, Vec3 feet) {
		return Math.abs(horizontal(floor, feet) - radius) <= RING_BAND / 2.0D + 0.3D && standing(floor.y, feet);
	}

	/** Where the ring throws a fighter it catches: in, toward the centre and the Bellows. */
	public static Vec3 ringThrow(Vec3 floor, Vec3 feet) {
		return flat(new Vec3(floor.x - feet.x, 0, floor.z - feet.z));
	}

	/** The cinder rain's marks: a seeded scatter over the ring, spread so no two fall on one spot. */
	public static List<Vec3> rainMarks(Vec3 floor, long seed) {
		List<Vec3> marks = new ArrayList<>();
		double turn = (seed & 0xFFFF) / 65536.0D * Math.PI * 2.0D;
		for (int i = 0; i < RAIN_MARKS; i++) {
			// A sunflower spread: each mark a golden angle on from the last, further out as it goes.
			double radius = 1.5D + (MARK_LIMIT - 1.5D) * Math.sqrt((i + 0.5D) / RAIN_MARKS);
			double angle = turn + i * 2.399963D;
			marks.add(new Vec3(floor.x + Math.cos(angle) * radius, floor.y, floor.z + Math.sin(angle) * radius));
		}
		return marks;
	}

	public static boolean rainHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) <= RAIN_RADIUS && standing(mark.y, feet);
	}

	// --- the Bellows' moves ----------------------------------------------------------------------------------------

	public static boolean blastHits(Vec3 origin, Vec3 aim, Vec3 feet) {
		double distance = horizontal(origin, feet);
		if (distance > BLAST_RADIUS || !standing(origin.y, feet)) return false;
		if (distance < 0.5D) return true;
		Vec3 to = flat(new Vec3(feet.x - origin.x, 0, feet.z - origin.z));
		return to.dot(flat(aim)) >= Math.cos(Math.toRadians(BLAST_DEGREES / 2.0D));
	}

	/** The inhale's draw on a fighter in front of the Bellows, toward its mouth; nothing behind it or beyond its reach. */
	public static Vec3 inhalePull(Vec3 origin, Vec3 aim, Vec3 feet) {
		Vec3 back = new Vec3(origin.x - feet.x, 0, origin.z - feet.z);
		double distance = back.length();
		if (distance > INHALE_REACH || distance < 1.0D || flat(back.scale(-1)).dot(flat(aim)) < 0.5D) return Vec3.ZERO;
		return back.normalize().scale(INHALE_PULL);
	}

	public static boolean ventHits(Vec3 origin, Vec3 feet) {
		return horizontal(origin, feet) <= VENT_RADIUS && standing(origin.y, feet);
	}

	public static double lashRadius(int ticks) {
		return LASH_FROM + (LASH_TO - LASH_FROM) * Math.min(1.0D, Math.max(0, ticks) / (double) LASH_TICKS);
	}

	public static boolean lashHits(Vec3 origin, double radius, Vec3 feet) {
		return Math.abs(horizontal(origin, feet) - radius) <= LASH_BAND / 2.0D + 0.3D && feet.y - origin.y < CLEARANCE;
	}

	private static boolean standing(double floorY, Vec3 feet) {
		return feet.y - floorY < FLAME_HEIGHT && feet.y >= floorY - 0.5D;
	}

	private static Vec3 inRing(Vec3 point, Vec3 floor) {
		Vec3 out = new Vec3(point.x - floor.x, 0, point.z - floor.z);
		if (out.length() <= MARK_LIMIT) return new Vec3(point.x, floor.y, point.z);
		Vec3 in = out.normalize().scale(MARK_LIMIT);
		return new Vec3(floor.x + in.x, floor.y, floor.z + in.z);
	}

	static Vec3 flat(Vec3 v) {
		Vec3 f = new Vec3(v.x, 0.0D, v.z);
		return f.lengthSqr() < 1.0E-6D ? Vec3.ZERO : f.normalize();
	}

	static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}

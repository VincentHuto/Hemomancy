package com.vincenthuto.hemomancy.common.encounter.ordeal;

import net.minecraft.world.phys.Vec3;

/**
 * The Beast Tamer's ordeal as pure geometry and timing (2026-11-17): their interventions from outside the cage
 * (whipcrack, hoop of fire, goad, beasts in the dark) and the Sanguine Hound's own moves (lunge, pounce, savage shake,
 * howl). What each one hits, so it can be read and dodged, and the beats the true selves' models pose to. Positions are
 * feet; {@code floor} is the top of the ring's plate.
 */
public final class BeastTamerMoves {
	/** Their footprints, the Beast Tamer's block of {@link OrdealHazardKinds}: four interventions, then the Hound's four moves. */
	public static final int KIND_CRACK = OrdealHazardKinds.BEAST_TAMER, KIND_HOOP = KIND_CRACK + 1, KIND_GOAD = KIND_CRACK + 2,
			KIND_BEAST = KIND_CRACK + 3, KIND_LUNGE = KIND_CRACK + 4, KIND_POUNCE = KIND_CRACK + 5, KIND_SHAKE = KIND_CRACK + 6,
			KIND_HOWL = KIND_CRACK + 7;

	/** How many times over the Tamer's true self is drawn (about four blocks tall), and its eyes over its feet, in model blocks. */
	public static final float SCALE = 1.5F;
	public static final double EYES = 2.3D;

	/** The true self's gestures ({@code CircusPerformerEntity.gesture}), one per intervention. */
	public static final int GESTURE_CRACK = 0, GESTURE_HOOP = 1, GESTURE_GOAD = 2, GESTURE_BEASTS = 3;
	/** Each intervention's wind-up, in ticks: it lands on the last. */
	public static final int CRACK_WINDUP = 20, HOOP_WINDUP = 24, GOAD_WINDUP = 16, BEASTS_WINDUP = 26;
	/** How long a gesture holds after its blow, then how long it takes to recover. */
	public static final int HELD = 6, RECOVER = 16;
	/** Feet this far above the floor clear a jumpable footprint. */
	public static final double CLEARANCE = 1.1D;
	/** Standing height: feet lower than this off the floor are caught by a footprint that is not jumped. */
	public static final double STANDING = 2.0D;
	/** A player's half-width, so an edge drawn on the floor is the edge that hits. */
	private static final double BODY = 0.3D;

	// The whipcrack: a lane from the rim nearest them through the fighter and on past.
	public static final double CRACK_OVERSHOOT = 4.0D, CRACK_HALF_WIDTH = 0.8D, CRACK_MAX = 19.0D;
	/** The lash unrolls down the lane over these ticks from the blow; a fighter in it then is struck. */
	public static final int CRACK_UNROLL = 3;
	public static final float CRACK_DAMAGE = 5.0F;
	/** The snare: a struck fighter is yanked this hard toward the Tamer's side, and up. */
	public static final double SNARE_PULL = 1.1D, SNARE_LIFT = 0.35D;

	// The hoop of fire, laid round the fighter.
	public static final double HOOP_RADIUS = 2.5D, HOOP_BAND = 0.8D;
	/** The hoop leads a moving fighter by this many ticks of their motion. */
	public static final double HOOP_LEAD = 4.0D;
	public static final int HOOP_BURN = 30, HOOP_BURN_EVERY = 10, HOOP_FIRE_TICKS = 60;
	public static final float HOOP_DAMAGE = 4.0F, HOOP_BURN_DAMAGE = 2.0F;

	// The goad: the Hound in a frenzy.
	public static final int FRENZY_TICKS = 100, FRENZY_REST = 8;
	/** The frenzy's added pace, as a share of the Hound's own. */
	public static final double FRENZY_PACE = 0.6D;

	// Beasts in the dark: three lanes into the fighter's mark from out past the bars.
	public static final int BEASTS = 3, BEAST_STAGGER = 5, BEAST_RUN = 5;
	public static final double BEAST_FROM = 8.0D, BEAST_PAST = 2.0D, BEAST_HALF_WIDTH = 0.9D;
	public static final float BEAST_DAMAGE = 4.0F;

	// The Sanguine Hound's own moves.
	public static final int LUNGE = 0, POUNCE = 1, SHAKE = 2, HOWL = 3;
	public static final int LUNGE_WINDUP = 12, LUNGE_RUN = 6, POUNCE_WINDUP = 14, POUNCE_FLIGHT = 10, SHAKE_WINDUP = 8,
			SHAKE_HOLD = 24, HOWL_WINDUP = 18, HOWL_WAVE = 9;
	public static final double LUNGE_SPEED = 0.95D, LUNGE_HALF_WIDTH = 0.9D, LUNGE_LENGTH = LUNGE_SPEED * LUNGE_RUN + 0.8D;
	public static final double POUNCE_HEIGHT = 2.2D, POUNCE_RADIUS = 1.8D, POUNCE_LEAD = 6.0D, POUNCE_REACH = 8.0D;
	public static final double SHAKE_AHEAD = 1.4D, SHAKE_REACH = 2.0D;
	/** The ticks into the hold when a shake bites, side to side. */
	public static final int[] SHAKES = {6, 14, 22};
	public static final double HOWL_RADIUS = 7.0D, HOWL_BAND = 1.2D;
	public static final float LUNGE_DAMAGE = 5.0F, POUNCE_DAMAGE = 6.0F, SHAKE_DAMAGE = 2.0F, FLING_DAMAGE = 3.0F,
			HOWL_DAMAGE = 3.0F;
	/** How long each move takes from its start to the Hound's next choice, recovery included. */
	public static final int[] LENGTH = {LUNGE_WINDUP + LUNGE_RUN + 14, POUNCE_WINDUP + POUNCE_FLIGHT + 16,
			SHAKE_WINDUP + SHAKE_HOLD + 12, HOWL_WINDUP + HOWL_WAVE + 14};
	/** Quiet ticks between moves, when its ordinary bite carries the fight. */
	public static final int REST = 30;

	private BeastTamerMoves() {
	}

	/** Whether feet stand in a lane from {@code from} along {@code dir} (flat, unit) for {@code length}. */
	public static boolean laneHits(Vec3 from, Vec3 dir, double length, double halfWidth, Vec3 feet) {
		double dx = feet.x - from.x, dz = feet.z - from.z;
		double along = dx * dir.x + dz * dir.z, across = Math.abs(dx * dir.z - dz * dir.x);
		return along >= -BODY && along <= length + BODY && across <= halfWidth + BODY && feet.y - from.y < STANDING
				&& feet.y >= from.y - 0.5D;
	}

	/**
	 * The whipcrack's lane starts where the line from the Tamer to the mark crosses the rim of radius {@code rim}, at the
	 * plate's height (or at the mark, if the line somehow misses the ring).
	 */
	public static Vec3 crackFrom(Vec3 floor, Vec3 tamer, Vec3 mark, double rim) {
		Vec3 d = flat(mark.subtract(tamer));
		double ox = tamer.x - floor.x, oz = tamer.z - floor.z;
		double b = ox * d.x + oz * d.z, c = ox * ox + oz * oz - rim * rim, disc = b * b - c;
		double s = disc < 0.0D ? horizontal(tamer, mark) : Math.max(0.0D, -b - Math.sqrt(disc));
		return new Vec3(tamer.x + d.x * s, floor.y, tamer.z + d.z * s);
	}

	public static double crackLength(Vec3 from, Vec3 mark) {
		return Math.min(CRACK_MAX, horizontal(from, mark) + CRACK_OVERSHOOT);
	}

	/** Feet on the hoop's burning band, and not leaping it. */
	public static boolean hoopBandHits(Vec3 mark, Vec3 feet) {
		return Math.abs(horizontal(mark, feet) - HOOP_RADIUS) <= HOOP_BAND / 2.0D + BODY && feet.y - mark.y < CLEARANCE
				&& feet.y >= mark.y - 0.5D;
	}

	/** Feet inside the hoop, where the floor burns once it is lit. */
	public static boolean hoopInsideHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) < HOOP_RADIUS - HOOP_BAND / 2.0D && feet.y - mark.y < STANDING && feet.y >= mark.y - 0.5D;
	}

	/**
	 * The turn of the three beasts round the mark, in radians: a seeded angle read from the mark as the client receives
	 * it (its float coordinates), so the server's lanes and the client's drawn beasts agree.
	 */
	public static double beastTurn(Vec3 mark) {
		float x = (float) mark.x, z = (float) mark.z;
		return Math.toRadians(Math.floorMod((int) Math.floor(x * 7.0F + z * 13.0F), 360));
	}

	/** Where beast {@code i} comes out of the dark. */
	public static Vec3 beastFrom(Vec3 mark, int i) {
		double a = beastTurn(mark) + i * Math.PI * 2.0D / BEASTS;
		return new Vec3(mark.x + Math.cos(a) * BEAST_FROM, mark.y, mark.z + Math.sin(a) * BEAST_FROM);
	}

	/** Beast {@code i}'s line of run (flat, unit), out of the dark at the mark. */
	public static Vec3 beastDir(Vec3 mark, int i) {
		double a = beastTurn(mark) + i * Math.PI * 2.0D / BEASTS;
		return new Vec3(-Math.cos(a), 0.0D, -Math.sin(a));
	}

	/** The tick, from the wind-up's start, that beast {@code i} leaps. */
	public static int beastLands(int i) {
		return BEASTS_WINDUP + i * BEAST_STAGGER;
	}

	/** Where beast {@code i}'s head is {@code ticks} after it leaps (feet height; its bound is the model's). */
	public static Vec3 beastAt(Vec3 mark, int i, double ticks) {
		double t = Math.max(0.0D, Math.min(1.0D, ticks / BEAST_RUN));
		return beastFrom(mark, i).add(beastDir(mark, i).scale((BEAST_FROM + BEAST_PAST) * t));
	}

	/** Whether beast {@code i}, {@code ticks} after its leap, has the fighter. */
	public static boolean beastHits(Vec3 mark, int i, double ticks, Vec3 feet) {
		return horizontal(beastAt(mark, i, ticks), feet) <= BEAST_HALF_WIDTH + BODY && feet.y - mark.y < STANDING
				&& feet.y >= mark.y - 0.5D;
	}

	/** The pounce in flight: from its take-off to its landing over {@code t} 0 to 1, arcing {@link #POUNCE_HEIGHT} high. */
	public static Vec3 pounceArc(Vec3 from, Vec3 to, double t) {
		t = Math.max(0.0D, Math.min(1.0D, t));
		return from.lerp(to, t).add(0.0D, 4.0D * POUNCE_HEIGHT * t * (1.0D - t), 0.0D);
	}

	/** Where a pounce lands: ahead of the fighter by their motion, never past the curb or too far from the Hound. */
	public static Vec3 pounceMark(Vec3 hound, Vec3 feet, Vec3 motion, Vec3 floor, double limit) {
		Vec3 ahead = feet.add(motion.x * POUNCE_LEAD, 0.0D, motion.z * POUNCE_LEAD);
		Vec3 reach = ahead.subtract(hound);
		reach = new Vec3(reach.x, 0.0D, reach.z);
		if (reach.length() > POUNCE_REACH) ahead = hound.add(reach.normalize().scale(POUNCE_REACH));
		return inRing(new Vec3(ahead.x, floor.y, ahead.z), floor, limit);
	}

	public static boolean discHits(Vec3 centre, double radius, Vec3 feet) {
		return horizontal(centre, feet) <= radius + BODY && feet.y - centre.y < STANDING && feet.y >= centre.y - 0.5D;
	}

	public static double howlRadius(int ticks) {
		return 0.5D + (HOWL_RADIUS - 0.5D) * Math.max(0.0D, Math.min(1.0D, ticks / (double) HOWL_WAVE));
	}

	/** The howl's wave, jumped. */
	public static boolean howlHits(Vec3 centre, double wave, Vec3 feet) {
		return Math.abs(horizontal(centre, feet) - wave) <= HOWL_BAND / 2.0D + BODY && feet.y - centre.y < CLEARANCE
				&& feet.y >= centre.y - 0.5D;
	}

	/** A point held inside the ring: set back on the line to the centre if it is past {@code limit}. */
	public static Vec3 inRing(Vec3 point, Vec3 floor, double limit) {
		double dx = point.x - floor.x, dz = point.z - floor.z, d = Math.hypot(dx, dz);
		if (d <= limit) return point;
		return new Vec3(floor.x + dx / d * limit, point.y, floor.z + dz / d * limit);
	}

	/** Whether a lunge's next step would cross the curb: the rush stops short of it. */
	public static boolean lungeStops(Vec3 next, Vec3 floor, double limit) {
		return horizontal(next, floor) > limit;
	}

	public static Vec3 flat(Vec3 v) {
		Vec3 f = new Vec3(v.x, 0.0D, v.z);
		return f.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : f.normalize();
	}

	public static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}

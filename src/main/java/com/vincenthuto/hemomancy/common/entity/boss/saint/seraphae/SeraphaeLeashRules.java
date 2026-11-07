package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/**
 * Her reach is her chain (design section 8): the leash length L from integrity, slipped anchors and woken reserves;
 * where along a line she runs out of chain; where she wants to stand; and the reeling that draws her in when L shrinks.
 * Pure, planar, so it is JVM-tested.
 */
public final class SeraphaeLeashRules {
	public static final double BASE = 6.0D;
	public static final double SPAN = 14.0D;
	public static final double PER_SLIP = 3.0D;
	public static final double PER_RESERVE = -2.0D;
	public static final double MIN = 4.0D;
	/** How far past the chain's end her hands still reach. */
	public static final double HAND_REACH = 1.5D;
	/** Where she likes to stand from her target: NEAR, where her favoured moves live. */
	public static final double PREFERRED = 5.0D;
	public static final double REEL_SPEED = 0.15D;
	/** She picks no move while further than this past the chain's end. */
	public static final double REEL_PICK_SLACK = 1.0D;
	/** The Straining Reach may fire when the target stands past the chain's end but within L + this. */
	public static final double STRAINING_REACH = 8.0D;
	/** The Laying On of Hands still picks from FAR when the target stands within L + this. */
	public static final double REACH_FROM_FAR = 3.0D;
	/** How far past L the Straining Reach's torn chain lets her hands go. */
	public static final double STRAINING_EXTRA = 5.0D;
	/** The reach's glide length in ticks, and the tear it makes in a chain at the Straining Reach. */
	public static final int REACH_GLIDE_TICKS = 10;

	private SeraphaeLeashRules() {
	}

	/** L = 6 + 14 (1 - integrity / 100) + 3 per slipped binding anchor - 2 per woken reserve, never under 4. */
	public static double length(float integrity, int slipped, int reserves) {
		double fraction = Math.max(0.0D, Math.min(1.0D, integrity / SeraphaeIntegrityRules.MAX));
		return Math.max(MIN, BASE + SPAN * (1.0D - fraction) + PER_SLIP * slipped + PER_RESERVE * reserves);
	}

	/**
	 * How far she can travel from (x, z) along the unit direction (dx, dz) before the chain of length {@code length}
	 * round (cx, cz) stops her: the ray's exit from the leash circle; 0 when she is already outside it.
	 */
	public static double chainStop(double x, double z, double dx, double dz, double cx, double cz, double length) {
		double ox = x - cx, oz = z - cz;
		double b = ox * dx + oz * dz;
		double c = ox * ox + oz * oz - length * length;
		if (c > 0.0D) return 0.0D;
		double disc = b * b - c;
		return Math.max(0.0D, -b + Math.sqrt(Math.max(0.0D, disc)));
	}

	/** The reach lane's length: to the target and a hand past it, or to the chain's end and a hand past it. */
	public static double reachLength(double targetDistance, double chainStop) {
		return Math.min(targetDistance, chainStop) + HAND_REACH;
	}

	/** Whether the chains stop her hands short of a target this far along the reach. */
	public static boolean stoppedShort(double targetDistance, double chainStop) {
		return targetDistance > chainStop + HAND_REACH;
	}

	/** How far she glides in the reach: to the chain's end, or to just short of the target. */
	public static double glide(double targetDistance, double chainStop) {
		return Math.max(0.0D, Math.min(chainStop, targetDistance - 0.9D));
	}

	/**
	 * Where she wants to stand: on the line from her target toward her, {@link #PREFERRED} out from them, clamped into
	 * the leash disc. Returns {x, z}.
	 */
	public static double[] desired(double sx, double sz, double tx, double tz, double cx, double cz, double length) {
		double dx = sx - tx, dz = sz - tz;
		double d = Math.sqrt(dx * dx + dz * dz);
		double px = d < 1.0E-6D ? tx : tx + dx / d * PREFERRED;
		double pz = d < 1.0E-6D ? tz : tz + dz / d * PREFERRED;
		return clamp(px, pz, cx, cz, length);
	}

	/** A point pulled into the leash disc. */
	public static double[] clamp(double x, double z, double cx, double cz, double length) {
		double ox = x - cx, oz = z - cz;
		double d = Math.sqrt(ox * ox + oz * oz);
		if (d <= length || d < 1.0E-6D) return new double[]{x, z};
		return new double[]{cx + ox / d * length, cz + oz / d * length};
	}

	/** Whether a target this far from the leash centre is past her chain's end: she strains toward them. */
	public static boolean straining(double targetFromCentre, double length) {
		return targetFromCentre > length + HAND_REACH;
	}

	/** Whether she is being reeled: further from the centre than L. */
	public static boolean reeling(double fromCentre, double length) {
		return fromCentre > length + 0.05D;
	}

	/** Whether she may pick while reeled: only within {@link #REEL_PICK_SLACK} of the chain's end. */
	public static boolean mayPickWhileReeled(double fromCentre, double length) {
		return fromCentre <= length + REEL_PICK_SLACK;
	}
}

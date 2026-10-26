package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * What she leaves lying on the Nursery floor and what she throws there: the nectar pools, spore clouds, lattice nets
 * and binding tendrils, the nectar globs and mortar eggs, and the hyphae lances. Sizes, lives, damage and the few
 * pieces of geometry the entities share. No Minecraft types.
 */
public final class MycophantHazardRules {
	public static final float POOL_RADIUS = 1.5F;
	public static final int POOL_LIFE = 160;
	public static final float CLOUD_RADIUS = 3.0F;
	public static final int CLOUD_LIFE = 120;
	public static final float NET_RADIUS = 4.0F;
	/** How long a landed net holds whoever it caught. */
	public static final int NET_HOLD = 40;
	/** Slowness IV: the net all but roots its catch. */
	public static final int NET_SLOW_AMPLIFIER = 3;
	/** A pool's Slowness I, renewed every tick a victim stands in it. */
	public static final int POOL_SLOW_TICKS = 25;
	/** Nectar pressure per tick stood in a pool or cloud (one per second). */
	public static final float PRESSURE_PER_TICK = 0.05F;
	public static final float CLOUD_DAMAGE = 1.0F;
	public static final int CLOUD_DAMAGE_EVERY = 20;
	/** How far above or below a floor hazard's plane a body still counts as standing in it. */
	public static final double FLOOR_BELOW = 0.5;
	public static final double FLOOR_ABOVE = 2.0;

	/** A binding tendril runs out from her body at about this height to its anchor on the floor. */
	public static final double TENDRIL_ROOT_UP = 1.6;
	public static final double TENDRIL_ROOT_FORWARD = 1.4;
	/** A blow counts on a tendril only if the attacker's aim passes this close to it. */
	public static final double TENDRIL_HIT_SLACK = 0.9;
	/** Half the tendril's thickness, for its hit box. */
	public static final double TENDRIL_HALF_WIDTH = 0.35;

	/** Downward speed a glob or egg loses each tick; {@link MycophantStrikeRules#lobVelocity} is given the same. */
	public static final double GLOB_GRAVITY = 0.04;
	public static final float GLOB_DAMAGE = 5.0F;
	public static final float EGG_DAMAGE = 6.0F;
	public static final double EGG_RADIUS = 2.5;
	/** A glob that has found nothing to land on by then is gone. */
	public static final int GLOB_MAX_AGE = 200;

	public static final float LANCE_DAMAGE = 10.0F;
	public static final double LANCE_RADIUS = 0.9;
	public static final double LANCE_TOSS = 0.6;
	/** Ticks a lance stands after it erupts, sinking back over the last ten. */
	public static final int LANCE_LIFE = 24;

	private MycophantHazardRules() {
	}

	/** Every hazard goes with its owner, and otherwise lasts its life. */
	public static boolean shouldExpire(boolean ownerAlive, int age, int life) {
		return !ownerAlive || age >= life;
	}

	/** A net comes down {@code landDelay} ticks after it is cast. */
	public static boolean landed(int age, int landDelay) {
		return age >= landDelay;
	}

	/** Standing in a floor hazard: within {@code radius} across, and a body's feet near its plane. */
	public static boolean standingIn(double dx, double dy, double dz, double radius) {
		return dx * dx + dz * dz <= radius * radius && dy >= -FLOOR_BELOW && dy <= FLOOR_ABOVE;
	}

	/**
	 * One tick of a glob's flight, in place: it moves by its velocity, then loses {@code gravity} from its vertical
	 * speed. No drag, which is what {@link MycophantStrikeRules#lobVelocity} assumes.
	 */
	public static void step(double[] position, double[] velocity, double gravity) {
		position[0] += velocity[0];
		position[1] += velocity[1];
		position[2] += velocity[2];
		velocity[1] -= gravity;
	}

	/**
	 * The closest the aim from {@code o} along unit direction {@code u}, out to {@code reach}, comes to the segment
	 * {@code a}-{@code b}. Arrays are (x, y, z).
	 */
	public static double aimDistance(double[] o, double[] u, double reach, double[] a, double[] b) {
		double[] v = { b[0] - a[0], b[1] - a[1], b[2] - a[2] };
		double[] w = { o[0] - a[0], o[1] - a[1], o[2] - a[2] };
		double uu = dot(u, u), uv = dot(u, v), vv = dot(v, v), uw = dot(u, w), vw = dot(v, w);
		double denominator = uu * vv - uv * uv;
		// s runs along the aim (0..reach), t along the segment (0..1)
		double s = denominator < 1.0E-9 ? 0.0 : clamp((uv * vw - vv * uw) / denominator, 0.0, reach);
		double t = vv < 1.0E-9 ? 0.0 : clamp((vw + s * uv) / vv, 0.0, 1.0);
		s = uu < 1.0E-9 ? 0.0 : clamp((t * uv - uw) / uu, 0.0, reach);
		double dx = w[0] + u[0] * s - v[0] * t;
		double dy = w[1] + u[1] * s - v[1] * t;
		double dz = w[2] + u[2] * s - v[2] * t;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private static double dot(double[] a, double[] b) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}

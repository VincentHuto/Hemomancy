package com.vincenthuto.hemomancy.common.encounter.ordeal;

import net.minecraft.world.phys.Vec3;

/**
 * The Acrobat's ordeal as pure geometry (2026-11-17): what the Acrobat's rigging and the Veinwing Vulture's moves hit,
 * so each can be read and dodged. Positions are feet; {@code floor} is the top of the ring's plate.
 */
public final class AcrobatOrdealHits {
	/** The Acrobat's kinds in {@link OrdealHazardKinds}: their four interventions, then the Vulture's four moves. */
	public static final int KIND_SWING = OrdealHazardKinds.ACROBAT, KIND_DROP = KIND_SWING + 1, KIND_SNARE = KIND_SWING + 2,
			KIND_NET = KIND_SWING + 3, KIND_STOOP = KIND_SWING + 4, KIND_RAKE = KIND_SWING + 5, KIND_DOWNDRAFT = KIND_SWING + 6,
			KIND_CARRY = KIND_SWING + 7;

	/** Each intervention's wind-up, when it lands; the Acrobat's gestures are timed from these. */
	public static final int SWING_WINDUP = 24, DROP_WINDUP = 22, SNARE_WINDUP = 20, NET_WINDUP = 30;
	/** The swing's lane, half its length and half its width, and its crossing time. */
	public static final double SWING_HALF_LENGTH = 8.0D, SWING_HALF_WIDTH = 1.0D;
	public static final int SWING_TICKS = 14;
	/** The swing's bar over the plate: lowest in the middle, highest at the ends. */
	public static final double BAR_LOW = 0.9D, BAR_HIGH = 4.1D, BAR_REACH = 0.9D;
	public static final double DROP_RADIUS = 2.0D, DROP_HEIGHT = 15.0D;
	public static final double SNARE_RADIUS = 1.8D, NET_RADIUS = 3.5D, NET_HEIGHT = 12.0D;
	public static final float SWING_DAMAGE = 5.0F, DROP_DAMAGE = 6.0F, SNARE_DAMAGE = 4.0F, NET_DAMAGE = 3.0F;
	/** A body this tall stands under the bar. */
	public static final double BODY = 1.8D;

	/** The Vulture's moves' reach. */
	public static final double STOOP_RADIUS = 1.6D, STOOP_HEIGHT = 7.0D, RAKE_HALF_WIDTH = 1.0D, RAKE_SPEED = 0.95D,
			DOWNDRAFT_RADIUS = 3.2D, DOWNDRAFT_HEIGHT = 4.0D, CARRY_REACH = 2.4D;

	private AcrobatOrdealHits() {
	}

	/**
	 * Where the swing's bar is along its lane, {@code ticks} after it is let go: a pendulum, slow at the ends and
	 * fastest through the middle, from {@code -SWING_HALF_LENGTH} to {@code +SWING_HALF_LENGTH}.
	 */
	public static double swingAlong(double ticks) {
		double u = Math.min(1.0D, Math.max(0.0D, ticks / SWING_TICKS));
		return -SWING_HALF_LENGTH * Math.cos(Math.PI * u);
	}

	/** The bar's height over the plate at {@code along}: it dips to its lowest in the middle of the lane. */
	public static double barHeight(double along) {
		double t = along / SWING_HALF_LENGTH;
		return BAR_LOW + (BAR_HIGH - BAR_LOW) * t * t;
	}

	/**
	 * Whether the swinging bar, at {@code along} on the lane from {@code centre} along {@code direction}, hits feet at
	 * {@code feet}: inside the lane's width, near the bar along it, and with the bar between their feet and their head.
	 */
	public static boolean swingHits(Vec3 centre, Vec3 direction, double along, double floor, Vec3 feet) {
		Vec3 d = flat(direction);
		if (d.lengthSqr() < 1.0E-6D) return false;
		double dx = feet.x - centre.x, dz = feet.z - centre.z;
		double onLane = dx * d.x + dz * d.z, across = Math.abs(-dx * d.z + dz * d.x);
		if (across > SWING_HALF_WIDTH || Math.abs(onLane - along) > BAR_REACH) return false;
		double bar = floor + barHeight(along), bottom = feet.y;
		return bar > bottom - 0.3D && bar < bottom + BODY + 0.1D;
	}

	/** Where the drop is aimed: the fighter's feet led ahead by {@code lead} ticks of their motion, on the plate. */
	public static Vec3 lead(Vec3 feet, Vec3 motion, double lead, double floor) {
		return new Vec3(feet.x + motion.x * lead, floor, feet.z + motion.z * lead);
	}

	/** How high the falling double is over its mark {@code ticks} into the drop's wind-up: slow, then a plummet. */
	public static double dropHeight(double ticks) {
		double w = DROP_WINDUP, hang = w - 4.0D;
		if (ticks <= hang) return DROP_HEIGHT - 3.0D * ticks / hang;
		double t = Math.min(1.0D, (ticks - hang) / 4.0D);
		return (DROP_HEIGHT - 3.0D) * (1.0D - t * t);
	}

	/** A disc on the plate: within {@code radius} across and standing on it (not far over it, not under it). */
	public static boolean discHits(Vec3 mark, double radius, Vec3 feet) {
		return horizontal(mark, feet) <= radius && feet.y - mark.y < 2.5D && feet.y >= mark.y - 0.5D;
	}

	/** The snare's yank: up and toward the Acrobat. */
	public static Vec3 snareYank(Vec3 feet, Vec3 acrobat) {
		Vec3 toward = flat(acrobat.subtract(feet));
		return new Vec3(toward.x * 1.1D, 1.15D, toward.z * 1.1D);
	}

	/** Where the Vulture's rake has got to {@code ticks} into its pass from {@code start} along {@code aim}. */
	public static Vec3 rakeAt(Vec3 start, Vec3 aim, double ticks) {
		return start.add(flat(aim).scale(RAKE_SPEED * Math.max(0.0D, ticks)));
	}

	/** Whether the stoop's next step stays over the ring: it never dives out past the curb. */
	public static boolean insideRing(Vec3 centre, Vec3 at, double margin) {
		return horizontal(centre, at) <= OrdealStage.RING_RADIUS - margin;
	}

	/** How high over the plate the stooping Vulture is {@code ticks} into its 5-tick dive: accelerating. */
	public static double stoopHeight(double ticks) {
		double t = Math.min(1.0D, Math.max(0.0D, ticks / 5.0D));
		return STOOP_HEIGHT * (1.0D - t * t);
	}

	/** The downdraft's shove on feet inside its disc: out from under the Vulture and pressed down. */
	public static Vec3 downdraftShove(Vec3 under, Vec3 feet) {
		Vec3 out = flat(feet.subtract(under));
		return new Vec3(out.x, -0.8D, out.z);
	}

	public static Vec3 flat(Vec3 v) {
		Vec3 f = new Vec3(v.x, 0.0D, v.z);
		return f.lengthSqr() < 1.0E-6D ? Vec3.ZERO : f.normalize();
	}

	public static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}

package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The lash, the same arithmetic on the server and on every client. A held lash runs through four phases. While it is
 * {@link Phase#CHARGING} nothing is out: blood gathers in the hand and the charge climbs. A flick of the look throws
 * it ({@link Phase#THROWING}): the strand crosses an arc through the old aim in the flick's direction and cracks where
 * it first meets something, or at the arc's end, carrying the charge. Then it is {@link Phase#LIVE}, a chain of nodes
 * that keep their momentum and are drawn toward the whip aim, so the tip trails, overshoots and snaps. A hand at rest
 * lets it dissolve back ({@link Phase#FADING}); a flick during the fade picks it up again.
 *
 * <p>The whip aim is not the look: the casting client turns it with the whole of the mouse while the view takes a
 * capped share, and the server holds the copy every hit is decided from. Damage comes from the tip's speed, between a
 * floor and a cap, and only while the stroke is fresh: a stroke keeps its full damage for its first quarter turn.
 * Back and forth is strong; spinning in place is weak.
 */
public final class LashRules {
	public static final int NODES = 10;
	/** A lash reaches this far past the verb's reach: about 7.5 blocks bare, short of the R cast's 12. */
	public static final double REACH_BONUS = 2.0D;
	/** How much of the way to its place the node at the hand is pulled each tick; the tip's share is the tuning's. */
	public static final double NEAR_PULL = 0.85D;
	/** How much tighter the strand is drawn through a throw, so it crosses its arc and does not trail behind it. */
	public static final double THROW_TAUT = 2.5D;
	/** The most a link may stretch past its rest length. */
	public static final double STRETCH = 1.2D;
	public static final float FLOOR_DAMAGE = 3.0F, CAP_DAMAGE = 6.0F;
	/** Degrees a stroke may turn at full damage. */
	public static final double FRESH_TURN = 90.0D;
	/** How far the new direction must turn back against the stroke to count as a reversal (cosine). */
	public static final double REVERSAL_DOT = -0.2D;
	public static final float CRACK_MULTIPLIER = 1.5F;
	/** Ticks between free cracks from a stroke, and the shorter wait a paid snap keeps. */
	public static final int CRACK_COOLDOWN = 20, SNAP_COOLDOWN = 10;
	/** How close to the tip a crack reaches, and how thick the strand is for its sweeps. */
	public static final double CRACK_RADIUS = 1.25D, STRAND_RADIUS = 0.35D;

	private static volatile LashTuning tuning = LashTuning.DEFAULT;

	private LashRules() {
	}

	public static LashTuning tuning() {
		return tuning;
	}

	/** Swaps the feel numbers while the game runs: the tuning client's seam. Null restores the defaults. */
	public static void setTuning(LashTuning replacement) {
		tuning = replacement == null ? LashTuning.DEFAULT : replacement;
	}

	public enum Phase { CHARGING, THROWING, LIVE, FADING }

	/**
	 * Whether a left click with a lash selected is left to vanilla rather than tapped: aimed at a block, at a body that
	 * is no creature (a boat, an item frame, an end crystal), or at a creature while the Projection tool is in the off
	 * hand and the main hand holds something of its own, whose attack the click is.
	 */
	public static boolean clickStaysVanilla(boolean onBlock, boolean onEntity, boolean onCreature, boolean toolInMainHand,
			boolean mainHandEmpty) {
		if (onBlock) return true;
		if (!onEntity) return false;
		return !onCreature || !toolInMainHand && !mainHandEmpty;
	}

	/** Where a node may go: the point a segment from {@code from} toward {@code to} stops at. */
	@FunctionalInterface
	public interface Clipper {
		Clipper NONE = (from, to) -> to;

		Vec3 clip(Vec3 from, Vec3 to);
	}

	/** One lash's running state. */
	public static final class State {
		/** Whether this copy throws on a flick it reads from the look itself; the server's is told ({@link #flick}). */
		final boolean readsFlicks;
		final Vec3[] nodes = new Vec3[NODES];
		final Vec3[] previous = new Vec3[NODES];
		boolean seeded;
		Phase phase = Phase.CHARGING;
		int chargeTicks;
		double charge;
		double extent = 1.0D;
		float aimYaw, aimPitch;
		boolean hasLook;
		float lastLookYaw, lastLookPitch;
		float throwFromYaw, throwFromPitch, throwToYaw, throwToPitch;
		float throwCentreYaw, throwCentrePitch;
		double throwX, throwY;
		int throwTick;
		double throwCharge;
		int restTicks;
		double strokeTurn, strokeX, strokeY;
		long lastCrack = Long.MIN_VALUE / 2;

		/** A copy that reads its own flicks: the casting client's, and a watcher's guess. */
		public State() {
			this(true);
		}

		public State(boolean readsFlicks) {
			this.readsFlicks = readsFlicks;
		}

		/** The look the last throw's arc was centred on, and the unit turn it was thrown along. */
		public float throwCentreYaw() {
			return throwCentreYaw;
		}

		public float throwCentrePitch() {
			return throwCentrePitch;
		}

		public double throwX() {
			return throwX;
		}

		public double throwY() {
			return throwY;
		}

		public Phase phase() {
			return phase;
		}

		/** How far through its arc a throw is, 0 to 1; 1 at any other time. */
		public double throwProgress() {
			return phase == Phase.THROWING ? Math.min(1.0D, throwTick / (double) tuning.throwTicks()) : 1.0D;
		}

		/** The charge a throw in flight carries to its crack; 0 at any other time. */
		public double throwCharge() {
			return phase == Phase.THROWING ? throwCharge : 0.0D;
		}

		/** How full the orb is, 0 to 1. */
		public double charge() {
			return charge;
		}

		/** How much of its full length the strand has, 0 to 1: it shrinks through a fade and regrows on a revival. */
		public double extent() {
			return extent;
		}

		public float aimYaw() {
			return aimYaw;
		}

		public float aimPitch() {
			return aimPitch;
		}

		public Vec3 tip() {
			return nodes[NODES - 1];
		}

		public Vec3 node(int i) {
			return nodes[i];
		}

		public Vec3 previous(int i) {
			return previous[i];
		}

		/** Whether the strand has nodes out; false while the orb gathers. */
		public boolean seeded() {
			return seeded;
		}

		public double strokeTurn() {
			return strokeTurn;
		}

		public long lastCrack() {
			return lastCrack;
		}
	}

	/**
	 * One tick's outcome: the phase after it, the tip's speed in blocks a tick, the turn rate in degrees a tick (the
	 * look's while charging, else the whip aim's), whether this tick threw the lash, whether it cracked and the charge
	 * that crack carries, whether a block stopped the tip, whether the stroke is past its fresh quarter turn, and
	 * whether the aim was at rest.
	 */
	public record Step(Phase phase, double tipSpeed, double turnRate, boolean threw, boolean crack, double crackCharge,
			boolean tipBlocked, boolean tired, boolean resting) {
	}

	/** The lash's full length for a verb reach. */
	public static double length(double reach) {
		return Math.max(1.0D, reach + REACH_BONUS);
	}

	/** The unit vector an entity's yaw and pitch (degrees) look along. */
	public static Vec3 direction(float yaw, float pitch) {
		double y = Math.toRadians(yaw), p = Math.toRadians(pitch);
		double cos = Math.cos(p);
		return new Vec3(-Math.sin(y) * cos, -Math.sin(p), Math.cos(y) * cos);
	}

	/** How much of the way to its place node {@code i} is pulled each tick; {@code taut} above 1 tightens the tip. */
	public static double pull(int i, double taut) {
		double t = (i + 1) / (double) NODES;
		double tip = Math.min(1.0D, tuning.aimPull() * Math.max(0.1D, taut));
		return Math.max(0.0D, Math.min(1.0D, NEAR_PULL + (tip - NEAR_PULL) * t));
	}

	/**
	 * Advances the lash one tick. {@code lookYaw} and {@code lookPitch} are the caster's view; {@code aimYaw} and
	 * {@code aimPitch} the whip aim, read only while the lash is live or fading (the rules set the aim themselves while
	 * it charges and through the throw).
	 */
	public static Step step(State state, Vec3 hand, float lookYaw, float lookPitch, float aimYaw, float aimPitch,
			double length, long now, Clipper clipper, double taut, LashProfile profile) {
		return step(state, hand, lookYaw, lookPitch, aimYaw, aimPitch, length, now, clipper, taut, profile, true);
	}

	/**
	 * {@link #step} for a copy whose whip aim arrives from elsewhere and may miss a tick. With {@code aimKnown} false
	 * the aim is not news: a live lash keeps swinging on its momentum and its stroke is neither advanced nor rested (a
	 * tick with no word is not a hand at rest), and a fading lash goes on fading.
	 */
	public static Step step(State state, Vec3 hand, float lookYaw, float lookPitch, float aimYaw, float aimPitch,
			double length, long now, Clipper clipper, double taut, LashProfile profile, boolean aimKnown) {
		LashTuning t = tuning;
		// The lash's own rope: how hard it answers the hand, how much it carries, how long it lingers.
		LashStyle style = profile.style();
		taut *= style.taut();
		double damping = Math.min(0.99D, t.damping() * style.damping());
		float beforeYaw = state.lastLookYaw, beforePitch = state.lastLookPitch;
		boolean hadLook = state.hasLook;
		double lookX = hadLook ? wrapDegrees(lookYaw - beforeYaw) : 0.0D;
		double lookY = hadLook ? lookPitch - beforePitch : 0.0D;
		state.lastLookYaw = lookYaw;
		state.lastLookPitch = lookPitch;
		state.hasLook = true;
		return switch (state.phase) {
			case CHARGING -> {
				double rate = Math.sqrt(lookX * lookX + lookY * lookY);
				if (state.readsFlicks && hadLook && rate >= t.flickTurn()) {
					beginThrow(state, hand, beforeYaw, beforePitch, lookX / rate, lookY / rate, length, clipper, t);
					yield new Step(Phase.THROWING, 0.0D, rate, true, false, 0.0D, false, false, false);
				}
				state.aimYaw = lookYaw;
				state.aimPitch = lookPitch;
				state.chargeTicks++;
				state.charge = Math.min(1.0D, state.chargeTicks / (double) profile.chargeTicks());
				yield new Step(Phase.CHARGING, 0.0D, rate, false, false, 0.0D, false, false, rate < t.restTurn());
			}
			case THROWING -> {
				state.throwTick++;
				double f = Math.min(1.0D, state.throwTick / (double) t.throwTicks());
				double turn = t.arcDegrees() / t.throwTicks();
				state.aimYaw = (float) (state.throwFromYaw + wrapDegrees(state.throwToYaw - state.throwFromYaw) * f);
				state.aimPitch = (float) (state.throwFromPitch + (state.throwToPitch - state.throwFromPitch) * f);
				boolean blocked = rope(state, hand, length, clipper, taut * THROW_TAUT, damping, t);
				double speed = tipSpeed(state);
				if (f >= 1.0D) {
					double carried = land(state, now);
					yield new Step(Phase.LIVE, speed, turn, false, true, carried, blocked, false, false);
				}
				yield new Step(Phase.THROWING, speed, turn, false, false, 0.0D, blocked, false, false);
			}
			case LIVE -> {
				state.extent = Math.min(1.0D, state.extent + 1.0D / t.regrowTicks());
				if (!aimKnown) {
					boolean blocked = rope(state, hand, length, clipper, taut, damping, t);
					yield new Step(Phase.LIVE, tipSpeed(state), 0.0D, false, false, 0.0D, blocked, state.strokeTurn > FRESH_TURN, false);
				}
				double dx = wrapDegrees(aimYaw - state.aimYaw), dy = aimPitch - state.aimPitch;
				state.aimYaw = aimYaw;
				state.aimPitch = aimPitch;
				boolean blocked = rope(state, hand, length, clipper, taut, damping, t);
				yield stroke(state, dx, dy, tipSpeed(state), blocked, now, t);
			}
			case FADING -> {
				if (!aimKnown) {
					aimYaw = state.aimYaw;
					aimPitch = state.aimPitch;
				}
				double dx = wrapDegrees(aimYaw - state.aimYaw), dy = aimPitch - state.aimPitch;
				double rate = Math.sqrt(dx * dx + dy * dy);
				state.aimYaw = aimYaw;
				state.aimPitch = aimPitch;
				if (rate >= t.flickTurn()) {
					state.phase = Phase.LIVE;
					state.restTicks = 0;
					boolean blocked = rope(state, hand, length, clipper, taut, damping, t);
					yield stroke(state, dx, dy, tipSpeed(state), blocked, now, t);
				}
				state.extent -= 1.0D / Math.max(1.0D, t.fadeTicks() * style.fade());
				if (state.extent <= 0.0D) {
					toOrb(state);
					yield new Step(Phase.CHARGING, 0.0D, rate, false, false, 0.0D, false, false, true);
				}
				boolean blocked = rope(state, hand, length, clipper, taut, damping, t);
				yield new Step(Phase.FADING, tipSpeed(state), rate, false, false, 0.0D, blocked, false, rate < t.restTurn());
			}
		};
	}

	/**
	 * Lays the strand out for a throw: straight along the arc's start, which is half the arc back from the old aim
	 * against the flick, so the stroke passes through what the caster was looking at.
	 */
	private static void beginThrow(State state, Vec3 hand, float centreYaw, float centrePitch, double ux, double uy,
			double length, Clipper clipper, LashTuning t) {
		double half = t.arcDegrees() * 0.5D;
		state.throwCentreYaw = centreYaw;
		state.throwCentrePitch = centrePitch;
		state.throwX = ux;
		state.throwY = uy;
		state.throwFromYaw = (float) (centreYaw - ux * half);
		state.throwFromPitch = clampPitch(centrePitch - uy * half);
		state.throwToYaw = (float) (centreYaw + ux * half);
		state.throwToPitch = clampPitch(centrePitch + uy * half);
		state.throwTick = 0;
		state.throwCharge = state.charge;
		state.aimYaw = state.throwFromYaw;
		state.aimPitch = state.throwFromPitch;
		state.extent = 1.0D;
		state.phase = Phase.THROWING;
		seed(state, hand, length, clipper);
	}

	/**
	 * Throws a charging lash along the turn ({@code turnYaw}, {@code turnPitch}) with its arc centred on the given look:
	 * how the server, which reads no flicks of its own, is told of the caster's. A lash still dissolving is taken back
	 * first and thrown afresh with nothing gathered. Returns false, changing nothing, when the lash is mid-throw or live,
	 * or the turn has no direction.
	 */
	public static boolean flick(State state, Vec3 hand, float centreYaw, float centrePitch, double turnYaw, double turnPitch,
			double length, Clipper clipper) {
		double size = Math.sqrt(turnYaw * turnYaw + turnPitch * turnPitch);
		if (!(size > 1.0E-6D) || !Double.isFinite(size)) return false;
		if (state.phase == Phase.FADING) toOrb(state);
		if (state.phase != Phase.CHARGING) return false;
		beginThrow(state, hand, centreYaw, centrePitch, turnYaw / size, turnPitch / size, length, clipper, tuning);
		return true;
	}

	/**
	 * Brings a copy into line with the phase the server reports. A lash the server has out is laid out here if it is
	 * not; one the server has taken back is taken back. A throw is never started this way (its arc is not known), so a
	 * copy still charging waits for the server's next word.
	 */
	public static void adopt(State state, Phase phase, double charge, LashProfile profile) {
		if (phase == state.phase || phase == Phase.THROWING) {
			if (phase == Phase.CHARGING && state.phase == Phase.CHARGING) {
				state.chargeTicks = (int) Math.round(Math.max(0.0D, Math.min(1.0D, charge)) * profile.chargeTicks());
				state.charge = Math.min(1.0D, state.chargeTicks / (double) profile.chargeTicks());
			}
			return;
		}
		if (phase == Phase.CHARGING) {
			toOrb(state);
			return;
		}
		if (state.phase == Phase.CHARGING) state.seeded = false;
		state.charge = 0.0D;
		state.chargeTicks = 0;
		state.throwCharge = 0.0D;
		state.restTicks = 0;
		state.phase = phase;
	}

	/** The throw ends in its crack: the lash is live from here, and the charge is spent. Returns what it carried. */
	private static double land(State state, long now) {
		double carried = state.throwCharge;
		state.phase = Phase.LIVE;
		state.lastCrack = now;
		state.charge = 0.0D;
		state.chargeTicks = 0;
		state.throwCharge = 0.0D;
		state.restTicks = 0;
		state.strokeTurn = 0.0D;
		state.strokeX = 0.0D;
		state.strokeY = 0.0D;
		return carried;
	}

	private static void toOrb(State state) {
		state.phase = Phase.CHARGING;
		state.seeded = false;
		state.extent = 1.0D;
		state.charge = 0.0D;
		state.chargeTicks = 0;
		state.restTicks = 0;
		state.strokeTurn = 0.0D;
	}

	/** Lays the strand out straight along the aim, each node stopped where a block stops it. */
	private static void seed(State state, Vec3 hand, double length, Clipper clipper) {
		Vec3 dir = direction(state.aimYaw, state.aimPitch);
		double rest = length * Math.max(0.0D, state.extent) / NODES;
		Vec3 anchor = hand;
		for (int i = 0; i < NODES; i++) {
			Vec3 place = clipper.clip(anchor, hand.add(dir.scale(rest * (i + 1))));
			state.nodes[i] = place;
			state.previous[i] = place;
			anchor = place;
		}
		state.seeded = true;
	}

	/**
	 * Moves the chain one tick: each node carries its damped velocity, is drawn toward its place on the aim line (hard
	 * at the hand, lightly at the tip), and is held to the speed limit, to its link's stretch and out of blocks. Returns
	 * whether a block stopped the tip.
	 */
	private static boolean rope(State state, Vec3 hand, double length, Clipper clipper, double taut, double damping, LashTuning t) {
		if (!state.seeded) {
			seed(state, hand, length, clipper);
			return false;
		}
		Vec3 dir = direction(state.aimYaw, state.aimPitch);
		double rest = length * Math.max(0.0D, state.extent) / NODES;
		double most = rest * STRETCH;
		double fastest = t.maxNodeSpeed();
		Vec3 anchor = hand;
		boolean tipBlocked = false;
		for (int i = 0; i < NODES; i++) {
			Vec3 from = state.nodes[i];
			Vec3 moved = from.add(from.subtract(state.previous[i]).scale(damping));
			state.previous[i] = from;
			Vec3 place = hand.add(dir.scale(rest * (i + 1)));
			moved = moved.add(place.subtract(moved).scale(pull(i, taut)));
			Vec3 travel = moved.subtract(from);
			if (travel.lengthSqr() > fastest * fastest) moved = from.add(travel.normalize().scale(fastest));
			Vec3 link = moved.subtract(anchor);
			if (link.lengthSqr() > most * most) moved = anchor.add(link.normalize().scale(most));
			Vec3 clipped = clipper.clip(anchor, moved);
			if (i == NODES - 1 && clipped.distanceToSqr(moved) > 1.0E-8D) tipBlocked = true;
			state.nodes[i] = clipped;
			anchor = clipped;
		}
		return tipBlocked;
	}

	private static double tipSpeed(State state) {
		return state.nodes[NODES - 1].distanceTo(state.previous[NODES - 1]);
	}

	/**
	 * Reads the whip aim's turn this tick into the stroke: rest (a stroke that ends with the tip still flying cracks, and
	 * enough rest starts the fade), a fresh stroke, a reversal (a crack, if the tip is fast enough) or more of it.
	 */
	private static Step stroke(State state, double dx, double dy, double tipSpeed, boolean blocked, long now, LashTuning t) {
		double rate = Math.sqrt(dx * dx + dy * dy);
		boolean ready = tipSpeed >= t.crackSpeed() && now - state.lastCrack >= CRACK_COOLDOWN;
		if (rate < t.restTurn()) {
			boolean crack = state.strokeTurn > 0.0D && ready;
			if (crack) state.lastCrack = now;
			state.strokeTurn = 0.0D;
			state.strokeX = 0.0D;
			state.strokeY = 0.0D;
			if (++state.restTicks >= t.restTicksToFade()) state.phase = Phase.FADING;
			return new Step(state.phase, tipSpeed, rate, false, crack, 0.0D, blocked, false, true);
		}
		state.restTicks = 0;
		double ux = dx / rate, uy = dy / rate;
		boolean crack = false;
		boolean stroking = state.strokeTurn > 0.0D;
		if (stroking && ux * state.strokeX + uy * state.strokeY < REVERSAL_DOT) {
			if (ready) {
				crack = true;
				state.lastCrack = now;
			}
			state.strokeTurn = rate;
			state.strokeX = ux;
			state.strokeY = uy;
		} else {
			state.strokeTurn += rate;
			double bx = stroking ? state.strokeX * 0.7D + ux * 0.3D : ux;
			double by = stroking ? state.strokeY * 0.7D + uy * 0.3D : uy;
			double norm = Math.sqrt(bx * bx + by * by);
			state.strokeX = norm < 1.0E-6D ? ux : bx / norm;
			state.strokeY = norm < 1.0E-6D ? uy : by / norm;
		}
		return new Step(Phase.LIVE, tipSpeed, rate, false, crack, 0.0D, blocked, state.strokeTurn > FRESH_TURN, false);
	}

	/**
	 * The throw met something before its arc ran out: it cracks there and the lash is live. Returns the charge that
	 * crack carries, or -1 when the lash is not mid-throw.
	 */
	public static double struck(State state, long now) {
		if (state.phase != Phase.THROWING) return -1.0D;
		return land(state, now);
	}

	/**
	 * The attack button while a lash is held. From the orb it lets the charge go straight down the look and returns that
	 * charge; from a live or fading lash it brings the whip aim back to the look at full length and returns 0. Either
	 * way the lash is live along the look afterwards, and the crack is the caller's to land.
	 */
	public static double release(State state, float lookYaw, float lookPitch, long now) {
		double carried = state.phase == Phase.CHARGING ? state.charge : state.phase == Phase.THROWING ? state.throwCharge : 0.0D;
		if (state.phase == Phase.CHARGING) state.seeded = false;
		land(state, now);
		state.aimYaw = lookYaw;
		state.aimPitch = lookPitch;
		state.extent = 1.0D;
		return carried;
	}

	/** A sweep's damage for the tip's speed: nothing while slack, the floor once tired, else floor to cap by speed. */
	public static float sweepDamage(double tipSpeed, boolean tired) {
		LashTuning t = tuning;
		if (!(tipSpeed >= t.slackSpeed())) return 0.0F;
		if (tired) return FLOOR_DAMAGE;
		double f = Math.min(1.0D, (tipSpeed - t.slackSpeed()) / (t.fullSpeed() - t.slackSpeed()));
		return (float) (FLOOR_DAMAGE + (CAP_DAMAGE - FLOOR_DAMAGE) * f);
	}

	/** A crack's damage: the cap times the crack multiplier, raised by the charge it carries as the profile allows. */
	public static float crackDamage(double crackCharge, LashProfile profile) {
		return (float) (CAP_DAMAGE * CRACK_MULTIPLIER * profile.chargeScale(crackCharge));
	}

	/**
	 * Whether the attack button may crack the lash now: never mid-throw, never inside the snap's cooldown, and an orb
	 * only once it has gathered the tuning's least charge, so a click on a lash barely begun is not a free crack.
	 */
	public static boolean maySnap(State state, long now) {
		if (state.phase == Phase.THROWING || now - state.lastCrack < SNAP_COOLDOWN) return false;
		return state.phase != Phase.CHARGING || state.charge >= tuning.releaseMinCharge();
	}

	/**
	 * The swept segments of the outer half of the chain this tick: each outer node's travel, and the travel of the
	 * point midway between each pair of outer nodes, as {@code {from, to}} pairs. Nothing while the orb gathers.
	 */
	public static List<Vec3[]> sweeps(State state) {
		return sweeps(state, NODES / 2);
	}

	/**
	 * The swept segments of the whole chain this tick: what a throw meets. A thrown lash cracks on the first thing in
	 * its arc, a body at arm's length as surely as one out at the tip.
	 */
	public static List<Vec3[]> throwSweeps(State state) {
		return sweeps(state, 0);
	}

	private static List<Vec3[]> sweeps(State state, int first) {
		List<Vec3[]> sweeps = new ArrayList<>();
		if (!state.seeded || state.phase == Phase.CHARGING) return sweeps;
		for (int i = first; i < NODES; i++) {
			sweeps.add(new Vec3[] {state.previous[i], state.nodes[i]});
			if (i > first) {
				sweeps.add(new Vec3[] {state.previous[i - 1].lerp(state.previous[i], 0.5D),
						state.nodes[i - 1].lerp(state.nodes[i], 0.5D)});
			}
		}
		return sweeps;
	}

	private static float clampPitch(double pitch) {
		return (float) Math.max(-90.0D, Math.min(90.0D, pitch));
	}

	static double wrapDegrees(double degrees) {
		double wrapped = degrees % 360.0D;
		if (wrapped >= 180.0D) wrapped -= 360.0D;
		if (wrapped < -180.0D) wrapped += 360.0D;
		return wrapped;
	}
}

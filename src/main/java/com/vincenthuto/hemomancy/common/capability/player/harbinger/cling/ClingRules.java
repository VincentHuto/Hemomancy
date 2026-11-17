package com.vincenthuto.hemomancy.common.capability.player.harbinger.cling;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * The one set of cling numbers (core-loop Track 9), shared by Venous Cling and the Holdfast morphling: which faces
 * hold, how far a body may be from its face, how fast it crawls, and what it costs. No level in here.
 *
 * <p>A wall is a sturdy side face; a ceiling is a sturdy underside. A floor is not a cling: standing is already
 * holding on. Crawling maps the player's own walking input onto the surface: on a wall, toward the wall climbs and
 * away from it descends while sideways stays sideways; under a ceiling, walking input shuffles along it.
 */
public final class ClingRules {
	/** What a player clings to. */
	public enum Surface {
		NONE, WALL, CEILING;

		/** The surface a block face makes: its underside a ceiling, a side a wall, its top nothing. */
		public static Surface forFace(Direction face) {
			return switch (face) {
				case UP -> NONE;
				case DOWN -> CEILING;
				default -> WALL;
			};
		}
	}

	/** How far the body may stand off its face and still hold. */
	public static final double CONTACT = 0.35D;
	/** The push into the face each tick that keeps the body against it. */
	public static final double PRESS = 0.04D;
	/** The root itself, one pulse of Projection. */
	public static final double ROOT_BLOOD = 6.0D;
	public static final int ROOT_CADENCE = 20;
	/** Upkeep is paid once a second. */
	public static final int UPKEEP_INTERVAL = 20;
	/** The push off a wall when the player jumps. */
	public static final double JUMP_OFF = 0.32D;
	public static final double JUMP_UP = 0.42D;

	private ClingRules() {
	}

	/** Blood a second to stay rooted: a wall 4 mL, a ceiling 6 mL; Hematic Cleats halve both (CB-21). */
	public static double upkeepPerSecond(Surface surface, boolean cleats) {
		double base = switch (surface) {
			case NONE -> 0.0D;
			case WALL -> 4.0D;
			case CEILING -> 6.0D;
		};
		return cleats ? base * 0.5D : base;
	}

	/**
	 * Blocks a tick the player crawls: a slow 0.06 on a wall and 0.045 under a ceiling; Hematic Cleats bring a wall
	 * near walking pace (0.17, a walk is about 0.22) and a ceiling to 0.08.
	 */
	public static double speed(Surface surface, boolean cleats) {
		return switch (surface) {
			case NONE -> 0.0D;
			case WALL -> cleats ? 0.17D : 0.06D;
			case CEILING -> cleats ? 0.08D : 0.045D;
		};
	}

	/**
	 * The player's walking input as a horizontal direction, as vanilla turns it: {@code forward} and {@code strafe}
	 * in -1..1, turned by the body's yaw in degrees.
	 */
	public static Vec3 intent(float forward, float strafe, float yawDegrees) {
		double length = Math.sqrt(forward * forward + strafe * strafe);
		if (length < 1.0E-4D) return Vec3.ZERO;
		double f = forward / Math.max(1.0D, length);
		double s = strafe / Math.max(1.0D, length);
		double yaw = Math.toRadians(yawDegrees);
		double sin = Math.sin(yaw);
		double cos = Math.cos(yaw);
		return new Vec3(s * cos - f * sin, 0.0D, f * cos + s * sin);
	}

	/**
	 * The velocity of a clinging body this tick: its walking intent laid onto the surface at {@code speed}, plus a
	 * small push into the face. {@code face} is the block face held, pointing out of the block toward the player.
	 */
	public static Vec3 crawl(Direction face, Vec3 intent, double speed) {
		Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
		Surface surface = Surface.forFace(face);
		if (surface == Surface.CEILING) {
			return new Vec3(intent.x * speed, PRESS, intent.z * speed);
		}
		if (surface != Surface.WALL) return Vec3.ZERO;
		double into = -intent.dot(normal);
		Vec3 side = intent.subtract(normal.scale(intent.dot(normal)));
		Vec3 along = side.add(0.0D, into, 0.0D);
		if (along.lengthSqr() > 1.0D) along = along.normalize();
		return along.scale(speed).subtract(normal.scale(PRESS));
	}

	/** The jump off a surface: out and up from a wall, a short drop from a ceiling. */
	public static Vec3 jumpOff(Direction face) {
		if (Surface.forFace(face) == Surface.CEILING) return new Vec3(0.0D, -0.1D, 0.0D);
		return Vec3.atLowerCornerOf(face.getNormal()).scale(JUMP_OFF).add(0.0D, JUMP_UP, 0.0D);
	}

	/** Holdfast's cling (no crawl): the body does not slide down whatever it holds. */
	public static double holdfastVertical(double vertical) {
		return Math.max(vertical, 0.0D);
	}
}

package com.vincenthuto.hemomancy.client.render.world.rite;

import net.minecraft.world.phys.Vec3;

/**
 * The break crumbs of an offering the daemon broke: they stream from the break
 * into the daemon head first, then snake around its body in a trailing line,
 * coiling along its length as they circle and drawing slowly tighter, for as
 * long as it lasts. Positions are in the body's own frame (y runs along the
 * body from its middle toward its crown; x and z go round it), so the coil
 * turns and tips over with the daemon as it moves. Pure maths so it can be
 * tested without a client.
 */
public final class SwallowedFragmentRules {
	/** Ticks a crumb takes to fly from the break into the daemon. */
	public static final float TRAVEL_TICKS = 10.0F;
	/** Each crumb follows the one ahead of it by this many ticks, so they form a line. */
	private static final float TRAIL_DELAY = 0.25F;
	private static final float CONDENSE_TICKS = 120.0F;

	private SwallowedFragmentRules() {
	}

	/**
	 * Where crumb {@code index} sits in the body's frame, {@code age} ticks after the break. {@code start} is the
	 * break point in the body's frame; {@code girth} is how far round the body the coil winds and
	 * {@code halfLength} how far along it from the middle.
	 */
	public static Vec3 offset(float age, int index, double time, Vec3 start, double girth, double halfLength) {
		float own = age - index * TRAIL_DELAY;
		double condense = 1.0D - 0.4D * smooth(Math.max(0.0F, own - TRAVEL_TICKS) / CONDENSE_TICKS);
		// The line circles the body, each crumb a step behind the last, travelling up and down its length.
		double angle = time * 0.22D - index * 0.32D;
		double along = Math.sin(time * 0.07D - index * 0.12D) * 0.9D * halfLength;
		double around = girth * condense;
		Vec3 coil = new Vec3(Math.cos(angle) * around, along, Math.sin(angle) * around);
		return start.lerp(coil, smooth(own / TRAVEL_TICKS));
	}

	/**
	 * The body's frame for a daemon whose crown points along {@code crown} (a unit vector) and which faces
	 * {@code yaw} (radians, Minecraft's): {x axis, y axis (the crown), z axis}, each a unit vector in world axes.
	 */
	public static Vec3[] frame(Vec3 crown, double yaw) {
		Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
		Vec3 side = forward.subtract(crown.scale(forward.dot(crown)));
		if (side.lengthSqr() < 1.0E-8D) side = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
		side = side.normalize();
		return new Vec3[] {side, crown, side.cross(crown)};
	}

	public static Vec3 toWorld(Vec3 local, Vec3[] frame) {
		return frame[0].scale(local.x).add(frame[1].scale(local.y)).add(frame[2].scale(local.z));
	}

	public static Vec3 toLocal(Vec3 world, Vec3[] frame) {
		return new Vec3(world.dot(frame[0]), world.dot(frame[1]), world.dot(frame[2]));
	}

	/** Each crumb's half-size: it wears down as the daemon digests it, but never quite vanishes while it lasts. */
	public static float size(float age) {
		return (float) (0.05D * (1.0D - 0.4D * smooth(Math.max(0.0F, age - TRAVEL_TICKS) / CONDENSE_TICKS)));
	}

	private static double smooth(double t) {
		double c = Math.max(0.0D, Math.min(1.0D, t));
		return c * c * (3.0D - 2.0D * c);
	}
}

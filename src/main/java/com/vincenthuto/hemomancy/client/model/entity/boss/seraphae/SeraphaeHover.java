package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Seraphae's locomotion and floor contact as pure numbers, her answer to a gait: she never walks, she hovers 0.35
 * blocks over the floor and glides. Her velocity is read in her own frame (forward and to her right) so she leans into
 * a glide and banks into a slide; her hanging legs fold just enough that the soles never pass through the floor when
 * she sinks; a hanging panel of her gown flares out just enough that its hem lies on the floor instead of through it.
 * Model units throughout (render scale 2: eight to a block; the model's feet at y 24). JVM-tested.
 */
public final class SeraphaeHover {
	/** Model units in a block at render scale 2. */
	public static final float BLOCK = 8.0F;
	/** Her hover over the floor, in blocks (the server's {@code SeraphaeEntity.HOVER}). */
	public static final float HOVER = 0.35F;
	/** The floor's model y beneath her: her feet are at 24 and she hovers over it. */
	public static final float FLOOR = 24.0F + HOVER * BLOCK;
	/** Her full glide speed in blocks a tick (the server's {@code SeraphaeEntity.GLIDE_SPEED}). */
	public static final float GLIDE = 0.11F;
	/** The thigh and the shin to the sole, in model units. */
	public static final float THIGH = 5.5F, SHIN = 5.5F;
	/** A sole or a hem keeps this far over the floor. */
	public static final float CLEARANCE = 0.25F;
	/** The most the knees fold to clear the floor; per unit, the thigh comes forward this share of the fold. */
	public static final float FOLD_MAX = 2.2F, THIGH_SHARE = 0.4F;

	private SeraphaeHover() {
	}

	/**
	 * Her velocity in her own frame, from a tick's travel {@code dx, dz} and her body yaw (Minecraft degrees: 0 faces
	 * south): {forward, right}, blocks a tick.
	 */
	public static float[] local(double dx, double dz, float yawDegrees) {
		double yaw = Math.toRadians(yawDegrees);
		double fx = -Math.sin(yaw), fz = Math.cos(yaw);
		double rx = -fz, rz = fx;
		return new float[]{(float) (dx * fx + dz * fz), (float) (dx * rx + dz * rz)};
	}

	/** A speed as a share of her full glide, clamped to {@code [-limit, limit]}. */
	public static float glide(float speed, float limit) {
		return Math.max(-limit, Math.min(limit, speed / GLIDE));
	}

	/**
	 * How far an aim point lies off her facing, radians, positive to her right: {@code (ax, az)} from her origin, her
	 * body yaw in Minecraft degrees. Zero when the aim is on top of her.
	 */
	public static float bearing(double ax, double az, float yawDegrees) {
		float[] local = local(ax, az, yawDegrees);
		if (Math.abs(local[0]) + Math.abs(local[1]) < 1.0E-5F) return 0.0F;
		return (float) Math.atan2(local[1], local[0]);
	}

	/** How far below the hip a leg's sole hangs, from the thigh's pitch (negative forward) and the knee's bend. */
	public static float reach(float thigh, float knee) {
		return THIGH * (float) Math.cos(thigh) + SHIN * (float) Math.cos(thigh + knee);
	}

	/**
	 * The extra fold that lifts a hanging leg's sole to {@code available} under the hip (less {@link #CLEARANCE}): the
	 * knee bends back by the fold and the thigh comes forward by {@link #THIGH_SHARE} of it. Zero when it already clears.
	 */
	public static float foldToClear(float thigh, float knee, float available) {
		float room = available - CLEARANCE;
		if (reach(thigh, knee) <= room) return 0.0F;
		float lo = 0.0F, hi = FOLD_MAX;
		if (reach(thigh - hi * THIGH_SHARE, knee + hi) > room) return hi;
		for (int i = 0; i < 16; i++) {
			float mid = (lo + hi) * 0.5F;
			if (reach(thigh - mid * THIGH_SHARE, knee + mid) > room) lo = mid;
			else hi = mid;
		}
		return hi;
	}

	/**
	 * The extra outward tilt that lays a hanging panel's hem on the floor instead of through it. The panel hangs along
	 * a unit direction whose downward part is {@code down}; tilting it by an angle about a horizontal axis square to its
	 * outward direction moves that downward part along {@code down cos + out sin}, where {@code out} is the rate it
	 * starts to change (negative for a panel already leaning outward). {@code room} is how far below the pivot the floor
	 * lies and {@code length} the panel's length, model units. Zero when the hem already clears.
	 */
	public static float flare(float down, float out, float room, float length) {
		float floor = room - CLEARANCE;
		if (length * down <= floor || length < 1.0E-4F) return 0.0F;
		float reach = (float) Math.sqrt(down * down + out * out);
		float angle = (float) Math.atan2(out, down) + acos(floor / (length * reach));
		return Math.max(0.0F, angle);
	}

	/** 0 to 1 across one beat of a gesture that recurs every {@code period} ticks: in by b, held to c, out by d. */
	public static float every(float age, float period, float offset, float a, float b, float c, float d) {
		float into = age + offset;
		into -= (float) Math.floor(into / period) * period;
		return HemoEasing.window(into, a, b, c, d);
	}

	/** Which beat of a recurring gesture {@code age} falls in, for alternating sides. */
	public static int cycle(float age, float period, float offset) {
		return (int) Math.floor((age + offset) / period);
	}

	private static float acos(float cosine) {
		return (float) Math.acos(Math.max(-1.0F, Math.min(1.0F, cosine)));
	}
}

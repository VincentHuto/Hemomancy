package com.vincenthuto.hemomancy.common.world.ground;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The numbers of a field of dark (core-loop round two P8), pure. Blood Eclipse's Absorption draws the light out of what
 * it can put out, and the light taken hangs where it was aimed as a field of dark: the more light taken, the longer and
 * wider it hangs. Each player keeps one field; a snuff inside it adds to it, a snuff farther off starts a new one there.
 */
public final class DarkFieldRules {
	/** Ticks of dark for each level of block light taken. */
	public static final int TICKS_PER_LIGHT = 20;
	/** The least a light taken gives, for a dim one (a lit run of light 4 or less). */
	public static final int MIN_CHARGE = 40;
	/** The longest a field hangs, however much light is fed into it. */
	public static final int MAX_TICKS = 2_400;
	/** The smallest and largest radius, in blocks. */
	public static final double MIN_RADIUS = 3.0D, MAX_RADIUS = 8.0D;
	/** Each this many ticks a field has left widens it by a block, from {@link #MIN_RADIUS}. */
	public static final double TICKS_PER_BLOCK = 400.0D;
	/** The width of the field's thinning edge, in blocks: the client fog eases out across it. */
	public static final double SOFT_EDGE = 1.5D;
	/** The least share of its light a dry pull still hangs: Temper never refuses, so a dry field is shorter, never none. */
	public static final double DRY_FLOOR = 0.25D;

	private DarkFieldRules() {
	}

	/** The ticks of dark one light of {@code blockLight} gives when it is taken. */
	public static int charge(int blockLight) {
		return Math.max(MIN_CHARGE, Math.max(0, blockLight) * TICKS_PER_LIGHT);
	}

	/**
	 * The ticks a pulse that took {@code lightTicks} worth of light adds, at the pulse's {@code workScale} (a dry pull
	 * scales down to {@link #DRY_FLOOR}, the Staff's rate skills scale up). Zero only when nothing was taken.
	 */
	public static int fieldTicks(int lightTicks, double workScale) {
		if (lightTicks <= 0) return 0;
		double scale = Math.max(DRY_FLOOR, Double.isFinite(workScale) ? workScale : 1.0D);
		return Math.max(1, (int) Math.round(lightTicks * scale));
	}

	/** A field's time left after {@code added} ticks are fed into one with {@code remaining} left, capped. */
	public static long extended(long remaining, int added) {
		return Math.min(MAX_TICKS, Math.max(0L, remaining) + Math.max(0, added));
	}

	/** The radius of a field with {@code remainingTicks} left: it shrinks as it runs down. */
	public static double radius(long remainingTicks) {
		double radius = MIN_RADIUS + Math.max(0L, remainingTicks) / TICKS_PER_BLOCK;
		return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
	}

	/** Whether a snuff {@code distance} blocks from a live field's centre adds to it (true) or starts a new field there. */
	public static boolean joins(double distance, double radius) {
		return distance <= radius;
	}

	/** Whether a body {@code distance} blocks from the centre of a field of {@code radius} stands in it. */
	public static boolean inside(double distance, double radius) {
		return distance <= radius;
	}

	/**
	 * How deep in the dark a viewer {@code distance} from the centre of a field of {@code radius} is: 1 inside
	 * {@code radius - SOFT_EDGE}, easing to 0 at the radius and beyond.
	 */
	public static float depth(double distance, double radius) {
		if (!(radius > 0.0D) || !Double.isFinite(distance)) return 0.0F;
		double inner = Math.max(0.0D, radius - SOFT_EDGE);
		return (float) (1.0D - HemoEasing.smoothstep(inner, radius, distance));
	}
}

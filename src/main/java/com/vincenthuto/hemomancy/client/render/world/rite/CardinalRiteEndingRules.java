package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Timing for how a finished rite's boundary leaves the world, from the ghost a
 * terminal beat leaves behind. A collapse tears the rings apart segment by
 * segment; a completion draws them into the Focus; a cancellation simply fades
 * (its recall has already played). Pure maths, so JVM tests can cover it.
 */
public final class CardinalRiteEndingRules {
	/** Collapse and broken-structure endings. */
	public static final float VIOLENT_TICKS = 40.0F;
	public static final float COMPLETED_TICKS = 32.0F;
	public static final float CANCELLED_TICKS = 20.0F;
	/** Pieces of ring that tear away together. */
	public static final int RUPTURE_CHUNKS_PER_RING = 24;
	/** Ticks for the tear to travel once around a ring. */
	private static final float RUPTURE_SWEEP_TICKS = 12.0F;
	/** Ticks a torn chunk takes to sag, drift and drain away. */
	private static final float RUPTURE_TICKS = 14.0F;
	/** Ticks between successive sockets bursting. */
	private static final float SOCKET_BURST_STAGGER = 2.5F;

	private CardinalRiteEndingRules() {
	}

	public static boolean violent(CardinalRiteEventKind kind) {
		return kind == CardinalRiteEventKind.COLLAPSED || kind == CardinalRiteEventKind.BROKEN;
	}

	/** How long an ending of this kind lasts. */
	public static float duration(CardinalRiteEventKind kind) {
		if (kind == null) return 0.0F;
		if (violent(kind)) return VIOLENT_TICKS;
		if (kind == CardinalRiteEventKind.COMPLETED) return COMPLETED_TICKS;
		return CANCELLED_TICKS;
	}

	public static boolean finished(CardinalRiteEventKind kind, float age) {
		return kind != null && age >= duration(kind);
	}

	/**
	 * Overall opacity of what remains (stains, veins, sigils, landmarks). A
	 * collapse holds briefly while the rings tear, then everything drains.
	 */
	public static float fade(CardinalRiteEventKind kind, float age) {
		if (kind == null) return 1.0F;
		float hold = violent(kind) ? 8.0F : 0.0F;
		return 1.0F - HemoEasing.smoothstep((age - hold) / (duration(kind) - hold));
	}

	/** How far the dome has sunk into the floor, as a fraction of its radius. */
	public static float domeSink(CardinalRiteEventKind kind, float age) {
		if (kind == null) return 0.0F;
		return HemoEasing.easeInCubic(age / duration(kind)) * 1.05F;
	}

	/**
	 * Staff melt ticks for the ending, never behind the melt the rite had
	 * already reached. Collapse melts it fast; completion draws it away gently.
	 */
	public static float staffMeltTicks(CardinalRiteEventKind kind, float age, float liveTicks, float totalTicks) {
		if (kind == null) return liveTicks;
		float meltTicks = violent(kind) ? 22.0F : kind == CardinalRiteEventKind.COMPLETED ? 16.0F : 10.0F;
		return Math.max(liveTicks, totalTicks * HemoEasing.clamp01(age / meltTicks));
	}

	/**
	 * Rupture progress of one chunk of a ring: the tear starts at a seeded angle
	 * and runs around the ring, outer rings a little later, with some jitter.
	 *
	 * @param order  the chunk's position around the ring from the tear's start, 0 to 1
	 * @param jitter a stable per-chunk value in [0,1)
	 */
	public static float rupture(float age, int ring, float order, float jitter) {
		float delay = order * RUPTURE_SWEEP_TICKS + Math.min(Math.max(0, ring), 6) * 1.5F + jitter * 3.0F;
		return HemoEasing.clamp01((age - delay) / RUPTURE_TICKS);
	}

	/** Ticks after the ending began that a socket bursts. */
	public static float socketBurstDelay(int anchorIndex) {
		return 1.0F + anchorIndex * SOCKET_BURST_STAGGER;
	}

	/** Contraction of the rings into the Focus after a completion, 0 to 1. */
	public static float completionContraction(float age) {
		return HemoEasing.easeInCubic(age / COMPLETED_TICKS);
	}

	/** A stable hash in [0,1) for per-chunk and per-fragment variation. */
	public static float hash(int a, int b) {
		int value = a * 0x27D4EB2D ^ (b + 0x165667B1) * 0x61C88647;
		value ^= value >>> 15;
		value *= 0x2C1B3C6D;
		value ^= value >>> 12;
		return (value & 0xFFFFFF) / (float) 0x1000000;
	}
}

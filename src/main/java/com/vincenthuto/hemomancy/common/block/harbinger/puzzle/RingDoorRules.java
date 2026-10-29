package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * The ring door before a Saint's chamber. A Silentium opens it properly once every Chapel's node is lit, paying a
 * ring of blood for each; an Apotheos shatters it at any time, and the Saint then fights with every deed skipped.
 */
public final class RingDoorRules {
	public static final int RINGS = 3;
	public static final int BLOOD_PER_RING = 300;
	public static final int FALL_TICKS = 40;
	/** How far a door looks for its Chapels and its tomb: the wings run thirty-odd blocks out from the nave (Track 3). */
	public static final int CHAPEL_SEARCH = 64;
	/** The wings leave the nave at its floor, from its gallery seven up and down a flight into the crypt. */
	public static final int CHAPEL_SEARCH_HEIGHT = 16;
	public static final int SPREAD = 16;
	/** A lit node turns its ring to the seam over this long. */
	public static final int TURN_TICKS = 40;
	/** One ring fills with the Silentium's blood over this long. */
	public static final int FILL_TICKS = 40;
	/** Paid at the start of each ring's stage. */
	public static final int STAGE_COST = BLOOD_PER_RING;
	/** The crimson pulse through the seam once the inner ring is full. */
	public static final int PULSE_TICKS = 20;
	/** The slab sinks into the floor over this long. */
	public static final int SINK_TICKS = FALL_TICKS;
	/** The hyphae crack the slab over this long before it bursts. */
	public static final int SHATTER_TICKS = 30;
	/** The burst slab's fragments fall for this long. */
	public static final int BURST_TICKS = 30;
	/** How far the projecting Silentium may stand from the rings. */
	public static final double PROJECTOR_REACH = 16.0D;
	/** A scrambled ring never rests this close to the seam, so an unlit door never looks aligned. */
	public static final float REST_CLEARANCE = 30f;

	/** The projection's stages, in tick order: the three rings, the pulse, the sink, then done. */
	public static final int STAGE_PULSE = RINGS, STAGE_SINK = RINGS + 1, STAGE_DONE = RINGS + 2;

	private RingDoorRules() {}

	public static int turnTicks() {
		return TURN_TICKS;
	}

	public static int fillTicks() {
		return FILL_TICKS;
	}

	public static int stageCost() {
		return STAGE_COST;
	}

	public static int shatterTicks() {
		return SHATTER_TICKS;
	}

	/** The tick the pulse starts on: every ring full. */
	public static int pulseStart() {
		return RINGS * FILL_TICKS;
	}

	/** The tick the slab starts to sink on, and the tomb learns it was opened properly. */
	public static int sinkStart() {
		return pulseStart() + PULSE_TICKS;
	}

	/** The whole projection, from the click to the open door. */
	public static int projectionLength() {
		return sinkStart() + SINK_TICKS;
	}

	/** At {@code tick} of a projection: the ring (0 outer .. 2 inner) filling, then the pulse, sink and done stages. */
	public static int stageAt(int tick) {
		if (tick < pulseStart()) return Math.max(0, tick) / FILL_TICKS;
		if (tick < sinkStart()) return STAGE_PULSE;
		if (tick < projectionLength()) return STAGE_SINK;
		return STAGE_DONE;
	}

	/** The ring whose blood is due at exactly this tick of a projection, or -1. */
	public static int paysAt(int tick) {
		if (tick < 0 || tick >= pulseStart() || tick % FILL_TICKS != 0) return -1;
		return tick / FILL_TICKS;
	}

	/** How full {@code ring} is at {@code tick} of a projection, 0 to 1. */
	public static float ringFill(int ring, float tick) {
		return HemoEasing.clamp01((tick - ring * FILL_TICKS) / FILL_TICKS);
	}

	/** How far the pulse has run up the seam at {@code tick} of a projection, 0 to 1. */
	public static float pulse(float tick) {
		return HemoEasing.clamp01((tick - pulseStart()) / PULSE_TICKS);
	}

	/** How far the slab has sunk at {@code tick} of a projection, 0 to 1, eased. */
	public static float sink(float tick) {
		return HemoEasing.smoothstep((tick - sinkStart()) / SINK_TICKS);
	}

	/** How far the hyphae have cracked the slab at {@code tick} of a shattering, 0 to 1. */
	public static float crack(float tick) {
		return HemoEasing.clamp01(tick / SHATTER_TICKS);
	}

	/** How far a ring has turned from its rest to the seam, {@code ticksSinceLit} after its node lit; 0 to 1, eased. */
	public static float turn(float ticksSinceLit) {
		return HemoEasing.smoothstep(ticksSinceLit / TURN_TICKS);
	}

	/**
	 * The angle in degrees an unlit ring rests at, the same every time for one door ({@code seed} is its position):
	 * between {@link #REST_CLEARANCE} and 360 less it, so never near the seam.
	 */
	public static float restAngle(int ring, long seed) {
		long mixed = seed * 0x9E3779B97F4A7C15L + (ring + 1) * 0xC2B2AE3D27D4EB4FL;
		mixed = (mixed ^ (mixed >>> 33)) * 0xFF51AFD7ED558CCDL;
		mixed = (mixed ^ (mixed >>> 33)) * 0xC4CEB9FE1A85EC53L;
		mixed ^= mixed >>> 33;
		float unit = (mixed >>> 40) / (float) (1 << 24);
		return REST_CLEARANCE + unit * (360f - 2 * REST_CLEARANCE);
	}

	public static boolean mayProject(boolean silentium, int nodesLit) {
		return silentium && nodesLit >= RINGS;
	}

	public static boolean mayShatter(boolean apotheos) {
		return apotheos;
	}

	/** The Chapel kinds, slot by slot, that were not completed. */
	public static List<ChapelRules.Kind> skipped(List<ChapelRules.Kind> kinds, boolean[] completed) {
		List<ChapelRules.Kind> skipped = new ArrayList<>();
		for (int slot = 0; slot < kinds.size(); slot++) {
			if (slot >= completed.length || !completed[slot]) skipped.add(kinds.get(slot));
		}
		return skipped;
	}
}

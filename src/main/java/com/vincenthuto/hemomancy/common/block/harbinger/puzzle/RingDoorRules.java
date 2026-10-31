package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * The ring door before a Saint's chamber. It opens to held Blood Projection: {@link #OPEN_TICKS} fed ticks, each
 * paying {@link #BLOOD_PER_TICK}, and a door left alone bleeds its charge back off. A Silentium opens it properly once
 * every Chapel's node is lit; an Apotheos opens it by force at any time, and the Saint then fights with every deed
 * skipped.
 */
public final class RingDoorRules {
	public static final int RINGS = 3;
	/** Held projection to open the door: ten seconds. */
	public static final int OPEN_TICKS = 200;
	/** What the whole opening costs, on either path. */
	public static final int BLOOD_COST = 1000;
	/** Paid on each fed tick. */
	public static final int BLOOD_PER_TICK = BLOOD_COST / OPEN_TICKS;
	/** A door not fed for this many ticks starts to bleed its charge off. */
	public static final int FEED_GRACE = 4;
	/** While bleeding, the door loses a tick of charge this often: half as fast as it fills. */
	public static final int BLEED_TICKS = 2;
	public static final int FALL_TICKS = 40;
	/** How far a door looks for its Chapels and its tomb: the wings run thirty-odd blocks out from the nave (Track 3). */
	public static final int CHAPEL_SEARCH = 64;
	/** The wings leave the nave at its floor, from its gallery seven up and down a flight into the crypt. */
	public static final int CHAPEL_SEARCH_HEIGHT = 16;
	/** A lit node turns its ring to the seam over this long. */
	public static final int TURN_TICKS = 40;
	/** A Silentium's slab sinks into the floor over this long once the door is full. */
	public static final int SINK_TICKS = FALL_TICKS;
	/** The burst slab's fragments fall for this long. */
	public static final int BURST_TICKS = 30;
	/** A scrambled ring never rests this close to the seam, so an unlit door never looks aligned. */
	public static final float REST_CLEARANCE = 30f;
	/** Where each ring's channel starts filling, as a share of the charge, outer to inner; each fills over RING_SPAN. */
	private static final float[] RING_START = {0.22f, 0.42f, 0.62f};
	private static final float RING_SPAN = 0.22f;
	/** The share of the charge from which the pulse runs down the seam. */
	public static final float PULSE_START = 0.86f;

	private RingDoorRules() {}

	public static int turnTicks() {
		return TURN_TICKS;
	}

	/** How far along the opening a charge is, 0 to 1. */
	public static float progress(float charge) {
		return HemoEasing.clamp01(charge / OPEN_TICKS);
	}

	/** The charge after one tick: fed, it fills by one; unfed past the grace, it bleeds one every BLEED_TICKS. */
	public static int nextCharge(int charge, boolean fed, long ticksSinceFed) {
		if (fed) return Math.min(OPEN_TICKS, charge + 1);
		if (ticksSinceFed <= FEED_GRACE || ticksSinceFed % BLEED_TICKS != 0) return charge;
		return Math.max(0, charge - 1);
	}

	/** Whether the charge is being fed, from the ticks since its last feed. */
	public static boolean fed(long ticksSinceFed) {
		return ticksSinceFed <= FEED_GRACE;
	}

	/** How full a Silentium's {@code ring} channel is at {@code progress} of the opening, 0 to 1. */
	public static float ringFill(int ring, float progress) {
		return HemoEasing.clamp01((progress - RING_START[ring]) / RING_SPAN);
	}

	/** How far the pulse has run down the seam at {@code progress} of the opening, 0 to 1. */
	public static float pulse(float progress) {
		return HemoEasing.clamp01((progress - PULSE_START) / (1 - PULSE_START));
	}

	/** How far the slab has sunk {@code tick} ticks into its sinking, 0 to 1, eased. */
	public static float sink(float tick) {
		return HemoEasing.smoothstep(tick / SINK_TICKS);
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

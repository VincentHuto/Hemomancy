package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteFinaleTiming;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Timelines and shapes for the Cardinal Rite daemon, offering draws and culminations. Pure maths with no Minecraft
 * types so JVM tests can cover it; the renderers only place surfaces where these say. Visual timing never gates
 * gameplay: the server owns every phase and these only shape how its beats read.
 */
public final class CardinalRiteVisualRules {
	// ── Daemon ──
	/** Canonical daemon height at sprite scale 1, matching the entity's four-block form. */
	public static final double DAEMON_HEIGHT = 4.0D;
	/** Half-width unit of the canonical form: the shoulders reach this far either side. */
	public static final double DAEMON_HALF_WIDTH = 0.44D;
	/** Absorbed offerings shown inside the daemon; later ones replace the oldest. */
	public static final int MAX_NODULES = 12;
	/** Ticks an absorbed offering's item takes to sink into its nodule. */
	public static final float NODULE_SINK_TICKS = 30.0F;
	/** Ticks for the daemon to firm up once sealed. */
	public static final float SEAL_FIRM_TICKS = 20.0F;

	// ── Offering draw ──
	/** Ticks for the vessel to reach from the brazier to the daemon. */
	public static final float OFFERING_EXTEND_TICKS = 6.0F;
	/** Tick the offering leaves the brazier. */
	public static final float OFFERING_DEPART_TICKS = 3.0F;
	/** Tick the offering reaches the daemon: the server swells the daemon on this same tick. */
	public static final float OFFERING_ARRIVE_TICKS = CardinalRiteFinaleTiming.OFFERING_DRAW_TRAVEL_TICKS;
	/** Tick the vessel has fully withdrawn into the daemon. */
	public static final float OFFERING_RETRACT_END_TICKS = OFFERING_ARRIVE_TICKS + 8.0F;
	/** Last tick anything of a draw is drawn, including the arrival cells. */
	public static final float OFFERING_TOTAL_TICKS = OFFERING_ARRIVE_TICKS + 12.0F;

	// ── Culminations ──
	/** Ticks the server's culmination phase lasts before the rite completes. */
	public static final float CULMINATION_TICKS = CardinalRiteFinaleTiming.TOTAL_TICKS;
	/** When the daemon starts merging, within the culmination phase. */
	public static final float MERGE_START_TICKS = CardinalRiteFinaleTiming.GROWTH_TICKS;
	public static final float ASCENSION_TICKS = 60.0F;
	public static final float GROUNDING_TICKS = 50.0F;
	public static final float BINDING_TICKS = 56.0F;
	public static final float FORGING_TICKS = 48.0F;
	public static final float DOMAIN_TICKS = 70.0F;
	public static final float COLLAPSE_TICKS = 40.0F;

	public static final double ASCENSION_HEIGHT = 26.0D;
	public static final float ASCENSION_RISE_TICKS = 12.0F;
	public static final float ASCENSION_FRAY_START = 38.0F;
	public static final float GROUNDING_FRONT_TICKS = 26.0F;
	public static final double GROUNDING_REACH_PAST_FLOOR = 14.0D;
	public static final float BINDING_REACH_TICKS = 10.0F;
	public static final float BINDING_SNAP_TICKS = 38.0F;
	public static final double BINDING_RADIUS = 16.0D;
	public static final float FORGING_DROP_TICKS = 7.0F;
	public static final double FORGING_KNOT_HEIGHT = 2.4D;
	/** Three chunks: the zone a territorial rite claims. */
	public static final double DOMAIN_RADIUS = 48.0D;
	public static final float DOMAIN_SWELL_TICKS = 36.0F;

	private CardinalRiteVisualRules() {
	}

	// ── Easing ──

	/** Pulse shared with the consecrated floor, so the daemon beats with the membrane it stands over. */
	public static float heartbeat(double time) {
		return CardinalRiteFloorVisualRules.heartbeat((float) time);
	}

	/** Where the current heartbeat is in its cycle, 0 at the strong beat. */
	public static double heartbeatPhase(double time) {
		double period = CardinalRiteFloorVisualRules.HEARTBEAT_TICKS;
		double phase = (time % period) / period;
		return phase < 0 ? phase + 1 : phase;
	}

	// ── Daemon anatomy ──

	/**
	 * Silhouette half-width at a height fraction (0 feet, 1 crown), in half-width units: a frayed narrow base,
	 * a chest swelling to the shoulders, a pinched neck and a rounded head.
	 */
	public static double contour(double h) {
		if (h < 0.12) return HemoEasing.lerp(0.08, 0.25, h / 0.12);
		if (h < 0.58) return HemoEasing.lerp(0.25, 0.72, (h - 0.12) / 0.46);
		if (h < 0.72) return HemoEasing.lerp(0.72, 1.0, (h - 0.58) / 0.14);
		if (h < 0.78) return HemoEasing.lerp(1.0, 0.48, (h - 0.72) / 0.06);
		double head = (h - 0.89) / 0.11;
		return 0.08 + 0.48 * Math.sqrt(Math.max(0.0, 1.0 - head * head));
	}

	/** Body-axis height of a fraction, relative to the entity's centre. */
	public static double bodyY(double h, double scale) {
		return (h - 0.5) * DAEMON_HEIGHT * scale;
	}

	/** How firm the daemon reads, from loose smoke before sealing to a tight body after. */
	public static double firmness(float sealedAge, boolean pastSealing) {
		if (sealedAge >= 0) return HemoEasing.lerp(0.55, 1.0, HemoEasing.smoothstep((double) (sealedAge / SEAL_FIRM_TICKS)));
		return pastSealing ? 1.0 : 0.55;
	}

	/** Seeded position of an absorbed offering's nodule: height fraction, angle, radius in half-width units. */
	public static double noduleHeight(int index) {
		return 0.34 + 0.34 * hash(index * 7 + 3);
	}

	public static double noduleAngle(int index) {
		return index * 2.399963 + 0.7;
	}

	public static double noduleRadius(int index) {
		return contour(noduleHeight(index)) * (0.32 + 0.22 * hash(index * 13 + 5));
	}

	/** How far an absorbed item has sunk toward its nodule, 0 at the skin to 1 dissolved. */
	public static double noduleSink(float ageSinceArrival) {
		return HemoEasing.smoothstep((double) (ageSinceArrival / NODULE_SINK_TICKS));
	}

	// ── Offering draw ──

	/** How far the vessel has reached from the brazier, 0 to 1. */
	public static double offeringReach(float age) {
		return HemoEasing.smoothstep((double) (age / OFFERING_EXTEND_TICKS));
	}

	/** How far the vessel's brazier end has withdrawn toward the daemon, 0 to 1. */
	public static double offeringTail(float age) {
		return HemoEasing.smoothstep((double) ((age - OFFERING_ARRIVE_TICKS) / (OFFERING_RETRACT_END_TICKS - OFFERING_ARRIVE_TICKS)));
	}

	/** The offering's position along the vessel, 0 at the brazier to 1 inside the daemon. */
	public static double offeringTravel(float age) {
		return HemoEasing.easeInOutCubic((age - OFFERING_DEPART_TICKS) / (OFFERING_ARRIVE_TICKS - OFFERING_DEPART_TICKS));
	}

	/** Item scale: whole while carried, dissolving through the last quarter of the journey. */
	public static double offeringItemScale(float age) {
		if (age < OFFERING_DEPART_TICKS) return HemoEasing.smoothstep((double) (age / OFFERING_DEPART_TICKS));
		return 1.0 - HemoEasing.smoothstep((offeringTravel(age) - 0.75) / 0.25);
	}

	/** Brazier-end flare, bright on the draw and spent by the time the item is halfway. */
	public static double offeringFlare(float age) {
		return HemoEasing.smoothstep(age / 2.0) * (1.0 - HemoEasing.smoothstep((age - 4.0) / 12.0));
	}

	/** Arrival burst at the daemon. */
	public static double offeringArrival(float age) {
		double since = age - OFFERING_ARRIVE_TICKS + 1.0;
		if (since < 0) return 0;
		return HemoEasing.smoothstep(since / 2.0) * (1.0 - HemoEasing.smoothstep((since - 2.0) / (OFFERING_TOTAL_TICKS - OFFERING_ARRIVE_TICKS - 2.0)));
	}

	public static boolean offeringVisible(float age) {
		return age >= 0 && age < OFFERING_TOTAL_TICKS;
	}

	/**
	 * A point on the low arc from the brazier ({@code a}) to the daemon ({@code b}): the vessel dips out of the
	 * brazier, runs just over the floor, then climbs into the daemon. Writes x, y, z into {@code out}.
	 */
	public static void offeringArc(double ax, double ay, double az, double bx, double by, double bz, double floorY,
			double t, double[] out) {
		double low = Math.min(ay, floorY + 0.3);
		double p1x = HemoEasing.lerp(ax, bx, 0.30), p1y = low, p1z = HemoEasing.lerp(az, bz, 0.30);
		double p2x = HemoEasing.lerp(ax, bx, 0.72), p2y = low + 0.15 + Math.max(0, by - floorY) * 0.12, p2z = HemoEasing.lerp(az, bz, 0.72);
		double u = 1 - t;
		double c0 = u * u * u, c1 = 3 * u * u * t, c2 = 3 * u * t * t, c3 = t * t * t;
		out[0] = ax * c0 + p1x * c1 + p2x * c2 + bx * c3;
		out[1] = ay * c0 + p1y * c1 + p2y * c2 + by * c3;
		out[2] = az * c0 + p1z * c1 + p2z * c2 + bz * c3;
	}

	// ── Culminations ──

	/** Final fade shared by every culmination: full, then out over the last stretch. */
	public static double fadeOut(float age, float total, float fadeTicks) {
		return 1.0 - HemoEasing.smoothstep((double) ((age - (total - fadeTicks)) / fadeTicks));
	}

	public static double ascensionHeight(float age) {
		return ASCENSION_HEIGHT * HemoEasing.easeOutCubic((double) (age / ASCENSION_RISE_TICKS));
	}

	/** Fray: widths grow, alpha falls and strands drift upward. */
	public static double ascensionFray(float age) {
		return HemoEasing.smoothstep((double) ((age - ASCENSION_FRAY_START) / (ASCENSION_TICKS - ASCENSION_FRAY_START)));
	}

	/** The thin thread that rises through the caster while the daemon merges, before the rite completes. */
	public static double ascensionThread(float culminationAge) {
		return HemoEasing.smoothstep((double) ((culminationAge - MERGE_START_TICKS) / (CULMINATION_TICKS - MERGE_START_TICKS)));
	}

	public static double groundingFront(float age, double floorRadius) {
		return (Math.max(1.0, floorRadius) + GROUNDING_REACH_PAST_FLOOR) * HemoEasing.easeOutCubic((double) (age / GROUNDING_FRONT_TICKS));
	}

	public static double bindingReach(float age) {
		return HemoEasing.smoothstep((double) (age / BINDING_REACH_TICKS));
	}

	/** 0 while the vessels hold, rising to 1 as they snap and recoil. */
	public static double bindingSnap(float age) {
		return HemoEasing.smoothstep((age - BINDING_SNAP_TICKS) / 10.0);
	}

	/** Knot height above the Focus: hovering through the culmination, then dropping on completion. */
	public static double forgingKnotHeight(float completedAge) {
		if (completedAge < 0) return FORGING_KNOT_HEIGHT;
		return HemoEasing.lerp(FORGING_KNOT_HEIGHT, 0.12, HemoEasing.easeInCubic((double) (completedAge / FORGING_DROP_TICKS)));
	}

	/** How condensed the forging knot is during the culmination, 0 to 1. */
	public static double forgingCondense(float culminationAge) {
		return HemoEasing.smoothstep((double) (culminationAge / CULMINATION_TICKS));
	}

	/** Flash after the knot lands. */
	public static double forgingFlash(float completedAge) {
		double since = completedAge - FORGING_DROP_TICKS;
		if (since < 0) return 0;
		return 1.0 - HemoEasing.smoothstep(since / 12.0);
	}

	public static double domainRadius(float age) {
		return DOMAIN_RADIUS * HemoEasing.easeOutCubic((double) (age / DOMAIN_SWELL_TICKS));
	}

	/** How far the swollen dome has sunk into the ground, 0 to 1. */
	public static double domainSink(float age) {
		return HemoEasing.smoothstep((double) ((age - DOMAIN_SWELL_TICKS) / (DOMAIN_TICKS - DOMAIN_SWELL_TICKS)));
	}

	/** Distance a collapse cell has flown, with a slight drop. */
	public static double collapseReach(float age, double speed) {
		return speed * HemoEasing.easeOutCubic(age / 18.0) * 3.2;
	}

	// ── Helpers ──

	/** Deterministic hash in [0, 1). */
	public static double hash(int n) {
		long x = n * 0x9E3779B97F4A7C15L;
		x ^= x >>> 31;
		x *= 0xBF58476D1CE4E5B9L;
		x ^= x >>> 29;
		return (x >>> 11) * 0x1.0p-53;
	}
}

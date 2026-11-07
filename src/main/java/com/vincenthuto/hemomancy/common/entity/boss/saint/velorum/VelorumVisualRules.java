package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Saint Velorum's presentation timelines for the Frozen Cruor and liquid dark revision (2026-11-06, design R.2, R.3,
 * R.7, R.11), free of Minecraft types like {@link CruorJavelinRules} so the JVM can test them: the lantern's light
 * against the dark, a pool's growth, the javelins' draw, form, flight, lodge and shatter, the Arrested Pulse's mark and
 * beats, the Shadow Step's sink and rise, the gloam cuts, the Eclipse's shapes and its light going out, and the stains.
 * Lengths are blocks and times ticks. The pure server rules ({@code VelorumDarkRules}, {@code VelorumPulseRules},
 * {@code VelorumEclipseRules}) own the numbers that strike; these only say how they are shown, and where the two meet
 * (the lantern's radius, a pool's radius) the numbers are the design's own so the drawn edge is the tested edge.
 * The old {@code VelorumVisualRules} of the first build is in {@code removed_assets/}; this is a new file.
 */
public final class VelorumVisualRules {
	// ── cue packing ──────────────────────────────────────────────────

	/** A cue's beat lives in the low bits of the packet's count; an entity it is about rides above them, plus one. */
	public static final int KIND_BITS = 6, KIND_MASK = (1 << KIND_BITS) - 1;

	private VelorumVisualRules() {
	}

	/** The count a cue carries: its beat, and the entity it is about (or -1 for none). */
	public static int pack(int kind, int entityId) {
		return (kind & KIND_MASK) | ((entityId + 1) << KIND_BITS);
	}

	public static int kind(int count) {
		return count & KIND_MASK;
	}

	/** The entity a packed cue is about, or -1. */
	public static int entity(int count) {
		return (count >>> KIND_BITS) - 1;
	}

	// ── the lantern's light against the dark (R.2) ───────────────────

	/** {@code L} at rest by phase: 6, 5, 4 blocks. */
	public static float lanternRest(int phase) {
		return phase >= 3 ? 4.0F : phase == 2 ? 5.0F : 6.0F;
	}

	public static final float NIGHT_LIGHT = 3.0F, GUTTER_LIGHT = 1.0F, RELIT_LIGHT = 8.0F;
	public static final int GUTTER_FALL = 10, GUTTER_RETURN = 20, RELIT_FLARE = 20, POOL_RETURN = 40;

	/**
	 * The lantern's radius {@code L}. {@code nightBuild} is the Long Night's wind-up, 0 to 1 (or 0); {@code gutter} the
	 * ticks into an open Guttering Lantern (or -1), {@code sinceGutter} the ticks since one closed (or -1); {@code eclipse}
	 * how far the Eclipse's light has gone out, 0 to 1; {@code relit} the ticks since it was relit (or -1).
	 */
	public static float lanternRadius(int phase, float nightBuild, float gutter, float sinceGutter, float eclipse, float relit) {
		// the server's own rule, so the drawn edge of his light is the edge the dark is tested against
		return VelorumDarkRules.lanternRadius(phase, nightBuild, gutter, sinceGutter, eclipse, relit);
	}

	/** How much dark is pushed back at {@code distance} from the lantern's floor point: 1 inside L, torn over a block. */
	public static float lit(double distance, double lantern) {
		if (lantern <= 0.0D) return 0.0F;
		return (float) HemoEasing.smoothstep(HemoEasing.clamp01((lantern + 0.6D - distance) / 1.2D));
	}

	// ── pools (R.3) ──────────────────────────────────────────────────

	/** The Gutter's flood: every live pool grows this much a tick toward FLOOD_CAP. */
	public static final double FLOOD_RATE = 0.25D, FLOOD_CAP = 7.0D;

	/** How many ticks a pool of cap radius {@code cap} grows over, by its source (R.3's table). */
	public static int poolGrowTicks(double cap) {
		return VelorumDarkRules.growTicks(cap);
	}

	/** A pool's radius at {@code age}, grown over its source's ticks with an ease-out, before any flood (the server's rule). */
	public static double poolRadius(float age, double cap) {
		return VelorumDarkRules.ownRadius(cap, age);
	}

	/** The same through the Gutter's flood: {@code flood} ticks of +0.25 a tick toward 7, easing back over 40 after it. */
	public static double floodedRadius(double radius, float flood, float sinceFlood) {
		return VelorumDarkRules.floodedRadius(radius, flood, sinceFlood);
	}

	/** A pool's coverage at {@code distance} from its centre: whole inside, torn away over the last 0.9 of its radius. */
	public static float poolCover(double distance, double radius) {
		if (radius <= 0.05D) return 0.0F;
		return (float) HemoEasing.clamp01((radius - distance) / Math.min(0.9D, radius * 0.5D));
	}

	/** Torn smoke cards round a pool's rim: one every 0.9 blocks of rim, at least five. */
	public static int rimTatters(double radius) {
		return Math.max(5, (int) Math.round(Math.PI * 2.0D * radius / 0.9D));
	}

	/** A tatter's life: it lifts off the rim, frays and tears away over TATTER ticks, then a new one rises. */
	public static final float TATTER = 36.0F;

	/** A tatter's {height, torn} at phase 0..1 of its life: rising 1.4 blocks, tearing away over its second half. */
	public static double[] tatter(double phase) {
		double p = HemoEasing.clamp01(phase);
		return new double[]{0.3D + 1.1D * HemoEasing.easeOutCubic(p), HemoEasing.clamp01((p - 0.35D) / 0.65D)};
	}

	// ── Cruor Javelins (R.11) ────────────────────────────────────────

	/** The cruor climbs the staff from the heel over 0 to CLIMB, is drawn into the cage by DRAW_END. */
	public static final int CLIMB = 4, DRAW_END = 8;
	/** The shafts freeze together at the cage from FORM_START to FORM_END, one after another. */
	public static final int FORM_START = 8, FORM_END = 22;
	/** Loosed this many ticks apart from the commit; each flies at most FLIGHT ticks. */
	public static final int LOOSE_GAP = 4, FLIGHT = 12;
	/** A lodged shaft stands LODGE ticks in its rime collar, then shatters over SHATTER. */
	public static final int LODGE = 40, SHATTER = 10;
	public static final double SHAFT_SPEED = 1.5D, SHAFT_LENGTH = 1.4D;

	/** The ribbon of cruor up the staff: 0 at the heel to 1 at the cage. */
	public static float climb(float moveTime) {
		return HemoEasing.smoothstep(HemoEasing.clamp01(moveTime / CLIMB));
	}

	/**
	 * How far shaft {@code index} of {@code count} has frozen together at the cage, 0 to 1: they form one after another
	 * across FORM_START to FORM_END, each on {@link CruorJavelinRules#formation}'s curve stretched over its share.
	 */
	public static float formed(float moveTime, int index, int count) {
		int n = Math.max(1, count);
		float share = (FORM_END - FORM_START) / (float) n;
		float start = FORM_START + share * index * 0.6F;
		float span = Math.max(CruorJavelinRules.FORM_END, share * 1.4F);
		return (float) CruorJavelinRules.formation((moveTime - start) / span * CruorJavelinRules.FORM_END);
	}

	/** The tick after the commit shaft {@code index} is loosed (its Union part's delay when one is given). */
	public static int loosedAt(int index) {
		return index * LOOSE_GAP;
	}

	/** How far a loosed shaft has flown {@code since} ticks after its loose, over {@code length} blocks; -1 before it. */
	public static double flown(float since, double length) {
		if (since < 0.0F) return -1.0D;
		return Math.min(length, SHAFT_SPEED * since);
	}

	/** The tick after its loose a shaft of reach {@code length} lands. */
	public static float lands(double length) {
		return (float) Math.min(FLIGHT, length / SHAFT_SPEED);
	}

	/** A lodged shaft's shatter, 0 whole to 1 gone, {@code since} ticks after it landed; on rime it shatters at once. */
	public static float shatter(float since, boolean onRime) {
		float start = onRime ? 0.0F : LODGE;
		return HemoEasing.smoothstep(HemoEasing.clamp01((since - start) / SHATTER));
	}

	// ── Arrested Pulse (R.5, R.11) ───────────────────────────────────

	/** Beats after the mark, and the tell that shows each one first. */
	public static final int[] BEATS = {30, 60, 90};
	public static final int BEAT_TELL = 12, MARK_LIFE = 96, BURST = 14, PASS = 10;

	/** Which beat's tell or strike is next {@code sinceMark} ticks after the mark, or -1 after the last. */
	public static int beat(float sinceMark) {
		for (int i = 0; i < BEATS.length; i++) if (sinceMark < BEATS[i] + 1.0F) return i;
		return -1;
	}

	/** The beat's tell, 0 to 1 over its 12 ticks before the beat; 0 outside it. */
	public static float tell(float sinceMark) {
		int i = beat(sinceMark);
		if (i < 0) return 0.0F;
		float t = sinceMark - (BEATS[i] - BEAT_TELL);
		return t < 0.0F ? 0.0F : HemoEasing.clamp01(t / BEAT_TELL);
	}

	/** The cruor ring round the marked: it tightens with each beat, from 1.5 to 0.7 blocks. */
	public static double markRing(float sinceMark) {
		int passed = 0;
		for (int b : BEATS) if (sinceMark >= b) passed++;
		double from = 1.5D - 0.27D * passed, to = 1.5D - 0.27D * Math.min(3, passed + 1);
		int i = beat(sinceMark);
		if (i < 0) return 0.7D;
		return HemoEasing.lerp(from, to, HemoEasing.smoothstep(tell(sinceMark)) * 0.5D);
	}

	/** A burst's spines out of the body: punching to full over 2.5 ticks, holding, crumbling from 8 to BURST. */
	public static float spines(float since) {
		if (since <= 0.0F || since >= BURST) return 0.0F;
		if (since < 2.5F) return (float) HemoEasing.easeOutCubic(since / 2.5F);
		return 1.0F - HemoEasing.smoothstep(HemoEasing.clamp01((since - 8.0F) / (BURST - 8.0F)));
	}

	// ── the Shadow Step (R.11, R.12) ─────────────────────────────────

	public static final int POOL_DEPART = 2, POOL_ARRIVE = 4, SINK_START = 8, SINK_END = 24, RISE = 10, RING_DELAY = 1,
			RING_TICKS = 6;
	public static final double SINK_DEPTH = 2.8D;

	/** How far he has sunk into the dark at {@code moveTime}: slowly then faster, upright, 0 to SINK_DEPTH. */
	public static double sunk(float moveTime) {
		return SINK_DEPTH * HemoEasing.easeInCubic(HemoEasing.clamp01((moveTime - SINK_START) / (SINK_END - SINK_START)));
	}

	/** The arrival's torn column of dark, 0 to 1 rising over RISE ticks after the commit, falling back over the next 12. */
	public static float column(float age) {
		if (age < 0.0F) return 0.0F;
		if (age < RISE) return HemoEasing.smoothstep(age / RISE);
		return 1.0F - HemoEasing.smoothstep(HemoEasing.clamp01((age - RISE) / 12.0F));
	}

	/** The arrival ring's radius {@code age} ticks after the commit, -1 outside its run (0.6 to 3.5 over 6 ticks). */
	public static double arrivalRing(float age, double from, double to) {
		float t = age - RING_DELAY;
		if (t < 0.0F || t > RING_TICKS) return -1.0D;
		// linear, as the hit Ring's own radius, so the drawn wave is where it strikes
		return from + (to - from) * (t / RING_TICKS);
	}

	// ── Gloam Cuts (R.11) ────────────────────────────────────────────

	public static final int GATHER = 22;
	public static final int[] CUTS = {0, 6, 12};
	public static final float CUT_FLIGHT = 12.5F;

	/** How far a cut that left {@code since} ticks ago has flown along its lane of {@code length}, -1 before or after it. */
	public static double cutFront(float since, double length, double speed) {
		if (since < 0.0F || since > CUT_FLIGHT + 2.0F) return -1.0D;
		return Math.min(length, speed * since);
	}

	/** A cut's own strength: whole in flight, settling into its pool over the 2 ticks after it ends. */
	public static float cutFade(float since) {
		if (since < 0.0F) return 0.0F;
		if (since <= CUT_FLIGHT) return 1.0F;
		return HemoEasing.clamp01(1.0F - (since - CUT_FLIGHT) / 2.0F);
	}

	// ── the Eclipse (R.7, R.11, R.12) ────────────────────────────────

	public static final int ECLIPSE_WINDUP = 40, FINGER_FIRST = 10, FINGER_GAP = 5, FINGERS = 5;
	public static final int SHAPE_RISE = 20, REACH_TELL = 16, GRIP = 30, SHAPE_REST = 60;
	public static final int[] REACH_BEATS = {40, 90, 140};
	public static final int RELIT_BURST = 4, RELIT_TEAR = 12;

	/** How many fingers are closed over the cage at {@code moveTime}. */
	public static int fingers(float moveTime) {
		if (moveTime < FINGER_FIRST) return 0;
		return Math.min(FINGERS, 1 + (int) ((moveTime - FINGER_FIRST) / FINGER_GAP));
	}

	/** How far the light has gone out through the wind-up, 0 to 1 (follows the fingers, the last clench at 38 to 40). */
	public static float lightOut(float moveTime) {
		if (moveTime <= 0.0F) return 0.0F;
		float closed = fingers(moveTime) / (float) FINGERS;
		float clench = HemoEasing.clamp01((moveTime - 38.0F) / 2.0F);
		return HemoEasing.clamp01(closed * 0.85F + clench * 0.15F);
	}

	/** The red seam grid's strength: rising as the light goes, full in the hold. */
	public static float grid(float moveTime) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((moveTime - 16.0F) / 24.0F));
	}

	/** A shape standing up out of its figure, {@code since} ticks after it began to rise, 0 to 1. */
	public static float shapeRise(float since) {
		return HemoEasing.smoothstep(HemoEasing.clamp01(since / SHAPE_RISE));
	}

	/** An arm thrown along the floor: its reach 0 to 1 over the reach's 16-tick tell. */
	public static float reach(float since) {
		return (float) HemoEasing.easeOutCubic(HemoEasing.clamp01(since / REACH_TELL));
	}

	/** The relit burst's ring of torn dark blown back to the walls: 0 to 1 over RELIT_TEAR after the burst. */
	public static float tearBack(float since) {
		return (float) HemoEasing.easeOutCubic(HemoEasing.clamp01((since - RELIT_BURST * 0.5F) / RELIT_TEAR));
	}

	/** The cruor's own glow: 0.15 at rest, rising to 0.8 as the Eclipse puts the light out, back as it is relit. */
	public static float cruorGlow(float lightOut) {
		return HemoEasing.lerp(0.15F, 0.8F, HemoEasing.clamp01(lightOut));
	}

	// ── stains (R.8) ─────────────────────────────────────────────────

	/** A stain cue lives a little past the 20-tick cadence, so the stains never blink. */
	public static final int STAIN_LIFE = 26;

	/** A stain's drawn radius at depth 0 to 1: it spreads from 0.45 to 1.5 (the Splinter Crown's disc). */
	public static double stainRadius(float depth) {
		return 0.45D + 1.05D * HemoEasing.clamp01(depth);
	}

	// ── veins (R.11) ─────────────────────────────────────────────────

	public static final int VEIN_FLARE = 6, VEIN_RUN_END = 30, WARM_VEINS = 6;

	/** How far a vein has run toward its meeting point at {@code moveTime}: all fronts arrive together at 30. */
	public static float veinRun(float moveTime) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((moveTime - VEIN_FLARE) / (VEIN_RUN_END - VEIN_FLARE)));
	}

	/**
	 * A vein from a figure at {fx, fz} to a meeting point at {tx, tz} along the floor's block seams: straight runs with
	 * at most two right-angle turns. Returns the corner points as {x0, z0, x1, z1, ...}, snapped to block seams except
	 * the two ends.
	 */
	public static double[] veinPath(double fx, double fz, double tx, double tz, int seed) {
		double sx = Math.floor(fx) + (fx - Math.floor(fx) < 0.5D ? 0.0D : 1.0D);
		double sz = Math.floor(fz) + (fz - Math.floor(fz) < 0.5D ? 0.0D : 1.0D);
		double ex = Math.rint(tx), ez = Math.rint(tz);
		boolean xFirst = (seed & 1) == 0;
		if (Math.abs(ex - sx) < 1.0E-6D || Math.abs(ez - sz) < 1.0E-6D) return new double[]{fx, fz, sx, sz, ex, ez, tx, tz};
		if (xFirst) {
			double mx = Math.rint(HemoEasing.lerp(sx, ex, 0.5D + 0.2D * ((seed >> 1 & 3) - 1.5D) / 1.5D));
			return new double[]{fx, fz, sx, sz, mx, sz, mx, ez, ex, ez, tx, tz};
		}
		double mz = Math.rint(HemoEasing.lerp(sz, ez, 0.5D + 0.2D * ((seed >> 1 & 3) - 1.5D) / 1.5D));
		return new double[]{fx, fz, sx, sz, sx, mz, ex, mz, ex, ez, tx, tz};
	}

	/** Right-angle turns in a vein path's corner list (the snapped interior; the short end stubs are not turns). */
	public static int turns(double[] path) {
		int turns = 0;
		for (int i = 4; i + 5 < path.length; i += 2) {
			double ax = path[i] - path[i - 2], az = path[i + 1] - path[i - 1];
			double bx = path[i + 2] - path[i], bz = path[i + 3] - path[i + 1];
			if (Math.abs(ax) + Math.abs(az) < 1.0E-6D || Math.abs(bx) + Math.abs(bz) < 1.0E-6D) continue;
			if (Math.abs(ax * bx + az * bz) < 1.0E-6D) turns++;
		}
		return turns;
	}
}

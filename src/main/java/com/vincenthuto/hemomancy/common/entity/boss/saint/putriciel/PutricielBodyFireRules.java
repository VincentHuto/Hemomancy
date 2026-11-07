package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * How much the purifier burns, by region, anchor and time (design revision 2026-11-06, R1): the kindle at the Ignition
 * (smoke before flame, the climb up the staff and across the back, the locked beat, the commit's flare and its
 * settle), the steady burn following the Kindling, each move's peak, the Offering's draw-down and its acceptance
 * pulse, the hit, the Embering's die-back in reverse kindle order and the Repose gutter. Pure, with no client class, so
 * the body fire's geometry, the model's burning layer and the growth stagger all read the same numbers and JVM tests
 * cover them. In the ROT stage every value is zero, whatever the time, the Kindling or the deeds.
 * <p>
 * An anchor's {@code place} is its kindle rank within its region as a fraction: 0 catches first (the staff's grip, the
 * colony's root, the shoulder before the knuckle), 1 last.
 */
public final class PutricielBodyFireRules {
	/** Where fire stands on the body (the generator's anchor regions, contract 1 of the revision's build list). */
	public enum Region {
		STAFF(0.35D, 0.55D, 16.0F, 26.0F, 22.0F, 30.0F),
		CENSER(0.5D, 0.8D, 22.0F, 25.0F, 18.0F, 22.0F),
		BACK(0.5D, 0.9D, 20.0F, 28.0F, 27.0F, 16.0F),
		SHOULDER(0.15D, 0.35D, 18.0F, 26.0F, 17.0F, 12.0F),
		CROWN(0.2D, 0.35D, 24.0F, 29.0F, 15.0F, 10.0F),
		CHEST(0.3D, 0.5D, 16.0F, 17.0F, 15.0F, 19.0F),
		HEM(0.15D, 0.3D, 26.0F, 28.0F, 10.0F, 13.0F);

		/** A tongue's height at the steady burn, blocks, from its smallest to its largest anchor. */
		private final double low, high;
		/** The kindle runs from {@code kindleFrom} (place 0) to {@code kindleTo} (place 1) on the Ignition's clock. */
		private final float kindleFrom, kindleTo;
		/** The Embering's embers wink out from {@code outFirst} (place 0) to {@code outLast} (place 1). */
		private final float outFirst, outLast;

		Region(double low, double high, float kindleFrom, float kindleTo, float outFirst, float outLast) {
			this.low = low;
			this.high = high;
			this.kindleFrom = kindleFrom;
			this.kindleTo = kindleTo;
			this.outFirst = outFirst;
			this.outLast = outLast;
		}

		/** The tongue height at the steady burn for an anchor of {@code size} 0 (smallest) to 1 (largest). */
		public double height(double size) {
			return HemoEasing.lerp(low, high, HemoEasing.clamp01(size));
		}

		public double low() {
			return low;
		}

		public double high() {
			return high;
		}

		/** The tick on the Ignition's clock an anchor at {@code place} catches. */
		public float kindleTick(double place) {
			return HemoEasing.lerp(kindleFrom, kindleTo, (float) HemoEasing.clamp01(place));
		}

		/** The tick on the Embering's clock an anchor at {@code place} winks out. */
		public float outTick(double place) {
			return HemoEasing.lerp(outFirst, outLast, (float) HemoEasing.clamp01(place));
		}
	}

	// ── the Ignition's beats (R1.2), ticks on the Ignition move's own clock ──
	/** Thin soot seeps from the seams, the sac splits and the colony's root before anything is lit. */
	public static final float SEEP_END = 10.0F;
	/** The first embers open in the chest tears and at the grip wrap. */
	public static final float EMBERS_FROM = 10.0F;
	/** An anchor smokes this many ticks before it catches, and its flame grows over {@link #GROW}. */
	public static final float SMOKE_LEAD = 4.0F, GROW = 5.0F;
	/** A newly caught flame grows to this much of its height. */
	public static final float KINDLED = 0.6F;
	/** The locked beat: every flame drawn in to {@link #LOCKED} of itself, the cores white. */
	public static final float LOCK = 28.0F, LOCKED = 0.7F;
	/** The commit: every anchor flares to {@link #FLARE}; settled at {@link #SETTLED}, dipping to {@link #DIP} at 36. */
	public static final float COMMIT = 30.0F, FLARE = 1.45F, DIP = 0.92F, SETTLED = 42.0F;
	/** The Ignition clock's stand-in once the move has run: the steady burn. */
	public static final float STEADY = 1000.0F;
	/** The back column over the colony: its height at the steady burn, at the commit's flare and at the Pyre Heart. */
	public static final double COLUMN = 1.5D, COLUMN_FLARE = 2.2D, COLUMN_HEART = 2.6D;

	// ── the steady burn (R1.3) ──
	/** Intensity is 0.6 + 0.4 x the Kindling fraction: at 66% the body burns at 0.86, at 33% at 0.73. */
	public static final float KINDLING_FLOOR = 0.6F;
	/** The seeded flicker stays within this fraction. */
	public static final float FLICKER = 0.12F;
	/** The last of the burn-down: under Repose the fire gutters out across it, otherwise it falters to half. */
	public static final float GUTTER = 0.06F, FALTER = 0.5F;
	/** The Offering draws the fire down to the palm. */
	public static final float OFFERING_DRAW = 0.55F;
	/** A hit flares the body for this many ticks. */
	public static final float HIT_TICKS = 4.0F, HIT_PEAK = 1.2F;

	// ── the Embering (R1.6), ticks on the Embering's stage clock ──
	/** The tongues gutter to nothing by here; the embers are out by {@link #EMBERS_OUT}; the smoke by {@link #SMOKE_OUT}. */
	public static final float GUTTERED = 10.0F, EMBERS_OUT = 30.0F, SMOKE_OUT = 45.0F;
	/** The ending's fire (section 11): the climb from the core starts here and the body is out by {@link #ENDING_OUT}. */
	public static final float ENDING_FROM = PutricielEndingRules.BURN_START, ENDING_OUT = PutricielEndingRules.SOOT_END;

	// ── motion (R1.4) ──
	/** A tongue's tip follows up + a quarter of the normal - 1.6 x the velocity, clamped this far from up. */
	public static final double NORMAL_LEAN = 0.25D, DRAG = 1.6D, MAX_LEAN_DEGREES = 70.0D;
	/** The tip's spring: frequency (radians a tick) and damping ratio. */
	public static final float FOLLOW_FREQUENCY = 0.35F, FOLLOW_DAMPING = 0.5F;
	/** A tongue grows up to 80% longer with speed, full at 0.35 blocks a tick; above 0.25 it sheds tear-offs. */
	public static final double STRETCH = 0.8D, STRETCH_FULL = 0.35D, TEAR_OFF_SPEED = 0.25D;
	/** An arm moving faster than this flares its smoulder to {@link #ARM_FLARE} blocks. */
	public static final double ARM_FAST = 0.2D, ARM_FLARE = 0.6D;

	// ── the growth stagger and regrowth (R1.8) ──
	/** At the Ignition each growth part blackens over this many ticks, one tick a kindle step after tick 10. */
	public static final float CHAR_RAMP = 6.0F;
	/** The latest kindle step the stagger reaches, so every part is black by the Ignition's tick 30. */
	public static final int MAX_STEP = 14;
	/** In the Embering each part regrows over this many ticks, three ticks a step from the root outward. */
	public static final float REGROW_STEP = 3.0F, REGROW_SPAN = 12.0F;
	public static final int MAX_REGROW_STEP = 16;
	/** The outermost back shelves and the back mushroom are eaten from 0.35 to this by the end of the burn-down. */
	public static final float EATEN = 0.2F;

	private PutricielBodyFireRules() {
	}

	/**
	 * The moment the fire is read at. {@code commit} and {@code active} are the running move's (stretched) commit tick
	 * and active span; {@code sinceAccepted} ticks since the last accepted absolution (large when none);
	 * {@code sinceHit} ticks since the body was last struck (negative when it was not); {@code endingTime} the
	 * ending's tick or negative.
	 */
	public record Moment(PutricielStage stage, float stageTime, float kindling, boolean repose, int moveId, float moveTime,
			int commit, int active, int openingId, float openingTime, float sinceAccepted, float sinceHit, float endingTime) {
		/** A quiet moment in {@code stage}: no move, no opening, no hit. */
		public static Moment of(PutricielStage stage, float stageTime, float kindling) {
			return new Moment(stage, stageTime, kindling, false, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, -1.0F, -1.0F);
		}

		/** The same moment while {@code move} runs {@code moveTime} ticks in. */
		public Moment moving(PutricielMove move, float moveTime, int commit, int active) {
			return new Moment(stage, stageTime, kindling, repose, move.id(), moveTime, commit, active, openingId, openingTime,
					sinceAccepted, sinceHit, endingTime);
		}

		public boolean ending() {
			return endingTime >= 0.0F;
		}

		boolean is(PutricielMove move) {
			return moveId == move.id() && moveTime >= 0.0F;
		}
	}

	/**
	 * One anchor's fire: {@code tongue} the flame's height factor (1 at the steady burn; times the region's height),
	 * {@code bed} the glow of the coals it rises from (0 to 1), {@code smoke} how much soot lifts off it (0 to 1),
	 * {@code heat} how hot its core burns (1 white, 0 guttering to soot).
	 */
	public record Flame(float tongue, float bed, float smoke, float heat) {
		public static final Flame NONE = new Flame(0.0F, 0.0F, 0.0F, 0.0F);

		public boolean visible() {
			return tongue > 0.005F || bed > 0.005F || smoke > 0.005F;
		}
	}

	// ── the gates (R1.9) ─────────────────────────────────────────────

	/** The body fire draws in IGNITING and BURNING, in the Embering under its tick 45, and in the ending's fire beats. */
	public static boolean drawsBody(Moment m) {
		if (m.ending()) return m.endingTime() >= ENDING_FROM && m.endingTime() < ENDING_OUT;
		return switch (m.stage()) {
			case ROT -> false;
			case IGNITING, BURNING -> true;
			case EMBERING -> m.stageTime() < SMOKE_OUT;
		};
	}

	/** The burning layer on the model draws as the body fire does, but in the Embering only under its tick 30. */
	public static boolean drawsLayer(Moment m) {
		if (m.ending()) return m.endingTime() >= ENDING_FROM && m.endingTime() < ENDING_OUT;
		return switch (m.stage()) {
			case ROT -> false;
			case IGNITING, BURNING -> true;
			case EMBERING -> m.stageTime() < EMBERS_OUT;
		};
	}

	/**
	 * The Ignition's clock: the Ignition move's own time while it runs, negative while the interval waits for it, and
	 * {@link #STEADY} once it has run.
	 */
	public static float ignitionTick(Moment m) {
		if (m.is(PutricielMove.IGNITION)) return m.moveTime();
		return m.stage() == PutricielStage.IGNITING ? -1.0F : STEADY;
	}

	// ── one anchor ───────────────────────────────────────────────────

	/** Anchor at {@code place} in {@code region}; {@code palmArm} marks the offering arm's own shoulder anchors. */
	public static Flame anchor(Region region, double place, boolean palmArm, Moment m) {
		if (!drawsBody(m)) return Flame.NONE;
		if (m.ending()) return ending(region, place, m.endingTime());
		if (m.stage() == PutricielStage.EMBERING) return embering(region, place, m);
		float t = ignitionTick(m);
		float k = region.kindleTick(place);
		float base = kindle(t, k);
		float seep = t >= 0.0F && t < STEADY ? seep(region, place, t, k) : 0.0F;
		float smoke = t >= STEADY ? 0.0F : HemoEasing.ramp(t, k - SMOKE_LEAD, k);
		if (t >= STEADY) smoke = region == Region.SHOULDER ? 0.9F : region == Region.BACK ? 0.6F : 0.35F;
		smoke = Math.max(smoke, seep);
		float heat = heat(t, k);
		float scale = burn(m) * peak(region, place, palmArm, m);
		float tongue = base * scale;
		float bed = t >= k ? HemoEasing.clamp01(0.5F + 0.5F * Math.min(1.0F, tongue)) : embers(region, place, t);
		return new Flame(tongue, bed * Math.min(1.0F, 0.4F + scale), HemoEasing.clamp01(smoke), heat);
	}

	/** A region as one value for the burning layer's alpha: its middle anchor's fire, never over 1. */
	public static float region(Region region, Moment m) {
		if (!drawsLayer(m)) return 0.0F;
		Flame flame = anchor(region, 0.5D, false, m);
		return HemoEasing.clamp01(Math.max(Math.min(1.0F, flame.tongue()), flame.bed() * 0.6F));
	}

	/** The back column's height over the colony, blocks (0 when it is out). */
	public static double column(Moment m) {
		if (!drawsBody(m)) return 0.0D;
		if (m.ending()) return COLUMN * PutricielEndingRules.ember(m.endingTime());
		if (m.stage() == PutricielStage.EMBERING) return COLUMN * (1.0F - HemoEasing.easeInCubic(HemoEasing.clamp01(m.stageTime() / GUTTERED)));
		float t = ignitionTick(m);
		float k = Region.BACK.kindleTick(0.0D);
		if (t < k) return 0.0D;
		if (t < COMMIT) return COLUMN * kindle(t, k);
		if (t < STEADY) {
			// the commit's leap to 2.2, settling to the steady column with the body
			float leap = (float) (COLUMN_FLARE / COLUMN);
			return COLUMN * HemoEasing.catmullRom(t, COMMIT, leap, COMMIT + 3.0F, 1.2F, 36.0F, DIP, SETTLED, 1.0F);
		}
		double column = COLUMN * burn(m);
		if (m.is(PutricielMove.PYRE_HEART)) {
			float peak = heartPeak(m.moveTime(), m.commit());
			// the Pyre Heart's column leaps to 2.6 to join the core's
			double heart = HemoEasing.catmullRom(m.moveTime(), 0.0F, (float) COLUMN, m.commit() - 1.0F, (float) (COLUMN * 0.6D),
					m.commit() + 1.0F, (float) COLUMN_HEART, m.commit() + 8.0F, (float) (COLUMN * 1.25D), m.commit() + 16.0F, (float) COLUMN);
			return Math.max(column * Math.min(1.0F, peak), heart * burn(m));
		}
		return column * peak(Region.BACK, 0.5D, false, m);
	}

	/** The core's whiteness on the body at this moment (the locked beat and the flare run white). */
	public static float heat(Moment m) {
		if (!drawsBody(m)) return 0.0F;
		if (m.ending()) return PutricielEndingRules.ember(m.endingTime());
		if (m.stage() == PutricielStage.EMBERING) return 1.0F - HemoEasing.ramp(m.stageTime(), 0.0F, GUTTERED);
		return heat(ignitionTick(m), 0.0F);
	}

	// ── the pieces ───────────────────────────────────────────────────

	/**
	 * The kindle on the Ignition's clock {@code t} for an anchor catching at {@code k}: nothing before it, growing to
	 * 0.6 over five ticks, drawn in to 0.7 of itself at the locked beat, flaring to 1.45 at the commit and settling to 1
	 * by 42 with a breath at 36; 1 once the Ignition has run.
	 */
	public static float kindle(float t, float k) {
		if (t >= STEADY) return 1.0F;
		if (t >= COMMIT) return HemoEasing.catmullRom(t, COMMIT, FLARE, COMMIT + 3.0F, 1.2F, 36.0F, DIP, SETTLED, 1.0F);
		if (t < k) return 0.0F;
		float grown = KINDLED * HemoEasing.easeOutCubic(HemoEasing.clamp01((t - k) / GROW));
		return grown * HemoEasing.lerp(1.0F, LOCKED, HemoEasing.ramp(t, LOCK, LOCK + 1.0F));
	}

	/** The heat of an anchor's core: warming as it catches, white at the locked beat and the flare, then the steady 0.7. */
	public static float heat(float t, float k) {
		if (t >= STEADY) return 0.7F;
		if (t < 0.0F) return 0.0F;
		if (t >= LOCK) return HemoEasing.lerp(1.0F, 0.7F, HemoEasing.ramp(t, COMMIT + 2.0F, SETTLED));
		return t < k ? 0.0F : 0.45F + 0.25F * HemoEasing.ramp(t, k, k + GROW);
	}

	/** Heat without flame: thin soot from the seams, the sacs and the colony's root through the first ten ticks. */
	private static float seep(Region region, double place, float t, float k) {
		boolean seam = region == Region.CHEST || region == Region.BACK && place < 0.2D;
		if (!seam || t >= k) return 0.0F;
		return HemoEasing.lerp(0.15F, 0.35F, HemoEasing.ramp(t, 0.0F, SEEP_END));
	}

	/** The first embers: a glow bed in the chest tears and at the grip wrap from tick 10, before anything catches. */
	private static float embers(Region region, double place, float t) {
		boolean first = region == Region.CHEST || region == Region.STAFF && place < 0.1D;
		return first ? HemoEasing.ramp(t, EMBERS_FROM, Region.CHEST.kindleTick(0.0D)) * 0.6F : 0.0F;
	}

	/**
	 * The steady burn's scale: 0.6 + 0.4 x the Kindling (it burns down with the meter), the last six percent
	 * guttering out under Repose and faltering to half without it.
	 */
	public static float burn(Moment m) {
		float k = HemoEasing.clamp01(m.kindling());
		if (m.stage() == PutricielStage.IGNITING) k = 1.0F;
		float scale = KINDLING_FLOOR + (1.0F - KINDLING_FLOOR) * k;
		if (m.stage() == PutricielStage.BURNING && k < GUTTER) {
			float f = k / GUTTER;
			scale *= m.repose() ? f : HemoEasing.lerp(FALTER, 1.0F, f);
		}
		return scale;
	}

	/** The running move's peak on a region (R1.3), with the Offering's draw-down, its acceptance pulse and a hit. */
	public static float peak(Region region, double place, boolean palmArm, Moment m) {
		float peak = 1.0F;
		float t = m.moveTime(), c = m.commit(), a = m.active();
		if (m.is(PutricielMove.PURGING_SWEEP)) {
			float w = HemoEasing.window(t, c - 5.0F, c - 3.0F, c + 8.0F, c + 10.0F);
			peak *= 1.0F + (region == Region.STAFF || region == Region.CENSER ? 0.4F : 0.1F) * w;
		} else if (m.is(PutricielMove.CENSER_CAST)) {
			float w = HemoEasing.window(t, 1.0F, 4.0F, c + a + 2.0F, c + a + 8.0F);
			if (region == Region.CENSER && place < 0.5D) peak *= 1.0F + 0.5F * w;
			if (region == Region.STAFF) peak *= 1.0F + 0.2F * w;
		} else if (m.is(PutricielMove.CAUTERISING_LANE)) {
			float w = HemoEasing.window(t, 0.0F, 6.0F, c + a, c + a + 10.0F);
			if (region == Region.SHOULDER && palmArm) peak *= 1.0F + 0.5F * w;
		} else if (m.is(PutricielMove.PYRE_HEART)) {
			peak *= heartPeak(t, m.commit());
		} else if (m.is(PutricielMove.PYRE_LINES)) {
			float w = HemoEasing.window(t, 0.0F, 12.0F, c - 2.0F, c + 4.0F);
			if (region == Region.CROWN || region == Region.SHOULDER && palmArm) peak *= 1.0F + 0.3F * w;
		} else if (m.is(PutricielMove.FULL_PURGE)) {
			float ring = 0.0F;
			for (int k = 0; k < 3; k++) ring = Math.max(ring, HemoEasing.pulse(t - (c + k * 16.0F), 1.0F, 6.0F));
			peak *= 1.0F + 0.35F * ring;
		}
		peak *= offering(region, palmArm, m);
		if (m.sinceHit() >= 0.0F && m.sinceHit() < HIT_TICKS + 1.0F) {
			peak *= HemoEasing.lerp(HIT_PEAK, 1.0F, HemoEasing.ramp(m.sinceHit(), HIT_TICKS, HIT_TICKS + 1.0F));
		}
		return peak;
	}

	/** The Pyre Heart: drawn in through the wind-up, sucked into the core, then 1.6 at the commit and back by 44. */
	private static float heartPeak(float t, float commit) {
		return HemoEasing.catmullRom(t, 0.0F, 1.0F, 6.0F, 0.8F, commit - 8.0F, 0.8F, commit - 4.0F, 0.6F, commit - 0.5F, 0.6F,
				commit + 1.0F, 1.6F, commit + 8.0F, 1.25F, commit + 16.0F, 1.0F);
	}

	/**
	 * The Offering: every region drawn down to 0.55 while the palm is out, the palm's own arm at full; an accepted one
	 * runs a 1.5 pulse from the palm up the arm and across the back, holds through the recoil and returns over 8
	 * ticks.
	 */
	public static float offering(Region region, boolean palmArm, Moment m) {
		boolean palm = region == Region.SHOULDER && palmArm;
		float draw = 1.0F;
		if (m.is(PutricielMove.OFFERING)) draw = HemoEasing.lerp(1.0F, OFFERING_DRAW, HemoEasing.ramp(m.moveTime(), 0.0F, 8.0F));
		else if (m.openingId() == PutricielOpenings.OFFERING_ID) draw = OFFERING_DRAW;
		float since = m.sinceAccepted();
		float recoil = PutricielAbsolutionRules.RECOIL_TICKS;
		if (since >= 0.0F && since < recoil + 8.0F) {
			draw = Math.min(draw, HemoEasing.lerp(OFFERING_DRAW, 1.0F, HemoEasing.ramp(since, recoil, recoil + 8.0F)));
			float delay = palm ? 0.0F : region == Region.SHOULDER || region == Region.CHEST ? 2.0F : region == Region.BACK ? 3.5F : 4.5F;
			float pulse = HemoEasing.pulse(since - delay, 1.5F, 6.0F);
			return (palm ? 1.0F : draw) * (1.0F + 0.5F * pulse);
		}
		return palm ? 1.0F : draw;
	}

	/** The Embering's die-back: the tongues gutter by 10, the embers wink out in reverse order, smoke to 45. */
	private static Flame embering(Region region, double place, Moment m) {
		float e = m.stageTime();
		float gutter = 1.0F - HemoEasing.easeInCubic(HemoEasing.clamp01(e / GUTTERED));
		float out = region.outTick(place);
		float bed = 1.0F - HemoEasing.ramp(e, out - 1.0F, out + 1.0F);
		float puff = HemoEasing.window(e, out - 1.0F, out, out + 2.0F, out + 9.0F);
		float smoke = Math.max(puff, 0.55F * (1.0F - HemoEasing.ramp(e, EMBERS_OUT, SMOKE_OUT)) * HemoEasing.ramp(e, 0.0F, 4.0F));
		return new Flame(burnFloor(m) * gutter, bed, HemoEasing.clamp01(smoke), 1.0F - HemoEasing.ramp(e, 0.0F, GUTTERED));
	}

	/** The scale the fire held as it died: the faltered burn-down's last level. */
	private static float burnFloor(Moment m) {
		return KINDLING_FLOOR * FALTER;
	}

	/** The ending's last cleansing: the fire climbs the body from the core over 20 to 32 and dies 50 to 70. */
	private static Flame ending(Region region, double place, float t) {
		float k = ENDING_FROM + (region.kindleTick(place) - Region.CHEST.kindleTick(0.0D)) * (12.0F / 13.0F);
		float ember = PutricielEndingRules.ember(t);
		float grown = HemoEasing.easeOutCubic(HemoEasing.clamp01((t - k) / GROW));
		float smoke = HemoEasing.window(t, k - SMOKE_LEAD, k, PutricielEndingRules.BURN_END, ENDING_OUT);
		return new Flame(grown * ember, grown * ember, smoke, ember);
	}

	// ── motion ───────────────────────────────────────────────────────

	/**
	 * Where a tongue's tip wants to point, before the spring: up, plus a quarter of the anchor's outward normal, minus
	 * 1.6 x its velocity (blocks a tick), turned back to within 70 degrees of up. A unit vector.
	 */
	public static Vec3 tip(Vec3 normal, Vec3 velocity) {
		Vec3 want = new Vec3(0.0D, 1.0D, 0.0D).add(normal.scale(NORMAL_LEAN)).subtract(velocity.scale(DRAG));
		if (want.lengthSqr() < 1.0E-8D) return new Vec3(0.0D, 1.0D, 0.0D);
		return clampLean(want.normalize());
	}

	/** A direction turned back to within {@link #MAX_LEAN_DEGREES} of up. */
	public static Vec3 clampLean(Vec3 unit) {
		double max = Math.toRadians(MAX_LEAN_DEGREES);
		double angle = Math.acos(Math.max(-1.0D, Math.min(1.0D, unit.y)));
		if (angle <= max) return unit;
		double flat = Math.hypot(unit.x, unit.z);
		if (flat < 1.0E-6D) return new Vec3(Math.sin(max), Math.cos(max), 0.0D);
		return new Vec3(unit.x / flat * Math.sin(max), Math.cos(max), unit.z / flat * Math.sin(max));
	}

	/** How much longer a tongue burns at {@code speed} blocks a tick: 1 at rest, 1.8 from 0.35 up. */
	public static double stretch(double speed) {
		return 1.0D + STRETCH * HemoEasing.clamp01(speed / STRETCH_FULL);
	}

	/** Whether an anchor this fast sheds tear-offs. */
	public static boolean sheds(double speed) {
		return speed > TEAR_OFF_SPEED;
	}

	/** A shoulder anchor's height factor on a fast arm: its smoulder licks up toward 0.6 blocks. */
	public static double armFlare(double speed, double height) {
		if (speed <= ARM_FAST) return height;
		return HemoEasing.lerp(height, Math.max(height, ARM_FLARE), HemoEasing.clamp01((speed - ARM_FAST) / ARM_FAST));
	}

	/** The seeded flicker on an anchor: two slow sines and a hash, within 12%. */
	public static float flicker(double time, int seed) {
		double hash = ((seed * 0x9E3779B9L) >>> 40 & 0xFFFF) / 65535.0D;
		double wave = 0.6D * Math.sin(time * 0.21D + hash * 6.283D) + 0.3D * Math.sin(time * 0.53D + seed * 1.7D)
				+ 0.1D * (hash * 2.0D - 1.0D);
		return (float) (1.0D + FLICKER * Math.max(-1.0D, Math.min(1.0D, wave)));
	}

	// ── the growths (R1.8): what the model's per-part growth scale and char read ──

	/**
	 * A growth part's scale: {@code order} its kindle order, {@code regrowRank} its rank in the Embering's regrowth
	 * (from the colony's root outward; from the tips inward on the arms and crown), {@code eaten} for the outermost
	 * back shelves and the back mushroom the burn eats further.
	 */
	public static float growth(int order, int regrowRank, boolean eaten, Moment m) {
		if (m.ending()) return PutricielEndingRules.growth(m.endingTime());
		float burnt = PutricielVisualRules.GROWTH_BURNT;
		return switch (m.stage()) {
			case ROT -> 1.0F;
			case IGNITING -> HemoEasing.lerp(1.0F, burnt, blacken(order, ignitionTick(m)));
			case BURNING -> eaten ? HemoEasing.lerp(burnt, EATEN, 1.0F - HemoEasing.clamp01(m.kindling())) : burnt;
			case EMBERING -> {
				float r = regrowth(regrowRank, m.stageTime());
				float grown = PutricielVisualRules.regrow(r);
				yield eaten ? grown - (burnt - EATEN) * (1.0F - HemoEasing.smoothstep(r)) : grown;
			}
		};
	}

	/** A growth part's char: blackening in kindle order at the Ignition and clearing as it regrows. */
	public static float charAt(int order, int regrowRank, Moment m) {
		if (m.ending()) return PutricielEndingRules.charAlpha(m.endingTime());
		return switch (m.stage()) {
			case ROT -> 0.0F;
			case IGNITING -> PutricielVisualRules.CHAR_FULL * blacken(order, ignitionTick(m));
			case BURNING -> PutricielVisualRules.CHAR_FULL;
			case EMBERING -> PutricielVisualRules.CHAR_FULL * (1.0F - HemoEasing.smoothstep(regrowth(regrowRank, m.stageTime())));
		};
	}

	/** How far a part of kindle order {@code order} has blackened at the Ignition's tick {@code t}. */
	public static float blacken(int order, float t) {
		if (t >= STEADY) return 1.0F;
		float from = PutricielVisualRules.BLACKEN_START + Math.min(Math.max(0, order), MAX_STEP);
		return HemoEasing.ramp(t, from, from + CHAR_RAMP);
	}

	/** How far a part of regrowth rank {@code rank} has regrown {@code e} ticks into the Embering, 0 to 1. */
	public static float regrowth(int rank, float e) {
		float from = REGROW_STEP * Math.min(Math.max(0, rank), MAX_REGROW_STEP);
		return HemoEasing.clamp01((e - from) / REGROW_SPAN);
	}
}

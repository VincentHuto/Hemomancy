package com.vincenthuto.hemomancy.common.rite.sigil;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Stage values for a sigil's gouged links, worked out from client-stamped ages in ticks.
 * A link cuts in when it is first seen; when the pattern closes the cuts fill, a bright
 * front runs the pattern in node order, the ground veins creep from the lips and, once
 * the front has passed, the node tendrils rise. Every number is a starting value.
 */
public final class IchorianSigilGougeStages {
	public static final float CUT_IN_TICKS = 20.0F;
	/** The fill and the first glow run. */
	public static final float COMPLETION_TICKS = 40.0F;
	/** Starts with completion. */
	public static final float VEIN_CREEP_TICKS = 60.0F;
	/** The dimmer front that re-runs the pattern while the sigil is awake. */
	public static final float RERUN_PERIOD_TICKS = 160.0F;
	/** How long the node tendrils take to rise once the first front has passed. */
	public static final float TENDRIL_RISE_TICKS = 30.0F;
	/** An age meaning "long done"; {@link #settledAge} keeps it moving through the re-run period. */
	public static final float SETTLED = 1.0e6F;
	/** A calm ending (completion, cancellation) drains the cuts, veins and tendrils over this. */
	public static final float DRAIN_TICKS = 24.0F;
	/** A violent ending drains them faster... */
	public static final float VIOLENT_DRAIN_TICKS = 14.0F;
	/** ...then closes the emptied cuts over this. */
	public static final float VIOLENT_CLOSE_TICKS = 16.0F;

	private static final float FRONT_HALF_WIDTH = 0.12F;
	private static final float SETTLED_BASE = 0.35F;
	private static final float BEAT_GAIN = 0.25F;
	private static final float RERUN_GAIN = 0.4F;

	private IchorianSigilGougeStages() {
	}

	/**
	 * The age of a stage that was done before this client looked. It is at least {@link #SETTLED} and still advances
	 * with the game time through one {@link #RERUN_PERIOD_TICKS}, so a sigil first seen awake keeps re-running its front.
	 */
	public static float settledAge(float gameTime) {
		float phase = gameTime % RERUN_PERIOD_TICKS;
		return SETTLED + (phase < 0.0F ? phase + RERUN_PERIOD_TICKS : phase);
	}

	/**
	 * 1..0: how much of the blood, the ground veins and the node tendrils remain {@code endingAgeTicks} into the rite's
	 * ending. A violent ending drains faster.
	 */
	public static float drain(float endingAgeTicks, boolean violent) {
		return 1.0F - HemoEasing.smoothstep(endingAgeTicks / (violent ? VIOLENT_DRAIN_TICKS : DRAIN_TICKS));
	}

	/** 1..0: how open the cuts stay through the ending. Only a violent ending closes them, once they have drained. */
	public static float close(float endingAgeTicks, boolean violent) {
		if (!violent) return 1.0F;
		return 1.0F - HemoEasing.smoothstep((endingAgeTicks - VIOLENT_DRAIN_TICKS) / VIOLENT_CLOSE_TICKS);
	}

	/** 0..1: how far along its length the link has opened. */
	public static float cutIn(float linkAgeTicks) {
		return HemoEasing.easeOutCubic(HemoEasing.clamp01(linkAgeTicks / CUT_IN_TICKS));
	}

	/** 0..1: blood level in the cut; 0 until the sigil is awake. */
	public static float fill(float awakeAgeTicks) {
		if (awakeAgeTicks < 0.0F) return 0.0F;
		return HemoEasing.smoothstep(awakeAgeTicks / COMPLETION_TICKS);
	}

	/**
	 * 0..1 glow at a point of the pattern. {@code patternAlong} is
	 * {@code (linkIndex + along) / linkCount}. The first run is a bright front crossing the
	 * pattern over {@link #COMPLETION_TICKS}, leaving the settled glow behind it; after it the
	 * glow is {@code 0.35 + 0.25 * beat}, plus a 0.4 front re-running every
	 * {@link #RERUN_PERIOD_TICKS}.
	 */
	public static float glow(float awakeAgeTicks, float patternAlong, float beat) {
		if (awakeAgeTicks < 0.0F) return 0.0F;
		float settled = SETTLED_BASE + BEAT_GAIN * HemoEasing.clamp01(beat);
		if (awakeAgeTicks < COMPLETION_TICKS) {
			float front = awakeAgeTicks / COMPLETION_TICKS;
			float trail = patternAlong <= front ? settled : 0.0F;
			return Math.max(trail, front(patternAlong, front));
		}
		float sinceRun = (awakeAgeTicks - COMPLETION_TICKS) % RERUN_PERIOD_TICKS;
		float rerun = front(patternAlong, sinceRun / COMPLETION_TICKS);
		return HemoEasing.clamp01(settled + RERUN_GAIN * rerun);
	}

	/** 0..1: how far the ground veins have crept from the lip. */
	public static float veinReach(float awakeAgeTicks) {
		if (awakeAgeTicks < 0.0F) return 0.0F;
		return HemoEasing.easeOutCubic(HemoEasing.clamp01(awakeAgeTicks / VEIN_CREEP_TICKS));
	}

	/** 0..1: how far the node tendrils have risen; starts when the glow front has passed. */
	public static float tendrilRise(float awakeAgeTicks) {
		if (awakeAgeTicks <= COMPLETION_TICKS) return 0.0F;
		return HemoEasing.easeOutCubic(HemoEasing.clamp01((awakeAgeTicks - COMPLETION_TICKS) / TENDRIL_RISE_TICKS));
	}

	/** A soft peak of 1 at the front's position, gone {@link #FRONT_HALF_WIDTH} either side. */
	private static float front(float patternAlong, float frontAlong) {
		float distance = Math.abs(patternAlong - frontAlong);
		return HemoEasing.smoothstep(1.0F - distance / FRONT_HALF_WIDTH);
	}
}

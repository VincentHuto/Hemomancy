package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Her containment at full integrity (design section 11), 140 ticks: the anchors flare one after another and braid to
 * her irons, the chains reel her back over the tomb as her wings and hands are drawn inward, her light retreats to her
 * heart, and she sinks. {@code endTrial} runs once at tick 100; the radiant stone goes dark from the walls inward over
 * the next 20 ticks; she is discarded at 140. A reload resumes the ending (resolution B-10). Pure, so it is JVM-tested.
 */
public final class SeraphaeEndingRules {
	public static final int FLARE_END = 20;
	public static final int FLARE_GAP = 5;
	public static final int GROWTH_CLEARED = 20;
	public static final int REEL_END = 60;
	public static final int FIRST_LINE = 0;
	public static final int SECOND_LINE = 80;
	public static final int LAST_FLASH = 88;
	public static final int SINK_START = 90;
	public static final int END_TRIAL = 100;
	public static final int RESTORE_TICKS = 20;
	public static final int LENGTH = 140;

	public static final SaintEnding.Spec SPEC = new SaintEnding.Spec(END_TRIAL, LENGTH, false, true,
			SaintArena.RestoreOrder.OUTERMOST_FIRST, RESTORE_TICKS);

	public enum Beat { FLARE, REEL, STILL, SINK, RESTORE, FADE }

	private SeraphaeEndingRules() {
	}

	public static Beat beat(float tick) {
		if (tick < FLARE_END) return Beat.FLARE;
		if (tick < REEL_END) return Beat.REEL;
		if (tick < SINK_START) return Beat.STILL;
		if (tick < END_TRIAL) return Beat.SINK;
		if (tick < END_TRIAL + RESTORE_TICKS) return Beat.RESTORE;
		return Beat.FADE;
	}

	/** Whether anchor {@code index} (binding anchors first, then reserves) flares on this tick. */
	public static boolean flaresAt(int tick, int index) {
		return tick == index * FLARE_GAP && tick < FLARE_END;
	}

	/** How lit anchor {@code index}'s braid is at {@code tick}: from its flare to full over 6 ticks. */
	public static float braid(float tick, int index) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((tick - index * FLARE_GAP) / 6.0F));
	}

	/** How far she has been reeled from where she stood to over the tomb, 0 to 1. */
	public static float reel(float tick) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((tick - FLARE_END) / (REEL_END - FLARE_END)));
	}

	/** Her inward draw: wings curled, hands crossed at the wrists, head bowed; 0 to 1 across the reel. */
	public static float drawIn(float tick) {
		return reel(tick);
	}

	/** Her white vessel light, retreating from the tips toward her heart from tick 60 to 90; 1 lit, 0 dark. */
	public static float light(float tick) {
		return 1.0F - HemoEasing.smoothstep(HemoEasing.clamp01((tick - REEL_END) / (SINK_START - REEL_END)));
	}

	/** The sink into the tomb, 0 to 1 over ticks 90 to 100. */
	public static float sink(float tick) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((tick - SINK_START) / (END_TRIAL - SINK_START)));
	}

	/** The point the chains reel her to: over the tomb, a little above the dais, by the reel fraction. */
	public static double[] reelPoint(double fromX, double fromY, double fromZ, double tombX, double tombY, double tombZ,
			float tick) {
		float t = reel(tick);
		return new double[]{fromX + (tombX - fromX) * t, fromY + (tombY - fromY) * t, fromZ + (tombZ - fromZ) * t};
	}
}

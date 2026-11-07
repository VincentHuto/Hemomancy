package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;

/**
 * The ending's beats (design section 11): a hit-stop, the burst, the exsanguination into every gutter, the fold into
 * his vestments, the road home along the east channel, and {@code endTrial} once at tick 125. Either road (a full meter
 * in phase 2 or the Last Thirst, or his last health point) runs the same ending, which resumes on a reload
 * (resolution B-10). JVM-tested.
 */
public final class HemorathEndingRules {
	public static final int HIT_STOP_END = 6;
	public static final int BURST_END = 30;
	public static final int POUR_END = 80;
	public static final int FOLD_END = 110;
	public static final int HOME_END = 125;
	public static final int END_TRIAL = 125;
	public static final int LENGTH = 130;
	/** The gutters flood in fronts from him outward, this many ticks a block. */
	public static final int FLOOD_TICKS_PER_BLOCK = 1;
	/** The arena restores everything at endTrial; the client's wet trace fades over this. */
	public static final int WET_TRACE_TICKS = 100;

	public static final SaintEnding.Spec SPEC = new SaintEnding.Spec(END_TRIAL, LENGTH, false, true,
			SaintArena.RestoreOrder.IMMEDIATE, 0);

	public enum Beat { HIT_STOP, BURST, POUR, FOLD, HOME, CLOSED }

	private HemorathEndingRules() {
	}

	public static Beat beat(int tick) {
		if (tick < HIT_STOP_END) return Beat.HIT_STOP;
		if (tick < BURST_END) return Beat.BURST;
		if (tick < POUR_END) return Beat.POUR;
		if (tick < FOLD_END) return Beat.FOLD;
		if (tick < HOME_END) return Beat.HOME;
		return Beat.CLOSED;
	}

	/** True on the first tick of a beat, where its one-off effects fire. */
	public static boolean starts(int tick, Beat beat) {
		return switch (beat) {
			case HIT_STOP -> tick == 0;
			case BURST -> tick == HIT_STOP_END;
			case POUR -> tick == BURST_END;
			case FOLD -> tick == POUR_END;
			case HOME -> tick == FOLD_END;
			case CLOSED -> tick == HOME_END;
		};
	}
}

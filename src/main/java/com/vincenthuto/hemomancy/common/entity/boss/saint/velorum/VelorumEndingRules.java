package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;

/**
 * Velorum's ending (design section 11): his body fails only from a blow inside an opening. The hand lowers, the hood
 * bows, the frost falls out of the light, the meters drain, and the faithful walk back to their places and kneel; the
 * trial ends at 200 ticks, or as soon as every figure is home after the walk began. The window is saved and resumes on
 * a reload (resolution B-10). JVM-tested.
 */
public final class VelorumEndingRules {
	public static final int LENGTH = 200;
	public static final int HAND_LOWERED = 16;
	public static final int HOOD_BOWED = 40;
	public static final int FROST_FALLS = 64;
	public static final int METERS_DRAIN_FROM = 40;
	public static final int METERS_DRAIN_TO = 80;
	public static final int DEVOTEES_TURN = 64;
	public static final int WALK_HOME = 80;
	/** The rest burst and the line at the tomb; the three chimes as the frost falls. */
	public static final int[] CHIMES = {40, 48, 56};
	public static final float FAILED_GLOW = 0.15F;

	public static final SaintEnding.Spec SPEC = new SaintEnding.Spec(LENGTH, LENGTH, true, true,
			SaintArena.RestoreOrder.IMMEDIATE, 0);

	/** The beats, in order. */
	public enum Beat { HAND, HOOD, FROST, TURN, WALK, END }

	private VelorumEndingRules() {
	}

	public static Beat beat(int tick) {
		if (tick < HAND_LOWERED) return Beat.HAND;
		if (tick < HOOD_BOWED) return Beat.HOOD;
		if (tick < DEVOTEES_TURN) return Beat.FROST;
		if (tick < WALK_HOME) return Beat.TURN;
		if (tick < LENGTH) return Beat.WALK;
		return Beat.END;
	}

	/** The fraction of every meter left: whole until 40, gone by 80. */
	public static float meterLeft(int tick) {
		if (tick <= METERS_DRAIN_FROM) return 1.0F;
		if (tick >= METERS_DRAIN_TO) return 0.0F;
		return 1.0F - (tick - METERS_DRAIN_FROM) / (float) (METERS_DRAIN_TO - METERS_DRAIN_FROM);
	}

	/** The trial may end early only once the walk home has begun and every figure is home. */
	public static boolean done(int tick, boolean everyoneHome) {
		return tick >= WALK_HOME && everyoneHome;
	}
}

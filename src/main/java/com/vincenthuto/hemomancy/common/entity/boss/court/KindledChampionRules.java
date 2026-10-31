package com.vincenthuto.hemomancy.common.entity.boss.court;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;

import javax.annotation.Nullable;

/**
 * The Kindled Champion's pure rules (Brined Court Track 10, Tier 2): its regrowth, the crown it buds at each threshold,
 * its moves, the vents' heat and when it yields. The title is held in {@code CourtProgress}; the gate and the retry
 * wait are {@code CourtChampionRules}.
 */
public final class KindledChampionRules {
	public static final float MAX_HEALTH = 220.0F;
	public static final int INTRO = 80;
	/** It yields, alive, at this fraction of its health. */
	public static final float YIELD_AT = 0.1F;
	public static final int YIELD_TICKS = 60;
	public static final int TARGET_GRACE = 100;
	public static final double LEASH = 32.0D;
	/** How far round its mound it may follow the challenger. */
	public static final int ARENA_RADIUS = 12;

	/** Health it regrows each second, by crown stage: Kinbud's regrowth at a miniboss's scale. */
	private static final float[] REGROWTH = {2.0F, 3.0F, 4.5F};
	/** The crown's stages: the eight it starts with, then four more buds at each threshold. */
	public static final float[] BUD_THRESHOLDS = {0.66F, 0.33F};
	public static final int CROWN = 8;
	public static final int BUDS_PER_STAGE = 4;

	/** Neck sweep: the whole neck swung low through a wide arc in front. */
	public static final BossMoveTimeline NECK_SWEEP = new BossMoveTimeline(22, 6, 18);
	public static final double SWEEP_RADIUS = 5.0D;
	public static final double SWEEP_DEGREES = 140.0D;
	public static final float SWEEP_DAMAGE = 9.0F;
	/** Crown sting: the crown driven down onto the challenger; the Stung venom of Track 2. */
	public static final BossMoveTimeline CROWN_STING = new BossMoveTimeline(18, 4, 14);
	public static final double STING_REACH = 4.0D;
	public static final float STING_DAMAGE = 5.0F;
	public static final int STING_TICKS = 100;
	/** Crown grab: the tentacles close on the challenger and draw them in under the head. */
	public static final BossMoveTimeline CROWN_GRAB = new BossMoveTimeline(26, 30, 16);
	public static final double GRAB_REACH = 7.0D;
	public static final float GRAB_DAMAGE = 3.0F;
	public static final int GRAB_HIT_INTERVAL = 10;

	/** The chimneys vent heat through the fight: telegraphed, then a column of scalding water. */
	public static final int VENT_INTERVAL = 120;
	public static final int VENT_WARNING = 30;
	public static final int VENTS_PER_BURST = 3;
	public static final double VENT_RADIUS = 1.6D;
	public static final float VENT_DAMAGE = 6.0F;

	private KindledChampionRules() {
	}

	public enum Move {
		NONE(null), NECK_SWEEP(KindledChampionRules.NECK_SWEEP), CROWN_STING(KindledChampionRules.CROWN_STING),
		CROWN_GRAB(KindledChampionRules.CROWN_GRAB);

		@Nullable
		private final BossMoveTimeline timeline;

		Move(@Nullable BossMoveTimeline timeline) {
			this.timeline = timeline;
		}

		@Nullable
		public BossMoveTimeline timeline() {
			return timeline;
		}

		public static Move byId(int id) {
			Move[] values = values();
			return id >= 0 && id < values.length ? values[id] : NONE;
		}
	}

	/** How the challenge ended. */
	public enum Outcome {
		/** The challenger won: the old Champion yields and lives, the head crowns the victor, the title moves. */
		YIELDED,
		/** The challenger died: the wait starts and the Champion keeps the title. */
		CHALLENGER_DIED,
		/** The challenger left: nothing is recorded either way. */
		GONE
	}

	/** The crown stage for a health fraction: 0, then 1 below two thirds, then 2 below one third. */
	public static int budStage(float healthFraction) {
		int stage = 0;
		for (float threshold : BUD_THRESHOLDS) if (healthFraction < threshold) stage++;
		return stage;
	}

	/** Crown tentacles drawn at a stage. */
	public static int crownCount(int stage) {
		return CROWN + BUDS_PER_STAGE * Math.max(0, Math.min(BUD_THRESHOLDS.length, stage));
	}

	public static float regrowthPerSecond(int stage) {
		return REGROWTH[Math.max(0, Math.min(REGROWTH.length - 1, stage))];
	}

	/** Whether a hit leaving {@code health} brings it to yield. */
	public static boolean yieldsAt(float health, float maxHealth) {
		return health <= maxHealth * YIELD_AT;
	}

	/** The move it starts now, or NONE. A later crown stage reaches for the grab more often. */
	public static Move choose(double distance, int stage, long seed) {
		if (distance > GRAB_REACH) return Move.NONE;
		int roll = (int) Math.floorMod(seed, 6);
		if (distance > SWEEP_RADIUS) return roll < 2 + stage ? Move.CROWN_GRAB : Move.NONE;
		if (distance <= STING_REACH && roll < 2) return Move.CROWN_STING;
		if (roll < 2 + stage && stage > 0) return Move.CROWN_GRAB;
		return Move.NECK_SWEEP;
	}
}

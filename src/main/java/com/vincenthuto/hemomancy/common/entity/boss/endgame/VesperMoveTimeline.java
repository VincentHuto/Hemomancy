package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** One mount move as wind-up, active and recovery spans, counted in ticks from the move's start. */
public record VesperMoveTimeline(int windup, int active, int recovery) {
	public enum Stage { WINDUP, ACTIVE, RECOVERY, COMPLETE }

	public int total() {
		return windup + active + recovery;
	}

	/** The tick the move stops tracking and commits to its locked aim. */
	public int commitTick() {
		return windup;
	}

	public Stage stageAt(int tick) {
		if (tick < windup) return Stage.WINDUP;
		if (tick < windup + active) return Stage.ACTIVE;
		if (tick < total()) return Stage.RECOVERY;
		return Stage.COMPLETE;
	}
}

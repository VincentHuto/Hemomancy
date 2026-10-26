package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;

/**
 * Timelines, phase gates and cooldowns for the Spawn Mother's moves. Selection lives in {@link MycophantSelectionRules}.
 * Pure, so it is JVM-tested.
 */
public final class MycophantControllerRules {
	public enum Interrupt { QUEUE, STAGGER }

	private MycophantControllerRules() {
	}

	public static BossMoveTimeline timeline(MycophantAttack attack, int phase) {
		return timeline(attack, phase, 0);
	}

	/** The wind-up, active and recovery ticks of {@code attack}; strings key their length on the variant. */
	public static BossMoveTimeline timeline(MycophantAttack attack, int phase, int variant) {
		boolean two = phase >= 2;
		return switch (attack) {
			case RUSHING_EMBRACE -> two ? new BossMoveTimeline(18, 10, 22) : new BossMoveTimeline(24, 10, 26);
			case SIPHON_FEED -> new BossMoveTimeline(20, 60, 16);
			case PLUCK -> new BossMoveTimeline(14, 6, 16);
			case RAKE_STRING -> rakeString(two, variant);
			case BROOD_CALL -> new BossMoveTimeline(30, 10, 30);
			case NECTAR_SURGE -> new BossMoveTimeline(30, 4, 20);
			case FUSION_EMBRACE -> new BossMoveTimeline(22, 12, 26);
			case WING_GUARD -> new BossMoveTimeline(8, 60, 10);
			case WING_SWEEP -> new BossMoveTimeline(18, 6, 20);
			case PULP_BLOOM -> new BossMoveTimeline(26, 8, 24);
			case BROOD_SETTLE -> two ? new BossMoveTimeline(16, 6, 36) : new BossMoveTimeline(18, 6, 40);
			case CLAW_PIN -> two ? new BossMoveTimeline(12, 4, 14) : new BossMoveTimeline(14, 4, 16);
			case TAIL_LASH -> new BossMoveTimeline(two ? 10 : 12, variant >= 1 ? 16 : 6, 18);
			case VENT_BLOOM -> two ? new BossMoveTimeline(14, 4, 16) : new BossMoveTimeline(16, 4, 18);
			case HAND_SWEEP -> two ? new BossMoveTimeline(14, 6, 18) : new BossMoveTimeline(16, 6, 20);
			case SIPHON_SPIT -> two ? new BossMoveTimeline(16, 14, 16) : new BossMoveTimeline(18, 12, 18);
			case SPORE_MORTAR -> two ? new BossMoveTimeline(20, 10, 20) : new BossMoveTimeline(22, 10, 22);
			case NURSING -> new BossMoveTimeline(12, 80, 16);
			case HYPHAE_LANCES -> new BossMoveTimeline(18, 24, 20);
			case LATTICE_NET -> new BossMoveTimeline(16, 6, 24);
			case GAZE -> new BossMoveTimeline(24, 40, 20);
			// The tell, then the pull; its outcomes are staggers the controller forces.
			case LAST_BROOD -> new BossMoveTimeline(30, 160, 0);
			case NONE -> new BossMoveTimeline(0, 0, 0);
		};
	}

	/** Two rakes (variant 0), two and a delayed slam (1), or three and the slam (2); phase 2 winds up faster. */
	private static BossMoveTimeline rakeString(boolean two, int variant) {
		int windup = two ? 12 : 14;
		if (variant >= 2) return new BossMoveTimeline(windup, 34, 22);
		if (variant == 1) return two ? new BossMoveTimeline(windup, 28, 20) : new BossMoveTimeline(windup, 30, 22);
		return new BossMoveTimeline(windup, 16, 18);
	}

	/** Phase 2 replaces the Rushing Embrace with the Fusion Embrace and adds the wings, the pulp and the Last Brood. */
	public static boolean allowed(MycophantAttack attack, int phase) {
		return switch (attack) {
			case NONE -> false;
			case RUSHING_EMBRACE -> phase < 2;
			case FUSION_EMBRACE, WING_GUARD, WING_SWEEP, PULP_BLOOM, HYPHAE_LANCES, LATTICE_NET, GAZE, LAST_BROOD ->
					phase >= 2;
			default -> true;
		};
	}

	public static int cooldownTicks(MycophantAttack attack, int phase) {
		boolean two = phase >= 2;
		return switch (attack) {
			case RUSHING_EMBRACE -> two ? 90 : 120;
			case FUSION_EMBRACE -> 100;
			case SIPHON_FEED -> two ? 110 : 140;
			case PLUCK -> 50;
			case RAKE_STRING -> two ? 40 : 50;
			case BROOD_CALL -> two ? 320 : 400;
			case NECTAR_SURGE -> two ? 240 : 300;
			case WING_GUARD -> 200;
			case WING_SWEEP -> 60;
			case PULP_BLOOM -> 160;
			case BROOD_SETTLE -> two ? 100 : 120;
			case CLAW_PIN -> two ? 120 : 140;
			case TAIL_LASH -> two ? 50 : 60;
			case VENT_BLOOM -> two ? 170 : 200;
			case HAND_SWEEP -> two ? 70 : 80;
			case SIPHON_SPIT -> two ? 100 : 120;
			case SPORE_MORTAR -> two ? 180 : 220;
			case NURSING -> two ? 260 : 300;
			case HYPHAE_LANCES -> 140;
			case LATTICE_NET -> 260;
			case GAZE -> 320;
			// Triggered once per fight, never picked.
			case LAST_BROOD, NONE -> 0;
		};
	}

	public static float turnRateDegrees(int phase, boolean windup) {
		if (phase >= 2) return windup ? 8.0F : 4.0F;
		return windup ? 6.0F : 3.0F;
	}

	/**
	 * Moves that hold every split and stagger until they end, whatever their stage. The Last Brood spends its once-a-fight
	 * flag as it starts, so a grief in its tell must not cut it.
	 */
	public static boolean queuesInterrupts(MycophantAttack attack) {
		return attack == MycophantAttack.LAST_BROOD;
	}

	/** Events that must not cut an active move: a wind-up can still be staggered out of, anything later queues. */
	public static Interrupt interrupt(BossMoveTimeline.Stage stage) {
		return stage == BossMoveTimeline.Stage.WINDUP ? Interrupt.STAGGER : Interrupt.QUEUE;
	}

	/** She drives along the locked line only until she has someone: never while holding, never after a catch. */
	public static boolean mayLunge(boolean holding, boolean caughtThisMove) {
		return !holding && !caughtThisMove;
	}

	/** One catch per embrace: a released player is not taken straight back. */
	public static boolean mayCatch(boolean holding, boolean caughtThisMove) {
		return !holding && !caughtThisMove;
	}

	/** The aim locks once, on the commit tick, even if a hold freezes the clock there. */
	public static boolean shouldCommit(int tick, int commitTick, boolean aimLocked) {
		return tick == commitTick && !aimLocked;
	}

	public static int staggerTicks() {
		return 16;
	}

	/** She turns toward a fallen brood fungling and pauses: an opening. */
	public static int griefTicks() {
		return 30;
	}

	/** After the player breaks a hold she reels: the biggest damage window. */
	public static int breakStaggerTicks() {
		return 40;
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

/** Timelines, ranges, weights and cooldowns for the Spawn Mother's moves. Pure, so it is JVM-tested. */
public final class MycophantControllerRules {
	public enum Interrupt { QUEUE, STAGGER }

	private MycophantControllerRules() {
	}

	public static VesperMoveTimeline timeline(MycophantAttack attack, int phase) {
		boolean two = phase >= 2;
		return switch (attack) {
			case RUSHING_EMBRACE -> two ? new VesperMoveTimeline(18, 10, 22) : new VesperMoveTimeline(24, 10, 26);
			case SIPHON_FEED -> new VesperMoveTimeline(20, 60, 16);
			case PLUCK -> new VesperMoveTimeline(14, 6, 16);
			case CLAW_RAKE -> new VesperMoveTimeline(16, 6, 18);
			case BROOD_CALL -> new VesperMoveTimeline(30, 10, 30);
			case NECTAR_SURGE -> new VesperMoveTimeline(30, 4, 20);
			case FUSION_EMBRACE -> new VesperMoveTimeline(22, 12, 26);
			case WING_GUARD -> new VesperMoveTimeline(8, 60, 10);
			case WING_SWEEP -> new VesperMoveTimeline(18, 6, 20);
			case PULP_BLOOM -> new VesperMoveTimeline(26, 8, 24);
			case NONE -> new VesperMoveTimeline(0, 0, 0);
		};
	}

	/** Phase 2 replaces the Rushing Embrace with the Fusion Embrace and adds the wings and the pulp. */
	public static boolean allowed(MycophantAttack attack, int phase) {
		return switch (attack) {
			case NONE -> false;
			case RUSHING_EMBRACE -> phase < 2;
			case FUSION_EMBRACE, WING_GUARD, WING_SWEEP, PULP_BLOOM -> phase >= 2;
			default -> true;
		};
	}

	public static double minRange(MycophantAttack attack) {
		return switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> 5.0;
			case SIPHON_FEED -> 4.0;
			default -> 0.0;
		};
	}

	public static double maxRange(MycophantAttack attack) {
		return switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> 16.0;
			case SIPHON_FEED -> 10.0;
			case PLUCK -> 5.0;
			case CLAW_RAKE -> 6.0;
			case WING_SWEEP, PULP_BLOOM -> 7.0;
			case WING_GUARD -> 12.0;
			case BROOD_CALL, NECTAR_SURGE -> 30.0;
			case NONE -> 0.0;
		};
	}

	public static int weight(MycophantAttack attack, int phase, double distance) {
		return switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> 30;
			case SIPHON_FEED -> 20;
			case PLUCK -> 18;
			case CLAW_RAKE -> 22;
			case WING_SWEEP -> 18;
			case PULP_BLOOM -> 12;
			case BROOD_CALL, WING_GUARD -> 10;
			case NECTAR_SURGE -> 6;
			case NONE -> 0;
		};
	}

	public static int cooldownTicks(MycophantAttack attack, int phase) {
		boolean two = phase >= 2;
		return switch (attack) {
			case RUSHING_EMBRACE -> two ? 90 : 120;
			case FUSION_EMBRACE -> 100;
			case SIPHON_FEED -> two ? 110 : 140;
			case PLUCK -> 50;
			case CLAW_RAKE -> 40;
			case BROOD_CALL -> two ? 320 : 400;
			case NECTAR_SURGE -> two ? 240 : 300;
			case WING_GUARD -> 200;
			case WING_SWEEP -> 60;
			case PULP_BLOOM -> 160;
			case NONE -> 0;
		};
	}

	public static float turnRateDegrees(int phase, boolean windup) {
		if (phase >= 2) return windup ? 8.0F : 4.0F;
		return windup ? 6.0F : 3.0F;
	}

	/** Events that must not cut an active move: a wind-up can still be staggered out of, anything later queues. */
	public static Interrupt interrupt(VesperMoveTimeline.Stage stage) {
		return stage == VesperMoveTimeline.Stage.WINDUP ? Interrupt.STAGGER : Interrupt.QUEUE;
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

	/** Weighted pick among the moves that are allowed, in range and off cooldown; NONE when nothing qualifies. */
	public static MycophantAttack choose(int phase, double distance, ToIntFunction<MycophantAttack> cooldownLeft,
			IntUnaryOperator roll) {
		List<MycophantAttack> eligible = new ArrayList<>();
		int sum = 0;
		for (MycophantAttack attack : MycophantAttack.values()) {
			if (!allowed(attack, phase) || cooldownLeft.applyAsInt(attack) > 0) continue;
			if (distance < minRange(attack) || distance > maxRange(attack)) continue;
			eligible.add(attack);
			sum += weight(attack, phase, distance);
		}
		if (sum <= 0) return MycophantAttack.NONE;
		int pick = Math.floorMod(roll.applyAsInt(sum), sum);
		for (MycophantAttack attack : eligible) {
			pick -= weight(attack, phase, distance);
			if (pick < 0) return attack;
		}
		return eligible.get(eligible.size() - 1);
	}
}

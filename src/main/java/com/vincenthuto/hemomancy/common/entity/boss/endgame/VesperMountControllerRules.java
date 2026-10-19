package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;

/** Pure timing, interrupt and facing rules for the Crowned Refusal's mount controller. */
public final class VesperMountControllerRules {
	public static final int STAGGER_TICKS = 14;

	public enum Priority { WOUND, WING_GROWTH, TRANSITION, BURROW_SURFACE }

	public enum Interrupt { TAKE_NOW, STAGGER, QUEUE }

	private VesperMountControllerRules() {
	}

	public static VesperMoveTimeline timeline(VesperPhaseOneAttack attack) {
		return switch (attack) {
			case IDLE -> new VesperMoveTimeline(0, 0, 0);
			case ROYAL_SCUTTLE -> new VesperMoveTimeline(25, 18, 19);
			case PINCER_VICE -> new VesperMoveTimeline(24, 15, 19);
			case STINGER_SCRIPT -> new VesperMoveTimeline(16, 37, 15);
			case BROOD_TRAMPLE -> new VesperMoveTimeline(22, 17, 23);
			case PUPPET_MUSTER -> new VesperMoveTimeline(32, 6, 30);
			case CARAPACE_ANEURYSM -> new VesperMoveTimeline(24, 81, 15);
			case GRAB_IMPALEMENT -> new VesperMoveTimeline(15, 36, 19);
			case WING_GROWTH -> new VesperMoveTimeline(20, 30, 10);
			case DIVING_REND -> new VesperMoveTimeline(20, 20, 25);
			case TAIL_NEEDLE_FUSILLADE -> new VesperMoveTimeline(12, 9, 9);
			// Nominal: the Burrow runs its own variable timeline in VesperBurrowRules.
			case BURROW -> new VesperMoveTimeline(VesperBurrowRules.DIVE_TICKS, 200, VesperBurrowRules.RECOVERY_TICKS);
			case RECALL -> new VesperMoveTimeline(20, 6, 20);
		};
	}

	/** Higher-priority events never cut an active swing: they queue, stagger a wind-up, or take a recovery. */
	public static Interrupt interrupt(VesperMoveTimeline.Stage current, Priority request) {
		return switch (current) {
			case ACTIVE -> Interrupt.QUEUE;
			case WINDUP -> Interrupt.STAGGER;
			case RECOVERY, COMPLETE -> Interrupt.TAKE_NOW;
		};
	}

	public static float turnToward(float currentYaw, float desiredYaw, float maxStep) {
		float delta = Mth.wrapDegrees(desiredYaw - currentYaw);
		return currentYaw + Mth.clamp(delta, -maxStep, maxStep);
	}

	public enum Telegraph { NONE, LINE, RING, CONE, MARKERS, STRINGS }

	/** The ground tell each move shows during its wind-up. */
	public static Telegraph telegraph(VesperPhaseOneAttack attack) {
		return switch (attack) {
			case IDLE -> Telegraph.NONE;
			case ROYAL_SCUTTLE, GRAB_IMPALEMENT, DIVING_REND -> Telegraph.LINE;
			case PINCER_VICE, TAIL_NEEDLE_FUSILLADE -> Telegraph.CONE;
			case STINGER_SCRIPT -> Telegraph.MARKERS;
			case BROOD_TRAMPLE, CARAPACE_ANEURYSM, WING_GROWTH, BURROW -> Telegraph.RING;
			case PUPPET_MUSTER, RECALL -> Telegraph.STRINGS;
		};
	}

	public static boolean tracksTarget(VesperMoveTimeline.Stage stage) {
		return stage == VesperMoveTimeline.Stage.WINDUP;
	}

	public enum Next { CONTINUE, SKIP_TO_RECOVERY, FINISH, PATROL }

	/** A lost target never stalls the mount: an uncommitted move bails to recovery, a committed one plays out. */
	public static Next nextState(VesperMoveTimeline.Stage stage, boolean hasTarget) {
		if (hasTarget) return Next.CONTINUE;
		return switch (stage) {
			case WINDUP -> Next.SKIP_TO_RECOVERY;
			case ACTIVE, RECOVERY -> Next.FINISH;
			case COMPLETE -> Next.PATROL;
		};
	}

	/** A slow lissajous loop around the arena centre, well inside the safe half-extent. */
	public static double[] patrolPoint(double centerX, double centerZ, long gameTime) {
		double radius = VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT * 0.55D;
		double t = gameTime * 0.006D;
		return new double[] { centerX + Math.cos(t) * radius, centerZ + Math.sin(t * 2.0D) * radius * 0.6D };
	}
}

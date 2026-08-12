package com.vincenthuto.hemomancy.common.entity.boss.endgame;

public final class VesperMountAttackRules {
	public static final int ANEURYSM_COOLDOWN_TICKS = 240;
	public static final int BLOOD_LOSS_TICKS = 160;
	public static final int POISON_TICKS = 100;
	public static final double RELEASE_HORIZONTAL_SPEED = 2.5D;
	public static final double RELEASE_UPWARD_SPEED = 0.45D;
	private static final double GRAB_RANGE_SQR = 25.0D;
	private static final double RESTRAINT_RANGE_SQR = 64.0D;

	private VesperMountAttackRules() {
	}

	public static AneurysmStage aneurysmStage(int tick) {
		if (tick < 24) return AneurysmStage.BRACE;
		if (tick == 24) return AneurysmStage.ERUPTION;
		if (tick < 85) return AneurysmStage.EXPOSED;
		if (tick < 105) return AneurysmStage.REFORM;
		if (tick < 120) return AneurysmStage.RECOVERY;
		return AneurysmStage.COMPLETE;
	}

	public static boolean isCarapaceExposed(int tick) {
		return tick >= 25 && tick < 105;
	}

	public static float carapaceDamageMultiplier(int tick) {
		return isCarapaceExposed(tick) ? 1.5F : 1.0F;
	}

	public static int startAneurysmCooldown() {
		return ANEURYSM_COOLDOWN_TICKS;
	}

	public static int tickCooldown(int cooldown) {
		return Math.max(0, cooldown - 1);
	}

	public static boolean mayStartAneurysm(int cooldown, boolean anchorExposed, boolean transitioning,
			boolean grabActive, boolean wingGrowth, boolean flightSortie) {
		return cooldown <= 0 && !anchorExposed && !transitioning && !grabActive && !wingGrowth && !flightSortie;
	}

	public static VesperPhaseOneAttack selectAttack(int attackStep, VesperPhaseOneAttack previous,
			int aneurysmCooldown, boolean grabEligible, boolean forbidden) {
		if (forbidden) return VesperPhaseOneAttack.IDLE;
		int beat = Math.floorMod(attackStep, 7);
		if (beat == 0 && aneurysmCooldown <= 0 && previous != VesperPhaseOneAttack.CARAPACE_ANEURYSM) {
			return VesperPhaseOneAttack.CARAPACE_ANEURYSM;
		}
		if (beat == 1) return grabEligible ? VesperPhaseOneAttack.GRAB_IMPALEMENT : VesperPhaseOneAttack.PINCER_VICE;
		return VesperCombatRules.phaseOneAttack(beat == 0 ? 0 : beat - 2);
	}

	public static boolean mayGrab(double distanceSqr, boolean onGround, boolean alive,
			boolean creative, boolean spectator, boolean alliedPuppet, boolean anotherBoss) {
		return distanceSqr <= GRAB_RANGE_SQR && onGround && alive && !creative && !spectator
				&& !alliedPuppet && !anotherBoss;
	}

	public static GrabStage grabStage(int tick, boolean hasVictim) {
		if (tick < 15) return GrabStage.TELEGRAPH;
		if (tick < 21) return GrabStage.LUNGE;
		if (!hasVictim) return tick < 70 ? GrabStage.RECOVERY : GrabStage.COMPLETE;
		if (tick < 30) return GrabStage.LIFT;
		if (tick == 30) return GrabStage.BITE;
		if (tick < 42) return GrabStage.TAIL_WINDUP;
		if (tick == 42) return GrabStage.IMPALE;
		if (tick < 51) return GrabStage.RELEASE;
		if (tick < 70) return GrabStage.RECOVERY;
		return GrabStage.COMPLETE;
	}

	public static boolean shouldApply(int mask, Hit hit) {
		return (mask & hit.bit) == 0;
	}

	public static int markApplied(int mask, Hit hit) {
		return mask | hit.bit;
	}

	public static boolean shouldReleaseRestraint(boolean bossAlive, boolean victimPresent,
			boolean victimAlive, boolean sameLevel, double distanceSqr, boolean anchorExposed,
			boolean transitioning, boolean grabAttackActive) {
		return !bossAlive || !victimPresent || !victimAlive || !sameLevel || distanceSqr > RESTRAINT_RANGE_SQR
				|| anchorExposed || transitioning || !grabAttackActive;
	}

	public static double liftProgress(int tick) {
		double linear = Math.max(0.0D, Math.min(1.0D, (tick - 20.0D) / 9.0D));
		return linear * linear * (3.0D - 2.0D * linear);
	}

	public enum AneurysmStage { BRACE, ERUPTION, EXPOSED, REFORM, RECOVERY, COMPLETE }
	public enum GrabStage { TELEGRAPH, LUNGE, LIFT, BITE, TAIL_WINDUP, IMPALE, RELEASE, RECOVERY, COMPLETE }
	public enum Hit {
		BITE(1), IMPALE(2), RELEASE(4);
		private final int bit;
		Hit(int bit) { this.bit = bit; }
	}
}

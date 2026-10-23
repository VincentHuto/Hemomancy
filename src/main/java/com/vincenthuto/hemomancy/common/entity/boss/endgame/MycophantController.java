package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The one owner of the Spawn Mother's locomotion (the belly drag), facing (turn-rate limited) and move timeline.
 * Higher-priority events (the split, a grief or break stagger) never cut an active move: a wind-up staggers out, later
 * stages queue until the recovery boundary.
 */
final class MycophantController {
	private final MycophantEntity boss;
	private final int[] cooldowns = new int[MycophantAttack.values().length];
	private int decisionDelay = 30;
	private boolean pendingSplit;
	private int pendingStagger;
	@Nullable private Vec3 pendingStaggerFace;
	private int staggerLeft;
	@Nullable private Vec3 staggerFace;
	@Nullable private Vec3 lockedAim;
	private Vec3 lungeDirection = Vec3.ZERO;
	private double lungeSpeed;
	private boolean guarding;
	private boolean caughtThisMove;

	MycophantController(MycophantEntity boss) {
		this.boss = boss;
	}

	void tick() {
		for (int i = 0; i < cooldowns.length; i++) if (cooldowns[i] > 0) cooldowns[i]--;
		boss.tickStateClock();
		if (!boss.isIntroDone()) {
			tickIntro();
			return;
		}
		switch (boss.getState()) {
			case ATTACK -> tickAttack();
			case STAGGER -> tickStagger();
			case SPLIT -> tickSplit();
			case YIELD -> { }
			default -> tickRoam();
		}
	}

	// ------------------------------------------------------------------ the waking

	private void tickIntro() {
		ServerPlayer owner = boss.owner();
		if (boss.getEncounterOwner() == null) {
			boss.finishIntro();
			boss.setState(MycophantState.IDLE);
			return;
		}
		if (boss.getState() != MycophantState.INTRO) {
			boss.setState(MycophantState.INTRO);
			boss.setIntroTick(0);
		}
		boolean rematch = boss.isRematchIntro();
		int t = boss.getIntroTick() + 1;
		boss.setIntroTick(t);
		if (owner != null && MycophantIntroRules.bound(t, rematch)) {
			owner.setDeltaMovement(0.0D, Math.min(0.0D, owner.getDeltaMovement().y), 0.0D);
			owner.hurtMarked = true;
		}
		if (owner != null && t >= MycophantIntroRules.tickOf(MycophantIntroRules.Beat.TURN, rematch))
			face(owner.position(), MycophantIntroRules.turnRateDegrees(rematch));
		MycophantMoves.introBeat(boss, t, rematch, owner);
		if (t >= MycophantIntroRules.length(rematch)) {
			boss.finishIntro();
			if (owner != null) begin(MycophantAttack.RUSHING_EMBRACE);
			else boss.setState(MycophantState.IDLE);
		}
	}

	// ------------------------------------------------------------------ roaming: drag, face and choose

	private void tickRoam() {
		if (pendingSplit) { startSplit(); return; }
		if (pendingStagger > 0) {
			forceStagger(pendingStagger, pendingStaggerFace);
			pendingStagger = 0;
			return;
		}
		LivingEntity quarry = boss.quarry();
		if (quarry == null) {
			enter(MycophantState.IDLE);
			return;
		}
		int phase = boss.getPhase();
		double distance = horizontalDistance(quarry.position());
		face(quarry.position(), MycophantControllerRules.turnRateDegrees(phase, false) * 1.5F);
		if (distance > 5.5D) {
			enter(MycophantState.DRAG);
			drag(boss.forward(), MycophantMotionRules.haulSpeed(boss.tickCount, phase));
		} else {
			enter(MycophantState.IDLE);
		}
		if (--decisionDelay > 0) return;
		MycophantAttack choice = MycophantControllerRules.choose(phase, distance,
				attack -> cooldowns[attack.ordinal()], sum -> boss.getRandom().nextInt(Math.max(1, sum)));
		if (choice == MycophantAttack.NONE) {
			decisionDelay = 10;
			return;
		}
		begin(choice);
	}

	void begin(MycophantAttack attack) {
		boss.setState(MycophantState.ATTACK);
		boss.setAttack(attack);
		cooldowns[attack.ordinal()] = MycophantControllerRules.cooldownTicks(attack, boss.getPhase());
		lockedAim = null;
		guarding = false;
		caughtThisMove = false;
		decisionDelay = boss.getPhase() >= 2 ? 16 : 24;
	}

	// ------------------------------------------------------------------ the move timeline

	private void tickAttack() {
		MycophantAttack attack = boss.getAttack();
		int phase = boss.getPhase();
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase);
		int tick = boss.getAttackTick();
		BossMoveTimeline.Stage stage = timeline.stageAt(tick);
		LivingEntity quarry = boss.quarry();
		if ((pendingSplit || pendingStagger > 0) && !boss.isHolding()) {
			MycophantControllerRules.Interrupt policy = MycophantControllerRules.interrupt(stage);
			if (policy == MycophantControllerRules.Interrupt.STAGGER || stage == BossMoveTimeline.Stage.RECOVERY
					|| stage == BossMoveTimeline.Stage.COMPLETE) {
				int ticks = pendingStagger > 0 ? pendingStagger : MycophantControllerRules.staggerTicks();
				Vec3 face = pendingStaggerFace;
				pendingStagger = 0;
				if (pendingSplit && policy != MycophantControllerRules.Interrupt.STAGGER) {
					endAttack();
					startSplit();
				} else {
					forceStagger(ticks, face);
				}
				return;
			}
		}
		if (stage == BossMoveTimeline.Stage.WINDUP && quarry != null)
			face(quarry.position(), MycophantControllerRules.turnRateDegrees(phase, true));
		boolean committedNow = MycophantControllerRules.shouldCommit(tick, timeline.commitTick(), lockedAim != null);
		if (committedNow) {
			lockedAim = quarry != null ? quarry.position() : boss.position().add(boss.forward().scale(6.0D));
			Vec3 to = lockedAim.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			lungeDirection = to.lengthSqr() < 1.0E-4D ? boss.forward() : to.normalize();
			double reach = Mth.clamp(Math.sqrt(to.lengthSqr()) - 2.5D, 2.0D, 13.0D);
			lungeSpeed = reach / Math.max(1, timeline.active());
		}
		MycophantMoves.tick(boss, this, attack, tick, stage, committedNow, lockedAim, quarry);
		if (boss.isHolding()) return; // the embrace holds its pose until the player breaks free or the meter fills
		if (stage == BossMoveTimeline.Stage.COMPLETE || boss.getState() != MycophantState.ATTACK) {
			if (boss.getState() == MycophantState.ATTACK) {
				endAttack();
				enter(MycophantState.IDLE);
			}
			return;
		}
		boss.setAttackTick(tick + 1);
	}

	void lunge() {
		drag(lungeDirection, lungeSpeed);
		face(boss.position().add(lungeDirection), 30.0F);
	}

	boolean caughtThisMove() {
		return caughtThisMove;
	}

	void markCaught() {
		caughtThisMove = true;
	}

	void skipToRecovery() {
		if (boss.getState() != MycophantState.ATTACK) return;
		BossMoveTimeline timeline = MycophantControllerRules.timeline(boss.getAttack(), boss.getPhase());
		boss.setAttackTick(Math.max(boss.getAttackTick(), timeline.windup() + timeline.active()));
		guarding = false;
	}

	private void endAttack() {
		guarding = false;
		boss.unlatchSiphon();
		if (boss.isHolding()) boss.releaseHold();
		boss.setAttack(MycophantAttack.NONE);
	}

	boolean isGuarding() {
		return guarding && boss.getState() == MycophantState.ATTACK && boss.getAttack() == MycophantAttack.WING_GUARD;
	}

	void setGuarding(boolean guarding) {
		this.guarding = guarding;
	}

	void breakGuard() {
		guarding = false;
		skipToRecovery();
	}

	// ------------------------------------------------------------------ staggers and the split

	/** A grief or a hit reaction that respects the interrupt policy. */
	void requestStagger(int ticks, @Nullable Vec3 face) {
		MycophantState state = boss.getState();
		if (state == MycophantState.INTRO || state == MycophantState.SPLIT || state == MycophantState.YIELD) return;
		if (state == MycophantState.STAGGER) {
			staggerLeft = Math.max(staggerLeft, ticks);
			if (face != null) staggerFace = face;
			return;
		}
		pendingStagger = Math.max(pendingStagger, ticks);
		pendingStaggerFace = face;
	}

	/** An immediate stagger (the hold was broken: there is no active move left to protect). */
	void forceStagger(int ticks, @Nullable Vec3 face) {
		if (boss.getState() == MycophantState.ATTACK) endAttack();
		boss.setState(MycophantState.STAGGER);
		staggerLeft = ticks;
		staggerFace = face;
	}

	void requestSplit() {
		pendingSplit = true;
	}

	private void tickStagger() {
		if (staggerFace != null) face(staggerFace, MycophantControllerRules.turnRateDegrees(boss.getPhase(), false));
		if (--staggerLeft > 0) return;
		staggerFace = null;
		if (pendingSplit) startSplit();
		else {
			enter(MycophantState.IDLE);
			decisionDelay = Math.min(decisionDelay, 8);
		}
	}

	private void startSplit() {
		pendingSplit = false;
		pendingStagger = 0;
		boss.releaseHold();
		boss.unlatchSiphon();
		boss.setAttack(MycophantAttack.NONE);
		boss.setState(MycophantState.SPLIT);
		boss.setSplitTick(0);
	}

	private void tickSplit() {
		int t = boss.getSplitTick() + 1;
		boss.setSplitTick(t);
		MycophantMoves.splitTick(boss, t);
		if (t >= MycophantSplitRules.length()) {
			boss.setPhase(2);
			boss.finishSplit();
			enter(MycophantState.IDLE);
			decisionDelay = 20;
		}
	}

	// ------------------------------------------------------------------ movement helpers

	private void enter(MycophantState state) {
		if (boss.getState() != state) boss.setState(state);
	}

	/** Moves her along {@code direction} at {@code speed} blocks per tick, staying on the ground. */
	void drag(Vec3 direction, double speed) {
		if (speed <= 0.0D) return;
		Vec3 flat = direction.multiply(1.0D, 0.0D, 1.0D);
		if (flat.lengthSqr() < 1.0E-6D) return;
		boss.move(MoverType.SELF, flat.normalize().scale(speed));
	}

	void face(Vec3 point, float maxStep) {
		double dx = point.x - boss.getX();
		double dz = point.z - boss.getZ();
		if (dx * dx + dz * dz < 1.0E-4D) return;
		float desired = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float yaw = Mth.approachDegrees(boss.getYRot(), desired, maxStep);
		boss.setYRot(yaw);
		boss.yBodyRot = yaw;
		boss.yHeadRot = yaw;
	}

	private double horizontalDistance(Vec3 point) {
		double dx = point.x - boss.getX();
		double dz = point.z - boss.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}
}

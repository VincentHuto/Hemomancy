package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantPositionRules.Band;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The one owner of the Spawn Mother's locomotion (the belly drag), facing (turn-rate limited) and move timeline.
 * Higher-priority events (the split, a grief or break stagger) never cut an active move: a wind-up staggers out, later
 * stages queue until the recovery boundary. It picks each move from where the quarry stands
 * ({@link MycophantSelectionRules}), chains follow-ups into strings, and starts the Last Brood once.
 */
final class MycophantController {
	/** Follow-ups never chain more than two moves deep. */
	private static final int MAX_CHAIN = 2;

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
	// the picker's memory: her recent moves and where the quarry has been standing
	private final MycophantSelectionRules.History history = new MycophantSelectionRules.History();
	private final MycophantSelectionRules.Pressure pressure = new MycophantSelectionRules.Pressure();
	// strings: the follow-up waiting out its link gap, how deep this chain is, the band as the move ended, its chance roll
	private MycophantAttack pendingFollowUp = MycophantAttack.NONE;
	private int chainDepth;
	@Nullable private Band followBand;
	private double followRoll;
	// the Last Brood fired during a sac hold or a stagger: it starts when that ends
	private boolean pendingLastBrood;
	// what the current move did
	private boolean hitLanded;
	private boolean netted;
	private float nursingDamage;

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
		samplePressure();
		checkLastBrood();
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
			pendingFollowUp = MycophantAttack.NONE;
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
		if (pendingFollowUp != MycophantAttack.NONE) {
			// the next move of a string: no picker, no anti-repeat damping, no cooldown check
			MycophantAttack next = pendingFollowUp;
			pendingFollowUp = MycophantAttack.NONE;
			chainDepth++;
			begin(next);
			return;
		}
		MycophantSelectionRules.Context context = new MycophantSelectionRules.Context(nursingTarget() != null);
		MycophantAttack choice = MycophantSelectionRules.choose(phase, MycophantPositionRules.band(distance),
				MycophantPositionRules.sector(boss.angleTo(quarry.position())), attack -> cooldowns[attack.ordinal()],
				history, pressure, context, sum -> boss.getRandom().nextInt(Math.max(1, sum)));
		if (choice == MycophantAttack.NONE) {
			decisionDelay = 10;
			return;
		}
		chainDepth = 0;
		begin(choice);
	}

	/** One sample a tick of the quarry's band, for the kite and hug pressure. */
	private void samplePressure() {
		LivingEntity quarry = boss.quarry();
		if (quarry != null) pressure.sample(MycophantPositionRules.band(horizontalDistance(quarry.position())));
	}

	/**
	 * The first time her health falls below a quarter in phase 2 she starts the Last Brood at once, cutting whatever
	 * move or pin she was in. A sac hold or a stagger is let finish first, as the split is.
	 */
	private void checkLastBrood() {
		MycophantState state = boss.getState();
		if (state == MycophantState.INTRO || state == MycophantState.SPLIT || state == MycophantState.YIELD || pendingSplit) return;
		if (state == MycophantState.ATTACK && boss.getAttack() == MycophantAttack.LAST_BROOD) return;
		if (!pendingLastBrood && !MycophantLastBroodRules.triggers(boss.getPhase(), boss.getHealth(), boss.getMaxHealth(),
				boss.isLastBroodDone())) return;
		pendingFollowUp = MycophantAttack.NONE;
		if (state == MycophantState.STAGGER || (boss.isHolding() && boss.getHoldKind().breakable())) {
			pendingLastBrood = true;
			return;
		}
		pendingLastBrood = false;
		pendingStagger = 0;
		pendingStaggerFace = null;
		// saved at once, so a reload mid-tell never plays it again
		boss.finishLastBrood();
		chainDepth = 0;
		beginNow(MycophantAttack.LAST_BROOD);
	}

	/** Follow-ups never chain more than two deep, and a pending split, stagger or Last Brood cancels the chain. */
	static boolean mayFollowUp(int depth, boolean interruptPending) {
		return depth < MAX_CHAIN && !interruptPending;
	}

	// ------------------------------------------------------------------ hooks for the move families

	/** The current move landed on its quarry (follow-ups keyed ON_HIT read this). */
	void markHit() { hitLanded = true; }
	boolean hitLanded() { return hitLanded; }
	/** The current move's net caught its quarry. */
	void markNetted() { netted = true; }
	boolean netted() { return netted; }
	/** The variant the current move was started with. */
	int variant() { return boss.getAttackVariant(); }
	/**
	 * The brood fungling she would nurse now: her nearest living, ungrown brood within
	 * {@link MycophantBroodRules#NURSE_RANGE}, while fewer than {@link MycophantBroodRules#GROWN_CAP} are grown.
	 */
	@Nullable
	FunglingEntity nursingTarget() {
		List<FunglingEntity> brood = boss.level().getEntitiesOfClass(FunglingEntity.class, boss.getBoundingBox().inflate(48.0D),
				f -> f.isAlive() && boss.getUUID().equals(f.getBroodMother()));
		int grown = 0;
		for (FunglingEntity fungling : brood) if (fungling.isGrown()) grown++;
		if (!MycophantBroodRules.mayNurse(brood.size(), grown)) return null;
		FunglingEntity nearest = null;
		double best = MycophantBroodRules.NURSE_RANGE * MycophantBroodRules.NURSE_RANGE;
		for (FunglingEntity fungling : brood) {
			if (fungling.isGrown()) continue;
			double distance = fungling.distanceToSqr(boss);
			if (distance <= best) {
				best = distance;
				nearest = fungling;
			}
		}
		return nearest;
	}
	/** Damage she took while nursing; the feeding breaks past MycophantBroodRules.NURSE_BREAK_DAMAGE. */
	void nursingDamage(float amount) { nursingDamage += amount; }
	float nursingDamageTaken() { return nursingDamage; }
	/** Where the current move locked its aim on the commit tick; null before it. */
	@Nullable Vec3 lockedAim() { return lockedAim; }

	/** A frontal blow landed on the Wing Guard: the riposte, an immediate Wing Sweep that counts as a follow-up. */
	void onGuardedHit() {
		if (!isGuarding()) return;
		int depth = chainDepth + 1;
		beginNow(MycophantAttack.WING_SWEEP);
		chainDepth = depth;
	}

	/** Starts a move at once, skipping the decision delay and ending the current one (the riposte, the Last Brood). */
	void beginNow(MycophantAttack attack) {
		if (boss.getState() == MycophantState.ATTACK) endAttack();
		pendingFollowUp = MycophantAttack.NONE;
		begin(attack);
	}

	void begin(MycophantAttack attack) {
		int phase = boss.getPhase();
		hitLanded = false;
		netted = false;
		nursingDamage = 0.0F;
		followBand = null;
		followRoll = boss.getRandom().nextDouble();
		boss.setState(MycophantState.ATTACK);
		boss.setAttack(attack);
		boss.setAttackVariant(MycophantSelectionRules.rollVariant(attack, phase, n -> boss.getRandom().nextInt(Math.max(1, n))));
		history.record(attack);
		if (attack.isEmbrace()) pressure.resetKite();
		cooldowns[attack.ordinal()] = MycophantControllerRules.cooldownTicks(attack, phase);
		lockedAim = null;
		guarding = false;
		caughtThisMove = false;
		decisionDelay = boss.getPhase() >= 2 ? 16 : 24;
	}

	// ------------------------------------------------------------------ the move timeline

	private void tickAttack() {
		MycophantAttack attack = boss.getAttack();
		int phase = boss.getPhase();
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase, boss.getAttackVariant());
		int tick = boss.getAttackTick();
		BossMoveTimeline.Stage stage = timeline.stageAt(tick);
		LivingEntity quarry = boss.quarry();
		// the Last Brood's flag is spent when it starts, so a grief in its tell must wait rather than cut it for good
		if ((pendingSplit || pendingStagger > 0) && !boss.isHolding() && !MycophantControllerRules.queuesInterrupts(attack)) {
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
		if (boss.getState() == MycophantState.ATTACK && (stage == BossMoveTimeline.Stage.RECOVERY
				|| stage == BossMoveTimeline.Stage.COMPLETE) && queueFollowUp(attack, phase, quarry)) return;
		if (stage == BossMoveTimeline.Stage.COMPLETE || boss.getState() != MycophantState.ATTACK) {
			if (boss.getState() == MycophantState.ATTACK) {
				endAttack();
				enter(MycophantState.IDLE);
			}
			return;
		}
		// a move that skipped itself to recovery this tick (the siphon, the net, the cocoon) has moved the clock on already;
		// a move that started another (a riposte, a trigger) owns a fresh clock
		if (boss.getAttack() == attack) boss.setAttackTick(Math.max(boss.getAttackTick(), tick + 1));
	}

	/**
	 * Asks, through the recovery, for the move that follows {@code done}: a string cuts the recovery short and starts
	 * its next move after the link gap. The band is the one the quarry stood in as the move ended and the chance is
	 * rolled once per move, so only a hit or a net landing late in the recovery can still change the answer.
	 */
	private boolean queueFollowUp(MycophantAttack done, int phase, @Nullable LivingEntity quarry) {
		boolean interrupt = pendingSplit || pendingStagger > 0 || pendingLastBrood;
		if (quarry == null || !mayFollowUp(chainDepth, interrupt)) return false;
		if (followBand == null) followBand = MycophantPositionRules.band(horizontalDistance(quarry.position()));
		double roll = followRoll;
		MycophantAttack next = MycophantSelectionRules.followUp(done, phase, hitLanded, netted, followBand, chainDepth,
				interrupt, () -> roll);
		if (next == MycophantAttack.NONE) return false;
		endAttack();
		enter(MycophantState.IDLE);
		pendingFollowUp = next;
		decisionDelay = MycophantSelectionRules.linkGapTicks(phase);
		return true;
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
		BossMoveTimeline timeline = MycophantControllerRules.timeline(boss.getAttack(), boss.getPhase(), boss.getAttackVariant());
		boss.setAttackTick(Math.max(boss.getAttackTick(), timeline.windup() + timeline.active()));
		guarding = false;
	}

	private void endAttack() {
		MycophantAttack ending = boss.getAttack();
		guarding = false;
		boss.unlatchSiphon();
		if (boss.isHolding()) boss.releaseHold();
		if (ending == MycophantAttack.LAST_BROOD) MycophantLastBrood.clear(boss);
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
		pendingFollowUp = MycophantAttack.NONE;
		// a grief queued through a hold or the Last Brood's pull folds into this stagger instead of starting a second one
		ticks = Math.max(ticks, pendingStagger);
		pendingStagger = 0;
		pendingStaggerFace = null;
		if (boss.getState() == MycophantState.ATTACK) endAttack();
		boss.setState(MycophantState.STAGGER);
		staggerLeft = ticks;
		staggerFace = face;
	}

	void requestSplit() {
		pendingSplit = true;
		pendingFollowUp = MycophantAttack.NONE;
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
		pendingFollowUp = MycophantAttack.NONE;
		chainDepth = 0;
		boss.releaseHold();
		boss.unlatchSiphon();
		boss.setAttack(MycophantAttack.NONE);
		boss.setState(MycophantState.SPLIT);
		boss.setSplitTick(0);
		BossScenes.play(boss, BossPresentationIds.SCENE_MYCOPHANT_SPLIT_CARD);
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

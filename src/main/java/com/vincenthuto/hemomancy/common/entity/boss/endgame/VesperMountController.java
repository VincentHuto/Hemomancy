package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The single owner of the grounded Crowned Refusal's body: locomotion, facing and the active move's timeline.
 * Nothing else stops navigation or snaps yaw, so moves always finish or wind down instead of being cut off.
 */
public final class VesperMountController {
	private static final double ATTACK_RANGE = 64.0D;

	private final VesperTheCrownedRefusalEntity boss;
	@Nullable private VesperMountControllerRules.Priority pending;
	private Vec3 lockedAim = Vec3.ZERO;
	private int strafeSide = 1;
	private int strafeTicks;

	public VesperMountController(VesperTheCrownedRefusalEntity boss) {
		this.boss = boss;
	}

	/** Asks for a higher-priority beat; it is granted only per {@link VesperMountControllerRules#interrupt}. */
	public void request(VesperMountControllerRules.Priority priority) {
		if (pending == null || rank(priority) > rank(pending)) pending = priority;
	}

	/** Returns and clears the pending request once the body is free to act on it. */
	@Nullable
	public VesperMountControllerRules.Priority takeGranted() {
		if (busy() || pending == null) return null;
		VesperMountControllerRules.Priority granted = pending;
		pending = null;
		return granted;
	}

	public boolean busy() {
		return boss.getAttack() != VesperPhaseOneAttack.IDLE || boss.getStaggerTick() > 0;
	}

	public Vec3 lockedAim() {
		return lockedAim;
	}

	public void clear() {
		pending = null;
		boss.setStaggerTick(0);
	}

	@Nullable
	public LivingEntity target() {
		LivingEntity target = EndgameBossActions.acquireTarget(boss, ATTACK_RANGE);
		if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return null;
		return target;
	}

	public void begin(VesperPhaseOneAttack attack, @Nullable LivingEntity target) {
		lockedAim = target != null ? target.position() : boss.position().add(boss.getLookAngle().scale(4.0D));
		boss.beginGroundAttack(attack);
	}

	/** One grounded tick. Returns true while a move or stagger owns the body. */
	public boolean tickGround(@Nullable LivingEntity target, @Nullable VesperOrdealManager.FlightArena arena) {
		if (boss.getStaggerTick() > 0) {
			boss.setStaggerTick(boss.getStaggerTick() - 1);
			holdStill();
			return true;
		}
		VesperPhaseOneAttack attack = boss.getAttack();
		if (attack == VesperPhaseOneAttack.IDLE) {
			locomote(target, arena);
			return false;
		}
		VesperMoveTimeline timeline = VesperMountControllerRules.timeline(attack);
		int tick = boss.getAttackTick() + 1;
		VesperMoveTimeline.Stage stage = timeline.stageAt(tick);
		if (pending != null) {
			VesperMountControllerRules.Interrupt interrupt = VesperMountControllerRules.interrupt(stage, pending);
			if (interrupt != VesperMountControllerRules.Interrupt.QUEUE) {
				boss.endGroundAttack();
				if (interrupt == VesperMountControllerRules.Interrupt.STAGGER) {
					boss.setStaggerTick(VesperMountControllerRules.STAGGER_TICKS);
				}
				holdStill();
				return true;
			}
		}
		switch (VesperMountControllerRules.nextState(stage, target != null)) {
			case SKIP_TO_RECOVERY -> {
				tick = timeline.windup() + timeline.active();
				stage = VesperMoveTimeline.Stage.RECOVERY;
			}
			default -> { }
		}
		if (VesperMountControllerRules.tracksTarget(stage) && target != null) lockedAim = target.position();
		if (stage != VesperMoveTimeline.Stage.RECOVERY) face(lockedAim, VesperMountMoveControl.GROUND_TURN_DEGREES);
		boss.setAttackTick(tick);
		VesperMountMoves.tick(boss, attack, tick, stage, lockedAim, target);
		if (timeline.stageAt(tick) == VesperMoveTimeline.Stage.COMPLETE) boss.endGroundAttack();
		return true;
	}

	/** Turns the body toward a point at a capped rate; the head and body follow the same yaw. */
	public void face(Vec3 point, float maxStep) {
		double dx = point.x - boss.getX();
		double dz = point.z - boss.getZ();
		if (dx * dx + dz * dz < 1.0E-4D) return;
		float desired = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float yaw = VesperMountControllerRules.turnToward(boss.getYRot(), desired, maxStep);
		boss.setYRot(yaw);
		boss.setYBodyRot(yaw);
		boss.setYHeadRot(yaw);
	}

	private void locomote(@Nullable LivingEntity target, @Nullable VesperOrdealManager.FlightArena arena) {
		VesperMountMoveControl move = (VesperMountMoveControl) boss.getMoveControl();
		if (target == null) {
			if (arena == null) {
				holdStill();
				return;
			}
			double[] point = VesperMountControllerRules.patrolPoint(arena.centerX(), arena.centerZ(),
					boss.level().getGameTime());
			move.setWantedPosition(point[0], boss.getY(), point[1], 0.7D);
			return;
		}
		double distance = boss.distanceTo(target);
		if (distance > 8.0D) {
			move.setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.25D);
			return;
		}
		if (--strafeTicks <= 0) {
			strafeSide = boss.getRandom().nextBoolean() ? 1 : -1;
			strafeTicks = 30 + boss.getRandom().nextInt(25);
		}
		face(target.position(), VesperMountMoveControl.GROUND_TURN_DEGREES);
		float forward = distance < 3.5D ? -1.0F : 0.15F;
		move.strafe(forward, strafeSide, distance < 3.5D ? 0.9D : 0.7D);
	}

	private void holdStill() {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
	}

	private static int rank(VesperMountControllerRules.Priority priority) {
		return switch (priority) {
			case TRANSITION -> 3;
			case WOUND -> 2;
			case BURROW_SURFACE -> 1;
			case WING_GROWTH -> 0;
		};
	}
}

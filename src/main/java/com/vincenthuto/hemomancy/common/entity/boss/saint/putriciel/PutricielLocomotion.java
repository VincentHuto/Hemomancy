package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLocomotion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMotor;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Putriciel's way of moving (design section 8). In the rot, the shamble: a lurching step of 0.9 blocks every 16 ticks
 * (Rot III every 12), holding the middle of the boss floor and closing on the quarry only once it has kited (MID or
 * FAR) for more than 60 of the last 200 ticks. In the fire, the stride: an upright walk closing to NEAR on the most
 * colonised challenger, whom the interval hunts. Still through every move and opening. In the rot it prefers to stand
 * in a pool of its sludge or on a colony patch near the middle (revision 2026-11-06, R2.3); the sludge never slows it.
 */
public final class PutricielLocomotion implements SaintLocomotion {
	public static final double STEP = 0.9D;
	public static final int STEP_TICKS = 6;
	/** Kiting this long (of the last 200 ticks) draws the shamble toward the quarry. */
	public static final int KITE_TO_FOLLOW = 60;
	/** The middle it holds: within this of the boss floor's centre it stands. */
	public static final double HOLD_RADIUS = 3.0D;
	/** The stride closes to this distance, and walks at this speed modifier (about 0.11 blocks a tick). */
	public static final double STRIDE_CLOSE = 5.0D;
	public static final double STRIDE_SPEED = 0.55D;
	public static final double SHAMBLE_FOLLOW_CLOSE = 5.5D;
	/** A pool or colony patch this far beyond the hold radius still draws the hold; it stands within this of its centre. */
	public static final double ROT_PULL = 4.0D, ROT_STAND = 0.75D;
	/** Turning in place toward the Offering's challenger. */
	public static final float OFFERING_TURN = 6.0F;

	@Override
	public void roam(SaintTrialEntity saint, SaintMotor motor, @Nullable LivingEntity target) {
		if (!(saint instanceof PutricielEntity putriciel)) {
			motor.stop();
			return;
		}
		SaintArena arena = saint.arena();
		PutricielPhase phase = putriciel.currentPhase();
		if (putriciel.stage().fire()) {
			if (target == null || saint.distanceTo(target) <= STRIDE_CLOSE) motor.stop();
			else motor.walk(target.position().subtract(saint.position()), STRIDE_SPEED);
			return;
		}
		if (motor.lunging()) return;
		Vec3 goal = null;
		if (target != null && saint.controller().pressure().kiteTicks() > KITE_TO_FOLLOW
				&& saint.distanceTo(target) > SHAMBLE_FOLLOW_CLOSE) {
			goal = target.position();
		} else if (arena != null) {
			AABB floor = arena.bossFloor();
			Vec3 centre = new Vec3(floor.getCenter().x, saint.getY(), floor.getCenter().z);
			// it stands in or beside the rot it laid: a pool or colony patch near the middle draws the hold
			Vec3 rot = putriciel.restingRot(centre, HOLD_RADIUS + ROT_PULL);
			if (rot != null) {
				if (rot.distanceTo(saint.position()) > ROT_STAND) goal = rot;
			} else if (centre.distanceTo(saint.position()) > HOLD_RADIUS) {
				goal = centre;
			}
		}
		if (goal == null) {
			motor.stop();
			return;
		}
		if (arena != null) goal = clampToFloor(arena.bossFloor(), goal);
		if (saint.tickCount % Math.max(1, phase.stepCycle()) == 0) {
			motor.lunge(goal.subtract(saint.position()), STEP, STEP_TICKS);
		}
	}

	@Override
	public void inMove(SaintTrialEntity saint, SaintMotor motor, SaintMoveContext ctx) {
		if (!motor.lunging()) motor.stop();
	}

	@Override
	public @Nullable LivingEntity chooseTarget(SaintTrialEntity saint, List<Player> challengers,
			@Nullable LivingEntity current) {
		if (saint instanceof PutricielEntity putriciel && putriciel.stage().interval()) {
			Player chosen = null;
			float most = -1.0F;
			double nearest = Double.MAX_VALUE;
			for (Player player : challengers) {
				float value = putriciel.colonisation(player);
				double distance = saint.distanceToSqr(player);
				if (value > most + 1.0E-3F || Math.abs(value - most) <= 1.0E-3F && distance < nearest) {
					chosen = player;
					most = value;
					nearest = distance;
				}
			}
			if (chosen != null) return chosen;
		}
		return SaintCombatController.defaultTarget(saint, challengers);
	}

	@Override
	public float turnRate(SaintTrialEntity saint, boolean trackingWindup) {
		if (!(saint instanceof PutricielEntity putriciel)) return 6.0F;
		if (putriciel.controller().move() == PutricielMove.OFFERING) return OFFERING_TURN;
		return putriciel.currentPhase().turnRate();
	}

	@Override
	public int decisionDelay(SaintTrialEntity saint) {
		return saint instanceof PutricielEntity putriciel ? putriciel.currentPhase().decisionDelay() : 20;
	}

	/** It never walks onto a pyre or into the aisles: goals stay on the boss floor, a block in from its edge. */
	static Vec3 clampToFloor(AABB floor, Vec3 point) {
		return new Vec3(Math.max(floor.minX + 1.0D, Math.min(floor.maxX - 1.0D, point.x)), point.y,
				Math.max(floor.minZ + 1.0D, Math.min(floor.maxZ - 1.0D, point.z)));
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLocomotion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMotor;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * The stalk (design section 8): a staff-planted three-beat walk, hungry and quick when he is hollow, lumbering when he
 * is full. Hollow he wants to be close, where his drain takes most; filling, a little off; brimming, at middle range or
 * in the pit when he means to vent or drink. Sunken, he lurches at a quarry that keeps out of reach. He wants the
 * fullest vessel he can see; the Reckoning wants the most indebted. Every motion goes through the kit's motor.
 */
public final class HemorathStalk implements SaintLocomotion {
	/** The mob's base movement speed; a state's speed is asked for as a multiple of it. */
	public static final double BASE_SPEED = 0.25D;
	public static final double PHASE_2_GAIT = 1.15D;
	/** How close each state likes to stand. */
	public static final double SUNKEN_RANGE = 2.5D;
	public static final double FILLING_RANGE = 5.0D;
	public static final double BRIMMING_RANGE = 8.0D;
	public static final double HOLD_SLACK = 1.0D;
	/** The Sunken lurch: after 40 ticks with the quarry out of HUG, 2.5 blocks in 6 ticks, never twice inside 80. */
	public static final int LURCH_AFTER = 40;
	public static final double LURCH_DISTANCE = 2.5D;
	public static final int LURCH_TICKS = 6;
	public static final int LURCH_GAP = 80;
	/** The Last Thirst's drag to the pit. */
	public static final double DRAG_SPEED = 0.25D;
	/** Tracking turn rate while a wind-up follows its target. */
	public static final float TRACK_TURN = 4.0F;
	/** Who he wants: 10 x the blood fraction they carry, less their distance, among those he sees within 24. */
	public static final double TARGET_RANGE = 24.0D;
	public static final double FULLNESS_WEIGHT = 10.0D;

	private final HemorathEntity saint;
	private int outOfReach;
	private int lastLurch = -LURCH_GAP;

	public HemorathStalk(HemorathEntity saint) {
		this.saint = saint;
	}

	@Override
	public void roam(SaintTrialEntity body, SaintMotor motor, @Nullable LivingEntity target) {
		HemorathMeterRules.State state = saint.state();
		double speed = speed(state);
		HemorathArena chamber = saint.chamber();
		if (saint.thirsting() && chamber != null) {
			if (!chamber.inPit(saint.position())) motor.pathTo(chamber.pit(), DRAG_SPEED / BASE_SPEED);
			else motor.stop();
			return;
		}
		if (chamber != null && saint.wantsPit() && !chamber.inPit(saint.position())) {
			motor.pathTo(chamber.pit(), speed);
			return;
		}
		if (target == null) {
			motor.stop();
			return;
		}
		Vec3 to = target.position().subtract(saint.position());
		double distance = Math.hypot(to.x, to.z);
		double wanted = switch (state) {
			case SUNKEN -> SUNKEN_RANGE;
			case FILLING -> FILLING_RANGE;
			case BRIMMING -> BRIMMING_RANGE;
		};
		outOfReach = distance >= com.vincenthuto.hemomancy.common.entity.boss.BossBands.STANDARD.hug() ? outOfReach + 1 : 0;
		if (state == HemorathMeterRules.State.SUNKEN && outOfReach >= LURCH_AFTER
				&& saint.tickCount - lastLurch >= LURCH_GAP && !motor.lunging()) {
			lastLurch = saint.tickCount;
			outOfReach = 0;
			motor.lunge(to, Math.min(LURCH_DISTANCE, Math.max(0.0D, distance - 1.5D)), LURCH_TICKS);
			return;
		}
		if (motor.lunging()) return;
		if (distance > wanted + HOLD_SLACK) motor.pathTo(target.position(), speed);
		else if (distance < wanted - HOLD_SLACK) motor.walk(to.scale(-1.0D), speed);
		else motor.stop();
	}

	@Override
	public void inMove(SaintTrialEntity body, SaintMotor motor, SaintMoveContext ctx) {
		HemorathArena chamber = saint.chamber();
		if (ctx.move() == HemorathMove.LAST_THIRST && ctx.stage() == BossMoveTimeline.Stage.WINDUP && chamber != null) {
			if (!chamber.inPit(saint.position())) motor.pathTo(chamber.pit(), DRAG_SPEED / BASE_SPEED);
			else motor.stop();
			return;
		}
		if (!motor.lunging()) motor.stop();
	}

	@Override
	@Nullable
	public LivingEntity chooseTarget(SaintTrialEntity body, List<Player> challengers, @Nullable LivingEntity current) {
		if (challengers.isEmpty()) return null;
		if (saint.controller().move() == HemorathMove.TITHE_RECKONING) {
			UUID owed = saint.tithe().mostIndebted(challengers.stream().map(Player::getUUID).toList());
			for (Player player : challengers) if (player.getUUID().equals(owed)) return player;
		}
		Player best = null;
		double bestScore = -Double.MAX_VALUE;
		for (Player player : challengers) {
			double distance = saint.distanceTo(player);
			if (distance > TARGET_RANGE || !saint.hasLineOfSight(player)) continue;
			double score = score(fullness(player), distance);
			if (score > bestScore) {
				bestScore = score;
				best = player;
			}
		}
		return best != null ? best : SaintCombatController.defaultTarget(body, challengers);
	}

	/** The pure targeting score: the fullest vessel, nearer first. */
	public static double score(double bloodFraction, double distance) {
		return FULLNESS_WEIGHT * bloodFraction - distance;
	}

	private static double fullness(Player player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive() || volume.getMaxBloodVolume() <= 0.0D) return 0.0D;
		return Math.max(0.0D, Math.min(1.0D, volume.getBloodVolume() / volume.getMaxBloodVolume()));
	}

	@Override
	public float turnRate(SaintTrialEntity body, boolean trackingWindup) {
		return trackingWindup ? TRACK_TURN : saint.state().turnRate;
	}

	@Override
	public int decisionDelay(SaintTrialEntity body) {
		return HemorathPhaseRules.decisionDelay(saint.state().decisionDelay, saint.phases().phase(), saint.thirsting());
	}

	/** The move-control speed multiple for a state's walk, 15% quicker in phase 2. */
	public double speed(HemorathMeterRules.State state) {
		double gait = saint.phases().phase() >= 2 ? PHASE_2_GAIT : 1.0D;
		return state.speed * gait / BASE_SPEED;
	}
}

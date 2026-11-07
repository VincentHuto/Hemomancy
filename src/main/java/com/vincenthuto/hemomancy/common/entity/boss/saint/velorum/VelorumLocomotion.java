package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLocomotion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMotor;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Near-stillness (design section 8): a glide on the rime, never a stride; he holds MID and closes only for a melee move
 * or a target that has stayed FAR, settles after every move, stands his first 40 ticks on the dais, keeps to the boss
 * floor and a block round it, and from phase 3 is planted at the blue-ice ring's centre and only turns. Since the
 * 2026-11-06 revision he never walks to a far target in phase 2: the Shadow Step closes for him, and at the phase-3
 * card he sinks into the dark and rises on the ring.
 */
public final class VelorumLocomotion implements SaintLocomotion {
	/** He keeps to the boss floor and a block round it; the aisles are where the congregation kneels. */
	private static final double FLOOR_MARGIN = 1.0D;

	@Override
	public void roam(SaintTrialEntity saint, SaintMotor motor, @Nullable LivingEntity target) {
		if (!(saint instanceof VelorumEntity velorum)) return;
		if (velorum.lessonLeft() > 0 || velorum.gripped()) {
			motor.stop();
			return;
		}
		if (velorum.phases().phase() >= 3) {
			plant(velorum, motor);
			return;
		}
		if (target == null) {
			motor.stop();
			return;
		}
		double distance = horizontal(velorum.position(), target.position());
		double away = target instanceof Player player ? awaySpeed(velorum, player) : 0.0D;
		if (VelorumLocomotionRules.closesByGlide(velorum.phases().phase())
				&& VelorumLocomotionRules.approach(distance, velorum.farTicks(), away) > 0) {
			glide(velorum, motor, target.position());
		} else if (!velorum.onFloor()) {
			// down off the dais once the lesson is over
			glide(velorum, motor, velorum.arena() == null ? target.position() : velorum.arena().bossFloor().getCenter());
		} else {
			motor.stop();
		}
	}

	@Override
	public void inMove(SaintTrialEntity saint, SaintMotor motor, SaintMoveContext ctx) {
		if (!(saint instanceof VelorumEntity velorum) || velorum.gripped()) {
			motor.stop();
			return;
		}
		if (velorum.phases().phase() >= 3) {
			plant(velorum, motor);
			return;
		}
		SaintMove move = ctx.move();
		if (move == VelorumMove.UMBRAL_STEP) {
			// he never walks: he is elsewhere
			motor.stop();
			return;
		}
		LivingEntity target = ctx.target();
		boolean closing = ctx.stage() == BossMoveTimeline.Stage.WINDUP && move.kind() == SaintMove.Kind.MELEE
				|| move == VelorumMove.SET_IN_PLACE && ctx.stage() == BossMoveTimeline.Stage.WINDUP;
		if (closing && target != null && horizontal(velorum.position(), target.position()) > VelorumLocomotionRules.MELEE_CLOSE) {
			glide(velorum, motor, target.position());
		} else {
			motor.stop();
		}
	}

	/** No pick while he settles, during the dais lesson, while an opening is open, or through a phase card. */
	@Override
	public boolean mayPick(SaintTrialEntity saint) {
		if (!(saint instanceof VelorumEntity velorum)) return true;
		return velorum.settleLeft() <= 0 && velorum.lessonLeft() <= 0 && !velorum.openings().isOpen()
				&& velorum.phases().cardTicks() <= 0 && !velorum.hourPending() && !velorum.cardStepping()
				&& !velorum.eclipsePending();
	}

	/** He keeps the target he chose while it stays in sight; otherwise he prefers the stillest challenger. */
	@Override
	public @Nullable LivingEntity chooseTarget(SaintTrialEntity saint, List<Player> challengers,
			@Nullable LivingEntity current) {
		if (!(saint instanceof VelorumEntity velorum)) return SaintLocomotion.super.chooseTarget(saint, challengers, current);
		return velorum.chooseTarget(challengers, current);
	}

	@Override
	public float turnRate(SaintTrialEntity saint, boolean trackingWindup) {
		if (!(saint instanceof VelorumEntity velorum)) return VelorumLocomotionRules.TURN[0];
		if (velorum.settleLeft() > 0) return 0.0F;
		return VelorumLocomotionRules.turn(velorum.phases().phase(), speedFactor(velorum), velorum.gripped());
	}

	@Override
	public int decisionDelay(SaintTrialEntity saint) {
		boolean repose = VelorumDeedRules.repose(saint.deeds().mask());
		return VelorumLocomotionRules.decisionDelay(saint.phases().phase(), repose);
	}

	private static void plant(VelorumEntity velorum, SaintMotor motor) {
		Vec3 ring = velorum.ringCentre();
		if (ring == null || velorum.cardStepping()
				|| horizontal(velorum.position(), ring) <= VelorumLocomotionRules.PLANTED_REACH) {
			motor.stop();
			return;
		}
		// the card steps him to the ring through the dark; a fight loaded in phase 3 glides there instead
		motor.glide(new Vec3(ring.x, velorum.getY(), ring.z), VelorumLocomotionRules.GLIDE[1] * speedFactor(velorum), 0.0D);
	}

	private static void glide(VelorumEntity velorum, SaintMotor motor, Vec3 toward) {
		double speed = VelorumLocomotionRules.glide(velorum.phases().phase(), speedFactor(velorum));
		if (speed <= 0.0D) {
			motor.stop();
			return;
		}
		Vec3 point = new Vec3(toward.x, velorum.getY(), toward.z);
		if (velorum.arena() != null) {
			AABB floor = velorum.arena().bossFloor().inflate(FLOOR_MARGIN, 0.0D, FLOOR_MARGIN);
			point = new Vec3(Math.max(floor.minX + 0.5D, Math.min(floor.maxX - 0.5D, point.x)), point.y,
					Math.max(floor.minZ + 0.5D, Math.min(floor.maxZ - 0.5D, point.z)));
		}
		motor.glide(point, speed, 0.0D);
		velorum.glided();
	}

	/** The movement-speed attribute over its base: the enrage's and the Repose deed's bonus. */
	static double speedFactor(LivingEntity entity) {
		AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null || speed.getBaseValue() <= 1.0E-6D) return 1.0D;
		return speed.getValue() / speed.getBaseValue();
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}

	private static double awaySpeed(VelorumEntity velorum, Player player) {
		Vec3 motion = new Vec3(player.getX() - player.xo, 0.0D, player.getZ() - player.zo);
		Vec3 away = new Vec3(player.getX() - velorum.getX(), 0.0D, player.getZ() - velorum.getZ());
		if (away.lengthSqr() < 1.0E-6D) return 0.0D;
		return motion.dot(away.normalize());
	}
}

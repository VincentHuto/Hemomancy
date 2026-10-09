package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.function.Predicate;
import net.minecraft.world.phys.Vec3;

/** Pure fight rules: health stages, move choice, cooldowns and the numbers each move uses. */
public final class NaeglerophaeonCombatRules {
	public static final float MAX_HEALTH = 150.0F;
	public static final float PHASE_TWO_FRACTION = .5F;
	public static final float OVERLOAD_FRACTION = .10F;
	public static final float OVERLOAD_FLOOR_FRACTION = .01F;

	public static final float ZAP_DAMAGE = 4, DISCHARGE_DAMAGE = 8, LUNGE_DAMAGE = 6, LASH_DAMAGE = 5,
			SPARK_DAMAGE = 2.5F, CONDUCT_DAMAGE = 4, NOVA_DAMAGE = 7, HOMING_DAMAGE = 3, DRAIN_DAMAGE = 1;
	public static final double LASH_RADIUS = 5, NOVA_RADIUS = 10, CONDUCT_REACH = 1.5;
	public static final int LUNGE_WINDUP = 14, LUNGE_TICKS = 8, LUNGE_STUN = 20, LASH_WINDUP = 10,
			VOLLEY_INTERVAL = 6, SPARK_TRAVEL = 6, CONDUCT_WINDUP = 15, CONDUCT_CELLS = 40, NOVA_WINDUP = 30,
			NOVA_SPREAD = 10, DIVE_TICKS = 12, EMERGE_WARNING = 10, TRANSITION_TICKS = 40, RETALIATE_WINDUP = 10,
			RETALIATE_COOLDOWN = 40, OVERLOAD_PULSE = 30, BLAST_TRAVEL = 8, HOMING_LIFETIME = 80, MAX_HOMING = 14,
			ASCEND_LIMIT = 100;
	public static final double LUNGE_SPEED = 1.6, BLINK_SPEED = 32.0 / 20.0;
	/** Blocks and radians per tick: slower than a sprint (~.28), faster than a walk, with a ~3-block turning circle. */
	public static final double HOMING_SPEED = .26, HOMING_TURN = .09;

	public enum Move { NONE, LASH, CAPTURE, NOVA, LUNGE, CONDUCT, VOLLEY, CHARGE }

	private NaeglerophaeonCombatRules() {}

	public static Vec3 gripPosition(Vec3 feet, double height, float yaw) {
		return feet.add(new Vec3(0, height * .48, .48).yRot((float)-Math.toRadians(yaw)));
	}

	public static boolean breaksGrab(float damageDealtDuringGrab) {
		return damageDealtDuringGrab >= 8.0F;
	}

	public static boolean phaseTwo(float health, float max) {
		return health <= max * PHASE_TWO_FRACTION;
	}

	public static float overloadLine(float max) { return max * OVERLOAD_FRACTION; }
	public static float overloadFloor(float max) { return max * OVERLOAD_FLOOR_FRACTION; }

	/**
	 * Health the boss may actually drop to. Before Overload nothing skips the finale; during it the
	 * boss cannot die; once Drained (or for void/kill damage) ordinary health rules apply.
	 */
	public static float allowedHealth(float proposed, float max, boolean overloading, boolean drained, boolean bypass) {
		if (bypass || drained) return proposed;
		return Math.max(proposed, overloading ? overloadFloor(max) : overloadLine(max));
	}

	public static boolean startsOverload(float health, float max, boolean overloading, boolean drained) {
		return !overloading && !drained && health <= overloadLine(max);
	}

	public static int cooldown(Move move, boolean phaseTwo) {
		return switch (move) {
			case LASH -> phaseTwo ? 60 : 80;
			case CAPTURE -> phaseTwo ? 160 : 200;
			case NOVA -> phaseTwo ? 180 : 260;
			case LUNGE -> phaseTwo ? 90 : 140;
			case CONDUCT -> phaseTwo ? 120 : 220;
			case VOLLEY -> phaseTwo ? 70 : 110;
			case CHARGE -> phaseTwo ? 60 : 90;
			case NONE -> 0;
		};
	}

	/** Delay before a fresh boss may open with each move, so the fight starts with its grab and bolt. */
	public static int openingCooldown(Move move) {
		return switch (move) {
			case LUNGE -> 120;
			case VOLLEY -> 100;
			case CONDUCT -> 160;
			case NOVA -> 200;
			default -> 0;
		};
	}

	public static int recoveryTicks(boolean phaseTwo) { return phaseTwo ? 25 : 40; }
	public static int volleyCount(boolean phaseTwo) { return phaseTwo ? 5 : 3; }
	public static int lungeCount(boolean phaseTwo) { return phaseTwo ? 2 : 1; }
	public static float drainHeal(boolean phaseTwo) { return phaseTwo ? 3 : 2; }

	/** 2-6 sparks per broken node, never more than the live cap allows. */
	public static int homingSparks(int roll, int live) {
		return Math.max(0, Math.min(2 + Math.floorMod(roll, 5), MAX_HOMING - live));
	}

	public static Move choose(double distance, boolean lineOfSight, boolean phaseTwo, int crowd,
			boolean nodeNearTarget, Predicate<Move> ready) {
		if (!lineOfSight) return Move.NONE;
		if (distance <= LASH_RADIUS && ready.test(Move.LASH)) return Move.LASH;
		if (distance <= 12 && ready.test(Move.CAPTURE)) return Move.CAPTURE;
		if ((crowd >= 2 && distance <= 8 || phaseTwo && distance <= NOVA_RADIUS) && ready.test(Move.NOVA)) return Move.NOVA;
		if (distance >= 6 && distance <= 20 && ready.test(Move.LUNGE)) return Move.LUNGE;
		if (nodeNearTarget && distance <= 24 && ready.test(Move.CONDUCT)) return Move.CONDUCT;
		if (distance >= 8 && distance <= 24 && ready.test(Move.VOLLEY)) return Move.VOLLEY;
		if (distance <= 16 && ready.test(Move.CHARGE)) return Move.CHARGE;
		return Move.NONE;
	}

	/** Steers a homing spark toward its target, turning by at most {@link #HOMING_TURN} radians. */
	public static Vec3 steer(Vec3 velocity, Vec3 toTarget) {
		if (toTarget.lengthSqr() < 1.0E-8) return velocity;
		Vec3 current = velocity.lengthSqr() < 1.0E-8 ? toTarget.normalize() : velocity.normalize();
		Vec3 wanted = toTarget.normalize();
		double angle = Math.acos(Math.clamp(current.dot(wanted), -1, 1));
		if (angle <= HOMING_TURN) return wanted.scale(HOMING_SPEED);
		Vec3 axis = current.cross(wanted);
		if (axis.lengthSqr() < 1.0E-8) axis = Math.abs(current.y) < .9 ? current.cross(new Vec3(0, 1, 0)) : current.cross(new Vec3(1, 0, 0));
		axis = axis.normalize();
		double c = Math.cos(HOMING_TURN), s = Math.sin(HOMING_TURN);
		Vec3 turned = current.scale(c).add(axis.cross(current).scale(s)).add(axis.scale(axis.dot(current) * (1 - c)));
		return turned.normalize().scale(HOMING_SPEED);
	}
}

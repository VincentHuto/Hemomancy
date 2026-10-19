package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Velocity steering for the winged mount: it turns at a capped rate, accelerates toward its heading instead of
 * snapping, eases into arrival, and reports the bank and pitch the model leans into.
 */
public final class VesperFlightSteeringRules {
	public static final float BANK_MAX = 35.0F;
	public static final float PITCH_MAX = 40.0F;
	private static final float BANK_PER_DEGREE_OF_TURN = 1.6F;

	private VesperFlightSteeringRules() {
	}

	public record Steer(Vec3 velocity, float yaw, float bank, float pitch) {
	}

	public static Steer step(Vec3 pos, Vec3 vel, float yaw, Vec3 desired, double maxSpeed, double accel,
			float maxTurnDeg) {
		Vec3 delta = desired.subtract(pos);
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float desiredYaw = horizontal > 0.01D ? (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F : yaw;
		float turn = Mth.wrapDegrees(desiredYaw - yaw);
		float newYaw = yaw + Mth.clamp(turn, -maxTurnDeg, maxTurnDeg);
		Vec3 forward = new Vec3(-Mth.sin(newYaw * Mth.DEG_TO_RAD), 0.0D, Mth.cos(newYaw * Mth.DEG_TO_RAD));
		// Heading the wrong way, the mount slows to turn rather than flying backwards.
		double alignment = Math.max(0.15D, Math.cos(Math.toRadians(Math.abs(turn))));
		double cruise = Math.min(maxSpeed, horizontal * 0.25D) * alignment;
		double climb = Mth.clamp(delta.y * 0.15D, -maxSpeed, maxSpeed);
		Vec3 wanted = forward.scale(cruise).add(0.0D, climb, 0.0D);
		Vec3 change = wanted.subtract(vel);
		if (change.length() > accel) change = change.normalize().scale(accel);
		Vec3 next = vel.add(change);
		if (next.length() > maxSpeed) next = next.normalize().scale(maxSpeed);
		float bank = Mth.clamp(turn * BANK_PER_DEGREE_OF_TURN / Math.max(1.0F, maxTurnDeg) * 6.0F, -BANK_MAX, BANK_MAX);
		double flat = Math.sqrt(next.x * next.x + next.z * next.z);
		float pitch = Mth.clamp((float) (Mth.atan2(next.y, Math.max(1.0E-4D, flat)) * Mth.RAD_TO_DEG),
				-PITCH_MAX, PITCH_MAX);
		return new Steer(next, newYaw, bank, pitch);
	}

	public static Vec3 circlePoint(Vec3 target, double radius, double angle, double altitude) {
		return new Vec3(target.x + Math.cos(angle) * radius, altitude, target.z + Math.sin(angle) * radius);
	}
}

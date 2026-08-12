package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class LivingFlailRules {
	public static final int MAX_CHARGE_TICKS = 100;
	public static final int MINIMUM_RELEASE_TICKS = 5;
	public static final int ORBIT_ACCELERATION_TICKS = 30;
	public static final double CHARGING_MOVEMENT_SCALE = 0.5D;

	private LivingFlailRules() {
	}

	public static float charge(int usedTicks) {
		return Mth.clamp(usedTicks / (float) MAX_CHARGE_TICKS, 0.0F, 1.0F);
	}

	public static float launchSpeed(float charge) {
		return interpolate(0.9F, 1.8F, charge);
	}

	public static float visualScale(float charge) {
		return interpolate(0.75F, 1.5F, charge);
	}

	public static float lifetimeTicks(float charge) {
		return interpolate(35.0F, 70.0F, charge);
	}

	public static float damage(float charge) {
		return interpolate(6.0F, 14.0F, charge);
	}

	public static float impactRadius(float charge) {
		return interpolate(2.5F, 6.0F, charge);
	}

	public static float knockback(float charge) {
		return interpolate(0.6F, 1.6F, charge);
	}

	public static int slownessTicks(float charge) {
		return Math.round(interpolate(60.0F, 160.0F, charge));
	}

	public static int slownessAmplifier(float charge) {
		return Mth.clamp(charge, 0.0F, 1.0F) < 0.5F ? 1 : 2;
	}

	public static int snowRadius(float charge) {
		return Math.round(interpolate(1.0F, 5.0F, charge));
	}

	public static boolean mayFire(int usedTicks) {
		return usedTicks >= MINIMUM_RELEASE_TICKS;
	}

	public static boolean shouldPlayMaximumCue(int usedTicks) {
		return usedTicks == MAX_CHARGE_TICKS;
	}

	public static float orbitSpeedScale(int usedTicks) {
		return Mth.clamp(usedTicks / (float) ORBIT_ACCELERATION_TICKS, 0.0F, 1.0F);
	}

	public static float orbitAngle(int usedTicks, float partialTick) {
		float time = Math.max(0.0F, usedTicks + Mth.clamp(partialTick, 0.0F, 1.0F));
		float minimumSpeed = 0.18F;
		float maximumSpeed = 0.8F;
		if (time <= ORBIT_ACCELERATION_TICKS) {
			return minimumSpeed * time + (maximumSpeed - minimumSpeed) * time * time
					/ (2.0F * ORBIT_ACCELERATION_TICKS);
		}
		float accelerationAngle = minimumSpeed * ORBIT_ACCELERATION_TICKS
				+ (maximumSpeed - minimumSpeed) * ORBIT_ACCELERATION_TICKS * 0.5F;
		return accelerationAngle + maximumSpeed * (time - ORBIT_ACCELERATION_TICKS);
	}

	public static Vec3 launchDirection(Vec3 look, Vec3 orbitTangent, float charge) {
		Vec3 aim = look.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
		Vec3 carriedSpin = orbitTangent.subtract(aim.scale(orbitTangent.dot(aim)));
		if (carriedSpin.lengthSqr() > 1.0E-8D) carriedSpin = carriedSpin.normalize().scale(0.09D);
		Vec3 arc = new Vec3(0.0D, 1.0D, 0.0D).subtract(aim.scale(aim.y));
		if (arc.lengthSqr() > 1.0E-8D) {
			arc = arc.normalize().scale(0.03D + Mth.clamp(charge, 0.0F, 1.0F) * 0.03D);
		}
		return aim.add(carriedSpin).add(arc).normalize();
	}

	public static LaunchGeometry launchGeometry(Vec3 look, double orbitAngle, boolean rightHand) {
		Vec3 horizontalForward = new Vec3(look.x, 0.0D, look.z);
		if (horizontalForward.lengthSqr() < 1.0E-8D) horizontalForward = new Vec3(0.0D, 0.0D, 1.0D);
		horizontalForward = horizontalForward.normalize();
		Vec3 handSide = horizontalForward.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
		if (!rightHand) handSide = handSide.scale(-1.0D);
		double cosine = Math.cos(orbitAngle);
		double sine = Math.sin(orbitAngle);
		Vec3 offset = handSide.scale(0.48D)
				.add(horizontalForward.scale(0.28D + cosine * 0.28D))
				.add(0.0D, -0.45D + sine * 0.28D, 0.0D);
		Vec3 tangent = horizontalForward.scale(-sine).add(0.0D, cosine, 0.0D).normalize();
		return new LaunchGeometry(offset, tangent);
	}

	public record LaunchGeometry(Vec3 offset, Vec3 tangent) {
	}

	public static float maximumChargeFlash(int usedTicks, float partialTick) {
		if (usedTicks < MAX_CHARGE_TICKS || usedTicks >= MAX_CHARGE_TICKS + 8) return 0.0F;
		float phase = (usedTicks - MAX_CHARGE_TICKS + partialTick) * 0.45F;
		float envelope = 1.0F - (usedTicks - MAX_CHARGE_TICKS + Mth.clamp(partialTick, 0.0F, 1.0F)) / 8.0F;
		return Math.max(0.0F, envelope * (0.72F + 0.28F * Mth.sin(phase)));
	}

	private static float interpolate(float minimum, float maximum, float charge) {
		return Mth.lerp(Mth.clamp(charge, 0.0F, 1.0F), minimum, maximum);
	}
}

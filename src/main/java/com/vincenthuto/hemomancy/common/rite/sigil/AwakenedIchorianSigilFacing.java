package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.util.Mth;

public final class AwakenedIchorianSigilFacing {
	private AwakenedIchorianSigilFacing() {
	}

	public static float update(float previousYaw, double dx, double dz, float smoothing) {
		if (dx * dx + dz * dz < 1.0E-12D) return previousYaw;
		float target = (float) Math.toDegrees(Math.atan2(-dx, dz));
		return previousYaw + Mth.wrapDegrees(target - previousYaw)
				* Mth.clamp(smoothing, 0.0F, 1.0F);
	}

	public static Orientation update(Orientation previous,
			double dx, double dy, double dz, float smoothing) {
		double horizontal = Math.hypot(dx, dz);
		double distanceSquared = horizontal * horizontal + dy * dy;
		float blend = Mth.clamp(smoothing, 0.0F, 1.0F);
		if (distanceSquared < 1.0E-12D) {
			return new Orientation(previous.yaw(), previous.pitch(),
					Mth.lerp(blend, previous.roll(), 0.0F));
		}
		float targetYaw = horizontal < 1.0E-8D
				? previous.yaw()
				: (float) Math.toDegrees(Math.atan2(-dx, dz));
		float targetPitch = (float) Math.toDegrees(-Math.atan2(dy, horizontal));
		targetPitch = Mth.clamp(targetPitch, -70.0F, 70.0F);
		float yawChange = Mth.wrapDegrees(targetYaw - previous.yaw());
		float targetRoll = Mth.clamp(yawChange * 0.35F, -25.0F, 25.0F);
		return new Orientation(
				previous.yaw() + yawChange * blend,
				Mth.lerp(blend, previous.pitch(), targetPitch),
				Mth.lerp(blend, previous.roll(), targetRoll));
	}

	public static float authoredForwardCorrection(double forwardX, double forwardZ) {
		if (forwardX * forwardX + forwardZ * forwardZ < 1.0E-12D) return 0.0F;
		return Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-forwardX, forwardZ)));
	}

	public static float renderYaw(float minecraftYaw) {
		return -minecraftYaw;
	}

	public record Orientation(float yaw, float pitch, float roll) {
	}
}

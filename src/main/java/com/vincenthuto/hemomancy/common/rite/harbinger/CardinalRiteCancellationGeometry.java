package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;

import net.minecraft.world.phys.Vec3;

/** Smooth geometry shared by the server daemon and client planted-staff render. */
public final class CardinalRiteCancellationGeometry {
	public static final int RECOVERY_TICKS = 20;

	private CardinalRiteCancellationGeometry() {
	}

	public static Vec3 daemonPosition(Vec3 start, Vec3 staff, int channelTicks) {
		return start.lerp(staff, smoothstep(
				CardinalRiteCancellationRules.daemonAbsorptionProgress(channelTicks)));
	}

	public static float daemonScale(float startScale, int channelTicks) {
		double progress = smoothstep(
				CardinalRiteCancellationRules.daemonAbsorptionProgress(channelTicks));
		return (float) lerp(progress, Math.max(HumanitySpriteEntity.MIN_SCALE, startScale),
				HumanitySpriteEntity.MIN_SCALE);
	}

	public static float staffScale(int channelTicks) {
		return (float) (1.0D - smoothstep(
				CardinalRiteCancellationRules.staffDissolutionProgress(channelTicks)));
	}

	public static float riteOpacity(int channelTicks) {
		return staffScale(channelTicks);
	}

	public static Vec3 recoveryPosition(Vec3 current, Vec3 target, int remainingTicks) {
		if (current == null) return target;
		if (target == null) return current;
		return current.lerp(target, recoveryStep(remainingTicks));
	}

	public static float recoveryScale(float current, float target, int remainingTicks) {
		return (float) lerp(recoveryStep(remainingTicks), current, target);
	}

	private static double recoveryStep(int remainingTicks) {
		return 1.0D / Math.max(1, remainingTicks);
	}

	private static double smoothstep(double value) {
		double clamped = Math.max(0.0D, Math.min(1.0D, value));
		return clamped * clamped * (3.0D - 2.0D * clamped);
	}

	private static double lerp(double progress, double start, double end) {
		return start + (end - start) * progress;
	}
}

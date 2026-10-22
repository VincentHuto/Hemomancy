package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/** Smooth geometry shared by the server daemon and client planted-staff render. */
public final class CardinalRiteCancellationGeometry {
	public static final int RECOVERY_TICKS = 20;

	private CardinalRiteCancellationGeometry() {
	}

	public static Vec3 daemonPosition(Vec3 start, Vec3 staff, int channelTicks) {
		return start.lerp(staff, HemoEasing.smoothstep(CardinalRiteCancellationRules.daemonAbsorptionProgress(channelTicks)));
	}

	public static float daemonScale(float startScale, int channelTicks) {
		double progress = HemoEasing.smoothstep(CardinalRiteCancellationRules.daemonAbsorptionProgress(channelTicks));
		return (float) HemoEasing.lerp(Math.max(HumanitySpriteEntity.MIN_SCALE, startScale),
				HumanitySpriteEntity.MIN_SCALE, progress);
	}

	public static float staffScale(int channelTicks) {
		return (float) (1.0D - HemoEasing.smoothstep(CardinalRiteCancellationRules.staffDissolutionProgress(channelTicks)));
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
		return (float) HemoEasing.lerp(current, target, recoveryStep(remainingTicks));
	}

	private static double recoveryStep(int remainingTicks) {
		return 1.0D / Math.max(1, remainingTicks);
	}
}

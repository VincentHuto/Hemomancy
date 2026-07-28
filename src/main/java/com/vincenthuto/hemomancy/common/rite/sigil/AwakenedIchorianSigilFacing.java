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
}

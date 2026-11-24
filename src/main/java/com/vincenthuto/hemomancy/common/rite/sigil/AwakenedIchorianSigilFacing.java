package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.util.Mth;

public final class AwakenedIchorianSigilFacing {
	private AwakenedIchorianSigilFacing() {
	}

	public static float authoredForwardCorrection(double forwardX, double forwardZ) {
		if (forwardX * forwardX + forwardZ * forwardZ < 1.0E-12D) return 0.0F;
		return Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-forwardX, forwardZ)));
	}

	public static float renderYaw(float minecraftYaw) {
		return -minecraftYaw;
	}
}

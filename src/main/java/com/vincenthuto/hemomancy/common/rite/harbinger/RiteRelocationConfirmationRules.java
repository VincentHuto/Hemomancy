package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;

/** Short, location-specific confirmation used before moving persistent rite domains. */
public final class RiteRelocationConfirmationRules {
	public static final long CONFIRMATION_TICKS = 600L;

	private RiteRelocationConfirmationRules() {
	}

	public static boolean confirmed(BlockPos requested, String dimension, long gameTime,
			BlockPos warned, String warnedDimension, long warningExpiresAt) {
		return requested != null && requested.equals(warned)
				&& dimension != null && dimension.equals(warnedDimension)
				&& gameTime <= warningExpiresAt;
	}
}

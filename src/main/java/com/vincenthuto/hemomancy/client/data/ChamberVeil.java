package com.vincenthuto.hemomancy.client.data;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;

import javax.annotation.Nullable;

/**
 * What a crossing that leaves the level hands to the transition screen: its colours and its hole, so the veil drawn
 * over the dimension change continues the walk. Armed as the crossing ends and spent by the screen; it outlives the
 * level unload on purpose. A visit by seat or rite arms nothing, and the screen draws a plain veil for it.
 */
public final class ChamberVeil {
	/**
	 * {@code holeRadius} is where the walk's hole ended; {@code eyeShape} 1 for the eye's diamond, 0 for a round hole;
	 * {@code kept} the walk's last live frame, handed over from the proxy view, or null.
	 */
	public record Armed(LidlessTheme theme, float seed, LidlessPassageService.Mode mode, long armedAt, float holeRadius,
			float eyeShape, @Nullable TextureTarget kept) {
	}

	@Nullable
	private static Armed armed;

	private ChamberVeil() {
	}

	public static void arm(LidlessTheme theme, float seed, LidlessPassageService.Mode mode, float holeRadius, float eyeShape,
			@Nullable TextureTarget kept) {
		if (armed != null && armed.kept() != null && armed.kept() != kept) armed.kept().destroyBuffers();
		armed = new Armed(theme, seed, mode, System.currentTimeMillis(), holeRadius, eyeShape, kept);
	}

	/**
	 * The armed veil if one is fresh, left armed: the client builds the transition screen more than once over one
	 * dimension change, and each must find it. A stale one's frame is let go.
	 */
	@Nullable
	public static Armed peek() {
		if (armed == null) return null;
		if (ChamberVeilRules.stillArmed(armed.armedAt(), System.currentTimeMillis())) return armed;
		release(armed);
		return null;
	}

	/** Spent: the screen that saw the level arrive lets the veil and its frame go. Another's veil is left alone. */
	public static void release(@Nullable Armed spent) {
		if (spent == null || armed != spent) return;
		if (spent.kept() != null) spent.kept().destroyBuffers();
		armed = null;
	}
}

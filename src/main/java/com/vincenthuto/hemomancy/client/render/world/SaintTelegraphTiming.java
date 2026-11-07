package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.util.Mth;

/** The telegraph pass's pure timing: when a footprint shows, how firm it is, the commit flash and a hazard's fade. */
public final class SaintTelegraphTiming {
	/** A hazard eases in over this many ticks. */
	public static final float HAZARD_EASE_IN = 6.0F;
	/** A hazard that runs out its life fades over this many ticks (the hazard's own natural expiry). */
	public static final float HAZARD_NATURAL_FADE = 10.0F;

	private SaintTelegraphTiming() {
	}

	/** From the reveal tick to the commit, and through the active span when {@code showWhileActive}. */
	public static boolean visible(float moveTime, int revealTick, BossMoveTimeline timeline, boolean showWhileActive) {
		if (moveTime < revealTick) return false;
		if (moveTime < timeline.commitTick()) return true;
		return showWhileActive && moveTime < timeline.windup() + timeline.active();
	}

	/** 0 at the reveal tick, 1 at the commit. */
	public static float reveal(float moveTime, int revealTick, int commitTick) {
		if (commitTick <= revealTick) return 1.0F;
		return Mth.clamp((moveTime - revealTick) / (commitTick - revealTick), 0.0F, 1.0F);
	}

	/** 1 at the commit, falling to 0 over {@code ticks}; 0 before the commit. */
	public static float commitFlash(float moveTime, int commitTick, float ticks) {
		float since = moveTime - commitTick;
		if (since < 0.0F || since >= ticks) return 0.0F;
		return 1.0F - since / ticks;
	}

	/**
	 * A hazard's strength: 1 while it stands, falling to 0 over its fade once expired ({@code fadeStart} is the tick
	 * count {@code expire} ran at), or over the natural fade once {@code age} passes its life.
	 */
	public static float hazardFade(float age, int life, int fade, boolean fading, int fadeStart) {
		if (fading && fade >= 0) {
			if (fade == 0) return 0.0F;
			return Mth.clamp(1.0F - (age - fadeStart) / fade, 0.0F, 1.0F);
		}
		if (age > life) return Mth.clamp(1.0F - (age - life) / HAZARD_NATURAL_FADE, 0.0F, 1.0F);
		return 1.0F;
	}
}

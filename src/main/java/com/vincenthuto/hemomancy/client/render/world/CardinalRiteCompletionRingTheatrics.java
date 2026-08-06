package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteFinaleTiming;

/** Pure client-side pose math for the boundary rings during the rite finale. */
public final class CardinalRiteCompletionRingTheatrics {
	private static final float FUNNEL_RISE_TICKS = 30.0F;
	private static final float MAX_FUNNEL_HEIGHT = 2.0F;
	private static final float CONTRACT_TICKS = 20.0F;
	private static final float COLLAPSE_START_TICK = 32.0F;
	private static final float TARGET_RADIUS = 1.5F;
	private static final float STACK_BASE_HEIGHT = 0.45F;
	private static final float STACK_SPACING = 0.55F;
	private static final float ROTATION_RADIANS_PER_TICK = 0.018F;

	private CardinalRiteCompletionRingTheatrics() {
	}

	public static RingPose pose(String phase, float phaseTicks, int ringIndex,
			int ringCount, float originalRadius) {
		return pose(phase, phaseTicks, phaseTicks, ringIndex, ringCount, originalRadius);
	}

	public static RingPose pose(String phase, float phaseTicks, float animationTicks,
			int ringIndex, int ringCount, float originalRadius) {
		if (ringCount <= 0 || originalRadius <= 0.0F) return RingPose.DEFAULT;
		float ringFraction = ringCount <= 1 ? 0.0F
				: clamp(ringIndex / (float) (ringCount - 1));
		float direction = (ringIndex & 1) == 0 ? 1.0F : -1.0F;
		float rotation = Math.max(0.0F, animationTicks) * ROTATION_RADIANS_PER_TICK * direction;
		if ("OFFERING_PROCESSION".equals(phase)) {
			float rise = smooth(clamp(phaseTicks / FUNNEL_RISE_TICKS));
			return new RingPose(1.0F, MAX_FUNNEL_HEIGHT * ringFraction * rise, rotation, 1.0F);
		}
		if (!"CULMINATION".equals(phase)) return RingPose.DEFAULT;

		float contraction = smooth(clamp(phaseTicks / CONTRACT_TICKS));
		float funnelHeight = MAX_FUNNEL_HEIGHT * ringFraction;
		float stackHeight = STACK_BASE_HEIGHT + STACK_SPACING * ringIndex;
		float targetScale = TARGET_RADIUS / originalRadius;
		float scale = lerp(contraction, 1.0F, targetScale);
		float height = lerp(contraction, funnelHeight, stackHeight);
		float collapse = smooth(clamp((phaseTicks - COLLAPSE_START_TICK)
				/ (CardinalRiteFinaleTiming.GROWTH_TICKS - COLLAPSE_START_TICK)));
		return new RingPose(scale * (1.0F - collapse), height, rotation, 1.0F - collapse);
	}

	public static float flashAlpha(float culminationTicks) {
		float start = CardinalRiteFinaleTiming.GROWTH_TICKS - 6.0F;
		float progress = (culminationTicks - start)
				/ (CardinalRiteFinaleTiming.GROWTH_TICKS - start);
		if (progress <= 0.0F || progress >= 1.0F) return 0.0F;
		return (float) Math.sin(progress * Math.PI);
	}

	private static float lerp(float amount, float start, float end) {
		return start + (end - start) * amount;
	}

	private static float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public record RingPose(float radialScale, float verticalOffset,
			float rotationRadians, float opacity) {
		public static final RingPose DEFAULT = new RingPose(1.0F, 0.0F, 0.0F, 1.0F);
	}
}

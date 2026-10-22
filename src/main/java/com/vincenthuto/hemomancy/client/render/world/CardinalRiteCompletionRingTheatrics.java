package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteFinaleTiming;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Pure client-side pose math for the boundary rings during the rite finale.
 * Sockets ride inside each ring's pose, so the spin never pulls an arc away
 * from the anchors it joins.
 */
public final class CardinalRiteCompletionRingTheatrics {
	private static final float FUNNEL_RISE_TICKS = 30.0F;
	private static final float MAX_FUNNEL_HEIGHT = 2.0F;
	private static final float CONTRACT_TICKS = 20.0F;
	private static final float COLLAPSE_START_TICK = 32.0F;
	private static final float TARGET_RADIUS = 1.5F;
	private static final float STACK_BASE_HEIGHT = 0.45F;
	private static final float STACK_SPACING = 0.55F;
	private static final float ROTATION_RADIANS_PER_TICK = 0.018F;
	/** Ticks for the spin to wind up from rest to full speed. */
	private static final float SPIN_RAMP_TICKS = 24.0F;
	/** Ticks over which sockets fold into their ring as culmination contracts it. */
	private static final float SOCKET_FOLD_TICKS = 12.0F;

	private CardinalRiteCompletionRingTheatrics() {
	}

	public static RingPose pose(CardinalRitePhase phase, float phaseTicks, int ringIndex,
			int ringCount, float originalRadius) {
		return pose(phase, phaseTicks, phaseTicks, ringIndex, ringCount, originalRadius);
	}

	/**
	 * @param spinTicks ticks the rings have been spinning, counted from the start
	 *                  of the procession (see {@code RiteEntry.ringSpinTicks})
	 */
	public static RingPose pose(CardinalRitePhase phase, float phaseTicks, float spinTicks,
			int ringIndex, int ringCount, float originalRadius) {
		if (ringCount <= 0 || originalRadius <= 0.0F) return RingPose.DEFAULT;
		float ringFraction = ringCount <= 1 ? 0.0F
				: HemoEasing.clamp01(ringIndex / (float) (ringCount - 1));
		float direction = (ringIndex & 1) == 0 ? 1.0F : -1.0F;
		float rotation = spinAngle(spinTicks) * direction;
		if (phase == CardinalRitePhase.OFFERING_PROCESSION) {
			float rise = HemoEasing.smoothstep(phaseTicks / FUNNEL_RISE_TICKS);
			return new RingPose(1.0F, MAX_FUNNEL_HEIGHT * ringFraction * rise, rotation, 1.0F);
		}
		if (phase != CardinalRitePhase.CULMINATION) return RingPose.DEFAULT;

		float contraction = HemoEasing.smoothstep(phaseTicks / CONTRACT_TICKS);
		float funnelHeight = MAX_FUNNEL_HEIGHT * ringFraction;
		float stackHeight = STACK_BASE_HEIGHT + STACK_SPACING * ringIndex;
		float targetScale = TARGET_RADIUS / originalRadius;
		float scale = HemoEasing.lerp(1.0F, targetScale, contraction);
		float height = HemoEasing.lerp(funnelHeight, stackHeight, contraction);
		float collapse = HemoEasing.smoothstep((phaseTicks - COLLAPSE_START_TICK)
				/ (CardinalRiteFinaleTiming.GROWTH_TICKS - COLLAPSE_START_TICK));
		return new RingPose(scale * (1.0F - collapse), height, rotation, 1.0F - collapse);
	}

	/**
	 * The spin angle after {@code spinTicks}: the integral of a speed that ramps
	 * linearly from rest, so the rings ease into their turn instead of snapping.
	 */
	public static float spinAngle(float spinTicks) {
		float ticks = Math.max(0.0F, spinTicks);
		if (ticks < SPIN_RAMP_TICKS) {
			return ROTATION_RADIANS_PER_TICK * ticks * ticks / (2.0F * SPIN_RAMP_TICKS);
		}
		return ROTATION_RADIANS_PER_TICK * (ticks - SPIN_RAMP_TICKS * 0.5F);
	}

	/** How much of each anchor socket still shows while culmination folds the rings inward. */
	public static float socketVisibility(CardinalRitePhase phase, float phaseTicks) {
		if (phase != CardinalRitePhase.CULMINATION) return 1.0F;
		return 1.0F - HemoEasing.smoothstep(phaseTicks / SOCKET_FOLD_TICKS);
	}

	public static float flashAlpha(float culminationTicks) {
		float start = CardinalRiteFinaleTiming.GROWTH_TICKS - 6.0F;
		float progress = (culminationTicks - start)
				/ (CardinalRiteFinaleTiming.GROWTH_TICKS - start);
		if (progress <= 0.0F || progress >= 1.0F) return 0.0F;
		return (float) Math.sin(progress * Math.PI);
	}

	public record RingPose(float radialScale, float verticalOffset,
			float rotationRadians, float opacity) {
		public static final RingPose DEFAULT = new RingPose(1.0F, 0.0F, 0.0F, 1.0F);
	}
}

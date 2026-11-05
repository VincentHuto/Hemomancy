package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.data.ChamberVeilRules;

/**
 * The hole at the end of a gateway walk, in radians of the view. From outside, an open eye is a diamond that fills
 * the view as a body steps into it; the walk's hole starts at that size and opens past the view's edges as the walk
 * goes, so stepping in is passing through the window and never a small round hole starting over. Pure.
 */
public final class LidlessHoleRules {
	/** Half the eye panel's width ({@code LidlessPortalPanel}). */
	public static final double EYE_HALF_WIDTH = 1.95D;
	/** About how far a body stands from the panel as the crossing takes it. */
	public static final double THRESHOLD_GAP = 0.6D;
	/** The eye's apparent half width from the threshold. */
	public static final float THRESHOLD_RADIUS = (float) Math.atan2(EYE_HALF_WIDTH, THRESHOLD_GAP);

	private LidlessHoleRules() {
	}

	/** The hole {@code along} of the way through a gateway walk (0 to 1): eased out, full by the end. */
	public static float gatewayRadius(float along) {
		float t = Math.clamp(along, 0.0F, 1.0F);
		float eased = 1.0F - (1.0F - t) * (1.0F - t);
		return THRESHOLD_RADIUS + (ChamberVeilRules.FULL_RADIUS - THRESHOLD_RADIUS) * eased;
	}
}

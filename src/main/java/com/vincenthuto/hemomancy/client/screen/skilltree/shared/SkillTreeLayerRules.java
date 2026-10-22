package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ConcentricTreeGeometry;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;

public final class SkillTreeLayerRules {
	public static final int SURFACE_MAX_DEGREE = 4;
	public static final int DEEP_MIN_DEGREE = 5;
	public static final int DIVE_UNLOCK_DEGREE = 5;
	public static final float DIVE_ZOOM_START = 1.85f;
	public static final float DIVE_ZOOM_END = 2.35f;
	private static final float CENTER_FOCUS_INNER_RADIUS = 70.0f;
	private static final float CENTER_FOCUS_OUTER_RADIUS = 240.0f;

	private SkillTreeLayerRules() {
	}

	public static SkillTreeLayer layerForDegree(int requiredDegree) {
		return requiredDegree <= SURFACE_MAX_DEGREE ? SkillTreeLayer.SURFACE : SkillTreeLayer.DEEP;
	}

	public static boolean isDeepUnlocked(int playerDegree) {
		return playerDegree >= DIVE_UNLOCK_DEGREE;
	}

	/** Gives both layers the same compact concentric footprint used by the Skills view. */
	public static int ringRadiusForDegree(int degree, SkillTreeLayer layer) {
		int radialDegree = layer == SkillTreeLayer.DEEP ? degree - SURFACE_MAX_DEGREE : degree;
		return ConcentricTreeGeometry.radiusForDegree(radialDegree);
	}

	public static int outerRingRadius(SkillTreeLayer layer) {
		int outerDegree = layer == SkillTreeLayer.DEEP ? 8 : SURFACE_MAX_DEGREE;
		return ringRadiusForDegree(outerDegree, layer);
	}

	public static float diveProgress(int playerDegree, float zoom, float centerFocus) {
		if (!isDeepUnlocked(playerDegree)) return 0.0f;
		return HemoEasing.smoothstep(DIVE_ZOOM_START, DIVE_ZOOM_END, zoom) * Mth.clamp(centerFocus, 0.0f, 1.0f);
	}

	public static float lockedPortalProgress(int playerDegree, float zoom, float centerFocus) {
		if (isDeepUnlocked(playerDegree)) return 0.0f;
		return HemoEasing.smoothstep(DIVE_ZOOM_START, DIVE_ZOOM_END, zoom) * Mth.clamp(centerFocus, 0.0f, 1.0f);
	}

	public static float centerFocus(double centerScreenX, double centerScreenY, int viewportWidth, int viewportHeight) {
		double dx = centerScreenX - viewportWidth / 2.0;
		double dy = centerScreenY - viewportHeight / 2.0;
		double distance = Math.sqrt(dx * dx + dy * dy);
		float fade = 1.0f - HemoEasing.smoothstep(CENTER_FOCUS_INNER_RADIUS, CENTER_FOCUS_OUTER_RADIUS, (float) distance);
		return Mth.clamp(fade, 0.0f, 1.0f);
	}
}

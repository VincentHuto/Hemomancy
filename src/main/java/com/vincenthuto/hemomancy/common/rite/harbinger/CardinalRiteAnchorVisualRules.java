package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Shared state rules for the visual language of interactive rite anchors.
 */
public final class CardinalRiteAnchorVisualRules {
	public static final int BOUNDARY_COLOR = 0xFF3746;
	public static final double RITE_PLANE_OFFSET = 0.1D;

	private CardinalRiteAnchorVisualRules() {
	}

	public enum Visual {
		INTERACTION_MARKER,
		SANGUINE_BLOB
	}

	public static Visual boundaryVisual(int bloodMl, int requiredBloodMl) {
		return bloodMl > 0 ? Visual.SANGUINE_BLOB : Visual.INTERACTION_MARKER;
	}

	public static Visual sigilVisual(int nodeIndex, int completedNodes) {
		return nodeIndex < completedNodes ? Visual.SANGUINE_BLOB : Visual.INTERACTION_MARKER;
	}

	public static int sigilColor(int authoredColor) {
		return authoredColor & 0xFFFFFF;
	}

	public static float formingBoundaryRadius(int bloodMl, int requiredBloodMl) {
		return organicGrowthRadius(bloodMl, requiredBloodMl, 0.03F, 0.19F);
	}

	public static float formingSigilRadius(int bloodMl, int requiredBloodMl) {
		return organicGrowthRadius(bloodMl, requiredBloodMl, 0.025F, 0.16F);
	}

	private static float organicGrowthRadius(int bloodMl, int requiredBloodMl,
			float initialRadius, float fullRadius) {
		if (bloodMl <= 0 || requiredBloodMl <= 0) return 0.0F;
		float progress = Math.max(0.0F, Math.min(1.0F, bloodMl / (float) requiredBloodMl));
		float organicProgress = progress * progress * (3.0F - 2.0F * progress);
		return initialRadius + (fullRadius - initialRadius) * organicProgress;
	}

	public static double ritePlaneY(int centerY) {
		return centerY + RITE_PLANE_OFFSET;
	}
}

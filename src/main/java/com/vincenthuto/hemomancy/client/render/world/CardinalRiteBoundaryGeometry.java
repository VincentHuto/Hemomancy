package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAnchorVisualRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Shared geometry rules for the visible Cardinal Rite rings and the enclosing
 * exterior field. Keeping both calculations together prevents the Fane shell
 * from drifting inside or outside the ring that visually defines the rite.
 */
public final class CardinalRiteBoundaryGeometry {
	private CardinalRiteBoundaryGeometry() {
	}

	public static boolean shouldRenderExterior(int degree) {
		return degree >= 3;
	}

	public static float boundaryPlaneY(int centerY) {
		return (float) CardinalRiteAnchorVisualRules.ritePlaneY(centerY);
	}

	public static float interactiveRingRadius(int ringIndex) {
		return 3.0F + Math.max(0, ringIndex);
	}

	public static float exteriorRadius(int riteSize, int completedRings, boolean legacy) {
		int ringCount = legacy ? Math.max(1, (riteSize - 1) / 2) : Math.max(0, completedRings);
		if (ringCount == 0) return 0.0F;
		return legacy
				? (float) (riteSize / 2.0D + 1.0D + (ringCount - 1) * 2.0D)
				: interactiveRingRadius(ringCount - 1);
	}

	public static float footprintRadius(List<BlockPos> boundaryPoints, List<BlockPos> sigilPoints) {
		return CardinalRiteFootprintRules.radius(boundaryPoints, sigilPoints);
	}
}

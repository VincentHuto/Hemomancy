package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.event.worldevent.FaneFootprint;

import net.minecraft.core.BlockPos;

public final class CardinalRiteFoundationRules {
	private CardinalRiteFoundationRules() {
	}

	public static List<BlockPos> squareBelow(BlockPos focusPos, float riteRadius,
			boolean includesFaneBoundary) {
		double requiredRadius = Float.isFinite(riteRadius) ? Math.max(0.0F, riteRadius) : 0.0D;
		if (includesFaneBoundary) {
			requiredRadius = Math.max(requiredRadius, FaneFootprint.HEART_RADIUS);
		}
		int halfExtent = (int) Math.ceil(requiredRadius);
		List<BlockPos> positions = new ArrayList<>((halfExtent * 2 + 1) * (halfExtent * 2 + 1));
		for (int x = -halfExtent; x <= halfExtent; x++) {
			for (int z = -halfExtent; z <= halfExtent; z++) {
				positions.add(focusPos.offset(x, -1, z));
			}
		}
		return List.copyOf(positions);
	}
}

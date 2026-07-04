package com.vincenthuto.hemomancy.common.world.fold;

import javax.annotation.Nullable;

public record FoldTravelerState(@Nullable FoldedHallwaySpec activeFold, double realDepthProgress,
		double virtualDepthProgress, int cooldownTicks, @Nullable FoldViewerSide entrySide) {
	public static FoldTravelerState active(FoldedHallwaySpec spec, FoldViewerSide entrySide,
			double realDepthProgress) {
		double virtualDepthProgress = realDepthProgress / FoldTransform.of(spec).compressionRatio();
		return new FoldTravelerState(spec, realDepthProgress, virtualDepthProgress, 0, entrySide);
	}

	public static FoldTravelerState inactive(int cooldownTicks) {
		return new FoldTravelerState(null, 0.0D, 0.0D, Math.max(0, cooldownTicks), null);
	}

	public boolean isActive() {
		return activeFold != null;
	}

	public FoldTravelerState withCooldownTicked() {
		if (cooldownTicks <= 1) {
			return inactive(0);
		}
		return inactive(cooldownTicks - 1);
	}
}

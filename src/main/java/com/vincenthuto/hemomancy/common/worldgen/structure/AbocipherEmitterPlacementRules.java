package com.vincenthuto.hemomancy.common.worldgen.structure;

final class AbocipherEmitterPlacementRules {
	static final int HARBINGER_OUTPOST_FLOOR_COUNT = 3;
	private static final int HARBINGER_OUTPOST_FLOOR_HEADROOM = 3;

	private AbocipherEmitterPlacementRules() {
	}

	static int harbingerOutpostEmitterY(int minY, int maxY, int floorIndex) {
		if (floorIndex < 0 || floorIndex >= HARBINGER_OUTPOST_FLOOR_COUNT) {
			throw new IllegalArgumentException("floorIndex must be 0-2");
		}

		int height = Math.max(1, maxY - minY + 1);
		int floorSpan = Math.max(1, height / HARBINGER_OUTPOST_FLOOR_COUNT);
		int targetY = minY + HARBINGER_OUTPOST_FLOOR_HEADROOM + floorSpan * floorIndex;
		int lowerBound = Math.min(maxY, minY + 1);
		int upperBound = Math.max(lowerBound, maxY - 1);
		return Math.max(lowerBound, Math.min(upperBound, targetY));
	}
}

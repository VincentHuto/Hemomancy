package com.vincenthuto.hemomancy.common.world.fold;

public record FoldedHallwaySpec(
		String dimensionId,
		int anchorX,
		int anchorY,
		int anchorZ,
		FoldFacing facing,
		double apertureHalfWidth,
		double apertureHeight,
		double realDepth,
		double virtualLength) {
	public static final double PROTOTYPE_APERTURE_HALF_WIDTH = 1.0D;
	public static final double PROTOTYPE_APERTURE_HEIGHT = 3.0D;
	public static final double PROTOTYPE_REAL_DEPTH = 2.0D;
	public static final double PROTOTYPE_VIRTUAL_LENGTH = 32.0D;

	public FoldedHallwaySpec {
		if (dimensionId == null || dimensionId.isBlank()) {
			throw new IllegalArgumentException("dimensionId must be set");
		}
		if (facing == null) {
			throw new IllegalArgumentException("facing must be set");
		}
		if (apertureHalfWidth <= 0.0D) {
			throw new IllegalArgumentException("apertureHalfWidth must be positive");
		}
		if (apertureHeight <= 0.0D) {
			throw new IllegalArgumentException("apertureHeight must be positive");
		}
		if (realDepth <= 0.0D) {
			throw new IllegalArgumentException("realDepth must be positive");
		}
		if (virtualLength < realDepth) {
			throw new IllegalArgumentException("virtualLength must be at least realDepth");
		}
	}

	public static FoldedHallwaySpec prototype(String dimensionId, int anchorX, int anchorY, int anchorZ,
			FoldFacing facing) {
		return new FoldedHallwaySpec(dimensionId, anchorX, anchorY, anchorZ, facing,
				PROTOTYPE_APERTURE_HALF_WIDTH, PROTOTYPE_APERTURE_HEIGHT,
				PROTOTYPE_REAL_DEPTH, PROTOTYPE_VIRTUAL_LENGTH);
	}

	public FoldVector origin() {
		return new FoldVector(anchorX + 0.5D, anchorY, anchorZ + 0.5D);
	}
}

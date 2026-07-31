package com.vincenthuto.hemomancy.common.rite;

/** Plain restart-time tuning for each Cardinal Rite anchor ring. */
public final class CardinalRiteRingTuning {
	public static final double[] ROTATION_DEGREES = {
			45.0D,90.0D,  135.0D, 180.0D, 225.0D, 270.0D, 315.0D,360.0D
	};
	public static final double[] RADIUS_BLOCKS = {
			0.75D, 3, 4, 3D, 3.75D, 4.5D, 5.25D, 6D
	};

	private CardinalRiteRingTuning() {
	}

	public static CardinalRiteCeremonyDefinition.Anchor anchor(
			int ring, int order, int y) {
		if (ROTATION_DEGREES.length != RADIUS_BLOCKS.length) {
			throw new IllegalStateException("Cardinal Rite rotation and radius arrays must have equal lengths");
		}
		if (ring < 0 || ring >= ROTATION_DEGREES.length) {
			throw new IllegalArgumentException("No Cardinal Rite tuning for ring " + ring);
		}
		double angle = Math.toRadians(
				ROTATION_DEGREES[ring] + Math.floorMod(order, 4) * 90.0D);
		double radius = RADIUS_BLOCKS[ring];
		if (!Double.isFinite(angle) || !Double.isFinite(radius) || radius < 0.0D) {
			throw new IllegalStateException("Invalid Cardinal Rite tuning for ring " + ring);
		}
		int x = (int) Math.round(Math.sin(angle) * radius);
		int z = (int) Math.round(-Math.cos(angle) * radius);
		return new CardinalRiteCeremonyDefinition.Anchor(x, y, z, ring, order);
	}
}

package com.vincenthuto.hemomancy.common.block.shared;

public final class DiscoveryInscriptionVisuals {
	public static final double LOW_FACE = 2.0D / 16.0D;
	public static final double HIGH_FACE = 14.0D / 16.0D;
	public static final double PARTICLE_OFFSET = 0.0125D;

	private DiscoveryInscriptionVisuals() {
	}

	public enum Face {
		NORTH,
		SOUTH,
		EAST,
		WEST
	}

	public record PixelCenter(double x, double y, double z) {
		public PixelCenter offset(double xOffset, double yOffset, double zOffset) {
			return new PixelCenter(x + xOffset, y + yOffset, z + zOffset);
		}
	}

	public static PixelCenter pixelCenter(Face face, int px, int py) {
		double u = (px + 0.5D) / 16.0D;
		double mirroredU = (15 - px + 0.5D) / 16.0D;
		double y = (15 - py + 0.5D) / 16.0D;

		return switch (face) {
			case SOUTH -> new PixelCenter(u, y, LOW_FACE);
			case NORTH -> new PixelCenter(mirroredU, y, HIGH_FACE);
			case EAST -> new PixelCenter(LOW_FACE, y, u);
			case WEST -> new PixelCenter(HIGH_FACE, y, mirroredU);
		};
	}

	public static PixelCenter particleCenter(Face face, int px, int py) {
		PixelCenter center = pixelCenter(face, px, py);
		return switch (outwardFace(face)) {
			case SOUTH -> center.offset(0.0D, 0.0D, PARTICLE_OFFSET);
			case NORTH -> center.offset(0.0D, 0.0D, -PARTICLE_OFFSET);
			case EAST -> center.offset(PARTICLE_OFFSET, 0.0D, 0.0D);
			case WEST -> center.offset(-PARTICLE_OFFSET, 0.0D, 0.0D);
		};
	}

	public static Face outwardFace(Face face) {
		return face;
	}
}

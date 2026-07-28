package com.vincenthuto.hemomancy.client.particle;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a world-vertical quad that rotates only around Y to face the camera.
 */
public final class RitePillarParticleGeometry {
	private RitePillarParticleGeometry() {
	}

	public static List<Vertex> quad(float halfWidth, float halfHeight,
			float horizontalLookX, float horizontalLookZ) {
		float length = (float) Math.hypot(horizontalLookX, horizontalLookZ);
		float lookX = length < 0.0001F ? 0.0F : horizontalLookX / length;
		float lookZ = length < 0.0001F ? 1.0F : horizontalLookZ / length;
		float rightX = lookZ * halfWidth;
		float rightZ = -lookX * halfWidth;
		return List.of(
				new Vertex(-rightX, -halfHeight, -rightZ, 0.0F, 1.0F),
				new Vertex(-rightX, halfHeight, -rightZ, 0.0F, 0.0F),
				new Vertex(rightX, halfHeight, rightZ, 1.0F, 0.0F),
				new Vertex(rightX, -halfHeight, rightZ, 1.0F, 1.0F));
	}

	/**
	 * Builds connected camera-facing quads whose centerline travels sideways
	 * as {@code phase} advances. UVs remain continuous across the full height,
	 * so the pillar texture bends rather than repeating at every joint.
	 */
	public static List<Vertex> ribbon(float halfWidth, float halfHeight,
			float horizontalLookX, float horizontalLookZ, float phase,
			int segmentCount, float sway) {
		int segments = Math.max(1, segmentCount);
		float length = (float) Math.hypot(horizontalLookX, horizontalLookZ);
		float lookX = length < 0.0001F ? 0.0F : horizontalLookX / length;
		float lookZ = length < 0.0001F ? 1.0F : horizontalLookZ / length;
		float rightX = lookZ;
		float rightZ = -lookX;
		List<Vertex> vertices = new ArrayList<>(segments * 4);
		for (int segment = 0; segment < segments; segment++) {
			float bottom = segment / (float) segments;
			float top = (segment + 1) / (float) segments;
			float bottomOffset = (float) Math.sin(phase + bottom * Math.PI * 2.0D) * sway;
			float topOffset = (float) Math.sin(phase + top * Math.PI * 2.0D) * sway;
			float bottomWidth = halfWidth * (1.0F
					+ 0.12F * (float) Math.sin(phase * 1.7F - bottom * Math.PI * 4.0D));
			float topWidth = halfWidth * (1.0F
					+ 0.12F * (float) Math.sin(phase * 1.7F - top * Math.PI * 4.0D));
			float bottomY = -halfHeight + bottom * halfHeight * 2.0F;
			float topY = -halfHeight + top * halfHeight * 2.0F;
			float bottomX = rightX * bottomOffset;
			float bottomZ = rightZ * bottomOffset;
			float topX = rightX * topOffset;
			float topZ = rightZ * topOffset;

			vertices.add(new Vertex(bottomX - rightX * bottomWidth, bottomY,
					bottomZ - rightZ * bottomWidth, 0.0F, 1.0F - bottom));
			vertices.add(new Vertex(topX - rightX * topWidth, topY,
					topZ - rightZ * topWidth, 0.0F, 1.0F - top));
			vertices.add(new Vertex(topX + rightX * topWidth, topY,
					topZ + rightZ * topWidth, 1.0F, 1.0F - top));
			vertices.add(new Vertex(bottomX + rightX * bottomWidth, bottomY,
					bottomZ + rightZ * bottomWidth, 1.0F, 1.0F - bottom));
		}
		return List.copyOf(vertices);
	}

	public record Vertex(float x, float y, float z, float u, float v) {
	}
}

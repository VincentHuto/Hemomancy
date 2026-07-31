package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Applies the rite's dissolve opacity while preserving a baked model's colors. */
final class CardinalRiteFadingVertexConsumer implements VertexConsumer {
	private final VertexConsumer delegate;
	private final float opacity;

	CardinalRiteFadingVertexConsumer(VertexConsumer delegate, float opacity) {
		this.delegate = delegate;
		this.opacity = Math.max(0.0F, Math.min(1.0F, opacity));
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		delegate.setColor(red, green, blue, Math.round(alpha * opacity));
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		delegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		delegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		delegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		delegate.setNormal(normalX, normalY, normalZ);
		return this;
	}
}

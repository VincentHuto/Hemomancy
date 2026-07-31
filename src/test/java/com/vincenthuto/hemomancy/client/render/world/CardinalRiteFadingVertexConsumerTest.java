package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteFadingVertexConsumerTest {
	@Test
	void staffVertexAlphaIsMultipliedByDissolutionOpacity() {
		RecordingVertexConsumer delegate = new RecordingVertexConsumer();
		VertexConsumer fading = new CardinalRiteFadingVertexConsumer(delegate, 0.4F);

		fading.setColor(12, 34, 56, 200);

		assertEquals(12, delegate.red);
		assertEquals(34, delegate.green);
		assertEquals(56, delegate.blue);
		assertEquals(80, delegate.alpha);
	}

	private static final class RecordingVertexConsumer implements VertexConsumer {
		private int red;
		private int green;
		private int blue;
		private int alpha;

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
			return this;
		}
	}
}

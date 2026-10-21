package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/** The undulating blood bead shared by Condenser formations and the Ichorian sigils. */
public final class SanguineSphereRenderer {
	private static final int LAT_BANDS = 10;
	private static final int LON_BANDS = 16;

	private SanguineSphereRenderer() {
	}

	public static void renderSphere(VertexConsumer consumer, Matrix4f matrix, float baseRadius, float time, long seed,
			float red, float green, float blue, float alpha) {
		for (int lat = 0; lat < LAT_BANDS; lat++) {
			double theta0 = Math.PI * lat / LAT_BANDS;
			double theta1 = Math.PI * (lat + 1) / LAT_BANDS;

			for (int lon = 0; lon < LON_BANDS; lon++) {
				double phi0 = 2.0D * Math.PI * lon / LON_BANDS;
				double phi1 = 2.0D * Math.PI * (lon + 1) / LON_BANDS;

				float r00 = baseRadius + scaledUndulation(
						baseRadius, undulation(theta0, phi0, time, seed));
				float r10 = baseRadius + scaledUndulation(
						baseRadius, undulation(theta1, phi0, time, seed));
				float r11 = baseRadius + scaledUndulation(
						baseRadius, undulation(theta1, phi1, time, seed));
				float r01 = baseRadius + scaledUndulation(
						baseRadius, undulation(theta0, phi1, time, seed));

				addVertex(consumer, matrix, theta0, phi0, r00, red, green, blue, alpha);
				addVertex(consumer, matrix, theta1, phi0, r10, red, green, blue, alpha);
				addVertex(consumer, matrix, theta1, phi1, r11, red, green, blue, alpha);
				addVertex(consumer, matrix, theta0, phi1, r01, red, green, blue, alpha);
			}
		}
	}

	private static void addVertex(VertexConsumer consumer, Matrix4f matrix, double theta, double phi, float radius,
			float red, float green, float blue, float alpha) {
		float x = (float) (Math.sin(theta) * Math.cos(phi)) * radius;
		float y = (float) Math.cos(theta) * radius;
		float z = (float) (Math.sin(theta) * Math.sin(phi)) * radius;
		consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
	}

	private static float undulation(double theta, double phi, float time, long seed) {
		double offset = (seed & 255) * 0.013D;
		return (float) (0.018D * Math.sin(theta * 4.0D + time * 0.17D + offset)
				+ 0.012D * Math.cos(phi * 5.0D + time * 0.11D + offset));
	}

	static float scaledUndulation(float baseRadius, float rawUndulation) {
		float emergence = Math.max(0.0F, Math.min(1.0F, baseRadius / 0.04F));
		return rawUndulation * emergence;
	}
}

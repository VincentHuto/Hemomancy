package com.vincenthuto.hemomancy.client.render.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/** Reusable, client-only sphere mesh authored for the Sanguine Conduit core. */
public final class SanguineConduitCoreGeometry {
	private static final int LATITUDE_BANDS = 16;
	private static final int LONGITUDE_BANDS = 24;

	private SanguineConduitCoreGeometry() {
	}

	public static void render(PoseStack poseStack, MultiBufferSource buffer, double time,
			float phaseRadians, Style style) {
		float pulseSin = (float) Math.sin(time * style.pulseSpeed() + phaseRadians);
		float pulse = style.pulseBase() + style.pulseAmplitude() * pulseSin;
		Matrix4f matrix = poseStack.last().pose();
		VertexConsumer consumer = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		renderSphere(consumer, matrix, style.radius() * pulse,
				time, phaseRadians, style.agitation(), style.core().brighten(style.flashStrength())
						.alphaScaled(0.85F + 0.15F * pulseSin));
		renderSphere(consumer, matrix, (style.radius() + style.shellExtra()) * pulse,
				time, phaseRadians, style.agitation(), style.shell().alphaScaled(0.80F + 0.20F * pulseSin));
	}

	private static void renderSphere(VertexConsumer consumer, Matrix4f matrix, float baseRadius,
			double time, float phaseRadians, float agitation, Color color) {
		for (int latitude = 0; latitude < LATITUDE_BANDS; latitude++) {
			double theta0 = Math.PI * latitude / LATITUDE_BANDS;
			double theta1 = Math.PI * (latitude + 1) / LATITUDE_BANDS;
			double sinTheta0 = Math.sin(theta0);
			double cosTheta0 = Math.cos(theta0);
			double sinTheta1 = Math.sin(theta1);
			double cosTheta1 = Math.cos(theta1);
			for (int longitude = 0; longitude < LONGITUDE_BANDS; longitude++) {
				double phi0 = Math.PI * 2.0D * longitude / LONGITUDE_BANDS;
				double phi1 = Math.PI * 2.0D * (longitude + 1) / LONGITUDE_BANDS;
				double cosPhi0 = Math.cos(phi0);
				double sinPhi0 = Math.sin(phi0);
				double cosPhi1 = Math.cos(phi1);
				double sinPhi1 = Math.sin(phi1);
				float radius00 = baseRadius + undulation(theta0, phi0, time, phaseRadians) * agitation;
				float radius10 = baseRadius + undulation(theta1, phi0, time, phaseRadians) * agitation;
				float radius11 = baseRadius + undulation(theta1, phi1, time, phaseRadians) * agitation;
				float radius01 = baseRadius + undulation(theta0, phi1, time, phaseRadians) * agitation;
				emit(consumer, matrix, sinTheta0, cosTheta0, cosPhi0, sinPhi0, radius00, color);
				emit(consumer, matrix, sinTheta1, cosTheta1, cosPhi0, sinPhi0, radius10, color);
				emit(consumer, matrix, sinTheta1, cosTheta1, cosPhi1, sinPhi1, radius11, color);
				emit(consumer, matrix, sinTheta0, cosTheta0, cosPhi1, sinPhi1, radius01, color);
			}
		}
	}

	private static void emit(VertexConsumer consumer, Matrix4f matrix, double sinTheta, double cosTheta,
			double cosPhi, double sinPhi, float radius, Color color) {
		consumer.addVertex(matrix, (float) (sinTheta * cosPhi) * radius, (float) cosTheta * radius,
				(float) (sinTheta * sinPhi) * radius).setColor(color.red(), color.green(), color.blue(), color.alpha());
	}

	private static float undulation(double theta, double phi, double time, float phaseRadians) {
		double primary = 0.08D * Math.sin(3.0D * theta + 0.06D * time + phaseRadians);
		double secondary = 0.018D * Math.cos(7.0D * phi + 0.3D * time);
		double heartbeat = 0.020D * Math.sin(0.3D * time);
		return (float) (primary + secondary + heartbeat);
	}

	public record Style(float radius, float shellExtra, float pulseBase, float pulseAmplitude,
			float pulseSpeed, float agitation, float flashStrength, Color core, Color shell) {
	}

	public record Color(float red, float green, float blue, float alpha) {
		private Color brighten(float amount) {
			float clamped = Math.max(0.0F, Math.min(1.0F, amount));
			return new Color(red + (1.0F - red) * clamped,
					green + (0.72F - green) * clamped,
					blue + (0.72F - blue) * clamped, alpha);
		}

		private Color alphaScaled(float multiplier) {
			return new Color(red, green, blue, alpha * multiplier);
		}
	}
}

package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.item.MorphicNectarMutationRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

final class MorphicNectarTendrilRenderer {
	private static final float[][] TENDRILS = new float[][] {
			{ -1.3F, 11.5F, -0.45F, 17.0F, 2.4F, 0.62F, 0.0F },
			{ 3.2F, -1.4F, 0.86F, 16.4F, 2.1F, 0.52F, 1.3F },
			{ 17.2F, 3.2F, 2.43F, 17.3F, 2.3F, 0.58F, 2.7F },
			{ 15.2F, 17.2F, -2.36F, 16.0F, 2.0F, 0.50F, 4.1F },
			{ 8.1F, 17.8F, -1.54F, 15.0F, 2.2F, 0.70F, 5.2F }
	};

	private MorphicNectarTendrilRenderer() {
	}

	static void renderPrimalOverlay(GuiGraphics graphics, int x, int y, long phase) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		Matrix4f matrix = graphics.pose().last().pose();
		VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
		float bodyWidth = MorphicNectarMutationRules.primalProceduralBodyWidthCentipixels() / 100.0F;
		for (int index = 0; index < MorphicNectarMutationRules.primalTendrilCount(); index++) {
			drawTendrilLayer(buffer, matrix, x, y, index, phase, bodyWidth * 1.75F, 44, 4, 2, 52, 0.0F);
			drawTendrilLayer(buffer, matrix, x, y, index, phase, bodyWidth, 155, 35, 16, 196, 0.42F);
			drawTendrilLayer(buffer, matrix, x, y, index, phase, bodyWidth * 0.32F, 228, 178, 45, 130, 0.86F);
			if ((index & 1) == 0) {
				drawTendrilLayer(buffer, matrix, x, y, index, phase, bodyWidth * 0.24F, 76, 120, 24, 82, 1.55F);
			}
		}
		graphics.flush();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void drawTendrilLayer(VertexConsumer buffer, Matrix4f matrix, int x, int y, int index, long phase,
			float width, int red, int green, int blue, int alpha, float layerPhase) {
		float[] config = TENDRILS[index % TENDRILS.length];
		int samples = MorphicNectarMutationRules.primalProceduralCurveSamples();
		float time = phase * 0.135F;
		float trim = MorphicNectarMutationRules.primalProceduralEndpointTrimPercent() / 100.0F;
		for (int sample = 0; sample < samples - 1; sample++) {
			float t0 = trimmedT(sample / (float) (samples - 1), trim);
			float t1 = trimmedT((sample + 1) / (float) (samples - 1), trim);
			float x0 = curveX(config, t0, time, layerPhase);
			float y0 = curveY(config, t0, time, layerPhase);
			float x1 = curveX(config, t1, time, layerPhase);
			float y1 = curveY(config, t1, time, layerPhase);
			float dx = x1 - x0;
			float dy = y1 - y0;
			float length = Mth.sqrt(dx * dx + dy * dy);
			if (length <= 0.001F) {
				continue;
			}
			float nX = -dy / length;
			float nY = dx / length;
			float width0 = widthAt(width, t0);
			float width1 = widthAt(width, t1);
			int a0 = alphaAt(alpha, t0);
			int a1 = alphaAt(alpha, t1);
			addQuad(buffer, matrix,
					x + x0 + nX * width0, y + y0 + nY * width0,
					x + x0 - nX * width0, y + y0 - nY * width0,
					x + x1 - nX * width1, y + y1 - nY * width1,
					x + x1 + nX * width1, y + y1 + nY * width1,
					red, green, blue, a0, a1);
		}
	}

	private static float curveX(float[] config, float t, float time, float layerPhase) {
		float angle = config[2] + Mth.sin(time * 0.28F + config[6]) * 0.14F;
		float length = config[3] * eased(t);
		float base = config[0] + Mth.cos(angle) * length;
		float normal = Mth.cos(angle + Mth.HALF_PI);
		return base + normal * wave(config, t, time, layerPhase);
	}

	private static float curveY(float[] config, float t, float time, float layerPhase) {
		float angle = config[2] + Mth.sin(time * 0.28F + config[6]) * 0.14F;
		float length = config[3] * eased(t);
		float base = config[1] + Mth.sin(angle) * length;
		float normal = Mth.sin(angle + Mth.HALF_PI);
		return base + normal * wave(config, t, time, layerPhase);
	}

	private static float wave(float[] config, float t, float time, float layerPhase) {
		float taper = Mth.sin(t * Mth.PI);
		float main = Mth.sin(t * Mth.TWO_PI * 1.28F + time * config[5] + config[6] + layerPhase);
		float small = Mth.sin(t * Mth.TWO_PI * 3.1F - time * 0.34F + config[6]);
		return (main * 0.78F + small * 0.22F) * config[4] * taper;
	}

	private static float eased(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	private static float widthAt(float width, float t) {
		float rootBulge = 0.66F + 0.18F * Mth.sin((1.0F - t) * Mth.HALF_PI);
		float tipTaper = 1.0F - Mth.clamp((t - 0.72F) / 0.28F, 0.0F, 1.0F) * 0.72F;
		return width * rootBulge * tipTaper;
	}

	private static float trimmedT(float value, float trim) {
		return trim + value * (1.0F - trim * 2.0F);
	}

	private static int alphaAt(int alpha, float t) {
		float tipFade = 1.0F - Mth.clamp((t - 0.82F) / 0.18F, 0.0F, 1.0F) * 0.64F;
		float rootFade = 0.58F + Mth.clamp(t / 0.12F, 0.0F, 1.0F) * 0.42F;
		return Mth.clamp((int) (alpha * rootFade * tipFade), 0, 255);
	}

	private static void addQuad(VertexConsumer buffer, Matrix4f matrix,
			float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
			int red, int green, int blue, int alpha0, int alpha1) {
		float z = MorphicNectarMutationRules.primalProceduralOverlayZ();
		int color0 = argb(alpha0, red, green, blue);
		int color1 = argb(alpha1, red, green, blue);
		buffer.addVertex(matrix, x1, y1, z).setColor(color0);
		buffer.addVertex(matrix, x0, y0, z).setColor(color0);
		buffer.addVertex(matrix, x3, y3, z).setColor(color1);
		buffer.addVertex(matrix, x2, y2, z).setColor(color1);
	}

	private static int argb(int alpha, int red, int green, int blue) {
		return Mth.clamp(alpha, 0, 255) << 24
				| Mth.clamp(red, 0, 255) << 16
				| Mth.clamp(green, 0, 255) << 8
				| Mth.clamp(blue, 0, 255);
	}
}

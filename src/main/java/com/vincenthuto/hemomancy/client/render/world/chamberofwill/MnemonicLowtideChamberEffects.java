package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Random;

public final class MnemonicLowtideChamberEffects extends AbstractChamberThemeEffects {
	private static final int SKY_LAKE_SUBDIVISIONS = 80;
	private static final int LOWTIDE_WATERY_FOG_BAND_COUNT = 10;
	private static final int LOWTIDE_WATERY_FOG_COLUMNS = 6;
	private static final int LOWTIDE_WATERY_FOG_ROWS = 4;
	private static final float SKY_LAKE_Y_SCALE = -0.05F;
	private static final float SKY_LAKE_HALF_SPAN_SCALE = 3.2F;
	private static final float SKY_LAKE_DEPTH_BOW_SCALE = 0.020F;
	private static final float LOWTIDE_WATERY_FOG_MIN_HEIGHT_SCALE = 0.018F;
	private static final float LOWTIDE_WATERY_FOG_MAX_HEIGHT_SCALE = 0.045F;
	private static final float LOWTIDE_SHADER_TIME_SCALE = 0.115F;
	private static final float WAVE_STRENGTH = 2.05F;
	private static final float WAVE_DETAIL_SCALE = 3.0F;
	private static final float NOISE_SCALE =  222.0F;
	private static final float GLOSS_STRENGTH = 0.005F;
	private static final float EDGE_FADE = 0F;

	MnemonicLowtideChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBeforeSharedLayers(ChamberThemeRenderContext context) {
		renderLowtideSkyLake(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideWateryFog(context.poseStack(), context.time(), context.skyDistance());
	}

	static void renderLowtideSkyLake(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.mnemonicLowtideLake(time * LOWTIDE_SHADER_TIME_SCALE, 17.0F,
				WAVE_STRENGTH, WAVE_DETAIL_SCALE, NOISE_SCALE, GLOSS_STRENGTH, EDGE_FADE);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitSkyLakeGrid(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
	}

	static void renderLowtideWateryFog(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(HemoRenderTypes.MNEMONIC_LOWTIDE_WATERY_FOG);
		Random random = new Random(73129L);
		float lakeY = skyDistance * SKY_LAKE_Y_SCALE;
		for (int band = 0; band < LOWTIDE_WATERY_FOG_BAND_COUNT; band++) {
			float ringT = (band + 0.5F) / LOWTIDE_WATERY_FOG_BAND_COUNT;
			float angle = ringT * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.18F, 0.18F);
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.24F, 0.92F);
			float driftX = Mth.sin(time * 0.0009F + band * 0.73F) * skyDistance * 0.035F;
			float driftZ = Mth.cos(time * 0.0007F + band * 0.61F) * skyDistance * 0.035F;
			float x = Mth.cos(angle) * distance + driftX;
			float z = Mth.sin(angle) * distance + driftZ;
			float baseY = lakeY + skyDistance
					* Mth.lerp(random.nextFloat(), LOWTIDE_WATERY_FOG_MIN_HEIGHT_SCALE,
							LOWTIDE_WATERY_FOG_MAX_HEIGHT_SCALE);
			float topY = baseY + skyDistance * Mth.lerp(random.nextFloat(), 0.030F, 0.070F);
			float halfWidth = skyDistance * Mth.lerp(random.nextFloat(), 0.18F, 0.42F);
			float depthBow = skyDistance * Mth.lerp(random.nextFloat(), 0.018F, 0.055F);
			float yaw = angle * Mth.RAD_TO_DEG + 90.0F + Mth.sin(time * 0.0012F + band) * 10.0F;
			float fogFadePulse = 0.52F + 0.48F * (0.5F + 0.5F * Mth.sin(time * 0.0015F + band * 1.19F));
			float phase = time * 0.00018F + band * 0.137F;
			emitLowtideWateryFogRibbon(consumer, poseStack.last().pose(), x, baseY, topY, z, yaw, halfWidth, depthBow,
					phase, fogFadePulse);
			emitLowtideWateryFogRibbon(consumer, poseStack.last().pose(), x, baseY, topY, z, yaw + 82.0F,
					halfWidth * 0.62F, depthBow * 0.72F, phase + 0.37F, fogFadePulse * 0.78F);
		}

		buffer.endBatch(HemoRenderTypes.MNEMONIC_LOWTIDE_WATERY_FOG);
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
	}

	private static void emitLowtideWateryFogRibbon(VertexConsumer consumer, Matrix4f matrix, float centerX,
			float bottomY, float topY, float centerZ, float yawDegrees, float halfWidth, float depthBow, float phase,
			float fogFadePulse) {
		float yaw = yawDegrees * Mth.DEG_TO_RAD;
		float rightX = Mth.cos(yaw);
		float rightZ = Mth.sin(yaw);
		float forwardX = -rightZ;
		float forwardZ = rightX;
		int xCells = LOWTIDE_WATERY_FOG_COLUMNS;
		int yCells = LOWTIDE_WATERY_FOG_ROWS;
		for (int xCell = 0; xCell < xCells; xCell++) {
			for (int yCell = 0; yCell < yCells; yCell++) {
				float x0T = xCell / (float) xCells;
				float x1T = (xCell + 1) / (float) xCells;
				float y0T = yCell / (float) yCells;
				float y1T = (yCell + 1) / (float) yCells;
				emitLowtideWateryFogCell(consumer, matrix, centerX, bottomY, topY, centerZ, rightX, rightZ, forwardX,
						forwardZ, halfWidth, depthBow, x0T, x1T, y0T, y1T, phase, fogFadePulse);
			}
		}
	}

	private static void emitLowtideWateryFogCell(VertexConsumer consumer, Matrix4f matrix, float centerX,
			float bottomY, float topY, float centerZ, float rightX, float rightZ, float forwardX, float forwardZ,
			float halfWidth, float depthBow, float x0T, float x1T, float y0T, float y1T, float phase,
			float fogFadePulse) {
		float x0 = Mth.lerp(x0T, -halfWidth, halfWidth);
		float x1 = Mth.lerp(x1T, -halfWidth, halfWidth);
		float y0 = Mth.lerp(y0T, bottomY, topY);
		float y1 = Mth.lerp(y1T, bottomY, topY);
		float z00 = lowtideWateryFogBow(x0T, y0T, depthBow, phase);
		float z01 = lowtideWateryFogBow(x0T, y1T, depthBow, phase);
		float z11 = lowtideWateryFogBow(x1T, y1T, depthBow, phase);
		float z10 = lowtideWateryFogBow(x1T, y0T, depthBow, phase);

		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x0, y0,
				z00, x0T, y0T, phase, fogFadePulse);
		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x0, y1,
				z01, x0T, y1T, phase, fogFadePulse);
		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x1, y1,
				z11, x1T, y1T, phase, fogFadePulse);
		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x1, y0,
				z10, x1T, y0T, phase, fogFadePulse);
	}

	private static void addLowtideWateryFogVertex(VertexConsumer consumer, Matrix4f matrix, float centerX,
			float centerZ, float rightX, float rightZ, float forwardX, float forwardZ, float xOffset, float y,
			float zOffset, float xT, float yT, float phase, float fogFadePulse) {
		float x = centerX + rightX * xOffset + forwardX * zOffset;
		float z = centerZ + rightZ * xOffset + forwardZ * zOffset;
		int packedColor = lowtideWateryFogColor(xT, yT, phase, fogFadePulse);
		consumer.addVertex(matrix, x, y, z)
				.setColor((packedColor >> 16) & 255, (packedColor >> 8) & 255, packedColor & 255,
						(packedColor >> 24) & 255);
	}

	private static float lowtideWateryFogBow(float xT, float yT, float depthBow, float phase) {
		float center = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
		float roll = Mth.sin((xT * 3.8F + yT * 2.4F + phase * 7.0F) * Mth.PI) * 0.32F;
		return depthBow * (center * 0.68F + roll);
	}

	private static int lowtideWateryFogColor(float xT, float yT, float phase, float fogFadePulse) {
		float warm = 0.5F + 0.5F * Mth.sin((xT * 4.2F + yT * 1.7F + phase) * Mth.TWO_PI);
		float redVeil = 0.5F + 0.5F * Mth.sin((xT * 6.8F - yT * 2.3F + phase * 0.71F) * Mth.TWO_PI);
		int red = Mth.floor(Mth.clamp(Mth.lerp(warm, 78.0F, 148.0F) + redVeil * 26.0F, 0.0F, 255.0F));
		int green = Mth.floor(Mth.clamp(Mth.lerp(warm, 34.0F, 94.0F) + yT * 20.0F, 0.0F, 255.0F));
		int blue = Mth.floor(Mth.clamp(Mth.lerp(warm, 36.0F, 82.0F) + yT * 18.0F, 0.0F, 255.0F));
		int alpha = lowtideWateryFogAlpha(xT, yT, phase, fogFadePulse);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private static int lowtideWateryFogAlpha(float xT, float yT, float phase, float fogFadePulse) {
		float xFade = 1.0F - Math.abs(xT - 0.5F) * 2.0F;
		float yFade = 1.0F - Math.abs(yT - 0.40F) * 1.45F;
		float baseLift = Mth.clamp(1.0F - yT * 0.44F, 0.54F, 1.0F);
		float edgeFade = Mth.clamp(Math.min(xFade, yFade) * 1.85F, 0.0F, 1.0F);
		float tornWisps = 0.70F + 0.30F * Mth.sin((xT * 5.0F + yT * 7.0F + phase * 3.0F) * Mth.PI);
		return (int) Mth.clamp(168.0F * edgeFade * baseLift * tornWisps * fogFadePulse, 0.0F, 178.0F);
	}

	private static void emitSkyLakeGrid(VertexConsumer consumer, Matrix4f matrix, float skyDistance) {
		float halfSpan = skyDistance * SKY_LAKE_HALF_SPAN_SCALE;
		float centerY = skyDistance * SKY_LAKE_Y_SCALE;
		float depthBow = skyDistance * SKY_LAKE_DEPTH_BOW_SCALE;
		float step = (halfSpan * 2.0F) / SKY_LAKE_SUBDIVISIONS;
		for (int xIndex = 0; xIndex < SKY_LAKE_SUBDIVISIONS; xIndex++) {
			float x0 = -halfSpan + step * xIndex;
			float x1 = x0 + step;
			float u0 = (float) xIndex / SKY_LAKE_SUBDIVISIONS;
			float u1 = (float) (xIndex + 1) / SKY_LAKE_SUBDIVISIONS;
			for (int zIndex = 0; zIndex < SKY_LAKE_SUBDIVISIONS; zIndex++) {
				float z0 = -halfSpan + step * zIndex;
				float z1 = z0 + step;
				float v0 = (float) zIndex / SKY_LAKE_SUBDIVISIONS;
				float v1 = (float) (zIndex + 1) / SKY_LAKE_SUBDIVISIONS;

				emitSkyLakeVertex(consumer, matrix, x0, skyLakeY(centerY, depthBow, u0, v1), z1, u0, v1);
				emitSkyLakeVertex(consumer, matrix, x1, skyLakeY(centerY, depthBow, u1, v1), z1, u1, v1);
				emitSkyLakeVertex(consumer, matrix, x1, skyLakeY(centerY, depthBow, u1, v0), z0, u1, v0);
				emitSkyLakeVertex(consumer, matrix, x0, skyLakeY(centerY, depthBow, u0, v0), z0, u0, v0);
			}
		}
	}

	private static float skyLakeY(float centerY, float depthBow, float u, float v) {
		float centeredX = Math.abs(u - 0.5F) * 2.0F;
		float centeredZ = Math.abs(v - 0.5F) * 2.0F;
		float edge = Math.max(centeredX, centeredZ);
		return centerY - depthBow * edge * edge;
	}

	private static void emitSkyLakeVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u,
			float v) {
		consumer.addVertex(matrix, x, y, z)
				.setUv(u, v)
				.setColor(255, 255, 255, 230);
	}
}

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
	private static final int LOWTIDE_SKYBOX_FACE_COUNT = 6;
	private static final int SKY_LAKE_SUBDIVISIONS = 80;
	private static final int LOWTIDE_WATERY_FOG_BAND_COUNT =12;
	private static final int LOWTIDE_WATERY_FOG_COLUMNS = 14;
	private static final int LOWTIDE_WATERY_FOG_ROWS = 9;
	private static final float LOWTIDE_SKYBOX_DISTANCE_SCALE = 0.965F;
	private static final float LOWTIDE_SKYBOX_TIME_SCALE = 0.040F;
	private static final float LOWTIDE_SKYBOX_BASE_TIME_SCALE = 0.026F;
	private static final float LOWTIDE_SKYBOX_TUNNEL_SEED = 41.0F;
	private static final float LOWTIDE_SKYBOX_BASE_SEED = 113.0F;
	private static final float LOWTIDE_SKYBOX_FACE_SEED_STEP = 37.0F;
	private static final float LOWTIDE_SKYBOX_BASE_NODULE_SCALE = 1.0F;
	private static final float LOWTIDE_SKYBOX_BASE_VEIN_INTENSITY = 1.0F;
	private static final float LOWTIDE_SKYBOX_BASE_INTENSITY = 1.02F;
	private static final float LOWTIDE_SKYBOX_TUNNEL_SCALE = 1.12F;
	private static final float LOWTIDE_SKYBOX_BUBBLE_SCALE = 1.14F;
	private static final float LOWTIDE_SKYBOX_TENDRIL_INTENSITY = 1.22F;
	private static final float SKY_LAKE_Y_SCALE = -0.05F;
	private static final float SKY_LAKE_HALF_SPAN_SCALE = 3.2F;
	private static final float SKY_LAKE_DEPTH_BOW_SCALE = 0.020F;
	private static final float LOWTIDE_WATERY_FOG_MIN_HEIGHT_SCALE = 0.018F;
	private static final float LOWTIDE_WATERY_FOG_MAX_HEIGHT_SCALE = 0.045F;
	private static final float LOWTIDE_WATERY_FOG_EDGE_LIVELINESS = 0.17F;
	private static final float LOWTIDE_WATERY_FOG_CYCLE_SPEED = 0.00082F;
	private static final float LOWTIDE_WATERY_FOG_CYCLE_STAGGER = 0.173F;
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
		renderLowtideSkyboxBase(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideTunnelSkybox(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideSkyLake(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideWateryFog(context.poseStack(), context.time(), context.skyDistance());
	}

	static void renderLowtideSkyboxBase(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		float faceDistance = skyDistance * LOWTIDE_SKYBOX_DISTANCE_SCALE;
		for (int face = 0; face < LOWTIDE_SKYBOX_FACE_COUNT; face++) {
			poseStack.pushPose();
			ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
			float coverageBias = lowtideSkyboxCoverageBias(face);
			RenderType renderType = HemoRenderTypes.mnemonicLowtideSkyboxBase(time * LOWTIDE_SKYBOX_BASE_TIME_SCALE,
					LOWTIDE_SKYBOX_BASE_SEED + face * LOWTIDE_SKYBOX_FACE_SEED_STEP, coverageBias,
					LOWTIDE_SKYBOX_BASE_NODULE_SCALE, LOWTIDE_SKYBOX_BASE_VEIN_INTENSITY,
					LOWTIDE_SKYBOX_BASE_INTENSITY);
			VertexConsumer consumer = buffer.getBuffer(renderType);
			emitLowtideSkyboxFace(consumer, poseStack.last().pose(), faceDistance, coverageBias);
			buffer.endBatch(renderType);
			poseStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
	}

	static void renderLowtideTunnelSkybox(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		float faceDistance = skyDistance * LOWTIDE_SKYBOX_DISTANCE_SCALE;
		for (int face = 0; face < LOWTIDE_SKYBOX_FACE_COUNT; face++) {
			poseStack.pushPose();
			ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
			float coverageBias = lowtideSkyboxCoverageBias(face);
			RenderType renderType = HemoRenderTypes.mnemonicLowtideSkybox(time * LOWTIDE_SKYBOX_TIME_SCALE,
					LOWTIDE_SKYBOX_TUNNEL_SEED + face * LOWTIDE_SKYBOX_FACE_SEED_STEP, coverageBias,
					LOWTIDE_SKYBOX_TUNNEL_SCALE, LOWTIDE_SKYBOX_BUBBLE_SCALE,
					LOWTIDE_SKYBOX_TENDRIL_INTENSITY);
			VertexConsumer consumer = buffer.getBuffer(renderType);
			emitLowtideSkyboxFace(consumer, poseStack.last().pose(), faceDistance, coverageBias);
			buffer.endBatch(renderType);
			poseStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
	}

	private static float lowtideSkyboxCoverageBias(int face) {
		return switch (face) {
			case 0 -> 0.46F;
			case 1 -> 1.0F;
			case 2 -> 0.78F;
			default -> 0.88F;
		};
	}

	private static void emitLowtideSkyboxFace(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float coverageBias) {
		int alpha = Mth.floor(Mth.clamp(coverageBias * 236.0F, 0.0F, 236.0F));
		emitLowtideSkyboxVertex(consumer, matrix, -skyDistance, -skyDistance, -skyDistance, 0.0F, 0.0F, alpha);
		emitLowtideSkyboxVertex(consumer, matrix, -skyDistance, -skyDistance, skyDistance, 0.0F, 1.0F, alpha);
		emitLowtideSkyboxVertex(consumer, matrix, skyDistance, -skyDistance, skyDistance, 1.0F, 1.0F, alpha);
		emitLowtideSkyboxVertex(consumer, matrix, skyDistance, -skyDistance, -skyDistance, 1.0F, 0.0F, alpha);
	}

	private static void emitLowtideSkyboxVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float u, float v, int alpha) {
		consumer.addVertex(matrix, x, y, z)
				.setUv(u, v)
				.setColor(255, 255, 255, alpha);
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
		float lakeY = skyDistance * SKY_LAKE_Y_SCALE;
		for (int band = 0; band < LOWTIDE_WATERY_FOG_BAND_COUNT; band++) {
			float cycleProgress = lowtideWateryFogCycleProgress(time, band);
			float cycleFade = lowtideWateryFogCycleFade(cycleProgress);
			if (cycleFade <= 0.01F) {
				continue;
			}
			long cycleIndex = lowtideWateryFogCycleIndex(time, band);
			Random random = lowtideWateryFogLifecycleRandom(band, cycleIndex);
			float ringT = random.nextFloat();
			float angle = ringT * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.18F, 0.18F);
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.24F, 0.92F);
			float driftX = Mth.sin(time * 0.0009F + band * 1.73F + cycleIndex * 0.37F) * skyDistance * 0.035F;
			float driftZ = Mth.cos(time * 0.007F + band * 1.61F + cycleIndex * 0.29F) * skyDistance * 0.035F;
			float x = Mth.cos(angle) * distance + driftX;
			float z = Mth.sin(angle) * distance + driftZ;
			float baseY = lakeY + skyDistance
					* Mth.lerp(random.nextFloat(), LOWTIDE_WATERY_FOG_MIN_HEIGHT_SCALE,
							LOWTIDE_WATERY_FOG_MAX_HEIGHT_SCALE);
			float topY = baseY + skyDistance * Mth.lerp(random.nextFloat(), 0.030F, 0.070F);
			float halfWidth = skyDistance * Mth.lerp(random.nextFloat(), 0.18F, 0.42F);
			float depthBow = skyDistance * Mth.lerp(random.nextFloat(), 0.018F, 0.055F);
			float yaw = angle * Mth.RAD_TO_DEG + 90.0F + Mth.sin(time * 0.0012F + band) * 10.0F;
			float fogFadePulse = cycleFade
					* (0.58F + 0.42F * (0.5F + 0.5F * Mth.sin(time * 0.0015F + band * 1.19F)));
			float phase = cycleProgress + time * 0.00018F + band * 0.137F + cycleIndex * 0.061F;
			emitLowtideWateryFogRibbon(consumer, poseStack.last().pose(), x, baseY, topY, z, yaw, halfWidth, depthBow,
					phase, fogFadePulse);
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
		float x00 = lowtideWateryFogX(x0, halfWidth, x0T, y0T, phase);
		float x01 = lowtideWateryFogX(x0, halfWidth, x0T, y1T, phase);
		float x11 = lowtideWateryFogX(x1, halfWidth, x1T, y1T, phase);
		float x10 = lowtideWateryFogX(x1, halfWidth, x1T, y0T, phase);
		float y00 = lowtideWateryFogY(bottomY, topY, x0T, y0T, phase);
		float y01 = lowtideWateryFogY(bottomY, topY, x0T, y1T, phase);
		float y11 = lowtideWateryFogY(bottomY, topY, x1T, y1T, phase);
		float y10 = lowtideWateryFogY(bottomY, topY, x1T, y0T, phase);
		float z00 = lowtideWateryFogBow(x0T, y0T, depthBow, phase);
		float z01 = lowtideWateryFogBow(x0T, y1T, depthBow, phase);
		float z11 = lowtideWateryFogBow(x1T, y1T, depthBow, phase);
		float z10 = lowtideWateryFogBow(x1T, y0T, depthBow, phase);

		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x00, y00,
				z00, x0T, y0T, phase, fogFadePulse);
		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x01, y01,
				z01, x0T, y1T, phase, fogFadePulse);
		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x11, y11,
				z11, x1T, y1T, phase, fogFadePulse);
		addLowtideWateryFogVertex(consumer, matrix, centerX, centerZ, rightX, rightZ, forwardX, forwardZ, x10, y10,
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

	private static float lowtideWateryFogX(float xOffset, float halfWidth, float xT, float yT, float phase) {
		float side = xT < 0.5F ? -1.0F : 1.0F;
		float sidePhase = side > 0.0F ? 0.41F : 0.0F;
		float broadCurl = Mth.sin((yT * 3.2F + phase * 2.4F + sidePhase) * Mth.TWO_PI);
		float tornCurl = Mth.sin((yT * 8.1F - phase * 4.3F + sidePhase * 0.53F) * Mth.TWO_PI);
		float edgeOffset = (broadCurl * 0.58F + tornCurl * 0.42F) * LOWTIDE_WATERY_FOG_EDGE_LIVELINESS;
		return xOffset + side * edgeOffset * halfWidth * 0.22F * lowtideWateryFogHorizontalEdgeInfluence(xT);
	}

	private static float lowtideWateryFogY(float bottomY, float topY, float xT, float yT, float phase) {
		float height = topY - bottomY;
		return Mth.lerp(yT, bottomY, topY)
				+ (lowtideWateryFogLowerEdgeOffset(xT, phase) * lowtideWateryFogEdgeInfluence(yT)
						+ lowtideWateryFogUpperEdgeOffset(xT, phase) * lowtideWateryFogTopEdgeInfluence(yT))
						* height * lowtideWateryFogVerticalEdgeInfluence(yT);
	}

	private static float lowtideWateryFogLowerEdgeOffset(float xT, float phase) {
		float broadSag = Mth.sin((xT * 2.7F + phase * 2.1F) * Mth.TWO_PI);
		float tornLift = Mth.sin((xT * 8.9F - phase * 3.8F) * Mth.TWO_PI);
		return (broadSag * 0.62F + tornLift * 0.38F) * LOWTIDE_WATERY_FOG_EDGE_LIVELINESS;
	}

	private static float lowtideWateryFogEdgeInfluence(float yT) {
		float influence = Mth.clamp(1.0F - yT / 0.38F, 0.0F, 1.0F);
		return influence * influence * (3.0F - 2.0F * influence);
	}

	private static float lowtideWateryFogUpperEdgeOffset(float xT, float phase) {
		float broadLift = Mth.sin((xT * 2.2F - phase * 1.8F + 0.31F) * Mth.TWO_PI);
		float tornLift = Mth.sin((xT * 7.4F + phase * 4.6F + 0.17F) * Mth.TWO_PI);
		return (broadLift * 0.56F + tornLift * 0.44F) * LOWTIDE_WATERY_FOG_EDGE_LIVELINESS;
	}

	private static float lowtideWateryFogTopEdgeInfluence(float yT) {
		float influence = Mth.clamp((yT - 0.62F) / 0.38F, 0.0F, 1.0F);
		return influence * influence * (3.0F - 2.0F * influence);
	}

	private static float lowtideWateryFogHorizontalEdgeInfluence(float xT) {
		float edgeDistance = Math.min(xT, 1.0F - xT);
		float influence = Mth.clamp((0.32F - edgeDistance) / 0.32F, 0.0F, 1.0F);
		return influence * influence * (3.0F - 2.0F * influence);
	}

	private static float lowtideWateryFogVerticalEdgeInfluence(float yT) {
		return Math.max(lowtideWateryFogEdgeInfluence(yT), lowtideWateryFogTopEdgeInfluence(yT));
	}

	private static float lowtideWateryFogCycleProgress(float time, int band) {
		float cycle = time * LOWTIDE_WATERY_FOG_CYCLE_SPEED + band * LOWTIDE_WATERY_FOG_CYCLE_STAGGER;
		return cycle - Mth.floor(cycle);
	}

	private static long lowtideWateryFogCycleIndex(float time, int band) {
		return Mth.floor(time * LOWTIDE_WATERY_FOG_CYCLE_SPEED + band * LOWTIDE_WATERY_FOG_CYCLE_STAGGER);
	}

	private static float lowtideWateryFogCycleFade(float cycleProgress) {
		float fadeIn = lowtideSmoothstep(0.06F, 0.24F, cycleProgress);
		float fadeOut = 1.0F - lowtideSmoothstep(0.72F, 0.96F, cycleProgress);
		return fadeIn * fadeOut;
	}

	private static Random lowtideWateryFogLifecycleRandom(int band, long cycleIndex) {
		return new Random(73129L + band * 8191L + cycleIndex * 104729L);
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
		float bottomBreakup = 0.13F
				+ 0.08F * (0.5F + 0.5F * Mth.sin((xT * 9.0F + phase * 5.2F) * Mth.TWO_PI));
		float bottomEdgeFade = lowtideSmoothstep(0.015F, bottomBreakup, yT);
		float sideBreakup = 0.08F
				+ 0.05F * (0.5F + 0.5F * Mth.sin((yT * 8.6F - phase * 4.7F) * Mth.TWO_PI));
		float leftRightEdgeFade = lowtideSmoothstep(0.010F, sideBreakup, Math.min(xT, 1.0F - xT));
		float topBreakup = 0.09F
				+ 0.06F * (0.5F + 0.5F * Mth.sin((xT * 7.7F - phase * 3.6F) * Mth.TWO_PI));
		float topEdgeFade = lowtideSmoothstep(0.012F, topBreakup, 1.0F - yT);
		return (int) Mth.clamp(
				168.0F * edgeFade * baseLift * tornWisps * bottomEdgeFade * leftRightEdgeFade * topEdgeFade
						* fogFadePulse,
				0.0F, 178.0F);
	}

	private static float lowtideSmoothstep(float edge0, float edge1, float value) {
		float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
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

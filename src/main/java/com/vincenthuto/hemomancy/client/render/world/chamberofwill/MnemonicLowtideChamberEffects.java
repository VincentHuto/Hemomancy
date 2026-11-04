package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import com.vincenthuto.hemomancy.config.HemoClientConfig.LowtideRuinStructureQuality;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Random;

public final class MnemonicLowtideChamberEffects extends AbstractChamberThemeEffects {
	// ========== SKYBOX ==========
	private static final int LOWTIDE_SKYBOX_FACE_COUNT = 6;
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

	// ========== SKY LAKE ==========
	private static final int SKY_LAKE_SUBDIVISIONS = 80;
	private static final float SKY_LAKE_Y_SCALE = -0.05F;
	private static final float SKY_LAKE_HALF_SPAN_SCALE = 3.2F;
	private static final float SKY_LAKE_DEPTH_BOW_SCALE = 0.020F;

	// ========== RUIN CLUSTERS ==========
	private static final int LOWTIDE_RUIN_HIGH_FAR_CLUSTER_COUNT = 127;
	private static final int LOWTIDE_RUIN_HIGH_DISTANT_CLUSTER_COUNT = 12;
	private static final int LOWTIDE_RUIN_LOW_FAR_CLUSTER_COUNT = 27;
	private static final int LOWTIDE_RUIN_LOW_DISTANT_CLUSTER_COUNT = 6;
	private static final long LOWTIDE_RUIN_SEED = 108571L;
	private static final float LOWTIDE_RUIN_DISTANT_LIGHT = 0.30F;

	// ========== RUIN OBJECT SCALES ==========
	private static final float LOWTIDE_RUIN_OBJ_FOUNDATION_SCALE = 0.34F;
	private static final float LOWTIDE_RUIN_OBJ_TOWER_SCALE = 0.23F;
	private static final float LOWTIDE_RUIN_OBJ_ARCH_SCALE = 0.25F;
	private static final float LOWTIDE_RUIN_OBJ_DOME_SCALE = 0.25F;
	private static final float LOWTIDE_RUIN_OBJ_SPIRE_SCALE = 0.122F;
	private static final float LOWTIDE_RUIN_UNDERWATER_RED_DIMMING_STRENGTH = 0.92F;
	private static final float LOWTIDE_RUIN_UNDERWATER_DIMMING_HEIGHT = 5.8F;
	private static final float LOWTIDE_RUIN_UNDERWATER_RED_FLOOR = 0.38F;
	private static final float LOWTIDE_RUIN_UNDERWATER_GREEN_FLOOR = 0.13F;
	private static final float LOWTIDE_RUIN_UNDERWATER_BLUE_FLOOR = 0.10F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_AMBIENT = 0.74F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_TOP_STRENGTH = 0.34F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_SIDE_STRENGTH = 0.26F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_SIDE_X = -0.48F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_SIDE_Y = 0.34F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_SIDE_Z = 0.81F;
	private static final float LOWTIDE_RUIN_OBJ_LIGHT_MAX = 1.22F;

	// ========== CEILING ROOTS ==========
	private static final int LOWTIDE_CEILING_ROOT_CLUSTER_COUNT = 24;
	private static final int LOWTIDE_CEILING_ROOT_MIN_STRANDS = 5;
	private static final int LOWTIDE_CEILING_ROOT_MAX_STRANDS = 11;
	private static final int LOWTIDE_CEILING_ROOT_SEGMENTS = 22;
	private static final int LOWTIDE_CEILING_ROOT_FORK_SEGMENTS = 9;
	private static final long LOWTIDE_CEILING_ROOT_SEED = 219781L;
	private static final float LOWTIDE_CEILING_ROOT_TIME_SCALE = 0.018F;
	private static final float LOWTIDE_CEILING_ROOT_CEILING_Y_SCALE = 0.80F;
	private static final float LOWTIDE_CEILING_ROOT_MIN_LENGTH_SCALE = 0.20F;
	private static final float LOWTIDE_CEILING_ROOT_MAX_LENGTH_SCALE = 0.56F;
	private static final float LOWTIDE_CEILING_ROOT_SPAN_SCALE = 0.82F;
	private static final float LOWTIDE_CEILING_ROOT_CLUSTER_RADIUS_SCALE = 0.044F;
	private static final float LOWTIDE_CEILING_ROOT_MIN_WIDTH_SCALE = 0.0024F;
	private static final float LOWTIDE_CEILING_ROOT_MAX_WIDTH_SCALE = 0.0058F;
	private static final float LOWTIDE_CEILING_ROOT_BODY_ALPHA = 214.0F;
	private static final float LOWTIDE_CEILING_ROOT_HIGHLIGHT_ALPHA = 132.0F;

	// ========== PARCHMENT ==========
	private static final int LOWTIDE_WATER_PARCHMENT_COUNT = 24;
	private static final int LOWTIDE_AIR_PARCHMENT_COUNT = 12;
	private static final int LOWTIDE_PARCHMENT_DISTANCE_LAYER_PERIOD = 3;
	private static final int LOWTIDE_PARCHMENT_GRID_COLUMNS = 6;
	private static final int LOWTIDE_PARCHMENT_GRID_ROWS = 4;
	private static final long LOWTIDE_PARCHMENT_SEED = 317219L;
	private static final float LOWTIDE_PARCHMENT_TIME_SCALE = 0.010F;
	private static final float LOWTIDE_PARCHMENT_SHADER_TIME_SCALE = 0.060F;
	private static final float LOWTIDE_PARCHMENT_WATER_WIND_STRENGTH = 0.055F;
	private static final float LOWTIDE_PARCHMENT_AIR_WIND_STRENGTH = 0.420F;
	private static final float LOWTIDE_PARCHMENT_WIND_SCALE = 1.6F;
	private static final float LOWTIDE_PARCHMENT_WATER_MIN_DISTANCE_SCALE = 0.38F;
	private static final float LOWTIDE_PARCHMENT_WATER_MAX_DISTANCE_SCALE = 1.16F;
	private static final float LOWTIDE_PARCHMENT_AIR_MIN_DISTANCE_SCALE = 0.42F;
	private static final float LOWTIDE_PARCHMENT_AIR_MAX_DISTANCE_SCALE = 1.22F;
	private static final float LOWTIDE_PARCHMENT_AIR_MIN_UPRIGHT_PITCH = 1.02F;
	private static final float LOWTIDE_PARCHMENT_AIR_MAX_UPRIGHT_PITCH = 1.34F;
	private static final float LOWTIDE_PARCHMENT_NEAR_DISTANCE_SCALE = 0.20F;
	private static final float[][] LOWTIDE_PARCHMENT_ATLAS_VARIANTS = {
			{ 0.0F, 0.0F, 0.25F, 1.0F },
			{ 0.25F, 0.0F, 0.5F, 1.0F },
			{ 0.5F, 0.0F, 0.75F, 1.0F },
			{ 0.75F, 0.0F, 1.0F, 1.0F }
	};

	// ========== WATERY FOG ==========
	private static final int LOWTIDE_WATERY_FOG_BAND_COUNT = 12;
	private static final int LOWTIDE_WATERY_FOG_COLUMNS = 14;
	private static final int LOWTIDE_WATERY_FOG_ROWS = 9;
	private static final float LOWTIDE_WATERY_FOG_MIN_HEIGHT_SCALE = 0.018F;
	private static final float LOWTIDE_WATERY_FOG_MAX_HEIGHT_SCALE = 0.045F;
	private static final float LOWTIDE_WATERY_FOG_EDGE_LIVELINESS = 0.17F;
	private static final float LOWTIDE_WATERY_FOG_CYCLE_SPEED = 0.00082F;
	private static final float LOWTIDE_WATERY_FOG_CYCLE_STAGGER = 0.173F;

	// ========== SHADER & WAVE EFFECTS ==========
	private static final float LOWTIDE_SHADER_TIME_SCALE = 0.115F;
	private static final float WAVE_STRENGTH = 2.05F;
	private static final float WAVE_DETAIL_SCALE = 3.0F;
	private static final float NOISE_SCALE = 222.0F;
	private static final float GLOSS_STRENGTH = 0.005F;
	// Lake-local rim width. The shader clamps near 0.49 because the UV edge distance tops out at 0.5.
	private static final float EDGE_FADE = 0.47F;
	private static VertexBuffer lowtideRuinObjVertexBuffer;
	private static float lowtideRuinObjVertexBufferSkyDistance = Float.NaN;
	private static LowtideRuinStructureQuality lowtideRuinObjVertexBufferQuality = LowtideRuinStructureQuality.HIGH;
	private static boolean lowtideRuinObjVertexBufferDirty = true;

	MnemonicLowtideChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	static void invalidateLowtideObjRuinCache() {
		lowtideRuinObjVertexBufferDirty = true;
	}

	@Override
	protected void renderBeforeSharedLayers(ChamberThemeRenderContext context) {
		renderLowtideSkyboxBase(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideTunnelSkybox(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideCeilingRoots(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideRuinedStructures(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideWaterParchment(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideSkyLake(context.poseStack(), context.time(), context.skyDistance());
		renderLowtideAirParchment(context.poseStack(), context.time(), context.skyDistance());
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

	static void renderLowtideCeilingRoots(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(HemoRenderTypes.MNEMONIC_LOWTIDE_CEILING_ROOTS);
		Matrix4f matrix = poseStack.last().pose();
		Random random = new Random(LOWTIDE_CEILING_ROOT_SEED);
		float rootTime = time * LOWTIDE_CEILING_ROOT_TIME_SCALE;
		float ceilingY = skyDistance * LOWTIDE_CEILING_ROOT_CEILING_Y_SCALE;
		for (int cluster = 0; cluster < LOWTIDE_CEILING_ROOT_CLUSTER_COUNT; cluster++) {
			float ringT = (cluster + random.nextFloat() * 0.78F) / LOWTIDE_CEILING_ROOT_CLUSTER_COUNT;
			float angle = ringT * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.16F, 0.16F);
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.10F, LOWTIDE_CEILING_ROOT_SPAN_SCALE);
			if (cluster % 6 == 0) {
				distance *= 0.58F;
			}
			float clusterX = Mth.cos(angle) * distance;
			float clusterZ = Mth.sin(angle) * distance;
			float clusterY = ceilingY + skyDistance * Mth.lerp(random.nextFloat(), -0.026F, 0.018F);
			int strandCount = LOWTIDE_CEILING_ROOT_MIN_STRANDS
					+ random.nextInt(LOWTIDE_CEILING_ROOT_MAX_STRANDS - LOWTIDE_CEILING_ROOT_MIN_STRANDS + 1);
			for (int strand = 0; strand < strandCount; strand++) {
				Random strandRandom = new Random(LOWTIDE_CEILING_ROOT_SEED + cluster * 1299709L + strand * 64271L);
				float localAngle = strandRandom.nextFloat() * Mth.TWO_PI;
				float localDistance = skyDistance * LOWTIDE_CEILING_ROOT_CLUSTER_RADIUS_SCALE
						* Mth.lerp(strandRandom.nextFloat(), 0.08F, 1.12F);
				float anchorX = clusterX + Mth.cos(localAngle) * localDistance;
				float anchorY = clusterY + skyDistance * Mth.lerp(strandRandom.nextFloat(), -0.010F, 0.010F);
				float anchorZ = clusterZ + Mth.sin(localAngle) * localDistance;
				float length = skyDistance * Mth.lerp(strandRandom.nextFloat(),
						LOWTIDE_CEILING_ROOT_MIN_LENGTH_SCALE, LOWTIDE_CEILING_ROOT_MAX_LENGTH_SCALE);
				float baseWidth = skyDistance * Mth.lerp(strandRandom.nextFloat(),
						LOWTIDE_CEILING_ROOT_MIN_WIDTH_SCALE, LOWTIDE_CEILING_ROOT_MAX_WIDTH_SCALE);
				float ribbonAngle = localAngle + Mth.lerp(strandRandom.nextFloat(), -0.40F, 0.40F);
				float phase = strandRandom.nextFloat() * Mth.TWO_PI + cluster * 0.47F;
				float alphaScale = Mth.lerp(strandRandom.nextFloat(), 0.82F, 1.0F);
				int seed = cluster * 97 + strand * 29;
				emitLowtideCeilingRootStrand(consumer, matrix, anchorX, anchorY, anchorZ, length, baseWidth,
						ribbonAngle, phase, rootTime, alphaScale, seed);
			}
		}

		buffer.endBatch(HemoRenderTypes.MNEMONIC_LOWTIDE_CEILING_ROOTS);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void emitLowtideCeilingRootStrand(VertexConsumer consumer, Matrix4f matrix, float anchorX,
			float anchorY, float anchorZ, float length, float baseWidth, float ribbonAngle, float phase,
			float rootTime, float alphaScale, int seed) {
		for (int segment = 0; segment < LOWTIDE_CEILING_ROOT_SEGMENTS; segment++) {
			float t0 = segment / (float) LOWTIDE_CEILING_ROOT_SEGMENTS;
			float t1 = (segment + 1) / (float) LOWTIDE_CEILING_ROOT_SEGMENTS;
			float x0 = lowtideCeilingRootX(anchorX, length, t0, phase, rootTime, seed);
			float y0 = lowtideCeilingRootY(anchorY, length, t0, phase, rootTime);
			float z0 = lowtideCeilingRootZ(anchorZ, length, t0, phase, rootTime, seed);
			float x1 = lowtideCeilingRootX(anchorX, length, t1, phase, rootTime, seed);
			float y1 = lowtideCeilingRootY(anchorY, length, t1, phase, rootTime);
			float z1 = lowtideCeilingRootZ(anchorZ, length, t1, phase, rootTime, seed);
			float width0 = lowtideCeilingRootWidth(baseWidth, t0, phase, rootTime);
			float width1 = lowtideCeilingRootWidth(baseWidth, t1, phase, rootTime);
			int body0 = lowtideCeilingRootBodyColor(seed, t0, alphaScale);
			int body1 = lowtideCeilingRootBodyColor(seed, t1, alphaScale);
			emitLowtideCeilingRootCrossRibbon(consumer, matrix, x0, y0, z0, width0, body0, x1, y1, z1, width1,
					body1, ribbonAngle);

			if ((seed + segment) % 3 == 0) {
				int glint0 = lowtideCeilingRootParchmentColor(seed, t0, alphaScale);
				int glint1 = lowtideCeilingRootParchmentColor(seed, t1, alphaScale);
				emitLowtideCeilingRootCrossRibbon(consumer, matrix, x0, y0, z0, width0 * 0.28F, glint0,
						x1, y1, z1, width1 * 0.24F, glint1, ribbonAngle + 0.28F);
			}
		}

		if (seed % 3 != 1) {
			emitLowtideCeilingRootFork(consumer, matrix, anchorX, anchorY, anchorZ, length, baseWidth, ribbonAngle,
					phase, rootTime, alphaScale, seed);
		}
	}

	private static void emitLowtideCeilingRootFork(VertexConsumer consumer, Matrix4f matrix, float anchorX,
			float anchorY, float anchorZ, float parentLength, float parentWidth, float ribbonAngle, float phase,
			float rootTime, float alphaScale, int seed) {
		float forkT = 0.36F + Math.floorMod(seed, 7) * 0.055F;
		float startX = lowtideCeilingRootX(anchorX, parentLength, forkT, phase, rootTime, seed);
		float startY = lowtideCeilingRootY(anchorY, parentLength, forkT, phase, rootTime);
		float startZ = lowtideCeilingRootZ(anchorZ, parentLength, forkT, phase, rootTime, seed);
		float side = seed % 2 == 0 ? 1.0F : -1.0F;
		float forkAngle = ribbonAngle + side * (0.70F + Math.floorMod(seed, 5) * 0.09F);
		float forkLength = parentLength * (0.14F + Math.floorMod(seed, 6) * 0.018F);
		float alongX = Mth.cos(forkAngle);
		float alongZ = Mth.sin(forkAngle);
		float forkPhase = phase + seed * 0.37F;
		for (int segment = 0; segment < LOWTIDE_CEILING_ROOT_FORK_SEGMENTS; segment++) {
			float t0 = segment / (float) LOWTIDE_CEILING_ROOT_FORK_SEGMENTS;
			float t1 = (segment + 1) / (float) LOWTIDE_CEILING_ROOT_FORK_SEGMENTS;
			float x0 = lowtideCeilingRootForkX(startX, alongX, forkLength, t0, forkPhase, rootTime);
			float y0 = lowtideCeilingRootForkY(startY, forkLength, t0, forkPhase, rootTime);
			float z0 = lowtideCeilingRootForkZ(startZ, alongZ, forkLength, t0, forkPhase, rootTime);
			float x1 = lowtideCeilingRootForkX(startX, alongX, forkLength, t1, forkPhase, rootTime);
			float y1 = lowtideCeilingRootForkY(startY, forkLength, t1, forkPhase, rootTime);
			float z1 = lowtideCeilingRootForkZ(startZ, alongZ, forkLength, t1, forkPhase, rootTime);
			float width0 = lowtideCeilingRootWidth(parentWidth * 0.42F, t0, forkPhase, rootTime);
			float width1 = lowtideCeilingRootWidth(parentWidth * 0.42F, t1, forkPhase, rootTime);
			int color0 = lowtideCeilingRootParchmentColor(seed + 11, t0, alphaScale * 0.74F);
			int color1 = lowtideCeilingRootParchmentColor(seed + 11, t1, alphaScale * 0.74F);
			emitLowtideCeilingRootCrossRibbon(consumer, matrix, x0, y0, z0, width0, color0, x1, y1, z1, width1,
					color1, forkAngle);
		}
	}

	private static float lowtideCeilingRootX(float anchorX, float length, float t, float phase, float rootTime,
			int seed) {
		float rootEase = lowtideSmoothstep(0.0F, 0.18F, t);
		float broad = Mth.sin(rootTime * 0.73F + phase + t * 4.6F);
		float fine = Mth.sin(rootTime * 1.17F + phase * 0.47F + t * 13.2F);
		float fixedCurl = Mth.sin(seed * 0.31F + t * Mth.PI * 1.35F);
		return anchorX + (broad * 0.018F + fine * 0.006F + fixedCurl * 0.010F) * length * rootEase;
	}

	private static float lowtideCeilingRootY(float anchorY, float length, float t, float phase, float rootTime) {
		float tremble = Mth.sin(rootTime * 0.42F + phase + t * 5.8F) * length * 0.004F * t;
		return anchorY - length * t + tremble;
	}

	private static float lowtideCeilingRootZ(float anchorZ, float length, float t, float phase, float rootTime,
			int seed) {
		float rootEase = lowtideSmoothstep(0.0F, 0.18F, t);
		float broad = Mth.cos(rootTime * 0.68F + phase * 0.83F + t * 4.1F);
		float fine = Mth.sin(rootTime * 1.03F + phase * 0.59F + t * 12.4F);
		float fixedCurl = Mth.cos(seed * 0.27F + t * Mth.PI * 1.48F);
		return anchorZ + (broad * 0.017F + fine * 0.006F + fixedCurl * 0.010F) * length * rootEase;
	}

	private static float lowtideCeilingRootForkX(float startX, float alongX, float forkLength, float t,
			float phase, float rootTime) {
		float curl = Mth.sin(rootTime * 0.92F + phase + t * 7.4F) * forkLength * 0.040F;
		return startX + alongX * forkLength * t + curl;
	}

	private static float lowtideCeilingRootForkY(float startY, float forkLength, float t, float phase,
			float rootTime) {
		float sag = Mth.sin(t * Mth.PI) * forkLength * 0.10F;
		float flutter = Mth.sin(rootTime * 0.74F + phase + t * 5.0F) * forkLength * 0.010F;
		return startY - forkLength * 0.66F * t - sag + flutter;
	}

	private static float lowtideCeilingRootForkZ(float startZ, float alongZ, float forkLength, float t,
			float phase, float rootTime) {
		float curl = Mth.cos(rootTime * 0.88F + phase * 0.71F + t * 6.7F) * forkLength * 0.040F;
		return startZ + alongZ * forkLength * t + curl;
	}

	private static float lowtideCeilingRootWidth(float baseWidth, float t, float phase, float rootTime) {
		float taper = Mth.clamp(1.12F - t * 0.96F, 0.080F, 1.0F);
		float pulse = 0.88F + 0.12F * Mth.sin(rootTime * 1.6F + phase + t * 9.0F);
		return baseWidth * taper * pulse;
	}

	private static int lowtideCeilingRootBodyColor(int seed, float t, float alphaScale) {
		float damp = 0.88F + Math.floorMod(seed, 5) * 0.040F;
		int red = Mth.floor(Mth.clamp((88.0F + Math.floorMod(seed, 4) * 10.0F) * damp, 0.0F, 255.0F));
		int green = Mth.floor(Mth.clamp(28.0F + t * 24.0F, 0.0F, 255.0F));
		int blue = Mth.floor(Mth.clamp(18.0F + t * 14.0F, 0.0F, 255.0F));
		int alpha = lowtideCeilingRootAlpha(t, alphaScale, LOWTIDE_CEILING_ROOT_BODY_ALPHA);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private static int lowtideCeilingRootParchmentColor(int seed, float t, float alphaScale) {
		int red = 206 + Math.floorMod(seed, 5) * 6;
		int green = Mth.floor(Mth.clamp(142.0F + t * 30.0F, 0.0F, 255.0F));
		int blue = Mth.floor(Mth.clamp(82.0F + t * 22.0F, 0.0F, 255.0F));
		int alpha = lowtideCeilingRootAlpha(t, alphaScale, LOWTIDE_CEILING_ROOT_HIGHLIGHT_ALPHA);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private static int lowtideCeilingRootAlpha(float t, float alphaScale, float baseAlpha) {
		float tipFade = 1.0F - lowtideSmoothstep(0.72F, 1.0F, t);
		float rootFade = Mth.clamp(0.42F + t * 5.0F, 0.0F, 1.0F);
		float lowerFade = Mth.clamp(1.0F - t * 0.24F, 0.42F, 1.0F);
		return Mth.floor(Mth.clamp(baseAlpha * tipFade * rootFade * lowerFade * alphaScale, 0.0F, 220.0F));
	}

	private static void emitLowtideCeilingRootCrossRibbon(VertexConsumer consumer, Matrix4f matrix, float x0,
			float y0, float z0, float width0, int color0, float x1, float y1, float z1, float width1, int color1,
			float ribbonAngle) {
		emitLowtideCeilingRootRibbon(consumer, matrix, x0, y0, z0, width0, color0, x1, y1, z1, width1, color1,
				ribbonAngle);
		emitLowtideCeilingRootRibbon(consumer, matrix, x0, y0, z0, width0, color0, x1, y1, z1, width1, color1,
				ribbonAngle + Mth.HALF_PI);
	}

	private static void emitLowtideCeilingRootRibbon(VertexConsumer consumer, Matrix4f matrix, float x0, float y0,
			float z0, float width0, int color0, float x1, float y1, float z1, float width1, int color1,
			float ribbonAngle) {
		float normalX = Mth.cos(ribbonAngle);
		float normalZ = Mth.sin(ribbonAngle);
		addLowtideCeilingRootVertex(consumer, matrix, x0 - normalX * width0, y0, z0 - normalZ * width0, color0);
		addLowtideCeilingRootVertex(consumer, matrix, x0 + normalX * width0, y0, z0 + normalZ * width0, color0);
		addLowtideCeilingRootVertex(consumer, matrix, x1 + normalX * width1, y1, z1 + normalZ * width1, color1);
		addLowtideCeilingRootVertex(consumer, matrix, x1 - normalX * width1, y1, z1 - normalZ * width1, color1);
	}

	private static void addLowtideCeilingRootVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, int color) {
		consumer.addVertex(matrix, x, y, z)
				.setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255);
	}

	static void renderLowtideSkyLake(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
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
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	static void renderLowtideWaterParchment(PoseStack poseStack, float time, float skyDistance) {
		renderLowtideParchmentLayer(poseStack, time, skyDistance, true);
	}

	static void renderLowtideAirParchment(PoseStack poseStack, float time, float skyDistance) {
		renderLowtideParchmentLayer(poseStack, time, skyDistance, false);
	}

	private static void renderLowtideParchmentLayer(PoseStack poseStack, float time, float skyDistance,
			boolean waterLayer) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		float windStrength = waterLayer ? LOWTIDE_PARCHMENT_WATER_WIND_STRENGTH : LOWTIDE_PARCHMENT_AIR_WIND_STRENGTH;
		RenderType renderType = HemoRenderTypes.mnemonicLowtideParchment(time * LOWTIDE_PARCHMENT_SHADER_TIME_SCALE,
				waterLayer ? 23.0F : 61.0F, windStrength, LOWTIDE_PARCHMENT_WIND_SCALE,
				waterLayer ? 0.82F : 0.36F, waterLayer ? 0.27F : 0.93F);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		int count = waterLayer ? LOWTIDE_WATER_PARCHMENT_COUNT : LOWTIDE_AIR_PARCHMENT_COUNT;
		float lakeY = skyDistance * SKY_LAKE_Y_SCALE;
		float parchmentTime = time * LOWTIDE_PARCHMENT_TIME_SCALE;
		Random random = new Random(LOWTIDE_PARCHMENT_SEED + (waterLayer ? 13L : 71L));
		for (int piece = 0; piece < count; piece++) {
			Random pieceRandom = new Random(LOWTIDE_PARCHMENT_SEED + (waterLayer ? 101L : 503L)
					+ piece * 104729L);
			float ringT = (piece + random.nextFloat() * 0.82F) / count;
			float angle = ringT * Mth.TWO_PI + Mth.lerp(pieceRandom.nextFloat(), -0.18F, 0.18F);
			boolean nearLayer = piece % LOWTIDE_PARCHMENT_DISTANCE_LAYER_PERIOD == 0;
			float minDistance = nearLayer ? LOWTIDE_PARCHMENT_NEAR_DISTANCE_SCALE
					: waterLayer
							? LOWTIDE_PARCHMENT_WATER_MIN_DISTANCE_SCALE
							: LOWTIDE_PARCHMENT_AIR_MIN_DISTANCE_SCALE;
			float maxDistance = nearLayer ? (waterLayer ? 0.38F : 0.40F)
					: waterLayer
							? LOWTIDE_PARCHMENT_WATER_MAX_DISTANCE_SCALE
							: LOWTIDE_PARCHMENT_AIR_MAX_DISTANCE_SCALE;
			float distance = skyDistance * Mth.lerp(pieceRandom.nextFloat(), minDistance, maxDistance);
			float phase = pieceRandom.nextFloat() * Mth.TWO_PI + piece * 0.37F;
			float radialX = Mth.cos(angle);
			float radialZ = Mth.sin(angle);
			float tangentX = -radialZ;
			float tangentZ = radialX;
			float radialDrift = Mth.sin(parchmentTime * (waterLayer ? 0.48F : 0.62F) + phase)
					* skyDistance * (waterLayer ? 0.012F : 0.020F);
			float tangentDrift = Mth.cos(parchmentTime * (waterLayer ? 0.57F : 0.74F) + phase * 0.73F)
					* skyDistance * (waterLayer ? 0.024F : 0.038F);
			float centerX = radialX * (distance + radialDrift) + tangentX * tangentDrift;
			float centerZ = radialZ * (distance + radialDrift) + tangentZ * tangentDrift;
			float heightScale = waterLayer
					? Mth.lerp(pieceRandom.nextFloat(), 0.006F, 0.030F)
					: Mth.lerp(pieceRandom.nextFloat(), 0.070F, 0.270F);
			float bob = Mth.sin(parchmentTime * (waterLayer ? 0.86F : 1.08F) + phase)
					* skyDistance * (waterLayer ? 0.0032F : 0.012F);
			float centerY = lakeY + skyDistance * heightScale + bob;
			float landmarkScale = nearLayer ? 0.88F : piece % (waterLayer ? 7 : 4) == 0 ? 1.18F : 1.0F;
			float sheetWidth = skyDistance * (waterLayer
					? Mth.lerp(pieceRandom.nextFloat(), 0.040F, 0.092F)
					: Mth.lerp(pieceRandom.nextFloat(), 0.040F, 0.078F)) * landmarkScale;
			float sheetHeight = sheetWidth * Mth.lerp(pieceRandom.nextFloat(), waterLayer ? 0.30F : 0.46F,
					waterLayer ? 0.54F : 0.72F);
			float yaw = Mth.HALF_PI - angle + Mth.lerp(pieceRandom.nextFloat(), -0.44F, 0.44F)
					+ parchmentTime * (waterLayer ? 0.026F : 0.054F);
			float pitch = waterLayer
					? Mth.lerp(pieceRandom.nextFloat(), -0.075F, 2.075F)
							+ Mth.sin(parchmentTime * 0.72F + phase) * 0.038F
					: Mth.lerp(pieceRandom.nextFloat(), LOWTIDE_PARCHMENT_AIR_MIN_UPRIGHT_PITCH,
							LOWTIDE_PARCHMENT_AIR_MAX_UPRIGHT_PITCH)
							+ Mth.sin(parchmentTime * 0.86F + phase) * 0.16F;
			float roll = waterLayer
					? Mth.lerp(pieceRandom.nextFloat(), -0.18F, 0.18F)
							+ Mth.cos(parchmentTime * 0.66F + phase) * 0.052F
					: Mth.lerp(pieceRandom.nextFloat(), -0.48F, 0.48F)
							+ Mth.cos(parchmentTime * 0.92F + phase) * 0.22F;
			float fadePulse = 0.78F + 0.22F * Mth.sin(parchmentTime * 0.54F + phase);
			float alphaScale = (waterLayer
					? Mth.lerp(pieceRandom.nextFloat(), 0.78F, 1.0F)
					: Mth.lerp(pieceRandom.nextFloat(), 0.80F, 1.0F)) * fadePulse;

			poseStack.pushPose();
			poseStack.translate(centerX, centerY + 1.0F, centerZ);
			poseStack.mulPose(Axis.YP.rotation(yaw));
			poseStack.mulPose(Axis.XP.rotation(pitch));
			poseStack.mulPose(Axis.ZP.rotation(roll));
			emitLowtideTexturedParchmentSheet(consumer, poseStack.last().pose(), sheetWidth * 0.5F,
					sheetHeight * 0.5F, piece, parchmentTime + phase, alphaScale, waterLayer);
			poseStack.popPose();
		}

		buffer.endBatch(renderType);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void emitLowtideTexturedParchmentSheet(VertexConsumer consumer, Matrix4f matrix, float halfWidth,
			float halfHeight, int seed, float flutterTime, float alphaScale, boolean waterLayer) {
		float[] uvBounds = LOWTIDE_PARCHMENT_ATLAS_VARIANTS[Math.floorMod(seed,
				LOWTIDE_PARCHMENT_ATLAS_VARIANTS.length)];
		int color = lowtideParchmentTextureTint(seed, alphaScale, waterLayer);
		for (int row = 0; row < LOWTIDE_PARCHMENT_GRID_ROWS; row++) {
			float zT0 = row / (float) LOWTIDE_PARCHMENT_GRID_ROWS;
			float zT1 = (row + 1) / (float) LOWTIDE_PARCHMENT_GRID_ROWS;
			for (int column = 0; column < LOWTIDE_PARCHMENT_GRID_COLUMNS; column++) {
				float xT0 = column / (float) LOWTIDE_PARCHMENT_GRID_COLUMNS;
				float xT1 = (column + 1) / (float) LOWTIDE_PARCHMENT_GRID_COLUMNS;
				addLowtideParchmentGridVertex(consumer, matrix, halfWidth, halfHeight, seed, flutterTime,
						waterLayer, uvBounds, xT0, zT0, color);
				addLowtideParchmentGridVertex(consumer, matrix, halfWidth, halfHeight, seed + column * 17 + 3,
						flutterTime, waterLayer, uvBounds, xT1, zT0, color);
				addLowtideParchmentGridVertex(consumer, matrix, halfWidth, halfHeight, seed + column * 17 + row * 31,
						flutterTime, waterLayer, uvBounds, xT1, zT1, color);
				addLowtideParchmentGridVertex(consumer, matrix, halfWidth, halfHeight, seed + row * 31 + 5,
						flutterTime, waterLayer, uvBounds, xT0, zT1, color);
			}
		}
	}

	private static float lowtideParchmentCurl(float x, float z, float halfWidth, float halfHeight, int seed,
			float flutterTime, boolean waterLayer) {
		float safeHalfWidth = Math.max(halfWidth, 0.001F);
		float safeHalfHeight = Math.max(halfHeight, 0.001F);
		float xT = x / safeHalfWidth;
		float zT = z / safeHalfHeight;
		float broad = Mth.sin(flutterTime * (waterLayer ? 0.48F : 0.72F) + seed * 0.43F + xT * 1.7F);
		float torn = Mth.cos(flutterTime * (waterLayer ? 0.36F : 0.58F) + seed * 0.31F + zT * 3.6F);
		float edgeLift = lowtideSmoothstep(0.18F, 0.96F, Math.max(Math.abs(xT), Math.abs(zT)));
		return (broad * 0.034F + torn * 0.018F) * Math.max(halfWidth, halfHeight)
				* edgeLift * (waterLayer ? 0.36F : 1.0F);
	}

	private static void addLowtideParchmentGridVertex(VertexConsumer consumer, Matrix4f matrix, float halfWidth,
			float halfHeight, int seed, float flutterTime, boolean waterLayer, float[] uvBounds, float xT, float zT,
			int color) {
		float x = Mth.lerp(xT, -halfWidth, halfWidth);
		float z = Mth.lerp(zT, -halfHeight, halfHeight);
		float y = lowtideParchmentCurl(x, z, halfWidth, halfHeight, seed, flutterTime, waterLayer);
		float u = Mth.lerp(xT, uvBounds[0], uvBounds[2]);
		float v = Mth.lerp(zT, uvBounds[1], uvBounds[3]);
		addLowtideParchmentTexturedVertex(consumer, matrix, x, y, z, u, v, color);
	}

	private static int lowtideParchmentTextureTint(int seed, float alphaScale, boolean waterLayer) {
		int red = waterLayer ? 232 + Math.floorMod(seed, 3) * 5 : 248 + Math.floorMod(seed, 2) * 3;
		int green = waterLayer ? 218 + Math.floorMod(seed, 4) * 4 : 239 + Math.floorMod(seed, 3) * 3;
		int blue = waterLayer ? 198 + Math.floorMod(seed, 3) * 3 : 220 + Math.floorMod(seed, 4) * 2;
		float alpha = (waterLayer ? 245.0F : 238.0F) * alphaScale;
		return lowtideParchmentTextureColor(red, green, blue, alpha);
	}

	private static int lowtideParchmentTextureColor(int red, int green, int blue, float alpha) {
		int packedAlpha = Mth.floor(Mth.clamp(alpha, 0.0F, 255.0F));
		return packedAlpha << 24
				| Mth.floor(Mth.clamp(red, 0.0F, 255.0F)) << 16
				| Mth.floor(Mth.clamp(green, 0.0F, 255.0F)) << 8
				| Mth.floor(Mth.clamp(blue, 0.0F, 255.0F));
	}

	private static void addLowtideParchmentTexturedVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, float u, float v, int color) {
		consumer.addVertex(matrix, x, y, z)
				.setUv(u, v)
				.setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255);
	}

	static void renderLowtideRuinedStructures(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		LowtideRuinStructureQuality quality = HemoClientConfig.lowtideRuinStructureQuality();
		if (quality != LowtideRuinStructureQuality.OFF) {
			renderLowtideObjRuinField(poseStack, time, skyDistance, quality);
		}
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderLowtideObjRuinField(PoseStack poseStack, float time, float skyDistance,
			LowtideRuinStructureQuality quality) {
		RenderSystem.enableCull();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		if (!ensureLowtideObjRuinVertexBuffer(skyDistance, quality)) {
			return;
		}

		ShaderInstance shader = GameRenderer.getRendertypeSolidShader();
		if (shader == null) {
			return;
		}

		RenderType renderType = RenderType.solid();
		renderType.setupRenderState();
		lowtideRuinObjVertexBuffer.bind();
		lowtideRuinObjVertexBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), shader);
		VertexBuffer.unbind();
		renderType.clearRenderState();
	}

	private static boolean ensureLowtideObjRuinVertexBuffer(float skyDistance, LowtideRuinStructureQuality quality) {
		if (!lowtideRuinObjVertexBufferDirty && lowtideRuinObjVertexBuffer != null
				&& Float.compare(lowtideRuinObjVertexBufferSkyDistance, skyDistance) == 0
				&& lowtideRuinObjVertexBufferQuality == quality) {
			return true;
		}

		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		BlockState state = Blocks.AIR.defaultBlockState();
		PoseStack ruinPoseStack = new PoseStack();
		Random random = new Random(LOWTIDE_RUIN_SEED + 4049L);
		int farClusterCount = lowtideRuinFarClusterCount(quality);
		int distantClusterCount = lowtideRuinDistantClusterCount(quality);
		for (int cluster = 0; cluster < farClusterCount; cluster++) {
			float ringT = (cluster + random.nextFloat() * 0.72F) / farClusterCount;
			float angle = ringT * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.12F, 0.12F);
			float depthLane = random.nextFloat();
			float distanceT;
			if (depthLane < 0.22F) {
				distanceT = Mth.lerp(random.nextFloat(), 0.58F, 0.78F);
			} else if (depthLane < 0.58F) {
				distanceT = Mth.lerp(random.nextFloat(), 0.82F, 1.08F);
			} else if (depthLane < 0.86F) {
				distanceT = Mth.lerp(random.nextFloat(), 1.14F, 1.42F);
			} else {
				distanceT = Mth.lerp(random.nextFloat(), 1.50F, 1.74F);
			}
			distanceT += Mth.sin(cluster * 2.173F) * 0.035F;
			float distance = skyDistance * distanceT;
			float distanceFade = Mth.clamp((distanceT - 0.58F) / 1.16F, 0.0F, 1.0F);
			float scale = skyDistance * Mth.lerp(distanceFade, Mth.lerp(random.nextFloat(), 0.026F, 0.040F),
					Mth.lerp(random.nextFloat(), 0.010F, 0.020F));
			float light = Mth.lerp(distanceFade, 0.64F, 0.34F);
			emitLowtideObjRuinCluster(ruinPoseStack, buffer, state, skyDistance, cluster, angle,
					distance, scale, light, false,
					new Random(LOWTIDE_RUIN_SEED + 9001L + cluster * 1871L));
		}

		random = new Random(LOWTIDE_RUIN_SEED + 19731L);
		for (int cluster = 0; cluster < distantClusterCount; cluster++) {
			float ringT = (cluster + random.nextFloat() * 0.86F) / distantClusterCount;
			float angle = ringT * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.10F, 0.10F);
			float distanceT = Mth.lerp(random.nextFloat(), 1.62F, 2.05F) + Mth.sin(cluster * 1.719F) * 0.045F;
			float distance = skyDistance * distanceT;
			float scale = skyDistance * Mth.lerp(random.nextFloat(), 0.007F, 0.014F);
			float light = Mth.lerp(Mth.clamp((distanceT - 1.62F) / 0.43F, 0.0F, 1.0F), LOWTIDE_RUIN_DISTANT_LIGHT,
					0.18F);
			emitLowtideObjRuinCluster(ruinPoseStack, buffer, state, skyDistance,
					farClusterCount + cluster, angle, distance, scale,
					light, true, new Random(LOWTIDE_RUIN_SEED + 31817L + cluster * 1327L));
		}

		MeshData meshData = buffer.build();
		if (meshData == null) {
			lowtideRuinObjVertexBufferDirty = true;
			return false;
		}

		if (lowtideRuinObjVertexBuffer != null) {
			lowtideRuinObjVertexBuffer.close();
		}
		lowtideRuinObjVertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		lowtideRuinObjVertexBuffer.bind();
		lowtideRuinObjVertexBuffer.upload(meshData);
		VertexBuffer.unbind();
		lowtideRuinObjVertexBufferSkyDistance = skyDistance;
		lowtideRuinObjVertexBufferQuality = quality;
		lowtideRuinObjVertexBufferDirty = false;
		return true;
	}

	private static int lowtideRuinFarClusterCount(LowtideRuinStructureQuality quality) {
		return quality == LowtideRuinStructureQuality.LOW
				? LOWTIDE_RUIN_LOW_FAR_CLUSTER_COUNT
				: LOWTIDE_RUIN_HIGH_FAR_CLUSTER_COUNT;
	}

	private static int lowtideRuinDistantClusterCount(LowtideRuinStructureQuality quality) {
		return quality == LowtideRuinStructureQuality.LOW
				? LOWTIDE_RUIN_LOW_DISTANT_CLUSTER_COUNT
				: LOWTIDE_RUIN_HIGH_DISTANT_CLUSTER_COUNT;
	}

	private static void emitLowtideObjRuinCluster(PoseStack poseStack, VertexConsumer consumer, BlockState state,
			float skyDistance, int cluster, float angle,
			float distance, float scale, float lightLevel, boolean distant, Random random) {
		float lakeY = skyDistance * SKY_LAKE_Y_SCALE;
		float drift = Mth.sin(cluster * 0.83F) * skyDistance * (distant ? 0.0018F : 0.0028F);
		float centerX = Mth.cos(angle) * distance + Mth.cos(angle + Mth.PI * 0.5F) * drift;
		float centerZ = Mth.sin(angle) * distance + Mth.sin(angle + Mth.PI * 0.5F) * drift;
		float rightX = -Mth.sin(angle);
		float rightZ = Mth.cos(angle);
		float forwardX = Mth.cos(angle);
		float forwardZ = Mth.sin(angle);
		float bobY = Mth.sin(cluster * 1.17F) * skyDistance * (distant ? 0.0007F : 0.0011F);
		float yaw = Mth.HALF_PI - angle + Mth.lerp(random.nextFloat(), -0.16F, 0.16F);
		float redScale = lightLevel;
		float greenScale = lightLevel * 0.92F;
		float blueScale = lightLevel * 0.82F;
		float xOffset = scale * Mth.lerp(random.nextFloat(), -1.8F, 1.8F);
		float zOffset = scale * Mth.lerp(random.nextFloat(), -1.4F, 1.4F);
		LowtideRuinObjModels.Piece piece = lowtideRuinObjPieceForCluster(cluster);
		float modelScale = scale * lowtideRuinObjScale(piece);
		float sinkDepth = lowtideRuinObjSinkDepth(piece, scale, distant, random);
		float pitch = lowtideRuinObjPitch(piece, distant, random);
		float roll = lowtideRuinObjRoll(piece, distant, random);

		emitLowtideObjPiece(poseStack, consumer, state, piece, centerX, centerZ, rightX, rightZ,
				forwardX, forwardZ, xOffset, lakeY + bobY + lowtideRuinObjBaseLift(piece, scale) - sinkDepth,
				zOffset, yaw, modelScale, pitch, roll, lowtideRuinObjSourceCenterX(piece), lowtideRuinObjSourceCenterZ(piece),
				lowtideRuinObjRed(piece) * redScale, lowtideRuinObjGreen(piece) * greenScale,
				lowtideRuinObjBlue(piece) * blueScale);
	}

	private static LowtideRuinObjModels.Piece lowtideRuinObjPieceForCluster(int cluster) {
		if (cluster % 5 == 0) {
			return LowtideRuinObjModels.Piece.ARCH_FRAGMENT;
		}
		if (cluster % 7 == 0) {
			return LowtideRuinObjModels.Piece.DOME_CHAPEL;
		}
		return switch (Math.floorMod(cluster, 3)) {
			case 0 -> LowtideRuinObjModels.Piece.FOUNDATION_RUBBLE;
			case 1 -> LowtideRuinObjModels.Piece.SPIRE_FRAGMENT;
			default -> LowtideRuinObjModels.Piece.TOWER_SHORT;
		};
	}

	private static float lowtideRuinObjScale(LowtideRuinObjModels.Piece piece) {
		return switch (piece) {
			case FOUNDATION_RUBBLE -> LOWTIDE_RUIN_OBJ_FOUNDATION_SCALE;
			case TOWER_SHORT -> LOWTIDE_RUIN_OBJ_TOWER_SCALE;
			case ARCH_FRAGMENT -> LOWTIDE_RUIN_OBJ_ARCH_SCALE;
			case DOME_CHAPEL -> LOWTIDE_RUIN_OBJ_DOME_SCALE;
			case SPIRE_FRAGMENT -> LOWTIDE_RUIN_OBJ_SPIRE_SCALE;
		};
	}

	private static float lowtideRuinObjBaseLift(LowtideRuinObjModels.Piece piece, float scale) {
		return switch (piece) {
			case FOUNDATION_RUBBLE -> -scale * 0.18F;
			case TOWER_SHORT -> scale * 0.14F;
			case ARCH_FRAGMENT -> scale * 0.22F;
			case DOME_CHAPEL -> scale * 0.10F;
			case SPIRE_FRAGMENT -> scale * 0.26F;
		};
	}

	private static float lowtideRuinObjSinkDepth(LowtideRuinObjModels.Piece piece, float scale, boolean distant,
			Random random) {
		float baseSink = switch (piece) {
			case FOUNDATION_RUBBLE -> Mth.lerp(random.nextFloat(), 0.12F, 0.34F);
			case TOWER_SHORT -> Mth.lerp(random.nextFloat(), 0.24F, 0.52F);
			case ARCH_FRAGMENT -> Mth.lerp(random.nextFloat(), 0.18F, 0.44F);
			case DOME_CHAPEL -> Mth.lerp(random.nextFloat(), 0.26F, 0.58F);
			case SPIRE_FRAGMENT -> Mth.lerp(random.nextFloat(), 0.30F, 0.66F);
		};
		return scale * (baseSink + (distant ? Mth.lerp(random.nextFloat(), 0.10F, 0.26F) : 0.0F));
	}

	private static float lowtideRuinObjPitch(LowtideRuinObjModels.Piece piece, boolean distant, Random random) {
		float maxPitch = switch (piece) {
			case FOUNDATION_RUBBLE -> 0.10F;
			case TOWER_SHORT -> 0.22F;
			case ARCH_FRAGMENT -> 0.18F;
			case DOME_CHAPEL -> 0.20F;
			case SPIRE_FRAGMENT -> 0.28F;
		};
		if (distant) {
			maxPitch *= 1.22F;
		}
		return Mth.lerp(random.nextFloat(), -maxPitch, maxPitch);
	}

	private static float lowtideRuinObjRoll(LowtideRuinObjModels.Piece piece, boolean distant, Random random) {
		float maxRoll = switch (piece) {
			case FOUNDATION_RUBBLE -> 0.12F;
			case TOWER_SHORT -> 0.30F;
			case ARCH_FRAGMENT -> 0.24F;
			case DOME_CHAPEL -> 0.22F;
			case SPIRE_FRAGMENT -> 0.34F;
		};
		if (distant) {
			maxRoll *= 1.18F;
		}
		return Mth.lerp(random.nextFloat(), -maxRoll, maxRoll);
	}

	private static float lowtideRuinObjSourceCenterX(LowtideRuinObjModels.Piece piece) {
		return switch (piece) {
			case FOUNDATION_RUBBLE, ARCH_FRAGMENT -> 7.0F;
			case TOWER_SHORT -> 3.0F;
			case DOME_CHAPEL -> 6.0F;
			case SPIRE_FRAGMENT -> 2.0F;
		};
	}

	private static float lowtideRuinObjSourceCenterZ(LowtideRuinObjModels.Piece piece) {
		return switch (piece) {
			case FOUNDATION_RUBBLE -> 4.0F;
			case TOWER_SHORT -> 3.0F;
			case ARCH_FRAGMENT, SPIRE_FRAGMENT -> 2.0F;
			case DOME_CHAPEL -> 6.0F;
		};
	}

	private static float lowtideRuinObjRed(LowtideRuinObjModels.Piece piece) {
		return switch (piece) {
			case FOUNDATION_RUBBLE -> 0.96F;
			case TOWER_SHORT -> 0.90F;
			case ARCH_FRAGMENT -> 0.88F;
			case DOME_CHAPEL -> 0.86F;
			case SPIRE_FRAGMENT -> 0.92F;
		};
	}

	private static float lowtideRuinObjGreen(LowtideRuinObjModels.Piece piece) {
		return switch (piece) {
			case FOUNDATION_RUBBLE -> 0.90F;
			case TOWER_SHORT -> 0.84F;
			case ARCH_FRAGMENT -> 0.80F;
			case DOME_CHAPEL -> 0.78F;
			case SPIRE_FRAGMENT -> 0.83F;
		};
	}

	private static float lowtideRuinObjBlue(LowtideRuinObjModels.Piece piece) {
		return switch (piece) {
			case FOUNDATION_RUBBLE -> 0.78F;
			case TOWER_SHORT -> 0.72F;
			case ARCH_FRAGMENT -> 0.68F;
			case DOME_CHAPEL -> 0.66F;
			case SPIRE_FRAGMENT -> 0.70F;
		};
	}

	private static void emitLowtideObjPiece(PoseStack poseStack, VertexConsumer consumer, BlockState state,
			LowtideRuinObjModels.Piece piece, float centerX, float centerZ, float rightX,
			float rightZ, float forwardX, float forwardZ, float xOffset, float baseY, float zOffset, float yaw,
			float modelScale, float pitch, float roll, float sourceCenterX, float sourceCenterZ, float red, float green,
			float blue) {
		BakedModel model = LowtideRuinObjModels.model(piece);
		if (model == null) {
			return;
		}

		float x = centerX + rightX * xOffset + forwardX * zOffset;
		float z = centerZ + rightZ * xOffset + forwardZ * zOffset;
		poseStack.pushPose();
		poseStack.translate(x, baseY, z);
		poseStack.mulPose(Axis.YP.rotation(yaw));
		poseStack.mulPose(Axis.XP.rotation(pitch));
		poseStack.mulPose(Axis.ZP.rotation(roll));
		poseStack.scale(modelScale, modelScale, modelScale);
		poseStack.translate(-sourceCenterX, 0.5F, -sourceCenterZ);
		emitLowtideObjModelQuads(consumer, poseStack.last(), state, model, red, green, blue);
		poseStack.popPose();
	}

	private static void emitLowtideObjModelQuads(VertexConsumer consumer, PoseStack.Pose pose, BlockState state,
			BakedModel model, float red, float green, float blue) {
		RandomSource random = RandomSource.create(42L);
		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			emitLowtideObjQuadList(consumer, pose, model.getQuads(state, direction, random, ModelData.EMPTY,
					RenderType.solid()), red, green, blue);
		}
		random.setSeed(42L);
		emitLowtideObjQuadList(consumer, pose, model.getQuads(state, null, random, ModelData.EMPTY,
				RenderType.solid()), red, green, blue);
	}

	private static void emitLowtideObjQuadList(VertexConsumer consumer, PoseStack.Pose pose, Iterable<BakedQuad> quads,
			float red, float green, float blue) {
		for (BakedQuad quad : quads) {
			emitLowtideObjQuadWithUnderwaterDimming(consumer, pose, quad, red, green, blue);
		}
	}

	private static void emitLowtideObjQuadWithUnderwaterDimming(VertexConsumer consumer, PoseStack.Pose pose,
			BakedQuad quad, float red, float green, float blue) {
		int[] vertexData = quad.getVertices();
		int vertexCount = vertexData.length / 8;
		Matrix4f positionMatrix = pose.pose();
		Matrix3f normalMatrix = pose.normal();

		for (int vertex = 0; vertex < vertexCount; vertex++) {
			int offset = vertex * 8;
			float x = Float.intBitsToFloat(vertexData[offset]);
			float y = Float.intBitsToFloat(vertexData[offset + 1]);
			float z = Float.intBitsToFloat(vertexData[offset + 2]);
			float u = Float.intBitsToFloat(vertexData[offset + 4]);
			float v = Float.intBitsToFloat(vertexData[offset + 5]);
			int packedNormal = vertexData[offset + 7];
			float nx = (byte) (packedNormal & 255) / 127.0F;
			float ny = (byte) ((packedNormal >> 8) & 255) / 127.0F;
			float nz = (byte) ((packedNormal >> 16) & 255) / 127.0F;
			float dimming = lowtideRuinUnderwaterDimming(y);
			float vertexRed = red * Mth.lerp(dimming, 1.0F, LOWTIDE_RUIN_UNDERWATER_RED_FLOOR);
			float vertexGreen = green * Mth.lerp(dimming, 1.0F, LOWTIDE_RUIN_UNDERWATER_GREEN_FLOOR);
			float vertexBlue = blue * Mth.lerp(dimming, 1.0F, LOWTIDE_RUIN_UNDERWATER_BLUE_FLOOR);

			Vector4f position = new Vector4f(x, y, z, 1.0F);
			position.mul(positionMatrix);
			Vector3f normal = new Vector3f(nx, ny, nz);
			normal.mul(normalMatrix);
			normal.normalize();
			float lighting = lowtideRuinObjVertexLighting(normal);
			vertexRed *= lighting;
			vertexGreen *= lighting;
			vertexBlue *= lighting;

			consumer.addVertex(position.x(), position.y(), position.z())
					.setColor(Mth.clamp(vertexRed, 0.0F, 1.0F), Mth.clamp(vertexGreen, 0.0F, 1.0F),
							Mth.clamp(vertexBlue, 0.0F, 1.0F), 1.0F)
					.setUv(u, v)
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(LightTexture.FULL_BRIGHT)
					.setNormal(normal.x(), normal.y(), normal.z());
		}
	}

	private static float lowtideRuinObjVertexLighting(Vector3f normal) {
		float topLight = Math.max(normal.y(), 0.0F) * LOWTIDE_RUIN_OBJ_LIGHT_TOP_STRENGTH;
		float sideDot = normal.x() * LOWTIDE_RUIN_OBJ_LIGHT_SIDE_X
				+ normal.y() * LOWTIDE_RUIN_OBJ_LIGHT_SIDE_Y
				+ normal.z() * LOWTIDE_RUIN_OBJ_LIGHT_SIDE_Z;
		float sideLight = Math.max(sideDot, 0.0F) * LOWTIDE_RUIN_OBJ_LIGHT_SIDE_STRENGTH;
		return Mth.clamp(LOWTIDE_RUIN_OBJ_LIGHT_AMBIENT + topLight + sideLight,
				LOWTIDE_RUIN_OBJ_LIGHT_AMBIENT, LOWTIDE_RUIN_OBJ_LIGHT_MAX);
	}

	private static float lowtideRuinUnderwaterDimming(float modelY) {
		float heightT = 1.0F - lowtideSmoothstep(0.0F, LOWTIDE_RUIN_UNDERWATER_DIMMING_HEIGHT, modelY);
		return Mth.clamp(heightT * LOWTIDE_RUIN_UNDERWATER_RED_DIMMING_STRENGTH, 0.0F, 1.0F);
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

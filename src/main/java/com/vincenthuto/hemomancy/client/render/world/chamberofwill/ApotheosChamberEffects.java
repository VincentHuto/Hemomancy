package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

final class ApotheosChamberEffects extends AbstractChamberThemeEffects {
	private static final int APOTHEOS_FLOOR_RING_SEGMENTS = 64;
	private static final int APOTHEOS_FLOOR_RADIAL_SEGMENTS = 48;
	private static final float APOTHEOS_FLOOR_INNER_RADIUS_SCALE = 0.018F;
	private static final float APOTHEOS_FLOOR_OUTER_RADIUS_SCALE = 0.62F;
	private static final float APOTHEOS_FLOOR_Y_SCALE = -0.30F;
	private static final float APOTHEOS_FLOOR_DROP_SCALE = 0.56F;
	private static final float APOTHEOS_FLOOR_RISE_SCALE = 0.095F;
	private static final float APOTHEOS_FLOOR_SHADER_TIME_SCALE = 0.088F;
	private static final float APOTHEOS_FLOOR_RING_RISE = 0.72F;
	private static final float APOTHEOS_FLOOR_RING_SPEED = 0.92F;
	private static final float APOTHEOS_FLOOR_MEAT_NOISE_SCALE = 7.8F;
	private static final float APOTHEOS_FLOOR_HIGHLIGHT_INTENSITY = 1.18F;
	private static final float APOTHEOS_FLOOR_CENTER_VOID_RADIUS = 0.095F;

	private static final int APOTHEOS_WALL_RING_SEGMENTS = 96;
	private static final int APOTHEOS_WALL_VERTICAL_SEGMENTS = 20;
	private static final int APOTHEOS_WALL_FRAME_RIBS = 20;

	private static final float APOTHEOS_WALL_RADIUS_SCALE = 0.66F;
	private static final float APOTHEOS_WALL_BOTTOM_Y_SCALE = -0.58F;
	private static final float APOTHEOS_WALL_TOP_Y_SCALE = 1.36F;
	private static final float APOTHEOS_WALL_FIBER_SCALE = 2.4F;
	private static final float APOTHEOS_WALL_TRACE_INTENSITY = 0.68F;
	private static final float APOTHEOS_WALL_RED_GLOW_INTENSITY = 0.95F;
	private static final float APOTHEOS_WALL_CEILING_FADE_START = 0.68F;
	private static final float APOTHEOS_WALL_CEILING_FADE_END = 0.92F;

	private static final int APOTHEOS_CEILING_RING_SEGMENTS = 96;
	private static final int APOTHEOS_CEILING_RADIAL_SEGMENTS = 36;
	private static final int APOTHEOS_CEILING_TENDRIL_COUNT = 14;
	private static final int APOTHEOS_CEILING_TENDRIL_SEGMENTS = 18;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_COUNT = 18;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENTS = 15;
	private static final int APOTHEOS_CEILING_ORB_COUNT = 9;

	private static final float APOTHEOS_CEILING_DOME_SPAN_SCALE = 0.78F;
	private static final float APOTHEOS_CEILING_Y_SCALE = 1.83F;
	private static final float APOTHEOS_CEILING_DROP_SCALE = 0.29F;
	private static final float APOTHEOS_CEILING_SHADER_TIME_SCALE = 0.052F;
	private static final float APOTHEOS_CEILING_CORE_NOISE_SCALE = 5.8F;
	private static final float APOTHEOS_CEILING_ATMOSPHERE_NOISE_SCALE = 9.4F;
	private static final float APOTHEOS_CEILING_CORE_ROTATION_SPEED = 0.20F;
	private static final float APOTHEOS_CEILING_ATMOSPHERE_ROTATION_SPEED = 0.56F;
	private static final float APOTHEOS_CEILING_YELLOW_GLOW_INTENSITY = 2.25F;
	private static final float APOTHEOS_CEILING_GREEN_ORB_INTENSITY = 1.82F;
	private static final float APOTHEOS_CEILING_CORE_RADIUS_SCALE = 0.82F;
	private static final float APOTHEOS_CEILING_ATMOSPHERE_RADIUS_SCALE = 1.1F;
	private static final float APOTHEOS_CEILING_ATMOSPHERE_Y_OFFSET_SCALE = -0.045F;

	private static final float APOTHEOS_CEILING_CORE_UNDULATION_INTENSITY = 0.18F;
	private static final float APOTHEOS_CEILING_CORE_BODY_YAW_SPEED = 0.82F;
	private static final float APOTHEOS_CEILING_CORE_BODY_PITCH_SPEED = 0F;
	private static final float APOTHEOS_CEILING_CORE_BODY_ROLL_SPEED = -0F;
	private static final float APOTHEOS_CEILING_CORE_INNER_IRREGULARITY_SCALE = 0.026F;
	private static final float APOTHEOS_CEILING_CORE_INNER_RIDGE_SCALE = 0.008F;
	private static final float APOTHEOS_CEILING_ATMOSPHERE_STORM_INTENSITY = 1.45F;
	private static final float APOTHEOS_CEILING_ATMOSPHERE_OPACITY = 0.28F;
	private static final float APOTHEOS_CEILING_CORE_CENTER_ANGLE_T = 0.5F;
	private static final float APOTHEOS_CEILING_CORE_CENTER_RADIAL_EPSILON = 0.001F;

	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_ROOT_RADIAL_T = 0.56F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_MIN_LENGTH_MULTIPLIER = 0.88F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_MAX_LENGTH_MULTIPLIER = 1.22F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_WIDTH_SCALE = 0.014F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_CORE_WIDTH_MULTIPLIER = 0.95F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_GLOW_WIDTH_MULTIPLIER = 2.10F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_CAMERA_PULL_SCALE = 0.30F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_RADIAL_PUSH_SCALE = 0.78F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_VERTICAL_SAG_SCALE = 0.026F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_TIME_SCALE = 0.24F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_SCALE = 0.015F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_FREQUENCY = 3.0F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_GEOMETRY_TIME_SCALE = 0.124F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RATIO = 0.8F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_ALPHA = 236.0F;
	private static final float APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_ALPHA = 224.0F;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_RED = 255;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_GREEN = 218;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_BLUE = 46;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RED = 236;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_GREEN = 238;
	private static final int APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_BLUE = 234;
	private static final TendrilEffectConfig APOTHEOS_CEILING_OUTWARD_TENDRIL_CONFIG = TendrilEffectConfig.defaults()
			.withMode(TendrilEffectConfig.Mode.FREEFORM)
			.withBlendColors(false)
			.withLifecycle(1, 1, 1)
			.withShape(28, 1, 0.088F, 0.13F)
			.withBranching(2, 1, 0.18F, 0.72F)
			.withWrithe(1.85F, 0.135F, 2.35F, -0.42F);

	private static final float APOTHEOS_WALL_TOP_RIM_HEIGHT_T = 0.28F;
	private static final float APOTHEOS_WALL_TOP_RIM_RADIUS_SCALE = 0.2F;
	private static final float APOTHEOS_WALL_TOP_RIM_CORE_WIDTH_SCALE = 0.03F;
	private static final float APOTHEOS_WALL_TOP_RIM_GLOW_WIDTH_SCALE = .2F;
	private static final float APOTHEOS_WALL_TOP_RIM_Y_OFFSET_SCALE = 0.3F;
	private static final float APOTHEOS_WALL_TOP_RIM_SHADER_TIME_SCALE = 0.064F;
	private static final float APOTHEOS_WALL_TOP_RIM_PULSE_SPEED = 1.86F;
	private static final float APOTHEOS_WALL_TOP_RIM_CORE_INTENSITY = 2.18F;
	private static final float APOTHEOS_WALL_TOP_RIM_GLOW_INTENSITY = 1.55F;
	private static final int APOTHEOS_WALL_TOP_RIM_GLOW_RED = 155;
	private static final int APOTHEOS_WALL_TOP_RIM_GLOW_GREEN = 168;
	private static final int APOTHEOS_WALL_TOP_RIM_GLOW_BLUE = 238;
	private static final int APOTHEOS_WALL_TOP_RIM_CORE_RED = 255;
	private static final int APOTHEOS_WALL_TOP_RIM_CORE_GREEN = 104;
	private static final int APOTHEOS_WALL_TOP_RIM_CORE_BLUE = 4;

	private static final int APOTHEOS_WALL_WEB_RIBBONS = 0;
	private static final int APOTHEOS_WALL_WEB_RIBBON_SEGMENTS = 22;
	private static final float APOTHEOS_WALL_WEB_MIN_HEIGHT_T = 0.46F;

	private static final float APOTHEOS_PORTAL_GLOW_RADIUS = 0.42F;
	private static final float APOTHEOS_PORTAL_GLOW_INTENSITY = 1.35F;
	private static final float APOTHEOS_PORTAL_HAZE_SPEED = 0.58F;
	private static final float APOTHEOS_PORTAL_HAZE_INTENSITY = 1.18F;

	ApotheosChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		if (context.theme().renderBaseSkybox()) {
			ChamberOfWillRenderHelpers.renderSolidBox(context.poseStack(), context.tesselator(),
					context.skyDistance(), context.theme().skyboxColor());
		}
	}

	@Override
	protected void renderBeforeSharedLayers(ChamberThemeRenderContext context) {
		renderApotheosWallMembrane(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosWallFrames(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosCeilingTendrils(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosCeilingMass(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosCeilingOutwardTendrils(context.poseStack(), context.tesselator(), context.time(),
				context.skyDistance());
		renderApotheosCeilingOrbsAndGlow(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosWallTopRim(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance());
	}

	private static void renderApotheosWallMembrane(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosWallMembrane(time * APOTHEOS_FLOOR_SHADER_TIME_SCALE,
				311.0F, APOTHEOS_WALL_FIBER_SCALE, APOTHEOS_WALL_TRACE_INTENSITY,
				APOTHEOS_WALL_RED_GLOW_INTENSITY, APOTHEOS_WALL_CEILING_FADE_START,
				APOTHEOS_WALL_CEILING_FADE_END);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosWallCylinderMesh(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosWallFrames(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.APOTHEOS_WALL_FRAME;
		VertexConsumer consumer = buffer.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		for (int rib = 0; rib < APOTHEOS_WALL_FRAME_RIBS; rib++) {
			float angleT = rib / (float) APOTHEOS_WALL_FRAME_RIBS;
			emitApotheosWallRib(consumer, matrix, skyDistance, time, angleT, rib % 3 == 0, rib * 17.31F);
		}
		for (int ribbon = 0; ribbon < APOTHEOS_WALL_WEB_RIBBONS; ribbon++) {
			float startAngleT = (ribbon * 0.071F + 0.034F) % 1.0F;
			float endAngleT = startAngleT + 0.035F + apotheosWallHash(ribbon * 3.17F) * 0.052F;
			float startHeightT = APOTHEOS_WALL_WEB_MIN_HEIGHT_T + apotheosWallHash(ribbon * 7.09F) * 0.30F;
			float endHeightT = Mth.clamp(startHeightT + (apotheosWallHash(ribbon * 11.43F) - 0.5F) * 0.22F,
					APOTHEOS_WALL_WEB_MIN_HEIGHT_T, 0.76F);
			emitApotheosWallWebRibbon(consumer, matrix, skyDistance, startAngleT, endAngleT, startHeightT,
					endHeightT, ribbon * 5.61F);
		}
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosCeilingMass(PoseStack poseStack, float time, float skyDistance) {
		renderApotheosCeilingAtmosphere(poseStack, time, skyDistance);
		renderApotheosCeilingCore(poseStack, time, skyDistance);
	}

	private static void renderApotheosCeilingCore(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosCeilingCore(time * APOTHEOS_CEILING_SHADER_TIME_SCALE,
				509.0F, APOTHEOS_CEILING_CORE_NOISE_SCALE, APOTHEOS_CEILING_CORE_ROTATION_SPEED,
				APOTHEOS_CEILING_YELLOW_GLOW_INTENSITY, APOTHEOS_CEILING_GREEN_ORB_INTENSITY,
				APOTHEOS_CEILING_CORE_UNDULATION_INTENSITY);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		poseStack.pushPose();
		float coreCenterY = apotheosCeilingCoreCenterY(skyDistance);
		poseStack.translate(0.0F, coreCenterY, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(time * APOTHEOS_CEILING_CORE_BODY_YAW_SPEED));
		poseStack.mulPose(Axis.XP.rotationDegrees(time * APOTHEOS_CEILING_CORE_BODY_PITCH_SPEED));
		poseStack.mulPose(Axis.ZP.rotationDegrees(time * APOTHEOS_CEILING_CORE_BODY_ROLL_SPEED));
		poseStack.translate(0.0F, -coreCenterY, 0.0F);
		emitApotheosCeilingCoreMesh(consumer, poseStack.last().pose(), skyDistance);
		poseStack.popPose();
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosCeilingAtmosphere(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosCeilingAtmosphere(time * APOTHEOS_CEILING_SHADER_TIME_SCALE,
				613.0F, APOTHEOS_CEILING_ATMOSPHERE_NOISE_SCALE, APOTHEOS_CEILING_ATMOSPHERE_ROTATION_SPEED,
				APOTHEOS_CEILING_ATMOSPHERE_STORM_INTENSITY, APOTHEOS_CEILING_ATMOSPHERE_OPACITY);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosCeilingAtmosphereMesh(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosCeilingTendrils(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.APOTHEOS_CEILING_PRIMITIVES;
		VertexConsumer consumer = buffer.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		for (int tendril = 0; tendril < APOTHEOS_CEILING_TENDRIL_COUNT; tendril++) {
			emitApotheosCeilingTendril(consumer, matrix, skyDistance, time, tendril);
		}
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosCeilingOutwardTendrils(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		Matrix4f matrix = poseStack.last().pose();
		for (int tendril = 0; tendril < APOTHEOS_CEILING_OUTWARD_TENDRIL_COUNT; tendril++) {
			emitApotheosCeilingOutwardTendril(buffer, matrix, skyDistance, time, tendril);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosCeilingOrbsAndGlow(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.APOTHEOS_CEILING_PRIMITIVES;
		VertexConsumer consumer = buffer.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		for (int orb = 0; orb < APOTHEOS_CEILING_ORB_COUNT; orb++) {
			emitApotheosCeilingOrb(consumer, matrix, skyDistance, time, orb);
		}
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosWallTopRim(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosWallTopRim(time * APOTHEOS_WALL_TOP_RIM_SHADER_TIME_SCALE,
				719.0F, APOTHEOS_WALL_TOP_RIM_PULSE_SPEED, APOTHEOS_WALL_TOP_RIM_CORE_INTENSITY,
				APOTHEOS_WALL_TOP_RIM_GLOW_INTENSITY);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosWallTopRim(consumer, poseStack.last().pose(), skyDistance, time);
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	static void renderApotheosFloorFunnel(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosFloorFunnel(time * APOTHEOS_FLOOR_SHADER_TIME_SCALE,
				83.0F, APOTHEOS_FLOOR_RING_RISE, APOTHEOS_FLOOR_RING_SPEED, APOTHEOS_FLOOR_MEAT_NOISE_SCALE,
				APOTHEOS_FLOOR_HIGHLIGHT_INTENSITY, APOTHEOS_FLOOR_CENTER_VOID_RADIUS);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosFunnelMesh(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);
		renderApotheosPortalHaze(buffer, poseStack.last().pose(), time, skyDistance);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosPortalGlow(PoseStack poseStack, float time, float skyDistance) {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosPortalGlow(time * APOTHEOS_FLOOR_SHADER_TIME_SCALE,
				227.0F, APOTHEOS_PORTAL_GLOW_INTENSITY, APOTHEOS_PORTAL_GLOW_RADIUS,
				APOTHEOS_FLOOR_CENTER_VOID_RADIUS);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosFunnelMesh(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void renderApotheosPortalHaze(MultiBufferSource.BufferSource buffer, Matrix4f matrix, float time,
			float skyDistance) {
		RenderType renderType = HemoRenderTypes.apotheosPortalHaze(time * APOTHEOS_FLOOR_SHADER_TIME_SCALE,
				191.0F, APOTHEOS_PORTAL_HAZE_SPEED, APOTHEOS_PORTAL_HAZE_INTENSITY,
				APOTHEOS_FLOOR_CENTER_VOID_RADIUS);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosFunnelMesh(consumer, matrix, skyDistance);
		buffer.endBatch(renderType);
	}

	private static void emitApotheosWallCylinderMesh(VertexConsumer consumer, Matrix4f matrix, float skyDistance) {
		for (int vertical = 0; vertical < APOTHEOS_WALL_VERTICAL_SEGMENTS; vertical++) {
			float h0 = vertical / (float) APOTHEOS_WALL_VERTICAL_SEGMENTS;
			float h1 = (vertical + 1) / (float) APOTHEOS_WALL_VERTICAL_SEGMENTS;
			for (int ring = 0; ring < APOTHEOS_WALL_RING_SEGMENTS; ring++) {
				float a0 = ring / (float) APOTHEOS_WALL_RING_SEGMENTS;
				float a1 = (ring + 1) / (float) APOTHEOS_WALL_RING_SEGMENTS;
				addApotheosWallVertex(consumer, matrix, skyDistance, a0, h0);
				addApotheosWallVertex(consumer, matrix, skyDistance, a1, h0);
				addApotheosWallVertex(consumer, matrix, skyDistance, a1, h1);
				addApotheosWallVertex(consumer, matrix, skyDistance, a0, h1);
			}
		}
	}

	private static void addApotheosWallVertex(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float angleT, float heightT) {
		float angle = angleT * Mth.TWO_PI;
		float lowWall = 1.0F - smoothstep(0.12F, 0.46F, heightT);
		float radiusRipple = Mth.sin(angle * 7.0F + heightT * 12.0F) * 0.006F
				+ Mth.sin(angle * 3.0F - heightT * 17.0F) * 0.004F;
		float radius = skyDistance * (APOTHEOS_WALL_RADIUS_SCALE - lowWall * 0.010F + radiusRipple);
		float x = Mth.cos(angle) * radius;
		float y = apotheosWallY(skyDistance, heightT);
		float z = Mth.sin(angle) * radius;
		float alpha = (194.0F + lowWall * 42.0F) * apotheosWallCeilingFade(heightT);
		consumer.addVertex(matrix, x, y, z).setUv(angleT, heightT)
				.setColor(255, 255, 255, (int) Mth.clamp(alpha, 0.0F, 242.0F));
	}

	private static void emitApotheosWallRib(VertexConsumer consumer, Matrix4f matrix, float skyDistance, float time,
			float angleT, boolean majorRib, float phase) {
		int segments = 20;
		for (int segment = 0; segment < segments; segment++) {
			float h0 = segment / (float) segments;
			float h1 = (segment + 1) / (float) segments;
			float wobble0 = Mth.sin(h0 * 8.0F + phase + time * 0.045F) * 0.0048F;
			float wobble1 = Mth.sin(h1 * 8.0F + phase + time * 0.045F) * 0.0048F;
			float width0 = apotheosWallRibWidth(skyDistance, h0, majorRib, phase);
			float width1 = apotheosWallRibWidth(skyDistance, h1, majorRib, phase);
			float a0 = (angleT + wobble0) * Mth.TWO_PI;
			float a1 = (angleT + wobble1) * Mth.TWO_PI;
			float r0 = skyDistance * (APOTHEOS_WALL_RADIUS_SCALE - 0.018F + (majorRib ? 0.006F : 0.0F));
			float r1 = skyDistance * (APOTHEOS_WALL_RADIUS_SCALE - 0.018F + (majorRib ? 0.006F : 0.0F));
			float x0 = Mth.cos(a0) * r0;
			float y0 = apotheosWallY(skyDistance, h0);
			float z0 = Mth.sin(a0) * r0;
			float x1 = Mth.cos(a1) * r1;
			float y1 = apotheosWallY(skyDistance, h1);
			float z1 = Mth.sin(a1) * r1;
			float tangentX0 = -Mth.sin(a0);
			float tangentZ0 = Mth.cos(a0);
			float tangentX1 = -Mth.sin(a1);
			float tangentZ1 = Mth.cos(a1);
			int red0 = apotheosWallRibRed(h0, majorRib);
			int red1 = apotheosWallRibRed(h1, majorRib);
			int green = majorRib ? 48 : 35;
			int blue = majorRib ? 72 : 62;
			int alpha0 = apotheosWallRibAlpha(h0, majorRib);
			int alpha1 = apotheosWallRibAlpha(h1, majorRib);

			addApotheosWallFrameVertex(consumer, matrix, x0 - tangentX0 * width0, y0, z0 - tangentZ0 * width0,
					red0, green, blue, alpha0);
			addApotheosWallFrameVertex(consumer, matrix, x1 - tangentX1 * width1, y1, z1 - tangentZ1 * width1,
					red1, green, blue, alpha1);
			addApotheosWallFrameVertex(consumer, matrix, x1 + tangentX1 * width1, y1, z1 + tangentZ1 * width1,
					red1, green, blue, alpha1);
			addApotheosWallFrameVertex(consumer, matrix, x0 + tangentX0 * width0, y0, z0 + tangentZ0 * width0,
					red0, green, blue, alpha0);
		}
	}

	private static void emitApotheosWallWebRibbon(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float startAngleT, float endAngleT, float startHeightT, float endHeightT, float phase) {
		for (int segment = 0; segment < APOTHEOS_WALL_WEB_RIBBON_SEGMENTS; segment++) {
			float t0 = segment / (float) APOTHEOS_WALL_WEB_RIBBON_SEGMENTS;
			float t1 = (segment + 1) / (float) APOTHEOS_WALL_WEB_RIBBON_SEGMENTS;
			float angleT0 = apotheosWallWebArcAngle(startAngleT, endAngleT, t0, phase);
			float angleT1 = apotheosWallWebArcAngle(startAngleT, endAngleT, t1, phase);
			float heightT0 = apotheosWallWebArcHeight(startHeightT, endHeightT, t0, phase);
			float heightT1 = apotheosWallWebArcHeight(startHeightT, endHeightT, t1, phase);
			emitApotheosWallWebRibbonSpan(consumer, matrix, skyDistance, angleT0, angleT1, heightT0, heightT1);
		}
	}

	private static void emitApotheosWallWebRibbonSpan(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float startAngleT, float endAngleT, float startHeightT, float endHeightT) {
		float startAngle = startAngleT * Mth.TWO_PI;
		float endAngle = endAngleT * Mth.TWO_PI;
		float radius = skyDistance * (APOTHEOS_WALL_RADIUS_SCALE - 0.026F);
		float x0 = Mth.cos(startAngle) * radius;
		float y0 = apotheosWallY(skyDistance, startHeightT);
		float z0 = Mth.sin(startAngle) * radius;
		float x1 = Mth.cos(endAngle) * radius;
		float y1 = apotheosWallY(skyDistance, endHeightT);
		float z1 = Mth.sin(endAngle) * radius;
		float dx = x1 - x0;
		float dz = z1 - z0;
		float horizontalLength = Mth.sqrt(dx * dx + dz * dz);
		if (horizontalLength < 0.001F) {
			return;
		}
		float width = skyDistance * 0.0026F;
		float offsetX = -dz / horizontalLength * width;
		float offsetZ = dx / horizontalLength * width;
		float heightMid = (startHeightT + endHeightT) * 0.5F;
		int alpha = (int) (118.0F * apotheosWallCeilingFade(heightMid)
				* (1.0F - smoothstep(0.60F, 0.82F, heightMid)));
		int red = (int) Mth.lerp(1.0F - smoothstep(0.10F, 0.42F, heightMid), 132.0F, 190.0F);
		int green = 158;
		int blue = 196;
		addApotheosWallFrameVertex(consumer, matrix, x0 - offsetX, y0, z0 - offsetZ, red, green, blue, alpha);
		addApotheosWallFrameVertex(consumer, matrix, x1 - offsetX, y1, z1 - offsetZ, red, green, blue, alpha);
		addApotheosWallFrameVertex(consumer, matrix, x1 + offsetX, y1, z1 + offsetZ, red, green, blue, alpha);
		addApotheosWallFrameVertex(consumer, matrix, x0 + offsetX, y0, z0 + offsetZ, red, green, blue, alpha);
	}

	private static float apotheosWallWebArcAngle(float startAngleT, float endAngleT, float t, float phase) {
		float sideBow = Mth.sin(t * Mth.TWO_PI * 0.5F) * (apotheosWallHash(phase + 4.31F) - 0.5F) * 0.034F;
		float tremor = Mth.sin(t * Mth.TWO_PI * 2.0F + phase * 0.71F) * 0.0045F;
		return Mth.lerp(t, startAngleT, endAngleT) + sideBow + tremor;
	}

	private static float apotheosWallWebArcHeight(float startHeightT, float endHeightT, float t, float phase) {
		return Mth.clamp(Mth.lerp(t, startHeightT, endHeightT) + apotheosWallWebArcBend(t, phase),
				APOTHEOS_WALL_WEB_MIN_HEIGHT_T, 0.80F);
	}

	private static float apotheosWallWebArcBend(float t, float phase) {
		float arc = Mth.sin(t * Mth.TWO_PI * 0.5F) * (0.038F + apotheosWallHash(phase + 1.9F) * 0.034F);
		float wobble = Mth.sin(t * Mth.TWO_PI * 2.0F + phase) * 0.012F;
		float direction = apotheosWallHash(phase + 8.73F) < 0.5F ? -1.0F : 1.0F;
		return arc * direction + wobble;
	}

	private static float apotheosWallRibWidth(float skyDistance, float heightT, boolean majorRib, float phase) {
		float baseWidth = majorRib ? 0.017F : 0.010F;
		float lowerRoot = 1.0F - smoothstep(0.10F, 0.48F, heightT);
		float taper = 1.0F - heightT * 0.32F;
		float organicWaver = 0.86F + Mth.sin(heightT * 11.0F + phase) * 0.14F;
		return skyDistance * baseWidth * taper * organicWaver * (1.0F + lowerRoot * 0.22F);
	}

	private static int apotheosWallRibRed(float heightT, boolean majorRib) {
		float lowRed = 1.0F - smoothstep(0.08F, 0.44F, heightT);
		return (int) Mth.lerp(lowRed, majorRib ? 42.0F : 28.0F, majorRib ? 136.0F : 92.0F);
	}

	private static int apotheosWallRibAlpha(float heightT, boolean majorRib) {
		float ceilingFade = apotheosWallCeilingFade(heightT);
		float lowerStrength = 0.82F + (1.0F - smoothstep(0.12F, 0.54F, heightT)) * 0.18F;
		float alpha = (majorRib ? 224.0F : 166.0F) * ceilingFade * lowerStrength;
		return (int) Mth.clamp(alpha, 0.0F, 238.0F);
	}

	private static void addApotheosWallFrameVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, int red, int green, int blue, int alpha) {
		consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
	}

	private static void emitApotheosCeilingCoreMesh(VertexConsumer consumer, Matrix4f matrix, float skyDistance) {
		for (int radial = 0; radial < APOTHEOS_CEILING_RADIAL_SEGMENTS; radial++) {
			float t0 = radial / (float) APOTHEOS_CEILING_RADIAL_SEGMENTS;
			float t1 = (radial + 1) / (float) APOTHEOS_CEILING_RADIAL_SEGMENTS;
			for (int ring = 0; ring < APOTHEOS_CEILING_RING_SEGMENTS; ring++) {
				float a0 = ring / (float) APOTHEOS_CEILING_RING_SEGMENTS;
				float a1 = (ring + 1) / (float) APOTHEOS_CEILING_RING_SEGMENTS;
				addApotheosCeilingCoreVertex(consumer, matrix, skyDistance, a0, t0);
				addApotheosCeilingCoreVertex(consumer, matrix, skyDistance, a1, t0);
				addApotheosCeilingCoreVertex(consumer, matrix, skyDistance, a1, t1);
				addApotheosCeilingCoreVertex(consumer, matrix, skyDistance, a0, t1);
			}
		}
	}

	private static void emitApotheosCeilingAtmosphereMesh(VertexConsumer consumer, Matrix4f matrix,
			float skyDistance) {
		for (int radial = 0; radial < APOTHEOS_CEILING_RADIAL_SEGMENTS; radial++) {
			float t0 = radial / (float) APOTHEOS_CEILING_RADIAL_SEGMENTS;
			float t1 = (radial + 1) / (float) APOTHEOS_CEILING_RADIAL_SEGMENTS;
			for (int ring = 0; ring < APOTHEOS_CEILING_RING_SEGMENTS; ring++) {
				float a0 = ring / (float) APOTHEOS_CEILING_RING_SEGMENTS;
				float a1 = (ring + 1) / (float) APOTHEOS_CEILING_RING_SEGMENTS;
				addApotheosCeilingAtmosphereVertex(consumer, matrix, skyDistance, a0, t0);
				addApotheosCeilingAtmosphereVertex(consumer, matrix, skyDistance, a1, t0);
				addApotheosCeilingAtmosphereVertex(consumer, matrix, skyDistance, a1, t1);
				addApotheosCeilingAtmosphereVertex(consumer, matrix, skyDistance, a0, t1);
			}
		}
	}

	private static void addApotheosCeilingCoreVertex(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float angleT, float radialT) {
		float stableAngleT = radialT <= APOTHEOS_CEILING_CORE_CENTER_RADIAL_EPSILON
				? APOTHEOS_CEILING_CORE_CENTER_ANGLE_T
				: angleT;
		float angle = stableAngleT * Mth.TWO_PI;
		float radius = apotheosCeilingCoreRadius(skyDistance, radialT)
				+ apotheosCeilingCoreInnerIrregularity(skyDistance, stableAngleT, radialT);
		float x = Mth.cos(angle) * radius;
		float y = apotheosCeilingCoreY(skyDistance, radialT, stableAngleT)
				+ apotheosCeilingCoreInnerRidge(skyDistance, stableAngleT, radialT);
		float z = Mth.sin(angle) * radius;
		float edgePresence = 1.0F - smoothstep(0.92F, 1.0F, radialT) * 0.28F;
		float alpha = Mth.clamp(214.0F + radialT * 28.0F, 0.0F, 246.0F) * edgePresence;
		consumer.addVertex(matrix, x, y, z).setUv(stableAngleT, radialT)
				.setColor(255, 255, 255, (int) Mth.clamp(alpha, 0.0F, 248.0F));
	}

	private static void addApotheosCeilingAtmosphereVertex(VertexConsumer consumer, Matrix4f matrix,
			float skyDistance, float angleT, float radialT) {
		float stableAngleT = radialT <= APOTHEOS_CEILING_CORE_CENTER_RADIAL_EPSILON
				? APOTHEOS_CEILING_CORE_CENTER_ANGLE_T
				: angleT;
		float angle = stableAngleT * Mth.TWO_PI;
		float radius = apotheosCeilingAtmosphereRadius(skyDistance, radialT);
		float x = Mth.cos(angle) * radius;
		float y = apotheosCeilingAtmosphereY(skyDistance, radialT, stableAngleT);
		float z = Mth.sin(angle) * radius;
		float shellPresence = smoothstep(0.04F, 0.22F, radialT) * (1.0F - smoothstep(0.94F, 1.0F, radialT));
		float alpha = Mth.clamp(255.0F * shellPresence, 0.0F, 255.0F);
		consumer.addVertex(matrix, x, y, z).setUv(stableAngleT, radialT)
				.setColor(255, 255, 255, (int) alpha);
	}

	private static void emitApotheosCeilingTendril(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float time, int tendril) {
		float phase = tendril * 23.17F;
		float rootAngleT = apotheosWrap01(tendril / (float) APOTHEOS_CEILING_TENDRIL_COUNT
				+ (apotheosWallHash(phase) - 0.5F) * 0.060F);
		float rootRadialT = 0.76F + apotheosWallHash(phase + 1.73F) * 0.22F;
		float endRadialT = Mth.clamp(rootRadialT - 0.22F - apotheosWallHash(phase + 4.19F) * 0.38F, 0.16F,
				0.78F);
		float drift = (apotheosWallHash(phase + 7.31F) - 0.5F) * 0.18F;
		float hang = 0.030F + apotheosWallHash(phase + 12.67F) * 0.110F;
		boolean whiteTendril = tendril % 5 == 0 || apotheosWallHash(phase + 3.43F) > 0.78F;
		for (int segment = 0; segment < APOTHEOS_CEILING_TENDRIL_SEGMENTS; segment++) {
			float t0 = segment / (float) APOTHEOS_CEILING_TENDRIL_SEGMENTS;
			float t1 = (segment + 1) / (float) APOTHEOS_CEILING_TENDRIL_SEGMENTS;
			float angleT0 = apotheosCeilingTendrilAngle(rootAngleT, drift, phase, time, t0);
			float angleT1 = apotheosCeilingTendrilAngle(rootAngleT, drift, phase, time, t1);
			float radialT0 = Mth.lerp(t0, rootRadialT, endRadialT);
			float radialT1 = Mth.lerp(t1, rootRadialT, endRadialT);
			float x0 = apotheosCeilingCoreX(skyDistance, angleT0, radialT0);
			float y0 = apotheosCeilingCoreY(skyDistance, radialT0, angleT0) - skyDistance * hang * t0
					- skyDistance * 0.014F * Mth.sin(t0 * Mth.TWO_PI + phase + time * 0.030F);
			float z0 = apotheosCeilingCoreZ(skyDistance, angleT0, radialT0);
			float x1 = apotheosCeilingCoreX(skyDistance, angleT1, radialT1);
			float y1 = apotheosCeilingCoreY(skyDistance, radialT1, angleT1) - skyDistance * hang * t1
					- skyDistance * 0.014F * Mth.sin(t1 * Mth.TWO_PI + phase + time * 0.030F);
			float z1 = apotheosCeilingCoreZ(skyDistance, angleT1, radialT1);
			float width = skyDistance * (0.0028F + apotheosWallHash(phase + 9.7F) * 0.0046F)
					* (1.0F - t0 * 0.46F);
			int alpha = (int) Mth.clamp((whiteTendril ? 132.0F : 178.0F) * (1.0F - t0 * 0.38F), 34.0F,
					220.0F);
			int red = whiteTendril ? 230 : 255;
			int green = whiteTendril ? 230 : 218;
			int blue = whiteTendril ? 226 : 46;
			emitApotheosCeilingTendrilSpan(consumer, matrix, x0, y0, z0, x1, y1, z1, width, red, green, blue,
					alpha);
		}
	}

	private static float apotheosCeilingTendrilAngle(float rootAngleT, float drift, float phase, float time, float t) {
		float swing = Mth.sin(t * Mth.TWO_PI * 1.35F + phase + time * 0.026F) * 0.018F;
		float coil = Mth.sin(t * Mth.TWO_PI * 3.0F + phase * 0.31F) * 0.006F;
		return apotheosWrap01(rootAngleT + drift * t * 0.34F + swing + coil);
	}

	private static void emitApotheosCeilingTendrilSpan(VertexConsumer consumer, Matrix4f matrix, float x0, float y0,
			float z0, float x1, float y1, float z1, float width, int red, int green, int blue, int alpha) {
		float dx = x1 - x0;
		float dz = z1 - z0;
		float horizontalLength = Mth.sqrt(dx * dx + dz * dz);
		if (horizontalLength < 0.001F) {
			return;
		}
		float offsetX = -dz / horizontalLength * width;
		float offsetZ = dx / horizontalLength * width;
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x0 - offsetX, y0, z0 - offsetZ, red, green, blue,
				alpha);
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x1 - offsetX, y1, z1 - offsetZ, red, green, blue,
				alpha);
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x1 + offsetX, y1, z1 + offsetZ, red, green, blue,
				alpha);
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x0 + offsetX, y0, z0 + offsetZ, red, green, blue,
				alpha);
	}

	private static void emitApotheosCeilingOutwardTendril(BufferBuilder buffer, Matrix4f matrix, float skyDistance,
			float time, int tendril) {
		float phase = tendril * 41.91F;
		float rootAngleT = apotheosWrap01(tendril / (float) APOTHEOS_CEILING_OUTWARD_TENDRIL_COUNT
				+ (apotheosWallHash(phase + 2.0F) - 0.5F) * 0.040F);
		float rootRadialT = APOTHEOS_CEILING_OUTWARD_TENDRIL_ROOT_RADIAL_T
				+ apotheosWallHash(phase + 5.0F) * 0.10F;
		float drift = (apotheosWallHash(phase + 7.0F) - 0.5F) * 0.22F;
		float lengthMultiplier = apotheosCeilingOutwardTendrilLengthMultiplier(phase);
		boolean whiteTendril = apotheosWallHash(phase + 11.0F) < APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RATIO;
		Vec3 sourceAtMass = apotheosCeilingOutwardTendrilPoint(skyDistance, rootAngleT, rootRadialT, drift, phase,
				time, 0.0F);
		Vec3 forward = new Vec3(sourceAtMass.x * 0.56D, sourceAtMass.y * 0.12D, sourceAtMass.z * 0.56D).normalize();
		Vec3 radialOut = new Vec3(sourceAtMass.x, 0.0D, sourceAtMass.z);
		if (radialOut.lengthSqr() < 0.0001D) {
			radialOut = new Vec3(1.0D, 0.0D, 0.0D);
		} else {
			radialOut = radialOut.normalize();
		}
		Vec3 tangent = new Vec3(-forward.z, 0.0D, forward.x);
		if (tangent.lengthSqr() < 0.0001D) {
			tangent = new Vec3(1.0D, 0.0D, 0.0D);
		} else {
			tangent = tangent.normalize();
		}
		double pull = skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_CAMERA_PULL_SCALE * lengthMultiplier;
		double radialPush = skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_RADIAL_PUSH_SCALE * lengthMultiplier;
		double verticalSag = skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_VERTICAL_SAG_SCALE
				* (0.35D + apotheosWallHash(phase + 13.0F) * 0.65D);
		double curl = Math.sin(phase) * skyDistance * 0.055D;
		Vec3 targetTowardPlayer = sourceAtMass.subtract(forward.scale(pull))
				.add(radialOut.scale(radialPush))
				.add(tangent.scale(curl))
				.add(0.0D, -verticalSag, 0.0D);
		long tendrilSeed = apotheosCeilingOutwardTendrilSeed(tendril, whiteTendril);
		TendrilEffectConfig config = APOTHEOS_CEILING_OUTWARD_TENDRIL_CONFIG
				.withColors(apotheosCeilingOutwardTendrilCoreColor(whiteTendril),
						apotheosCeilingOutwardTendrilGlowColor(whiteTendril))
				.withFixedSeed(true, tendrilSeed);
		TendrilGeometry geometry = TendrilGeometry.generate(sourceAtMass, targetTowardPlayer, config, tendrilSeed,
				time * APOTHEOS_CEILING_OUTWARD_TENDRIL_GEOMETRY_TIME_SCALE,
				TendrilGeometry.SurfaceResolver.NONE);
		emitApotheosCeilingOutwardTendrilTube(buffer, matrix, geometry, whiteTendril, skyDistance, time,
				tendrilSeed);
	}

	private static Vec3 apotheosCeilingOutwardTendrilPoint(float skyDistance, float rootAngleT, float rootRadialT,
			float drift, float phase, float time, float t) {
		float angleT = apotheosCeilingOutwardTendrilAngle(rootAngleT, drift, phase, time, t);
		float radialT = rootRadialT;
		return apotheosCeilingCorePoint(skyDistance, angleT, radialT,
				apotheosCeilingOutwardTendrilY(skyDistance, angleT, radialT, phase, time, t),
				0.0F);
	}

	private static float apotheosCeilingOutwardTendrilLengthMultiplier(float phase) {
		float longTailBias = Mth.square(apotheosWallHash(phase + 17.0F));
		float lengthT = Mth.lerp(0.36F, apotheosWallHash(phase + 19.0F), longTailBias);
		return Mth.lerp(lengthT, APOTHEOS_CEILING_OUTWARD_TENDRIL_MIN_LENGTH_MULTIPLIER,
				APOTHEOS_CEILING_OUTWARD_TENDRIL_MAX_LENGTH_MULTIPLIER);
	}

	private static long apotheosCeilingOutwardTendrilSeed(int tendril, boolean whiteTendril) {
		long seed = whiteTendril ? 0x61C8864680B583EBL : 0x9E3779B97F4A7C15L;
		seed ^= (long) tendril * 0xD6E8FEB86659FD93L;
		return seed;
	}

	private static int apotheosCeilingOutwardTendrilCoreColor(boolean whiteTendril) {
		int alpha = (int) (whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_ALPHA
				: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_ALPHA);
		return apotheosArgb(alpha,
				whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RED
						: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_RED,
				whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_GREEN
						: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_GREEN,
				whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_BLUE
						: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_BLUE);
	}

	private static int apotheosCeilingOutwardTendrilGlowColor(boolean whiteTendril) {
		return whiteTendril ? apotheosArgb(98, 236, 238, 234) : apotheosArgb(132, 255, 226, 76);
	}

	private static int apotheosArgb(int alpha, int red, int green, int blue) {
		return (Mth.clamp(alpha, 0, 255) << 24) | (Mth.clamp(red, 0, 255) << 16)
				| (Mth.clamp(green, 0, 255) << 8) | Mth.clamp(blue, 0, 255);
	}

	private static void emitApotheosCeilingOutwardTendrilTube(BufferBuilder buffer, Matrix4f matrix,
			TendrilGeometry geometry, boolean whiteTendril, float skyDistance, float time, long tendrilSeed) {
		int coreAlpha = (int) (whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_ALPHA
				: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_ALPHA);
		int glowAlpha = whiteTendril ? 92 : 118;
		int coreRed = whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_RED
				: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_RED;
		int coreGreen = whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_GREEN
				: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_GREEN;
		int coreBlue = whiteTendril ? APOTHEOS_CEILING_OUTWARD_TENDRIL_WHITE_BLUE
				: APOTHEOS_CEILING_OUTWARD_TENDRIL_YELLOW_BLUE;
		int glowRed = whiteTendril ? 236 : 255;
		int glowGreen = whiteTendril ? 238 : 226;
		int glowBlue = whiteTendril ? 234 : 76;
		float baseWidth = APOTHEOS_CEILING_OUTWARD_TENDRIL_CONFIG.baseWidth();
		float coreWidthScale = skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_WIDTH_SCALE
				* APOTHEOS_CEILING_OUTWARD_TENDRIL_CORE_WIDTH_MULTIPLIER / baseWidth;
		float glowWidthScale = skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_WIDTH_SCALE
				* APOTHEOS_CEILING_OUTWARD_TENDRIL_GLOW_WIDTH_MULTIPLIER / baseWidth;
		int strandIndex = 0;
		for (TendrilGeometry.Strand strand : geometry.strands()) {
			TendrilGeometry.Strand wriggledStrand = apotheosCeilingOutwardWriggledStrand(strand, time, skyDistance,
					tendrilSeed, strandIndex++);
			TendrilGeometry.TubeQuads glowQuads = TendrilGeometry.createTubeQuads(wriggledStrand, glowWidthScale);
			for (Vec3 vertex : glowQuads.vertices()) {
				buffer.addVertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z)
						.setColor(glowRed, glowGreen, glowBlue, glowAlpha);
			}
			TendrilGeometry.TubeQuads coreQuads = TendrilGeometry.createTubeQuads(wriggledStrand, coreWidthScale);
			for (Vec3 vertex : coreQuads.vertices()) {
				buffer.addVertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z)
						.setColor(coreRed, coreGreen, coreBlue, coreAlpha);
			}
		}
	}

	private static TendrilGeometry.Strand apotheosCeilingOutwardWriggledStrand(TendrilGeometry.Strand strand,
			float time, float skyDistance, long tendrilSeed, int strandIndex) {
		float strandPhase = (float) (((tendrilSeed & 0xFFFFL) * 0.0017D) + strandIndex * 1.91D
				+ strand.depth() * 0.73D + (strand.branch() ? 0.47D : 0.0D));
		List<TendrilGeometry.Ring> wriggledRings = new ArrayList<>(strand.rings().size());
		for (TendrilGeometry.Ring ring : strand.rings()) {
			Vec3 offset = apotheosCeilingOutwardWriggleOffset(ring, time, skyDistance, strandPhase);
			wriggledRings.add(new TendrilGeometry.Ring(ring.center().add(offset), ring.width(), ring.right(),
					ring.up(), ring.progress()));
		}
		return new TendrilGeometry.Strand(List.copyOf(wriggledRings), strand.branch(), strand.depth());
	}

	private static Vec3 apotheosCeilingOutwardWriggleOffset(TendrilGeometry.Ring ring, float time,
			float skyDistance, float strandPhase) {
		float bodyMask = Mth.sin(ring.progress() * (float) Math.PI);
		double animatedTime = time * APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_TIME_SCALE;
		double segmentPhase = ring.progress() * APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_FREQUENCY;
		double wriggleScale = skyDistance * APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENT_WRIGGLE_SCALE * bodyMask;
		double lateralWave = Math.sin(animatedTime + strandPhase + segmentPhase) * wriggleScale;
		double crossWave = Math.cos(animatedTime * 1.37D + strandPhase * 0.71D + segmentPhase * 1.43D)
				* wriggleScale * 0.56D;
		double fineWave = Math.sin(animatedTime * 2.18D - strandPhase + segmentPhase * 1.91D) * wriggleScale
				* 0.24D;
		return ring.right().scale(lateralWave + fineWave).add(ring.up().scale(crossWave));
	}

	private static float apotheosCeilingOutwardTendrilAngle(float rootAngleT, float drift, float phase, float time,
			float t) {
		float writhe = Mth.sin(time * 0.041F + phase + t * Mth.TWO_PI * 2.1F) * 0.022F;
		float curl = Mth.sin(time * 0.023F + phase * 0.37F + t * Mth.TWO_PI * 4.0F) * 0.010F;
		return apotheosWrap01(rootAngleT + drift * t * 0.42F + writhe + curl);
	}

	private static float apotheosCeilingOutwardTendrilY(float skyDistance, float angleT, float radialT, float phase,
			float time, float t) {
		float surfaceY = apotheosCeilingCoreY(skyDistance, Math.min(radialT, 1.0F), angleT);
		float outwardSag = skyDistance * (0.025F + t * t * 0.115F);
		float livingWobble = skyDistance * 0.010F * Mth.sin(time * 0.033F + phase + t * Mth.TWO_PI * 1.6F);
		return surfaceY - outwardSag + livingWobble;
	}

	private static void emitApotheosCeilingOrb(VertexConsumer consumer, Matrix4f matrix, float skyDistance, float time,
			int orb) {
		float phase = orb * 31.47F;
		float drift = Mth.sin(time * 0.018F + phase) * 0.014F;
		float angleT = apotheosWrap01(orb / (float) APOTHEOS_CEILING_ORB_COUNT
				+ (apotheosWallHash(phase + 1.1F) - 0.5F) * 0.085F + drift);
		float radialT = 0.24F + apotheosWallHash(phase + 5.9F) * 0.56F;
		float x = apotheosCeilingCoreX(skyDistance, angleT, radialT);
		float y = apotheosCeilingCoreY(skyDistance, radialT, angleT)
				- skyDistance * (0.018F + apotheosWallHash(phase + 9.0F) * 0.055F);
		float z = apotheosCeilingCoreZ(skyDistance, angleT, radialT);
		float pulse = 0.82F + Mth.sin(time * 0.090F + phase) * 0.18F;
		float glowSize = skyDistance * (0.020F + apotheosWallHash(phase + 2.4F) * 0.024F) * pulse;
		boolean greenOrb = orb % 3 == 0 || apotheosWallHash(phase + 7.4F) > 0.74F;
		int coreRed = greenOrb ? 188 : 255;
		int coreGreen = greenOrb ? 255 : 213;
		int coreBlue = greenOrb ? 54 : 36;
		emitApotheosCeilingOrbQuad(consumer, matrix, x, y, z, angleT, glowSize * 2.8F, coreRed, coreGreen,
				coreBlue, 46);
		emitApotheosCeilingOrbQuad(consumer, matrix, x, y - skyDistance * 0.002F, z, angleT + 0.125F,
				glowSize * 1.2F, coreRed, coreGreen, coreBlue, 112);
		emitApotheosCeilingOrbQuad(consumer, matrix, x, y - skyDistance * 0.004F, z, angleT + 0.25F,
				glowSize * 0.44F, 255, 246, greenOrb ? 108 : 68, 206);
	}

	private static void emitApotheosCeilingOrbQuad(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, float angleT, float halfSize, int red, int green, int blue, int alpha) {
		float angle = angleT * Mth.TWO_PI;
		float axisX = Mth.cos(angle);
		float axisZ = Mth.sin(angle);
		float tangentX = -axisZ;
		float tangentZ = axisX;
		float ax = axisX * halfSize;
		float az = axisZ * halfSize;
		float tx = tangentX * halfSize;
		float tz = tangentZ * halfSize;
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x - ax - tx, y, z - az - tz, red, green, blue,
				alpha);
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x + ax - tx, y, z + az - tz, red, green, blue,
				alpha);
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x + ax + tx, y, z + az + tz, red, green, blue,
				alpha);
		addApotheosCeilingPrimitiveVertex(consumer, matrix, x - ax + tx, y, z - az + tz, red, green, blue,
				alpha);
	}

	private static void emitApotheosWallTopRim(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float time) {
		emitApotheosWallTopRimBand(consumer, matrix, skyDistance, time, APOTHEOS_WALL_TOP_RIM_GLOW_WIDTH_SCALE,
				APOTHEOS_WALL_TOP_RIM_GLOW_RED, APOTHEOS_WALL_TOP_RIM_GLOW_GREEN, APOTHEOS_WALL_TOP_RIM_GLOW_BLUE,
				72);
		emitApotheosWallTopRimBand(consumer, matrix, skyDistance, time, APOTHEOS_WALL_TOP_RIM_CORE_WIDTH_SCALE,
				APOTHEOS_WALL_TOP_RIM_CORE_RED, APOTHEOS_WALL_TOP_RIM_CORE_GREEN, APOTHEOS_WALL_TOP_RIM_CORE_BLUE,
				218);
	}

	private static void emitApotheosWallTopRimBand(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float time, float widthScale, int red, int green, int blue, int alpha) {
		float baseRadius = skyDistance * APOTHEOS_WALL_TOP_RIM_RADIUS_SCALE;
		float innerRadius = baseRadius - skyDistance * widthScale;
		float outerRadius = baseRadius + skyDistance * widthScale;
		for (int ring = 0; ring < APOTHEOS_WALL_RING_SEGMENTS ; ring++) {
			float a0 = ring / (float) APOTHEOS_WALL_RING_SEGMENTS;
			float a1 = (ring + 1) / (float) APOTHEOS_WALL_RING_SEGMENTS;
			float angle0 = a0 * Mth.TWO_PI;
			float angle1 = a1 * Mth.TWO_PI;
			float y0 = apotheosWallTopRimY(skyDistance)
					+ skyDistance * 0.0026F * Mth.sin(angle0 * 8.0F + time * 0.052F);
			float y1 = apotheosWallTopRimY(skyDistance)
					+ skyDistance * 0.0026F * Mth.sin(angle1 * 8.0F + time * 0.052F);
			addApotheosWallTopRimVertex(consumer, matrix, Mth.cos(angle0) * innerRadius, y0,
					Mth.sin(angle0) * innerRadius, a0, 0.0F, red, green, blue, alpha);
			addApotheosWallTopRimVertex(consumer, matrix, Mth.cos(angle1) * innerRadius, y1,
					Mth.sin(angle1) * innerRadius, a1, 0.0F, red, green, blue, alpha);
			addApotheosWallTopRimVertex(consumer, matrix, Mth.cos(angle1) * outerRadius, y1,
					Mth.sin(angle1) * outerRadius, a1, 1.0F, red, green, blue, alpha);
			addApotheosWallTopRimVertex(consumer, matrix, Mth.cos(angle0) * outerRadius, y0,
					Mth.sin(angle0) * outerRadius, a0, 1.0F, red, green, blue, alpha);
		}
	}

	private static float apotheosWallTopRimY(float skyDistance) {
		return apotheosWallY(skyDistance, APOTHEOS_WALL_TOP_RIM_HEIGHT_T)
				+ skyDistance * APOTHEOS_WALL_TOP_RIM_Y_OFFSET_SCALE;
	}

	private static void addApotheosCeilingPrimitiveVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, int red, int green, int blue, int alpha) {
		consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
	}

	private static void addApotheosWallTopRimVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, float angleT, float widthT, int red, int green, int blue, int alpha) {
		consumer.addVertex(matrix, x, y, z).setUv(angleT, widthT).setColor(red, green, blue, alpha);
	}

	private static Vec3 apotheosCeilingCorePoint(float skyDistance, float angleT, float radialT, float y,
			float yOffsetScale) {
		return new Vec3(apotheosCeilingCoreX(skyDistance, angleT, radialT), y + skyDistance * yOffsetScale,
				apotheosCeilingCoreZ(skyDistance, angleT, radialT));
	}

	private static float apotheosCeilingCoreX(float skyDistance, float angleT, float radialT) {
		return Mth.cos(angleT * Mth.TWO_PI) * apotheosCeilingCoreRadius(skyDistance, radialT);
	}

	private static float apotheosCeilingCoreZ(float skyDistance, float angleT, float radialT) {
		return Mth.sin(angleT * Mth.TWO_PI) * apotheosCeilingCoreRadius(skyDistance, radialT);
	}

	private static float apotheosCeilingCoreRadius(float skyDistance, float radialT) {
		float shapedRadiusT = apotheosCeilingPlanetoidRadiusT(radialT);
		return skyDistance * Mth.lerp(shapedRadiusT, 0.035F,
				APOTHEOS_CEILING_DOME_SPAN_SCALE * APOTHEOS_CEILING_CORE_RADIUS_SCALE);
	}

	private static float apotheosCeilingCoreCenterY(float skyDistance) {
		return apotheosCeilingCoreY(skyDistance, 0.5F, APOTHEOS_CEILING_CORE_CENTER_ANGLE_T);
	}

	private static float apotheosCeilingAtmosphereRadius(float skyDistance, float radialT) {
		float shapedRadiusT = apotheosCeilingPlanetoidRadiusT(radialT);
		return skyDistance * Mth.lerp(shapedRadiusT, 0.045F,
				APOTHEOS_CEILING_DOME_SPAN_SCALE * APOTHEOS_CEILING_ATMOSPHERE_RADIUS_SCALE);
	}

	private static float apotheosCeilingPlanetoidRadiusT(float radialT) {
		float smoothed = smoothstep(0.0F, 1.0F, radialT);
		return Mth.lerp(radialT, smoothed, 0.72F);
	}

	private static float apotheosCeilingCoreY(float skyDistance, float radialT, float angleT) {
		float planetoidDepthT = 1.0F - Mth.square(1.0F - radialT);
		float centerDrop = (float) Math.pow(1.0F - planetoidDepthT, 1.20F) * APOTHEOS_CEILING_DROP_SCALE
				* skyDistance;
		float edgeRise = Mth.square(radialT) * skyDistance * 0.030F;
		float centerRippleMask = smoothstep(0.24F, 0.68F, radialT);
		float ripple = Mth.sin(angleT * Mth.TWO_PI * 6.0F + radialT * 12.0F) * skyDistance * 0.0045F
				* centerRippleMask;
		return skyDistance * APOTHEOS_CEILING_Y_SCALE - centerDrop + edgeRise + ripple;
	}

	private static float apotheosCeilingCoreInnerIrregularity(float skyDistance, float angleT, float radialT) {
		float angle = angleT * Mth.TWO_PI;
		float mask = apotheosCeilingCoreInnerIrregularityMask(radialT);
		float cellularSurface = Mth.sin(angle * 8.0F + radialT * 19.0F)
				+ Mth.sin(angle * 13.0F - radialT * 27.0F) * 0.52F
				+ Mth.sin(angle * 21.0F + radialT * 8.0F) * 0.28F;
		return skyDistance * APOTHEOS_CEILING_CORE_INNER_IRREGULARITY_SCALE * mask * cellularSurface;
	}

	private static float apotheosCeilingCoreInnerRidge(float skyDistance, float angleT, float radialT) {
		float angle = angleT * Mth.TWO_PI;
		float mask = apotheosCeilingCoreInnerIrregularityMask(radialT);
		float foldedRidge = Mth.sin(angle * 6.0F - radialT * 22.0F)
				* Mth.sin(angle * 15.0F + radialT * 31.0F);
		float fineRidge = Mth.sin(angle * 28.0F + radialT * 17.0F) * 0.34F;
		return skyDistance * APOTHEOS_CEILING_CORE_INNER_RIDGE_SCALE * mask * (foldedRidge + fineRidge);
	}

	private static float apotheosCeilingCoreInnerIrregularityMask(float radialT) {
		return smoothstep(0.05F, 0.20F, radialT) * (1.0F - smoothstep(0.58F, 0.93F, radialT));
	}

	private static float apotheosCeilingAtmosphereY(float skyDistance, float radialT, float angleT) {
		float coreY = apotheosCeilingCoreY(skyDistance, radialT, angleT);
		float shellLift = skyDistance * (0.045F + smoothstep(0.12F, 0.86F, radialT) * 0.045F);
		float stormSag = skyDistance * APOTHEOS_CEILING_ATMOSPHERE_Y_OFFSET_SCALE;
		return coreY + shellLift + stormSag;
	}

	private static float apotheosWallY(float skyDistance, float heightT) {
		return skyDistance * Mth.lerp(heightT, APOTHEOS_WALL_BOTTOM_Y_SCALE, APOTHEOS_WALL_TOP_Y_SCALE);
	}

	private static float apotheosWallCeilingFade(float heightT) {
		return 1.0F - smoothstep(APOTHEOS_WALL_CEILING_FADE_START, APOTHEOS_WALL_CEILING_FADE_END, heightT);
	}

	private static float smoothstep(float edge0, float edge1, float value) {
		float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}

	private static float apotheosWallHash(float value) {
		double hashed = Math.sin(value * 12.9898D + 78.233D) * 43758.5453D;
		return (float) (hashed - Math.floor(hashed));
	}

	private static float apotheosWrap01(float value) {
		return value - Mth.floor(value);
	}

	private static void emitApotheosFunnelMesh(VertexConsumer consumer, Matrix4f matrix, float skyDistance) {
		for (int radial = 0; radial < APOTHEOS_FLOOR_RADIAL_SEGMENTS; radial++) {
			float t0 = radial / (float) APOTHEOS_FLOOR_RADIAL_SEGMENTS;
			float t1 = (radial + 1) / (float) APOTHEOS_FLOOR_RADIAL_SEGMENTS;
			for (int ring = 0; ring < APOTHEOS_FLOOR_RING_SEGMENTS; ring++) {
				float a0 = ring / (float) APOTHEOS_FLOOR_RING_SEGMENTS;
				float a1 = (ring + 1) / (float) APOTHEOS_FLOOR_RING_SEGMENTS;
				addApotheosFunnelVertex(consumer, matrix, skyDistance, a0, t0);
				addApotheosFunnelVertex(consumer, matrix, skyDistance, a0, t1);
				addApotheosFunnelVertex(consumer, matrix, skyDistance, a1, t1);
				addApotheosFunnelVertex(consumer, matrix, skyDistance, a1, t0);
			}
		}
	}

	private static void addApotheosFunnelVertex(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float angleT, float radialT) {
		float shapedRadiusT = Mth.sqrt(radialT);
		float radiusScale = Mth.lerp(shapedRadiusT, APOTHEOS_FLOOR_INNER_RADIUS_SCALE,
				APOTHEOS_FLOOR_OUTER_RADIUS_SCALE);
		float angle = angleT * Mth.TWO_PI;
		float radius = skyDistance * radiusScale;
		float x = Mth.cos(angle) * radius;
		float z = Mth.sin(angle) * radius;
		float centerDrop = (float) Math.pow(1.0F - radialT, 1.72F) * APOTHEOS_FLOOR_DROP_SCALE * skyDistance;
		float outerRise = Mth.square(radialT) * APOTHEOS_FLOOR_RISE_SCALE * skyDistance;
		float ringRelief = Mth.sin(radialT * Mth.TWO_PI * 18.0F) * skyDistance * 0.0038F
				* Mth.lerp(radialT, 0.35F, 1.0F);
		float y = skyDistance * APOTHEOS_FLOOR_Y_SCALE - centerDrop + outerRise + ringRelief;
		float alpha = Mth.clamp(Mth.lerp(radialT, 184.0F, 238.0F), 0.0F, 255.0F);
		consumer.addVertex(matrix, x, y, z).setUv(angleT, radialT).setColor(255, 255, 255, (int) alpha);
	}
}

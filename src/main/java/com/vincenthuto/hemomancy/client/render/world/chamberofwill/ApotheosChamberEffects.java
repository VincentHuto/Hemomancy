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
	private static final float APOTHEOS_WALL_TOP_Y_SCALE = 1.86F;
	private static final float APOTHEOS_WALL_FIBER_SCALE = 2.4F;
	private static final float APOTHEOS_WALL_TRACE_INTENSITY = 0.68F;
	private static final float APOTHEOS_WALL_RED_GLOW_INTENSITY = 0.95F;
	private static final float APOTHEOS_WALL_CEILING_FADE_START = 0.68F;
	private static final float APOTHEOS_WALL_CEILING_FADE_END = 0.92F;

	private static final int APOTHEOS_WALL_WEB_RIBBONS = 18;
	private static final int APOTHEOS_WALL_WEB_RIBBON_SEGMENTS = 22;
	private static final float APOTHEOS_WALL_WEB_MIN_HEIGHT_T = 0.46F;

	private static final float APOTHEOS_PORTAL_GLOW_RADIUS = 0.42F;
	private static final float APOTHEOS_PORTAL_GLOW_INTENSITY = 1.35F;
	private static final float APOTHEOS_PORTAL_HAZE_SPEED = 0.58F;
	private static final float APOTHEOS_PORTAL_HAZE_INTENSITY = 1.18F;

	// ========== CEILING (descending overhead mass + spanning canopy membrane) ==========
	private static final float APOTHEOS_CEILING_SHADER_TIME_SCALE = 0.062F;
	private static final float APOTHEOS_CEILING_FIBER_SCALE = 2.6F;
	private static final float APOTHEOS_CEILING_TRACE_INTENSITY = 0.60F;
	private static final float APOTHEOS_CEILING_RED_GLOW_INTENSITY = 1.12F;
	private static final float APOTHEOS_CEILING_RIM_FADE_START = 0.84F;
	private static final float APOTHEOS_CEILING_RIM_FADE_END = 0.98F;

	// Canopy: a fibrous dome spanning from the descending mass out to the wall rim.
	private static final int APOTHEOS_CANOPY_RING_SEGMENTS = 96;
	private static final int APOTHEOS_CANOPY_RADIAL_SEGMENTS = 30;
	private static final float APOTHEOS_CANOPY_INNER_RADIUS_SCALE = 0.090F;
	private static final float APOTHEOS_CANOPY_OUTER_RADIUS_SCALE = 0.66F;
	private static final float APOTHEOS_CANOPY_APEX_Y_SCALE = 2.02F;
	private static final float APOTHEOS_CANOPY_RIM_Y_SCALE = 1.56F;
	private static final float APOTHEOS_CANOPY_INNER_RADIAL_T = 0.14F;
	private static final float APOTHEOS_CANOPY_MASS_MOUTH_DIP = 0.10F;

	// Descending mass: a bulbous vascular bloom hanging from the canopy apex into the room.
	private static final int APOTHEOS_MASS_RING_SEGMENTS = 64;
	private static final int APOTHEOS_MASS_VERTICAL_SEGMENTS = 28;
	private static final float APOTHEOS_MASS_TOP_Y_SCALE = 2.00F;
	private static final float APOTHEOS_MASS_TIP_Y_SCALE = 0.62F;
	private static final float APOTHEOS_MASS_TOP_RADIUS_SCALE = 0.090F;
	private static final float APOTHEOS_MASS_TIP_RADIUS_SCALE = 0.014F;
	private static final float APOTHEOS_MASS_MAX_RADIUS_SCALE = 0.235F;
	private static final float APOTHEOS_MASS_RED_GLOW_INTENSITY = 1.55F;

	// Red glow nodes and hanging drip tendrils studding the overhead mass.
	private static final int APOTHEOS_CEILING_NODE_COUNT = 30;
	private static final int APOTHEOS_CEILING_TENDRIL_COUNT = 22;
	private static final int APOTHEOS_CEILING_TENDRIL_SEGMENTS = 14;

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
		renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosCeilingCanopy(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosCeilingMass(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosCeilingGrowths(context.poseStack(), context.time(), context.skyDistance());
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
		RenderType renderType = HemoRenderTypes.QLIPHOTH_GLOW;
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

	static void renderApotheosCeilingCanopy(PoseStack poseStack, float time, float skyDistance) {
		beginApotheosOverheadPass();
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosCeilingMembrane(
				time * APOTHEOS_CEILING_SHADER_TIME_SCALE, 137.0F, APOTHEOS_CEILING_FIBER_SCALE,
				APOTHEOS_CEILING_TRACE_INTENSITY, APOTHEOS_CEILING_RED_GLOW_INTENSITY, 1.0F,
				APOTHEOS_CEILING_RIM_FADE_START, APOTHEOS_CEILING_RIM_FADE_END);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosCanopyMesh(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);
		endApotheosOverheadPass();
	}

	static void renderApotheosCeilingMass(PoseStack poseStack, float time, float skyDistance) {
		beginApotheosOverheadPass();
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.apotheosCeilingMembrane(
				time * APOTHEOS_CEILING_SHADER_TIME_SCALE, 263.0F, APOTHEOS_CEILING_FIBER_SCALE * 1.15F,
				APOTHEOS_CEILING_TRACE_INTENSITY * 0.85F, APOTHEOS_MASS_RED_GLOW_INTENSITY, 1.0F,
				APOTHEOS_CEILING_RIM_FADE_START, APOTHEOS_CEILING_RIM_FADE_END);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		emitApotheosMassMesh(consumer, poseStack.last().pose(), skyDistance);
		buffer.endBatch(renderType);
		endApotheosOverheadPass();
	}

	static void renderApotheosCeilingGrowths(PoseStack poseStack, float time, float skyDistance) {
		beginApotheosOverheadPass();
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.QLIPHOTH_GLOW;
		VertexConsumer consumer = buffer.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		for (int node = 0; node < APOTHEOS_CEILING_NODE_COUNT; node++) {
			emitApotheosCeilingNode(consumer, matrix, skyDistance, time, node);
		}
		for (int tendril = 0; tendril < APOTHEOS_CEILING_TENDRIL_COUNT; tendril++) {
			emitApotheosCeilingTendril(consumer, matrix, skyDistance, time, tendril);
		}
		buffer.endBatch(renderType);
		endApotheosOverheadPass();
	}

	private static void beginApotheosOverheadPass() {
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static void endApotheosOverheadPass() {
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private static void emitApotheosCanopyMesh(VertexConsumer consumer, Matrix4f matrix, float skyDistance) {
		for (int radial = 0; radial < APOTHEOS_CANOPY_RADIAL_SEGMENTS; radial++) {
			float t0 = radial / (float) APOTHEOS_CANOPY_RADIAL_SEGMENTS;
			float t1 = (radial + 1) / (float) APOTHEOS_CANOPY_RADIAL_SEGMENTS;
			for (int ring = 0; ring < APOTHEOS_CANOPY_RING_SEGMENTS; ring++) {
				float a0 = ring / (float) APOTHEOS_CANOPY_RING_SEGMENTS;
				float a1 = (ring + 1) / (float) APOTHEOS_CANOPY_RING_SEGMENTS;
				addApotheosCanopyVertex(consumer, matrix, skyDistance, a0, t0);
				addApotheosCanopyVertex(consumer, matrix, skyDistance, a0, t1);
				addApotheosCanopyVertex(consumer, matrix, skyDistance, a1, t1);
				addApotheosCanopyVertex(consumer, matrix, skyDistance, a1, t0);
			}
		}
	}

	private static void addApotheosCanopyVertex(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float angleT, float stepT) {
		float shapedRadiusT = (float) Math.pow(stepT, 0.85F);
		float radiusScale = Mth.lerp(shapedRadiusT, APOTHEOS_CANOPY_INNER_RADIUS_SCALE,
				APOTHEOS_CANOPY_OUTER_RADIUS_SCALE);
		float angle = angleT * Mth.TWO_PI;
		float relief = Mth.sin(stepT * Mth.TWO_PI * 9.0F + angle * 3.0F) * 0.006F * Mth.lerp(stepT, 0.4F, 1.0F);
		float radius = skyDistance * (radiusScale + relief);
		float x = Mth.cos(angle) * radius;
		float z = Mth.sin(angle) * radius;
		float domeDrop = (float) Math.pow(stepT, 0.85F);
		float mouthDip = APOTHEOS_CANOPY_MASS_MOUTH_DIP * (1.0F - smoothstep(0.0F, 0.22F, stepT));
		float y = skyDistance * (Mth.lerp(domeDrop, APOTHEOS_CANOPY_APEX_Y_SCALE, APOTHEOS_CANOPY_RIM_Y_SCALE)
				- mouthDip);
		float radialT = Mth.lerp(stepT, APOTHEOS_CANOPY_INNER_RADIAL_T, 1.0F);
		float alpha = Mth.clamp(Mth.lerp(stepT, 240.0F, 226.0F), 0.0F, 255.0F);
		consumer.addVertex(matrix, x, y, z).setUv(angleT, radialT).setColor(255, 255, 255, (int) alpha);
	}

	private static void emitApotheosMassMesh(VertexConsumer consumer, Matrix4f matrix, float skyDistance) {
		for (int vertical = 0; vertical < APOTHEOS_MASS_VERTICAL_SEGMENTS; vertical++) {
			float v0 = vertical / (float) APOTHEOS_MASS_VERTICAL_SEGMENTS;
			float v1 = (vertical + 1) / (float) APOTHEOS_MASS_VERTICAL_SEGMENTS;
			for (int ring = 0; ring < APOTHEOS_MASS_RING_SEGMENTS; ring++) {
				float a0 = ring / (float) APOTHEOS_MASS_RING_SEGMENTS;
				float a1 = (ring + 1) / (float) APOTHEOS_MASS_RING_SEGMENTS;
				addApotheosMassVertex(consumer, matrix, skyDistance, a0, v0);
				addApotheosMassVertex(consumer, matrix, skyDistance, a1, v0);
				addApotheosMassVertex(consumer, matrix, skyDistance, a1, v1);
				addApotheosMassVertex(consumer, matrix, skyDistance, a0, v1);
			}
		}
	}

	private static void addApotheosMassVertex(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float angleT, float verticalT) {
		float angle = angleT * Mth.TWO_PI;
		float radius = skyDistance * apotheosMassRadiusScale(verticalT, angle);
		float x = Mth.cos(angle) * radius;
		float z = Mth.sin(angle) * radius;
		float y = skyDistance * Mth.lerp(verticalT, APOTHEOS_MASS_TIP_Y_SCALE, APOTHEOS_MASS_TOP_Y_SCALE);
		float radialT = verticalT * APOTHEOS_CANOPY_INNER_RADIAL_T;
		float alpha = Mth.clamp(Mth.lerp(verticalT, 246.0F, 232.0F), 0.0F, 255.0F);
		consumer.addVertex(matrix, x, y, z).setUv(angleT, radialT).setColor(255, 255, 255, (int) alpha);
	}

	private static float apotheosMassRadiusScale(float verticalT, float angle) {
		float bulge = (float) Math.pow(Math.max(0.0F, Mth.sin(verticalT * Mth.PI)), 0.90F);
		float innerRadius = Mth.lerp(smoothstep(0.0F, 1.0F, verticalT), APOTHEOS_MASS_TIP_RADIUS_SCALE,
				APOTHEOS_MASS_TOP_RADIUS_SCALE);
		float lobeWaver = 0.90F + Mth.sin(angle * 5.0F + verticalT * 7.0F) * 0.10F;
		return Mth.lerp(bulge, innerRadius, APOTHEOS_MASS_MAX_RADIUS_SCALE) * lobeWaver;
	}

	private static void emitApotheosCeilingNode(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float time, int index) {
		float angleT = apotheosWallHash(index * 4.13F);
		float verticalT = 0.14F + apotheosWallHash(index * 9.71F) * 0.78F;
		float angle = angleT * Mth.TWO_PI;
		float radius = skyDistance * (apotheosMassRadiusScale(verticalT, angle) + 0.006F);
		float x = Mth.cos(angle) * radius;
		float z = Mth.sin(angle) * radius;
		float y = skyDistance * Mth.lerp(verticalT, APOTHEOS_MASS_TIP_Y_SCALE, APOTHEOS_MASS_TOP_Y_SCALE);
		float flare = 0.72F + 0.28F * Mth.sin(time * 0.05F + index * 1.7F);
		float size = skyDistance * (0.010F + apotheosWallHash(index * 2.53F) * 0.012F);
		int red = 255;
		int green = (int) (26.0F + verticalT * 34.0F);
		int blue = (int) (30.0F + verticalT * 26.0F);
		int alpha = (int) Mth.clamp((150.0F + (1.0F - verticalT) * 90.0F) * flare, 0.0F, 235.0F);
		addApotheosCeilingQuad(consumer, matrix, x, y, z, size, red, green, blue, alpha);
	}

	private static void emitApotheosCeilingTendril(VertexConsumer consumer, Matrix4f matrix, float skyDistance,
			float time, int index) {
		float angleT = (index * 0.093F + apotheosWallHash(index * 5.9F) * 0.05F) % 1.0F;
		float angle = angleT * Mth.TWO_PI;
		float rootRadiusScale = Mth.lerp(apotheosWallHash(index * 3.31F), APOTHEOS_CANOPY_INNER_RADIUS_SCALE + 0.02F,
				APOTHEOS_MASS_MAX_RADIUS_SCALE + 0.10F);
		float dropScale = 0.42F + apotheosWallHash(index * 8.17F) * 0.55F;
		float topY = APOTHEOS_CANOPY_APEX_Y_SCALE - 0.10F;
		float width = skyDistance * (0.0022F + apotheosWallHash(index * 6.4F) * 0.0022F);
		float phase = index * 12.9F;
		for (int segment = 0; segment < APOTHEOS_CEILING_TENDRIL_SEGMENTS; segment++) {
			float s0 = segment / (float) APOTHEOS_CEILING_TENDRIL_SEGMENTS;
			float s1 = (segment + 1) / (float) APOTHEOS_CEILING_TENDRIL_SEGMENTS;
			float[] p0 = apotheosTendrilPoint(skyDistance, time, angle, rootRadiusScale, topY, dropScale, phase, s0);
			float[] p1 = apotheosTendrilPoint(skyDistance, time, angle, rootRadiusScale, topY, dropScale, phase, s1);
			float taper0 = width * (1.0F - s0 * 0.7F);
			float taper1 = width * (1.0F - s1 * 0.7F);
			int red = 200;
			int green = 24;
			int blue = 30;
			int alpha0 = (int) Mth.clamp(Mth.lerp(s0, 150.0F, 210.0F), 0.0F, 220.0F);
			int alpha1 = (int) Mth.clamp(Mth.lerp(s1, 150.0F, 210.0F), 0.0F, 220.0F);
			addApotheosCeilingFrameVertex(consumer, matrix, p0[0] - taper0, p0[1], p0[2], red, green, blue, alpha0);
			addApotheosCeilingFrameVertex(consumer, matrix, p1[0] - taper1, p1[1], p1[2], red, green, blue, alpha1);
			addApotheosCeilingFrameVertex(consumer, matrix, p1[0] + taper1, p1[1], p1[2], red, green, blue, alpha1);
			addApotheosCeilingFrameVertex(consumer, matrix, p0[0] + taper0, p0[1], p0[2], red, green, blue, alpha0);
		}
	}

	private static float[] apotheosTendrilPoint(float skyDistance, float time, float angle, float rootRadiusScale,
			float topY, float dropScale, float phase, float s) {
		float sway = Mth.sin(s * 5.0F + phase + time * 0.03F) * 0.012F * s;
		float radiusScale = rootRadiusScale + sway;
		float radius = skyDistance * radiusScale;
		float x = Mth.cos(angle) * radius;
		float z = Mth.sin(angle) * radius;
		float y = skyDistance * (topY - dropScale * s);
		return new float[] { x, y, z };
	}

	private static void addApotheosCeilingQuad(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float size, int red, int green, int blue, int alpha) {
		addApotheosCeilingFrameVertex(consumer, matrix, x - size, y, z - size, red, green, blue, alpha);
		addApotheosCeilingFrameVertex(consumer, matrix, x + size, y, z - size, red, green, blue, alpha);
		addApotheosCeilingFrameVertex(consumer, matrix, x + size, y, z + size, red, green, blue, alpha);
		addApotheosCeilingFrameVertex(consumer, matrix, x - size, y, z + size, red, green, blue, alpha);
	}

	private static void addApotheosCeilingFrameVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
			float z, int red, int green, int blue, int alpha) {
		consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
	}
}

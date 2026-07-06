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
		renderApotheosPortalGlow(context.poseStack(), context.time(), context.skyDistance());
		renderApotheosFloorFunnel(context.poseStack(), context.time(), context.skyDistance());
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

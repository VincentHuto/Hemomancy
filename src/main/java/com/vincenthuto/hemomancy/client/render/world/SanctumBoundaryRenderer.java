package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.data.SanctumBoundaryClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.event.worldevent.SanctumBoundaryRelation;
import com.vincenthuto.hemomancy.common.event.worldevent.SanctumBoundaryVisibilityRules;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

public final class SanctumBoundaryRenderer {
	private static final int DOME_SEGMENTS = 80;
	private static final int DOME_RINGS = 16;
	private static final float HOSTILE_DOME_ALPHA = 0.82F;
	private static final float RIVAL_DOME_ALPHA = 0.30F;
	private static final float HOSTILE_DOME_GROUND_OVERRUN = 1.5F;
	private static final float MEMBER_SHIMMER_ALPHA = 0.10F;
	private static final float FADE_STEP = 0.08F;

	private static TextureTarget frameCopyTarget;
	private static float insideAlpha;
	private static SanctumBoundaryRelation insideRelation = SanctumBoundaryRelation.MEMBER;

	private SanctumBoundaryRenderer() {
	}

	public static void renderWorldMask(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (!sanctumBoundaryRendererEnabled() || mc.level == null || mc.player == null) {
			return;
		}

		List<SanctumBoundaryClientData.Entry> entries = SanctumBoundaryClientData.entries();
		if (entries.isEmpty()) {
			return;
		}

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		float time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		renderHostileWorldDomes(poseStack, entries, buffer, time, cam);
		for (SanctumBoundaryClientData.Entry entry : entries) {
			if (entry.relation() == SanctumBoundaryRelation.MEMBER) {
				drawMemberShimmer(poseStack, cam, entry, time);
			}
		}
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}

	public static void renderPost(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (!sanctumBoundaryRendererEnabled() || mc.level == null || mc.player == null || screenWidth <= 0
				|| screenHeight <= 0) {
			insideAlpha = 0.0F;
			return;
		}

		SanctumBoundaryRelation targetRelation = strongestContainingRelation(mc.player);
		float targetAlpha = targetRelation == SanctumBoundaryRelation.MEMBER ? 0.0F : 1.0F;
		insideRelation = targetRelation;
		insideAlpha = approach(insideAlpha, targetAlpha, FADE_STEP);

		if (!requiresPostPass(targetAlpha)) {
			return;
		}
		copyMainRenderTarget(mc);
		if (frameCopyTarget == null) {
			return;
		}

		float time = mc.level.getGameTime() + partialTick;
		if (insideAlpha > 0.001F) {
			if (insideRelation == SanctumBoundaryRelation.OUTSIDER) {
				renderFullWorldGrade(graphics, screenWidth, screenHeight, time, 0.88F * insideAlpha);
			} else if (insideRelation == SanctumBoundaryRelation.RIVAL_ELDER) {
				renderScreenFog(graphics, screenWidth, screenHeight, time, 0.42F * insideAlpha);
			}
		}
	}

	private static SanctumBoundaryRelation strongestContainingRelation(Player player) {
		SanctumBoundaryRelation strongest = SanctumBoundaryRelation.MEMBER;
		for (SanctumBoundaryClientData.Entry entry : SanctumBoundaryClientData.entries()) {
			if (contains(player.position(), entry.center(), entry.radius())) {
				strongest = SanctumBoundaryVisibilityRules.strongerInsideEffect(strongest, entry.relation());
			}
		}
		return strongest;
	}

	private static boolean sanctumBoundaryRendererEnabled() {
		return HemoClientConfig.RENDER_SANCTUM_BOUNDARY == null || HemoClientConfig.RENDER_SANCTUM_BOUNDARY.get();
	}

	private static boolean contains(Vec3 pos, BlockPos center, float radius) {
		double dx = pos.x - (center.getX() + 0.5D);
		double domeBaseY = center.getY() + 0.15D;
		double dy = Math.max(0.0D, pos.y - domeBaseY);
		double dz = pos.z - (center.getZ() + 0.5D);
		return dx * dx + dy * dy + dz * dz <= radius * radius;
	}

	private static void renderFullWorldGrade(GuiGraphics graphics, int screenWidth, int screenHeight,
			float time, float intensity) {
		ShaderInstance shader = ShaderInit.SANGUINE_OMEN_WORLD.getInstance().get();
		if (shader == null) {
			return;
		}
		setUniform(shader, "HemoTime", time);
		setUniform(shader, "Progress", 0.5F);
		setUniform(shader, "Intensity", intensity);
		setUniform(shader, "Seed", 91.0F);
		RenderSystem.setShaderTexture(0, frameCopyTarget.getColorTextureId());
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(ShaderInit.SANGUINE_OMEN_WORLD.getInstance());
		drawFullscreenQuad(graphics, screenWidth, screenHeight);
		RenderSystem.enableDepthTest();
	}

	private static void renderScreenFog(GuiGraphics graphics, int screenWidth, int screenHeight,
			float time, float intensity) {
		ShaderInstance shader = ShaderInit.SANGUINE_OMEN_SCREEN_OVERLAY.getInstance().get();
		if (shader == null) {
			return;
		}
		setUniform(shader, "HemoTime", time);
		setUniform(shader, "Progress", 0.5F);
		setUniform(shader, "Intensity", intensity);
		setUniform(shader, "Seed", 53.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(ShaderInit.SANGUINE_OMEN_SCREEN_OVERLAY.getInstance());
		drawFullscreenQuad(graphics, screenWidth, screenHeight);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void renderHostileWorldDomes(PoseStack poseStack, List<SanctumBoundaryClientData.Entry> entries,
			MultiBufferSource.BufferSource buffer, float time, Vec3 cam) {
		for (SanctumBoundaryClientData.Entry entry : entries) {
			if (entry.relation() == SanctumBoundaryRelation.MEMBER) {
				continue;
			}
			float seed = sanctumSeed(entry);
			float alpha = entry.relation() == SanctumBoundaryRelation.RIVAL_ELDER
					? RIVAL_DOME_ALPHA * rivalPulse(time + seed)
					: HOSTILE_DOME_ALPHA;
			drawHostileDomeShell(poseStack, buffer, cam, entry.center(), entry.radius(), time, seed, alpha, false);
			drawHostileDomeShell(poseStack, buffer, cam, entry.center(), entry.radius() * 1.025F, time,
					seed + 19.0F, alpha * 0.34F, true);
		}
	}

	private static void drawHostileDomeShell(PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 cam,
			BlockPos center, float radius, float time, float seed, float alpha, boolean glow) {
		double cx = center.getX() + 0.5D;
		double cy = center.getY() + 0.15D;
		double cz = center.getZ() + 0.5D;

		poseStack.pushPose();
		poseStack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = poseStack.last().pose();
		Vector3f shaderCenter = mat.transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
		RenderType coreType = !glow ? HemoRenderTypes.loomOrbShell(time, seed, shaderCenter.x(), shaderCenter.y(),
				shaderCenter.z(), radius, 0.12F, 9.0F, false) : null;
		RenderType glowType = glow ? HemoRenderTypes.loomOrbShell(time, seed, shaderCenter.x(), shaderCenter.y(),
				shaderCenter.z(), radius, 0.055F, 6.0F, true) : null;
		RenderType domeType = glow ? glowType : coreType;
		VertexConsumer vc = buffer.getBuffer(domeType);
		drawHostileDomeShell(vc, mat, radius, alpha, glow);
		if (glow) {
			buffer.endBatch(glowType);
		} else {
			buffer.endBatch(coreType);
		}
		poseStack.popPose();
	}

	private static void drawHostileDomeShell(VertexConsumer vc, Matrix4f mat, float radius, float alpha,
			boolean glow) {
		double thetaEnd = Math.min(Math.PI * 0.58D,
				Math.PI * 0.5D + HOSTILE_DOME_GROUND_OVERRUN / Math.max(1.0F, radius));
		for (int ring = 0; ring < DOME_RINGS; ring++) {
			double v0 = (double) ring / DOME_RINGS;
			double v1 = (double) (ring + 1) / DOME_RINGS;
			double theta0 = v0 * thetaEnd;
			double theta1 = v1 * thetaEnd;
			float y0 = (float) (Math.cos(theta0) * radius);
			float y1 = (float) (Math.cos(theta1) * radius);
			float r0 = (float) (Math.sin(theta0) * radius);
			float r1 = (float) (Math.sin(theta1) * radius);
			float edgeFade0 = hostileDomeFade(theta0, thetaEnd);
			float edgeFade1 = hostileDomeFade(theta1, thetaEnd);

			for (int seg = 0; seg < DOME_SEGMENTS; seg++) {
				double a0 = (Math.PI * 2.0D / DOME_SEGMENTS) * seg;
				double a1 = (Math.PI * 2.0D / DOME_SEGMENTS) * (seg + 1);
				float x00 = (float) Math.cos(a0) * r0;
				float z00 = (float) Math.sin(a0) * r0;
				float x01 = (float) Math.cos(a1) * r0;
				float z01 = (float) Math.sin(a1) * r0;
				float x10 = (float) Math.cos(a0) * r1;
				float z10 = (float) Math.sin(a0) * r1;
				float x11 = (float) Math.cos(a1) * r1;
				float z11 = (float) Math.sin(a1) * r1;

				if (glow) {
					emit(vc, mat, x00, y0, z00, 0.85F, 0.05F, 0.04F, alpha * edgeFade0 * 0.55F);
					emit(vc, mat, x10, y1, z10, 0.85F, 0.05F, 0.04F, alpha * edgeFade1);
					emit(vc, mat, x11, y1, z11, 0.85F, 0.05F, 0.04F, alpha * edgeFade1);
					emit(vc, mat, x01, y0, z01, 0.85F, 0.05F, 0.04F, alpha * edgeFade0 * 0.55F);
				} else {
					emit(vc, mat, x00, y0, z00, 0.04F, 0.0F, 0.0F, alpha * edgeFade0);
					emit(vc, mat, x10, y1, z10, 0.04F, 0.0F, 0.0F, alpha * edgeFade1);
					emit(vc, mat, x11, y1, z11, 0.04F, 0.0F, 0.0F, alpha * edgeFade1);
					emit(vc, mat, x01, y0, z01, 0.04F, 0.0F, 0.0F, alpha * edgeFade0);
				}
			}
		}
	}

	private static void drawMemberShimmer(PoseStack poseStack, Vec3 cam, SanctumBoundaryClientData.Entry entry,
			float time) {
		float alpha = MEMBER_SHIMMER_ALPHA * (0.70F + 0.30F * (float) Math.sin(time * 0.07F));
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		drawDome(poseStack, cam, entry.center(), entry.radius(), alpha);
	}

	private static void drawDome(PoseStack poseStack, Vec3 cam, BlockPos center, float radius, float alpha) {
		double cx = center.getX() + 0.5D;
		double cy = center.getY() + 0.15D;
		double cz = center.getZ() + 0.5D;

		poseStack.pushPose();
		poseStack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);

		for (int ring = 0; ring < DOME_RINGS; ring++) {
			double v0 = (double) ring / DOME_RINGS;
			double v1 = (double) (ring + 1) / DOME_RINGS;
			double theta0 = v0 * Math.PI * 0.5D;
			double theta1 = v1 * Math.PI * 0.5D;
			float y0 = (float) (Math.cos(theta0) * radius);
			float y1 = (float) (Math.cos(theta1) * radius);
			float r0 = (float) (Math.sin(theta0) * radius);
			float r1 = (float) (Math.sin(theta1) * radius);
			float edgeFade0 = memberDomeFade(theta0);
			float edgeFade1 = memberDomeFade(theta1);

			for (int seg = 0; seg < DOME_SEGMENTS; seg++) {
				double a0 = (Math.PI * 2.0D / DOME_SEGMENTS) * seg;
				double a1 = (Math.PI * 2.0D / DOME_SEGMENTS) * (seg + 1);
				float x00 = (float) Math.cos(a0) * r0;
				float z00 = (float) Math.sin(a0) * r0;
				float x01 = (float) Math.cos(a1) * r0;
				float z01 = (float) Math.sin(a1) * r0;
				float x10 = (float) Math.cos(a0) * r1;
				float z10 = (float) Math.sin(a0) * r1;
				float x11 = (float) Math.cos(a1) * r1;
				float z11 = (float) Math.sin(a1) * r1;

				emit(buffer, mat, x00, y0, z00, 0.95F, 0.22F, 0.13F, alpha * 0.18F * edgeFade0);
				emit(buffer, mat, x10, y1, z10, 0.95F, 0.22F, 0.13F, alpha * edgeFade1);
				emit(buffer, mat, x11, y1, z11, 0.95F, 0.22F, 0.13F, alpha * edgeFade1);
				emit(buffer, mat, x01, y0, z01, 0.95F, 0.22F, 0.13F, alpha * 0.18F * edgeFade0);
			}
		}

		BufferUploader.drawWithShader(buffer.buildOrThrow());
		poseStack.popPose();
	}

	private static float rivalPulse(float time) {
		return 0.58F + 0.42F * (float) ((Math.sin(time * 0.045F) + 1.0D) * 0.5D);
	}

	private static float sanctumSeed(SanctumBoundaryClientData.Entry entry) {
		BlockPos center = entry.center();
		return (float) (center.getX() * 0.13D + center.getY() * 0.07D + center.getZ() * 0.17D
				+ entry.radius() * 0.31D);
	}

	private static float memberDomeFade(double theta) {
		float horizon = (float) Math.sin(theta);
		return horizon * horizon;
	}

	private static float hostileDomeFade(double theta, double thetaEnd) {
		float horizon = (float) Math.sin(theta);
		float crown = (float) Math.sin(Math.min(1.0D, theta / Math.max(0.001D, thetaEnd)) * Math.PI * 0.5D);
		return Math.max(0.08F, horizon * horizon) * (0.35F + 0.65F * crown);
	}

	private static float approach(float current, float target, float step) {
		if (current < target) {
			return Math.min(target, current + step);
		}
		return Math.max(target, current - step);
	}

	private static void emit(BufferBuilder buffer, Matrix4f mat, float x, float y, float z,
			float r, float g, float b, float a) {
		buffer.addVertex(mat, x, y, z).setColor(r, g, b, a);
	}

	private static void emit(VertexConsumer vc, Matrix4f mat, float x, float y, float z,
			float r, float g, float b, float a) {
		vc.addVertex(mat, x, y, z).setColor(r, g, b, a);
	}

	private static void drawFullscreenQuad(GuiGraphics graphics, int screenWidth, int screenHeight) {
		Matrix4f matrix = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_TEX_COLOR);
		buffer.addVertex(matrix, 0.0F, screenHeight, -90.0F).setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.addVertex(matrix, screenWidth, screenHeight, -90.0F).setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.addVertex(matrix, screenWidth, 0.0F, -90.0F).setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.addVertex(matrix, 0.0F, 0.0F, -90.0F).setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	private static void setUniform(ShaderInstance shader, String name, float value) {
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(value);
		}
	}

	private static void setUniform(ShaderInstance shader, String name, float x, float y) {
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(x, y);
		}
	}

	private static boolean requiresPostPass(float targetAlpha) {
		return insideAlpha > 0.001F || targetAlpha > 0.0F;
	}

	private static void copyMainRenderTarget(Minecraft minecraft) {
		RenderTarget mainTarget = minecraft.getMainRenderTarget();
		if (mainTarget.width <= 0 || mainTarget.height <= 0) {
			return;
		}

		ensureFrameCopyTarget(mainTarget);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameCopyTarget.frameBufferId);
		GL30.glBlitFramebuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0, frameCopyTarget.width,
				frameCopyTarget.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
		mainTarget.bindWrite(false);
	}

	private static void ensureFrameCopyTarget(RenderTarget mainTarget) {
		if (frameCopyTarget != null && frameCopyTarget.width == mainTarget.width
				&& frameCopyTarget.height == mainTarget.height) {
			return;
		}
		if (frameCopyTarget != null) {
			frameCopyTarget.destroyBuffers();
		}
		frameCopyTarget = new TextureTarget(mainTarget.width, mainTarget.height, false, Minecraft.ON_OSX);
		frameCopyTarget.setFilterMode(GL11.GL_NEAREST);
	}
}

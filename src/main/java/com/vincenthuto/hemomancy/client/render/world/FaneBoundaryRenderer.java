package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryVisibilityRules;
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

public final class FaneBoundaryRenderer {
	private static final int DOME_SEGMENTS = 80;
	private static final int DOME_RINGS = 64;
	private static final double SPHERE_LATITUDE_END = Math.PI;
	private static final float HEART_RADIUS = 40.0F;
	private static final float HOSTILE_DOME_ALPHA = 0.90F;
	private static final float RIVAL_DOME_ALPHA = 0.45F;
	private static final float MUNDANE_OUTSIDER_DOME_ALPHA = 0.52F;
	private static final float MUNDANE_OUTSIDER_SHELL_RED = 0.32F;
	private static final float MUNDANE_OUTSIDER_SHELL_GREEN = 0.015F;
	private static final float MUNDANE_OUTSIDER_SHELL_BLUE = 0.01F;
	private static final float MUNDANE_OUTSIDER_SHELL_ALPHA_SCALE = 0.50F;
	private static final float HOSTILE_SHELL_ALPHA_SCALE = 0.70F;
	private static final float HOSTILE_GLOW_ALPHA_SCALE = 0.18F;
	private static final float MEMBER_SHIMMER_ALPHA = 0.18F;
	private static final float MEMBER_SHELL_ALPHA_SCALE = 0.42F;
	private static final float FADE_STEP = 0.08F;

	private static TextureTarget frameCopyTarget;
	private static float insideAlpha;
	private static FaneBoundaryRelation insideRelation = FaneBoundaryRelation.MEMBER;

	private FaneBoundaryRenderer() {
	}

	public static void renderWorldMask(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (!faneBoundaryRendererEnabled() || mc.level == null || mc.player == null) {
			return;
		}

		List<FaneBoundaryClientData.Entry> entries = FaneBoundaryClientData.entries();
		if (entries.isEmpty()) {
			return;
		}

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		float time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		renderHostileWorldDomes(poseStack, entries, buffer, time, cam);
		for (FaneBoundaryClientData.Entry entry : entries) {
			if (effectiveRelation(entry) == FaneBoundaryRelation.MEMBER) {
				drawMemberShimmer(poseStack, cam, entry, time);
			}
		}
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}

	public static void renderPost(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (!faneBoundaryRendererEnabled() || mc.level == null || mc.player == null || screenWidth <= 0
				|| screenHeight <= 0) {
			insideAlpha = 0.0F;
			return;
		}

		FaneBoundaryRelation targetRelation = strongestContainingRelation(mc.player);
		float targetAlpha = targetRelation == FaneBoundaryRelation.MEMBER
				|| targetRelation == FaneBoundaryRelation.MUNDANE_OUTSIDER ? 0.0F : 1.0F;
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
			if (insideRelation == FaneBoundaryRelation.OUTSIDER) {
				renderFullWorldGrade(graphics, screenWidth, screenHeight, time, 0.88F * insideAlpha);
			} else if (insideRelation == FaneBoundaryRelation.RIVAL_ELDER) {
				renderScreenFog(graphics, screenWidth, screenHeight, time, 0.42F * insideAlpha);
			}
		}
	}

	private static FaneBoundaryRelation strongestContainingRelation(Player player) {
		FaneBoundaryRelation strongest = FaneBoundaryRelation.MEMBER;
		for (FaneBoundaryClientData.Entry entry : FaneBoundaryClientData.entries()) {
			if (contains(player.position(), entry.heart(), HEART_RADIUS) || containsAnyStake(player.position(), entry)) {
				strongest = FaneBoundaryVisibilityRules.strongerInsideEffect(strongest, effectiveRelation(entry));
			}
		}
		return strongest;
	}

	private static boolean faneBoundaryRendererEnabled() {
		FaneBoundaryClientData.ViewMode viewMode = FaneBoundaryClientData.viewMode();
		return viewMode != FaneBoundaryClientData.ViewMode.HIDDEN
				&& (viewMode == FaneBoundaryClientData.ViewMode.MUNDANE
				|| viewMode == FaneBoundaryClientData.ViewMode.REVEALED
				|| HemoClientConfig.RENDER_FANE_BOUNDARY == null || HemoClientConfig.RENDER_FANE_BOUNDARY.get());
	}

	private static FaneBoundaryRelation effectiveRelation(FaneBoundaryClientData.Entry entry) {
		FaneBoundaryClientData.ViewMode viewMode = FaneBoundaryClientData.viewMode();
		if (viewMode == FaneBoundaryClientData.ViewMode.MUNDANE) {
			return FaneBoundaryRelation.MUNDANE_OUTSIDER;
		}
		return viewMode == FaneBoundaryClientData.ViewMode.REVEALED ? FaneBoundaryRelation.OUTSIDER : entry.relation();
	}

	private static boolean contains(Vec3 pos, BlockPos center, float radius) {
		double dx = pos.x - (center.getX() + 0.5D);
		double dy = pos.y - (center.getY() + 0.15D);
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

	private static void renderHostileWorldDomes(PoseStack poseStack, List<FaneBoundaryClientData.Entry> entries,
			MultiBufferSource.BufferSource buffer, float time, Vec3 cam) {
		for (FaneBoundaryClientData.Entry entry : entries) {
			FaneBoundaryRelation relation = effectiveRelation(entry);
			if (relation == FaneBoundaryRelation.MEMBER) {
				continue;
			}
			float seed = faneSeed(entry);
			if (relation == FaneBoundaryRelation.OUTSIDER) {
				drawRevealedFaneStyleDome(poseStack, buffer, cam, entry.heart(), HEART_RADIUS, time, seed);
				for (BlockPos stake : entry.stakes()) {
					ShellStyle style = ShellStyle.forRelation(relation, time + seed);
					drawHostileDomeShell(poseStack, buffer, cam, stake, entry.radius(), time, seed + stake.getX(),
							style.withAlpha(style.alpha() * 0.65F), false);
				}
				continue;
			}
			ShellStyle style = ShellStyle.forRelation(relation, time + seed);
			drawHostileDomeShell(poseStack, buffer, cam, entry.heart(), HEART_RADIUS, time, seed, style, false);
			if (style.glowAlphaScale() > 0.0F) {
				drawHostileDomeShell(poseStack, buffer, cam, entry.heart(), HEART_RADIUS * 1.025F, time,
						seed + 19.0F, style.withAlpha(style.alpha() * 0.34F), true);
			}
			for (BlockPos stake : entry.stakes()) {
				drawHostileDomeShell(poseStack, buffer, cam, stake, entry.radius(), time, seed + stake.getX(),
						style.withAlpha(style.alpha() * 0.65F), false);
			}
		}
	}

	public static void drawRevealedFaneStyleDome(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			Vec3 cam, BlockPos center, float radius, float time, float seed) {
		ShellStyle style = ShellStyle.forRelation(FaneBoundaryRelation.OUTSIDER, time + seed);
		drawHostileDomeShell(poseStack, buffer, cam, center, radius, time, seed, style, false);
		if (style.glowAlphaScale() > 0.0F) {
			drawHostileDomeShell(poseStack, buffer, cam, center, radius * 1.025F, time, seed + 19.0F,
					style.withAlpha(style.alpha() * 0.34F), true);
		}
	}

	public static float revealedFaneStyleSeed(BlockPos center, float radius) {
		return (float) (center.getX() * 0.13D + center.getY() * 0.07D + center.getZ() * 0.17D
				+ radius * 0.31D);
	}

	private static void drawHostileDomeShell(PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 cam,
			BlockPos center, float radius, float time, float seed, ShellStyle style, boolean glow) {
		double cx = center.getX() + 0.5D;
		double cy = center.getY() + 0.15D;
		double cz = center.getZ() + 0.5D;

		poseStack.pushPose();
		poseStack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = poseStack.last().pose();
		Vector3f shaderCenter = mat.transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
		RenderType coreType = !glow ? HemoRenderTypes.faneBoundaryShell(time, seed, shaderCenter.x(), shaderCenter.y(),
				shaderCenter.z(), radius, 0.12F, 9.0F, false) : null;
		RenderType glowType = glow ? HemoRenderTypes.faneBoundaryShell(time, seed, shaderCenter.x(), shaderCenter.y(),
				shaderCenter.z(), radius, 0.055F, 6.0F, true) : null;
		RenderType domeType = glow ? glowType : coreType;
		VertexConsumer vc = buffer.getBuffer(domeType);
		drawHostileDomeShell(vc, mat, radius, style, glow);
		if (glow) {
			buffer.endBatch(glowType);
		} else {
			buffer.endBatch(coreType);
		}
		poseStack.popPose();
	}

	private static void drawHostileDomeShell(VertexConsumer vc, Matrix4f mat, float radius, ShellStyle style,
			boolean glow) {
		for (int ring = 0; ring < DOME_RINGS; ring++) {
			double v0 = (double) ring / DOME_RINGS;
			double v1 = (double) (ring + 1) / DOME_RINGS;
			double theta0 = v0 * SPHERE_LATITUDE_END;
			double theta1 = v1 * SPHERE_LATITUDE_END;
			float y0 = (float) (Math.cos(theta0) * radius);
			float y1 = (float) (Math.cos(theta1) * radius);
			float r0 = (float) (Math.sin(theta0) * radius);
			float r1 = (float) (Math.sin(theta1) * radius);
			float shellAlpha = style.alpha() * (glow ? style.glowAlphaScale() : style.shellAlphaScale());

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
					emit(vc, mat, x00, y0, z00, style.glowRed(), style.glowGreen(), style.glowBlue(), shellAlpha);
					emit(vc, mat, x10, y1, z10, style.glowRed(), style.glowGreen(), style.glowBlue(), shellAlpha);
					emit(vc, mat, x11, y1, z11, style.glowRed(), style.glowGreen(), style.glowBlue(), shellAlpha);
					emit(vc, mat, x01, y0, z01, style.glowRed(), style.glowGreen(), style.glowBlue(), shellAlpha);
				} else {
					emit(vc, mat, x00, y0, z00, style.red(), style.green(), style.blue(), shellAlpha);
					emit(vc, mat, x10, y1, z10, style.red(), style.green(), style.blue(), shellAlpha);
					emit(vc, mat, x11, y1, z11, style.red(), style.green(), style.blue(), shellAlpha);
					emit(vc, mat, x01, y0, z01, style.red(), style.green(), style.blue(), shellAlpha);
				}
			}
		}
	}

	private static void drawMemberShimmer(PoseStack poseStack, Vec3 cam, FaneBoundaryClientData.Entry entry,
			float time) {
		float alpha = MEMBER_SHIMMER_ALPHA * (0.70F + 0.30F * (float) Math.sin(time * 0.07F));
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		drawDome(poseStack, cam, entry.heart(), HEART_RADIUS, alpha);
		for (BlockPos stake : entry.stakes()) {
			drawDome(poseStack, cam, stake, entry.radius(), alpha * 0.72F);
		}
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
			double theta0 = v0 * SPHERE_LATITUDE_END;
			double theta1 = v1 * SPHERE_LATITUDE_END;
			float y0 = (float) (Math.cos(theta0) * radius);
			float y1 = (float) (Math.cos(theta1) * radius);
			float r0 = (float) (Math.sin(theta0) * radius);
			float r1 = (float) (Math.sin(theta1) * radius);
			float shellAlpha = alpha * MEMBER_SHELL_ALPHA_SCALE;

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

				emit(buffer, mat, x00, y0, z00, 0.95F, 0.22F, 0.13F, shellAlpha);
				emit(buffer, mat, x10, y1, z10, 0.95F, 0.22F, 0.13F, shellAlpha);
				emit(buffer, mat, x11, y1, z11, 0.95F, 0.22F, 0.13F, shellAlpha);
				emit(buffer, mat, x01, y0, z01, 0.95F, 0.22F, 0.13F, shellAlpha);
			}
		}

		BufferUploader.drawWithShader(buffer.buildOrThrow());
		poseStack.popPose();
	}

	private static float rivalPulse(float time) {
		return 0.58F + 0.42F * (float) ((Math.sin(time * 0.045F) + 1.0D) * 0.5D);
	}

	private static float faneSeed(FaneBoundaryClientData.Entry entry) {
		return revealedFaneStyleSeed(entry.heart(), entry.radius());
	}

	private static boolean containsAnyStake(Vec3 pos, FaneBoundaryClientData.Entry entry) {
		for (BlockPos stake : entry.stakes()) {
			if (contains(pos, stake, entry.radius())) {
				return true;
			}
		}
		return false;
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

	private record ShellStyle(float alpha, float red, float green, float blue, float shellAlphaScale,
			float glowRed, float glowGreen, float glowBlue, float glowAlphaScale) {
		private static ShellStyle forRelation(FaneBoundaryRelation relation, float pulseTime) {
			if (relation == FaneBoundaryRelation.MUNDANE_OUTSIDER) {
				return new ShellStyle(MUNDANE_OUTSIDER_DOME_ALPHA, MUNDANE_OUTSIDER_SHELL_RED,
						MUNDANE_OUTSIDER_SHELL_GREEN, MUNDANE_OUTSIDER_SHELL_BLUE,
						MUNDANE_OUTSIDER_SHELL_ALPHA_SCALE, 0.55F, 0.04F, 0.03F, 0.0F);
			}
			float alpha = relation == FaneBoundaryRelation.RIVAL_ELDER
					? RIVAL_DOME_ALPHA * rivalPulse(pulseTime)
					: HOSTILE_DOME_ALPHA;
			return new ShellStyle(alpha, 0.04F, 0.0F, 0.0F, HOSTILE_SHELL_ALPHA_SCALE,
					0.85F, 0.05F, 0.04F, HOSTILE_GLOW_ALPHA_SCALE);
		}

		private ShellStyle withAlpha(float alpha) {
			return new ShellStyle(alpha, red, green, blue, shellAlphaScale, glowRed, glowGreen, glowBlue,
					glowAlphaScale);
		}
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

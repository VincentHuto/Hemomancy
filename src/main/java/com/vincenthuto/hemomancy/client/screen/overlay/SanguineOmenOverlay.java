package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class SanguineOmenOverlay {

	public static SanguineOmenOverlay instance;

	public enum Mode {
		WORLD_GRADE,
		SCREEN_OVERLAY
	}

	private final SanguineOmenOverlayState state = new SanguineOmenOverlayState();
	private TextureTarget frameCopyTarget;
	private Mode mode = Mode.WORLD_GRADE;

	public void start(int durationTicks, float peakAlpha, int seed) {
		start(durationTicks, peakAlpha, seed, Mode.WORLD_GRADE);
	}

	public void start(int durationTicks, float peakAlpha, int seed, Mode mode) {
		this.mode = mode;
		state.start(durationTicks, peakAlpha, seed);
	}

	public void tick() {
		state.tick();
	}

	public void clear() {
		state.clear();
	}

	public void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		if (!state.isActive() || mode != Mode.SCREEN_OVERLAY) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null || screenWidth <= 0 || screenHeight <= 0) {
			return;
		}

		ShaderInstance shader = ShaderInit.SANGUINE_OMEN_SCREEN_OVERLAY.getInstance().get();
		float alpha = state.alpha(partialTicks);
		if (shader == null || alpha <= 0.001F) {
			return;
		}

		float time = minecraft.level.getGameTime() + partialTicks;
		setUniform(shader, "HemoTime", time);
		setUniform(shader, "Progress", state.progress(partialTicks));
		setUniform(shader, "Intensity", alpha);
		setUniform(shader, "Seed", state.seed());

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(ShaderInit.SANGUINE_OMEN_SCREEN_OVERLAY.getInstance());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		drawFullscreenQuad(graphics, screenWidth, screenHeight);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	public void renderWorldGrade(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		if (!state.isActive() || mode != Mode.WORLD_GRADE) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null || screenWidth <= 0 || screenHeight <= 0) {
			return;
		}

		ShaderInstance shader = ShaderInit.SANGUINE_OMEN_WORLD.getInstance().get();
		float alpha = state.alpha(partialTicks);
		if (shader == null || alpha <= 0.001F) {
			return;
		}

		float time = minecraft.level.getGameTime() + partialTicks;
		setUniform(shader, "HemoTime", time);
		setUniform(shader, "Progress", state.progress(partialTicks));
		setUniform(shader, "Intensity", alpha);
		setUniform(shader, "Seed", state.seed());

		copyMainRenderTarget(minecraft);
		if (frameCopyTarget == null) {
			return;
		}
		RenderSystem.setShaderTexture(0, frameCopyTarget.getColorTextureId());
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(ShaderInit.SANGUINE_OMEN_WORLD.getInstance());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		drawFullscreenQuad(graphics, screenWidth, screenHeight);
		RenderSystem.enableDepthTest();
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

	private void copyMainRenderTarget(Minecraft minecraft) {
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

	private void ensureFrameCopyTarget(RenderTarget mainTarget) {
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

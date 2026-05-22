package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public class SanguineOmenOverlay {

	public static SanguineOmenOverlay instance;

	private final SanguineOmenOverlayState state = new SanguineOmenOverlayState();

	public void start(int durationTicks, float peakAlpha, int seed) {
		state.start(durationTicks, peakAlpha, seed);
	}

	public void tick() {
		state.tick();
	}

	public void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks) {
		if (!state.isActive()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null || screenWidth <= 0 || screenHeight <= 0) {
			return;
		}

		ShaderInstance shader = ShaderInit.SANGUINE_OMEN_OVERLAY.getInstance().get();
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
		RenderSystem.setShader(ShaderInit.SANGUINE_OMEN_OVERLAY.getInstance());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		Matrix4f matrix = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_TEX_COLOR);
		buffer.addVertex(matrix, 0.0F, screenHeight, -90.0F).setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.addVertex(matrix, screenWidth, screenHeight, -90.0F).setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.addVertex(matrix, screenWidth, 0.0F, -90.0F).setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.addVertex(matrix, 0.0F, 0.0F, -90.0F).setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		BufferUploader.drawWithShader(buffer.buildOrThrow());

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void setUniform(ShaderInstance shader, String name, float value) {
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(value);
		}
	}
}

package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

/**
 * HUD overlay that renders a reddish vignette around the screen edges
 * while any blood manipulation cooldown is active. The vignette fades
 * out as the cooldown expires.
 */
public class ManipCooldownOverlay {

	public static ManipCooldownOverlay instance;

	private static volatile int cooldownDuration = 0;
	private static volatile int cooldownRemaining = 0;

	public static void startCooldown(int ticks) {
		cooldownDuration = ticks;
		cooldownRemaining = ticks;
	}

	public static boolean isOnCooldown() {
		return cooldownRemaining > 0;
	}

	public static void tick() {
		if (cooldownRemaining > 0) {
			cooldownRemaining--;
		}
	}

	public void renderHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTicks) {
		if (cooldownRemaining <= 0 || cooldownDuration <= 0) {
			return;
		}

		float progress = (cooldownRemaining - partialTicks) / (float) cooldownDuration;
		progress = Math.max(0.0f, Math.min(1.0f, progress));

		float alpha = progress * 0.6f;

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();

		float r = 0.6f;
		float g = 0.0f;
		float b = 0.0f;

		float edgeSize = Math.min(screenWidth, screenHeight) * 0.35f;

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		// Top edge
		buffer.vertex(0, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(screenWidth, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(screenWidth, edgeSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(0, edgeSize, 0).color(r, g, b, 0.0f).endVertex();

		// Bottom edge
		buffer.vertex(0, screenHeight - edgeSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(screenWidth, screenHeight - edgeSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(screenWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(0, screenHeight, 0).color(r, g, b, alpha).endVertex();

		// Left edge
		buffer.vertex(0, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(edgeSize, 0, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(edgeSize, screenHeight, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(0, screenHeight, 0).color(r, g, b, alpha).endVertex();

		// Right edge
		buffer.vertex(screenWidth - edgeSize, 0, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(screenWidth, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(screenWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(screenWidth - edgeSize, screenHeight, 0).color(r, g, b, 0.0f).endVertex();

		BufferUploader.drawWithShader(buffer.end());

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}
}

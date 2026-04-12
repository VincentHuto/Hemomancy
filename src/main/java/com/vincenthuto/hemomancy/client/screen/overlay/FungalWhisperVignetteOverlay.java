package com.vincenthuto.hemomancy.client.screen.overlay;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

/**
 * Full-screen blood vignette overlay that flashes briefly when a fungal
 * whisper dialogue is triggered. The effect rapidly flashes in over a few
 * ticks, holds briefly, then fades out smoothly — giving the impression
 * of blood rushing to the edges of the player's vision.
 */
public class FungalWhisperVignetteOverlay {

	public static FungalWhisperVignetteOverlay instance;

	/** Total duration of the vignette effect in ticks (flash in + hold + fade out). */
	private static final int TOTAL_DURATION = 50; // ~2.5 seconds
	/** Ticks for the initial flash-in phase. */
	private static final int FLASH_IN_TICKS = 5;
	/** Ticks to hold at peak intensity after flash-in. */
	private static final int HOLD_TICKS = 10;

	/** Peak alpha at full intensity (0.0–1.0). */
	private static final float PEAK_ALPHA = 0.7f;

	private int remainingTicks = 0;

	/**
	 * Triggers the vignette flash. Can be called multiple times —
	 * resets the timer each time.
	 */
	public void trigger() {
		remainingTicks = TOTAL_DURATION;
	}

	/** Returns true if the vignette is currently active. */
	public boolean isActive() {
		return remainingTicks > 0;
	}

	/** Called once per client tick to count down the effect. */
	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
		}
	}

	public void renderHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTicks) {
		if (remainingTicks <= 0) {
			return;
		}

		// Calculate which phase we're in and derive alpha
		float elapsed = (TOTAL_DURATION - remainingTicks) + (1.0f - partialTicks);
		float alpha;

		if (elapsed < FLASH_IN_TICKS) {
			// Phase 1: rapid flash in
			float t = elapsed / FLASH_IN_TICKS;
			// Use ease-out (fast start, slow finish) for a sudden flash feel
			alpha = PEAK_ALPHA * (1.0f - (1.0f - t) * (1.0f - t));
		} else if (elapsed < FLASH_IN_TICKS + HOLD_TICKS) {
			// Phase 2: hold at peak with slight pulsing
			float holdProgress = (elapsed - FLASH_IN_TICKS) / HOLD_TICKS;
			float pulse = 1.0f - 0.1f * Mth.sin(holdProgress * (float) Math.PI * 2.0f);
			alpha = PEAK_ALPHA * pulse;
		} else {
			// Phase 3: smooth fade out
			float fadeElapsed = elapsed - FLASH_IN_TICKS - HOLD_TICKS;
			float fadeDuration = TOTAL_DURATION - FLASH_IN_TICKS - HOLD_TICKS;
			float t = Math.min(1.0f, fadeElapsed / fadeDuration);
			// Ease-in for gentle fade (slow start, fast end)
			alpha = PEAK_ALPHA * (1.0f - t * t);
		}

		alpha = Mth.clamp(alpha, 0.0f, 1.0f);
		if (alpha <= 0.001f) return;

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Matrix4f matrix = guiGraphics.pose().last().pose();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();

		// Deep blood red with a slight dark tint
		float r = 0.45f;
		float g = 0.0f;
		float b = 0.02f;

		// Inner alpha is 0 (transparent center), outer alpha is the computed value
		// Use a wider edge for a more dramatic, enveloping vignette
		float edgeSize = Math.min(screenWidth, screenHeight) * 0.45f;

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		// Top edge — opaque at top, fading to transparent
		buffer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(matrix, screenWidth, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(matrix, screenWidth, edgeSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, 0, edgeSize, 0).color(r, g, b, 0.0f).endVertex();

		// Bottom edge
		buffer.vertex(matrix, 0, screenHeight - edgeSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, screenWidth, screenHeight - edgeSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, screenWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(matrix, 0, screenHeight, 0).color(r, g, b, alpha).endVertex();

		// Left edge
		buffer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(matrix, edgeSize, 0, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, edgeSize, screenHeight, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, 0, screenHeight, 0).color(r, g, b, alpha).endVertex();

		// Right edge
		buffer.vertex(matrix, screenWidth - edgeSize, 0, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, screenWidth, 0, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(matrix, screenWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
		buffer.vertex(matrix, screenWidth - edgeSize, screenHeight, 0).color(r, g, b, 0.0f).endVertex();

		// Corner intensifiers — small triangular quads in the four corners
		// for extra blood pooling effect
		float cornerSize = edgeSize * 0.6f;
		float cornerAlpha = alpha * 0.5f;

		// Top-left corner fill
		buffer.vertex(matrix, 0, 0, 0).color(r, g, b, cornerAlpha).endVertex();
		buffer.vertex(matrix, cornerSize, 0, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, 0, cornerSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, 0, 0, 0).color(r, g, b, cornerAlpha).endVertex();

		// Top-right corner fill
		buffer.vertex(matrix, screenWidth, 0, 0).color(r, g, b, cornerAlpha).endVertex();
		buffer.vertex(matrix, screenWidth, 0, 0).color(r, g, b, cornerAlpha).endVertex();
		buffer.vertex(matrix, screenWidth, cornerSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, screenWidth - cornerSize, 0, 0).color(r, g, b, 0.0f).endVertex();

		// Bottom-left corner fill
		buffer.vertex(matrix, 0, screenHeight, 0).color(r, g, b, cornerAlpha).endVertex();
		buffer.vertex(matrix, 0, screenHeight, 0).color(r, g, b, cornerAlpha).endVertex();
		buffer.vertex(matrix, cornerSize, screenHeight, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, 0, screenHeight - cornerSize, 0).color(r, g, b, 0.0f).endVertex();

		// Bottom-right corner fill
		buffer.vertex(matrix, screenWidth, screenHeight, 0).color(r, g, b, cornerAlpha).endVertex();
		buffer.vertex(matrix, screenWidth - cornerSize, screenHeight, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, screenWidth, screenHeight - cornerSize, 0).color(r, g, b, 0.0f).endVertex();
		buffer.vertex(matrix, screenWidth, screenHeight, 0).color(r, g, b, cornerAlpha).endVertex();

		BufferUploader.drawWithShader(buffer.end());

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}
}

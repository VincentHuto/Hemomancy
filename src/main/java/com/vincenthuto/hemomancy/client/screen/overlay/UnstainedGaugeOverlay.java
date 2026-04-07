package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * HUD overlay that displays the Unstained purity and clarity gauges.
 * Uses an angular diamond aesthetic, positioned in the top-right corner.
 * The gauge has two vertical bars: purity (silver) and clarity (teal),
 * with diamond-shaped accents and stage indicators.
 */
public class UnstainedGaugeOverlay {

	public static UnstainedGaugeOverlay instance;

	private static final ResourceLocation GAUGE_FRAME = Hemomancy.rloc("textures/gui/unstained_gauge.png");
	private static final ResourceLocation PURITY_FILL = Hemomancy.rloc("textures/gui/unstained_fill_tiled.png");
	private static final ResourceLocation CLARITY_FILL = Hemomancy.rloc("textures/gui/unstained_clarity_fill_tiled.png");

	/** Bar dimensions matching the frame texture regions */
	private static final int BAR_WIDTH = 6;
	private static final int BAR_HEIGHT = 98;
	private static final int FRAME_WIDTH = 14;
	private static final int FRAME_HEIGHT = 106;

	/** Spacing between the two bars */
	private static final int BAR_SPACING = 18;

	/** Diamond indicator size */
	private static final int DIAMOND_SIZE = 8;

	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}

		player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(cap -> {
			if (!cap.hasBegunPurification()) {
				return;
			}
			renderGauge(guiGraphics, screenWidth, screenHeight, cap, partialTicks);
		});
	}

	private void renderGauge(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
			IUnstainedProgress cap, float partialTicks) {
		Font fr = mc.font;
		ClientLevel world = mc.level;

		// Position: top-right area, offset from edge
		int baseX = screenWidth - 42;
		int baseY = 4;

		float purity = cap.getPurity();
		float clarity = cap.getClarity();
		boolean clarityUnlocked = cap.hasClarityUnlocked();

		EnumPurityStage purityStage = EnumPurityStage.byPurity(purity);
		EnumClarityStage clarityStage = EnumClarityStage.byClarity(clarity);

		// --- Render Purity Bar ---
		renderBar(guiGraphics, baseX, baseY, purity, 100f, PURITY_FILL, world, 0xFFBEC8D7);

		// Purity stage label
		guiGraphics.drawString(fr, Component.literal(purityStage.getTitle()),
				baseX - fr.width(purityStage.getTitle()) - 4, baseY + 4, 0xBEC8D7, true);

		// Purity percentage
		String purityPct = String.format("%.0f%%", purity);
		guiGraphics.drawString(fr, Component.literal(purityPct),
				baseX - fr.width(purityPct) - 4, baseY + 16, 0x8890A0, true);

		// --- Render Clarity Bar (if unlocked) ---
		if (clarityUnlocked) {
			int clarityX = baseX + BAR_SPACING;
			renderBar(guiGraphics, clarityX, baseY, clarity, 100f, CLARITY_FILL, world, 0xFFA0D2C8);

			// Clarity stage label
			guiGraphics.drawString(fr, Component.literal(clarityStage.getTitle()),
					clarityX + FRAME_WIDTH + 4, baseY + 4, 0xA0D2C8, true);

			// Clarity percentage
			String clarityPct = String.format("%.0f%%", clarity);
			guiGraphics.drawString(fr, Component.literal(clarityPct),
					clarityX + FRAME_WIDTH + 4, baseY + 16, 0x70A098, true);
		}

		// --- Render diamond stage indicators below the bars ---
		int indicatorY = baseY + FRAME_HEIGHT + 6;
		renderStageIndicators(guiGraphics, baseX, indicatorY, purityStage.getLevel(), 5, 0xBEC8D7, 0x404850);

		if (clarityUnlocked) {
			renderStageIndicators(guiGraphics, baseX + BAR_SPACING, indicatorY,
					clarityStage.getLevel(), 5, 0xA0D2C8, 0x304840);
		}

		// --- Render top diamond accent ---
		renderDiamondAccent(guiGraphics, baseX + 3, baseY - 2, purity >= 100f ? 0xFFE8F0FF : 0xC0BEC8D7);
		if (clarityUnlocked) {
			renderDiamondAccent(guiGraphics, baseX + BAR_SPACING + 3, baseY - 2,
					clarity >= 100f ? 0xFFD0F0E8 : 0xC0A0D2C8);
		}
	}

	/**
	 * Renders a single vertical bar with fill and frame.
	 */
	private void renderBar(GuiGraphics guiGraphics, int x, int y, float value, float maxValue,
			ResourceLocation fillTexture, ClientLevel world, int tintColor) {

		float fillRatio = Math.min(value / maxValue, 1.0f);
		int fillHeight = (int) (BAR_HEIGHT * fillRatio);
		int emptyHeight = BAR_HEIGHT - fillHeight;

		// Calculate texture animation offset
		float textureShift = world != null ? (world.getGameTime() * 0.15f % 256) : 0;

		// -- Fill --
		if (fillHeight > 0) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			float r = ((tintColor >> 16) & 0xFF) / 255f;
			float g = ((tintColor >> 8) & 0xFF) / 255f;
			float b = (tintColor & 0xFF) / 255f;
			RenderSystem.setShaderColor(r, g, b, 0.7f);
			RenderSystem.setShaderTexture(0, fillTexture);

			// Fill from bottom up
			int fillY = y + 4 + emptyHeight;
			guiGraphics.blit(fillTexture,
					x + 4, x + 4 + BAR_WIDTH,           // x coords
					fillY, fillY + fillHeight,             // y coords
					0,                                      // blit offset
					BAR_WIDTH, fillHeight,                 // uWidth, vHeight
					22 + textureShift, textureShift,       // uOffset, vOffset
					256, 256);                              // texture dimensions
		}

		// -- Frame --
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GAUGE_FRAME);

		// Draw the main frame (from texture region 1,0 -> 13,105)
		guiGraphics.blit(GAUGE_FRAME, x + 1, y, 1, 0, 12, FRAME_HEIGHT);

		// Draw bottom cap (diamond cap from texture region 0,106)
		guiGraphics.blit(GAUGE_FRAME, x, y + FRAME_HEIGHT - 1, 0, 106, FRAME_WIDTH, 13);
	}

	/**
	 * Renders diamond-shaped stage indicators.
	 * Filled diamonds for achieved stages, hollow for unachieved.
	 */
	private void renderStageIndicators(GuiGraphics guiGraphics, int x, int y,
			int currentStage, int totalStages, int filledColor, int emptyColor) {

		for (int i = 0; i < totalStages; i++) {
			int dx = x + 2;
			int dy = y + i * (DIAMOND_SIZE + 2);
			int color = (i <= currentStage) ? (0xFF000000 | filledColor) : (0xFF000000 | emptyColor);

			// Draw small diamond using fill calls (2px diamond)
			guiGraphics.fill(dx + 2, dy, dx + 4, dy + 1, color);      // top
			guiGraphics.fill(dx + 1, dy + 1, dx + 5, dy + 2, color);  // upper-mid
			guiGraphics.fill(dx, dy + 2, dx + 6, dy + 3, color);      // center
			guiGraphics.fill(dx + 1, dy + 3, dx + 5, dy + 4, color);  // lower-mid
			guiGraphics.fill(dx + 2, dy + 4, dx + 4, dy + 5, color);  // bottom
		}
	}

	/**
	 * Renders a small decorative diamond accent at the top of a bar.
	 */
	private void renderDiamondAccent(GuiGraphics guiGraphics, int x, int y, int color) {
		int alpha = (color >> 24) & 0xFF;
		if (alpha == 0) {
			alpha = 0xFF;
		}
		int argb = (alpha << 24) | (color & 0x00FFFFFF);

		// 5px wide diamond
		guiGraphics.fill(x + 2, y - 3, x + 3, y - 2, argb);
		guiGraphics.fill(x + 1, y - 2, x + 4, y - 1, argb);
		guiGraphics.fill(x, y - 1, x + 5, y, argb);
		guiGraphics.fill(x + 1, y, x + 4, y + 1, argb);
		guiGraphics.fill(x + 2, y + 1, x + 3, y + 2, argb);
	}
}

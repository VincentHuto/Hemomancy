package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * HUD overlay that displays the Unstained purity and clarity gauges.
 * Uses a sharp angular / beveled aesthetic, positioned in the top-right corner.
 * Fully programmatic — no texture files required.
 * The gauge has two vertical bars: purity (silver) and clarity (teal),
 * with angular frames, chevron stage indicators, and geometric accents.
 */
public class UnstainedGaugeOverlay {

	public static UnstainedGaugeOverlay instance;

	// ── Angular bar dimensions ──
	private static final int BAR_WIDTH = 6;
	private static final int BAR_HEIGHT = 80;
	private static final int FRAME_THICKNESS = 2;

	/** Spacing between the two bars */
	private static final int BAR_SPACING = 22;

	// ── Angular color palette ──
	private static final int PURITY_COLOR = 0xBEC8D7;
	private static final int PURITY_DIM = 0x404850;
	private static final int PURITY_FRAME = 0xFF505868;
	private static final int PURITY_FRAME_INNER = 0xFF303840;

	private static final int CLARITY_COLOR = 0xA0D2C8;
	private static final int CLARITY_DIM = 0x304840;
	private static final int CLARITY_FRAME = 0xFF508878;
	private static final int CLARITY_FRAME_INNER = 0xFF284838;

	private static final int FRAME_BG = 0xFF0A0C10;

	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null) return;

		player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(cap -> {
			if (!cap.hasBegunPurification()) return;
			renderGauge(gfx, screenWidth, screenHeight, cap, partialTicks);
		});
	}

	private void renderGauge(GuiGraphics gfx, int screenWidth, int screenHeight,
			IUnstainedProgress cap, float partialTicks) {
		Font fr = mc.font;
		ClientLevel world = mc.level;
		float time = System.nanoTime() / 1_000_000_000f;

		// Position: top-right area
		int baseX = screenWidth - 46;
		int baseY = 6;

		float purity = cap.getPurity();
		float clarity = cap.getClarity();
		boolean clarityUnlocked = cap.hasClarityUnlocked();

		EnumPurityStage purityStage = EnumPurityStage.byPurity(purity);
		EnumClarityStage clarityStage = EnumClarityStage.byClarity(clarity);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// ── Render Purity Bar ──
		renderAngularBar(gfx, baseX, baseY, purity, 100f, PURITY_COLOR,
				PURITY_FRAME, PURITY_FRAME_INNER, time, false);

		// Purity stage label — sharp left-aligned
		String pTitle = purityStage.getTitle();
		gfx.drawString(fr, Component.literal(pTitle),
				baseX - fr.width(pTitle) - 6, baseY + 2, PURITY_COLOR, true);

		// Purity percentage
		String purityPct = String.format("%.0f%%", purity);
		gfx.drawString(fr, Component.literal(purityPct),
				baseX - fr.width(purityPct) - 6, baseY + 14, 0x8890A0, true);

		// ── Render Clarity Bar (if unlocked) ──
		if (clarityUnlocked) {
			int clarityX = baseX + BAR_SPACING;
			renderAngularBar(gfx, clarityX, baseY, clarity, 100f, CLARITY_COLOR,
					CLARITY_FRAME, CLARITY_FRAME_INNER, time, true);

			// Clarity stage label — sharp right-aligned
			gfx.drawString(fr, Component.literal(clarityStage.getTitle()),
					clarityX + BAR_WIDTH + FRAME_THICKNESS * 2 + 6, baseY + 2, CLARITY_COLOR, true);

			String clarityPct = String.format("%.0f%%", clarity);
			gfx.drawString(fr, Component.literal(clarityPct),
					clarityX + BAR_WIDTH + FRAME_THICKNESS * 2 + 6, baseY + 14, 0x70A098, true);
		}

		// ── Angular chevron stage indicators below bars ──
		int indicatorY = baseY + BAR_HEIGHT + FRAME_THICKNESS * 2 + 8;
		renderChevronIndicators(gfx, baseX, indicatorY, purityStage.getLevel(), 5,
				PURITY_COLOR, PURITY_DIM, time);

		if (clarityUnlocked) {
			renderChevronIndicators(gfx, baseX + BAR_SPACING, indicatorY, clarityStage.getLevel(), 5,
					CLARITY_COLOR, CLARITY_DIM, time);
		}

		// ── Angular arrow accent at top ──
		renderAngularArrowAccent(gfx, baseX, baseY - 6, purity >= 100f ? 0xFFE8F0FF : 0xA0BEC8D7);
		if (clarityUnlocked) {
			renderAngularArrowAccent(gfx, baseX + BAR_SPACING, baseY - 6,
					clarity >= 100f ? 0xFFD0F0E8 : 0xA0A0D2C8);
		}

		RenderSystem.disableBlend();
	}

	// ───── Angular bar with beveled frame ─────

	private void renderAngularBar(GuiGraphics gfx, int x, int y, float value, float maxValue,
			int fillColor, int frameColor, int frameInnerColor, float time, boolean mirrorBevel) {

		float fillRatio = Math.min(value / maxValue, 1.0f);
		int fillHeight = (int) (BAR_HEIGHT * fillRatio);
		int emptyHeight = BAR_HEIGHT - fillHeight;

		int fx = x;
		int fy = y;
		int fw = BAR_WIDTH + FRAME_THICKNESS * 2;
		int fh = BAR_HEIGHT + FRAME_THICKNESS * 2;

		// ── Outer frame — angular beveled look ──
		// Top edge (bright bevel)
		gfx.fill(fx, fy, fx + fw, fy + 1, frameColor);
		// Bottom edge (dark bevel)
		gfx.fill(fx, fy + fh - 1, fx + fw, fy + fh, frameInnerColor);
		// Left edge
		gfx.fill(fx, fy, fx + 1, fy + fh, mirrorBevel ? frameInnerColor : frameColor);
		// Right edge
		gfx.fill(fx + fw - 1, fy, fx + fw, fy + fh, mirrorBevel ? frameColor : frameInnerColor);

		// Inner border
		gfx.fill(fx + 1, fy + 1, fx + fw - 1, fy + 2, frameInnerColor);
		gfx.fill(fx + 1, fy + fh - 2, fx + fw - 1, fy + fh - 1, frameColor);
		gfx.fill(fx + 1, fy + 1, fx + 2, fy + fh - 1, mirrorBevel ? frameColor : frameInnerColor);
		gfx.fill(fx + fw - 2, fy + 1, fx + fw - 1, fy + fh - 1, mirrorBevel ? frameInnerColor : frameColor);

		// Background fill
		int innerX = fx + FRAME_THICKNESS;
		int innerY = fy + FRAME_THICKNESS;
		gfx.fill(innerX, innerY, innerX + BAR_WIDTH, innerY + BAR_HEIGHT, FRAME_BG);

		// ── Angular notch separators at each 25% ──
		for (int notch = 1; notch <= 3; notch++) {
			int ny = innerY + BAR_HEIGHT - (BAR_HEIGHT * notch / 4);
			// Left notch
			gfx.fill(fx, ny, innerX + 1, ny + 1, frameColor);
			// Right notch
			gfx.fill(innerX + BAR_WIDTH - 1, ny, fx + fw, ny + 1, frameColor);
		}

		// ── Fill from bottom up ──
		if (fillHeight > 0) {
			int fillTop = innerY + emptyHeight;
			float r = ((fillColor >> 16) & 0xFF) / 255f;
			float g = ((fillColor >> 8) & 0xFF) / 255f;
			float b = (fillColor & 0xFF) / 255f;

			for (int row = 0; row < fillHeight; row++) {
				float rowT = (float) row / fillHeight;
				// Subtle brightness gradient: brighter at bottom, dimmer at top
				float brightness = 0.5f + 0.5f * rowT;
				float pulse = 0.85f + 0.15f * Mth.sin(time * 2.0f + row * 0.06f);
				float finalBright = brightness * pulse;

				int cr = (int) (Mth.clamp(r * finalBright * 255, 0, 255));
				int cg = (int) (Mth.clamp(g * finalBright * 255, 0, 255));
				int cb = (int) (Mth.clamp(b * finalBright * 255, 0, 255));
				int alpha = (int) (200 + 55 * rowT);
				int color = (alpha << 24) | (cr << 16) | (cg << 8) | cb;
				gfx.fill(innerX, fillTop + row, innerX + BAR_WIDTH, fillTop + row + 1, color);
			}

			// Sharp top-edge highlight on the fill (angular meniscus)
			float edgePulse = 0.6f + 0.4f * Mth.sin(time * 3.5f);
			int eAlpha = (int) (220 * edgePulse);
			int eR = (int) (Mth.clamp(r * 255 * 1.3f, 0, 255));
			int eG = (int) (Mth.clamp(g * 255 * 1.3f, 0, 255));
			int eB = (int) (Mth.clamp(b * 255 * 1.3f, 0, 255));
			gfx.fill(innerX, fillTop, innerX + BAR_WIDTH, fillTop + 1,
					(eAlpha << 24) | (eR << 16) | (eG << 8) | eB);

			// Angular specular line on left edge of fill
			for (int row = 0; row < fillHeight; row++) {
				float specFade = 0.2f + 0.1f * Mth.sin(time * 1.5f + row * 0.12f);
				int sAlpha = (int) (60 * specFade);
				gfx.fill(innerX, fillTop + row, innerX + 1, fillTop + row + 1,
						(sAlpha << 24) | 0xFFFFFF);
			}
		}
	}

	// ───── Angular chevron (▶ or ◀) stage indicators ─────

	private void renderChevronIndicators(GuiGraphics gfx, int x, int y,
			int currentStage, int totalStages, int filledColor, int emptyColor, float time) {

		int chevronW = 8;
		int chevronH = 5;
		int spacing = 2;

		for (int i = 0; i < totalStages; i++) {
			int cy = y + i * (chevronH + spacing);
			boolean filled = i <= currentStage;
			int baseColor = filled ? filledColor : emptyColor;

			// Pulse effect for current stage
			if (filled && i == currentStage) {
				float pulse = 0.7f + 0.3f * Mth.sin(time * 4f + i);
				int r = (int) (((baseColor >> 16) & 0xFF) * pulse);
				int g = (int) (((baseColor >> 8) & 0xFF) * pulse);
				int b = (int) ((baseColor & 0xFF) * pulse);
				baseColor = (r << 16) | (g << 8) | b;
			}

			int argb = 0xFF000000 | baseColor;

			// Angular chevron pointing right: ▷
			// Row 0:     ##
			// Row 1:   ####
			// Row 2: ######
			// Row 3:   ####
			// Row 4:     ##
			int cx = x + FRAME_THICKNESS;
			gfx.fill(cx + 4, cy, cx + chevronW, cy + 1, argb);           // top point
			gfx.fill(cx + 2, cy + 1, cx + chevronW, cy + 2, argb);      // upper
			gfx.fill(cx, cy + 2, cx + chevronW, cy + 3, argb);          // widest
			gfx.fill(cx + 2, cy + 3, cx + chevronW, cy + 4, argb);      // lower
			gfx.fill(cx + 4, cy + 4, cx + chevronW, cy + 5, argb);      // bottom point
		}
	}

	// ───── Angular arrow accent at top of bar ─────

	private void renderAngularArrowAccent(GuiGraphics gfx, int x, int y, int color) {
		int alpha = (color >> 24) & 0xFF;
		if (alpha == 0) alpha = 0xFF;
		int argb = (alpha << 24) | (color & 0x00FFFFFF);

		// Downward-pointing angular arrow: ▽
		int cx = x + FRAME_THICKNESS + BAR_WIDTH / 2;
		gfx.fill(cx - 4, y, cx + 4, y + 1, argb);       // top wide
		gfx.fill(cx - 3, y + 1, cx + 3, y + 2, argb);   // taper
		gfx.fill(cx - 2, y + 2, cx + 2, y + 3, argb);   // taper
		gfx.fill(cx - 1, y + 3, cx + 1, y + 4, argb);   // near point
		gfx.fill(cx, y + 4, cx + 1, y + 5, argb);       // point
	}
}

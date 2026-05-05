package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * HUD overlay for the Unstained path.
 *
 * <p>The gauge is a single reliquary orb: purity washes the blood-red orb toward
 * white, then clarity brightens the halo around the sealed diamond frame.</p>
 */
public class UnstainedGaugeOverlay {

	public static UnstainedGaugeOverlay instance;

	private static final int ORB_RADIUS = 18;
	private static final int DIAMOND_RADIUS = 27;
	private static final int PIP_COUNT = 5;

	private static final int BLOOD_TOP = 0xFF9A1016;
	private static final int BLOOD_BOTTOM = 0xFF220305;
	private static final int PALE_TOP = 0xFFF1F6FF;
	private static final int PALE_BOTTOM = 0xFF7A8795;
	private static final int VERDIGRIS = 0xFF8BD6C6;
	private static final int VERDIGRIS_DIM = 0xFF315F56;
	private static final int TEXT_DIM = 0xFF9AA6B4;

	private final Minecraft mc = Minecraft.getInstance();
	private float animTime = 0f;

	public static boolean isConfiguredOnLeftSide() {
		return false;
	}

	public static int getGaugeCenterX(int screenWidth) {
		return screenWidth - 34;
	}

	public static int getGaugeCenterY() {
		return 54;
	}

	public static int getGaugeRadius() {
		return DIAMOND_RADIUS;
	}

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null) return;

		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(cap -> {
			if (!cap.hasBegunPurification()) return;
			renderGauge(gfx, screenWidth, cap);
		});
	}

	private void renderGauge(GuiGraphics gfx, int screenWidth, IUnstainedProgress cap) {
		Font font = mc.font;
		animTime += 0.016f;
		float time = animTime;

		float purity = Mth.clamp(cap.getPurity(), 0f, 100f);
		float clarity = Mth.clamp(cap.getClarity(), 0f, 100f);
		boolean clarityUnlocked = cap.hasClarityUnlocked();

		EnumPurityStage purityStage = EnumPurityStage.byPurity(purity);
		EnumClarityStage clarityStage = EnumClarityStage.byClarity(clarity);

		String title = clarityUnlocked ? clarityStage.getTitle() : purityStage.getTitle();
		String percent = clarityUnlocked ? String.format("Clarity %.0f%%", clarity) : String.format("Purity %.0f%%", purity);
		int titleColor = clarityUnlocked ? VERDIGRIS : lerpColor(0xFFCC4A4A, 0xFFE6EEF8, purity / 100f);
		int percentColor = clarityUnlocked ? 0xFF9EDACC : TEXT_DIM;

		int right = screenWidth - 8;
		int titleY = 6;
		int percentY = titleY + 11;
		int centerX = getGaugeCenterX(screenWidth);
		int centerY = getGaugeCenterY();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		gfx.drawString(font, Component.literal(title), right - font.width(title), titleY, titleColor, true);
		gfx.drawString(font, Component.literal(percent), right - font.width(percent), percentY, percentColor, true);

		if (clarityUnlocked || purity >= 100f) {
			renderHalo(gfx, centerX, centerY, clarityUnlocked ? clarity / 100f : 0f, time);
			renderDiamondFrame(gfx, centerX, centerY, clarityUnlocked, clarity / 100f, time);
		}

		renderOrb(gfx, centerX, centerY, purity, clarityUnlocked, time);

		int currentStage = clarityUnlocked ? clarityStage.getLevel() : purityStage.getLevel();
		renderLowerFacetPips(gfx, centerX, centerY, currentStage, clarityUnlocked, time);

		RenderSystem.disableBlend();
	}

	private void renderOrb(GuiGraphics gfx, int centerX, int centerY, float purity, boolean sealed, float time) {
		float purification = sealed ? 1f : purity / 100f;
		int topColor = lerpColor(BLOOD_TOP, PALE_TOP, purification);
		int bottomColor = lerpColor(BLOOD_BOTTOM, PALE_BOTTOM, purification);
		int borderColor = lerpColor(0xFF330808, 0xFFDCE6F2, purification);
		int innerBorderColor = lerpColor(0xFF170304, 0xFF27313C, purification);

		renderFilledCircle(gfx, centerX, centerY, ORB_RADIUS + 2, borderColor);
		renderFilledCircle(gfx, centerX, centerY, ORB_RADIUS, innerBorderColor);

		for (int y = -ORB_RADIUS + 1; y <= ORB_RADIUS - 1; y++) {
			for (int x = -ORB_RADIUS + 1; x <= ORB_RADIUS - 1; x++) {
				float distSq = x * x + y * y;
				if (distSq > (ORB_RADIUS - 1) * (ORB_RADIUS - 1)) continue;

				float vertical = (y + ORB_RADIUS) / (ORB_RADIUS * 2f);
				float pulse = 0.88f + 0.12f * Mth.sin(time * 1.7f + y * 0.18f);
				int color = multiplyColor(lerpColor(topColor, bottomColor, vertical), pulse);

				float highlight = 1f - ellipseDistance(x, y, -7f, -8f, 5.5f, 8f);
				if (highlight > 0f) {
					int alpha = (int) (Mth.clamp(highlight, 0f, 1f) * (sealed ? 70 : 55));
					color = alphaBlend(color, (alpha << 24) | 0x00FFFFFF);
				}

				float lowerGleam = 1f - ellipseDistance(x, y, 8f, 8f, 3.5f, 2.5f);
				if (lowerGleam > 0f) {
					int alpha = (int) (Mth.clamp(lowerGleam, 0f, 1f) * 42);
					color = alphaBlend(color, (alpha << 24) | 0x00FFFFFF);
				}

				gfx.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
			}
		}

		renderMeniscus(gfx, centerX, centerY, purification, time);
		if (!sealed) {
			renderBloodParticles(gfx, centerX, centerY, purification, time);
		}
	}

	private void renderMeniscus(GuiGraphics gfx, int centerX, int centerY, float purification, float time) {
		int lineY = centerY - 3 + Math.round(Mth.sin(time * 1.8f) * 1.2f);
		int color = lerpColor(0xCCDD2A22, 0xBFFFFFFF, purification);
		for (int x = -11; x <= 11; x++) {
			float curve = 1f - Math.abs(x) / 12f;
			if (curve <= 0f) continue;
			int alpha = (int) (alpha(color) * curve);
			gfx.fill(centerX + x, lineY, centerX + x + 1, lineY + 1, (alpha << 24) | (color & 0x00FFFFFF));
		}
	}

	private void renderBloodParticles(GuiGraphics gfx, int centerX, int centerY, float purification, float time) {
		Random random = new Random(9127L);
		float bloodVisibility = 1f - purification;
		for (int i = 0; i < 6; i++) {
			float phase = random.nextFloat();
			float speed = 0.22f + random.nextFloat() * 0.32f;
			float progress = (time * speed + phase) % 1f;
			int x = centerX - 9 + random.nextInt(19) + Math.round(Mth.sin(time * 0.8f + i) * 2f);
			int y = centerY + 10 - Math.round(progress * 21f);
			int dx = x - centerX;
			int dy = y - centerY;
			if (dx * dx + dy * dy > (ORB_RADIUS - 4) * (ORB_RADIUS - 4)) continue;

			int alpha = (int) (Mth.clamp(bloodVisibility, 0f, 1f) * 70f * (1f - Math.abs(progress - 0.5f)));
			if (alpha <= 4) continue;
			gfx.fill(x, y, x + 1, y + 1, (alpha << 24) | 0x00FF6A58);
		}
	}

	private void renderHalo(GuiGraphics gfx, int centerX, int centerY, float clarityRatio, float time) {
		float pulse = 0.75f + 0.25f * Mth.sin(time * (1.6f + clarityRatio * 1.5f));
		int radius = 24 + Math.round(clarityRatio * 7f + pulse * 2f);

		for (int ring = 0; ring < 8; ring++) {
			int r = radius - ring;
			if (r <= ORB_RADIUS + 1) continue;
			float fade = 1f - ring / 8f;
			int alpha = (int) ((34 + clarityRatio * 78f) * fade * pulse);
			int color = (alpha << 24) | 0x00E7EFFA;
			renderCircleOutline(gfx, centerX, centerY, r, color);
		}

		if (clarityRatio > 0f) {
			int verdigrisAlpha = (int) (80f * clarityRatio * pulse);
			renderCircleOutline(gfx, centerX, centerY, radius - 5, (verdigrisAlpha << 24) | (VERDIGRIS & 0x00FFFFFF));
		}
	}

	private void renderDiamondFrame(GuiGraphics gfx, int centerX, int centerY, boolean clarityUnlocked,
			float clarityRatio, float time) {
		int bright = clarityUnlocked ? lerpColor(0xFFDCE6F2, 0xFFFFFFFF, clarityRatio) : 0xFFDCE6F2;
		int shadow = clarityUnlocked ? lerpColor(0xFF73808E, VERDIGRIS_DIM, clarityRatio) : 0xFF73808E;
		int teal = withAlpha(VERDIGRIS, (int) ((80 + clarityRatio * 120) * (0.8f + 0.2f * Mth.sin(time * 2.3f))));

		drawDiamondOutline(gfx, centerX, centerY, DIAMOND_RADIUS, shadow);
		drawDiamondOutline(gfx, centerX, centerY, DIAMOND_RADIUS - 1, bright);
		gfx.fill(centerX, centerY - DIAMOND_RADIUS - 2, centerX + 1, centerY - DIAMOND_RADIUS + 2, bright);
		gfx.fill(centerX + DIAMOND_RADIUS - 2, centerY, centerX + DIAMOND_RADIUS + 2, centerY + 1, shadow);
		gfx.fill(centerX - 1, centerY + DIAMOND_RADIUS - 2, centerX + 1, centerY + DIAMOND_RADIUS + 2, bright);
		gfx.fill(centerX - DIAMOND_RADIUS - 2, centerY, centerX - DIAMOND_RADIUS + 2, centerY + 1, shadow);

		if (clarityUnlocked) {
			drawDiamondOutline(gfx, centerX, centerY, DIAMOND_RADIUS - 5, teal);
		}
	}

	private void renderLowerFacetPips(GuiGraphics gfx, int centerX, int centerY, int currentStage, boolean clarityUnlocked,
			float time) {
		int[][] offsets = {
				{ -14, DIAMOND_RADIUS - 10 },
				{ -7, DIAMOND_RADIUS - 6 },
				{ 0, DIAMOND_RADIUS - 2 },
				{ 7, DIAMOND_RADIUS - 6 },
				{ 14, DIAMOND_RADIUS - 10 }
		};

		for (int i = 0; i < PIP_COUNT; i++) {
			boolean filled = i <= currentStage;
			boolean active = i == currentStage;
			int color;
			if (!filled) {
				color = 0xFF3B4652;
			} else if (clarityUnlocked && active) {
				float pulse = 0.74f + 0.26f * Mth.sin(time * 4.2f);
				color = multiplyColor(VERDIGRIS, pulse);
			} else if (clarityUnlocked) {
				color = 0xFFDCE6F2;
			} else if (active) {
				color = multiplyColor(lerpColor(0xFFB42024, 0xFFE6EEF8, currentStage / 4f),
						0.78f + 0.22f * Mth.sin(time * 3.4f));
			} else {
				color = lerpColor(0xFFB42024, 0xFFDCE6F2, i / 4f);
			}

			renderDiamondPip(gfx, centerX + offsets[i][0], centerY + offsets[i][1], color);
		}
	}

	private void renderDiamondPip(GuiGraphics gfx, int centerX, int centerY, int color) {
		gfx.fill(centerX, centerY - 2, centerX + 1, centerY + 3, color);
		gfx.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, color);
		gfx.fill(centerX - 2, centerY, centerX + 3, centerY + 1, color);
	}

	private void renderFilledCircle(GuiGraphics gfx, int centerX, int centerY, int radius, int color) {
		int radiusSq = radius * radius;
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				if (x * x + y * y <= radiusSq) {
					gfx.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
				}
			}
		}
	}

	private void renderCircleOutline(GuiGraphics gfx, int centerX, int centerY, int radius, int color) {
		int radiusSq = radius * radius;
		int innerSq = (radius - 1) * (radius - 1);
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				int distSq = x * x + y * y;
				if (distSq <= radiusSq && distSq >= innerSq) {
					gfx.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
				}
			}
		}
	}

	private void drawDiamondOutline(GuiGraphics gfx, int centerX, int centerY, int radius, int color) {
		for (int d = 0; d <= radius; d++) {
			gfx.fill(centerX + d, centerY - radius + d, centerX + d + 1, centerY - radius + d + 1, color);
			gfx.fill(centerX + radius - d, centerY + d, centerX + radius - d + 1, centerY + d + 1, color);
			gfx.fill(centerX - d, centerY - radius + d, centerX - d + 1, centerY - radius + d + 1, color);
			gfx.fill(centerX - radius + d, centerY + d, centerX - radius + d + 1, centerY + d + 1, color);
		}
	}

	private float ellipseDistance(float px, float py, float cx, float cy, float rx, float ry) {
		float dx = (px - cx) / rx;
		float dy = (py - cy) / ry;
		return dx * dx + dy * dy;
	}

	private int lerpColor(int from, int to, float t) {
		t = Mth.clamp(t, 0f, 1f);
		int a = (int) (alpha(from) + (alpha(to) - alpha(from)) * t);
		int r = (int) (red(from) + (red(to) - red(from)) * t);
		int g = (int) (green(from) + (green(to) - green(from)) * t);
		int b = (int) (blue(from) + (blue(to) - blue(from)) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int alphaBlend(int base, int overlay) {
		float overlayA = alpha(overlay) / 255f;
		int a = Math.max(alpha(base), alpha(overlay));
		int r = (int) (red(base) * (1f - overlayA) + red(overlay) * overlayA);
		int g = (int) (green(base) * (1f - overlayA) + green(overlay) * overlayA);
		int b = (int) (blue(base) * (1f - overlayA) + blue(overlay) * overlayA);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int multiplyColor(int color, float factor) {
		int a = alpha(color);
		int r = (int) Mth.clamp(red(color) * factor, 0, 255);
		int g = (int) Mth.clamp(green(color) * factor, 0, 255);
		int b = (int) Mth.clamp(blue(color) * factor, 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int withAlpha(int color, int alpha) {
		return (Mth.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
	}

	private int alpha(int color) {
		return color >>> 24 & 0xFF;
	}

	private int red(int color) {
		return color >>> 16 & 0xFF;
	}

	private int green(int color) {
		return color >>> 8 & 0xFF;
	}

	private int blue(int color) {
		return color & 0xFF;
	}
}

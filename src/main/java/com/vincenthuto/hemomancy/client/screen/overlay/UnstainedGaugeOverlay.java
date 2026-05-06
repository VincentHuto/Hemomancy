package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Random;

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
	private static final int TEXTURE_SIZE = 72;
	private static final int TEXTURE_HALF = TEXTURE_SIZE / 2;
	private static final int PURITY_TEXTURE_STEPS = 20;
	private static final int CLARITY_TEXTURE_STEPS = 10;
	private static final int BASE_CENTER_Y = 54;
	private static final int EFFECT_ICON_OFFSET_Y = 26;
	private static final int FLUID_TOP_OFFSET = -11;
	private static final int FLUID_BOTTOM_OFFSET = 10;
	private static final float MENISCUS_WAVE_AMPLITUDE = 1.2f;

	private static final int VERDIGRIS = 0xFF8BD6C6;
	private static final int TEXT_DIM = 0xFF9AA6B4;

	private static final ResourceLocation[] ORB_TEXTURES = textureRange("orb_purity_", PURITY_TEXTURE_STEPS + 1);
	private static final ResourceLocation[] HALO_TEXTURES = textureRange("halo_", CLARITY_TEXTURE_STEPS + 1);
	private static final ResourceLocation[] CLARITY_DIAMOND_TEXTURES = textureRange("diamond_clarity_", CLARITY_TEXTURE_STEPS + 1);
	private static final ResourceLocation PURIFIED_DIAMOND_TEXTURE = texture("diamond_purified");
	private static final ResourceLocation[] PURITY_PIP_TEXTURES = textureRange("pips_purity_", PIP_COUNT);
	private static final ResourceLocation[] CLARITY_PIP_TEXTURES = textureRange("pips_clarity_", PIP_COUNT);

	private final Minecraft mc = Minecraft.getInstance();
	private float animTime = 0f;

	public static boolean isConfiguredOnLeftSide() {
		return false;
	}

	public static int getGaugeCenterX(int screenWidth) {
		return screenWidth - 34;
	}

	public static int getGaugeCenterY(LocalPlayer player, int screenHeight) {
		int centerY = BASE_CENTER_Y;
		if (player != null && !player.getActiveEffects().isEmpty()) {
			centerY += EFFECT_ICON_OFFSET_Y;
		}
		return Math.min(centerY, screenHeight - DIAMOND_RADIUS - 6);
	}

	private static int getLabelTopY(LocalPlayer player) {
		return player != null && !player.getActiveEffects().isEmpty() ? 6 + EFFECT_ICON_OFFSET_Y : 6;
	}

	public static int getGaugeCenterY() {
		return BASE_CENTER_Y;
	}

	public static int getGaugeRadius() {
		return DIAMOND_RADIUS;
	}

	public void renderHUD(GuiGraphics gfx, int screenWidth, int screenHeight, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null) return;

		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(cap -> {
			if (!cap.hasBegunPurification()) return;
			renderGauge(gfx, screenWidth, screenHeight, player, cap);
		});
	}

	private void renderGauge(GuiGraphics gfx, int screenWidth, int screenHeight, LocalPlayer player, IUnstainedProgress cap) {
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
		int titleY = getLabelTopY(player);
		int percentY = titleY + 11;
		int centerX = getGaugeCenterX(screenWidth);
		int centerY = getGaugeCenterY(player, screenHeight);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		gfx.drawString(font, Component.literal(title), right - font.width(title), titleY, titleColor, true);
		gfx.drawString(font, Component.literal(percent), right - font.width(percent), percentY, percentColor, true);

		if (clarityUnlocked || purity >= 100f) {
			renderLayer(gfx, clarityUnlocked ? HALO_TEXTURES[stepIndex(clarity, CLARITY_TEXTURE_STEPS)] : HALO_TEXTURES[0],
					centerX, centerY);
		}

		renderLayer(gfx, ORB_TEXTURES[clarityUnlocked ? PURITY_TEXTURE_STEPS : stepIndex(purity, PURITY_TEXTURE_STEPS)],
				centerX, centerY);

		float purification = clarityUnlocked ? 1f : purity / 100f;
		float fluidLevel = clarityUnlocked ? 1f : getBloodFillRatio(player, purification);
		renderOrbActivity(gfx, centerX, centerY, purification, fluidLevel, clarityUnlocked, time);

		if (clarityUnlocked || purity >= 100f) {
			renderLayer(gfx, clarityUnlocked ? CLARITY_DIAMOND_TEXTURES[stepIndex(clarity, CLARITY_TEXTURE_STEPS)]
					: PURIFIED_DIAMOND_TEXTURE, centerX, centerY);
		}

		int currentStage = clarityUnlocked ? clarityStage.getLevel() : purityStage.getLevel();
		renderLayer(gfx, (clarityUnlocked ? CLARITY_PIP_TEXTURES : PURITY_PIP_TEXTURES)[Mth.clamp(currentStage, 0, PIP_COUNT - 1)],
				centerX, centerY);

		RenderSystem.disableBlend();
	}

	private float getBloodFillRatio(LocalPlayer player, float fallbackRatio) {
		return HemoCapabilityAccess.getBloodVolume(player)
				.filter(blood -> blood.isActive() && blood.getMaxBloodVolume() > 0)
				.map(blood -> (float) Mth.clamp(blood.getBloodVolume() / blood.getMaxBloodVolume(), 0.0, 1.0))
				.orElse(Mth.clamp(fallbackRatio, 0f, 1f));
	}

	private static ResourceLocation texture(String name) {
		return Hemomancy.rloc("textures/gui/unstained_overlay/" + name + ".png");
	}

	private static ResourceLocation[] textureRange(String prefix, int count) {
		ResourceLocation[] textures = new ResourceLocation[count];
		for (int i = 0; i < count; i++) {
			textures[i] = texture(prefix + i);
		}
		return textures;
	}

	private int stepIndex(float value, int maxStep) {
		return Mth.clamp(Math.round(Mth.clamp(value, 0f, 100f) / 100f * maxStep), 0, maxStep);
	}

	private void renderLayer(GuiGraphics gfx, ResourceLocation texture, int centerX, int centerY) {
		int x = centerX - TEXTURE_HALF;
		int y = centerY - TEXTURE_HALF;
		blitAlphaTexture(gfx, texture, x, y, x + TEXTURE_SIZE, y + TEXTURE_SIZE);
	}

	private void blitAlphaTexture(GuiGraphics gfx, ResourceLocation texture, int x1, int y1, int x2, int y2) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Matrix4f matrix = gfx.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		buffer.addVertex(matrix, x1, y1, 0).setUv(0.0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f);
		buffer.addVertex(matrix, x1, y2, 0).setUv(0.0f, 1.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f);
		buffer.addVertex(matrix, x2, y2, 0).setUv(1.0f, 1.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f);
		buffer.addVertex(matrix, x2, y1, 0).setUv(1.0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	private void renderOrbActivity(GuiGraphics gfx, int centerX, int centerY, float purification, float fluidLevel, boolean sealed, float time) {
		if (fluidLevel <= 0.001f) {
			return;
		}

		renderMeniscus(gfx, centerX, centerY, purification, fluidLevel, time);
		if (!sealed) {
			renderBloodParticles(gfx, centerX, centerY, purification, fluidLevel, time);
		}
	}

	private int getFluidTopY(int centerY, float fluidLevel) {
		float level = Mth.clamp(fluidLevel, 0f, 1f);
		if (level <= 0f) {
			return centerY + FLUID_BOTTOM_OFFSET;
		}
		if (level >= 1f) {
			return centerY + FLUID_TOP_OFFSET;
		}

		double top = FLUID_TOP_OFFSET;
		double bottom = FLUID_BOTTOM_OFFSET;
		double radius = (bottom - top) / 2.0;
		double mid = (top + bottom) / 2.0;

		double low = -radius;
		double high = radius;
		for (int i = 0; i < 16; i++) {
			double y = (low + high) * 0.5;
			double fraction = circleAreaFractionBelow(y, radius);
			if (fraction < level) {
				low = y;
			} else {
				high = y;
			}
		}

		double ySolved = (low + high) * 0.5;
		return centerY + (int) Math.round(mid - ySolved);
	}

	private double circleAreaFractionBelow(double y, double radius) {
		double clampedY = Mth.clamp(y, -radius, radius);
		double root = Math.sqrt(Math.max(0.0, radius * radius - clampedY * clampedY));
		double area = clampedY * root + radius * radius * (Math.asin(clampedY / radius) + Math.PI / 2.0);
		return Mth.clamp(area / (Math.PI * radius * radius), 0.0, 1.0);
	}

	private void renderMeniscus(GuiGraphics gfx, int centerX, int centerY, float purification, float fluidLevel, float time) {
		if (fluidLevel >= 0.995f) {
			return;
		}

		int baseY = getFluidTopY(centerY, fluidLevel);
		float waveScale = Mth.clamp((1f - fluidLevel) / 0.15f, 0f, 1f);
		float wave = Mth.sin(time * 1.8f) * MENISCUS_WAVE_AMPLITUDE * waveScale;
		int lineY = Mth.clamp(baseY + Math.round(wave), centerY + FLUID_TOP_OFFSET, centerY + FLUID_BOTTOM_OFFSET);
		int color = lerpColor(0xCCDD2A22, 0xBFFFFFFF, purification);
		for (int x = -11; x <= 11; x++) {
			float curve = 1f - Math.abs(x) / 12f;
			if (curve <= 0f) continue;
			int alpha = (int) (alpha(color) * curve);
			gfx.fill(centerX + x, lineY, centerX + x + 1, lineY + 1, (alpha << 24) | (color & 0x00FFFFFF));
		}
	}

	private void renderBloodParticles(GuiGraphics gfx, int centerX, int centerY, float purification, float fluidLevel, float time) {
		if (fluidLevel <= 0f) {
			return;
		}

		Random random = new Random(9127L);
		float bloodVisibility = 1f - purification;
		int fillBottomY = centerY + FLUID_BOTTOM_OFFSET;
		int fillTopY = getFluidTopY(centerY, fluidLevel);
		int fillHeight = Math.max(1, fillBottomY - fillTopY + 1);
		for (int i = 0; i < 6; i++) {
			float phase = random.nextFloat();
			float speed = 0.22f + random.nextFloat() * 0.32f;
			float progress = (time * speed + phase) % 1f;
			int x = centerX - 9 + random.nextInt(19) + Math.round(Mth.sin(time * 0.8f + i) * 2f);
			int y = fillBottomY - Math.round(progress * (fillHeight - 1));
			int dx = x - centerX;
			int dy = y - centerY;
			if (dx * dx + dy * dy > (ORB_RADIUS - 4) * (ORB_RADIUS - 4)) continue;

			int alpha = (int) (Mth.clamp(bloodVisibility, 0f, 1f) * 70f * (1f - Math.abs(progress - 0.5f)));
			if (alpha <= 4) continue;
			gfx.fill(x, y, x + 1, y + 1, (alpha << 24) | 0x00FF6A58);
		}
	}

	private int lerpColor(int from, int to, float t) {
		t = Mth.clamp(t, 0f, 1f);
		int a = (int) (alpha(from) + (alpha(to) - alpha(from)) * t);
		int r = (int) (red(from) + (red(to) - red(from)) * t);
		int g = (int) (green(from) + (green(to) - green(from)) * t);
		int b = (int) (blue(from) + (blue(to) - blue(from)) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
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

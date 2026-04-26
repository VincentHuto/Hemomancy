package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import java.util.Random;

import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.CharmGourdMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.config.HemoClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;

/**
 * Fully programmatic blood volume HUD overlay.
 * Renders an organic vial-style bar matching the Ghastly Alembic / Morphling
 * Incubator aesthetic — gradient fill, meniscus, specular highlight,
 * animated bubbles, pulsing vein tendrils around the frame, and tick marks.
 */
public class BloodVolumeOverlay {

	public static BloodVolumeOverlay instance;


	// ── Organic bar dimensions ──
	private static final int BAR_W = 10;
	private static final int BAR_H = 80;
	private static final int GOURD_W = 18;
	private static final int GOURD_H = 26;
	private static final int HORN_W = 24;
	private static final int HORN_H = 18;

	// ── Colors (matching Hemomancy dark-blood palette) ──
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int BAR_BG = 0xFF060102;

	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics gfx, int width, int height, float partialTicks) {
		LocalPlayer player = this.mc.player;
		if (player == null) return;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(bloodCap -> {
			if (bloodCap == null || !bloodCap.isActive()) return;
			HemoCapabilityAccess.getScars(player).ifPresent(inv -> {
				if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem charm) {
					PacketHandler.sendToServer(new BloodVolumeClientPacket());

					var positionLoc = HemoClientConfig.HUD_LOCATION.get();
					int posX, posY;
					switch (positionLoc) {
						case 1 -> {
							posX = width - BAR_W - 8;
							posY = player.getActiveEffects().isEmpty() ? 4 : 56;
						}
						case 2 -> { posX = 4; posY = height - BAR_H - 30; }
						case 3 -> { posX = width - BAR_W - 8; posY = height - BAR_H - 30; }
						default -> { posX = 4; posY = 4; }
					}
					renderBloodBar(gfx, posX, posY, bloodCap, player, mc.level, partialTicks, width);
				}
			});
		});
	}

	// ───── Organic blood vial bar ─────
	private float animTime = 0f;

	private void renderBloodBar(GuiGraphics gfx, int posX, int posY, IBloodVolume bloodCap,
			Player player, ClientLevel world, float partialTicks, int screenWidth) {
		Font fr = mc.font;
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;

		double vol = bloodCap.getBloodVolume();
		double maxVol = bloodCap.getMaxBloodVolume();
		double ratio = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

		int barX = posX;
		int barY = posY + 12; // leave room for label above

		// ── Organic vein tendrils around the frame ──
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		renderVeinTendrils(gfx, barX, barY, time);

		// ── Outer frame — double border with organic bulge ──
		// Slightly irregular outer shape
		gfx.fill(barX - 2, barY - 3, barX + BAR_W + 2, barY + BAR_H + 3, BORDER_OUTER);
		gfx.fill(barX - 3, barY + 4, barX - 2, barY + BAR_H - 4, BORDER_OUTER); // left bulge
		gfx.fill(barX + BAR_W + 2, barY + 4, barX + BAR_W + 3, barY + BAR_H - 4, BORDER_OUTER); // right bulge
		gfx.fill(barX - 1, barY - 2, barX + BAR_W + 1, barY + BAR_H + 2, BORDER_INNER);
		gfx.fill(barX - 2, barY + 5, barX - 1, barY + BAR_H - 5, BORDER_INNER); // left inner bulge
		gfx.fill(barX + BAR_W + 1, barY + 5, barX + BAR_W + 2, barY + BAR_H - 5, BORDER_INNER); // right inner bulge

		// Inner dark background
		gfx.fill(barX, barY, barX + BAR_W, barY + BAR_H, BAR_BG);

		// ── Fill from bottom up with pulsing gradient ──
		int fillH = (int) (BAR_H * ratio);
		if (fillH > 0) {
			int fillTop = barY + BAR_H - fillH;
			for (int row = 0; row < fillH; row++) {
				float rowT = (float) row / fillH; // 0 at top of fill, 1 at bottom
				float pulse = 0.75f + 0.25f * Mth.sin(time * .3f + row * 0.08f);
				// Gradient: lighter crimson at bottom → darker at meniscus
				int r = (int) (Mth.clamp((100 + 80 * rowT) * pulse, 0, 255));
				int g = (int) (Mth.clamp((5 + 15 * rowT) * pulse, 0, 255));
				int b = (int) (Mth.clamp((8 + 10 * rowT) * pulse, 0, 255));
				int color = (0xEE << 24) | (r << 16) | (g << 8) | b;
				gfx.fill(barX, fillTop + row, barX + BAR_W, fillTop + row + 1, color);
			}

			// Meniscus highlight — organic wobble at the top of the fill
			float wobble = Mth.sin(time * 2.2f) * 0.8f;
			float meniscusPulse = 0.6f + 0.4f * Mth.sin(time * 3f + wobble);
			int mAlpha = (int) (200 * meniscusPulse);
			int meniscusColor = (mAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x18;
			gfx.fill(barX, fillTop, barX + BAR_W, fillTop + 1, meniscusColor);
			// Secondary dimmer meniscus line for organic depth
			if (fillTop + 1 < barY + BAR_H) {
				int m2Alpha = (int) (100 * meniscusPulse);
				gfx.fill(barX, fillTop + 1, barX + BAR_W, fillTop + 2,
						(m2Alpha << 24) | (0x90 << 16) | (0x15 << 8) | 0x10);
			}

			// Specular highlight — thin bright strip on the left side
			for (int row = 0; row < fillH; row++) {
				float fade = 0.3f + 0.15f * Mth.sin(time * 1.5f + row * 0.15f);
				int hAlpha = (int) (80 * fade);
				gfx.fill(barX + 1, fillTop + row, barX + 2, fillTop + row + 1,
						(hAlpha << 24) | (0xFF << 16) | (0x60 << 8) | 0x50);
			}

			// Animated bubbles rising through the blood
			Random bubbleRand = new Random(7777L);
			int bubbleCount = 5;
			for (int bi = 0; bi < bubbleCount; bi++) {
				float bSpeed = 0.3f + bubbleRand.nextFloat() * 0.5f;
				float bPhase = bubbleRand.nextFloat() * 100f;
				int bx = barX + 2 + bubbleRand.nextInt(Math.max(BAR_W - 4, 1));
				float bProgress = ((time * bSpeed + bPhase) % 1.0f);
				int by = fillTop + fillH - (int) (bProgress * fillH);
				if (by >= fillTop && by < fillTop + fillH - 1) {
					int bAlpha = (int) (60 * (1f - Math.abs(bProgress - 0.5f) * 2f));
					gfx.fill(bx, by, bx + 1, by + 1, (bAlpha << 24) | (0xFF << 16) | (0x40 << 8) | 0x30);
				}
			}

			// Subtle clot/particle drifting inside the blood
			Random clotRand = new Random(3141L);
			for (int ci = 0; ci < 3; ci++) {
				float cSpeed = 0.08f + clotRand.nextFloat() * 0.12f;
				float cPhase = clotRand.nextFloat() * 200f;
				float cXDrift = Mth.sin(time * 0.7f + ci * 2.1f) * 2f;
				int cx = barX + 2 + (int) (cXDrift + clotRand.nextInt(Math.max(BAR_W - 5, 1)));
				float cProgress = ((time * cSpeed + cPhase) % 1.0f);
				int cy = fillTop + fillH - (int) (cProgress * fillH);
				cx = Mth.clamp(cx, barX + 1, barX + BAR_W - 2);
				if (cy >= fillTop + 2 && cy < fillTop + fillH - 2) {
					int cAlpha = (int) (35 * (1f - Math.abs(cProgress - 0.5f) * 2f));
					gfx.fill(cx, cy, cx + 2, cy + 1, (cAlpha << 24) | (0x80 << 16) | (0x08 << 8) | 0x05);
				}
			}
		}

		// ── Tick marks on the right side ──
		for (int tick = 1; tick <= 3; tick++) {
			int tickY = barY + BAR_H - (BAR_H * tick / 4);
			gfx.fill(barX + BAR_W, tickY, barX + BAR_W + 2, tickY + 1, 0x50FFFFFF);
		}

		// ── Top organic cap — rounded drip shape ──
		gfx.fill(barX + 1, barY - 3, barX + BAR_W - 1, barY - 2, BORDER_OUTER);
		gfx.fill(barX + 2, barY - 4, barX + BAR_W - 2, barY - 3, BORDER_OUTER);
		gfx.fill(barX + 3, barY - 5, barX + BAR_W - 3, barY - 4, BORDER_OUTER);

		// ── Bottom organic cap — flat with slight bulge ──
		gfx.fill(barX + 1, barY + BAR_H + 2, barX + BAR_W - 1, barY + BAR_H + 3, BORDER_OUTER);
		gfx.fill(barX + 2, barY + BAR_H + 3, barX + BAR_W - 2, barY + BAR_H + 4, BORDER_OUTER);

		// ── Pome communion tracker — 9 items alongside the bar ──
		int orbColX = (posX > screenWidth / 2) ? barX -10 : barX + BAR_W + 3;
		int pomeProgress = HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(d -> d.getTotalPomesConsumed())
				.orElse(0);
		renderPomeTracker(gfx, orbColX, barY, pomeProgress);
		renderEquippedGourd(gfx, player, barX, barY, screenWidth, mc.getWindow().getGuiScaledHeight(), time);

		RenderSystem.disableBlend();

		// ── Volume text ──
		String volText = String.format("%.0f", vol);
		int textX = barX + (BAR_W / 2) - (fr.width(volText) / 2);
		gfx.drawString(fr, Component.literal(volText), textX, posY, 0xCC4444, true);
	}

	// ───── Pome communion tracker ─────

	private void renderEquippedGourd(GuiGraphics gfx, Player player, int barX, int barY, int screenWidth, int screenHeight,
			float time) {
		HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
			ItemStack gourdStack = scars.getStackInSlot(CharmGourdMenu.GOURD_SLOT_INDEX);
			if (!(gourdStack.getItem() instanceof BloodGourdItem)) {
				return;
			}

			IBloodVolume gourdVolume = HemoCapabilityAccess.getBloodVolume(gourdStack).orElse(null);
			if (gourdVolume == null) {
				return;
			}

			double maxVol = gourdVolume.getMaxBloodVolume();
			double ratio = maxVol > 0 ? Mth.clamp(gourdVolume.getBloodVolume() / maxVol, 0, 1) : 0;
			boolean curvedHorn = gourdStack.is(ItemInit.curved_horn.get());
			int iconW = curvedHorn ? HORN_W : GOURD_W;
			int iconH = curvedHorn ? HORN_H : GOURD_H;
			int gourdX = barX + (BAR_W - iconW) / 2;
			int gourdY = barY + BAR_H + 8;

			if (gourdY + iconH + 10 > screenHeight) {
				gourdY = barY - iconH - 8;
			}
			if (barX > screenWidth / 2) {
				gourdX = barX + BAR_W - iconW;
			}

			GourdPalette palette = getGourdPalette(gourdStack);
			if (isOpenGourd(gourdStack)) {
				renderGourdGlow(gfx, gourdX, gourdY, iconW, iconH, curvedHorn, time);
			}
			if (curvedHorn) {
				renderCurvedHorn(gfx, gourdX, gourdY + 3, ratio, time);
			} else {
				renderTwoLobedGourd(gfx, gourdX, gourdY, ratio, palette, time);
			}

			String volumeText = String.format("%.0f", gourdVolume.getBloodVolume());
			int textX = gourdX + (iconW / 2) - (mc.font.width(volumeText) / 2);
			gfx.drawString(mc.font, Component.literal(volumeText), textX, gourdY + iconH + 1,
					palette.textColor(), true);
		});
	}

	private boolean isOpenGourd(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.copyTag()
				.getBoolean(BloodGourdItem.TAG_STATE);
	}

	private void renderGourdGlow(GuiGraphics gfx, int x, int y, int iconW, int iconH, boolean curvedHorn, float time) {
		float pulse = 0.72f + 0.28f * Mth.sin(time * 2.1f);
		float cx = x + iconW * (curvedHorn ? 0.55f : 0.50f);
		float cy = y + iconH * (curvedHorn ? 0.58f : 0.54f);
		float rx = iconW * (curvedHorn ? 0.68f : 0.64f);
		float ry = iconH * (curvedHorn ? 0.72f : 0.58f);

		for (int gy = -4; gy < iconH + 5; gy++) {
			for (int gx = -5; gx < iconW + 6; gx++) {
				float dx = (x + gx - cx) / rx;
				float dy = (y + gy - cy) / ry;
				float distance = dx * dx + dy * dy;
				if (distance > 1.0f) {
					continue;
				}

				float edge = 1.0f - distance;
				int alpha = (int) (Mth.clamp(edge * edge * pulse, 0.0f, 1.0f) * (curvedHorn ? 105 : 125));
				if (alpha <= 0) {
					continue;
				}

				int red = curvedHorn ? 0xB8 : 0xD4;
				int green = curvedHorn ? 0x1A : 0x22;
				int blue = curvedHorn ? 0x12 : 0x16;
				gfx.fill(x + gx, y + gy, x + gx + 1, y + gy + 1, (alpha << 24) | (red << 16) | (green << 8) | blue);
			}
		}
	}

	private GourdPalette getGourdPalette(ItemStack stack) {
		if (stack.is(ItemInit.blood_gourd_red.get())) {
			return new GourdPalette(0xFF3A0606, 0xFF6A1010, 0xFF9A2220, 0xFFD24438);
		}
		if (stack.is(ItemInit.blood_gourd_black.get())) {
			return new GourdPalette(0xFF050307, 0xFF18111E, 0xFF3A2B40, 0xFF8C6A99);
		}
		return new GourdPalette(0xFF3A2A22, 0xFFC9B7A2, 0xFFFFF5E2, 0xFFEEDCC6);
	}

	private void renderTwoLobedGourd(GuiGraphics gfx, int x, int y, double ratio, GourdPalette palette, float time) {
		int fillLimit = y + GOURD_H - (int) Math.round(GOURD_H * ratio);
		for (int py = 0; py < GOURD_H; py++) {
			for (int px = 0; px < GOURD_W; px++) {
				float upperLobe = ellipseDistance(px, py, 9.5f, 7.0f, 6.2f, 6.6f);
				float lowerLobe = ellipseDistance(px, py, 8.0f, 17.0f, 8.0f, 8.8f);
				float waist = ellipseDistance(px, py, 8.8f, 11.5f, 4.8f, 4.4f);
				float shape = Math.min(Math.min(upperLobe, lowerLobe), waist);
				if (shape > 1.0f) {
					continue;
				}

				int screenY = y + py;
				boolean border = shape > 0.80f;
				if (border) {
					gfx.fill(x + px, screenY, x + px + 1, screenY + 1, palette.borderColor());
					continue;
				}

				float vertical = py / (float) GOURD_H;
				float shine = Mth.clamp(1.0f - ellipseDistance(px, py, 7.0f, 7.0f, 3.0f, 3.5f), 0.0f, 1.0f);
				int color = blendColor(palette.shellLowColor(), palette.shellHighColor(), 1.0f - vertical);
				color = alphaBlend(color, 0x55000000);

				float wave = 1.2f * Mth.sin(time * 2.0f + px * 0.7f);
				int liquidLine = fillLimit + Math.round(wave);
				boolean filled = screenY >= liquidLine;
				if (filled) {
					float liquidDepth = Mth.clamp((screenY - liquidLine) / (float) Math.max(GOURD_H, 1), 0.0f, 1.0f);
					float pulse = 0.78f + 0.22f * Mth.sin(time * 1.8f + py * 0.16f);
					int bloodLow = 0xEE520608;
					int bloodHigh = 0xF0C82022;
					color = blendColor(bloodHigh, bloodLow, liquidDepth);
					color = multiplyColor(color, pulse);

					if (Math.abs(screenY - liquidLine) <= 1) {
						color = alphaBlend(color, 0x99FF5548);
					}
					if ((px + py + (int) (time * 12)) % 17 == 0) {
						color = alphaBlend(color, 0x66FF7A68);
					}
				}
				if (shine > 0.0f) {
					int alpha = filled ? (int) (70 * shine) : (int) (38 * shine);
					color = alphaBlend(color, (alpha << 24) | 0x00FFFFFF);
				}
				gfx.fill(x + px, screenY, x + px + 1, screenY + 1, color);
			}
		}

		int neckPulse = 130 + (int) (35 * Mth.sin(time * 1.6f));
		int neckColor = (neckPulse << 24) | 0x3B261A;
		gfx.fill(x + 8, y - 2, x + 13, y + 1, neckColor);
		gfx.fill(x + 9, y - 4, x + 12, y - 2, neckColor);
		gfx.fill(x + 6, y + 10, x + 12, y + 12, alphaBlend(palette.borderColor(), 0x44000000));
	}

	private void renderCurvedHorn(GuiGraphics gfx, int x, int y, double ratio, float time) {
		float fillLimit = y + HORN_H - (float) (HORN_H * ratio);
		for (int py = 0; py < HORN_H; py++) {
			for (int px = 0; px < HORN_W; px++) {
				HornSample sample = sampleHorn(px, py);
				if (!sample.inside()) {
					continue;
				}

				int screenY = y + py;
				int color;
				if (sample.border()) {
					color = 0xFF3B261A;
				} else {
					float t = Mth.clamp(sample.pathT(), 0.0f, 1.0f);
					color = blendColor(0xFFEEDCC6, 0xFF8A6B4D, t);
					if (((int) (t * 18.0f) + py) % 4 == 0) {
						color = alphaBlend(color, 0x553B261A);
					}

					float wave = 0.9f * Mth.sin(time * 2.0f + px * 0.55f);
					if (screenY >= fillLimit + wave) {
						float liquidDepth = Mth.clamp((screenY - fillLimit) / (float) Math.max(HORN_H, 1), 0.0f, 1.0f);
						float pulse = 0.76f + 0.24f * Mth.sin(time * 1.7f + sample.pathT() * 5.0f);
						color = multiplyColor(blendColor(0xF0C82022, 0xEE4C0507, liquidDepth), pulse);
						if (Math.abs(screenY - (fillLimit + wave)) <= 1.0f) {
							color = alphaBlend(color, 0x99FF5548);
						}
					}

					float shine = Mth.clamp(1.0f - ellipseDistance(px, py, 9.0f, 5.0f, 6.0f, 3.2f), 0.0f, 1.0f);
					if (shine > 0.0f) {
						color = alphaBlend(color, ((int) (45 * shine) << 24) | 0x00FFFFFF);
					}
				}
				gfx.fill(x + px, screenY, x + px + 1, screenY + 1, color);
			}
		}

		gfx.fill(x + 19, y + 3, x + 23, y + 7, 0xFF4A130E);
		gfx.fill(x + 20, y + 4, x + 23, y + 6, 0xFF8C2A20);
		gfx.fill(x + 18, y + 7, x + 21, y + 10, 0xFF2E1B14);
	}

	private HornSample sampleHorn(int px, int py) {
		float[][] points = {
				{ 21.0f, 5.0f },
				{ 17.5f, 3.2f },
				{ 12.8f, 3.8f },
				{ 8.4f, 6.4f },
				{ 5.8f, 10.0f },
				{ 7.0f, 14.0f },
				{ 10.8f, 15.2f },
				{ 14.3f, 13.0f }
		};

		float bestDistance = Float.MAX_VALUE;
		float bestT = 0.0f;
		for (int i = 0; i < points.length - 1; i++) {
			float ax = points[i][0];
			float ay = points[i][1];
			float bx = points[i + 1][0];
			float by = points[i + 1][1];
			float vx = bx - ax;
			float vy = by - ay;
			float lengthSq = vx * vx + vy * vy;
			float localT = lengthSq > 0 ? Mth.clamp(((px - ax) * vx + (py - ay) * vy) / lengthSq, 0.0f, 1.0f) : 0.0f;
			float cx = ax + vx * localT;
			float cy = ay + vy * localT;
			float dx = px - cx;
			float dy = py - cy;
			float distance = Mth.sqrt(dx * dx + dy * dy);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestT = (i + localT) / (points.length - 1);
			}
		}

		float thickness = 3.35f - bestT * 1.45f;
		boolean inside = bestDistance <= thickness;
		boolean border = inside && bestDistance >= thickness - 0.85f;
		return new HornSample(inside, border, bestT);
	}

	private float ellipseDistance(float px, float py, float cx, float cy, float rx, float ry) {
		float dx = (px - cx) / rx;
		float dy = (py - cy) / ry;
		return dx * dx + dy * dy;
	}

	private int blendColor(int from, int to, float t) {
		t = Mth.clamp(t, 0.0f, 1.0f);
		int a = (int) (alpha(from) + (alpha(to) - alpha(from)) * t);
		int r = (int) (red(from) + (red(to) - red(from)) * t);
		int g = (int) (green(from) + (green(to) - green(from)) * t);
		int b = (int) (blue(from) + (blue(to) - blue(from)) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int alphaBlend(int base, int overlay) {
		float overlayA = alpha(overlay) / 255.0f;
		int a = Math.max(alpha(base), alpha(overlay));
		int r = (int) (red(base) * (1.0f - overlayA) + red(overlay) * overlayA);
		int g = (int) (green(base) * (1.0f - overlayA) + green(overlay) * overlayA);
		int b = (int) (blue(base) * (1.0f - overlayA) + blue(overlay) * overlayA);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int multiplyColor(int color, float factor) {
		int a = alpha(color);
		int r = (int) Mth.clamp(red(color) * factor, 0, 255);
		int g = (int) Mth.clamp(green(color) * factor, 0, 255);
		int b = (int) Mth.clamp(blue(color) * factor, 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private int alpha(int color) { return color >>> 24 & 0xFF; }
	private int red(int color) { return color >>> 16 & 0xFF; }
	private int green(int color) { return color >>> 8 & 0xFF; }
	private int blue(int color) { return color & 0xFF; }

	private record GourdPalette(int borderColor, int shellLowColor, int shellHighColor, int textColor) {}
	private record HornSample(boolean inside, boolean border, float pathT) {}

	private void renderPomeTracker(GuiGraphics gfx, int orbColX, int barY, int progress) {
		ItemStack pomeStack = new ItemStack(ItemInit.qliphoth_pome.get());
		// Render 9 slots at 0.5x scale → 8×8 px each, 9 px apart, fits in BAR_H=80
		gfx.pose().pushPose();
		gfx.pose().translate(orbColX, barY, 0);
		gfx.pose().scale(0.5f, 0.5f, 1.0f);
		for (int i = 0; i < 9; i++) {
			int py = (8 - i) * 18; // bottom-to-top in scaled space
			if (i < progress) {
				gfx.renderItem(pomeStack, 0, py);
			} else {
//				// Empty slot — faint dark-purple square
//				gfx.fill(1, py + 1, 15, py + 15, 0x35100028);
			}
		}
		gfx.pose().popPose();
	}

	// ───── Organic vein tendrils around the bar frame ─────

	private void renderVeinTendrils(GuiGraphics gfx, int barX, int barY, float time) {
		Random veinRand = new Random(2718L);
		int tendrilCount = 6;

		for (int i = 0; i < tendrilCount; i++) {
			// Tendrils originate from the edges of the bar
			boolean leftSide = i % 2 == 0;
			float startY = barY + (BAR_H * veinRand.nextFloat());
			float startX = leftSide ? barX - 3 : barX + BAR_W + 2;
			float dir = leftSide ? -1f : 1f;
			float angle = (veinRand.nextFloat() - 0.5f) * 1.2f;
			int len = 8 + veinRand.nextInt(12);
			float speed = 0.5f + veinRand.nextFloat() * 0.5f;

			float cosA = Mth.cos(angle);
			float sinA = Mth.sin(angle);

			for (int step = 0; step < len; step++) {
				float squiggle = 2f * Mth.sin(0.4f * step + time * speed + i);
				float px = startX + step * dir * cosA * 0.8f - squiggle * sinA * 0.3f;
				float py = startY + step * sinA * 0.8f + squiggle * cosA * 0.3f;

				float tipFade = 1f;
				if (step < 3) tipFade = step / 3f;
				else if (step > len - 3) tipFade = (len - step) / 3f;

				float pulse = 0.5f + 0.5f * Mth.sin(time * 1.2f + i * 0.8f + step * 0.05f);
				int a = (int) (Mth.clamp(tipFade * pulse * 120, 10, 120));
				int r = (int) (Mth.clamp(50 * pulse, 0, 255));
				int g = (int) (Mth.clamp(5 * pulse, 0, 255));
				int b = (int) (Mth.clamp(5 * pulse, 0, 255));

				gfx.fill((int) px, (int) py, (int) px + 1, (int) py + 1,
						(a << 24) | (r << 16) | (g << 8) | b);
			}
		}
	}
}

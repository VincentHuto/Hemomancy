package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.config.HemoClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

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

	// ── Colors (matching Hemomancy dark-blood palette) ──
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int BAR_BG = 0xFF060102;

	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics gfx, int width, int height, float partialTicks) {
		LocalPlayer player = this.mc.player;
		if (player == null) return;

		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(bloodCap -> {
			if (bloodCap == null || !bloodCap.isActive()) return;
			player.getCapability(ScarsCapabilities.SCARS).ifPresent(inv -> {
				if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem charm) {
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new BloodVolumeClientPacket());

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
					renderBloodBar(gfx, posX, posY, bloodCap, player, mc.level, partialTicks);
				}
			});
		});
	}

	// ───── Organic blood vial bar ─────

	private void renderBloodBar(GuiGraphics gfx, int posX, int posY, IBloodVolume bloodCap,
			Player player, ClientLevel world, float partialTicks) {
		Font fr = mc.font;
		float time = System.nanoTime() / 1_000_000_000f;

		double vol = bloodCap.getBloodVolume();
		double maxVol = bloodCap.getMaxBloodVolume();
		double ratio = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

		int barX = posX;
		int barY = posY + 12; // leave room for label above

		// ── Organic vein tendrils around the frame ──
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		//renderVeinTendrils(gfx, barX, barY, time);

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
				float pulse = 0.75f + 0.25f * Mth.sin(time * 2.5f + row * 0.08f);
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

		RenderSystem.disableBlend();

		// ── Volume text ──
		String volText = String.format("%.0f", vol);
		int textX = barX + (BAR_W / 2) - (fr.width(volText) / 2);
		gfx.drawString(fr, Component.literal(volText), textX, posY, 0xCC4444, true);
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

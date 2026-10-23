package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MorphlingIncubatorMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MorphlingIncubatorBlockEntity;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Random;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;

public class MorphlingIncubatorScreen extends AbstractContainerScreen<MorphlingIncubatorMenu> {

	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;

	private static final int CRAFT_AREA_HEIGHT = 96;

	final MorphlingIncubatorBlockEntity te;
	private static final VeinBackgroundRenderer.Style VEINS = new VeinBackgroundRenderer.Style(
			VeinBackgroundRenderer.Field.STANDARD.count(22), VeinBackgroundRenderer.Palette.CRIMSON,
			VeinBackgroundRenderer.Speckles.Scattered.CRIMSON.count(80));
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VEINS);

	private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;

	public MorphlingIncubatorScreen(MorphlingIncubatorMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.te = menu.getTe();
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
		BloodVolumeBarWidget.renderTooltip(graphics, font, bloodBarBounds,
				te.getBloodVolume(), te.getMaxBloodVolume(), mouseX, mouseY);
		if (BloodVolumeBarWidget.Bounds.EMPTY != null) return;

		// Blood bar hover tooltip
		if (bloodBarBounds.contains(mouseX, mouseY)) {
			double vol = te.getBloodVolume();
			double maxVol = te.getMaxBloodVolume();
			graphics.renderTooltip(font, List.of(
					Component.literal(String.format("§4Blood: §c%.0f §4/ §c%.0f", vol, maxVol))
			), java.util.Optional.empty(), mouseX, mouseY);
		}
	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
		int gx = this.leftPos;
		int gy = this.topPos;
		int gw = this.imageWidth;
		int gh = this.imageHeight;
		float animationTime = getAnimationTime(partialTicks);

		// ───── Venous background for upper crafting area ─────
		veins.draw(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT, animationTime);
		ScreenDrawUtils.drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT, BORDER_OUTER, BORDER_INNER);

		// ───── Red gradient panel behind inventory section ─────
		Slot firstInventorySlot = this.menu.slots.get(MorphlingIncubatorMenu.SLOT_COUNT);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.BLOODY,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		// ───── Draw slot backgrounds for all slots ─────
		for (int i = 0; i < MorphlingIncubatorMenu.SLOT_COUNT; i++) {
			Slot slot = this.menu.slots.get(i);
			int sx = gx + slot.x;
			int sy = gy + slot.y;
			drawSlotBackground(gfx, sx, sy, slot.index);
		}

		// ───── Progress ring ─────
		renderProgressRing(gfx, gx, gy, animationTime);

		// ───── Blood volume bar (inline) ─────
		renderBloodBar(gfx, gx, gy, animationTime);
	}

	private float getAnimationTime(float partialTicks) {
		if (this.minecraft != null && this.minecraft.level != null) {
			return AnimationTime.gameTime(this.minecraft.level, partialTicks) * 0.05f;
		}
		return Util.getMillis() * 0.001f;
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		// Title centered in the craft area
		gfx.drawString(font, this.title, this.titleLabelX, 4, 0xFFAA2222, false);

		// Inventory label

		// Mode indicator text — bottom right of craft area
		int mode = this.menu.getMode();
		if (mode == 1) {
			String text = "Incubating...";
			int tw = font.width(text);
			gfx.drawString(font, text, this.imageWidth - tw - 4, CRAFT_AREA_HEIGHT - 10, 0xFF9B30FF, false);
		} else if (mode == 2) {
			Component text = Component.translatable("screen.hemomancy.morphling_incubator.feeding");
			int tw = font.width(text);
			gfx.drawString(font, text, this.imageWidth - tw - 4, CRAFT_AREA_HEIGHT - 10, 0xFF22AA22, false);
		} else {
			Component status = switch (this.menu.getEnzymeFeedStatus()) {
				case 1 -> Component.translatable("screen.hemomancy.morphling_incubator.needs_blood");
				case 2 -> Component.translatable("screen.hemomancy.morphling_incubator.needs_enzyme");
				case 3 -> Component.translatable("screen.hemomancy.morphling_incubator.apex");
				default -> Component.empty();
			};
			if (!status.getString().isEmpty()) {
				int tw = font.width(status);
				gfx.drawString(font, status, this.imageWidth - tw - 4, CRAFT_AREA_HEIGHT - 10, 0xFFCC5555, false);
			}
		}
	}

	// ───── Red gradient inventory background ─────

	// ───── Slot background rendering ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
		ScreenDrawUtils.drawSlotWell(gfx, sx, sy, SLOT_BORDER_DARK, SLOT_BG, SLOT_BORDER_LIGHT);

		// Special tint for output slot
		if (slotIndex == MorphlingIncubatorMenu.OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
		// Special tint for blood slot
		if (slotIndex == MorphlingIncubatorMenu.BLOOD_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA0000);
		}
		// Special tint for flask output slot
		if (slotIndex == MorphlingIncubatorMenu.FLASK_OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
		// Special tint for center slot
		if (slotIndex == MorphlingIncubatorMenu.CENTER_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x208B008B);
		}
	}

	// ───── Progress ring (around center slot) ─────
	private void renderProgressRing(GuiGraphics gfx, int gx, int gy, float time) {
		// Ring centered on the center slot (80+8, 40+8 = slot center)
		int cx = gx + 88;
		int cy = gy + 48;
		int outerRadius = 24;
		int innerRadius = 19;
		int segments = 64;

		double progress = 0;
		if (this.menu.isCrafting()) {
			progress = this.menu.getCraftProgress() / 24.0;
		}
		int filledSegments = (int) (segments * progress);

		for (int i = 0; i < segments; i++) {
			double angle1 = -Math.PI / 2 + (2 * Math.PI / segments) * i;
			double angle2 = -Math.PI / 2 + (2 * Math.PI / segments) * (i + 1);

			// Draw arc segment pixel by pixel along the radial
			for (int r = innerRadius; r <= outerRadius; r++) {
				// Sample a few points along the arc
				for (double a = angle1; a < angle2; a += 0.04) {
					int px = cx + (int) (r * Math.cos(a));
					int py = cy + (int) (r * Math.sin(a));

					int color;
					if (i < filledSegments) {
						// Filled — bright pulsing crimson
						float pulse = 0.7f + 0.3f * Mth.sin(time * 3f + i * 0.1f);
						int red = (int) (200 * pulse);
						int green = (int) (20 * pulse);
						color = (0xDD << 24) | (red << 16) | (green << 8) | 0x15;
					} else {
						// Empty — dim dark track
						color = 0x30200808;
					}
					gfx.fill(px, py, px + 1, py + 1, color);
				}
			}
		}

		// Draw a subtle glow at the leading edge of progress
		if (filledSegments > 0 && filledSegments < segments) {
			double leadAngle = -Math.PI / 2 + (2 * Math.PI / segments) * filledSegments;
			int leadX = cx + (int) ((innerRadius + outerRadius) / 2.0 * Math.cos(leadAngle));
			int leadY = cy + (int) ((innerRadius + outerRadius) / 2.0 * Math.sin(leadAngle));

			float pulse = 0.5f + 0.5f * Mth.sin(time * 5f);
			int glowAlpha = (int) (120 * pulse);
			int glowColor = (glowAlpha << 24) | (0xFF << 16) | (0x30 << 8) | 0x20;
			// Small 3x3 glow dot
			gfx.fill(leadX - 1, leadY - 1, leadX + 2, leadY + 2, glowColor);
		}
	}

	// ───── Blood volume bar ─────

	private void renderBloodBar(GuiGraphics gfx, int gx, int gy, float time) {
		// Vertical vial bar directly above the blood slot (blood slot is at 8, 74)
		int barW = 8;
		int barH = 52;
		int barX = gx + 8 + (16 - barW) / 2; // centered above the 16px slot
		int barY = gy + 74 - barH - 4;        // 4px gap above the slot
		if (BloodVolumeBarWidget.Bounds.EMPTY != null) {
			bloodBarBounds = BloodVolumeBarWidget.render(gfx, barX, barY, barW, barH,
					te.getBloodVolume(), te.getMaxBloodVolume(), time, BORDER_OUTER, BORDER_INNER);
			return;
		}

		// Store bounds for hover tooltip
		bloodBarBounds = new BloodVolumeBarWidget.Bounds(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2);

		double vol = te.getBloodVolume();
		double maxVol = te.getMaxBloodVolume();
		double ratio = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

		// Outer frame — double border
		gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, BORDER_OUTER);
		gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, BORDER_INNER);

		// Inner dark background
		gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF060102);

		// Fill from bottom up with a vertical gradient
		int fillH = (int) (barH * ratio);
		if (fillH > 0) {
			int fillTop = barY + barH - fillH;
			for (int row = 0; row < fillH; row++) {
				float rowT = (float) row / fillH; // 0 at top of fill, 1 at bottom
				float pulse = 0.75f + 0.25f * Mth.sin(time * 2.5f + row * 0.08f);
				// Gradient: lighter crimson at bottom → darker at meniscus
				int r = (int) (Mth.clamp((100 + 80 * rowT) * pulse, 0, 255));
				int g = (int) (Mth.clamp((5 + 15 * rowT) * pulse, 0, 255));
				int b = (int) (Mth.clamp((8 + 10 * rowT) * pulse, 0, 255));
				int color = (0xEE << 24) | (r << 16) | (g << 8) | b;
				gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1, color);
			}

			// Meniscus highlight — bright line at the top of the fill
			float meniscusPulse = 0.6f + 0.4f * Mth.sin(time * 3f);
			int mAlpha = (int) (200 * meniscusPulse);
			int meniscusColor = (mAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x18;
			gfx.fill(barX, fillTop, barX + barW, fillTop + 1, meniscusColor);

			// Specular highlight — thin bright strip on the left side
			for (int row = 0; row < fillH; row++) {
				float fade = 0.3f + 0.15f * Mth.sin(time * 1.5f + row * 0.15f);
				int hAlpha = (int) (80 * fade);
				gfx.fill(barX + 1, fillTop + row, barX + 2, fillTop + row + 1,
						(hAlpha << 24) | (0xFF << 16) | (0x60 << 8) | 0x50);
			}

			// Animated bubbles rising through the blood
			Random bubbleRand = new Random(7777L);
			for (int bi = 0; bi < 3; bi++) {
				float bSpeed = 0.4f + bubbleRand.nextFloat() * 0.6f;
				float bPhase = bubbleRand.nextFloat() * 100f;
				int bx = barX + 2 + bubbleRand.nextInt(Math.max(barW - 4, 1));
				float bProgress = ((time * bSpeed + bPhase) % 1.0f);
				int by = fillTop + fillH - (int) (bProgress * fillH);
				if (by >= fillTop && by < fillTop + fillH - 1) {
					int bAlpha = (int) (60 * (1f - Math.abs(bProgress - 0.5f) * 2f));
					gfx.fill(bx, by, bx + 1, by + 1, (bAlpha << 24) | (0xFF << 16) | (0x40 << 8) | 0x30);
				}
			}
		}

		// Tick marks on the right side of the bar
		for (int tick = 1; tick <= 3; tick++) {
			int tickY = barY + barH - (barH * tick / 4);
			gfx.fill(barX + barW, tickY, barX + barW + 1, tickY + 1, 0x60FFFFFF);
		}
	}


}

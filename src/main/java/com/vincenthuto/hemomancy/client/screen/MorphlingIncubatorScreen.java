package com.vincenthuto.hemomancy.client.screen;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.menu.MorphlingIncubatorMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.MorphlingIncubatorBlockEntity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class MorphlingIncubatorScreen extends AbstractContainerScreen<MorphlingIncubatorMenu> {

	private static final int VEIN_COUNT = 22;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;

	// Working area is now taller: 96px craft area, then inventory below
	private static final int CRAFT_AREA_HEIGHT = 96;

	final MorphlingIncubatorBlockEntity te;
	private float[][] veinParams;

	// Blood bar screen-space bounds for hover detection
	private int bloodBarX1, bloodBarY1, bloodBarX2, bloodBarY2;

	public MorphlingIncubatorScreen(MorphlingIncubatorMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.te = menu.getTe();
		this.imageWidth = 176;
		this.imageHeight = 186; // taller to accommodate bigger working area
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 2; // push inventory label down
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

		// Seed vein parameters for the animated background
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
			veinParams[i][4] = 8f + rand.nextFloat() * 18f;
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
			veinParams[i][6] = 60 + rand.nextInt(120);
			veinParams[i][7] = 1 + rand.nextInt(3);
			veinParams[i][8] = rand.nextFloat();
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);

		// Blood bar hover tooltip
		if (mouseX >= bloodBarX1 && mouseX < bloodBarX2 && mouseY >= bloodBarY1 && mouseY < bloodBarY2) {
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

		// ───── Venous background for upper crafting area ─────
		renderVeinBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
		drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

		// ───── Red gradient panel behind inventory section ─────
		int invTop = gy + CRAFT_AREA_HEIGHT;
		int invH = gh - CRAFT_AREA_HEIGHT;
		renderRedGradientBackground(gfx, gx, invTop, gw, invH);

		// Separator border between crafting area and inventory
		gfx.fill(gx, invTop, gx + gw, invTop + 1, BORDER_OUTER);
		gfx.fill(gx, invTop + 1, gx + gw, invTop + 2, BORDER_INNER);

		// ───── Draw slot backgrounds for all slots ─────
		for (Slot slot : this.menu.slots) {
			int sx = gx + slot.x;
			int sy = gy + slot.y;
			drawSlotBackground(gfx, sx, sy, slot.index);
		}

		// ───── Progress ring ─────
		renderProgressRing(gfx, gx, gy);

		// ───── Blood volume bar (inline) ─────
		renderBloodBar(gfx, gx, gy);
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		// Title centered in the craft area
		//gfx.drawString(font, this.title, this.titleLabelX, 4, 0xFFAA2222, false);

		// Inventory label
		gfx.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF444444, false);

		// Mode indicator text — bottom right of craft area
		int mode = this.menu.getMode();
		if (mode == 1) {
			String text = "Incubating...";
			int tw = font.width(text);
			gfx.drawString(font, text, this.imageWidth - tw - 4, CRAFT_AREA_HEIGHT - 10, 0xFF9B30FF, false);
		} else if (mode == 2) {
			String text = "Feeding...";
			int tw = font.width(text);
			gfx.drawString(font, text, this.imageWidth - tw - 4, CRAFT_AREA_HEIGHT - 10, 0xFF22AA22, false);
		}
	}

	// ───── Red gradient inventory background ─────

	private void renderRedGradientBackground(GuiGraphics gfx, int x, int y, int w, int h) {
		// Render a vertical gradient from darker red at top to lighter at bottom
		for (int row = 0; row < h; row++) {
			float t = (float) row / h;
			// Dark blood-red at top → muted lighter red at bottom
			int r = (int) (60 + 100 * t);
			int g = (int) (10 + 40 * t);
			int b = (int) (10 + 30 * t);
			int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
			gfx.fill(x, y + row, x + w, y + row + 1, color);
		}
	}

	// ───── Slot background rendering ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
		// Outer border (dark edge)
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		// Inner fill
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		// Bottom/right highlight
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

		// Special tint for output slot
		if (slotIndex == MorphlingIncubatorMenu.OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
		// Special tint for blood slot
		if (slotIndex == MorphlingIncubatorMenu.BLOOD_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA0000);
		}
		// Special tint for center slot
		if (slotIndex == MorphlingIncubatorMenu.CENTER_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x208B008B);
		}
	}

	// ───── Progress ring (around center slot) ─────

	private void renderProgressRing(GuiGraphics gfx, int gx, int gy) {
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

		float time = System.nanoTime() / 1_000_000_000f;

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

	private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
		// Vertical vial bar directly above the blood slot (blood slot is at 8, 74)
		int barW = 8;
		int barH = 52;
		int barX = gx + 8 + (16 - barW) / 2; // centered above the 16px slot
		int barY = gy + 74 - barH - 4;        // 4px gap above the slot

		// Store bounds for hover tooltip
		bloodBarX1 = barX - 2;
		bloodBarY1 = barY - 2;
		bloodBarX2 = barX + barW + 2;
		bloodBarY2 = barY + barH + 2;

		double vol = te.getBloodVolume();
		double maxVol = te.getMaxBloodVolume();
		double ratio = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

		float time = System.nanoTime() / 1_000_000_000f;

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

	// ───── Programmatic Dark-Red Border ─────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 1, BORDER_OUTER);
		gfx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		gfx.fill(x, y, x + 1, y + h, BORDER_OUTER);
		gfx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);

		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
	}

	// ───── Procedural Animated Vein Background ─────

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: solid near-black base
		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		// Layer 2: subtle dark-red radial glow in the center
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (35 * (1f - t));
			int red = (int) (40 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated vein tendrils
		float time = (System.nanoTime() / 1_000_000_000f);
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: subtle noise-like speckles
		Random speckRand = new Random(12345L);
		for (int s = 0; s < 80; s++) {
			int sx = gx + speckRand.nextInt(gw);
			int sy = gy + speckRand.nextInt(gh);
			int sr = 10 + speckRand.nextInt(20);
			int sg = speckRand.nextInt(6);
			int sa = 15 + speckRand.nextInt(25);
			graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics graphics, int index, float time,
			int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		float timeOffset = time * speed * 2.0f;

		int baseRed = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2 + 8 * brightness);
		int baseBlue = (int) (5 + 5 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5f - displacement * sinA;
			float py = startY + step * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

			int a = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			graphics.fill(ix, iy, ix + thickness, iy + thickness,
					(a << 24) | (r << 16) | (g << 8) | b);
		}
	}
}

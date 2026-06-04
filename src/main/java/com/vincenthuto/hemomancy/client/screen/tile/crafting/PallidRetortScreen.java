package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.WhiteHumorBarWidget;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.PallidRetortMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.PallidRetortBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Random;

/**
 * Pallid Retort screen — fully programmatic, no texture files.
 * Renders a white humor distillery UI with a diamond/blue crystalline background,
 * white humor tank, progress arrow, heat indicator, and item slots.
 */
public class PallidRetortScreen extends AbstractContainerScreen<PallidRetortMenu> {

	// ── Colors (pale blue / silver Unstained theme) ──
	private static final int SLOT_BG = 0xFF101418;
	private static final int SLOT_BORDER_DARK = 0xFF0A0F12;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A454C;
	private static final int BORDER_OUTER = 0xFF22303A;
	private static final int BORDER_INNER = 0xFF172028;

	private static final int CRAFT_AREA_HEIGHT = 86;
	private static final int RHOMBUS_COUNT     = 5;

	final PallidRetortBlockEntity te;
	private float[][] rhombusParams;

	private WhiteHumorBarWidget.Bounds whiteHumorBarBounds = WhiteHumorBarWidget.Bounds.EMPTY;

	public PallidRetortScreen(PallidRetortMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.te = menu.getTe();
		this.imageWidth = 176;
		this.imageHeight = 176;
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

		// Seed rhombus parameters — same logic as UnstainedProgressScreen
		Random rand = new Random(99887L);
		rhombusParams = new float[RHOMBUS_COUNT][8];
		for (int i = 0; i < RHOMBUS_COUNT; i++) {
			rhombusParams[i][0] = rand.nextFloat();                             // startX ratio
			rhombusParams[i][1] = rand.nextFloat();                             // startY ratio
			rhombusParams[i][2] = 6 + rand.nextInt(24);                        // half-size
			rhombusParams[i][3] = (rand.nextFloat() - 0.5f) * 10f;             // velX (pixels/sec)
			rhombusParams[i][4] = (rand.nextFloat() - 0.5f) * 8f;              // velY (pixels/sec)
			rhombusParams[i][5] = rand.nextFloat() * (float) (Math.PI * 2);    // phase offset
			rhombusParams[i][6] = 0.5f + rand.nextFloat() * 0.5f;              // brightness
			rhombusParams[i][7] = (rand.nextFloat() - 0.5f) * 1.2f;            // rotation speed (rad/sec)
		}
	}

	// ───── Main render ─────

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);

		WhiteHumorBarWidget.renderTooltip(graphics, font, whiteHumorBarBounds,
				te.getWhiteHumorVolume(), te.getMaxWhiteHumorVolume(), mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
		int gx = this.leftPos;
		int gy = this.topPos;
		int gw = this.imageWidth;
		int gh = this.imageHeight;

		// ── Diamond/blue crystalline background for upper crafting area ──
		renderDiamondBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
		drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

		// ── Blue/silver gradient panel behind inventory section ──
		Slot firstInventorySlot = this.menu.slots.get(PallidRetortMenu.SLOT_COUNT);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.PALLID,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		// ── Draw slot backgrounds ──
		for (int i = 0; i < PallidRetortMenu.SLOT_COUNT; i++) {
			Slot slot = this.menu.slots.get(i);
			int sx = gx + slot.x;
			int sy = gy + slot.y;
			drawSlotBackground(gfx, sx, sy, slot.index);
		}
		animTime += 0.016f; // ~60 FPS approximation
		// ── Heat indicator (flame area below input slot) ──
		renderHeatIndicator(gfx, gx, gy);

		// ── Progress arrow from input → output ──
		renderProgressArrow(gfx, gx, gy);

		// ── White Humor volume bar ──
		renderWhiteHumorBar(gfx, gx, gy);
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		// Title centered
		gfx.drawString(font, this.title, this.titleLabelX, 4, 0xFFD8E7F0, false);
		// Inventory label

		// Heat status text below heat indicator
		if (this.menu.isHeated()) {
			gfx.drawString(font, Component.literal("Refining"), 36, 56, 0xFFC8D8E8, false);
		} else {
			gfx.drawString(font, Component.literal("No Coolant"), 40, 56, 0xFF8899AA, false);
		}
	}

	// ───── Blue/silver gradient inventory background ─────

	// ───── Slot backgrounds ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
		// Outer border (dark edge)
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		// Inner fill
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		// Bottom/right highlight
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

		// Special tint for result slot (reddish)
		if (slotIndex == PallidRetortMenu.RESULT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20DDEEFF);
		}
		// Special tint for flask slot (dark red)
		if (slotIndex == PallidRetortMenu.FLASK_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x2088A4B8);
		}
		// Special tint for flask output slot
		if (slotIndex == PallidRetortMenu.FLASK_OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20DDEEFF);
		}
		// Tint for input slot (slight highlight)
		if (slotIndex == PallidRetortMenu.INGREDIENT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15DCEBFF);
		}
	}

	// ───── Heat indicator ─────
	private float animTime = 0f;

	private void renderHeatIndicator(GuiGraphics gfx, int gx, int gy) {
		// Flame area centered below the input slot
		int flameX = gx + 44;
		int flameY = gy + 50;
		int flameW = 16;
		int flameH = 14;



		float time = animTime;
		if (this.menu.isHeated()) {
			// Animated flame — draw pixel fire from bottom up
			for (int row = 0; row < flameH; row++) {
				float rowT = (float) row / flameH; // 0 at top, 1 at bottom
				float intensity = rowT; // brighter at bottom
				float flicker = 0.7f + 0.3f * Mth.sin(time * 8f + row * 0.5f);

				for (int col = 0; col < flameW; col++) {
					// Taper the flame: narrower at top
					float center = flameW / 2f;
					float dist = Math.abs(col - center) / center;
					float taper = 1f - dist * (1f - rowT * 0.6f);
					if (taper < 0.2f) continue;

					float wave = Mth.sin(time * 6f + col * 0.7f + row * 0.3f) * 0.15f;
					float alpha = Mth.clamp(intensity * taper * flicker + wave, 0, 1);

					// Color: orange at bottom → red at top
					int r = (int) (20 * alpha);
					int g = (int) (Mth.clamp(180 * rowT * alpha, 0, 255));
					int b = (int) (255 * alpha);
					int a = (int) (220 * alpha);
					if (a < 10) continue;

					int px = flameX + col;
					int py = flameY + flameH - 1 - row;
					gfx.fill(px, py, px + 1, py -1, (a << 24) | (r << 16) | (g << 8) | b);
				}
			}
		} else {
			// Cold — dim ash dots
			Random r = new Random(999L);
			for (int i = 0; i < 8; i++) {
				int dx = r.nextInt(flameW);
				int dy = r.nextInt(flameH / 2) + flameH / 2;
				gfx.fill(flameX + dx, flameY + dy, flameX + dx + 1, flameY + dy + 1, 0x30666666);
			}
		}
	}

	// ───── Progress arrow ─────

	private void renderProgressArrow(GuiGraphics gfx, int gx, int gy) {
		// Arrow from input slot (44+16=60) to result slot (134) at y=32+4 = center
		int arrowX = gx + 68;
		int arrowY = gy + 36;
		int arrowFullW = 58;
		int arrowH = 8;


		float time = animTime;
				double progress = this.menu.getBurnProgress() / 24.0;
		int filledW = (int) (arrowFullW * progress);

		// Background track (dark blue-grey)
		gfx.fill(arrowX, arrowY, arrowX + arrowFullW, arrowY + arrowH, 0x40081020);
		// Inner track line
		gfx.fill(arrowX, arrowY + 3, arrowX + arrowFullW, arrowY + 5, 0x30102840);

		// Arrow head outline (always visible) — points RIGHT (narrows to tip)
		int headBaseX = arrowX + arrowFullW;  // where the shaft ends
		int midY = arrowY + arrowH / 2;
		int headLen = 5;
		for (int i = 0; i < headLen; i++) {
			int spread = headLen - 1 - i; // wide at left, narrow at right (tip)
			gfx.fill(headBaseX + i, midY - spread, headBaseX + i + 1, midY + spread + 1, 0x30204060);
		}

		if (filledW > 0) {
			// Filled portion — pulsing pale blue
			for (int col = 0; col < filledW; col++) {
				float pulse = 0.7f + 0.3f * Mth.sin(time * 4f + col * 0.15f);
				int r = (int) (60  * pulse);
				int g = (int) (140 * pulse);
				int b = (int) (220 * pulse);
				int a = (int) (200 * pulse);
				int color = (a << 24) | (r << 16) | (g << 8) | b;
				gfx.fill(arrowX + col, arrowY + 1, arrowX + col + 1, arrowY + arrowH - 1, color);
			}

			// Bright leading edge
			float edgePulse = 0.5f + 0.5f * Mth.sin(time * 6f);
			int edgeAlpha = (int) (180 * edgePulse);
			int edgeColor = (edgeAlpha << 24) | (0xC8 << 16) | (0xF0 << 8) | 0xFF;
			gfx.fill(arrowX + filledW - 1, arrowY, arrowX + filledW, arrowY + arrowH, edgeColor);

			// Filled arrow head — points RIGHT
			double headProgress = Mth.clamp((progress - 0.9) / 0.1, 0, 1);
			if (headProgress > 0) {
				int headAlpha = (int) (200 * headProgress);
				for (int i = 0; i < headLen; i++) {
					int spread = headLen - 1 - i;
					int hColor = (headAlpha << 24) | (0x80 << 16) | (0xC0 << 8) | 0xFF;
					gfx.fill(headBaseX + i, midY - spread, headBaseX + i + 1, midY + spread + 1, hColor);
				}
			}
		}
	}

	// ───── White Humor volume bar ─────

	private void renderWhiteHumorBar(GuiGraphics gfx, int gx, int gy) {
		// Vertical vial on the right side of crafting area (above the flask slot)
		int barW = 10;
		int barH = 38;
		int barX = gx + 158;
		int barY = gy + 16;

		whiteHumorBarBounds = WhiteHumorBarWidget.render(gfx, barX, barY, barW, barH,
				te.getWhiteHumorVolume(), te.getMaxWhiteHumorVolume(), animTime, BORDER_OUTER, BORDER_INNER);
	}

	// ───── Programmatic border ─────

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

	// ───── Diamond / blue background (matches UnstainedProgressScreen) ─────

	/**
	 * Dark blue base with a radial glow, floating rotating hollow rhombuses,
	 * and subtle blue speckles — identical to the UnstainedProgressScreen background.
	 */
	private void renderDiamondBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: rich dark blue base
		gfx.fill(gx, gy, gx + gw, gy + gh, 0xFF060A1E);

		// Layer 2: dark-blue → blue-white radial gradient from the centre (subtle)
		int centerX = gx + gw / 2;
		int centerY = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 3) {
			float t = (float) ring / glowRadius;           // 1.0 at edge, 0.0 at centre
			float intensity = (1f - t) * (1f - t);         // quadratic falloff
			int alpha = (int) (20 * intensity);
			int r     = (int) (200 * intensity);
			int g     = (int) (210 * intensity);
			int b     = (int) (255 * intensity);
			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			gfx.fill(centerX - ring, centerY - ring, centerX + ring, centerY + ring, color);
		}

		// Layer 3: floating hollow rhombuses

		float time = animTime;
				if (rhombusParams != null) {
			for (int i = 0; i < RHOMBUS_COUNT; i++) {
				drawFloatingRhombus(gfx, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: subtle blue-tinted speckles
		Random speckRand = new Random(54321L);
		for (int s = 0; s < 120; s++) {
			int spx = gx + speckRand.nextInt(gw);
			int spy = gy + speckRand.nextInt(gh);
			int sb  = 10 + speckRand.nextInt(20);
			int sg  = speckRand.nextInt(8);
			int sa  = 15 + speckRand.nextInt(25);
			gfx.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sg << 8) | sb);
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	/** Draws one floating hollow rhombus particle that drifts, wraps, and rotates. */
	private void drawFloatingRhombus(GuiGraphics gfx, int index, float time,
									 int gx, int gy, int gw, int gh) {
		float[] p = rhombusParams[index];
		float startXRatio = p[0];
		float startYRatio = p[1];
		int   halfSize    = (int) p[2];
		float velX        = p[3];
		float velY        = p[4];
		float phase       = p[5];
		float brightness  = p[6];
		float rotSpeed    = p[7];

		float rawX = startXRatio * gw + velX * time;
		float rawY = startYRatio * gh + velY * time;
		rawX += 5f * Mth.sin(time * 0.4f + phase);
		rawY += 4f * Mth.cos(time * 0.35f + phase * 1.3f);

		int cx = gx + ((int) rawX % gw + gw) % gw;
		int cy = gy + ((int) rawY % gh + gh) % gh;

		float angle = phase + rotSpeed * time;

		float pulse = 0.6f + 0.4f * Mth.sin(time * 0.7f + phase);
		int baseAlpha = (int) (50 + 80 * brightness * pulse);

		int r = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
		int g = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
		int b = (int) Mth.clamp(200 + 55 * brightness, 0, 255);
		int color = (baseAlpha << 24) | (r << 16) | (g << 8) | b;

		int thickness = 3 + halfSize / 6;
		drawRotatedHollowRhombus(gfx, cx, cy, halfSize, thickness, angle, color);
	}

	/**
	 * Draws a hollow diamond/rhombus ring rotated by the given angle.
	 * Tests each pixel against the rotated diamond distance function (|u|+|v|).
	 */
	private void drawRotatedHollowRhombus(GuiGraphics gfx, int cx, int cy,
										  int halfSize, int thickness, float angle, int color) {
		float cosA = Mth.cos(angle);
		float sinA = Mth.sin(angle);
		int innerSize = halfSize - thickness;
		int bound = halfSize + 1;

		for (int dy = -bound; dy <= bound; dy++) {
			int spanStart = Integer.MIN_VALUE;

			for (int dx = -bound; dx <= bound; dx++) {
				float u = dx * cosA + dy * sinA;
				float v = -dx * sinA + dy * cosA;
				float dist = Math.abs(u) + Math.abs(v);
				boolean inRing = dist <= halfSize && (innerSize <= 0 || dist >= innerSize);

				if (inRing) {
					if (spanStart == Integer.MIN_VALUE) spanStart = dx;
				} else {
					if (spanStart != Integer.MIN_VALUE) {
						gfx.fill(cx + spanStart, cy + dy, cx + dx, cy + dy + 1, color);
						spanStart = Integer.MIN_VALUE;
					}
				}
			}
			if (spanStart != Integer.MIN_VALUE) {
				gfx.fill(cx + spanStart, cy + dy, cx + bound + 1, cy + dy + 1, color);
			}
		}
	}
}

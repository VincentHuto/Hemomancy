package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Vial Centrifuge screen — fully programmatic, no texture files.
 * Renders a blood centrifuge UI with vein background, spinning progress ring,
 * blood tank, item slots, and a start button.
 */
public class VialCentrifugeScreen extends AbstractContainerScreen<VialCentrifugeMenu> {

	// ── Colors (matching the Hemomancy blood-themed style) ──
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;

	private static final int CRAFT_AREA_HEIGHT = 114;
	private static final int VEIN_COUNT = 20;

	// Ring center for the vial slot arrangement (in GUI-relative coords)
	private static final int RING_CX = 77;
	private static final int RING_CY = 50;

	final VialCentrifugeBlockEntity te;
	private float[][] veinParams;

	// Blood bar screen-space bounds for hover detection
	private int bloodBarX1, bloodBarY1, bloodBarX2, bloodBarY2;

	// Start button bounds (center of the ring)
	private int btnX1, btnY1, btnX2, btnY2;

	public VialCentrifugeScreen(VialCentrifugeMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.te = menu.getTe();
		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 2;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

		// Seed vein parameters for the animated background
		Random rand = new Random(27182L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
			veinParams[i][4] = 8f + rand.nextFloat() * 16f;
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
			veinParams[i][6] = 50 + rand.nextInt(110);
			veinParams[i][7] = 1 + rand.nextInt(2);
			veinParams[i][8] = rand.nextFloat();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ───── Main render ─────

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);

		// Blood bar hover tooltip
		if (mouseX >= bloodBarX1 && mouseX < bloodBarX2 && mouseY >= bloodBarY1 && mouseY < bloodBarY2) {
			double vol = te.getBloodVolume();
			double maxVol = te.getMaxBloodVolume();
			graphics.renderTooltip(font, List.of(
					Component.literal(String.format("\u00A74Blood: \u00A7c%.0f \u00A74/ \u00A7c%.0f", vol, maxVol))
			), java.util.Optional.empty(), mouseX, mouseY);
		}

		// Start button hover tooltip
		if (!this.menu.isSpinning() && mouseX >= btnX1 && mouseX < btnX2 && mouseY >= btnY1 && mouseY < btnY2) {
			graphics.renderTooltip(font, List.of(
					Component.literal("\u00A7cStart Centrifuge")
			), java.util.Optional.empty(), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Check if click is on the start button
		if (button == 0 && !this.menu.isSpinning()
				&& mouseX >= btnX1 && mouseX < btnX2 && mouseY >= btnY1 && mouseY < btnY2) {
			PacketHandler.sendToServer(new StartCentrifugeButtonPacket());
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
		int gx = this.leftPos;
		int gy = this.topPos;
		int gw = this.imageWidth;
		int gh = this.imageHeight;

		// ── Venous background for upper crafting area ──
		renderVeinBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
		drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

		// ── Red gradient panel behind inventory section ──
		int invTop = gy + CRAFT_AREA_HEIGHT;
		int invH = gh - CRAFT_AREA_HEIGHT;
		renderRedGradientBackground(gfx, gx, invTop, gw, invH);

		// Separator border
		gfx.fill(gx, invTop, gx + gw, invTop + 1, BORDER_OUTER);
		gfx.fill(gx, invTop + 1, gx + gw, invTop + 2, BORDER_INNER);

		// ── Draw slot backgrounds ──
		for (Slot slot : this.menu.slots) {
			int sx = gx + slot.x;
			int sy = gy + slot.y;
			drawSlotBackground(gfx, sx, sy, slot.index);
		}

		// ── Progress ring around the vial slots ──
		renderProgressRing(gfx, gx, gy);

		// ── Start button in center of ring (when idle) / glow (when spinning) ──
		renderStartButton(gfx, gx, gy, mouseX, mouseY);

		// ── Blood volume bar ──
		renderBloodBar(gfx, gx, gy);
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		// Inventory label
		gfx.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF444444, false);

		// Status text — bottom right of craft area
		if (this.menu.isSpinning()) {
			String text = "Centrifuging...";
			int tw = font.width(text);
			gfx.drawString(font, text, this.imageWidth - tw - 4, CRAFT_AREA_HEIGHT - 10, 0xFFCC2222, false);
		}
	}

	// ───── Red gradient inventory background ─────

	private void renderRedGradientBackground(GuiGraphics gfx, int x, int y, int w, int h) {
		for (int row = 0; row < h; row++) {
			float t = (float) row / h;
			int r = (int) (60 + 100 * t);
			int g = (int) (10 + 40 * t);
			int b = (int) (10 + 30 * t);
			int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
			gfx.fill(x, y + row, x + w, y + row + 1, color);
		}
	}

	// ───── Slot backgrounds ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
		// Outer border (dark edge)
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		// Inner fill
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		// Bottom/right highlight
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

		// Special tint for vial slots (crimson)
		if (slotIndex >= VialCentrifugeMenu.VIAL_START && slotIndex <= VialCentrifugeMenu.VIAL_END) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15CC2222);
		}
		// Output slots (reddish)
		if (slotIndex >= VialCentrifugeMenu.OUTPUT_START && slotIndex <= VialCentrifugeMenu.OUTPUT_END) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
		// Blood flask slot (dark red)
		if (slotIndex == VialCentrifugeMenu.BLOOD_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA0000);
		}
		// Aux output slot (amber)
		if (slotIndex == VialCentrifugeMenu.AUX_OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FFAA44);
		}
		// Flask output slot (dark red, like blood slot)
		if (slotIndex == VialCentrifugeMenu.FLASK_OUTPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
		// Input slot (slight highlight)
		if (slotIndex == VialCentrifugeMenu.INPUT_SLOT) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15FFAA88);
		}
	}

	// ───── Progress ring (around the vial slots) ─────

	private void renderProgressRing(GuiGraphics gfx, int gx, int gy) {
		int cx = gx + RING_CX;
		int cy = gy + RING_CY;
		int outerRadius = 42;
		int innerRadius = 37;
		int segments = 64;

		double progress = 0;
		if (this.menu.isSpinning()) {
			progress = this.menu.getSpinProgress() / 24.0;
		}
		int filledSegments = (int) (segments * progress);

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		for (int i = 0; i < segments; i++) {
			double angle1 = -Math.PI / 2 + (2 * Math.PI / segments) * i;
			double angle2 = -Math.PI / 2 + (2 * Math.PI / segments) * (i + 1);

			for (int r = innerRadius; r <= outerRadius; r++) {
				for (double a = angle1; a < angle2; a += 0.04) {
					int px = cx + (int) (r * Math.cos(a));
					int py = cy + (int) (r * Math.sin(a));

					int color;
					if (i < filledSegments) {
						float pulse = 0.7f + 0.3f * Mth.sin(time * 3f + i * 0.1f);
						int red = (int) (200 * pulse);
						int green = (int) (20 * pulse);
						color = (0xDD << 24) | (red << 16) | (green << 8) | 0x15;
					} else {
						color = 0x30200808;
					}
					gfx.fill(px, py, px + 1, py + 1, color);
				}
			}
		}

		// Glow at leading edge
		if (filledSegments > 0 && filledSegments < segments) {
			double leadAngle = -Math.PI / 2 + (2 * Math.PI / segments) * filledSegments;
			int leadX = cx + (int) ((innerRadius + outerRadius) / 2.0 * Math.cos(leadAngle));
			int leadY = cy + (int) ((innerRadius + outerRadius) / 2.0 * Math.sin(leadAngle));

			float pulse = 0.5f + 0.5f * Mth.sin(time * 5f);
			int glowAlpha = (int) (120 * pulse);
			int glowColor = (glowAlpha << 24) | (0xFF << 16) | (0x30 << 8) | 0x20;
			gfx.fill(leadX - 1, leadY - 1, leadX + 2, leadY + 2, glowColor);
		}
	}

	// ───── Start button (center of ring) ─────

	private void renderStartButton(GuiGraphics gfx, int gx, int gy, int mouseX, int mouseY) {
		int btnW = 16;
		int btnH = 16;
		int bx = gx + RING_CX - btnW / 2;
		int by = gy + RING_CY - btnH / 2;

		// Update bounds for click / hover detection
		btnX1 = bx;
		btnY1 = by;
		btnX2 = bx + btnW;
		btnY2 = by + btnH;

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		if (this.menu.isSpinning()) {
			// Spinning — pulsing center glow
			float spinPulse = 0.5f + 0.5f * Mth.sin(time * 4f);
			int alpha = (int) (80 * spinPulse);
			int red = (int) (180 * spinPulse);
			gfx.fill(bx + 3, by + 3, bx + btnW - 3, by + btnH - 3,
					(alpha << 24) | (red << 16) | 0x0808);
		} else {
			// Idle — clickable start button with play triangle icon
			boolean hovered = mouseX >= btnX1 && mouseX < btnX2 && mouseY >= btnY1 && mouseY < btnY2;
			int bgColor = hovered ? 0xDD4A0E0E : 0xBB2A0808;
			int borderColor = hovered ? 0xFFBB3333 : 0xFF772222;

			gfx.fill(bx, by, bx + btnW, by + btnH, borderColor);
			gfx.fill(bx + 1, by + 1, bx + btnW - 1, by + btnH - 1, bgColor);

			// Play triangle (pointing right)
			int triColor = hovered ? 0xFFFF5555 : 0xFFCC3333;
			int triX = bx + 5;
			int triCY = by + btnH / 2;
			for (int col = 0; col < 6; col++) {
				int halfH = 5 - col;
				for (int row = -halfH; row <= halfH; row++) {
					gfx.fill(triX + col, triCY + row, triX + col + 1, triCY + row + 1, triColor);
				}
			}
		}
	}

	// ───── Blood volume bar ─────
	private float animTime = 0f;

	private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
		int barW = 8;
		int barH = 52;
		int barX = gx + 8 + (16 - barW) / 2;
		int barY = gy + 90 - barH - 4;

		// Store bounds for hover tooltip
		bloodBarX1 = barX - 2;
		bloodBarY1 = barY - 2;
		bloodBarX2 = barX + barW + 2;
		bloodBarY2 = barY + barH + 2;

		double vol = te.getBloodVolume();
		double maxVol = te.getMaxBloodVolume();
		double ratio = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		// Outer frame — double border
		gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, BORDER_OUTER);
		gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, BORDER_INNER);

		// Inner dark background
		gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF060102);

		// Fill from bottom up with vertical gradient
		int fillH = (int) (barH * ratio);
		if (fillH > 0) {
			int fillTop = barY + barH - fillH;
			for (int row = 0; row < fillH; row++) {
				float rowT = (float) row / fillH;
				float pulse = 0.75f + 0.25f * Mth.sin(time * 2.5f + row * 0.08f);
				int r = (int) (Mth.clamp((100 + 80 * rowT) * pulse, 0, 255));
				int g = (int) (Mth.clamp((5 + 15 * rowT) * pulse, 0, 255));
				int b = (int) (Mth.clamp((8 + 10 * rowT) * pulse, 0, 255));
				int color = (0xEE << 24) | (r << 16) | (g << 8) | b;
				gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1, color);
			}

			// Meniscus highlight
			float meniscusPulse = 0.6f + 0.4f * Mth.sin(time * 3f);
			int mAlpha = (int) (200 * meniscusPulse);
			int meniscusColor = (mAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x18;
			gfx.fill(barX, fillTop, barX + barW, fillTop + 1, meniscusColor);

			// Specular highlight — thin bright strip on the left
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

	// ───── Procedural animated vein background ─────

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
			int alpha = (int) (30 * (1f - t));
			int red = (int) (35 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated vein tendrils
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
				if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: noise speckles
		Random speckRand = new Random(13579L);
		for (int s = 0; s < 70; s++) {
			int sx = gx + speckRand.nextInt(gw);
			int sy = gy + speckRand.nextInt(gh);
			int sr = 10 + speckRand.nextInt(20);
			int sg = speckRand.nextInt(6);
			int sa = 15 + speckRand.nextInt(20);
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

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

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

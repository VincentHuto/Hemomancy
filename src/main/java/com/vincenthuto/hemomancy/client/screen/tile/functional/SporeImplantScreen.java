package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.menu.slot.ScarSlot;
import com.vincenthuto.hemomancy.common.menu.slot.SelectiveEquipmentTypeSlot;
import com.vincenthuto.hemomancy.common.menu.tile.functional.SporeImplantMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class SporeImplantScreen extends AbstractContainerScreen<SporeImplantMenu> {

	// Fungal palette — reddish yellow / amber tones
	private static final int BG_BASE = 0xFF1A0D04;
	private static final int SLOT_BG = 0xFF2A1508;
	private static final int SLOT_BORDER_DARK = 0xFF140A02;
	private static final int SLOT_BORDER_LIGHT = 0xFF4A2A10;

	// Border gradient endpoints
	private static final int BORDER_RED = 0xFFAA2200;
	private static final int BORDER_YELLOW = 0xFFCC8800;
	private static final int BORDER_INNER = 0xFF3A1A08;

	private static final int TENDRIL_COUNT = 18;
	private static final int SPORE_COUNT = 40;

	// Upper fungal area height (where scar slots sit)
	private static final int FUNGAL_AREA_HEIGHT = 76;
	private float[][] tendrilParams;
	private float[][] sporeParams;
	private int[][] speckleParams;

	private float animTime = 0f;


	public SporeImplantScreen(SporeImplantMenu container, Inventory inventory, Component name) {
		super(container, inventory, name);
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = FUNGAL_AREA_HEIGHT + 7;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

		// Seed tendril parameters
		Random rand = new Random(0xF04A1L);
		tendrilParams = new float[TENDRIL_COUNT][9];
		for (int i = 0; i < TENDRIL_COUNT; i++) {
			tendrilParams[i][0] = rand.nextFloat();           // startX ratio
			tendrilParams[i][1] = rand.nextFloat();           // startY ratio
			tendrilParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // baseAngle
			tendrilParams[i][3] = 0.2f + rand.nextFloat() * 0.6f;          // speed
			tendrilParams[i][4] = 6f + rand.nextFloat() * 14f;             // amplitude
			tendrilParams[i][5] = 0.05f + rand.nextFloat() * 0.1f;         // frequency
			tendrilParams[i][6] = 40 + rand.nextInt(80);                    // length
			tendrilParams[i][7] = 1 + rand.nextInt(2);                      // thickness
			tendrilParams[i][8] = rand.nextFloat();                          // brightness
		}

		// Seed floating spore particles
		sporeParams = new float[SPORE_COUNT][5];
		for (int i = 0; i < SPORE_COUNT; i++) {
			sporeParams[i][0] = rand.nextFloat();             // x ratio
			sporeParams[i][1] = rand.nextFloat();             // y ratio
			sporeParams[i][2] = 0.3f + rand.nextFloat() * 0.7f; // speed
			sporeParams[i][3] = rand.nextFloat() * 100f;     // phase
			sporeParams[i][4] = rand.nextFloat();             // brightness
		}

		// Seed speckle positions (static pattern, computed once)
		int speckleCount = 60;
		speckleParams = new int[speckleCount][5];
		Random speckRand = new Random(0x5E0DE);
		for (int s = 0; s < speckleCount; s++) {
			speckleParams[s][0] = speckRand.nextInt(this.imageWidth);  // x offset
			speckleParams[s][1] = speckRand.nextInt(FUNGAL_AREA_HEIGHT); // y offset
			speckleParams[s][2] = 30 + speckRand.nextInt(40);        // red
			speckleParams[s][3] = 10 + speckRand.nextInt(20);        // green
			speckleParams[s][4] = 15 + speckRand.nextInt(30);        // alpha
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);

	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
		int gx = this.leftPos;
		int gy = this.topPos;
		int gw = this.imageWidth;
		int gh = this.imageHeight;

		// ───── Fungal background for the upper scar area ─────
		renderFungalBackground(gfx, gx, gy, gw, FUNGAL_AREA_HEIGHT);
		drawBorder(gfx, gx, gy, gw, FUNGAL_AREA_HEIGHT);

		// ───── Dark amber gradient panel behind the inventory section ─────
		Slot firstInventorySlot = this.menu.slots.get(SporeImplantMenu.SLOT_COUNT);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.FUNGAL,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		// ───── Slot backgrounds ─────
		for (int i = 0; i < SporeImplantMenu.SLOT_COUNT; i++) {
			Slot slot = this.menu.slots.get(i);
			int sx = gx + slot.x;
			int sy = gy + slot.y;
			drawSlotBackground(gfx, sx, sy, slot);
		}

		// ───── Pulsing glow ring around center fungal slot ─────
//		renderFungalSlotGlow(gfx, gx, gy);

		// ───── Outer border around the entire GUI ─────
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		// Title centered in the fungal area
		gfx.drawString(font, this.title, this.titleLabelX, 3, 0xFFDD8822, false);

		// Inventory label
	}

	// ───── Fungal-themed animated background ─────

	private void renderFungalBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		// NOTE: Avoid scissor here. A mismatched scissor space (GUI coords vs window coords)
		// can clip vanilla slot/item rendering and hover tooltips after renderBg.
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: dark amber/brown base
		gfx.fill(gx, gy, gx + gw, gy + gh, BG_BASE);

		// Layer 2: warm radial glow in center
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 3) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (30 * (1f - t));
			int r = (int) (60 * (1f - t));
			int g = (int) (25 * (1f - t));
			int b = (int) (5 * (1f - t));
			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated fungal tendrils
		if (tendrilParams != null) {
			for (int i = 0; i < TENDRIL_COUNT; i++) {
				drawFungalTendril(gfx, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: floating spore particles
		if (sporeParams != null) {
			drawFloatingSpores(gfx, time, gx, gy, gw, gh);
		}

		// Layer 5: static warm speckles (pre-computed in init)
		if (speckleParams != null) {
			for (int[] sp : speckleParams) {
				int sx = gx + sp[0];
				int sy = gy + sp[1];
				int sr = sp[2];
				int sg = sp[3];
				int sa = sp[4];
				gfx.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
			}
		}

		RenderSystem.disableBlend();
	}

	private void drawFungalTendril(GuiGraphics gfx, int index, float time,
			int gx, int gy, int gw, int gh) {
		float[] p = tendrilParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.12f * Mth.sin(time * speed * 0.4f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		float timeOffset = time * speed * 1.5f;

		// Fungal palette: amber-red-yellow tones
		int baseRed = (int) (50 + 80 * brightness);
		int baseGreen = (int) (20 + 40 * brightness);
		int baseBlue = (int) (2 + 8 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.25f) * Mth.sin(frequency * 2.5f * step + timeOffset * 1.3f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5f - displacement * sinA;
			float py = startY + step * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			float tipFade = 1f;
			if (step < 8) tipFade = step / 8f;
			else if (step > length - 8) tipFade = (length - step) / 8f;

			float pulse = 0.65f + 0.35f * Mth.sin(time * 1.2f + index * 0.6f + step * 0.025f);

			int a = (int) (Mth.clamp(tipFade * pulse * 160, 15, 180));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.4f, 0, 255);

			gfx.fill(ix, iy, ix + thickness, iy + thickness,
					(a << 24) | (r << 16) | (g << 8) | b);
		}
	}

	private void drawFloatingSpores(GuiGraphics gfx, float time, int gx, int gy, int gw, int gh) {
		for (int i = 0; i < SPORE_COUNT; i++) {
			float[] p = sporeParams[i];
			float baseX = p[0] * gw;
			float baseY = p[1] * gh;
			float speed = p[2];
			float phase = p[3];
			float brightness = p[4];

			// Drift upward slowly with horizontal wobble
			float yOffset = ((time * speed * 8f + phase) % (gh + 10)) - 5;
			float xWobble = 3f * Mth.sin(time * speed * 2f + phase);

			int sx = gx + Math.floorMod((int) (baseX + xWobble), gw);
			int sy = gy + gh - (int) yOffset;

			if (sy < gy || sy >= gy + gh || sx < gx || sx >= gx + gw) continue;

			float pulse = 0.5f + 0.5f * Mth.sin(time * 3f + phase);
			int alpha = (int) (40 + 60 * pulse * brightness);
			int r = (int) (120 + 80 * brightness);
			int g = (int) (60 + 40 * brightness);
			int b = (int) (5 + 10 * brightness);

			gfx.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (r << 16) | (g << 8) | b);
		}
	}

	// ───── Pulsing glow around the central fungal slot ─────

	private void renderFungalSlotGlow(GuiGraphics gfx, int gx, int gy) {
		// Center fungal scar slot is at (80, 35) in GUI coords
		int cx = gx + 80 + 8; // center of the 16px slot
		int cy = gy + 35 + 8;
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		int outerRadius = 18;
		int innerRadius = 12;

		for (int ring = outerRadius; ring >= innerRadius; ring--) {
			float t = (float) (ring - innerRadius) / (outerRadius - innerRadius);
			float pulse = 0.4f + 0.6f * Mth.sin(time * 2.5f + t * 3f);

			int alpha = (int) (30 * pulse * (1f - t));
			int r = (int) (180 * pulse);
			int g = (int) (80 * pulse);
			int b = (int) (10 * pulse);

			int color = (alpha << 24) | (r << 16) | (g << 8) | b;

			// Draw ring approximation as a filled square with inner cut-out effect
			gfx.fill(cx - ring, cy - ring, cx + ring, cy - ring + 1, color);
			gfx.fill(cx - ring, cy + ring - 1, cx + ring, cy + ring, color);
			gfx.fill(cx - ring, cy - ring, cx - ring + 1, cy + ring, color);
			gfx.fill(cx + ring - 1, cy - ring, cx + ring, cy + ring, color);
		}
	}

	// ───── Red-to-yellow gradient border ─────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 1, BORDER_RED);
		gfx.fill(x, y + h - 1, x + w, y + h, BORDER_YELLOW);
		gfx.fill(x, y, x + 1, y + h, BORDER_RED);
		gfx.fill(x + w - 1, y, x + w, y + h, BORDER_YELLOW);
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
	}

	// ───── Dark amber inventory background ─────

	// ───── Slot background rendering ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, Slot slot) {
		// Outer border (dark edge)
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		// Inner fill
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		// Bottom/right highlight
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

		// Special amber tint for the central fungal scar slot
		if (slot instanceof SelectiveEquipmentTypeSlot) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x25CC6600);
		}
		// Subtle warm tint for the surrounding scar slots
		else if (slot instanceof ScarSlot) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15AA4400);
		}
	}

}

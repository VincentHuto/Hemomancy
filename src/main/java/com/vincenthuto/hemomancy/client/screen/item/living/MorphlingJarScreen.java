package com.vincenthuto.hemomancy.client.screen.item.living;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingIdentity;
import com.vincenthuto.hemomancy.common.menu.MorphlingJarMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.PacketUpdateLivingStaffMorph;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MorphlingJarScreen extends AbstractContainerScreen<MorphlingJarMenu> {

	private static final int DISPLAY_X = 58;
	private static final int DISPLAY_Y = 18;
	private static final int DISPLAY_W = 116;
	private static final int DISPLAY_H = 76;
	private static final int TOP_AREA_HEIGHT = 116;
	private static final int ICON = 16;
	private static final int HIT_R = 14;
	private static final int VEIN_COUNT = 22;
	private static final int BUBBLE_COUNT = 14;

	private static final int SLOT_BG = 0xFF071607;
	private static final int SLOT_BG_JAR = 0xFF0A1D0A;
	private static final int SLOT_BORDER_DARK = 0xFF010501;
	private static final int SLOT_BORDER_LIGHT = 0xFF244E24;
	private static final int SLOT_BORDER_ACTIVE = 0xFFFFB000;
	private static final int BORDER_OUTER = 0xFF0B250B;
	private static final int BORDER_INNER = 0xFF1C4A1C;

	private static final Map<Item, boolean[][]> PIXEL_MASK_CACHE = new HashMap<>();

	private float[] speedX;
	private float[] speedY;
	private float[] phaseX;
	private float[] phaseY;
	private float[][] veinParams;
	private float[][] bubbleParams;
	private int hoveredIndex = -1;
	private int activeIndex = -1;
	private long openTick;

	public MorphlingJarScreen(MorphlingJarMenu container, Inventory playerInventory, Component name) {
		super(container, playerInventory, name);
		this.imageWidth = MorphlingJarMenu.SCREEN_WIDTH;
		this.imageHeight = MorphlingJarMenu.SCREEN_HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = 0;
		this.titleLabelY = 6;
		this.inventoryLabelX = MorphlingJarMenu.PLAYER_INV_X;
		this.inventoryLabelY = MorphlingJarMenu.PLAYER_INV_Y - 11;
		this.openTick = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;

		int slotCount = Math.max(this.menu.slotcount, 0);
		this.speedX = new float[slotCount];
		this.speedY = new float[slotCount];
		this.phaseX = new float[slotCount];
		this.phaseY = new float[slotCount];
		for (int i = 0; i < slotCount; i++) {
			this.speedX[i] = 0.4f + i * 0.15f;
			this.speedY[i] = 0.3f + i * 0.12f;
			this.phaseX[i] = i * 2.399f;
			this.phaseY[i] = i * 3.883f;
		}

		Random rand = new Random(71L);
		this.veinParams = new float[VEIN_COUNT][10];
		for (int i = 0; i < VEIN_COUNT; i++) {
			this.veinParams[i][0] = rand.nextFloat();
			this.veinParams[i][1] = rand.nextFloat();
			this.veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			this.veinParams[i][3] = 0.4f + rand.nextFloat() * 0.8f;
			this.veinParams[i][4] = 14f + rand.nextFloat() * 28f;
			this.veinParams[i][5] = 0.08f + rand.nextFloat() * 0.16f;
			this.veinParams[i][6] = 70 + rand.nextInt(120);
			this.veinParams[i][7] = 1 + rand.nextInt(2);
			this.veinParams[i][8] = rand.nextFloat();
			this.veinParams[i][9] = (rand.nextFloat() - 0.5f) * 0.06f;
		}

		this.bubbleParams = new float[BUBBLE_COUNT][6];
		for (int i = 0; i < BUBBLE_COUNT; i++) {
			this.bubbleParams[i][0] = rand.nextFloat();
			this.bubbleParams[i][1] = 0.015f + rand.nextFloat() * 0.035f;
			this.bubbleParams[i][2] = 2f + rand.nextFloat() * 4f;
			this.bubbleParams[i][3] = 1.5f + rand.nextFloat() * 2.5f;
			this.bubbleParams[i][4] = 1f + rand.nextFloat() * 2f;
			this.bubbleParams[i][5] = rand.nextFloat();
		}

		refreshActiveIndex();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderTransparentBackground(graphics);
		this.renderBg(graphics, partialTick, mouseX, mouseY);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderMorphlingDisplay(graphics, mouseX, mouseY, partialTicks);
		if (this.hoveredIndex >= 0 && this.menu.handler != null) {
			ItemStack hoverStack = this.menu.handler.getStackInSlot(this.hoveredIndex);
			if (!hoverStack.isEmpty()) {
				graphics.renderTooltip(this.font, hoverStack, mouseX, mouseY);
				return;
			}
		}
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int gx = this.leftPos;
		int gy = this.topPos;
		float time = getAnimationTime(partialTicks);

		renderVeinBackground(graphics, gx, gy, this.imageWidth, TOP_AREA_HEIGHT, time);
		drawBorder(graphics, gx, gy, this.imageWidth, TOP_AREA_HEIGHT);
		Slot firstInventorySlot = this.menu.slots.get(this.menu.slotcount);
		InventoryPanelTextures.blit(graphics, InventoryPanelTextures.MORPHIC,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		renderDisplayFrame(graphics, gx + DISPLAY_X, gy + DISPLAY_Y, DISPLAY_W, DISPLAY_H, time);

		for (int menuIndex = 0; menuIndex < this.menu.slots.size(); menuIndex++) {
			if (menuIndex >= this.menu.slotcount) {
				continue;
			}
			Slot slot = this.menu.slots.get(menuIndex);
			boolean jarSlot = menuIndex < this.menu.slotcount;
			boolean activeSlot = jarSlot && menuIndex == this.activeIndex;
			drawSlotBackground(graphics, gx + slot.x, gy + slot.y, jarSlot, activeSlot);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		int titleWidth = this.font.width(this.title);
		graphics.drawString(this.font, this.title, (this.imageWidth - titleWidth) / 2, this.titleLabelY,
				0xFF9BCB8D, false);
		if (this.menu.slotcount > 0) {
			String fillText = this.menu.getFilledSlotCount() + "/" + this.menu.slotcount;
			graphics.drawString(this.font, fillText, DISPLAY_X + DISPLAY_W - this.font.width(fillText) - 4,
					DISPLAY_Y + DISPLAY_H + 4, 0xFF7EB96E, false);
		}
	}

	private void refreshActiveIndex() {
		this.activeIndex = -1;
		Player player = this.minecraft != null ? this.minecraft.player : null;
		if (player == null || this.menu.handler == null) {
			return;
		}

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			ItemStack equipped = cap.getEquippedMorphling();
			if (equipped.isEmpty()) {
				return;
			}
			for (int i = 0; i < this.menu.slotcount; i++) {
				if (MorphlingIdentity.matches(this.menu.handler.getStackInSlot(i), equipped)) {
					this.activeIndex = i;
					break;
				}
			}
		});
	}

	private float getAnimationTime(float partialTicks) {
		if (this.minecraft != null && this.minecraft.level != null) {
			long elapsed = this.minecraft.level.getGameTime() - this.openTick;
			return (elapsed + partialTicks) / 20.0f;
		}
		return Util.getMillis() * 0.001f;
	}

	private int[] getMorphPos(int index, float time, int displayX, int displayY) {
		if (this.speedX == null || index < 0 || index >= this.speedX.length) {
			return new int[] { displayX + DISPLAY_W / 2 - ICON / 2, displayY + DISPLAY_H / 2 - ICON / 2 };
		}
		float sx = Mth.sin(time * this.speedX[index] + this.phaseX[index]);
		float sy = Mth.sin(time * this.speedY[index] + this.phaseY[index]);
		float wx = 0.03f * Mth.sin(time * this.speedX[index] * 2.0f + this.phaseX[index] + 1.0f);
		float wy = 0.03f * Mth.sin(time * this.speedY[index] * 1.8f + this.phaseY[index] + 2.0f);
		float nx = Mth.clamp(0.5f + 0.42f * sx + wx, 0.02f, 0.98f);
		float ny = Mth.clamp(0.5f + 0.40f * sy + wy, 0.02f, 0.98f);
		int cx = displayX + 4 + (int) (nx * (DISPLAY_W - ICON - 8));
		int cy = displayY + 4 + (int) (ny * (DISPLAY_H - ICON - 8));
		return new int[] { cx, cy };
	}

	private void renderMorphlingDisplay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.hoveredIndex = -1;
		if (this.menu.handler == null || this.speedX == null) {
			return;
		}

		int displayX = this.leftPos + DISPLAY_X;
		int displayY = this.topPos + DISPLAY_Y;
		float time = getAnimationTime(partialTicks);

		graphics.enableScissor(displayX + 1, displayY + 1, displayX + DISPLAY_W - 1, displayY + DISPLAY_H - 1);
		for (int i = 0; i < this.menu.slotcount; i++) {
			ItemStack morphStack = this.menu.handler.getStackInSlot(i);
			if (morphStack.isEmpty()) {
				continue;
			}

			int[] pos = getMorphPos(i, time, displayX, displayY);
			int iconX = pos[0];
			int iconY = pos[1];
			float dx = mouseX - (iconX + ICON / 2f);
			float dy = mouseY - (iconY + ICON / 2f);
			boolean hovered = dx * dx + dy * dy <= HIT_R * HIT_R;
			boolean active = i == this.activeIndex;

			if (hovered) {
				this.hoveredIndex = i;
			}

			graphics.renderItem(morphStack, iconX, iconY);
			graphics.renderItemDecorations(this.font, morphStack, iconX, iconY);

			if (active || hovered) {
				int outlineColor = active ? SLOT_BORDER_ACTIVE : 0xFF61D978;
				drawPixelOutline(graphics, iconX, iconY, getPixelMask(morphStack), outlineColor);
			}
			if (active) {
				drawActivePip(graphics, iconX + ICON - 2, iconY - 3);
			}
		}
		graphics.disableScissor();
	}

	private void renderDisplayFrame(GuiGraphics graphics, int x, int y, int w, int h, float time) {
		drawRectBorder(graphics, x - 3, y - 3, w + 6, h + 6, 1, 0xDD020802);
		drawRectBorder(graphics, x - 2, y - 2, w + 4, h + 4, 1, 0xFF143214);
		graphics.fill(x - 1, y - 1, x + w + 1, y, 0xFF071607);
		graphics.fill(x - 1, y + h, x + w + 1, y + h + 1, 0xFF071607);
		graphics.fill(x - 1, y, x, y + h, 0xFF071607);
		graphics.fill(x + w, y, x + w + 1, y + h, 0xFF071607);

		int pulseAlpha = 35 + (int) (18 * (0.5f + 0.5f * Mth.sin(time * 2.2f)));
		int pulseColor = ((pulseAlpha / 2) << 24) | 0x003B9A34;
		graphics.fill(x + 4, y + 4, x + w - 4, y + 5, pulseColor);
		graphics.fill(x + 4, y + h - 5, x + w - 4, y + h - 4, pulseColor);
		graphics.fill(x + 4, y + 4, x + 5, y + h - 4, pulseColor);
		graphics.fill(x + w - 5, y + 4, x + w - 4, y + h - 4, pulseColor);
		graphics.renderOutline(x, y, w, h, 0x883D8D3D);
	}

	private void drawRectBorder(GuiGraphics graphics, int x, int y, int w, int h, int thickness, int color) {
		graphics.fill(x, y, x + w, y + thickness, color);
		graphics.fill(x, y + h - thickness, x + w, y + h, color);
		graphics.fill(x, y, x + thickness, y + h, color);
		graphics.fill(x + w - thickness, y, x + w, y + h, color);
	}

	private void drawSlotBackground(GuiGraphics graphics, int sx, int sy, boolean jarSlot, boolean activeSlot) {
		graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, activeSlot ? SLOT_BORDER_ACTIVE : SLOT_BORDER_DARK);
		graphics.fill(sx, sy, sx + 16, sy + 16, jarSlot ? SLOT_BG_JAR : SLOT_BG);
		graphics.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		graphics.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		if (jarSlot) {
			graphics.fill(sx, sy, sx + 16, sy + 16, activeSlot ? 0x33FFD76A : 0x220BAA42);
		}
	}

	private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + 1, BORDER_OUTER);
		graphics.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		graphics.fill(x, y, x + 1, y + h, BORDER_OUTER);
		graphics.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);
		graphics.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		graphics.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
	}

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh, float time) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF020902);
		for (int row = 0; row < gh; row++) {
			float t = (float) row / Math.max(gh, 1);
			int red = (int) (3 + 10 * t);
			int green = (int) (12 + 36 * t);
			int blue = (int) (4 + 10 * t);
			graphics.fill(gx, gy + row, gx + gw, gy + row + 1,
					(0xEE << 24) | (red << 16) | (green << 8) | blue);
		}

		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 3) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (30 * (1f - t));
			int green = (int) (45 * (1f - t));
			int red = (int) (10 * (1f - t));
			int color = (alpha << 24) | (red << 16) | (green << 8) | 0x08;
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		if (this.veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawGreenVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		Random speckles = new Random(54321L);
		for (int i = 0; i < 80; i++) {
			int sx = gx + speckles.nextInt(gw);
			int sy = gy + speckles.nextInt(gh);
			int alpha = 12 + speckles.nextInt(20);
			int green = 12 + speckles.nextInt(20);
			int red = speckles.nextInt(6);
			graphics.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (red << 16) | (green << 8) | 0x05);
		}

		if (this.bubbleParams != null) {
			drawBubbles(graphics, time, gx, gy, gw, gh);
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawBubbles(GuiGraphics graphics, float time, int gx, int gy, int gw, int gh) {
		for (int i = 0; i < BUBBLE_COUNT; i++) {
			float[] bubble = this.bubbleParams[i];
			float cycle = (time * bubble[1] + bubble[5]) % 1.0f;
			float by = gy + gh - cycle * (gh + bubble[4] * 4);
			float bx = gx + bubble[0] * gw + bubble[2] * Mth.sin(time * bubble[3] + bubble[5] * 6.28f);
			int ix = (int) bx;
			int iy = (int) by;
			if (iy < gy - 4 || iy > gy + gh + 4 || ix < gx - 4 || ix > gx + gw + 4) {
				continue;
			}

			float relY = (float) (iy - gy) / Math.max(gh, 1);
			float fadeY = 1f;
			if (relY > 0.85f) {
				fadeY = (1f - relY) / 0.15f;
			} else if (relY < 0.1f) {
				fadeY = relY / 0.1f;
			}
			fadeY = Mth.clamp(fadeY, 0f, 1f);

			int radius = Math.max(1, (int) bubble[4]);
			for (int ring = radius; ring >= 0; ring--) {
				float t = radius > 0 ? (float) ring / radius : 0f;
				int alpha = (int) Mth.clamp((50 + 80 * (1f - t)) * fadeY, 0, 180);
				int green = (int) (40 + 60 * (1f - t));
				int red = (int) (10 + 20 * (1f - t));
				int blue = (int) (15 + 15 * (1f - t));
				graphics.fill(ix - ring, iy - ring, ix + ring + 1, iy + ring + 1,
						(alpha << 24) | (red << 16) | (green << 8) | blue);
			}
		}
	}

	private void drawGreenVeinTendril(GuiGraphics graphics, int index, float time, int gx, int gy, int gw, int gh) {
		float[] p = this.veinParams[index];
		float px = gx + p[0] * gw;
		float py = gy + p[1] * gh;
		float heading = p[2] + 0.3f * Mth.sin(time * p[3] * 0.25f + index);
		float liveCurvature = p[9] + 0.012f * Mth.sin(time * p[3] * 0.35f + index * 2.1f);
		int length = (int) p[6];
		int thickness = (int) p[7];
		int baseGreen = (int) (35 + 55 * p[8]);
		int baseRed = (int) (4 + 10 * p[8]);
		int baseBlue = (int) (5 + 8 * p[8]);
		float timeOffset = time * p[3] * 2.5f;

		for (int step = 0; step < length; step++) {
			heading += liveCurvature;
			float wobble = 0.08f * Mth.sin(p[5] * 2.0f * step + timeOffset)
					+ 0.05f * Mth.sin(p[5] * 4.3f * step + timeOffset * 1.7f + index);
			float currentHeading = heading + wobble;
			float squiggle = p[4] * 0.3f * Mth.sin(p[5] * step + timeOffset);
			float micro = p[4] * 0.12f * Mth.sin(p[5] * 3.4f * step + timeOffset * 1.5f + index);
			float cosH = Mth.cos(currentHeading);
			float sinH = Mth.sin(currentHeading);
			px += cosH * 1.2f - (squiggle + micro) * sinH * 0.15f;
			py += sinH * 1.2f + (squiggle + micro) * cosH * 0.15f;

			int ix = (int) px;
			int iy = (int) py;
			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			float tipFade = 1f;
			if (step < 10) {
				tipFade = step / 10f;
			} else if (step > length - 10) {
				tipFade = (length - step) / 10f;
			}
			float pulse = 0.6f + 0.4f * Mth.sin(time * 1.8f + index * 0.6f + step * 0.03f);
			int alpha = (int) Mth.clamp(tipFade * pulse * 170, 15, 190);
			int red = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int green = (int) Mth.clamp(baseGreen * pulse, 0, 255);
			int blue = (int) Mth.clamp(baseBlue * pulse * 0.4f, 0, 255);
			graphics.fill(ix, iy, ix + thickness, iy + thickness,
					(alpha << 24) | (red << 16) | (green << 8) | blue);
		}
	}

	private static boolean[][] getPixelMask(ItemStack stack) {
		Item item = stack.getItem();
		if (PIXEL_MASK_CACHE.containsKey(item)) {
			return PIXEL_MASK_CACHE.get(item);
		}

		boolean[][] mask = new boolean[16][16];
		boolean anyOpaque = false;
		try {
			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getItemRenderer().getModel(stack, null, null, 0);
			TextureAtlasSprite sprite = model.getParticleIcon();
			int spriteW = sprite.contents().width();
			int spriteH = sprite.contents().height();
			for (int px = 0; px < 16; px++) {
				for (int py = 0; py < 16; py++) {
					int sampleX = Math.min((px * spriteW) / 16, spriteW - 1);
					int sampleY = Math.min((py * spriteH) / 16, spriteH - 1);
					int pixel = sprite.getPixelRGBA(0, sampleX, sampleY);
					int alphaHigh = (pixel >> 24) & 0xFF;
					int alphaLow = pixel & 0xFF;
					boolean opaque = alphaHigh > 10 || (alphaHigh == 0 && alphaLow > 10 && pixel != 0);
					mask[px][py] = opaque;
					anyOpaque |= opaque;
				}
			}
		} catch (RuntimeException e) {
			anyOpaque = false;
		}
		if (!anyOpaque) {
			for (int px = 0; px < 16; px++) {
				for (int py = 0; py < 16; py++) {
					mask[px][py] = true;
				}
			}
		}
		PIXEL_MASK_CACHE.put(item, mask);
		return mask;
	}

	private static void drawPixelOutline(GuiGraphics graphics, int screenX, int screenY, boolean[][] mask, int color) {
		for (int ox = -1; ox <= 16; ox++) {
			for (int oy = -1; oy <= 16; oy++) {
				boolean thisOpaque = ox >= 0 && ox < 16 && oy >= 0 && oy < 16 && mask[ox][oy];
				if (thisOpaque) {
					continue;
				}

				boolean adjacentToOpaque = false;
				for (int dx = -1; dx <= 1 && !adjacentToOpaque; dx++) {
					for (int dy = -1; dy <= 1 && !adjacentToOpaque; dy++) {
						if (dx == 0 && dy == 0) {
							continue;
						}
						int nx = ox + dx;
						int ny = oy + dy;
						if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16 && mask[nx][ny]) {
							adjacentToOpaque = true;
						}
					}
				}
				if (adjacentToOpaque) {
					graphics.fill(screenX + ox, screenY + oy, screenX + ox + 1, screenY + oy + 1, color);
				}
			}
		}
	}

	private static void drawActivePip(GuiGraphics graphics, int x, int y) {
		graphics.fill(x, y + 1, x + 5, y + 4, SLOT_BORDER_ACTIVE);
		graphics.fill(x + 1, y, x + 4, y + 5, SLOT_BORDER_ACTIVE);
		graphics.fill(x + 1, y + 1, x + 4, y + 4, 0xFFFFC247);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && this.menu.handler != null) {
			float time = getAnimationTime(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
			int displayX = this.leftPos + DISPLAY_X;
			int displayY = this.topPos + DISPLAY_Y;
			for (int i = 0; i < this.menu.slotcount; i++) {
				ItemStack morphStack = this.menu.handler.getStackInSlot(i);
				if (morphStack.isEmpty() || !(morphStack.getItem() instanceof MorphlingItem)) {
					continue;
				}
				int[] pos = getMorphPos(i, time, displayX, displayY);
				float dx = (float) (mouseX - (pos[0] + ICON / 2f));
				float dy = (float) (mouseY - (pos[1] + ICON / 2f));
				if (dx * dx + dy * dy <= HIT_R * HIT_R) {
					int selected = i == this.activeIndex ? -1 : i;
					PacketHandler.sendToServer(new PacketUpdateLivingStaffMorph(selected));
					this.activeIndex = selected;
					Player player = Minecraft.getInstance().player;
					if (player != null) {
						player.playSound(SoundEvents.GLASS_PLACE, 0.4f, 1.2f);
					}
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}

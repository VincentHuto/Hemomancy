package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.menu.slot.EquipmentArmorSlot;
import com.vincenthuto.hemomancy.common.menu.slot.ScarOffHandSlot;
import com.vincenthuto.hemomancy.common.menu.slot.SelectiveEquipmentTypeSlot;
import com.vincenthuto.hemomancy.common.menu.slot.VasculariumCharmSlot;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleEquipmentLayerVisibilityPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class HarbingerEquipmentScreen extends EffectRenderingInventoryScreen<HarbingerEquipmentMenu> {

	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;

	private static final int VANITY_AREA_HEIGHT = 108;
	private static final int MIRROR_X = 82;
	private static final int MIRROR_Y = 13;
	private static final int MIRROR_W = 74;
	private static final int MIRROR_H = 87;
	private static final int VEIN_COUNT = 18;
	private static final int LAYER_TOGGLE_SIZE = 7;
	private static final ResourceLocation EMPTY_CHARM_SLOT =
			Hemomancy.rloc("textures/gui/empty_charm_slot_background.png");
	private static final ResourceLocation EMPTY_GOURD_SLOT =
			Hemomancy.rloc("textures/gui/empty_gourd_slot_background.png");
	private static final ResourceLocation EMPTY_JAR_SLOT =
			Hemomancy.rloc("textures/gui/empty_jar_slot_background.png");
	private static final ResourceLocation EMPTY_FITTING_SLOT =
			Hemomancy.rloc("textures/gui/empty_staf_fitting_slot_background.png");

	private final Inventory playerInventory;
	private float oldMouseX;
	private float oldMouseY;
	private float animTime;
	private float[][] veinParams;
	private int[][] speckleData;

	public HarbingerEquipmentScreen(HarbingerEquipmentMenu container, Inventory inventory, Component name) {
		super(container, inventory, name);
		this.playerInventory = inventory;
		this.imageWidth = 214;
		this.imageHeight = 198;
		this.inventoryLabelX = 26;
		this.inventoryLabelY = VANITY_AREA_HEIGHT + 4;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(getScreenTitle())) / 2;

		Random veinRand = new Random(41285L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = veinRand.nextFloat();
			veinParams[i][1] = veinRand.nextFloat();
			veinParams[i][2] = (float) (veinRand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.25f + veinRand.nextFloat() * 0.65f;
			veinParams[i][4] = 7f + veinRand.nextFloat() * 15f;
			veinParams[i][5] = 0.04f + veinRand.nextFloat() * 0.08f;
			veinParams[i][6] = 48 + veinRand.nextInt(100);
			veinParams[i][7] = 1 + veinRand.nextInt(2);
			veinParams[i][8] = veinRand.nextFloat();
		}

		Random speckRand = new Random(49152L);
		speckleData = new int[72][4];
		for (int i = 0; i < speckleData.length; i++) {
			speckleData[i][0] = speckRand.nextInt(this.imageWidth);
			speckleData[i][1] = speckRand.nextInt(VANITY_AREA_HEIGHT);
			speckleData[i][2] = 10 + speckRand.nextInt(26);
			speckleData[i][3] = 14 + speckRand.nextInt(28);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderLayerToggleIcons(graphics, mouseX, mouseY);
		this.renderTooltip(graphics, mouseX, mouseY);
		renderLayerToggleTooltip(graphics, mouseX, mouseY);
		this.oldMouseX = mouseX;
		this.oldMouseY = mouseY;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (handleLayerToggleClick(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
		animTime += partialTicks * 0.008f;

		int gx = this.leftPos;
		int gy = this.topPos;

		renderVeinBackground(gfx, gx, gy, this.imageWidth, VANITY_AREA_HEIGHT);
		drawBorder(gfx, gx, gy, this.imageWidth, VANITY_AREA_HEIGHT);
		renderMirrorPanel(gfx, gx, gy);
		renderSlotRails(gfx, gx, gy);

		Slot firstInventorySlot = this.menu.slots.get(8);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.BLOODY,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		for (int i = 0; i < this.menu.slots.size(); i++) {
			if (i >= 8 && i <= 43) {
				continue;
			}
			Slot slot = this.menu.slots.get(i);
			drawSlotBackground(gfx, gx + slot.x, gy + slot.y, slot);
		}

		if (this.minecraft != null && this.minecraft.player != null) {
			gfx.enableScissor(gx + MIRROR_X, gy + MIRROR_Y, gx + MIRROR_X + MIRROR_W, gy + MIRROR_Y + MIRROR_H);
			InventoryScreen.renderEntityInInventoryFollowsMouse(gfx, gx + MIRROR_X + 7, gy + MIRROR_Y + 3,
					gx + MIRROR_X + MIRROR_W - 7, gy + MIRROR_Y + MIRROR_H - 2, 34,
					0.0625F, this.oldMouseX, this.oldMouseY, this.minecraft.player);
			gfx.disableScissor();
		}
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		Component screenTitle = getScreenTitle();
		gfx.drawString(font, screenTitle, (this.imageWidth - font.width(screenTitle)) / 2, 5, 0xFFAA2222, true);
	}

	private Component getScreenTitle() {
		return this.menu.isOpenedFromScarletVanity() ? Component.literal("Scarlet Vanity") : this.title;
	}

	private void renderMirrorPanel(GuiGraphics gfx, int gx, int gy) {
		int panelX = gx + MIRROR_X;
		int panelY = gy + MIRROR_Y;
		int panelW = MIRROR_W;
		int panelH = MIRROR_H;
		gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xAA050102);
		gfx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0x550F0405);
		drawBorder(gfx, panelX, panelY, panelW, panelH);

		int glassX = panelX + 8;
		int glassY = panelY + 8;
		int glassW = panelW - 16;
		int glassH = panelH - 14;
		gfx.fill(glassX, glassY, glassX + glassW, glassY + glassH, 0x33000000);
		gfx.fill(glassX, glassY, glassX + glassW, glassY + 1, 0x55401212);
		gfx.fill(glassX, glassY, glassX + 1, glassY + glassH, 0x55401212);
	}

	private void renderSlotRails(GuiGraphics gfx, int gx, int gy) {
		gfx.fill(gx + 12, gy + 13, gx + 42, gy + 101, 0x77060203);
		gfx.fill(gx + 170, gy + 6, gx + 200, gy + 105, 0x77060203);
		drawBorder(gfx, gx + 12, gy + 13, 30, 88);
		drawBorder(gfx, gx + 170, gy + 6, 30, 99);
		gfx.fill(gx + 45, gy + 73, gx + 74, gy + 101, 0x66060203);
		drawInventoryBorder(gfx, gx + 45, gy + 73, 29, 28);
	}

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, Slot slot) {
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

		if (slot instanceof EquipmentArmorSlot) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15CC2222);
		} else if (slot instanceof SelectiveEquipmentTypeSlot) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x18AA1530);
		} else if (slot instanceof VasculariumCharmSlot) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20D65C7A);
		} else if (slot instanceof ScarOffHandSlot) {
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x15303030);
		}

		renderEmptySlotOverlay(gfx, sx, sy, slot);
	}

	private void renderLayerToggleIcons(GuiGraphics gfx, int mouseX, int mouseY) {
		renderLayerToggleIcon(gfx, mouseX, mouseY, HarbingerEquipmentMenu.JAR_MENU_SLOT,
				HarbingerEquipmentMenu.JAR_SLOT_INDEX);
		renderLayerToggleIcon(gfx, mouseX, mouseY, HarbingerEquipmentMenu.CHARM_MENU_SLOT,
				HarbingerEquipmentMenu.CHARM_SLOT_INDEX);
		renderLayerToggleIcon(gfx, mouseX, mouseY, HarbingerEquipmentMenu.GOURD_MENU_SLOT,
				HarbingerEquipmentMenu.GOURD_SLOT_INDEX);
	}

	private void renderLayerToggleIcon(GuiGraphics gfx, int mouseX, int mouseY, int menuSlot, int slotIndex) {
		Slot slot = this.menu.slots.get(menuSlot);
		int iconX = this.leftPos + slot.x + 16;
		int iconY = this.topPos + slot.y -5;
		boolean visible = this.menu.equipment.isRenderLayerVisible(slotIndex);
		boolean hovered = isMouseOver(iconX, iconY, LAYER_TOGGLE_SIZE, LAYER_TOGGLE_SIZE, mouseX, mouseY);
		drawLayerVisibilityIcon(gfx, iconX, iconY, visible, hovered);
	}

	private void drawLayerVisibilityIcon(GuiGraphics gfx, int x, int y, boolean visible, boolean hovered) {
		int outline = visible ? 0xFFE3A2A8 : 0xFF5E3538;
		int pupil = visible ? 0xFFFFD0D5 : 0xFF7A484C;
		int shadow = hovered ? 0xAA2A080A : 0x880A0203;

		gfx.fill(x, y, x + LAYER_TOGGLE_SIZE, y + LAYER_TOGGLE_SIZE, shadow);
		gfx.fill(x + 1, y + 2, x + 6, y + 3, outline);
		gfx.fill(x, y + 3, x + 2, y + 4, outline);
		gfx.fill(x + 5, y + 3, x + 7, y + 4, outline);
		gfx.fill(x + 1, y + 4, x + 6, y + 5, outline);
		gfx.fill(x + 3, y + 3, x + 4, y + 4, pupil);
		if (!visible) {
			gfx.fill(x + 1, y + 5, x + 2, y + 6, 0xFFFF4A52);
			gfx.fill(x + 2, y + 4, x + 3, y + 5, 0xFFFF4A52);
			gfx.fill(x + 3, y + 3, x + 4, y + 4, 0xFFFF4A52);
			gfx.fill(x + 4, y + 2, x + 5, y + 3, 0xFFFF4A52);
			gfx.fill(x + 5, y + 1, x + 6, y + 2, 0xFFFF4A52);
		}
	}

	private void renderLayerToggleTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		Integer slotIndex = layerToggleSlotAt(mouseX, mouseY);
		if (slotIndex == null) {
			return;
		}
		String key = this.menu.equipment.isRenderLayerVisible(slotIndex)
				? "screen.hemomancy.scarlet_vanity.layer.hide"
				: "screen.hemomancy.scarlet_vanity.layer.show";
		gfx.renderTooltip(this.font, Component.translatable(key), mouseX, mouseY);
	}

	private boolean handleLayerToggleClick(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}
		Integer slotIndex = layerToggleSlotAt(mouseX, mouseY);
		if (slotIndex == null) {
			return false;
		}
		PacketHandler.sendToServer(new ToggleEquipmentLayerVisibilityPacket(slotIndex));
		return true;
	}

	private Integer layerToggleSlotAt(double mouseX, double mouseY) {
		if (isOverLayerToggle(this.menu.slots.get(HarbingerEquipmentMenu.JAR_MENU_SLOT), mouseX, mouseY)) {
			return HarbingerEquipmentMenu.JAR_SLOT_INDEX;
		}
		if (isOverLayerToggle(this.menu.slots.get(HarbingerEquipmentMenu.CHARM_MENU_SLOT), mouseX, mouseY)) {
			return HarbingerEquipmentMenu.CHARM_SLOT_INDEX;
		}
		if (isOverLayerToggle(this.menu.slots.get(HarbingerEquipmentMenu.GOURD_MENU_SLOT), mouseX, mouseY)) {
			return HarbingerEquipmentMenu.GOURD_SLOT_INDEX;
		}
		return null;
	}

	private boolean isOverLayerToggle(Slot slot, double mouseX, double mouseY) {
		int iconX = this.leftPos + slot.x + 16;
		int iconY = this.topPos + slot.y -5;
		return isMouseOver(iconX, iconY, LAYER_TOGGLE_SIZE, LAYER_TOGGLE_SIZE, mouseX, mouseY);
	}

	private static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private void renderEmptySlotOverlay(GuiGraphics gfx, int sx, int sy, Slot slot) {
		if (slot.hasItem()) {
			return;
		}
		int menuSlot = this.menu.slots.indexOf(slot);
		if (slot instanceof VasculariumCharmSlot) {
			gfx.blit(EMPTY_CHARM_SLOT, sx, sy, 0, 0, 16, 16, 16, 16);
		} else if (slot instanceof SelectiveEquipmentTypeSlot) {
			if (menuSlot == HarbingerEquipmentMenu.GOURD_MENU_SLOT) {
				gfx.blit(EMPTY_GOURD_SLOT, sx, sy, 0, 0, 16, 16, 16, 16);
			} else if (menuSlot == HarbingerEquipmentMenu.JAR_MENU_SLOT) {
				gfx.blit(EMPTY_JAR_SLOT, sx, sy, 0, 0, 16, 16, 16, 16);
			} else if (menuSlot == HarbingerEquipmentMenu.FITTING_MENU_SLOT) {
				gfx.blit(EMPTY_FITTING_SLOT, sx, sy, 0, 0, 16, 16, 16, 16);
			}
		}
	}

	private void drawInventoryBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 1, BORDER_OUTER);
		gfx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		gfx.fill(x, y, x + 1, y + h, BORDER_OUTER);
		gfx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
	}

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

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (32 * (1f - t));
			int red = (int) (42 * (1f - t));
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16));
		}

		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, animTime, gx, gy, gw, gh);
			}
		}

		if (speckleData != null) {
			for (int[] speck : speckleData) {
				int sx = gx + speck[0] % gw;
				int sy = gy + speck[1] % gh;
				int sr = speck[2];
				int sg = sr / 5;
				int sa = speck[3];
				graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
			}
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics graphics, int index, float time, int gx, int gy, int gw, int gh) {
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

		int baseRed = (int) (42 + 54 * brightness);
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
			if (step < 10) {
				tipFade = step / 10f;
			} else if (step > length - 10) {
				tipFade = (length - step) / 10f;
			}

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);
			int a = (int) Mth.clamp(tipFade * pulse * 180, 20, 200);
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);
			graphics.fill(ix, iy, ix + thickness, iy + thickness, (a << 24) | (r << 16) | (g << 8) | b);
		}
	}
}

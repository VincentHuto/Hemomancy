package com.vincenthuto.hemomancy.client.screen.tile.functional;

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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;

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
	private static final VeinBackgroundRenderer.Style VEINS = new VeinBackgroundRenderer.Style(
			new VeinBackgroundRenderer.Field(18, 41285L, 0.25f, 0.65f, 7f, 15f, 48, 100, 2, 1, 0, false),
			VeinBackgroundRenderer.Palette.CRIMSON.rings(32, 42, 0, 0).tendrils(42, 54, 2, 8, 5, 5),
			new VeinBackgroundRenderer.Speckles.Fixed(49152L, 72, 232, VANITY_AREA_HEIGHT, 10, 26, 0, 5, 14, 28));
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VEINS);

	public HarbingerEquipmentScreen(HarbingerEquipmentMenu container, Inventory inventory, Component name) {
		super(container, inventory, name);
		this.playerInventory = inventory;
		this.imageWidth = 232;
		this.imageHeight = 198;
		this.inventoryLabelX = 26;
		this.inventoryLabelY = VANITY_AREA_HEIGHT + 4;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(getScreenTitle())) / 2;
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
        if(mouseX>=leftPos+198 && mouseX<leftPos+214 && mouseY>=topPos+38 && mouseY<topPos+54 && !menu.getSlot(45).hasItem())
            graphics.renderTooltip(font,Component.translatable("hemomancy.antecedent.scar.socket"),mouseX,mouseY);
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

		veins.draw(gfx, gx, gy, this.imageWidth, VANITY_AREA_HEIGHT, animTime);
		ScreenDrawUtils.drawBorder(gfx, gx, gy, this.imageWidth, VANITY_AREA_HEIGHT, BORDER_OUTER, BORDER_INNER);
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
		ScreenDrawUtils.drawBorder(gfx, panelX, panelY, panelW, panelH, BORDER_OUTER, BORDER_INNER);

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
		ScreenDrawUtils.drawBorder(gfx, gx + 12, gy + 13, 30, 88, BORDER_OUTER, BORDER_INNER);
		ScreenDrawUtils.drawBorder(gfx, gx + 170, gy + 6, 30, 99, BORDER_OUTER, BORDER_INNER);
		gfx.fill(gx + 45, gy + 73, gx + 74, gy + 101, 0x66060203);
		ScreenDrawUtils.drawSimpleBorder(gfx, gx + 45, gy + 73, 29, 28, BORDER_OUTER);
		gfx.fill(gx + 46, gy + 74, gx + 73, gy + 75, BORDER_INNER);
	}

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, Slot slot) {
		ScreenDrawUtils.drawSlotWell(gfx, sx, sy, SLOT_BORDER_DARK, SLOT_BG, SLOT_BORDER_LIGHT);

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
}

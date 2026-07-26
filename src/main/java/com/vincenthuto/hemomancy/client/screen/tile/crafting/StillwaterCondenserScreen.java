package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.StillwaterCondenserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class StillwaterCondenserScreen extends AbstractContainerScreen<StillwaterCondenserMenu> {
	private static final int PALE = 0xFFD7EDF0;
	private static final int TEAL = 0xFF62B9AE;
	private static final int DIM = 0xFF52636C;

	public StillwaterCondenserScreen(StillwaterCondenserMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 166;
		inventoryLabelY = 73;
	}

	@Override protected void init() {
		super.init();
		titleLabelX = (imageWidth - font.width(title)) / 2;
	}

	@Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		gfx.fillGradient(x, y, x + imageWidth, y + 74, 0xFF101F28, 0xFF172E35);
		drawFrame(gfx, x, y, imageWidth, 74);
		Slot inventory = menu.slots.get(StillwaterCondenserMenu.SLOT_COUNT);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.PALLID,
				x + inventory.x - 5, y + inventory.y - 6);
		for (int i = 0; i < StillwaterCondenserMenu.SLOT_COUNT; i++) {
			Slot slot = menu.slots.get(i);
			drawSlot(gfx, x + slot.x, y + slot.y, i == 1 ? 0x3048BDAA : 0x303E7580);
		}

		// Condensation channel: bottle -> falling drops -> collected dew.
		int startX = x + 66;
		int lineY = y + 43;
		gfx.fill(startX, lineY - 2, startX + 40, lineY + 3, 0xFF213B43);
		gfx.fill(startX, lineY - 1, startX + menu.progressPixels(), lineY + 2, TEAL);
		for (int i = 0; i < 3; i++) {
			int dropX = startX + 7 + i * 12;
			gfx.fill(dropX, lineY - 8, dropX + 2, lineY - 4, 0xFF91DCD7);
			gfx.fill(dropX - 1, lineY - 5, dropX + 3, lineY - 3, 0xFF91DCD7);
		}

		statusLamp(gfx, x + 15, y + 18, menu.hasWater(), "W");
		statusLamp(gfx, x + 31, y + 18, menu.hasGhostPipe(), "G");
		statusLamp(gfx, x + 145, y + 18, menu.hasLatticeBoost(), "L");
	}

	@Override protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		gfx.drawString(font, title, titleLabelX, 6, PALE, false);
		String status = !menu.hasWater() ? "Needs still water"
				: !menu.hasGhostPipe() ? "Needs living Ghost Pipe"
				: menu.hasLatticeBoost() ? "Lattice-assisted condensation" : "Condensing patiently";
		gfx.drawCenteredString(font, Component.literal(status), imageWidth / 2, 61,
				menu.hasWater() && menu.hasGhostPipe() ? TEAL : DIM);
	}

	private void statusLamp(GuiGraphics gfx, int x, int y, boolean active, String glyph) {
		int color = active ? TEAL : 0xFF29343A;
		gfx.fill(x, y, x + 11, y + 11, 0xFF0A1115);
		gfx.fill(x + 1, y + 1, x + 10, y + 10, color);
		gfx.drawCenteredString(font, Component.literal(glyph), x + 5, y + 2, active ? 0xFF071315 : 0xFF718087);
	}

	private static void drawSlot(GuiGraphics gfx, int x, int y, int tint) {
		gfx.fill(x - 2, y - 2, x + 18, y + 18, 0xFF091015);
		gfx.fill(x - 1, y - 1, x + 17, y + 17, 0xFF49606A);
		gfx.fill(x, y, x + 16, y + 16, 0xFF111B20);
		gfx.fill(x, y, x + 16, y + 16, tint);
	}

	private static void drawFrame(GuiGraphics gfx, int x, int y, int width, int height) {
		gfx.fill(x, y, x + width, y + 2, 0xFF6C929D);
		gfx.fill(x, y + height - 2, x + width, y + height, 0xFF20363E);
		gfx.fill(x, y, x + 2, y + height, 0xFF6C929D);
		gfx.fill(x + width - 2, y, x + width, y + height, 0xFF20363E);
	}
}

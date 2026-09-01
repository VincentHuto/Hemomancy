package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicFolioLayout;
import com.vincenthuto.hemomancy.common.menu.MnemonicFolioMenu;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MnemonicFolioScreen extends AbstractContainerScreen<MnemonicFolioMenu> {
	public MnemonicFolioScreen(MnemonicFolioMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 194;
		imageHeight = 174;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.fillGradient(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight,
				0xF20C0813, 0xF2181020);
		renderWisps(graphics);
		graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF8C4264);
		for (int index = 0; index < MnemonicFolioLayout.SLOT_COUNT; index++) {
			drawSlot(graphics, MnemonicFolioLayout.folioSlot(index), 0xFF6E3852);
		}
		for (int index = 0; index < 27; index++) drawSlot(graphics, MnemonicFolioLayout.playerSlot(index), 0xFF49313E);
		for (int index = 0; index < 9; index++) drawSlot(graphics, MnemonicFolioLayout.hotbarSlot(index), 0xFF49313E);
	}

	private void drawSlot(GuiGraphics graphics, MnemonicFolioLayout.Point point, int border) {
		int x = leftPos + point.x();
		int y = topPos + point.y();
		graphics.fill(x, y, x + 18, y + 18, 0xDC160E14);
		graphics.renderOutline(x, y, 18, 18, border);
	}

	private void renderWisps(GuiGraphics graphics) {
		float timeTicks = (Util.getMillis() % 1_000_000L) / 50.0F;
		graphics.enableScissor(leftPos + 1, topPos + 1,
				leftPos + imageWidth - 1, topPos + imageHeight - 1);
		for (MnemonicFolioWisps.Sample sample : MnemonicFolioWisps.samples(timeTicks, imageWidth, imageHeight)) {
			int x = leftPos + sample.x();
			int y = topPos + sample.y();
			int radius = sample.radius();
			graphics.fill(x - radius, y - 1, x + radius + 1, y + 2,
					argb(Math.max(2, sample.alpha() / 4), sample.rgb()));
			graphics.fill(x - Math.max(1, radius - 2), y, x + Math.max(1, radius - 2) + 1, y + 1,
					argb(sample.alpha(), sample.rgb()));
		}
		graphics.disableScissor();
	}

	private static int argb(int alpha, int rgb) {
		return alpha << 24 | rgb;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawCenteredString(font, title, imageWidth / 2, 6, 0xFFD8B8C8);
		graphics.drawString(font, playerInventoryTitle, 16, 78, 0xFF9B8791, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}
}

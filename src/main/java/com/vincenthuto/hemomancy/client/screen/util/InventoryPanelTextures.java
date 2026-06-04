package com.vincenthuto.hemomancy.client.screen.util;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class InventoryPanelTextures {
	public static final Panel BLOODY = new Panel(Hemomancy.rloc("textures/gui/inventory_panel_bloody.png"), 172, 86);
	public static final Panel FUNGAL = new Panel(Hemomancy.rloc("textures/gui/inventory_panel_fungal.png"), 172, 86);
	public static final Panel MORPHIC = new Panel(Hemomancy.rloc("textures/gui/inventory_panel_morphic.png"), 172, 86);
	public static final Panel PALLID = new Panel(Hemomancy.rloc("textures/gui/inventory_panel_pallid.png"), 172, 86);

	private InventoryPanelTextures() {
	}

	public static void blit(GuiGraphics graphics, Panel panel, int x, int y, int width, int height) {
		graphics.blit(panel.texture(), x, y, width, height, 0.0f, 0.0f, panel.width(), panel.height(),
				panel.width(), panel.height());
	}

	public static void blit(GuiGraphics graphics, Panel panel, int x, int y) {
		blit(graphics, panel, x, y, panel.width(), panel.height());
	}

	public record Panel(ResourceLocation texture, int width, int height) {
	}
}

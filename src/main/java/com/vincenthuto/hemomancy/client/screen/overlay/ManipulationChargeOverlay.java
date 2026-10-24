package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.event.ManipulationChargeInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** The charge bar above the hotbar while a charged manipulation winds up. Ironhearted shows its charge as forming hearts instead. */
public final class ManipulationChargeOverlay {

	private ManipulationChargeOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		String chargingManipulation = ManipulationChargeInput.getChargingManipulationName();
		if (chargingManipulation.isEmpty() || chargingManipulation.equals("ironhearted")) return;
		int barWidth = 52;
		int x = width / 2 - barWidth / 2;
		int y = height - 51;
		graphics.fill(x - 1, y - 1, x + barWidth + 1, y + 5, 0xDD130D17);
		graphics.fill(x, y, x + Math.round(barWidth * ManipulationChargeInput.getManipulationChargeProgress()), y + 4,
				0xFF9E1B32);
	}
}

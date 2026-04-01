package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.capability.player.morphling.EquippedMorphlingProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * HUD overlay that displays the player's currently equipped morphling
 * in the bottom-left corner of the screen, above the hotbar.
 */
public class EquippedMorphlingOverlay {

	public static EquippedMorphlingOverlay instance;

	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null)
			return;

		player.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA).ifPresent(cap -> {
			ItemStack equipped = cap.getEquippedMorphling();
			if (!equipped.isEmpty()) {
				// Position: bottom-left, just above the hotbar
				int x = 4;
				int y = screenHeight - 20 - 16 - 4; // above hotbar (hotbar ~20px from bottom)

				// Draw a subtle dark background
				guiGraphics.fill(x - 2, y - 2, x + 18 + mc.font.width(equipped.getHoverName()) + 4, y + 18, 0x80000000);

				// Render the item icon
				guiGraphics.renderItem(equipped, x, y);

				// Render the item name next to it
				guiGraphics.drawString(mc.font, equipped.getHoverName(), x + 20, y + 4, 0xFFFFFF, true);
			}
		});
	}

}

package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * HUD overlay that displays the player's currently equipped morphling
 * beside the blood volume bar.
 */
public class EquippedMorphlingOverlay {

	public static EquippedMorphlingOverlay instance;

	private final Minecraft mc = Minecraft.getInstance();

	public void renderHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTicks) {
		LocalPlayer player = mc.player;
		if (player == null)
			return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			ItemStack equipped = cap.getEquippedMorphling();
			if (!equipped.isEmpty()) {
				boolean barOnLeft = BloodVolumeOverlay.isConfiguredOnLeftSide();
				int barX = BloodVolumeOverlay.getConfiguredBarX(screenWidth);
				int barY = BloodVolumeOverlay.getConfiguredBarY(player, screenHeight);
				int x = EquippedMorphlingOverlayPlacement.iconXForBloodBar(barOnLeft, barX, BloodVolumeOverlay.getBarWidth());
				int y = EquippedMorphlingOverlayPlacement.iconYForBloodBar(barY, BloodVolumeOverlay.getBarHeight());

				guiGraphics.renderItem(equipped, x, y);
			}
		});
	}

}

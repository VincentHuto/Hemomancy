package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.mortem.BlackheartedRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Blackhearted's necrotic saturation bar, under the health row, and its refractory state after a rupture. */
public final class BlackheartedOverlay {

	private BlackheartedOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		var state = HemoCapabilityAccess.getPowerGuardrails(minecraft.player);
		long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		boolean refractory = now < state.getBlackheartedCooldownUntil();
		if (state.getNecroticSaturation() <= 0.0F && !refractory) return;
		int x = width / 2 - 91;
		int barWidth = 43;
		int barY = height - 39 + 9;
		graphics.fill(x, barY, x + barWidth + 2, barY + 5, 0xDD130D17);
		int fill = Math.round(barWidth * state.getNecroticSaturation()
				/ BlackheartedRules.NECROTIC_SATURATION_CAP);
		graphics.fill(x + 1, barY + 1, x + 1 + fill, barY + 4,
				refractory ? 0xFF4B3549 : 0xFF4D162F);
		graphics.drawString(minecraft.font, Component.translatable(refractory ? "hud.hemomancy.blackhearted.refractory"
				: "hud.hemomancy.blackhearted.necrosis"),
				x + barWidth + 5, barY - 2, refractory ? 0xFF8E788A : 0xFFB85B78, true);
	}
}

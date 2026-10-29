package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** A possessing Silentium's Concentration: one pip per hit it can take, the iron shell as pale pips before them. */
public final class ConcentrationOverlay {
	private static final int FULL = 0xFFB8A0E0;
	private static final int EMPTY = 0xFF241A30;
	private static final int SHELL = 0xFFD8D4CC;
	private static final int RIM = 0xFF0C0810;
	private static final int PIP = 5;
	private static final int GAP = 2;

	private ConcentrationOverlay() {}

	public static void render(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui || !HemoCapabilityAccess.isPossessing(minecraft.player)) return;
		Possession possession = HemoCapabilityAccess.getPossession(minecraft.player);
		int pips = (int) Math.ceil(possession.max());
		int shell = possession.shell();
		int count = pips + shell;
		int total = count * (PIP + GAP) - GAP;
		int x = width / 2 - total / 2;
		int y = height - 58;
		graphics.drawManaged(() -> {
			for (int i = 0; i < count; i++) {
				int left = x + i * (PIP + GAP);
				int colour = i < shell ? SHELL : (i - shell) < possession.concentration() ? FULL : EMPTY;
				graphics.fill(left - 1, y - 1, left + PIP + 1, y + PIP + 1, RIM);
				graphics.fill(left, y, left + PIP, y + PIP, colour);
			}
		});
	}
}

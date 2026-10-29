package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.world.entity.LivingEntity;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoadRules;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** The Apotheos's spore bar, drawn where the food bar was: ten spore icons, right to left, ten load each. */
public final class SporeLoadOverlay {
	private static final int EMPTY = 0xFF2A1A22;
	private static final int HALF = 0xFF8E4A62;
	private static final int FULL = 0xFFD8A8B8;
	private static final int RIM = 0xFF140A10;
	/** A 9x9 puffball: 0 blank, 1 rim, 2 body, 3 highlight. */
	private static final String[] ICON = {
			"..11111..",
			".1222221.",
			"122232221",
			"122222221",
			"122222221",
			".1222221.",
			"..12221..",
			"...121...",
			"...111..."};

	private SporeLoadOverlay() {}

	public static void render(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui || minecraft.gameMode == null
				|| !minecraft.gameMode.canHurtPlayer() || !ApotheosBodyEvents.isApotheos(minecraft.player)) return;
		// Vanilla hides the food bar while mount hearts show; the spore bar steps aside the same way.
		if (minecraft.player.getVehicle() instanceof LivingEntity vehicle && vehicle.showVehicleHealth()) return;
		float load = HemoCapabilityAccess.getSporeLoad(minecraft.player).load();
		int perIcon = (int) (SporeLoadRules.MAX / 10);
		int right = width / 2 + 91;
		// Take the food bar's row and reserve it, so air bubbles stack above as they do over food.
		int y = height - minecraft.gui.rightHeight;
		minecraft.gui.rightHeight += 10;
		graphics.drawManaged(() -> {
			for (int i = 0; i < 10; i++) {
				float filled = load - i * perIcon;
				int body = filled >= perIcon ? FULL : filled >= perIcon / 2f ? HALF : EMPTY;
				drawIcon(graphics, right - 9 - i * 8, y, body);
			}
		});
	}

	private static void drawIcon(GuiGraphics graphics, int x, int y, int body) {
		for (int row = 0; row < ICON.length; row++) {
			for (int col = 0; col < ICON[row].length(); col++) {
				char c = ICON[row].charAt(col);
				if (c == '.') continue;
				int color = c == '1' ? RIM : c == '3' && body != EMPTY ? 0xFFF4E2E8 : body;
				graphics.fill(x + col, y + row, x + col + 1, y + row + 1, color);
			}
		}
	}
}

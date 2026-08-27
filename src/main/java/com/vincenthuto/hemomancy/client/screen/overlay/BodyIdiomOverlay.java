package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.BodyIdiomRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class BodyIdiomOverlay {
	private static final String[] HEART = {
			" ## ## ",
			"#######",
			"#######",
			" ##### ",
			"  ###  ",
			"   #   "
	};

	private BodyIdiomOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) return;
		var state = HemoCapabilityAccess.getPowerGuardrails(minecraft.player);
		float charge = Mth.clamp(ClientEvents.getManipulationChargeTicks()
				/ (float) BodyIdiomRules.IRON_HEART_CHARGE_TICKS, 0.0F, 1.0F);
		float shownIron = state.getIronHeartHealth() + charge * BodyIdiomRules.IRON_HEART_HEALTH_PER_CAST;
		int x = width / 2 - 91;
		int y = height - 50;
		if (shownIron > 0.0F) {
			for (int heart = 0; heart < 5; heart++) {
				float fill = Mth.clamp((shownIron - heart * 2.0F) / 2.0F, 0.0F, 1.0F);
				drawIronHeart(graphics, x + heart * 9, y, fill,
						minecraft.level == null ? 0L : minecraft.level.getGameTime());
			}
		}

		long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		boolean refractory = now < state.getBlackheartedCooldownUntil();
		if (state.getNecroticSaturation() > 0.0F || refractory) {
			int barWidth = 43;
			int barY = y + 9;
			graphics.fill(x, barY, x + barWidth + 2, barY + 5, 0xDD130D17);
			int fill = Math.round(barWidth * state.getNecroticSaturation()
					/ BodyIdiomRules.NECROTIC_SATURATION_CAP);
			graphics.fill(x + 1, barY + 1, x + 1 + fill, barY + 4,
					refractory ? 0xFF4B3549 : 0xFF4D162F);
			graphics.drawString(minecraft.font, refractory ? "REFRACTORY" : "NECROSIS",
					x + barWidth + 5, barY - 2, refractory ? 0xFF8E788A : 0xFFB85B78, true);
		}
	}

	private static void drawIronHeart(GuiGraphics graphics, int x, int y, float fill, long gameTime) {
		int filledColumns = Math.round(fill * 7.0F);
		boolean pulse = gameTime / 5L % 2L == 0L;
		for (int row = 0; row < HEART.length; row++) {
			for (int column = 0; column < HEART[row].length(); column++) {
				if (HEART[row].charAt(column) != '#') continue;
				boolean edge = row == 0 || row == HEART.length - 1 || column == 0
						|| column == HEART[row].length() - 1
						|| HEART[Math.max(0, row - 1)].charAt(column) != '#'
						|| HEART[Math.min(HEART.length - 1, row + 1)].charAt(column) != '#';
				int color = edge ? 0xFF28262D : column < filledColumns ? 0xFFA7A3A8 : 0xFF4C4850;
				if (fill > 0.0F && column == 3 && row >= 1 && row <= 4) {
					color = pulse ? 0xFFD3444F : 0xFFA32934;
				}
				graphics.fill(x + column, y + row, x + column + 1, y + row + 1, color);
			}
		}
	}
}

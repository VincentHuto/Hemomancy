package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.data.RingmasterFinaleClientData;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterAbsorptionRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * The Ringmaster's kneel (2026-11-18): the first time a player brings him to his knees, two lines name the ways out,
 * the kill and the absorbing (shown closed to a player unfit to take his place). While his blood is being drawn off, a
 * thin crimson bar shows how much is left.
 */
public final class FinaleChoicePrompt {
	/** The prompt's first line never sits higher than this: under two boss bars. */
	private static final int TOP = 52;
	private static final int TITLE = 0xFFE8DAC0, LINE = 0xFFD8C8B0, CLOSED = 0xFF7A6A64, BAR = 0xFFB01430, BAR_BACK = 0x88000000;

	private FinaleChoicePrompt() {
	}

	public static void render(GuiGraphics graphics) {
		CircusRingmasterEntity copy = RingmasterFinaleClientData.ringmaster();
		if (copy == null || !RingmasterFinaleClientData.isActive() || RingmasterFinaleClientData.phase() != FinalePhase.KNEEL) return;
		Font font = Minecraft.getInstance().font;
		// High in the top third, below the boss bars and well clear of the chat at the bottom left.
		int cx = graphics.guiWidth() / 2, y = Math.max(TOP, graphics.guiHeight() / 7);
		int prompt = copy.kneelPrompt();
		if (prompt != 0) {
			graphics.drawCenteredString(font, Component.translatable("hemomancy.circus.finale.prompt.title"), cx, y, TITLE);
			graphics.drawCenteredString(font, Component.translatable("hemomancy.circus.finale.prompt.kill"), cx, y + 14, LINE);
			graphics.drawCenteredString(font, Component.translatable(prompt == 1 ? "hemomancy.circus.finale.prompt.absorb"
					: "hemomancy.circus.finale.prompt.absorb_closed"), cx, y + 26, prompt == 1 ? LINE : CLOSED);
		}
		int drawn = copy.absorbed();
		if (drawn > 0) {
			int width = 80, top = y + 42;
			graphics.fill(cx - width / 2 - 1, top - 1, cx + width / 2 + 1, top + 4, BAR_BACK);
			graphics.fill(cx - width / 2, top, cx - width / 2 + width * drawn / RingmasterAbsorptionRules.ABSORPTION_REQUIRED, top + 3, BAR);
		}
	}
}

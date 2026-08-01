package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Shared Degree/Family filter chrome for progress-tree tabs. */
final class ProgressFilterControlsView {
	private ProgressFilterControlsView() {}

	static void draw(GuiGraphics gfx, ProgressScreenContext ctx,
			RecipeMapInspectorLayout.IntRect bounds, String text, int accent) {
		gfx.fill(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 0xDD10060B);
		ScreenDrawUtils.drawSimpleBorder(gfx, bounds.left(), bounds.top(),
				bounds.width(), bounds.height(), withAlpha(accent, 0x88));
		gfx.drawCenteredString(ctx.font(), ctx.font().plainSubstrByWidth(text, bounds.width() - 6),
				bounds.left() + bounds.width() / 2, bounds.top() + 3, 0xFFBBBBBB);
	}

	private static int withAlpha(int color, int alpha) {
		return (alpha << 24) | (color & 0x00FFFFFF);
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.HarbingerChromeRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Common visual chrome for the Rites and Crafting recipe-map inspectors. */
public final class RecipeMapInspectorView {
	private static final int TOGGLE_WIDTH = 18;
	private static final int TOGGLE_HEIGHT = 20;

	private RecipeMapInspectorView() {}

	public static void drawChrome(GuiGraphics gfx, ProgressScreenContext ctx,
			RecipeMapInspectorLayout layout, int backgroundColor, int accentColor,
			int mouseX, int mouseY) {
		RecipeMapInspectorLayout.IntRect panel = layout.panel();
		gfx.fill(panel.left(), panel.top(), panel.right(), panel.bottom(), backgroundColor);
		ScreenDrawUtils.drawSimpleBorder(gfx, panel.left(), panel.top(), panel.width(), panel.height(), accentColor);
		HarbingerChromeRenderer.drawFrame(gfx, panel.left(), panel.top(), panel.width(), panel.height(), accentColor,
				HarbingerChromeRenderer.State.ACTIVE);
		ScreenDrawUtils.drawSidebarToggleTab(gfx, ctx.font(), panel.left(), panel.top(),
				TOGGLE_WIDTH, TOGGLE_HEIGHT, layout.expanded(), layout.isOverToggle(mouseX, mouseY),
				0xDD1A0505, 0x99120303, accentColor, 0xFF444444, 0xFFEEDDFF, 0xFF888888);
		HarbingerChromeRenderer.drawFrame(gfx, panel.left(), panel.top(), TOGGLE_WIDTH, TOGGLE_HEIGHT, accentColor,
				layout.isOverToggle(mouseX, mouseY) ? HarbingerChromeRenderer.State.HOVERED
						: HarbingerChromeRenderer.State.IDLE);
	}

	public static void drawPreviewControls(GuiGraphics gfx, ProgressScreenContext ctx,
			RecipeMapInspectorLayout layout, int maxLayer, int visibleLayer, int accentColor,
			int mouseX, int mouseY) {
		RecipeMapInspectorLayout.IntRect preview = layout.previewContent();
		ScreenDrawUtils.drawLayerButtons(gfx, ctx.font(), preview.right() - 18,
				preview.top() + preview.height() / 2, maxLayer, visibleLayer,
				accentColor, mouseX, mouseY);
		ScreenDrawUtils.drawHarbingerLayerButtonFrames(gfx, preview.right() - 18,
				preview.top() + preview.height() / 2, maxLayer, accentColor, mouseX, mouseY);
		gfx.drawCenteredString(ctx.font(), "Drag preview to rotate",
				preview.left() + preview.width() / 2, layout.preview().bottom() - 11, 0x66888888);
	}
}

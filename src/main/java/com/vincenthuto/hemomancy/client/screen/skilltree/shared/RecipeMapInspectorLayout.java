package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.util.Mth;

public record RecipeMapInspectorLayout(IntRect mapViewport, IntRect panel, IntRect preview,
		IntRect info, boolean overlay, boolean expanded) {
	private static final int GAP = 8;
	private static final int COLLAPSED_WIDTH = 18;
	private static final int TAB_CLEARANCE = 20;
	private static final int TOGGLE_HEIGHT = 20;
	private static final int PREVIEW_LEFT_INSET = 8;
	private static final int PREVIEW_TOP_INSET = 22;
	private static final int PREVIEW_RIGHT_INSET = 8;
	private static final int PREVIEW_BOTTOM_INSET = 6;

	public static RecipeMapInspectorLayout calculate(int left, int top, int width, int height, boolean expanded) {
		int panelTop = top + TAB_CLEARANCE;
		int panelHeight = Math.max(1, height - TAB_CLEARANCE);
		if (!expanded) {
			IntRect panel = new IntRect(left + width - COLLAPSED_WIDTH, panelTop, COLLAPSED_WIDTH, panelHeight);
			return new RecipeMapInspectorLayout(new IntRect(left, top, width - COLLAPSED_WIDTH - GAP, height),
					panel, panel, new IntRect(panel.left(), panel.bottom(), panel.width(), 0), false, false);
		}
		boolean overlay = width < 700;
		int panelWidth = expandedPanelWidth(width);
		int panelLeft = left + width - panelWidth;
		IntRect panel = new IntRect(panelLeft, panelTop, panelWidth, panelHeight);
		int previewHeight = Mth.clamp((int) Math.floor(panelHeight * 0.42), 120, 240);
		IntRect preview = new IntRect(panelLeft, panelTop, panelWidth, previewHeight);
		IntRect info = new IntRect(panelLeft, panelTop + previewHeight, panelWidth, panelHeight - previewHeight);
		int mapWidth = overlay ? width : width - panelWidth - GAP;
		return new RecipeMapInspectorLayout(new IntRect(left, top, mapWidth, height), panel, preview, info, overlay, true);
	}

	public static RecipeMapInspectorLayout calculateCrafting(
			int left, int top, int width, int height, boolean expanded) {
		if (!expanded) return calculate(left, top, width, height, false);

		int panelTop = top + TAB_CLEARANCE;
		int panelHeight = Math.max(1, height - TAB_CLEARANCE);
		boolean overlay = width < 700;
		int preferredWidth = Mth.clamp((int) Math.floor(width * (width < 700 ? 0.46 : 0.30)),
				220, 280);
		int panelWidth = Math.min(width, preferredWidth);
		int panelLeft = left + width - panelWidth;
		IntRect panel = new IntRect(panelLeft, panelTop, panelWidth, panelHeight);

		int previewWidth = Mth.clamp((int) Math.floor(panelWidth * 0.42), 96, 128);
		int infoWidth = panelWidth - previewWidth;
		IntRect info = new IntRect(panelLeft, panelTop, infoWidth, panelHeight);
		IntRect preview = new IntRect(panelLeft + infoWidth, panelTop,
				previewWidth, Math.min(panelHeight, previewWidth));
		int mapWidth = overlay ? width : width - panelWidth - GAP;
		return new RecipeMapInspectorLayout(new IntRect(left, top, mapWidth, height),
				panel, preview, info, overlay, true);
	}

	public static int expandedPanelWidth(int width) {
		return width < 700 ? Math.min(220, Math.max(160, width - 48))
				: Mth.clamp((int) Math.floor(width * 0.22), 190, 260);
	}

	public IntRect previewContent() {
		return preview.inset(PREVIEW_LEFT_INSET, PREVIEW_TOP_INSET,
				PREVIEW_RIGHT_INSET, PREVIEW_BOTTOM_INSET);
	}

	public boolean isOverToggle(double mouseX, double mouseY) {
		return panel.width() > 0 && mouseX >= panel.left() && mouseX < panel.left() + COLLAPSED_WIDTH
				&& mouseY >= panel.top() && mouseY < panel.top() + TOGGLE_HEIGHT;
	}

	public int layerButtonAt(double mouseX, double mouseY, int maxLayer) {
		if (!expanded || maxLayer <= 0) return 0;
		IntRect content = previewContent();
		int x = content.right() - 18;
		int centerY = content.top() + content.height() / 2;
		if (ScreenDrawUtils.isOverLayerButton(mouseX, mouseY, x, centerY - 30)) return 1;
		if (ScreenDrawUtils.isOverLayerButton(mouseX, mouseY, x, centerY + 14)) return -1;
		return 0;
	}

	public record IntRect(int left, int top, int width, int height) {
		public int right() { return left + width; }
		public int bottom() { return top + height; }
		public boolean contains(double x, double y) {
			return x >= left && x < right() && y >= top && y < bottom();
		}
		public IntRect inset(int leftInset, int topInset, int rightInset, int bottomInset) {
			return new IntRect(left + leftInset, top + topInset,
					Math.max(1, width - leftInset - rightInset),
					Math.max(1, height - topInset - bottomInset));
		}
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

/** Fixed screen-space chrome for both radial recipe-map tabs. */
final class RecipeMapControlsLayout {
	private static final int FILTER_LEFT_PAD = 8;
	private static final int FILTER_TOP_PAD = 5;
	private static final int FILTER_HEIGHT = 13;
	private static final int FILTER_WIDTH = 68;
	private static final int FILTER_GAP = 4;
	private static final int RECENT_LEFT_PAD = 58;
	private static final int RECENT_BASELINE_BOTTOM_PAD = 16;
	private static final int RECENT_ICON_OFFSET = 39;
	private static final int RECENT_ICON_WIDTH = 18;
	private static final int RECENT_ICON_HEIGHT = 14;

	private RecipeMapControlsLayout() {}

	static Result calculate(RecipeMapInspectorLayout.IntRect viewport) {
		int filterX = viewport.left() + FILTER_LEFT_PAD;
		int filterY = viewport.top() + FILTER_TOP_PAD;
		RecipeMapInspectorLayout.IntRect degree = new RecipeMapInspectorLayout.IntRect(
				filterX, filterY, FILTER_WIDTH, FILTER_HEIGHT);
		RecipeMapInspectorLayout.IntRect family = new RecipeMapInspectorLayout.IntRect(
				filterX, degree.bottom() + FILTER_GAP, FILTER_WIDTH, FILTER_HEIGHT);
		RecipeMapInspectorLayout.IntRect layer = new RecipeMapInspectorLayout.IntRect(
				filterX, family.bottom() + FILTER_GAP, FILTER_WIDTH, FILTER_HEIGHT);
		int recentLabelX = viewport.left() + RECENT_LEFT_PAD;
		int recentBaselineY = viewport.bottom() - RECENT_BASELINE_BOTTOM_PAD;
		RecipeMapInspectorLayout.IntRect firstRecent = new RecipeMapInspectorLayout.IntRect(
				recentLabelX + RECENT_ICON_OFFSET, recentBaselineY - 2,
				RECENT_ICON_WIDTH, RECENT_ICON_HEIGHT);
		return new Result(degree, family, layer, recentLabelX, recentBaselineY, firstRecent);
	}

	record Result(RecipeMapInspectorLayout.IntRect degree,
			RecipeMapInspectorLayout.IntRect family,
			RecipeMapInspectorLayout.IntRect layer,
			int recentLabelX, int recentBaselineY,
			RecipeMapInspectorLayout.IntRect firstRecent) {}
}

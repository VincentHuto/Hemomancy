package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

final class IchorianSigilSidebarLayout {
	static final int HEADER_HEIGHT = 18;
	static final int HEADER_ADVANCE = 20;
	static final int ROW_HEIGHT = 14;
	static final int ROW_ADVANCE = 16;

	private IchorianSigilSidebarLayout() {
	}

	static int rowIndexAt(double mouseX, double mouseY, int x, int y, int width,
			boolean expanded, int rowCount) {
		if (!expanded || rowCount <= 0 || mouseX < x || mouseX > x + width) return -1;
		double rowOffset = mouseY - (y + HEADER_ADVANCE);
		if (rowOffset < 0.0D) return -1;
		int index = (int) (rowOffset / ROW_ADVANCE);
		if (index >= rowCount || rowOffset - index * ROW_ADVANCE > ROW_HEIGHT) return -1;
		return index;
	}
}

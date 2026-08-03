package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.util.ArrayList;
import java.util.List;

final class BottomRightTabLayout {
	private BottomRightTabLayout() {
	}

	static List<Bounds> layout(int guiLeft, int guiTop, int guiWidth, int guiHeight,
			int tabPad, int tabHeight, List<Integer> tabWidths) {
		List<Bounds> bounds = new ArrayList<>(tabWidths.size());
		for (int i = 0; i < tabWidths.size(); i++) bounds.add(null);

		int right = guiLeft + guiWidth - tabPad;
		int y = guiTop + guiHeight - tabPad - tabHeight;
		for (int i = tabWidths.size() - 1; i >= 0; i--) {
			int width = tabWidths.get(i);
			int x = right - width;
			bounds.set(i, new Bounds(x, y, width, tabHeight));
			right = x - tabPad;
		}
		return List.copyOf(bounds);
	}

	record Bounds(int x, int y, int width, int height) {
		boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width
					&& mouseY >= y && mouseY <= y + height;
		}
	}
}

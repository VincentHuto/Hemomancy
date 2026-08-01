package com.vincenthuto.hemomancy.client.screen.skilltree.util;

/** Shared navigation rules for layered rite and structure previews. */
public final class LayerViewNavigation {
	private LayerViewNavigation() {}

	public static int cycle(int visibleLayer, int maxLayer, int direction) {
		if (maxLayer <= 0) return -1;
		if (direction > 0) {
			if (visibleLayer < 0) return maxLayer;
			return visibleLayer < maxLayer ? visibleLayer + 1 : -1;
		}
		if (direction < 0) {
			if (visibleLayer < 0) return 0;
			return visibleLayer > 0 ? visibleLayer - 1 : -1;
		}
		return visibleLayer;
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.util;

public final class ProgressViewportCulling {

	private ProgressViewportCulling() {
	}

	public static boolean intersects(int centerX, int centerY, int halfExtent, int left, int top, int right,
			int bottom) {
		return centerX + halfExtent >= left && centerX - halfExtent <= right && centerY + halfExtent >= top
				&& centerY - halfExtent <= bottom;
	}
}

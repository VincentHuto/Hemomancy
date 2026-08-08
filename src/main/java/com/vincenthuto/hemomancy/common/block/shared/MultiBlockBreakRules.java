package com.vincenthuto.hemomancy.common.block.shared;

public final class MultiBlockBreakRules {
	private MultiBlockBreakRules() {
	}

	public static boolean shouldDestroyMainFromPlayer(boolean qliphothBloom, boolean creativePlayer) {
		return !qliphothBloom || creativePlayer;
	}
}

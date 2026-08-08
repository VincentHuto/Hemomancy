package com.vincenthuto.hemomancy.common.event.worldevent;

final class ChamberPlayerSafeguardRules {
	private ChamberPlayerSafeguardRules() {
	}

	static boolean shouldApply(boolean creative, boolean spectator, boolean vesperOrdealActive) {
		return !creative && !spectator && !vesperOrdealActive;
	}
}

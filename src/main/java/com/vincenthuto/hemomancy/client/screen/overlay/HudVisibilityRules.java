package com.vincenthuto.hemomancy.client.screen.overlay;

/** When the mod's HUD layers draw: never under F1 (hide GUI), and never for a spectator, like vanilla's status bars. */
public final class HudVisibilityRules {
	private HudVisibilityRules() {
	}

	public static boolean shows(boolean hideGui, boolean spectator) {
		return !hideGui && !spectator;
	}
}

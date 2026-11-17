package com.vincenthuto.hemomancy.client.screen.overlay;

/** When the mod's HUD layers draw: never under F1 (hide GUI), and never for a spectator, like vanilla's status bars. */
public final class HudVisibilityRules {
	private HudVisibilityRules() {
	}

	public static boolean shows(boolean hideGui, boolean spectator) {
		return !hideGui && !spectator;
	}

	/** The vessel, reserve and Morphling jar belong to the Vascularium Charm. */
	public static boolean showsCharmParts(boolean charm, boolean bandolier) {
		return charm;
	}

	/** The Temper vials belong to the Titration Bandolier, worn with or without the charm. */
	public static boolean showsVials(boolean charm, boolean bandolier) {
		return bandolier;
	}

	public static boolean showsAnything(boolean charm, boolean bandolier) {
		return showsCharmParts(charm, bandolier) || showsVials(charm, bandolier);
	}

	/** A Morphling's passive icon leaves vanilla's effect row while the hematic HUD's jar already shows it. */
	public static boolean showsMorphlingEffectIcon(boolean jarShown) {
		return !jarShown;
	}
}

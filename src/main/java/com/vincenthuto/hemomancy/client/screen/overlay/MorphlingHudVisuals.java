package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.Map;

final class MorphlingHudVisuals {
	private static final Map<String, Visual> VISUALS = Map.of(
			"morphling_deadmans_purse", visual("deadmans_purse", 22, 0xFFD34A52),
			"morphling_gravecap", visual("gravecap", 25, 0xFFB95272),
			"morphling_witchs_ear", visual("witchs_ear", 20, 0xFF9C63C7),
			"morphling_foxfire", visual("lumenlace", 23, 0xFFF0D15A),
			"morphling_bootlace", visual("bootlace", 24, 0xFF76509D),
			"morphling_irontooth", visual("irontooth", 26, 0xFFC28A63),
			"morphling_emberfang", visual("emberfang", 24, 0xFFE03A63),
			"morphling_winter_shroud", visual("winter_shroud", 23, 0xFF9AC7D8));

	private MorphlingHudVisuals() {
	}

	static Visual forItemPath(String itemPath) {
		return VISUALS.get(itemPath);
	}

	private static Visual visual(String textureName, int mouthY, int accentColor) {
		return new Visual(textureName, 5, mouthY, accentColor);
	}

	record Visual(String textureName, int mouthX, int mouthY, int accentColor) {
	}
}

package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.Map;

final class MorphlingHudVisuals {
	private static final Map<String, Visual> VISUALS = Map.of(
			"morphling_deadmans_purse", new Visual("deadmans_purse", 0xFFD34A52),
			"morphling_gravecap", new Visual("gravecap", 0xFFB95272),
			"morphling_witchs_ear", new Visual("witchs_ear", 0xFF9C63C7),
			"morphling_lumenlace", new Visual("lumenlace", 0xFFF0D15A),
			"morphling_bootlace", new Visual("bootlace", 0xFF76509D),
			"morphling_irontooth", new Visual("irontooth", 0xFFC28A63),
			"morphling_emberfang", new Visual("emberfang", 0xFFE03A63),
			"morphling_winter_shroud", new Visual("winter_shroud", 0xFF9AC7D8));

	private MorphlingHudVisuals() {
	}

	static Visual forItemPath(String itemPath) {
		return VISUALS.get(itemPath);
	}

	record Visual(String textureName, int accentColor) {
	}
}

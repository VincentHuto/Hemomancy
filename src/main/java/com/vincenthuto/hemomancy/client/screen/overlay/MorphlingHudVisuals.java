package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;

/** HUD sprite and accent per strain, read from the strain table. */
final class MorphlingHudVisuals {
	private MorphlingHudVisuals() {
	}

	static Visual forItemPath(String itemPath) {
		return MorphlingStrains.byItemPath(itemPath)
				.map(strain -> new Visual(strain.hudTexture(), strain.accentColor()))
				.orElse(null);
	}

	record Visual(String textureName, int accentColor) {
	}
}

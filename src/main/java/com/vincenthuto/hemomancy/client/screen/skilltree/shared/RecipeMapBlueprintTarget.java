package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;

import javax.annotation.Nullable;

final class RecipeMapBlueprintTarget {
	private RecipeMapBlueprintTarget() {
	}

	@Nullable
	static MnemonicBlueprintTarget from(RecipeMapEntry entry) {
		if (entry == null || !entry.unlocked()) return null;
		return switch (entry.key().kind()) {
			case RITE, FLOOR -> new MnemonicBlueprintTarget(
					MnemonicBlueprintTarget.Type.CARDINAL_RITE, entry.id());
			case CRAFTING -> new MnemonicBlueprintTarget(
					MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, entry.id());
			case SIGIL -> null;
		};
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import com.vincenthuto.hemomancy.common.recipe.RiteDiscoveryRules;
import net.minecraft.resources.ResourceLocation;

/** Client facade over the shared discovery rules also enforced by the imprint packet. */
public final class RiteUnlockRegistry {
	private RiteUnlockRegistry() {
	}

	public static UnlockPredicate get(ResourceLocation recipeId) {
		return player -> RiteDiscoveryRules.isDiscovered(player, recipeId);
	}
}

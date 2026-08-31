package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import net.minecraft.network.chat.Component;

public final class ScarLoreData {
	private static final String KEY_PREFIX = "screen.hemomancy.scars.lore.";

	private ScarLoreData() {}

	public static Component getLore(String recipePathKey) {
		return Component.translatable(KEY_PREFIX + recipePathKey);
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

final class MaterialIconCache {
	private final Map<MaterialEntry, ItemStack> icons = new HashMap<>();

	void initialize(List<MaterialEntry> entries) {
		icons.clear();
		for (MaterialEntry entry : entries) icons.put(entry, entry.iconStack().get());
	}

	ItemStack get(MaterialEntry entry) {
		return icons.get(entry);
	}
}

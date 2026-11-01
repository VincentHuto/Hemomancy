package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;

/**
 * What an {@code items} want accepts: the item and tag entries of a vanilla ingredient ({@code {"item": ...}},
 * {@code {"tag": ...}} or an array of them). Held as ids so a card can be checked before tags are bound; tags resolve
 * when they are tested.
 */
public record NpcTradeMatch(List<ResourceLocation> items, List<ResourceLocation> tags) {
	public NpcTradeMatch {
		items = List.copyOf(items);
		tags = List.copyOf(tags);
	}

	public boolean matches(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) return true;
		return tags.stream().anyMatch(tag -> stack.is(TagKey.create(Registries.ITEM, tag)));
	}

	/** The same entries as a vanilla ingredient, for display. */
	public Ingredient toIngredient() {
		Stream<Ingredient.Value> values = Stream.concat(
				items.stream().map(id -> new Ingredient.ItemValue(new ItemStack(BuiltInRegistries.ITEM.get(id)))),
				tags.stream().map(tag -> new Ingredient.TagValue(TagKey.create(Registries.ITEM, tag))));
		return Ingredient.fromValues(values);
	}
}

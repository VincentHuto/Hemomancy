package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * One stack a card pays: an item, a count and, optionally, its data components as JSON (held as text so the record
 * stays immutable; {@link #toStack} decodes it against the server's registries).
 */
public record NpcTradeGive(ResourceLocation item, int count, Optional<String> components) {
	public ItemStack toStack(HolderLookup.Provider registries) {
		JsonObject json = new JsonObject();
		json.addProperty("id", item.toString());
		json.addProperty("count", count);
		components.ifPresent(text -> json.add("components", JsonParser.parseString(text)));
		return ItemStack.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
				.getOrThrow(JsonSyntaxException::new);
	}
}

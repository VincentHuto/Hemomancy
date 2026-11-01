package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.Set;
import java.util.stream.Collectors;

/** Re-checks the loaded want cards once the server's item tags are bound (they aren't while data loads). */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class NpcTradeTagEvents {
	private NpcTradeTagEvents() {}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
		Registry<Item> items = event.getRegistryAccess().registryOrThrow(Registries.ITEM);
		NpcTradeRegistry.bindTags(tag -> items.getTag(TagKey.create(Registries.ITEM, tag))
				.map(set -> set.stream().map(Holder::unwrapKey).flatMap(java.util.Optional::stream)
						.map(ResourceKey::location).collect(Collectors.toSet()))
				.orElse(Set.of()));
	}
}

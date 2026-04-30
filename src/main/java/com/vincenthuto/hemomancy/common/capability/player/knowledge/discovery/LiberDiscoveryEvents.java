package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.Set;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.common.book.knowledge.BookEntryRegistry;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * Thin bridging handlers that read from HutosLib's {@link BookEntryRegistry}
 * and write the resulting unlocks into Hemomancy's own {@code ILiberKnowledge}
 * capability via {@link LiberKnowledgeHelper}.
 *
 * <p>HutosLib's {@code BookDiscoveryEvents} mirrors the same events and writes
 * into HutosLib's own {@code BookKnowledge} attachment (used by HutosLib's
 * generic guide). Because Hemomancy stores knowledge in its own capability
 * ({@code HemoAttachmentTypes.LIBER_KNOWLEDGE}), it must maintain its own
 * listeners — but registration of <em>what unlocks what</em> is now shared
 * through {@code BookEntryRegistry} rather than Hemomancy's own internal maps.
 *
 * <p>Item and advancement → entry mappings are registered during
 * {@code FMLCommonSetupEvent} in
 * {@link com.vincenthuto.hemomancy.Hemomancy#initLiberBookUnlocks()}.
 *
 * <p>Rite-based and dialogue-based unlocks are not event-driven and are
 * triggered programmatically from {@link LiberKnowledgeHelper} directly.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LiberDiscoveryEvents {
	private LiberDiscoveryEvents() {
	}

	@SubscribeEvent
	public static void onItemPickup(ItemEntityPickupEvent.Post event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;
		if (event.getOriginalStack().isEmpty()) return;
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(event.getOriginalStack().getItem());
		Set<ResourceLocation> entries = BookEntryRegistry.entriesForItem(itemId);
		if (!entries.isEmpty()) {
			LiberKnowledgeHelper.unlockEntries(player, entries, CommonDiscoverySource.ITEM_PICKUP);
		}
	}

	@SubscribeEvent
	public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Set<ResourceLocation> entries = BookEntryRegistry.entriesForAdvancement(event.getAdvancement().id());
		if (!entries.isEmpty()) {
			LiberKnowledgeHelper.unlockEntries(player, entries, CommonDiscoverySource.ADVANCEMENT);
		}
	}
}

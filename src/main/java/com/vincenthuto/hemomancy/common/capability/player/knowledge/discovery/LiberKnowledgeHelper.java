package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.DiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledgeEvents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LiberKnowledgeHelper {
	private LiberKnowledgeHelper() {
	}

	public static boolean unlockEntry(ServerPlayer player, ResourceLocation entryId, DiscoverySource source) {
		return HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> {
					boolean changed = knowledge.unlockEntry(entryId, source);
					if (changed) {
						LiberKnowledgeEvents.sync(player);
					}
					return changed;
				})
				.orElse(false);
	}

	public static boolean unlockEntries(ServerPlayer player, Iterable<ResourceLocation> entryIds, DiscoverySource source) {
		return HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> {
					boolean changed = false;
					for (ResourceLocation entryId : entryIds) {
						changed |= knowledge.unlockEntry(entryId, source);
					}
					if (changed) {
						LiberKnowledgeEvents.sync(player);
					}
					return changed;
				})
				.orElse(false);
	}

	public static boolean unlockForRite(ServerPlayer player, String ritePath) {
		return unlockEntries(player, LiberEntryDefinitions.forRite(ritePath), DiscoverySource.RITE);
	}

	public static boolean unlockForDegree(ServerPlayer player, int degree) {
		boolean changed = false;
		if (degree >= 1) {
			changed |= unlockEntry(player, LiberEntryDefinitions.HEMOMANCY, DiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.THE_HARBINGERS, DiscoverySource.DEGREE);
		}
		if (degree >= 2) changed |= unlockEntry(player, LiberEntryDefinitions.DEGREES, DiscoverySource.DEGREE);
		if (degree >= 3) changed |= unlockEntry(player, LiberEntryDefinitions.ORDER_BELIEFS, DiscoverySource.DEGREE);
		if (degree >= 4) changed |= unlockEntry(player, LiberEntryDefinitions.HISTORICAL_RECORD, DiscoverySource.DEGREE);
		if (degree >= 5) changed |= unlockEntry(player, LiberEntryDefinitions.HERMITS, DiscoverySource.DEGREE);
		if (degree >= 7) changed |= unlockEntry(player, LiberEntryDefinitions.ENTITY, DiscoverySource.DEGREE);
		if (degree >= 8) changed |= unlockEntry(player, LiberEntryDefinitions.TRUTH, DiscoverySource.DEGREE);
		return changed;
	}

	public static boolean unlockForAdvancement(ServerPlayer player, ResourceLocation advancementId) {
		return unlockEntries(player, LiberEntryDefinitions.forAdvancement(advancementId), DiscoverySource.ADVANCEMENT);
	}

	public static boolean unlockForItemPickup(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return unlockEntries(player, LiberEntryDefinitions.forItem(itemId), DiscoverySource.ITEM_PICKUP);
	}

	public static boolean handleDialogueEvent(ServerPlayer player, String eventId) {
		return unlockEntries(player, LiberEntryDefinitions.forDialogueEvent(eventId), DiscoverySource.DIALOGUE);
	}

	public static boolean unlockMemo(ServerPlayer player, ResourceLocation memoId, ResourceLocation entryId) {
		return HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> {
					boolean changed = knowledge.unlockMemo(memoId, entryId);
					if (changed) {
						LiberKnowledgeEvents.sync(player);
					}
					return changed;
				})
				.orElse(false);
	}

	public static boolean hasEntry(Player player, ResourceLocation entryId) {
		return HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> knowledge.hasEntry(entryId))
				.orElse(false);
	}
}

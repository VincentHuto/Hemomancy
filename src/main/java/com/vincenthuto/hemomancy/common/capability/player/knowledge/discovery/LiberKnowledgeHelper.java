package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class LiberKnowledgeHelper {
	private LiberKnowledgeHelper() {
	}

	public static boolean unlockEntry(ServerPlayer player, ResourceLocation entryId, IDiscoverySource source) {
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

	public static boolean unlockEntries(ServerPlayer player, Iterable<ResourceLocation> entryIds, IDiscoverySource source) {
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
		return unlockEntries(player, LiberEntryDefinitions.forRite(ritePath), HemomancyDiscoverySource.RITE);
	}

	public static boolean unlockForDegree(ServerPlayer player, int degree) {
		boolean changed = false;
		if (degree >= 1) {
			changed |= unlockEntry(player, LiberEntryDefinitions.HEMOMANCY, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.THE_HARBINGERS, HemomancyDiscoverySource.DEGREE);
		}
		if (degree >= 2) changed |= unlockEntry(player, LiberEntryDefinitions.DEGREES, HemomancyDiscoverySource.DEGREE);
		if (degree >= 3) changed |= unlockEntry(player, LiberEntryDefinitions.ORDER_BELIEFS, HemomancyDiscoverySource.DEGREE);
		if (degree >= 4) changed |= unlockEntry(player, LiberEntryDefinitions.HISTORICAL_RECORD, HemomancyDiscoverySource.DEGREE);
		if (degree >= 5) changed |= unlockEntry(player, LiberEntryDefinitions.HERMITS, HemomancyDiscoverySource.DEGREE);
		if (degree >= 7) changed |= unlockEntry(player, LiberEntryDefinitions.ENTITY, HemomancyDiscoverySource.DEGREE);
		if (degree >= 8) changed |= unlockEntry(player, LiberEntryDefinitions.TRUTH, HemomancyDiscoverySource.DEGREE);
		return changed;
	}

	public static boolean handleDialogueEvent(ServerPlayer player, String eventId) {
		return unlockEntries(player, LiberEntryDefinitions.forDialogueEvent(eventId), HemomancyDiscoverySource.DIALOGUE);
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

	public static boolean knowsMemo(Player player, ResourceLocation memoId) {
		return HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> knowledge.knowsMemo(memoId))
				.orElse(false);
	}
}

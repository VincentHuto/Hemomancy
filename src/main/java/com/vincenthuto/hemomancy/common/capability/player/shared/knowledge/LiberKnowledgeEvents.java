package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public final class LiberKnowledgeEvents {
	private LiberKnowledgeEvents() {
	}


	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncLiberKnowledge(HemoCapabilityAccess.getLiberKnowledge(player)));
	}

	public static void sync(ServerPlayer player, Set<ResourceLocation> markUnreadEntries) {
		PacketHandler.sendToPlayer(player,
						new PacketSyncLiberKnowledge(HemoCapabilityAccess.getLiberKnowledge(player), markUnreadEntries));
	}
}

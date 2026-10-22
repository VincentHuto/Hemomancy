package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import com.vincenthuto.hemomancy.Hemomancy;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LiberKnowledgeEvents {
	private LiberKnowledgeEvents() {
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			migrateLegacyNotes(player);
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			migrateLegacyNotes(player);
		}
	}

	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			migrateLegacyNotes(player);
		}
	}

	private static void migrateLegacyNotes(ServerPlayer player) {
		MemoHelper.migrateLegacyFieldNotes(player);
		if (HarbingerAdvancementGranter.isVeinMasonFirstLesson(player)) {
			LiberKnowledgeHelper.unlockEntry(player, LiberEntryDefinitions.SCAR_PRACTICE,
					CommonDiscoverySource.ADVANCEMENT);
		}
	}

	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncLiberKnowledge(HemoCapabilityAccess.getLiberKnowledge(player)));
	}

	public static void sync(ServerPlayer player, Set<ResourceLocation> markUnreadEntries) {
		PacketHandler.sendToPlayer(player,
						new PacketSyncLiberKnowledge(HemoCapabilityAccess.getLiberKnowledge(player), markUnreadEntries));
	}
}

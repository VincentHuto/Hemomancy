package com.vincenthuto.hemomancy.client;

import com.vincenthuto.hutoslib.client.screen.guide.HLGuiGuideTitlePage;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class ClientLiberScreenHooks {
	private ClientLiberScreenHooks() {
	}

	public static void markEntriesUnreadAndRefresh(UUID playerId, Collection<ResourceLocation> entryIds) {
		HLGuiGuideTitlePage.markEntriesUnreadAndRefreshIfOpen(playerId, entryIds);
	}

	public static void knowledgeSynced(net.minecraft.world.entity.player.Player player,
			com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge knowledge,
			java.util.Set<ResourceLocation> before, java.util.Set<ResourceLocation> after,
			java.util.Set<ResourceLocation> explicit) {
		com.vincenthuto.hutoslib.client.book.BookClientHooks.knowledgeSnapshot(player, knowledge, before, after, explicit);
	}

	public static void progressChanged(net.minecraft.world.entity.player.Player player) {
		com.vincenthuto.hutoslib.client.book.BookClientHooks.playerStateChanged(player);
	}
}



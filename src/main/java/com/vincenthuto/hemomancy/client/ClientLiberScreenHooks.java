package com.vincenthuto.hemomancy.client;

import java.util.Collection;
import java.util.UUID;

import com.vincenthuto.hutoslib.client.screen.guide.HLGuiGuideTitlePage;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientLiberScreenHooks {
	private ClientLiberScreenHooks() {
	}

	public static void markEntriesUnreadAndRefresh(UUID playerId, Collection<ResourceLocation> entryIds) {
		HLGuiGuideTitlePage.markEntriesUnreadAndRefreshIfOpen(playerId, entryIds);
	}
}



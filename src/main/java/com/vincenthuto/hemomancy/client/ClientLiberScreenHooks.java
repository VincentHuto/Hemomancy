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
}



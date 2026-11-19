package com.vincenthuto.hemomancy.client.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.FinaleCrossingClientState;
import com.vincenthuto.hemomancy.common.network.ClientPayloadHandlers;
import com.vincenthuto.hemomancy.common.network.circus.FinaleCrossingPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The Ringmaster finale's client-bound payloads (2026-11-18). */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientboundFinaleHandlers {
	private ClientboundFinaleHandlers() {
	}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ClientPayloadHandlers.register(FinaleCrossingPacket.TYPE, ClientboundFinaleHandlers::handle);
	}

	static void handle(FinaleCrossingPacket packet, IPayloadContext context) {
		FinaleCrossingClientState.capture();
	}
}

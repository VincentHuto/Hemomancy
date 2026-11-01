package com.vincenthuto.hemomancy.client.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen;
import com.vincenthuto.hemomancy.common.network.ClientPayloadHandlers;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client halves of the NPC trade payloads, registered with {@link ClientPayloadHandlers}. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientboundTradeHandlers {
	private ClientboundTradeHandlers() {
	}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ClientPayloadHandlers.register(NpcTradeResultPacket.TYPE, ClientboundTradeHandlers::handle);
	}

	static void handle(NpcTradeResultPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> DialogueScreen.receiveTradeResult(msg.entityId(),
				new DialogueScreen.TradeReply(msg.offerId(), msg.outcome(), msg.reasonKey()), msg.board()));
	}
}

package com.vincenthuto.hemomancy.client.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterClientState;
import com.vincenthuto.hemomancy.common.network.ClientPayloadHandlers;
import com.vincenthuto.hemomancy.common.network.boss.SaintMeterPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The client halves of the Saint combat kit's payload: the meter a challenger reads. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientboundSaintHandlers {
	private ClientboundSaintHandlers() {
	}

	@SubscribeEvent
	public static void register(FMLClientSetupEvent event) {
		ClientPayloadHandlers.register(SaintMeterPacket.TYPE, ClientboundSaintHandlers::meter);
	}

	static void meter(SaintMeterPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> SaintMeterClientState.accept(packet));
	}
}

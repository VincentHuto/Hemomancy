package com.vincenthuto.hemomancy.network;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.event.LayerEvents;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PacketCurvedHornAnimation {

	public PacketCurvedHornAnimation(FriendlyByteBuf buf) {
	}

	public PacketCurvedHornAnimation() {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> LayerEvents::playHornAnimation);
		});
		ctx.get().setPacketHandled(true);
	}

}

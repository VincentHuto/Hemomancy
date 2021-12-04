package com.vincenthuto.hemomancy.network.capa.runes;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.event.ClientEventSubscriber;

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
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEventSubscriber::playHornAnimation);
		});
		ctx.get().setPacketHandled(true);
	}

}

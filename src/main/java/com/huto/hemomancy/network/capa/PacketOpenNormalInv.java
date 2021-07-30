package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketOpenNormalInv {

	public PacketOpenNormalInv(FriendlyByteBuf buf) {
	}

	public PacketOpenNormalInv() {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer playerEntity = ctx.get().getSender();
			if (playerEntity != null) {
				playerEntity.openContainer.onContainerClosed(playerEntity);
				playerEntity.openContainer = playerEntity.container;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}

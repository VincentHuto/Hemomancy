package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketOpenNormalInv {

	public PacketOpenNormalInv() {
	}

	public PacketOpenNormalInv(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer playerEntity = ctx.get().getSender();
			if (playerEntity != null) {
				playerEntity.containerMenu.removed(playerEntity);
				playerEntity.containerMenu = playerEntity.inventoryMenu;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}

package com.vincenthuto.hemomancy.network;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.gui.RuneMenuProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class PacketOpenRunesInv {

	public PacketOpenRunesInv(FriendlyByteBuf buf) {
	}

	public PacketOpenRunesInv() {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ctx.get().getSender().doCloseContainer();
			NetworkHooks.openGui(ctx.get().getSender(), new RuneMenuProvider());
		});
		ctx.get().setPacketHandled(true);
	}
}

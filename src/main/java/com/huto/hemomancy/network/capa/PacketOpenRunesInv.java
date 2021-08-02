package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.gui.mindrunes.GuiProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

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
			NetworkHooks.openGui(ctx.get().getSender(), new GuiProvider());
		});
		ctx.get().setPacketHandled(true);
	}
}

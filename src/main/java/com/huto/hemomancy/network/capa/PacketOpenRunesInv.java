package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.gui.mindrunes.GuiProvider;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

public class PacketOpenRunesInv {

	public PacketOpenRunesInv(PacketBuffer buf) {
	}

	public PacketOpenRunesInv() {
	}

	public void decode(PacketBuffer buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ctx.get().getSender().closeContainer();
			NetworkHooks.openGui(ctx.get().getSender(), new GuiProvider());
		});
		ctx.get().setPacketHandled(true);
	}
}

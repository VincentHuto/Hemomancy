package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class PacketOpenVascularView {

	public PacketOpenVascularView() {
	}

	public PacketOpenVascularView(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ctx.get().getSender().doCloseContainer();
			NetworkHooks.openScreen(ctx.get().getSender(), new VascularViewMenuProvider());
		});
		ctx.get().setPacketHandled(true);
	}
}

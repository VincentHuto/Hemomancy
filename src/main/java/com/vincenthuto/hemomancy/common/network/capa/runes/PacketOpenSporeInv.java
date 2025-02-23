package com.vincenthuto.hemomancy.common.network.capa.runes;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.menu.FungalImplantMenuProvider;
import com.vincenthuto.hemomancy.common.menu.RuneMenuProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class PacketOpenSporeInv {

	public PacketOpenSporeInv() {
	}

	public PacketOpenSporeInv(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ctx.get().getSender().doCloseContainer();
			NetworkHooks.openScreen(ctx.get().getSender(), new FungalImplantMenuProvider());
		});
		ctx.get().setPacketHandled(true);
	}
}

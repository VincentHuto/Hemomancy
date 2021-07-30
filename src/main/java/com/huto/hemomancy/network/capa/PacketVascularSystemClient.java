package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.vascular.IVascularSystem;
import com.huto.hemomancy.capa.vascular.VascularSystemProvider;
import com.huto.hemomancy.network.PacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketVascularSystemClient {

	public PacketVascularSystemClient() {

	}

	public static void handle(final PacketVascularSystemClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IVascularSystem BloodFlow = sender.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> sender),
						new PacketVascularSystemServer(BloodFlow.getVascularSystem()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketVascularSystemClient msg, final FriendlyByteBuf packetBuffer) {

	}

	public static PacketVascularSystemClient decode(final FriendlyByteBuf packetBuffer) {
		return new PacketVascularSystemClient();
	}
}
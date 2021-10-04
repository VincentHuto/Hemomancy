package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.capa.player.vascular.VascularSystemProvider;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

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
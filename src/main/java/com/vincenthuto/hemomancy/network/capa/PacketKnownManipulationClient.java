package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class PacketKnownManipulationClient {

	public PacketKnownManipulationClient() {

	}

	public static void handle(final PacketKnownManipulationClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IKnownManipulations manips = sender.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> sender),
						new PacketKnownManipulationServer(manips));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketKnownManipulationClient msg, final FriendlyByteBuf packetBuffer) {

	}

	public static PacketKnownManipulationClient decode(final FriendlyByteBuf packetBuffer) {
		return new PacketKnownManipulationClient();
	}
}
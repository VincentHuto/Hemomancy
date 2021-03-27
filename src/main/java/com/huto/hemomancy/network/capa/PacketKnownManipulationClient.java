package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.network.PacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketKnownManipulationClient {

	public PacketKnownManipulationClient() {

	}

	public static void handle(final PacketKnownManipulationClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IKnownManipulations manips = sender.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> sender),
						new PacketKnownManipulationServer(manips.getKnownManips(), manips.getSelectedManip()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketKnownManipulationClient msg, final PacketBuffer packetBuffer) {

	}

	public static PacketKnownManipulationClient decode(final PacketBuffer packetBuffer) {
		return new PacketKnownManipulationClient();
	}
}
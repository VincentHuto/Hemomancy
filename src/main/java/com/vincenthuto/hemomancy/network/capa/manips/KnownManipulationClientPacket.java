package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class KnownManipulationClientPacket {

	public static KnownManipulationClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new KnownManipulationClientPacket();
	}

	public static void encode(final KnownManipulationClientPacket msg, final FriendlyByteBuf packetBuffer) {

	}

	public static void handle(final KnownManipulationClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IKnownManipulations manips = sender.getCapability(KnownManipulationProvider.MANIP_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> sender),
						new KnownManipulationServerPacket(manips));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public KnownManipulationClientPacket() {

	}
}
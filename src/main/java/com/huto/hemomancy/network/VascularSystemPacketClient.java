package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.vascularsystem.VascularSystemProvider;
import com.huto.hemomancy.capabilities.vascularsystem.IVascularSystem;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class VascularSystemPacketClient {

	public VascularSystemPacketClient() {

	}

	public static void handle(final VascularSystemPacketClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IVascularSystem BloodFlow = sender.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> sender),
						new VascularSystemPacketServer(BloodFlow.getVascularSystem()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final VascularSystemPacketClient msg, final PacketBuffer packetBuffer) {

	}

	public static VascularSystemPacketClient decode(final PacketBuffer packetBuffer) {
		return new VascularSystemPacketClient();
	}
}
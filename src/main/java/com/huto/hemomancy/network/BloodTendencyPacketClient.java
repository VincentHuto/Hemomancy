package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capabilities.tendency.IBloodTendency;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class BloodTendencyPacketClient {

	public BloodTendencyPacketClient() {

	}

	public static void handle(final BloodTendencyPacketClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodTendency BloodTendency = sender.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> sender),
						new BloodTendencyPacketServer(BloodTendency.getDevotion()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final BloodTendencyPacketClient msg, final PacketBuffer packetBuffer) {

	}

	public static BloodTendencyPacketClient decode(final PacketBuffer packetBuffer) {
		return new BloodTendencyPacketClient();
	}
}
package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.network.PacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketBloodTendencyClient {

	public PacketBloodTendencyClient() {

	}

	public static void handle(final PacketBloodTendencyClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodTendency bloodTendency = sender.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> sender),
						new PacketBloodTendencyServer(bloodTendency.getTendency()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketBloodTendencyClient msg, final PacketBuffer packetBuffer) {

	}

	public static PacketBloodTendencyClient decode(final PacketBuffer packetBuffer) {
		return new PacketBloodTendencyClient();
	}
}
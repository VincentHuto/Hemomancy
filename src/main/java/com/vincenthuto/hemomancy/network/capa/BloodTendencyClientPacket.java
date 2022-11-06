package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodTendencyClientPacket {

	public static BloodTendencyClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new BloodTendencyClientPacket();
	}

	public static void encode(final BloodTendencyClientPacket msg, final FriendlyByteBuf packetBuffer) {

	}

	public static void handle(final BloodTendencyClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodTendency bloodTendency = sender.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> sender),
						new BloodTendencyServerPacket(bloodTendency.getTendency()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public BloodTendencyClientPacket() {

	}
}
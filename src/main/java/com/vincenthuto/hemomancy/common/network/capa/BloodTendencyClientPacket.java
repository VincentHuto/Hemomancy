package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
				IBloodTendency bloodTendency = HemoCapabilityAccess.getBloodTendency(sender)
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
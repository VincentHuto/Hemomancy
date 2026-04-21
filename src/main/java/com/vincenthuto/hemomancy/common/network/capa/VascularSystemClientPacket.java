package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class VascularSystemClientPacket {

	public static VascularSystemClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new VascularSystemClientPacket();
	}

	public static void encode(final VascularSystemClientPacket msg, final FriendlyByteBuf packetBuffer) {

	}

	public static void handle(final VascularSystemClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IVascularSystem BloodFlow = HemoCapabilityAccess.getVascularSystem(sender)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> sender),
						new VascularSystemServerPacket(BloodFlow.getVascularSystem()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public VascularSystemClientPacket() {

	}
}
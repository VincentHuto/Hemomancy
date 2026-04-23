package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class KnownManipulationClientPacket implements CustomPacketPayload {

	public static final Type<KnownManipulationClientPacket> TYPE = new Type<>(Hemomancy.rloc("known_manipulation_client_packet"));
	public static final StreamCodec<FriendlyByteBuf, KnownManipulationClientPacket> STREAM_CODEC = StreamCodec.of(KnownManipulationClientPacket::encode, KnownManipulationClientPacket::decode);

	public static KnownManipulationClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new KnownManipulationClientPacket();
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final KnownManipulationClientPacket msg) {

	}

	public static void handle(final KnownManipulationClientPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.player(); // the client that sent this packet
			if (sender != null) {
				IKnownManipulations manips = HemoCapabilityAccess.getKnownManipulations(sender)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.sendToPlayer(sender, new KnownManipulationServerPacket(manips));
			}
		});
	}

	public KnownManipulationClientPacket() {

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

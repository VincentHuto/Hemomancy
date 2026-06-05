package com.vincenthuto.hemomancy.common.network.capa.harbinger.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class KnownSummonsRequestPacket implements CustomPacketPayload {
	public static final Type<KnownSummonsRequestPacket> TYPE = new Type<>(Hemomancy.rloc("known_summons_request_packet"));
	public static final StreamCodec<FriendlyByteBuf, KnownSummonsRequestPacket> STREAM_CODEC =
			StreamCodec.of(KnownSummonsRequestPacket::encode, KnownSummonsRequestPacket::decode);

	public KnownSummonsRequestPacket() {
	}

	public static void encode(FriendlyByteBuf buf, KnownSummonsRequestPacket msg) {
	}

	public static KnownSummonsRequestPacket decode(FriendlyByteBuf buf) {
		return new KnownSummonsRequestPacket();
	}

	public static void handle(final KnownSummonsRequestPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player)) {
				return;
			}
			HemoCapabilityAccess.getKnownSummons(player)
					.ifPresent(known -> KnownSummonEvents.sync(player, known));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

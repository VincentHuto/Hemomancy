package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineMembership;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: Leader/progenitor removes a player member from their bloodline.
 */
public class PacketKickBloodlinePlayer implements CustomPacketPayload {

	public static final Type<PacketKickBloodlinePlayer> TYPE = new Type<>(Hemomancy.rloc("packet_kick_bloodline_player"));
	public static final StreamCodec<FriendlyByteBuf, PacketKickBloodlinePlayer> STREAM_CODEC = StreamCodec.of(PacketKickBloodlinePlayer::encode, PacketKickBloodlinePlayer::decode);

	private final UUID targetUUID;

	public PacketKickBloodlinePlayer(UUID targetUUID) {
		this.targetUUID = targetUUID;
	}

	public static void encode(FriendlyByteBuf buf, PacketKickBloodlinePlayer msg) {
		buf.writeUUID(msg.targetUUID);
	}

	public static PacketKickBloodlinePlayer decode(FriendlyByteBuf buf) {
		return new PacketKickBloodlinePlayer(buf.readUUID());
	}

	public static void handle(final PacketKickBloodlinePlayer msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer leader) BloodlineMembership.expel(leader, msg.targetUUID);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

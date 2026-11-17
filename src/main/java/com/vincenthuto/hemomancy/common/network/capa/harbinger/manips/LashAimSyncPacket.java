package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to the clients watching a caster: the whip aim the server holds for their lash, so each watcher's copy of the
 * strand swings where the caster's does and not along their look.
 */
public record LashAimSyncPacket(int entityId, float yaw, float pitch) implements CustomPacketPayload {
	public static final Type<LashAimSyncPacket> TYPE = new Type<>(Hemomancy.rloc("lash_aim_sync"));
	public static final StreamCodec<FriendlyByteBuf, LashAimSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, LashAimSyncPacket::entityId, ByteBufCodecs.FLOAT, LashAimSyncPacket::yaw,
			ByteBufCodecs.FLOAT, LashAimSyncPacket::pitch, LashAimSyncPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

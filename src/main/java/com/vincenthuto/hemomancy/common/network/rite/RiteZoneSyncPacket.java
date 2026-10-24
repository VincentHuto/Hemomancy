package com.vincenthuto.hemomancy.common.network.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZone;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZoneKind;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Server to client: the visible rite zones of the receiver's current
 * dimension. Each packet replaces the client's whole list.
 */
public record RiteZoneSyncPacket(List<Zone> zones) implements CustomPacketPayload {
	public static final int MAX_ZONES = 512;

	public record Zone(RiteZoneKind kind, BlockPos center, int blockRadius) {
		private static final StreamCodec<ByteBuf, RiteZoneKind> KIND_CODEC =
				ByteBufCodecs.VAR_INT.map(RiteZoneKind::byOrdinal, RiteZoneKind::ordinal);
		public static final StreamCodec<ByteBuf, Zone> STREAM_CODEC = StreamCodec.composite(
				KIND_CODEC, Zone::kind,
				BlockPos.STREAM_CODEC, Zone::center,
				ByteBufCodecs.VAR_INT, Zone::blockRadius,
				Zone::new);

		public static Zone of(RiteZone zone) {
			return new Zone(zone.kind(), zone.center(), zone.blockRadius());
		}
	}

	public static final Type<RiteZoneSyncPacket> TYPE = new Type<>(Hemomancy.rloc("rite_zone_sync"));
	public static final StreamCodec<ByteBuf, RiteZoneSyncPacket> STREAM_CODEC = Zone.STREAM_CODEC
			.apply(ByteBufCodecs.list(MAX_ZONES))
			.map(RiteZoneSyncPacket::new, RiteZoneSyncPacket::zones);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

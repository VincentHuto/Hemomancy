package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendon.TendonAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server to the owner and the players tracking them: where a player's Tendon Line is rooted, or that it is loose
 * ({@code set} false). The owner's client claims the haul with it; every client draws the cord.
 */
public record PacketSyncTendonAnchor(int entityId, boolean set, BlockPos pos, Direction face) implements CustomPacketPayload {
	public static final Type<PacketSyncTendonAnchor> TYPE = new Type<>(Hemomancy.rloc("sync_tendon_anchor"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncTendonAnchor> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> {
				buffer.writeVarInt(packet.entityId());
				buffer.writeBoolean(packet.set());
				if (packet.set()) {
					buffer.writeBlockPos(packet.pos());
					buffer.writeEnum(packet.face());
				}
			},
			buffer -> {
				int id = buffer.readVarInt();
				return buffer.readBoolean()
						? new PacketSyncTendonAnchor(id, true, buffer.readBlockPos(), buffer.readEnum(Direction.class))
						: PacketSyncTendonAnchor.loose(id);
			});

	public static PacketSyncTendonAnchor loose(int entityId) {
		return new PacketSyncTendonAnchor(entityId, false, BlockPos.ZERO, Direction.UP);
	}

	public static PacketSyncTendonAnchor of(Player player, TendonAnchor anchor) {
		return anchor.isSet() ? new PacketSyncTendonAnchor(player.getId(), true, anchor.pos(), anchor.face())
				: loose(player.getId());
	}

	public static void handle(PacketSyncTendonAnchor packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player().level().getEntity(packet.entityId()) instanceof Player player)) return;
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			if (packet.set()) anchor.set(packet.pos(), packet.face(), player.level().dimension());
			else anchor.clear();
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

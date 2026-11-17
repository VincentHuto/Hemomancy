package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server to the owner and the players tracking them: whether a player clings by Venous Cling, to which face and
 * block, and whether they wear Hematic Cleats (core-loop Track 9). The owner's client moves them along the face; every
 * client draws the pose and the rooting veins.
 */
public record PacketSyncCling(int entityId, ClingRules.Surface surface, Direction face, BlockPos anchor, boolean cleats)
		implements CustomPacketPayload {
	public static final Type<PacketSyncCling> TYPE = new Type<>(Hemomancy.rloc("sync_cling"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncCling> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> {
				buffer.writeVarInt(packet.entityId());
				buffer.writeEnum(packet.surface());
				if (packet.surface() != ClingRules.Surface.NONE) {
					buffer.writeEnum(packet.face());
					buffer.writeBlockPos(packet.anchor());
					buffer.writeBoolean(packet.cleats());
				}
			},
			buffer -> {
				int id = buffer.readVarInt();
				ClingRules.Surface surface = buffer.readEnum(ClingRules.Surface.class);
				return surface == ClingRules.Surface.NONE ? loose(id)
						: new PacketSyncCling(id, surface, buffer.readEnum(Direction.class), buffer.readBlockPos(),
								buffer.readBoolean());
			});

	public static PacketSyncCling loose(int entityId) {
		return new PacketSyncCling(entityId, ClingRules.Surface.NONE, Direction.NORTH, BlockPos.ZERO, false);
	}

	public static PacketSyncCling of(Player player, ClingState state) {
		return state.isClinging()
				? new PacketSyncCling(player.getId(), state.surface(), state.face(), state.anchor(), state.cleats())
				: loose(player.getId());
	}

	public static void handle(PacketSyncCling packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player().level().getEntity(packet.entityId()) instanceof Player player)) return;
			ClingState state = HemoCapabilityAccess.getCling(player);
			if (packet.surface() == ClingRules.Surface.NONE) state.clear();
			else state.set(packet.surface(), packet.face(), packet.anchor(), packet.cleats());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

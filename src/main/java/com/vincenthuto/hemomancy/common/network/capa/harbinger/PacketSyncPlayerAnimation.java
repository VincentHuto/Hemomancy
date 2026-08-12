package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.PlayerAnimationClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Starts or stops a keyed player presentation on all tracking clients. */
public record PacketSyncPlayerAnimation(int entityId, PlayerAnimationKind kind,
		boolean active, InteractionHand hand) implements CustomPacketPayload {
	public static final Type<PacketSyncPlayerAnimation> TYPE =
			new Type<>(Hemomancy.rloc("sync_player_animation"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncPlayerAnimation> STREAM_CODEC =
			StreamCodec.of(PacketSyncPlayerAnimation::encode, PacketSyncPlayerAnimation::decode);

	private static void encode(FriendlyByteBuf buffer, PacketSyncPlayerAnimation packet) {
		buffer.writeVarInt(packet.entityId);
		buffer.writeEnum(packet.kind);
		buffer.writeBoolean(packet.active);
		buffer.writeEnum(packet.hand);
	}

	private static PacketSyncPlayerAnimation decode(FriendlyByteBuf buffer) {
		return new PacketSyncPlayerAnimation(buffer.readVarInt(), buffer.readEnum(PlayerAnimationKind.class),
				buffer.readBoolean(), buffer.readEnum(InteractionHand.class));
	}

	public static void handle(PacketSyncPlayerAnimation packet, IPayloadContext context) {
		context.enqueueWork(() -> PlayerAnimationClientState.set(packet.entityId, packet.kind,
				packet.active, packet.hand));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

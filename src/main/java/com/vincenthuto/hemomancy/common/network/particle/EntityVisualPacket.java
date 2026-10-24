package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Marks an entity as showing a timed visual on clients that track it. */
public record EntityVisualPacket(Kind kind, int entityId, int durationTicks) implements CustomPacketPayload {
	public enum Kind {
		/** Silent Severance's Monolithic Dislocation shell. */
		MONOLITHIC_DISLOCATION,
		/** Crimson fire burning on the entity. */
		CRIMSON_FIRE
	}

	// MOD_ID is a compile-time constant, so building the id this way keeps the codec usable without loading
	// Hemomancy (VisualPayloadCodecTest).
	public static final Type<EntityVisualPacket> TYPE = new Type<>(Hemomancy.rloc("entity_visual"));
	public static final StreamCodec<FriendlyByteBuf, EntityVisualPacket> STREAM_CODEC =
			StreamCodec.of(EntityVisualPacket::encode, EntityVisualPacket::decode);

	private static void encode(FriendlyByteBuf buffer, EntityVisualPacket packet) {
		buffer.writeEnum(packet.kind());
		buffer.writeVarInt(packet.entityId());
		buffer.writeVarInt(packet.durationTicks());
	}

	private static EntityVisualPacket decode(FriendlyByteBuf buffer) {
		return new EntityVisualPacket(buffer.readEnum(Kind.class), buffer.readVarInt(), buffer.readVarInt());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

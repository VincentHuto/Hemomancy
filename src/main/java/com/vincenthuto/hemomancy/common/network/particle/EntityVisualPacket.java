package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.CrimsonFireClientState;
import com.vincenthuto.hemomancy.client.data.MonolithicDislocationClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Marks an entity as showing a timed visual on clients that track it. */
public record EntityVisualPacket(Kind kind, int entityId, int durationTicks) implements CustomPacketPayload {
	public enum Kind {
		/** Silent Severance's Monolithic Dislocation shell. */
		MONOLITHIC_DISLOCATION,
		/** Crimson fire burning on the entity. */
		CRIMSON_FIRE
	}

	public static final Type<EntityVisualPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "entity_visual"));
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

	public static void handle(EntityVisualPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			switch (packet.kind()) {
				case MONOLITHIC_DISLOCATION ->
						MonolithicDislocationClientState.markActive(packet.entityId(), packet.durationTicks());
				case CRIMSON_FIRE -> CrimsonFireClientState.markActive(packet.entityId(), packet.durationTicks());
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

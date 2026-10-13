package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

/** Server floor definitions for remote clients; an integrated client already shares the server's registry. */
public record CardinalRiteFloorSyncPacket(Map<ResourceLocation, String> definitions) implements CustomPacketPayload {
	public CardinalRiteFloorSyncPacket { definitions = Map.copyOf(definitions); }
	public static final Type<CardinalRiteFloorSyncPacket> TYPE = new Type<>(Hemomancy.rloc("cardinal_rite_floor_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CardinalRiteFloorSyncPacket> STREAM_CODEC =
			StreamCodec.of(CardinalRiteFloorSyncPacket::encode, CardinalRiteFloorSyncPacket::decode);

	private static void encode(RegistryFriendlyByteBuf buf, CardinalRiteFloorSyncPacket packet) {
		buf.writeVarInt(packet.definitions.size());
		packet.definitions.forEach((id, json) -> {
			buf.writeResourceLocation(id);
			buf.writeUtf(json, CardinalRiteFloorRegistry.MAX_SYNCED_JSON_LENGTH);
		});
	}

	private static CardinalRiteFloorSyncPacket decode(RegistryFriendlyByteBuf buf) {
		int count = buf.readVarInt();
		if (count < 0 || count > CardinalRiteFloorRegistry.MAX_SYNCED_FLOORS) {
			throw new IllegalArgumentException("Too many Cardinal Rite floors");
		}
		var entries = new HashMap<ResourceLocation, String>();
		for (int i = 0; i < count; i++) {
			entries.put(buf.readResourceLocation(), buf.readUtf(CardinalRiteFloorRegistry.MAX_SYNCED_JSON_LENGTH));
		}
		return new CardinalRiteFloorSyncPacket(entries);
	}

	public static void handle(CardinalRiteFloorSyncPacket packet, IPayloadContext context) {
		if (context.connection().isMemoryConnection()) return;
		context.enqueueWork(() -> CardinalRiteFloorRegistry.receiveSync(packet.definitions));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

package com.vincenthuto.hemomancy.common.network.dialogue;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vincenthuto.hemomancy.client.data.NpcProgressionMarkerClientState;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncNpcProgressionMarkers(Map<Integer, DialogueAttention> markers)
		implements CustomPacketPayload {
	public static final Type<PacketSyncNpcProgressionMarkers> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath("hemomancy", "sync_npc_progression_markers"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncNpcProgressionMarkers> STREAM_CODEC =
			StreamCodec.of(PacketSyncNpcProgressionMarkers::encode, PacketSyncNpcProgressionMarkers::decode);

	public PacketSyncNpcProgressionMarkers {
		markers = Map.copyOf(markers);
	}

	private static void encode(FriendlyByteBuf buffer, PacketSyncNpcProgressionMarkers packet) {
		buffer.writeVarInt(packet.markers.size());
		packet.markers.forEach((entityId, attention) -> {
			buffer.writeVarInt(entityId);
			buffer.writeVarInt(attention.ordinal());
		});
	}

	private static PacketSyncNpcProgressionMarkers decode(FriendlyByteBuf buffer) {
		int count = buffer.readVarInt();
		if (count < 0 || count > 256) throw new IllegalArgumentException("Invalid NPC progression marker count: " + count);
		Map<Integer, DialogueAttention> markers = new LinkedHashMap<>(count);
		for (int i = 0; i < count; i++) {
			markers.put(buffer.readVarInt(), DialogueAttention.fromOrdinal(buffer.readVarInt()));
		}
		return new PacketSyncNpcProgressionMarkers(markers);
	}

	public static void handle(PacketSyncNpcProgressionMarkers packet, IPayloadContext context) {
		context.enqueueWork(() -> NpcProgressionMarkerClientState.replace(packet.markers));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

package com.vincenthuto.hemomancy.common.network;

import java.util.HashMap;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.data.DatapackSyncChannels;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** R-6: one channel of server data definitions for remote clients; the channel sets the size limits. */
public record DatapackSyncPacket(ResourceLocation channel, Map<ResourceLocation, String> definitions)
		implements CustomPacketPayload {
	public DatapackSyncPacket {
		definitions = Map.copyOf(definitions);
	}

	public static final Type<DatapackSyncPacket> TYPE = new Type<>(Hemomancy.rloc("datapack_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DatapackSyncPacket> STREAM_CODEC =
			StreamCodec.of(DatapackSyncPacket::encode, DatapackSyncPacket::decode);

	private static void encode(RegistryFriendlyByteBuf buf, DatapackSyncPacket packet) {
		var channel = DatapackSyncChannels.get(packet.channel);
		buf.writeResourceLocation(packet.channel);
		buf.writeVarInt(packet.definitions.size());
		packet.definitions.forEach((id, json) -> {
			buf.writeResourceLocation(id);
			buf.writeUtf(json, channel.maxJsonLength());
		});
	}

	private static DatapackSyncPacket decode(RegistryFriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		var channel = DatapackSyncChannels.get(id);
		int count = buf.readVarInt();
		if (count < 0 || count > channel.maxDefinitions()) {
			throw new IllegalArgumentException("Too many " + id + " definitions");
		}
		var entries = new HashMap<ResourceLocation, String>();
		for (int i = 0; i < count; i++) {
			entries.put(buf.readResourceLocation(), buf.readUtf(channel.maxJsonLength()));
		}
		return new DatapackSyncPacket(id, entries);
	}

	public static void handle(DatapackSyncPacket packet, IPayloadContext context) {
		boolean integrated = context.connection().isMemoryConnection();
		context.enqueueWork(() -> DatapackSyncChannels.receive(packet.channel, packet.definitions, integrated));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

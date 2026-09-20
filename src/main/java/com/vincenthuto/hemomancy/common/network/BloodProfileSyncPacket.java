package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.*;

public record BloodProfileSyncPacket(Map<ResourceLocation, String> definitions) implements CustomPacketPayload {
    public BloodProfileSyncPacket { definitions = Map.copyOf(definitions); }
    public static final Type<BloodProfileSyncPacket> TYPE = new Type<>(Hemomancy.rloc("blood_profile_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodProfileSyncPacket> STREAM_CODEC =
        StreamCodec.of(BloodProfileSyncPacket::encode, BloodProfileSyncPacket::decode);
    private static void encode(RegistryFriendlyByteBuf buf, BloodProfileSyncPacket packet) {
        buf.writeVarInt(packet.definitions.size());
        packet.definitions.forEach((id, json) -> { buf.writeResourceLocation(id); buf.writeUtf(json, BloodProfileData.MAX_JSON_LENGTH); });
    }
    private static BloodProfileSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > BloodProfileData.MAX_DEFINITIONS) throw new IllegalArgumentException("Too many blood profiles");
        var entries = new HashMap<ResourceLocation, String>();
        for (int i = 0; i < count; i++) entries.put(buf.readResourceLocation(), buf.readUtf(BloodProfileData.MAX_JSON_LENGTH));
        return new BloodProfileSyncPacket(entries);
    }
    public static void handle(BloodProfileSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> BloodProfileData.receive(packet.definitions));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}


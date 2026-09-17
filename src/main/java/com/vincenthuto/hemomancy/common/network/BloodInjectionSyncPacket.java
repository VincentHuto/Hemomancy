package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.*;

public record BloodInjectionSyncPacket(Map<ResourceLocation, String> definitions) implements CustomPacketPayload {
    public BloodInjectionSyncPacket { definitions = Map.copyOf(definitions); }
    public static final Type<BloodInjectionSyncPacket> TYPE = new Type<>(Hemomancy.rloc("blood_injection_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodInjectionSyncPacket> STREAM_CODEC =
        StreamCodec.of(BloodInjectionSyncPacket::encode, BloodInjectionSyncPacket::decode);
    private static void encode(RegistryFriendlyByteBuf buf, BloodInjectionSyncPacket packet) {
        buf.writeVarInt(packet.definitions.size());
        packet.definitions.forEach((id, json) -> { buf.writeResourceLocation(id); buf.writeUtf(json, BloodInjectionData.MAX_JSON_LENGTH); });
    }
    private static BloodInjectionSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > BloodInjectionData.MAX_DEFINITIONS) throw new IllegalArgumentException("Too many blood responses");
        var entries = new HashMap<ResourceLocation, String>();
        for (int i = 0; i < count; i++) entries.put(buf.readResourceLocation(), buf.readUtf(BloodInjectionData.MAX_JSON_LENGTH));
        return new BloodInjectionSyncPacket(entries);
    }
    public static void handle(BloodInjectionSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> BloodInjectionData.receive(packet.definitions));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}


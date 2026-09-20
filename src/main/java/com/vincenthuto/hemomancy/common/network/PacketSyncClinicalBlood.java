package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncClinicalBlood(CompoundTag data) implements CustomPacketPayload {
    public static final Type<PacketSyncClinicalBlood> TYPE = new Type<>(Hemomancy.rloc("clinical_blood"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncClinicalBlood> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.data), buf -> new PacketSyncClinicalBlood(buf.readNbt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(PacketSyncClinicalBlood packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.data != null) HemoCapabilityAccess.clinicalBlood(context.player())
                    .deserializeNBT(context.player().registryAccess(), packet.data);
        });
    }
}

package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketAntecedentResearch(CompoundTag data) implements CustomPacketPayload {
    public static final Type<PacketAntecedentResearch> TYPE = new Type<>(Hemomancy.rloc("antecedent_research"));
    public static final StreamCodec<FriendlyByteBuf,PacketAntecedentResearch> STREAM_CODEC = StreamCodec.of(
            (b,p)->b.writeNbt(p.data),b->new PacketAntecedentResearch(b.readNbt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(PacketAntecedentResearch packet, IPayloadContext context) {
        context.enqueueWork(()-> { if(packet.data != null) HemoCapabilityAccess.antecedent(context.player()).deserializeNBT(context.player().registryAccess(),packet.data); });
    }
}

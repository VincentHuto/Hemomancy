package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Presentation only; the existing cast packet remains the sole gameplay request. */
public record CastingChargePacket(String manipulation,int held) implements CustomPacketPayload {
    public static final Type<CastingChargePacket> TYPE = new Type<>(Hemomancy.rloc("casting_charge"));
    public static final StreamCodec<FriendlyByteBuf,CastingChargePacket> STREAM_CODEC = StreamCodec.of(
            (b,p) -> { b.writeUtf(p.manipulation,128); b.writeVarInt(p.held); },
            b -> new CastingChargePacket(b.readUtf(128),b.readVarInt()));
    public static void handle(CastingChargePacket packet,IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer player) CastingAnimationManager.charge(player,packet.manipulation,packet.held);
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

package com.vincenthuto.hemomancy.common.network.axonal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AxonalInputPacket(long token, int direction, boolean precise) implements CustomPacketPayload {
    public static final Type<AxonalInputPacket> TYPE = new Type<>(Hemomancy.rloc("axonal_input"));
    public static final StreamCodec<FriendlyByteBuf, AxonalInputPacket> STREAM_CODEC = StreamCodec.of(
            (b, p) -> { b.writeLong(p.token); b.writeByte(p.direction); b.writeBoolean(p.precise); },
            b -> new AxonalInputPacket(b.readLong(), b.readByte(), b.readBoolean()));
    public static void handle(AxonalInputPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> { if (context.player() instanceof ServerPlayer p)
            AxonalTransductionManager.input(p, packet.token, packet.direction, packet.precise); });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

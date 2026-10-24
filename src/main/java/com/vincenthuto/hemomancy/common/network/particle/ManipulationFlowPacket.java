package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;

/** Cosmetic moving connections; the endpoints and lifetime use the existing tendril contract. */
public record ManipulationFlowPacket(boolean umbra, TendrilAnchor start, TendrilAnchor end,
        TendrilEffectConfig config) implements CustomPacketPayload {
    public static final Type<ManipulationFlowPacket> TYPE = new Type<>(
            Hemomancy.rloc("manipulation_flow"));
    public static final StreamCodec<FriendlyByteBuf, ManipulationFlowPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBoolean(p.umbra);
                TendrilAnchor.toBuffer(buf, p.start);
                TendrilAnchor.toBuffer(buf, p.end);
                p.config.toBuffer(buf);
            }, buf -> new ManipulationFlowPacket(buf.readBoolean(), TendrilAnchor.fromBuffer(buf),
                    TendrilAnchor.fromBuffer(buf), TendrilEffectConfig.fromBuffer(buf)));

    public static void send(ServerLevel level, boolean umbra, TendrilAnchor start, TendrilAnchor end,
            TendrilEffectConfig config) {
        var resolver = TendrilAnchor.forLevel(level);
        var from = start.resolve(resolver);
        var to = end.resolve(resolver);
        if (from.isEmpty() || to.isEmpty()) return;
        var center = from.get().lerp(to.get(), .5);
        PacketHandler.sendNear(level, center.x, center.y, center.z,
                64 + from.get().distanceTo(to.get()) * .5, new ManipulationFlowPacket(umbra, start, end, config));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

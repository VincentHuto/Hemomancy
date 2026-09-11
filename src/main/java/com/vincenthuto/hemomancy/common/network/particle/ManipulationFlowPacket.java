package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.client.render.world.LuxUmbraEffects;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Cosmetic moving connections; the endpoints and lifetime use the existing tendril contract. */
public record ManipulationFlowPacket(boolean umbra, TendrilAnchor start, TendrilAnchor end,
        TendrilEffectConfig config) implements CustomPacketPayload {
    public static final Type<ManipulationFlowPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("hemomancy", "manipulation_flow"));
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
        PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z,
                64 + from.get().distanceTo(to.get()) * .5, new ManipulationFlowPacket(umbra, start, end, config));
    }

    public static void handle(ManipulationFlowPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> LuxUmbraEffects.accept(packet));
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

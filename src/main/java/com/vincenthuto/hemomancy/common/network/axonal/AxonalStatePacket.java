package com.vincenthuto.hemomancy.common.network.axonal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.AxonalTransductionClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AxonalStatePacket(ResourceLocation dimension, int playerId, long token, boolean active, Vec3 position)
        implements CustomPacketPayload {
    public static final Type<AxonalStatePacket> TYPE = new Type<>(Hemomancy.rloc("axonal_state"));
    public static final StreamCodec<FriendlyByteBuf, AxonalStatePacket> STREAM_CODEC = StreamCodec.of(
            (b, p) -> { b.writeResourceLocation(p.dimension); b.writeVarInt(p.playerId); b.writeLong(p.token);
                b.writeBoolean(p.active); b.writeDouble(p.position.x); b.writeDouble(p.position.y); b.writeDouble(p.position.z); },
            b -> new AxonalStatePacket(b.readResourceLocation(), b.readVarInt(), b.readLong(), b.readBoolean(),
                    new Vec3(b.readDouble(), b.readDouble(), b.readDouble())));
    public static void handle(AxonalStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> AxonalTransductionClient.update(packet));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

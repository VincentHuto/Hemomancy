package com.vincenthuto.hemomancy.common.network.succession;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ResidentsSnapshotPacket(CompoundTag data) implements CustomPacketPayload {
    public static final Type<ResidentsSnapshotPacket> TYPE = new Type<>(Hemomancy.rloc("residents_snapshot"));
    public static final StreamCodec<FriendlyByteBuf, ResidentsSnapshotPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeNbt(msg.data), buf -> new ResidentsSnapshotPacket(buf.readNbt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

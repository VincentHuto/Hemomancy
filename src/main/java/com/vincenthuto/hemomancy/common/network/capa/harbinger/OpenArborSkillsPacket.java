package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenArborSkillsPacket(int skillId) implements CustomPacketPayload {
    public static final Type<OpenArborSkillsPacket> TYPE = new Type<>(Hemomancy.rloc("open_arbor_skills"));
    public static final StreamCodec<FriendlyByteBuf, OpenArborSkillsPacket> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> buf.writeVarInt(msg.skillId), buf -> new OpenArborSkillsPacket(buf.readVarInt()));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

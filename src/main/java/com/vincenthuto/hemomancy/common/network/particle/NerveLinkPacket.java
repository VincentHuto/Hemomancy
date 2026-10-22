package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Moving cosmetic endpoints use HutosLib's existing anchor contract. */
public record NerveLinkPacket(TendrilAnchor start,TendrilAnchor end,int ticks) implements CustomPacketPayload {
    public static final Type<NerveLinkPacket> TYPE=new Type<>(Hemomancy.rloc("nerve_link"));
    public static final StreamCodec<FriendlyByteBuf,NerveLinkPacket> STREAM_CODEC=StreamCodec.of((buf,p)->{
        TendrilAnchor.toBuffer(buf,p.start);TendrilAnchor.toBuffer(buf,p.end);buf.writeVarInt(p.ticks);
    },buf->new NerveLinkPacket(TendrilAnchor.fromBuffer(buf),TendrilAnchor.fromBuffer(buf),buf.readVarInt()));
    public NerveLinkPacket { ticks=Math.clamp(ticks,0,100); }
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}

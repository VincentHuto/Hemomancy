package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.BloodFlowEffects;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Cosmetic connections; a stop packet retires the owner's matching style. */
public record BloodFlowPacket(long key,int owner,Style style,TendrilAnchor start,TendrilAnchor end,
        TendrilEffectConfig config,boolean stop) implements CustomPacketPayload {
    public enum Style { LIVING, TEAR, EXTRACTION, COMMUNION, LIGNUM }
    public static final Type<BloodFlowPacket> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,"blood_flow"));
    public static final StreamCodec<FriendlyByteBuf,BloodFlowPacket> STREAM_CODEC=StreamCodec.of((buf,p)->{
        buf.writeLong(p.key);buf.writeVarInt(p.owner);buf.writeEnum(p.style);
        TendrilAnchor.toBuffer(buf,p.start);TendrilAnchor.toBuffer(buf,p.end);p.config.toBuffer(buf);buf.writeBoolean(p.stop);
    },buf->new BloodFlowPacket(buf.readLong(),buf.readVarInt(),buf.readEnum(Style.class),
            TendrilAnchor.fromBuffer(buf),TendrilAnchor.fromBuffer(buf),TendrilEffectConfig.fromBuffer(buf),buf.readBoolean()));
    public static void handle(BloodFlowPacket packet,IPayloadContext context){context.enqueueWork(()->BloodFlowEffects.accept(packet));}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}

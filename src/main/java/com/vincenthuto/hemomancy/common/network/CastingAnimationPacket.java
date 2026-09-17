package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public record CastingAnimationPacket(int entityId, UUID playerId, String manipulation, long sequence,
        EnumBloodTendency school, EnumBloodTendency secondary, EnumManipulationRank rank,
        CastPurpose purpose, CastStyle style, CastPhase phase, long started, long updated,
        int held, int required, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<CastingAnimationPacket> TYPE = new Type<>(Hemomancy.rloc("casting_animation"));
    public static final StreamCodec<FriendlyByteBuf,CastingAnimationPacket> STREAM_CODEC =
            StreamCodec.of(CastingAnimationPacket::encode,CastingAnimationPacket::decode);
    private static void encode(FriendlyByteBuf b,CastingAnimationPacket p) {
        b.writeVarInt(p.entityId); b.writeUUID(p.playerId); b.writeUtf(p.manipulation,128); b.writeVarLong(p.sequence);
        b.writeEnum(p.school); b.writeBoolean(p.secondary!=null); if(p.secondary!=null)b.writeEnum(p.secondary);
        b.writeEnum(p.rank); b.writeEnum(p.purpose); b.writeEnum(p.style); b.writeEnum(p.phase);
        b.writeLong(p.started); b.writeLong(p.updated); b.writeVarInt(p.held); b.writeVarInt(p.required); b.writeEnum(p.hand);
    }
    private static CastingAnimationPacket decode(FriendlyByteBuf b) {
        return new CastingAnimationPacket(b.readVarInt(),b.readUUID(),b.readUtf(128),b.readVarLong(),
                b.readEnum(EnumBloodTendency.class),b.readBoolean()?b.readEnum(EnumBloodTendency.class):null,
                b.readEnum(EnumManipulationRank.class),b.readEnum(CastPurpose.class),b.readEnum(CastStyle.class),
                b.readEnum(CastPhase.class),b.readLong(),b.readLong(),b.readVarInt(),b.readVarInt(),b.readEnum(InteractionHand.class));
    }
    public static void handle(CastingAnimationPacket packet,IPayloadContext context) {
        context.enqueueWork(() -> com.vincenthuto.hemomancy.client.player.CastingAnimationClientState.accept(packet));
    }
    public CastingAnimationPacket at(CastPhase next,long start,long now,int ticks) {
        return new CastingAnimationPacket(entityId,playerId,manipulation,sequence,school,secondary,rank,purpose,style,
                next,start,now,ticks,required,hand);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

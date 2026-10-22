package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import java.util.UUID;

/** Harbinger packets carry school/rank/purpose; Unstained (Still Art) packets carry stage/ending and leave those null. */
public record CastingAnimationPacket(int entityId, UUID playerId, String manipulation, long sequence,
        EnumBloodTendency school, EnumBloodTendency secondary, EnumManipulationRank rank,
        CastPurpose purpose, CastStyle style, CastPhase phase, long started, long updated,
        int held, int required, InteractionHand hand,
        CastLane lane, EnumClarityStage stage, StillArtEnding ending) implements CustomPacketPayload {
    // MOD_ID is a constant, so this never initialises the mod class (keeps the codec usable in JVM tests).
    public static final Type<CastingAnimationPacket> TYPE = new Type<>(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,"casting_animation"));
    public static final StreamCodec<FriendlyByteBuf,CastingAnimationPacket> STREAM_CODEC =
            StreamCodec.of(CastingAnimationPacket::encode,CastingAnimationPacket::decode);
    public static CastingAnimationPacket harbinger(int entityId,UUID playerId,String manipulation,long sequence,
            EnumBloodTendency school,EnumBloodTendency secondary,EnumManipulationRank rank,CastPurpose purpose,CastStyle style,
            CastPhase phase,long started,long updated,int held,int required,InteractionHand hand) {
        return new CastingAnimationPacket(entityId,playerId,manipulation,sequence,school,secondary,rank,purpose,style,
                phase,started,updated,held,required,hand,CastLane.HARBINGER,null,null);
    }
    public static CastingAnimationPacket stillArt(int entityId,UUID playerId,String art,long sequence,
            EnumClarityStage stage,StillArtEnding ending,CastPhase phase,long started,long updated,InteractionHand hand) {
        return new CastingAnimationPacket(entityId,playerId,art,sequence,null,null,null,null,CastStyle.SCHOOL,
                phase,started,updated,0,0,hand,CastLane.UNSTAINED,stage,ending);
    }
    private static <E extends Enum<E>> void writeNullable(FriendlyByteBuf b,E value) {
        b.writeBoolean(value!=null); if(value!=null)b.writeEnum(value);
    }
    private static <E extends Enum<E>> E readNullable(FriendlyByteBuf b,Class<E> type) {
        return b.readBoolean()?b.readEnum(type):null;
    }
    private static void encode(FriendlyByteBuf b,CastingAnimationPacket p) {
        b.writeVarInt(p.entityId); b.writeUUID(p.playerId); b.writeUtf(p.manipulation,128); b.writeVarLong(p.sequence);
        b.writeEnum(p.lane);
        writeNullable(b,p.school); writeNullable(b,p.secondary); writeNullable(b,p.rank); writeNullable(b,p.purpose);
        writeNullable(b,p.stage); writeNullable(b,p.ending);
        b.writeEnum(p.style); b.writeEnum(p.phase);
        b.writeLong(p.started); b.writeLong(p.updated); b.writeVarInt(p.held); b.writeVarInt(p.required); b.writeEnum(p.hand);
    }
    private static CastingAnimationPacket decode(FriendlyByteBuf b) {
        int entityId=b.readVarInt(); UUID playerId=b.readUUID(); String manipulation=b.readUtf(128); long sequence=b.readVarLong();
        CastLane lane=b.readEnum(CastLane.class);
        var school=readNullable(b,EnumBloodTendency.class); var secondary=readNullable(b,EnumBloodTendency.class);
        var rank=readNullable(b,EnumManipulationRank.class); var purpose=readNullable(b,CastPurpose.class);
        var stage=readNullable(b,EnumClarityStage.class); var ending=readNullable(b,StillArtEnding.class);
        return new CastingAnimationPacket(entityId,playerId,manipulation,sequence,school,secondary,rank,purpose,
                b.readEnum(CastStyle.class),b.readEnum(CastPhase.class),b.readLong(),b.readLong(),b.readVarInt(),b.readVarInt(),
                b.readEnum(InteractionHand.class),lane,stage,ending);
    }
    public CastingAnimationPacket at(CastPhase next,long start,long now,int ticks) {
        return new CastingAnimationPacket(entityId,playerId,manipulation,sequence,school,secondary,rank,purpose,style,
                next,start,now,ticks,required,hand,lane,stage,ending);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

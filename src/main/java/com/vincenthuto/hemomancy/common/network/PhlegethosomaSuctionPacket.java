package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public record PhlegethosomaSuctionPacket(int entityId,UUID source,long attack,double force) implements CustomPacketPayload {
    public static final Type<PhlegethosomaSuctionPacket> TYPE=new Type<>(Hemomancy.rloc("phlegethosoma_suction"));
    public static final StreamCodec<FriendlyByteBuf,PhlegethosomaSuctionPacket> STREAM_CODEC=StreamCodec.of(
        (b,p)->{b.writeVarInt(p.entityId);b.writeUUID(p.source);b.writeLong(p.attack);b.writeDouble(p.force);},
        b->new PhlegethosomaSuctionPacket(b.readVarInt(),b.readUUID(),b.readLong(),b.readDouble()));
    @Override public Type<? extends CustomPacketPayload> type() {return TYPE;}
    public static void handle(PhlegethosomaSuctionPacket p,IPayloadContext context) {
        context.enqueueWork(()->{
            var player=context.player();
            if(!player.isAlive() || player.isCreative() || player.isSpectator() || !Double.isFinite(p.force) || p.force<=0 || p.force>.06)return;
            if(player.level().getEntity(p.entityId) instanceof PhlegethosomaEntity boss && boss.getUUID().equals(p.source)
                    && boss.attackEpoch()==p.attack && boss.isAlive())boss.applySuction(player,p.force);
        });
    }
}

package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AntecedentEffectPacket(int kind,BlockPos position) implements CustomPacketPayload {
    public static final Type<AntecedentEffectPacket> TYPE=new Type<>(Hemomancy.rloc("antecedent_effect"));
    public static final StreamCodec<FriendlyByteBuf,AntecedentEffectPacket> STREAM_CODEC=StreamCodec.of(
            (b,p)->{b.writeVarInt(p.kind);b.writeBlockPos(p.position);},b->new AntecedentEffectPacket(b.readVarInt(),b.readBlockPos()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    /** On the server it records the epilogue; on the client it plays the effect (ClientPayloadHandlers). */
    public static void handle(AntecedentEffectPacket packet,IPayloadContext context) {
        if(!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
            ClientPayloadHandlers.dispatch(packet,context);
            return;
        }
        context.enqueueWork(()->{
            var research=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.antecedent(player);
            if(packet.kind==4 && research.complete() && ProgressionGates.hasDegree(player, 7))
                com.vincenthuto.hemomancy.common.antecedent.AntecedentKnowledge.record(player,com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.EPILOGUE);
        });
    }
}

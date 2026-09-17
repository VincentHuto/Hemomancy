package com.vincenthuto.hemomancy.common.network;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record ClairaudiographSoundPacket(ResourceLocation dimension,BlockPos pos,long token,boolean stop,boolean preview,String sound,float pitch) implements CustomPacketPayload {
    public static final Type<ClairaudiographSoundPacket> TYPE=new Type<>(Hemomancy.rloc("clairaudiograph_sound"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ClairaudiographSoundPacket> STREAM_CODEC=StreamCodec.of(
        (b,p)->{b.writeResourceLocation(p.dimension);b.writeBlockPos(p.pos);b.writeLong(p.token);b.writeBoolean(p.stop);b.writeBoolean(p.preview);b.writeUtf(p.sound);b.writeFloat(p.pitch);},
        b->new ClairaudiographSoundPacket(b.readResourceLocation(),b.readBlockPos(),b.readLong(),b.readBoolean(),b.readBoolean(),b.readUtf(),b.readFloat()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void handle(ClairaudiographSoundPacket p,IPayloadContext c){c.enqueueWork(()->com.vincenthuto.hemomancy.client.sound.ClairaudiographSounds.accept(p));}
}

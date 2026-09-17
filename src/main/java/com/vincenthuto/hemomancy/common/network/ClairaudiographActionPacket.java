package com.vincenthuto.hemomancy.common.network;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.tile.functional.ClairaudiographMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record ClairaudiographActionPacket(int menu,long version,int action,int index) implements CustomPacketPayload {
    public static final Type<ClairaudiographActionPacket> TYPE=new Type<>(Hemomancy.rloc("clairaudiograph_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ClairaudiographActionPacket> STREAM_CODEC=StreamCodec.of(
        (b,p)->{b.writeVarInt(p.menu);b.writeLong(p.version);b.writeVarInt(p.action);b.writeVarInt(p.index);},
        b->new ClairaudiographActionPacket(b.readVarInt(),b.readLong(),b.readVarInt(),b.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void handle(ClairaudiographActionPacket p,IPayloadContext c){c.enqueueWork(()->{if(c.player().containerMenu instanceof ClairaudiographMenu m && m.containerId==p.menu)m.action(c.player(),p.version,p.action,p.index);});}
}

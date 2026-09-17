package com.vincenthuto.hemomancy.common.network;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording;
import com.vincenthuto.hemomancy.common.menu.tile.functional.ClairaudiographMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.List;
public record ClairaudiographChoicesPacket(int menu,long version,String source,List<ClairaudiographRecording> choices) implements CustomPacketPayload {
    public static final Type<ClairaudiographChoicesPacket> TYPE=new Type<>(Hemomancy.rloc("clairaudiograph_choices"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ClairaudiographChoicesPacket> STREAM_CODEC=StreamCodec.of(
        (b,p)->{b.writeVarInt(p.menu);b.writeLong(p.version);b.writeUtf(p.source);b.writeCollection(p.choices,(buf,r)->ClairaudiographRecording.STREAM_CODEC.encode(buf,r));},
        b->new ClairaudiographChoicesPacket(b.readVarInt(),b.readLong(),b.readUtf(),b.readCollection(net.minecraft.network.FriendlyByteBuf.limitValue(java.util.ArrayList::new,64),buf->ClairaudiographRecording.STREAM_CODEC.decode(buf))));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void handle(ClairaudiographChoicesPacket p,IPayloadContext c){c.enqueueWork(()->{if(c.player().containerMenu instanceof ClairaudiographMenu m && m.containerId==p.menu){m.version=p.version;m.source=p.source;m.choices=List.copyOf(p.choices);}});}
}

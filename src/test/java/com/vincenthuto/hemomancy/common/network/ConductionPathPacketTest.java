package com.vincenthuto.hemomancy.common.network;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class ConductionPathPacketTest {
    @Test void actualNodesAndRemovalRoundTrip() {
        for (var nodes:List.of(List.<BlockPos>of(),IntStream.range(0,256).mapToObj(i->new BlockPos(i%12,64,i/12)).toList())) {
            var packet=new ConductionPathPacket(UUID.randomUUID(),nodes,312);
            var buffer=new FriendlyByteBuf(Unpooled.buffer());
            try {
                ConductionPathPacket.STREAM_CODEC.encode(buffer,packet);
                assertEquals(packet,ConductionPathPacket.STREAM_CODEC.decode(buffer));
                assertEquals(0,buffer.readableBytes());
            } finally { buffer.release(); }
        }
    }

    @Test void rejectsUnboundedPacketBeforeAllocatingNodes() {
        for (int count:new int[]{-1,257,Integer.MAX_VALUE}) {
            var buffer=new FriendlyByteBuf(Unpooled.buffer());
            try {
                buffer.writeUUID(UUID.randomUUID()); buffer.writeLong(30); buffer.writeVarInt(count);
                assertThrows(DecoderException.class,()->ConductionPathPacket.STREAM_CODEC.decode(buffer));
            } finally { buffer.release(); }
        }
    }
}

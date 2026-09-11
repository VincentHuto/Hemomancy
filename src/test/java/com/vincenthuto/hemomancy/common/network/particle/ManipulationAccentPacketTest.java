package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManipulationAccentPacketTest {
    @Test void schoolAndSaintAccentsRoundTripForActivationAndHits() {
        for(var school:EnumBloodTendency.values())for(boolean impact:new boolean[]{false,true}) {
            var packet=new ManipulationAccentPacket(school,impact?EnumBloodTendency.LUX:null,
                    new Vec3(2.5,100.75,-3),new Vec3(.1,.2,.3),impact?24:-1,impact);
            var buffer=new FriendlyByteBuf(Unpooled.buffer());
            try {
                ManipulationAccentPacket.STREAM_CODEC.encode(buffer,packet);
                assertEquals(packet,ManipulationAccentPacket.STREAM_CODEC.decode(buffer));
                assertEquals(0,buffer.readableBytes());
            } finally {buffer.release();}
        }
    }
}

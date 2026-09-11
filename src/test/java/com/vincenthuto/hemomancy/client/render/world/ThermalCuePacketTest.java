package com.vincenthuto.hemomancy.client.render.world;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ThermalCuePacketTest {
    @Test void thermalStatesRetainAttachmentsEndpointsAndExplicitRemovalOnTheWire() {
        for(Form form:new Form[]{Form.IGNITION,Form.CRYOGENIC_PULSE,Form.CRUOR_FORM,Form.CRUOR_BREAK,Form.RIMEBOUND,
                Form.FROZEN_VEINS,Form.HOUR_BREAK,Form.CRUOR_SURFACE,Form.FLAME_CONJURE,Form.FROST_CONJURE,Form.FROST_ADVANCE})for(int ticks:new int[]{0,27,580}) {
            var packet=new ManipulationVisualPacket(form,77,new Vec3(1,3,9),new Vec3(2,4,8),.75f,ticks,63);
            var buf=new FriendlyByteBuf(Unpooled.buffer());
            try {ManipulationVisualPacket.STREAM_CODEC.encode(buf,packet);assertEquals(packet,ManipulationVisualPacket.STREAM_CODEC.decode(buf));}
            finally {buf.release();}
        }
    }
}


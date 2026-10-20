package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CastingAnimationPacketTest {
    private static CastingAnimationPacket roundTrip(CastingAnimationPacket packet) {
        var buffer=new FriendlyByteBuf(Unpooled.buffer());
        try {
            CastingAnimationPacket.STREAM_CODEC.encode(buffer,packet);
            var decoded=CastingAnimationPacket.STREAM_CODEC.decode(buffer);
            assertEquals(0,buffer.readableBytes());
            return decoded;
        } finally { buffer.release(); }
    }
    @Test void stillArtPacketRoundTripsWithNullSchool() {
        for(var stage:EnumClarityStage.values())for(var ending:StillArtEnding.values()) {
            var packet=CastingAnimationPacket.stillArt(7,UUID.randomUUID(),"silver_rebuke",19,stage,ending,
                    CastPhase.RELEASE,500,500,InteractionHand.MAIN_HAND);
            var decoded=roundTrip(packet);
            assertEquals(packet,decoded);
            assertEquals(CastLane.UNSTAINED,decoded.lane());
            assertNull(decoded.school());
            assertNull(decoded.rank());
            assertNull(decoded.purpose());
            assertEquals(stage,decoded.stage());
            assertEquals(ending,decoded.ending());
            assertEquals(CastStyle.SCHOOL,decoded.style());
            assertEquals(0,decoded.held());
            assertEquals(0,decoded.required());
        }
    }
    @Test void harbingerPacketStillRoundTrips() {
        var id=UUID.randomUUID();
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values())for(var phase:CastPhase.values()) {
            var packet=CastingAnimationPacket.harbinger(42,id,"blood_shot",73,school,EnumBloodTendency.MORTEM,rank,
                    CastPurpose.AREA,CastStyle.SCHOOL,phase,12345,12350,5,40,InteractionHand.OFF_HAND);
            var decoded=roundTrip(packet);
            assertEquals(packet,decoded);
            assertEquals(CastLane.HARBINGER,decoded.lane());
            assertEquals(school,decoded.school());
            assertEquals(EnumBloodTendency.MORTEM,decoded.secondary());
            assertEquals(rank,decoded.rank());
            assertEquals(CastPurpose.AREA,decoded.purpose());
            assertEquals(phase,decoded.phase());
            assertEquals(12345,decoded.started());
            assertEquals(12350,decoded.updated());
            assertEquals(5,decoded.held());
            assertEquals(40,decoded.required());
            assertEquals(InteractionHand.OFF_HAND,decoded.hand());
            assertNull(decoded.stage());
            assertNull(decoded.ending());
        }
    }
    @Test void atCarriesLaneStageAndEnding() {
        var packet=CastingAnimationPacket.stillArt(7,UUID.randomUUID(),"memory_shear",3,EnumClarityStage.VIGILANT,
                StillArtEnding.SHEAR,CastPhase.RELEASE,10,10,InteractionHand.MAIN_HAND);
        var next=packet.at(CastPhase.CANCEL,20,21,0);
        assertEquals(CastLane.UNSTAINED,next.lane());
        assertEquals(EnumClarityStage.VIGILANT,next.stage());
        assertEquals(StillArtEnding.SHEAR,next.ending());
        assertEquals(CastPhase.CANCEL,next.phase());
        assertEquals(3,next.sequence());
    }
}
